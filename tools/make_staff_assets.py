#!/usr/bin/env python3
"""Generates the crystal staff's item model, its texture and the mod icon.

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
MODEL = ROOT / "mc_core_common/src/main/resources/assets/mcqw/models/item/crystal_staff.json"
ASSETS = ROOT / "core/src/main/resources/assets/mcqw"
TEXTURE = ASSETS / "textures/item/crystal_staff.png"
ICON = ASSETS / "icon.png"

# Staff axis and vertical extent. Minecraft allows element coordinates from -16 to 32.
AXIS = 8.0
BOTTOM = -8.0
TOP = 24.0

TEXTURE_SIZE = 32
UV_PER_PIXEL = 16 / TEXTURE_SIZE
# texture regions in pixels: (x0, y0, x1, y1)
WOOD = (0, 0, 4, 32)
GRIP = (4, 0, 8, 16)
METAL = (8, 0, 16, 16)
CRYSTAL = (16, 0, 32, 16)

# Where the hand sits on the staff, as model height. Standing on the ground with the arm pose of StaffArmPose, the
# hand is 13.44 units above the feet; with the 32-unit staff reaching from the ground to the top of the head this
# is 2.56 units below the middle of the staff.
GRIP_CENTER = AXIS - 2.56


def uv(region):
    return [c * UV_PER_PIXEL for c in region]


def box(width, y0, y1, region, *, rotate=False, shade=True, x=AXIS, z=AXIS, depth=None):
    """A square column around (x, z); rotate turns it by 45 degrees around the staff axis."""
    depth = width if depth is None else depth
    element = {
        "from": [x - width / 2, y0, z - depth / 2],
        "to": [x + width / 2, y1, z + depth / 2],
        "shade": shade,
        "faces": {face: {"uv": uv(region), "texture": "#staff"}
                  for face in ("north", "east", "south", "west", "up", "down")},
    }
    if rotate:
        element["rotation"] = {"origin": [AXIS, (y0 + y1) / 2, AXIS], "axis": "y", "angle": 45}
    return element


def octagon(width, y0, y1, region, **kwargs):
    return [box(width, y0, y1, region, **kwargs), box(width, y0, y1, region, rotate=True, **kwargs)]


def elements():
    parts = []
    # metal foot
    parts += [box(2.6, BOTTOM, BOTTOM + 2, METAL)]
    # shaft
    parts += [box(2, BOTTOM + 2, 16, WOOD)]
    # wrapped grip and the rings bounding it
    parts += [box(2.5, GRIP_CENTER - 3.5, GRIP_CENTER + 3.5, GRIP)]
    parts += [box(3, GRIP_CENTER - 4, GRIP_CENTER - 3.5, METAL), box(3, GRIP_CENTER + 3.5, GRIP_CENTER + 4, METAL)]
    # collar with four prongs holding the crystal
    parts += [box(3, 14.5, 16, METAL)]
    parts += octagon(4.6, 16, 17, METAL)
    for dx, dz in ((2.1, 2.1), (-2.1, 2.1), (2.1, -2.1), (-2.1, -2.1)):
        parts += [box(0.7, 16.5, 19.5, METAL, x=AXIS + dx, z=AXIS + dz)]
    # the crystal: a faceted, double-pointed shard; unshaded so it looks lit from within
    for width, y0, y1 in ((2.2, 17, 18), (3.6, 18, 19), (4.4, 19, 21.5), (3.2, 21.5, 23), (1.6, 23, TOP)):
        parts += octagon(width, y0, y1, CRYSTAL, shade=False)
    return parts


def display():
    # Third person: the item frame sits at the hand, with its +z along the arm's up direction and its +y to the front.
    # Tilting the staff by 90 degrees would make it upright for a hanging arm; the arm is rotated 18 degrees forward
    # (StaffArmPose), so 72 degrees cancels that out. The translation moves the staff's axis into the hand.
    angle = math.radians(18)
    up_in_item_frame = (math.sin(angle), math.cos(angle))  # (y, z)
    above_hand = AXIS - GRIP_CENTER
    third_person = {
        "rotation": [72, 0, 0],
        "translation": [0,
                        round(-2 + above_hand * up_in_item_frame[0], 2),
                        round(1 + above_hand * up_in_item_frame[1], 2)],
        "scale": [1, 1, 1],
    }
    first_person = {"rotation": [0, 0, 4], "translation": [0.5, 3.4, 0], "scale": [0.42, 0.42, 0.42]}
    return {
        "thirdperson_righthand": third_person,
        "firstperson_righthand": first_person,
        # 32 units * 0.62 at 45 degrees still fits into the 16 pixels of an inventory slot
        "gui": {"rotation": [0, 0, -45], "translation": [0, 0, 0], "scale": [0.62, 0.62, 0.62]},
        "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.45, 0.45, 0.45]},
        "fixed": {"rotation": [0, 0, -45], "translation": [0, 0, 0], "scale": [0.6, 0.6, 0.6]},
    }


def write_model():
    model = {
        "textures": {"staff": "mcqw:item/crystal_staff", "particle": "mcqw:item/crystal_staff"},
        "elements": elements(),
        "display": display(),
    }
    MODEL.parent.mkdir(parents=True, exist_ok=True)
    MODEL.write_text(json.dumps(model, indent=1) + "\n")


def fill(pixels, region, color_at):
    x0, y0, x1, y1 = region
    for y in range(y0, y1):
        for x in range(x0, x1):
            pixels[x, y] = (*color_at(x - x0, y - y0), 255)


def write_texture():
    rng = random.Random(7)
    image = Image.new("RGBA", (TEXTURE_SIZE, TEXTURE_SIZE), (0, 0, 0, 0))
    pixels = image.load()

    def wood(x, y):
        base = (74, 50, 34) if x in (1, 2) else (58, 38, 26)
        grain = rng.randint(-7, 7) + (10 if (y + x * 5) % 11 == 0 else 0)
        return tuple(max(0, c + grain) for c in base)

    def grip(x, y):
        strap = (x + y) % 4
        base = (112, 78, 48) if strap < 2 else (84, 56, 34)
        return tuple(c + rng.randint(-4, 4) for c in base)

    def metal(x, y):
        shine = 1 - abs(x - 3.5) / 3.5
        return (int(120 + 90 * shine), int(92 + 76 * shine), int(46 + 44 * shine))

    def crystal(x, y):
        # bright at the top, deep blue at the bottom, with a soft highlight down the middle of each face
        height = 1 - y / 15
        highlight = max(0.0, 1 - abs(x - 7.5) / 3.5) * 36
        return (min(255, int(40 + 70 * height + highlight)),
                min(255, int(120 + 80 * height + highlight)),
                min(255, int(215 + 40 * height + highlight / 2)))

    fill(pixels, WOOD, wood)
    fill(pixels, GRIP, grip)
    fill(pixels, METAL, metal)
    fill(pixels, CRYSTAL, crystal)
    TEXTURE.parent.mkdir(parents=True, exist_ok=True)
    image.save(TEXTURE)


def write_icon():
    size = 128
    image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)
    draw.line([(18, 110), (88, 40)], fill=(74, 50, 34, 255), width=9)
    draw.line([(40, 88), (58, 70)], fill=(112, 78, 48, 255), width=12)
    draw.line([(78, 50), (86, 42)], fill=(200, 160, 80, 255), width=16)
    draw.polygon([(112, 16), (108, 56), (88, 40), (72, 20)], fill=(70, 170, 245, 255))
    draw.polygon([(112, 16), (92, 36), (72, 20)], fill=(150, 220, 255, 255))
    ICON.parent.mkdir(parents=True, exist_ok=True)
    image.save(ICON)


if __name__ == "__main__":
    write_model()
    write_texture()
    write_icon()
