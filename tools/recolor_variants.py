"""Generate the recoloured *_variant textures for the Vallumraptor (Stealer) and the
Atlatitan (Rammer) from Alex's Caves' base textures, with model-aware masking.

Why model-aware: a naive whole-image hue rotation recolours every pixel the same way, which is
how the old Stealer variants turned its keratin claws into noisy maroon and would turn the
Rammer's glowing eyes purple. This script rebuilds the UV layout of each model from its
decompiled geometry (part -> texture rectangles, including zero-width quill/claw planes), so
every rule below states explicitly WHICH body part it touches:

  * body/skin parts get the variant's palette hue rotation (the established palettes are kept:
    Stealer standard blue->green, retro ice->sand, tectonic rust->purple);
  * claws, teeth and eyes are either protected or given one deliberate, uniform treatment that
    preserves the base shading (value channel) instead of inheriting a blind rotation;
  * glowing accents (the Stealer's fire-tipped quills, the tectonic Rammer's magma cracks and
    claws, every blue bioluminescent eye) keep their identity.

The Rammer's three variants are new in 3.0.0 (design brief 2026-08-07):
  standard -> purple-oriented tones, retro -> green/lime, tectonic -> lighter ash greys.

Run:  python3 tools/recolor_variants.py   (writes into src/main/resources/.../textures/entity)
"""
import colorsys
import os
import zipfile
from io import BytesIO

import numpy as np
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
PROJ = os.path.dirname(HERE)
AC_JAR = os.path.join(PROJ, 'libs/alexscaves-full-2.0.2.jar')
OUT = os.path.join(PROJ, 'src/main/resources/assets/alexscaves/textures/entity')

# ---------------------------------------------------------------------------------------------
# Model geometry, transcribed from Alex's Caves 2.0.2 (decompiled VallumraptorModel /
# SauropodBaseModel / AtlatitanModel). (part, u, v, w, h, d) per addBox call.
# ---------------------------------------------------------------------------------------------

VALLUMRAPTOR_BOXES = [
    ('body',      0,  0, 7, 7, 12),
    ('leg',      34, 14, 4, 8, 5),
    ('leg2',      0, 48, 2, 9, 2),
    ('foot',     20,  0, 5, 0, 6),      # flat sole plane, toe claws painted on it
    ('claw',     21, 34, 0, 5, 5),      # the sickle claw plane
    ('arm',       8, 48, 2, 6, 2),
    ('armwing',  44,  0, 2, 3, 3),
    ('hand',      0, 28, 4, 3, 3),
    ('neck',     47, 22, 3, 9, 5),
    ('head',      0,  0, 2, 4, 4),
    ('head',     50,  8, 2, 6, 5),
    ('head',     46, 56, 5, 4, 4),
    ('head',     26, 54, 4, 4, 6),
    ('jaw',      30,  4, 3, 2, 6),
    ('teeth',     2, 57, 3, 1, 6),      # upper teeth strip
    ('headquill',46, 27, 0, 14, 9),
    ('tail',     16, 19, 3, 3, 12),
    ('tailtip',   0,  5, 0, 9, 14),
    ('tailquill',18, 36, 0, 6, 12),
    ('quill',    16, 47, 0, 3, 7),
]
VALLUMRAPTOR_TEX = (64, 64)

SAUROPOD_BOXES = [
    ('hips',    230, 149, 38, 48, 41),
    ('tail',      0, 246, 24, 29, 49),
    ('tail2',   245, 238, 16, 20, 57),
    ('tail3',   138, 174, 10, 13, 72),
    ('leg',     139,   0, 19, 35, 27),
    ('foot',    270, 315, 13, 35, 17),
    ('toes',    153, 149, 13, 4, 4),    # toe claw band
    ('toes',    153, 157, 13, 4, 4),
    ('chest',     0, 123, 48, 66, 57),
    ('arm',       0,   0, 14, 44, 21),
    ('hand',    264,   0, 24, 47, 29),
    ('handspike',20, 238,  0, 47, 10),  # keratin spike sheets on the hands
    ('thumb',    49,   0,  8,  8,  8),
    ('neck',      0,   0, 26, 36, 87),
    ('neck2',   153,  44, 16, 26, 79),
    ('head',    198, 315, 18, 22, 18),
    ('head',      0, 324, 10, 20, 20),
    ('snout',   264,  76, 22,  7, 21),
    ('snout',     0,  65, 21,  6, 13),
    ('jaw',     331,  83, 22,  9, 21),
    ('teeth',   360,   0, 22,  2, 17),  # mouth strip
    ('dewlap',   97, 194,  0, 26, 65),  # skin fin under the neck
]
ATLATITAN_EXTRA_BOXES = [
    ('shoulderspike', 0, 123, 11, 38, 11),   # 4 boxes; shares the chest UV region
    ('dorsalspike', 146, 285, 13, 51, 13),   # the big back spikes (dedicated region)
    ('handspike',    20, 236,  0, 47, 10),
]
ATLATITAN_TEX = (512, 512)


def box_rects(u, v, w, h, d):
    """The 6 face rectangles of a box in the standard Minecraft UV unwrap; zero-area faces skipped."""
    faces = [
        (u + d,         v,     w, d),   # top
        (u + d + w,     v,     w, d),   # bottom
        (u,             v + d, d, h),   # east
        (u + d,         v + d, w, h),   # north
        (u + d + w,     v + d, d, h),   # west
        (u + d + w + d, v + d, w, h),   # south
    ]
    return [(x, y, rw, rh) for x, y, rw, rh in faces if rw > 0 and rh > 0]


def build_masks(boxes, texsize):
    """part name -> boolean HxW mask of the pixels that part renders."""
    tw, th = texsize
    masks = {}
    for part, u, v, w, h, d in boxes:
        m = masks.setdefault(part, np.zeros((th, tw), dtype=bool))
        for x, y, rw, rh in box_rects(u, v, w, h, d):
            m[y:y + rh, x:x + rw] = True
    return masks


def union(masks, *parts):
    m = np.zeros_like(next(iter(masks.values())))
    for p in parts:
        if p in masks:
            m |= masks[p]
    return m


# ---------------------------------------------------------------------------------------------
# Colour helpers (vectorised HSV)
# ---------------------------------------------------------------------------------------------

def to_hsv(rgba):
    rgb = rgba[..., :3] / 255.0
    maxc = rgb.max(-1)
    minc = rgb.min(-1)
    v = maxc
    delta = maxc - minc
    s = np.where(maxc > 0, delta / np.maximum(maxc, 1e-9), 0)
    r, g, b = rgb[..., 0], rgb[..., 1], rgb[..., 2]
    h = np.zeros_like(maxc)
    nz = delta > 0
    rmax = nz & (maxc == r)
    gmax = nz & (maxc == g) & ~rmax
    bmax = nz & ~rmax & ~gmax
    h[rmax] = (60 * (g - b)[rmax] / delta[rmax]) % 360
    h[gmax] = (60 * (b - r)[gmax] / delta[gmax] + 120) % 360
    h[bmax] = (60 * (r - g)[bmax] / delta[bmax] + 240) % 360
    return h, s, v


def from_hsv(h, s, v, alpha):
    h = (h % 360) / 60.0
    i = np.floor(h).astype(int) % 6
    f = h - np.floor(h)
    p = v * (1 - s)
    q = v * (1 - s * f)
    t = v * (1 - s * (1 - f))
    r = np.choose(i, [v, q, p, p, t, v])
    g = np.choose(i, [t, v, v, q, p, p])
    b = np.choose(i, [p, p, t, v, v, q])
    out = np.stack([r, g, b], axis=-1)
    out = np.clip(np.round(out * 255), 0, 255).astype(np.uint8)
    return np.dstack([out, alpha.astype(np.uint8)])


def hue_in(h, lo, hi):
    lo %= 360
    hi %= 360
    return (h >= lo) & (h <= hi) if lo <= hi else (h >= lo) | (h <= hi)


def load_base(name):
    jar = zipfile.ZipFile(AC_JAR)
    img = Image.open(BytesIO(jar.read('assets/alexscaves/textures/entity/%s.png' % name))).convert('RGBA')
    return np.array(img)


def save(name, rgba):
    Image.fromarray(rgba, 'RGBA').save(os.path.join(OUT, '%s.png' % name))
    print('wrote', name + '.png')


# ---------------------------------------------------------------------------------------------
# Recolour primitives
# ---------------------------------------------------------------------------------------------

def rotate_hue(h, s, v, sel, rotation, sat_scale=1.0, val_scale=1.0, val_add=0.0):
    h = h.copy(); s = s.copy(); v = v.copy()
    h[sel] = (h[sel] + rotation) % 360
    s[sel] = np.clip(s[sel] * sat_scale, 0, 1)
    v[sel] = np.clip(v[sel] * val_scale + val_add, 0, 1)
    return h, s, v


def set_hue(h, s, v, sel, hue, sat=None, sat_scale=1.0, val_scale=1.0, val_add=0.0):
    """Deliberate uniform treatment: one hue for the whole selection, shading (V) preserved."""
    h = h.copy(); s = s.copy(); v = v.copy()
    h[sel] = hue
    if sat is not None:
        s[sel] = sat
    else:
        s[sel] = np.clip(s[sel] * sat_scale, 0, 1)
    v[sel] = np.clip(v[sel] * val_scale + val_add, 0, 1)
    return h, s, v


# ---------------------------------------------------------------------------------------------
# The Stealer (Vallumraptor) — six variant textures, established palettes kept
# ---------------------------------------------------------------------------------------------

def stealer_variants():
    masks = build_masks(VALLUMRAPTOR_BOXES, VALLUMRAPTOR_TEX)
    claws = union(masks, 'claw', 'foot', 'hand', 'armwing')
    teeth = union(masks, 'teeth')
    quills = union(masks, 'headquill', 'tailquill', 'tailtip', 'quill')

    def make(base_name, out_name, body_rule, claw_rule, protect_warm_quills=True):
        rgba = load_base(base_name)
        alpha = rgba[..., 3]
        opaque = alpha > 0
        h, s, v = to_hsv(rgba)
        # Fire-tipped quill accents (warm hues on the quill planes) keep their identity.
        warm = hue_in(h, 330, 70) & (s > 0.45)
        protected = teeth | (quills & warm if protect_warm_quills else np.zeros_like(opaque))
        body_sel = opaque & ~protected & ~claws
        claw_sel = opaque & ~protected & claws
        h, s, v = body_rule(h, s, v, body_sel)
        h, s, v = claw_rule(h, s, v, claw_sel)
        save(out_name, from_hsv(h, s, v, alpha))

    # STANDARD: blue body -> green (as shipped since 2.x); cream belly (low-sat) keeps itself
    # because a hue rotation barely moves near-grey pixels. Claws: one deliberate dark horn
    # colour (the old blind rotation left them noisy maroon), shading preserved.
    def std_body(h, s, v, sel):
        blues = sel & hue_in(h, 160, 280) & (s > 0.25)
        return rotate_hue(h, s, v, blues, -88)

    def std_claws(h, s, v, sel):
        return set_hue(h, s, v, sel & (s > 0.15), 28, sat=0.5)          # dark horn brown

    # RETRO: icy blue -> warm sand (as shipped). Claw planes keep a cool slate so they still
    # read as keratin against the sandy hide.
    def retro_body(h, s, v, sel):
        cool = sel & hue_in(h, 120, 280)
        return rotate_hue(h, s, v, cool, -153, sat_scale=1.6)

    def retro_claws(h, s, v, sel):
        return set_hue(h, s, v, sel & (s > 0.10), 200, sat=0.25)        # slate horn

    # TECTONIC: rust -> purple (as shipped), but the magma accents are the whole point of the
    # tectonic skin: glowing claws, ember quills and the pale eyes stay untouched.
    def tect_body(h, s, v, sel):
        # rust/red hide only; the bright lava oranges (high V, high S warm) are protected below
        rust = sel & hue_in(h, 340, 45) & ~(hue_in(h, 15, 60) & (v > 0.55) & (s > 0.6))
        return rotate_hue(h, s, v, rust, 269, sat_scale=0.6)

    def tect_claws(h, s, v, sel):
        return h, s, v                                                   # magma claws: identity

    make('vallumraptor', 'vallumraptor_variant', std_body, std_claws)
    make('vallumraptor_elder', 'vallumraptor_elder_variant', std_body, std_claws)
    make('vallumraptor_retro', 'vallumraptor_retro_variant', retro_body, retro_claws)
    make('vallumraptor_retro_elder', 'vallumraptor_retro_elder_variant', retro_body, retro_claws)
    make('vallumraptor_tectonic', 'vallumraptor_tectonic_variant', tect_body, tect_claws)
    make('vallumraptor_tectonic_elder', 'vallumraptor_tectonic_elder_variant', tect_body, tect_claws)


# ---------------------------------------------------------------------------------------------
# The Rammer (Atlatitan) — three new variant textures (3.0.0)
# ---------------------------------------------------------------------------------------------

def rammer_variants():
    boxes = SAUROPOD_BOXES + ATLATITAN_EXTRA_BOXES
    masks = build_masks(boxes, ATLATITAN_TEX)
    spikes = union(masks, 'dorsalspike', 'handspike', 'thumb', 'toes')
    teeth = union(masks, 'teeth')

    def finish(name, h, s, v, alpha):
        save(name, from_hsv(h, s, v, alpha))

    # The bioluminescent blue (eyes + glow markings) sits at the SAME UV pixels on all three
    # skins, but on the retro texture its dimmer edge pixels blend into the navy hide. Deriving
    # the mask once, from the standard texture where blue is unambiguous, keeps every variant's
    # eyes intact. Grown by one pixel to catch anti-aliased edges.
    rgba_std = load_base('atlatitan')
    h0, s0, v0 = to_hsv(rgba_std)
    eye_mask = hue_in(h0, 180, 265) & (s0 > 0.35) & (rgba_std[..., 3] > 0)
    grown = eye_mask.copy()
    grown[1:, :] |= eye_mask[:-1, :]; grown[:-1, :] |= eye_mask[1:, :]
    grown[:, 1:] |= eye_mask[:, :-1]; grown[:, :-1] |= eye_mask[:, 1:]
    eye_mask = grown

    # STANDARD -> purple. The brown/orange hide rotates into purple; the pale keratin spikes
    # (low saturation) and the bioluminescent blue keep their identity.
    alpha = rgba_std[..., 3]; opaque = alpha > 0
    h, s, v = h0.copy(), s0.copy(), v0.copy()
    keratin = (s < 0.28) | spikes & (s < 0.45)
    hide = opaque & ~eye_mask & ~teeth & ~keratin & hue_in(h, 350, 70)
    h, s, v = rotate_hue(h, s, v, hide, 245, sat_scale=0.75)             # ~25 deg -> ~270 purple
    finish('atlatitan_variant', h, s, v, alpha)

    # RETRO -> green/lime. The navy hide turns deep green; the pale ice-blue spikes turn lime;
    # the eye pixels stay the retro texture's own blue.
    rgba = load_base('atlatitan_retro')
    alpha = rgba[..., 3]; opaque = alpha > 0
    h, s, v = to_hsv(rgba)
    pale = (v > 0.55) & (s < 0.55)                                       # icy spikes/highlights
    body = opaque & ~eye_mask & ~teeth & ~pale & hue_in(h, 180, 300)
    h, s, v = rotate_hue(h, s, v, body, -110, sat_scale=1.1)             # navy -> deep green
    lime = opaque & ~eye_mask & ~teeth & pale & hue_in(h, 120, 260)
    h, s, v = set_hue(h, s, v, lime, 80, sat_scale=1.7, val_scale=1.05)  # icy -> lime
    finish('atlatitan_retro_variant', h, s, v, alpha)

    # TECTONIC -> ash. The whole hide (including the red magma web) cools into pale ashen grey;
    # only the BRIGHTEST lava pixels stay as dim embers glowing through the ash. Bone spikes,
    # teeth and the blue eyes stay themselves.
    rgba = load_base('atlatitan_tectonic')
    alpha = rgba[..., 3]; opaque = alpha > 0
    h, s, v = to_hsv(rgba)
    bone = (spikes | teeth) & ~hue_in(h, 340, 30)
    bright_ember = hue_in(h, 340, 40) & (s > 0.55) & (v > 0.5) & opaque
    ash = opaque & ~eye_mask & ~bone & ~bright_ember
    h, s, v = set_hue(h, s, v, ash, 30, sat=0.06, val_scale=0.5, val_add=0.4)      # ashen grey
    h, s, v = rotate_hue(h, s, v, bright_ember & ~eye_mask, 0, sat_scale=0.55, val_scale=0.85)  # dim embers
    finish('atlatitan_tectonic_variant', h, s, v, alpha)


if __name__ == '__main__':
    stealer_variants()
    rammer_variants()
