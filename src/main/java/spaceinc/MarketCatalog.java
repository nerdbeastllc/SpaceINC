package spaceinc;

import java.util.Arrays;
import java.util.List;

public final class MarketCatalog {

    public static class MarketListing {
        public final String id;
        public final String name;
        public final String shortCode;
        public final String kind;
        public final double unitMassKg;
        public final double unitVolumeM3;
        public final int buyPrice;
        public final int sellPrice;

        public MarketListing(String id, String name, String shortCode, String kind,
                             double unitMassKg, double unitVolumeM3, int buyPrice, int sellPrice) {
            this.id = id;
            this.name = name;
            this.shortCode = shortCode;
            this.kind = kind;
            this.unitMassKg = unitMassKg;
            this.unitVolumeM3 = unitVolumeM3;
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
        }

        public ShipInventory.ItemSpec toItemSpec() {
            return new ShipInventory.ItemSpec(id, name, shortCode, kind, true, unitMassKg, unitVolumeM3);
        }
    }

    public static final List<MarketListing> LISTINGS = Arrays.asList(
        new MarketListing("H2_CAN", "HYDROGEN CANISTER", "H2", "FUEL", 0.20, 2.00, 25, 18),
        new MarketListing("XE_CAN", "XENON CANISTER", "XE", "FUEL", 1.50, 1.50, 85, 62),
        new MarketListing("ORE_HEM", "HEMATITE ORE", "FE+", "ORE", 4.50, 0.80, 40, 28),
        new MarketListing("ORE_CHA", "CHALCOPYRITE ORE", "CU+", "ORE", 3.80, 0.80, 55, 38),
        new MarketListing("ORE_SPO", "SPODUMENE ORE", "LI+", "ORE", 2.60, 0.80, 75, 52),
        new MarketListing("EL_TI", "REFINED TITANIUM", "TI", "ELEMENT", 4.50, 0.30, 160, 120),
        new MarketListing("EL_AU", "REFINED GOLD", "AU", "ELEMENT", 19.3, 0.15, 420, 340)
    );

    public static MarketListing findById(String id) {
        for (MarketListing m : LISTINGS) {
            if (m.id.equals(id)) return m;
        }
        return null;
    }

    private MarketCatalog() {}
}
