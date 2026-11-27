package spaceinc;

import org.joml.Vector3f;
import spaceinc.proto.CargoItemState;
import spaceinc.proto.InventoryState;
import spaceinc.proto.SaveGame;
import spaceinc.proto.ShipState;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Versioned Protocol Buffers Save/Load Manager.
 *
 * BACKWARD COMPATIBILITY CONTRACT:
 * - Every save file includes schema_version (currently 1) in save_game.proto.
 * - Protobuf field numbers must never be changed or reused.
 * - Future fields default safely when reading older save files.
 */
public final class SaveManager {
    public static final int CURRENT_SCHEMA_VERSION = 1;
    private static final String SAVE_EXT = ".pb";
    private static final Path SAVE_DIR = Paths.get("saves");

    private SaveManager() {}

    public static List<String> listSaveNames() {
        List<String> names = new ArrayList<>();
        if (!Files.isDirectory(SAVE_DIR)) return names;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(SAVE_DIR, "*" + SAVE_EXT)) {
            for (Path p : stream) {
                String fn = p.getFileName().toString();
                names.add(fn.substring(0, fn.length() - SAVE_EXT.length()));
            }
        } catch (IOException ignored) {}
        Collections.sort(names);
        return names;
    }

    public static String sanitizeFileName(String raw) {
        if (raw == null) return "quicksave";
        StringBuilder out = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == ' ') out.append('_');
            else if (isSafeFileChar(c)) out.append(c);
        }
        return (out.length() == 0) ? "quicksave" : out.toString();
    }

    private static boolean isSafeFileChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
            || (c >= '0' && c <= '9') || c == '_' || c == '-';
    }

    public static void saveGame(String rawName, GameState state) throws IOException {
        Files.createDirectories(SAVE_DIR);
        Path target = SAVE_DIR.resolve(sanitizeFileName(rawName) + SAVE_EXT);
        SaveGame proto = buildSaveProto(state);
        try (OutputStream out = Files.newOutputStream(target)) {
            proto.writeTo(out);
        }
    }

    private static SaveGame buildSaveProto(GameState state) {
        SaveGame.Builder builder = SaveGame.newBuilder()
            .setSchemaVersion(CURRENT_SCHEMA_VERSION)
            .setCurrentSystemId(state.currentSystemId)
            .setCredits(state.credits)
            .setShip(buildShipProto(state.ship));
        for (Map.Entry<String, ShipInventory> entry : state.stationHangars.entrySet()) {
            builder.putStationHangars(entry.getKey(), buildInventoryProto(entry.getValue().items));
        }
        return builder.build();
    }

    private static ShipState buildShipProto(Ship ship) {
        return ShipState.newBuilder()
            .setX(ship.position.x)
            .setY(ship.position.y)
            .setZ(ship.position.z)
            .setYaw(ship.yaw)
            .setInventory(buildInventoryProto(ship.inventory.items))
            .build();
    }

    private static InventoryState buildInventoryProto(List<ShipInventory.CargoItem> items) {
        InventoryState.Builder builder = InventoryState.newBuilder();
        for (ShipInventory.CargoItem it : items) {
            builder.addItems(toItemProto(it));
        }
        return builder.build();
    }

    private static CargoItemState toItemProto(ShipInventory.CargoItem it) {
        return CargoItemState.newBuilder()
            .setId(it.id)
            .setName(it.name)
            .setShortCode(it.shortCode)
            .setKind(it.kind)
            .setCount(it.count)
            .setStackable(it.stackable)
            .setUnitMassKg(it.unitMassKg)
            .setUnitVolumeM3(it.unitVolumeM3)
            .setGridCol(it.gridCol)
            .setGridRow(it.gridRow)
            .build();
    }

    public static LoadedState loadGame(String saveName) throws IOException {
        Path target = SAVE_DIR.resolve(sanitizeFileName(saveName) + SAVE_EXT);
        SaveGame proto;
        try (InputStream in = Files.newInputStream(target)) {
            proto = SaveGame.parseFrom(in);
        }

        LoadedState state = new LoadedState();
        state.schemaVersion = (proto.getSchemaVersion() > 0) ? proto.getSchemaVersion() : 1;
        if (!proto.getCurrentSystemId().isEmpty()) state.currentSystemId = proto.getCurrentSystemId();
        state.credits = proto.getCredits();
        if (proto.hasShip()) applyShipProto(proto.getShip(), state);
        for (Map.Entry<String, InventoryState> e : proto.getStationHangarsMap().entrySet()) {
            state.stationHangars.put(e.getKey(), parseItems(e.getValue()));
        }
        return state;
    }

    private static void applyShipProto(ShipState ship, LoadedState state) {
        state.shipPos.set(ship.getX(), ship.getY(), ship.getZ());
        state.shipYaw = ship.getYaw();
        if (ship.hasInventory()) {
            state.shipItems = parseItems(ship.getInventory());
        }
    }

    private static List<ShipInventory.CargoItem> parseItems(InventoryState inv) {
        List<ShipInventory.CargoItem> list = new ArrayList<>();
        for (CargoItemState c : inv.getItemsList()) {
            int count = Math.max(1, c.getCount());
            list.add(new ShipInventory.CargoItem(toItemSpec(c), count, c.getGridCol(), c.getGridRow()));
        }
        return list;
    }

    private static ShipInventory.ItemSpec toItemSpec(CargoItemState c) {
        return new ShipInventory.ItemSpec(
            c.getId().isEmpty() ? "UNKNOWN" : c.getId(),
            c.getName().isEmpty() ? "UNKNOWN ITEM" : c.getName(),
            c.getShortCode().isEmpty() ? "?" : c.getShortCode(),
            c.getKind().isEmpty() ? "ORE" : c.getKind(),
            c.getStackable(),
            (c.getUnitMassKg() > 0.0) ? c.getUnitMassKg() : 1.0,
            (c.getUnitVolumeM3() > 0.0) ? c.getUnitVolumeM3() : 1.0
        );
    }

    public static class LoadedState {
        public int schemaVersion = 1;
        public String currentSystemId = "SOL";
        public int credits = 10000;
        public final Vector3f shipPos = new Vector3f(0f, 0f, 118f);
        public float shipYaw = (float) Math.PI;
        public List<ShipInventory.CargoItem> shipItems = new ArrayList<>();
        public final Map<String, List<ShipInventory.CargoItem>> stationHangars = new HashMap<>();
    }
}
