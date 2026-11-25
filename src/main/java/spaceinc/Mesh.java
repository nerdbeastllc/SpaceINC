package spaceinc;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    private final int vao;
    private final int vbo;
    private final int ebo;
    private final int vertexCount;
    private final boolean indexed;
    private final int drawMode;

    private Mesh(int vao, int vbo, int ebo, int vertexCount, boolean indexed, int drawMode) {
        this.vao = vao;
        this.vbo = vbo;
        this.ebo = ebo;
        this.vertexCount = vertexCount;
        this.indexed = indexed;
        this.drawMode = drawMode;
    }

    public void draw() {
        glBindVertexArray(vao);
        if (indexed) {
            glDrawElements(drawMode, vertexCount, GL_UNSIGNED_INT, 0);
        } else {
            glDrawArrays(drawMode, 0, vertexCount);
        }
        glBindVertexArray(0);
    }

    public void dispose() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        if (ebo != 0) glDeleteBuffers(ebo);
    }

    /**
     * Vertex layout: pos(3), normal(3), uv(2) -> 8 floats per vertex.
     */
    private static Mesh buildIndexed(float[] vertices, int[] indices, int drawMode) {
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);

        FloatBuffer vb = MemoryUtil.memAllocFloat(vertices.length);
        vb.put(vertices).flip();
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vb, GL_STATIC_DRAW);
        MemoryUtil.memFree(vb);

        IntBuffer ib = MemoryUtil.memAllocInt(indices.length);
        ib.put(indices).flip();
        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ib, GL_STATIC_DRAW);
        MemoryUtil.memFree(ib);

        int stride = 8 * Float.BYTES;
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 6L * Float.BYTES);

        glBindVertexArray(0);
        return new Mesh(vao, vbo, ebo, indices.length, true, drawMode);
    }

    public static Mesh createSphere(int stacks, int slices) {
        List<Float> verts = new ArrayList<>();
        List<Integer> inds = new ArrayList<>();

        for (int i = 0; i <= stacks; i++) {
            float v = (float) i / stacks;
            float phi = v * (float) Math.PI;
            float sinPhi = (float) Math.sin(phi);
            float cosPhi = (float) Math.cos(phi);

            for (int j = 0; j <= slices; j++) {
                float u = (float) j / slices;
                float theta = u * 2.0f * (float) Math.PI;
                float sinTheta = (float) Math.sin(theta);
                float cosTheta = (float) Math.cos(theta);

                float x = sinPhi * cosTheta;
                float y = cosPhi;
                float z = sinPhi * sinTheta;

                verts.add(x); verts.add(y); verts.add(z);
                verts.add(x); verts.add(y); verts.add(z);
                verts.add(u); verts.add(1.0f - v);
            }
        }

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

        return buildIndexed(toFloatArray(verts), toIntArray(inds), GL_TRIANGLES);
    }

    public static Mesh createRing(int segments, float innerR, float outerR) {
        List<Float> verts = new ArrayList<>();
        List<Integer> inds = new ArrayList<>();

        for (int i = 0; i <= segments; i++) {
            float u = (float) i / segments;
            float angle = u * 2.0f * (float) Math.PI;
            float c = (float) Math.cos(angle);
            float s = (float) Math.sin(angle);

            // Inner vertex
            verts.add(c * innerR); verts.add(0f); verts.add(s * innerR);
            verts.add(0f); verts.add(1f); verts.add(0f);
            verts.add(u); verts.add(0f);

            // Outer vertex
            verts.add(c * outerR); verts.add(0f); verts.add(s * outerR);
            verts.add(0f); verts.add(1f); verts.add(0f);
            verts.add(u); verts.add(1f);
        }

        for (int i = 0; i < segments; i++) {
            int i0 = i * 2;
            int i1 = i0 + 1;
            int i2 = i0 + 2;
            int i3 = i0 + 3;
            inds.add(i0); inds.add(i1); inds.add(i2);
            inds.add(i1); inds.add(i3); inds.add(i2);
        }

        return buildIndexed(toFloatArray(verts), toIntArray(inds), GL_TRIANGLES);
    }

    public static Mesh createOrbitCircle(int segments, float radius) {
        float[] verts = new float[segments * 8];
        int[] inds = new int[segments];
        for (int i = 0; i < segments; i++) {
            float angle = ((float) i / segments) * 2.0f * (float) Math.PI;
            int base = i * 8;
            verts[base] = (float) Math.cos(angle) * radius;
            verts[base + 1] = 0f;
            verts[base + 2] = (float) Math.sin(angle) * radius;
            verts[base + 3] = 0f; verts[base + 4] = 1f; verts[base + 5] = 0f;
            verts[base + 6] = (float) i / segments; verts[base + 7] = 0f;
            inds[i] = i;
        }
        return buildIndexed(verts, inds, GL_LINE_LOOP);
    }

    public static Mesh createQuad() {
        float[] verts = {
            -1f, -1f, 0f,  0f, 0f, 1f,  0f, 0f,
             1f, -1f, 0f,  0f, 0f, 1f,  1f, 0f,
             1f,  1f, 0f,  0f, 0f, 1f,  1f, 1f,
            -1f,  1f, 0f,  0f, 0f, 1f,  0f, 1f
        };
        int[] inds = { 0, 1, 2, 0, 2, 3 };
        return buildIndexed(verts, inds, GL_TRIANGLES);
    }

    /**
     * Generates 3D arched solar prominence loops & flare plumes anchored on the unit sphere
     * so they shoot outward in true 3D across both the center and limb of the star.
     */
    public static Mesh createProminences(int loopCount, int arcSegments) {
        List<Float> verts = new ArrayList<>();
        List<Integer> inds = new ArrayList<>();
        java.util.Random rng = new java.util.Random(42L);

        for (int l = 0; l < loopCount; l++) {
            // Distribute footpoints around the sphere (bias some toward front/mid-latitudes)
            float theta = (float) (l * 2.399963 + rng.nextFloat() * 0.4); // Golden angle
            float y = -0.65f + 1.30f * ((float) l / Math.max(1, loopCount - 1));
            float rXZ = (float) Math.sqrt(Math.max(0.05f, 1.0f - y * y));
            float ax = rXZ * (float) Math.cos(theta);
            float ay = y;
            float az = rXZ * (float) Math.sin(theta);

            // Second footpoint nearby on the sphere with varied span
            float span = 0.10f + rng.nextFloat() * 0.34f;
            float dTheta = (rng.nextBoolean() ? 1f : -1f) * span;
            float dy = (rng.nextFloat() - 0.5f) * span * 1.2f;
            float bx = (float) (Math.sqrt(Math.max(0.05f, 1.0f - Math.min(0.95f, (ay + dy) * (ay + dy)))) * Math.cos(theta + dTheta));
            float by = Math.max(-0.92f, Math.min(0.92f, ay + dy));
            float bz = (float) (Math.sqrt(Math.max(0.05f, 1.0f - by * by)) * Math.sin(theta + dTheta));

            // Mix of tiny low filaments, medium arches, and occasional towering eruptive loops
            float hRand = rng.nextFloat();
            float archHeight = 0.05f + (hRand * hRand) * 0.36f;
            float ribbonHalfW = 0.015f + rng.nextFloat() * 0.048f;
            float loopSeed = rng.nextFloat() * 100.0f;

            // Compute a stable sideways tangent for the ribbon
            float mx = (ax + bx) * 0.5f, my = (ay + by) * 0.5f, mz = (az + bz) * 0.5f;
            float dx = bx - ax, dyv = by - ay, dz = bz - az;
            float sx = my * dz - mz * dyv;
            float sy = mz * dx - mx * dz;
            float sz = mx * dyv - my * dx;
            float slen = (float) Math.sqrt(sx * sx + sy * sy + sz * sz);
            if (slen < 1e-5f) { sx = 0f; sy = 1f; sz = 0f; slen = 1f; }
            sx /= slen; sy /= slen; sz /= slen;

            int baseVert = verts.size() / 8;
            for (int i = 0; i <= arcSegments; i++) {
                float u = (float) i / arcSegments;
                // Interpolate on sphere + raise outward along radial normal
                float px = ax * (1f - u) + bx * u;
                float py = ay * (1f - u) + by * u;
                float pz = az * (1f - u) + bz * u;
                float plen = (float) Math.sqrt(px * px + py * py + pz * pz);
                px /= plen; py /= plen; pz /= plen;

                float lift = 1.0f + archHeight * (float) Math.sin(u * Math.PI);
                float cx = px * lift;
                float cy = py * lift;
                float cz = pz * lift;

                // Two ribbon vertices across width; pack loopSeed & radial normal into normal attr
                verts.add(cx - sx * ribbonHalfW); verts.add(cy - sy * ribbonHalfW); verts.add(cz - sz * ribbonHalfW);
                verts.add(loopSeed); verts.add(sx); verts.add(sy);
                verts.add(u); verts.add(0.0f);

                verts.add(cx + sx * ribbonHalfW); verts.add(cy + sy * ribbonHalfW); verts.add(cz + sz * ribbonHalfW);
                verts.add(loopSeed); verts.add(sx); verts.add(sy);
                verts.add(u); verts.add(1.0f);
            }

            for (int i = 0; i < arcSegments; i++) {
                int i0 = baseVert + i * 2;
                int i1 = i0 + 1;
                int i2 = i0 + 2;
                int i3 = i0 + 3;
                inds.add(i0); inds.add(i1); inds.add(i2);
                inds.add(i1); inds.add(i3); inds.add(i2);
            }
        }

        return buildIndexed(toFloatArray(verts), toIntArray(inds), GL_TRIANGLES);
    }

    /**
     * Sleek EVE-inspired angular corvette/frigate pointing along +Z.
     * UV.x encodes material zone: 0.1 = hull armor, 0.5 = dark trim/canopy, 0.9 = emissive thruster/accent.
     */
    public static Mesh createSpaceship() {
        List<Float> v = new ArrayList<>();
        List<Integer> idx = new ArrayList<>();

        // Helper nose & body points
        float[] nose = { 0f, 0.05f, 1.8f };
        float[] midTop = { 0f, 0.36f, 0.2f };
        float[] midBot = { 0f, -0.22f, 0.1f };
        float[] midLeft = { -0.55f, 0.02f, 0.0f };
        float[] midRight = { 0.55f, 0.02f, 0.0f };
        float[] aftTop = { 0f, 0.28f, -1.2f };
        float[] aftBot = { 0f, -0.18f, -1.2f };
        float[] aftLeft = { -0.45f, 0.02f, -1.25f };
        float[] aftRight = { 0.45f, 0.02f, -1.25f };

        // Main hull (armor = 0.1)
        addTri(v, idx, nose, midTop, midRight, 0.1f);
        addTri(v, idx, nose, midLeft, midTop, 0.1f);
        addTri(v, idx, nose, midRight, midBot, 0.1f);
        addTri(v, idx, nose, midBot, midLeft, 0.1f);

        addQuad(v, idx, midTop, aftTop, aftRight, midRight, 0.1f);
        addQuad(v, idx, midLeft, aftLeft, aftTop, midTop, 0.1f);
        addQuad(v, idx, midRight, aftRight, aftBot, midBot, 0.1f);
        addQuad(v, idx, midBot, aftBot, aftLeft, midLeft, 0.1f);

        // Aft cap (dark trim = 0.5)
        addTri(v, idx, aftTop, aftLeft, aftBot, 0.5f);
        addTri(v, idx, aftTop, aftBot, aftRight, 0.5f);

        // Canopy (dark glass = 0.5)
        float[] cFront = { 0f, 0.24f, 0.85f };
        float[] cTop = { 0f, 0.46f, 0.15f };
        float[] cLeft = { -0.18f, 0.25f, 0.15f };
        float[] cRight = { 0.18f, 0.25f, 0.15f };
        float[] cBack = { 0f, 0.34f, -0.45f };
        addTri(v, idx, cFront, cTop, cRight, 0.5f);
        addTri(v, idx, cFront, cLeft, cTop, 0.5f);
        addTri(v, idx, cTop, cBack, cRight, 0.5f);
        addTri(v, idx, cLeft, cBack, cTop, 0.5f);

        // Swept wings (armor = 0.1)
        for (int side : new int[] { -1, 1 }) {
            float[] wRootF = { side * 0.42f, 0.04f, 0.15f };
            float[] wRootB = { side * 0.40f, 0.04f, -0.95f };
            float[] wTipF = { side * 1.45f, -0.06f, -0.55f };
            float[] wTipB = { side * 1.35f, -0.06f, -1.15f };
            float[] wRootTop = { side * 0.40f, 0.14f, -0.4f };

            if (side > 0) {
                addQuad(v, idx, wRootF, wRootB, wTipB, wTipF, 0.1f);
                addQuad(v, idx, wTipF, wTipB, wRootB, wRootF, 0.1f);
                addTri(v, idx, wRootF, wRootTop, wTipF, 0.5f);
            } else {
                addQuad(v, idx, wTipF, wTipB, wRootB, wRootF, 0.1f);
                addQuad(v, idx, wRootF, wRootB, wTipB, wTipF, 0.1f);
                addTri(v, idx, wTipF, wRootTop, wRootF, 0.5f);
            }

            // Wingtip accent glow (0.9)
            float[] tA = { side * 1.38f, -0.04f, -0.60f };
            float[] tB = { side * 1.44f, -0.06f, -1.12f };
            float[] tC = { side * 1.32f, -0.08f, -1.12f };
            addTri(v, idx, tA, tB, tC, 0.9f);

            // Engine nacelles + glowing thruster nozzles (0.9)
            float nx = side * 0.32f;
            float ny = 0.03f;
            float nz = -1.26f;
            float r = 0.14f;
            float[] n0 = { nx - r, ny + r, nz };
            float[] n1 = { nx + r, ny + r, nz };
            float[] n2 = { nx + r, ny - r, nz };
            float[] n3 = { nx - r, ny - r, nz };
            addQuad(v, idx, n0, n3, n2, n1, 0.9f);
        }

        return buildIndexed(toFloatArray(v), toIntArray(idx), GL_TRIANGLES);
    }

    private static void addQuad(List<Float> v, List<Integer> idx, float[] a, float[] b, float[] c, float[] d, float mat) {
        addTri(v, idx, a, b, c, mat);
        addTri(v, idx, a, c, d, mat);
    }

    private static void addTri(List<Float> v, List<Integer> idx, float[] a, float[] b, float[] c, float mat) {
        float ux = b[0] - a[0], uy = b[1] - a[1], uz = b[2] - a[2];
        float vx = c[0] - a[0], vy = c[1] - a[1], vz = c[2] - a[2];
        float nx = uy * vz - uz * vy;
        float ny = uz * vx - ux * vz;
        float nz = ux * vy - uy * vx;
        float len = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len > 1e-6f) {
            nx /= len; ny /= len; nz /= len;
        }
        int base = v.size() / 8;
        float[][] pts = { a, b, c };
        for (float[] p : pts) {
            v.add(p[0]); v.add(p[1]); v.add(p[2]);
            v.add(nx); v.add(ny); v.add(nz);
            v.add(mat); v.add(0.5f);
        }
        idx.add(base); idx.add(base + 1); idx.add(base + 2);
    }

    private static float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private static int[] toIntArray(List<Integer> list) {
        int[] arr = new int[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }
}
