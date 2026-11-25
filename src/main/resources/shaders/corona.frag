#version 330 core
in vec2 vUV;
in vec3 vWorldDir;
in float vStarRatio;
out vec4 FragColor;

uniform float uTime;
uniform vec3 uCoronaColor;
uniform vec3 uHotColor;
uniform float uHdrIntensity;

float hash31(vec3 p) {
    p = fract(p * vec3(443.8975, 397.2973, 491.1871));
    p += dot(p, p.yxz + 19.19);
    return fract((p.x + p.y) * p.z);
}

float valueNoise3D(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float n000 = hash31(i + vec3(0,0,0));
    float n100 = hash31(i + vec3(1,0,0));
    float n010 = hash31(i + vec3(0,1,0));
    float n110 = hash31(i + vec3(1,1,0));
    float n001 = hash31(i + vec3(0,0,1));
    float n101 = hash31(i + vec3(1,0,1));
    float n011 = hash31(i + vec3(0,1,1));
    float n111 = hash31(i + vec3(1,1,1));
    return mix(
        mix(mix(n000, n100, f.x), mix(n010, n110, f.x), f.y),
        mix(mix(n001, n101, f.x), mix(n011, n111, f.x), f.y),
        f.z
    );
}

void main() {
    float dist = length(vUV);
    if (dist > 1.0) discard;

    // Normalized radial distance: 1.0 = star surface edge
    float r = dist / vStarRatio;
    if (r < 0.99) discard;

    // Anchor coronal rays & prominences in 3D world space so they don't follow the camera/ship
    vec3 dir = normalize(vWorldDir);
    float t = uTime * 0.22;

    float ray1 = valueNoise3D(dir * 5.5 + vec3(0.0, t * 0.15, 0.0));
    float ray2 = valueNoise3D(dir * 13.0 - vec3(t * 0.12, 0.0, t * 0.18));
    float rays = pow(0.55 * ray1 + 0.45 * ray2, 2.2);

    // Solar prominences licking off the limb (r in 1.0 .. 1.22)
    float promNoise = valueNoise3D(dir * 13.0 + vec3(r * 4.2 - t * 0.7));
    float promMask = smoothstep(0.58, 0.88, promNoise) * exp(-max(0.0, r - 1.0) * 14.0);

    // Volumetric atmospheric / coronal falloff (kept subtle so it frames the rim without washing the center)
    float dSurf = max(0.0, r - 1.0);
    float innerGlow = exp(-dSurf * 12.0) * 0.42;
    float outerHalo = exp(-dSurf * 2.6) * 0.18;
    float rayGlow   = exp(-dSurf * 1.9) * rays * 0.28;

    float edgeFade = smoothstep(1.0, 0.72, dist);

    vec3 col = uCoronaColor * (outerHalo + rayGlow)
             + mix(uCoronaColor, uHotColor, 0.70) * innerGlow
             + uHotColor * promMask * 1.25;

    col *= edgeFade * uHdrIntensity;
    float alpha = clamp((innerGlow + outerHalo + rayGlow + promMask) * edgeFade, 0.0, 1.0);

    FragColor = vec4(col, alpha);
}
