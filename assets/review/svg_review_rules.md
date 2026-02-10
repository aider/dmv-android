# SVG Review Rules — Regulatory/Warning Sign Correctness

**Version:** 1.0
**Applies to:** All SVG review agent runs, svg-review-agent and svg-asset-curator agents.
**Scope:** All assets in `assets/svg/`, with special enforcement for regulatory and warning signs.

---

## 1. Hard-Fail Rule

**Any sign whose geometry, border, or text is incorrect — even slightly — is a hard fail.**

- Mark the asset `status=needs_fix` in the audit report.
- Create a GitHub issue (see Section 7 for issue requirements).
- Do NOT rate it "acceptable", "close enough", or "looks ok."
- Do NOT defer to subjective judgment — use the geometry specs below.

A "slight" error includes:
- Octagon that is not a true regular octagon (8 equal sides, equal angles)
- Triangle that is not equilateral or has wrong orientation
- Border missing where the real-world sign has one
- Text that overflows its container or touches the border
- Inconsistent padding between signs of the same family

---

## 2. Geometry Correctness Checks (Minimum Set)

Each sign type has mandatory geometry constraints. The review agent MUST verify these.

### STOP Sign
- **Shape:** True regular octagon — 8 equal sides, internal angles = 135 degrees each.
- **Proportions:** Sign diameter-to-side ratio matches MUTCD R1-1 (side = diameter * 0.383).
- **Border:** White border, present on all 8 sides, uniform width.
- **Text:** "STOP" centered horizontally and vertically. White text on red background.
- **Padding:** Text must not touch or approach the border. Minimum inner padding = 10% of sign diameter.
- **Common failures:** Irregular polygon (sides not equal), missing border, text not centered, STOP text too large for border padding.

### YIELD Sign
- **Shape:** Equilateral triangle, point-down (inverted).
- **Border:** Red border on white background, OR solid red with white inner triangle (MUTCD R1-2).
- **Proportions:** All three sides equal length. Triangle must not be distorted or skewed.
- **Text:** "YIELD" centered if text variant is used (modern signs are textless).
- **Common failures:** Non-equilateral triangle, wrong orientation (point-up), missing red border.

### SPEED LIMIT Sign
- **Shape:** White rectangle with black border, portrait orientation.
- **Border:** Black border, uniform width on all 4 sides.
- **Text hierarchy:** "SPEED" on top line, "LIMIT" on second line, number on third line (largest).
- **Padding:** Consistent inner padding on all sides. Minimum = 8% of sign width.
- **Number:** Centered, largest text element, digits must be fully visible with clearance.
- **Common failures:** Text overflow, inconsistent padding top vs bottom, number too large for container, missing or thin border.

### DO NOT ENTER Sign
- **Shape:** Square with rounded corners, red background.
- **Interior:** White horizontal rectangle centered in sign.
- **Text:** "DO NOT ENTER" on the white rectangle, or no text (symbol-only per MUTCD R5-1).
- **Border:** White border around square exterior.
- **Common failures:** Rectangle not centered, text overflow on interior bar, missing white border.

### WRONG WAY Sign
- **Shape:** Red rectangle, landscape orientation.
- **Text:** "WRONG WAY" in white, centered.
- **Border:** White border, uniform width.
- **Common failures:** Text not centered, border missing, wrong aspect ratio.

### ONE WAY Sign
- **Shape:** Black rectangle with white arrow, landscape orientation.
- **Text:** "ONE WAY" in white alongside arrow.
- **Border:** White border.
- **Arrow:** White arrow pointing left or right, sized proportionally.
- **Common failures:** Arrow too small, text and arrow overlap, missing border.

### School Zone / School Crossing Signs
- **Shape:** Pentagon (house-shape, point-up) for school zone (S1-1).
- **Color:** Fluorescent yellow-green background with black border and symbols.
- **Symbols:** Pedestrian figures must be recognizable, not clipped.
- **Common failures:** Pentagon distorted, symbols too small to read at 96dp.

### Railroad Crossing Signs
- **Crossbuck (R15-1):** X-shape (two boards crossing), white with "RAILROAD CROSSING" text.
- **Advance warning (W10-1):** Circular yellow sign with black X and "RR" letters.
- **Common failures:** X not at correct angle, text illegible, circle not round.

### Warning Signs (Diamond-shaped)
- **Shape:** Square rotated 45 degrees (diamond orientation).
- **Color:** Yellow background with black border and symbols.
- **Border:** Black border, uniform width, follows diamond edges.
- **Symbols:** Must be recognizable at 96dp. Lines/arrows must have sufficient stroke width.
- **Common failures:** Diamond not square (aspect ratio off), symbols too detailed for small render.

### Merge/Lane-Related Signs
- **Shape:** Diamond (warning) or rectangular (guide).
- **Arrows:** Must clearly indicate direction. Arrowheads must be visible at 96dp.
- **Common failures:** Arrows too thin, merge direction ambiguous.

---

## 3. Border + Padding Rules

### Border Requirements
- A border MUST exist wherever the corresponding real-world MUTCD sign has one.
- If a sign family has borders, ALL signs in that family must have borders.
- Border width must be consistent across all signs of the same family.
- Border must follow the sign shape precisely (no gaps, no shortcuts).

### Padding Rules
- **Minimum inner padding:** 8% of the sign's primary dimension (width for landscape, height for portrait).
- **Recommended inner padding:** 10-12% of the sign's primary dimension.
- **Text must never overflow** into the border zone or beyond the sign boundary.
- **Padding must be consistent** within a sign family:
  - All SPEED LIMIT signs must use the same padding ratio.
  - All WARNING diamond signs must use the same padding ratio.
  - Inconsistent padding across the same family = needs_fix.

### Verification Method
For each sign, measure (from SVG coordinates):
1. Total sign dimension (e.g., viewBox width/height of the sign shape).
2. Distance from sign edge to nearest content element.
3. Verify: `distance / dimension >= 0.08` (8% minimum).
4. Verify: text bounding box does not intersect border stroke.

---

## 4. Mobile Readability Rules

### At 96dp (primary target)
- The sign MUST be immediately recognizable — a driver's ed student should identify it instantly.
- All text must be legible (not just "present" — actually readable).
- Border must be visible as a distinct line.
- Symbols must be distinguishable.
- Stroke widths: minimum 2px in a 200px viewBox (scales to ~1dp at 96dp).
  - Preferred minimum: 4px in 200px viewBox.
  - Stop lines, borders, emphasis strokes: minimum 6px in 200px viewBox.

### At 48dp (secondary target)
- The sign shape must still be distinguishable (octagon vs circle vs diamond vs rectangle).
- Border must still be visible (even if thin).
- Large text (like speed limit numbers) should still be distinguishable.
- Fine details (small text, thin symbols) may degrade but shape must read.

### Hard Failures for Readability
- Any stroke that vanishes entirely at 96dp render = **hard fail**.
- Text that becomes an unreadable blob at 96dp = **hard fail**.
- Sign shape that cannot be distinguished from a circle/square at 48dp = **hard fail**.
- Border that is not visible at 96dp = **hard fail**.

---

## 5. Source-of-Truth Guidance

### Primary Reference
- **MUTCD (Manual on Uniform Traffic Control Devices)** — the federal standard for all traffic signs.
- Use public-domain MUTCD/SHS (Standard Highway Signs) shape specifications.
- MUTCD sign IDs (e.g., R1-1 for STOP, R1-2 for YIELD, R2-1 for SPEED LIMIT) are the canonical references.

### Geometry Standards
- If sign geometry is generated by the component library (`assets/components/primitives.py`), verify the output matches MUTCD specs.
- If geometry is hand-generated (directly in SVG), it MUST match strict MUTCD specs:
  - No approximate shapes (e.g., using a hexagon instead of an octagon).
  - No freehand curves where straight lines are specified.
  - Polygon vertex counts must be exact.
- **Acceptable simplifications** for 96dp rendering:
  - Omitting extremely fine detail that is invisible at target size.
  - Simplifying complex symbols to recognizable silhouettes.
  - Using flat colors instead of reflective surface simulation.
- **Unacceptable simplifications:**
  - Wrong number of polygon sides.
  - Wrong shape orientation (e.g., point-up triangle for YIELD).
  - Missing required text (e.g., STOP sign without "STOP").
  - Wrong colors (red where yellow should be, etc.).

---

## 6. Golden Set — Critical Signs Audit Table

Every audit report produced by the svg-review-agent MUST include this section.

List each asset that matches a Golden Set sign type. For each, report status and link to any open issue.

### Golden Set Sign Types

| Priority | Sign Type | MUTCD ID | Expected Shape | Expected Color |
|----------|-----------|----------|----------------|----------------|
| P0 | STOP | R1-1 | Regular octagon | Red + white border |
| P0 | YIELD | R1-2 | Inverted equilateral triangle | Red border on white / red+white |
| P0 | SPEED LIMIT | R2-1 | Rectangle (portrait) | White + black border |
| P0 | DO NOT ENTER | R5-1 | Square | Red + white bar + white border |
| P0 | WRONG WAY | R5-1a | Rectangle (landscape) | Red + white text + white border |
| P1 | ONE WAY | R6-1/R6-2 | Rectangle (landscape) | Black + white arrow/text |
| P1 | NO PARKING | R8-3 | Rectangle | White + red/green + black border |
| P1 | SCHOOL ZONE | S1-1 | Pentagon (point-up) | Fl. yellow-green + black |
| P1 | RAILROAD CROSSING | R15-1/W10-1 | Crossbuck / Circle | White+black / Yellow+black |
| P2 | MERGE | W4-1 | Diamond | Yellow + black |
| P2 | CURVE/TURN | W1-series | Diamond | Yellow + black |
| P2 | PEDESTRIAN | W11-2 | Diamond | Yellow + black |

### Report Format

For each Golden Set sign, include in the audit report:

```markdown
### Golden Set Status

| Sign Type | Asset File | Status | Issue | Notes |
|-----------|-----------|--------|-------|-------|
| STOP | SIGN_STOP.svg | ok | — | Correct octagon, border, text |
| YIELD | SIGN_YIELD.svg | needs_fix | #42 | Triangle not equilateral |
| SPEED LIMIT 30 | SPEED_LIMIT_RESIDENTIAL_30.svg | needs_fix | #43 | Text overflow, thin border |
| ... | ... | ... | ... | ... |
```

If a Golden Set sign type has NO corresponding asset in the library, note it as `missing` and recommend creation.

---

## 7. Issue Creation Requirements

### When to Create Issues

| Condition | Action |
|-----------|--------|
| Golden Set sign with geometry error | Create per-asset P0 issue |
| Golden Set sign with text overflow / missing border | Create per-asset P0 issue |
| Non-Golden-Set sign with geometry error | Create per-family P1 issue (group related signs) |
| Readability failure at 96dp | Create per-asset P1 issue |
| Readability failure at 48dp only | Create per-family P2 issue |
| Padding inconsistency within family | Create per-family P1 issue |

### Issue Structure

Every issue MUST include:

```markdown
## Problem
[Specific description of what is wrong, with measurements from SVG coordinates]

## Assets Affected
- [List each affected file and the questions that reference it]

## Acceptance Criteria
- [ ] Geometry correct per MUTCD spec ([sign ID])
- [ ] Border present, uniform width, follows sign shape
- [ ] Text centered with >= 8% inner padding
- [ ] Readable and recognizable at 96dp
- [ ] Shape distinguishable at 48dp
- [ ] No clipping (all content within viewBox, 5% margin)
- [ ] Colors match MUTCD spec and style_tokens.py
- [ ] File size < 3KB
- [ ] Valid XML

## Reference
- MUTCD sign ID: [e.g., R1-1]
- Expected geometry: [description]
- Current geometry: [description of error]
```

### Issue Labels
- `svg` — all SVG issues
- `priority/P0`, `priority/P1`, or `priority/P2`
- `sign-correctness` — geometry/border/text errors
- `readability` — mobile readability failures
- `golden-set` — affects a Golden Set sign

---

## 8. Review Checklist (Per Asset)

For every SVG reviewed, apply this checklist in order. Stop at the first hard fail.

### Structural Checks
- [ ] Valid XML (parseable, no unclosed tags)
- [ ] viewBox present and correct dimensions
- [ ] All `url(#id)` references resolve to defined elements
- [ ] File size < 3KB
- [ ] No embedded raster images
- [ ] Coordinates are clean (integers or single-decimal)

### Sign Correctness (Hard Fail)
- [ ] Shape matches MUTCD spec for this sign type
- [ ] Correct number of polygon sides (8 for STOP, 3 for YIELD, etc.)
- [ ] Correct orientation (inverted triangle for YIELD, diamond rotation for warnings)
- [ ] Border present where MUTCD requires it
- [ ] Border width consistent with same-family signs
- [ ] Text matches real sign (STOP, YIELD, speed number, etc.)
- [ ] Text centered with >= 8% inner padding
- [ ] No text overflow beyond sign boundary

### Mobile Readability (Hard Fail)
- [ ] Recognizable at 96dp
- [ ] Shape distinguishable at 48dp
- [ ] No strokes that vanish at 96dp (minimum stroke-width check)
- [ ] Border visible at 96dp

### Quality Checks (Soft Fail)
- [ ] Colors match style_tokens.py / MUTCD spec
- [ ] No clipping (content within viewBox with 5% padding)
- [ ] Consistent with other signs in same family
- [ ] Learning cues present (for scene assets: >= 2 cues)

---

## 9. Integration with Component Library

When reviewing assets generated by `assets/components/`:
- Verify that `primitives.py` component functions produce correct geometry.
- If a component produces incorrect geometry (e.g., `stop_sign()` creates a non-regular octagon), file the issue against the component, not just the generated SVG.
- Reference the component function in the issue (e.g., "Fix `stop_sign()` in `primitives.py` to produce a true regular octagon").
- After component fixes, regenerate all dependent scenes and re-audit.

---

## 10. Changelog

| Date | Version | Change |
|------|---------|--------|
| 2026-02-09 | 1.0 | Initial version — hard-fail rules, geometry specs, Golden Set, issue requirements |
