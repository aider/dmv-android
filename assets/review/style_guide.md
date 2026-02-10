# SVG Asset Style Guide

Standards for all SVG assets in the DMV Texas quiz app. These rules ensure consistent rendering at 96dp target size on Android.

## ViewBox Standards

| Category | ViewBox | Example |
|---|---|---|
| MUTCD regulatory signs (R*) | 0 0 200 200 | MUTCD_R1-1_STOP |
| MUTCD warning signs (W*) | 0 0 200 200 | MUTCD_W1-1_CURVE_RIGHT |
| MUTCD school signs (S*) | 0 0 200 200 | MUTCD_S1-1_SCHOOL_CROSSING |
| MUTCD guide signs (D*) | 0 0 200 150 | MUTCD_D1-1_EXIT_SIGN |
| MUTCD route markers (M*, I*) | 0 0 200 200 | MUTCD_I-5_INTERSTATE_35 |
| Traffic signals (3-light) | 0 0 100 250 | SIGNAL_SOLID_RED |
| Pedestrian signals | 0 0 100 150 | SIGNAL_PED_WALK |
| Intersection scenarios | 0 0 200 200 | INTERSECTION_4WAY_STOP |
| Pavement markings | 0 0 300 200 | PAVEMENT_SOLID_WHITE_LINE |
| Speed/distance diagrams | 0 0 300 200 | SPEED_STOPPING_DISTANCE |
| Parking scenarios | 0 0 200 200 | PARKING_PARALLEL_STEPS |
| Safety diagrams | 0 0 200 200 | SAFE_BLIND_SPOT_CHECK |
| Hand signals | 0 0 200 200 | MARKING_HAND_SIGNAL_LEFT |

## Color Palette

| Element | Hex | Usage |
|---|---|---|
| Road surface | #4A4A4A | All paved roads and lanes |
| Grass / off-road | #88AA88 | Shoulders, medians, backgrounds |
| Sky | #87CEEB | Sky backgrounds in scenes |
| Yellow center line | #FFCC00 | Center line markings |
| White lane marking | #FFFFFF | Lane lines, stop lines, crosswalks |
| Ego vehicle | #3366CC | The "your car" vehicle in scenarios |
| Other vehicle | #666666 | Other cars in scenarios |
| Emergency vehicle | #CC0000 | Emergency vehicles |
| School bus | #FFB800 | School buses |
| Skin tone | #FFE0BD | Human figures (hands, faces) |
| Construction zone | #FF6600 | Work zone cones, vests, signs |

### Traffic Signal Colors

| State | Base Fill | Glow Overlay |
|---|---|---|
| Red (active) | #C1272D | #FF4444 opacity 0.6 |
| Yellow (active) | #FFCC00 | #FFEE44 opacity 0.6 |
| Green (active) | #00B140 | #44FF44 opacity 0.6 |
| Inactive light | #444444 | none |
| Signal housing | #1A1A1A | none |

## Stroke Widths (Minimums)

All measurements assume rendering at 96dp on Android.

| ViewBox | Min Stroke | Renders At | Use For |
|---|---|---|---|
| 200px wide | 6px | ~2.9dp | Stop lines, lane markings |
| 300px wide | 8px | ~2.6dp | Lane markings, distance bars |
| 100px wide | 3px | ~2.9dp | Signal details |

## Text Sizes (Minimums)

| ViewBox | Min Font | Renders At | Use For |
|---|---|---|---|
| 200px | 18px | ~8.6dp | Labels, step numbers |
| 300px | 24px | ~7.7dp | Scene labels, descriptions |
| 100px | 9px | ~8.6dp | Signal text (minimal) |

Font: `font-family="Arial"` for all text.
Weight: `font-weight="700"` or `font-weight="900"` for readability.

## Padding Standards

| ViewBox | Padding | Content Area |
|---|---|---|
| 200x200 (signs) | 10px all sides | 180x180 |
| 200x200 (scenarios) | 5px all sides | 190x190 |
| 300x200 | 10px horizontal, 5px vertical | 280x190 |
| 100x250 (signals) | 8px horizontal | 84x230 |

## Effects Policy

- **NO shadows**: No `opacity="0.15"` shadow elements
- **NO texture overlays**: No `opacity="0.05"` texture rectangles
- **Glow overlays OK**: Traffic signal active light overlays (`opacity="0.6"`) serve functional purpose
- **Functional opacity OK**: Blind spot zones, mirror cones, space cushions
- **Flashing indicators**: Use radiating lines (stroke-width="3") around active light

## File Naming

Format: `CATEGORY_DESCRIPTIVE_NAME.svg`

Categories: `MUTCD_`, `SIGNAL_`, `INTERSECTION_`, `PAVEMENT_`, `SPEED_`, `PARKING_`, `SAFE_`, `SPECIAL_`, `MARKING_`

## Manifest Format

```json
{
  "assetId": "CATEGORY_DESCRIPTIVE_NAME",
  "description": "Brief human-readable description",
  "file": "assets/svg/CATEGORY_DESCRIPTIVE_NAME.svg",
  "sourceUrl": "generated",
  "license": "generated"
}
```

## New Asset Checklist

1. ViewBox matches category standard
2. All colors from palette
3. Stroke widths meet minimums
4. Text sizes meet minimums
5. No shadows or texture overlays
6. Content within viewBox bounds (no clipping)
7. Manifest entry added
8. Renders clearly at 96dp width in browser
9. Recognizable at 48dp (degraded but identifiable)
