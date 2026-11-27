package spaceinc;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StarSystem {
    public enum StarType {
        O_HYPERGIANT("ZETA-CLASS O3V (BLUE HYPERGIANT)", 20.5f, 10.5f, 0.96f,
            new Vector3f(0.92f, 0.96f, 1.00f), new Vector3f(0.24f, 0.44f, 1.00f),
            new Vector3f(0.03f, 0.06f, 0.28f), new Vector3f(0.36f, 0.56f, 1.00f), new Vector3f(0.80f, 0.88f, 1.00f)),
        B_BLUE_SUPERGIANT("RIGEL-CLASS B8IA (BLUE SUPERGIANT)", 18.5f, 9.5f, 0.88f,
            new Vector3f(0.85f, 0.96f, 1.00f), new Vector3f(0.16f, 0.56f, 1.00f),
            new Vector3f(0.03f, 0.14f, 0.42f), new Vector3f(0.26f, 0.68f, 1.00f), new Vector3f(0.76f, 0.90f, 1.00f)),
        A_WHITE_BLUE("SIRIUS-CLASS A1V (WHITE-BLUE STAR)", 15.5f, 8.2f, 0.86f,
            new Vector3f(0.98f, 0.99f, 1.00f), new Vector3f(0.48f, 0.78f, 1.00f),
            new Vector3f(0.08f, 0.22f, 0.44f), new Vector3f(0.62f, 0.84f, 1.00f), new Vector3f(0.92f, 0.96f, 1.00f)),
        F_YELLOW_WHITE("PROCYON-CLASS F5V (YELLOW-WHITE)", 14.8f, 7.4f, 0.84f,
            new Vector3f(1.00f, 0.98f, 0.86f), new Vector3f(0.92f, 0.68f, 0.28f),
            new Vector3f(0.24f, 0.12f, 0.03f), new Vector3f(1.00f, 0.82f, 0.46f), new Vector3f(1.00f, 0.96f, 0.88f)),
        G_SOL_YELLOW("SOL-CLASS G2V (YELLOW DWARF)", 14.5f, 6.8f, 0.82f,
            new Vector3f(1.00f, 0.96f, 0.64f), new Vector3f(0.95f, 0.26f, 0.01f),
            new Vector3f(0.18f, 0.012f, 0.002f), new Vector3f(0.98f, 0.34f, 0.04f), new Vector3f(1.00f, 0.92f, 0.78f)),
        K_ORANGE_DWARF("EPSILON-CLASS K1V (ORANGE DWARF)", 12.5f, 5.8f, 0.78f,
            new Vector3f(1.00f, 0.78f, 0.36f), new Vector3f(0.88f, 0.22f, 0.02f),
            new Vector3f(0.16f, 0.018f, 0.004f), new Vector3f(0.95f, 0.30f, 0.05f), new Vector3f(1.00f, 0.78f, 0.54f)),
        M_RED_DWARF("PROXIMA-CLASS M5V (RED DWARF)", 9.8f, 4.6f, 0.72f,
            new Vector3f(1.00f, 0.48f, 0.16f), new Vector3f(0.72f, 0.08f, 0.02f),
            new Vector3f(0.12f, 0.008f, 0.006f), new Vector3f(0.85f, 0.14f, 0.03f), new Vector3f(1.00f, 0.56f, 0.38f)),
        M_RED_GIANT("BETELGEUSE-CLASS M1IA (RED GIANT)", 23.0f, 3.6f, 0.74f,
            new Vector3f(1.00f, 0.62f, 0.24f), new Vector3f(0.86f, 0.14f, 0.03f),
            new Vector3f(0.18f, 0.02f, 0.01f), new Vector3f(0.95f, 0.22f, 0.05f), new Vector3f(1.00f, 0.65f, 0.42f)),
        L_BROWN_DWARF("TEIDE-CLASS L4 (BROWN DWARF)", 8.2f, 4.2f, 0.58f,
            new Vector3f(0.92f, 0.26f, 0.32f), new Vector3f(0.42f, 0.05f, 0.14f),
            new Vector3f(0.07f, 0.008f, 0.024f), new Vector3f(0.62f, 0.10f, 0.22f), new Vector3f(0.82f, 0.42f, 0.48f)),
        WOLF_RAYET("WR-104 WN5 (WOLF-RAYET STAR)", 13.5f, 11.5f, 0.94f,
            new Vector3f(0.82f, 1.00f, 0.96f), new Vector3f(0.10f, 0.68f, 0.72f),
            new Vector3f(0.02f, 0.12f, 0.22f), new Vector3f(0.18f, 0.85f, 0.88f), new Vector3f(0.76f, 0.98f, 0.96f)),
        WHITE_DWARF("SIRIUS-B DA2 (WHITE DWARF)", 5.8f, 12.0f, 1.05f,
            new Vector3f(1.00f, 1.00f, 1.00f), new Vector3f(0.55f, 0.82f, 1.00f),
            new Vector3f(0.14f, 0.26f, 0.46f), new Vector3f(0.72f, 0.90f, 1.00f), new Vector3f(0.95f, 0.98f, 1.00f)),
        NEUTRON_MAGNETAR("CRAB-PSR B0531 (MAGNETAR)", 4.5f, 14.0f, 1.08f,
            new Vector3f(0.88f, 0.95f, 1.00f), new Vector3f(0.44f, 0.18f, 0.96f),
            new Vector3f(0.06f, 0.02f, 0.24f), new Vector3f(0.56f, 0.34f, 1.00f), new Vector3f(0.78f, 0.72f, 1.00f));

        public final String displayName;
        public final float radius;
        public final float granulationScale;
        public final float hdrIntensity;
        public final Vector3f hotColor;
        public final Vector3f midColor;
        public final Vector3f coolColor;
        public final Vector3f coronaColor;
        public final Vector3f lightColor;

        StarType(String displayName, float radius, float granulationScale, float hdrIntensity,
                 Vector3f hotColor, Vector3f midColor, Vector3f coolColor, Vector3f coronaColor, Vector3f lightColor) {
            this.displayName = displayName;
            this.radius = radius;
            this.granulationScale = granulationScale;
            this.hdrIntensity = hdrIntensity;
            this.hotColor = hotColor;
            this.midColor = midColor;
            this.coolColor = coolColor;
            this.coronaColor = coronaColor;
            this.lightColor = lightColor;
        }
    }

    public static class BodyPalette {
        public final Vector3f colorA;
        public final Vector3f colorB;
        public final Vector3f colorC;

        public BodyPalette(Vector3f colorA, Vector3f colorB, Vector3f colorC) {
            this.colorA = saturate(colorA);
            this.colorB = saturate(colorB);
            this.colorC = saturate(colorC);
        }

        private static Vector3f saturate(Vector3f c) {
            float luma = 0.299f * c.x + 0.587f * c.y + 0.114f * c.z;
            float r = clampColor((luma + (c.x - luma) * 1.48f) * 1.08f);
            float g = clampColor((luma + (c.y - luma) * 1.48f) * 1.08f);
            float b = clampColor((luma + (c.z - luma) * 1.48f) * 1.08f);
            return new Vector3f(r, g, b);
        }

        private static float clampColor(float v) {
            return Math.max(0.0f, Math.min(1.35f, v));
        }
    }

    public static class OrbitSpec {
        public final float orbitRadius;
        public final float startAngle;
        public final float orbitSpeed;
        public final float inclinationOrTilt;

        public OrbitSpec(float orbitRadius, float startAngle, float orbitSpeed, float inclinationOrTilt) {
            this.orbitRadius = orbitRadius;
            this.startAngle = startAngle;
            this.orbitSpeed = orbitSpeed;
            this.inclinationOrTilt = inclinationOrTilt;
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
        public final String category;
        public List<MineralRegistry.OreDeposit> deposits;
        public Mesh orbitMesh;

        public Moon(String name, int planetType, float radius, OrbitSpec orbit, BodyPalette palette) {
            this.name = name;
            this.planetType = planetType;
            this.radius = radius;
            this.orbitRadius = orbit.orbitRadius;
            this.angle = orbit.startAngle;
            this.orbitSpeed = orbit.orbitSpeed;
            this.inclination = orbit.inclinationOrTilt;
            this.colorA = palette.colorA;
            this.colorB = palette.colorB;
            this.colorC = palette.colorC;
            this.category = resolveMoonCategory(name, planetType);
            this.deposits = MineralRegistry.forMoon(name, planetType, true);
        }

        private static String resolveMoonCategory(String name, int planetType) {
            if (planetType == 6) return "VOLCANIC MOON";
            if ("TITAN".equals(name)) return "HAZY HYDROCARBON MOON";
            if (planetType == 7) return "ICY OCEAN MOON";
            return "ROCKY / CRATERED MOON";
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

        public Planet(String name, int planetType, float radius, OrbitSpec orbit,
                      float spinSpeed, boolean hasRings, BodyPalette palette) {
            this(name, planetType, radius, orbit, spinSpeed, hasRings, false, 0f, palette);
        }

        public Planet(String name, int planetType, float radius, OrbitSpec orbit,
                      float spinSpeed, boolean hasRings, boolean isBinaryPluto,
                      float binaryOffset, BodyPalette palette) {
            this.name = name;
            this.planetType = planetType;
            this.radius = radius;
            this.orbitRadius = orbit.orbitRadius;
            this.angle = orbit.startAngle;
            this.orbitSpeed = orbit.orbitSpeed;
            this.axialTilt = orbit.inclinationOrTilt;
            this.spinSpeed = spinSpeed;
            this.hasRings = hasRings;
            this.isBinaryPluto = isBinaryPluto;
            this.binaryOffset = binaryOffset;
            this.colorA = palette.colorA;
            this.colorB = palette.colorB;
            this.colorC = palette.colorC;
        }

        public Planet addMoon(Moon m) {
            moons.add(m);
            return this;
        }

        public String getCategory() {
            if (isBinaryPluto) return "BINARY DWARF PLANET";
            return switch (planetType) {
                case 0 -> "TERRAN WORLD";
                case 1 -> "GAS GIANT";
                case 3 -> "VENUSIAN WORLD";
                case 4 -> "MARTIAN TERRESTRIAL";
                case 5 -> "ICE GIANT";
                default -> "ROCKY TERRESTRIAL";
            };
        }

        public List<MineralRegistry.OreDeposit> getDeposits(boolean isCoreFederation) {
            return MineralRegistry.forPlanet(name, planetType, isCoreFederation);
        }
    }

    public static class JumpGate {
        public final String targetSystemId;
        public final String targetSystemName;
        public final boolean isFrontier;
        public final Vector3f position = new Vector3f();
        public final float radius = 3.2f;

        public JumpGate(String targetSystemId, String targetSystemName, boolean isFrontier, Vector3f pos) {
            this.targetSystemId = targetSystemId;
            this.targetSystemName = targetSystemName;
            this.isFrontier = isFrontier;
            this.position.set(pos);
        }
    }

    public static class MarketStation {
        public final String id;
        public final String name;
        public final Planet hostPlanet;
        public final float orbitRadius;
        public float angle;
        public final float orbitSpeed = 0.045f;
        public final float radius = 2.4f;
        public final Vector3f position = new Vector3f();

        public MarketStation(String id, String name, Planet hostPlanet, float orbitRadius, float startAngle) {
            this.id = id;
            this.name = name;
            this.hostPlanet = hostPlanet;
            this.orbitRadius = orbitRadius;
            this.angle = startAngle;
        }
    }

    public String systemName = "SOL SYSTEM";
    public boolean isCoreFederation = true;
    public StarType starType = StarType.G_SOL_YELLOW;
    public final Vector3f starPos = new Vector3f(0f, 0f, 0f);
    public final List<Planet> planets = new ArrayList<>();
    public final List<JumpGate> jumpGates = new ArrayList<>();
    public final List<MarketStation> marketStations = new ArrayList<>();

    public StarSystem() {
        buildSolSystem();
    }

    public void loadSystem(GalaxyMap.SystemNode node, GalaxyMap map) {
        dispose();
        planets.clear();
        jumpGates.clear();
        marketStations.clear();

        this.systemName = node.name;
        this.isCoreFederation = node.isCoreFederation;
        this.starType = node.starType;

        if ("SOL".equals(node.id)) {
            buildSolSystem();
        } else {
            buildProceduralSystem(node);
        }

        refreshMoonDeposits();
        if (node.isCoreFederation) {
            spawnCoreInfrastructure(node, map);
        }
        initMeshes();
    }

    private void refreshMoonDeposits() {
        for (Planet p : planets) {
            for (Moon m : p.moons) {
                m.deposits = MineralRegistry.forMoon(m.name, m.planetType, isCoreFederation);
            }
        }
    }

    private void spawnCoreInfrastructure(GalaxyMap.SystemNode node, GalaxyMap map) {
        if (!planets.isEmpty()) {
            Planet host = ("SOL".equals(node.id) && planets.size() > 2) ? planets.get(2) : planets.get(0);
            marketStations.add(new MarketStation(
                node.id + "_MARKET", node.id + " TRADE EXCHANGE", host, host.radius * 3.8f + 3.5f, 2.2f));
        }
        float lastPlanetOrbit = planets.isEmpty() ? 100.0f : planets.get(planets.size() - 1).orbitRadius;
        float minGateR = lastPlanetOrbit + 35.0f;
        float maxGateR = Math.max(minGateR + 30.0f, 880.0f);

        int n = node.gateLinks.size();
        for (int i = 0; i < n; i++) {
            GalaxyMap.SystemNode dest = map.findById(node.gateLinks.get(i));
            float angle = 0.8f + (float) (i * (2.0 * Math.PI / Math.max(1, n)));
            float dist = minGateR + (maxGateR - minGateR) * ((i + 1.0f) / (n + 1.0f));
            jumpGates.add(new JumpGate(dest.id, dest.name, !dest.isCoreFederation,
                new Vector3f((float) Math.cos(angle) * dist, 0f, (float) Math.sin(angle) * dist)));
        }
    }

    private void buildProceduralSystem(GalaxyMap.SystemNode node) {
        Random rng = new Random(node.seed);
        int count = 4 + rng.nextInt(4);
        float r = 65f + rng.nextFloat() * 15f;

        for (int i = 0; i < count; i++) {
            int pType = pickProceduralPlanetType(rng, i, count);
            float radius = pickProceduralRadius(rng, pType);
            float tilt = (pType == 5 && rng.nextBoolean()) ? 1.55f : rng.nextFloat() * 0.45f;
            boolean rings = (pType == 1 || pType == 5) && rng.nextFloat() < 0.45f;
            BodyPalette pal = randomPalette(rng);

            String pName = node.id + "-" + (i + 1);
            Planet p = new Planet(pName, pType, radius,
                new OrbitSpec(r, rng.nextFloat() * 6.28f, 0.022f / (1.0f + i * 0.7f), tilt),
                0.08f + rng.nextFloat() * 0.14f, rings, pal);
            addProceduralMoons(p, pal, rng);
            planets.add(p);
            r += 58f + rng.nextFloat() * 48f;
        }
    }

    private static int pickProceduralPlanetType(Random rng, int idx, int total) {
        if (idx == 0) return rng.nextBoolean() ? 2 : 4;
        if (idx >= total - 2) return rng.nextBoolean() ? 1 : 5;
        if (rng.nextInt(4) == 0) return 0;
        return rng.nextBoolean() ? 3 : 4;
    }

    private static float pickProceduralRadius(Random rng, int pType) {
        if (pType == 1) return 4.8f + rng.nextFloat() * 2.5f;
        if (pType == 5) return 3.5f + rng.nextFloat() * 1.4f;
        return 1.2f + rng.nextFloat() * 1.4f;
    }

    private static BodyPalette randomPalette(Random rng) {
        Vector3f cA = new Vector3f(0.22f + rng.nextFloat() * 0.78f, 0.22f + rng.nextFloat() * 0.78f, 0.22f + rng.nextFloat() * 0.78f);
        Vector3f cB = new Vector3f(cA.x * 0.45f, cA.y * 0.45f, cA.z * 0.45f);
        Vector3f cC = new Vector3f(0.45f + rng.nextFloat() * 0.55f, 0.45f + rng.nextFloat() * 0.55f, 0.45f + rng.nextFloat() * 0.55f);
        return new BodyPalette(cA, cB, cC);
    }

    private static void addProceduralMoons(Planet p, BodyPalette pal, Random rng) {
        boolean isGiant = p.planetType == 1 || p.planetType == 5;
        int moonCount = isGiant ? (2 + rng.nextInt(3)) : (rng.nextFloat() < 0.45f ? 1 : 0);
        for (int m = 0; m < moonCount; m++) {
            int mType = pickMoonType(p.planetType, m, rng);
            p.addMoon(new Moon(p.name + " " + (char) ('A' + m), mType, 0.28f + rng.nextFloat() * 0.42f,
                new OrbitSpec(p.radius * (1.8f + m * 0.7f), rng.nextFloat() * 6.28f,
                    0.06f + rng.nextFloat() * 0.08f, (rng.nextFloat() - 0.5f) * 0.25f), pal));
        }
    }

    private static int pickMoonType(int planetType, int moonIdx, Random rng) {
        if (moonIdx == 0 && planetType == 1) return rng.nextBoolean() ? 6 : 2;
        return rng.nextBoolean() ? 7 : 2;
    }

    private void buildSolSystem() {
        buildInnerSol();
        buildOuterSol();
    }

    private void buildInnerSol() {
        planets.add(new Planet("MERCURY", 2, 1.05f, new OrbitSpec(60f, 0.4f, 0.024f, 0.01f), 0.03f, false,
            pal(0.58f, 0.55f, 0.52f, 0.30f, 0.28f, 0.27f, 0.4f, 0.4f, 0.4f)));

        planets.add(new Planet("VENUS", 3, 2.05f, new OrbitSpec(98f, 1.9f, 0.017f, 3.09f), -0.02f, false,
            pal(0.95f, 0.80f, 0.50f, 0.66f, 0.46f, 0.22f, 0.98f, 0.78f, 0.42f)));

        planets.add(new Planet("EARTH", 0, 2.35f, new OrbitSpec(145f, 0.75f, 0.012f, 0.41f), 0.14f, false,
            pal(0.2f, 0.5f, 0.9f, 0.1f, 0.4f, 0.2f, 0.9f, 0.9f, 1.0f))
            .addMoon(new Moon("LUNA", 2, 0.62f, new OrbitSpec(5.2f, 0.8f, 0.085f, 0.09f),
                pal(0.68f, 0.67f, 0.65f, 0.32f, 0.32f, 0.34f, 0.5f, 0.5f, 0.55f))));

        planets.add(new Planet("MARS", 4, 1.55f, new OrbitSpec(198f, 2.8f, 0.0085f, 0.44f), 0.13f, false,
            pal(0.84f, 0.36f, 0.18f, 0.42f, 0.16f, 0.09f, 0.92f, 0.52f, 0.32f))
            .addMoon(new Moon("PHOBOS", 2, 0.24f, new OrbitSpec(2.8f, 0.5f, 0.16f, 0.04f),
                pal(0.48f, 0.42f, 0.38f, 0.24f, 0.20f, 0.18f, 0.4f, 0.4f, 0.4f)))
            .addMoon(new Moon("DEIMOS", 2, 0.18f, new OrbitSpec(4.1f, 3.1f, 0.10f, 0.06f),
                pal(0.52f, 0.46f, 0.42f, 0.26f, 0.22f, 0.20f, 0.4f, 0.4f, 0.4f))));
    }

    private void buildOuterSol() {
        planets.add(new Planet("JUPITER", 1, 7.0f, new OrbitSpec(305f, 1.25f, 0.0048f, 0.05f), 0.24f, false,
            pal(0.88f, 0.78f, 0.66f, 0.66f, 0.42f, 0.24f, 0.78f, 0.28f, 0.16f))
            .addMoon(new Moon("IO", 6, 0.62f, new OrbitSpec(10.6f, 0.2f, 0.135f, 0.01f),
                pal(0.96f, 0.82f, 0.24f, 0.58f, 0.32f, 0.08f, 1.25f, 0.38f, 0.05f)))
            .addMoon(new Moon("EUROPA", 7, 0.55f, new OrbitSpec(13.6f, 1.8f, 0.095f, 0.02f),
                pal(0.88f, 0.86f, 0.82f, 0.64f, 0.58f, 0.52f, 0.72f, 0.44f, 0.30f)))
            .addMoon(new Moon("GANYMEDE", 2, 0.85f, new OrbitSpec(17.2f, 3.5f, 0.065f, 0.02f),
                pal(0.62f, 0.58f, 0.52f, 0.32f, 0.30f, 0.28f, 0.5f, 0.5f, 0.5f)))
            .addMoon(new Moon("CALLISTO", 2, 0.76f, new OrbitSpec(21.5f, 5.1f, 0.044f, 0.03f),
                pal(0.46f, 0.44f, 0.42f, 0.22f, 0.21f, 0.22f, 0.4f, 0.4f, 0.4f))));

        planets.add(new Planet("SATURN", 1, 5.8f, new OrbitSpec(435f, 2.55f, 0.0032f, 0.466f), 0.22f, true,
            pal(0.90f, 0.82f, 0.64f, 0.72f, 0.60f, 0.42f, 0.56f, 0.48f, 0.36f))
            .addMoon(new Moon("MIMAS", 2, 0.24f, new OrbitSpec(14.8f, 0.4f, 0.145f, 0.03f),
                pal(0.76f, 0.76f, 0.78f, 0.44f, 0.44f, 0.46f, 0.6f, 0.6f, 0.6f)))
            .addMoon(new Moon("ENCELADUS", 7, 0.28f, new OrbitSpec(16.5f, 1.5f, 0.120f, 0.01f),
                pal(0.96f, 0.98f, 1.00f, 0.74f, 0.84f, 0.94f, 0.52f, 0.84f, 1.00f)))
            .addMoon(new Moon("TETHYS", 2, 0.34f, new OrbitSpec(18.4f, 2.7f, 0.098f, 0.02f),
                pal(0.84f, 0.84f, 0.86f, 0.52f, 0.52f, 0.56f, 0.7f, 0.7f, 0.75f)))
            .addMoon(new Moon("DIONE", 7, 0.36f, new OrbitSpec(20.5f, 3.9f, 0.080f, 0.01f),
                pal(0.82f, 0.82f, 0.84f, 0.48f, 0.48f, 0.52f, 0.76f, 0.78f, 0.82f)))
            .addMoon(new Moon("RHEA", 2, 0.44f, new OrbitSpec(23.0f, 5.0f, 0.062f, 0.01f),
                pal(0.74f, 0.72f, 0.70f, 0.42f, 0.40f, 0.40f, 0.6f, 0.6f, 0.6f)))
            .addMoon(new Moon("TITAN", 7, 0.92f, new OrbitSpec(27.0f, 0.9f, 0.042f, 0.01f),
                pal(0.92f, 0.62f, 0.24f, 0.68f, 0.42f, 0.14f, 1.00f, 0.68f, 0.26f)))
            .addMoon(new Moon("IAPETUS", 2, 0.42f, new OrbitSpec(32.0f, 4.4f, 0.026f, 0.27f),
                pal(0.82f, 0.80f, 0.76f, 0.18f, 0.15f, 0.14f, 0.5f, 0.5f, 0.5f))));

        planets.add(new Planet("URANUS", 5, 4.1f, new OrbitSpec(560f, 4.1f, 0.0020f, 1.71f), 0.18f, true,
            pal(0.56f, 0.88f, 0.92f, 0.32f, 0.68f, 0.76f, 0.80f, 0.96f, 0.98f))
            .addMoon(new Moon("MIRANDA", 2, 0.24f, new OrbitSpec(7.6f, 0.6f, 0.11f, 0.07f),
                pal(0.68f, 0.70f, 0.74f, 0.38f, 0.40f, 0.44f, 0.5f, 0.5f, 0.6f)))
            .addMoon(new Moon("ARIEL", 7, 0.34f, new OrbitSpec(9.2f, 2.1f, 0.085f, 0.01f),
                pal(0.80f, 0.82f, 0.86f, 0.50f, 0.52f, 0.56f, 0.75f, 0.82f, 0.90f)))
            .addMoon(new Moon("UMBRIEL", 2, 0.34f, new OrbitSpec(11.0f, 3.8f, 0.064f, 0.01f),
                pal(0.42f, 0.44f, 0.48f, 0.20f, 0.22f, 0.25f, 0.4f, 0.4f, 0.4f)))
            .addMoon(new Moon("TITANIA", 7, 0.46f, new OrbitSpec(13.4f, 5.2f, 0.046f, 0.01f),
                pal(0.76f, 0.74f, 0.74f, 0.46f, 0.44f, 0.46f, 0.72f, 0.74f, 0.78f)))
            .addMoon(new Moon("OBERON", 2, 0.44f, new OrbitSpec(16.0f, 1.3f, 0.034f, 0.01f),
                pal(0.66f, 0.60f, 0.58f, 0.36f, 0.32f, 0.32f, 0.6f, 0.6f, 0.6f))));

        planets.add(new Planet("NEPTUNE", 5, 3.9f, new OrbitSpec(665f, 5.3f, 0.0014f, 0.49f), 0.19f, false,
            pal(0.22f, 0.46f, 0.94f, 0.10f, 0.22f, 0.68f, 0.72f, 0.88f, 1.00f))
            .addMoon(new Moon("TRITON", 7, 0.58f, new OrbitSpec(9.6f, 2.2f, -0.052f, 0.44f),
                pal(0.84f, 0.78f, 0.76f, 0.56f, 0.48f, 0.50f, 0.88f, 0.86f, 0.94f))));

        planets.add(new Planet("PLUTO & CHARON", 7, 1.05f, new OrbitSpec(770f, 0.18f, 0.00095f, 2.13f), 0.06f, false, true, 1.15f,
            pal(0.82f, 0.72f, 0.62f, 0.46f, 0.30f, 0.22f, 0.92f, 0.88f, 0.84f))
            .addMoon(new Moon("CHARON", 2, 0.56f, new OrbitSpec(2.65f, 0.0f, 0.075f, 0.08f),
                pal(0.58f, 0.54f, 0.52f, 0.28f, 0.24f, 0.24f, 0.5f, 0.5f, 0.5f))));
    }

    private static BodyPalette pal(float... c) {
        return new BodyPalette(new Vector3f(c[0], c[1], c[2]), new Vector3f(c[3], c[4], c[5]), new Vector3f(c[6], c[7], c[8]));
    }

    public void initMeshes() {
        for (Planet p : planets) {
            p.orbitMesh = Mesh.createOrbitCircle(240, p.orbitRadius);
            if (p.hasRings) {
                p.ringMesh = "URANUS".equals(p.name)
                    ? Mesh.createRing(128, 1.45f, 1.95f)
                    : Mesh.createRing(140, 1.32f, 2.38f);
            }
            for (Moon m : p.moons) {
                m.orbitMesh = Mesh.createOrbitCircle(96, m.orbitRadius);
            }
        }
    }

    public void update(float dt) {
        float orbitDt = dt / 3.0f;
        for (Planet p : planets) {
            updatePlanet(p, orbitDt);
        }
        for (MarketStation ms : marketStations) {
            ms.angle += orbitDt * ms.orbitSpeed;
            ms.position.set(
                ms.hostPlanet.position.x + (float) Math.cos(ms.angle) * ms.orbitRadius,
                0.8f,
                ms.hostPlanet.position.z + (float) Math.sin(ms.angle) * ms.orbitRadius
            );
        }
    }

    private static void updatePlanet(Planet p, float orbitDt) {
        p.angle += orbitDt * p.orbitSpeed;
        p.barycenter.set((float) Math.cos(p.angle) * p.orbitRadius, 0f, (float) Math.sin(p.angle) * p.orbitRadius);

        if (p.isBinaryPluto && !p.moons.isEmpty()) {
            Moon charon = p.moons.get(0);
            charon.angle += orbitDt * charon.orbitSpeed;
            float cosA = (float) Math.cos(charon.angle);
            float sinA = (float) Math.sin(charon.angle);
            p.position.set(p.barycenter.x - cosA * p.binaryOffset, p.barycenter.y - sinA * p.binaryOffset * 0.12f, p.barycenter.z - sinA * p.binaryOffset);
            charon.position.set(p.barycenter.x + cosA * charon.orbitRadius, p.barycenter.y + sinA * charon.orbitRadius * 0.12f, p.barycenter.z + sinA * charon.orbitRadius);
            return;
        }

        p.position.set(p.barycenter);
        Matrix4f tiltMat = new Matrix4f().rotateZ(p.axialTilt);
        Vector3f localOffset = new Vector3f();
        for (Moon m : p.moons) {
            m.angle += orbitDt * m.orbitSpeed;
            localOffset.set(
                (float) Math.cos(m.angle) * m.orbitRadius,
                (float) Math.sin(m.angle) * m.orbitRadius * (float) Math.sin(m.inclination),
                (float) Math.sin(m.angle) * m.orbitRadius * (float) Math.cos(m.inclination)
            );
            tiltMat.transformPosition(localOffset);
            m.position.set(p.position).add(localOffset);
        }
    }

    private static class PickCandidate {
        TargetInfo info;
        float bestT = Float.MAX_VALUE;
    }

    public TargetInfo pickCelestial(Vector3f rayOrigin, Vector3f rayDir) {
        PickCandidate best = new PickCandidate();
        pickStar(rayOrigin, rayDir, best);
        pickJumpGates(rayOrigin, rayDir, best);
        pickMarketStations(rayOrigin, rayDir, best);
        pickPlanetsAndMoons(rayOrigin, rayDir, best);
        return best.info;
    }

    private void pickStar(Vector3f rayOrigin, Vector3f rayDir, PickCandidate best) {
        float t = raySphereHitT(rayOrigin, rayDir, starPos, starType.radius);
        if (t < best.bestT) {
            best.bestT = t;
            best.info = new TargetInfo(starType.displayName, "CENTRAL STAR",
                MineralRegistry.forStar(isCoreFederation),
                isCoreFederation ? "FEDERATION CORE" : "UNCLAIMED", starType.radius, () -> starPos);
        }
    }

    private void pickJumpGates(Vector3f rayOrigin, Vector3f rayDir, PickCandidate best) {
        for (JumpGate g : jumpGates) {
            float gr = Math.max(g.radius * 1.6f, g.position.distance(rayOrigin) * 0.018f);
            float t = raySphereHitT(rayOrigin, rayDir, g.position, gr);
            if (t < best.bestT) {
                best.bestT = t;
                String gName = (g.isFrontier ? "FRONTIER GATE -> " : "FEDERATION GATE -> ") + g.targetSystemName;
                String gCat = g.isFrontier ? "LONG-RANGE JUMP GATE [PRESS J]" : "STARGATE CONDUIT [PRESS J]";
                best.info = new TargetInfo(gName, gCat, Collections.emptyList(),
                    g.isFrontier ? "FRONTIER LINK" : "FEDERATION", g.radius, g.targetSystemId, () -> g.position);
            }
        }
    }

    private void pickMarketStations(Vector3f rayOrigin, Vector3f rayDir, PickCandidate best) {
        for (MarketStation ms : marketStations) {
            float sr = Math.max(ms.radius * 1.5f, ms.position.distance(rayOrigin) * 0.018f);
            float t = raySphereHitT(rayOrigin, rayDir, ms.position, sr);
            if (t < best.bestT) {
                best.bestT = t;
                best.info = new TargetInfo(ms.name, "NPC MARKET STATION", Collections.emptyList(),
                    "FEDERATION COMMERCE", ms.radius, null, ms, () -> ms.position);
            }
        }
    }

    private void pickPlanetsAndMoons(Vector3f rayOrigin, Vector3f rayDir, PickCandidate best) {
        String faction = isCoreFederation ? "FEDERATION CORE" : "UNCLAIMED";
        for (Planet p : planets) {
            pickSinglePlanet(p, best, rayOrigin, rayDir);
            pickPlanetMoons(p, best, rayOrigin, rayDir);
        }
    }

    private void pickSinglePlanet(Planet p, PickCandidate best, Vector3f rayOrigin, Vector3f rayDir) {
        float pr = Math.max(p.radius * 1.25f, p.position.distance(rayOrigin) * 0.015f);
        float t = raySphereHitT(rayOrigin, rayDir, p.position, pr);
        if (t < best.bestT) {
            best.bestT = t;
            best.info = new TargetInfo(p.name, p.getCategory(), p.getDeposits(isCoreFederation), factionName(), p.radius, () -> p.position);
        }
    }

    private void pickPlanetMoons(Planet p, PickCandidate best, Vector3f rayOrigin, Vector3f rayDir) {
        String faction = factionName();
        for (Moon m : p.moons) {
            float mr = Math.max(m.radius * 1.8f, m.position.distance(rayOrigin) * 0.013f);
            float mt = raySphereHitT(rayOrigin, rayDir, m.position, mr);
            if (mt < best.bestT) {
                best.bestT = mt;
                best.info = new TargetInfo(m.name, m.category, m.deposits, faction, m.radius, () -> m.position);
            }
        }
    }

    private String factionName() {
        return isCoreFederation ? "SOLAR FEDERATION" : "FRONTIER INDEPENDENT";
    }

    private static float raySphereHitT(Vector3f rayOrigin, Vector3f rayDir, Vector3f center, float radius) {
        Vector3f oc = new Vector3f(center).sub(rayOrigin);
        float proj = oc.dot(rayDir);
        if (proj <= 0f) return Float.MAX_VALUE;
        float d2 = oc.lengthSquared() - proj * proj;
        return (d2 <= radius * radius) ? proj : Float.MAX_VALUE;
    }

    public void dispose() {
        for (Planet p : planets) {
            disposePlanet(p);
        }
    }

    private static void disposePlanet(Planet p) {
        if (p.orbitMesh != null) p.orbitMesh.dispose();
        if (p.ringMesh != null) p.ringMesh.dispose();
        for (Moon m : p.moons) {
            if (m.orbitMesh != null) m.orbitMesh.dispose();
        }
    }
}
