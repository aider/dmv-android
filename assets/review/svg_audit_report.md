# SVG Asset Audit Report

**Generated:** 2026-02-09 (UPDATED with MUTCD Geometry Compliance Check)
**Scope:** 109 SVG assets reviewed
**Repository:** `/Users/ayder/projects/dmv.tx/`
**Review Rules:** `assets/review/svg_review_rules.md` v1.0

---

## Executive Summary

- **Total assets in manifest:** 109
- **Total SVG files on disk:** 109
- **Assets referenced by questions:** 108 (99% coverage)
- **Questions using images:** 136 questions across 8 topics
- **Issues identified:** 23 total (**P0: 6**, P1: 12, P2: 5)
- **Overall health:** ❌ **CRITICAL ISSUES FOUND - Sign correctness failures**

### Critical New Findings (P0)

This audit enforces the **hard-fail sign correctness rules** from `svg_review_rules.md`. The following P0 issues were discovered:

1. **MUTCD_R1-1_STOP:** ❌ **NOT a true regular octagon** - angles alternate 125°/144° instead of 135°, radii vary by 6.55px
2. **Invalid XML:** 4 assets have duplicate `font-weight` attributes (parser error)
3. **MUTCD_R5-1_DO_NOT_ENTER:** Shape is circle, should be square per MUTCD R5-1
4. **Speed Limit signs:** Padding < 8% minimum requirement
5. **School Speed Limit sign:** Bottom text overflow risk

### Sign Correctness Hard-Fail Summary

Per `svg_review_rules.md` Section 1: **Any sign whose geometry, border, or text is incorrect — even slightly — is a hard fail.**

**Result:** 5 Golden Set signs FAIL geometry/border/padding requirements.

---

## Golden Set Status

The Golden Set comprises critical regulatory/warning signs that must have correct MUTCD geometry. Per review rules, these MUST be verified and reported in every audit.

| Sign Type | Asset File | Status | Issue | Geometry Check |
|-----------|-----------|--------|-------|----------------|
| **STOP** | MUTCD_R1-1_STOP.svg | ❌ **needs_fix** | #NEW-01 | NOT regular octagon - angles 125°/144° (expect 135°), radii vary 6.55px |
| **YIELD** | MUTCD_R1-2_YIELD.svg | ✓ ok | — | Equilateral triangle, point-down, sides 173.4±0.6px ✓ |
| **SPEED LIMIT 65** | MUTCD_R2-1_SPEED_LIMIT_65.svg | ⚠️ **needs_fix** | #NEW-02 | Rectangle ✓, border ✓, padding BELOW 8% minimum |
| **SPEED LIMIT 70** | MUTCD_R2-1_SPEED_LIMIT_70.svg | ⚠️ **needs_fix** | #NEW-02 | Rectangle ✓, border ✓, padding BELOW 8% minimum |
| **SPEED LIMIT 30** | MUTCD_R2-1_SPEED_LIMIT_30.svg | ⚠️ **needs_fix** | #NEW-02 | Rectangle ✓, border ✓, padding BELOW 8% minimum |
| **DO NOT ENTER** | MUTCD_R5-1_DO_NOT_ENTER.svg | ❌ **needs_fix** | #NEW-03 | WRONG SHAPE - uses circle, should be square with rounded corners per R5-1 |
| **WRONG WAY** | MUTCD_R5-1a_WRONG_WAY.svg | ✓ ok | — | Rectangle landscape ✓, border ✓, text centered ✓ |
| **ONE WAY** | MUTCD_R10-6_ONE_WAY.svg | ✓ ok | — | Rectangle landscape ✓, arrow visible ✓, border ✓ |
| **NO PARKING** | MUTCD_R7-8_NO_PARKING.svg | ⚠️ **needs_fix** | #NEW-02 | Rectangle ✓, border ✓, padding BELOW 8% minimum |
| **SCHOOL ZONE** | MUTCD_S1-1_SCHOOL_CROSSING.svg | ✓ ok | — | Pentagon point-up ✓, fluorescent yellow-green ✓, symbols visible ✓ |
| **RAILROAD CROSSING** | MUTCD_R15-1_RAILROAD_CROSSING.svg | ✓ ok | — | Circle ✓, X and RR ✓, yellow background ✓ |
| **MERGE** | MUTCD_W3-1_MERGE.svg | ✓ ok | — | Diamond (rotated square) ✓, yellow ✓, border ✓ |
| **SCHOOL SPEED LIMIT 20** | MUTCD_S4-3_SCHOOL_SPEED_LIMIT_20.svg | ⚠️ **needs_fix** | #NEW-04 | Rectangle ✓, bottom text overflow risk, padding insufficient |

### Golden Set Summary
- **Total:** 13 sign types
- **OK:** 5 (38%)
- **Needs Fix:** 8 (62%)
- **Missing:** 0

---

## Critical Issues Detail

### P0-01: STOP Sign NOT a True Regular Octagon

**Asset:** MUTCD_R1-1_STOP.svg (used by 2 questions)

**MUTCD Requirement (R1-1):** True regular octagon with 8 equal sides and 8 equal internal angles of 135°.

**Current Geometry:**
- Vertices: 8 ✓
- Radii from center: 90.00, 83.45, 88.00, 83.45, 90.00, 83.45, 88.00, 83.45 (variation: 6.55px ❌)
- Side lengths: 65.30, 67.08, 67.08, 65.30, 65.30, 67.08, 67.08, 65.30 (variation: 1.78px ❌)
- Internal angles: 125.3°, 143.9°, 126.9°, 143.9°, 125.3°, 143.9°, 126.9°, 143.9° ❌
- Expected angles: all 135°

**Verdict:** ❌ NOT A REGULAR OCTAGON - alternating angles violate MUTCD spec

**Impact:** This is the most recognizable traffic sign. Incorrect geometry undermines training accuracy and fails regulatory compliance.

---

### P0-02: Invalid XML - Duplicate Attributes

**Assets:**
- SPEED_FOLLOWING_DISTANCE_3SEC.svg (used by 2 questions)
- SPEED_HIGHWAY_70MPH.svg
- SPEED_SCHOOL_ZONE_20MPH.svg (used by 1 question)
- SPEED_STOPPING_DISTANCE.svg (used by 1 question)

**Error:** Duplicate `font-weight` attribute on `<text>` elements (e.g., `font-weight="bold" font-weight="900"`)

**Impact:** These files fail XML validation. While some parsers may tolerate this, it's non-compliant SVG.

**Example:**
```xml
<!-- Line 15 in SPEED_FOLLOWING_DISTANCE_3SEC.svg -->
<text ... font-size="28" font-weight="bold" font-weight="900" ...>
```

---

### P0-03: DO NOT ENTER Wrong Shape

**Asset:** MUTCD_R5-1_DO_NOT_ENTER.svg (used by 1 question)

**MUTCD Requirement (R5-1):** Square with rounded corners, white horizontal bar, red background.

**Current Geometry:** Circle (`<circle cx="100" cy="100" r="90">`)

**Verdict:** ❌ WRONG SHAPE - uses circle instead of square

**Impact:** The shape is part of sign recognition training. Circle vs. square changes the meaning.

---

### P0-04: Speed Limit Signs Padding Violation

**Assets:**
- MUTCD_R2-1_SPEED_LIMIT_65.svg (used by 2 questions)
- MUTCD_R2-1_SPEED_LIMIT_70.svg (used by 2 questions)
- MUTCD_R2-1_SPEED_LIMIT_30.svg (used by 1 question)
- MUTCD_R7-8_NO_PARKING.svg (used by 2 questions)

**Requirement:** Minimum 8% inner padding per svg_review_rules.md Section 3.

**Current State:**
- ViewBox: 0 0 150 200
- Outer rect: x=5, y=5, width=140, height=190
- Inner rect: x=12, y=12, width=126, height=176
- Effective padding from outer edge: 5px
- Padding ratio: 5/150 = 3.3% (horizontal), 5/200 = 2.5% (vertical)

**Verdict:** ❌ BELOW 8% MINIMUM - padding is 2.5-3.3% instead of required 8%

**Impact:** Text/numbers risk being cut off at small render sizes or touching borders.

---

### P0-05: School Speed Limit Text Overflow Risk

**Asset:** MUTCD_S4-3_SCHOOL_SPEED_LIMIT_20.svg (used by 3 questions - HIGH priority)

**Issue:** Bottom text "WHEN CHILDREN" (y=175) and "PRESENT" (y=190) at 14px font are only 10px and 5px from bottom edge respectively. With a viewBox height of 200, this gives ~5% padding at the bottom.

**Requirement:** Minimum 8% padding = 16px from bottom edge

**Verdict:** ⚠️ Text overflow risk at bottom edge

---

## Asset Inventory by Category

| Category | Count | Primary ViewBox | Status | Issues |
|----------|-------|-----------------|--------|--------|
| **MUTCD Regulatory** | 20 | Mixed (5 sizes) | ⚠️ Needs work | Geometry failures, padding violations |
| **MUTCD Warning** | 16 | 0 0 200 200 | ✓ Good | Diamond shapes correct |
| **MUTCD School** | 2 | 150x200 / 200x200 | ⚠️ Needs work | Padding issues |
| **SIGNAL** | 13 | 0 0 100 250 (10) | ✓ Good | Consistent |
| **PAVEMENT** | 22 | 0 0 300 200 | ✓ Good | Consistent viewBox |
| **INTERSECTION** | 8 | 0 0 200 200 (6) | ⚠️ Needs work | Training cues needed |
| **PARKING** | 5 | 0 0 200 200 | ✓ Good | Consistent |
| **SAFE** | 4 | 0 0 200 200 | ✓ Good | Consistent |
| **SPEED** | 6 | 0 0 300 200 | ❌ FAIL | 4 files have XML errors |
| **SPECIAL** | 2 | Mixed | ✓ Good | Small category |
| **OTHER** | 11 | Mixed | ⚠️ Check | Route markers, service signs |

---

## Top 20 Most-Used Assets (by Question References)

| Rank | Count | Asset ID | Category | Status |
|------|-------|----------|----------|--------|
| 1 | 3× | MUTCD_R1-2_YIELD | regulatory | ✓ OK |
| 2 | 3× | MUTCD_R15-1_RAILROAD_CROSSING | regulatory | ✓ OK |
| 3 | 3× | MUTCD_S4-3_SCHOOL_SPEED_LIMIT_20 | school | ❌ P0-05 |
| 4 | 2× | MUTCD_R8-3a_NO_PARKING_ANYTIME | regulatory | ✓ OK |
| 5 | 2× | MUTCD_R7-8_NO_PARKING | regulatory | ❌ P0-04 |
| 6 | 2× | MUTCD_R7-107_HANDICAPPED_PARKING | regulatory | ✓ OK |
| 7 | 2× | PAVEMENT_HANDICAP_SYMBOL | pavement | ✓ OK |
| 8 | 2× | PAVEMENT_SCHOOL_ZONE | pavement | ⚠️ P1 |
| 9 | 2× | PAVEMENT_RAILROAD_CROSSING_X | pavement | ✓ OK |
| 10 | 2× | MUTCD_R1-1_STOP | regulatory | ❌ P0-01 |
| 11 | 2× | MUTCD_W11-1_PEDESTRIAN_CROSSING | warning | ✓ OK |
| 12 | 2× | MUTCD_W11-2_BICYCLE_CROSSING | warning | ✓ OK |
| 13 | 2× | INTERSECTION_EMERGENCY_VEHICLE | intersection | ⚠️ P1 |
| 14 | 2× | INTERSECTION_SCHOOL_BUS_STOPPED | intersection | ⚠️ P1 |
| 15 | 2× | MUTCD_W3-1_MERGE | warning | ✓ OK |
| 16 | 2× | MUTCD_R10-6_ONE_WAY | regulatory | ✓ OK |
| 17 | 2× | SPEED_FOLLOWING_DISTANCE_3SEC | speed | ❌ P0-02 |
| 18 | 2× | MUTCD_R2-1_SPEED_LIMIT_65 | regulatory | ❌ P0-04 |
| 19 | 2× | MUTCD_R10-7_DO_NOT_PASS | regulatory | ✓ OK |
| 20 | 2× | MUTCD_R2-1_SPEED_LIMIT_70 | regulatory | ❌ P0-04 |

**Critical:** 8 of the top 20 most-used assets have P0 issues.

---

## Recurring Patterns

### Pattern 1: Sign Geometry Approximations (5 occurrences)
**Issue:** Hand-drawn sign shapes deviate from MUTCD specs
- MUTCD_R1-1_STOP: irregular octagon (angles off by 10°)
- MUTCD_R5-1_DO_NOT_ENTER: circle instead of square

**Fix:** Use MUTCD-compliant primitives or component library

---

### Pattern 2: Padding Below 8% Minimum (8 occurrences)
**Issue:** Text/content too close to sign borders
- All SPEED_LIMIT signs: 2.5-3.3% instead of 8%
- School speed limit: bottom text at 5% padding
- NO_PARKING signs: similar issue

**Fix:** Increase sign outer rect dimensions or reduce inner content size

---

### Pattern 3: Duplicate XML Attributes (4 occurrences)
**Issue:** Invalid SVG due to duplicate `font-weight`
- All SPEED_* educational diagrams affected

**Fix:** Remove `font-weight="bold"`, keep only `font-weight="900"`

---

### Pattern 4: ViewBox Inconsistency Within Categories (49 regulatory signs)
**Issue:** 5 different viewBox sizes within MUTCD regulatory signs
- Causes visual "jumping" when signs appear in sequence
- Existing P1 issue from previous audit

---

## Standards Recommendations

### 1. Sign Geometry Standards

**STOP Signs (R1-1):**
- Use formula for regular octagon: vertices at 45° intervals
- Center at (100, 100), radius 90px
- Verify: all angles = 135°, all sides equal length

**DO NOT ENTER (R5-1):**
- Shape: `<rect>` with `rx` for rounded corners, NOT `<circle>`
- Aspect ratio: 1:1 (square)
- White bar height: ~15% of sign height

**SPEED LIMIT Signs (R2-1):**
- Aspect ratio: 3:4 (portrait rectangle)
- Minimum padding: 8% = 12px horizontal, 16px vertical for 150x200 viewBox
- Recommended: 10% = 15px horizontal, 20px vertical
- Text hierarchy: SPEED 24px, LIMIT 24px, number 76px

---

### 2. ViewBox Standards by Category

| Category | Recommended ViewBox | Rationale |
|----------|---------------------|-----------|
| **Regulatory (square)** | 0 0 200 200 | Standard for octagon, circle, diamond signs |
| **Regulatory (portrait)** | 0 0 150 200 | Speed limit, parking, turn restrictions |
| **Regulatory (landscape)** | 0 0 200 100 | ONE WAY, WRONG WAY |
| **Warning (diamond)** | 0 0 200 200 | All diamond shapes fit well |
| **Signals (vertical)** | 0 0 100 250 | Standard 3-light vertical signal |
| **Pavement** | 0 0 300 200 | Wide lane view |
| **Intersection** | 0 0 200 200 | Standard overhead view |

---

### 3. Padding Standards

**Minimum inner padding:** 8% of primary dimension
**Recommended:** 10-12% for comfort

**Calculation:**
```
For 150x200 viewBox (portrait):
- Primary dimension: 150 (width)
- 8% minimum: 0.08 × 150 = 12px
- Recommended 10%: 0.10 × 150 = 15px

Current outer rect: x=5, width=140 → only 5px padding = 3.3% ❌
Correct outer rect: x=15, width=120 → 15px padding = 10% ✓
```

---

### 4. Stroke Width Standards

**For mobile readability at 96dp:**

| Element Type | ViewBox 200×200 | ViewBox 300×200 | Effective dp at 96dp |
|--------------|-----------------|-----------------|----------------------|
| Lane markings | 8px | 8px | 3.8dp / 2.5dp |
| Stop lines | 8px | 12px | 3.8dp / 3.8dp |
| Sign borders | 6px | 6px | 2.9dp |
| Fine details | 4px minimum | 4px minimum | 1.9dp |

**Recommendation:** Use 8px minimum for all critical markings in pavement assets.

---

### 5. Color Palette (MUTCD Standard)

```python
# Regulatory/Warning Colors
MUTCD_RED = "#C1272D"          # Stop, yield, prohibitory
MUTCD_YELLOW = "#FFCC00"       # Warning signs
MUTCD_GREEN = "#00843D"        # Guide signs (not widely used in this set)
MUTCD_BLUE = "#003F87"         # Service/motorist info
MUTCD_ORANGE = "#FF6B00"       # Construction/work zone
MUTCD_FLUORESCENT_YELLOW = "#C8E800"  # School/pedestrian

# Scene Elements
ROAD_SURFACE = "#4A4A4A"       # Asphalt
GRASS_OFFROAD = "#88AA88"      # Grass/shoulder
LANE_WHITE = "#FFFFFF"         # White markings
LANE_YELLOW = "#FFCC00"        # Yellow center lines
EGO_VEHICLE = "#3366CC"        # User's vehicle (blue)
OTHER_VEHICLE = "#666666"      # Other traffic (gray)
```

---

## Coverage Analysis

### Well-Covered Concepts
✓ Stop/yield right-of-way
✓ Lane markings (all major types)
✓ Traffic signals (standard + arrows + pedestrian)
✓ Warning signs (curves, intersections, hazards)
✓ Parking scenarios (parallel, hill, disabled)
✓ School zones
✓ Railroad crossings

### Coverage Gaps (Potential Future Assets)
- Hand signals (left, right, stop) - currently text-only
- Gore area / exit ramp tapers
- Roundabout lane positioning (multiple lanes)
- Shared turn lane (center TWLTL detail)
- Perpendicular/angle parking
- Highway HOV lane entry/exit
- Flashing beacon types

---

## Manifest Alignment

### Files vs. Manifest
- **Total SVG files:** 109
- **Total manifest entries:** 109
- **Perfect alignment:** ✓ All files have manifest entries
- **Orphaned files:** 0
- **Broken references:** 0

### Asset Usage
- **Referenced by questions:** 108 / 109 (99%)
- **Unused assets:** 1 (SPEED_HIGHWAY_70MPH - created but not yet assigned to questions)

---

## File Size Analysis

| Size Range | Count | Notes |
|------------|-------|-------|
| < 1KB | 79 | Good - clean, simple signs |
| 1-2KB | 24 | Acceptable - intersection scenes |
| 2-3KB | 5 | Check for optimization |
| > 3KB | 1 | PAVEMENT_BIKE_LANE (needs review) |

**Average size:** 847 bytes
**Median size:** 782 bytes
**Largest:** PAVEMENT_BIKE_LANE (4.2KB - likely has complex path data)

---

## Action Plan

### Phase 1: Fix P0 Issues (Immediate - BLOCKING)

1. **Fix STOP sign geometry** (#NEW-01)
   - Regenerate using regular octagon formula
   - Verify all angles = 135°, all radii equal

2. **Fix duplicate XML attributes** (#NEW-02)
   - SPEED_FOLLOWING_DISTANCE_3SEC
   - SPEED_HIGHWAY_70MPH
   - SPEED_SCHOOL_ZONE_20MPH
   - SPEED_STOPPING_DISTANCE
   - Remove `font-weight="bold"`, keep `font-weight="900"`

3. **Fix DO NOT ENTER shape** (#NEW-03)
   - Change from circle to square with rounded corners
   - ViewBox 0 0 200 200, outer square rx=6

4. **Fix speed limit padding** (#NEW-04)
   - Increase padding to 10% minimum
   - Adjust outer rect: x=15, y=20 (instead of x=5, y=5)
   - Reduce inner content size if needed

5. **Fix school speed limit overflow** (#NEW-05)
   - Move bottom text up or reduce font size
   - Ensure 16px minimum from bottom edge (8% of 200px)

### Phase 2: Fix P1 Issues (High Priority)
- Apply previous audit's P1 fixes (still valid)
- Normalize viewBox dimensions
- Add training cues to intersection scenarios
- Increase stroke widths for pavement markings

### Phase 3: Documentation & Validation
- Create primitives library for MUTCD-compliant shapes
- Add geometry validation tests
- Document sign construction standards

---

## Validation Checklist (Per Sign)

Use this checklist for every Golden Set sign:

### Structural Checks
- [ ] Valid XML (no duplicate attributes, well-formed)
- [ ] viewBox present and appropriate for sign type
- [ ] File size < 3KB

### Sign Correctness (Hard Fail)
- [ ] Shape matches MUTCD spec (count sides, measure angles)
- [ ] Correct orientation (point-up/point-down for triangles, diamond rotation)
- [ ] Border present where MUTCD requires it
- [ ] Border width consistent (6-8px for 200px viewBox)
- [ ] Text matches real sign (STOP, YIELD, speed number)
- [ ] Text centered with >= 8% inner padding
- [ ] No text overflow beyond sign boundary

### Mobile Readability (Hard Fail)
- [ ] Recognizable at 96dp render
- [ ] Shape distinguishable at 48dp render
- [ ] Border visible at 96dp (minimum 6px stroke in 200px viewBox)
- [ ] Text legible at 96dp (minimum 20px font for labels, 60px for numbers)

### Quality Checks (Soft Fail)
- [ ] Colors match MUTCD spec
- [ ] No clipping (content within viewBox with 5% margin)
- [ ] Consistent with other signs in same family

---

## Conclusion

This audit identified **6 new P0 critical issues** that block MUTCD compliance:

1. STOP sign geometry failure (most critical)
2. 4 files with invalid XML
3. DO NOT ENTER wrong shape
4. Speed limit padding violations
5. School speed limit overflow

**All 6 P0 issues MUST be fixed before the app is production-ready.** Sign correctness is non-negotiable for a driver education app.

**Recommendation:** Pause other SVG work and fix all P0 issues first. Then proceed with P1 optimizations from the previous audit.
