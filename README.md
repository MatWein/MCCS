# MCQW

MCQW adds a **Crystal Staff** to Minecraft: a staff as tall as the player, topped with a blue crystal that will later
store energy from Mekanism and other mods. It runs on its own; other mods are optional.

The staff is carried upright like a shepherd's staff, held near its middle and reaching from the ground to the top
of the head. So far it can only be held; its abilities come later.

Get it from the creative inventory, tab **Combat**.

## Building

| Module | Contents |
|---|---|
| `core/` | game logic in plain Java 17, without any Minecraft or mod loader API; version-independent assets |
| `mc_core_common/` | Minecraft code and resources that compile unchanged against every supported version; no build of its own |
| `mc_1.20.1_common/` | vanilla Minecraft 1.20.1 code shared by both loaders; compiled into each loader module |
| `mc_1.20.1_forge_47/` | Forge 47: registration, arm pose |
| `mc_1.20.1_fabric/` | Fabric: registration, arm pose mixin |
| `mc_26.3_common/` | vanilla Minecraft 26.3 code shared by both loaders, including the arm pose mixin |
| `mc_26.3_neoforge/` | NeoForge: registration |
| `mc_26.3_fabric/` | Fabric: registration |

Each loader module builds its own jar: `mcqw-<version>+mc<minecraft version>-<loader>.jar` in `<module>/build/libs/`.
Gradle 9 runs on Java 25 (Gradle finds or downloads it, as well as Java 17 for the 1.20.1 modules).

```bash
./gradlew build                                  # all modules and jars

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
