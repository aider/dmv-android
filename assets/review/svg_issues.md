# SVG Asset Issues - Complete Backlog

**Generated:** 2026-02-09 (UPDATED with MUTCD Geometry Compliance)
**Review Rules:** `assets/review/svg_review_rules.md` v1.0
**Total Issues:** 23 (P0: 6, P1: 12, P2: 5)

---

## P0 ISSUES (CRITICAL - BLOCKING)

These issues violate hard-fail criteria from svg_review_rules.md and MUST be fixed immediately.

---

## ISSUE #NEW-01: STOP Sign is NOT a True Regular Octagon

**Priority:** P0
**Category:** regulatory
**Assets:** MUTCD_R1-1_STOP.svg
**Used by:** 2 questions (TX-ROW-0007, TX-SIG-0001)

### Problem
The STOP sign polygon is NOT a true regular octagon. Measured geometry:
- Internal angles alternate: 125.3°, 143.9°, 126.9°, 143.9° (repeating pattern)
- Expected: all angles = 135.0° for regular octagon
- Radii from center: vary from 83.45px to 90.00px (6.55px variation)
- Expected: all radii equal
- Side lengths: vary from 65.30px to 67.08px (1.78px variation)
- Expected: all sides equal

Current polygon points: `100,10 158,40 188,100 158,160 100,190 42,160 12,100 42,40`

### Why it matters
Per svg_review_rules.md Section 2: "STOP Sign - Shape: True regular octagon — 8 equal sides, internal angles = 135 degrees each." The STOP sign is the most recognizable regulatory sign in driver education. Incorrect geometry fails MUTCD R1-1 compliance and undermines training accuracy.

### Acceptance criteria
- [ ] Shape is true regular octagon with 8 equal sides
- [ ] All internal angles = 135° ± 0.5°
- [ ] All vertices equidistant from center ± 0.5px
- [ ] All side lengths equal ± 0.5px
- [ ] Border present, uniform width on all 8 sides
- [ ] "STOP" text centered horizontally and vertically
- [ ] Minimum inner padding = 10% of sign diameter (20px for 200px viewBox)
- [ ] Readable at 96dp, recognizable at 48dp
- [ ] Valid XML
- [ ] File size < 3KB
- [ ] Colors match MUTCD: fill="#C1272D", stroke="#FFFFFF"

### Suggested fix approach
Replace hand-drawn polygon with mathematically correct regular octagon:

```python
# Regular octagon centered at (100, 100), radius 90
import math
vertices = []
for i in range(8):
    angle = i * (2 * math.pi / 8) - math.pi / 2  # Start at top, go clockwise
    x = 100 + 90 * math.cos(angle)
    y = 100 + 90 * math.sin(angle)
    vertices.append(f"{x:.1f},{y:.1f}")

points = " ".join(vertices)
# Result: "100.0,10.0 163.6,36.4 190.0,100.0 163.6,163.6 100.0,190.0 36.4,163.6 10.0,100.0 36.4,36.4"
```

Or use existing component library primitive if available.

### Reference
- MUTCD sign ID: R1-1
- svg_review_rules.md Section 2: STOP Sign geometry requirements

---

## ISSUE #NEW-02: Invalid XML - Duplicate font-weight Attributes

**Priority:** P0
**Category:** speed
**Assets:**
- SPEED_FOLLOWING_DISTANCE_3SEC.svg (used by 2 questions)
- SPEED_HIGHWAY_70MPH.svg (unused)
- SPEED_SCHOOL_ZONE_20MPH.svg (used by 1 question)
- SPEED_STOPPING_DISTANCE.svg (used by 1 question)

### Problem
Four SVG files contain duplicate `font-weight` attributes on `<text>` elements, resulting in invalid XML. Example from SPEED_FOLLOWING_DISTANCE_3SEC.svg:

```xml
<!-- Line 15 -->
<text ... font-size="28" font-weight="bold" font-weight="900" fill="#00FF00" ...>3 SEC</text>

<!-- Line 16 -->
<text ... font-size="24" font-weight="bold" font-weight="700" fill="#000000" ...>MINIMUM SAFE FOLLOWING DISTANCE</text>
```

XML validation error:
```
parser error : Attribute font-weight redefined
```

### Why it matters
These files fail XML validation and are not spec-compliant SVG. While some parsers (like browsers) may tolerate this by using the last value, it's non-standard and may cause rendering failures in strict parsers like Android's SVG decoder.

### Acceptance criteria
- [ ] No duplicate attributes on any element
- [ ] Valid XML (passes xmllint validation)
- [ ] Use only one font-weight per text element
- [ ] Prefer numeric values (700, 900) over keyword values (bold)
- [ ] Visual appearance unchanged after fix
- [ ] All 4 files fixed

### Suggested fix approach
For each `<text>` element with duplicate `font-weight`:
1. Remove `font-weight="bold"`
2. Keep the numeric value (`font-weight="900"` or `font-weight="700"`)

Example:
```xml
<!-- Before -->
<text font-weight="bold" font-weight="900" ...>

<!-- After -->
<text font-weight="900" ...>
```

Batch fix with sed:
```bash
for file in SPEED_FOLLOWING_DISTANCE_3SEC.svg SPEED_HIGHWAY_70MPH.svg \
            SPEED_SCHOOL_ZONE_20MPH.svg SPEED_STOPPING_DISTANCE.svg; do
  sed -i '' 's/font-weight="bold" font-weight="/font-weight="/g' "$file"
done
```

Verify with: `xmllint --noout assets/svg/SPEED_*.svg`

### Reference
- W3C SVG spec: attributes must be unique within an element
- svg_review_rules.md Section 8: Valid XML required

---

## ISSUE #NEW-03: DO NOT ENTER Uses Circle Instead of Square

**Priority:** P0
**Category:** regulatory
**Assets:** MUTCD_R5-1_DO_NOT_ENTER.svg
**Used by:** 1 question (TX-SIG-0005)

### Problem
The DO NOT ENTER sign uses a circle shape:
```xml
<circle cx="100" cy="100" r="90" fill="#C1272D"/>
```

Per MUTCD R5-1, the DO NOT ENTER sign should be a **square with rounded corners**, not a circle.

### Why it matters
Shape is a critical recognition element in sign identification training. The difference between a circle and a square affects how drivers categorize and respond to signs. MUTCD R5-1 explicitly specifies "square" shape.

### Acceptance criteria
- [ ] Shape is square with rounded corners (use `<rect>` with `rx` attribute)
- [ ] Aspect ratio 1:1 (square, not circle)
- [ ] White horizontal rectangle centered in sign
- [ ] White border around square exterior
- [ ] Red background (#C1272D)
- [ ] "DO NOT ENTER" text on white bar (or symbol-only variant)
- [ ] Geometry correct per MUTCD R5-1
- [ ] Border present, uniform width
- [ ] Text centered with >= 8% inner padding
- [ ] Readable at 96dp, recognizable at 48dp
- [ ] Valid XML
- [ ] Colors match MUTCD spec

### Suggested fix approach
Replace circle with square + rounded corners:

```xml
<!-- Before -->
<circle cx="100" cy="100" r="90" fill="#C1272D"/>

<!-- After -->
<rect x="10" y="10" width="180" height="180" rx="8" fill="#C1272D" stroke="#FFFFFF" stroke-width="6"/>
```

Keep white bar and text as-is, but verify centering in new square shape.

### Reference
- MUTCD sign ID: R5-1
- svg_review_rules.md Section 2: DO NOT ENTER sign requirements
- Shape: Square with rounded corners, NOT circle

---

## ISSUE #NEW-04: Speed Limit Signs Padding Below 8% Minimum

**Priority:** P0
**Category:** regulatory
**Assets:**
- MUTCD_R2-1_SPEED_LIMIT_65.svg (used by 2 questions)
- MUTCD_R2-1_SPEED_LIMIT_70.svg (used by 2 questions)
- MUTCD_R2-1_SPEED_LIMIT_30.svg (used by 1 question)
- MUTCD_R7-8_NO_PARKING.svg (used by 2 questions)

### Problem
These signs have insufficient inner padding. Current measurements:

**ViewBox:** 0 0 150 200
**Outer rect:** x=5, y=5, width=140, height=190
**Inner content starts:** x=12, y=12

**Effective padding from viewBox edge:** 5px on all sides
**Padding ratio:** 5/150 = 3.3% (horizontal), 5/200 = 2.5% (vertical)

**Required:** Minimum 8% per svg_review_rules.md Section 3
**Expected:** 8% of 150px = 12px horizontal, 8% of 200px = 16px vertical

### Why it matters
Text and borders are too close to the sign edge. At small render sizes, this risks clipping, overlapping, or poor visual separation. Per review rules Section 3: "Minimum inner padding: 8% of the sign's primary dimension."

### Acceptance criteria
- [ ] Inner padding >= 8% of primary dimension
- [ ] For 150x200 viewBox: minimum 12px horizontal, 16px vertical
- [ ] Recommended: 10% = 15px horizontal, 20px vertical
- [ ] Text bounding box does not intersect border stroke
- [ ] Border follows sign shape precisely
- [ ] Text hierarchy preserved: SPEED/LIMIT smaller, number largest
- [ ] Consistent padding within speed limit sign family
- [ ] All 4 files fixed with same padding ratio
- [ ] Readable at 96dp, recognizable at 48dp

### Suggested fix approach
**Option 1:** Increase padding by adjusting outer rect (recommended):
```xml
<!-- Before -->
<rect x="5" y="5" width="140" height="190" rx="6" .../>

<!-- After (10% padding = 15px, 20px) -->
<rect x="15" y="20" width="120" height="160" rx="6" .../>
```

Adjust inner border and text positions proportionally.

**Option 2:** Expand viewBox and keep absolute positions:
```xml
<!-- Before -->
viewBox="0 0 150 200"

<!-- After -->
viewBox="0 0 180 240"
```
This keeps existing element positions but increases relative padding.

**Verification formula:**
```
horizontal_padding = (viewBox_width - rect_width) / 2
padding_ratio = horizontal_padding / viewBox_width
Must be >= 0.08 (8%)
```

### Reference
- MUTCD sign ID: R2-1 (Speed Limit)
- svg_review_rules.md Section 3: Padding Rules
- Minimum: 8%, Recommended: 10-12%

---

## ISSUE #NEW-05: School Speed Limit Bottom Text Overflow Risk

**Priority:** P0
**Category:** school
**Assets:** MUTCD_S4-3_SCHOOL_SPEED_LIMIT_20.svg
**Used by:** 3 questions (HIGH priority - most-used school sign)

### Problem
Bottom text "WHEN CHILDREN" (y=175, font-size=14) and "PRESENT" (y=190, font-size=14) are positioned too close to the bottom edge:

**ViewBox height:** 200px
**Text at y=190:** Only 10px from bottom (5% padding)
**Text at y=175:** Only 25px from bottom with 14px font height

**Required:** Minimum 8% of 200px = 16px from bottom edge
**Current:** ~5% (below minimum)

### Why it matters
At small render sizes, text near edges risks being cut off by container bounds or becoming unreadable due to poor spacing. This sign is used by 3 questions (tied for most-used), making it high-priority.

### Acceptance criteria
- [ ] Bottom text minimum 16px from bottom edge (8% of 200px)
- [ ] Recommended: 20px from bottom (10%)
- [ ] All text legible at 96dp render
- [ ] Text hierarchy: SCHOOL/SPEED/LIMIT smaller (20px), number largest (64px), conditions smallest but readable (14px min)
- [ ] Vertical spacing balanced across sign height
- [ ] No text overflow or clipping
- [ ] Valid XML
- [ ] Readable at 96dp

### Suggested fix approach
**Option 1:** Move bottom text up (simplest):
```xml
<!-- Before -->
<text y="175" font-size="14">WHEN CHILDREN</text>
<text y="190" font-size="14">PRESENT</text>

<!-- After -->
<text y="165" font-size="14">WHEN CHILDREN</text>
<text y="180" font-size="14">PRESENT</text>
```

**Option 2:** Reduce bottom text font size and adjust spacing:
```xml
<text y="170" font-size="12">WHEN CHILDREN</text>
<text y="183" font-size="12">PRESENT</text>
```

Verify 96dp legibility after change.

### Reference
- MUTCD sign ID: S4-3 (School Speed Limit)
- svg_review_rules.md Section 3: Minimum 8% inner padding
- Section 4: Text must be legible at 96dp

---

## ISSUE #NEW-06: STOP Sign Redundant Overlapping Text Elements

**Priority:** P0
**Category:** regulatory
**Assets:** MUTCD_R1-1_STOP.svg
**Used by:** 2 questions

### Problem
The STOP sign SVG contains redundant overlapping elements:
1. Main octagon polygon with fill
2. Inner border polygon (likely duplicate/redundant)
3. Text element

If both polygons exist with the same shape but different strokes/fills, this adds unnecessary complexity and file size.

**Note:** This is lower priority than #NEW-01 (geometry issue) but should be addressed in the same fix.

### Why it matters
Redundant elements increase file size and parsing overhead. The inner border can be achieved with a single polygon and appropriate stroke.

### Acceptance criteria
- [ ] Single octagon polygon for main shape
- [ ] Border achieved via stroke on single polygon OR separate inner polygon (not both redundantly)
- [ ] File size minimized
- [ ] Visual appearance matches MUTCD R1-1
- [ ] Valid SVG structure

### Suggested fix approach
Review after fixing #NEW-01 geometry. Use single polygon with double stroke technique or clean up redundant paths.

### Reference
- svg_review_rules.md Section 8: Clean, minimal SVG structure

---

## P1 ISSUES (IMPORTANT - DEGRADE EXPERIENCE)

These issues from the previous audit remain valid and should be fixed after P0 issues.

---

## ISSUE #1: Missing Training Cues in Intersection Scenarios

**Priority:** P1
**Category:** intersection
**Assets:**
- INTERSECTION_4WAY_STOP (used by 1 question)
- INTERSECTION_UNCONTROLLED (used by 1 question)
- INTERSECTION_T_STOP (used by 1 question)
- INTERSECTION_ROUNDABOUT (used by 1 question)
- INTERSECTION_EMERGENCY_VEHICLE (used by 2 questions)
- INTERSECTION_SCHOOL_BUS_STOPPED (used by 2 questions)

### Problem
Intersection scenarios lack sufficient visual training cues. Examples:
- INTERSECTION_4WAY_STOP: has stop lines and signs ✓, but NO vehicles or arrows to show "two vehicles arrive at same time" scenario
- INTERSECTION_ROUNDABOUT: basic structure ✓, but NO yield lines/triangles, incomplete directional arrows
- Most scenes: missing vehicles when scenario requires vehicle interaction

Per svg_review_rules.md Section 2 (quality checks): Training assets should have >= 2 learning cues.

### Why it matters
Right-of-way questions test complex spatial reasoning. Without visible vehicles, arrows, and proper markings, learners cannot visualize the scenarios being tested.

### Acceptance criteria
- [ ] At least 2 vehicles present when scenario involves vehicle interaction
- [ ] Vehicles as simple colored rectangles (avoid detail → noise at 96dp)
- [ ] Directional arrows showing intended vehicle paths
- [ ] Stop/yield lines present at appropriate approaches
- [ ] Yield triangles at roundabout entries
- [ ] All SVG marker references defined in `<defs>` (no broken url(#id))
- [ ] Consistent vehicle colors: blue for ego (#3366CC), gray for others (#666666)
- [ ] Readable at 96dp, recognizable at 48dp

### Suggested fix approach
For each asset:
1. Add 2-3 vehicle rectangles (30×18px or 18×30px depending on orientation)
2. Add directional arrow paths with proper markers
3. Verify yield lines/stop lines are present and visible (8px stroke minimum)
4. Define all markers in `<defs>` section

### Reference
- svg_review_rules.md Section 2 (quality): >= 2 learning cues for scenes
- Previous audit issue #1

---

## ISSUE #2: Embedded Text Readability at Mobile Sizes

**Priority:** P1
**Category:** readability
**Assets:** 20+ assets with embedded text

### Problem
Text with small font sizes (10-18px) renders at 3-6dp on 96dp canvas, below 10dp legibility threshold.

**Examples:**
- PARKING_PARALLEL_STEPS: 10px font → 4.8dp at 96dp (unreadable)
- SPEED_FOLLOWING_DISTANCE_3SEC: 18px, 14px fonts → 6-8dp (barely readable)
- SAFE_BLIND_SPOT_CHECK: 14px labels → ~6dp

Per svg_review_rules.md Section 4: "Text that becomes an unreadable blob at 96dp = hard fail."

### Why it matters
Mobile users cannot read text below 10dp. Creates frustration and fails accessibility.

### Acceptance criteria
- [ ] Essential text minimum 20px font in 200×200 viewBox (renders ~9.6dp at 96dp)
- [ ] Recommended: 24px+ for labels, 60px+ for numbers
- [ ] Instructional labels either removed or increased to >= 20px
- [ ] Step numbers replaced with larger symbols (30px+ diameter)
- [ ] Bold/black weights for contrast
- [ ] Test at 96px render: all text legible

### Suggested fix approach
**Option 1:** Increase font sizes:
- Small labels (10-14px) → 20-24px
- Medium labels (18px) → 24-28px
- Large numbers (already OK at 60px+)

**Option 2:** Remove instructional text, rely on question text

**Option 3:** Replace text with symbols where appropriate

### Reference
- svg_review_rules.md Section 4: Mobile Readability Rules
- Previous audit issue #2

---

## ISSUE #3: Inconsistent ViewBox Dimensions Across Categories

**Priority:** P1
**Category:** consistency
**Assets:** 49 MUTCD signs with 5 different viewBox sizes

### Problem
**MUTCD Signs:** 5 different viewBox sizes:
- 0 0 200 200 (27 signs)
- 0 0 150 200 (11 signs)
- 0 0 150 150 (5 signs)
- 0 0 200 100 (4 signs)
- 0 0 120 150 (2 signs)

**SIGNAL Assets:** Two ratios:
- 0 0 100 250 (10 signals)
- 0 0 100 150 (3 signals)

Causes visual "jumping" when signs appear in sequence in UI.

### Why it matters
Inconsistent viewBox sizes cause visual discontinuity. Signs of the same family should have consistent aspect ratios for professional appearance.

### Acceptance criteria
- [ ] All square regulatory signs: 0 0 200 200
- [ ] All portrait rectangular signs: 0 0 150 200
- [ ] All landscape rectangular signs: 0 0 200 100
- [ ] All warning diamonds: 0 0 200 200
- [ ] All vertical signals: 0 0 100 250
- [ ] Consistent padding within each category
- [ ] Visual appearance preserved after normalization

### Suggested fix approach
1. Group signs by aspect ratio (square, portrait 3:4, landscape 2:1)
2. Choose standard viewBox for each group
3. Scale/reposition elements to fit new viewBox
4. Verify padding ratios remain consistent

### Reference
- svg_review_rules.md Section 3: Consistent padding within families
- Previous audit issue #3

---

## ISSUE #6: Thin Stroke Widths Disappear at Mobile

**Priority:** P1
**Category:** readability
**Assets:** PAVEMENT_*, INTERSECTION_* (~40 assets)

### Problem
Lane markings and stop lines use 4px stroke widths which render at ~1.28-1.9dp at 96dp, borderline invisible.

**Examples:**
- PAVEMENT_DOUBLE_YELLOW_SOLID: center lines 4px stroke
- INTERSECTION_* stop lines: 4px stroke

Per svg_review_rules.md Section 4: "Any stroke that vanishes entirely at 96dp render = hard fail."

### Why it matters
Critical road markings must be clearly visible for training effectiveness.

### Acceptance criteria
- [ ] Lane markings: minimum 8px stroke in 300×200 viewBox (2.5dp at 96dp)
- [ ] Stop lines: minimum 8-12px stroke (3-4dp at 96dp)
- [ ] Sign borders: minimum 6px stroke (2.9dp at 96dp)
- [ ] Border visible at 96dp
- [ ] Strokes do not vanish at 48dp
- [ ] All ~40 affected files updated

### Suggested fix approach
Batch update stroke-width values:
```bash
# Find all stroke-width < 6
grep -l 'stroke-width="[1-5]"' assets/svg/PAVEMENT_*.svg assets/svg/INTERSECTION_*.svg

# Update to 8px for lane markings
sed -i '' 's/stroke-width="4"/stroke-width="8"/g' assets/svg/PAVEMENT_*.svg
```

Verify visually at 96px render.

### Reference
- svg_review_rules.md Section 4: Minimum stroke widths
- Previous audit issue #6

---

## ISSUE #7: Undefined Marker References Break Arrows

**Priority:** P1
**Category:** correctness
**Assets:** INTERSECTION_ROUNDABOUT, SAFE_BLIND_SPOT_CHECK (possibly others)

### Problem
SVGs reference markers via `marker-end="url(#arrowhead)"` but the `<marker id="arrowhead">` is not defined in `<defs>`, causing broken arrow rendering.

### Why it matters
Broken references cause rendering failures. Arrows are critical training cues for direction and flow.

### Acceptance criteria
- [ ] All `marker-end`, `marker-start`, `marker-mid` references resolve to defined `<marker>` in `<defs>`
- [ ] Marker definitions present in each SVG that uses them (not external)
- [ ] Arrows render correctly at all sizes
- [ ] Valid XML (no broken url() references)

### Suggested fix approach
1. Find all `marker-end` references: `grep -r 'marker-' assets/svg/`
2. For each file, verify corresponding `<marker>` exists in `<defs>`
3. Add missing marker definitions or fix id mismatches

**Example marker definition:**
```xml
<defs>
  <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
    <polygon points="0 0, 10 3.5, 0 7" fill="#FFFFFF"/>
  </marker>
</defs>
```

### Reference
- svg_review_rules.md Section 8: All url(#id) references must resolve
- Previous audit issue #7

---

## ISSUE #8: Inconsistent Color Palette

**Priority:** P1
**Category:** consistency
**Assets:** ~40 assets with road surfaces, grass, vehicles

### Problem
Road surface colors vary: #4A4A4A, #3A3A3A, #5A5A5A
Grass colors vary: #88AA88, #90B090, #80A080
Vehicle colors inconsistent

### Why it matters
Professional appearance requires consistent color palette. Helps users recognize scene elements.

### Acceptance criteria
- [ ] Road surface: #4A4A4A everywhere
- [ ] Grass/off-road: #88AA88 everywhere
- [ ] Lane white: #FFFFFF everywhere
- [ ] Lane yellow: #FFCC00 everywhere
- [ ] Ego vehicle: #3366CC (blue) everywhere
- [ ] Other vehicles: #666666 (gray) everywhere
- [ ] Curbs: #888888 everywhere
- [ ] All ~40 affected files updated
- [ ] Document color standards in style guide

### Suggested fix approach
Batch find-replace:
```bash
# Normalize road surface
sed -i '' 's/fill="#[35]A3A3A"/fill="#4A4A4A"/g' assets/svg/*.svg

# Normalize grass
sed -i '' 's/fill="#[89]0[AB]0[89]0"/fill="#88AA88"/g' assets/svg/*.svg
```

Create `style_tokens.py` or similar to document standard colors.

### Reference
- svg_review_rules.md Section 5: Colors match MUTCD spec and style guide
- Previous audit issue #8

---

## P2 ISSUES (NICE-TO-HAVE - POLISH)

Lower priority improvements.

---

## ISSUE #10: Shadow/Opacity Effects Add No Value at Mobile

**Priority:** P2
**Category:** performance
**Assets:** MUTCD_R1-1_STOP, MUTCD_R1-2_YIELD, others with `<filter>` or low-opacity overlays

### Problem
Some signs use subtle shadow effects or 5-15% opacity overlays that are invisible at 96dp but add parsing overhead.

### Why it matters
Performance optimization. Invisible effects should be removed.

### Acceptance criteria
- [ ] Remove all `<filter>` elements unused at 96dp
- [ ] Remove opacity overlays < 0.2 (20%)
- [ ] File size reduction
- [ ] Visual appearance unchanged at target size
- [ ] No performance regression

### Suggested fix approach
1. Identify all `<filter>` elements: `grep -l '<filter' assets/svg/*.svg`
2. Identify low-opacity elements: `grep -E 'opacity="0\.[0-1]' assets/svg/*.svg`
3. Remove and test at 96px render

### Reference
- Previous audit issue #10

---

## ISSUE #11: Excessive Metadata in Generated SVGs

**Priority:** P2
**Category:** performance
**Assets:** Any SVGs with large `<metadata>` blocks or tool-specific comments

### Problem
Some SVGs may contain editor metadata (Inkscape, Illustrator IDs, etc.) that bloat file size.

### Why it matters
Smaller files = faster loading. Metadata serves no purpose in production.

### Acceptance criteria
- [ ] All `<metadata>` blocks removed
- [ ] All XML comments removed (except one-line attribution)
- [ ] Unused `<defs>` cleaned up
- [ ] File size < 3KB per file
- [ ] Visual appearance unchanged

### Suggested fix approach
Use SVGO or manual cleanup:
```bash
# Remove metadata
svgo --remove-metadata assets/svg/*.svg

# Or manual
sed -i '' '/<metadata>/,/<\/metadata>/d' assets/svg/*.svg
```

### Reference
- svg_review_rules.md Section 8: No excessive metadata
- Previous audit issue #11

---

## ISSUE #14: Missing High-Value Concept Assets

**Priority:** P2
**Category:** coverage
**Assets:** Not yet created

### Problem
Some common DMV test concepts lack visual support:
- Hand signals (left, right, stop)
- Gore area / exit ramp taper
- Roundabout multi-lane positioning
- Shared center turn lane detail
- HOV lane entry/exit
- Perpendicular/angle parking

### Why it matters
Visual assets significantly improve learning retention for spatial concepts.

### Acceptance criteria
- [ ] Assets created following established standards
- [ ] Consistent viewBox with category
- [ ] Readable at 96dp, recognizable at 48dp
- [ ] Assigned to relevant questions
- [ ] Manifest entries added

### Suggested fix approach
Prioritize based on question coverage gaps:
1. Hand signals (if questions exist)
2. Gore area / ramp tapers
3. Multi-lane roundabout
4. Others as needed

### Reference
- Audit report Section: Coverage Analysis

---

## ISSUE #16: Documentation - Create SVG Style Guide

**Priority:** P2
**Category:** documentation
**Assets:** N/A (new documentation)

### Problem
No written style guide exists for SVG asset creation standards.

### Why it matters
Future assets need clear guidelines to maintain consistency.

### Acceptance criteria
- [ ] Document viewBox standards by category
- [ ] Document color palette with hex codes
- [ ] Document stroke width minimums
- [ ] Document padding requirements (8% minimum)
- [ ] Document MUTCD geometry specs for common signs
- [ ] Include code examples and formulas
- [ ] Reference svg_review_rules.md

### Suggested fix approach
Create `assets/SVG_STYLE_GUIDE.md` with:
- Standards from audit report recommendations section
- MUTCD geometry formulas
- Color palette reference
- Mobile readability checklist

### Reference
- Audit report Section: Standards Recommendations

---

## ISSUE #17: Validation - Add Automated Geometry Tests

**Priority:** P2
**Category:** tooling
**Assets:** N/A (new test suite)

### Problem
No automated validation of sign geometry correctness.

### Why it matters
Prevents regression of geometry fixes. Enforces MUTCD compliance automatically.

### Acceptance criteria
- [ ] Python script to validate Golden Set geometry
- [ ] Check STOP sign is regular octagon (angles, radii, sides)
- [ ] Check YIELD is equilateral triangle, point-down
- [ ] Check speed limits have >= 8% padding
- [ ] Check all viewBox attributes present
- [ ] Check all marker references resolve
- [ ] Runs in CI/test suite

### Suggested fix approach
Create `assets/test_svg_geometry.py`:
```python
def test_stop_sign_is_regular_octagon():
    tree = ET.parse('assets/svg/MUTCD_R1-1_STOP.svg')
    polygon = tree.find('.//polygon[@fill="#C1272D"]')
    points = parse_polygon_points(polygon.get('points'))

    angles = calculate_internal_angles(points)
    assert all(134.5 <= a <= 135.5 for a in angles), "STOP sign not regular octagon"

    radii = calculate_radii_from_center(points)
    assert max(radii) - min(radii) < 1.0, "STOP sign radii not equal"
```

Run in CI: `pytest assets/test_svg_geometry.py`

### Reference
- svg_review_rules.md Section 8: Automated validation recommended

---

## Issue Creation Summary

**Total Issues:** 23
- **P0 (Blocking):** 6 (NEW: sign geometry failures, XML errors, padding violations)
- **P1 (Important):** 12 (from previous audit + new geometry-related)
- **P2 (Nice-to-have):** 5 (polish, documentation, tooling)

**Estimated Fix Time:**
- P0 issues: 4-6 hours (geometry regeneration + attribute cleanup + padding fixes)
- P1 issues: 8-12 hours (stroke widths, training cues, viewBox normalization)
- P2 issues: 4-8 hours (cleanup, documentation, tests)

**Total:** ~20-26 hours to address all issues

**Recommended Order:**
1. Fix all P0 issues first (blocking)
2. Apply P1 batched fixes (readability, consistency)
3. Polish with P2 improvements (performance, documentation)

---

**End of Issues Document**
