#version 330 core
in vec2 vUV;
in vec3 vLocalPos;
in float vSeed;

out vec4 FragColor;

uniform float uTime;
uniform vec3 uHotColor;
uniform vec3 uMidColor;
uniform float uHdrIntensity;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

void main() {
    float along = vUV.x;
    float t = uTime * 0.18 + vSeed;

    // Distort the ribbon edges & plasma coordinates organically
    float warp = valueNoise(vec2(along * 7.0 - t * 0.9, vSeed * 0.37)) - 0.5;
    float warp2 = valueNoise(vec2(along * 15.0 + t * 1.4, vUV.y * 5.0)) - 0.5;

    float distortedY = vUV.y + warp * 0.35 + warp2 * 0.22;
    float across = abs(distortedY * 2.0 - 1.0);

    float widthFade = pow(max(0.0, 1.0 - across), 2.0);
    float endFade = smoothstep(0.0, 0.14, along) * smoothstep(1.0, 0.86, along);

    // Streaky distorted plasma flowing along the twisted arch
    float flow1 = valueNoise(vec2(along * 10.0 - t * 1.1, distortedY * 6.0 + warp * 2.5));
    float flow2 = valueNoise(vec2(along * 22.0 - t * 1.8, distortedY * 11.0 - warp2 * 3.0));
    float plasma = clamp(0.35 + 0.75 * (0.6 * flow1 + 0.4 * flow2), 0.0, 1.0);

    float pulse = 0.70 + 0.30 * sin(uTime * 0.35 + vSeed * 3.1);

    float intensity = widthFade * endFade * plasma * pulse;
    if (intensity < 0.01) discard;

    vec3 col = mix(uMidColor, uHotColor, smoothstep(0.32, 0.92, widthFade * plasma));
    col *= intensity * 1.65 * uHdrIntensity;

    FragColor = vec4(col, clamp(intensity * 0.92, 0.0, 1.0));
}
