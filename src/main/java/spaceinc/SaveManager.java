package spaceinc;

import org.joml.Vector3f;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Versioned JSON Save/Load Manager.
 *
 * BACKWARD COMPATIBILITY CONTRACT:
 * - Every save file includes "schemaVersion" (currently 1).
 * - All fields added in future versions MUST be optional when reading older saves,
 *   falling back to sensible defaults so older save files always load cleanly.
 */
public final class SaveManager {
    public static final int CURRENT_SCHEMA_VERSION = 1;
    private static final Path SAVE_DIR = Paths.get("saves");

    private SaveManager() {}

    public static List<String> listSaveNames() {
        List<String> names = new ArrayList<>();
        if (!Files.isDirectory(SAVE_DIR)) return names;
        try {
            Files.list(SAVE_DIR)
                .filter(p -> p.toString().endsWith(".json"))
                .sorted()
                .forEach(p -> {
                    String fn = p.getFileName().toString();
                    names.add(fn.substring(0, fn.length() - 5));
                });
        } catch (IOException ignored) {}
        return names;
    }

    public static String sanitizeFileName(String raw) {
        String cleaned = raw.trim().replaceAll("[^a-zA-Z0-9_\\- ]", "").replace(' ', '_');
        return cleaned.isEmpty() ? "quicksave" : cleaned;
    }

    public static void saveGame(String rawName, String currentSystemId, int credits,
                                Ship ship, Map<String, ShipInventory> stationHangars) throws IOException {
        Files.createDirectories(SAVE_DIR);
        String safeName = sanitizeFileName(rawName);
        Path target = SAVE_DIR.resolve(safeName + ".json");

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"schemaVersion\": ").append(CURRENT_SCHEMA_VERSION).append(",\n");
        sb.append("  \"currentSystemId\": \"").append(escape(currentSystemId)).append("\",\n");
        sb.append("  \"credits\": ").append(credits).append(",\n");
        sb.append("  \"ship\": {\n");
        sb.append("    \"x\": ").append(ship.position.x).append(",\n");
        sb.append("    \"y\": ").append(ship.position.y).append(",\n");
        sb.append("    \"z\": ").append(ship.position.z).append(",\n");
        sb.append("    \"yaw\": ").append(ship.yaw).append(",\n");
        sb.append("    \"inventory\": ");
        appendItemsJson(sb, ship.inventory.items, "    ");
        sb.append("\n  },\n");
        sb.append("  \"stationHangars\": {\n");
        int hIdx = 0;
        for (Map.Entry<String, ShipInventory> entry : stationHangars.entrySet()) {
            if (hIdx > 0) sb.append(",\n");
            sb.append("    \"").append(escape(entry.getKey())).append("\": ");
            appendItemsJson(sb, entry.getValue().items, "    ");
            hIdx++;
        }
        sb.append("\n  }\n");
        sb.append("}\n");

        Files.write(target, sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    public static LoadedState loadGame(String saveName) throws IOException {
        Path target = SAVE_DIR.resolve(sanitizeFileName(saveName) + ".json");
        String json = new String(Files.readAllBytes(target), StandardCharsets.UTF_8);
        Map<String, Object> root = (Map<String, Object>) new JsonParser(json).parseValue();

        int version = optInt(root, "schemaVersion", 1);
        String sysId = optString(root, "currentSystemId", "SOL");
        int credits = optInt(root, "credits", 10000);

        Vector3f shipPos = new Vector3f(0f, 0f, 118f);
        float yaw = (float) Math.PI;
        List<ShipInventory.CargoItem> shipItems = new ArrayList<>();

        Object shipObj = root.get("ship");
        if (shipObj instanceof Map) {
            Map<String, Object> sm = (Map<String, Object>) shipObj;
            shipPos.set(optFloat(sm, "x", 0f), optFloat(sm, "y", 0f), optFloat(sm, "z", 118f));
            yaw = optFloat(sm, "yaw", (float) Math.PI);
            shipItems = parseItems(sm.get("inventory"));
        }

        Map<String, List<ShipInventory.CargoItem>> hangars = new HashMap<>();
        Object hangarsObj = root.get("stationHangars");
        if (hangarsObj instanceof Map) {
            Map<String, Object> hm = (Map<String, Object>) hangarsObj;
            for (Map.Entry<String, Object> e : hm.entrySet()) {
                hangars.put(e.getKey(), parseItems(e.getValue()));
            }
        }

        return new LoadedState(version, sysId, credits, shipPos, yaw, shipItems, hangars);
    }

    public static class LoadedState {
        public final int schemaVersion;
        public final String currentSystemId;
        public final int credits;
        public final Vector3f shipPos;
        public final float shipYaw;
        public final List<ShipInventory.CargoItem> shipItems;
        public final Map<String, List<ShipInventory.CargoItem>> stationHangars;

        public LoadedState(int schemaVersion, String currentSystemId, int credits,
                           Vector3f shipPos, float shipYaw, List<ShipInventory.CargoItem> shipItems,
                           Map<String, List<ShipInventory.CargoItem>> stationHangars) {
            this.schemaVersion = schemaVersion;
            this.currentSystemId = currentSystemId;
            this.credits = credits;
            this.shipPos = shipPos;
            this.shipYaw = shipYaw;
            this.shipItems = shipItems;
            this.stationHangars = stationHangars;
        }
    }

    private static void appendItemsJson(StringBuilder sb, List<ShipInventory.CargoItem> items, String indent) {
        sb.append("[\n");
        for (int i = 0; i < items.size(); i++) {
            ShipInventory.CargoItem it = items.get(i);
            sb.append(indent).append("  {");
            sb.append("\"id\":\"").append(escape(it.id)).append("\",");
            sb.append("\"name\":\"").append(escape(it.name)).append("\",");
            sb.append("\"shortCode\":\"").append(escape(it.shortCode)).append("\",");
            sb.append("\"kind\":\"").append(escape(it.kind)).append("\",");
            sb.append("\"count\":").append(it.count).append(",");
            sb.append("\"stackable\":").append(it.stackable).append(",");
            sb.append("\"unitMassKg\":").append(it.unitMassKg).append(",");
            sb.append("\"unitVolumeM3\":").append(it.unitVolumeM3).append(",");
            sb.append("\"gridCol\":").append(it.gridCol).append(",");
            sb.append("\"gridRow\":").append(it.gridRow);
            sb.append("}");
            if (i < items.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append(indent).append("]");
    }

    @SuppressWarnings("unchecked")
    private static List<ShipInventory.CargoItem> parseItems(Object listObj) {
        List<ShipInventory.CargoItem> list = new ArrayList<>();
        if (!(listObj instanceof List)) return list;
        for (Object elem : (List<Object>) listObj) {
            if (!(elem instanceof Map)) continue;
            Map<String, Object> m = (Map<String, Object>) elem;
            list.add(new ShipInventory.CargoItem(
                optString(m, "id", "UNKNOWN"),
                optString(m, "name", "UNKNOWN ITEM"),
                optString(m, "shortCode", "?"),
                optString(m, "kind", "ORE"),
                optInt(m, "count", 1),
                optBool(m, "stackable", true),
                optDouble(m, "unitMassKg", 1.0),
                optDouble(m, "unitVolumeM3", 1.0),
                optInt(m, "gridCol", 0),
                optInt(m, "gridRow", 0)
            ));
        }
        return list;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String optString(Map<String, Object> m, String k, String def) {
        Object v = m.get(k);
        return (v instanceof String) ? (String) v : def;
    }

    private static int optInt(Map<String, Object> m, String k, int def) {
        Object v = m.get(k);
        return (v instanceof Number) ? ((Number) v).intValue() : def;
    }

    private static float optFloat(Map<String, Object> m, String k, float def) {
        Object v = m.get(k);
        return (v instanceof Number) ? ((Number) v).floatValue() : def;
    }

    private static double optDouble(Map<String, Object> m, String k, double def) {
        Object v = m.get(k);
        return (v instanceof Number) ? ((Number) v).doubleValue() : def;
    }

    private static boolean optBool(Map<String, Object> m, String k, boolean def) {
        Object v = m.get(k);
        return (v instanceof Boolean) ? (Boolean) v : def;
    }

    /** Minimal zero-dependency recursive-descent JSON parser. */
    private static class JsonParser {
        private final String s;
        private int pos = 0;

        JsonParser(String s) { this.s = s; }

        Object parseValue() {
            skipWs();
            if (pos >= s.length()) return null;
            char c = s.charAt(pos);
            if (c == '{') return parseObject();
            if (c == '[') return parseArray();
            if (c == '"') return parseString();
            if (c == 't' || c == 'f') return parseBoolean();
            if (c == 'n') { pos += 4; return null; }
            return parseNumber();
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            pos++; // '{'
            skipWs();
            if (pos < s.length() && s.charAt(pos) == '}') { pos++; return map; }
            while (pos < s.length()) {
                skipWs();
                String key = parseString();
                skipWs();
                if (pos < s.length() && s.charAt(pos) == ':') pos++;
                Object val = parseValue();
                map.put(key, val);
                skipWs();
                if (pos < s.length() && s.charAt(pos) == ',') { pos++; continue; }
                if (pos < s.length() && s.charAt(pos) == '}') { pos++; break; }
                break;
            }
            return map;
        }

        private List<Object> parseArray() {
            List<Object> list = new ArrayList<>();
            pos++; // '['
            skipWs();
            if (pos < s.length() && s.charAt(pos) == ']') { pos++; return list; }
            while (pos < s.length()) {
                list.add(parseValue());
                skipWs();
                if (pos < s.length() && s.charAt(pos) == ',') { pos++; continue; }
                if (pos < s.length() && s.charAt(pos) == ']') { pos++; break; }
                break;
            }
            return list;
        }

        private String parseString() {
            pos++; // '"'
            StringBuilder sb = new StringBuilder();
            while (pos < s.length()) {
                char c = s.charAt(pos++);
                if (c == '"') break;
                if (c == '\\' && pos < s.length()) {
                    char esc = s.charAt(pos++);
                    if (esc == 'n') sb.append('\n');
                    else sb.append(esc);
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private Boolean parseBoolean() {
            if (s.startsWith("true", pos)) { pos += 4; return Boolean.TRUE; }
            pos += 5;
            return Boolean.FALSE;
        }

        private Double parseNumber() {
            int start = pos;
            while (pos < s.length()) {
                char c = s.charAt(pos);
                if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                    pos++;
                } else break;
            }
            return Double.parseDouble(s.substring(start, pos));
        }

        private void skipWs() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) pos++;
        }
    }
}
