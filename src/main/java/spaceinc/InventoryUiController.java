package spaceinc;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Handles all Ship Cargo & Station Hangar grid interactions: selection, dragging,
 * stack splitting (Shift+Drag & Ctrl+Click slider), scrolling, and cross-window transfers.
 */
public class InventoryUiController {
    private final GameState s;

    public InventoryUiController(GameState state) {
        this.s = state;
    }

    private static final class Pt {
        final float x, y;
        Pt(float x, float y) { this.x = x; this.y = y; }
    }

    private static final class InvWin {
        final ShipInventory inv;
        final float x, y;
        InvWin(ShipInventory inv, float x, float y) {
            this.inv = inv;
            this.x = x;
            this.y = y;
        }
    }

    public boolean tryScroll(float cx, float cy, double yoff) {
        Pt p = new Pt(cx, cy);
        if (s.inventoryOpen && scrollIfInside(new InvWin(s.ship.inventory, s.invX, s.invY), p, yoff)) return true;
        if (s.stationHangarOpen && s.activeStation != null) {
            InvWin win = new InvWin(s.getHangarFor(s.activeStation), s.stationInvX, s.stationInvY);
            return scrollIfInside(win, p, yoff);
        }
        return false;
    }

    private static boolean scrollIfInside(InvWin win, Pt p, double yoff) {
        if (!hitRect(p.x, p.y, win.x, win.y, 456f, 356f)) return false;
        int maxScroll = ShipInventory.ROWS - ShipInventory.VISIBLE_ROWS;
        win.inv.scrollRow = Math.max(0, Math.min(maxScroll, win.inv.scrollRow - (int) Math.signum(yoff)));
        return true;
    }

    public boolean handleStationHangarClick(float clickX, float clickY, int mods) {
        ShipInventory hangar = s.getHangarFor(s.activeStation);
        InvWin win = new InvWin(hangar, s.stationInvX, s.stationInvY);
        Pt p = new Pt(clickX, clickY);
        if (hitRect(p.x, p.y, win.x + 430f, win.y + 4f, 20f, 20f)) { s.stationHangarOpen = false; return true; }

        if (hangar.selectedItem != null && hitRect(p.x, p.y, win.x + 320f, win.y + 308f, 116f, 30f)) {
            transferWholeStack(hangar.selectedItem, hangar, s.ship.inventory);
            return true;
        }
        if (!hitRect(p.x, p.y, win.x, win.y, 456f, 356f)) return false;
        if (tryScrollbarGrab(win, p)) return true;
        if (tryBeginItemInteraction(win, p, mods)) return true;

        s.draggingStationInv = true;
        s.stationDragOffX = p.x - win.x;
        s.stationDragOffY = p.y - win.y;
        return true;
    }

    public boolean handleShipInventoryClick(float clickX, float clickY, int mods) {
        InvWin win = new InvWin(s.ship.inventory, s.invX, s.invY);
        Pt p = new Pt(clickX, clickY);
        if (hitRect(p.x, p.y, win.x + 430f, win.y + 4f, 20f, 20f)) {
            s.inventoryOpen = false;
            s.draggingItem = null;
            s.splitSourceItem = null;
            s.splitDialogTarget = null;
            return true;
        }
        if (!hitRect(p.x, p.y, win.x, win.y, 456f, 356f)) return false;

        if (canTransferToStation(p, win)) {
            transferWholeStack(s.ship.inventory.selectedItem, s.ship.inventory, s.getHangarFor(s.activeStation));
            return true;
        }
        if (tryScrollbarGrab(win, p)) return true;
        if (tryBeginItemInteraction(win, p, mods)) return true;

        s.draggingInv = true;
        s.invDragOffX = p.x - win.x;
        s.invDragOffY = p.y - win.y;
        return true;
    }

    private boolean canTransferToStation(Pt p, InvWin win) {
        return s.ship.inventory.selectedItem != null && s.activeStation != null && s.stationHangarOpen
            && hitRect(p.x, p.y, win.x + 310f, win.y + 308f, 126f, 30f);
    }

    private boolean tryScrollbarGrab(InvWin win, Pt p) {
        if (!hitRect(p.x, p.y, win.x + 434f, win.y + 72f, 18f, 208f)) return false;
        s.draggingScrollInv = win.inv;
        s.draggingScrollWinY = win.y;
        updateScrollFromMouse(win.inv, win.y, p.y);
        return true;
    }

    public void handleSplitDialogClick(float clickX, float clickY) {
        float dx = (s.winWidth - 340f) * 0.5f, dy = (s.winHeight - 160f) * 0.5f;
        float trackX = dx + 20f, trackW = 300f;
        if (hitRect(clickX, clickY, trackX - 8f, dy + 56f, trackW + 16f, 32f)) {
            s.draggingSlider = true;
            updateSliderAmount(clickX, trackX, trackW);
            return;
        }
        float btnY = dy + 114f;
        if (hitRect(clickX, clickY, dx + 20f, btnY, 136f, 32f)) {
            confirmSplitDialog();
        } else if (hitRect(clickX, clickY, dx + 184f, btnY, 136f, 32f)) {
            s.splitDialogTarget = null;
            s.splitDialogInventory = null;
        }
    }

    private void confirmSplitDialog() {
        ShipInventory targetInv = (s.splitDialogInventory != null) ? s.splitDialogInventory : s.ship.inventory;
        int[] empty = targetInv.findFirstEmptySlot();
        if (empty != null) {
            ShipInventory.CargoItem split = targetInv.detachSplit(s.splitDialogTarget, s.splitDialogAmount);
            if (split != null) targetInv.dropSplitItem(split, s.splitDialogTarget, empty[0], empty[1]);
        }
        s.splitDialogTarget = null;
        s.splitDialogInventory = null;
    }

    public void handleMouseMove(float xpos, float ypos) {
        s.dragMouseX = xpos;
        s.dragMouseY = ypos;
        if (s.draggingSlider && s.splitDialogTarget != null) {
            float dx = (s.winWidth - 340f) * 0.5f;
            updateSliderAmount(xpos, dx + 20f, 300f);
        }
        if (s.draggingScrollInv != null) {
            updateScrollFromMouse(s.draggingScrollInv, s.draggingScrollWinY, ypos);
        }
        if (s.draggingInv && s.inventoryOpen) {
            s.invX = clamp(xpos - s.invDragOffX, 0f, s.winWidth - 456f);
            s.invY = clamp(ypos - s.invDragOffY, 0f, s.winHeight - 356f);
        }
        if (s.draggingStationInv && s.stationHangarOpen) {
            s.stationInvX = clamp(xpos - s.stationDragOffX, 0f, s.winWidth - 456f);
            s.stationInvY = clamp(ypos - s.stationDragOffY, 0f, s.winHeight - 356f);
        }
    }

    private boolean tryBeginItemInteraction(InvWin win, Pt p, int mods) {
        float gridX = win.x + 16f, gridY = win.y + 72f, step = 72f;
        int col = (int) Math.floor((p.x - gridX) / step);
        int visRow = (int) Math.floor((p.y - gridY) / step);
        if (!isInsideVisibleGrid(col, visRow)) return false;
        if (p.x > gridX + col * step + 64f || p.y > gridY + visRow * step + 64f) return false;

        ShipInventory.CargoItem hit = win.inv.getItemAt(col, visRow + win.inv.scrollRow);
        if (hit == null) return false;
        win.inv.selectedItem = hit;
        beginItemDragOrSplit(win, hit, p, mods);
        return true;
    }

    private static boolean isInsideVisibleGrid(int col, int visRow) {
        return col >= 0 && col < ShipInventory.COLS && visRow >= 0 && visRow < ShipInventory.VISIBLE_ROWS;
    }

    private void beginItemDragOrSplit(InvWin win, ShipInventory.CargoItem hit, Pt p, int mods) {
        boolean isCtrl = (mods & (GLFW_MOD_CONTROL | GLFW_MOD_SUPER)) != 0;
        boolean isShift = (mods & GLFW_MOD_SHIFT) != 0;

        if (isCtrl && canSplitItem(hit)) {
            s.splitDialogTarget = hit;
            s.splitDialogInventory = win.inv;
            s.splitDialogAmount = Math.max(1, hit.count / 2);
            return;
        }
        if (isShift && canSplitItem(hit)) {
            ShipInventory.CargoItem split = win.inv.detachSplit(hit, (hit.count + 1) / 2);
            if (split != null) { startDragging(split, hit, win.inv, p); return; }
        }
        startDragging(hit, null, win.inv, p);
    }

    private static boolean canSplitItem(ShipInventory.CargoItem hit) {
        return hit.stackable && hit.count > 1;
    }

    private void startDragging(ShipInventory.CargoItem item, ShipInventory.CargoItem splitSrc,
                               ShipInventory srcInv, Pt p) {
        s.draggingItem = item;
        s.splitSourceItem = splitSrc;
        s.draggingFromInventory = srcInv;
        s.dragMouseX = p.x;
        s.dragMouseY = p.y;
    }

    public void handleItemDrop(float clickX, float clickY) {
        ShipInventory srcInv = (s.draggingFromInventory != null) ? s.draggingFromInventory : s.ship.inventory;
        Pt p = new Pt(clickX, clickY);
        InvWin dest = findDropWindow(p);

        if (dest == null) restoreSplitIfNeeded();
        else if (dest.inv == srcInv) dropInSameInventory(dest, p);
        else if (s.activeStation != null) dropAcrossInventories(srcInv, dest, p);

        s.draggingItem = null;
        s.splitSourceItem = null;
        s.draggingFromInventory = null;
    }

    private InvWin findDropWindow(Pt p) {
        if (s.inventoryOpen && hitRect(p.x, p.y, s.invX, s.invY, 456f, 356f)) {
            return new InvWin(s.ship.inventory, s.invX, s.invY);
        }
        if (s.stationHangarOpen && s.activeStation != null && hitRect(p.x, p.y, s.stationInvX, s.stationInvY, 456f, 356f)) {
            return new InvWin(s.getHangarFor(s.activeStation), s.stationInvX, s.stationInvY);
        }
        return null;
    }

    private void dropInSameInventory(InvWin win, Pt p) {
        int col = (int) Math.floor((p.x - (win.x + 16f)) / 72f);
        int row = (int) Math.floor((p.y - (win.y + 72f)) / 72f) + win.inv.scrollRow;
        if (s.splitSourceItem != null) win.inv.dropSplitItem(s.draggingItem, s.splitSourceItem, col, row);
        else win.inv.moveItemTo(s.draggingItem, col, row);
    }

    private void dropAcrossInventories(ShipInventory srcInv, InvWin dest, Pt p) {
        float dist = s.ship.position.distance(s.activeStation.position);
        if (dist > 22.0f) {
            s.setStatus(String.format("TOO FAR FROM STATION (%.1f KM > 22.0 KM)", dist));
            restoreSplitIfNeeded();
            return;
        }
        if (!dest.inv.canFit(s.draggingItem.getTotalMassKg(), s.draggingItem.getTotalVolumeM3())) {
            s.setStatus("DESTINATION CARGO CAPACITY EXCEEDED");
            restoreSplitIfNeeded();
            return;
        }
        int col = (int) clamp((float) Math.floor((p.x - (dest.x + 16f)) / 72f), 0, ShipInventory.COLS - 1);
        int row = (int) clamp((float) Math.floor((p.y - (dest.y + 72f)) / 72f) + dest.inv.scrollRow, 0, ShipInventory.ROWS - 1);
        placeTransferredItem(srcInv, dest.inv, col, row);
    }

    private void placeTransferredItem(ShipInventory srcInv, ShipInventory destInv, int col, int row) {
        ShipInventory.CargoItem copy = new ShipInventory.CargoItem(s.draggingItem.spec, s.draggingItem.count, col, row);
        ShipInventory.CargoItem occupant = destInv.getItemAt(col, row);
        if (occupant == null) {
            destInv.items.add(copy);
            destInv.selectedItem = copy;
        } else if (occupant.stackable && occupant.id.equals(copy.id)) {
            occupant.count += copy.count;
            destInv.selectedItem = occupant;
        } else {
            int[] empty = destInv.findFirstEmptySlot();
            if (empty == null) { s.setStatus("NO EMPTY SLOTS IN DESTINATION"); restoreSplitIfNeeded(); return; }
            copy.gridCol = empty[0]; copy.gridRow = empty[1];
            destInv.items.add(copy);
            destInv.selectedItem = copy;
        }
        if (s.splitSourceItem == null) srcInv.removeUnits(s.draggingItem, s.draggingItem.count);
        s.setStatus("TRANSFERRED " + copy.count + "X " + copy.name);
    }

    private void restoreSplitIfNeeded() {
        if (s.splitSourceItem != null) s.splitSourceItem.count += s.draggingItem.count;
    }

    private void transferWholeStack(ShipInventory.CargoItem item, ShipInventory from, ShipInventory to) {
        float dist = s.ship.position.distance(s.activeStation.position);
        if (dist > 22.0f) { s.setStatus(String.format("TOO FAR FROM STATION (%.1f KM > 22.0 KM)", dist)); return; }
        int qty = item.count;
        if (to.addOrStack(item.spec, qty)) {
            from.removeUnits(item, qty);
            s.setStatus("TRANSFERRED " + qty + "X " + item.name);
        } else {
            s.setStatus("DESTINATION CARGO CAPACITY EXCEEDED");
        }
    }

    private void updateSliderAmount(float mouseX, float trackX, float trackW) {
        if (s.splitDialogTarget == null || s.splitDialogTarget.count <= 1) return;
        float t = clamp((mouseX - trackX) / trackW, 0f, 1f);
        int maxTake = s.splitDialogTarget.count - 1;
        s.splitDialogAmount = Math.max(1, Math.min(maxTake, Math.round(1 + t * (maxTake - 1))));
    }

    private static void updateScrollFromMouse(ShipInventory inv, float winY, float mouseY) {
        float t = clamp((mouseY - (winY + 72f)) / 208f, 0f, 1f);
        inv.scrollRow = Math.round(t * (ShipInventory.ROWS - ShipInventory.VISIBLE_ROWS));
    }

    public static boolean hitRect(float px, float py, float rx, float ry, float rw, float rh) {
        return px >= rx && px <= rx + rw && py >= ry && py <= ry + rh;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
