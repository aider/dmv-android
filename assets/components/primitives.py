"""Reusable SVG component functions.

Each function returns an SVG markup string fragment (no root <svg> tag).
Coordinates use the caller's viewBox system.
"""

import math
from .style_tokens import *


def _r(v):
    """Round a coordinate to 1 decimal, strip trailing .0."""
    v = round(v, 1)
    return int(v) if v == int(v) else v


# ═══════════════════════════════════════════════════════════════════
# Backgrounds & Surfaces
# ═══════════════════════════════════════════════════════════════════

def grass_bg(w, h):
    """Full-canvas grass background."""
    return f'<rect x="0" y="0" width="{w}" height="{h}" fill="{COLOR_GRASS}"/>'


def road_h(x, y, w, h):
    """Horizontal road strip."""
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{COLOR_ROAD}"/>'


def road_v(x, y, w, h):
    """Vertical road strip."""
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{COLOR_ROAD}"/>'


def curb(x, y, w, h):
    """Curb edge rectangle."""
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{COLOR_CURB}"/>'


# ═══════════════════════════════════════════════════════════════════
# Lane Markings
# ═══════════════════════════════════════════════════════════════════

def yellow_solid(x, y, w, h):
    """Solid yellow center-line segment."""
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{COLOR_YELLOW_LINE}"/>'


def yellow_dashed(x, y, length, orient="H", dash=25, gap=10, thickness=4):
    """Dashed yellow center line.

    orient: "H" = horizontal, "V" = vertical.
    """
    parts = []
    pos = 0
    while pos < length:
        dlen = min(dash, length - pos)
        if orient == "H":
            parts.append(f'<rect x="{x + pos}" y="{y}" width="{dlen}" height="{thickness}" fill="{COLOR_YELLOW_LINE}"/>')
        else:
            parts.append(f'<rect x="{x}" y="{y + pos}" width="{thickness}" height="{dlen}" fill="{COLOR_YELLOW_LINE}"/>')
        pos += dash + gap
    return "\n  ".join(parts)


def stop_line(x, y, w, h=6):
    """White stop line. Minimum 6px height for visibility."""
    h = max(h, 6)
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{COLOR_WHITE}"/>'


def yield_triangles(x, y, w, orient="N", count=1, size=8):
    """Yield triangles at entry points.

    orient: direction triangles point — "N","S","E","W".
    """
    parts = []
    for i in range(count):
        if orient == "N":
            cx = x + (i * w / max(count, 1)) + w / (2 * max(count, 1))
            pts = f"{cx - size/2},{y} {cx + size/2},{y} {cx},{y - size}"
        elif orient == "S":
            cx = x + (i * w / max(count, 1)) + w / (2 * max(count, 1))
            pts = f"{cx - size/2},{y} {cx + size/2},{y} {cx},{y + size}"
        elif orient == "E":
            cy = y + (i * w / max(count, 1)) + w / (2 * max(count, 1))
            pts = f"{x},{cy - size/2} {x},{cy + size/2} {x + size},{cy}"
        else:  # W
            cy = y + (i * w / max(count, 1)) + w / (2 * max(count, 1))
            pts = f"{x},{cy - size/2} {x},{cy + size/2} {x - size},{cy}"
    parts.append(f'<polygon points="{pts}" fill="{COLOR_WHITE}"/>')
    return "\n  ".join(parts)


def crosswalk_zebra(x, y, w, h, n=3, stripe_w=12, gap=4):
    """Crosswalk zebra stripes (vertical bars across a horizontal road)."""
    parts = []
    total = n * stripe_w + (n - 1) * gap
    start_x = x + (w - total) / 2
    for i in range(n):
        sx = start_x + i * (stripe_w + gap)
        parts.append(f'<rect x="{sx}" y="{y}" width="{stripe_w}" height="{h}" fill="{COLOR_WHITE}"/>')
    return "\n  ".join(parts)


def lane_edge(x, y, w, h, dashed=False, dash=20, gap=10):
    """White lane edge — solid or dashed."""
    if not dashed:
        return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{COLOR_WHITE}"/>'
    parts = []
    pos = 0
    while pos < w:
        dlen = min(dash, w - pos)
        parts.append(f'<rect x="{x + pos}" y="{y}" width="{dlen}" height="{h}" fill="{COLOR_WHITE}"/>')
        pos += dash + gap
    return "\n  ".join(parts)


def merge_taper(x1, y1, x2, y2, n=4):
    """Dashed white taper line along a diagonal for merge zones."""
    parts = []
    for i in range(n):
        t = i / max(n - 1, 1)
        mx = round(x1 + (x2 - x1) * t)
        my = round(y1 + (y2 - y1) * t)
        parts.append(f'<rect x="{mx - 2}" y="{my}" width="4" height="15" fill="{COLOR_WHITE}"/>')
    return "\n  ".join(parts)


# ═══════════════════════════════════════════════════════════════════
# Traffic Control
# ═══════════════════════════════════════════════════════════════════

_OCTAGON = "100,10 158,40 188,100 158,160 100,190 42,160 12,100 42,40"


def stop_sign(x, y, scale=0.15):
    """Stop sign — octagon with STOP text at the given position."""
    return (
        f'<g transform="translate({x},{y}) scale({scale})">'
        f'<polygon points="{_OCTAGON}" fill="{COLOR_STOP_SIGN}" '
        f'stroke="{COLOR_WHITE}" stroke-width="8"/>'
        f'</g>'
    )


def yield_tri(x, y, bw=16, bh=8, orient="N"):
    """Single yield triangle."""
    if orient == "N":
        pts = f"{x - bw/2},{y} {x + bw/2},{y} {x},{y - bh}"
    elif orient == "S":
        pts = f"{x - bw/2},{y} {x + bw/2},{y} {x},{y + bh}"
    elif orient == "E":
        pts = f"{x},{y - bw/2} {x},{y + bw/2} {x + bh},{y}"
    else:  # W
        pts = f"{x},{y - bw/2} {x},{y + bw/2} {x - bh},{y}"
    return f'<polygon points="{pts}" fill="{COLOR_WHITE}"/>'


def cone(x, y, w=8, h=16):
    """Traffic cone (trapezoid)."""
    return (
        f'<polygon points="{x},{y + h} {x + w},{y + h} '
        f'{x + w * 0.75},{y} {x + w * 0.25},{y}" fill="{COLOR_CONE}"/>'
    )


def hydrant(x, y, scale=1):
    """Fire hydrant — body + cap."""
    return (
        f'<g transform="translate({x},{y}) scale({scale})">'
        f'<rect x="-4" y="0" width="8" height="14" rx="1" fill="{COLOR_VEHICLE_DANGER}"/>'
        f'<rect x="-6" y="2" width="12" height="4" rx="1" fill="{COLOR_VEHICLE_DANGER}"/>'
        f'<circle cx="0" cy="-2" r="4" fill="{COLOR_YELLOW_LINE}"/>'
        f'</g>'
    )


def arrow_defs(marker_id="arr"):
    """SVG <defs> block with a white arrowhead marker."""
    return (
        f'<defs>'
        f'<marker id="{marker_id}" markerWidth="10" markerHeight="7" '
        f'refX="9" refY="3.5" orient="auto">'
        f'<polygon points="0 0, 10 3.5, 0 7" fill="{COLOR_WHITE}"/>'
        f'</marker>'
        f'</defs>'
    )


# ═══════════════════════════════════════════════════════════════════
# Vehicles
# ═══════════════════════════════════════════════════════════════════

def sedan(x, y, w=SEDAN_W, h=SEDAN_H, color=COLOR_VEHICLE_EGO, orient="E"):
    """Sedan body. Orient N/S swaps w/h so the car faces that direction."""
    if orient in ("N", "S"):
        return f'<rect x="{x}" y="{y}" width="{h}" height="{w}" rx="3" fill="{color}"/>'
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="3" fill="{color}"/>'


def wheels(x, y, vw, vh, orient="E", r=5):
    """Four wheels on a vehicle body at (x, y) with body size (vw, vh)."""
    if orient in ("N", "S"):
        vw, vh = vh, vw
    parts = []
    offx = vw * 0.2
    offy = vh + 0
    parts.append(f'<circle cx="{x + offx}" cy="{y + offy}" r="{r}" fill="{COLOR_WHEELS}"/>')
    parts.append(f'<circle cx="{x + vw - offx}" cy="{y + offy}" r="{r}" fill="{COLOR_WHEELS}"/>')
    return "\n  ".join(parts)


def emergency(x, y, w=EMERGENCY_W, h=EMERGENCY_H, orient="E"):
    """Emergency vehicle — red body + lightbar + wheels."""
    if orient in ("N", "S"):
        bw, bh = h, w
    else:
        bw, bh = w, h
    parts = [
        f'<rect x="{x}" y="{y}" width="{bw}" height="{bh}" rx="3" '
        f'fill="{COLOR_VEHICLE_DANGER}" stroke="{COLOR_BLACK}" stroke-width="2"/>',
    ]
    # Lightbar
    if orient == "E" or orient == "W":
        lx, ly = _r(x + bw * 0.1), y - 6
        lw = _r(bw * 0.8)
        parts.append(f'<rect x="{lx}" y="{ly}" width="{lw}" height="6" rx="2" fill="{COLOR_WHITE}"/>')
        parts.append(f'<circle cx="{_r(lx + lw * 0.2)}" cy="{ly + 3}" r="4" fill="{COLOR_RED_LIGHT}" opacity="0.9"/>')
        parts.append(f'<circle cx="{_r(lx + lw * 0.5)}" cy="{ly + 3}" r="4" fill="{COLOR_BLUE_LIGHT}" opacity="0.9"/>')
        parts.append(f'<circle cx="{_r(lx + lw * 0.8)}" cy="{ly + 3}" r="4" fill="{COLOR_RED_LIGHT}" opacity="0.9"/>')
    else:
        lx, ly = x - 6, _r(y + bh * 0.1)
        lh = _r(bh * 0.8)
        parts.append(f'<rect x="{lx}" y="{ly}" width="6" height="{lh}" rx="2" fill="{COLOR_WHITE}"/>')
        parts.append(f'<circle cx="{lx + 3}" cy="{_r(ly + lh * 0.2)}" r="4" fill="{COLOR_RED_LIGHT}" opacity="0.9"/>')
        parts.append(f'<circle cx="{lx + 3}" cy="{_r(ly + lh * 0.5)}" r="4" fill="{COLOR_BLUE_LIGHT}" opacity="0.9"/>')
        parts.append(f'<circle cx="{lx + 3}" cy="{_r(ly + lh * 0.8)}" r="4" fill="{COLOR_RED_LIGHT}" opacity="0.9"/>')
    # Wheels
    if orient in ("E", "W"):
        parts.append(f'<circle cx="{_r(x + bw * 0.2)}" cy="{y + bh}" r="5" fill="{COLOR_WHEELS}"/>')
        parts.append(f'<circle cx="{_r(x + bw * 0.8)}" cy="{y + bh}" r="5" fill="{COLOR_WHEELS}"/>')
    else:
        parts.append(f'<circle cx="{x + bw}" cy="{_r(y + bh * 0.2)}" r="5" fill="{COLOR_WHEELS}"/>')
        parts.append(f'<circle cx="{x + bw}" cy="{_r(y + bh * 0.8)}" r="5" fill="{COLOR_WHEELS}"/>')
    return "\n  ".join(parts)


def school_bus(x, y, w=BUS_W, h=BUS_H, orient="E"):
    """School bus with windows, stop arm, and flashing lights."""
    parts = [
        # Body
        f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="5" '
        f'fill="{COLOR_SCHOOL_BUS}" stroke="{COLOR_BLACK}" stroke-width="2"/>',
        # Windows
        f'<rect x="{_r(x + w * 0.12)}" y="{_r(y + h * 0.2)}" width="{_r(w * 0.22)}" height="{_r(h * 0.34)}" rx="2" fill="{COLOR_WINDOW}"/>',
        f'<rect x="{_r(x + w * 0.56)}" y="{_r(y + h * 0.2)}" width="{_r(w * 0.3)}" height="{_r(h * 0.34)}" rx="2" fill="{COLOR_WINDOW}"/>',
        # Wheels
        f'<circle cx="{_r(x + w * 0.18)}" cy="{y + h}" r="7" fill="{COLOR_WHEELS}"/>',
        f'<circle cx="{_r(x + w * 0.82)}" cy="{y + h}" r="7" fill="{COLOR_WHEELS}"/>',
        # Flashing lights on top
        f'<circle cx="{_r(x + w * 0.12)}" cy="{y - 5}" r="5" fill="{COLOR_RED_LIGHT}" opacity="0.8"/>',
        f'<circle cx="{_r(x + w * 0.88)}" cy="{y - 5}" r="5" fill="{COLOR_RED_LIGHT}" opacity="0.8"/>',
    ]
    # Stop arm (extended to the right of the bus)
    arm_x = x + w
    arm_y = _r(y + h * 0.2)
    parts.append(f'<rect x="{arm_x}" y="{arm_y}" width="20" height="16" rx="2" fill="{COLOR_STOP_SIGN}"/>')
    parts.append(
        f'<text x="{arm_x + 10}" y="{arm_y + 11}" '
        f'font-family="Arial" font-size="8" font-weight="900" '
        f'fill="{COLOR_WHITE}" text-anchor="middle">STOP</text>'
    )
    return "\n  ".join(parts)


def truck(x, y, w=TRUCK_W, h=TRUCK_H, orient="E"):
    """Truck — cab + trailer body + wheels."""
    cab_w = w * 0.3
    parts = [
        # Trailer
        f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="2" '
        f'fill="{COLOR_VEHICLE_OTHER}" stroke="{COLOR_BLACK}" stroke-width="2"/>',
        # Cab (slightly taller)
        f'<rect x="{_r(x + w - cab_w)}" y="{y - 2}" width="{_r(cab_w)}" height="{h + 2}" rx="2" '
        f'fill="{COLOR_VEHICLE_OTHER}" stroke="{COLOR_BLACK}" stroke-width="2"/>',
        # Wheels
        f'<circle cx="{_r(x + w * 0.2)}" cy="{y + h}" r="5" fill="{COLOR_WHEELS}"/>',
        f'<circle cx="{_r(x + w * 0.8)}" cy="{y + h}" r="5" fill="{COLOR_WHEELS}"/>',
    ]
    return "\n  ".join(parts)


def compact(x, y, w=COMPACT_W, h=COMPACT_H, color=COLOR_VEHICLE_OTHER, orient="E"):
    """Compact car — smaller sedan variant."""
    if orient in ("N", "S"):
        return f'<rect x="{x}" y="{y}" width="{h}" height="{w}" rx="3" fill="{color}"/>'
    return f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="3" fill="{color}"/>'


# ═══════════════════════════════════════════════════════════════════
# People & Shapes
# ═══════════════════════════════════════════════════════════════════

def pedestrian(x, y, scale=1):
    """Stick-figure pedestrian centered at (x, y-top-of-head)."""
    return (
        f'<g fill="{COLOR_PEDESTRIAN}" stroke="{COLOR_BLACK}" stroke-width="2" '
        f'transform="translate({x},{y}) scale({scale})">'
        f'<circle cx="0" cy="0" r="6"/>'
        f'<line x1="0" y1="6" x2="0" y2="22" stroke-width="4"/>'
        f'<line x1="-8" y1="12" x2="8" y2="12" stroke-width="3"/>'
        f'<line x1="0" y1="22" x2="-6" y2="34" stroke-width="4"/>'
        f'<line x1="0" y1="22" x2="6" y2="34" stroke-width="4"/>'
        f'</g>'
    )


def hill_slope(w, h, direction="right"):
    """Diagonal slope shape."""
    if direction == "right":
        d = f"M 0,{h} L {w},0 L {w},{h} Z"
    else:
        d = f"M 0,0 L {w},{h} L 0,{h} Z"
    return f'<path d="{d}" fill="{COLOR_CURB}"/>'


def roundabout_road(cx, cy, r_out=60, r_in=35):
    """Roundabout — circular road ring + grass center island."""
    sw = r_out - r_in
    r_mid = (r_out + r_in) / 2
    return (
        f'<circle cx="{cx}" cy="{cy}" r="{r_mid}" fill="none" '
        f'stroke="{COLOR_ROAD}" stroke-width="{sw}"/>\n'
        f'  <circle cx="{cx}" cy="{cy}" r="{r_in}" fill="{COLOR_GRASS}"/>'
    )


def merge_ramp(x1, y1, x2, y2, cx, cy):
    """Curved entry ramp (quadratic Bezier)."""
    return f'<path d="M {x1},{y1} Q {cx},{cy} {x2},{y2}" fill="{COLOR_ROAD}"/>'


# ═══════════════════════════════════════════════════════════════════
# Trajectory
# ═══════════════════════════════════════════════════════════════════

def traj_arrow(x1, y1, x2, y2, marker_id="arr", sw=3):
    """Straight trajectory arrow with arrowhead marker."""
    return (
        f'<path d="M {x1},{y1} L {x2},{y2}" fill="none" '
        f'stroke="{COLOR_WHITE}" stroke-width="{sw}" '
        f'marker-end="url(#{marker_id})"/>'
    )


def traj_curve(x1, y1, cx, cy, x2, y2, marker_id="arr", sw=3):
    """Curved trajectory arrow (quadratic Bezier)."""
    return (
        f'<path d="M {x1},{y1} Q {cx},{cy} {x2},{y2}" fill="none" '
        f'stroke="{COLOR_WHITE}" stroke-width="{sw}" '
        f'marker-end="url(#{marker_id})"/>'
    )
