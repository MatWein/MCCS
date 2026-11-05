# MCQW

MCQW adds a **Crystal Staff** to Minecraft: a staff as tall as the player, topped with a blue crystal that will later
store energy from Mekanism and other mods. It runs on its own; other mods are optional.

The staff is carried upright like a shepherd's staff, held near its middle and reaching from the ground to the top
of the head.

Get it from the creative inventory, tab **Combat**: a new staff (20 % charge) and a fully charged one.

## Shield and cloak

Both only work while the staff is in a hand and you are not riding anything: putting the staff away or mounting (horse,
boat, jumper, ...) pauses them and hides the panel, afterwards they resume. Both keys can be rebound under
*Options → Controls → MCQW*. The sounds are those of MCPJ's jumper.

**Shield (`V`).** A bubble around the player absorbs all damage except `/kill` and the void, without damage sounds.
Hits make it flash and sound. Your own bubble is not visible from inside in first person.

**Cloak (`C`).** After half a second you are invisible to others including armor and items, apart from a very faint energy
bubble; you yourself see a faint glowing outline of your body in third person. Mobs lose track of you and cannot target
you. You remain solid, and sounds you make can still be heard. Attacking with the staff in your main hand ends the cloak.

A small panel at the bottom left shows the charge and both systems while you hold the staff, and also the consumption and
remaining time while they use energy. The time ignores the extra drain under fire.

## Energy

The staff stores energy and accepts it from other mods; it cannot be drained. On Forge and NeoForge it takes Forge Energy /
NeoForge energy (Mekanism's joules are converted by Mekanism), on Fabric Team Reborn Energy if that API is installed.

If the energy for a hit is missing, the shield collapses and the hit goes through. An empty staff switches everything off
and cannot start anything.

Settings live in `world/serverconfig/mcqw-server.toml` on Forge, in `config/mcqw-server.toml` on NeoForge and Fabric
(a server sends them to joining players):

| Setting (`[staff]`) | Default | |
|---|---|---|
| `consumeEnergy` | `true` | `false`: shield and cloak run for free, even when the staff is empty |
| `capacity` | 1,000,000 FE | |
| `startCharge` | 0.2 | charge of a new staff |
| `maxReceive` | 10,000 FE/t | fastest charging from other mods |
| `shieldDrain` | 50 FE/t | about 17 minutes on a full staff |
| `shieldWaterFactor` / `shieldLavaFactor` | 2 / 3 | shield drain under water / touching lava |
| `shieldHitCost` | 20 FE | per point of absorbed damage |
| `cloakDrain` | 40 FE/t | |
| `cloakWaterFactor` / `cloakLavaFactor` | 1.5 / 2 | cloak drain under water / touching lava |
| `underFireFactor` / `underFireSeconds` | 2 / 2 s | shield and cloak drain after being hit |

## Building

| Module | Contents |
|---|---|
| `core/` | game logic in plain Java 17, without any Minecraft or mod loader API; version-independent assets |
| `mc_core_common/` | Minecraft code and resources that compile unchanged against every supported version; no build of its own |
| `mc_1.20.1_common/` | vanilla Minecraft 1.20.1 code shared by both loaders; compiled into each loader module |
| `mc_1.20.1_forge_47/` | Forge 47: registration, Forge Energy, arm pose, network, config, events |
| `mc_1.20.1_fabric/` | Fabric: registration, Team Reborn Energy (optional), arm pose mixin, network, TOML config with server sync, events |
| `mc_26.3_common/` | vanilla Minecraft 26.3 code shared by both loaders, including the arm pose and shield bubble mixins |
| `mc_26.3_neoforge/` | NeoForge: registration, energy capability, payloads, config, events |
| `mc_26.3_fabric/` | Fabric: registration, Team Reborn Energy (optional), payloads, TOML config with server sync, events |

Each loader module builds its own jar: `mcqw-<version>+mc<minecraft version>-<loader>.jar` in `<module>/build/libs/`.
Gradle 9 runs on Java 25 (Gradle finds or downloads it, as well as Java 17 for the 1.20.1 modules).

```bash
./gradlew build                                  # all modules and jars
./gradlew :core:test                             # fast unit tests of the game logic (no Minecraft)

./gradlew :mc_1.20.1_forge_47:runClient          # development clients
./gradlew :mc_1.20.1_fabric:runClient
./gradlew :mc_26.3_neoforge:runClient
./gradlew :mc_26.3_fabric:runClient

./gradlew :mc_1.20.1_forge_47:runServer          # development servers (same for the other modules)
./gradlew :mc_1.20.1_forge_47:runClient -Pstandalone   # without the mods in run-server/mods
```

The Forge module also loads every mod jar in `mc_1.20.1_forge_47/run-server/mods/` into its development runs, for
testing integrations such as Mekanism.

## Model

`tools/make_staff_assets.py` generates the item model, its texture and the mod icon; the results are checked in. Edit
the script and run it again instead of editing them by hand.

The staff is 32 model units long, the height of the player model. Its upright pose in third person depends on two
things that must match: the arm angles in `StaffArmPose` and the `thirdperson_righthand` display transform in the
model, which the script calculates from the same angles. `StaffArmPose` also stops the arm from swinging while walking.
