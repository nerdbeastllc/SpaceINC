#version 330 core
in vec3 vWorldPos;
in vec3 vWorldNormal;
in vec3 vTint;
in float vIsIcy;

out vec4 FragColor;

uniform vec3 uStarPos;
uniform vec3 uStarLightColor;
uniform vec3 uCameraPos;

void main() {
    vec3 N = normalize(vWorldNormal);
    vec3 L = normalize(uStarPos - vWorldPos);
    vec3 V = normalize(uCameraPos - vWorldPos);
    vec3 H = normalize(L + V);

    float diff = max(dot(N, L), 0.0);
    float specPow = mix(12.0, 48.0, vIsIcy);
    float specStr = mix(0.08, 0.48, vIsIcy);
    float spec = pow(max(dot(N, H), 0.0), specPow) * specStr;

    vec3 ambient = vTint * mix(0.04, 0.09, vIsIcy);
    vec3 lit = vTint * uStarLightColor * diff + uStarLightColor * spec * diff;

    // Subtle icy rim glint for Oort Cloud comets
    float rim = pow(1.0 - max(dot(N, V), 0.0), 3.0) * vIsIcy * 0.28;
    FragColor = vec4(ambient + lit + vTint * rim, 1.0);
}
