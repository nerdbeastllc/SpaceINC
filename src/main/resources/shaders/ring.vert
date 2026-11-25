#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 2) in vec2 aUV;

out vec3 vLocalPos;
out vec3 vWorldPos;
out vec2 vUV;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

void main() {
    vLocalPos = aPos;
    vUV = aUV;
    vec4 world = uModel * vec4(aPos, 1.0);
    vWorldPos = world.xyz;
    gl_Position = uProjection * uView * world;
}
