#!/usr/bin/env python3
"""Emerald textures: removes white backgrounds, shrinks oversized icons (kept static,
no animation) and generates the green worn-armor body textures.
Re-runnable: always starts from the originals kept in art-source/emerald/."""
import os, shutil, colorsys
from PIL import Image

# reuse the helpers from the rainbow script (everything before its main section)
_src = open('scripts/make_rainbow_textures.py').read().split("print('--- rainbow icons')")[0]
exec(_src, globals())

EMERALD_SRC = 'art-source/emerald'
EMERALD_NAMES = ['emerald_sword', 'emerald_pickaxe', 'emerald_axe', 'emerald_shovel',
                 'emerald_helmet', 'emerald_chestplate', 'emerald_leggings', 'emerald_boots']
os.makedirs(EMERALD_SRC, exist_ok=True)

def paint(img, rect, face, rows=None):
    """Green emerald plate: faceted highlights, dark rim, soft fade."""
    px = img.load()
    x0, y0, x1, y1 = rect
    ya, yb = (y0, y1) if rows is None else (y0 + rows[0], y0 + rows[1])
    for y in range(ya, yb):
        for x in range(x0, x1):
            hue = 0.37 + 0.03 * (((x * 3 + y * 5) % 8) / 8.0)
            sat = 0.85
            val = (0.92 - 0.2 * (y - ya) / max(1, yb - ya)) * SHADE[face]
            if x in (x0, x1 - 1) or y in (ya, yb - 1):
                val *= 0.5
            elif x == x0 + 1 and y == ya + 1:
                val, sat = 1.0, 0.3
            elif (x + y) % 5 == 0:
                val, sat = min(1.0, val * 1.15), 0.6
            r, g, b = colorsys.hsv_to_rgb(hue, sat, max(0.0, min(1.0, val)))
            px[x, y] = (int(r * 255), int(g * 255), int(b * 255), 255)

def carve(img, rect):
    px = img.load()
    x0, y0, x1, y1 = rect
    for y in range(y0 - 1, y1 + 1):
        for x in range(x0 - 1, x1 + 1):
            inside = x0 <= x < x1 and y0 <= y < y1
            px[x, y] = (0, 0, 0, 0) if inside else (8, 44, 24, 255)

print('--- emerald icons')
for name in EMERALD_NAMES:
    path = os.path.join(ICONS, name + '.png')
    src = os.path.join(EMERALD_SRC, name + '.png')
    if not os.path.exists(src):
        if not os.path.exists(path):
            print('MISSING (skipped):', name); continue
        shutil.copy(path, src)
    cleaned, removed = remove_background(Image.open(src))
    cleaned.save(src)
    load_square(src).save(path)
    print('%-20s background pixels removed: %d' % (name, removed))

print('--- emerald armor body textures')
for layer, build in ((1, armor_layer1), (2, armor_layer2)):
    out = os.path.join(ARMOR, 'modernkit_emerald_layer_%d.png' % layer)
    if os.path.exists(out) and not os.environ.get('FORCE_ARMOR'):
        print('kept existing', os.path.basename(out)); continue
    decorate(build(), layer, (150, 255, 200)).save(out)
    print('created', os.path.basename(out))
