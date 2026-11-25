#version 330 core
layout (location = 0) in vec3 aPos;
layout (location = 2) in vec2 aUV;

out vec2 vUV;
out vec3 vWorldDir;
out float vStarRatio;

uniform mat4 uView;
uniform mat4 uProjection;
uniform vec3 uStarPos;
uniform vec3 uCameraPos;
uniform float uStarRadius;
uniform float uBillboardRadius;

void main() {
    vUV = aUV * 2.0 - 1.0; // [-1, 1]

    vec3 toCam = uCameraPos - uStarPos;
    float d = max(length(toCam), uStarRadius * 1.05);
    vec3 look = toCam / d;

    vec3 worldUp = abs(look.y) > 0.99 ? vec3(0.0, 0.0, 1.0) : vec3(0.0, 1.0, 0.0);
    vec3 right = normalize(cross(worldUp, look));
    vec3 up    = cross(look, right);

    // Exact 3D tangent silhouette plane of the sphere so corona never separates at FOV edges
    float ratio = uStarRadius / d;
    vec3 silCenter = uStarPos + look * (uStarRadius * ratio);
    float silRadius = uStarRadius * sqrt(max(0.01, 1.0 - ratio * ratio));
    vStarRatio = silRadius / uBillboardRadius;

    vec3 offset = right * (aPos.x * uBillboardRadius)
                + up    * (aPos.y * uBillboardRadius);
    vWorldDir = (silCenter + offset) - uStarPos;

    gl_Position = uProjection * uView * vec4(silCenter + offset, 1.0);
}
