package spaceinc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class MineralRegistry {

    public enum Element {
        H("H", "Hydrogen"), HE("HE", "Helium-3"), LI("LI", "Lithium"), BE("BE", "Beryllium"),
        B("B", "Boron"), C("C", "Carbon"), N("N", "Nitrogen"), O("O", "Oxygen"),
        F("F", "Fluorine"), NE("NE", "Neon"), NA("NA", "Sodium"), MG("MG", "Magnesium"),
        AL("AL", "Aluminium"), SI("SI", "Silicon"), P("P", "Phosphorus"), S("S", "Sulfur"),
        CL("CL", "Chlorine"), AR("AR", "Argon"), K("K", "Potassium"), CA("CA", "Calcium"),
        TI("TI", "Titanium"), V("V", "Vanadium"), CR("CR", "Chromium"), MN("MN", "Manganese"),
        FE("FE", "Iron"), CO("CO", "Cobalt"), NI("NI", "Nickel"), CU("CU", "Copper"),
        ZN("ZN", "Zinc"), GA("GA", "Gallium"), Y("Y", "Yttrium"), PD("PD", "Palladium"),
        AG("AG", "Silver"), SN("SN", "Tin"), XE("XE", "Xenon"), LA("LA", "Lanthanum"),
        CE("CE", "Cerium"), ND("ND", "Neodymium"), W("W", "Tungsten"), IR("IR", "Iridium"),
        PT("PT", "Platinum"), AU("AU", "Gold"), PB("PB", "Lead"), TH("TH", "Thorium"),
        U("U", "Uranium");

        public final String symbol;
        public final String fullName;

        Element(String symbol, String fullName) {
            this.symbol = symbol;
            this.fullName = fullName;
        }
    }

    public enum OreType {
        SPODUMENE("SPODUMENE", Element.LI, Element.AL, Element.SI),
        BERYL("BERYL", Element.BE, Element.AL, Element.SI),
        BAUXITE("BAUXITE", Element.AL, Element.GA),
        DOLOMITE("DOLOMITE", Element.MG, Element.CA, Element.C),
        HALITE("HALITE", Element.NA, Element.K, Element.CL),
        HEMATITE("HEMATITE", Element.FE, Element.MN),
        ILMENITE("ILMENITE", Element.TI, Element.FE),
        PENTLANDITE("PENTLANDITE", Element.NI, Element.FE, Element.CO),
        CHROMITE("CHROMITE", Element.CR, Element.FE, Element.V),
        WOLFRAMITE("WOLFRAMITE", Element.W, Element.MN),
        CHALCOPYRITE("CHALCOPYRITE", Element.CU, Element.FE, Element.S),
        ELECTRUM("ELECTRUM", Element.AU, Element.AG, Element.CU),
        SPERRYLITE("SPERRYLITE", Element.PT, Element.PD, Element.IR),
        CASSITERITE("CASSITERITE", Element.SN, Element.PB, Element.ZN),
        QUARTZ("QUARTZ", Element.SI, Element.O),
        GRAPHITE("GRAPHITE", Element.C, Element.N, Element.P),
        BRIMSTONE("BRIMSTONE", Element.S, Element.FE),
        FLUORITE("FLUORITE", Element.F, Element.B),
        WATER_ICE("WATER ICE", Element.H, Element.O, Element.C),
        MONAZITE("MONAZITE", Element.ND, Element.CE, Element.LA, Element.Y),
        URANINITE("URANINITE", Element.U, Element.TH),
        GAS_HARVEST("ATMOSPHERIC GAS", Element.H, Element.HE, Element.XE, Element.NE, Element.AR);

        public final String displayName;
        public final List<Element> elements;
        public final String elementListText;

        OreType(String displayName, Element... elements) {
            this.displayName = displayName;
            this.elements = Arrays.asList(elements);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < elements.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(elements[i].symbol);
            }
            this.elementListText = sb.toString();
        }
    }

    public static class OreDeposit {
        public final OreType ore;
        public final int percent;

        public OreDeposit(OreType ore, int percent) {
            this.ore = ore;
            this.percent = percent;
        }
    }

    private static final OreType[] ROCKY_ORES = {
        OreType.HEMATITE, OreType.ILMENITE, OreType.CHALCOPYRITE, OreType.BAUXITE,
        OreType.PENTLANDITE, OreType.SPODUMENE, OreType.BERYL, OreType.ELECTRUM,
        OreType.SPERRYLITE, OreType.CHROMITE, OreType.WOLFRAMITE, OreType.CASSITERITE,
        OreType.QUARTZ, OreType.MONAZITE, OreType.URANINITE, OreType.DOLOMITE
    };

    private static final OreType[] VOLCANIC_ORES = {
        OreType.BRIMSTONE, OreType.ILMENITE, OreType.WOLFRAMITE, OreType.MONAZITE,
        OreType.CHALCOPYRITE, OreType.FLUORITE, OreType.URANINITE
    };

    private static final OreType[] ICY_ORES = {
        OreType.WATER_ICE, OreType.HALITE, OreType.GRAPHITE, OreType.QUARTZ,
        OreType.SPODUMENE, OreType.FLUORITE, OreType.DOLOMITE
    };

    private static final OreType[] GAS_ORES = {
        OreType.GAS_HARVEST, OreType.WATER_ICE, OreType.GRAPHITE
    };

    private MineralRegistry() {}

    public static List<OreDeposit> forStar() {
        return Arrays.asList(
            new OreDeposit(OreType.GAS_HARVEST, 96),
            new OreDeposit(OreType.WATER_ICE, 4)
        );
    }

    public static List<OreDeposit> forPlanet(String name, int planetType) {
        if ("EARTH".equals(name)) {
            return Arrays.asList(
                new OreDeposit(OreType.HEMATITE, 32),
                new OreDeposit(OreType.BAUXITE, 26),
                new OreDeposit(OreType.WATER_ICE, 24),
                new OreDeposit(OreType.CHALCOPYRITE, 18)
            );
        }
        if ("MERCURY".equals(name)) {
            return Arrays.asList(
                new OreDeposit(OreType.HEMATITE, 44),
                new OreDeposit(OreType.ILMENITE, 28),
                new OreDeposit(OreType.PENTLANDITE, 18),
                new OreDeposit(OreType.ELECTRUM, 10)
            );
        }
        if ("VENUS".equals(name)) {
            return Arrays.asList(
                new OreDeposit(OreType.BRIMSTONE, 40),
                new OreDeposit(OreType.QUARTZ, 28),
                new OreDeposit(OreType.BAUXITE, 20),
                new OreDeposit(OreType.FLUORITE, 12)
            );
        }
        if ("MARS".equals(name)) {
            return Arrays.asList(
                new OreDeposit(OreType.HEMATITE, 42),
                new OreDeposit(OreType.QUARTZ, 24),
                new OreDeposit(OreType.WATER_ICE, 20),
                new OreDeposit(OreType.SPODUMENE, 14)
            );
        }
        if (planetType == 1 || planetType == 5) {
            return Arrays.asList(
                new OreDeposit(OreType.GAS_HARVEST, 78),
                new OreDeposit(OreType.WATER_ICE, 14),
                new OreDeposit(OreType.GRAPHITE, 8)
            );
        }
        return generateSeeded(name.hashCode(), ROCKY_ORES, 4);
    }

    public static List<OreDeposit> forMoon(String name, int planetType) {
        if ("TITAN".equals(name)) {
            return Arrays.asList(
                new OreDeposit(OreType.GRAPHITE, 42),
                new OreDeposit(OreType.WATER_ICE, 36),
                new OreDeposit(OreType.GAS_HARVEST, 22)
            );
        }
        if (planetType == 6) {
            return generateSeeded(name.hashCode(), VOLCANIC_ORES, 4);
        }
        if (planetType == 7) {
            return generateSeeded(name.hashCode(), ICY_ORES, 3);
        }
        return generateSeeded(name.hashCode(), ROCKY_ORES, 3);
    }

    public static List<OreDeposit> forAsteroid(int index, boolean isComet) {
        return generateSeeded(index * 7919L + (isComet ? 999L : 17L), isComet ? ICY_ORES : ROCKY_ORES, 3);
    }

    private static List<OreDeposit> generateSeeded(long seed, OreType[] pool, int count) {
        Random rng = new Random(seed);
        List<OreType> chosen = new ArrayList<>();
        while (chosen.size() < count && chosen.size() < pool.length) {
            OreType candidate = pool[rng.nextInt(pool.length)];
            if (!chosen.contains(candidate)) {
                chosen.add(candidate);
            }
        }
        int[] weights = new int[chosen.size()];
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            weights[i] = 15 + rng.nextInt(45);
            sum += weights[i];
        }
        List<OreDeposit> result = new ArrayList<>();
        int assigned = 0;
        for (int i = 0; i < chosen.size(); i++) {
            int pct = (i == chosen.size() - 1) ? (100 - assigned) : Math.max(5, (weights[i] * 100) / sum);
            assigned += pct;
            result.add(new OreDeposit(chosen.get(i), pct));
        }
        return result;
    }
}
