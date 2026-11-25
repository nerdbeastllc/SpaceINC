#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in float aAlpha;

out float vAlpha;

uniform mat4 uView;
uniform mat4 uProjection;
uniform int uUseAlphaAttr;

void main() {
    vAlpha = (uUseAlphaAttr == 1) ? aAlpha : 1.0;
    gl_Position = uProjection * uView * vec4(aPos, 1.0);
}
