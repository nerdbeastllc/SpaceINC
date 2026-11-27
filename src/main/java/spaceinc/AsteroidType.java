package spaceinc;

/**
 * Extensible classification for asteroid belt and Oort cloud bodies.
 * Each type defines its target UI metadata, shader surface ID, base color tint,
 * and mineral pool so new asteroid classes can be added in one place.
 */
public enum AsteroidType {
    STONY(
        "BELT ASTEROID",
        "STONY ASTEROID",
        0.0f,
        0.46f, 0.36f,
        1.26f, 0.88f, 0.58f
    ),
    ICY_COMET(
        "OORT COMET",
        "ICY PLANETESIMAL",
        1.0f,
        0.68f, 0.36f,
        0.48f, 0.92f, 1.28f
    ),
    METALLIC(
        "METALLIC ASTEROID",
        "METALLIC ASTEROID",
        2.0f,
        0.58f, 0.34f,
        0.96f, 1.00f, 1.10f
    );

    public final String namePrefix;
    public final String category;
    public final float shaderSurfaceType;
    public final float baseTone;
    public final float toneSpan;
    public final float mulR;
    public final float mulG;
    public final float mulB;

    AsteroidType(String namePrefix, String category, float shaderSurfaceType,
                 float baseTone, float toneSpan, float mulR, float mulG, float mulB) {
        this.namePrefix = namePrefix;
        this.category = category;
        this.shaderSurfaceType = shaderSurfaceType;
        this.baseTone = baseTone;
        this.toneSpan = toneSpan;
        this.mulR = mulR;
        this.mulG = mulG;
        this.mulB = mulB;
    }
}
