package spaceinc;

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
    private final GameState state = new GameState();
    private UiController ui;

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
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        configureWindowHints();
        window = glfwCreateWindow(state.winWidth, state.winHeight, "SpaceINC — 3D Isometric Space PoC (Java 25)", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create GLFW window");

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        GL.createCapabilities();
        updateSizes();

        ui = new UiController(state, window);
        registerCallbacks();
        glfwShowWindow(window);
    }

    private static void configureWindowHints() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
    }

    private void registerCallbacks() {
        glfwSetFramebufferSizeCallback(window, (w, width, height) -> {
            state.fbWidth = Math.max(1, width);
            state.fbHeight = Math.max(1, height);
        });
        glfwSetWindowSizeCallback(window, (w, width, height) -> {
            state.winWidth = Math.max(1, width);
            state.winHeight = Math.max(1, height);
        });
        glfwSetScrollCallback(window, (w, xoff, yoff) -> {
            double[] mx = new double[1], my = new double[1];
            glfwGetCursorPos(window, mx, my);
            ui.handleScroll(yoff, (float) mx[0], (float) my[0]);
        });
        glfwSetMouseButtonCallback(window, (w, button, action, mods) -> {
            double[] mx = new double[1], my = new double[1];
            glfwGetCursorPos(window, mx, my);
            if (button == GLFW_MOUSE_BUTTON_RIGHT) ui.handleRightButton(action, (float) mx[0], (float) my[0]);
            else if (button == GLFW_MOUSE_BUTTON_LEFT) ui.handleLeftButton(action, mods, (float) mx[0], (float) my[0]);
        });
        glfwSetCursorPosCallback(window, (w, xpos, ypos) -> ui.handleMouseMove((float) xpos, (float) ypos));
        glfwSetCharCallback(window, (w, codepoint) -> ui.handleCharInput(codepoint));
        glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) ui.handleKeyPress(key, mods);
        });
    }

    private void updateSizes() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer fw = stack.mallocInt(1), fh = stack.mallocInt(1);
            glfwGetFramebufferSize(window, fw, fh);
            state.fbWidth = Math.max(1, fw.get(0));
            state.fbHeight = Math.max(1, fh.get(0));

            IntBuffer ww = stack.mallocInt(1), wh = stack.mallocInt(1);
            glfwGetWindowSize(window, ww, wh);
            state.winWidth = Math.max(1, ww.get(0));
            state.winHeight = Math.max(1, wh.get(0));
        }
    }

    private void loop() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        state.asteroidField = new AsteroidField(1150, 1500);
        state.starSystem.loadSystem(state.galaxyMap.findById("SOL"), state.galaxyMap);
        state.asteroidField.setCoreFederation(true);
        state.ship.initTrail();

        SceneRenderer renderer = new SceneRenderer(state.fbWidth, state.fbHeight);
        HudOverlay hud = new HudOverlay();

        double lastTime = glfwGetTime(), fpsTimer = lastTime;
        int frames = 0, currentFps = 60;

        while (!glfwWindowShouldClose(window)) {
            double now = glfwGetTime();
            float dt = (float) Math.min(0.05, now - lastTime);
            lastTime = now;
            state.currentElapsed = (float) now;

            frames++;
            if (now - fpsTimer >= 0.5) {
                currentFps = (int) Math.round(frames / (now - fpsTimer));
                frames = 0;
                fpsTimer = now;
            }

            glfwPollEvents();
            updateSimulation(dt);
            renderer.renderFrame(state, hud, currentFps);
            glfwSwapBuffers(window);
        }

        renderer.dispose();
        hud.dispose();
        state.asteroidField.dispose();
        state.starSystem.dispose();
        state.ship.dispose();
    }

    private void updateSimulation(float dt) {
        if (state.statusTimer > 0f) {
            state.statusTimer -= dt;
            if (state.statusTimer <= 0f) state.statusMsg = "";
        }
        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) state.camera.orbit(-1.5f * dt, 0f);
        if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) state.camera.orbit(1.5f * dt, 0f);

        Vector3f moveInput = readShipMoveInput();
        boolean boost = glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;

        state.starSystem.update(dt);
        state.ship.update(dt, moveInput, boost);
        state.camera.update(dt, state.getFocusTarget(), (float) state.fbWidth / state.fbHeight);
    }

    private Vector3f readShipMoveInput() {
        Vector3f moveInput = new Vector3f();
        Vector3f camFwd = state.camera.getForwardXZ();
        Vector3f camRight = state.camera.getRightXZ();
        if (isKeyDown(GLFW_KEY_W) || isKeyDown(GLFW_KEY_UP)) moveInput.add(camFwd);
        if (isKeyDown(GLFW_KEY_S) || isKeyDown(GLFW_KEY_DOWN)) moveInput.sub(camFwd);
        if (isKeyDown(GLFW_KEY_D) || isKeyDown(GLFW_KEY_RIGHT)) moveInput.add(camRight);
        if (isKeyDown(GLFW_KEY_A) || isKeyDown(GLFW_KEY_LEFT)) moveInput.sub(camRight);
        return moveInput;
    }

    private boolean isKeyDown(int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }
}
