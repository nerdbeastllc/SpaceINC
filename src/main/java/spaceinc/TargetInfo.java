package spaceinc;

import org.joml.Vector3f;

import java.util.List;
import java.util.function.Supplier;

public class TargetInfo {
    public final String name;
    public final String category;
    public final List<MineralRegistry.OreDeposit> deposits;
    public final String faction;
    public final float radius;
    public final String jumpTargetSystemId;
    public final StarSystem.MarketStation marketStation;
    private final Supplier<Vector3f> positionSupplier;

    public TargetInfo(String name, String category, List<MineralRegistry.OreDeposit> deposits,
                      String faction, float radius, Supplier<Vector3f> positionSupplier) {
        this(name, category, deposits, faction, radius, null, null, positionSupplier);
    }

    public TargetInfo(String name, String category, List<MineralRegistry.OreDeposit> deposits,
                      String faction, float radius, String jumpTargetSystemId,
                      Supplier<Vector3f> positionSupplier) {
        this(name, category, deposits, faction, radius, jumpTargetSystemId, null, positionSupplier);
    }

    public TargetInfo(String name, String category, List<MineralRegistry.OreDeposit> deposits,
                      String faction, float radius, String jumpTargetSystemId,
                      StarSystem.MarketStation marketStation, Supplier<Vector3f> positionSupplier) {
        this.name = name;
        this.category = category;
        this.deposits = deposits;
        this.faction = faction;
        this.radius = radius;
        this.jumpTargetSystemId = jumpTargetSystemId;
        this.marketStation = marketStation;
        this.positionSupplier = positionSupplier;
    }

    public Vector3f getPosition() {
        return positionSupplier.get();
    }
}
