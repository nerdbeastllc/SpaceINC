package spaceinc;

import org.joml.Vector3f;

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

    public StarType starType = StarType.G_SOL_YELLOW;
    public final Vector3f starPos = new Vector3f(0f, 0f, 0f);

    // Planet 1: Terran / Continental world
    public final float terranOrbitRadius = 68.0f;
    public float terranAngle = 0.65f;
    public final float terranRadius = 4.2f;
    public final Vector3f terranPos = new Vector3f();

    // Moon orbiting Terran world
    public final float moonOrbitRadius = 9.5f;
    public float moonAngle = 1.2f;
    public final float moonRadius = 0.95f;
    public final Vector3f moonPos = new Vector3f();

    // Planet 2: Ringed Gas Giant
    public final float giantOrbitRadius = 132.0f;
    public float giantAngle = 2.55f;
    public final float giantRadius = 8.4f;
    public final Vector3f giantPos = new Vector3f();

    public void update(float dt) {
        terranAngle += dt * 0.032f;
        moonAngle += dt * 0.28f;
        giantAngle += dt * 0.014f;

        terranPos.set(
            (float) Math.cos(terranAngle) * terranOrbitRadius,
            0f,
            (float) Math.sin(terranAngle) * terranOrbitRadius
        );

        moonPos.set(
            terranPos.x + (float) Math.cos(moonAngle) * moonOrbitRadius,
            (float) Math.sin(moonAngle * 0.7f) * 1.1f,
            terranPos.z + (float) Math.sin(moonAngle) * moonOrbitRadius
        );

        giantPos.set(
            (float) Math.cos(giantAngle) * giantOrbitRadius,
            0f,
            (float) Math.sin(giantAngle) * giantOrbitRadius
        );
    }
}
