#version 330 core
in vec3 vLocalPos;
in vec3 vWorldPos;
in vec3 vWorldNormal;

out vec4 FragColor;

uniform float uTime;
uniform vec3 uCameraPos;
uniform vec3 uHotColor;
uniform vec3 uMidColor;
uniform vec3 uCoolColor;
uniform float uGranulationScale;
uniform float uHdrIntensity;

// 3D Simplex Noise (Ashima Arts)
vec3 mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 mod289(vec4 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec4 permute(vec4 x) { return mod289(((x * 34.0) + 1.0) * x); }
vec4 taylorInvSqrt(vec4 r) { return 1.79284291400159 - 0.85373472095314 * r; }

float snoise(vec3 v) {
    const vec2 C = vec2(1.0 / 6.0, 1.0 / 3.0);
    const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);

    vec3 i  = floor(v + dot(v, C.yyy));
    vec3 x0 = v - i + dot(i, C.xxx);

    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min(g.xyz, l.zxy);
    vec3 i2 = max(g.xyz, l.zxy);

    vec3 x1 = x0 - i1 + C.xxx;
    vec3 x2 = x0 - i2 + C.yyy;
    vec3 x3 = x0 - D.yyy;

    i = mod289(i);
    vec4 p = permute(permute(permute(
              i.z + vec4(0.0, i1.z, i2.z, 1.0))
            + i.y + vec4(0.0, i1.y, i2.y, 1.0))
            + i.x + vec4(0.0, i1.x, i2.x, 1.0));

    float n_ = 0.142857142857;
    vec3 ns = n_ * D.wyz - D.xzx;

    vec4 j = p - 49.0 * floor(p * ns.z * ns.z);
    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_);

    vec4 x = x_ * ns.x + ns.yyyy;
    vec4 y = y_ * ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);

    vec4 b0 = vec4(x.xy, y.xy);
    vec4 b1 = vec4(x.zw, y.zw);

    vec4 s0 = floor(b0) * 2.0 + 1.0;
    vec4 s1 = floor(b1) * 2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));

    vec4 a0 = b0.xzyw + s0.xzyw * sh.xxyy;
    vec4 a1 = b1.xzyw + s1.xzyw * sh.zzww;

    vec3 p0 = vec3(a0.xy, h.x);
    vec3 p1 = vec3(a0.zw, h.y);
    vec3 p2 = vec3(a1.xy, h.z);
    vec3 p3 = vec3(a1.zw, h.w);

    vec4 norm = taylorInvSqrt(vec4(dot(p0,p0), dot(p1,p1), dot(p2,p2), dot(p3,p3)));
    p0 *= norm.x; p1 *= norm.y; p2 *= norm.z; p3 *= norm.w;

    vec4 m = max(0.6 - vec4(dot(x0,x0), dot(x1,x1), dot(x2,x2), dot(x3,x3)), 0.0);
    m = m * m;
    return 42.0 * dot(m * m, vec4(dot(p0,x0), dot(p1,x1), dot(p2,x2), dot(p3,x3)));
}

// Turbulent abs-FBM for vibrant swirling plasma & boiling gases
float turbulence(vec3 p) {
    float sum = 0.0;
    float amp = 0.55;
    float freq = 1.0;
    for (int i = 0; i < 5; i++) {
        sum += amp * abs(snoise(p * freq));
        freq *= 2.05;
        amp *= 0.5;
    }
    return sum;
}

void main() {
    vec3 p = normalize(vLocalPos);
    vec3 N = normalize(vWorldNormal);
    vec3 V = normalize(uCameraPos - vWorldPos);

    float t = uTime * 0.14;

    // 1. Vibrant swirling boiling plasma (anchored in 3D object space)
    vec3 warp = vec3(
        snoise(p * 3.0 + vec3(0.0, t, 0.0)),
        snoise(p * 3.0 + vec3(5.2, -t * 0.8, 1.3)),
        snoise(p * 3.0 + vec3(-2.4, 3.1, t * 0.7))
    );

    float gas1 = turbulence(p * (uGranulationScale * 0.65) + warp * 0.85 + vec3(t * 0.3));
    float gas2 = turbulence(p * (uGranulationScale * 1.45) - warp * 0.65 - vec3(t * 0.45));
    float plasma = clamp(0.6 * gas1 + 0.4 * gas2, 0.0, 1.0);

    // Rich, saturated interior color (deep cool core -> vibrant plasma streams)
    vec3 innerCol = mix(uCoolColor, uMidColor, smoothstep(0.18, 0.85, plasma));

    // 2. Fresnel: 0 at center looking straight in, 1 at the silhouette edge
    float fresnel = 1.0 - clamp(dot(N, V), 0.0, 1.0);

    // Keep the center deep & rich (0.28) so plasma colors stay vivid and dark, ramping toward the edge
    float coreDarkening = mix(0.28, 1.0, pow(fresnel, 1.6));
    vec3 col = innerCol * coreDarkening * uHdrIntensity;

    // 3. Very bright glowing edge (Fresnel rim)
    float edgeGlow = pow(fresnel, 4.0);
    float sharpRim = pow(fresnel, 8.5);
    col += mix(uMidColor, uHotColor, 0.7) * edgeGlow * 1.35;
    col += uHotColor * sharpRim * 2.40;

    FragColor = vec4(col, 1.0);
}
