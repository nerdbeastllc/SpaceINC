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

    private int focusIndex = 0; // 0=Ship, 1=Star, 2=Terran, 3=Gas Giant
    private static final String[] FOCUS_NAMES = {
        "PLAYER CORVETTE", "CENTRAL STAR", "TERRAN WORLD", "RINGED GAS GIANT"
    };

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
                Vector3f hit = camera.screenToPlane((float) mx[0], (float) my[0], winWidth, winHeight, 0f);
                if (hit != null) {
                    ship.waypoint = hit;
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
                focusIndex = (focusIndex + 1) % 4;
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
        Mesh ringMesh = Mesh.createRing(128, 1.35f, 2.35f);
        Mesh shipMesh = Mesh.createSpaceship();
        Mesh terranOrbit = Mesh.createOrbitCircle(180, starSystem.terranOrbitRadius);
        Mesh giantOrbit = Mesh.createOrbitCircle(220, starSystem.giantOrbitRadius);
        Mesh waypointRing = Mesh.createOrbitCircle(36, 1.4f);
        Mesh prominenceMesh = Mesh.createProminences(26, 32);
        ship.initTrail();

        Shader skyboxShader = new Shader("shaders/skybox.vert", "shaders/skybox.frag");
        Shader starShader = new Shader("shaders/star.vert", "shaders/star.frag");
        Shader prominenceShader = new Shader("shaders/prominence.vert", "shaders/prominence.frag");
        Shader coronaShader = new Shader("shaders/corona.vert", "shaders/corona.frag");
        Shader planetShader = new Shader("shaders/planet.vert", "shaders/planet.frag");
        Shader ringShader = new Shader("shaders/ring.vert", "shaders/ring.frag");
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

            Vector3f focusTarget = switch (focusIndex) {
                case 1 -> starSystem.starPos;
                case 2 -> starSystem.terranPos;
                case 3 -> starSystem.giantPos;
                default -> ship.position;
            };
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
            lineShader.setFloat("uOpacity", 0.28f);
            terranOrbit.draw();
            giantOrbit.draw();

            if (ship.waypoint != null) {
                Matrix4f wpView = new Matrix4f(view).translate(ship.waypoint);
                lineShader.setMat4("uView", wpView);
                lineShader.setVec3("uColor", 0.25f, 0.95f, 0.72f);
                lineShader.setFloat("uOpacity", 0.85f);
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

            // 4. Planets & Moon
            planetShader.use();
            planetShader.setMat4("uView", view);
            planetShader.setMat4("uProjection", proj);
            planetShader.setFloat("uTime", elapsed);
            planetShader.setVec3("uStarPos", starSystem.starPos);
            planetShader.setVec3("uStarLightColor", st.lightColor);
            planetShader.setVec3("uCameraPos", camera.getEyePos());

            // Terran World
            planetShader.setInt("uPlanetType", 0);
            planetShader.setMat4("uModel", new Matrix4f()
                .translate(starSystem.terranPos)
                .rotateY(elapsed * 0.12f)
                .scale(starSystem.terranRadius));
            sphereMesh.draw();

            // Terran Moon
            planetShader.setInt("uPlanetType", 2);
            planetShader.setMat4("uModel", new Matrix4f()
                .translate(starSystem.moonPos)
                .rotateY(elapsed * 0.08f)
                .scale(starSystem.moonRadius));
            sphereMesh.draw();

            // Gas Giant
            planetShader.setInt("uPlanetType", 1);
            planetShader.setMat4("uModel", new Matrix4f()
                .translate(starSystem.giantPos)
                .rotateZ(0.24f)
                .rotateY(elapsed * 0.06f)
                .scale(starSystem.giantRadius));
            sphereMesh.draw();

            // Gas Giant Rings (double-sided, alpha blended)
            glDisable(GL_CULL_FACE);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            ringShader.use();
            ringShader.setMat4("uModel", new Matrix4f()
                .translate(starSystem.giantPos)
                .rotateZ(0.24f)
                .scale(starSystem.giantRadius));
            ringShader.setMat4("uView", view);
            ringShader.setMat4("uProjection", proj);
            ringShader.setVec3("uStarPos", starSystem.starPos);
            ringShader.setVec3("uStarLightColor", st.lightColor);
            ringShader.setVec3("uPlanetPos", starSystem.giantPos);
            ringShader.setFloat("uPlanetRadius", starSystem.giantRadius);
            ringMesh.draw();
            glDisable(GL_BLEND);
            glEnable(GL_CULL_FACE);

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
            hud.render(winWidth, winHeight, starSystem, ship, FOCUS_NAMES[focusIndex], currentFps);

            glfwSwapBuffers(window);
        }

        // Cleanup
        sphereMesh.dispose();
        quadMesh.dispose();
        ringMesh.dispose();
        shipMesh.dispose();
        terranOrbit.dispose();
        giantOrbit.dispose();
        waypointRing.dispose();
        prominenceMesh.dispose();
        ship.dispose();

        skyboxShader.dispose();
        starShader.dispose();
        prominenceShader.dispose();
        coronaShader.dispose();
        planetShader.dispose();
        ringShader.dispose();
        shipShader.dispose();
        lineShader.dispose();
        bloom.dispose();
        hud.dispose();
    }
}
