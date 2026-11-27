#version 330 core
in vec3 vWorldPos;
in vec3 vWorldNormal;
in vec3 vLocalPos;
in vec3 vTint;
in float vSurfaceType;

out vec4 FragColor;

uniform vec3 uStarPos;
uniform vec3 uStarLightColor;
uniform vec3 uCameraPos;
uniform float uTime;

vec3 hash33(vec3 p) {
    p = vec3(
        dot(p, vec3(127.1, 311.7, 74.7)),
        dot(p, vec3(269.5, 183.3, 246.1)),
        dot(p, vec3(113.5, 271.9, 124.6))
    );
    return fract(sin(p) * 43758.5453123) * 2.0 - 1.0;
}

float facetSparkle(vec3 localPos, vec3 worldPos, vec3 N, vec3 H, float scale, float speed, float power) {
    vec3 cell = floor(localPos * scale);
    vec3 flake = hash33(cell);
    vec3 glitterN = normalize(N + flake * 0.72);
    float specAlign = pow(max(dot(glitterN, H), 0.0), power);
    float motionShimmer = 0.5 + 0.5 * sin(uTime * speed + dot(worldPos, vec3(0.35, 0.55, 0.45)) + flake.x * 19.0 + flake.y * 11.0);
    return specAlign * smoothstep(0.30, 0.95, motionShimmer);
}

vec3 crystalSparkle(vec3 localPos, vec3 worldPos, vec3 N, vec3 H, vec3 V) {
    vec3 cellA = floor(localPos * 10.5);
    vec3 cellB = floor((localPos + vec3(2.7, 5.1, 1.3)) * 22.0);
    vec3 flakeA = hash33(cellA);
    vec3 flakeB = hash33(cellB);

    vec3 nA = normalize(N + flakeA * 0.78);
    vec3 nB = normalize(N + flakeB * 0.85);
    float glintA = pow(max(dot(nA, H), 0.0), 54.0) * 0.65 + pow(max(dot(nA, V), 0.0), 40.0) * 0.35;
    float glintB = pow(max(dot(nB, H), 0.0), 72.0);

    float twinkleA = smoothstep(0.25, 0.92, 0.5 + 0.5 * sin(uTime * 2.6 + dot(worldPos, vec3(0.25, 0.45, 0.35)) + flakeA.y * 23.0));
    float twinkleB = smoothstep(0.35, 0.96, 0.5 + 0.5 * sin(uTime * 4.4 + dot(worldPos, vec3(0.45, 0.30, 0.55)) + flakeB.z * 31.0));

    vec3 prismA = 0.5 + 0.5 * cos(flakeA.x * 6.2831 + uTime * 1.1 + vec3(0.0, 2.094, 4.188));
    vec3 prismB = 0.5 + 0.5 * cos(flakeB.z * 6.2831 + uTime * 1.7 + vec3(0.5, 2.6, 4.7));
    vec3 crystalColA = mix(vec3(0.75, 0.96, 1.0), prismA, 0.38);
    vec3 crystalColB = mix(vec3(0.90, 0.98, 1.0), prismB, 0.45);

    return crystalColA * (glintA * twinkleA * 3.0) + crystalColB * (glintB * twinkleB * 4.2);
}

void main() {
    vec3 N = normalize(vWorldNormal);
    vec3 L = normalize(uStarPos - vWorldPos);
    vec3 V = normalize(uCameraPos - vWorldPos);
    vec3 H = normalize(L + V);

    float isMetal = smoothstep(1.5, 2.0, vSurfaceType);
    float isIcy = clamp(vSurfaceType, 0.0, 1.0) * (1.0 - isMetal);

    float diff = max(dot(N, L), 0.0);
    float diffWrap = max(dot(N, L) * 0.85 + 0.15, 0.0);

    // High contrast on metallic asteroids: darker non-reflective base vs bright star-lit reflections
    float diffuseScale = mix(1.0, 0.30, isMetal);
    float specPow = mix(mix(12.0, 64.0, isIcy), 64.0, isMetal);
    float specStr = mix(mix(0.08, 0.85, isIcy), 1.65, isMetal);
    float spec = pow(max(dot(N, H), 0.0), specPow) * specStr;

    float sparkleA = facetSparkle(vLocalPos, vWorldPos, N, H, 13.0, 3.2, 42.0);
    float sparkleB = facetSparkle(vLocalPos + vec3(4.3, 1.9, 7.1), vWorldPos, N, H, 24.0, 4.7, 42.0);
    float glitter = (sparkleA * 2.8 + sparkleB * 3.6) * isMetal;

    vec3 icyGlints = crystalSparkle(vLocalPos, vWorldPos, N, H, V) * isIcy * (0.35 + 0.65 * diffWrap);

    // Reflections and sparkles take on the parent star's color
    vec3 starTint = uStarLightColor * (0.65 + 0.45 * vTint);
    vec3 specColor = mix(mix(uStarLightColor, mix(uStarLightColor, vec3(0.82, 0.96, 1.0), 0.4), isIcy), starTint, isMetal);
    vec3 ambient = vTint * mix(mix(0.04, 0.14, isIcy), 0.03, isMetal);
    vec3 lit = vTint * uStarLightColor * (diff * diffuseScale)
             + specColor * (spec * diff + glitter * diffWrap)
             + icyGlints;

    float rimPow = pow(1.0 - max(dot(N, V), 0.0), mix(mix(3.0, 2.3, isIcy), 2.2, isMetal));
    vec3 icyRimColor = mix(vTint, vec3(0.76, 0.95, 1.0), 0.45);
    vec3 rimColor = mix(mix(vTint, icyRimColor, isIcy), uStarLightColor * (0.75 + 0.35 * vTint), isMetal);
    float rimStr = isIcy * 0.56 + isMetal * (0.48 * (0.35 + 0.65 * diffWrap));
    FragColor = vec4(ambient + lit + rimColor * (rimPow * rimStr), 1.0);
}
