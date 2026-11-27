package spaceinc;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Modular 2D tactical HUD with built-in 5x7 bitmap font.
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
        FONT.put('_', new int[]{0x00,0x00,0x00,0x00,0x00,0x00,0x1F});
        FONT.put(' ', new int[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00});
    }

    private static class Rgba {
        final float r, g, b, a;
        Rgba(float r, float g, float b, float a) { this.r = r; this.g = g; this.b = b; this.a = a; }
    }

    private static class Pos {
        final float x, y;
        Pos(float x, float y) { this.x = x; this.y = y; }
    }

    private static class WindowSpec {
        final float x, y;
        final String title;
        final Rgba accent;
        final String transferLabel;
        WindowSpec(float x, float y, String title, Rgba accent, String transferLabel) {
            this.x = x; this.y = y; this.title = title; this.accent = accent; this.transferLabel = transferLabel;
        }
    }

    private static final Rgba CYAN = new Rgba(0.38f, 0.90f, 1.00f, 1.0f);
    private static final Rgba GOLD = new Rgba(1.00f, 0.84f, 0.42f, 1.0f);
    private static final Rgba GREEN = new Rgba(0.45f, 0.94f, 0.68f, 1.0f);
    private static final Rgba WHITE = new Rgba(0.95f, 0.98f, 1.00f, 1.0f);
    private static final Rgba MUTED = new Rgba(0.65f, 0.72f, 0.82f, 0.9f);
    private static final Rgba BG_DARK = new Rgba(0.03f, 0.05f, 0.09f, 0.88f);

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

    public void render(GameState s, int fps) {
        vertCount = 0;
        buffer.clear();

        drawTopLeftPanel(s);
        drawTargetCard(s);
        drawInventoryOverlays(s);
        drawMarketModal(s);
        drawSplitDialog(s);
        drawGalaxyMap(s);
        drawPauseMenu(s);
        drawBottomHud(s, fps);

        flushToScreen(s.winWidth, s.winHeight);
    }

    private void flushToScreen(int width, int height) {
        if (vertCount == 0) return;
        buffer.flip();

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        shader.use();
        int loc = glGetUniformLocation(glGetInteger(GL_CURRENT_PROGRAM), "uScreenSize");
        if (loc != -1) glUniform2f(loc, (float) width, (float) height);

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, buffer);
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, vertCount);
        glBindVertexArray(0);

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    private void drawTopLeftPanel(GameState s) {
        drawRect(18, 18, 560, 108, new Rgba(0.03f, 0.05f, 0.09f, 0.76f));
        String zone = s.starSystem.isCoreFederation ? "FEDERATION CORE (LOW PURITY)" : "FRONTIER (HIGH PURITY)";
        Rgba zoneCol = s.starSystem.isCoreFederation ? CYAN : new Rgba(1.0f, 0.58f, 0.28f, 1.0f);
        drawText(30, 28, 1.8f, "SYSTEM: " + s.starSystem.systemName + " // " + zone, zoneCol);

        int starNum = s.starSystem.starType.ordinal() + 1;
        int starTotal = StarSystem.StarType.values().length;
        drawText(30, 50, 1.5f, "STAR [" + starNum + "/" + starTotal + "]: " + s.starSystem.starType.displayName, GOLD);
        drawText(30, 70, 1.45f, String.format("CREDITS: %d CR   [I] INVENTORY   [M] GALAXY MAP", s.credits), GREEN);
        drawText(30, 90, 1.35f, "[ / ] CYCLE STAR   [J] JUMP GATE   [ESC] MENU", MUTED);

        if (s.statusMsg != null && !s.statusMsg.isEmpty()) {
            drawRect(18, 132, 560, 28, new Rgba(0.16f, 0.09f, 0.04f, 0.92f));
            drawText(30, 140, 1.35f, s.statusMsg, GOLD);
        }
    }

    private void drawTargetCard(GameState s) {
        TargetInfo sel = s.selectedTarget;
        if (sel == null) return;

        float cardW = 590f;
        float cardH = (sel.jumpTargetSystemId != null) ? 116f
            : (sel.marketStation != null ? 148f : (128f + sel.deposits.size() * 20f));
        float cx = s.winWidth - cardW - 18f;
        float cy = 18f;

        drawRect(cx, cy, cardW, cardH, BG_DARK);
        drawRect(cx, cy, cardW, 2, CYAN);
        drawText(cx + 14, cy + 12, 1.8f, "TARGET: " + sel.name, CYAN);
        drawText(cx + 14, cy + 36, 1.5f, "CLASS:  " + sel.category, new Rgba(0.72f, 0.82f, 0.92f, 0.95f));

        float dist = sel.getPosition().distance(s.ship.position);
        drawText(cx + 14, cy + 56, 1.5f, String.format("DIST:   %.1f KM", dist), GOLD);

        if (sel.jumpTargetSystemId != null) {
            drawText(cx + 14, cy + 82, 1.5f, "ACTION: PRESS [J] TO INITIATE STARGATE JUMP", GREEN);
        } else if (sel.marketStation != null) {
            drawStationTargetActions(cx, cy, dist);
        } else {
            drawOreDepositsList(cx, cy, sel);
        }
    }

    private void drawStationTargetActions(float cx, float cy, float dist) {
        boolean inRange = dist <= 22.0f;
        String msg = inRange ? "DOCKING PROXIMITY: IN RANGE (TRANSFER READY)" : "PROXIMITY: APPROACH <= 22.0 KM TO TRANSFER";
        drawText(cx + 14, cy + 78, 1.35f, msg, inRange ? GREEN : new Rgba(1.0f, 0.65f, 0.25f, 1.0f));

        float btnY = cy + 104;
        String[] actions = { "BUY", "SELL", "INVENTORY" };
        for (int i = 0; i < actions.length; i++) {
            float bx = cx + 14 + i * 136;
            drawRect(bx, btnY, 124, 30, new Rgba(0.09f, 0.20f, 0.28f, 0.95f));
            drawRect(bx, btnY, 124, 1.5f, CYAN);
            drawText(bx + 18, btnY + 9, 1.45f, actions[i], WHITE);
        }
    }

    private void drawOreDepositsList(float cx, float cy, TargetInfo sel) {
        drawText(cx + 14, cy + 78, 1.4f, "MINABLE ORES (SHARE @ PURITY) -> REFINED ELEMENTS:", GREEN);
        float rowY = cy + 98;
        for (MineralRegistry.OreDeposit dep : sel.deposits) {
            String line = String.format("- %s (%d%% @ %d%%) -> %s",
                dep.ore.displayName, dep.percent, dep.purityPercent, dep.ore.elementListText);
            drawText(cx + 18, rowY, 1.4f, line, WHITE);
            rowY += 20;
        }
        drawText(cx + 14, rowY + 6, 1.4f, "OWNER:  " + sel.faction + "  [NAVIGATE / SURFACE SOON]", MUTED);
    }

    private void drawInventoryOverlays(GameState s) {
        if (s.inventoryOpen) {
            boolean showTransfer = s.activeStation != null && s.stationHangarOpen;
            drawInventoryWindow(s.ship.inventory,
                new WindowSpec(s.invX, s.invY, "CORVETTE CARGO HOLD", CYAN, showTransfer ? "-> TO STATION" : null),
                s.draggingItem);
        }
        if (s.stationHangarOpen && s.activeStation != null) {
            ShipInventory hangar = s.getHangarFor(s.activeStation);
            drawInventoryWindow(hangar,
                new WindowSpec(s.stationInvX, s.stationInvY, s.activeStation.name + " HANGAR", GOLD, "<- TO SHIP"),
                s.draggingItem);
        }
        if (s.shipMenuOpen) {
            drawRect(s.shipMenuX, s.shipMenuY, 156, 36, new Rgba(0.04f, 0.07f, 0.12f, 0.95f));
            drawRect(s.shipMenuX, s.shipMenuY, 156, 2, CYAN);
            drawText(s.shipMenuX + 16, s.shipMenuY + 11, 1.55f, "INVENTORY", WHITE);
        }
        if (s.draggingItem != null) {
            drawCargoTile(new Pos(s.dragMouseX - 32f, s.dragMouseY - 32f), 64f, s.draggingItem, true);
        }
    }

    private void drawInventoryWindow(ShipInventory inv, WindowSpec spec, ShipInventory.CargoItem draggingItem) {
        float wx = spec.x, wy = spec.y, w = 456f, h = 356f;
        drawRect(wx, wy, w, h, new Rgba(0.03f, 0.05f, 0.09f, 0.92f));
        drawRect(wx, wy, w, 28, new Rgba(0.08f, 0.14f, 0.22f, 0.96f));
        drawRect(wx, wy, w, 2, spec.accent);
        drawText(wx + 12, wy + 8, 1.55f, spec.title, spec.accent);

        drawRect(wx + w - 26, wy + 4, 20, 20, new Rgba(0.32f, 0.08f, 0.10f, 0.92f));
        drawText(wx + w - 20, wy + 8, 1.5f, "X", WHITE);

        drawCapacityBars(inv, wx, wy, w);
        drawInventoryGrid(inv, wx + 16, wy + 72, draggingItem);
        drawVerticalScrollbar(wx + w - 16, wy + 72, inv.scrollRow, spec.accent);
        drawInventoryFooter(inv, spec, w, h);
    }

    private void drawCapacityBars(ShipInventory inv, float wx, float wy, float w) {
        float halfBarW = (w - 40) * 0.5f;
        double usedM = inv.getUsedMassKg(), maxM = inv.maxMassKg;
        drawText(wx + 14, wy + 38, 1.2f, String.format("MASS: %.0f / %.0f KG", usedM, maxM), GOLD);
        drawRect(wx + 14, wy + 53, halfBarW, 7, new Rgba(0.08f, 0.11f, 0.16f, 0.95f));
        drawRect(wx + 14, wy + 53, halfBarW * (float) Math.min(1.0, usedM / maxM), 7, GOLD);

        float volX = wx + 26 + halfBarW;
        double usedV = inv.getUsedVolumeM3(), maxV = inv.maxVolumeM3;
        drawText(volX, wy + 38, 1.2f, String.format("VOL:  %.0f / %.0f M3", usedV, maxV), CYAN);
        drawRect(volX, wy + 53, halfBarW, 7, new Rgba(0.08f, 0.11f, 0.16f, 0.95f));
        drawRect(volX, wy + 53, halfBarW * (float) Math.min(1.0, usedV / maxV), 7, CYAN);
    }

    private void drawInventoryGrid(ShipInventory inv, float gridX, float gridY, ShipInventory.CargoItem draggingItem) {
        float cell = 64f, gap = 8f;
        for (ShipInventory.CargoItem item : inv.items) {
            if (item == draggingItem) continue;
            int visRow = item.gridRow - inv.scrollRow;
            if (visRow < 0 || visRow >= ShipInventory.VISIBLE_ROWS) continue;
            drawCargoTile(new Pos(gridX + item.gridCol * (cell + gap), gridY + visRow * (cell + gap)),
                cell, item, item == inv.selectedItem);
        }
    }

    private void drawInventoryFooter(ShipInventory inv, WindowSpec spec, float w, float h) {
        float wx = spec.x, wy = spec.y, footY = wy + h - 54;
        drawRect(wx + 12, footY, w - 24, 42, new Rgba(0.05f, 0.08f, 0.13f, 0.90f));
        if (inv.selectedItem == null) {
            drawText(wx + 20, footY + 14, 1.2f, "DRAG: MOVE   SHIFT+DRAG: HALF   CTRL+CLICK: SPLIT", MUTED);
            return;
        }
        ShipInventory.CargoItem sel = inv.selectedItem;
        drawText(wx + 20, footY + 7, 1.35f, sel.name + "  X" + sel.count, WHITE);
        drawText(wx + 20, footY + 24, 1.25f,
            String.format("MASS: %.1f KG   VOL: %.1f M3", sel.getTotalMassKg(), sel.getTotalVolumeM3()), CYAN);
        if (spec.transferLabel != null) {
            drawRect(wx + w - 146, footY + 6, 126, 30, new Rgba(0.10f, 0.24f, 0.34f, 0.95f));
            drawText(wx + w - 138, footY + 15, 1.25f, spec.transferLabel, WHITE);
        }
    }

    private void drawMarketModal(GameState s) {
        if (s.marketMode == null || s.activeStation == null) return;
        float mw = 540f, mh = 340f;
        float mx = (s.winWidth - mw) * 0.5f, my = (s.winHeight - mh) * 0.5f;

        drawRect(mx, my, mw, mh, new Rgba(0.03f, 0.05f, 0.09f, 0.95f));
        drawRect(mx, my, mw, 28, new Rgba(0.10f, 0.16f, 0.24f, 0.96f));
        drawRect(mx, my, mw, 2, CYAN);
        drawText(mx + 14, my + 8, 1.55f, s.activeStation.name + " // " + s.marketMode + " TERMINAL", CYAN);
        drawRect(mx + mw - 26, my + 4, 20, 20, new Rgba(0.32f, 0.08f, 0.10f, 0.92f));
        drawText(mx + mw - 20, my + 8, 1.5f, "X", WHITE);

        if ("BUY".equals(s.marketMode)) {
            drawMarketBuyRows(mx, my + 44);
        } else if ("SELL".equals(s.marketMode)) {
            drawMarketSellRows(mx, my + 44, s.getHangarFor(s.activeStation));
        }
    }

    private void drawMarketBuyRows(float mx, float rowY) {
        for (MarketCatalog.MarketListing m : MarketCatalog.LISTINGS) {
            drawText(mx + 16, rowY + 8, 1.35f, m.name, WHITE);
            drawText(mx + 250, rowY + 8, 1.35f, m.buyPrice + " CR", GOLD);
            drawRect(mx + 340, rowY, 82, 26, new Rgba(0.10f, 0.25f, 0.34f, 0.95f));
            drawText(mx + 354, rowY + 7, 1.3f, "BUY 1", WHITE);
            drawRect(mx + 434, rowY, 82, 26, new Rgba(0.10f, 0.25f, 0.34f, 0.95f));
            drawText(mx + 448, rowY + 7, 1.3f, "BUY 5", WHITE);
            rowY += 38;
        }
    }

    private void drawMarketSellRows(float mx, float rowY, ShipInventory hangar) {
        if (hangar.items.isEmpty()) {
            drawText(mx + 20, rowY + 16, 1.4f, "STATION HANGAR IS EMPTY. TRANSFER ITEMS TO SELL.", MUTED);
            return;
        }
        for (int i = 0; i < Math.min(7, hangar.items.size()); i++) {
            ShipInventory.CargoItem item = hangar.items.get(i);
            MarketCatalog.MarketListing ml = MarketCatalog.findById(item.id);
            int price = (ml != null) ? ml.sellPrice : 20;
            drawText(mx + 16, rowY + 8, 1.35f, item.name + " X" + item.count, WHITE);
            drawText(mx + 250, rowY + 8, 1.35f, price + " CR", GREEN);
            drawRect(mx + 340, rowY, 82, 26, new Rgba(0.26f, 0.18f, 0.10f, 0.95f));
            drawText(mx + 350, rowY + 7, 1.3f, "SELL 1", WHITE);
            drawRect(mx + 434, rowY, 88, 26, new Rgba(0.26f, 0.18f, 0.10f, 0.95f));
            drawText(mx + 440, rowY + 7, 1.3f, "SELL ALL", WHITE);
            rowY += 38;
        }
    }

    private void drawSplitDialog(GameState s) {
        if (s.splitDialogTarget == null) return;
        float dw = 340f, dh = 160f;
        float dx = (s.winWidth - dw) * 0.5f, dy = (s.winHeight - dh) * 0.5f;

        drawRect(dx, dy, dw, dh, new Rgba(0.03f, 0.05f, 0.09f, 0.96f));
        drawRect(dx, dy, dw, 2, CYAN);
        drawText(dx + 14, dy + 12, 1.5f, "SPLIT STACK: " + s.splitDialogTarget.name, CYAN);

        int leave = s.splitDialogTarget.count - s.splitDialogAmount;
        drawText(dx + 14, dy + 38, 1.35f, String.format("TAKE: %d    LEAVE: %d", s.splitDialogAmount, leave), GOLD);

        float trackX = dx + 20, trackY = dy + 68, trackW = dw - 40;
        float frac = (s.splitDialogTarget.count > 2) ? (float) (s.splitDialogAmount - 1) / (s.splitDialogTarget.count - 2) : 0.5f;
        drawRect(trackX, trackY, trackW, 8, new Rgba(0.10f, 0.14f, 0.20f, 0.95f));
        drawRect(trackX, trackY, trackW * frac, 8, CYAN);
        drawRect(trackX + trackW * frac - 6, trackY - 5, 12, 18, WHITE);

        float btnW = 136, btnH = 32, btnY = dy + dh - 46;
        drawRect(dx + 20, btnY, btnW, btnH, new Rgba(0.10f, 0.26f, 0.34f, 0.95f));
        drawRect(dx + 20, btnY, btnW, 1.5f, CYAN);
        drawText(dx + 58, btnY + 10, 1.45f, "SPLIT", WHITE);
        drawRect(dx + dw - 20 - btnW, btnY, btnW, btnH, new Rgba(0.16f, 0.10f, 0.12f, 0.95f));
        drawText(dx + dw - 20 - btnW + 34, btnY + 10, 1.45f, "CANCEL", MUTED);
    }

    private void drawGalaxyMap(GameState s) {
        if (!s.galaxyMapOpen) return;
        float mw = 680, mh = 430;
        float mx = (s.winWidth - mw) * 0.5f, my = (s.winHeight - mh) * 0.5f;
        drawRect(mx, my, mw, mh, new Rgba(0.02f, 0.04f, 0.08f, 0.94f));
        drawRect(mx, my, mw, 2, CYAN);
        drawText(mx + 20, my + 16, 1.9f, "MILKY WAY SECTOR MAP // 16 SYSTEMS (12 STAR CLASSES)", CYAN);
        drawText(mx + 20, my + 42, 1.3f, "CLICK ANY STAR TO JUMP   R-DRAG: PAN   SCROLL: ZOOM   [M] CLOSE", MUTED);

        float mapCx = mx + mw * 0.5f + s.mapPanX;
        float mapCy = my + mh * 0.54f + s.mapPanY;
        float scaleX = mw * 0.36f * s.mapZoom, scaleY = mh * 0.30f * s.mapZoom;
        float boxHalf = 22f * Math.max(0.75f, Math.min(1.6f, s.mapZoom));

        for (int i = 0; i < s.galaxyMap.systems.size(); i++) {
            GalaxyMap.SystemNode node = s.galaxyMap.systems.get(i);
            float nx = mapCx + node.mapX * scaleX;
            float ny = mapCy + node.mapY * scaleY;
            if (!isInsideMap(nx, ny, mx, my, mw, mh)) continue;
            drawMapSystemLabel(node, i, nx, ny, boxHalf, s.currentSystemId);
        }
    }

    private static boolean isInsideMap(float nx, float ny, float mx, float my, float mw, float mh) {
        return nx >= mx + 12 && nx <= mx + mw - 12 && ny >= my + 62 && ny <= my + mh - 18;
    }

    private void drawMapSystemLabel(GalaxyMap.SystemNode node, int idx, float nx, float ny, float boxHalf, String currentId) {
        if (node.id.equals(currentId)) {
            drawRect(nx - boxHalf, ny - boxHalf, boxHalf * 2f, 2, GREEN);
            drawRect(nx - boxHalf, ny + boxHalf - 2, boxHalf * 2f, 2, GREEN);
        }
        Rgba tierCol = node.isCoreFederation ? CYAN : new Rgba(1.00f, 0.52f, 0.22f, 0.9f);
        drawText(nx - 58, ny + boxHalf + 6, 1.35f, "[" + (idx + 1) + "] " + node.name, WHITE);
        drawText(nx - 58, ny + boxHalf + 22, 1.15f, node.isCoreFederation ? "CORE (LOW PURITY)" : "FRONTIER (HIGH PURITY)", tierCol);
    }

    private void drawPauseMenu(GameState s) {
        if (!s.pauseMenuOpen) return;
        drawRect(0, 0, s.winWidth, s.winHeight, new Rgba(0.0f, 0.01f, 0.02f, 0.55f));
        if ("SAVE".equals(s.menuSubMode)) {
            drawSavePrompt(s);
        } else if ("LOAD".equals(s.menuSubMode)) {
            drawLoadList(s);
        } else {
            drawMainMenuButtons(s);
        }
    }

    private void drawSavePrompt(GameState s) {
        float dw = 380, dh = 180;
        float dx = (s.winWidth - dw) * 0.5f, dy = (s.winHeight - dh) * 0.5f;
        drawRect(dx, dy, dw, dh, new Rgba(0.03f, 0.05f, 0.09f, 0.96f));
        drawRect(dx, dy, dw, 2, CYAN);
        drawText(dx + 18, dy + 16, 1.75f, "SAVE GAME (JSON)", CYAN);
        drawText(dx + 18, dy + 44, 1.3f, "ENTER SAVE FILE NAME:", MUTED);
        drawRect(dx + 18, dy + 64, dw - 36, 34, new Rgba(0.06f, 0.09f, 0.14f, 0.95f));
        drawRect(dx + 18, dy + 64, dw - 36, 1.5f, CYAN);
        drawText(dx + 28, dy + 74, 1.55f, s.saveNameInput + "_", WHITE);

        float btnW = 156, btnH = 34, btnY = dy + dh - 48;
        drawRect(dx + 18, btnY, btnW, btnH, new Rgba(0.10f, 0.28f, 0.34f, 0.95f));
        drawRect(dx + 18, btnY, btnW, 1.5f, CYAN);
        drawText(dx + 64, btnY + 10, 1.5f, "SAVE", WHITE);
        drawRect(dx + dw - 18 - btnW, btnY, btnW, btnH, new Rgba(0.16f, 0.10f, 0.12f, 0.95f));
        drawText(dx + dw - 18 - btnW + 46, btnY + 10, 1.5f, "CANCEL", MUTED);
    }

    private void drawLoadList(GameState s) {
        float dw = 400, dh = 340;
        float dx = (s.winWidth - dw) * 0.5f, dy = (s.winHeight - dh) * 0.5f;
        drawRect(dx, dy, dw, dh, new Rgba(0.03f, 0.05f, 0.09f, 0.96f));
        drawRect(dx, dy, dw, 2, CYAN);
        drawText(dx + 18, dy + 16, 1.75f, "LOAD GAME", CYAN);

        List<String> saves = s.availableSaves;
        if (saves.isEmpty()) {
            drawText(dx + 22, dy + 70, 1.4f, "NO SAVE FILES FOUND IN SAVES/", MUTED);
        } else {
            float ry = dy + 56;
            for (int i = 0; i < Math.min(6, saves.size()); i++) {
                drawRect(dx + 18, ry, dw - 36, 32, new Rgba(0.08f, 0.14f, 0.22f, 0.95f));
                drawRect(dx + 18, ry, dw - 36, 1.5f, CYAN);
                drawText(dx + 28, ry + 9, 1.45f, saves.get(i) + ".PB", WHITE);
                ry += 38;
            }
        }
        drawRect(dx + 18, dy + dh - 48, dw - 36, 34, new Rgba(0.16f, 0.10f, 0.12f, 0.95f));
        drawText(dx + 162, dy + dh - 38, 1.5f, "BACK", MUTED);
    }

    private void drawMainMenuButtons(GameState s) {
        float pw = 320, ph = 340;
        float px = (s.winWidth - pw) * 0.5f, py = (s.winHeight - ph) * 0.5f;
        drawRect(px, py, pw, ph, new Rgba(0.03f, 0.05f, 0.09f, 0.95f));
        drawRect(px, py, pw, 2, CYAN);
        drawText(px + 92, py + 18, 2.0f, "SPACEINC MENU", CYAN);

        String[] labels = { "CONTINUE", "SAVE", "LOAD", "SETTINGS", "ABOUT", "QUIT" };
        boolean[] enabled = { true, true, true, false, false, true };
        float btnW = 240, btnH = 36, btnX = px + (pw - btnW) * 0.5f, startY = py + 58;

        for (int i = 0; i < labels.length; i++) {
            float by = startY + i * 44;
            if (!enabled[i]) {
                drawRect(btnX, by, btnW, btnH, new Rgba(0.05f, 0.07f, 0.11f, 0.80f));
                drawText(btnX + 20, by + 11, 1.7f, labels[i], new Rgba(0.42f, 0.48f, 0.56f, 0.8f));
                continue;
            }
            boolean isQuit = "QUIT".equals(labels[i]);
            Rgba bg = isQuit ? new Rgba(0.24f, 0.07f, 0.08f, 0.95f) : new Rgba(0.09f, 0.16f, 0.24f, 0.95f);
            Rgba border = isQuit ? new Rgba(0.95f, 0.32f, 0.32f, 0.9f) : CYAN;
            drawRect(btnX, by, btnW, btnH, bg);
            drawRect(btnX, by, btnW, 1.5f, border);
            drawText(btnX + 20, by + 11, 1.7f, labels[i], WHITE);
        }
    }

    private void drawBottomHud(GameState s, int fps) {
        int h = s.winHeight, w = s.winWidth;
        drawRect(18, h - 92, 410, 74, new Rgba(0.03f, 0.05f, 0.09f, 0.72f));
        drawText(30, h - 80, 1.6f, String.format("SPEED: %5.1f M/S   MODE: MANUAL", s.ship.velocity.length()), WHITE);
        drawText(30, h - 58, 1.6f, "CAM FOCUS [TAB]: " + s.getFocusName(), GREEN);
        drawText(30, h - 38, 1.4f, "FPS: " + fps, MUTED);

        drawRect(w - 378, h - 112, 360, 94, new Rgba(0.03f, 0.05f, 0.09f, 0.72f));
        drawText(w - 364, h - 100, 1.4f, "WASD / ARROWS : THRUST", MUTED);
        drawText(w - 364, h - 82, 1.4f, "SHIFT         : AFTERBURNER", MUTED);
        drawText(w - 364, h - 64, 1.4f, "LEFT CLICK    : TARGET OBJECT", MUTED);
        drawText(w - 364, h - 46, 1.4f, "R-DRAG / Q-E  : ORBIT CAMERA", MUTED);
        drawText(w - 364, h - 28, 1.4f, "SCROLL        : ZOOM", MUTED);
    }

    private void drawVerticalScrollbar(float x, float y, int scrollRow, Rgba col) {
        float h = 208f;
        drawRect(x, y, 8, h, new Rgba(0.07f, 0.10f, 0.15f, 0.90f));
        int maxScroll = Math.max(1, ShipInventory.ROWS - ShipInventory.VISIBLE_ROWS);
        float thumbH = Math.max(24f, h * ((float) ShipInventory.VISIBLE_ROWS / ShipInventory.ROWS));
        float thumbY = y + ((float) scrollRow / maxScroll) * (h - thumbH);
        drawRect(x + 1, thumbY, 6, thumbH, new Rgba(col.r, col.g, col.b, 0.85f));
    }

    private void drawCargoTile(Pos pos, float size, ShipInventory.CargoItem item, boolean selected) {
        float x = pos.x, y = pos.y;
        Rgba base = "FUEL".equals(item.kind) ? new Rgba(0.14f, 0.42f, 0.62f, 1.0f)
            : ("ORE".equals(item.kind) ? new Rgba(0.52f, 0.28f, 0.14f, 1.0f) : new Rgba(0.24f, 0.48f, 0.38f, 1.0f));
        if (selected) {
            drawRect(x - 2, y - 2, size + 4, size + 4, CYAN);
        }
        drawRect(x, y, size, size, new Rgba(base.r * 0.55f, base.g * 0.55f, base.b * 0.55f, 0.95f));
        drawRect(x, y, size, 3, base);
        drawText(x + 12, y + 18, 2.0f, item.shortCode, WHITE);

        if (item.stackable) {
            String qty = String.valueOf(item.count);
            float bw = Math.max(18, qty.length() * 8 + 6);
            drawRect(x + size - bw - 2, y + size - 15, bw, 13, new Rgba(0.02f, 0.04f, 0.07f, 0.88f));
            drawText(x + size - bw + 1, y + size - 13, 1.2f, qty, GOLD);
        }
    }

    private void drawText(float x, float y, float scale, String text, Rgba col) {
        String upper = text.toUpperCase();
        float cx = x;
        for (int i = 0; i < upper.length(); i++) {
            int[] glyph = FONT.get(upper.charAt(i));
            if (glyph != null) drawGlyph(new Pos(cx, y), scale, glyph, col);
            cx += 6.0f * scale;
        }
    }

    private void drawGlyph(Pos pos, float scale, int[] glyph, Rgba col) {
        for (int row = 0; row < 7; row++) {
            int bits = glyph[row];
            for (int colIdx = 0; colIdx < 5; colIdx++) {
                if ((bits & (1 << (4 - colIdx))) != 0) {
                    drawRect(pos.x + colIdx * scale, pos.y + row * scale, scale, scale, col);
                }
            }
        }
    }

    private void drawRect(float x, float y, float w, float h, Rgba c) {
        if (vertCount + 6 > MAX_VERTS) return;
        putVert(x, y, c);
        putVert(x, y + h, c);
        putVert(x + w, y + h, c);
        putVert(x, y, c);
        putVert(x + w, y + h, c);
        putVert(x + w, y, c);
    }

    private void putVert(float x, float y, Rgba c) {
        buffer.put(x).put(y).put(c.r).put(c.g).put(c.b).put(c.a);
        vertCount++;
    }

    public void dispose() {
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        MemoryUtil.memFree(buffer);
        shader.dispose();
    }
}
