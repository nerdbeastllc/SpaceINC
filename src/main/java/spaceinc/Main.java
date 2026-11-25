package spaceinc;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {
    private long window;
    private int winWidth = 1440;
    private int winHeight = 900;
    private int fbWidth = 1440;
    private int fbHeight = 900;

    private boolean rightDragging = false;
    private double lastMouseX, lastMouseY;

    private final IsometricCamera camera = new IsometricCamera();
    private final StarSystem starSystem = new StarSystem();
    private final Ship ship = new Ship();
    private AsteroidField asteroidField;
    private TargetInfo selectedTarget = null;
    private float currentElapsed = 0f;

    private int focusIndex = 0; // 0=Ship, 1=Star, 2..10=Planets

    private String getFocusName() {
        if (focusIndex == 0) return "PLAYER CORVETTE";
        if (focusIndex == 1) return "CENTRAL STAR";
        return starSystem.planets.get(focusIndex - 2).name;
    }

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        initWindow();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        GLFWErrorCallback cb = glfwSetErrorCallback(null);
        if (cb != null) cb.free();
    }

    private void initWindow() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(winWidth, winHeight, "SpaceINC — 3D Isometric Space PoC (Java 25)", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // VSync
        GL.createCapabilities();

        updateSizes();

        glfwSetFramebufferSizeCallback(window, (w, width, height) -> {
            fbWidth = Math.max(1, width);
            fbHeight = Math.max(1, height);
        });

        glfwSetWindowSizeCallback(window, (w, width, height) -> {
            winWidth = Math.max(1, width);
            winHeight = Math.max(1, height);
        });

        glfwSetScrollCallback(window, (w, xoff, yoff) -> camera.zoom((float) yoff));

        glfwSetMouseButtonCallback(window, (w, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                rightDragging = (action == GLFW_PRESS);
                double[] mx = new double[1];
                double[] my = new double[1];
                glfwGetCursorPos(window, mx, my);
                lastMouseX = mx[0];
                lastMouseY = my[0];
            } else if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
                double[] mx = new double[1];
                double[] my = new double[1];
                glfwGetCursorPos(window, mx, my);
                Vector3f rayOrigin = new Vector3f();
                Vector3f rayDir = new Vector3f();
                camera.screenToRay((float) mx[0], (float) my[0], winWidth, winHeight, rayOrigin, rayDir);

                TargetInfo celestialHit = starSystem.pickCelestial(rayOrigin, rayDir);
                int astIdx = (asteroidField != null)
                    ? asteroidField.pick(rayOrigin, rayDir, currentElapsed, 1.0f)
                    : -1;
                TargetInfo astHit = (astIdx >= 0) ? asteroidField.getTargetInfo(astIdx, () -> currentElapsed) : null;

                if (celestialHit != null && astHit != null) {
                    float dCel = celestialHit.getPosition().distanceSquared(rayOrigin);
                    float dAst = astHit.getPosition().distanceSquared(rayOrigin);
                    selectedTarget = (dCel <= dAst) ? celestialHit : astHit;
                } else {
                    selectedTarget = (celestialHit != null) ? celestialHit : astHit;
                }
            }
        });

        glfwSetCursorPosCallback(window, (w, xpos, ypos) -> {
            if (rightDragging) {
                float dx = (float) (xpos - lastMouseX);
                float dy = (float) (ypos - lastMouseY);
                camera.orbit(-dx * 0.0065f, dy * 0.0055f);
            }
            lastMouseX = xpos;
            lastMouseY = ypos;
        });

        glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (action != GLFW_PRESS) return;
            StarSystem.StarType[] types = StarSystem.StarType.values();
            if (key == GLFW_KEY_ESCAPE) {
                glfwSetWindowShouldClose(window, true);
            } else if (key >= GLFW_KEY_1 && key <= GLFW_KEY_9) {
                int idx = key - GLFW_KEY_1;
                if (idx < types.length) starSystem.starType = types[idx];
            } else if (key == GLFW_KEY_0) {
                starSystem.starType = types[9];
            } else if (key == GLFW_KEY_MINUS) {
                starSystem.starType = types[10];
            } else if (key == GLFW_KEY_EQUAL) {
                starSystem.starType = types[11];
            } else if (key == GLFW_KEY_LEFT_BRACKET) {
                int idx = (starSystem.starType.ordinal() - 1 + types.length) % types.length;
                starSystem.starType = types[idx];
            } else if (key == GLFW_KEY_RIGHT_BRACKET) {
                int idx = (starSystem.starType.ordinal() + 1) % types.length;
                starSystem.starType = types[idx];
            } else if (key == GLFW_KEY_TAB) {
                int total = 2 + starSystem.planets.size();
                if ((mods & GLFW_MOD_SHIFT) != 0) {
                    focusIndex = (focusIndex - 1 + total) % total;
                } else {
                    focusIndex = (focusIndex + 1) % total;
                }
            }
        });

        glfwShowWindow(window);
    }

    private void updateSizes() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer fw = stack.mallocInt(1);
            IntBuffer fh = stack.mallocInt(1);
            glfwGetFramebufferSize(window, fw, fh);
            fbWidth = Math.max(1, fw.get(0));
            fbHeight = Math.max(1, fh.get(0));

            IntBuffer ww = stack.mallocInt(1);
            IntBuffer wh = stack.mallocInt(1);
            glfwGetWindowSize(window, ww, wh);
            winWidth = Math.max(1, ww.get(0));
            winHeight = Math.max(1, wh.get(0));
        }
    }

    private void loop() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        Mesh sphereMesh = Mesh.createSphere(80, 140);
        Mesh quadMesh = Mesh.createQuad();
        Mesh shipMesh = Mesh.createSpaceship();
        Mesh waypointRing = Mesh.createOrbitCircle(36, 1.4f);
        Mesh prominenceMesh = Mesh.createProminences(26, 32);
        asteroidField = new AsteroidField(1150, 1500);
        starSystem.initMeshes();
        ship.initTrail();

        Shader skyboxShader = new Shader("shaders/skybox.vert", "shaders/skybox.frag");
        Shader starShader = new Shader("shaders/star.vert", "shaders/star.frag");
        Shader prominenceShader = new Shader("shaders/prominence.vert", "shaders/prominence.frag");
        Shader coronaShader = new Shader("shaders/corona.vert", "shaders/corona.frag");
        Shader planetShader = new Shader("shaders/planet.vert", "shaders/planet.frag");
        Shader ringShader = new Shader("shaders/ring.vert", "shaders/ring.frag");
        Shader asteroidShader = new Shader("shaders/asteroid.vert", "shaders/asteroid.frag");
        Shader shipShader = new Shader("shaders/ship.vert", "shaders/ship.frag");
        Shader lineShader = new Shader("shaders/line.vert", "shaders/line.frag");

        BloomRenderer bloom = new BloomRenderer(fbWidth, fbHeight, quadMesh);
        HudOverlay hud = new HudOverlay();

        double lastTime = glfwGetTime();
        double fpsTimer = lastTime;
        int frames = 0;
        int currentFps = 60;

        while (!glfwWindowShouldClose(window)) {
            double now = glfwGetTime();
            float dt = (float) Math.min(0.05, now - lastTime);
            lastTime = now;
            float elapsed = (float) now;
            currentElapsed = elapsed;

            frames++;
            if (now - fpsTimer >= 0.5) {
                currentFps = (int) Math.round(frames / (now - fpsTimer));
                frames = 0;
                fpsTimer = now;
            }

            glfwPollEvents();
            bloom.resize(fbWidth, fbHeight);

            // Camera keyboard orbit
            if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) camera.orbit(-1.5f * dt, 0f);
            if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) camera.orbit(1.5f * dt, 0f);

            // Ship manual thrust relative to isometric camera
            Vector3f moveInput = new Vector3f();
            Vector3f camFwd = camera.getForwardXZ();
            Vector3f camRight = camera.getRightXZ();
            if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
                moveInput.add(camFwd);
            }
            if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
                moveInput.sub(camFwd);
            }
            if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) {
                moveInput.add(camRight);
            }
            if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) {
                moveInput.sub(camRight);
            }
            boolean boost = glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;

            starSystem.update(dt);
            ship.update(dt, moveInput, boost);

            Vector3f focusTarget;
            if (focusIndex == 0) {
                focusTarget = ship.position;
            } else if (focusIndex == 1) {
                focusTarget = starSystem.starPos;
            } else {
                focusTarget = starSystem.planets.get(focusIndex - 2).barycenter;
            }
            camera.update(dt, focusTarget, (float) fbWidth / fbHeight);

            Matrix4f view = camera.getView();
            Matrix4f proj = camera.getProjection();
            Matrix4f invVP = new Matrix4f(proj).mul(view).invert();

            // ================= HDR SCENE PASS =================
            bloom.beginScene();

            // 1. Deep-Space Skybox
            glDepthFunc(GL_LEQUAL);
            glDepthMask(false);
            skyboxShader.use();
            skyboxShader.setMat4("uInvVP", invVP);
            skyboxShader.setVec3("uCameraPos", camera.getEyePos());
            quadMesh.draw();
            glDepthMask(true);
            glDepthFunc(GL_LESS);

            // 2. Tactical Orbit Rings & Waypoint
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            lineShader.use();
            lineShader.setMat4("uView", view);
            lineShader.setMat4("uProjection", proj);
            lineShader.setInt("uUseAlphaAttr", 0);
            lineShader.setVec3("uColor", 0.22f, 0.45f, 0.72f);
            lineShader.setFloat("uOpacity", 0.11f);
            for (StarSystem.Planet p : starSystem.planets) {
                p.orbitMesh.draw();
            }

            // Moon orbit rings for the focused planet (or all if zoomed in close)
            if (focusIndex >= 2) {
                StarSystem.Planet fp = starSystem.planets.get(focusIndex - 2);
                lineShader.setVec3("uColor", 0.35f, 0.68f, 0.85f);
                lineShader.setFloat("uOpacity", 0.14f);
                for (StarSystem.Moon m : fp.moons) {
                    Matrix4f mOrbView = new Matrix4f(view)
                        .translate(fp.isBinaryPluto ? fp.barycenter : fp.position)
                        .rotateZ(fp.isBinaryPluto ? 0f : fp.axialTilt)
                        .rotateX(fp.isBinaryPluto ? 0f : m.inclination);
                    lineShader.setMat4("uView", mOrbView);
                    m.orbitMesh.draw();
                }
            }

            if (selectedTarget != null) {
                Vector3f tPos = selectedTarget.getPosition();
                float ringScale = Math.max(1.2f, selectedTarget.radius * 1.45f) / 1.4f;
                Matrix4f tgtView = new Matrix4f(view).translate(tPos).scale(ringScale);
                lineShader.setMat4("uView", tgtView);
                lineShader.setVec3("uColor", 0.30f, 0.90f, 1.00f);
                lineShader.setFloat("uOpacity", 0.90f);
                waypointRing.draw();
            }
            glDisable(GL_BLEND);

            // 3. Central Star
            StarSystem.StarType st = starSystem.starType;
            starShader.use();
            Matrix4f starModel = new Matrix4f()
                .translate(starSystem.starPos)
                .rotateY(elapsed * 0.03f)
                .scale(st.radius);
            starShader.setMat4("uModel", starModel);
            starShader.setMat4("uView", view);
            starShader.setMat4("uProjection", proj);
            starShader.setFloat("uTime", elapsed);
            starShader.setVec3("uCameraPos", camera.getEyePos());
            starShader.setVec3("uHotColor", st.hotColor);
            starShader.setVec3("uMidColor", st.midColor);
            starShader.setVec3("uCoolColor", st.coolColor);
            starShader.setFloat("uGranulationScale", st.granulationScale);
            starShader.setFloat("uHdrIntensity", st.hdrIntensity);
            sphereMesh.draw();

            // 3b. 3D Solar Prominence Loops & Flare Arches
            glDisable(GL_CULL_FACE);
            glDepthMask(false);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            prominenceShader.use();
            prominenceShader.setMat4("uModel", starModel);
            prominenceShader.setMat4("uView", view);
            prominenceShader.setMat4("uProjection", proj);
            prominenceShader.setFloat("uTime", elapsed);
            prominenceShader.setVec3("uHotColor", st.hotColor);
            prominenceShader.setVec3("uMidColor", st.midColor);
            prominenceShader.setFloat("uHdrIntensity", st.hdrIntensity);
            prominenceMesh.draw();
            glDisable(GL_BLEND);
            glDepthMask(true);
            glEnable(GL_CULL_FACE);

            // 4. All Planets, Moons, & Planetary Rings
            planetShader.use();
            planetShader.setMat4("uView", view);
            planetShader.setMat4("uProjection", proj);
            planetShader.setFloat("uTime", elapsed);
            planetShader.setVec3("uStarPos", starSystem.starPos);
            planetShader.setVec3("uStarLightColor", st.lightColor);
            planetShader.setVec3("uCameraPos", camera.getEyePos());

            for (StarSystem.Planet p : starSystem.planets) {
                planetShader.use();
                planetShader.setInt("uPlanetType", p.planetType);
                planetShader.setVec3("uColorA", p.colorA);
                planetShader.setVec3("uColorB", p.colorB);
                planetShader.setVec3("uColorC", p.colorC);
                planetShader.setMat4("uModel", new Matrix4f()
                    .translate(p.position)
                    .rotateZ(p.axialTilt)
                    .rotateY(elapsed * p.spinSpeed)
                    .scale(p.radius));
                sphereMesh.draw();

                for (StarSystem.Moon m : p.moons) {
                    planetShader.setInt("uPlanetType", m.planetType);
                    planetShader.setVec3("uColorA", m.colorA);
                    planetShader.setVec3("uColorB", m.colorB);
                    planetShader.setVec3("uColorC", m.colorC);
                    planetShader.setMat4("uModel", new Matrix4f()
                        .translate(m.position)
                        .rotateY(elapsed * 0.05f)
                        .scale(m.radius));
                    sphereMesh.draw();
                }

                if (p.hasRings && p.ringMesh != null) {
                    glDisable(GL_CULL_FACE);
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                    ringShader.use();
                    ringShader.setMat4("uModel", new Matrix4f()
                        .translate(p.position)
                        .rotateZ(p.axialTilt)
                        .scale(p.radius));
                    ringShader.setMat4("uView", view);
                    ringShader.setMat4("uProjection", proj);
                    ringShader.setVec3("uStarPos", starSystem.starPos);
                    ringShader.setVec3("uStarLightColor", st.lightColor);
                    ringShader.setVec3("uPlanetPos", p.position);
                    ringShader.setFloat("uPlanetRadius", p.radius);
                    p.ringMesh.draw();
                    glDisable(GL_BLEND);
                    glEnable(GL_CULL_FACE);
                }
            }

            // 4b. Instanced Asteroid Belt & Oort Cloud (single draw call)
            asteroidShader.use();
            asteroidShader.setVec3("uCameraPos", camera.getEyePos());
            asteroidField.draw(asteroidShader, view, proj, elapsed, starSystem);

            // 5. Player Starship & Engine Trail
            shipShader.use();
            shipShader.setMat4("uModel", ship.getModelMatrix());
            shipShader.setMat4("uView", view);
            shipShader.setMat4("uProjection", proj);
            shipShader.setVec3("uStarPos", starSystem.starPos);
            shipShader.setVec3("uStarLightColor", st.lightColor);
            shipShader.setVec3("uCameraPos", camera.getEyePos());
            shipShader.setFloat("uThrust", ship.thrustLevel);
            shipMesh.draw();

            lineShader.setInt("uUseAlphaAttr", 1);
            lineShader.setFloat("uOpacity", 0.85f);
            ship.drawTrail(lineShader, view, proj);

            // 6. Star Volumetric Corona & Radial Rays (additive billboard)
            glDisable(GL_CULL_FACE);
            glDepthMask(false);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            float billboardRadius = st.radius * 3.4f;
            coronaShader.use();
            coronaShader.setMat4("uView", view);
            coronaShader.setMat4("uProjection", proj);
            coronaShader.setVec3("uStarPos", starSystem.starPos);
            coronaShader.setVec3("uCameraPos", camera.getEyePos());
            coronaShader.setFloat("uStarRadius", st.radius);
            coronaShader.setFloat("uBillboardRadius", billboardRadius);
            coronaShader.setFloat("uTime", elapsed);
            coronaShader.setVec3("uCoronaColor", st.coronaColor);
            coronaShader.setVec3("uHotColor", st.hotColor);
            coronaShader.setFloat("uHdrIntensity", st.hdrIntensity);
            quadMesh.draw();
            glDisable(GL_BLEND);
            glDepthMask(true);
            glEnable(GL_CULL_FACE);

            // ================= HDR BLOOM + TONE MAPPING =================
            bloom.endSceneAndComposite(1.15f);

            // ================= TACTICAL 2D HUD =================
            hud.render(winWidth, winHeight, starSystem, ship, getFocusName(), selectedTarget, currentFps);

            glfwSwapBuffers(window);
        }

        // Cleanup
        sphereMesh.dispose();
        quadMesh.dispose();
        shipMesh.dispose();
        waypointRing.dispose();
        prominenceMesh.dispose();
        asteroidField.dispose();
        starSystem.dispose();
        ship.dispose();

        skyboxShader.dispose();
        starShader.dispose();
        prominenceShader.dispose();
        coronaShader.dispose();
        planetShader.dispose();
        ringShader.dispose();
        asteroidShader.dispose();
        shipShader.dispose();
        lineShader.dispose();
        bloom.dispose();
        hud.dispose();
    }
}
