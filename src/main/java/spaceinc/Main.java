package spaceinc;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {
    private long window;
    private int winWidth = 1440;
    private int winHeight = 900;
    private int fbWidth = 1440;
    private int fbHeight = 900;

    private boolean rightDragging = false;
    private double lastMouseX, lastMouseY;
    private double rightPressX, rightPressY;

    private final IsometricCamera camera = new IsometricCamera();
    private final GalaxyMap galaxyMap = new GalaxyMap();
    private final StarSystem starSystem = new StarSystem();
    private final Ship ship = new Ship();
    private AsteroidField asteroidField;
    private TargetInfo selectedTarget = null;
    private float currentElapsed = 0f;
    private boolean galaxyMapOpen = false;
    private boolean pauseMenuOpen = false;
    private String menuSubMode = null; // null, "SAVE", "LOAD"
    private String saveNameInput = "SAVE_1";
    private java.util.List<String> availableSaves = java.util.Collections.emptyList();
    private boolean inventoryOpen = false;
    private float invX = 40f;
    private float invY = 140f;
    private boolean draggingInv = false;
    private float invDragOffX = 0f;
    private float invDragOffY = 0f;
    private ShipInventory.CargoItem draggingItem = null;
    private ShipInventory.CargoItem splitSourceItem = null;
    private ShipInventory.CargoItem splitDialogTarget = null;
    private int splitDialogAmount = 1;
    private boolean draggingSlider = false;
    private float dragMouseX = 0f;
    private float dragMouseY = 0f;
    private boolean shipMenuOpen = false;
    private float shipMenuX = 0f;
    private float shipMenuY = 0f;
    private final Map<String, ShipInventory> stationHangars = new HashMap<>();
    private StarSystem.MarketStation activeStation = null;
    private boolean stationHangarOpen = false;
    private float stationInvX = 520f;
    private float stationInvY = 180f;
    private boolean draggingStationInv = false;
    private float stationDragOffX = 0f;
    private float stationDragOffY = 0f;
    private ShipInventory draggingFromInventory = null;
    private ShipInventory splitDialogInventory = null;
    private ShipInventory draggingScrollInv = null;
    private float draggingScrollWinY = 0f;
    private String marketMode = null; // null, "BUY", "SELL"
    private String statusMsg = "";
    private float statusTimer = 0f;
    private int credits = 10000;
    private String currentSystemId = "SOL";
    private float mapPanX = 0f;
    private float mapPanY = 0f;
    private float mapZoom = 0.72f;

    private int focusIndex = 0; // 0=Ship, 1=Star, 2..10=Planets

    private String getFocusName() {
        if (focusIndex == 0) return "PLAYER CORVETTE";
        if (focusIndex == 1) return "CENTRAL STAR";
        return starSystem.planets.get(focusIndex - 2).name;
    }

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        initWindow();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
        GLFWErrorCallback cb = glfwSetErrorCallback(null);
        if (cb != null) cb.free();
    }

    private void initWindow() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(winWidth, winHeight, "SpaceINC — 3D Isometric Space PoC (Java 25)", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // VSync
        GL.createCapabilities();

        updateSizes();

        glfwSetFramebufferSizeCallback(window, (w, width, height) -> {
            fbWidth = Math.max(1, width);
            fbHeight = Math.max(1, height);
        });

        glfwSetWindowSizeCallback(window, (w, width, height) -> {
            winWidth = Math.max(1, width);
            winHeight = Math.max(1, height);
        });

        glfwSetScrollCallback(window, (w, xoff, yoff) -> {
            double[] mx = new double[1];
            double[] my = new double[1];
            glfwGetCursorPos(window, mx, my);
            float curX = (float) mx[0];
            float curY = (float) my[0];

            int maxScroll = ShipInventory.ROWS - ShipInventory.VISIBLE_ROWS;
            if (inventoryOpen && curX >= invX && curX <= invX + 456f && curY >= invY && curY <= invY + 356f) {
                ship.inventory.scrollRow = Math.max(0, Math.min(maxScroll, ship.inventory.scrollRow - (int) Math.signum(yoff)));
                return;
            }
            if (stationHangarOpen && activeStation != null &&
                curX >= stationInvX && curX <= stationInvX + 456f && curY >= stationInvY && curY <= stationInvY + 356f) {
                ShipInventory hangar = getHangarFor(activeStation);
                hangar.scrollRow = Math.max(0, Math.min(maxScroll, hangar.scrollRow - (int) Math.signum(yoff)));
                return;
            }

            if (galaxyMapOpen) {
                mapZoom = Math.max(0.28f, Math.min(5.0f, mapZoom * (float) Math.pow(1.12, yoff)));
            } else {
                camera.zoom((float) yoff);
            }
        });

        glfwSetMouseButtonCallback(window, (w, button, action, mods) -> {
            if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                double[] mx = new double[1];
                double[] my = new double[1];
                glfwGetCursorPos(window, mx, my);
                if (action == GLFW_PRESS) {
                    rightDragging = true;
                    rightPressX = mx[0];
                    rightPressY = my[0];
                    lastMouseX = mx[0];
                    lastMouseY = my[0];
                } else if (action == GLFW_RELEASE) {
                    rightDragging = false;
                    double moved = Math.hypot(mx[0] - rightPressX, my[0] - rightPressY);
                    if (moved < 6.0 && !pauseMenuOpen && !galaxyMapOpen) {
                        Vector3f rayOrigin = new Vector3f();
                        Vector3f rayDir = new Vector3f();
                        camera.screenToRay((float) mx[0], (float) my[0], winWidth, winHeight, rayOrigin, rayDir);
                        if (ship.hitTest(rayOrigin, rayDir)) {
                            shipMenuOpen = true;
                            shipMenuX = Math.min(winWidth - 164f, (float) mx[0]);
                            shipMenuY = Math.min(winHeight - 44f, (float) my[0]);
                        } else {
                            shipMenuOpen = false;
                        }
                    }
                }
            } else if (button == GLFW_MOUSE_BUTTON_LEFT) {
                double[] mx = new double[1];
                double[] my = new double[1];
                glfwGetCursorPos(window, mx, my);
                float clickX = (float) mx[0];
                float clickY = (float) my[0];

                if (action == GLFW_RELEASE) {
                    draggingInv = false;
                    draggingStationInv = false;
                    draggingSlider = false;
                    draggingScrollInv = null;
                    if (draggingItem != null) {
                        handleItemDrop(clickX, clickY);
                    }
                    return;
                }
                if (action != GLFW_PRESS) return;

                if (pauseMenuOpen) {
                    if ("SAVE".equals(menuSubMode)) {
                        float dw = 380f, dh = 180f;
                        float dx = (winWidth - dw) * 0.5f, dy = (winHeight - dh) * 0.5f;
                        float btnW = 156f, btnH = 34f, btnY = dy + dh - 48f;
                        if (clickX >= dx + 18f && clickX <= dx + 18f + btnW && clickY >= btnY && clickY <= btnY + btnH) {
                            performSave();
                        } else if (clickX >= dx + dw - 18f - btnW && clickX <= dx + dw - 18f && clickY >= btnY && clickY <= btnY + btnH) {
                            menuSubMode = null;
                        }
                        return;
                    }
                    if ("LOAD".equals(menuSubMode)) {
                        float dw = 400f, dh = 340f;
                        float dx = (winWidth - dw) * 0.5f, dy = (winHeight - dh) * 0.5f;
                        float ry = dy + 56f;
                        for (int i = 0; i < Math.min(6, availableSaves.size()); i++) {
                            if (clickX >= dx + 18f && clickX <= dx + dw - 18f && clickY >= ry && clickY <= ry + 32f) {
                                performLoad(availableSaves.get(i));
                                return;
                            }
                            ry += 38f;
                        }
                        if (clickX >= dx + 18f && clickX <= dx + dw - 18f && clickY >= dy + dh - 48f && clickY <= dy + dh - 14f) {
                            menuSubMode = null;
                        }
                        return;
                    }

                    float pw = 320f, ph = 340f;
                    float px = (winWidth - pw) * 0.5f;
                    float py = (winHeight - ph) * 0.5f;
                    float btnW = 240f, btnH = 36f;
                    float btnX = px + (pw - btnW) * 0.5f;
                    float startY = py + 58f;

                    if (clickX >= btnX && clickX <= btnX + btnW) {
                        if (clickY >= startY && clickY <= startY + btnH) {
                            pauseMenuOpen = false;
                        } else if (clickY >= startY + 44f && clickY <= startY + 44f + btnH) {
                            menuSubMode = "SAVE";
                        } else if (clickY >= startY + 88f && clickY <= startY + 88f + btnH) {
                            availableSaves = SaveManager.listSaveNames();
                            menuSubMode = "LOAD";
                        }
                        float quitY = startY + 5 * 44f;
                        if (clickY >= quitY && clickY <= quitY + btnH) {
                            glfwSetWindowShouldClose(window, true);
                        }
                    }
                    return;
                }

                // Ship Right-Click Context Menu
                if (shipMenuOpen) {
                    if (clickX >= shipMenuX && clickX <= shipMenuX + 156f &&
                        clickY >= shipMenuY && clickY <= shipMenuY + 36f) {
                        inventoryOpen = true;
                        shipMenuOpen = false;
                        return;
                    }
                    shipMenuOpen = false;
                }

                // Market Buy / Sell Terminal interaction
                if (marketMode != null && activeStation != null) {
                    float mw = 540f, mh = 340f;
                    float mxPos = (winWidth - mw) * 0.5f;
                    float myPos = (winHeight - mh) * 0.5f;
                    float closeX = mxPos + mw - 26f, closeY = myPos + 4f;
                    if (clickX >= closeX && clickX <= closeX + 20f && clickY >= closeY && clickY <= closeY + 20f) {
                        marketMode = null;
                        return;
                    }
                    ShipInventory hangar = getHangarFor(activeStation);
                    float rowY = myPos + 44f;
                    if ("BUY".equals(marketMode)) {
                        for (MarketCatalog.MarketListing ml : MarketCatalog.LISTINGS) {
                            if (clickY >= rowY && clickY <= rowY + 26f) {
                                int qty = (clickX >= mxPos + 340f && clickX <= mxPos + 422f) ? 1
                                    : ((clickX >= mxPos + 434f && clickX <= mxPos + 516f) ? 5 : 0);
                                if (qty > 0) {
                                    int cost = ml.buyPrice * qty;
                                    if (credits < cost) {
                                        setStatus("INSUFFICIENT CREDITS (NEED " + cost + " CR)");
                                    } else if (hangar.addOrStack(ml.id, ml.name, ml.shortCode, ml.kind, qty, ml.unitMassKg, ml.unitVolumeM3)) {
                                        credits -= cost;
                                        setStatus("BOUGHT " + qty + "X " + ml.name + " INTO STATION HANGAR");
                                    } else {
                                        setStatus("STATION HANGAR FULL");
                                    }
                                    return;
                                }
                            }
                            rowY += 38f;
                        }
                    } else if ("SELL".equals(marketMode)) {
                        for (int i = 0; i < Math.min(7, hangar.items.size()); i++) {
                            ShipInventory.CargoItem item = hangar.items.get(i);
                            if (clickY >= rowY && clickY <= rowY + 26f) {
                                MarketCatalog.MarketListing ml = MarketCatalog.findById(item.id);
                                int unitPrice = (ml != null) ? ml.sellPrice : 20;
                                int qty = (clickX >= mxPos + 340f && clickX <= mxPos + 422f) ? 1
                                    : ((clickX >= mxPos + 434f && clickX <= mxPos + 522f) ? item.count : 0);
                                if (qty > 0) {
                                    int earn = unitPrice * qty;
                                    hangar.removeUnits(item, qty);
                                    credits += earn;
                                    setStatus("SOLD " + qty + "X " + item.name + " FOR " + earn + " CR");
                                    return;
                                }
                            }
                            rowY += 38f;
                        }
                    }
                    if (clickX >= mxPos && clickX <= mxPos + mw && clickY >= myPos && clickY <= myPos + mh) {
                        return;
                    }
                }

                // Target Card Station Actions: [BUY] [SELL] [INVENTORY]
                if (selectedTarget != null && selectedTarget.marketStation != null) {
                    float cardW = 590f;
                    float cx = winWidth - cardW - 18f;
                    float cy = 18f;
                    float btnY = cy + 104f;
                    if (clickY >= btnY && clickY <= btnY + 30f) {
                        StarSystem.MarketStation st = selectedTarget.marketStation;
                        if (clickX >= cx + 14f && clickX <= cx + 138f) {
                            activeStation = st;
                            marketMode = "BUY";
                            return;
                        } else if (clickX >= cx + 150f && clickX <= cx + 274f) {
                            activeStation = st;
                            marketMode = "SELL";
                            return;
                        } else if (clickX >= cx + 286f && clickX <= cx + 410f) {
                            activeStation = st;
                            stationHangarOpen = true;
                            inventoryOpen = true;
                            return;
                        }
                    }
                }

                // Station Hangar Window interaction (Close [X], <- TO SHIP, Item Select/Split/Drag, or Window Drag)
                if (stationHangarOpen && activeStation != null) {
                    ShipInventory hangar = getHangarFor(activeStation);
                    float sx = stationInvX, sy = stationInvY, sw = 456f, sh = 356f;
                    if (clickX >= sx + sw - 26f && clickX <= sx + sw - 6f && clickY >= sy + 4f && clickY <= sy + 24f) {
                        stationHangarOpen = false;
                        return;
                    }
                    float footY = sy + sh - 54f;
                    if (hangar.selectedItem != null && clickX >= sx + sw - 136f && clickX <= sx + sw - 20f &&
                        clickY >= footY + 6f && clickY <= footY + 36f) {
                        transferItem(hangar.selectedItem, hangar, ship.inventory, activeStation);
                        return;
                    }
                    if (clickX >= sx && clickX <= sx + sw && clickY >= sy && clickY <= sy + sh) {
                        if (clickX >= sx + sw - 22f && clickX <= sx + sw - 4f && clickY >= sy + 72f && clickY <= sy + 280f) {
                            draggingScrollInv = hangar;
                            draggingScrollWinY = sy;
                            updateScrollFromMouse(hangar, sy, clickY);
                            return;
                        }
                        if (tryBeginItemInteraction(hangar, sx, sy, clickX, clickY, mods)) {
                            return;
                        }
                        draggingStationInv = true;
                        stationDragOffX = clickX - stationInvX;
                        stationDragOffY = clickY - stationInvY;
                        return;
                    }
                }

                // Split Stack Slider Dialog interaction
                if (splitDialogTarget != null) {
                    float dw = 340f, dh = 160f;
                    float dx = (winWidth - dw) * 0.5f;
                    float dy = (winHeight - dh) * 0.5f;
                    float trackX = dx + 20f, trackY = dy + 68f, trackW = dw - 40f;
                    if (clickX >= trackX - 8f && clickX <= trackX + trackW + 8f &&
                        clickY >= trackY - 12f && clickY <= trackY + 20f) {
                        draggingSlider = true;
                        updateSliderAmount(clickX, trackX, trackW);
                        return;
                    }
                    float btnW = 136f, btnH = 32f, btnY = dy + dh - 46f;
                    // SPLIT button
                    if (clickX >= dx + 20f && clickX <= dx + 20f + btnW &&
                        clickY >= btnY && clickY <= btnY + btnH) {
                        ShipInventory targetInv = (splitDialogInventory != null) ? splitDialogInventory : ship.inventory;
                        int[] empty = targetInv.findFirstEmptySlot();
                        if (empty != null) {
                            ShipInventory.CargoItem split = targetInv.detachSplit(splitDialogTarget, splitDialogAmount);
                            if (split != null) {
                                targetInv.dropSplitItem(split, splitDialogTarget, empty[0], empty[1]);
                            }
                        }
                        splitDialogTarget = null;
                        splitDialogInventory = null;
                        return;
                    }
                    // CANCEL button
                    if (clickX >= dx + dw - 20f - btnW && clickX <= dx + dw - 20f &&
                        clickY >= btnY && clickY <= btnY + btnH) {
                        splitDialogTarget = null;
                        splitDialogInventory = null;
                        return;
                    }
                    return;
                }

                // Draggable Inventory Window (Close [X], Item Select/Split/Drag, or Title-bar Drag)
                if (inventoryOpen) {
                    float iw = 456f, ih = 356f;
                    float closeX = invX + iw - 26f;
                    float closeY = invY + 4f;
                    if (clickX >= closeX && clickX <= closeX + 20f &&
                        clickY >= closeY && clickY <= closeY + 20f) {
                        inventoryOpen = false;
                        draggingItem = null;
                        splitSourceItem = null;
                        splitDialogTarget = null;
                        return;
                    }
                    if (clickX >= invX && clickX <= invX + iw &&
                        clickY >= invY && clickY <= invY + ih) {
                        if (clickX >= invX + iw - 22f && clickX <= invX + iw - 4f && clickY >= invY + 72f && clickY <= invY + 280f) {
                            draggingScrollInv = ship.inventory;
                            draggingScrollWinY = invY;
                            updateScrollFromMouse(ship.inventory, invY, clickY);
                            return;
                        }
                        // -> TO STATION button in ship footer when station hangar is open
                        float footY = invY + ih - 54f;
                        if (ship.inventory.selectedItem != null && activeStation != null && stationHangarOpen &&
                            clickX >= invX + iw - 146f && clickX <= invX + iw - 20f &&
                            clickY >= footY + 6f && clickY <= footY + 36f) {
                            transferItem(ship.inventory.selectedItem, ship.inventory, getHangarFor(activeStation), activeStation);
                            return;
                        }
                        if (tryBeginItemInteraction(ship.inventory, invX, invY, clickX, clickY, mods)) {
                            return;
                        }
                        // Otherwise drag window via title bar or background
                        draggingInv = true;
                        invDragOffX = clickX - invX;
                        invDragOffY = clickY - invY;
                        return;
                    }
                }

                if (galaxyMapOpen) {
                    float mw = GalaxyMap.panelWidth(winWidth), mh = GalaxyMap.panelHeight(winHeight);
                    float mapCx = (winWidth - mw) * 0.5f + mw * 0.5f + mapPanX;
                    float mapCy = (winHeight - mh) * 0.5f + mh * 0.54f + mapPanY;
                    float scaleX = mw * 0.36f * mapZoom, scaleY = mh * 0.30f * mapZoom;
                    for (GalaxyMap.SystemNode node : galaxyMap.systems) {
                        float nx = mapCx + node.mapX * scaleX;
                        float ny = mapCy + node.mapY * scaleY;
                        float hitR = 26f * Math.max(0.75f, Math.min(1.8f, mapZoom));
                        if (Math.abs((float) mx[0] - nx) < hitR && Math.abs((float) my[0] - ny) < hitR) {
                            jumpToSystem(node);
                            galaxyMapOpen = false;
                            return;
                        }
                    }
                    return;
                }

                Vector3f rayOrigin = new Vector3f();
                Vector3f rayDir = new Vector3f();
                camera.screenToRay((float) mx[0], (float) my[0], winWidth, winHeight, rayOrigin, rayDir);

                TargetInfo celestialHit = starSystem.pickCelestial(rayOrigin, rayDir);
                int astIdx = (asteroidField != null)
                    ? asteroidField.pick(rayOrigin, rayDir, currentElapsed, 1.0f)
                    : -1;
                TargetInfo astHit = (astIdx >= 0) ? asteroidField.getTargetInfo(astIdx, () -> currentElapsed) : null;

                if (celestialHit != null && astHit != null) {
                    float dCel = celestialHit.getPosition().distanceSquared(rayOrigin);
                    float dAst = astHit.getPosition().distanceSquared(rayOrigin);
                    selectedTarget = (dCel <= dAst) ? celestialHit : astHit;
                } else {
                    selectedTarget = (celestialHit != null) ? celestialHit : astHit;
                }
            }
        });

        glfwSetCursorPosCallback(window, (w, xpos, ypos) -> {
            dragMouseX = (float) xpos;
            dragMouseY = (float) ypos;
            if (draggingSlider && splitDialogTarget != null) {
                float dw = 340f;
                float dx = (winWidth - dw) * 0.5f;
                updateSliderAmount((float) xpos, dx + 20f, dw - 40f);
            }
            if (draggingScrollInv != null) {
                updateScrollFromMouse(draggingScrollInv, draggingScrollWinY, (float) ypos);
            }
            if (draggingInv && inventoryOpen) {
                invX = Math.max(0f, Math.min(winWidth - 456f, (float) xpos - invDragOffX));
                invY = Math.max(0f, Math.min(winHeight - 356f, (float) ypos - invDragOffY));
            }
            if (draggingStationInv && stationHangarOpen) {
                stationInvX = Math.max(0f, Math.min(winWidth - 456f, (float) xpos - stationDragOffX));
                stationInvY = Math.max(0f, Math.min(winHeight - 356f, (float) ypos - stationDragOffY));
            }
            if (rightDragging) {
                float dx = (float) (xpos - lastMouseX);
                float dy = (float) (ypos - lastMouseY);
                if (galaxyMapOpen) {
                    mapPanX += dx;
                    mapPanY += dy;
                } else {
                    camera.orbit(-dx * 0.0065f, dy * 0.0055f);
                }
            }
            lastMouseX = xpos;
            lastMouseY = ypos;
        });

        glfwSetCharCallback(window, (w, codepoint) -> {
            if (pauseMenuOpen && "SAVE".equals(menuSubMode)) {
                char c = (char) codepoint;
                if ((Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == ' ') && saveNameInput.length() < 20) {
                    saveNameInput += Character.toUpperCase(c);
                }
            }
        });

        glfwSetKeyCallback(window, (w, key, scancode, action, mods) -> {
            if (action != GLFW_PRESS) return;
            StarSystem.StarType[] types = StarSystem.StarType.values();
            if (key == GLFW_KEY_ESCAPE) {
                if (menuSubMode != null) {
                    menuSubMode = null;
                } else if (galaxyMapOpen) {
                    galaxyMapOpen = false;
                } else {
                    pauseMenuOpen = !pauseMenuOpen;
                }
            } else if (pauseMenuOpen) {
                if ("SAVE".equals(menuSubMode)) {
                    if (key == GLFW_KEY_BACKSPACE && !saveNameInput.isEmpty()) {
                        saveNameInput = saveNameInput.substring(0, saveNameInput.length() - 1);
                    } else if (key == GLFW_KEY_ENTER) {
                        performSave();
                    }
                }
                return;
            } else if (key == GLFW_KEY_I) {
                inventoryOpen = !inventoryOpen;
                shipMenuOpen = false;
            } else if (key == GLFW_KEY_M) {
                galaxyMapOpen = !galaxyMapOpen;
            } else if (key == GLFW_KEY_J && selectedTarget != null && selectedTarget.jumpTargetSystemId != null) {
                jumpToSystem(galaxyMap.findById(selectedTarget.jumpTargetSystemId));
            } else if (galaxyMapOpen && key >= GLFW_KEY_1 && key <= GLFW_KEY_9) {
                int idx = key - GLFW_KEY_1;
                if (idx < galaxyMap.systems.size()) {
                    jumpToSystem(galaxyMap.systems.get(idx));
                    galaxyMapOpen = false;
                }
            } else if (key >= GLFW_KEY_1 && key <= GLFW_KEY_9) {
                int idx = key - GLFW_KEY_1;
                if (idx < types.length) starSystem.starType = types[idx];
            } else if (key == GLFW_KEY_0) {
                starSystem.starType = types[9];
            } else if (key == GLFW_KEY_MINUS) {
                starSystem.starType = types[10];
            } else if (key == GLFW_KEY_EQUAL) {
                starSystem.starType = types[11];
            } else if (key == GLFW_KEY_LEFT_BRACKET) {
                int idx = (starSystem.starType.ordinal() - 1 + types.length) % types.length;
                starSystem.starType = types[idx];
            } else if (key == GLFW_KEY_RIGHT_BRACKET) {
                int idx = (starSystem.starType.ordinal() + 1) % types.length;
                starSystem.starType = types[idx];
            } else if (key == GLFW_KEY_TAB) {
                int total = 2 + starSystem.planets.size();
                if ((mods & GLFW_MOD_SHIFT) != 0) {
                    focusIndex = (focusIndex - 1 + total) % total;
                } else {
                    focusIndex = (focusIndex + 1) % total;
                }
            }
        });

        glfwShowWindow(window);
    }

    private void updateSliderAmount(float mouseX, float trackX, float trackW) {
        if (splitDialogTarget == null || splitDialogTarget.count <= 1) return;
        float t = Math.max(0f, Math.min(1f, (mouseX - trackX) / trackW));
        int maxTake = splitDialogTarget.count - 1;
        splitDialogAmount = Math.max(1, Math.min(maxTake, Math.round(1 + t * (maxTake - 1))));
    }

    private void updateScrollFromMouse(ShipInventory inv, float winY, float mouseY) {
        float trackY = winY + 72f;
        float trackH = 208f;
        float t = Math.max(0f, Math.min(1f, (mouseY - trackY) / trackH));
        int maxScroll = ShipInventory.ROWS - ShipInventory.VISIBLE_ROWS;
        inv.scrollRow = Math.round(t * maxScroll);
    }

    private boolean tryBeginItemInteraction(ShipInventory inv, float winX, float winY,
                                            float clickX, float clickY, int mods) {
        float gridX = winX + 16f, gridY = winY + 72f, cell = 64f, gap = 8f;
        int col = (int) Math.floor((clickX - gridX) / (cell + gap));
        int visRow = (int) Math.floor((clickY - gridY) / (cell + gap));
        if (col < 0 || col >= ShipInventory.COLS || visRow < 0 || visRow >= ShipInventory.VISIBLE_ROWS) return false;
        int row = visRow + inv.scrollRow;

        float cellX = gridX + col * (cell + gap);
        float cellY = gridY + visRow * (cell + gap);
        if (clickX < cellX || clickX > cellX + cell || clickY < cellY || clickY > cellY + cell) return false;

        ShipInventory.CargoItem hitItem = inv.getItemAt(col, row);
        if (hitItem == null) return false;

        inv.selectedItem = hitItem;
        boolean isCtrl = (mods & (GLFW_MOD_CONTROL | GLFW_MOD_SUPER)) != 0;
        boolean isShift = (mods & GLFW_MOD_SHIFT) != 0;

        if (isCtrl && hitItem.stackable && hitItem.count > 1) {
            splitDialogTarget = hitItem;
            splitDialogInventory = inv;
            splitDialogAmount = Math.max(1, hitItem.count / 2);
            return true;
        }
        if (isShift && hitItem.stackable && hitItem.count > 1) {
            int half = (hitItem.count + 1) / 2;
            ShipInventory.CargoItem split = inv.detachSplit(hitItem, half);
            if (split != null) {
                splitSourceItem = hitItem;
                draggingItem = split;
                draggingFromInventory = inv;
                dragMouseX = clickX;
                dragMouseY = clickY;
                return true;
            }
        }
        splitSourceItem = null;
        draggingItem = hitItem;
        draggingFromInventory = inv;
        dragMouseX = clickX;
        dragMouseY = clickY;
        return true;
    }

    private void handleItemDrop(float clickX, float clickY) {
        ShipInventory srcInv = (draggingFromInventory != null) ? draggingFromInventory : ship.inventory;
        ShipInventory hangar = (activeStation != null) ? getHangarFor(activeStation) : null;

        ShipInventory destInv = null;
        float destX = 0f, destY = 0f;
        if (inventoryOpen && clickX >= invX && clickX <= invX + 456f && clickY >= invY && clickY <= invY + 356f) {
            destInv = ship.inventory;
            destX = invX;
            destY = invY;
        } else if (stationHangarOpen && hangar != null &&
                   clickX >= stationInvX && clickX <= stationInvX + 456f &&
                   clickY >= stationInvY && clickY <= stationInvY + 356f) {
            destInv = hangar;
            destX = stationInvX;
            destY = stationInvY;
        }

        if (destInv == null) {
            if (splitSourceItem != null) {
                splitSourceItem.count += draggingItem.count;
            }
        } else if (destInv == srcInv) {
            float step = 64f + 8f;
            int targetCol = (int) Math.floor((clickX - (destX + 16f)) / step);
            int targetRow = (int) Math.floor((clickY - (destY + 72f)) / step) + srcInv.scrollRow;
            if (splitSourceItem != null) {
                srcInv.dropSplitItem(draggingItem, splitSourceItem, targetCol, targetRow);
            } else {
                srcInv.moveItemTo(draggingItem, targetCol, targetRow);
            }
        } else if (activeStation != null) {
            // Cross-inventory drag & drop (Ship <-> Station Hangar)
            float dist = ship.position.distance(activeStation.position);
            if (dist > 22.0f) {
                setStatus(String.format("TOO FAR FROM STATION (%.1f KM > 22.0 KM)", dist));
                if (splitSourceItem != null) splitSourceItem.count += draggingItem.count;
            } else {
                float step = 64f + 8f;
                int targetCol = Math.max(0, Math.min(ShipInventory.COLS - 1, (int) Math.floor((clickX - (destX + 16f)) / step)));
                int targetRow = Math.max(0, Math.min(ShipInventory.ROWS - 1, (int) Math.floor((clickY - (destY + 72f)) / step) + destInv.scrollRow));
                double addMass = draggingItem.getTotalMassKg();
                double addVol = draggingItem.getTotalVolumeM3();
                if (!destInv.canFit(addMass, addVol)) {
                    setStatus("DESTINATION CARGO CAPACITY EXCEEDED");
                    if (splitSourceItem != null) splitSourceItem.count += draggingItem.count;
                } else {
                    ShipInventory.CargoItem copy = new ShipInventory.CargoItem(
                        draggingItem.id, draggingItem.name, draggingItem.shortCode, draggingItem.kind,
                        draggingItem.count, draggingItem.stackable, draggingItem.unitMassKg, draggingItem.unitVolumeM3,
                        targetCol, targetRow
                    );
                    ShipInventory.CargoItem occupant = destInv.getItemAt(targetCol, targetRow);
                    if (occupant == null) {
                        destInv.items.add(copy);
                        destInv.selectedItem = copy;
                    } else if (occupant.stackable && occupant.id.equals(copy.id)) {
                        occupant.count += copy.count;
                        destInv.selectedItem = occupant;
                    } else {
                        int[] empty = destInv.findFirstEmptySlot();
                        if (empty == null) {
                            setStatus("NO EMPTY SLOTS IN DESTINATION");
                            if (splitSourceItem != null) splitSourceItem.count += draggingItem.count;
                            draggingItem = null;
                            splitSourceItem = null;
                            draggingFromInventory = null;
                            return;
                        }
                        copy.gridCol = empty[0];
                        copy.gridRow = empty[1];
                        destInv.items.add(copy);
                        destInv.selectedItem = copy;
                    }
                    if (splitSourceItem == null) {
                        srcInv.removeUnits(draggingItem, draggingItem.count);
                    }
                    setStatus("TRANSFERRED " + copy.count + "X " + copy.name);
                }
            }
        }

        draggingItem = null;
        splitSourceItem = null;
        draggingFromInventory = null;
    }

    private void performSave() {
        try {
            SaveManager.saveGame(saveNameInput, currentSystemId, credits, ship, stationHangars);
            setStatus("SAVED GAME: " + SaveManager.sanitizeFileName(saveNameInput) + ".JSON");
            menuSubMode = null;
            pauseMenuOpen = false;
        } catch (Exception e) {
            setStatus("SAVE FAILED: " + e.getMessage());
        }
    }

    private void performLoad(String saveName) {
        try {
            SaveManager.LoadedState st = SaveManager.loadGame(saveName);
            jumpToSystem(galaxyMap.findById(st.currentSystemId));
            credits = st.credits;
            ship.position.set(st.shipPos);
            ship.yaw = st.shipYaw;
            ship.inventory.items.clear();
            ship.inventory.items.addAll(st.shipItems);
            ship.inventory.selectedItem = null;

            stationHangars.clear();
            for (Map.Entry<String, java.util.List<ShipInventory.CargoItem>> e : st.stationHangars.entrySet()) {
                ShipInventory inv = new ShipInventory(500_000.0, 250_000.0, false);
                inv.items.addAll(e.getValue());
                stationHangars.put(e.getKey(), inv);
            }

            menuSubMode = null;
            pauseMenuOpen = false;
            setStatus("LOADED SAVE: " + saveName + ".JSON (V" + st.schemaVersion + ")");
        } catch (Exception e) {
            setStatus("LOAD FAILED: " + e.getMessage());
        }
    }

    private ShipInventory getHangarFor(StarSystem.MarketStation st) {
        return stationHangars.computeIfAbsent(st.id, k -> new ShipInventory(500_000.0, 250_000.0, false));
    }

    private void setStatus(String msg) {
        this.statusMsg = msg;
        this.statusTimer = 3.5f;
    }

    private void transferItem(ShipInventory.CargoItem item, ShipInventory from, ShipInventory to, StarSystem.MarketStation st) {
        float dist = ship.position.distance(st.position);
        if (dist > 22.0f) {
            setStatus(String.format("TOO FAR FROM STATION (%.1f KM > 22.0 KM)", dist));
            return;
        }
        int qty = item.count;
        if (to.addOrStack(item.id, item.name, item.shortCode, item.kind, qty, item.unitMassKg, item.unitVolumeM3)) {
            from.removeUnits(item, qty);
            setStatus("TRANSFERRED " + qty + "X " + item.name);
        } else {
            setStatus("DESTINATION CARGO CAPACITY EXCEEDED");
        }
    }

    private void jumpToSystem(GalaxyMap.SystemNode node) {
        currentSystemId = node.id;
        starSystem.loadSystem(node, galaxyMap);
        if (asteroidField != null) {
            asteroidField.setCoreFederation(node.isCoreFederation);
        }
        selectedTarget = null;
        activeStation = null;
        stationHangarOpen = false;
        marketMode = null;
        focusIndex = 0;
        ship.position.set(0f, 0f, 118f);
        ship.velocity.set(0f, 0f, 0f);
    }

    private void updateSizes() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer fw = stack.mallocInt(1);
            IntBuffer fh = stack.mallocInt(1);
            glfwGetFramebufferSize(window, fw, fh);
            fbWidth = Math.max(1, fw.get(0));
            fbHeight = Math.max(1, fh.get(0));

            IntBuffer ww = stack.mallocInt(1);
            IntBuffer wh = stack.mallocInt(1);
            glfwGetWindowSize(window, ww, wh);
            winWidth = Math.max(1, ww.get(0));
            winHeight = Math.max(1, wh.get(0));
        }
    }

    private void loop() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);

        Mesh sphereMesh = Mesh.createSphere(80, 140);
        Mesh quadMesh = Mesh.createQuad();
        Mesh shipMesh = Mesh.createSpaceship();
        Mesh stationMesh = Mesh.createStation();
        Mesh waypointRing = Mesh.createOrbitCircle(36, 1.4f);
        Mesh prominenceMesh = Mesh.createProminences(26, 32);
        asteroidField = new AsteroidField(1150, 1500);
        starSystem.loadSystem(galaxyMap.findById("SOL"), galaxyMap);
        asteroidField.setCoreFederation(true);
        ship.initTrail();

        Shader skyboxShader = new Shader("shaders/skybox.vert", "shaders/skybox.frag");
        Shader starShader = new Shader("shaders/star.vert", "shaders/star.frag");
        Shader prominenceShader = new Shader("shaders/prominence.vert", "shaders/prominence.frag");
        Shader coronaShader = new Shader("shaders/corona.vert", "shaders/corona.frag");
        Shader planetShader = new Shader("shaders/planet.vert", "shaders/planet.frag");
        Shader ringShader = new Shader("shaders/ring.vert", "shaders/ring.frag");
        Shader asteroidShader = new Shader("shaders/asteroid.vert", "shaders/asteroid.frag");
        Shader shipShader = new Shader("shaders/ship.vert", "shaders/ship.frag");
        Shader lineShader = new Shader("shaders/line.vert", "shaders/line.frag");

        BloomRenderer bloom = new BloomRenderer(fbWidth, fbHeight, quadMesh);
        HudOverlay hud = new HudOverlay();

        double lastTime = glfwGetTime();
        double fpsTimer = lastTime;
        int frames = 0;
        int currentFps = 60;

        while (!glfwWindowShouldClose(window)) {
            double now = glfwGetTime();
            float dt = (float) Math.min(0.05, now - lastTime);
            lastTime = now;
            float elapsed = (float) now;
            currentElapsed = elapsed;
            if (statusTimer > 0f) {
                statusTimer -= dt;
                if (statusTimer <= 0f) statusMsg = "";
            }

            frames++;
            if (now - fpsTimer >= 0.5) {
                currentFps = (int) Math.round(frames / (now - fpsTimer));
                frames = 0;
                fpsTimer = now;
            }

            glfwPollEvents();
            bloom.resize(fbWidth, fbHeight);

            // Camera keyboard orbit
            if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) camera.orbit(-1.5f * dt, 0f);
            if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) camera.orbit(1.5f * dt, 0f);

            // Ship manual thrust relative to isometric camera
            Vector3f moveInput = new Vector3f();
            Vector3f camFwd = camera.getForwardXZ();
            Vector3f camRight = camera.getRightXZ();
            if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS) {
                moveInput.add(camFwd);
            }
            if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS) {
                moveInput.sub(camFwd);
            }
            if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) {
                moveInput.add(camRight);
            }
            if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS || glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) {
                moveInput.sub(camRight);
            }
            boolean boost = glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS;

            starSystem.update(dt);
            ship.update(dt, moveInput, boost);

            Vector3f focusTarget;
            if (focusIndex == 0) {
                focusTarget = ship.position;
            } else if (focusIndex == 1) {
                focusTarget = starSystem.starPos;
            } else {
                focusTarget = starSystem.planets.get(focusIndex - 2).barycenter;
            }
            camera.update(dt, focusTarget, (float) fbWidth / fbHeight);

            Matrix4f view = camera.getView();
            Matrix4f proj = camera.getProjection();
            Matrix4f invVP = new Matrix4f(proj).mul(view).invert();

            // ================= HDR SCENE PASS =================
            bloom.beginScene();

            // 1. Deep-Space Skybox
            glDepthFunc(GL_LEQUAL);
            glDepthMask(false);
            skyboxShader.use();
            skyboxShader.setMat4("uInvVP", invVP);
            skyboxShader.setVec3("uCameraPos", camera.getEyePos());
            quadMesh.draw();
            glDepthMask(true);
            glDepthFunc(GL_LESS);

            // 2. Tactical Orbit Rings & Waypoint
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
            lineShader.use();
            lineShader.setMat4("uView", view);
            lineShader.setMat4("uProjection", proj);
            lineShader.setInt("uUseAlphaAttr", 0);
            lineShader.setVec3("uColor", 0.22f, 0.45f, 0.72f);
            lineShader.setFloat("uOpacity", 0.11f);
            for (StarSystem.Planet p : starSystem.planets) {
                p.orbitMesh.draw();
            }

            // Moon orbit rings for the focused planet (or all if zoomed in close)
            if (focusIndex >= 2) {
                StarSystem.Planet fp = starSystem.planets.get(focusIndex - 2);
                lineShader.setVec3("uColor", 0.35f, 0.68f, 0.85f);
                lineShader.setFloat("uOpacity", 0.14f);
                for (StarSystem.Moon m : fp.moons) {
                    Matrix4f mOrbView = new Matrix4f(view)
                        .translate(fp.isBinaryPluto ? fp.barycenter : fp.position)
                        .rotateZ(fp.isBinaryPluto ? 0f : fp.axialTilt)
                        .rotateX(fp.isBinaryPluto ? 0f : m.inclination);
                    lineShader.setMat4("uView", mOrbView);
                    m.orbitMesh.draw();
                }
            }

            // Draw 3D Jump Gates
            for (StarSystem.JumpGate g : starSystem.jumpGates) {
                Matrix4f gView = new Matrix4f(view).translate(g.position).scale(g.radius / 1.4f);
                lineShader.setMat4("uView", gView);
                if (g.isFrontier) {
                    lineShader.setVec3("uColor", 1.00f, 0.55f, 0.20f);
                } else {
                    lineShader.setVec3("uColor", 0.35f, 0.85f, 1.00f);
                }
                lineShader.setFloat("uOpacity", 0.90f);
                waypointRing.draw();

                Matrix4f gView2 = new Matrix4f(view).translate(g.position).rotateX(1.1f).rotateZ(elapsed * 0.4f).scale(g.radius * 0.72f / 1.4f);
                lineShader.setMat4("uView", gView2);
                waypointRing.draw();
            }

            if (selectedTarget != null) {
                Vector3f tPos = selectedTarget.getPosition();
                float ringScale = Math.max(1.2f, selectedTarget.radius * 1.45f) / 1.4f;
                Matrix4f tgtView = new Matrix4f(view).translate(tPos).scale(ringScale);
                lineShader.setMat4("uView", tgtView);
                lineShader.setVec3("uColor", 0.30f, 0.90f, 1.00f);
                lineShader.setFloat("uOpacity", 0.90f);
                waypointRing.draw();
            }
            glDisable(GL_BLEND);

            // 3. Central Star
            StarSystem.StarType st = starSystem.starType;
            starShader.use();
            Matrix4f starModel = new Matrix4f()
                .translate(starSystem.starPos)
                .rotateY(elapsed * 0.03f)
                .scale(st.radius);
            starShader.setMat4("uModel", starModel);
            starShader.setMat4("uView", view);
            starShader.setMat4("uProjection", proj);
            starShader.setFloat("uTime", elapsed);
            starShader.setVec3("uCameraPos", camera.getEyePos());
            starShader.setVec3("uHotColor", st.hotColor);
            starShader.setVec3("uMidColor", st.midColor);
            starShader.setVec3("uCoolColor", st.coolColor);
            starShader.setFloat("uGranulationScale", st.granulationScale);
            starShader.setFloat("uHdrIntensity", st.hdrIntensity);
            sphereMesh.draw();

            // 3b. 3D Solar Prominence Loops & Flare Arches
            glDisable(GL_CULL_FACE);
            glDepthMask(false);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            prominenceShader.use();
            prominenceShader.setMat4("uModel", starModel);
            prominenceShader.setMat4("uView", view);
            prominenceShader.setMat4("uProjection", proj);
            prominenceShader.setFloat("uTime", elapsed);
            prominenceShader.setVec3("uHotColor", st.hotColor);
            prominenceShader.setVec3("uMidColor", st.midColor);
            prominenceShader.setFloat("uHdrIntensity", st.hdrIntensity);
            prominenceMesh.draw();
            glDisable(GL_BLEND);
            glDepthMask(true);
            glEnable(GL_CULL_FACE);

            // 4. All Planets, Moons, & Planetary Rings
            planetShader.use();
            planetShader.setMat4("uView", view);
            planetShader.setMat4("uProjection", proj);
            planetShader.setFloat("uTime", elapsed);
            planetShader.setVec3("uStarPos", starSystem.starPos);
            planetShader.setVec3("uStarLightColor", st.lightColor);
            planetShader.setVec3("uCameraPos", camera.getEyePos());

            for (StarSystem.Planet p : starSystem.planets) {
                planetShader.use();
                planetShader.setInt("uPlanetType", p.planetType);
                planetShader.setVec3("uColorA", p.colorA);
                planetShader.setVec3("uColorB", p.colorB);
                planetShader.setVec3("uColorC", p.colorC);
                planetShader.setMat4("uModel", new Matrix4f()
                    .translate(p.position)
                    .rotateZ(p.axialTilt)
                    .rotateY(elapsed * p.spinSpeed)
                    .scale(p.radius));
                sphereMesh.draw();

                for (StarSystem.Moon m : p.moons) {
                    planetShader.setInt("uPlanetType", m.planetType);
                    planetShader.setVec3("uColorA", m.colorA);
                    planetShader.setVec3("uColorB", m.colorB);
                    planetShader.setVec3("uColorC", m.colorC);
                    planetShader.setMat4("uModel", new Matrix4f()
                        .translate(m.position)
                        .rotateY(elapsed * 0.05f)
                        .scale(m.radius));
                    sphereMesh.draw();
                }

                if (p.hasRings && p.ringMesh != null) {
                    glDisable(GL_CULL_FACE);
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                    ringShader.use();
                    ringShader.setMat4("uModel", new Matrix4f()
                        .translate(p.position)
                        .rotateZ(p.axialTilt)
                        .scale(p.radius));
                    ringShader.setMat4("uView", view);
                    ringShader.setMat4("uProjection", proj);
                    ringShader.setVec3("uStarPos", starSystem.starPos);
                    ringShader.setVec3("uStarLightColor", st.lightColor);
                    ringShader.setVec3("uPlanetPos", p.position);
                    ringShader.setFloat("uPlanetRadius", p.radius);
                    p.ringMesh.draw();
                    glDisable(GL_BLEND);
                    glEnable(GL_CULL_FACE);
                }
            }

            // 4b. Instanced Asteroid Belt & Oort Cloud (single draw call)
            asteroidShader.use();
            asteroidShader.setVec3("uCameraPos", camera.getEyePos());
            asteroidField.draw(asteroidShader, view, proj, elapsed, starSystem);

            // 4c. NPC Market Stations
            shipShader.use();
            shipShader.setMat4("uView", view);
            shipShader.setMat4("uProjection", proj);
            shipShader.setVec3("uStarPos", starSystem.starPos);
            shipShader.setVec3("uStarLightColor", st.lightColor);
            shipShader.setVec3("uCameraPos", camera.getEyePos());
            shipShader.setFloat("uThrust", 0.85f);
            for (StarSystem.MarketStation ms : starSystem.marketStations) {
                shipShader.setMat4("uModel", new Matrix4f()
                    .translate(ms.position)
                    .rotateY(elapsed * 0.12f)
                    .scale(1.1f));
                stationMesh.draw();
            }

            // 5. Player Starship & Engine Trail
            shipShader.use();
            shipShader.setMat4("uModel", ship.getModelMatrix());
            shipShader.setMat4("uView", view);
            shipShader.setMat4("uProjection", proj);
            shipShader.setVec3("uStarPos", starSystem.starPos);
            shipShader.setVec3("uStarLightColor", st.lightColor);
            shipShader.setVec3("uCameraPos", camera.getEyePos());
            shipShader.setFloat("uThrust", ship.thrustLevel);
            shipMesh.draw();

            lineShader.setInt("uUseAlphaAttr", 1);
            lineShader.setFloat("uOpacity", 0.85f);
            ship.drawTrail(lineShader, view, proj);

            // 6. Star Volumetric Corona & Radial Rays (additive billboard)
            glDisable(GL_CULL_FACE);
            glDepthMask(false);
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
            float billboardRadius = st.radius * 3.4f;
            coronaShader.use();
            coronaShader.setMat4("uView", view);
            coronaShader.setMat4("uProjection", proj);
            coronaShader.setVec3("uStarPos", starSystem.starPos);
            coronaShader.setVec3("uCameraPos", camera.getEyePos());
            coronaShader.setFloat("uStarRadius", st.radius);
            coronaShader.setFloat("uBillboardRadius", billboardRadius);
            coronaShader.setFloat("uTime", elapsed);
            coronaShader.setVec3("uCoronaColor", st.coronaColor);
            coronaShader.setVec3("uHotColor", st.hotColor);
            coronaShader.setFloat("uHdrIntensity", st.hdrIntensity);
            quadMesh.draw();
            glDisable(GL_BLEND);
            glDepthMask(true);
            glEnable(GL_CULL_FACE);

            // ================= HDR BLOOM + TONE MAPPING =================
            bloom.endSceneAndComposite(1.15f);

            // ================= TACTICAL 2D HUD =================
            ShipInventory activeHangar = (activeStation != null) ? getHangarFor(activeStation) : null;
            hud.render(winWidth, winHeight, starSystem, ship, getFocusName(),
                selectedTarget, galaxyMap, galaxyMapOpen, pauseMenuOpen, menuSubMode,
                saveNameInput, availableSaves,
                inventoryOpen, invX, invY, draggingItem, dragMouseX, dragMouseY,
                splitDialogTarget, splitDialogAmount,
                shipMenuOpen, shipMenuX, shipMenuY,
                activeStation, activeHangar, stationHangarOpen, stationInvX, stationInvY,
                marketMode, statusMsg, credits,
                currentSystemId, mapPanX, mapPanY, mapZoom, currentFps);

            // ================= GALAXY MAP 3D MINIATURE STARS =================
            if (galaxyMapOpen && !pauseMenuOpen) {
                glClear(GL_DEPTH_BUFFER_BIT);
                glEnable(GL_DEPTH_TEST);
                glEnable(GL_SCISSOR_TEST);
                float scaleFactorX = (float) fbWidth / winWidth;
                float scaleFactorY = (float) fbHeight / winHeight;
                float mw = GalaxyMap.panelWidth(winWidth), mh = GalaxyMap.panelHeight(winHeight);
                float mx = (winWidth - mw) * 0.5f;
                float my = (winHeight - mh) * 0.5f;
                glScissor(
                    Math.round((mx + 2) * scaleFactorX),
                    Math.round((winHeight - (my + mh - 2)) * scaleFactorY),
                    Math.round((mw - 4) * scaleFactorX),
                    Math.round((mh - 64) * scaleFactorY)
                );

                Matrix4f mapProj = new Matrix4f().ortho(0f, winWidth, 0f, winHeight, -200f, 200f);
                Matrix4f mapView = new Matrix4f().identity();

                float mapCx = mx + mw * 0.5f + mapPanX;
                float mapCy = my + mh * 0.54f + mapPanY;
                float scaleX = mw * 0.36f * mapZoom, scaleY = mh * 0.30f * mapZoom;
                float zoomStarScale = Math.max(0.6f, Math.min(2.2f, mapZoom));

                for (GalaxyMap.SystemNode node : galaxyMap.systems) {
                    StarSystem.StarType nst = node.starType;
                    float nx = mapCx + node.mapX * scaleX;
                    float ny = winHeight - (mapCy + node.mapY * scaleY);
                    float mapRadius = (11.0f + nst.radius * 0.52f) * zoomStarScale;
                    Vector3f mapStarPos = new Vector3f(nx, ny, 0f);
                    Vector3f mapCamPos = new Vector3f(nx, ny, 120f);

                    starShader.use();
                    starShader.setMat4("uModel", new Matrix4f()
                        .translate(mapStarPos)
                        .rotateY(elapsed * 0.25f)
                        .scale(mapRadius));
                    starShader.setMat4("uView", mapView);
                    starShader.setMat4("uProjection", mapProj);
                    starShader.setFloat("uTime", elapsed);
                    starShader.setVec3("uCameraPos", mapCamPos);
                    starShader.setVec3("uHotColor", nst.hotColor);
                    starShader.setVec3("uMidColor", nst.midColor);
                    starShader.setVec3("uCoolColor", nst.coolColor);
                    starShader.setFloat("uGranulationScale", nst.granulationScale);
                    starShader.setFloat("uHdrIntensity", nst.hdrIntensity * 1.15f);
                    sphereMesh.draw();

                    glDisable(GL_CULL_FACE);
                    glDepthMask(false);
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE);
                    float bRad = mapRadius * 2.6f;
                    coronaShader.use();
                    coronaShader.setMat4("uView", mapView);
                    coronaShader.setMat4("uProjection", mapProj);
                    coronaShader.setVec3("uStarPos", mapStarPos);
                    coronaShader.setVec3("uCameraPos", mapCamPos);
                    coronaShader.setFloat("uStarRadius", mapRadius);
                    coronaShader.setFloat("uBillboardRadius", bRad);
                    coronaShader.setFloat("uTime", elapsed);
                    coronaShader.setVec3("uCoronaColor", nst.coronaColor);
                    coronaShader.setVec3("uHotColor", nst.hotColor);
                    coronaShader.setFloat("uHdrIntensity", nst.hdrIntensity);
                    quadMesh.draw();
                    glDisable(GL_BLEND);
                    glDepthMask(true);
                    glEnable(GL_CULL_FACE);
                }
                glDisable(GL_SCISSOR_TEST);
            }

            glfwSwapBuffers(window);
        }

        // Cleanup
        sphereMesh.dispose();
        quadMesh.dispose();
        shipMesh.dispose();
        stationMesh.dispose();
        waypointRing.dispose();
        prominenceMesh.dispose();
        asteroidField.dispose();
        starSystem.dispose();
        ship.dispose();

        skyboxShader.dispose();
        starShader.dispose();
        prominenceShader.dispose();
        coronaShader.dispose();
        planetShader.dispose();
        ringShader.dispose();
        asteroidShader.dispose();
        shipShader.dispose();
        lineShader.dispose();
        bloom.dispose();
        hud.dispose();
    }
}
