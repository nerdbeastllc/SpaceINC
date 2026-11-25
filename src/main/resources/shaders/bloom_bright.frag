#version 330 core
in vec2 vUV;
out vec4 FragColor;

uniform sampler2D uScene;
uniform float uThreshold;

void main() {
    vec3 col = texture(uScene, vUV).rgb;
    float lum = dot(col, vec3(0.2126, 0.7152, 0.0722));
    float soft = smoothstep(uThreshold * 0.75, uThreshold * 1.35, lum);
    FragColor = vec4(col * soft, 1.0);
}
