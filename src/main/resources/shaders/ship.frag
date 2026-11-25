#version 330 core
in vec3 vWorldPos;
in vec3 vWorldNormal;
in float vMatZone;

out vec4 FragColor;

uniform vec3 uStarPos;
uniform vec3 uStarLightColor;
uniform vec3 uCameraPos;
uniform float uThrust;

void main() {
    vec3 N = normalize(vWorldNormal);
    vec3 L = normalize(uStarPos - vWorldPos);
    vec3 V = normalize(uCameraPos - vWorldPos);
    vec3 H = normalize(L + V);

    if (vMatZone > 0.75) {
        // Emissive ion thruster & wingtip accents (HDR > 1.0 for bloom)
        vec3 engineCol = vec3(0.25, 0.85, 2.2) * (0.55 + uThrust * 1.35);
        FragColor = vec4(engineCol, 1.0);
        return;
    }

    float diff = max(dot(N, L), 0.0);
    float spec = pow(max(dot(N, H), 0.0), 32.0);
    float fresnel = pow(1.0 - max(dot(N, V), 0.0), 3.0);

    vec3 baseCol = (vMatZone > 0.35) ? vec3(0.06, 0.08, 0.12) : vec3(0.32, 0.36, 0.42);
    vec3 ambient = baseCol * 0.14 + vec3(0.04, 0.06, 0.10) * fresnel;
    vec3 lit = baseCol * uStarLightColor * diff + uStarLightColor * spec * 0.55;

    FragColor = vec4(ambient + lit, 1.0);
}
