#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;

out vec3 vLocalPos;
out vec3 vWorldPos;
out vec3 vWorldNormal;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;

void main() {
    vLocalPos = aPos;
    vec4 world = uModel * vec4(aPos, 1.0);
    vWorldPos = world.xyz;
    vWorldNormal = mat3(transpose(inverse(uModel))) * aNormal;
    gl_Position = uProjection * uView * world;
}
