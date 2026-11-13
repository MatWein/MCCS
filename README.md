# MCCS - Crystal Staff

> **Developed entirely by AI.** All code of this mod, its tools and this documentation were written by an AI. It's all about having fun while playing ;)

MCCS adds a **Crystal Staff** to Minecraft: a staff as tall as the player, topped with a glowing blue energy crystal.
It protects you with a shield, hides you with a cloak, fires lightning and energy balls, teleports you, lights up the
dark and lifts things by telekinesis, all powered by the energy stored in its crystal.

There are two staffs that work exactly the same way: the **Normal Crystal Staff** with its blue crystal, and the
**High-Energy Crystal Staff** with an orange crystal and a yellowish glow, which stores ten times as much energy.

| Minecraft | Loaders |
|---|---|
| 1.20.1 | Forge (47.4.16+), Fabric (with Fabric API) |
| 1.21.1 | NeoForge (21.1+), Fabric (with Fabric API) |
| 26.3 | NeoForge, Fabric (with Fabric API) |

It works on its own; energy mods such as Mekanism are optional. Install it on the server and on every client.
Translated into English, German, Spanish, French, Italian, Portuguese (Brazil), Polish, Russian, Ukrainian, Japanese,
Korean and Simplified Chinese.

## Getting started

- **Creative:** tab **Combat** holds each staff new (20 % charge) and fully charged; the search only shows the new
  ones.
- **Survival:** craft the **Normal Crystal Staff** from 2 emeralds in the middle column with a diamond on top and
  redstone dust left and right of the diamond. Upgrade it to the **High-Energy Crystal Staff** by surrounding it with
  4 gold blocks and 4 blaze rods (the new staff starts with 20 % charge):

  ```
  Normal            High-Energy
  R D R             B G B
    E               G S G
    E               B G B
  ```

The staff is carried upright like a shepherd's staff, held near its middle and reaching from the ground to a little
above the head. Its crystal, a cluster of glowing quartz shards, shines on its own: almost white at its foot, with glowing
edges, glints and a soft halo. It is brighter the fuller the staff is, crackles with small arcs from 60 % and turns red
and flickers below 10 %; the item has an energy bar.

## Controls

| Action | Key | Staff in |
|---|---|---|
| Toggle shield | `V` | either hand |
| Toggle cloak | `C` | either hand |
| Teleport | `R` | either hand |
| Toggle light | `L` (replaces advancements while holding the staff) | either hand |
| Toggle block damage | `K` | either hand |
| Lightning | hold the attack key (no mining or melee with the staff) | main hand |
| Energy ball | right click, hold to charge | main hand |
| Telekinesis: lift / release | middle mouse button (replaces pick block while holding the staff) | main hand |
| Telekinesis: throw | attack key while lifting | main hand |

All keys can be rebound under *Options → Controls → MCCS - Crystal Staff*.

None of the abilities work while you ride anything (horse, boat, minecart, ...) or when the staff is put away: shield
and cloak pause and resume afterwards. A small panel at the bottom left shows the charge and all systems while you
hold the staff, and also the consumption and remaining time while they use energy.

## Features

**Shield.** A bubble around the player absorbs all damage except `/kill` and the void, without damage sounds. Hits
make it flash and sound. In first person your own bubble only shows when it is hit.

**Cloak.** After half a second you are invisible to others including armor and items, apart from a very faint energy
bubble. You do not see yourself either, not even your hand and the staff in first person, and the crystal does not
glow. Mobs lose track of you and cannot target you. You remain solid, and sounds you make can still be heard.
Attacking with the staff ends the cloak.

**Lightning.** A flickering lightning runs from the crystal to what you aim at, up to 24 blocks away, and keeps
running as long as you hold the attack key; it ends at the first block or in the air without doing damage.

- It deals 16 damage per second, split into 4 hits. Staying on the same target charges it from 50 % to 150 % within
  3 seconds.
- It jumps on to up to 3 further hostile mobs, up to 6 blocks per jump, with half the damage each time. Pets,
  villagers and players are only hit when aimed at directly.
- Targets in water or rain take 1.5 times the damage and the lightning jumps 1.5 times as far from them.
- Hits slow mobs down strongly while the lightning is on them and set targets on fire unless they are wet.

**Energy ball.** A slow ball of energy flies up to 128 blocks in a straight line and explodes on the first block or
entity. The explosion deals up to 40 damage within 4 blocks and throws things away from it, but does not hurt you. A
shot costs 30,000 FE and takes 2 seconds until the next one. Holding the key charges it for up to 2 seconds: the ball
grows at the crystal, and a fully charged one turns yellow, is twice as large and deals 3 times the damage over 1.5
times the radius, for 3 times the energy. You walk slowly while charging.

**Teleport.** You jump to where you look, up to 30 blocks away, stopping before the first block. It costs 20,000 FE
and takes 3 seconds until the next one; without room to stand there it fails.

**Light.** Lights up your surroundings for 2 FE/t: invisible vanilla light blocks follow you. It stays dark while you
are cloaked.

**Block damage** (off by default). With it on, energy ball explosions break blocks up to a blast resistance of 100
(obsidian survives), and the lightning sets the block it aims at on fire, if it can burn there. Otherwise the staff
never breaks blocks, burns them or destroys items. The setting is saved on the staff and shown in the panel.

**Telekinesis.** Lifts the mob, animal, item or other object you aim at, up to 24 blocks away and up to 4 blocks
large. It floats in front of you, connected to the crystal by a calm line. Press again to let it go, or press the
attack key to throw it away; living targets are hurt by hitting something at speed. Holding uses 100 FE/t, a throw
5,000 FE. Other players can only be lifted if the server allows it (`liftPlayers`).

**Natural lightning** charges a staff you hold by 20 % of its capacity. During thunderstorms, lightning within 32
blocks strikes a player with an active shield under open sky.

**Self-charging.** A staff anywhere in your inventory slowly gathers energy on its own (250 FE/t by default: an empty
normal staff is full after about 17 minutes, a high-energy staff after nearly 3 hours), so it can be used without any
other mod. `selfCharge = 0` turns it off.

## Energy

The staff stores energy and accepts it from other mods; it cannot be drained by them. On Forge and NeoForge it takes
Forge Energy / NeoForge energy (Mekanism's joules are converted by Mekanism), on Fabric Team Reborn Energy if that API
is installed.

| Consumer | Default |
|---|---|
| Capacity | 5,000,000 FE (Normal), 50,000,000 FE (High-Energy) |
| Charging from other mods | up to 10,000 FE/t |
| Shield | 50 FE/t (about 83 minutes on a full staff), × 2 under water, × 3 touching lava, plus 4,000 FE per absorbed damage point |
| Cloak | 40 FE/t, × 1.5 under water, × 2 touching lava |
| Under fire (2 s after a hit) | shield × 6, cloak × 2 |
| Self-charging | +250 FE/t for every staff in the inventory |
| Lightning | 500 FE/t (about 8 minutes of firing) |
| Energy ball | 30,000 FE, fully charged 90,000 FE |
| Teleport | 20,000 FE |
| Light | 2 FE/t |
| Telekinesis | 100 FE/t holding, 5,000 FE per throw |

If the energy for a hit is missing, the shield collapses and the hit goes through. An empty staff switches everything
off and cannot start anything.

## Configuration

The settings live in `world/serverconfig/mccs-server.toml` on Forge 1.20.1 and NeoForge 1.21.1, and in
`config/mccs-server.toml` on NeoForge 26.3 and Fabric; a server sends them to joining players.

| Section | Contents |
|---|---|
| `[staff]` | `consumeEnergy` (`false`: everything runs for free), `capacity` (of the normal staff; the high-energy staff stores ten times as much), `startCharge`, `maxReceive`, shield and cloak drain with their water/lava factors, `shieldHitCost`, `shieldUnderFireFactor` (shield) / `underFireFactor` (cloak) / `underFireSeconds`, `selfCharge`, `lightDrain`, `lightLevel`, `lightningCharge`, `lightningAttractRange` |
| `[beam]` | the lightning: `damagePerSecond`, `hitsPerSecond`, `range`, `aimTolerance`, `drain`, `chainTargets`, `chainRange`, `chainFalloff`, `chargeSeconds`, `chargeStart`, `chargeMax`, `wetDamageFactor`, `wetChainRangeFactor`, `slowTicks`, `slowLevel`, `fireSeconds` |
| `[energyBall]` | `damage`, `radius`, `edgeDamage`, `knockback`, `speed`, `range`, `energy`, `cooldownTicks`, `chargeTicks`, `chargedDamageFactor` / `chargedRadiusFactor` / `chargedSizeFactor` / `chargedEnergyFactor`, `blockDropChance`, `maxBlockResistance` |
| `[teleport]` | `range`, `energy`, `cooldownTicks` |
| `[telekinesis]` | `range`, `aimTolerance`, `holdDistance`, `maxSize`, `liftPlayers`, `drain`, `throwEnergy`, `throwSpeed`, `impactDamage`, `maxImpactDamage` |

## Building

The project is split into modules so the game logic does not have to be rewritten for every Minecraft version or mod
loader. Code is written once and placed as widely as it compiles:

| Folder | Contents |
|---|---|
| `core/` | the game logic in plain Java 17, without any Minecraft or mod loader API (`StaffKind` defines the two staffs, `CrystalRender` makes the crystal and its light); version-independent assets (textures, translations in 12 languages, icon) |
| `shared/mc/all/` | Minecraft code that compiles unchanged against every supported version: staff systems (shield, cloak, lightning, ball, teleport, light, telekinesis), client effects, HUD, arm pose; also the item models |
| `shared/mc/<from>-<to>/` | Minecraft code shared by a range of versions, e.g. `1.20.1-1.21.1` (renderers and item models from the time before special item models) and `1.21.1-26.3` (data components, network payloads) |
| `shared/mixin/all/`, `shared/mixin/<from>-<to>/` | vanilla mixins: compiled into the common module where both loaders use them (1.21.1, 26.3), and into the Fabric module only for 1.20.1, where Forge uses its events instead |
| `shared/fabric/all/`, `shared/neoforge/all/`, `shared/<loader>/<from>-<to>/` | loader code shared by several versions: Fabric's TOML config with server sync and Team Reborn Energy, NeoForge's config and item additions |
| `versions/<version>/` | one folder per Minecraft version; its `gradle.properties` holds everything that differs between versions (Minecraft, Java, loader and API versions, which `shared/` folders are compiled in: `shared_mc` into common, `shared_<loader>` into that loader) |
| `versions/<version>/common/` | what only that Minecraft version has: compat classes (`StaffCompat`, `ClientCompat`, `RenderSinks`), item, registry objects, recipe, the remaining renderers and mixins; compiled into each loader module of that version, never shipped on its own |
| `versions/<version>/<loader>/` | the loader's side (`forge`, `neoforge`, `fabric`): registration, energy API, network transport, config, events, mod metadata |
| `build-logic/` | the Gradle convention plugins all these modules apply: `mccs.vanilla` (common), `mccs.forge`, `mccs.neoforge`, `mccs.fabric` (all via `mccs.loader`); module `build.gradle` files only hold what is special about them |

Gradle names the modules after their folder: `versions/26.3/neoforge` is `:mc_26.3_neoforge`. A new Minecraft version
is a copy of a `versions/<version>/` folder with adjusted `gradle.properties`, added to the list in `settings.gradle`;
whatever it shares with a neighbouring version goes into a `shared/` range folder instead of being copied.

Each loader module builds its own jar: `mccs-<version>+mc<minecraft version>-<loader>.jar` in
`versions/<version>/<loader>/build/libs/`. Gradle 9 runs on Java 25 (Gradle finds or downloads it, as well as Java 17
and 21 for the 1.20.1 and 1.21.1 modules). Build plugins: ModDevGradle (Forge 1.20.1 via `legacyforge`, NeoForge) and Fabric Loom.

```bash
./gradlew build                                       # all modules and jars
./gradlew :core:test                                  # fast unit tests of the game logic (no Minecraft)

./gradlew :mc_1.20.1_forge:runClient                  # development clients
./gradlew :mc_1.20.1_fabric:runClient
./gradlew :mc_1.21.1_neoforge:runClient
./gradlew :mc_1.21.1_fabric:runClient
./gradlew :mc_26.3_neoforge:runClient
./gradlew :mc_26.3_fabric:runClient

./gradlew :mc_1.20.1_forge:runServer                  # development servers (same for the other modules)
./gradlew :mc_1.20.1_forge:runClient -Pstandalone     # without the mods in run-server/mods
./gradlew :mc_26.3_fabric:runClient -PjoinServer=localhost   # joining a server directly
./gradlew :mc_26.3_fabric:runClient -PplayerName=Dev2        # a further player (own name and game directory run-Dev2)
```

The Forge module also loads every mod jar in `versions/1.20.1/forge/run-server/mods/` into its development runs, for
testing integrations such as Mekanism. Its development server runs in `run-dev-server/`.

### Model

`tools/make_staff_assets.py` generates the staff's models (without the crystal), their textures (one per staff kind,
differing only in color), the textures of the crystal and its light, and the mod icon; the results are checked in. Edit
the script and run it again instead of editing them by hand.

The crystal is not part of the item model: it shines on its own and changes with the charge, so the mod draws it while it
renders the staff item, with the item's own pose (Minecraft 1.20.1 and 1.21.1: the item model is `builtin/entity` and
`StaffItemRenderer` draws the staff; 26.x: the special item model `mccs:crystal` in `items/<staff>.json`). Shape, colors
and light are made in `core` (`CrystalShape`, `CrystalRender`). `tools/CrystalPreview.java` renders that same code to an
image (several views, night and day, both staffs), to judge changes without starting Minecraft:

```bash
./gradlew :core:classes
java -cp core/build/classes/java/main tools/CrystalPreview.java preview.png
```

The staff stands on the ground and reaches a fifth higher than the player model (32 model units): its shaft above the
grip is longer by `HEAD_RAISE` in the script, and `CrystalShape.FOOT` puts the crystal on the raised head. Its upright
pose in third person depends on two
things that must match: the arm angles in `StaffArmPose` and the `thirdperson_righthand` display transform in the
model, which the script calculates from the same angles. `StaffArmPose` also stops the arm from swinging while walking.

## License

MIT, see [LICENSE](LICENSE).
