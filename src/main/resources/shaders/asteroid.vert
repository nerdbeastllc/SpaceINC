#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;

// Per-instance attributes (updated once at startup; orbit animated on GPU!)
layout (location = 3) in vec4 aInstOrbit;     // x=orbitRadius, y=startAngle, z=orbitSpeed, w=yOffset
layout (location = 4) in vec4 aInstScaleSpin; // xyz=non-uniform scale, w=spinPhase
layout (location = 5) in vec4 aInstColor;     // rgb=tint, w=surfaceType (0=stony, 1=icy, 2=metallic)

out vec3 vWorldPos;
out vec3 vWorldNormal;
out vec3 vLocalPos;
out vec3 vTint;
out float vSurfaceType;

uniform mat4 uView;
uniform mat4 uProjection;
uniform float uTime;

mat3 rotY(float a) {
    float c = cos(a), s = sin(a);
    return mat3(c, 0.0, -s,  0.0, 1.0, 0.0,  s, 0.0, c);
}

mat3 rotX(float a) {
    float c = cos(a), s = sin(a);
    return mat3(1.0, 0.0, 0.0,  0.0, c, s,  0.0, -s, c);
}

void main() {
    float angle = aInstOrbit.y + uTime * aInstOrbit.z;
    vec3 center = vec3(
        cos(angle) * aInstOrbit.x,
        aInstOrbit.w,
        sin(angle) * aInstOrbit.x
    );

    float spin = aInstScaleSpin.w + uTime * (0.25 + fract(aInstScaleSpin.w) * 0.45);
    mat3 tumble = rotY(spin) * rotX(spin * 0.63);

    vec3 localScaled = aPos * aInstScaleSpin.xyz;
    vec3 worldPos = center + tumble * localScaled;
    vec3 worldNorm = normalize(tumble * (aNormal / aInstScaleSpin.xyz));

    vWorldPos = worldPos;
    vWorldNormal = worldNorm;
    vLocalPos = aPos;
    vTint = aInstColor.rgb;
    vSurfaceType = aInstColor.w;

    gl_Position = uProjection * uView * vec4(worldPos, 1.0);
}
