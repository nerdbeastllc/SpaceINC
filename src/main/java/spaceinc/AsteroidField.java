package spaceinc;

import org.joml.Matrix4f;
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
 * Single-draw-call hardware-instanced Asteroid Belt + 3D Oort Cloud.
 * All orbital & tumbling motion runs on the GPU for minimal CPU overhead.
 */
public class AsteroidField {
    private final int vao;
    private final int vbo;
    private final int ebo;
    private final int instanceVbo;
    private final int indexCount;
    private final int instanceCount;

    public AsteroidField(int beltCount, int oortCount) {
        this.instanceCount = beltCount + oortCount;

        // Build one craggy low-poly asteroid mesh (lat/lon displaced sphere)
        int stacks = 6;
        int slices = 9;
        List<Float> verts = new ArrayList<>();
        List<Integer> inds = new ArrayList<>();
        Random shapeRng = new Random(1337L);

        // Precompute seam-safe vertex radii by (i, j % slices)
        float[][] radii = new float[stacks + 1][slices];
        for (int i = 0; i <= stacks; i++) {
            float poleRadius = 0.85f + shapeRng.nextFloat() * 0.30f;
            for (int j = 0; j < slices; j++) {
                if (i == 0 || i == stacks) {
                    radii[i][j] = poleRadius;
                } else {
                    radii[i][j] = 0.72f + shapeRng.nextFloat() * 0.52f;
                }
            }
        }

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

        int stride = slices + 1;
        for (int i = 0; i < stacks; i++) {
            for (int j = 0; j < slices; j++) {
                int a = i * stride + j;
                int b = (i + 1) * stride + j;
                int c = (i + 1) * stride + (j + 1);
                int d = i * stride + (j + 1);
                // Counter-clockwise winding
                inds.add(a); inds.add(d); inds.add(b);
                inds.add(b); inds.add(d); inds.add(c);
            }
        }
        indexCount = inds.size();

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        FloatBuffer vb = MemoryUtil.memAllocFloat(verts.size());
        for (float f : verts) vb.put(f);
        vb.flip();
        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vb, GL_STATIC_DRAW);
        MemoryUtil.memFree(vb);

        IntBuffer ib = MemoryUtil.memAllocInt(inds.size());
        for (int idx : inds) ib.put(idx);
        ib.flip();
        ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ib, GL_STATIC_DRAW);
        MemoryUtil.memFree(ib);

        int vStride = 6 * Float.BYTES;
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, vStride, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, vStride, 3L * Float.BYTES);

        // Build per-instance data: 12 floats per instance (3 vec4s)
        FloatBuffer instBuf = MemoryUtil.memAllocFloat(instanceCount * 12);
        Random rng = new Random(2026L);

        // 1. Asteroid Belt (between Mars 198 and Jupiter 305 -> r = 228..272)
        for (int i = 0; i < beltCount; i++) {
            float r = 228.0f + rng.nextFloat() * 44.0f;
            float angle = rng.nextFloat() * 2.0f * (float) Math.PI;
            float speed = 0.0052f + rng.nextFloat() * 0.0022f;
            float yOff = (rng.nextFloat() - 0.5f) * 9.0f;

            float baseScale = 0.28f + (float) Math.pow(rng.nextFloat(), 2.4) * 0.85f;
            float sx = baseScale * (0.7f + rng.nextFloat() * 0.7f);
            float sy = baseScale * (0.6f + rng.nextFloat() * 0.6f);
            float sz = baseScale * (0.7f + rng.nextFloat() * 0.7f);
            float spinPhase = rng.nextFloat() * 6.283f;

            float shade = 0.36f + rng.nextFloat() * 0.32f;
            float cr = shade * (1.05f + rng.nextFloat() * 0.15f);
            float cg = shade * (0.95f + rng.nextFloat() * 0.08f);
            float cb = shade * (0.85f + rng.nextFloat() * 0.08f);

            instBuf.put(r).put(angle).put(speed).put(yOff);
            instBuf.put(sx).put(sy).put(sz).put(spinPhase);
            instBuf.put(cr).put(cg).put(cb).put(0.0f);
        }

        // 2. Oort Cloud (on the ecliptic plane beyond Pluto 770 -> r = 910..1225)
        for (int i = 0; i < oortCount; i++) {
            float r = 910.0f + rng.nextFloat() * 315.0f;
            float yOff = (rng.nextFloat() - 0.5f) * 18.0f;
            float angle = rng.nextFloat() * 2.0f * (float) Math.PI;
            float speed = 0.00035f + rng.nextFloat() * 0.00035f;

            float baseScale = 0.45f + (float) Math.pow(rng.nextFloat(), 2.0) * 1.15f;
            float sx = baseScale * (0.75f + rng.nextFloat() * 0.6f);
            float sy = baseScale * (0.75f + rng.nextFloat() * 0.6f);
            float sz = baseScale * (0.75f + rng.nextFloat() * 0.6f);
            float spinPhase = rng.nextFloat() * 6.283f;

            float ice = 0.62f + rng.nextFloat() * 0.34f;
            instBuf.put(r).put(angle).put(speed).put(yOff);
            instBuf.put(sx).put(sy).put(sz).put(spinPhase);
            instBuf.put(ice * 0.78f).put(ice * 0.90f).put(ice * 1.05f).put(1.0f);
        }
        instBuf.flip();

        instanceVbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, instanceVbo);
        glBufferData(GL_ARRAY_BUFFER, instBuf, GL_STATIC_DRAW);
        MemoryUtil.memFree(instBuf);

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

    public void draw(Shader shader, Matrix4f view, Matrix4f proj, float time, StarSystem sys) {
        shader.use();
        shader.setMat4("uView", view);
        shader.setMat4("uProjection", proj);
        shader.setFloat("uTime", time);
        shader.setVec3("uStarPos", sys.starPos);
        shader.setVec3("uStarLightColor", sys.starType.lightColor);

        glBindVertexArray(vao);
        glDrawElementsInstanced(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0L, instanceCount);
        glBindVertexArray(0);
    }

    public void dispose() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteBuffers(ebo);
        glDeleteBuffers(instanceVbo);
    }
}
