#!/usr/bin/env python3
"""Emerald 3D armor textures: 64x64 sheets = the normal armor layout (top half) plus
extra UV islands (bottom half) for the custom 3D pieces in EmeraldArmorModel.java.
Run AFTER make_emerald_textures.py. The pieces/UV table below must match the Java model."""
import colorsys

_e = open('scripts/make_emerald_textures.py').read().split("print('--- emerald icons')")[0]
exec(_e, globals())

TRIM = (150, 255, 200)   # light polished emerald, no gold

def canvas(img):
    out = Image.new('RGBA', (64, 64), (0, 0, 0, 0))
    out.paste(img, (0, 0))
    return out

def paint_part(img, rect, face):
    px = img.load()
    x0, y0, x1, y1 = rect
    w, h = x1 - x0, y1 - y0
    for y in range(y0, y1):
        for x in range(x0, x1):
            hue = 0.37 + 0.03 * (((x * 3 + y * 5) % 8) / 8.0)
            sat = 0.85
            val = (0.95 - 0.2 * (y - y0) / max(1, h)) * SHADE[face]
            if (x in (x0, x1 - 1) or y in (y0, y1 - 1)) and w >= 3 and h >= 3:
                val *= 0.55
            elif w >= 3 and (x + y) % 4 == 0:
                val, sat = min(1.0, val * 1.2), 0.55
            r, g, b = colorsys.hsv_to_rgb(hue, sat, max(0.0, min(1.0, val)))
            px[x, y] = (int(r * 255), int(g * 255), int(b * 255), 255)

def part(img, u, v, w, h, d):
    for f, r in faces(u, v, w, h, d).items():
        paint_part(img, r, f)

# (u, v, w, h, d) -- keep in sync with EmeraldArmorModel.java
OUTER = [(0, 32, 1, 3, 8),    # helmet crest
         (18, 32, 1, 4, 5),   # cheek guard
         (30, 32, 8, 5, 1),   # chest plate
         (30, 38, 6, 3, 1),   # abdomen plate
         (0, 43, 6, 8, 1),    # back plate
         (14, 43, 6, 2, 6),   # pauldron cap
         (38, 42, 1, 4, 6),   # pauldron side
         (0, 52, 7, 2, 7),    # boot cuff
         (28, 52, 6, 2, 2)]   # toe cap
INNER = [(0, 32, 4, 3, 1),    # knee plate
         (12, 32, 1, 5, 4),   # thigh plate
         (24, 32, 6, 3, 1)]   # belt buckle

layer1 = canvas(decorate(armor_layer1(), 1, TRIM))
for p in OUTER:
    part(layer1, *p)
layer1.save(os.path.join(ARMOR, 'modernkit_emerald_layer_1.png'))

layer2 = canvas(decorate(armor_layer2(), 2, TRIM))
for p in INNER:
    part(layer2, *p)
layer2.save(os.path.join(ARMOR, 'modernkit_emerald_layer_2.png'))
print('3D emerald armor textures created (64x64)')
