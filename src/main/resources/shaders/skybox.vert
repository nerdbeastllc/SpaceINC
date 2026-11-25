#version 330 core
layout (location = 0) in vec3 aPos;

out vec3 vDir;

uniform mat4 uInvVP;

void main() {
    vec4 clip = vec4(aPos.xy, 1.0, 1.0);
    vec4 world = uInvVP * clip;
    vDir = world.xyz / world.w;
    gl_Position = vec4(aPos.xy, 0.9999, 1.0);
}
