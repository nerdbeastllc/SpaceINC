package spaceinc;

import java.util.ArrayList;
import java.util.List;

public class ShipInventory {
    public static final int COLS = 6;
    public static final int ROWS = 12;
    public static final int VISIBLE_ROWS = 3;

    public static class CargoItem {
        public final String id;
        public final String name;
        public final String shortCode;
        public final String kind; // FUEL, ORE, ELEMENT
        public int count;
        public final boolean stackable;
        public final double unitMassKg;
        public final double unitVolumeM3;
        public int gridCol;
        public int gridRow;

        public CargoItem(String id, String name, String shortCode, String kind,
                         int count, boolean stackable, double unitMassKg, double unitVolumeM3,
                         int gridCol, int gridRow) {
            this.id = id;
            this.name = name;
            this.shortCode = shortCode;
            this.kind = kind;
            this.count = count;
            this.stackable = stackable;
            this.unitMassKg = unitMassKg;
            this.unitVolumeM3 = unitVolumeM3;
            this.gridCol = gridCol;
            this.gridRow = gridRow;
        }

        public double getTotalMassKg() {
            return count * unitMassKg;
        }

        public double getTotalVolumeM3() {
            return count * unitVolumeM3;
        }
    }

    public final double maxMassKg;
    public final double maxVolumeM3;
    public final List<CargoItem> items = new ArrayList<>();
    public CargoItem selectedItem = null;
    public int scrollRow = 0;

    /** Default starter Ship Cargo Hold (500 KG, 250 M3). */
    public ShipInventory() {
        this(500.0, 250.0, true);
    }

    /** Custom capacity inventory (e.g., 1000x station hangar). */
    public ShipInventory(double maxMassKg, double maxVolumeM3, boolean populateStarterCargo) {
        this.maxMassKg = maxMassKg;
        this.maxVolumeM3 = maxVolumeM3;
        if (populateStarterCargo) {
            items.add(new CargoItem("H2_CAN", "HYDROGEN CANISTER", "H2", "FUEL", 12, true, 0.20, 2.00, 0, 0));
            items.add(new CargoItem("XE_CAN", "XENON CANISTER", "XE", "FUEL", 8, true, 1.50, 1.50, 1, 0));
            items.add(new CargoItem("ORE_HEM", "HEMATITE ORE", "FE+", "ORE", 25, true, 4.50, 0.80, 2, 0));
            items.add(new CargoItem("ORE_CHA", "CHALCOPYRITE ORE", "CU+", "ORE", 14, true, 3.80, 0.80, 4, 0));
            items.add(new CargoItem("EL_TI", "REFINED TITANIUM", "TI", "ELEMENT", 10, true, 4.50, 0.30, 0, 1));
        }
    }

    public boolean canFit(double addMassKg, double addVolumeM3) {
        return getUsedMassKg() + addMassKg <= maxMassKg + 1e-6
            && getUsedVolumeM3() + addVolumeM3 <= maxVolumeM3 + 1e-6;
    }

    public boolean addOrStack(String id, String name, String shortCode, String kind,
                              int count, double unitMassKg, double unitVolumeM3) {
        if (count <= 0) return false;
        if (!canFit(count * unitMassKg, count * unitVolumeM3)) return false;

        for (CargoItem item : items) {
            if (item.stackable && item.id.equals(id)) {
                item.count += count;
                return true;
            }
        }
        int[] empty = findFirstEmptySlot();
        if (empty == null) return false;
        items.add(new CargoItem(id, name, shortCode, kind, count, true, unitMassKg, unitVolumeM3, empty[0], empty[1]));
        return true;
    }

    public void removeUnits(CargoItem item, int count) {
        if (item == null || count <= 0) return;
        item.count -= count;
        if (item.count <= 0) {
            items.remove(item);
            if (selectedItem == item) selectedItem = null;
        }
    }

    public CargoItem getItemAt(int col, int row) {
        for (CargoItem item : items) {
            if (item.gridCol == col && item.gridRow == row) return item;
        }
        return null;
    }

    public int[] findFirstEmptySlot() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (getItemAt(c, r) == null) return new int[]{c, r};
            }
        }
        return null;
    }

    public CargoItem detachSplit(CargoItem source, int takeCount) {
        if (source == null || !source.stackable || source.count <= 1) return null;
        int amount = Math.max(1, Math.min(source.count - 1, takeCount));
        source.count -= amount;
        return new CargoItem(
            source.id, source.name, source.shortCode, source.kind,
            amount, true, source.unitMassKg, source.unitVolumeM3,
            source.gridCol, source.gridRow
        );
    }

    public void dropSplitItem(CargoItem split, CargoItem source, int targetCol, int targetRow) {
        if (targetCol >= 0 && targetCol < COLS && targetRow >= 0 && targetRow < ROWS) {
            CargoItem occupant = getItemAt(targetCol, targetRow);
            if (occupant == null) {
                split.gridCol = targetCol;
                split.gridRow = targetRow;
                items.add(split);
                selectedItem = split;
                return;
            } else if (occupant.stackable && occupant.id.equals(split.id)) {
                occupant.count += split.count;
                selectedItem = occupant;
                return;
            }
        }
        source.count += split.count;
    }

    public void moveItemTo(CargoItem item, int targetCol, int targetRow) {
        if (targetCol < 0 || targetCol >= COLS || targetRow < 0 || targetRow >= ROWS) return;
        if (item.gridCol == targetCol && item.gridRow == targetRow) return;

        CargoItem occupant = getItemAt(targetCol, targetRow);
        if (occupant == null) {
            item.gridCol = targetCol;
            item.gridRow = targetRow;
        } else if (occupant != item && occupant.stackable && occupant.id.equals(item.id)) {
            occupant.count += item.count;
            items.remove(item);
            if (selectedItem == item) selectedItem = occupant;
        } else {
            int oldCol = item.gridCol;
            int oldRow = item.gridRow;
            item.gridCol = targetCol;
            item.gridRow = targetRow;
            occupant.gridCol = oldCol;
            occupant.gridRow = oldRow;
        }
    }

    public double getUsedMassKg() {
        double sum = 0.0;
        for (CargoItem item : items) sum += item.getTotalMassKg();
        return sum;
    }

    public double getUsedVolumeM3() {
        double sum = 0.0;
        for (CargoItem item : items) sum += item.getTotalVolumeM3();
        return sum;
    }
}
