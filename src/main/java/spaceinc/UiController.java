package spaceinc;

import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Handles keyboard, mouse, pause/save/load, market, galaxy map, and 3D world picking.
 */
public class UiController {
    private static final Map<Integer, Integer> STAR_KEY_MAP = createStarKeyMap();
    private final GameState s;
    private final long window;
    private final InventoryUiController invUi;

    public UiController(GameState state, long window) {
        this.s = state;
        this.window = window;
        this.invUi = new InventoryUiController(state);
    }

    private static Map<Integer, Integer> createStarKeyMap() {
        Map<Integer, Integer> m = new HashMap<>();
        for (int i = 0; i < 9; i++) m.put(GLFW_KEY_1 + i, i);
        m.put(GLFW_KEY_0, 9);
        m.put(GLFW_KEY_MINUS, 10);
        m.put(GLFW_KEY_EQUAL, 11);
        return m;
    }

    public void handleScroll(double yoff, float curX, float curY) {
        if (invUi.tryScroll(curX, curY, yoff)) return;
        if (s.galaxyMapOpen) {
            s.mapZoom = Math.max(0.28f, Math.min(5.0f, s.mapZoom * (float) Math.pow(1.12, yoff)));
        } else {
            s.camera.zoom((float) yoff);
        }
    }

    private static final class Pt {
        final float x, y;
        Pt(float x, float y) { this.x = x; this.y = y; }
    }

    private static final class MarketCtx {
        final float mx, rowY;
        final ShipInventory hangar;
        MarketCtx(float mx, float rowY, ShipInventory hangar) {
            this.mx = mx;
            this.rowY = rowY;
            this.hangar = hangar;
        }
    }

    public void handleRightButton(int action, float mx, float my) {
        if (action == GLFW_PRESS) {
            s.rightDragging = true;
            s.rightPressX = mx; s.rightPressY = my;
            s.lastMouseX = mx; s.lastMouseY = my;
            return;
        }
        if (action != GLFW_RELEASE) return;
        s.rightDragging = false;
        if (!isRightClickValid(mx, my)) return;

        Vector3f rayOrigin = new Vector3f(), rayDir = new Vector3f();
        s.camera.screenToRay(mx, my, s.winWidth, s.winHeight, rayOrigin, rayDir);
        if (s.ship.hitTest(rayOrigin, rayDir)) {
            s.shipMenuOpen = true;
            s.shipMenuX = Math.min(s.winWidth - 164f, mx);
            s.shipMenuY = Math.min(s.winHeight - 44f, my);
        } else {
            s.shipMenuOpen = false;
        }
    }

    private boolean isRightClickValid(float mx, float my) {
        if (s.pauseMenuOpen || s.galaxyMapOpen) return false;
        return Math.hypot(mx - s.rightPressX, my - s.rightPressY) < 6.0;
    }

    public void handleLeftButton(int action, int mods, float clickX, float clickY) {
        if (action == GLFW_RELEASE) {
            handleLeftRelease(clickX, clickY);
            return;
        }
        if (action != GLFW_PRESS) return;
        if (tryModalClicks(clickX, clickY, mods)) return;
        if (s.galaxyMapOpen) { handleGalaxyMapClick(clickX, clickY); return; }
        pickWorldTarget(clickX, clickY);
    }

    private boolean tryModalClicks(float cx, float cy, int mods) {
        if (tryMenuOrMarketClick(cx, cy)) return true;
        return tryHudPanelClick(cx, cy, mods);
    }

    private boolean tryMenuOrMarketClick(float cx, float cy) {
        if (s.pauseMenuOpen) { handlePauseMenuClick(cx, cy); return true; }
        if (handleShipContextMenuClick(cx, cy)) return true;
        if (s.marketMode == null || s.activeStation == null) return false;
        return handleMarketClick(cx, cy);
    }

    private boolean tryHudPanelClick(float cx, float cy, int mods) {
        if (handleStationCardClick(cx, cy)) return true;
        if (s.stationHangarOpen && s.activeStation != null && invUi.handleStationHangarClick(cx, cy, mods)) return true;
        if (s.splitDialogTarget != null) { invUi.handleSplitDialogClick(cx, cy); return true; }
        return s.inventoryOpen && invUi.handleShipInventoryClick(cx, cy, mods);
    }

    private void handleLeftRelease(float clickX, float clickY) {
        s.draggingInv = false;
        s.draggingStationInv = false;
        s.draggingSlider = false;
        s.draggingScrollInv = null;
        if (s.draggingItem != null) invUi.handleItemDrop(clickX, clickY);
    }

    private boolean handleShipContextMenuClick(float clickX, float clickY) {
        if (!s.shipMenuOpen) return false;
        s.shipMenuOpen = false;
        if (!InventoryUiController.hitRect(clickX, clickY, s.shipMenuX, s.shipMenuY, 156f, 36f)) return false;
        s.inventoryOpen = true;
        return true;
    }

    private void handlePauseMenuClick(float clickX, float clickY) {
        if ("SAVE".equals(s.menuSubMode)) { handleSavePromptClick(clickX, clickY); return; }
        if ("LOAD".equals(s.menuSubMode)) { handleLoadListClick(clickX, clickY); return; }

        float px = (s.winWidth - 320f) * 0.5f, py = (s.winHeight - 340f) * 0.5f;
        float btnX = px + 40f, startY = py + 58f;
        if (!InventoryUiController.hitRect(clickX, clickY, btnX, startY, 240f, 260f)) return;

        int idx = (int) ((clickY - startY) / 44f);
        if ((clickY - startY) - idx * 44f <= 36f) applyPauseMenuAction(idx);
    }

    private void applyPauseMenuAction(int idx) {
        if (idx == 0) s.pauseMenuOpen = false;
        else if (idx == 1) s.menuSubMode = "SAVE";
        else if (idx == 2) { s.availableSaves = SaveManager.listSaveNames(); s.menuSubMode = "LOAD"; }
        else if (idx == 5) glfwSetWindowShouldClose(window, true);
    }

    private void handleSavePromptClick(float clickX, float clickY) {
        float dx = (s.winWidth - 380f) * 0.5f, dy = (s.winHeight - 180f) * 0.5f;
        float btnY = dy + 132f;
        if (InventoryUiController.hitRect(clickX, clickY, dx + 18f, btnY, 156f, 34f)) performSave();
        else if (InventoryUiController.hitRect(clickX, clickY, dx + 206f, btnY, 156f, 34f)) s.menuSubMode = null;
    }

    private void handleLoadListClick(float clickX, float clickY) {
        float dx = (s.winWidth - 400f) * 0.5f, dy = (s.winHeight - 340f) * 0.5f;
        float ry = dy + 56f;
        for (int i = 0; i < Math.min(6, s.availableSaves.size()); i++) {
            if (InventoryUiController.hitRect(clickX, clickY, dx + 18f, ry, 364f, 32f)) {
                performLoad(s.availableSaves.get(i));
                return;
            }
            ry += 38f;
        }
        if (InventoryUiController.hitRect(clickX, clickY, dx + 18f, dy + 292f, 364f, 34f)) s.menuSubMode = null;
    }

    private boolean handleMarketClick(float clickX, float clickY) {
        float mx = (s.winWidth - 540f) * 0.5f, my = (s.winHeight - 340f) * 0.5f;
        if (InventoryUiController.hitRect(clickX, clickY, mx + 514f, my + 4f, 20f, 20f)) {
            s.marketMode = null;
            return true;
        }
        Pt click = new Pt(clickX, clickY);
        MarketCtx ctx = new MarketCtx(mx, my + 44f, s.getHangarFor(s.activeStation));
        if ("BUY".equals(s.marketMode)) processMarketBuy(click, ctx);
        else if ("SELL".equals(s.marketMode)) processMarketSell(click, ctx);
        return InventoryUiController.hitRect(clickX, clickY, mx, my, 540f, 340f);
    }

    private void processMarketBuy(Pt click, MarketCtx ctx) {
        float rowY = ctx.rowY;
        for (MarketCatalog.MarketListing ml : MarketCatalog.LISTINGS) {
            if (click.y >= rowY && click.y <= rowY + 26f) {
                int qty = InventoryUiController.hitRect(click.x, click.y, ctx.mx + 340f, rowY, 82f, 26f) ? 1
                    : (InventoryUiController.hitRect(click.x, click.y, ctx.mx + 434f, rowY, 82f, 26f) ? 5 : 0);
                if (qty > 0) executeBuy(ml, qty, ctx.hangar);
                return;
            }
            rowY += 38f;
        }
    }

    private void executeBuy(MarketCatalog.MarketListing ml, int qty, ShipInventory hangar) {
        int cost = ml.buyPrice * qty;
        if (s.credits < cost) { s.setStatus("INSUFFICIENT CREDITS (NEED " + cost + " CR)"); return; }
        if (hangar.addOrStack(ml.toItemSpec(), qty)) {
            s.credits -= cost;
            s.setStatus("BOUGHT " + qty + "X " + ml.name + " INTO STATION HANGAR");
        } else {
            s.setStatus("STATION HANGAR FULL");
        }
    }

    private void processMarketSell(Pt click, MarketCtx ctx) {
        float rowY = ctx.rowY;
        for (int i = 0; i < Math.min(7, ctx.hangar.items.size()); i++) {
            ShipInventory.CargoItem item = ctx.hangar.items.get(i);
            if (click.y >= rowY && click.y <= rowY + 26f) {
                int qty = InventoryUiController.hitRect(click.x, click.y, ctx.mx + 340f, rowY, 82f, 26f) ? 1
                    : (InventoryUiController.hitRect(click.x, click.y, ctx.mx + 434f, rowY, 88f, 26f) ? item.count : 0);
                if (qty > 0) executeSell(item, qty, ctx.hangar);
                return;
            }
            rowY += 38f;
        }
    }

    private void executeSell(ShipInventory.CargoItem item, int qty, ShipInventory hangar) {
        MarketCatalog.MarketListing ml = MarketCatalog.findById(item.id);
        int earn = ((ml != null) ? ml.sellPrice : 20) * qty;
        hangar.removeUnits(item, qty);
        s.credits += earn;
        s.setStatus("SOLD " + qty + "X " + item.name + " FOR " + earn + " CR");
    }

    private boolean handleStationCardClick(float clickX, float clickY) {
        if (s.selectedTarget == null || s.selectedTarget.marketStation == null) return false;
        float cx = s.winWidth - 590f - 18f, btnY = 122f;
        if (clickY < btnY || clickY > btnY + 30f) return false;

        for (int i = 0; i < 3; i++) {
            float bx = cx + 14f + i * 136f;
            if (clickX >= bx && clickX <= bx + 124f) {
                activateStationTab(s.selectedTarget.marketStation, i);
                return true;
            }
        }
        return false;
    }

    private void activateStationTab(StarSystem.MarketStation st, int tab) {
        s.activeStation = st;
        if (tab == 0) s.marketMode = "BUY";
        else if (tab == 1) s.marketMode = "SELL";
        else { s.stationHangarOpen = true; s.inventoryOpen = true; }
    }

    private void handleGalaxyMapClick(float clickX, float clickY) {
        float mw = 680f, mh = 430f;
        float mapCx = (s.winWidth - mw) * 0.5f + mw * 0.5f + s.mapPanX;
        float mapCy = (s.winHeight - mh) * 0.5f + mh * 0.54f + s.mapPanY;
        float scaleX = mw * 0.36f * s.mapZoom, scaleY = mh * 0.30f * s.mapZoom;
        float hitR = 26f * Math.max(0.75f, Math.min(1.8f, s.mapZoom));

        for (GalaxyMap.SystemNode node : s.galaxyMap.systems) {
            float nx = mapCx + node.mapX * scaleX, ny = mapCy + node.mapY * scaleY;
            if (Math.abs(clickX - nx) < hitR && Math.abs(clickY - ny) < hitR) {
                s.jumpToSystem(node);
                s.galaxyMapOpen = false;
                return;
            }
        }
    }

    private void pickWorldTarget(float clickX, float clickY) {
        Vector3f rayOrigin = new Vector3f(), rayDir = new Vector3f();
        s.camera.screenToRay(clickX, clickY, s.winWidth, s.winHeight, rayOrigin, rayDir);

        TargetInfo celHit = s.starSystem.pickCelestial(rayOrigin, rayDir);
        int astIdx = (s.asteroidField != null) ? s.asteroidField.pick(rayOrigin, rayDir, s.currentElapsed) : -1;
        TargetInfo astHit = (astIdx >= 0) ? s.asteroidField.getTargetInfo(astIdx, () -> s.currentElapsed) : null;

        if (celHit != null && astHit != null) {
            float dCel = celHit.getPosition().distanceSquared(rayOrigin);
            float dAst = astHit.getPosition().distanceSquared(rayOrigin);
            s.selectedTarget = (dCel <= dAst) ? celHit : astHit;
        } else {
            s.selectedTarget = (celHit != null) ? celHit : astHit;
        }
    }

    public void handleMouseMove(float xpos, float ypos) {
        invUi.handleMouseMove(xpos, ypos);
        if (s.rightDragging) {
            float dx = (float) (xpos - s.lastMouseX), dy = (float) (ypos - s.lastMouseY);
            if (s.galaxyMapOpen) { s.mapPanX += dx; s.mapPanY += dy; }
            else { s.camera.orbit(-dx * 0.0065f, dy * 0.0055f); }
        }
        s.lastMouseX = xpos;
        s.lastMouseY = ypos;
    }

    public void handleCharInput(int codepoint) {
        if (!s.pauseMenuOpen || !"SAVE".equals(s.menuSubMode)) return;
        char c = (char) codepoint;
        if (isValidSaveChar(c) && s.saveNameInput.length() < 20) {
            s.saveNameInput += Character.toUpperCase(c);
        }
    }

    private static boolean isValidSaveChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == ' ';
    }

    public void handleKeyPress(int key, int mods) {
        if (key == GLFW_KEY_ESCAPE) { handleEscape(); return; }
        if (s.pauseMenuOpen) { handleSaveTypingKey(key); return; }
        if (handleGlobalShortcutKey(key)) return;
        handleStarAndFocusKeys(key, mods);
    }

    private boolean handleGlobalShortcutKey(int key) {
        if (key == GLFW_KEY_I) { s.inventoryOpen = !s.inventoryOpen; s.shipMenuOpen = false; return true; }
        if (key == GLFW_KEY_M) { s.galaxyMapOpen = !s.galaxyMapOpen; return true; }
        if (key == GLFW_KEY_J && canJumpSelectedGate()) {
            s.jumpToSystem(s.galaxyMap.findById(s.selectedTarget.jumpTargetSystemId));
            return true;
        }
        if (tryMapNumberShortcut(key)) return true;
        return false;
    }

    private boolean tryMapNumberShortcut(int key) {
        if (!s.galaxyMapOpen || key < GLFW_KEY_1 || key > GLFW_KEY_9) return false;
        jumpToMapIndex(key - GLFW_KEY_1);
        return true;
    }

    private boolean canJumpSelectedGate() {
        return s.selectedTarget != null && s.selectedTarget.jumpTargetSystemId != null;
    }

    private void jumpToMapIndex(int idx) {
        if (idx >= s.galaxyMap.systems.size()) return;
        s.jumpToSystem(s.galaxyMap.systems.get(idx));
        s.galaxyMapOpen = false;
    }

    private void handleEscape() {
        if (s.menuSubMode != null) s.menuSubMode = null;
        else if (s.galaxyMapOpen) s.galaxyMapOpen = false;
        else s.pauseMenuOpen = !s.pauseMenuOpen;
    }

    private void handleSaveTypingKey(int key) {
        if (!"SAVE".equals(s.menuSubMode)) return;
        if (key == GLFW_KEY_BACKSPACE && !s.saveNameInput.isEmpty()) {
            s.saveNameInput = s.saveNameInput.substring(0, s.saveNameInput.length() - 1);
        } else if (key == GLFW_KEY_ENTER) {
            performSave();
        }
    }

    private void handleStarAndFocusKeys(int key, int mods) {
        StarSystem.StarType[] types = StarSystem.StarType.values();
        Integer starIdx = STAR_KEY_MAP.get(key);
        if (starIdx != null && starIdx < types.length) { s.starSystem.starType = types[starIdx]; return; }
        if (key == GLFW_KEY_LEFT_BRACKET) { cycleStar(types, -1); return; }
        if (key == GLFW_KEY_RIGHT_BRACKET) { cycleStar(types, 1); return; }
        if (key == GLFW_KEY_TAB) {
            int total = 2 + s.starSystem.planets.size();
            int dir = ((mods & GLFW_MOD_SHIFT) != 0) ? -1 : 1;
            s.focusIndex = (s.focusIndex + dir + total) % total;
        }
    }

    private void cycleStar(StarSystem.StarType[] types, int delta) {
        s.starSystem.starType = types[(s.starSystem.starType.ordinal() + delta + types.length) % types.length];
    }

    private void performSave() {
        try {
            SaveManager.saveGame(s.saveNameInput, s);
            s.setStatus("SAVED GAME: " + SaveManager.sanitizeFileName(s.saveNameInput) + ".PB");
            s.menuSubMode = null;
            s.pauseMenuOpen = false;
        } catch (Exception e) {
            s.setStatus("SAVE FAILED: " + e.getMessage());
        }
    }

    private void performLoad(String saveName) {
        try {
            SaveManager.LoadedState st = SaveManager.loadGame(saveName);
            s.jumpToSystem(s.galaxyMap.findById(st.currentSystemId));
            s.credits = st.credits;
            s.ship.position.set(st.shipPos);
            s.ship.yaw = st.shipYaw;
            s.ship.inventory.items.clear();
            s.ship.inventory.items.addAll(st.shipItems);
            s.ship.inventory.selectedItem = null;

            s.stationHangars.clear();
            for (Map.Entry<String, List<ShipInventory.CargoItem>> e : st.stationHangars.entrySet()) {
                ShipInventory inv = new ShipInventory(500_000.0, 250_000.0, false);
                inv.items.addAll(e.getValue());
                s.stationHangars.put(e.getKey(), inv);
            }
            s.menuSubMode = null;
            s.pauseMenuOpen = false;
            s.setStatus("LOADED SAVE: " + saveName + ".PB (V" + st.schemaVersion + ")");
        } catch (Exception e) {
            s.setStatus("LOAD FAILED: " + e.getMessage());
        }
    }
}
