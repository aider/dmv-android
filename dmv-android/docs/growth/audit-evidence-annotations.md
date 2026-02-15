# Image Quality Audit — Technical Evidence & Annotations

**Date:** 2026-02-14
**Auditor:** SVG Review Agent
**Methodology:** Technical SVG analysis + mobile rendering simulation

This document provides detailed evidence for the findings in the Image Quality Audit Report, with technical annotations showing why specific assets pass or fail mobile readability criteria.

---

## Mobile Rendering Math Reference

**Formula:**
```
effective_dp = element_size_px × (target_dp / viewBox_dimension)
```

**Quality Thresholds:**
- Minimum stroke width: 2.0dp (visible on mobile)
- Minimum text size: 9.6dp (legible on mobile)
- Target render sizes: 96dp (primary), 48dp (minimum)

**Example calculation:**
```
Asset: MUTCD_S4-3_SCHOOL_SPEED_LIMIT_20
viewBox: 200×200
Font size: 8px
Render target: 96dp

Effective dp = 8 × (96 / 200) = 3.84dp
Result: FAIL (< 9.6dp minimum for text legibility)
```

---

## Section 1: Critical Failures (P0) — 8 Annotated Examples

### 1.1 INTERSECTION_SCHOOL_BUS_STOPPED (P0)

**Asset ID:** INTERSECTION_SCHOOL_BUS_STOPPED
**Usage:** 2 questions
**Question context:** School bus safety, stopping requirements

**Technical Analysis:**
```
viewBox: 300×200
Min stroke width: 1.5px → 0.72dp at 96dp render ❌
Min font size: 5.5px → 2.64dp at 96dp render ❌
```

**Issues identified:**
1. **Stop sign border:** 1.5px stroke renders at 0.72dp (invisible on mobile)
2. **STOP text:** 5.5px font renders at 2.64dp (unreadable)
3. **Vehicle outlines:** 1.5px strokes disappear at mobile size
4. **Road markings:** Barely visible at 48dp minimum size

**User impact:** Learners cannot see the critical stop sign details, defeating the educational purpose of showing school bus stop procedure.

**Proposed fix:** Increase stop sign border to 8px (3.84dp), STOP text to 20px (9.6dp), vehicle outlines to 6px (2.88dp)

**Effort:** Small (0.5 hours)

---

### 1.2 INTERSECTION_4WAY_STOP (P0)

**Asset ID:** INTERSECTION_4WAY_STOP
**Usage:** 1 question
**Question context:** Four-way stop sign rules, right-of-way

**Technical Analysis:**
```
viewBox: 300×200
Min stroke width: 1.2px → 0.58dp at 96dp render ❌
Direction arrows: 2px → 0.96dp at 96dp render ❌
```

**Issues identified:**
1. **Stop sign borders:** 1.2px strokes render at 0.58dp (invisible)
2. **Direction arrows:** 2px strokes render at 0.96dp (barely visible)
3. **Lane markings:** Thin and difficult to distinguish
4. **Visual hierarchy:** Lost due to uniform thinness

**User impact:** The entire instructional value is lost — learner cannot see which vehicles have stop signs or understand the right-of-way sequence.

**Proposed fix:** Stop sign borders to 8px (3.84dp), direction arrows to 8px, lane markings to 6px

**Effort:** Small (0.5 hours)

---

### 1.3 MUTCD_S4-3_SCHOOL_SPEED_LIMIT_20 (P0)

**Asset ID:** MUTCD_S4-3_SCHOOL_SPEED_LIMIT_20
**Usage:** 3 questions (highest P0 usage)
**Question context:** School zone speed limits

**Technical Analysis:**
```
viewBox: 200×200
Border stroke: 2px → 0.96dp at 96dp render ❌
Main text "20": 24px → 11.52dp at 96dp render ✓
Sub-text "WHEN CHILDREN / PRESENT": 8px → 3.84dp at 96dp render ❌
```

**Issues identified:**
1. **Border:** 2px stroke at 0.96dp is barely visible
2. **Sub-text:** 8px font at 3.84dp is completely illegible on mobile
3. **Visual balance:** Sub-text disappears, making sign look incomplete

**User impact:** Critical instructional text "WHEN CHILDREN PRESENT" cannot be read, potentially causing confusion about when the speed limit applies.

**Proposed fix:**
- Option A: Increase sub-text to 20px (9.6dp) and enlarge viewBox to 200×250
- Option B: Remove sub-text entirely (questiontext already provides context)
- Increase border to 6px (2.88dp)

**Effort:** Small (0.5 hours)

---

### 1.4 SIGNAL_GREEN_ARROW_LEFT (P0)

**Asset ID:** SIGNAL_GREEN_ARROW_LEFT
**Usage:** 2 questions
**Question context:** Protected left turn signals

**Technical Analysis:**
```
viewBox: 100×250
Signal housing stroke: 2px → 0.96dp at 96dp render ❌
Arrow fill: solid (good) ✓
```

**Issues identified:**
1. **Housing outline:** 2px stroke renders at 0.96dp (barely visible)
2. **Signal head separation:** Thin borders make it hard to distinguish signal from background
3. **At 48dp:** Housing outline completely disappears

**User impact:** Signal appears as floating arrow without clear housing context, confusing for learners trying to understand signal types.

**Proposed fix:** Increase housing stroke to 6px (5.76dp at 96dp)

**Effort:** Small (0.5 hours)

---

### 1.5 SIGNAL_RED_ARROW_LEFT (P0)

**Asset ID:** SIGNAL_RED_ARROW_LEFT
**Usage:** 2 questions
**Question context:** Protected left turn prohibition

**Technical Analysis:**
```
viewBox: 100×250
Signal housing stroke: 2px → 0.96dp at 96dp render ❌
(Same issues as SIGNAL_GREEN_ARROW_LEFT)
```

**Issues identified:** Identical to SIGNAL_GREEN_ARROW_LEFT

**Proposed fix:** Increase housing stroke to 6px (5.76dp)

**Effort:** Small (0.5 hours) — can be batched with green arrow fix

---

### 1.6 MUTCD_R10-7_DO_NOT_PASS (P0)

**Asset ID:** MUTCD_R10-7_DO_NOT_PASS
**Usage:** 2 questions
**Question context:** No passing zones

**Technical Analysis:**
```
viewBox: 200×200
Border stroke: 2px → 0.96dp at 96dp render ❌
Text font: 24px → 11.52dp at 96dp render ✓
Pennant shape: custom path ✓
```

**Issues identified:**
1. **Border:** 2px stroke renders at 0.96dp (barely visible)
2. **Pennant geometry:** Correct, but border too thin to show distinctive pennant shape at mobile size
3. **At 48dp:** Border disappears entirely, sign loses recognizability

**User impact:** Learners cannot identify this distinctive pennant sign shape, which is critical for recognizing no-passing zones in real driving.

**Proposed fix:** Increase border to 8px (3.84dp)

**Effort:** Small (0.5 hours)

---

### 1.7 SPEED_FOLLOWING_DISTANCE_3SEC (P0)

**Asset ID:** SPEED_FOLLOWING_DISTANCE_3SEC
**Usage:** 2 questions
**Question context:** Safe following distance rule

**Technical Analysis:**
```
viewBox: 300×150
Vehicle outlines: 8px → 3.84dp at 96dp render ✓
Label text "3 SEC": 14px → 6.72dp at 96dp render ❌
```

**Issues identified:**
1. **Label text:** 14px font renders at 6.72dp (below 9.6dp legibility threshold)
2. **Instructional labels:** Critical information becomes unreadable

**User impact:** The "3 SEC" label is the core teaching element — without it, the visual is just two cars with no clear instructional message.

**Proposed fix:**
- Option A: Increase label text to 20px (9.6dp)
- Option B: Remove label text (question text already states "3 seconds")

**Effort:** Small (0.5 hours)

---

### 1.8 MUTCD_R7-107_HANDICAPPED_PARKING (P0)

**Asset ID:** MUTCD_R7-107_HANDICAPPED_PARKING
**Usage:** 2 questions
**Question context:** Disabled parking regulations

**Technical Analysis:**
```
viewBox: 200×200
Border stroke: 3px → 1.44dp at 96dp render ❌
Symbol stroke: 4px → 1.92dp at 96dp render ✓ (marginal)
Text "PARKING": 11px → 5.28dp at 96dp render ❌
Text "ONLY": 8px → 3.84dp at 96dp render ❌
```

**Issues identified:**
1. **Border:** 3px stroke at 1.44dp is too thin
2. **Text:** Both "PARKING" (5.28dp) and "ONLY" (3.84dp) fall below legibility threshold
3. **Symbol:** Wheelchair icon is borderline acceptable but could be bolder

**User impact:** Text is illegible, making it difficult for learners to distinguish this from other parking signs.

**Proposed fix:** Increase text to 20px (9.6dp), border to 6px (2.88dp)

**Effort:** Small (0.5 hours)

---

## Section 2: Pass Examples (Reference Quality) — 5 Annotated

### 2.1 MUTCD_R15-1_RAILROAD_CROSSING ✅

**Asset ID:** MUTCD_R15-1_RAILROAD_CROSSING
**Usage:** 3 questions (highest PASS usage)
**Question context:** Railroad crossing warnings

**Technical Analysis:**
```
viewBox: 200×200
Border stroke: 6px → 2.88dp at 96dp render ✅
"RR" text: 36px → 17.28dp at 96dp render ✅
X symbol: 6px stroke → 2.88dp at 96dp render ✅
```

**Success factors:**
1. **Bold strokes:** 6px border and X create strong visual presence
2. **Large text:** 36px "RR" is highly legible at all sizes
3. **High contrast:** Black on white with no gradients
4. **Simple geometry:** Clean crossbuck shape, iconic design
5. **MUTCD compliant:** Correct circle shape and proportions

**Why it passes:** All critical elements exceed minimum thresholds. At 48dp minimum render, sign remains recognizable and legible.

**Scores:** Readability 5, Semantic Clarity 5, Contrast 5, Consistency 5

---

### 2.2 MUTCD_W11-1_PEDESTRIAN_CROSSING ✅

**Asset ID:** MUTCD_W11-1_PEDESTRIAN_CROSSING
**Usage:** 2 questions
**Question context:** Pedestrian crossing warnings

**Technical Analysis:**
```
viewBox: 200×200
Border stroke: 6px → 2.88dp at 96dp render ✅
Icon stroke: 6px → 2.88dp at 96dp render ✅
No text elements (icon-based) ✅
```

**Success factors:**
1. **No text dependency:** Pure icon design eliminates text legibility issues
2. **Bold icon:** 6px strokes create clear pedestrian silhouette
3. **Correct geometry:** Diamond shape per MUTCD standards
4. **High contrast:** Black icon on yellow background
5. **Simple composition:** Single figure, no unnecessary detail

**Why it passes:** Icon-based design is inherently mobile-friendly. Strong strokes and simple shape work at all sizes.

**Scores:** Readability 5, Semantic Clarity 5, Contrast 5, Consistency 5

---

### 2.3 MUTCD_W11-2_BICYCLE_CROSSING ✅

**Asset ID:** MUTCD_W11-2_BICYCLE_CROSSING
**Usage:** 2 questions
**Question context:** Bicycle crossing warnings

**Technical Analysis:**
```
viewBox: 200×200
Border stroke: 6px → 2.88dp at 96dp render ✅
Icon stroke: 6px → 2.88dp at 96dp render ✅
No text elements (icon-based) ✅
```

**Success factors:**
(Same as MUTCD_W11-1_PEDESTRIAN_CROSSING — follows same design pattern)

1. **Icon clarity:** Bold bicycle silhouette
2. **Correct MUTCD geometry:** Diamond warning sign
3. **No text:** Pure symbolic communication
4. **Strong strokes:** 6px throughout

**Why it passes:** Demonstrates best practice for warning signs — bold, simple, symbolic.

**Scores:** Readability 5, Semantic Clarity 5, Contrast 5, Consistency 5

---

### 2.4 MUTCD_W20-1_ROAD_CONSTRUCTION ✅

**Asset ID:** MUTCD_W20-1_ROAD_CONSTRUCTION
**Usage:** 2 questions
**Question context:** Construction zone warnings

**Technical Analysis:**
```
viewBox: 200×200
Border stroke: 6px → 2.88dp at 96dp render ✅
Icon elements: 6px strokes → 2.88dp at 96dp render ✅
Orange diamond background ✅
```

**Success factors:**
1. **Construction icon:** Worker/shovel symbol with bold 6px strokes
2. **Color coding:** Orange background (standard for construction)
3. **No text:** Pure icon design
4. **High visibility:** Orange + black provides strong contrast

**Why it passes:** Bold design suitable for mobile. Icon communicates meaning without text dependency.

**Scores:** Readability 5, Semantic Clarity 5, Contrast 5, Consistency 5

---

### 2.5 MUTCD_W20-7_FLAGGER_AHEAD ✅

**Asset ID:** MUTCD_W20-7_FLAGGER_AHEAD
**Usage:** 2 questions
**Question context:** Flagger warnings in construction zones

**Technical Analysis:**
```
viewBox: 200×200
Border stroke: 6px → 2.88dp at 96dp render ✅
Flagger icon: 6px strokes → 2.88dp at 96dp render ✅
Orange diamond background ✅
```

**Success factors:**
1. **Clear icon:** Flagger figure with flag clearly visible
2. **Bold strokes:** 6px throughout ensures visibility
3. **Orange construction color:** Per MUTCD standards
4. **No text:** Icon-based design

**Why it passes:** Follows proven pattern of bold, text-free warning signs. Mobile-ready design.

**Scores:** Readability 5, Semantic Clarity 5, Contrast 5, Consistency 5

---

## Section 3: Category-Specific Findings

### 3.1 Warning Signs (W-series) — Best Performance ✅

**Overall health:** 100% PASS (16/16 assets)

**Pattern observed:**
All warning signs follow the same successful template:
- Diamond shape (viewBox 200×200)
- 6px border stroke (2.88dp at 96dp)
- Icon-based design (no text)
- 6px icon strokes
- Yellow background, black foreground

**Why this category succeeds:**
1. **Standardized template:** Consistent application of MUTCD standards
2. **No text dependency:** Pure symbolic communication
3. **Bold design:** 6px minimum strokes throughout
4. **Simple composition:** Single icon per sign, minimal complexity

**Recommendation:** Use warning signs as the reference standard for all other categories.

---

### 3.2 Traffic Signals — Worst Performance ❌

**Overall health:** 0% PASS (0/13 assets)

**Pattern observed:**
All 13 signal assets fail due to thin housing strokes:
- viewBox: 100×250 (vertical)
- Housing stroke: 2px → 0.96dp at 96dp ❌
- Signal lenses: adequate (filled circles)

**Why this category fails:**
1. **Consistent design flaw:** All signals share the same thin housing stroke
2. **Housing disappears:** At mobile sizes, signals appear as floating colored circles
3. **Missing context:** Without visible housing, signal type is ambiguous

**Recommendation:** Batch fix all 13 signals by increasing housing stroke to 6px (5.76dp at 96dp)

---

### 3.3 Intersection Scenes — Consistent Failure ❌

**Overall health:** 0% PASS (0/8 assets)

**Pattern observed:**
All intersection scenes fail due to:
- viewBox: 300×200 (wide)
- Thin strokes: 1.2-2px → 0.58-0.96dp at 96dp ❌
- Small text: 5.5-14px → 2.64-6.72dp at 96dp ❌
- Complex composition: Multiple vehicles, roads, signs

**Why this category fails:**
1. **Complexity meets thin strokes:** Lots of detail, all too thin
2. **Text labels:** Instructional labels become illegible
3. **Scaled-down complexity:** Desktop-sized designs compressed to mobile

**Recommendation:** Redesign intersection scenes with "optical sizing" approach:
- Increase all strokes to 6-8px
- Remove or enlarge text labels to 20px
- Simplify vehicle representations
- Increase viewBox to 400×300 if needed for breathing room

---

### 3.4 Pavement Markings — Mixed Performance ⚠️

**Overall health:** 74% PASS (20/27 assets)

**Failure pattern (7 assets):**
- Text-based markings (e.g., "ONLY", "SCHOOL ZONE") with fonts <20px
- Arrow markings with stroke <6px

**Success pattern (20 assets):**
- Bold strokes ≥6px (white/yellow lines)
- Filled shapes (handicap symbol, diamonds)
- Large text ≥20px

**Recommendation:** Fix the 7 failures by increasing font sizes to 20px and arrow strokes to 6px.

---

## Section 4: Common Design Patterns

### ✅ Patterns that Pass

1. **Icon-based design (no text)**
   - Example: Warning signs, railroad crossing
   - Success rate: 95%

2. **Bold strokes ≥6px**
   - Renders at ≥2.88dp on 96dp target
   - Visible at 48dp minimum size

3. **Large text ≥20px**
   - Renders at ≥9.6dp on 96dp target (200px viewBox)
   - Legible at mobile sizes

4. **Simple composition**
   - Single icon or symbol
   - Minimal elements
   - High contrast

5. **Standardized viewBox by category**
   - Signs: 200×200
   - Pavement: 300×200
   - Signals: 100×250

### ❌ Patterns that Fail

1. **Thin strokes <4px**
   - Renders at <1.92dp on 96dp target
   - Disappears at mobile sizes

2. **Small text <16px**
   - Renders at <7.68dp on 96dp target (200px viewBox)
   - Illegible on mobile

3. **Complex compositions with thin elements**
   - Intersection scenes with many thin lines
   - Detail lost at mobile scale

4. **Inconsistent viewBox**
   - Causes visual "jumping" in UI
   - Makes sizing unpredictable

---

## Section 5: Recommended Standards

Based on this audit, the following standards are recommended for all future SVG assets:

### Stroke Widths
- **Minimum:** 6px in 200px viewBox (2.88dp at 96dp)
- **Preferred:** 8px in 200px viewBox (3.84dp at 96dp)
- **For critical elements:** 10px in 200px viewBox (4.8dp at 96dp)

### Text Sizes
- **Minimum:** 20px in 200px viewBox (9.6dp at 96dp)
- **Preferred:** 24px in 200px viewBox (11.52dp at 96dp)
- **For primary text:** 36px in 200px viewBox (17.28dp at 96dp)

### viewBox Standards by Category
- **Regulatory signs:** 200×200 (square)
- **Warning signs:** 200×200 (square, diamond orientation via rotation)
- **Guide signs:** 200×150 (landscape)
- **Speed limit signs:** 150×200 (portrait)
- **Pavement markings:** 300×200 (landscape)
- **Traffic signals:** 100×250 (vertical)
- **Intersection scenes:** 400×300 (wide landscape, for breathing room)

### Design Philosophy
- **Prefer icon-based over text-based** when possible
- **Optical sizing, not geometric accuracy:** Mobile requires bolder elements than print
- **Test at 48dp:** If readable at 48dp, will be excellent at 96dp
- **Simplify for mobile:** Remove non-essential detail

---

## Section 6: Validation Checklist

Use this checklist for all new/revised SVG assets:

- [ ] **viewBox** matches category standard
- [ ] **Minimum stroke** ≥6px (for 200px viewBox)
- [ ] **Minimum text** ≥20px (for 200px viewBox) OR no text
- [ ] **Calculate effective dp:** All elements ≥2.0dp strokes, ≥9.6dp text
- [ ] **Test at 48dp:** Asset recognizable and key elements visible
- [ ] **No excessive complexity:** Element count <60 for icons
- [ ] **High contrast:** No low-opacity overlays (<0.5)
- [ ] **MUTCD compliance** (if applicable): Correct geometry, colors, proportions
- [ ] **Semantic clarity:** Instructional meaning is unambiguous
- [ ] **Consistent style:** Matches other assets in category

---

**Evidence compiled by:** SVG Review Agent
**Date:** 2026-02-14
**Supporting data:** image-quality-audit-2026-02.csv (136 rows)
**Report:** image-quality-audit-2026-02.md (203 lines)
