package spaceinc;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class StarSystem {
    public enum StarType {
        O_HYPERGIANT(
            "ZETA-CLASS O3V (BLUE HYPERGIANT)",
            20.5f,
            new Vector3f(0.92f, 0.96f, 1.00f),
            new Vector3f(0.24f, 0.44f, 1.00f),
            new Vector3f(0.03f, 0.06f, 0.28f),
            new Vector3f(0.36f, 0.56f, 1.00f),
            new Vector3f(0.80f, 0.88f, 1.00f),
            10.5f, 0.96f
        ),
        B_BLUE_SUPERGIANT(
            "RIGEL-CLASS B8IA (BLUE SUPERGIANT)",
            18.5f,
            new Vector3f(0.85f, 0.96f, 1.00f),
            new Vector3f(0.16f, 0.56f, 1.00f),
            new Vector3f(0.03f, 0.14f, 0.42f),
            new Vector3f(0.26f, 0.68f, 1.00f),
            new Vector3f(0.76f, 0.90f, 1.00f),
            9.5f, 0.88f
        ),
        A_WHITE_BLUE(
            "SIRIUS-CLASS A1V (WHITE-BLUE STAR)",
            15.5f,
            new Vector3f(0.98f, 0.99f, 1.00f),
            new Vector3f(0.48f, 0.78f, 1.00f),
            new Vector3f(0.08f, 0.22f, 0.44f),
            new Vector3f(0.62f, 0.84f, 1.00f),
            new Vector3f(0.92f, 0.96f, 1.00f),
            8.2f, 0.86f
        ),
        F_YELLOW_WHITE(
            "PROCYON-CLASS F5V (YELLOW-WHITE)",
            14.8f,
            new Vector3f(1.00f, 0.98f, 0.86f),
            new Vector3f(0.92f, 0.68f, 0.28f),
            new Vector3f(0.24f, 0.12f, 0.03f),
            new Vector3f(1.00f, 0.82f, 0.46f),
            new Vector3f(1.00f, 0.96f, 0.88f),
            7.4f, 0.84f
        ),
        G_SOL_YELLOW(
            "SOL-CLASS G2V (YELLOW DWARF)",
            14.5f,
            new Vector3f(1.00f, 0.96f, 0.64f),
            new Vector3f(0.95f, 0.26f, 0.01f),
            new Vector3f(0.18f, 0.012f, 0.002f),
            new Vector3f(0.98f, 0.34f, 0.04f),
            new Vector3f(1.00f, 0.92f, 0.78f),
            6.8f, 0.82f
        ),
        K_ORANGE_DWARF(
            "EPSILON-CLASS K1V (ORANGE DWARF)",
            12.5f,
            new Vector3f(1.00f, 0.78f, 0.36f),
            new Vector3f(0.88f, 0.22f, 0.02f),
            new Vector3f(0.16f, 0.018f, 0.004f),
            new Vector3f(0.95f, 0.30f, 0.05f),
            new Vector3f(1.00f, 0.78f, 0.54f),
            5.8f, 0.78f
        ),
        M_RED_DWARF(
            "PROXIMA-CLASS M5V (RED DWARF)",
            9.8f,
            new Vector3f(1.00f, 0.48f, 0.16f),
            new Vector3f(0.72f, 0.08f, 0.02f),
            new Vector3f(0.12f, 0.008f, 0.006f),
            new Vector3f(0.85f, 0.14f, 0.03f),
            new Vector3f(1.00f, 0.56f, 0.38f),
            4.6f, 0.72f
        ),
        M_RED_GIANT(
            "BETELGEUSE-CLASS M1IA (RED GIANT)",
            23.0f,
            new Vector3f(1.00f, 0.62f, 0.24f),
            new Vector3f(0.86f, 0.14f, 0.03f),
            new Vector3f(0.18f, 0.02f, 0.01f),
            new Vector3f(0.95f, 0.22f, 0.05f),
            new Vector3f(1.00f, 0.65f, 0.42f),
            3.6f, 0.74f
        ),
        L_BROWN_DWARF(
            "TEIDE-CLASS L4 (BROWN DWARF)",
            8.2f,
            new Vector3f(0.92f, 0.26f, 0.32f),
            new Vector3f(0.42f, 0.05f, 0.14f),
            new Vector3f(0.07f, 0.008f, 0.024f),
            new Vector3f(0.62f, 0.10f, 0.22f),
            new Vector3f(0.82f, 0.42f, 0.48f),
            4.2f, 0.58f
        ),
        WOLF_RAYET(
            "WR-104 WN5 (WOLF-RAYET STAR)",
            13.5f,
            new Vector3f(0.82f, 1.00f, 0.96f),
            new Vector3f(0.10f, 0.68f, 0.72f),
            new Vector3f(0.02f, 0.12f, 0.22f),
            new Vector3f(0.18f, 0.85f, 0.88f),
            new Vector3f(0.76f, 0.98f, 0.96f),
            11.5f, 0.94f
        ),
        WHITE_DWARF(
            "SIRIUS-B DA2 (WHITE DWARF)",
            5.8f,
            new Vector3f(1.00f, 1.00f, 1.00f),
            new Vector3f(0.55f, 0.82f, 1.00f),
            new Vector3f(0.14f, 0.26f, 0.46f),
            new Vector3f(0.72f, 0.90f, 1.00f),
            new Vector3f(0.95f, 0.98f, 1.00f),
            12.0f, 1.05f
        ),
        NEUTRON_MAGNETAR(
            "CRAB-PSR B0531 (MAGNETAR)",
            4.5f,
            new Vector3f(0.88f, 0.95f, 1.00f),
            new Vector3f(0.44f, 0.18f, 0.96f),
            new Vector3f(0.06f, 0.02f, 0.24f),
            new Vector3f(0.56f, 0.34f, 1.00f),
            new Vector3f(0.78f, 0.72f, 1.00f),
            14.0f, 1.08f
        );

        public final String displayName;
        public final float radius;
        public final Vector3f hotColor;
        public final Vector3f midColor;
        public final Vector3f coolColor;
        public final Vector3f coronaColor;
        public final Vector3f lightColor;
        public final float granulationScale;
        public final float hdrIntensity;

        StarType(String displayName, float radius, Vector3f hotColor, Vector3f midColor,
                 Vector3f coolColor, Vector3f coronaColor, Vector3f lightColor,
                 float granulationScale, float hdrIntensity) {
            this.displayName = displayName;
            this.radius = radius;
            this.hotColor = hotColor;
            this.midColor = midColor;
            this.coolColor = coolColor;
            this.coronaColor = coronaColor;
            this.lightColor = lightColor;
            this.granulationScale = granulationScale;
            this.hdrIntensity = hdrIntensity;
        }
    }

    public static class Moon {
        public final String name;
        public final int planetType;
        public final float orbitRadius;
        public float angle;
        public final float orbitSpeed;
        public final float inclination;
        public final float radius;
        public final Vector3f colorA;
        public final Vector3f colorB;
        public final Vector3f colorC;
        public final Vector3f position = new Vector3f();
        public Mesh orbitMesh;

        public Moon(String name, int planetType, float orbitRadius, float startAngle,
                    float orbitSpeed, float inclination, float radius,
                    Vector3f colorA, Vector3f colorB, Vector3f colorC) {
            this.name = name;
            this.planetType = planetType;
            this.orbitRadius = orbitRadius;
            this.angle = startAngle;
            this.orbitSpeed = orbitSpeed;
            this.inclination = inclination;
            this.radius = radius;
            this.colorA = colorA;
            this.colorB = colorB;
            this.colorC = colorC;
        }
    }

    public static class Planet {
        public final String name;
        public final int planetType;
        public final float orbitRadius;
        public float angle;
        public final float orbitSpeed;
        public final float radius;
        public final float axialTilt;
        public final float spinSpeed;
        public final boolean hasRings;
        public final boolean isBinaryPluto;
        public final float binaryOffset;
        public final Vector3f colorA;
        public final Vector3f colorB;
        public final Vector3f colorC;

        public final Vector3f barycenter = new Vector3f();
        public final Vector3f position = new Vector3f();
        public final List<Moon> moons = new ArrayList<>();
        public Mesh orbitMesh;
        public Mesh ringMesh;

        public Planet(String name, int planetType, float orbitRadius, float startAngle,
                      float orbitSpeed, float radius, float axialTilt, float spinSpeed,
                      boolean hasRings, boolean isBinaryPluto, float binaryOffset,
                      Vector3f colorA, Vector3f colorB, Vector3f colorC) {
            this.name = name;
            this.planetType = planetType;
            this.orbitRadius = orbitRadius;
            this.angle = startAngle;
            this.orbitSpeed = orbitSpeed;
            this.radius = radius;
            this.axialTilt = axialTilt;
            this.spinSpeed = spinSpeed;
            this.hasRings = hasRings;
            this.isBinaryPluto = isBinaryPluto;
            this.binaryOffset = binaryOffset;
            this.colorA = colorA;
            this.colorB = colorB;
            this.colorC = colorC;
        }

        public Planet addMoon(Moon m) {
            moons.add(m);
            return this;
        }
    }

    public StarType starType = StarType.G_SOL_YELLOW;
    public final Vector3f starPos = new Vector3f(0f, 0f, 0f);
    public final List<Planet> planets = new ArrayList<>();

    public StarSystem() {
        // 1. Mercury
        planets.add(new Planet("MERCURY", 2, 60f, 0.4f, 0.024f, 1.05f, 0.01f, 0.03f,
            false, false, 0f,
            new Vector3f(0.58f, 0.55f, 0.52f), new Vector3f(0.30f, 0.28f, 0.27f), new Vector3f(0.4f, 0.4f, 0.4f)));

        // 2. Venus
        planets.add(new Planet("VENUS", 3, 98f, 1.9f, 0.017f, 2.05f, 3.09f, -0.02f,
            false, false, 0f,
            new Vector3f(0.95f, 0.80f, 0.50f), new Vector3f(0.66f, 0.46f, 0.22f), new Vector3f(0.98f, 0.78f, 0.42f)));

        // 3. Earth + Luna
        planets.add(new Planet("EARTH", 0, 145f, 0.75f, 0.012f, 2.35f, 0.41f, 0.14f,
            false, false, 0f,
            new Vector3f(0.2f, 0.5f, 0.9f), new Vector3f(0.1f, 0.4f, 0.2f), new Vector3f(0.9f, 0.9f, 1.0f))
            .addMoon(new Moon("LUNA", 2, 5.2f, 0.8f, 0.085f, 0.09f, 0.62f,
                new Vector3f(0.68f, 0.67f, 0.65f), new Vector3f(0.32f, 0.32f, 0.34f), new Vector3f(0.5f, 0.5f, 0.55f))));

        // 4. Mars + Phobos & Deimos
        planets.add(new Planet("MARS", 4, 198f, 2.8f, 0.0085f, 1.55f, 0.44f, 0.13f,
            false, false, 0f,
            new Vector3f(0.84f, 0.36f, 0.18f), new Vector3f(0.42f, 0.16f, 0.09f), new Vector3f(0.92f, 0.52f, 0.32f))
            .addMoon(new Moon("PHOBOS", 2, 2.8f, 0.5f, 0.16f, 0.04f, 0.24f,
                new Vector3f(0.48f, 0.42f, 0.38f), new Vector3f(0.24f, 0.20f, 0.18f), new Vector3f(0.4f, 0.4f, 0.4f)))
            .addMoon(new Moon("DEIMOS", 2, 4.1f, 3.1f, 0.10f, 0.06f, 0.18f,
                new Vector3f(0.52f, 0.46f, 0.42f), new Vector3f(0.26f, 0.22f, 0.20f), new Vector3f(0.4f, 0.4f, 0.4f))));

        // 5. Jupiter + 4 Galilean Moons (Io, Europa, Ganymede, Callisto)
        planets.add(new Planet("JUPITER", 1, 305f, 1.25f, 0.0048f, 7.0f, 0.05f, 0.24f,
            false, false, 0f,
            new Vector3f(0.88f, 0.78f, 0.66f), new Vector3f(0.66f, 0.42f, 0.24f), new Vector3f(0.78f, 0.28f, 0.16f))
            .addMoon(new Moon("IO", 6, 10.6f, 0.2f, 0.135f, 0.01f, 0.62f,
                new Vector3f(0.96f, 0.82f, 0.24f), new Vector3f(0.58f, 0.32f, 0.08f), new Vector3f(1.25f, 0.38f, 0.05f)))
            .addMoon(new Moon("EUROPA", 7, 13.6f, 1.8f, 0.095f, 0.02f, 0.55f,
                new Vector3f(0.88f, 0.86f, 0.82f), new Vector3f(0.64f, 0.58f, 0.52f), new Vector3f(0.72f, 0.44f, 0.30f)))
            .addMoon(new Moon("GANYMEDE", 2, 17.2f, 3.5f, 0.065f, 0.02f, 0.85f,
                new Vector3f(0.62f, 0.58f, 0.52f), new Vector3f(0.32f, 0.30f, 0.28f), new Vector3f(0.5f, 0.5f, 0.5f)))
            .addMoon(new Moon("CALLISTO", 2, 21.5f, 5.1f, 0.044f, 0.03f, 0.76f,
                new Vector3f(0.46f, 0.44f, 0.42f), new Vector3f(0.22f, 0.21f, 0.22f), new Vector3f(0.4f, 0.4f, 0.4f))));

        // 6. Saturn + Rings + 7 Major Moons (Mimas, Enceladus, Tethys, Dione, Rhea, Titan, Iapetus)
        planets.add(new Planet("SATURN", 1, 435f, 2.55f, 0.0032f, 5.8f, 0.466f, 0.22f,
            true, false, 0f,
            new Vector3f(0.90f, 0.82f, 0.64f), new Vector3f(0.72f, 0.60f, 0.42f), new Vector3f(0.56f, 0.48f, 0.36f))
            .addMoon(new Moon("MIMAS", 2, 14.8f, 0.4f, 0.145f, 0.03f, 0.24f,
                new Vector3f(0.76f, 0.76f, 0.78f), new Vector3f(0.44f, 0.44f, 0.46f), new Vector3f(0.6f, 0.6f, 0.6f)))
            .addMoon(new Moon("ENCELADUS", 7, 16.5f, 1.5f, 0.120f, 0.01f, 0.28f,
                new Vector3f(0.96f, 0.98f, 1.00f), new Vector3f(0.74f, 0.84f, 0.94f), new Vector3f(0.52f, 0.84f, 1.00f)))
            .addMoon(new Moon("TETHYS", 2, 18.4f, 2.7f, 0.098f, 0.02f, 0.34f,
                new Vector3f(0.84f, 0.84f, 0.86f), new Vector3f(0.52f, 0.52f, 0.56f), new Vector3f(0.7f, 0.7f, 0.75f)))
            .addMoon(new Moon("DIONE", 7, 20.5f, 3.9f, 0.080f, 0.01f, 0.36f,
                new Vector3f(0.82f, 0.82f, 0.84f), new Vector3f(0.48f, 0.48f, 0.52f), new Vector3f(0.76f, 0.78f, 0.82f)))
            .addMoon(new Moon("RHEA", 2, 23.0f, 5.0f, 0.062f, 0.01f, 0.44f,
                new Vector3f(0.74f, 0.72f, 0.70f), new Vector3f(0.42f, 0.40f, 0.40f), new Vector3f(0.6f, 0.6f, 0.6f)))
            .addMoon(new Moon("TITAN", 7, 27.0f, 0.9f, 0.042f, 0.01f, 0.92f,
                new Vector3f(0.92f, 0.62f, 0.24f), new Vector3f(0.68f, 0.42f, 0.14f), new Vector3f(1.00f, 0.68f, 0.26f)))
            .addMoon(new Moon("IAPETUS", 2, 32.0f, 4.4f, 0.026f, 0.27f, 0.42f,
                new Vector3f(0.82f, 0.80f, 0.76f), new Vector3f(0.18f, 0.15f, 0.14f), new Vector3f(0.5f, 0.5f, 0.5f))));

        // 7. Uranus (odd ~98° sideways axial tilt = 1.71 rad) + Rings + Oberon & major moons
        planets.add(new Planet("URANUS", 5, 560f, 4.1f, 0.0020f, 4.1f, 1.71f, 0.18f,
            true, false, 0f,
            new Vector3f(0.56f, 0.88f, 0.92f), new Vector3f(0.32f, 0.68f, 0.76f), new Vector3f(0.80f, 0.96f, 0.98f))
            .addMoon(new Moon("MIRANDA", 2, 7.6f, 0.6f, 0.11f, 0.07f, 0.24f,
                new Vector3f(0.68f, 0.70f, 0.74f), new Vector3f(0.38f, 0.40f, 0.44f), new Vector3f(0.5f, 0.5f, 0.6f)))
            .addMoon(new Moon("ARIEL", 7, 9.2f, 2.1f, 0.085f, 0.01f, 0.34f,
                new Vector3f(0.80f, 0.82f, 0.86f), new Vector3f(0.50f, 0.52f, 0.56f), new Vector3f(0.75f, 0.82f, 0.90f)))
            .addMoon(new Moon("UMBRIEL", 2, 11.0f, 3.8f, 0.064f, 0.01f, 0.34f,
                new Vector3f(0.42f, 0.44f, 0.48f), new Vector3f(0.20f, 0.22f, 0.25f), new Vector3f(0.4f, 0.4f, 0.4f)))
            .addMoon(new Moon("TITANIA", 7, 13.4f, 5.2f, 0.046f, 0.01f, 0.46f,
                new Vector3f(0.76f, 0.74f, 0.74f), new Vector3f(0.46f, 0.44f, 0.46f), new Vector3f(0.72f, 0.74f, 0.78f)))
            .addMoon(new Moon("OBERON", 2, 16.0f, 1.3f, 0.034f, 0.01f, 0.44f,
                new Vector3f(0.66f, 0.60f, 0.58f), new Vector3f(0.36f, 0.32f, 0.32f), new Vector3f(0.6f, 0.6f, 0.6f))));

        // 8. Neptune + Triton (retrograde, inclined orbit)
        planets.add(new Planet("NEPTUNE", 5, 665f, 5.3f, 0.0014f, 3.9f, 0.49f, 0.19f,
            false, false, 0f,
            new Vector3f(0.22f, 0.46f, 0.94f), new Vector3f(0.10f, 0.22f, 0.68f), new Vector3f(0.72f, 0.88f, 1.00f))
            .addMoon(new Moon("TRITON", 7, 9.6f, 2.2f, -0.052f, 0.44f, 0.58f,
                new Vector3f(0.84f, 0.78f, 0.76f), new Vector3f(0.56f, 0.48f, 0.50f), new Vector3f(0.88f, 0.86f, 0.94f))));

        // 9. Pluto & Charon Binary Dwarf Planet System
        planets.add(new Planet("PLUTO & CHARON", 7, 770f, 0.18f, 0.00095f, 1.05f, 2.13f, 0.06f,
            false, true, 1.15f,
            new Vector3f(0.82f, 0.72f, 0.62f), new Vector3f(0.46f, 0.30f, 0.22f), new Vector3f(0.92f, 0.88f, 0.84f))
            .addMoon(new Moon("CHARON", 2, 2.65f, 0.0f, 0.075f, 0.08f, 0.56f,
                new Vector3f(0.58f, 0.54f, 0.52f), new Vector3f(0.28f, 0.24f, 0.24f), new Vector3f(0.5f, 0.5f, 0.5f))));
    }

    public void initMeshes() {
        for (Planet p : planets) {
            p.orbitMesh = Mesh.createOrbitCircle(240, p.orbitRadius);
            if (p.hasRings) {
                if ("URANUS".equals(p.name)) {
                    p.ringMesh = Mesh.createRing(128, 1.45f, 1.95f);
                } else {
                    p.ringMesh = Mesh.createRing(140, 1.32f, 2.38f);
                }
            }
            for (Moon m : p.moons) {
                m.orbitMesh = Mesh.createOrbitCircle(96, m.orbitRadius);
            }
        }
    }

    public void update(float dt) {
        for (Planet p : planets) {
            p.angle += dt * p.orbitSpeed;
            p.barycenter.set(
                (float) Math.cos(p.angle) * p.orbitRadius,
                0f,
                (float) Math.sin(p.angle) * p.orbitRadius
            );

            if (p.isBinaryPluto && !p.moons.isEmpty()) {
                // Binary dwarf planet dance around shared barycenter
                Moon charon = p.moons.get(0);
                charon.angle += dt * charon.orbitSpeed;
                float cosA = (float) Math.cos(charon.angle);
                float sinA = (float) Math.sin(charon.angle);
                // Pluto on one side of barycenter, Charon on the opposite side
                p.position.set(
                    p.barycenter.x - cosA * p.binaryOffset,
                    p.barycenter.y - sinA * p.binaryOffset * 0.12f,
                    p.barycenter.z - sinA * p.binaryOffset
                );
                charon.position.set(
                    p.barycenter.x + cosA * charon.orbitRadius,
                    p.barycenter.y + sinA * charon.orbitRadius * 0.12f,
                    p.barycenter.z + sinA * charon.orbitRadius
                );
            } else {
                p.position.set(p.barycenter);
                // Moons orbit in the planet's tilted equatorial plane + moon inclination
                Matrix4f tiltMat = new Matrix4f().rotateZ(p.axialTilt);
                Vector3f localOffset = new Vector3f();
                for (Moon m : p.moons) {
                    m.angle += dt * m.orbitSpeed;
                    localOffset.set(
                        (float) Math.cos(m.angle) * m.orbitRadius,
                        (float) Math.sin(m.angle) * m.orbitRadius * (float) Math.sin(m.inclination),
                        (float) Math.sin(m.angle) * m.orbitRadius * (float) Math.cos(m.inclination)
                    );
                    tiltMat.transformPosition(localOffset);
                    m.position.set(p.position).add(localOffset);
                }
            }
        }
    }

    public void dispose() {
        for (Planet p : planets) {
            if (p.orbitMesh != null) p.orbitMesh.dispose();
            if (p.ringMesh != null) p.ringMesh.dispose();
            for (Moon m : p.moons) {
                if (m.orbitMesh != null) m.orbitMesh.dispose();
            }
        }
    }
}
