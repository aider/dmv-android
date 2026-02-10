"""Design tokens for the SVG component library.

All colors, stroke widths, and size constants used across components.
Changing a value here regenerates every scene that references it.
"""

# ── Colors ──────────────────────────────────────────────────────────
COLOR_GRASS        = "#88AA88"
COLOR_ROAD         = "#4A4A4A"
COLOR_YELLOW_LINE  = "#FFCC00"
COLOR_WHITE        = "#FFFFFF"
COLOR_CURB         = "#888888"

COLOR_VEHICLE_EGO    = "#3366CC"   # blue — "your" car
COLOR_VEHICLE_OTHER  = "#666666"   # gray — neutral traffic
COLOR_VEHICLE_DANGER = "#CC0000"   # red  — hazard / must-yield

COLOR_WHEELS       = "#1A1A1A"
COLOR_STOP_SIGN    = "#C1272D"
COLOR_SCHOOL_BUS   = "#FFB800"
COLOR_CONE         = "#FF6600"
COLOR_PEDESTRIAN   = "#FFCC00"
COLOR_BLACK        = "#000000"
COLOR_WINDOW       = "#87CEEB"
COLOR_RED_LIGHT    = "#FF0000"
COLOR_BLUE_LIGHT   = "#0000FF"

# ── Stroke widths (minimum for mobile readability) ──────────────────
STROKE_THIN   = 2    # fine detail (200px viewBox)
STROKE_MEDIUM = 4    # standard lines
STROKE_THICK  = 6    # stop lines in 200px viewBox
STROKE_HEAVY  = 8    # stop lines in 300px viewBox, emphasis

# ── Vehicle sizes (200px viewBox base) ──────────────────────────────
SEDAN_W, SEDAN_H           = 30, 18
COMPACT_W, COMPACT_H       = 25, 16
BUS_W, BUS_H               = 80, 35
EMERGENCY_W, EMERGENCY_H   = 45, 22
TRUCK_W, TRUCK_H           = 50, 25
