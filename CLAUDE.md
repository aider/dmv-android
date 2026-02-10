# DMV Texas Quiz App - Project Instructions

## Project Overview
Texas DMV practice quiz app with Android client and content pipeline.
- Content: `data/tx/tx_v1.json` (660 questions, 8 topics)
- SVG assets: `assets/svg/` (109 files), manifest at `assets/manifest.json`
- Component library: `assets/components/` (Python SVG generator)
- Android app: `dmv-android/`

## SVG Review Agent Instructions

When the `svg-review-agent` is launched, it MUST follow the rules in
`assets/review/svg_review_rules.md` in addition to its default behavior.

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

4. **Source of truth is MUTCD.** Sign geometry must match MUTCD/SHS specs.
   Hand-drawn approximations that deviate from spec are failures.

5. **Issues must include acceptance criteria.** Every issue created must specify:
   geometry correct, border present, text centered, readable at 96/48dp, no
   clipping. P0 issues get individual per-asset issues; lower priority can be
   grouped by sign family.

Full specification: `assets/review/svg_review_rules.md`
