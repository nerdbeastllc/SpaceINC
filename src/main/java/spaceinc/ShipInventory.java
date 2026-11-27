package spaceinc;

import java.util.ArrayList;
import java.util.List;

public class ShipInventory {
    public static final int COLS = 6;
    public static final int ROWS = 12;
    public static final int VISIBLE_ROWS = 3;

    public static class ItemSpec {
        public final String id;
        public final String name;
        public final String shortCode;
        public final String kind;
        public final boolean stackable;
        public final double unitMassKg;
        public final double unitVolumeM3;

        public ItemSpec(String id, String name, String shortCode, String kind,
                        boolean stackable, double unitMassKg, double unitVolumeM3) {
            this.id = id;
            this.name = name;
            this.shortCode = shortCode;
            this.kind = kind;
            this.stackable = stackable;
            this.unitMassKg = unitMassKg;
            this.unitVolumeM3 = unitVolumeM3;
        }
    }

    public static class CargoItem {
        public final ItemSpec spec;
        public final String id;
        public final String name;
        public final String shortCode;
        public final String kind;
        public int count;
        public final boolean stackable;
        public final double unitMassKg;
        public final double unitVolumeM3;
        public int gridCol;
        public int gridRow;

        public CargoItem(ItemSpec spec, int count, int gridCol, int gridRow) {
            this.spec = spec;
            this.id = spec.id;
            this.name = spec.name;
            this.shortCode = spec.shortCode;
            this.kind = spec.kind;
            this.count = count;
            this.stackable = spec.stackable;
            this.unitMassKg = spec.unitMassKg;
            this.unitVolumeM3 = spec.unitVolumeM3;
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

    public ShipInventory() {
        this(500.0, 250.0, true);
    }

    public ShipInventory(double maxMassKg, double maxVolumeM3, boolean populateStarterCargo) {
        this.maxMassKg = maxMassKg;
        this.maxVolumeM3 = maxVolumeM3;
        if (populateStarterCargo) {
            items.add(new CargoItem(new ItemSpec("H2_CAN", "HYDROGEN CANISTER", "H2", "FUEL", true, 0.20, 2.00), 12, 0, 0));
            items.add(new CargoItem(new ItemSpec("XE_CAN", "XENON CANISTER", "XE", "FUEL", true, 1.50, 1.50), 8, 1, 0));
            items.add(new CargoItem(new ItemSpec("ORE_HEM", "HEMATITE ORE", "FE+", "ORE", true, 4.50, 0.80), 25, 2, 0));
            items.add(new CargoItem(new ItemSpec("ORE_CHA", "CHALCOPYRITE ORE", "CU+", "ORE", true, 3.80, 0.80), 14, 4, 0));
            items.add(new CargoItem(new ItemSpec("EL_TI", "REFINED TITANIUM", "TI", "ELEMENT", true, 4.50, 0.30), 10, 0, 1));
        }
    }

    public boolean canFit(double addMassKg, double addVolumeM3) {
        return getUsedMassKg() + addMassKg <= maxMassKg + 1e-6
            && getUsedVolumeM3() + addVolumeM3 <= maxVolumeM3 + 1e-6;
    }

    public boolean addOrStack(ItemSpec spec, int count) {
        if (count <= 0 || !canFit(count * spec.unitMassKg, count * spec.unitVolumeM3)) {
            return false;
        }
        for (CargoItem item : items) {
            if (item.stackable && item.id.equals(spec.id)) {
                item.count += count;
                return true;
            }
        }
        int[] empty = findFirstEmptySlot();
        if (empty == null) return false;
        items.add(new CargoItem(spec, count, empty[0], empty[1]));
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
        if (!canSplit(source)) return null;
        int amount = Math.max(1, Math.min(source.count - 1, takeCount));
        source.count -= amount;
        return new CargoItem(source.spec, amount, source.gridCol, source.gridRow);
    }

    private static boolean canSplit(CargoItem source) {
        return source != null && source.stackable && source.count > 1;
    }

    public void dropSplitItem(CargoItem split, CargoItem source, int targetCol, int targetRow) {
        if (!isInsideGrid(targetCol, targetRow)) {
            source.count += split.count;
            return;
        }
        CargoItem occupant = getItemAt(targetCol, targetRow);
        if (occupant == null) {
            split.gridCol = targetCol;
            split.gridRow = targetRow;
            items.add(split);
            selectedItem = split;
            return;
        }
        if (canStackWith(occupant, split)) {
            occupant.count += split.count;
            selectedItem = occupant;
            return;
        }
        source.count += split.count;
    }

    public void moveItemTo(CargoItem item, int targetCol, int targetRow) {
        if (!isInsideGrid(targetCol, targetRow)) return;
        if (item.gridCol == targetCol && item.gridRow == targetRow) return;

        CargoItem occupant = getItemAt(targetCol, targetRow);
        if (occupant == null) {
            item.gridCol = targetCol;
            item.gridRow = targetRow;
            return;
        }
        if (occupant != item && canStackWith(occupant, item)) {
            occupant.count += item.count;
            items.remove(item);
            if (selectedItem == item) selectedItem = occupant;
            return;
        }
        swapSlots(item, occupant);
    }

    private static boolean canStackWith(CargoItem a, CargoItem b) {
        return a.stackable && a.id.equals(b.id);
    }

    private static void swapSlots(CargoItem a, CargoItem b) {
        int oldCol = a.gridCol;
        int oldRow = a.gridRow;
        a.gridCol = b.gridCol;
        a.gridRow = b.gridRow;
        b.gridCol = oldCol;
        b.gridRow = oldRow;
    }

    private static boolean isInsideGrid(int col, int row) {
        return col >= 0 && col < COLS && row >= 0 && row < ROWS;
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
