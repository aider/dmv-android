# DMV Texas Quiz App - Project Instructions

## Project Overview
Texas DMV practice quiz app with Android client and content pipeline.
- Content: `data/tx/tx_v1.json` (660 questions, 8 topics)
- SVG assets: `assets/svg/` (109 files), manifest at `assets/manifest.json`
- Component library: `assets/components/` (Python SVG generator)
- Android app: `dmv-android/`

## SVG Asset Standards

**All SVG asset work MUST follow the learner-focused style guide:**
📖 `docs/growth/image-style-guide.md` (REQUIRED READING)

This guide contains:
- 3 good examples and 3 bad examples with full analysis
- 8 actionable rules with specific measurements
- Mobile readability minimums (6px strokes, 20px text)
- MUTCD sign geometry requirements
- Complete asset creation workflow

## SVG Review Agent Instructions

When the `svg-review-agent` is launched, it MUST follow the rules in
`assets/review/svg_review_rules.md` AND `docs/growth/image-style-guide.md`.

**Key enforcement rules (non-negotiable):**

1. **Sign correctness is a hard-fail criterion.** If any regulatory or warning
   sign has incorrect geometry, wrong border, missing text, or text overflow,
   mark it `status=needs_fix` and create a GitHub issue. Do NOT treat geometry
   errors as cosmetic or "looks ok enough."

2. **The Golden Set must appear in every audit report.** The Golden Set is the
   list of critical sign types (STOP, YIELD, SPEED LIMIT, DO NOT ENTER, WRONG
   WAY, ONE WAY, school zone, railroad crossing). Every report must include a
   dedicated table showing each Golden Set sign's status (ok/needs_fix) with
   links to issues for any failures.

3. **Mobile readability is mandatory.** Assets must be recognizable at 96dp and
   distinguishable at 48dp. Thin strokes that vanish at small sizes are failures.
   - Minimum stroke: 6px (200×200 viewBox), 8px (300×200 viewBox)
   - Minimum text: 20px (200×200 viewBox), 24px (300×200 viewBox)
   - Test formula: effectiveDp = (px / viewBoxWidth) × 96

4. **Source of truth is MUTCD.** Sign geometry must match MUTCD/SHS specs.
   Hand-drawn approximations that deviate from spec are failures.
   - STOP: Regular octagon, all angles 135°
   - DO NOT ENTER: Square with rounded corners (NOT circle)
   - Speed limits: Portrait 150×200 viewBox with 10% padding

5. **Issues must include acceptance criteria.** Every issue created must specify:
   geometry correct, border present, text centered, readable at 96/48dp, no
   clipping. P0 issues get individual per-asset issues; lower priority can be
   grouped by sign family.

**References:**
- Technical audit rules: `assets/review/svg_review_rules.md`
- Learner-focused style guide: `docs/growth/image-style-guide.md`
- Contribution guidelines: `CONTRIBUTING.md`
