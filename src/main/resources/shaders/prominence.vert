#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal; // x = loopSeed, y/z = ribbon tangent hints
layout (location = 2) in vec2 aUV;

out vec2 vUV;
out vec3 vLocalPos;
out float vSeed;

uniform mat4 uModel;
uniform mat4 uView;
uniform mat4 uProjection;
uniform float uTime;

void main() {
    vUV = aUV;
    vSeed = aNormal.x;

    float along = aUV.x;
    float side = aUV.y * 2.0 - 1.0;

    // Pin footpoints to the star surface; allow mid-arch to writhe and twist slowly in 3D
    float env = sin(along * 3.14159265);
    float t = uTime * 0.25 + vSeed;

    vec3 radial = normalize(aPos);
    vec3 twistAxis1 = normalize(cross(radial, vec3(0.0, 1.0, 0.1)));
    vec3 twistAxis2 = cross(radial, twistAxis1);

    float w1 = sin(along * 9.0 - t * 1.3) * 0.045 + cos(along * 15.0 + t * 0.8) * 0.022;
    float w2 = cos(along * 7.5 + t * 1.1) * 0.042 + sin(along * 13.0 - t * 1.5) * 0.020;

    // Helical twist of the ribbon edges along the arch
    float twistAngle = along * 6.28318 + t * 0.7;
    vec3 helicalOffset = (twistAxis1 * cos(twistAngle) + twistAxis2 * sin(twistAngle)) * side * 0.022 * env;

    vec3 displaced = aPos + (twistAxis1 * w1 + twistAxis2 * w2) * env + helicalOffset;
    vLocalPos = displaced;

    gl_Position = uProjection * uView * uModel * vec4(displaced, 1.0);
}
