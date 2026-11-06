#!/usr/bin/env python3
"""Rainbow textures: removes baked-in white backgrounds, builds the 5-frame
animations, the Rainbow Ingot icon and the worn-armor body textures.
Re-runnable: always starts from the originals kept in art-source/rainbow/."""
import os, json, shutil, colorsys
from collections import deque
from PIL import Image

ICONS = 'src/main/resources/assets/modernkit/textures/item'
ARMOR = 'src/main/resources/assets/minecraft/textures/models/armor'
SRC = 'art-source/rainbow'
FRAMES = 5
NAMES = ['rainbow_sword', 'rainbow_pickaxe', 'rainbow_helmet',
         'rainbow_chestplate', 'rainbow_leggings', 'rainbow_boots']

os.makedirs(SRC, exist_ok=True)
os.makedirs(ARMOR, exist_ok=True)

# ---------------------------------------------------------------- backgrounds
def _bg(p):
    r, g, b, a = p
    return a == 0 or (min(r, g, b) >= 185 and max(r, g, b) - min(r, g, b) <= 22)

def remove_background(im):
    """Flood-fill from the edges: light, unsaturated pixels connected to the
    border become transparent. Highlights inside the icon are kept."""
    im = im.convert('RGBA')
    w, h = im.size
    px = im.load()
    seen, dq = set(), deque()
    border = [(x, y) for x in range(w) for y in (0, h - 1)] + \
             [(x, y) for y in range(h) for x in (0, w - 1)]
    for p in border:
        if p not in seen and _bg(px[p]):
            seen.add(p); dq.append(p)
    while dq:
        x, y = dq.popleft()
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            n = (x + dx, y + dy)
            if 0 <= n[0] < w and 0 <= n[1] < h and n not in seen and _bg(px[n]):
                seen.add(n); dq.append(n)
    removed = 0
    for p in seen:
        if px[p][3] != 0:
            px[p] = (0, 0, 0, 0); removed += 1
    for _ in range(2):  # clear the light anti-aliasing fringe
        edge = []
        for y in range(h):
            for x in range(w):
                r, g, b, a = px[x, y]
                if a and min(r, g, b) >= 170 and max(r, g, b) - min(r, g, b) <= 40:
                    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        nx, ny = x + dx, y + dy
                        if 0 <= nx < w and 0 <= ny < h and px[nx, ny][3] == 0:
                            edge.append((x, y)); break
        for p in edge:
            px[p] = (0, 0, 0, 0); removed += 1
    return im, removed

# ----------------------------------------------------------------- animation
def hue_shift(img, shift):
    r, g, b, a = img.convert('RGBA').split()
    h, s, v = Image.merge('RGB', (r, g, b)).convert('HSV').split()
    h = h.point(lambda x: (x + shift) % 256)
    out = Image.merge('HSV', (h, s, v)).convert('RGB')
    out.putalpha(a)
    return out

def load_square(path):
    im = Image.open(path).convert('RGBA')
    w, h = im.size
    if w != h:
        s = min(w, h); l, t = (w - s) // 2, (h - s) // 2
        im = im.crop((l, t, l + s, t + s))
    if im.size not in ((16, 16), (32, 32)):
        im = im.resize((32, 32), Image.LANCZOS)
    return im

def write_animation(base, name):
    w, h = base.size
    strip = Image.new('RGBA', (w, h * FRAMES), (0, 0, 0, 0))
    for i in range(FRAMES):
        strip.paste(hue_shift(base, int(256 * i / FRAMES)), (0, i * h))
    strip.save(os.path.join(ICONS, name + '.png'))
    with open(os.path.join(ICONS, name + '.png.mcmeta'), 'w') as f:
        json.dump({"animation": {"frametime": 3, "interpolate": True}}, f, indent=2)

def make_ingot():
    im = Image.new('RGBA', (16, 16), (0, 0, 0, 0)); px = im.load()
    cells = {(x, y) for y in range(4, 12) for x in range(3 + (11 - y) // 2, 3 + (11 - y) // 2 + 9)}
    for (x, y) in cells:
        val = 1.0 if y <= 6 else max(0.6, 0.86 - 0.05 * (y - 7))
        r, g, b = colorsys.hsv_to_rgb(((x * 16 + y * 9) % 256) / 256.0, 0.8, val)
        px[x, y] = (int(r * 255), int(g * 255), int(b * 255), 255)
    for (x, y) in list(cells):
        for dx in (-1, 0, 1):
            for dy in (-1, 0, 1):
                nx, ny = x + dx, y + dy
                if (nx, ny) not in cells and 0 <= nx < 16 and 0 <= ny < 16 and px[nx, ny][3] == 0:
                    px[nx, ny] = (22, 16, 30, 255)
    px[7, 4] = px[8, 4] = (255, 255, 255, 255)
    return im

# --------------------------------------------------------------------- armor
def faces(u, v, w, h, d):
    return {'top': (u + d, v, u + d + w, v + d),
            'bottom': (u + d + w, v, u + d + 2 * w, v + d),
            'right': (u, v + d, u + d, v + d + h),
            'front': (u + d, v + d, u + d + w, v + d + h),
            'left': (u + d + w, v + d, u + 2 * d + w, v + d + h),
            'back': (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h)}

SHADE = {'top': 1.08, 'front': 1.0, 'right': 0.86, 'left': 0.86, 'back': 0.78, 'bottom': 0.66}

def new_armor():
    return Image.new('RGBA', (64, 32), (0, 0, 0, 0))

def paint(img, rect, face, rows=None):
    """Rainbow plate with a dark rim, a bright top-left highlight and a soft fade."""
    px = img.load()
    x0, y0, x1, y1 = rect
    ya, yb = (y0, y1) if rows is None else (y0 + rows[0], y0 + rows[1])
    for y in range(ya, yb):
        for x in range(x0, x1):
            hue = ((x * 2 + y * 3) % 64) / 64.0
            val = (0.98 - 0.22 * (y - ya) / max(1, yb - ya)) * SHADE[face]
            if x in (x0, x1 - 1) or y in (ya, yb - 1):
                val *= 0.55
            elif x == x0 + 1 and y == ya + 1:
                val = 1.0
            r, g, b = colorsys.hsv_to_rgb(hue, 0.8, max(0.0, min(1.0, val)))
            px[x, y] = (int(r * 255), int(g * 255), int(b * 255), 255)

def carve(img, rect):
    """Transparent window with a dark frame around it."""
    px = img.load()
    x0, y0, x1, y1 = rect
    for y in range(y0 - 1, y1 + 1):
        for x in range(x0 - 1, x1 + 1):
            inside = x0 <= x < x1 and y0 <= y < y1
            px[x, y] = (0, 0, 0, 0) if inside else (28, 20, 40, 255)

def armor_layer1():
    img = new_armor()
    head = faces(0, 0, 8, 8, 8)
    for f, r in head.items():
        paint(img, r, f)
    fx0, fy0, _, _ = head['front']
    carve(img, (fx0 + 1, fy0 + 3, fx0 + 7, fy0 + 8))          # face opening
    for f, r in faces(16, 16, 8, 12, 4).items():                # chestplate body
        paint(img, r, f)
    for f, r in faces(40, 16, 4, 12, 4).items():                # sleeves (upper arm only)
        if f == 'bottom':
            continue
        paint(img, r, f, None if f == 'top' else (0, 8))
    for f, r in faces(0, 16, 4, 12, 4).items():                 # boots (feet only)
        if f == 'top':
            continue
        paint(img, r, f, None if f == 'bottom' else (7, 12))
    return img

def armor_layer2():
    img = new_armor()
    for f, r in faces(16, 16, 8, 12, 4).items():                # waist
        if f == 'top':
            continue
        paint(img, r, f, None if f == 'bottom' else (8, 12))
    for f, r in faces(0, 16, 4, 12, 4).items():                 # legs
        if f == 'bottom':
            continue
        paint(img, r, f, None if f == 'top' else (0, 11))
    return img

# ---------------------------------------------------------------------- main
print('--- rainbow icons')
for name in NAMES:
    path = os.path.join(ICONS, name + '.png')
    src = os.path.join(SRC, name + '.png')
    if not os.path.exists(src):
        if not os.path.exists(path):
            print('MISSING (skipped):', name); continue
        shutil.copy(path, src)
    cleaned, removed = remove_background(Image.open(src))
    cleaned.save(src)
    write_animation(load_square(src), name)
    print('%-20s background pixels removed: %d' % (name, removed))

write_animation(make_ingot(), 'rainbow_ingot')
armor_layer1().save(os.path.join(ARMOR, 'modernkit_rainbow_layer_1.png'))
armor_layer2().save(os.path.join(ARMOR, 'modernkit_rainbow_layer_2.png'))
print('--- armor body textures rebuilt')

print('--- other item icons')
fixed = 0
for fn in sorted(os.listdir(ICONS)):
    name = fn[:-4]
    if not fn.endswith('.png') or name.startswith('rainbow_') or name == 'bedrock_pickaxe':
        continue
    p = os.path.join(ICONS, fn)
    im = Image.open(p).convert('RGBA')
    w, h = im.size
    if w != h:
        continue  # animated strip
    px = im.load()
    corners = [px[0, 0], px[w - 1, 0], px[0, h - 1], px[w - 1, h - 1]]
    if any(c[3] > 0 and _bg(c) for c in corners):
        cleaned, removed = remove_background(im)
        if removed:
            cleaned.save(p); fixed += 1
            print('%-24s background pixels removed: %d' % (fn, removed))
print('other icons fixed:', fixed)
