"""Armor detailing: trim bands, crests, belts, cuffs. Used by the rainbow and emerald texture scripts."""

SHADE = {'top': 1.08, 'front': 1.0, 'right': 0.86, 'left': 0.86, 'back': 0.78, 'bottom': 0.66}

def _faces(u, v, w, h, d):
    return {'top': (u + d, v, u + d + w, v + d),
            'bottom': (u + d + w, v, u + d + 2 * w, v + d),
            'right': (u, v + d, u + d, v + d + h),
            'front': (u + d, v + d, u + d + w, v + d + h),
            'left': (u + d + w, v + d, u + 2 * d + w, v + d + h),
            'back': (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h)}

def _fill(img, rect, face, x0, y0, x1, y1, trim, boost=1.0):
    """Fill a local rectangle of a face with the trim colour (only on already-opaque pixels)."""
    px = img.load()
    rx0, ry0, rx1, ry1 = rect
    sh = SHADE[face] * boost
    col = (min(255, int(trim[0] * sh)), min(255, int(trim[1] * sh)), min(255, int(trim[2] * sh)), 255)
    for y in range(ry0 + y0, min(ry0 + y1, ry1)):
        for x in range(rx0 + x0, min(rx0 + x1, rx1)):
            if px[x, y][3] != 0:
                px[x, y] = col

SIDES = ('right', 'front', 'left', 'back')

def decorate(img, layer, trim):
    if layer == 1:
        head = _faces(0, 0, 8, 8, 8)
        _fill(img, head['top'], 'top', 3, 0, 5, 8, trim)                 # crest
        _fill(img, head['front'], 'front', 0, 2, 8, 3, trim)             # brow band
        _fill(img, head['back'], 'back', 3, 0, 5, 8, trim)               # back ridge
        for f in ('right', 'left'):
            _fill(img, head[f], f, 0, 7, 8, 8, trim)                     # lower edge
            _fill(img, head[f], f, 3, 3, 5, 5, trim, 1.1)                # rivet
        body = _faces(16, 16, 8, 12, 4)
        _fill(img, body['front'], 'front', 0, 0, 8, 1, trim)             # collar
        _fill(img, body['front'], 'front', 3, 1, 5, 8, trim)             # chest ridge
        _fill(img, body['front'], 'front', 0, 9, 8, 10, trim)            # belt
        _fill(img, body['front'], 'front', 3, 9, 5, 11, trim, 1.15)      # buckle
        _fill(img, body['back'], 'back', 3, 0, 5, 9, trim)               # spine
        _fill(img, body['back'], 'back', 0, 9, 8, 10, trim)
        for f in ('right', 'left'):
            _fill(img, body[f], f, 0, 9, 4, 10, trim)
        arm = _faces(40, 16, 4, 12, 4)
        for f in SIDES:
            _fill(img, arm[f], f, 0, 3, 4, 4, trim)                      # shoulder band
        _fill(img, arm['top'], 'top', 1, 1, 3, 3, trim, 1.1)
        boot = _faces(0, 16, 4, 12, 4)
        for f in SIDES:
            _fill(img, boot[f], f, 0, 7, 4, 8, trim)                     # cuff
        _fill(img, boot['front'], 'front', 1, 10, 3, 11, trim, 1.1)      # toe cap
    else:
        body = _faces(16, 16, 8, 12, 4)
        for f in SIDES:
            _fill(img, body[f], f, 0, 8, 8, 9, trim)                     # belt
        _fill(img, body['front'], 'front', 3, 9, 5, 11, trim, 1.15)      # buckle
        leg = _faces(0, 16, 4, 12, 4)
        for f in SIDES:
            _fill(img, leg[f], f, 0, 0, 4, 1, trim)                      # top band
            _fill(img, leg[f], f, 0, 6, 4, 7, trim)                      # knee plate
    return img
