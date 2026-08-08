"""Orthographic preview renderer for the Atlatitan (Rammer): shows a skin ON THE MODEL.

Why this exists: judging a recolour on the flat 512x512 UV sheet is misleading — the sheet
says nothing about how regions meet on the silhouette, which faces dominate a view, or how
the vein web reads at in-game scale. This renders the actual model (geometry transcribed
verbatim from Alex's Caves 2.0.2's decompiled SauropodBaseModel + AtlatitanModel: every
rotationPoint, rotateAngle, texOffs, mirror flag) in its default pose, with vanilla Cube
UV/vertex conventions (mirror = per-face U flip, UP face V-flipped), painter-sorted with
backface culling.

Run:  python3 tools/preview_atlatitan.py <texture.png> <out.png> [yaw_degrees ...]
Default yaws: 90 (side), 40 (three-quarter), 0 (front).
"""
import sys

import numpy as np
from PIL import Image

# (name, parent, rotationPoint, rotateAngle(rad, XYZ))
PARTS = [
    ('root',       None,        (0, 24, 0),                 (0, 0, 0)),
    ('body',       'root',      (0, 0, 0),                  (0, 0, 0)),
    ('hips',       'body',      (0, -65, 0.5),              (0, 0, 0)),
    ('tail',       'hips',      (0, 6.5, 33),               (0, 0, 0)),
    ('tail2',      'tail',      (0, 1.5, 49),               (0, 0, 0)),
    ('tail3',      'tail2',     (0, 0.5, 48.5),             (0, 0, 0)),
    ('left_Leg',   'hips',      (18, 12, 22.5),             (0, 0, 0)),
    ('left_Foot',  'left_Leg',  (0, 23, 5),                 (0, 0, 0)),
    ('right_Leg',  'hips',      (-18, 12, 22.5),            (0, 0, 0)),
    ('right_Foot', 'right_Leg', (0, 23, 5),                 (0, 0, 0)),
    ('chest',      'hips',      (0, -9, 0),                 (0, 0, 0)),
    ('right_Arm',  'chest',     (-23, -3, -37.5),           (0, 0, 0)),
    ('right_Hand', 'right_Arm', (-3, 32, -9),               (0, 0, 0)),
    ('left_Arm',   'chest',     (23, -3, -37.5),            (0, 0, 0)),
    ('left_Hand',  'left_Arm',  (3, 32, -9),                (0, 0, 0)),
    ('neck',       'chest',     (0.5, -31, -33),            (0, 0, 0)),
    ('neck2',      'neck',      (-0.5, -6, -75.5),          (0, 0, 0)),
    ('head',       'neck2',     (0.8, 8, -75),              (0, 0, 0)),
    ('jaw',        'head',      (-0.8, 10.5, -8.5),         (0, 0, 0)),
    ('dewlap',     'neck2',     (0, 24, -57.5),             (0, 0, 0)),
    # Atlatitan spikes (cube_r1..r12)
    ('cube_r1',  'chest',      (-24, -33, -39),             (0, 0, -0.7854)),
    ('cube_r2',  'chest',      (-24, -33, -2),              (-0.7854, 0, -0.7854)),
    ('cube_r3',  'chest',      (24, -33, -2),               (-0.7854, 0, 0.7854)),
    ('cube_r4',  'chest',      (24, -33, -39),              (0, 0, 0.7854)),
    ('cube_r5',  'chest',      (-24, -33, -39),             (0.3927, 0, -0.7854)),
    ('cube_r6',  'chest',      (24, -33, -39),              (0.3927, 0, 0.7854)),
    ('cube_r7',  'right_Hand', (8.9991, 25.5, -2.7496),     (0, -0.3927, 0)),
    ('cube_r8',  'right_Hand', (-15, 25.5, -2.75),          (0, 0.3927, 0)),
    ('cube_r9',  'left_Hand',  (-8.9991, 25.5, -2.7496),    (0, 0.3927, 0)),
    ('cube_r10', 'left_Hand',  (15, 25.5, -2.75),           (0, -0.3927, 0)),
    ('cube_r11', 'neck2',      (-7, -2, -69),               (0, 0, -0.7854)),
    ('cube_r12', 'neck2',      (7, -2, -69),                (0, 0, 0.7854)),
]

# (part, u, v, (x, y, z), (w, h, d), mirror)
BOXES = [
    ('hips',       230, 149, (-19, -24, -3.5),    (38, 48, 41), False),
    ('tail',         0, 246, (-12, -14.5, 2.5),   (24, 29, 49), False),
    ('tail2',      245, 238, (-8, -10, -6.5),     (16, 20, 57), False),
    ('tail3',      138, 174, (-5, -6.5, -14),     (10, 13, 72), False),
    ('left_Leg',   139,   0, (-9.5, -7, -15),     (19, 35, 27), False),
    ('left_Foot',  270, 315, (-6.5, -5, -5),      (13, 35, 17), False),
    ('left_Foot',  153, 149, (-6.5, 26, -9),      (13, 4, 4),   False),
    ('left_Foot',  153, 157, (-6.5, 26, -9),      (13, 4, 4),   False),
    ('right_Leg',  139,   0, (-9.5, -7, -15),     (19, 35, 27), True),
    ('right_Foot', 270, 315, (-6.5, -5, -5),      (13, 35, 17), True),
    ('right_Foot', 153, 149, (-6.5, 26, -9),      (13, 4, 4),   True),
    ('right_Foot', 153, 157, (-6.5, 26, -9),      (13, 4, 4),   True),
    ('chest',        0, 123, (-24, -33, -56.5),   (48, 66, 57), False),
    ('right_Arm',    0,   0, (-13, -10, -11),     (14, 44, 21), True),
    ('right_Hand', 264,   0, (-15, -2, -2.75),    (24, 47, 29), True),
    ('right_Hand',  20, 238, (-8, -2, -12.75),    (0, 47, 10),  True),
    ('right_Hand',  20, 238, (2, -2, -12.75),     (0, 47, 10),  True),
    ('right_Hand',  49,   0, (9, 37, 13.25),      (8, 8, 8),    True),
    ('left_Arm',     0,   0, (-1, -10, -11),      (14, 44, 21), False),
    ('left_Hand',  264,   0, (-9, -2, -2.75),     (24, 47, 29), False),
    ('left_Hand',   20, 238, (8, -2, -12.75),     (0, 47, 10),  False),
    ('left_Hand',   20, 238, (-2, -2, -12.75),    (0, 47, 10),  False),
    ('left_Hand',   49,   0, (-17, 37, 13.25),    (8, 8, 8),    False),
    ('neck',         0,   0, (-13.5, -14, -76.5), (26, 36, 87), False),
    ('neck2',      153,  44, (-8, -2, -80),       (16, 26, 79), False),
    ('head',       198, 315, (-9.8, -4, -15),     (18, 22, 18), False),
    ('head',         0, 324, (-5.8, -13, -23),    (10, 20, 20), False),
    ('head',       264,  76, (-11.8, 6, -28),     (22, 7, 21),  False),
    ('head',         0,  65, (-11.3, 11, -27.5),  (21, 6, 13),  False),
    ('jaw',        331,  83, (-11, -1.5, -19.5),  (22, 9, 21),  False),
    ('jaw',        360,   0, (-11, 3, -19.5),     (22, 2, 17),  False),
    ('dewlap',      97, 194, (0, -4, -32.5),      (0, 26, 65),  False),
    ('cube_r1',      0, 123, (-6, -23, 14.5),     (11, 38, 11), True),
    ('cube_r2',      0, 123, (-6, -15, 0.5),      (11, 38, 11), True),
    ('cube_r3',      0, 123, (-5, -15, 0.5),      (11, 38, 11), False),
    ('cube_r4',      0, 123, (-5, -23, 14.5),     (11, 38, 11), False),
    ('cube_r5',    146, 285, (-7, -14, -6.5),     (13, 51, 13), True),
    ('cube_r6',    146, 285, (-6, -14, -6.5),     (13, 51, 13), False),
    ('cube_r7',     20, 236, (0, -27.5, -10),     (0, 47, 10),  True),
    ('cube_r8',     20, 236, (0, -27.5, -10),     (0, 47, 10),  True),
    ('cube_r9',     20, 236, (0, -27.5, -10),     (0, 47, 10),  False),
    ('cube_r10',    20, 236, (0, -27.5, -10),     (0, 47, 10),  False),
    ('cube_r11',   227, 259, (-4, -20, 19),       (8, 24, 8),   True),
    ('cube_r11',   227, 259, (-4, -20, 36),       (8, 24, 8),   True),
    ('cube_r11',   139,  62, (-4, -13, 1),        (8, 17, 8),   True),
    ('cube_r11',   139,  62, (-4, -8, 51),        (8, 17, 8),   True),
    ('cube_r12',   227, 259, (-4, -20, 36),       (8, 24, 8),   False),
    ('cube_r12',   227, 259, (-4, -20, 19),       (8, 24, 8),   False),
    ('cube_r12',   139,  62, (-4, -13, 2),        (8, 17, 8),   False),
    ('cube_r12',   139,  62, (-4, -10, 51),       (8, 17, 8),   False),
]


def rot_matrix(ax, ay, az):
    cx, sx = np.cos(ax), np.sin(ax)
    cy, sy = np.cos(ay), np.sin(ay)
    cz, sz = np.cos(az), np.sin(az)
    rx = np.array([[1, 0, 0], [0, cx, -sx], [0, sx, cx]])
    ry = np.array([[cy, 0, sy], [0, 1, 0], [-sy, 0, cy]])
    rz = np.array([[cz, -sz, 0], [sz, cz, 0], [0, 0, 1]])
    return rz @ ry @ rx                      # vanilla ModelPart: rotate Z, then Y, then X


def world_transforms():
    """part name -> (R, T): world point = R @ local + T."""
    out = {}
    for name, parent, rp, ang in PARTS:
        r = rot_matrix(*ang)
        t = np.array(rp, float)
        if parent is None:
            out[name] = (r, t)
        else:
            pr, pt = out[parent]
            out[name] = (pr @ r, pr @ t + pt)
    return out


def box_polygons(u, v, org, size, mirror):
    """The 6 quads of a cube in vanilla Cube convention: list of (corners4x3, uv4x2)."""
    x1, y1, z1 = org
    w, h, d = size
    x2, y2, z2 = x1 + w, y1 + h, z1 + d
    # the 8 vertices, numbered as in net.minecraft.client.model.geom.ModelPart.Cube
    v1 = (x1, y1, z1); v2 = (x2, y1, z1); v3 = (x2, y2, z1); v4 = (x1, y2, z1)
    v5 = (x1, y1, z2); v6 = (x2, y1, z2); v7 = (x2, y2, z2); v8 = (x1, y2, z2)
    l = u; m = u + d; n = u + d + w; o = u + d + w + w; p = u + d + w + d; q = u + d + w + d + w
    r = v; s = v + d; t = v + d + h
    polys = [
        ((v6, v2, v3, v7), (n, s, p, t)),    # EAST  (+X)
        ((v1, v5, v8, v4), (l, s, m, t)),    # WEST  (-X)
        ((v6, v5, v1, v2), (m, r, n, s)),    # DOWN  (-Y, top of texture pair)
        ((v3, v4, v8, v7), (n, s, o, r)),    # UP    (+Y, V-flipped in vanilla)
        ((v2, v1, v4, v3), (m, s, n, t)),    # NORTH (-Z)
        ((v5, v6, v7, v8), (p, s, q, t)),    # SOUTH (+Z)
    ]
    quads = []
    for verts, (ua, va, ub, vb) in polys:
        if mirror:
            verts = verts[::-1]              # vanilla: mirror reverses vertex order = U flip
        # Polygon UV corner assignment: vertex0 (u2,v1), 1 (u1,v1), 2 (u1,v2), 3 (u2,v2)
        uvs = [(ub, va), (ua, va), (ua, vb), (ub, vb)]
        c = np.array(verts, float)
        if np.linalg.norm(np.cross(c[1] - c[0], c[3] - c[0])) < 1e-9:
            continue                         # zero-area face of a flat (w=0 / h=0) plane
        quads.append((c, np.array(uvs, float)))
    return quads


def render(tex, yaw_deg, scale=3.0):
    tf = world_transforms()
    yaw = np.radians(yaw_deg)
    view = np.array([[np.cos(yaw), 0, -np.sin(yaw)], [0, 1, 0], [np.sin(yaw), 0, np.cos(yaw)]])
    quads = []
    for part, u, v, org, size, mirror in BOXES:
        r, t = tf[part]
        for corners, uvs in box_polygons(u, v, org, size, mirror):
            wc = (r @ corners.T).T + t
            sc = (view @ wc.T).T             # screen: x right, y down, z = depth (bigger = nearer)
            normal = np.cross(sc[1] - sc[0], sc[3] - sc[0])
            if normal[2] <= 0:               # backface: vanilla quads wind CCW toward the camera
                continue
            quads.append((sc, uvs, sc[:, 2].mean()))
    # w=0 planes emit both windings' faces? No: both flat faces share geometry, and we culled by
    # winding — re-add the reversed side of flat planes so they are visible from both sides.
    for part, u, v, org, size, mirror in BOXES:
        if size[0] != 0 and size[2] != 0:
            continue
        r, t = tf[part]
        for corners, uvs in box_polygons(u, v, org, size, mirror):
            wc = (r @ corners.T).T + t
            sc = (view @ wc.T).T
            normal = np.cross(sc[1] - sc[0], sc[3] - sc[0])
            if normal[2] > 0:
                continue                     # this side already drawn
            quads.append((sc[::-1], uvs[::-1], sc[:, 2].mean()))
    all_pts = np.vstack([q[0][:, :2] for q in quads])
    lo = all_pts.min(0) - 6
    hi = all_pts.max(0) + 6
    size_px = np.ceil((hi - lo) * scale).astype(int)
    img = np.zeros((size_px[1], size_px[0], 4), np.uint8)
    img[..., :3] = (28, 28, 36)
    img[..., 3] = 255
    th, tw = tex.shape[:2]
    for sc, uvs, _ in sorted(quads, key=lambda q: q[2]):
        p0 = (sc[0, :2] - lo) * scale
        e1 = (sc[1, :2] - sc[0, :2]) * scale
        e2 = (sc[3, :2] - sc[0, :2]) * scale
        det = e1[0] * e2[1] - e1[1] * e2[0]
        if abs(det) < 1e-9:
            continue
        xs0 = max(int(np.floor(min(p0[0], p0[0] + e1[0], p0[0] + e2[0], p0[0] + e1[0] + e2[0]))), 0)
        xs1 = min(int(np.ceil(max(p0[0], p0[0] + e1[0], p0[0] + e2[0], p0[0] + e1[0] + e2[0]))), size_px[0] - 1)
        ys0 = max(int(np.floor(min(p0[1], p0[1] + e1[1], p0[1] + e2[1], p0[1] + e1[1] + e2[1]))), 0)
        ys1 = min(int(np.ceil(max(p0[1], p0[1] + e1[1], p0[1] + e2[1], p0[1] + e1[1] + e2[1]))), size_px[1] - 1)
        if xs1 < xs0 or ys1 < ys0:
            continue
        gx, gy = np.meshgrid(np.arange(xs0, xs1 + 1) + 0.5, np.arange(ys0, ys1 + 1) + 0.5)
        rx = gx - p0[0]
        ry = gy - p0[1]
        a = (rx * e2[1] - ry * e2[0]) / det
        b = (ry * e1[0] - rx * e1[1]) / det
        inside = (a >= 0) & (a <= 1) & (b >= 0) & (b <= 1)
        if not inside.any():
            continue
        uu = uvs[0] + a[..., None] * (uvs[1] - uvs[0]) + b[..., None] * (uvs[3] - uvs[0])
        tu = np.clip(uu[..., 0].astype(int), 0, tw - 1)
        tv = np.clip(uu[..., 1].astype(int), 0, th - 1)
        texel = tex[tv, tu]
        ok = inside & (texel[..., 3] > 127)
        yy, xx = np.where(ok)
        img[ys0 + yy, xs0 + xx] = texel[yy, xx]
    return img


def main():
    tex_path, out_path = sys.argv[1], sys.argv[2]
    yaws = [float(a) for a in sys.argv[3:]] or [90, 40, 0]
    tex = np.array(Image.open(tex_path).convert('RGBA'))
    views = [Image.fromarray(render(tex, yaw)) for yaw in yaws]
    total_w = sum(v.width for v in views) + 10 * (len(views) + 1)
    total_h = max(v.height for v in views) + 20
    canvas = Image.new('RGB', (total_w, total_h), (28, 28, 36))
    x = 10
    for vimg in views:
        canvas.paste(vimg.convert('RGB'), (x, (total_h - vimg.height) // 2))
        x += vimg.width + 10
    canvas.save(out_path)
    print('wrote', out_path)


if __name__ == '__main__':
    main()
