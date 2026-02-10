# SVG Component Library

Python-based generator that produces standalone SVG files from reusable component functions.
Each SVG is self-contained (no `<symbol>`/`<use>`) for Coil 3 compatibility on Android.

## Quick Start

```bash
cd assets/components
python3 generate.py
```

Generates 8 `INTERSECTION_*.svg` files into `assets/svg/`.

## Architecture

```
style_tokens.py    # Colors, stroke widths, vehicle sizes
primitives.py      # 27 reusable component functions
scenes_intersection.py  # 8 scene compositions
generate.py        # CLI entry point
```

**Flow:** `style_tokens` -> `primitives` -> `scenes_*` -> `generate.py` -> `.svg` files

## Style Tokens

### Colors
| Token | Hex | Usage |
|-------|-----|-------|
| `COLOR_GRASS` | `#88AA88` | Background |
| `COLOR_ROAD` | `#4A4A4A` | Road surface |
| `COLOR_YELLOW_LINE` | `#FFCC00` | Center lines |
| `COLOR_WHITE` | `#FFFFFF` | Stop lines, lane edges, crosswalks |
| `COLOR_CURB` | `#888888` | Curb edges |
| `COLOR_VEHICLE_EGO` | `#3366CC` | "Your" car (blue) |
| `COLOR_VEHICLE_OTHER` | `#666666` | Neutral traffic (gray) |
| `COLOR_VEHICLE_DANGER` | `#CC0000` | Hazard / must-yield (red) |
| `COLOR_WHEELS` | `#1A1A1A` | Vehicle wheels |
| `COLOR_STOP_SIGN` | `#C1272D` | Stop sign red |
| `COLOR_SCHOOL_BUS` | `#FFB800` | School bus yellow |
| `COLOR_CONE` | `#FF6600` | Traffic cone orange |
| `COLOR_PEDESTRIAN` | `#FFCC00` | Pedestrian figure |
| `COLOR_BLACK` | `#000000` | Outlines |
| `COLOR_WINDOW` | `#87CEEB` | Vehicle windows |
| `COLOR_RED_LIGHT` | `#FF0000` | Emergency/flashing lights |
| `COLOR_BLUE_LIGHT` | `#0000FF` | Emergency lights |

### Stroke Widths
| Token | px | Usage |
|-------|-----|-------|
| `STROKE_THIN` | 2 | Fine detail |
| `STROKE_MEDIUM` | 4 | Standard lines |
| `STROKE_THICK` | 6 | Stop lines (200px viewBox) |
| `STROKE_HEAVY` | 8 | Stop lines (300px viewBox), emphasis |

### Vehicle Sizes (200px viewBox)
| Token | W x H | Usage |
|-------|-------|-------|
| `SEDAN` | 30 x 18 | Standard car |
| `COMPACT` | 25 x 16 | Small car |
| `BUS` | 80 x 35 | School bus |
| `EMERGENCY` | 45 x 22 | Emergency vehicle |
| `TRUCK` | 50 x 25 | Truck |

## Component Catalog (27 functions)

### Backgrounds & Surfaces (4)
| # | Function | Anchor Point | Returns |
|---|----------|-------------|---------|
| 1 | `grass_bg(w, h)` | (0,0) top-left | Full-canvas green rect |
| 2 | `road_h(x, y, w, h)` | (x,y) top-left | Horizontal road strip |
| 3 | `road_v(x, y, w, h)` | (x,y) top-left | Vertical road strip |
| 4 | `curb(x, y, w, h)` | (x,y) top-left | Curb edge rect |

### Lane Markings (7)
| # | Function | Anchor Point | Returns |
|---|----------|-------------|---------|
| 5 | `yellow_solid(x, y, w, h)` | (x,y) top-left | Solid yellow center line |
| 6 | `yellow_dashed(x, y, length, orient, dash, gap, thickness)` | (x,y) start | N dashed yellow rects |
| 7 | `stop_line(x, y, w, h=6)` | (x,y) top-left | White stop line (min 6px) |
| 8 | `yield_triangles(x, y, w, orient, count, size)` | (x,y) baseline | White yield triangles |
| 9 | `crosswalk_zebra(x, y, w, h, n=3, stripe_w, gap)` | (x,y) top-left of zone | N white stripes centered |
| 10 | `lane_edge(x, y, w, h, dashed, dash, gap)` | (x,y) top-left | Solid or dashed white edge |
| 11 | `merge_taper(x1, y1, x2, y2, n=4)` | (x1,y1) to (x2,y2) | N dashes along diagonal |

### Traffic Control (5)
| # | Function | Anchor Point | Returns |
|---|----------|-------------|---------|
| 12 | `stop_sign(x, y, scale=0.15)` | (x,y) translate origin | Octagon with STOP |
| 13 | `yield_tri(x, y, bw, bh, orient)` | (x,y) center baseline | Single yield triangle |
| 14 | `cone(x, y, w=8, h=16)` | (x,y) top-left | Orange trapezoid |
| 15 | `hydrant(x, y, scale=1)` | (x,y) center-top | Red body + yellow cap |
| 16 | `arrow_defs(marker_id="arr")` | N/A | `<defs>` with arrowhead marker |

### Vehicles (6)
| # | Function | Anchor Point | Returns |
|---|----------|-------------|---------|
| 17 | `sedan(x, y, w, h, color, orient)` | (x,y) top-left | Rounded rect body |
| 18 | `wheels(x, y, vw, vh, orient, r)` | (x,y) = vehicle (x,y) | 2 circles at bottom |
| 19 | `emergency(x, y, w, h, orient)` | (x,y) top-left | Red body + lightbar + wheels |
| 20 | `school_bus(x, y, w, h, orient)` | (x,y) top-left | Yellow body + windows + stop arm + wheels |
| 21 | `truck(x, y, w, h, orient)` | (x,y) top-left | Cab + trailer + wheels |
| 22 | `compact(x, y, w, h, color, orient)` | (x,y) top-left | Smaller sedan variant |

### People & Shapes (4)
| # | Function | Anchor Point | Returns |
|---|----------|-------------|---------|
| 23 | `pedestrian(x, y, scale)` | (x,y) head center | Stick figure |
| 24 | `hill_slope(w, h, direction)` | (0,0) | Diagonal triangle |
| 25 | `roundabout_road(cx, cy, r_out, r_in)` | (cx,cy) center | Two circles (road ring + island) |
| 26 | `merge_ramp(x1, y1, x2, y2, cx, cy)` | Start to end | Bezier curve road |

### Trajectory (1)
| # | Function | Anchor Point | Returns |
|---|----------|-------------|---------|
| 27 | `traj_arrow(x1, y1, x2, y2, marker_id, sw)` | (x1,y1) to (x2,y2) | White path with arrowhead |
| +  | `traj_curve(x1, y1, cx, cy, x2, y2, ...)` | Start via control to end | Curved trajectory arrow |

## Generated Scenes

| File | viewBox | Learning Cues |
|------|---------|---------------|
| `INTERSECTION_4WAY_STOP.svg` | 200x200 | Stop lines, signs, vehicles, arrows |
| `INTERSECTION_T_STOP.svg` | 200x200 | Stop line, sign, vehicles, arrows |
| `INTERSECTION_UNCONTROLLED.svg` | 200x200 | Center lines, vehicles, arrows (no controls) |
| `INTERSECTION_ROUNDABOUT.svg` | 200x200 | Yield triangles, arrows, vehicles |
| `INTERSECTION_PEDESTRIAN_CROSSWALK.svg` | 200x200 | Stop line, crosswalk, pedestrian, vehicle |
| `INTERSECTION_EMERGENCY_VEHICLE.svg` | 200x200 | Emergency vehicle, yielding cars, arrow |
| `INTERSECTION_SCHOOL_BUS_STOPPED.svg` | 300x200 | Bus + stop arm, stopped vehicles, center line |
| `INTERSECTION_MERGE_HIGHWAY.svg` | 300x200 | Ramp, taper dashes, vehicles, arrow |

## Acceptance Checks

Per generated SVG:
- [ ] viewBox correct (200x200 or 300x200)
- [ ] No clipping — all content inside viewBox with 5% padding
- [ ] Readable at 96dp — strokes >= 6px (200vb) / 8px (300vb)
- [ ] Recognizable at 48dp — key shapes distinguishable
- [ ] >= 2 learning cues (stop/yield line, markings, arrows, vehicles/pedestrian)
- [ ] All `marker-end` refs resolve to defined `<marker>` in `<defs>`
- [ ] Colors match `style_tokens.py` exactly
- [ ] File size < 3KB
- [ ] Valid XML (no unclosed tags)
- [ ] Coordinates are integers or single-decimal (no long floats)

## Adding a New Scene

1. **Define the scene function** in the appropriate `scenes_*.py` file:
   ```python
   def scene_my_new_scene():
       return _svg(200, 200,
           grass_bg(200, 200),
           road_h(0, 70, 200, 60),
           # ... compose from primitives
           defs=arrow_defs(),
       )
   ```

2. **Register in `ALL_SCENES`** at the bottom of the file:
   ```python
   ALL_SCENES = {
       ...
       "MY_NEW_SCENE.svg": scene_my_new_scene(),
   }
   ```

3. **Run the generator:**
   ```bash
   python3 generate.py
   ```

4. **Verify:** Open the SVG in a browser, check at 96px and 48px width.

5. **Add new primitives** to `primitives.py` if the scene needs elements not yet in the library.

## Adding New Primitives

1. Add the function to `primitives.py` in the appropriate section
2. Use `_r()` to round computed coordinates
3. Use colors/sizes from `style_tokens.py` — never hardcode
4. Return an SVG fragment string (no root `<svg>` tag)
5. Document the anchor point convention in the docstring

## Future Expansion

Scene files to create for remaining asset categories:
- `scenes_speed.py` — SPEED_* scenes (highway, residential, following distance)
- `scenes_parking.py` — PARKING_* scenes (parallel, angle, hill)
- `scenes_safe.py` — SAFE_* scenes (following distance, blind spots)
- `scenes_special.py` — SPECIAL_* scenes (railroad, construction, school zone)
