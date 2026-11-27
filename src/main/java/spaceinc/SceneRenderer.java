package spaceinc;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Owns 3D meshes, shaders, HDR bloom, and per-pass rendering.
 */
public class SceneRenderer {
    private final Mesh sphereMesh = Mesh.createSphere(80, 140);
    private final Mesh quadMesh = Mesh.createQuad();
    private final Mesh shipMesh = Mesh.createSpaceship();
    private final Mesh stationMesh = Mesh.createStation();
    private final Mesh waypointRing = Mesh.createOrbitCircle(36, 1.4f);
    private final Mesh prominenceMesh = Mesh.createProminences(26, 32);

    private final Shader skyboxShader = new Shader("shaders/skybox.vert", "shaders/skybox.frag");
    private final Shader starShader = new Shader("shaders/star.vert", "shaders/star.frag");
    private final Shader prominenceShader = new Shader("shaders/prominence.vert", "shaders/prominence.frag");
    private final Shader coronaShader = new Shader("shaders/corona.vert", "shaders/corona.frag");
    private final Shader planetShader = new Shader("shaders/planet.vert", "shaders/planet.frag");
    private final Shader ringShader = new Shader("shaders/ring.vert", "shaders/ring.frag");
    private final Shader asteroidShader = new Shader("shaders/asteroid.vert", "shaders/asteroid.frag");
    private final Shader shipShader = new Shader("shaders/ship.vert", "shaders/ship.frag");
    private final Shader lineShader = new Shader("shaders/line.vert", "shaders/line.frag");

    private final BloomRenderer bloom;

    public SceneRenderer(int fbWidth, int fbHeight) {
        this.bloom = new BloomRenderer(fbWidth, fbHeight, quadMesh);
    }

    public void renderFrame(GameState s, HudOverlay hud, int fps) {
        bloom.resize(s.fbWidth, s.fbHeight);
        bloom.beginScene();

        renderSkybox(s);
        renderOrbitsAndReticles(s);
        renderStarAndProminences(s);
        renderPlanetsAndMoons(s);
        s.asteroidField.draw(asteroidShader, s);
        renderStations(s);
        renderShip(s);
        renderCorona(s);

        bloom.endSceneAndComposite(1.15f);
        hud.render(s, fps);
        if (s.galaxyMapOpen && !s.pauseMenuOpen) {
            renderGalaxyMapStars(s);
        }
    }

    private void renderSkybox(GameState s) {
        Matrix4f invVP = new Matrix4f(s.camera.getProjection()).mul(s.camera.getView()).invert();
        glDepthFunc(GL_LEQUAL);
        glDepthMask(false);
        skyboxShader.use();
        skyboxShader.setMat4("uInvVP", invVP);
        skyboxShader.setVec3("uCameraPos", s.camera.getEyePos());
        quadMesh.draw();
        glDepthMask(true);
        glDepthFunc(GL_LESS);
    }

    private void renderOrbitsAndReticles(GameState s) {
        Matrix4f view = s.camera.getView(), proj = s.camera.getProjection();
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        lineShader.use();
        lineShader.setMat4("uView", view);
        lineShader.setMat4("uProjection", proj);
        lineShader.setInt("uUseAlphaAttr", 0);
        lineShader.setVec3("uColor", 0.22f, 0.45f, 0.72f);
        lineShader.setFloat("uOpacity", 0.11f);
        for (StarSystem.Planet p : s.starSystem.planets) p.orbitMesh.draw();

        if (s.focusIndex >= 2) renderFocusedMoonOrbits(s, view);
        renderJumpGates(s, view);
        renderTargetReticle(s, view);
        glDisable(GL_BLEND);
    }

    private void renderFocusedMoonOrbits(GameState s, Matrix4f view) {
        StarSystem.Planet fp = s.starSystem.planets.get(s.focusIndex - 2);
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

    private void renderJumpGates(GameState s, Matrix4f view) {
        for (StarSystem.JumpGate g : s.starSystem.jumpGates) {
            lineShader.setMat4("uView", new Matrix4f(view).translate(g.position).scale(g.radius / 1.4f));
            if (g.isFrontier) lineShader.setVec3("uColor", 1.00f, 0.55f, 0.20f);
            else lineShader.setVec3("uColor", 0.35f, 0.85f, 1.00f);
            lineShader.setFloat("uOpacity", 0.90f);
            waypointRing.draw();

            lineShader.setMat4("uView", new Matrix4f(view).translate(g.position)
                .rotateX(1.1f).rotateZ(s.currentElapsed * 0.4f).scale(g.radius * 0.72f / 1.4f));
            waypointRing.draw();
        }
    }

    private void renderTargetReticle(GameState s, Matrix4f view) {
        if (s.selectedTarget == null) return;
        float ringScale = Math.max(1.2f, s.selectedTarget.radius * 1.45f) / 1.4f;
        lineShader.setMat4("uView", new Matrix4f(view).translate(s.selectedTarget.getPosition()).scale(ringScale));
        lineShader.setVec3("uColor", 0.30f, 0.90f, 1.00f);
        lineShader.setFloat("uOpacity", 0.90f);
        waypointRing.draw();
    }

    private void renderStarAndProminences(GameState s) {
        StarSystem.StarType st = s.starSystem.starType;
        Matrix4f starModel = new Matrix4f().translate(s.starSystem.starPos).rotateY(s.currentElapsed * 0.03f).scale(st.radius);
        starShader.use();
        starShader.setMat4("uModel", starModel);
        starShader.setMat4("uView", s.camera.getView());
        starShader.setMat4("uProjection", s.camera.getProjection());
        starShader.setFloat("uTime", s.currentElapsed);
        starShader.setVec3("uCameraPos", s.camera.getEyePos());
        starShader.setVec3("uHotColor", st.hotColor);
        starShader.setVec3("uMidColor", st.midColor);
        starShader.setVec3("uCoolColor", st.coolColor);
        starShader.setFloat("uGranulationScale", st.granulationScale);
        starShader.setFloat("uHdrIntensity", st.hdrIntensity);
        sphereMesh.draw();

        glDisable(GL_CULL_FACE);
        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        prominenceShader.use();
        prominenceShader.setMat4("uModel", starModel);
        prominenceShader.setMat4("uView", s.camera.getView());
        prominenceShader.setMat4("uProjection", s.camera.getProjection());
        prominenceShader.setFloat("uTime", s.currentElapsed);
        prominenceShader.setVec3("uHotColor", st.hotColor);
        prominenceShader.setVec3("uMidColor", st.midColor);
        prominenceShader.setFloat("uHdrIntensity", st.hdrIntensity);
        prominenceMesh.draw();
        glDisable(GL_BLEND);
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
    }

    private void renderPlanetsAndMoons(GameState s) {
        StarSystem.StarType st = s.starSystem.starType;
        planetShader.use();
        planetShader.setMat4("uView", s.camera.getView());
        planetShader.setMat4("uProjection", s.camera.getProjection());
        planetShader.setFloat("uTime", s.currentElapsed);
        planetShader.setVec3("uStarPos", s.starSystem.starPos);
        planetShader.setVec3("uStarLightColor", st.lightColor);
        planetShader.setVec3("uCameraPos", s.camera.getEyePos());

        for (StarSystem.Planet p : s.starSystem.planets) {
            planetShader.use();
            planetShader.setInt("uPlanetType", p.planetType);
            planetShader.setVec3("uColorA", p.colorA);
            planetShader.setVec3("uColorB", p.colorB);
            planetShader.setVec3("uColorC", p.colorC);
            planetShader.setMat4("uModel", new Matrix4f().translate(p.position)
                .rotateZ(p.axialTilt).rotateY(s.currentElapsed * p.spinSpeed).scale(p.radius));
            sphereMesh.draw();

            for (StarSystem.Moon m : p.moons) {
                planetShader.setInt("uPlanetType", m.planetType);
                planetShader.setVec3("uColorA", m.colorA);
                planetShader.setVec3("uColorB", m.colorB);
                planetShader.setVec3("uColorC", m.colorC);
                planetShader.setMat4("uModel", new Matrix4f().translate(m.position)
                    .rotateY(s.currentElapsed * 0.05f).scale(m.radius));
                sphereMesh.draw();
            }
            if (p.hasRings && p.ringMesh != null) renderPlanetaryRings(s, p, st.lightColor);
        }
    }

    private void renderPlanetaryRings(GameState s, StarSystem.Planet p, Vector3f lightColor) {
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        ringShader.use();
        ringShader.setMat4("uModel", new Matrix4f().translate(p.position).rotateZ(p.axialTilt).scale(p.radius));
        ringShader.setMat4("uView", s.camera.getView());
        ringShader.setMat4("uProjection", s.camera.getProjection());
        ringShader.setVec3("uStarPos", s.starSystem.starPos);
        ringShader.setVec3("uStarLightColor", lightColor);
        ringShader.setVec3("uPlanetPos", p.position);
        ringShader.setFloat("uPlanetRadius", p.radius);
        p.ringMesh.draw();
        glDisable(GL_BLEND);
        glEnable(GL_CULL_FACE);
    }

    private void renderStations(GameState s) {
        shipShader.use();
        shipShader.setMat4("uView", s.camera.getView());
        shipShader.setMat4("uProjection", s.camera.getProjection());
        shipShader.setVec3("uStarPos", s.starSystem.starPos);
        shipShader.setVec3("uStarLightColor", s.starSystem.starType.lightColor);
        shipShader.setVec3("uCameraPos", s.camera.getEyePos());
        shipShader.setFloat("uThrust", 0.85f);
        for (StarSystem.MarketStation ms : s.starSystem.marketStations) {
            shipShader.setMat4("uModel", new Matrix4f().translate(ms.position).rotateY(s.currentElapsed * 0.12f).scale(1.1f));
            stationMesh.draw();
        }
    }

    private void renderShip(GameState s) {
        shipShader.use();
        shipShader.setMat4("uModel", s.ship.getModelMatrix());
        shipShader.setMat4("uView", s.camera.getView());
        shipShader.setMat4("uProjection", s.camera.getProjection());
        shipShader.setVec3("uStarPos", s.starSystem.starPos);
        shipShader.setVec3("uStarLightColor", s.starSystem.starType.lightColor);
        shipShader.setVec3("uCameraPos", s.camera.getEyePos());
        shipShader.setFloat("uThrust", s.ship.thrustLevel);
        shipMesh.draw();

        lineShader.setInt("uUseAlphaAttr", 1);
        lineShader.setFloat("uOpacity", 0.85f);
        s.ship.drawTrail(lineShader, s.camera.getView(), s.camera.getProjection());
    }

    private void renderCorona(GameState s) {
        StarSystem.StarType st = s.starSystem.starType;
        drawCoronaBillboard(st, s.starSystem.starPos, s.camera.getEyePos(), st.radius, st.radius * 3.4f,
            s.camera.getView(), s.camera.getProjection(), s.currentElapsed);
    }

    private void renderGalaxyMapStars(GameState s) {
        glClear(GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_SCISSOR_TEST);
        float sx = (float) s.fbWidth / s.winWidth, sy = (float) s.fbHeight / s.winHeight;
        float mw = 680f, mh = 430f, mx = (s.winWidth - mw) * 0.5f, my = (s.winHeight - mh) * 0.5f;
        glScissor(Math.round((mx + 2) * sx), Math.round((s.winHeight - (my + mh - 2)) * sy),
            Math.round((mw - 4) * sx), Math.round((mh - 64) * sy));

        Matrix4f mapProj = new Matrix4f().ortho(0f, s.winWidth, 0f, s.winHeight, -200f, 200f);
        Matrix4f mapView = new Matrix4f().identity();
        float mapCx = mx + mw * 0.5f + s.mapPanX, mapCy = my + mh * 0.54f + s.mapPanY;
        float scaleX = mw * 0.36f * s.mapZoom, scaleY = mh * 0.30f * s.mapZoom;
        float zoomStarScale = Math.max(0.6f, Math.min(2.2f, s.mapZoom));

        for (GalaxyMap.SystemNode node : s.galaxyMap.systems) {
            float nx = mapCx + node.mapX * scaleX;
            float ny = s.winHeight - (mapCy + node.mapY * scaleY);
            float mapRadius = (11.0f + node.starType.radius * 0.52f) * zoomStarScale;
            drawMiniMapStar(node.starType, new Vector3f(nx, ny, 0f), mapRadius, mapView, mapProj, s.currentElapsed);
        }
        glDisable(GL_SCISSOR_TEST);
    }

    private void drawMiniMapStar(StarSystem.StarType nst, Vector3f pos, float radius,
                                 Matrix4f mapView, Matrix4f mapProj, float elapsed) {
        Vector3f camPos = new Vector3f(pos.x, pos.y, 120f);
        starShader.use();
        starShader.setMat4("uModel", new Matrix4f().translate(pos).rotateY(elapsed * 0.25f).scale(radius));
        starShader.setMat4("uView", mapView);
        starShader.setMat4("uProjection", mapProj);
        starShader.setFloat("uTime", elapsed);
        starShader.setVec3("uCameraPos", camPos);
        starShader.setVec3("uHotColor", nst.hotColor);
        starShader.setVec3("uMidColor", nst.midColor);
        starShader.setVec3("uCoolColor", nst.coolColor);
        starShader.setFloat("uGranulationScale", nst.granulationScale);
        starShader.setFloat("uHdrIntensity", nst.hdrIntensity * 1.15f);
        sphereMesh.draw();
        drawCoronaBillboard(nst, pos, camPos, radius, radius * 2.6f, mapView, mapProj, elapsed);
    }

    private void drawCoronaBillboard(StarSystem.StarType st, Vector3f starPos, Vector3f camPos,
                                     float starRadius, float billboardRadius,
                                     Matrix4f view, Matrix4f proj, float elapsed) {
        glDisable(GL_CULL_FACE);
        glDepthMask(false);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        coronaShader.use();
        coronaShader.setMat4("uView", view);
        coronaShader.setMat4("uProjection", proj);
        coronaShader.setVec3("uStarPos", starPos);
        coronaShader.setVec3("uCameraPos", camPos);
        coronaShader.setFloat("uStarRadius", starRadius);
        coronaShader.setFloat("uBillboardRadius", billboardRadius);
        coronaShader.setFloat("uTime", elapsed);
        coronaShader.setVec3("uCoronaColor", st.coronaColor);
        coronaShader.setVec3("uHotColor", st.hotColor);
        coronaShader.setFloat("uHdrIntensity", st.hdrIntensity);
        quadMesh.draw();
        glDisable(GL_BLEND);
        glDepthMask(true);
        glEnable(GL_CULL_FACE);
    }

    public void dispose() {
        sphereMesh.dispose();
        quadMesh.dispose();
        shipMesh.dispose();
        stationMesh.dispose();
        waypointRing.dispose();
        prominenceMesh.dispose();
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
    }
}
