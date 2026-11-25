#version 330 core
in vec3 vDir;
out vec4 FragColor;

uniform vec3 uCameraPos;

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

float fbm(vec3 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 4; i++) {
        v += a * valueNoise3D(p);
        p *= 2.07;
        a *= 0.5;
    }
    return v;
}

vec3 starLayer(vec3 dir, float scale, float density, float brightness) {
    vec3 p = dir * scale;
    vec3 id = floor(p);
    vec3 f = fract(p) - 0.5;
    float h = hash31(id);
    if (h < density) return vec3(0.0);

    vec3 offset = vec3(hash31(id + 1.1), hash31(id + 2.3), hash31(id + 3.7)) - 0.5;
    float d = length(f - offset * 0.55);
    float star = smoothstep(0.11, 0.0, d);
    star = pow(star, 2.2) * brightness * (0.5 + 0.8 * fract(h * 91.7));

    float temp = fract(h * 37.3);
    vec3 col = mix(vec3(1.0, 0.72, 0.50), vec3(0.72, 0.86, 1.0), temp);
    return col * star;
}

void main() {
    vec3 dir = normalize(vDir - uCameraPos);

    // Deep cosmic background
    vec3 col = vec3(0.006, 0.009, 0.018);

    // Subtle galactic band & JWST-style cosmic dust nebula
    float band = exp(-abs(dir.y + dir.x * 0.35) * 4.5);
    float n1 = fbm(dir * 3.2 + vec3(1.4, -0.8, 2.1));
    float n2 = fbm(dir * 6.8 + n1 * 1.4);
    col += vec3(0.035, 0.045, 0.095) * band * smoothstep(0.32, 0.85, n1);
    col += vec3(0.085, 0.032, 0.025) * band * smoothstep(0.45, 0.90, n2) * 0.6;

    // Multi-scale crisp starfield
    col += starLayer(dir, 140.0, 0.965, 1.35);
    col += starLayer(dir, 260.0, 0.975, 0.75);
    col += starLayer(dir, 420.0, 0.985, 0.45);

    FragColor = vec4(col, 1.0);
}
