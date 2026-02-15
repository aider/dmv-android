# DMV Training Image Style Guide
**Learner-Focused Visual Design Standards**

## Purpose

This guide ensures every training image in the DMV quiz app is **clear, accurate, and helpful** for learners. These aren't just technical specifications—they're design principles that prioritize **educational effectiveness** over aesthetic preferences.

**Core Principle:** If a learner can't understand the image at a glance on a mobile screen, it fails its educational purpose.

---

## The Three Pillars of Learner-Focused Visuals

### 1. **Instant Recognition**
Learners should identify what they're looking at within 1 second. No squinting, no guessing.

### 2. **Accurate Representation**
Signs must match real-world MUTCD specs. A STOP sign with wonky angles teaches the wrong mental model.

### 3. **Mobile-First Clarity**
If it's not readable at 48-96dp on a phone screen, it doesn't belong in the app.

---

## Good Examples (Learn From These)

### ✅ Example 1: MUTCD_R1-1_STOP

**What Makes It Good:**
- **Perfect geometry**: True regular octagon with all angles at 135° (matches real signs)
- **High contrast**: White text on red background is instantly readable
- **Proper padding**: 10% spacing ensures text never touches borders
- **Mobile-ready**: Recognizable even at 48dp

**Learner Impact:** Student sees exactly what they'll encounter on the road. No confusion about sign shape or meaning.

**Technical Details:**
```
ViewBox: 0 0 200 200
Text: "STOP" at 52px font-weight 900
Padding: 20px all sides (10%)
Colors: Red #C1272D, White #FFFFFF
```

---

### ✅ Example 2: INTERSECTION_4WAY_STOP

**What Makes It Good:**
- **Training cues present**: Stop lines at all four approaches, clear intersection structure
- **Visual hierarchy**: Ego vehicle (blue) stands out from other vehicles (gray)
- **Educational elements**: Shows all four vehicles stopped, teaching right-of-way context
- **Stroke width**: 6px stop lines are visible at small sizes

**Learner Impact:** Student learns not just what a 4-way stop looks like, but how traffic should behave. The visual teaches the concept.

**Technical Details:**
```
ViewBox: 0 0 200 200
Stop lines: 6px stroke (renders at 2.9dp)
Vehicle colors: #3366CC (ego), #666666 (others)
Road surface: #4A4A4A
```

---

### ✅ Example 3: PAVEMENT_SOLID_YELLOW_LINE

**What Makes It Good:**
- **Single focus**: Shows only the marking being taught, no distractions
- **Realistic context**: Gray road surface provides proper contrast
- **Proper width**: 8px yellow line is clearly visible at mobile sizes
- **No clutter**: No unnecessary labels—the visual speaks for itself

**Learner Impact:** Student can quickly identify this marking type on subsequent questions. Clean visual = clear memory.

**Technical Details:**
```
ViewBox: 0 0 300 200
Line stroke: 8px (renders at 2.6dp at 96dp)
Color: #FFCC00 (MUTCD yellow)
No text labels (question provides context)
```

---

## Bad Examples (Avoid These Mistakes)

### ❌ Bad Example 1: STOP Sign with Irregular Angles (Pre-Fix)

**What Was Wrong:**
- **Geometry failure**: Angles alternated 125°/144° instead of 135°
- **Educational harm**: Teaches incorrect mental model of what a STOP sign looks like
- **MUTCD violation**: Doesn't match real-world regulatory spec

**Why It Matters:**
A driver education app must show accurate sign geometry. Students develop visual recognition skills—wrong shapes teach wrong patterns.

**The Fix:**
Regenerated with mathematically perfect octagon using proper vertex calculations.

---

### ❌ Bad Example 2: DO NOT ENTER Sign as Circle (Pre-Fix)

**What Was Wrong:**
- **Wrong shape**: Used circle instead of square with rounded corners
- **Recognition error**: Shape is a critical identifier for this sign type
- **Standards violation**: MUTCD R5-1 specifies square, not circle

**Why It Matters:**
Sign shape is part of the training. A student who learns "DO NOT ENTER = circle" will be confused by the real square signs on roads.

**The Fix:**
Changed from `<circle>` to square `<rect>` with rounded corners (rx="8").

---

### ❌ Bad Example 3: Speed Limit Signs with 2.5% Padding (Pre-Fix)

**What Was Wrong:**
- **Text cutoff risk**: Numbers and text only 5px from edge
- **Readability failure**: Insufficient breathing room makes sign feel cramped
- **Mobile rendering**: Risk of text being cropped at small sizes

**Why It Matters:**
Learners need to read numbers clearly and quickly. Cramped layouts slow recognition and create visual stress.

**The Fix:**
Increased padding to 10% (15px horizontal, 20px vertical on 150x200 viewBox).

---

## Actionable Rules for Future Assets

### Rule 1: Mobile Readability Minimums

**Stroke Widths:**
- 200×200 viewBox: **6px minimum** (renders at 2.9dp)
- 300×200 viewBox: **8px minimum** (renders at 2.6dp)

**Text Sizes:**
- 200×200 viewBox: **20px minimum** (renders at 9.6dp)
- 300×200 viewBox: **24px minimum with proper scaling**

**Test:** View at 48dp width. If you can't identify the key element, the stroke/text is too thin.

---

### Rule 2: Sign Geometry Accuracy

**STOP Signs:**
- Must be true regular octagon (8 equal sides, 8 equal angles of 135°)
- Use formula: vertices at 45° intervals around center point
- Never hand-draw—calculate mathematically

**DO NOT ENTER:**
- Square with rounded corners (rx="8"), NOT circle
- MUTCD R5-1 specification

**Speed Limit Signs:**
- Portrait rectangle: 150×200 viewBox (3:4 aspect ratio)
- Never use square 200×200 for portrait signs

**Verification:** Measure angles and radii. If values vary by more than 0.5px, regenerate.

---

### Rule 3: Padding Standards

**Minimum:** 8% of primary dimension
**Recommended:** 10% for regulatory signs (provides safety margin)

| Sign Type | ViewBox | Padding |
|-----------|---------|---------|
| Portrait signs | 0 0 150 200 | 15px horizontal, 20px vertical |
| Square signs | 0 0 200 200 | 20px all sides |
| Pavement (wide) | 0 0 300 200 | 10px horizontal, 5px vertical |

**Why:** Text/content too close to edges risks cutoff on mobile and looks cramped.

---

### Rule 4: Training Cues for Scenarios

Every scenario asset (INTERSECTION_*, PARKING_*, SAFE_*) should include **at least 2** of these educational elements:

- **Stop/yield lines** at appropriate positions
- **Direction arrows** showing traffic flow or trajectory
- **Vehicles** (1-2 simple colored rectangles showing positions)
- **Lane markings** (dashed center, solid edge)
- **Yield triangles** at roundabout entries
- **Distance markers** for speed/distance concepts
- **Color-coded zones** (red=danger, green=safe)

**Why:** Training images should teach concepts, not just show static shapes. Cues help learners understand what to do, not just what things look like.

---

### Rule 5: Color Consistency

**Use the standard palette—no variations:**

| Element | Hex Code | Usage |
|---------|----------|-------|
| **Road surface** | #4A4A4A | All paved areas |
| **Grass/shoulders** | #88AA88 | Off-road areas |
| **Ego vehicle** | #3366CC | "Your car" (always blue) |
| **Other vehicles** | #666666 | Other traffic (always gray) |
| **Emergency vehicle** | #CC0000 | Ambulance, police, fire |
| **School bus** | #FFB800 | School buses only |
| **Yellow lines** | #FFCC00 | Center markings (MUTCD) |
| **White lines** | #FFFFFF | Lane/edge markings |

**MUTCD Sign Colors (Critical):**
- Red: #C1272D (regulatory)
- Yellow: #FFCC00 (warning)
- Green: #006B3F (guide)
- Blue: #003DA5 (services)
- Orange: #FF6600 (construction)

**Why:** Consistency builds pattern recognition. Students learn "blue car = me" instantly when every diagram uses the same color.

---

### Rule 6: No Visual Noise

**Forbidden:**
- ❌ Decorative shadows (opacity 0.15 shadow rectangles)
- ❌ Texture overlays on road surfaces
- ❌ Gradients or complex fills
- ❌ Unnecessary labels when question text provides context
- ❌ Random decorative elements that don't teach

**Allowed (Functional Elements):**
- ✅ Traffic signal glow overlays (show active state)
- ✅ Blind spot zones with opacity (show hidden areas)
- ✅ Direction arrows (show movement/trajectory)
- ✅ Radiating lines on flashing signals (show flashing state)

**Why:** Every visual element should serve an educational purpose. Decoration distracts from learning.

---

### Rule 7: ViewBox Standards by Category

| Category | ViewBox | Rationale |
|----------|---------|-----------|
| MUTCD regulatory (square) | 0 0 200 200 | Octagon, circle, diamond signs |
| MUTCD regulatory (portrait) | 0 0 150 200 | Speed limit, parking, restrictions |
| MUTCD regulatory (landscape) | 0 0 200 100 | ONE WAY, WRONG WAY |
| MUTCD warning (diamond) | 0 0 200 200 | All warning signs (yellow diamonds) |
| Traffic signals (vertical) | 0 0 100 250 | Standard 3-light signals |
| Pavement markings | 0 0 300 200 | Wide view of road surface |
| Intersections | 0 0 200 200 | Overhead bird's-eye view |
| Parking scenarios | 0 0 200 200 | Vehicle positioning diagrams |

**Why:** Consistent viewBox within categories ensures visual rhythm. Signs appear the same size when displayed in lists.

---

### Rule 8: Transform Complexity (Avoid It)

**Bad:**
```svg
<g transform="translate(12 41.3333) scale(0.5867)">
  <text font-size="18">...</text>
</g>
```
Effective text size = 18 × 0.5867 / 200 × 96 = 5.1dp ❌

**Good:**
```svg
<text font-size="24">...</text>
```
Effective text size = 24 / 200 × 96 = 11.5dp ✓

**Why:** Transforms make readability calculations complex and often hide size issues. Use clean coordinates.

---

## Asset Creation Workflow

### 1. Define Purpose
- Is this an icon (simple, clean) or training diagram (includes educational cues)?
- What concept does it teach?

### 2. Choose ViewBox
- Match category standard from Rule 7
- Portrait signs: 150×200
- Square signs: 200×200
- Wide scenes: 300×200

### 3. Apply Standards
- Colors from palette (Rule 5)
- Padding minimums (Rule 3)
- Stroke widths (Rule 1)
- Text sizes (Rule 1)

### 4. Add Training Cues (If Applicable)
- For scenario assets, add at least 2 cues (Rule 4)
- Stop lines, arrows, vehicles, etc.

### 5. Validate
- [ ] ViewBox matches category standard
- [ ] All colors from standard palette
- [ ] Stroke widths ≥ minimum
- [ ] Text sizes ≥ minimum
- [ ] No visual noise (shadows, textures, gradients)
- [ ] Content within viewBox bounds (no clipping)
- [ ] Renders clearly at 96dp width
- [ ] Recognizable at 48dp

### 6. Test on Mobile
- Open in browser at 96dp and 48dp widths
- If you have to zoom to identify it, it fails

### 7. Add to Manifest
```json
{
  "assetId": "CATEGORY_DESCRIPTIVE_NAME",
  "description": "Brief learner-friendly description",
  "file": "assets/svg/CATEGORY_DESCRIPTIVE_NAME.svg",
  "purpose": "icon" or "training",
  "tags": ["category", "topic", "purpose", "key_elements"],
  "status": "ok",
  "lastReviewedAt": "YYYY-MM-DD",
  "sourceUrl": "generated",
  "license": "generated",
  "notes": "Optional implementation notes"
}
```

---

## Common Pitfalls and Fixes

### Pitfall 1: "It looks fine on my 27\" monitor"

**Problem:** What's readable at desktop sizes disappears on mobile.

**Fix:** Always test at 96dp width (mobile standard). If details vanish, increase stroke/text sizes.

---

### Pitfall 2: "Close enough" geometry

**Problem:** Hand-drawn STOP sign with irregular angles, DO NOT ENTER as circle instead of square.

**Fix:** Use mathematical formulas for sign geometry. Verify with measurements. Accuracy matters for training.

---

### Pitfall 3: "Let's add some visual interest"

**Problem:** Shadows, textures, gradients that make file size larger and distract from content.

**Fix:** Every pixel should teach. Remove anything that doesn't serve educational purpose.

---

### Pitfall 4: "I'll just scale it down"

**Problem:** Using transform scale() makes effective text/stroke sizes hard to calculate and often too small.

**Fix:** Design at target viewBox size with clean coordinates. No scaling wrappers.

---

### Pitfall 5: "The label explains it"

**Problem:** Adding text labels to every diagram, making them cluttered and harder to scan.

**Fix:** Let the question text provide context. Visual should be self-explanatory without labels when possible.

---

## Quality Metrics

### Mobile Readability Score
- **6px strokes** in 200×200 viewBox = 2.9dp at 96dp ✓
- **8px strokes** in 300×200 viewBox = 2.6dp at 96dp ✓ (acceptable)
- **20px text** in 200×200 viewBox = 9.6dp at 96dp ✓

**Target:** All critical elements ≥ 2.9dp strokes, ≥ 9.6dp text

---

### Educational Effectiveness Checklist
- [ ] Asset teaches the concept, not just shows a shape
- [ ] Training cues help learner understand what to do
- [ ] Consistent with other assets in same category
- [ ] Accurate to real-world MUTCD/regulatory specs
- [ ] Clean and uncluttered visual hierarchy
- [ ] Works without extensive labels

---

### File Size Targets
- Simple signs: **< 2KB**
- Training diagrams: **< 5KB**
- Complex scenarios: **< 10KB**

Larger files = slower load times = worse learner experience.

---

## When to Redesign an Asset

Redesign if:
- ❌ Fails mobile readability test (unrecognizable at 48dp)
- ❌ Has incorrect sign geometry (angles/radii off by >0.5px)
- ❌ Padding < 8% minimum
- ❌ Strokes < 2.9dp effective at 96dp
- ❌ Text < 9.6dp effective at 96dp
- ❌ Uses wrong colors (not from standard palette)
- ❌ Contains visual noise (shadows, textures, gradients)
- ❌ Complex transform makes calculations impossible
- ❌ Content clipped by viewBox boundary

---

## References

### Internal Documentation
- Technical specs: `assets/review/style_guide.md`
- Asset manifest: `assets/manifest.json`
- Audit reports: `assets/review/svg_audit_report.md`
- Fix reports: `assets/review/issue_53_fix_report.md`

### External Standards
- **MUTCD (Manual on Uniform Traffic Control Devices)**: Federal standard for US road signs
- **Texas MUTCD Supplement**: State-specific variations
- **Android Design Guidelines**: Material Design 96dp standard density

---

## Contribution Workflow

When creating or modifying SVG assets:

1. **Read this guide first** (especially the 3 good examples and 3 bad examples)
2. **Check existing similar assets** for style consistency
3. **Follow the 8 Actionable Rules** (especially Rules 1-3 for readability)
4. **Use the Asset Creation Workflow** checklist
5. **Test at 96dp and 48dp** before submitting
6. **Update manifest.json** with proper metadata
7. **Run validation** (if automated script exists)

---

## Questions?

If you're unsure about:
- **Sign geometry**: Consult MUTCD specifications or existing correct assets
- **Color choices**: Use the standard palette (Rule 5)—no exceptions
- **Size/readability**: Test at 96dp. If you can't identify it, it's wrong.
- **Training cues**: Look at existing INTERSECTION_* and PARKING_* assets for examples

**Golden Rule:** When in doubt, prioritize clarity over cleverness. Learners need clear, accurate visuals—not artistic masterpieces.

---

**Last Updated:** 2026-02-14
**Maintained By:** SVG Asset Curator
**Status:** Active standard for all DMV Texas quiz app visuals
