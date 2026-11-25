package spaceinc;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Lightweight crisp 2D tactical HUD with built-in 5x7 bitmap font.
 */
public class HudOverlay {
    private static final int MAX_VERTS = 16384;
    private final Shader shader;
    private final int vao;
    private final int vbo;
    private final FloatBuffer buffer;
    private int vertCount = 0;

    private static final Map<Character, int[]> FONT = new HashMap<>();
    static {
        // 5x7 bitmap font (5 bits per row, 7 rows)
        FONT.put('A', new int[]{0x0E,0x11,0x11,0x1F,0x11,0x11,0x11});
        FONT.put('B', new int[]{0x1E,0x11,0x11,0x1E,0x11,0x11,0x1E});
        FONT.put('C', new int[]{0x0E,0x11,0x10,0x10,0x10,0x11,0x0E});
        FONT.put('D', new int[]{0x1E,0x11,0x11,0x11,0x11,0x11,0x1E});
        FONT.put('E', new int[]{0x1F,0x10,0x10,0x1E,0x10,0x10,0x1F});
        FONT.put('F', new int[]{0x1F,0x10,0x10,0x1E,0x10,0x10,0x10});
        FONT.put('G', new int[]{0x0E,0x11,0x10,0x17,0x11,0x11,0x0F});
        FONT.put('H', new int[]{0x11,0x11,0x11,0x1F,0x11,0x11,0x11});
        FONT.put('I', new int[]{0x0E,0x04,0x04,0x04,0x04,0x04,0x0E});
        FONT.put('J', new int[]{0x07,0x02,0x02,0x02,0x02,0x12,0x0C});
        FONT.put('K', new int[]{0x11,0x12,0x14,0x18,0x14,0x12,0x11});
        FONT.put('L', new int[]{0x10,0x10,0x10,0x10,0x10,0x10,0x1F});
        FONT.put('M', new int[]{0x11,0x1B,0x15,0x15,0x11,0x11,0x11});
        FONT.put('N', new int[]{0x11,0x19,0x15,0x13,0x11,0x11,0x11});
        FONT.put('O', new int[]{0x0E,0x11,0x11,0x11,0x11,0x11,0x0E});
        FONT.put('P', new int[]{0x1E,0x11,0x11,0x1E,0x10,0x10,0x10});
        FONT.put('Q', new int[]{0x0E,0x11,0x11,0x11,0x15,0x12,0x0D});
        FONT.put('R', new int[]{0x1E,0x11,0x11,0x1E,0x14,0x12,0x11});
        FONT.put('S', new int[]{0x0F,0x10,0x10,0x0E,0x01,0x01,0x1E});
        FONT.put('T', new int[]{0x1F,0x04,0x04,0x04,0x04,0x04,0x04});
        FONT.put('U', new int[]{0x11,0x11,0x11,0x11,0x11,0x11,0x0E});
        FONT.put('V', new int[]{0x11,0x11,0x11,0x11,0x11,0x0A,0x04});
        FONT.put('W', new int[]{0x11,0x11,0x11,0x15,0x15,0x1B,0x11});
        FONT.put('X', new int[]{0x11,0x11,0x0A,0x04,0x0A,0x11,0x11});
        FONT.put('Y', new int[]{0x11,0x11,0x0A,0x04,0x04,0x04,0x04});
        FONT.put('Z', new int[]{0x1F,0x01,0x02,0x04,0x08,0x10,0x1F});
        FONT.put('0', new int[]{0x0E,0x11,0x13,0x15,0x19,0x11,0x0E});
        FONT.put('1', new int[]{0x04,0x0C,0x04,0x04,0x04,0x04,0x0E});
        FONT.put('2', new int[]{0x0E,0x11,0x01,0x06,0x08,0x10,0x1F});
        FONT.put('3', new int[]{0x1F,0x02,0x04,0x06,0x01,0x11,0x0E});
        FONT.put('4', new int[]{0x02,0x06,0x0A,0x12,0x1F,0x02,0x02});
        FONT.put('5', new int[]{0x1F,0x10,0x1E,0x01,0x01,0x11,0x0E});
        FONT.put('6', new int[]{0x06,0x08,0x10,0x1E,0x11,0x11,0x0E});
        FONT.put('7', new int[]{0x1F,0x01,0x02,0x04,0x08,0x08,0x08});
        FONT.put('8', new int[]{0x0E,0x11,0x11,0x0E,0x11,0x11,0x0E});
        FONT.put('9', new int[]{0x0E,0x11,0x11,0x0F,0x01,0x02,0x0C});
        FONT.put(':', new int[]{0x00,0x04,0x04,0x00,0x04,0x04,0x00});
        FONT.put('.', new int[]{0x00,0x00,0x00,0x00,0x00,0x0C,0x0C});
        FONT.put('-', new int[]{0x00,0x00,0x00,0x1F,0x00,0x00,0x00});
        FONT.put('/', new int[]{0x01,0x02,0x02,0x04,0x08,0x08,0x10});
        FONT.put('[', new int[]{0x0E,0x08,0x08,0x08,0x08,0x08,0x0E});
        FONT.put(']', new int[]{0x0E,0x02,0x02,0x02,0x02,0x02,0x0E});
        FONT.put('(', new int[]{0x02,0x04,0x08,0x08,0x08,0x04,0x02});
        FONT.put(')', new int[]{0x08,0x04,0x02,0x02,0x02,0x04,0x08});
        FONT.put('&', new int[]{0x0C,0x12,0x14,0x08,0x15,0x12,0x0D});
        FONT.put(' ', new int[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00});
    }

    public HudOverlay() {
        shader = new Shader("shaders/hud.vert", "shaders/hud.frag");
        buffer = MemoryUtil.memAllocFloat(MAX_VERTS * 6);

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) MAX_VERTS * 6 * Float.BYTES, GL_DYNAMIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 6 * Float.BYTES, 2L * Float.BYTES);
        glBindVertexArray(0);
    }

    public void render(int width, int height, StarSystem sys, Ship ship, String targetName, int fps) {
        vertCount = 0;
        buffer.clear();

        // Top-left panel
        drawRect(18, 18, 520, 74, 0.03f, 0.05f, 0.09f, 0.72f);
        drawText(30, 30, 2.0f, "SPACEINC // ISOMETRIC 4X POC", 0.45f, 0.85f, 1.0f, 1.0f);
        int starNum = sys.starType.ordinal() + 1;
        int starTotal = StarSystem.StarType.values().length;
        drawText(30, 52, 1.6f, "STAR [" + starNum + "/" + starTotal + "]: " + sys.starType.displayName, 1.0f, 0.84f, 0.45f, 1.0f);
        drawText(30, 70, 1.4f, "[ / ] CYCLE STAR TYPES   1-9 / 0 / - : DIRECT SELECT", 0.65f, 0.72f, 0.82f, 0.9f);

        // Bottom-left telemetry
        drawRect(18, height - 92, 410, 74, 0.03f, 0.05f, 0.09f, 0.72f);
        float speed = ship.velocity.length();
        String wp = (ship.waypoint != null) ? "AUTOPILOT ACTIVE" : "MANUAL";
        drawText(30, height - 80, 1.6f, String.format("SPEED: %5.1f M/S   MODE: %s", speed, wp), 0.78f, 0.92f, 1.0f, 1.0f);
        drawText(30, height - 58, 1.6f, "CAM FOCUS [TAB]: " + targetName, 0.45f, 0.90f, 0.75f, 1.0f);
        drawText(30, height - 38, 1.4f, "FPS: " + fps, 0.55f, 0.65f, 0.75f, 0.9f);

        // Bottom-right controls
        drawRect(width - 378, height - 112, 360, 94, 0.03f, 0.05f, 0.09f, 0.72f);
        drawText(width - 364, height - 100, 1.4f, "WASD / ARROWS : THRUST", 0.72f, 0.80f, 0.90f, 0.95f);
        drawText(width - 364, height - 82, 1.4f, "SHIFT         : AFTERBURNER", 0.72f, 0.80f, 0.90f, 0.95f);
        drawText(width - 364, height - 64, 1.4f, "LEFT CLICK    : SET WAYPOINT", 0.72f, 0.80f, 0.90f, 0.95f);
        drawText(width - 364, height - 46, 1.4f, "R-DRAG / Q-E  : ORBIT CAMERA", 0.72f, 0.80f, 0.90f, 0.95f);
        drawText(width - 364, height - 28, 1.4f, "SCROLL        : ZOOM", 0.72f, 0.80f, 0.90f, 0.95f);

        if (vertCount == 0) return;
        buffer.flip();

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader.use();
        shader.setFloat("uScreenSize", 0f); // unused, use vec2 via glUniform2f
        int loc = glGetUniformLocation(getProgramHack(), "uScreenSize");
        if (loc != -1) glUniform2f(loc, (float) width, (float) height);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertCount);
        glBindVertexArray(0);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private int getProgramHack() {
        return glGetInteger(GL_CURRENT_PROGRAM);
    }

    private void drawText(float x, float y, float scale, String text, float r, float g, float b, float a) {
        String upper = text.toUpperCase();
        float cx = x;
        for (int i = 0; i < upper.length(); i++) {
            char ch = upper.charAt(i);
            int[] glyph = FONT.get(ch);
            if (glyph != null) {
                for (int row = 0; row < 7; row++) {
                    int bits = glyph[row];
                    for (int col = 0; col < 5; col++) {
                        if ((bits & (1 << (4 - col))) != 0) {
                            drawRect(cx + col * scale, y + row * scale, scale, scale, r, g, b, a);
                        }
                    }
                }
            }
            cx += 6.0f * scale;
        }
    }

    private void drawRect(float x, float y, float w, float h, float r, float g, float b, float a) {
        if (vertCount + 6 > MAX_VERTS) return;
        putVert(x, y, r, g, b, a);
        putVert(x, y + h, r, g, b, a);
        putVert(x + w, y + h, r, g, b, a);

        putVert(x, y, r, g, b, a);
        putVert(x + w, y + h, r, g, b, a);
        putVert(x + w, y, r, g, b, a);
    }

    private void putVert(float x, float y, float r, float g, float b, float a) {
        buffer.put(x).put(y).put(r).put(g).put(b).put(a);
        vertCount++;
    }

    public void dispose() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        MemoryUtil.memFree(buffer);
        shader.dispose();
    }
}
