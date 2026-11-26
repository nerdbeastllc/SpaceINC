package spaceinc;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Ship {
    public final Vector3f position = new Vector3f(0f, 0f, 118f);
    public final Vector3f velocity = new Vector3f();
    public float yaw = (float) Math.PI; // Facing toward the star initially
    public float bank = 0f;
    public float thrustLevel = 0f;

    public Vector3f waypoint = null;
    public final ShipInventory inventory = new ShipInventory();

    public boolean hitTest(Vector3f rayOrigin, Vector3f rayDir) {
        Vector3f oc = new Vector3f(position).sub(rayOrigin);
        float proj = oc.dot(rayDir);
        if (proj < 0) return false;
        float distCam = position.distance(rayOrigin);
        float pickR = Math.max(2.2f, distCam * 0.024f);
        float d2 = oc.lengthSquared() - proj * proj;
        return d2 <= pickR * pickR;
    }

    private static final int MAX_TRAIL = 90;
    private final Deque<Vector3f> trailPoints = new ArrayDeque<>();
    private float trailTimer = 0f;
    private int trailVao = 0;
    private int trailVbo = 0;

    public void initTrail() {
        trailVao = glGenVertexArrays();
        trailVbo = glGenBuffers();
        glBindVertexArray(trailVao);
        glBindBuffer(GL_ARRAY_BUFFER, trailVbo);
        glBufferData(GL_ARRAY_BUFFER, (long) MAX_TRAIL * 4 * Float.BYTES, GL_DYNAMIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 1, GL_FLOAT, false, 4 * Float.BYTES, 3L * Float.BYTES);
        glBindVertexArray(0);
    }

    public void update(float dt, Vector3f manualInputDir, boolean boost) {
        Vector3f desiredDir = new Vector3f(manualInputDir);
        boolean hasManual = desiredDir.lengthSquared() > 0.01f;

        if (hasManual) {
            waypoint = null; // Manual piloting overrides autopilot waypoint
            desiredDir.normalize();
        } else if (waypoint != null) {
            Vector3f toWp = new Vector3f(waypoint).sub(position);
            toWp.y = 0f;
            float dist = toWp.length();
            if (dist < 1.2f) {
                waypoint = null;
            } else {
                desiredDir.set(toWp).div(dist);
            }
        }

        float maxSpeed = boost ? 44.0f : 20.0f;
        float accel = boost ? 38.0f : 18.0f;

        if (desiredDir.lengthSquared() > 0.01f) {
            float targetYaw = (float) Math.atan2(desiredDir.x, desiredDir.z);
            float diff = normalizeAngle(targetYaw - yaw);
            yaw = normalizeAngle(yaw + diff * Math.min(1.0f, dt * 5.5f));

            float targetBank = Math.max(-0.55f, Math.min(0.55f, -diff * 0.9f));
            bank += (targetBank - bank) * Math.min(1.0f, dt * 6.0f);

            Vector3f forward = new Vector3f((float) Math.sin(yaw), 0f, (float) Math.cos(yaw));
            velocity.add(forward.mul(accel * dt));
            if (velocity.length() > maxSpeed) {
                velocity.normalize().mul(maxSpeed);
            }
            thrustLevel += ((boost ? 1.5f : 1.0f) - thrustLevel) * Math.min(1.0f, dt * 8.0f);
        } else {
            // Gentle space drag for crisp isometric feel
            float drag = (float) Math.exp(-2.2 * dt);
            velocity.mul(drag);
            bank += (0f - bank) * Math.min(1.0f, dt * 4.0f);
            thrustLevel += (0.15f - thrustLevel) * Math.min(1.0f, dt * 5.0f);
        }

        position.add(new Vector3f(velocity).mul(dt));

        // Record engine trail
        trailTimer += dt;
        if (trailTimer >= 0.03f) {
            trailTimer = 0f;
            Vector3f aft = new Vector3f(
                position.x - (float) Math.sin(yaw) * 1.15f,
                position.y,
                position.z - (float) Math.cos(yaw) * 1.15f
            );
            trailPoints.addFirst(aft);
            while (trailPoints.size() > MAX_TRAIL) {
                trailPoints.removeLast();
            }
        }
    }

    public Matrix4f getModelMatrix() {
        return new Matrix4f()
            .translate(position)
            .rotateY(yaw)
            .rotateZ(bank)
            .scale(0.95f);
    }

    public void drawTrail(Shader lineShader, Matrix4f view, Matrix4f proj) {
        if (trailPoints.size() < 2) return;

        int n = trailPoints.size();
        FloatBuffer fb = MemoryUtil.memAllocFloat(n * 4);
        int idx = 0;
        for (Vector3f p : trailPoints) {
            float alpha = 1.0f - ((float) idx / (n - 1));
            alpha = alpha * alpha;
            fb.put(p.x).put(p.y).put(p.z).put(alpha);
            idx++;
        }
        fb.flip();

        glBindBuffer(GL_ARRAY_BUFFER, trailVbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, fb);
        MemoryUtil.memFree(fb);

        lineShader.use();
        lineShader.setMat4("uView", view);
        lineShader.setMat4("uProjection", proj);
        lineShader.setVec3("uColor", 1.75f, 0.78f, 0.14f);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        glBindVertexArray(trailVao);
        glDrawArrays(GL_LINE_STRIP, 0, n);
        glBindVertexArray(0);
        glDisable(GL_BLEND);
    }

    private static float normalizeAngle(float a) {
        while (a > Math.PI) a -= (float) (2.0 * Math.PI);
        while (a < -Math.PI) a += (float) (2.0 * Math.PI);
        return a;
    }

    public void dispose() {
        if (trailVao != 0) glDeleteVertexArrays(trailVao);
        if (trailVbo != 0) glDeleteBuffers(trailVbo);
    }
}
