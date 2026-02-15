# Screenshot Evidence - Image Quality Audit 2026-02

## P0 Failure Examples

### 1. SPECIAL_WORK_ZONE_FLAGGER (P0)
**File:** `03_quiz_question.png`
**Question:** TX-SPC-0033 "When you see a flagger holding a STOP paddle, you must:"

**Issues Identified:**
- ❌ Traffic cone outlines: 1px stroke = **0.48dp** (invisible on mobile)
- ❌ STOP paddle text: 9px font = **4.3dp** (illegible)
- ❌ Bottom label text: 14px font = **6.7dp** (borderline, should be 20px min)
- ❌ Flagger body strokes: 1-2px = **0.48-0.96dp** (barely visible)

**Measurements:**
- ViewBox: 200×200
- Min stroke: 0.48dp (cone border)
- Min text: 4.3dp ("STOP" on paddle)
- Readability score: 1/5

**Proposed Fix:**
- Increase cone strokes to 6px (2.88dp)
- Enlarge STOP paddle and text to 20px (9.6dp)
- Increase bottom label to 20px or remove (rely on question text)

**User Impact:** 1 question affected, but critical work zone safety concept

---

### 2. INTERSECTION_SCHOOL_BUS_STOPPED (P0)
**Status:** Not yet captured in quiz screenshots
**Question:** TX-ROW-0064, TX-SPC-0001

**Technical Analysis:**
- ❌ Min stroke: **0.72dp** (stop sign borders, vehicle outlines)
- ❌ "STOP" text on extended arm: 5.3px font = **2.6dp** (tiny)
- ❌ School bus details: 1.5px strokes (faint)

**Critical Issue:** Stop sign on bus arm is key learning cue but nearly invisible

---

### 3. INTERSECTION_4WAY_STOP (P0)
**Status:** Not yet captured
**Question:** TX-ROW-0001

**Technical Analysis:**
- ❌ Stop sign octagon borders: 1.2px stroke = **0.58dp** (invisible)
- ❌ Stop lines: 6px = **2.88dp** (marginal, should be 8px)
- ❌ Yellow center lines: 4px = **1.92dp** (too thin)
- ❌ Direction arrows: 3px stroke = **1.44dp** (faint)

**Critical Issue:** Four stop signs are primary learning cue but borders vanish at mobile size

---

### 4. MUTCD_R1-1_STOP Sign (P0)
**Status:** Referenced in TX-ROW-0007, TX-SIG-0001
**Question:** "What shape is a STOP sign?"

**Mixed Quality:**
- ✅ "STOP" text: 52px font = **24.96dp** (EXCELLENT)
- ✅ Red fill: High contrast, clear shape
- ✅ Octagon geometry: Correct MUTCD proportions
- ❌ Inner white border: 3px stroke = **1.44dp** (too thin, should be 6px)

**Issue:** Sign is recognizable but lacks the crisp border definition expected for this iconic sign

---

### 5. MUTCD_S4-3_SCHOOL_SPEED_LIMIT_20 (P0)
**Status:** Used by 3 questions (highest P0 usage)
**Questions:** TX-SIG-0022, TX-SPC-0004, TX-SPD-0022

**Technical Analysis:**
- ViewBox: 150×200 (vertical sign)
- ✅ "SPEED LIMIT" text: 16px = **10.2dp** (acceptable)
- ✅ "20" numeral: 40px = **25.6dp** (excellent)
- ❌ "WHEN CHILDREN" text: 11px = **7.0dp** (illegible)
- ❌ "PRESENT" text: 11px = **7.0dp** (illegible)
- ❌ Sign border: 2px = **1.28dp** (thin)

**Issue:** Main speed limit is clear, but conditional text is unreadable

---

### 6. Signal Housing Strokes (P0 - Multiple Assets)
**Affected:** SIGNAL_GREEN_ARROW_LEFT, SIGNAL_RED_ARROW_LEFT
**Usage:** 2 questions each

**Common Issue:**
- Traffic light housing: 2px stroke = **0.96dp** (barely visible)
- Arrow fills: Good (filled shapes, not strokes)
- Light circles: Good contrast

**Fix:** Increase housing border to 6px (2.88dp)

---

### 7. SPEED_FOLLOWING_DISTANCE_3SEC (P0)
**Questions:** TX-SAF-0003, TX-SPD-0031

**Technical Analysis:**
- ✅ Vehicle shapes: 8px stroke = **3.84dp** (good)
- ✅ Distance markers: Clear visual spacing
- ❌ "MIN SAFE" label: 14px = **6.7dp** (too small)
- ❌ "FOLLOWING DISTANCE" label: 14px = **6.7dp** (too small)

**Fix:** Increase text to 20px (9.6dp) or remove labels entirely

---

### 8. Parking Signs with Text Issues (P0)
**Assets:** MUTCD_R8-3a_NO_PARKING_ANYTIME, MUTCD_R7-107_HANDICAPPED_PARKING
**Issues:**
- Text sizes 8-12px = 5.3-8.6dp (below 9.6dp threshold)
- Border strokes 1.0-1.9px = 0.96-1.92dp (too thin)

---

## P1 Failure Examples

### 9. MUTCD_R1-2_YIELD (P1)
**Usage:** 3 questions (most-used P1 asset)
**Questions:** TX-ROW-0011, TX-SIG-0002, TX-SIG-0003

**Nearly Passing:**
- ✅ "YIELD" text: 32px = **15.36dp** (excellent)
- ✅ Triangle shape: Perfect MUTCD geometry
- ✅ Red border: High contrast
- ⚠️ Border stroke: 4px = **1.92dp** (just below 2.0dp threshold)

**Fix:** Increase border to 5px (2.4dp) → would PASS all criteria

---

### 10. Intersection Scenarios (P1)
**Assets:** INTERSECTION_UNCONTROLLED, INTERSECTION_T_STOP, INTERSECTION_ROUNDABOUT, etc.

**Common Pattern:**
- Strokes 1.4-1.9dp (marginal)
- Missing some training cues (vehicles, arrows)
- Generally understandable but not optimal

---

## PASS Examples (Good Quality Reference)

### 11. MUTCD_W11-1_PEDESTRIAN_CROSSING (PASS)
**Usage:** 2 questions
**Scores:** 5/5/5/4

**Why It Works:**
- ✅ Sign border: 6px = **2.88dp** (crisp)
- ✅ Pedestrian icon: Bold, filled shapes with 6px strokes
- ✅ High contrast: Black on yellow
- ✅ No text — icon is self-explanatory
- ✅ Simple, iconic design

**Render Quality:** Excellent at both 96dp and 48dp

---

### 12. MUTCD_W11-2_BICYCLE_CROSSING (PASS)
**Usage:** 2 questions
**Scores:** 5/5/5/4

**Why It Works:**
- Same quality factors as pedestrian crossing
- Bold bicycle icon with thick strokes
- Perfect for mobile rendering

---

### 13. PAVEMENT_HANDICAP_SYMBOL (PASS)
**Usage:** 2 questions
**Scores:** 5/4/4/4

**Why It Works:**
- ✅ Wheelchair icon: 8px strokes = **3.84dp** (thick, bold)
- ✅ Filled shapes (wheelchair seat, person)
- ✅ Excellent contrast on pavement background
- ✅ No text needed

**Render Quality:** Icon remains clear even at thumbnail sizes

---

### 14. MUTCD_R15-1_RAILROAD_CROSSING (PASS)
**Usage:** 3 questions (most-used PASS asset)
**Scores:** 5/5/5/4

**Why It Works:**
- ✅ Border: 6px = **2.88dp**
- ✅ "X" symbol: Bold, thick strokes
- ✅ "RR" letters: Large, clear (20px = 9.6dp)
- ✅ Iconic yellow circle design

**Best Practice Example:** This asset demonstrates ideal mobile optimization

---

### 15. Warning Signs (W-Series) - Multiple PASS
**Assets:** MUTCD_W3-1_MERGE, MUTCD_W20-1_ROAD_CONSTRUCTION, MUTCD_W20-7_FLAGGER_AHEAD, MUTCD_S1-1_SCHOOL_CROSSING

**Common Success Factors:**
- All use 6px borders (2.88dp)
- Bold icon-based design (no text or minimal text)
- Yellow diamond high-contrast scheme
- Simple, recognizable symbols

**Pattern:** Warning signs are the **highest-quality category** (60%+ PASS rate)

---

## Summary Statistics

### Screenshots Captured
- ✅ Quiz context screenshot: 1 (SPECIAL_WORK_ZONE_FLAGGER)
- ✅ Technical analysis: All 108 assets via SVG parsing
- ✅ Pass examples identified: 48 assets
- ✅ Failure examples documented: 60 assets (14 P0, 46 P1)

### Evidence Quality
- **Failure examples (P0):** 8 detailed (meets ≥8 requirement)
- **Pass examples:** 5 detailed (meets ≥5 requirement)
- **Total annotated:** 15 assets with specific dp measurements
- **CSV data:** 108 assets with full rubric scoring

### Mobile Render Validation
All measurements confirmed against:
- **Target render size:** 96dp (primary)
- **Minimum size:** 48dp (distinguishable)
- **Threshold enforcement:** 2.0dp strokes, 9.6dp text

---

## Recommended Next Actions

1. **Capture additional in-app screenshots** showing:
   - INTERSECTION_4WAY_STOP in quiz context
   - INTERSECTION_SCHOOL_BUS_STOPPED in quiz context
   - Signal assets in quiz context
   - Direct comparison: P0 failure vs PASS example side-by-side

2. **Create visual annotations** with:
   - Red circles highlighting invisible strokes
   - Text size callouts with dp measurements
   - Before/After mockups for Top 15 fixes

3. **Generate automated validation** script to:
   - Parse SVG attributes
   - Calculate effective dp sizes
   - Flag strokes <2.0dp and text <9.6dp
   - Enforce style guide standards

---

**Annotation prepared by:** SVG Review Agent
**Date:** 2026-02-14
**Source audit:** `image-quality-audit-2026-02.md`
