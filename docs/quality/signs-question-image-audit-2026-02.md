# SIGNS Question-Image Audit (2026-02)

Scope: `app/src/main/assets/questions/signs.json` questions with `image.assetId` (question-level audit).

## Summary
- Total image questions audited: **50**
- PASS: **0**
- BLOCK: **50**
- Severity mix: **P0=1, P1=2, P2=47**
- CSV output: `docs/quality/signs-question-image-audit-2026-02.csv`

## Method
- Mapped each image question to its `asset_id`.
- Checked semantic fit between question and selected asset.
- Linked open GitHub issues affecting the same asset (readability/reference alignment/shadow).
- Added canonical MUTCD section URL per asset class for reference validation.

## Top-15 Assets To Address First
- MUTCD_S4-3_SCHOOL_SPEED_LIMIT_20: P0 (blocked questions: 1)
- MUTCD_R5-1_DO_NOT_ENTER: P1 (blocked questions: 1)
- MUTCD_R7-8_NO_PARKING: P1 (blocked questions: 1)
- MUTCD_R1-2_YIELD: P2 (blocked questions: 2)
- MUTCD_D1-1_EXIT_SIGN: P2 (blocked questions: 1)
- MUTCD_D3-1_DISTANCE_SIGN: P2 (blocked questions: 1)
- MUTCD_D9-1_REST_AREA: P2 (blocked questions: 1)
- MUTCD_D9-2_HOSPITAL: P2 (blocked questions: 1)
- MUTCD_D9-3_GAS_STATION: P2 (blocked questions: 1)
- MUTCD_I-5_INTERSTATE_10: P2 (blocked questions: 1)
- MUTCD_I-5_INTERSTATE_35: P2 (blocked questions: 1)
- MUTCD_M1-1_US_ROUTE_90: P2 (blocked questions: 1)
- MUTCD_M1-4_STATE_ROUTE_71: P2 (blocked questions: 1)
- MUTCD_OM1-1_KEEP_RIGHT: P2 (blocked questions: 1)
- MUTCD_OM2-1_KEEP_LEFT: P2 (blocked questions: 1)

## Notes
- This is a question-level audit; multiple questions can reuse one asset.
- For implementation, prioritize assets with P0/P1 first, then close P2 reference-alignment batch issues.
