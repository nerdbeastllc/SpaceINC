package spaceinc;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class IsometricCamera {
    private float yaw = 0.785398f;    // 45 deg
    private float pitch = 0.663225f;  // ~38 deg isometric elevation
    private float distance = 36.0f;
    private float targetDistance = 36.0f;

    private final Vector3f focusPos = new Vector3f(0f, 0f, 0f);
    private final Vector3f eyePos = new Vector3f();

    private final Matrix4f view = new Matrix4f();
    private final Matrix4f projection = new Matrix4f();

    public void update(float dt, Vector3f targetPos, float aspect) {
        float lerp = 1.0f - (float) Math.exp(-8.0 * dt);
        focusPos.lerp(targetPos, lerp);
        distance += (targetDistance - distance) * (1.0f - (float) Math.exp(-10.0 * dt));

        float cosPitch = (float) Math.cos(pitch);
        float sinPitch = (float) Math.sin(pitch);
        float cosYaw = (float) Math.cos(yaw);
        float sinYaw = (float) Math.sin(yaw);

        eyePos.set(
            focusPos.x + distance * cosPitch * cosYaw,
            focusPos.y + distance * sinPitch,
            focusPos.z + distance * cosPitch * sinYaw
        );

        view.identity().lookAt(eyePos, focusPos, new Vector3f(0f, 1f, 0f));
        projection.identity().perspective((float) Math.toRadians(42.0), aspect, 0.3f, 2500.0f);
    }

    public void orbit(float deltaYaw, float deltaPitch) {
        yaw += deltaYaw;
        pitch = Math.max(0.12f, Math.min(1.42f, pitch + deltaPitch));
    }

    public void zoom(float scrollDelta) {
        float factor = (float) Math.pow(0.88, scrollDelta);
        targetDistance = Math.max(8.0f, Math.min(280.0f, targetDistance * factor));
    }

    public Vector3f getForwardXZ() {
        Vector3f fwd = new Vector3f(focusPos.x - eyePos.x, 0f, focusPos.z - eyePos.z);
        if (fwd.lengthSquared() < 1e-6f) fwd.set(0f, 0f, -1f);
        return fwd.normalize();
    }

    public Vector3f getRightXZ() {
        Vector3f fwd = getForwardXZ();
        return new Vector3f(-fwd.z, 0f, fwd.x).normalize();
    }

    /**
     * Intersects a screen-space mouse ray with the y=planeY ecliptic plane.
     */
    public Vector3f screenToPlane(float mouseX, float mouseY, float screenW, float screenH, float planeY) {
        float ndcX = (2.0f * mouseX) / screenW - 1.0f;
        float ndcY = 1.0f - (2.0f * mouseY) / screenH;

        Matrix4f invVP = new Matrix4f(projection).mul(view).invert();
        Vector4f nearPt = new Vector4f(ndcX, ndcY, -1.0f, 1.0f).mul(invVP);
        Vector4f farPt = new Vector4f(ndcX, ndcY, 1.0f, 1.0f).mul(invVP);
        nearPt.div(nearPt.w);
        farPt.div(farPt.w);

        Vector3f dir = new Vector3f(farPt.x - nearPt.x, farPt.y - nearPt.y, farPt.z - nearPt.z).normalize();
        if (Math.abs(dir.y) < 1e-5f) return null;
        float t = (planeY - nearPt.y) / dir.y;
        if (t < 0) return null;
        return new Vector3f(nearPt.x + dir.x * t, planeY, nearPt.z + dir.z * t);
    }

    public Matrix4f getView() { return view; }
    public Matrix4f getProjection() { return projection; }
    public Vector3f getEyePos() { return eyePos; }
    public Vector3f getFocusPos() { return focusPos; }
    public float getDistance() { return distance; }
}
