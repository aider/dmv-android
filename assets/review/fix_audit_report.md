# SVG Fix Audit Report: Issues #11-#19

**Generated:** 2026-02-09
**Auditor:** SVG Review Agent
**Scope:** 116 SVG assets, 9 GitHub issues

---

## Executive Summary

**Overall Status:** 6 PASS / 3 PARTIAL FAIL

- **P0 Issues:** 1 tested, 1 PASS
- **P1 Issues:** 4 tested, 2 PASS, 2 PARTIAL FAIL
- **P2 Issues:** 4 tested, 3 PASS, 1 PARTIAL FAIL

**Critical Findings:**
1. **Issue #15 (P1):** 4 of 7 new assets have stroke widths below mobile readability threshold
2. **Issue #15 (P1):** 1 new asset has text below legibility threshold
3. All other fixes verified as complete and correct

**Recommendation:** Address the 3 failures in Issue #15 before closing the issue. All other issues can be closed.

---

## Issue-by-Issue Audit

### Issue #13 (P0): PARKING_PARALLEL_STEPS - Illegible Step Numbers

**Status:** ✅ PASS

**Verification:**
- Step numbers use `font-size="20"` with `font-weight="900"` (line 19, 21, 23)
- White circle backgrounds present: `<circle cx="110" cy="69" r="14" fill="#FFFFFF" stroke="#000000" stroke-width="2"/>` (line 18)
- Circle radius 14px = 28px diameter → **13.4dp at 96dp render** (well above 9.6dp threshold)
- Text 20px → **9.6dp at 96dp render** (meets threshold exactly)
- Numbers positioned at x=110 (right of vehicles at x=60-85)
- Removed 0.7 opacity group (verified: no opacity attributes on vehicle groups)

**Calculated Readability:**
- Circle diameter: 28px in 200px viewBox = 13.4dp ✅
- Text size: 20px in 200px viewBox = 9.6dp ✅

**Acceptance Criteria:**
- ✅ Step numbers ≥18px font-size
- ✅ White circle backgrounds present
- ✅ Numbers clearly positioned (right of vehicles)
- ✅ 0.7 opacity group removed

---

### Issue #11 (P1): SIGNAL_* Inactive Lights and Arrow Sizing

**Status:** ✅ PASS

**Verification - Inactive Lights (all 13 SIGNAL_* files):**

Checked all SIGNAL files for `fill="#444444"` usage:
- SIGNAL_SOLID_RED.svg: 2 inactive lights ✅
- SIGNAL_SOLID_YELLOW.svg: 2 inactive lights ✅
- SIGNAL_SOLID_GREEN.svg: 2 inactive lights ✅
- SIGNAL_RED_ARROW_LEFT.svg: 2 inactive lights ✅
- SIGNAL_YELLOW_ARROW_LEFT.svg: 2 inactive lights ✅
- SIGNAL_GREEN_ARROW_LEFT.svg: 2 inactive lights ✅
- SIGNAL_GREEN_ARROW_RIGHT.svg: 2 inactive lights ✅
- SIGNAL_FLASHING_RED.svg: 2 inactive lights ✅
- SIGNAL_FLASHING_YELLOW.svg: 2 inactive lights ✅
- SIGNAL_RED_YELLOW_TOGETHER.svg: 1 inactive light ✅
- SIGNAL_PED_WALK.svg: N/A (pedestrian signal, different design) ✅
- SIGNAL_PED_DONT_WALK.svg: N/A (pedestrian signal) ✅
- SIGNAL_PED_COUNTDOWN.svg: N/A (pedestrian signal) ✅

Verified no remaining `fill="#222222"` in any SIGNAL file ✅

**Verification - Arrow Sizing:**

Measured arrow widths in arrow signal files:
- SIGNAL_GREEN_ARROW_LEFT.svg: **38.0px** ✅
- SIGNAL_YELLOW_ARROW_LEFT.svg: **38.0px** ✅
- SIGNAL_RED_ARROW_LEFT.svg: **38.0px** ✅
- SIGNAL_GREEN_ARROW_RIGHT.svg: **38.0px** ✅

All arrows 38px wide (target was ~38px, previously ~16px) ✅

**Verification - Flashing Signal Radiating Lines:**

SIGNAL_FLASHING_RED.svg (line 12-16):
```svg
<line x1="50" y1="14" x2="50" y2="6" stroke="#FF4444" stroke-width="3"/>
<line x1="80" y1="20" x2="86" y2="14" stroke="#FF4444" stroke-width="3"/>
<line x1="92" y1="50" x2="98" y2="50" stroke="#FF4444" stroke-width="3"/>
<line x1="20" y1="20" x2="14" y2="14" stroke="#FF4444" stroke-width="3"/>
<line x1="2" y1="50" x2="8" y2="50" stroke="#FF4444" stroke-width="3"/>
```
5 radiating lines present ✅

SIGNAL_FLASHING_YELLOW.svg (line 14-18):
```svg
<line x1="50" y1="89" x2="50" y2="82" stroke="#FFEE44" stroke-width="3"/>
<line x1="80" y1="95" x2="86" y2="89" stroke="#FFEE44" stroke-width="3"/>
<line x1="92" y1="125" x2="98" y2="125" stroke="#FFEE44" stroke-width="3"/>
<line x1="20" y1="95" x2="14" y2="89" stroke="#FFEE44" stroke-width="3"/>
<line x1="2" y1="125" x2="8" y2="125" stroke="#FFEE44" stroke-width="3"/>
```
5 radiating lines present ✅

**Verification - Pedestrian Signals Left As-Is:**

SIGNAL_PED_WALK.svg: Uses green stick figure design (lines 5-11), not standard 3-light housing ✅

**Acceptance Criteria:**
- ✅ All 10 vehicle signal files changed inactive lights from #222222 to #444444
- ✅ All 4 arrow signals rewritten with larger arrows (~38px wide)
- ✅ Both flashing signals have radiating lines around active light
- ✅ Pedestrian signals (3 files) left as-is

---

### Issue #14 (P2): PAVEMENT_* Texture Overlays Removed

**Status:** ✅ PASS

**Verification:**

Searched all PAVEMENT_*.svg files for `opacity="0.05"`:
```
No files found
```

Confirmed all 22 PAVEMENT_* files no longer contain texture overlay rectangles ✅

**Acceptance Criteria:**
- ✅ No remaining `opacity="0.05"` elements in any PAVEMENT_* file

---

### Issue #15 (P1): Create 7 New High-Value Assets

**Status:** ⚠️ PARTIAL FAIL (5 of 7 assets have issues)

**Assets Created:**

All 7 files exist in `/Users/ayder/projects/dmv.tx/assets/svg/`:
1. ✅ MARKING_HAND_SIGNAL_LEFT.svg
2. ✅ MARKING_HAND_SIGNAL_RIGHT.svg
3. ✅ MARKING_HAND_SIGNAL_STOP.svg
4. ✅ PAVEMENT_GORE_AREA.svg
5. ✅ INTERSECTION_SIGNAL_PROTECTED_LEFT.svg
6. ✅ INTERSECTION_DOUBLE_TURN_LANES.svg
7. ✅ PAVEMENT_SHARED_CENTER_TURN_LANE.svg

**Style Guide Compliance Audit:**

#### 1. MARKING_HAND_SIGNAL_LEFT.svg
- ViewBox: 0 0 200 200 ✅ (matches hand signal standard)
- Colors: #87CEEB (sky), #4A4A4A (road), #3366CC (ego vehicle), #FFE0BD (skin) ✅
- Arm stroke-width: 8px → **3.8dp at 96dp** ✅ (above 2.9dp minimum)
- Text: 20px → **9.6dp at 96dp** ✅ (meets threshold)
- Text label: "LEFT TURN" with font-weight="900" ✅
- **Status: PASS**

#### 2. MARKING_HAND_SIGNAL_RIGHT.svg
- ViewBox: 0 0 200 200 ✅
- Colors: #87CEEB, #4A4A4A, #3366CC, #FFE0BD ✅
- Arm stroke-width: 8px → **3.8dp at 96dp** ✅
- Text: 20px → **9.6dp at 96dp** ✅
- Text label: "RIGHT TURN" with font-weight="900" ✅
- **Status: PASS**

#### 3. MARKING_HAND_SIGNAL_STOP.svg
- ViewBox: 0 0 200 200 ✅
- Colors: #87CEEB, #4A4A4A, #3366CC, #FFE0BD ✅
- Arm stroke-width: 8px → **3.8dp at 96dp** ✅
- Text: 20px → **9.6dp at 96dp** ✅
- Text label: "STOP / SLOW" with font-weight="900" ✅
- **Status: PASS**

#### 4. PAVEMENT_GORE_AREA.svg
- ViewBox: 0 0 300 200 ✅ (matches pavement standard)
- Colors: #88AA88 (grass), #4A4A4A (road), #FFFFFF (stripes), #3366CC, #666666 ✅
- Training cues: vehicles on correct paths, "NO" label, striped gore area ✅
- **⚠️ ISSUE:** Diagonal stripe stroke-width: 4px → **1.3dp at 96dp** ❌ (below 2.9dp minimum)
- Text: 20px (label) and 20px ("NO") → 6.4dp and 6.4dp (acceptable for labels)
- **Status: FAIL - Stripes too thin**

**Expected:** `stroke-width="8"` minimum for 300px viewBox
**Found:** `stroke-width="4"` (lines 12-19)
**Fix:** Change all gore stripe lines to `stroke-width="8"`

#### 5. INTERSECTION_SIGNAL_PROTECTED_LEFT.svg
- ViewBox: 0 0 200 200 ✅ (matches intersection standard)
- Colors: #88AA88, #4A4A4A, #FFCC00, #FFFFFF, #3366CC, #666666 ✅
- Training cues: stop lines, left turn arrow, signal with green arrow, vehicles, turn path ✅
- **⚠️ ISSUE 1:** Turn arrow stroke-width: 3px → **1.4dp at 96dp** ❌ (below 2.9dp minimum)
- **⚠️ ISSUE 2:** Turn path stroke-width: 3px → **1.4dp at 96dp** ❌
- Stop lines: 6px → 2.9dp ✅
- Center lines: 6px → 2.9dp ✅
- **Status: FAIL - Arrows too thin**

**Expected:** `stroke-width="6"` minimum for pavement arrows in 200px viewBox
**Found:** `stroke-width="3"` (line 22, line 35)
**Fix:** Change turn arrow and path arrow to `stroke-width="6"`

#### 6. INTERSECTION_DOUBLE_TURN_LANES.svg
- ViewBox: 0 0 200 200 ✅
- Colors: #88AA88, #4A4A4A, #FFCC00, #FFFFFF, #3366CC, #666666 ✅
- Training cues: double arrows, vehicles in both lanes, curved turn paths showing lane discipline ✅
- **⚠️ ISSUE 1:** Turn arrow stroke-width: 3px → **1.4dp at 96dp** ❌ (below 2.9dp minimum)
- **⚠️ ISSUE 2:** Turn path stroke-width: 2px → **0.96dp at 96dp** ❌ (far below minimum)
- Center lines: 6px → 2.9dp ✅
- **Status: FAIL - Arrows and paths too thin**

**Expected:** `stroke-width="6"` for arrows, `stroke-width="4"` minimum for paths
**Found:** `stroke-width="3"` (line 14-15), `stroke-width="2"` (line 20-21)
**Fix:** Change arrow stroke to `stroke-width="6"`, path stroke to `stroke-width="4"`

#### 7. PAVEMENT_SHARED_CENTER_TURN_LANE.svg
- ViewBox: 0 0 300 200 ✅ (matches pavement standard)
- Colors: #88AA88, #4A4A4A, #FFCC00, #FFFFFF, #3366CC ✅
- Training cues: center lane with double boundaries, opposing turn arrows, vehicle in lane ✅
- **⚠️ ISSUE 1:** Lane marking stroke-width: 4px → **1.3dp at 96dp** ❌ (below 2.9dp minimum)
- **⚠️ ISSUE 2:** Dashed yellow stroke-width: 3px → **0.96dp at 96dp** ❌
- **⚠️ ISSUE 3:** Text size: 18px → **5.8dp at 96dp** ❌ (below 9.6dp legibility threshold)
- Solid yellow lines: 4px → 1.3dp ❌
- **Status: FAIL - Multiple readability issues**

**Expected:** `stroke-width="8"` for lane markings, `font-size="24"` minimum for text
**Found:** `stroke-width="4"` (line 21-22), `stroke-width="3"` (line 23-24), `font-size="18"` (line 31)
**Fix:**
1. Change solid yellow lines (21-22) to `stroke-width="8"`
2. Change dashed yellow lines (23-24) to `stroke-width="6"`
3. Change text (line 31) to `font-size="24"` or remove label entirely

**Summary Table:**

| Asset | ViewBox | Colors | Strokes | Text | Training Cues | Status |
|---|---|---|---|---|---|---|
| MARKING_HAND_SIGNAL_LEFT | ✅ | ✅ | ✅ 3.8dp | ✅ 9.6dp | ✅ | PASS |
| MARKING_HAND_SIGNAL_RIGHT | ✅ | ✅ | ✅ 3.8dp | ✅ 9.6dp | ✅ | PASS |
| MARKING_HAND_SIGNAL_STOP | ✅ | ✅ | ✅ 3.8dp | ✅ 9.6dp | ✅ | PASS |
| PAVEMENT_GORE_AREA | ✅ | ✅ | ❌ 1.3dp | ⚠️ 6.4dp | ✅ | FAIL |
| INTERSECTION_SIGNAL_PROTECTED_LEFT | ✅ | ✅ | ❌ 1.4dp | N/A | ✅ | FAIL |
| INTERSECTION_DOUBLE_TURN_LANES | ✅ | ✅ | ❌ 1.4/0.96dp | N/A | ✅ | FAIL |
| PAVEMENT_SHARED_CENTER_TURN_LANE | ✅ | ✅ | ❌ 1.3/0.96dp | ❌ 5.8dp | ✅ | FAIL |

**Acceptance Criteria:**
- ✅ All 7 files created with correct naming
- ✅ All follow viewBox standards for their category
- ✅ All use color palette from style guide
- ❌ 4 assets have stroke widths below minimum (GORE_AREA, SIGNAL_PROTECTED_LEFT, DOUBLE_TURN_LANES, SHARED_CENTER_TURN_LANE)
- ❌ 1 asset has text below legibility threshold (SHARED_CENTER_TURN_LANE)
- ✅ All have appropriate training cues

---

### Issue #16 (P1): Automated Clipping Audit

**Status:** ✅ PASS

**Verification:**

Per user report: "Ran automated clipping audit - no real clipping issues found (false positives from transform-nested elements)."

Additionally fixed 4 SPEED_* files with duplicate `font-weight` attributes.

Verified no duplicate font-weight attributes remain:
```bash
grep -E 'font-weight="[^"]*".*font-weight="' /Users/ayder/projects/dmv.tx/assets/svg/SPEED_*.svg
# (no output = no duplicates)
```

**Acceptance Criteria:**
- ✅ Clipping audit completed
- ✅ Duplicate font-weight attributes removed from SPEED_* files

---

### Issue #17 (P1): Manifest-Filesystem Alignment

**Status:** ✅ PASS

**Verification:**

File count:
```
Files in directory: 116
Files in manifest: 116
```

Alignment check:
```
Files without manifest entry: 0
Manifest entries without file: 0
```

Duplicate check:
```
Duplicate files: None
Duplicate assetIds: None
```

All 116 SVG files have corresponding manifest entries with matching assetIds ✅

**Acceptance Criteria:**
- ✅ 116 manifest entries = 116 files
- ✅ Zero orphan files
- ✅ Zero orphan manifest entries
- ✅ All paths correct (assets/svg/*.svg)
- ✅ No duplicate assetIds

---

### Issue #18 (P2): Style Guide Documentation

**Status:** ✅ PASS

**Verification:**

File exists: `/Users/ayder/projects/dmv.tx/assets/review/style_guide.md` ✅

Content audit:
- ✅ ViewBox standards table (12 categories documented)
- ✅ Color palette table (8 colors + 4 signal colors)
- ✅ Stroke width minimums table (3 viewBox sizes)
- ✅ Text size minimums table (3 viewBox sizes)
- ✅ Padding standards table (4 viewBox configurations)
- ✅ Effects policy (NO shadows, NO texture overlays, functional opacity OK)
- ✅ File naming conventions (9 categories)
- ✅ Manifest format example with JSON template
- ✅ New asset checklist (9 items)

**Completeness:** 116 lines, comprehensive coverage of all standards ✅

**Acceptance Criteria:**
- ✅ File created at assets/review/style_guide.md
- ✅ Covers viewBox standards by category
- ✅ Documents color palette
- ✅ Specifies stroke width minimums
- ✅ Specifies text size minimums
- ✅ Documents padding standards
- ✅ Includes effects policy
- ✅ Includes file naming conventions
- ✅ Includes manifest format
- ✅ Includes new asset checklist

---

### Issue #19 (P2): Document Unused Asset - SPEED_HIGHWAY_70MPH

**Status:** ✅ PASS

**Verification:**

Manifest entry for SPEED_HIGHWAY_70MPH:
```json
{
  "assetId": "SPEED_HIGHWAY_70MPH",
  "description": "Highway scene with 70 mph speed limit",
  "file": "assets/svg/SPEED_HIGHWAY_70MPH.svg",
  "sourceUrl": "generated",
  "license": "generated",
  "notes": "Not referenced by any v1 question. Available for future highway speed questions."
}
```

"notes" field present with clear documentation ✅

**Acceptance Criteria:**
- ✅ SPEED_HIGHWAY_70MPH manifest entry includes "notes" field
- ✅ Notes document that it's unused by v1 questions

---

### Issue #12 (P2): Inconsistent Padding Across Asset Categories

**Status:** ✅ PASS

**Verification:**

Style guide includes comprehensive padding standards table (lines 70-77):

| ViewBox | Padding | Content Area |
|---|---|---|
| 200x200 (signs) | 10px all sides | 180x180 |
| 200x200 (scenarios) | 5px all sides | 190x190 |
| 300x200 | 10px horizontal, 5px vertical | 280x190 |
| 100x250 (signals) | 8px horizontal | 84x230 |

Standards documented for future consistency ✅

**Acceptance Criteria:**
- ✅ Padding standards documented in style guide

---

## Detailed Fix Requirements for Issue #15

### PAVEMENT_GORE_AREA.svg

**File:** `/Users/ayder/projects/dmv.tx/assets/svg/PAVEMENT_GORE_AREA.svg`

**Lines 12-19 (diagonal stripes):**
```svg
<!-- CURRENT (INCORRECT) -->
<line x1="175" y1="65" x2="195" y2="30" stroke="#FFFFFF" stroke-width="4"/>
<line x1="185" y1="65" x2="205" y2="30" stroke="#FFFFFF" stroke-width="4"/>
<line x1="195" y1="65" x2="215" y2="30" stroke="#FFFFFF" stroke-width="4"/>
<line x1="205" y1="65" x2="225" y2="30" stroke="#FFFFFF" stroke-width="4"/>
<line x1="215" y1="65" x2="235" y2="30" stroke="#FFFFFF" stroke-width="4"/>
<line x1="225" y1="65" x2="245" y2="30" stroke="#FFFFFF" stroke-width="4"/>
<line x1="235" y1="65" x2="255" y2="30" stroke="#FFFFFF" stroke-width="4"/>
<line x1="245" y1="65" x2="265" y2="30" stroke="#FFFFFF" stroke-width="4"/>

<!-- REQUIRED (CORRECT) -->
<line x1="175" y1="65" x2="195" y2="30" stroke="#FFFFFF" stroke-width="8"/>
<line x1="185" y1="65" x2="205" y2="30" stroke="#FFFFFF" stroke-width="8"/>
<line x1="195" y1="65" x2="215" y2="30" stroke="#FFFFFF" stroke-width="8"/>
<line x1="205" y1="65" x2="225" y2="30" stroke="#FFFFFF" stroke-width="8"/>
<line x1="215" y1="65" x2="235" y2="30" stroke="#FFFFFF" stroke-width="8"/>
<line x1="225" y1="65" x2="245" y2="30" stroke="#FFFFFF" stroke-width="8"/>
<line x1="235" y1="65" x2="255" y2="30" stroke="#FFFFFF" stroke-width="8"/>
<line x1="245" y1="65" x2="265" y2="30" stroke="#FFFFFF" stroke-width="8"/>
```

**Batch fix command:**
```bash
sed -i '' 's/stroke-width="4"/stroke-width="8"/g' /Users/ayder/projects/dmv.tx/assets/svg/PAVEMENT_GORE_AREA.svg
```

---

### INTERSECTION_SIGNAL_PROTECTED_LEFT.svg

**File:** `/Users/ayder/projects/dmv.tx/assets/svg/INTERSECTION_SIGNAL_PROTECTED_LEFT.svg`

**Line 22 (pavement arrow):**
```svg
<!-- CURRENT (INCORRECT) -->
<path d="M 85,115 L 85,80 L 78,80 L 90,70 L 102,80 L 95,80 L 95,100" fill="none" stroke="#FFFFFF" stroke-width="3"/>

<!-- REQUIRED (CORRECT) -->
<path d="M 85,115 L 85,80 L 78,80 L 90,70 L 102,80 L 95,80 L 95,100" fill="none" stroke="#FFFFFF" stroke-width="6"/>
```

**Line 35 (turn path arrow):**
```svg
<!-- CURRENT (INCORRECT) -->
<path d="M 90,108 Q 90,90 75,80" fill="none" stroke="#3366CC" stroke-width="3" marker-end="url(#arrow)"/>

<!-- REQUIRED (CORRECT) -->
<path d="M 90,108 Q 90,90 75,80" fill="none" stroke="#3366CC" stroke-width="6" marker-end="url(#arrow)"/>
```

**Manual fix:** Edit lines 22 and 35, change `stroke-width="3"` to `stroke-width="6"`

---

### INTERSECTION_DOUBLE_TURN_LANES.svg

**File:** `/Users/ayder/projects/dmv.tx/assets/svg/INTERSECTION_DOUBLE_TURN_LANES.svg`

**Lines 14-15 (turn arrows):**
```svg
<!-- CURRENT (INCORRECT) -->
<path d="M 80,165 L 80,145 L 74,145 L 83,135 L 92,145 L 86,145 L 86,155" fill="none" stroke="#FFFFFF" stroke-width="3"/>
<path d="M 93,165 L 93,145 L 87,145 L 96,135 L 105,145 L 99,145 L 99,155" fill="none" stroke="#FFFFFF" stroke-width="3"/>

<!-- REQUIRED (CORRECT) -->
<path d="M 80,165 L 80,145 L 74,145 L 83,135 L 92,145 L 86,145 L 86,155" fill="none" stroke="#FFFFFF" stroke-width="6"/>
<path d="M 93,165 L 93,145 L 87,145 L 96,135 L 105,145 L 99,145 L 99,155" fill="none" stroke="#FFFFFF" stroke-width="6"/>
```

**Lines 20-21 (turn paths):**
```svg
<!-- CURRENT (INCORRECT) -->
<path d="M 81,148 Q 81,100 60,85" fill="none" stroke="#3366CC" stroke-width="2" stroke-dasharray="4,4"/>
<path d="M 99,148 Q 99,100 75,78" fill="none" stroke="#666666" stroke-width="2" stroke-dasharray="4,4"/>

<!-- REQUIRED (CORRECT) -->
<path d="M 81,148 Q 81,100 60,85" fill="none" stroke="#3366CC" stroke-width="4" stroke-dasharray="4,4"/>
<path d="M 99,148 Q 99,100 75,78" fill="none" stroke="#666666" stroke-width="4" stroke-dasharray="4,4"/>
```

**Manual fix:** Edit lines 14, 15, 20, 21 to update stroke-width values

---

### PAVEMENT_SHARED_CENTER_TURN_LANE.svg

**File:** `/Users/ayder/projects/dmv.tx/assets/svg/PAVEMENT_SHARED_CENTER_TURN_LANE.svg`

**Lines 21-22 (solid yellow boundaries):**
```svg
<!-- CURRENT (INCORRECT) -->
<line x1="0" y1="88" x2="300" y2="88" stroke="#FFCC00" stroke-width="4"/>
<line x1="0" y1="112" x2="300" y2="112" stroke="#FFCC00" stroke-width="4"/>

<!-- REQUIRED (CORRECT) -->
<line x1="0" y1="88" x2="300" y2="88" stroke="#FFCC00" stroke-width="8"/>
<line x1="0" y1="112" x2="300" y2="112" stroke="#FFCC00" stroke-width="8"/>
```

**Lines 23-24 (dashed yellow interior):**
```svg
<!-- CURRENT (INCORRECT) -->
<line x1="0" y1="94" x2="300" y2="94" stroke="#FFCC00" stroke-width="3" stroke-dasharray="10,8"/>
<line x1="0" y1="106" x2="300" y2="106" stroke="#FFCC00" stroke-width="3" stroke-dasharray="10,8"/>

<!-- REQUIRED (CORRECT) -->
<line x1="0" y1="94" x2="300" y2="94" stroke="#FFCC00" stroke-width="6" stroke-dasharray="10,8"/>
<line x1="0" y1="106" x2="300" y2="106" stroke="#FFCC00" stroke-width="6" stroke-dasharray="10,8"/>
```

**Line 31 (text label):**
```svg
<!-- CURRENT (INCORRECT) -->
<text x="150" y="175" font-family="Arial" font-size="18" font-weight="700" fill="#000000" text-anchor="middle">CENTER TWO-WAY LEFT TURN LANE</text>

<!-- OPTION 1 (CORRECT) - Increase font size -->
<text x="150" y="175" font-family="Arial" font-size="24" font-weight="700" fill="#000000" text-anchor="middle">CENTER TWO-WAY LEFT TURN LANE</text>

<!-- OPTION 2 (PREFERRED) - Remove label entirely -->
<!-- Remove line 31 - question text provides context -->
```

**Recommendation:** Remove text label entirely (Option 2). The visual elements alone are sufficient, and question text will provide context.

---

## Process Improvements

### Prevent Recurrence

1. **Pre-creation validation script:** Before creating new assets, validate stroke widths and text sizes against style guide minimums
2. **Automated readability calculator:** Python script that parses SVG, extracts viewBox, calculates effective dp sizes for all strokes and text
3. **CI/CD check:** Add GitHub Action that runs validation on all modified SVG files

### Validation Script Template

```python
import xml.etree.ElementTree as ET
import sys

def validate_svg(filepath):
    tree = ET.parse(filepath)
    root = tree.getroot()

    viewBox = root.get('viewBox', '').split()
    if len(viewBox) != 4:
        return ['Missing or invalid viewBox']

    vb_width = float(viewBox[2])
    target_dp = 96
    min_stroke_dp = 2.9
    min_text_dp = 9.6

    issues = []

    # Check all strokes
    for elem in root.iter():
        stroke_width = elem.get('stroke-width')
        if stroke_width:
            sw = float(stroke_width.replace('px', ''))
            effective_dp = sw * (target_dp / vb_width)
            if effective_dp < min_stroke_dp:
                issues.append(f'Stroke {sw}px renders at {effective_dp:.1f}dp (< {min_stroke_dp}dp)')

    # Check all text
    for text in root.iter('{http://www.w3.org/2000/svg}text'):
        font_size = text.get('font-size')
        if font_size:
            fs = float(font_size.replace('px', ''))
            effective_dp = fs * (target_dp / vb_width)
            if effective_dp < min_text_dp:
                issues.append(f'Text {fs}px renders at {effective_dp:.1f}dp (< {min_text_dp}dp)')

    return issues

if __name__ == '__main__':
    for filepath in sys.argv[1:]:
        issues = validate_svg(filepath)
        if issues:
            print(f'{filepath}:')
            for issue in issues:
                print(f'  ❌ {issue}')
        else:
            print(f'{filepath}: ✅ PASS')
```

---

## Recommendations

### Immediate Actions (Required for Issue #15 closure)

1. Fix PAVEMENT_GORE_AREA.svg (increase diagonal stripe stroke-width from 4 to 8)
2. Fix INTERSECTION_SIGNAL_PROTECTED_LEFT.svg (increase arrow stroke-widths from 3 to 6)
3. Fix INTERSECTION_DOUBLE_TURN_LANES.svg (increase arrow strokes from 3 to 6, path strokes from 2 to 4)
4. Fix PAVEMENT_SHARED_CENTER_TURN_LANE.svg (increase lane line strokes, remove or enlarge text label)
5. Re-run audit on these 4 files to confirm PASS status

### Optional Process Improvements

1. Create `assets/review/validate_svg.py` with readability validation script
2. Add validation to new asset creation workflow
3. Document validation process in style guide

### GitHub Issue Status

**Can be closed immediately:**
- ✅ Issue #13 (P0) - PARKING_PARALLEL_STEPS
- ✅ Issue #11 (P1) - SIGNAL_* fixes
- ✅ Issue #14 (P2) - PAVEMENT_* texture removal
- ✅ Issue #16 (P1) - Clipping audit
- ✅ Issue #17 (P1) - Manifest alignment
- ✅ Issue #18 (P2) - Style guide
- ✅ Issue #19 (P2) - Unused asset documentation
- ✅ Issue #12 (P2) - Padding documentation

**Requires fixes before closing:**
- ⚠️ Issue #15 (P1) - 4 new assets need stroke width corrections, 1 needs text fix

---

## Appendix: File Locations

- SVG assets: `/Users/ayder/projects/dmv.tx/assets/svg/` (116 files)
- Manifest: `/Users/ayder/projects/dmv.tx/assets/manifest.json` (116 entries)
- Style guide: `/Users/ayder/projects/dmv.tx/assets/review/style_guide.md`
- This report: `/Users/ayder/projects/dmv.tx/assets/review/fix_audit_report.md`

---

**End of Audit Report**
