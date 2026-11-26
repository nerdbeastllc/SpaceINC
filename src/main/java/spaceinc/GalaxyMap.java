package spaceinc;

import java.util.Arrays;
import java.util.List;

/**
 * Defines the Milky Way sector: Core Federated Systems (low ore purity, gate-linked)
 * and Frontier Systems (high ore purity) utilizing all 12 Milky Way star types.
 */
public class GalaxyMap {

    public static class SystemNode {
        public final String id;
        public final String name;
        public final StarSystem.StarType starType;
        public final boolean isCoreFederation;
        public final float mapX; // Normalized sector coordinates
        public final float mapY;
        public final long seed;
        public final List<String> gateLinks;

        public SystemNode(String id, String name, StarSystem.StarType starType,
                          boolean isCoreFederation, float mapX, float mapY, long seed,
                          String... gateLinks) {
            this.id = id;
            this.name = name;
            this.starType = starType;
            this.isCoreFederation = isCoreFederation;
            this.mapX = mapX;
            this.mapY = mapY;
            this.seed = seed;
            this.gateLinks = Arrays.asList(gateLinks);
        }
    }

    public final List<SystemNode> systems = Arrays.asList(
        // --- Core Federated Cluster (Low Purity, Safe, Gate-Linked) ---
        new SystemNode("SOL", "SOL SYSTEM", StarSystem.StarType.G_SOL_YELLOW, true,
            -0.35f, 0.08f, 1001L, "CENTAURI", "SIRIUS", "PROCYON"),
        new SystemNode("CENTAURI", "ALPHA CENTAURI", StarSystem.StarType.K_ORANGE_DWARF, true,
            -0.10f, -0.34f, 2002L, "SOL", "SIRIUS"),
        new SystemNode("SIRIUS", "SIRIUS", StarSystem.StarType.A_WHITE_BLUE, true,
            -0.06f, 0.44f, 3003L, "SOL", "CENTAURI"),
        new SystemNode("PROCYON", "PROCYON", StarSystem.StarType.F_YELLOW_WHITE, true,
            -0.68f, -0.26f, 3505L, "SOL"),

        // --- Frontier Systems (High Purity, No Jump Gates — FTL / Future Player Gates) ---
        new SystemNode("PROXIMA", "PROXIMA REACH", StarSystem.StarType.M_RED_DWARF, false,
            0.22f, -0.68f, 4001L),
        new SystemNode("RIGEL", "RIGEL EXPANSE", StarSystem.StarType.B_BLUE_SUPERGIANT, false,
            0.62f, -0.36f, 4004L),
        new SystemNode("BETELGEUSE", "BETELGEUSE REACH", StarSystem.StarType.M_RED_GIANT, false,
            0.58f, 0.36f, 5005L),
        new SystemNode("ZETA_PUPPIS", "ZETA PUPPIS", StarSystem.StarType.O_HYPERGIANT, false,
            1.02f, -0.08f, 6006L),
        new SystemNode("TEIDE", "TEIDE DRIFT", StarSystem.StarType.L_BROWN_DWARF, false,
            -0.88f, 0.46f, 7007L),
        new SystemNode("WR104", "WR-104 NEBULA", StarSystem.StarType.WOLF_RAYET, false,
            1.08f, 0.56f, 8008L),
        new SystemNode("SIRIUS_B", "SIRIUS-B REMNANT", StarSystem.StarType.WHITE_DWARF, false,
            0.26f, 0.62f, 9009L),
        new SystemNode("CRAB", "CRAB PULSAR", StarSystem.StarType.NEUTRON_MAGNETAR, false,
            0.84f, 0.88f, 1010L),
        new SystemNode("VEGA", "VEGA", StarSystem.StarType.A_WHITE_BLUE, false,
            -0.54f, 0.86f, 1111L),
        new SystemNode("ALDEBARAN", "ALDEBARAN", StarSystem.StarType.M_RED_GIANT, false,
            -0.46f, -0.78f, 1212L),
        new SystemNode("TAU_CETI", "TAU CETI", StarSystem.StarType.G_SOL_YELLOW, false,
            -1.08f, -0.14f, 1313L),
        new SystemNode("EPSILON", "EPSILON ERIDANI", StarSystem.StarType.K_ORANGE_DWARF, false,
            -0.08f, -0.96f, 1414L)
    );

    public SystemNode findById(String id) {
        for (SystemNode s : systems) {
            if (s.id.equals(id)) return s;
        }
        return systems.get(0);
    }
}
