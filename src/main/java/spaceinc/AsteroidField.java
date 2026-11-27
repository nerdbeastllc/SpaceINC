package spaceinc;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

/**
 * Single-draw-call hardware-instanced Asteroid Belt + Oort Cloud.
 */
public class AsteroidField {
    private int vao;
    private int vbo;
    private int ebo;
    private int instanceVbo;
    private int indexCount;
    private final int instanceCount;

    private final float[] orbitR;
    private final float[] startAngle;
    private final float[] orbitSpeed;
    private final float[] yOffset;
    private final float[] pickRadius;
    private final AsteroidType[] types;
    private boolean isCoreFederation = true;

    private static class RingSpec {
        int count, offset;
        float minR, spanR, minSpeed, spanSpeed, ySpread, minScale, spanScale, scalePow;
        boolean comet;

        private RingSpec() {}

        static RingSpec belt(int count) {
            RingSpec s = new RingSpec();
            s.count = count; s.offset = 0; s.minR = 228f; s.spanR = 44f;
            s.minSpeed = 0.0052f; s.spanSpeed = 0.0022f; s.ySpread = 9f;
            s.minScale = 0.28f; s.spanScale = 0.85f; s.scalePow = 2.4f; s.comet = false;
            return s;
        }

        static RingSpec oort(int count, int offset) {
            RingSpec s = new RingSpec();
            s.count = count; s.offset = offset; s.minR = 910f; s.spanR = 315f;
            s.minSpeed = 0.00035f; s.spanSpeed = 0.00035f; s.ySpread = 18f;
            s.minScale = 0.45f; s.spanScale = 1.15f; s.scalePow = 2.0f; s.comet = true;
            return s;
        }
    }

    public AsteroidField(int beltCount, int oortCount) {
        this.instanceCount = beltCount + oortCount;
        this.orbitR = new float[instanceCount];
        this.startAngle = new float[instanceCount];
        this.orbitSpeed = new float[instanceCount];
        this.yOffset = new float[instanceCount];
        this.pickRadius = new float[instanceCount];
        this.types = new AsteroidType[instanceCount];

        buildAsteroidMesh();
        FloatBuffer instBuf = MemoryUtil.memAllocFloat(instanceCount * 12);
        Random rng = new Random(2026L);
        populateRing(instBuf, rng, RingSpec.belt(beltCount));
        populateRing(instBuf, rng, RingSpec.oort(oortCount, beltCount));
        instBuf.flip();
        uploadInstanceBuffer(instBuf);
        MemoryUtil.memFree(instBuf);
    }

    private void buildAsteroidMesh() {
        int stacks = 6;
        int slices = 9;
        float[][] radii = buildCraggyRadii(stacks, slices, new Random(1337L));
        List<Float> verts = buildSphereVerts(stacks, slices, radii);
        List<Integer> inds = buildSphereIndices(stacks, slices);
        indexCount = inds.size();

        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        uploadMeshBuffers(verts, inds);

        int vStride = 6 * Float.BYTES;
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, vStride, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, vStride, 3L * Float.BYTES);
    }

    private static float[][] buildCraggyRadii(int stacks, int slices, Random rng) {
        float[][] radii = new float[stacks + 1][slices];
        for (int i = 0; i <= stacks; i++) {
            float poleRadius = 0.85f + rng.nextFloat() * 0.30f;
            boolean isPole = (i == 0 || i == stacks);
            for (int j = 0; j < slices; j++) {
                radii[i][j] = isPole ? poleRadius : (0.72f + rng.nextFloat() * 0.52f);
            }
        }
        return radii;
    }

    private static List<Float> buildSphereVerts(int stacks, int slices, float[][] radii) {
        List<Float> verts = new ArrayList<>();
        for (int i = 0; i <= stacks; i++) {
            float phi = ((float) i / stacks) * (float) Math.PI;
            float sinPhi = (float) Math.sin(phi);
            float cosPhi = (float) Math.cos(phi);
            for (int j = 0; j <= slices; j++) {
                float theta = ((float) j / slices) * 2.0f * (float) Math.PI;
                float r = radii[i][j % slices];
                float x = sinPhi * (float) Math.cos(theta) * r;
                float y = cosPhi * r;
                float z = sinPhi * (float) Math.sin(theta) * r;
                float len = (float) Math.sqrt(x * x + y * y + z * z);
                verts.add(x); verts.add(y); verts.add(z);
                verts.add(x / len); verts.add(y / len); verts.add(z / len);
            }
        }
        return verts;
    }

    private static List<Integer> buildSphereIndices(int stacks, int slices) {
        List<Integer> inds = new ArrayList<>();
        int stride = slices + 1;
        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int a = i * stride + j;
                int b = (i + 1) * stride + j;
                int c = (i + 1) * stride + (j + 1);
                int d = i * stride + (j + 1);
                inds.add(a); inds.add(d); inds.add(b);
                inds.add(b); inds.add(d); inds.add(c);
            }
        }
        return inds;
    }

    private void uploadMeshBuffers(List<Float> verts, List<Integer> inds) {
        FloatBuffer fb = MemoryUtil.memAllocFloat(verts.size());
        for (float f : verts) fb.put(f);
        fb.flip();
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, fb, GL_STATIC_DRAW);
        MemoryUtil.memFree(fb);

        IntBuffer ib = MemoryUtil.memAllocInt(inds.size());
        for (int idx : inds) ib.put(idx);
        ib.flip();
        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ib, GL_STATIC_DRAW);
        MemoryUtil.memFree(ib);
    }

    private void populateRing(FloatBuffer buf, Random rng, RingSpec spec) {
        for (int i = 0; i < spec.count; i++) {
            float r = spec.minR + rng.nextFloat() * spec.spanR;
            float speed = (spec.minSpeed + rng.nextFloat() * spec.spanSpeed) / 3.0f;
            float yOff = (rng.nextFloat() - 0.5f) * spec.ySpread;
            float baseScale = spec.minScale + (float) Math.pow(rng.nextFloat(), spec.scalePow) * spec.spanScale;
            float angle = rng.nextFloat() * 2.0f * (float) Math.PI;
            float sx = baseScale * (0.72f + rng.nextFloat() * 0.65f);
            float sy = baseScale * (0.65f + rng.nextFloat() * 0.60f);
            float sz = baseScale * (0.72f + rng.nextFloat() * 0.65f);
            float spinPhase = rng.nextFloat() * 6.283f;

            AsteroidType type = pickType(spec.comet, rng);
            float tone = type.baseTone + rng.nextFloat() * type.toneSpan;
            float cr = tone * type.mulR;
            float cg = tone * type.mulG;
            float cb = tone * type.mulB;

            buf.put(r).put(angle).put(speed).put(yOff);
            buf.put(sx).put(sy).put(sz).put(spinPhase);
            buf.put(cr).put(cg).put(cb).put(type.shaderSurfaceType);

            int idx = spec.offset + i;
            orbitR[idx] = r;
            startAngle[idx] = angle;
            orbitSpeed[idx] = speed;
            yOffset[idx] = yOff;
            pickRadius[idx] = Math.max(sx, Math.max(sy, sz));
            types[idx] = type;
        }
    }

    private static AsteroidType pickType(boolean cometRing, Random rng) {
        if (cometRing) return AsteroidType.ICY_COMET;
        return (rng.nextFloat() < 0.32f) ? AsteroidType.METALLIC : AsteroidType.STONY;
    }

    private void uploadInstanceBuffer(FloatBuffer instBuf) {
        instanceVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);
        glBufferData(GL_ARRAY_BUFFER, instBuf, GL_STATIC_DRAW);

        int iStride = 12 * Float.BYTES;
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 4, GL_FLOAT, false, iStride, 0);
        glVertexAttribDivisor(3, 1);

        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 4, GL_FLOAT, false, iStride, 4L * Float.BYTES);
        glVertexAttribDivisor(4, 1);

        glEnableVertexAttribArray(5);
        glVertexAttribPointer(5, 4, GL_FLOAT, false, iStride, 8L * Float.BYTES);
        glVertexAttribDivisor(5, 1);

        glBindVertexArray(0);
    }

    public void draw(Shader shader, GameState state) {
        shader.use();
        shader.setMat4("uView", state.camera.getView());
        shader.setMat4("uProjection", state.camera.getProjection());
        shader.setFloat("uTime", state.currentElapsed);
        shader.setVec3("uStarPos", state.starSystem.starPos);
        shader.setVec3("uStarLightColor", state.starSystem.starType.lightColor);
        shader.setVec3("uCameraPos", state.camera.getEyePos());

        glBindVertexArray(vao);
        glDrawElementsInstanced(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0L, instanceCount);
        glBindVertexArray(0);
    }

    public int pick(Vector3f rayOrigin, Vector3f rayDir, float elapsed) {
        int bestIdx = -1;
        float bestT = Float.MAX_VALUE;
        Vector3f pos = new Vector3f();
        Vector3f oc = new Vector3f();

        for (int i = 0; i < instanceCount; i++) {
            float a = startAngle[i] + elapsed * orbitSpeed[i];
            pos.set((float) Math.cos(a) * orbitR[i], yOffset[i], (float) Math.sin(a) * orbitR[i]);

            float distToCam = pos.distance(rayOrigin);
            float r = Math.max(pickRadius[i] * 1.8f, distToCam * 0.012f);

            oc.set(pos).sub(rayOrigin);
            float proj = oc.dot(rayDir);
            if (proj < 0) continue;
            float d2 = oc.lengthSquared() - proj * proj;
            if (d2 <= r * r && proj < bestT) {
                bestT = proj;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    public void setCoreFederation(boolean isCoreFederation) {
        this.isCoreFederation = isCoreFederation;
    }

    public TargetInfo getTargetInfo(int idx, java.util.function.Supplier<Float> timeSupplier) {
        AsteroidType type = types[idx];
        String name = String.format("%s #%04d", type.namePrefix, idx + 1);
        return new TargetInfo(name, type.category, MineralRegistry.forAsteroid(idx, type, isCoreFederation), "UNCLAIMED", pickRadius[idx], () -> {
            float a = startAngle[idx] + timeSupplier.get() * orbitSpeed[idx];
            return new Vector3f((float) Math.cos(a) * orbitR[idx], yOffset[idx], (float) Math.sin(a) * orbitR[idx]);
        });
    }

    public void dispose() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteBuffers(instanceVbo);
    }
}
