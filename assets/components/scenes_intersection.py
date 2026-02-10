"""Scene compositions for INTERSECTION_* SVGs.

Each function returns a complete SVG string ready to write to disk.
All scenes use primitives from primitives.py and colors from style_tokens.py.
"""

from .primitives import *
from .style_tokens import *


def _svg(w, h, *parts, defs=""):
    """Wrap parts into a complete SVG document."""
    body = "\n  ".join(p for p in parts if p)
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}">\n'
        f'  {defs}\n'
        f'  {body}\n'
        f'</svg>\n'
    )


# ═══════════════════════════════════════════════════════════════════
# 1. INTERSECTION_4WAY_STOP  (200x200)
# ═══════════════════════════════════════════════════════════════════

def scene_4way_stop():
    return _svg(200, 200,
        grass_bg(200, 200),
        "<!-- Roads -->",
        road_h(0, 70, 200, 60),
        road_v(70, 0, 60, 200),
        "<!-- Center lines -->",
        yellow_solid(98, 70, 4, 60),
        yellow_solid(70, 98, 60, 4),
        "<!-- Stop lines (6px for visibility) -->",
        stop_line(0, 64, 70, 6),
        stop_line(130, 64, 70, 6),
        stop_line(64, 0, 6, 70),
        stop_line(64, 130, 6, 70),
        "<!-- Stop signs at corners -->",
        stop_sign(20, 35),
        stop_sign(155, 35),
        stop_sign(20, 150),
        stop_sign(155, 150),
        "<!-- Blue ego vehicle heading east -->",
        sedan(22, 75, color=COLOR_VEHICLE_EGO),
        traj_arrow(54, 84, 66, 84),
        "<!-- Gray vehicle heading north -->",
        sedan(75, 148, color=COLOR_VEHICLE_OTHER, orient="N"),
        traj_arrow(84, 146, 84, 134),
        defs=arrow_defs(),
    )


# ═══════════════════════════════════════════════════════════════════
# 2. INTERSECTION_T_STOP  (200x200)
# ═══════════════════════════════════════════════════════════════════

def scene_t_stop():
    return _svg(200, 200,
        grass_bg(200, 200),
        "<!-- Main road (horizontal) -->",
        road_h(0, 70, 200, 60),
        "<!-- Side road (vertical, from bottom) -->",
        road_v(70, 70, 60, 130),
        "<!-- Center lines -->",
        yellow_solid(98, 130, 4, 70),
        yellow_solid(0, 98, 70, 4),
        yellow_solid(130, 98, 70, 4),
        "<!-- Stop line on side road (6px) -->",
        stop_line(70, 128, 60, 6),
        "<!-- Stop sign -->",
        stop_sign(45, 140),
        "<!-- Blue ego on side road approaching stop -->",
        sedan(82, 160, color=COLOR_VEHICLE_EGO, orient="N"),
        traj_arrow(91, 158, 91, 138),
        "<!-- Gray vehicle on main road (right of way) -->",
        sedan(22, 75, color=COLOR_VEHICLE_OTHER),
        traj_arrow(54, 84, 66, 84),
        defs=arrow_defs(),
    )


# ═══════════════════════════════════════════════════════════════════
# 3. INTERSECTION_UNCONTROLLED  (200x200)
# ═══════════════════════════════════════════════════════════════════

def scene_uncontrolled():
    return _svg(200, 200,
        grass_bg(200, 200),
        road_h(0, 70, 200, 60),
        road_v(70, 0, 60, 200),
        "<!-- Center lines (NO stop lines — that's the lesson) -->",
        yellow_solid(98, 0, 4, 70),
        yellow_solid(98, 130, 4, 70),
        yellow_solid(0, 98, 70, 4),
        yellow_solid(130, 98, 70, 4),
        "<!-- Blue ego vehicle from west -->",
        sedan(22, 75, color=COLOR_VEHICLE_EGO),
        traj_arrow(54, 84, 66, 84),
        "<!-- Gray vehicle from north -->",
        sedan(75, 22, color=COLOR_VEHICLE_OTHER, orient="N"),
        traj_arrow(84, 54, 84, 66),
        defs=arrow_defs(),
    )


# ═══════════════════════════════════════════════════════════════════
# 4. INTERSECTION_ROUNDABOUT  (200x200)
# ═══════════════════════════════════════════════════════════════════

def scene_roundabout():
    return _svg(200, 200,
        grass_bg(200, 200),
        "<!-- Circular road -->",
        roundabout_road(100, 100, r_out=60, r_in=35),
        "<!-- Approach roads -->",
        road_v(90, 0, 20, 50),
        road_h(150, 90, 50, 20),
        road_v(90, 150, 20, 50),
        road_h(0, 90, 50, 20),
        "<!-- Yield triangles at entries -->",
        yield_tri(100, 50, orient="N"),
        yield_tri(150, 100, orient="E"),
        yield_tri(100, 150, orient="S"),
        yield_tri(50, 100, orient="W"),
        "<!-- Counterclockwise flow arrows -->",
        traj_curve(100, 30, 80, 40, 70, 55, marker_id="arrowhead"),
        traj_curve(170, 100, 160, 80, 145, 70, marker_id="arrowhead"),
        traj_curve(100, 170, 120, 160, 130, 145, marker_id="arrowhead"),
        traj_curve(30, 100, 40, 120, 55, 130, marker_id="arrowhead"),
        "<!-- Ego vehicle (blue) entering from south -->",
        sedan(88, 158, w=24, h=16, color=COLOR_VEHICLE_EGO),
        "<!-- Other vehicle (gray) in roundabout -->",
        sedan(132, 88, w=16, h=24, color=COLOR_VEHICLE_OTHER, orient="N"),
        defs=arrow_defs("arrowhead"),
    )


# ═══════════════════════════════════════════════════════════════════
# 5. INTERSECTION_PEDESTRIAN_CROSSWALK  (200x200)
# ═══════════════════════════════════════════════════════════════════

def scene_pedestrian_crosswalk():
    return _svg(200, 200,
        grass_bg(200, 200),
        road_h(0, 70, 200, 60),
        "<!-- Stop line -->",
        stop_line(0, 64, 70, 6),
        "<!-- Crosswalk zebra stripes -->",
        crosswalk_zebra(76, 70, 50, 60, n=3),
        "<!-- Pedestrian in crosswalk -->",
        pedestrian(100, 88),
        "<!-- Stopped vehicle behind stop line -->",
        sedan(20, 45, w=35, h=20, color=COLOR_VEHICLE_OTHER),
    )


# ═══════════════════════════════════════════════════════════════════
# 6. INTERSECTION_EMERGENCY_VEHICLE  (200x200)
# ═══════════════════════════════════════════════════════════════════

def scene_emergency_vehicle():
    return _svg(200, 200,
        grass_bg(200, 200),
        road_h(0, 70, 200, 60),
        road_v(70, 0, 60, 200),
        "<!-- Emergency vehicle approaching from east -->",
        emergency(140, 75, orient="E"),
        "<!-- Direction arrow (moving west) -->",
        traj_arrow(138, 86, 134, 86),
        "<!-- Yielding vehicles pulled to curb -->",
        compact(20, 78, color=COLOR_VEHICLE_OTHER),
        compact(78, 15, color=COLOR_VEHICLE_OTHER, orient="N"),
        defs=arrow_defs(),
    )


# ═══════════════════════════════════════════════════════════════════
# 7. INTERSECTION_SCHOOL_BUS_STOPPED  (300x200)
# ═══════════════════════════════════════════════════════════════════

def scene_school_bus_stopped():
    return _svg(300, 200,
        grass_bg(300, 200),
        road_h(0, 80, 300, 40),
        "<!-- Yellow center line (dashed) -->",
        yellow_dashed(0, 98, 100, orient="H"),
        yellow_dashed(200, 98, 100, orient="H"),
        "<!-- School bus with stop arm -->",
        school_bus(100, 75),
        "<!-- Stopped vehicle behind bus -->",
        sedan(55, 83, w=30, h=16, color=COLOR_VEHICLE_OTHER),
        "<!-- Stopped vehicle on opposite side -->",
        sedan(210, 83, w=30, h=16, color=COLOR_VEHICLE_OTHER),
    )


# ═══════════════════════════════════════════════════════════════════
# 8. INTERSECTION_MERGE_HIGHWAY  (300x200)
# ═══════════════════════════════════════════════════════════════════

def scene_merge_highway():
    return _svg(300, 200,
        grass_bg(300, 200),
        "<!-- Main highway (2 lanes) -->",
        road_h(0, 60, 300, 80),
        yellow_solid(0, 98, 300, 4),
        "<!-- Entrance ramp -->",
        merge_ramp(0, 160, 180, 100, 100, 160),
        "<!-- Dashed merge taper -->",
        merge_taper(50, 130, 140, 102, n=4),
        "<!-- Vehicle on ramp (red = must yield) -->",
        compact(40, 148, w=20, h=12, color=COLOR_VEHICLE_DANGER),
        "<!-- Vehicle on highway -->",
        sedan(220, 68, color=COLOR_VEHICLE_OTHER),
        "<!-- Merge trajectory arrow -->",
        traj_arrow(62, 142, 100, 110, marker_id="arr"),
        defs=arrow_defs(),
    )


# ═══════════════════════════════════════════════════════════════════
# Registry — maps filename → SVG content
# ═══════════════════════════════════════════════════════════════════

ALL_SCENES = {
    "INTERSECTION_4WAY_STOP.svg":           scene_4way_stop(),
    "INTERSECTION_T_STOP.svg":              scene_t_stop(),
    "INTERSECTION_UNCONTROLLED.svg":        scene_uncontrolled(),
    "INTERSECTION_ROUNDABOUT.svg":          scene_roundabout(),
    "INTERSECTION_PEDESTRIAN_CROSSWALK.svg": scene_pedestrian_crosswalk(),
    "INTERSECTION_EMERGENCY_VEHICLE.svg":   scene_emergency_vehicle(),
    "INTERSECTION_SCHOOL_BUS_STOPPED.svg":  scene_school_bus_stopped(),
    "INTERSECTION_MERGE_HIGHWAY.svg":       scene_merge_highway(),
}
