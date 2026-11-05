# MCQW

MCQW adds a **Crystal Staff** to Minecraft: a staff as tall as the player, topped with a blue crystal that will later
store energy from Mekanism and other mods. It runs on its own; other mods are optional.

The staff is carried upright like a shepherd's staff, held near its middle and reaching from the ground to the top
of the head.

Craft it from 2 emeralds in the middle column with a diamond on top and redstone dust left and right of the diamond:

```
R D R
  E
  E
```

In the creative inventory, tab **Combat**, there are a new staff (20 % charge) and a fully charged one; the search only shows
the new one. The crystal in your hand glows according to the charge, crackles with small arcs from 60 % and turns red below 10 %; the
item has an energy bar.

## Shield, cloak, lightning and energy ball

Shield and cloak work while the staff is in a hand; none of this works while you ride anything (horse, boat, jumper, ...) or
when the staff is put away: shield and cloak pause and resume afterwards, the panel hides. The keys can be rebound under
*Options → Controls → MCQW*. The sounds are those of MCPJ's jumper.

**Shield (`V`).** A bubble around the player absorbs all damage except `/kill` and the void, without damage sounds.
Hits make it flash and sound. In first person your own bubble only shows when it is hit.

**Cloak (`C`).** After half a second you are invisible to others including armor and items, apart from a very faint energy
bubble. You do not see yourself either, not even your hand and the staff in first person, and the crystal does not glow. Mobs lose
track of you and cannot target you. You remain solid, and sounds you make can still be heard. Attacking with the staff in your main hand ends the cloak.

**Lightning (hold the attack key, staff in the main hand).** A flickering lightning runs from the crystal to what you aim
at, up to 24 blocks away, and keeps running as long as you hold the key; it ends at the first block or in the air without
doing damage. With the staff in the main hand the attack key belongs to the lightning: no mining and no melee. Firing ends the
cloak.

- It deals 16 damage per second, split into 4 hits. Staying on the same target charges it from 50 % to 150 % within 3 seconds.
- It jumps on to up to 3 further hostile mobs, up to 6 blocks per jump, with half the damage each time. Pets, villagers and
  players are only hit when aimed at directly.
- Targets in water or rain take 1.5 times the damage and the lightning jumps 1.5 times as far from them.
- Hits slow mobs down strongly while the lightning is on them and set targets on fire unless they are wet. Blocks never burn.

**Energy ball (right click, staff in the main hand).** A slow ball of energy flies up to 128 blocks in a straight line and
explodes on the first block or entity. The explosion deals up to 40 damage within 4 blocks and throws things away from it, but never
breaks blocks or destroys items, and it does not hurt you. A shot costs 30,000 FE and ends the cloak; the staff then needs 2 seconds
until the next one. Holding the key charges it for up to 2 seconds: the ball grows at the crystal, and a fully charged one turns yellow,
is twice as large and deals 3 times the damage over 1.5 times the radius, for 3 times the energy. You walk slowly while charging.

**Teleport (`R`, staff in a hand).** You jump to where you look, up to 30 blocks away, stopping before the first block. It costs
20,000 FE and takes 3 seconds until the next one; without room to stand there it fails.

**Light (`L`, staff in a hand).** Lights up your surroundings, using 2 FE/t, like the headlights of MCPJ's jumper: invisible light blocks follow
you. It stays dark while you are cloaked. While you hold the staff, L does not open the advancements.

**Block damage (`K`, staff in a hand, off by default).** With it on, energy ball explosions break blocks, up to a blast resistance of 100 (obsidian
survives), and the lightning sets the block it aims at on fire, if it can burn there. The setting is saved on the staff; you see it in the panel.

**Lightning strikes** charge a staff you hold by 20 % of its capacity. During thunderstorms, natural lightning within 32 blocks strikes
a player with an active shield under open sky, who can use that to charge without any other mods.

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
| `capacity` | 5,000,000 FE | |
| `startCharge` | 0.2 | charge of a new staff |
| `maxReceive` | 10,000 FE/t | fastest charging from other mods |
| `shieldDrain` | 50 FE/t | about 83 minutes on a full staff |
| `shieldWaterFactor` / `shieldLavaFactor` | 2 / 3 | shield drain under water / touching lava |
| `shieldHitCost` | 2,000 FE | per point of absorbed damage |
| `cloakDrain` | 40 FE/t | |
| `cloakWaterFactor` / `cloakLavaFactor` | 1.5 / 2 | cloak drain under water / touching lava |
| `underFireFactor` / `underFireSeconds` | 2 / 2 s | shield and cloak drain after being hit |

The lightning has its own section `[beam]`, with `damagePerSecond`, `hitsPerSecond`, `range`, `aimTolerance`, `drain` (500 FE/t,
about 8 minutes of firing on a full staff), `chainTargets`, `chainRange`, `chainFalloff`, `chargeSeconds`, `chargeStart`, `chargeMax`,
`wetDamageFactor`, `wetChainRangeFactor`, `slowTicks`, `slowLevel` and `fireSeconds`. The energy ball is in `[energyBall]`: `damage`,
`radius`, `edgeDamage`, `knockback`, `speed`, `range`, `energy`, `cooldownTicks`, `chargeTicks` and `chargedDamageFactor` / `chargedRadiusFactor` /
`chargedSizeFactor` / `chargedEnergyFactor`. The teleport is in `[teleport]`: `range`, `energy` and `cooldownTicks`. `[staff]` also has `lightDrain`, `lightLevel`, `lightningCharge` and
`lightningAttractRange`, `[energyBall]` also `blockDropChance` and `maxBlockResistance`.

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
testing integrations such as Mekanism. Its development server runs in `run-dev-server/`.

## Model

`tools/make_staff_assets.py` generates the item model, its texture and the mod icon; the results are checked in. Edit
the script and run it again instead of editing them by hand.

The staff is 32 model units long, the height of the player model. Its upright pose in third person depends on two
things that must match: the arm angles in `StaffArmPose` and the `thirdperson_righthand` display transform in the
model, which the script calculates from the same angles. `StaffArmPose` also stops the arm from swinging while walking.
