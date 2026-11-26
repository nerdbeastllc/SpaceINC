package spaceinc;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Lightweight crisp 2D tactical HUD with built-in 5x7 bitmap font.
 */
public class HudOverlay {
    private static final int MAX_VERTS = 262144;
    private final Shader shader;
    private final int vao;
    private final int vbo;
    private final FloatBuffer buffer;
    private int vertCount = 0;

    private static final Map<Character, int[]> FONT = new HashMap<>();
    static {
        // 5x7 bitmap font (5 bits per row, 7 rows)
        FONT.put('A', new int[]{0x0E,0x11,0x11,0x1F,0x11,0x11,0x11});
        FONT.put('B', new int[]{0x1E,0x11,0x11,0x1E,0x11,0x11,0x1E});
        FONT.put('C', new int[]{0x0E,0x11,0x10,0x10,0x10,0x11,0x0E});
        FONT.put('D', new int[]{0x1E,0x11,0x11,0x11,0x11,0x11,0x1E});
        FONT.put('E', new int[]{0x1F,0x10,0x10,0x1E,0x10,0x10,0x1F});
        FONT.put('F', new int[]{0x1F,0x10,0x10,0x1E,0x10,0x10,0x10});
        FONT.put('G', new int[]{0x0E,0x11,0x10,0x17,0x11,0x11,0x0F});
        FONT.put('H', new int[]{0x11,0x11,0x11,0x1F,0x11,0x11,0x11});
        FONT.put('I', new int[]{0x0E,0x04,0x04,0x04,0x04,0x04,0x0E});
        FONT.put('J', new int[]{0x07,0x02,0x02,0x02,0x02,0x12,0x0C});
        FONT.put('K', new int[]{0x11,0x12,0x14,0x18,0x14,0x12,0x11});
        FONT.put('L', new int[]{0x10,0x10,0x10,0x10,0x10,0x10,0x1F});
        FONT.put('M', new int[]{0x11,0x1B,0x15,0x15,0x11,0x11,0x11});
        FONT.put('N', new int[]{0x11,0x19,0x15,0x13,0x11,0x11,0x11});
        FONT.put('O', new int[]{0x0E,0x11,0x11,0x11,0x11,0x11,0x0E});
        FONT.put('P', new int[]{0x1E,0x11,0x11,0x1E,0x10,0x10,0x10});
        FONT.put('Q', new int[]{0x0E,0x11,0x11,0x11,0x15,0x12,0x0D});
        FONT.put('R', new int[]{0x1E,0x11,0x11,0x1E,0x14,0x12,0x11});
        FONT.put('S', new int[]{0x0F,0x10,0x10,0x0E,0x01,0x01,0x1E});
        FONT.put('T', new int[]{0x1F,0x04,0x04,0x04,0x04,0x04,0x04});
        FONT.put('U', new int[]{0x11,0x11,0x11,0x11,0x11,0x11,0x0E});
        FONT.put('V', new int[]{0x11,0x11,0x11,0x11,0x11,0x0A,0x04});
        FONT.put('W', new int[]{0x11,0x11,0x11,0x15,0x15,0x1B,0x11});
        FONT.put('X', new int[]{0x11,0x11,0x0A,0x04,0x0A,0x11,0x11});
        FONT.put('Y', new int[]{0x11,0x11,0x0A,0x04,0x04,0x04,0x04});
        FONT.put('Z', new int[]{0x1F,0x01,0x02,0x04,0x08,0x10,0x1F});
        FONT.put('0', new int[]{0x0E,0x11,0x13,0x15,0x19,0x11,0x0E});
        FONT.put('1', new int[]{0x04,0x0C,0x04,0x04,0x04,0x04,0x0E});
        FONT.put('2', new int[]{0x0E,0x11,0x01,0x06,0x08,0x10,0x1F});
        FONT.put('3', new int[]{0x1F,0x02,0x04,0x06,0x01,0x11,0x0E});
        FONT.put('4', new int[]{0x02,0x06,0x0A,0x12,0x1F,0x02,0x02});
        FONT.put('5', new int[]{0x1F,0x10,0x1E,0x01,0x01,0x11,0x0E});
        FONT.put('6', new int[]{0x06,0x08,0x10,0x1E,0x11,0x11,0x0E});
        FONT.put('7', new int[]{0x1F,0x01,0x02,0x04,0x08,0x08,0x08});
        FONT.put('8', new int[]{0x0E,0x11,0x11,0x0E,0x11,0x11,0x0E});
        FONT.put('9', new int[]{0x0E,0x11,0x11,0x0F,0x01,0x02,0x0C});
        FONT.put(':', new int[]{0x00,0x04,0x04,0x00,0x04,0x04,0x00});
        FONT.put('.', new int[]{0x00,0x00,0x00,0x00,0x00,0x0C,0x0C});
        FONT.put('-', new int[]{0x00,0x00,0x00,0x1F,0x00,0x00,0x00});
        FONT.put('/', new int[]{0x01,0x02,0x02,0x04,0x08,0x08,0x10});
        FONT.put('[', new int[]{0x0E,0x08,0x08,0x08,0x08,0x08,0x0E});
        FONT.put(']', new int[]{0x0E,0x02,0x02,0x02,0x02,0x02,0x0E});
        FONT.put('(', new int[]{0x02,0x04,0x08,0x08,0x08,0x04,0x02});
        FONT.put(')', new int[]{0x08,0x04,0x02,0x02,0x02,0x04,0x08});
        FONT.put('&', new int[]{0x0C,0x12,0x14,0x08,0x15,0x12,0x0D});
        FONT.put(',', new int[]{0x00,0x00,0x00,0x00,0x04,0x04,0x08});
        FONT.put('%', new int[]{0x19,0x1A,0x02,0x04,0x08,0x0B,0x13});
        FONT.put('#', new int[]{0x0A,0x0A,0x1F,0x0A,0x1F,0x0A,0x0A});
        FONT.put('+', new int[]{0x00,0x04,0x04,0x1F,0x04,0x04,0x00});
        FONT.put('>', new int[]{0x10,0x08,0x04,0x02,0x04,0x08,0x10});
        FONT.put('@', new int[]{0x0E,0x11,0x17,0x15,0x17,0x10,0x0E});
        FONT.put(' ', new int[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00});
    }

    public HudOverlay() {
        shader = new Shader("shaders/hud.vert", "shaders/hud.frag");
        buffer = MemoryUtil.memAllocFloat(MAX_VERTS * 6);

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) MAX_VERTS * 6 * Float.BYTES, GL_DYNAMIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 6 * Float.BYTES, 2L * Float.BYTES);
        glBindVertexArray(0);
    }

    public void render(int width, int height, StarSystem sys, Ship ship, String targetName,
                       TargetInfo selected, GalaxyMap galaxyMap, boolean galaxyMapOpen,
                       boolean pauseMenuOpen, String menuSubMode, String saveNameInput,
                       java.util.List<String> availableSaves,
                       boolean inventoryOpen, float invX, float invY,
                       ShipInventory.CargoItem draggingItem, float dragMouseX, float dragMouseY,
                       ShipInventory.CargoItem splitTarget, int splitAmount,
                       boolean shipMenuOpen, float shipMenuX, float shipMenuY,
                       StarSystem.MarketStation activeStation, ShipInventory stationHangar,
                       boolean stationHangarOpen, float stationInvX, float stationInvY,
                       String marketMode, String statusMsg,
                       int credits, String currentSystemId,
                       float mapPanX, float mapPanY, float mapZoom, int fps) {
        vertCount = 0;
        buffer.clear();

        // Top-left panel
        drawRect(18, 18, 560, 108, 0.03f, 0.05f, 0.09f, 0.76f);
        String zone = sys.isCoreFederation ? "FEDERATION CORE (LOW PURITY)" : "FRONTIER (HIGH PURITY)";
        drawText(30, 28, 1.8f, "SYSTEM: " + sys.systemName + " // " + zone,
            sys.isCoreFederation ? 0.45f : 1.00f, sys.isCoreFederation ? 0.85f : 0.58f, sys.isCoreFederation ? 1.0f : 0.28f, 1.0f);
        int starNum = sys.starType.ordinal() + 1;
        int starTotal = StarSystem.StarType.values().length;
        drawText(30, 50, 1.5f, "STAR [" + starNum + "/" + starTotal + "]: " + sys.starType.displayName, 1.0f, 0.84f, 0.45f, 1.0f);
        drawText(30, 70, 1.45f, String.format("CREDITS: %,d CR   [I] INVENTORY   [M] GALAXY MAP", credits).replace(",", ""), 0.45f, 0.94f, 0.68f, 1.0f);
        drawText(30, 90, 1.35f, "[ / ] CYCLE STAR   [J] JUMP GATE   [ESC] MENU", 0.65f, 0.72f, 0.82f, 0.9f);
        if (statusMsg != null && !statusMsg.isEmpty()) {
            drawRect(18, 132, 560, 28, 0.16f, 0.09f, 0.04f, 0.92f);
            drawText(30, 140, 1.35f, statusMsg, 1.00f, 0.82f, 0.35f, 1.0f);
        }

        // Top-right Selected Object Info Card
        if (selected != null) {
            int depCount = selected.deposits.size();
            float cardW = 590;
            float cardH = (selected.jumpTargetSystemId != null) ? 116
                : (selected.marketStation != null ? 148 : (128 + depCount * 20));
            float cx = width - cardW - 18;
            float cy = 18;
            drawRect(cx, cy, cardW, cardH, 0.03f, 0.05f, 0.09f, 0.88f);
            drawRect(cx, cy, cardW, 2, 0.30f, 0.82f, 1.00f, 0.90f);
            drawText(cx + 14, cy + 12, 1.8f, "TARGET: " + selected.name, 0.38f, 0.90f, 1.00f, 1.0f);
            drawText(cx + 14, cy + 36, 1.5f, "CLASS:  " + selected.category, 0.72f, 0.82f, 0.92f, 0.95f);

            float dist = selected.getPosition().distance(ship.position);
            drawText(cx + 14, cy + 56, 1.5f, String.format("DIST:   %.1f KM", dist), 1.00f, 0.84f, 0.42f, 1.0f);

            if (selected.jumpTargetSystemId != null) {
                drawText(cx + 14, cy + 82, 1.5f, "ACTION: PRESS [J] TO INITIATE STARGATE JUMP", 0.35f, 0.95f, 0.65f, 1.0f);
            } else if (selected.marketStation != null) {
                boolean inRange = dist <= 22.0f;
                drawText(cx + 14, cy + 78, 1.35f,
                    inRange ? "DOCKING PROXIMITY: IN RANGE (TRANSFER READY)" : "PROXIMITY: APPROACH <= 22.0 KM TO TRANSFER",
                    inRange ? 0.42f : 1.00f, inRange ? 0.95f : 0.65f, inRange ? 0.62f : 0.25f, 1.0f);

                float btnY = cy + 104;
                String[] actions = { "BUY", "SELL", "INVENTORY" };
                for (int i = 0; i < actions.length; i++) {
                    float bx = cx + 14 + i * 136;
                    drawRect(bx, btnY, 124, 30, 0.09f, 0.20f, 0.28f, 0.95f);
                    drawRect(bx, btnY, 124, 1.5f, 0.35f, 0.85f, 1.00f, 0.95f);
                    drawText(bx + 18, btnY + 9, 1.45f, actions[i], 0.95f, 0.98f, 1.0f, 1.0f);
                }
            } else {
                drawText(cx + 14, cy + 78, 1.4f, "MINABLE ORES (SHARE @ PURITY) -> REFINED ELEMENTS:", 0.56f, 0.92f, 0.72f, 0.95f);
                float rowY = cy + 98;
                for (MineralRegistry.OreDeposit dep : selected.deposits) {
                    String line = String.format("- %s (%d%% @ %d%%) -> %s",
                        dep.ore.displayName, dep.percent, dep.purityPercent, dep.ore.elementListText);
                    drawText(cx + 18, rowY, 1.4f, line, 0.85f, 0.94f, 0.90f, 0.95f);
                    rowY += 20;
                }
                drawText(cx + 14, rowY + 6, 1.4f, "OWNER:  " + selected.faction + "  [NAVIGATE / SURFACE SOON]", 0.55f, 0.65f, 0.76f, 0.85f);
            }
        }

        // Centered Galaxy Map Overlay [M]
        if (galaxyMapOpen) {
            float mw = GalaxyMap.panelWidth(width);
            float mh = GalaxyMap.panelHeight(height);
            float mx = (width - mw) * 0.5f;
            float my = (height - mh) * 0.5f;
            drawRect(mx, my, mw, mh, 0.02f, 0.04f, 0.08f, 0.94f);
            drawRect(mx, my, mw, 2, 0.40f, 0.86f, 1.00f, 0.95f);
            drawText(mx + 20, my + 16, 1.9f, "MILKY WAY SECTOR MAP // 16 SYSTEMS (12 STAR CLASSES)", 0.45f, 0.88f, 1.0f, 1.0f);
            drawText(mx + 20, my + 42, 1.3f, "CLICK ANY STAR TO JUMP   R-DRAG: PAN   SCROLL: ZOOM   [M] CLOSE", 0.70f, 0.78f, 0.88f, 0.9f);

            float mapCx = mx + mw * 0.5f + mapPanX;
            float mapCy = my + mh * 0.54f + mapPanY;
            float scaleX = mw * 0.36f * mapZoom;
            float scaleY = mh * 0.30f * mapZoom;

            // Draw system labels & current-system marker clipped inside map panel
            for (int i = 0; i < galaxyMap.systems.size(); i++) {
                GalaxyMap.SystemNode node = galaxyMap.systems.get(i);
                float nx = mapCx + node.mapX * scaleX;
                float ny = mapCy + node.mapY * scaleY;
                if (nx < mx + 12 || nx > mx + mw - 12 || ny < my + 62 || ny > my + mh - 18) continue;

                boolean isCurrent = node.id.equals(currentSystemId);
                float boxHalf = 22f * Math.max(0.75f, Math.min(1.6f, mapZoom));
                if (isCurrent) {
                    drawRect(nx - boxHalf, ny - boxHalf, boxHalf * 2f, 2, 0.25f, 0.95f, 0.72f, 0.85f);
                    drawRect(nx - boxHalf, ny + boxHalf - 2, boxHalf * 2f, 2, 0.25f, 0.95f, 0.72f, 0.85f);
                }
                float r = node.isCoreFederation ? 0.35f : 1.00f;
                float g = node.isCoreFederation ? 0.82f : 0.52f;
                float b = node.isCoreFederation ? 1.00f : 0.22f;

                String label = "[" + (i + 1) + "] " + node.name;
                drawText(nx - 58, ny + boxHalf + 6, 1.35f, label, 0.92f, 0.96f, 1.0f, 1.0f);
                String tier = node.isCoreFederation ? "CORE (LOW PURITY)" : "FRONTIER (HIGH PURITY)";
                drawText(nx - 58, ny + boxHalf + 22, 1.15f, tier, r, g, b, 0.9f);
            }
        }

        // Draggable Ship Inventory Window [I] (Invisible Grid + Mass & Volume + Draggable Items)
        if (inventoryOpen) {
            float iw = 456;
            float ih = 356;
            drawRect(invX, invY, iw, ih, 0.03f, 0.05f, 0.09f, 0.92f);
            drawRect(invX, invY, iw, 28, 0.08f, 0.14f, 0.22f, 0.96f);
            drawRect(invX, invY, iw, 2, 0.35f, 0.85f, 1.00f, 0.95f);
            drawText(invX + 12, invY + 8, 1.6f, "CORVETTE CARGO HOLD", 0.45f, 0.90f, 1.0f, 1.0f);

            // Close [X] button in top-right corner
            float closeX = invX + iw - 26;
            float closeY = invY + 4;
            drawRect(closeX, closeY, 20, 20, 0.32f, 0.08f, 0.10f, 0.92f);
            drawText(closeX + 6, closeY + 4, 1.5f, "X", 1.0f, 0.85f, 0.85f, 1.0f);

            // Dual Capacity Bars: Mass (KG) & Volume (M3)
            double usedMass = ship.inventory.getUsedMassKg();
            double maxMass = ship.inventory.maxMassKg;
            float massRatio = (float) Math.max(0.0, Math.min(1.0, usedMass / maxMass));

            double usedVol = ship.inventory.getUsedVolumeM3();
            double maxVol = ship.inventory.maxVolumeM3;
            float volRatio = (float) Math.max(0.0, Math.min(1.0, usedVol / maxVol));

            float halfBarW = (iw - 40) * 0.5f;
            drawText(invX + 14, invY + 38, 1.25f, String.format("MASS: %.1f / %.0f KG", usedMass, maxMass), 0.95f, 0.82f, 0.45f, 0.95f);
            drawRect(invX + 14, invY + 53, halfBarW, 7, 0.08f, 0.11f, 0.16f, 0.95f);
            drawRect(invX + 14, invY + 53, halfBarW * massRatio, 7, 0.95f, 0.68f, 0.24f, 0.95f);

            float volX = invX + 26 + halfBarW;
            drawText(volX, invY + 38, 1.25f, String.format("VOL:  %.1f / %.0f M3", usedVol, maxVol), 0.45f, 0.88f, 1.0f, 0.95f);
            drawRect(volX, invY + 53, halfBarW, 7, 0.08f, 0.11f, 0.16f, 0.95f);
            drawRect(volX, invY + 53, halfBarW * volRatio, 7, 0.30f, 0.82f, 1.00f, 0.95f);

            // Invisible grid area (6 cols x 3 visible rows out of 12, 64x64 each, 8px gap)
            float gridX = invX + 16;
            float gridY = invY + 72;
            float cell = 64f;
            float gap = 8f;

            // Draw non-dragged items within the visible scroll window
            for (ShipInventory.CargoItem item : ship.inventory.items) {
                if (item == draggingItem) continue;
                int visRow = item.gridRow - ship.inventory.scrollRow;
                if (visRow < 0 || visRow >= ShipInventory.VISIBLE_ROWS) continue;
                float ix = gridX + item.gridCol * (cell + gap);
                float iy = gridY + visRow * (cell + gap);
                boolean isSel = (item == ship.inventory.selectedItem);
                drawCargoTile(ix, iy, cell, item, isSel);
            }

            // Vertical Scrollbar
            drawVerticalScrollbar(invX + iw - 16, gridY, 8, 208, ship.inventory.scrollRow,
                ShipInventory.ROWS, ShipInventory.VISIBLE_ROWS, 0.30f, 0.82f, 1.00f);

            // Selected Item Details Footer
            float footY = invY + ih - 54;
            drawRect(invX + 12, footY, iw - 24, 42, 0.05f, 0.08f, 0.13f, 0.90f);
            if (ship.inventory.selectedItem != null) {
                ShipInventory.CargoItem sel = ship.inventory.selectedItem;
                drawText(invX + 20, footY + 7, 1.35f, sel.name + "  X" + sel.count + "  [" + sel.kind + "]", 0.92f, 0.97f, 1.0f, 1.0f);
                drawText(invX + 20, footY + 24, 1.25f,
                    String.format("MASS: %.1f KG   VOL: %.1f M3", sel.getTotalMassKg(), sel.getTotalVolumeM3()),
                    0.65f, 0.86f, 0.95f, 0.95f);
                if (activeStation != null && stationHangarOpen) {
                    drawRect(invX + iw - 146, footY + 6, 126, 30, 0.10f, 0.24f, 0.34f, 0.95f);
                    drawText(invX + iw - 138, footY + 15, 1.25f, "-> TO STATION", 0.95f, 0.98f, 1.0f, 1.0f);
                }
            } else {
                drawText(invX + 20, footY + 14, 1.25f, "DRAG: MOVE   SHIFT+DRAG: HALF   CTRL+CLICK: SPLIT", 0.55f, 0.65f, 0.76f, 0.85f);
            }
        }

        // Station Hangar Window (1000x Ship Capacity: 500,000 KG / 250,000 M3)
        if (stationHangarOpen && activeStation != null && stationHangar != null) {
            float sx = stationInvX;
            float sy = stationInvY;
            float sw = 456f;
            float sh = 356f;
            drawRect(sx, sy, sw, sh, 0.03f, 0.05f, 0.09f, 0.92f);
            drawRect(sx, sy, sw, 28, 0.12f, 0.14f, 0.22f, 0.96f);
            drawRect(sx, sy, sw, 2, 1.00f, 0.78f, 0.28f, 0.95f);
            drawText(sx + 12, sy + 8, 1.55f, activeStation.name + " HANGAR", 1.00f, 0.84f, 0.42f, 1.0f);

            float closeX = sx + sw - 26;
            float closeY = sy + 4;
            drawRect(closeX, closeY, 20, 20, 0.32f, 0.08f, 0.10f, 0.92f);
            drawText(closeX + 6, closeY + 4, 1.5f, "X", 1.0f, 0.85f, 0.85f, 1.0f);

            double usedMass = stationHangar.getUsedMassKg();
            double maxMass = stationHangar.maxMassKg;
            double usedVol = stationHangar.getUsedVolumeM3();
            double maxVol = stationHangar.maxVolumeM3;
            float halfBarW = (sw - 40) * 0.5f;
            drawText(sx + 14, sy + 38, 1.2f, String.format("MASS: %.0f / %.0f KG", usedMass, maxMass), 0.95f, 0.82f, 0.45f, 0.95f);
            drawRect(sx + 14, sy + 53, halfBarW, 7, 0.08f, 0.11f, 0.16f, 0.95f);
            drawRect(sx + 14, sy + 53, halfBarW * (float) Math.min(1.0, usedMass / maxMass), 7, 0.95f, 0.68f, 0.24f, 0.95f);

            float volX = sx + 26 + halfBarW;
            drawText(volX, sy + 38, 1.2f, String.format("VOL: %.0f / %.0f M3", usedVol, maxVol), 0.45f, 0.88f, 1.0f, 0.95f);
            drawRect(volX, sy + 53, halfBarW, 7, 0.08f, 0.11f, 0.16f, 0.95f);
            drawRect(volX, sy + 53, halfBarW * (float) Math.min(1.0, usedVol / maxVol), 7, 0.30f, 0.82f, 1.00f, 0.95f);

            float gridX = sx + 16;
            float gridY = sy + 72;
            float cell = 64f, gap = 8f;
            for (ShipInventory.CargoItem item : stationHangar.items) {
                if (item == draggingItem) continue;
                int visRow = item.gridRow - stationHangar.scrollRow;
                if (visRow < 0 || visRow >= ShipInventory.VISIBLE_ROWS) continue;
                float ix = gridX + item.gridCol * (cell + gap);
                float iy = gridY + visRow * (cell + gap);
                drawCargoTile(ix, iy, cell, item, item == stationHangar.selectedItem);
            }

            // Vertical Scrollbar
            drawVerticalScrollbar(sx + sw - 16, gridY, 8, 208, stationHangar.scrollRow,
                ShipInventory.ROWS, ShipInventory.VISIBLE_ROWS, 1.00f, 0.78f, 0.28f);

            float footY = sy + sh - 54;
            drawRect(sx + 12, footY, sw - 24, 42, 0.05f, 0.08f, 0.13f, 0.90f);
            if (stationHangar.selectedItem != null) {
                ShipInventory.CargoItem sel = stationHangar.selectedItem;
                drawText(sx + 20, footY + 7, 1.35f, sel.name + "  X" + sel.count, 0.92f, 0.97f, 1.0f, 1.0f);
                drawText(sx + 20, footY + 24, 1.25f, String.format("MASS: %.1f KG   VOL: %.1f M3", sel.getTotalMassKg(), sel.getTotalVolumeM3()), 0.65f, 0.86f, 0.95f, 0.95f);
                drawRect(sx + sw - 136, footY + 6, 116, 30, 0.10f, 0.24f, 0.34f, 0.95f);
                drawText(sx + sw - 126, footY + 15, 1.25f, "<- TO SHIP", 0.95f, 0.98f, 1.0f, 1.0f);
            } else {
                drawText(sx + 20, footY + 14, 1.25f, "DRAG ITEMS TO/FROM SHIP CARGO (<= 22 KM)", 0.55f, 0.65f, 0.76f, 0.85f);
            }
        }

        // Draw dragged item on top of BOTH inventory windows
        if (draggingItem != null) {
            drawCargoTile(dragMouseX - 32f, dragMouseY - 32f, 64f, draggingItem, true);
        }

        // Market Buy / Sell Panel
        if (marketMode != null && activeStation != null) {
            float mw = 540f;
            float mh = 340f;
            float mx = (width - mw) * 0.5f;
            float my = (height - mh) * 0.5f;
            drawRect(mx, my, mw, mh, 0.03f, 0.05f, 0.09f, 0.95f);
            drawRect(mx, my, mw, 28, 0.10f, 0.16f, 0.24f, 0.96f);
            drawRect(mx, my, mw, 2, 0.35f, 0.85f, 1.00f, 0.95f);
            drawText(mx + 14, my + 8, 1.55f, activeStation.name + " // " + marketMode + " TERMINAL", 0.45f, 0.90f, 1.0f, 1.0f);

            float closeX = mx + mw - 26;
            float closeY = my + 4;
            drawRect(closeX, closeY, 20, 20, 0.32f, 0.08f, 0.10f, 0.92f);
            drawText(closeX + 6, closeY + 4, 1.5f, "X", 1.0f, 0.85f, 0.85f, 1.0f);

            float rowY = my + 44;
            if ("BUY".equals(marketMode)) {
                for (MarketCatalog.MarketListing m : MarketCatalog.LISTINGS) {
                    drawText(mx + 16, rowY + 8, 1.35f, m.name, 0.90f, 0.95f, 1.0f, 0.95f);
                    drawText(mx + 250, rowY + 8, 1.35f, m.buyPrice + " CR", 1.00f, 0.84f, 0.42f, 1.0f);
                    drawRect(mx + 340, rowY, 82, 26, 0.10f, 0.25f, 0.34f, 0.95f);
                    drawText(mx + 354, rowY + 7, 1.3f, "BUY 1", 0.95f, 0.98f, 1.0f, 1.0f);
                    drawRect(mx + 434, rowY, 82, 26, 0.10f, 0.25f, 0.34f, 0.95f);
                    drawText(mx + 448, rowY + 7, 1.3f, "BUY 5", 0.95f, 0.98f, 1.0f, 1.0f);
                    rowY += 38;
                }
            } else if ("SELL".equals(marketMode) && stationHangar != null) {
                if (stationHangar.items.isEmpty()) {
                    drawText(mx + 20, rowY + 16, 1.4f, "STATION HANGAR IS EMPTY. TRANSFER ITEMS TO SELL.", 0.72f, 0.78f, 0.86f, 0.9f);
                } else {
                    for (int i = 0; i < Math.min(7, stationHangar.items.size()); i++) {
                        ShipInventory.CargoItem item = stationHangar.items.get(i);
                        MarketCatalog.MarketListing ml = MarketCatalog.findById(item.id);
                        int price = (ml != null) ? ml.sellPrice : 20;
                        drawText(mx + 16, rowY + 8, 1.35f, item.name + " X" + item.count, 0.90f, 0.95f, 1.0f, 0.95f);
                        drawText(mx + 250, rowY + 8, 1.35f, price + " CR", 0.56f, 0.92f, 0.72f, 1.0f);
                        drawRect(mx + 340, rowY, 82, 26, 0.26f, 0.18f, 0.10f, 0.95f);
                        drawText(mx + 350, rowY + 7, 1.3f, "SELL 1", 0.95f, 0.98f, 1.0f, 1.0f);
                        drawRect(mx + 434, rowY, 88, 26, 0.26f, 0.18f, 0.10f, 0.95f);
                        drawText(mx + 440, rowY + 7, 1.3f, "SELL ALL", 0.95f, 0.98f, 1.0f, 1.0f);
                        rowY += 38;
                    }
                }
            }
        }

        // Split Stack Slider Dialog (Ctrl/Cmd + Left-Click)
        if (splitTarget != null) {
            float dw = 340;
            float dh = 160;
            float dx = (width - dw) * 0.5f;
            float dy = (height - dh) * 0.5f;

            drawRect(dx, dy, dw, dh, 0.03f, 0.05f, 0.09f, 0.96f);
            drawRect(dx, dy, dw, 2, 0.35f, 0.85f, 1.00f, 0.95f);
            drawText(dx + 14, dy + 12, 1.5f, "SPLIT STACK: " + splitTarget.name, 0.45f, 0.90f, 1.0f, 1.0f);

            int leave = splitTarget.count - splitAmount;
            drawText(dx + 14, dy + 38, 1.35f, String.format("TAKE: %d    LEAVE: %d", splitAmount, leave), 0.95f, 0.90f, 0.55f, 1.0f);

            // Slider track
            float trackX = dx + 20;
            float trackY = dy + 68;
            float trackW = dw - 40;
            float frac = (splitTarget.count > 2) ? (float) (splitAmount - 1) / (splitTarget.count - 2) : 0.5f;
            drawRect(trackX, trackY, trackW, 8, 0.10f, 0.14f, 0.20f, 0.95f);
            drawRect(trackX, trackY, trackW * frac, 8, 0.35f, 0.85f, 1.00f, 0.95f);
            float knobX = trackX + trackW * frac - 6;
            drawRect(knobX, trackY - 5, 12, 18, 0.90f, 0.96f, 1.00f, 1.0f);

            // SPLIT & CANCEL buttons
            float btnW = 136;
            float btnH = 32;
            float btnY = dy + dh - 46;
            drawRect(dx + 20, btnY, btnW, btnH, 0.10f, 0.26f, 0.34f, 0.95f);
            drawRect(dx + 20, btnY, btnW, 1.5f, 0.35f, 0.88f, 1.00f, 0.95f);
            drawText(dx + 58, btnY + 10, 1.45f, "SPLIT", 0.95f, 0.98f, 1.0f, 1.0f);

            drawRect(dx + dw - 20 - btnW, btnY, btnW, btnH, 0.16f, 0.10f, 0.12f, 0.95f);
            drawText(dx + dw - 20 - btnW + 34, btnY + 10, 1.45f, "CANCEL", 0.85f, 0.82f, 0.85f, 0.95f);
        }

        // Ship Right-Click Context Menu
        if (shipMenuOpen) {
            float mw = 156;
            float mh = 36;
            drawRect(shipMenuX, shipMenuY, mw, mh, 0.04f, 0.07f, 0.12f, 0.95f);
            drawRect(shipMenuX, shipMenuY, mw, 2, 0.35f, 0.85f, 1.00f, 0.95f);
            drawText(shipMenuX + 16, shipMenuY + 11, 1.55f, "INVENTORY", 0.92f, 0.97f, 1.0f, 1.0f);
        }

        // Centered Pause Menu [ESC] + Save / Load Sub-Dialogs
        if (pauseMenuOpen) {
            drawRect(0, 0, width, height, 0.0f, 0.01f, 0.02f, 0.55f);

            if ("SAVE".equals(menuSubMode)) {
                float dw = 380, dh = 180;
                float dx = (width - dw) * 0.5f;
                float dy = (height - dh) * 0.5f;
                drawRect(dx, dy, dw, dh, 0.03f, 0.05f, 0.09f, 0.96f);
                drawRect(dx, dy, dw, 2, 0.35f, 0.85f, 1.00f, 0.95f);
                drawText(dx + 18, dy + 16, 1.75f, "SAVE GAME (JSON)", 0.45f, 0.90f, 1.0f, 1.0f);
                drawText(dx + 18, dy + 44, 1.3f, "ENTER SAVE FILE NAME:", 0.72f, 0.82f, 0.92f, 0.95f);

                drawRect(dx + 18, dy + 64, dw - 36, 34, 0.06f, 0.09f, 0.14f, 0.95f);
                drawRect(dx + 18, dy + 64, dw - 36, 1.5f, 0.35f, 0.85f, 1.00f, 0.85f);
                drawText(dx + 28, dy + 74, 1.55f, saveNameInput + "_", 0.95f, 0.98f, 1.0f, 1.0f);

                float btnW = 156, btnH = 34, btnY = dy + dh - 48;
                drawRect(dx + 18, btnY, btnW, btnH, 0.10f, 0.28f, 0.34f, 0.95f);
                drawRect(dx + 18, btnY, btnW, 1.5f, 0.35f, 0.88f, 1.00f, 0.95f);
                drawText(dx + 64, btnY + 10, 1.5f, "SAVE", 0.95f, 0.98f, 1.0f, 1.0f);

                drawRect(dx + dw - 18 - btnW, btnY, btnW, btnH, 0.16f, 0.10f, 0.12f, 0.95f);
                drawText(dx + dw - 18 - btnW + 46, btnY + 10, 1.5f, "CANCEL", 0.85f, 0.82f, 0.85f, 0.95f);
            } else if ("LOAD".equals(menuSubMode)) {
                float dw = 400, dh = 340;
                float dx = (width - dw) * 0.5f;
                float dy = (height - dh) * 0.5f;
                drawRect(dx, dy, dw, dh, 0.03f, 0.05f, 0.09f, 0.96f);
                drawRect(dx, dy, dw, 2, 0.35f, 0.85f, 1.00f, 0.95f);
                drawText(dx + 18, dy + 16, 1.75f, "LOAD GAME", 0.45f, 0.90f, 1.0f, 1.0f);

                if (availableSaves.isEmpty()) {
                    drawText(dx + 22, dy + 70, 1.4f, "NO SAVE FILES FOUND IN SAVES/", 0.70f, 0.76f, 0.85f, 0.9f);
                } else {
                    float ry = dy + 56;
                    for (int i = 0; i < Math.min(6, availableSaves.size()); i++) {
                        drawRect(dx + 18, ry, dw - 36, 32, 0.08f, 0.14f, 0.22f, 0.95f);
                        drawRect(dx + 18, ry, dw - 36, 1.5f, 0.35f, 0.82f, 1.00f, 0.80f);
                        drawText(dx + 28, ry + 9, 1.45f, availableSaves.get(i) + ".JSON", 0.92f, 0.97f, 1.0f, 1.0f);
                        ry += 38;
                    }
                }

                drawRect(dx + 18, dy + dh - 48, dw - 36, 34, 0.16f, 0.10f, 0.12f, 0.95f);
                drawText(dx + 162, dy + dh - 38, 1.5f, "BACK", 0.85f, 0.82f, 0.85f, 0.95f);
            } else {
                float pw = 320;
                float ph = 340;
                float px = (width - pw) * 0.5f;
                float py = (height - ph) * 0.5f;

                drawRect(px, py, pw, ph, 0.03f, 0.05f, 0.09f, 0.95f);
                drawRect(px, py, pw, 2, 0.35f, 0.85f, 1.00f, 0.95f);
                drawText(px + 92, py + 18, 2.0f, "SPACEINC MENU", 0.45f, 0.88f, 1.0f, 1.0f);

                String[] labels = { "CONTINUE", "SAVE", "LOAD", "SETTINGS", "ABOUT", "QUIT" };
                boolean[] enabled = { true, true, true, false, false, true };
                float btnW = 240;
                float btnH = 36;
                float btnX = px + (pw - btnW) * 0.5f;
                float startY = py + 58;

                for (int i = 0; i < labels.length; i++) {
                    float by = startY + i * 44;
                    boolean active = enabled[i];
                    boolean isQuit = "QUIT".equals(labels[i]);
                    if (active) {
                        drawRect(btnX, by, btnW, btnH, isQuit ? 0.24f : 0.09f, isQuit ? 0.07f : 0.16f, isQuit ? 0.08f : 0.24f, 0.95f);
                        drawRect(btnX, by, btnW, 1.5f, isQuit ? 0.95f : 0.35f, isQuit ? 0.32f : 0.82f, isQuit ? 0.32f : 1.00f, 0.9f);
                        drawText(btnX + 20, by + 11, 1.7f, labels[i], 0.95f, 0.98f, 1.0f, 1.0f);
                    } else {
                        drawRect(btnX, by, btnW, btnH, 0.05f, 0.07f, 0.11f, 0.80f);
                        drawText(btnX + 20, by + 11, 1.7f, labels[i], 0.42f, 0.48f, 0.56f, 0.8f);
                    }
                }
            }
        }

        // Bottom-left telemetry
        drawRect(18, height - 92, 410, 74, 0.03f, 0.05f, 0.09f, 0.72f);
        float speed = ship.velocity.length();
        String wp = (ship.waypoint != null) ? "AUTOPILOT ACTIVE" : "MANUAL";
        drawText(30, height - 80, 1.6f, String.format("SPEED: %5.1f M/S   MODE: %s", speed, wp), 0.78f, 0.92f, 1.0f, 1.0f);
        drawText(30, height - 58, 1.6f, "CAM FOCUS [TAB]: " + targetName, 0.45f, 0.90f, 0.75f, 1.0f);
        drawText(30, height - 38, 1.4f, "FPS: " + fps, 0.55f, 0.65f, 0.75f, 0.9f);

        // Bottom-right controls
        drawRect(width - 378, height - 112, 360, 94, 0.03f, 0.05f, 0.09f, 0.72f);
        drawText(width - 364, height - 100, 1.4f, "WASD / ARROWS : THRUST", 0.72f, 0.80f, 0.90f, 0.95f);
        drawText(width - 364, height - 82, 1.4f, "SHIFT         : AFTERBURNER", 0.72f, 0.80f, 0.90f, 0.95f);
        drawText(width - 364, height - 64, 1.4f, "LEFT CLICK    : TARGET OBJECT", 0.72f, 0.80f, 0.90f, 0.95f);
        drawText(width - 364, height - 46, 1.4f, "R-DRAG / Q-E  : ORBIT CAMERA", 0.72f, 0.80f, 0.90f, 0.95f);
        drawText(width - 364, height - 28, 1.4f, "SCROLL        : ZOOM", 0.72f, 0.80f, 0.90f, 0.95f);

        if (vertCount == 0) return;
        buffer.flip();

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader.use();
        shader.setFloat("uScreenSize", 0f); // unused, use vec2 via glUniform2f
        int loc = glGetUniformLocation(getProgramHack(), "uScreenSize");
        if (loc != -1) glUniform2f(loc, (float) width, (float) height);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertCount);
        glBindVertexArray(0);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private void drawVerticalScrollbar(float x, float y, float w, float h,
                                       int scrollRow, int totalRows, int visibleRows,
                                       float r, float g, float b) {
        drawRect(x, y, w, h, 0.07f, 0.10f, 0.15f, 0.90f);
        int maxScroll = Math.max(1, totalRows - visibleRows);
        float thumbH = Math.max(24f, h * ((float) visibleRows / totalRows));
        float frac = (float) scrollRow / maxScroll;
        float thumbY = y + frac * (h - thumbH);
        drawRect(x + 1, thumbY, w - 2, thumbH, r, g, b, 0.85f);
    }

    private void drawCargoTile(float x, float y, float size, ShipInventory.CargoItem item, boolean selected) {
        float r, g, b;
        if ("FUEL".equals(item.kind)) {
            r = 0.14f; g = 0.42f; b = 0.62f;
        } else if ("ORE".equals(item.kind)) {
            r = 0.52f; g = 0.28f; b = 0.14f;
        } else {
            r = 0.24f; g = 0.48f; b = 0.38f;
        }

        if (selected) {
            drawRect(x - 2, y - 2, size + 4, size + 4, 0.40f, 0.92f, 1.00f, 0.95f);
        }
        drawRect(x, y, size, size, r * 0.55f, g * 0.55f, b * 0.55f, 0.95f);
        drawRect(x, y, size, 3, r, g, b, 1.0f);

        // Short icon code in center
        drawText(x + 12, y + 18, 2.0f, item.shortCode, 0.95f, 0.98f, 1.0f, 1.0f);

        // Little stack count badge in bottom-right corner
        if (item.stackable) {
            String qty = String.valueOf(item.count);
            float bw = Math.max(18, qty.length() * 8 + 6);
            drawRect(x + size - bw - 2, y + size - 15, bw, 13, 0.02f, 0.04f, 0.07f, 0.88f);
            drawText(x + size - bw + 1, y + size - 13, 1.2f, qty, 1.00f, 0.90f, 0.45f, 1.0f);
        }
    }

    private int getProgramHack() {
        return glGetInteger(GL_CURRENT_PROGRAM);
    }

    private void drawText(float x, float y, float scale, String text, float r, float g, float b, float a) {
        String upper = text.toUpperCase();
        float cx = x;
        for (int i = 0; i < upper.length(); i++) {
            char ch = upper.charAt(i);
            int[] glyph = FONT.get(ch);
            if (glyph != null) {
                for (int row = 0; row < 7; row++) {
                    int bits = glyph[row];
                    for (int col = 0; col < 5; col++) {
                        if ((bits & (1 << (4 - col))) != 0) {
                            drawRect(cx + col * scale, y + row * scale, scale, scale, r, g, b, a);
                        }
                    }
                }
            }
            cx += 6.0f * scale;
        }
    }

    private void drawRect(float x, float y, float w, float h, float r, float g, float b, float a) {
        if (vertCount + 6 > MAX_VERTS) return;
        putVert(x, y, r, g, b, a);
        putVert(x, y + h, r, g, b, a);
        putVert(x + w, y + h, r, g, b, a);

        putVert(x, y, r, g, b, a);
        putVert(x + w, y + h, r, g, b, a);
        putVert(x + w, y, r, g, b, a);
    }

    private void putVert(float x, float y, float r, float g, float b, float a) {
        buffer.put(x).put(y).put(r).put(g).put(b).put(a);
        vertCount++;
    }

    public void dispose() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        MemoryUtil.memFree(buffer);
        shader.dispose();
    }
}
