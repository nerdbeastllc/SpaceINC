#version 330 core
in vec3 vLocalPos;
in vec3 vWorldPos;
in vec3 vWorldNormal;

out vec4 FragColor;

uniform float uTime;
uniform vec3 uStarPos;
uniform vec3 uStarLightColor;
uniform vec3 uCameraPos;
uniform int uPlanetType;
// 0 = Terran, 1 = Gas Giant, 2 = Cratered/Rocky, 3 = Venusian,
// 4 = Martian, 5 = Ice Giant, 6 = Volcanic (Io), 7 = Icy/Hazy/Dwarf
uniform vec3 uColorA;
uniform vec3 uColorB;
uniform vec3 uColorC;

float hash31(vec3 p) {
    p = fract(p * vec3(443.8975, 397.2973, 491.1871));
    p += dot(p, p.yxz + 19.19);
    return fract((p.x + p.y) * p.z);
}

float valueNoise3D(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float n000 = hash31(i + vec3(0,0,0));
    float n100 = hash31(i + vec3(1,0,0));
    float n010 = hash31(i + vec3(0,1,0));
    float n110 = hash31(i + vec3(1,1,0));
    float n001 = hash31(i + vec3(0,0,1));
    float n101 = hash31(i + vec3(1,0,1));
    float n011 = hash31(i + vec3(0,1,1));
    float n111 = hash31(i + vec3(1,1,1));
    return mix(
        mix(mix(n000, n100, f.x), mix(n010, n110, f.x), f.y),
        mix(mix(n001, n101, f.x), mix(n011, n111, f.x), f.y),
        f.z
    );
}

float fbm(vec3 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += a * valueNoise3D(p);
        p *= 2.08;
        a *= 0.5;
    }
    return v;
}

void main() {
    vec3 p = normalize(vLocalPos);
    vec3 N = normalize(vWorldNormal);
    vec3 L = normalize(uStarPos - vWorldPos);
    vec3 V = normalize(uCameraPos - vWorldPos);
    vec3 H = normalize(L + V);

    float ndotl = dot(N, L);
    float diffuse = smoothstep(-0.12, 0.95, ndotl);
    float ndotv = clamp(dot(N, V), 0.0, 1.0);

    vec3 surfaceColor = vec3(0.0);
    float spec = 0.0;
    vec3 emissive = vec3(0.0);
    vec3 atmoColor = vec3(0.0);
    float atmoStrength = 0.0;

    if (uPlanetType == 0) {
        // TERRAN (EARTH)
        float cont = fbm(p * 3.1 + vec3(1.7, 0.4, -2.3));
        float detail = fbm(p * 11.0);
        float height = cont + (detail - 0.5) * 0.22;
        float seaLevel = 0.51;

        if (height < seaLevel) {
            float depth = clamp((seaLevel - height) * 5.5, 0.0, 1.0);
            surfaceColor = mix(vec3(0.05, 0.34, 0.56), vec3(0.015, 0.08, 0.24), depth);
            spec = pow(max(dot(N, H), 0.0), 48.0) * 0.65;
        } else {
            float elev = clamp((height - seaLevel) * 4.0, 0.0, 1.0);
            vec3 coast = vec3(0.16, 0.44, 0.18);
            vec3 highland = vec3(0.38, 0.34, 0.22);
            vec3 snow = vec3(0.88, 0.92, 0.96);
            surfaceColor = mix(coast, highland, smoothstep(0.15, 0.65, elev));
            surfaceColor = mix(surfaceColor, snow, smoothstep(0.68, 0.95, elev + abs(p.y) * 0.45));

            float nightMask = smoothstep(0.04, -0.22, ndotl);
            float cityNoise = valueNoise3D(p * 46.0);
            float cityCluster = smoothstep(0.73, 0.88, cityNoise) * (1.0 - smoothstep(0.25, 0.65, elev));
            emissive += vec3(1.15, 0.78, 0.34) * cityCluster * nightMask * 0.9;
        }

        float polar = smoothstep(0.78, 0.92, abs(p.y) + (detail - 0.5) * 0.12);
        surfaceColor = mix(surfaceColor, vec3(0.88, 0.93, 0.98), polar);

        float clouds = fbm(p * 4.8 + vec3(uTime * 0.02, 0.0, -uTime * 0.012));
        float cloudAlpha = smoothstep(0.50, 0.78, clouds) * 0.82;
        surfaceColor = mix(surfaceColor, vec3(0.95, 0.97, 1.0), cloudAlpha);
        spec *= (1.0 - cloudAlpha);

        atmoColor = vec3(0.24, 0.58, 1.00);
        atmoStrength = pow(1.0 - ndotv, 3.2) * (0.25 + 0.85 * smoothstep(-0.25, 0.45, ndotl));
    } else if (uPlanetType == 1) {
        // GAS GIANT (JUPITER / SATURN)
        float warp = fbm(p * 4.5 + vec3(uTime * 0.01, 0.0, 0.0));
        float bands = sin((p.y + (warp - 0.5) * 0.22) * 26.0) * 0.5 + 0.5;
        float fineBands = fbm(vec3(p.x * 3.0, p.y * 16.0, p.z * 3.0) + warp * 0.8);

        surfaceColor = mix(uColorB, uColorA, smoothstep(0.25, 0.78, bands));
        surfaceColor = mix(surfaceColor, uColorC, smoothstep(0.55, 0.88, fineBands) * 0.45);

        // Optional Great Storm (when uColorC has warm red tint, e.g. Jupiter)
        vec3 stormCenter = normalize(vec3(0.7, -0.22, 0.65));
        float stormDist = length(p - stormCenter);
        float storm = exp(-stormDist * stormDist * 45.0);
        surfaceColor = mix(surfaceColor, uColorC * 1.35, storm * 0.65);

        atmoColor = uColorA;
        atmoStrength = pow(1.0 - ndotv, 3.6) * (0.15 + 0.75 * smoothstep(-0.2, 0.5, ndotl));
    } else if (uPlanetType == 3) {
        // VENUSIAN (THICK SULFURIC CLOUDS)
        float swirl = fbm(p * 3.2 + vec3(uTime * 0.015, 0.0, 0.0));
        float streaks = fbm(vec3(p.x * 2.5, p.y * 8.5, p.z * 2.5) + swirl * 1.4);
        surfaceColor = mix(uColorB, uColorA, smoothstep(0.25, 0.80, streaks));
        atmoColor = uColorC;
        atmoStrength = pow(1.0 - ndotv, 2.8) * (0.35 + 0.75 * smoothstep(-0.2, 0.5, ndotl));
    } else if (uPlanetType == 4) {
        // MARTIAN (DESERT + POLAR ICE)
        float terrain = fbm(p * 4.2 + vec3(0.5, -1.2, 0.8));
        float detail = fbm(p * 14.0);
        surfaceColor = mix(uColorB, uColorA, smoothstep(0.30, 0.72, terrain + (detail - 0.5) * 0.25));
        float polar = smoothstep(0.82, 0.94, abs(p.y) + (detail - 0.5) * 0.10);
        surfaceColor = mix(surfaceColor, vec3(0.92, 0.94, 0.96), polar);
        atmoColor = uColorC;
        atmoStrength = pow(1.0 - ndotv, 3.8) * 0.35 * smoothstep(-0.15, 0.5, ndotl);
    } else if (uPlanetType == 5) {
        // ICE GIANT (URANUS / NEPTUNE)
        float warp = fbm(p * 3.8 + vec3(uTime * 0.01, 0.0, 0.0));
        float bands = sin((p.y + (warp - 0.5) * 0.14) * 18.0) * 0.5 + 0.5;
        float wisps = smoothstep(0.68, 0.88, fbm(vec3(p.x * 4.0, p.y * 14.0, p.z * 4.0) + warp));
        surfaceColor = mix(uColorB, uColorA, smoothstep(0.2, 0.8, bands));
        surfaceColor = mix(surfaceColor, uColorC, wisps * 0.55);
        atmoColor = uColorA;
        atmoStrength = pow(1.0 - ndotv, 3.0) * (0.25 + 0.85 * smoothstep(-0.2, 0.5, ndotl));
    } else if (uPlanetType == 6) {
        // VOLCANIC (IO)
        float crust = fbm(p * 5.0);
        float fissures = 1.0 - abs(valueNoise3D(p * 11.0 + crust * 1.5) - 0.5) * 2.0;
        float lava = smoothstep(0.84, 0.97, fissures);
        surfaceColor = mix(uColorB, uColorA, smoothstep(0.3, 0.75, crust));
        emissive += uColorC * lava * 1.45;
    } else if (uPlanetType == 7) {
        // ICY / HAZY / DWARF (EUROPA, ENCELADUS, TITAN, PLUTO)
        float baseN = fbm(p * 4.8);
        float cracks = 1.0 - abs(valueNoise3D(p * 14.0 + baseN * 1.2) - 0.5) * 2.0;
        surfaceColor = mix(uColorB, uColorA, smoothstep(0.28, 0.78, baseN));
        surfaceColor = mix(surfaceColor, uColorC, smoothstep(0.82, 0.96, cracks) * 0.55);
        atmoColor = uColorC;
        atmoStrength = pow(1.0 - ndotv, 3.4) * 0.40 * smoothstep(-0.15, 0.5, ndotl);
    } else {
        // CRATERED / ROCKY (MERCURY, LUNA, CALLISTO, MIMAS, CHARON)
        float regolith = fbm(p * 6.5);
        float craters = abs(valueNoise3D(p * 18.0) - 0.5) * 2.0;
        surfaceColor = mix(uColorB, uColorA, regolith * 0.7 + craters * 0.3);
        atmoColor = uColorA;
        atmoStrength = pow(1.0 - ndotv, 4.5) * 0.10 * smoothstep(-0.1, 0.5, ndotl);
    }

    vec3 ambient = surfaceColor * 0.035;
    vec3 lit = surfaceColor * uStarLightColor * diffuse + uStarLightColor * spec * diffuse;
    vec3 finalCol = ambient + lit + emissive + atmoColor * uStarLightColor * atmoStrength * 1.15;

    FragColor = vec4(finalCol, 1.0);
}
