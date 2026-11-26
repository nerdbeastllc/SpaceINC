# SpaceINC — Game Design Document (Living Prototype)

> **Vision**: A 3rd-person 3D isometric space game blending **Open-World exploration**, **4X empire building**, and **EVE Online** logistics/economy—built in **Java 25 + LWJGL 3 (OpenGL 3.3 Core)** with procedural GPU shaders.

---

## 1. Core Pillars

1. **Tactile 3D Isometric Space**
   - Smooth 3D isometric/orbiting camera (`Right-Drag` / `Q`/`E` orbit, `Scroll` zoom from ship-scale `4.5` to system-scale `2800`).
   - Direct piloting (`WASD` + `Shift` afterburner) and EVE-style 3D object targeting (`Left-Click` on stars, planets, moons, asteroids, comets, and jump gates).
2. **Stellar & Planetary Fidelity**
   - **12 Milky Way Star Classes** (`O3V`, `B8Ia`, `A1V`, `F5V`, `G2V`, `K1V`, `M5V`, `M1Ia`, `L4`, `Wolf-Rayet`, `White Dwarf`, `Magnetar`) rendered with 3D cellular convection, swirling plasma, 3D animated prominence loops, and perspective-locked coronas.
   - **Multi-Biome Planets & Moons** (Terran with night city lights & Rayleigh atmosphere, Gas Giants with ring shadows, Ice Giants with axial tilts, Volcanic Io-style worlds, cratered moons, Asteroid Belts, and Oort Cloud comets).
3. **Ore → Element Refining Economy**
   - **22 Minable Ores / Deposits** refine into **45 representative elements** across every periodic table group (structural metals, conductors, precious metals, semiconductors, halogens, lanthanides, actinides, noble gases).
   - Every deposit has a **Purity / Grade (%)** determining refining yield and economic viability.
4. **Core Federation vs. Frontier Loop**
   - **Core Federated Systems** (e.g., *Sol*, *Alpha Centauri*, *Sirius*): Safe, gate-linked starter systems where **all elements exist**, but at **weak, barely economic purity (`12%–26%`)**.
   - **Frontier Systems** (e.g., *Rigel Expanse*, *Betelgeuse Reach*): Unclaimed/contested systems with **high-purity deposits (`65%–95%`)** that reward scouting, claim-staking, and logistics.
5. **Hybrid Interstellar Travel**
   - **Early Game**: Federation **Jump Gates** and natural **Wormholes** connect nearby systems.
   - **Mid Game**: **FTL Jump Drives** (fueled by refined *Helium-3* / *Xenon*) allow freeform jumps on the Galaxy Map (`M`).
   - **Late Game**: Player/Faction-built **Jump Gates** link rich frontier colonies back to Federation markets.

---

## 2. Roadmap (Next Iterations)

- [x] **Prototype Scene**: 3D isometric engine, HDR bloom, 12 star classes, full Sol system + moons + Asteroid Belt + Oort Cloud.
- [x] **Gradle + `jlink` / `jpackage`**: Java 25 toolchain and self-contained `.app` bundle.
- [x] **3D Targeting & Ore Refining Model**: Left-click ray picking + HUD Target Info Card with 22 ores → 45 elements.
- [x] **Multi-System Travel & Purity Tiers**: Ore purity/grade, Jump Gates, and Galaxy Map (`M`).
- [x] **Ship & Station Inventories + Market**: Invisible-grid draggable cargo, Mass/Volume limits, stack splitting, NPC Market Stations with 1000x hangars.
- [x] **Save / Load System (Backward-Compatible JSON)**: Versioned JSON saves (`saves/<name>.json`, `schemaVersion`). **Rule**: Every future addition to save data must remain backward-compatible so older save files always load cleanly with sensible defaults for new fields.
- [ ] **Navigation & Autopilot**: `Navigate` button on the Target Card to warp/fly to targeted bodies.
- [ ] **Mining & Refining Loop**: Ship mining lasers, cargo hold, and refining ores into elemental inventory.
- [ ] **Surface View & 4X Stations**: Dedicated planetary/moon surface view to construct mining stations, refineries, factories, and defense platforms.
- [ ] **Factions & Territory**: Faction ownership, gate tolls, NPC patrols, and frontier skirmishes.
