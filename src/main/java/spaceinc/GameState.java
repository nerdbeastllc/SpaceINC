package spaceinc;

import org.joml.Vector3f;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central mutable game and UI state shared across input, simulation, and rendering.
 */
public class GameState {
    public int winWidth = 1440;
    public int winHeight = 900;
    public int fbWidth = 1440;
    public int fbHeight = 900;

    public boolean rightDragging = false;
    public double lastMouseX;
    public double lastMouseY;
    public double rightPressX;
    public double rightPressY;

    public final IsometricCamera camera = new IsometricCamera();
    public final GalaxyMap galaxyMap = new GalaxyMap();
    public final StarSystem starSystem = new StarSystem();
    public final Ship ship = new Ship();
    public AsteroidField asteroidField;

    public TargetInfo selectedTarget = null;
    public float currentElapsed = 0f;
    public int focusIndex = 0;

    public boolean galaxyMapOpen = false;
    public float mapPanX = 0f;
    public float mapPanY = 0f;
    public float mapZoom = 0.72f;

    public boolean pauseMenuOpen = false;
    public String menuSubMode = null; // null, "SAVE", "LOAD"
    public String saveNameInput = "SAVE_1";
    public List<String> availableSaves = Collections.emptyList();

    public boolean inventoryOpen = false;
    public float invX = 40f;
    public float invY = 140f;
    public boolean draggingInv = false;
    public float invDragOffX = 0f;
    public float invDragOffY = 0f;

    public ShipInventory.CargoItem draggingItem = null;
    public ShipInventory.CargoItem splitSourceItem = null;
    public ShipInventory.CargoItem splitDialogTarget = null;
    public ShipInventory splitDialogInventory = null;
    public int splitDialogAmount = 1;
    public boolean draggingSlider = false;
    public float dragMouseX = 0f;
    public float dragMouseY = 0f;

    public boolean shipMenuOpen = false;
    public float shipMenuX = 0f;
    public float shipMenuY = 0f;

    public final Map<String, ShipInventory> stationHangars = new HashMap<>();
    public StarSystem.MarketStation activeStation = null;
    public boolean stationHangarOpen = false;
    public float stationInvX = 520f;
    public float stationInvY = 180f;
    public boolean draggingStationInv = false;
    public float stationDragOffX = 0f;
    public float stationDragOffY = 0f;
    public ShipInventory draggingFromInventory = null;
    public ShipInventory draggingScrollInv = null;
    public float draggingScrollWinY = 0f;

    public String marketMode = null; // null, "BUY", "SELL"
    public String statusMsg = "";
    public float statusTimer = 0f;
    public int credits = 10000;
    public String currentSystemId = "SOL";

    public void setStatus(String msg) {
        this.statusMsg = msg;
        this.statusTimer = 3.5f;
    }

    public ShipInventory getHangarFor(StarSystem.MarketStation st) {
        return stationHangars.computeIfAbsent(st.id, k -> new ShipInventory(500_000.0, 250_000.0, false));
    }

    public void jumpToSystem(GalaxyMap.SystemNode node) {
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

    public String getFocusName() {
        if (focusIndex == 0) return "PLAYER CORVETTE";
        if (focusIndex == 1) return "CENTRAL STAR";
        return starSystem.planets.get(focusIndex - 2).name;
    }

    public Vector3f getFocusTarget() {
        if (focusIndex == 0) return ship.position;
        if (focusIndex == 1) return starSystem.starPos;
        return starSystem.planets.get(focusIndex - 2).barycenter;
    }
}
