#version 330 core
in vec3 vLocalPos;
in vec3 vWorldPos;
in vec2 vUV;

out vec4 FragColor;

uniform vec3 uStarPos;
uniform vec3 uStarLightColor;
uniform vec3 uPlanetPos;
uniform float uPlanetRadius;

void main() {
    float r = vUV.y; // 0 = inner, 1 = outer

    // Multi-band ring structure with Cassini-style divisions
    float b1 = sin(r * 42.0) * 0.5 + 0.5;
    float b2 = sin(r * 115.0 + 1.3) * 0.5 + 0.5;
    float gap1 = 1.0 - smoothstep(0.0, 0.045, abs(r - 0.62));
    float gap2 = 1.0 - smoothstep(0.0, 0.025, abs(r - 0.34));
    float edgeFade = smoothstep(0.0, 0.08, r) * smoothstep(1.0, 0.90, r);

    float density = (0.35 + 0.45 * b1 + 0.25 * b2) * (1.0 - gap1 * 0.85) * (1.0 - gap2 * 0.7) * edgeFade;
    if (density < 0.03) discard;

    // Analytic planet shadow cast onto the ring
    vec3 toStar = normalize(uStarPos - vWorldPos);
    vec3 oc = vWorldPos - uPlanetPos;
    float proj = dot(oc, toStar);
    float shadow = 1.0;
    if (proj < 0.0) {
        vec3 closest = oc - toStar * proj;
        float dist = length(closest);
        shadow = smoothstep(uPlanetRadius * 0.95, uPlanetRadius * 1.08, dist);
    }

    vec3 ringCol = mix(vec3(0.55, 0.48, 0.40), vec3(0.86, 0.79, 0.68), b1);
    vec3 lit = ringCol * uStarLightColor * (0.08 + 0.92 * shadow);

    FragColor = vec4(lit, clamp(density * 0.78, 0.0, 0.9));
}
