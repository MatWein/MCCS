#!/usr/bin/env python3
"""Generates the crystal staffs' item models, their textures and the mod icon.

There is one staff per kind (StaffKind): all share the geometry of models/item/crystal_staff_shape.json and differ only
in the colors of their texture (models/item/<kind>_body.json). The models hold the staff without its crystal: the crystal
shines on its own and changes with the charge, so the mod draws it at runtime (CrystalGlow, with textures/entity/crystal.png):
- Minecraft 1.20.1: the item model is "builtin/entity" with the staff's display transforms, and StaffItemRenderer draws
  the body model, the crystal and its light
- Minecraft 26.x: items/<kind>.json combines the body model with the special model "mccs:crystal" (with the light in the
  hands)

Run from anywhere: python3 tools/make_staff_assets.py

Model units: 16 = one block at scale 1. The player model is 32 units tall (the renderer scales the whole player, and
with it the held item, to 1.8 blocks), so the 32-unit staff is exactly as tall as the player.
"""
import json
import math
import random
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
MODELS = ROOT / "shared/mc/all/resources/assets/mccs/models/item"
# Minecraft 1.20.1's item models ("builtin/entity", drawn by StaffItemRenderer)
ITEM_MODELS_1_20 = ROOT / "versions/1.20.1/common/src/main/resources/assets/mccs/models/item"
# Minecraft 26.x picks an item's model through items/<item>.json
ITEM_DEFINITIONS = ROOT / "versions/26.3/common/src/main/resources/assets/mccs/items"
ASSETS = ROOT / "core/src/main/resources/assets/mccs"
TEXTURES = ASSETS / "textures/item"
SHAPE = "crystal_staff_shape"
ICON = ASSETS / "icon.png"
BEAM = ASSETS / "textures/entity/beam.png"
CRYSTAL_GLOW = ASSETS / "textures/entity/crystal_glow.png"
CRYSTAL_FACETS = ASSETS / "textures/entity/crystal.png"
GLINT = ASSETS / "textures/entity/glint.png"

# Staff axis and vertical extent. Minecraft allows element coordinates from -16 to 32.
AXIS = 8.0
BOTTOM = -8.0
TOP = 24.0

TEXTURE_SIZE = 32
UV_PER_PIXEL = 16 / TEXTURE_SIZE
# texture regions in pixels: (x0, y0, x1, y1)
SHAFT = (0, 0, 4, 32)
GRIP = (4, 0, 8, 16)
GLOW = (4, 16, 8, 20)
STEEL = (8, 0, 16, 16)

# The arm pose of StaffArmPose, in degrees: forward and away from the body. Keep both in sync with that class.
ARM_FORWARD = 22.0
ARM_OUTWARD = 9.0

# Player model coordinates (+y points down, -x to the player's right, -z to the front), for the right arm.
SHOULDER = (-5.0, 2.0, 0.0)
HAND = (-1.0, 9.0, 0.0)  # middle of the hand, relative to the shoulder
ITEM_ORIGIN = (-1.0, 10.0, -2.0)  # where Minecraft attaches a held item, relative to the shoulder
BODY_MIDDLE = 8.0  # halfway between the ground (y = 24) and the top of the head (y = -8)


def rotate_x(v, degrees):
    c, s = math.cos(math.radians(degrees)), math.sin(math.radians(degrees))
    return (v[0], v[1] * c - v[2] * s, v[1] * s + v[2] * c)


def rotate_z(v, degrees):
    c, s = math.cos(math.radians(degrees)), math.sin(math.radians(degrees))
    return (v[0] * c - v[1] * s, v[0] * s + v[1] * c, v[2])


def arm_to_body(v):
    return rotate_z(rotate_x(v, -ARM_FORWARD), ARM_OUTWARD)


def body_to_arm(v):
    return rotate_x(rotate_z(v, -ARM_OUTWARD), ARM_FORWARD)


def arm_to_item(v):
    """Axes of a held item relative to the arm, from Minecraft's ItemInHandLayer."""
    return (-v[0], -v[2], -v[1])


# How far the middle of the staff is above the hand when the staff stands on the ground.
ABOVE_HAND = SHOULDER[1] + arm_to_body(HAND)[1] - BODY_MIDDLE
GRIP_CENTER = AXIS - ABOVE_HAND


def uv(region):
    return [c * UV_PER_PIXEL for c in region]


def box(width, y0, y1, region, *, depth=None, x=AXIS, z=AXIS, shade=True, rotation=None):
    """A column around (x, z), optionally with a Minecraft element rotation (origin, axis, angle)."""
    depth = width if depth is None else depth
    element = {
        "from": [x - width / 2, y0, z - depth / 2],
        "to": [x + width / 2, y1, z + depth / 2],
        "shade": shade,
        "faces": {face: {"uv": uv(region), "texture": "#staff"}
                  for face in ("north", "east", "south", "west", "up", "down")},
    }
    if rotation:
        origin, axis, angle = rotation
        element["rotation"] = {"origin": origin, "axis": axis, "angle": angle}
    return element


def octagon(width, y0, y1, region, **kwargs):
    """A round-looking column: a square one plus the same one turned by 45 degrees around the staff axis."""
    turned = ([AXIS, (y0 + y1) / 2, AXIS], "y", 45)
    return [box(width, y0, y1, region, **kwargs), box(width, y0, y1, region, rotation=turned, **kwargs)]


def glow_ring(width, y):
    return octagon(width, y, y + 0.35, GLOW, shade=False)


def fins():
    """Four slanted fins around the crystal, opening upwards."""
    base = 15.4
    length = 6.0
    spread = 1.5
    parts = []
    for sign in (1, -1):
        parts.append(box(0.5, base, base + length, STEEL, depth=1.2, x=AXIS + sign * spread,
                         rotation=([AXIS + sign * spread, base, AXIS], "z", sign * -22.5)))
        parts.append(box(1.2, base, base + length, STEEL, depth=0.5, z=AXIS + sign * spread,
                         rotation=([AXIS, base, AXIS + sign * spread], "x", sign * 22.5)))
    return parts


def elements():
    parts = []
    # pointed steel foot
    parts += [box(0.8, BOTTOM, BOTTOM + 0.8, STEEL)]
    parts += octagon(1.5, BOTTOM + 0.8, BOTTOM + 2.6, STEEL)
    parts += glow_ring(1.8, BOTTOM + 2.6)
    # slim shaft
    parts += octagon(1.5, BOTTOM + 2.95, 14.5, SHAFT)
    # energy lines along the shaft, on two opposite sides
    for sign in (1, -1):
        parts += [box(0.25, GRIP_CENTER + 4.5, 14.5, GLOW, depth=0.5, x=AXIS + sign * 0.8, shade=False)]
        parts += [box(0.25, BOTTOM + 3.5, GRIP_CENTER - 4.5, GLOW, depth=0.5, x=AXIS + sign * 0.8, shade=False)]
    # ribbed grip between two steel sleeves
    parts += octagon(2.0, GRIP_CENTER - 3.2, GRIP_CENTER + 3.2, GRIP)
    parts += octagon(2.2, GRIP_CENTER - 4.2, GRIP_CENTER - 3.2, STEEL)
    parts += octagon(2.2, GRIP_CENTER + 3.2, GRIP_CENTER + 4.2, STEEL)
    # emitter head
    parts += octagon(2.2, 14.5, 15.4, STEEL)
    parts += octagon(3.2, 15.4, 16.1, SHAFT)
    parts += glow_ring(2.2, 16.1)
    parts += fins()
    # the crystal floating above the emitter is drawn by the mod (CrystalGlow.CRYSTAL_LAYERS)
    return parts


def third_person():
    """Places the staff upright with its grip in the hand, whatever the arm pose is."""
    up = arm_to_item(body_to_arm((0.0, -1.0, 0.0)))
    tilt_x = math.degrees(math.atan2(up[2], up[1]))
    tilt_z = math.degrees(math.asin(-up[0]))
    to_hand = arm_to_item(tuple(h - o for h, o in zip(HAND, ITEM_ORIGIN)))
    translation = [t + ABOVE_HAND * u for t, u in zip(to_hand, up)]
    return {
        "rotation": [round(tilt_x, 2), 0, round(tilt_z, 2)],
        "translation": [round(t, 2) for t in translation],
        "scale": [1, 1, 1],
    }


def display():
    return {
        "thirdperson_righthand": third_person(),
        "firstperson_righthand": {"rotation": [0, 0, 4], "translation": [1.3, 3.4, -1.5], "scale": [0.42, 0.42, 0.42]},
        # 32 units * 0.62 at 45 degrees still fits into the 16 pixels of an inventory slot
        "gui": {"rotation": [0, 0, -45], "translation": [0, 0, 0], "scale": [0.62, 0.62, 0.62]},
        "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.45, 0.45, 0.45]},
        "fixed": {"rotation": [0, 0, -45], "translation": [0, 0, 0], "scale": [0.6, 0.6, 0.6]},
    }


def write_shape():
    """The geometry shared by all staffs (without the crystal); the body models only fill in the texture."""
    model = {"textures": {"particle": "#staff"}, "elements": elements(), "display": display()}
    MODELS.mkdir(parents=True, exist_ok=True)
    (MODELS / f"{SHAPE}.json").write_text(json.dumps(model, indent=1) + "\n")


def write_item_model_1_20(kind):
    """Minecraft 1.20.1: "builtin/entity" hands the drawing to StaffItemRenderer, with the staff's display transforms."""
    model = {"parent": "builtin/entity", "textures": {"particle": f"mccs:item/{kind}"}, "display": display()}
    ITEM_MODELS_1_20.mkdir(parents=True, exist_ok=True)
    (ITEM_MODELS_1_20 / f"{kind}.json").write_text(json.dumps(model, indent=1) + "\n")


# the hands, where the crystal's light is shown (not in the inventory, on the ground or in item frames); in first person
# the crystal is drawn narrower and taller to keep its proportions
FIRST_PERSON = ["firstperson_righthand", "firstperson_lefthand"]
THIRD_PERSON = ["thirdperson_righthand", "thirdperson_lefthand"]


def write_item_definition(kind):
    """Minecraft 26.x: the body, and the crystal drawn by the mod, with its light in the hands."""
    def crystal(**options):
        return {"type": "minecraft:special", "base": f"mccs:item/{kind}_body",
                "model": {"type": "mccs:crystal", **options}}

    crystal_by_context = {
        "type": "minecraft:select",
        "property": "minecraft:display_context",
        "cases": [{"when": FIRST_PERSON, "model": crystal(effects=True, first_person=True)},
                  {"when": THIRD_PERSON, "model": crystal(effects=True)}],
        "fallback": crystal(effects=False),
    }
    body = {"type": "minecraft:model", "model": f"mccs:item/{kind}_body"}
    definition = {"model": {"type": "minecraft:composite", "models": [body, crystal_by_context]}}
    ITEM_DEFINITIONS.mkdir(parents=True, exist_ok=True)
    (ITEM_DEFINITIONS / f"{kind}.json").write_text(json.dumps(definition, indent=2) + "\n")


def fill(pixels, region, color_at):
    x0, y0, x1, y1 = region
    for y in range(y0, y1):
        for x in range(x0, x1):
            pixels[x, y] = (*color_at(x - x0, y - y0), 255)


def write_body_model(kind):
    texture = f"mccs:item/{kind}"
    model = {"parent": f"mccs:item/{SHAPE}", "textures": {"staff": texture, "particle": texture}}
    (MODELS / f"{kind}_body.json").write_text(json.dumps(model, indent=1) + "\n")


# The kinds of staff (StaffKind): item name -> color of the glowing rings and energy lines (the crystal's colors are in
# StaffKind).
KINDS = {
    "crystal_staff": {"glow": (120, 235, 255)},
    "high_energy_crystal_staff": {"glow": (255, 225, 120)},
}


def write_texture(kind, colors):
    rng = random.Random(7)
    image = Image.new("RGBA", (TEXTURE_SIZE, TEXTURE_SIZE), (0, 0, 0, 0))
    pixels = image.load()

    def shaft(x, y):
        # dark gunmetal with a lighter edge and panel seams
        shade = 52 if x in (1, 2) else 40
        if y % 8 == 0:
            shade = 26
        return (shade, shade + 5, shade + 12)

    def grip(x, y):
        # matte black ribs
        shade = 34 if y % 2 == 0 else 18
        return (shade, shade, shade + 3)

    def glow(x, y):
        return colors["glow"]

    def steel(x, y):
        # bright brushed metal, slightly blue, with a dark seam
        shine = 1 - abs(x - 3.5) / 3.5
        value = 165 + int(60 * shine) + rng.randint(-3, 3)
        if y % 8 == 7:
            value -= 70
        return (value - 6, value, value + 8)

    fill(pixels, SHAFT, shaft)
    fill(pixels, GRIP, grip)
    fill(pixels, GLOW, glow)
    fill(pixels, STEEL, steel)
    TEXTURES.mkdir(parents=True, exist_ok=True)
    image.save(TEXTURES / f"{kind}.png")
    write_body_model(kind)


def write_crystal_facets():
    """
    The facets of the crystal the mod draws, in grey (CrystalRender colors them): bright and smooth, a little brighter
    down the middle of each face, with faint streaks along the crystal like the growth lines of real quartz. The edges
    are drawn as glowing lines by the mod.
    """
    size = 32
    rng = random.Random(3)
    image = Image.new("RGBA", (size, size))
    pixels = image.load()
    streaks = [rng.uniform(-0.06, 0.06) for _ in range(size)]
    for y in range(size):
        for x in range(size):
            middle = 1 - abs(x + 0.5 - size / 2) / (size / 2)
            value = 0.8 + 0.2 * middle + streaks[x] + rng.uniform(-0.015, 0.015)
            pixels[x, y] = (*(round(255 * max(0.0, min(1.0, value))),) * 3, 255)
    CRYSTAL_FACETS.parent.mkdir(parents=True, exist_ok=True)
    image.save(CRYSTAL_FACETS)


def write_beam():
    """The lightning's ribbon: bright in the middle and fading to nothing (black, it is drawn additively) at both edges."""
    size = 16
    image = Image.new("RGBA", (size, size))
    pixels = image.load()
    for y in range(size):
        edge = 1 - abs(y + 0.5 - size / 2) / (size / 2)
        value = int(255 * edge * edge)
        for x in range(size):
            pixels[x, y] = (value, value, value, 255)
    BEAM.parent.mkdir(parents=True, exist_ok=True)
    image.save(BEAM)


def write_crystal_glow():
    """
    The crystal's halo: bright in the middle, fading to nothing (black, it is drawn additively) at the edge. Large and
    filtered linearly (the .mcmeta file), so it shows no pixels even close up.
    """
    size = 128
    image = Image.new("RGBA", (size, size))
    pixels = image.load()
    for y in range(size):
        for x in range(size):
            distance = math.hypot(x + 0.5 - size / 2, y + 0.5 - size / 2) / (size / 2)
            value = round(255 * max(0.0, 1 - distance) ** 2)
            pixels[x, y] = (value, value, value, 255)
    save_smooth(image, CRYSTAL_GLOW)


def write_glint():
    """A glint on the crystal: a four-pointed star with a small soft core, filtered linearly like the halo."""
    size = 64
    image = Image.new("RGBA", (size, size))
    pixels = image.load()
    for y in range(size):
        for x in range(size):
            dx = abs(x + 0.5 - size / 2) / (size / 2)
            dy = abs(y + 0.5 - size / 2) / (size / 2)
            # thin rays along both axes, thinner towards their tips, and a round core
            rays = max(max(0.0, 1 - dx) ** 2 * max(0.0, 1 - dy / 0.06 / max(0.05, 1 - dx)),
                       max(0.0, 1 - dy) ** 2 * max(0.0, 1 - dx / 0.06 / max(0.05, 1 - dy)))
            core = max(0.0, 1 - math.hypot(dx, dy) / 0.35) ** 2
            value = round(255 * min(1.0, rays + core))
            pixels[x, y] = (value, value, value, 255)
    save_smooth(image, GLINT)


def save_smooth(image, path):
    """Saves a texture that Minecraft filters linearly instead of showing its pixels."""
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)
    path.with_name(path.name + ".mcmeta").write_text(json.dumps({"texture": {"blur": True}}, indent=2) + "\n")


def write_icon():
    size = 128
    image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw.line([(18, 110), (84, 44)], fill=(46, 51, 58, 255), width=8)
    draw.line([(18, 110), (84, 44)], fill=(120, 235, 255, 255), width=2)
    draw.line([(38, 90), (56, 72)], fill=(24, 24, 27, 255), width=11)
    draw.line([(76, 52), (84, 44)], fill=(205, 212, 220, 255), width=14)
    draw.polygon([(114, 14), (106, 54), (90, 46), (74, 22)], fill=(70, 170, 245, 255))
    draw.polygon([(114, 14), (92, 36), (74, 22)], fill=(150, 225, 255, 255))
    ICON.parent.mkdir(parents=True, exist_ok=True)
    image.save(ICON)


if __name__ == "__main__":
    write_shape()
    write_crystal_facets()
    write_glint()
    for kind, colors in KINDS.items():
        write_item_model_1_20(kind)
        write_item_definition(kind)
        write_texture(kind, colors)
    write_icon()
    write_beam()
    write_crystal_glow()
