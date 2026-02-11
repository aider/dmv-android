# MUTCD Official Reference Audit

Date: 2026-02-10
Scope: cross-check `MUTCD_*` asset IDs against official FHWA/TxDOT references.

## Primary Sources
- FHWA Signs Library overview: https://highways.dot.gov/federal-lands/cadd-support/signs-library
- FHWA Series R: https://highways.dot.gov/federal-lands/cadd-support/signs-library-series-r
- FHWA Series D: https://highways.dot.gov/federal-lands/cadd-support/signs-library-series-d
- FHWA Series M: https://highways.dot.gov/federal-lands/cadd-support/signs-library-series-m
- FHWA Series G & I: https://highways.dot.gov/federal-lands/cadd-support/signs-library-series-g-i
- FHWA Series S: https://highways.dot.gov/federal-lands/cadd-support/signs-library-series-s
- FHWA MUTCD 2009 Part 2C (code groups/table): https://mutcd.fhwa.dot.gov/htm/2009r1r2r3/part2/part2c.htm
- FHWA MUTCD 2009 Figure 9B-3 long description (W11 symbols): https://mutcd.fhwa.dot.gov/htm/2009r1r2/part9/fig9b_03_longdesc.htm
- TxDOT TMUTCD (2025 adopted, effective 2026-01-18): https://www.txdot.gov/business/resources/traffic-design-standards/tmutcd.html
- TxDOT SHSD: https://www.txdot.gov/business/resources/traffic-design-standards/highway-sign-designs.html

## High-Confidence Code Mismatches Found

| Asset ID | Current Content | Official Reference | Finding |
|---|---|---|---|
| `MUTCD_I-5_INTERSTATE_10` | Interstate shield | FHWA Series G&I (`I-5 Airport`) | `I-5` code is not interstate shield code in FHWA Signs Library. |
| `MUTCD_I-5_INTERSTATE_35` | Interstate shield | FHWA Series G&I (`I-5 Airport`) | Same mismatch as above. |
| `MUTCD_M1-1_US_ROUTE_90` | US route shield | FHWA Series M (`M1-1.x Interstate Route`, `M1-4.x US Route`) | US route placed under interstate code family. |
| `MUTCD_M1-4_STATE_ROUTE_71` | Texas state route marker | FHWA Series M (`M1-4.x US Route`) | `M1-4` is US route family, not state-route family. |
| `MUTCD_D9-1_REST_AREA` | Rest area symbol | FHWA Series D (`D9-1 Telephone`) | Symbol/meaning mismatch to code. |
| `MUTCD_D9-3_GAS_STATION` | Fuel pump symbol | FHWA Series D (`D9-3 Camping`, `D9-7 Fuel`) | Fuel symbol mapped to wrong D-code. |
| `MUTCD_R10-6_ONE_WAY` | One-way sign | FHWA Series R (`R10-6 STOP HERE ON RED`) | Code meaning mismatch. |
| `MUTCD_R10-7_DO_NOT_PASS` | Do Not Pass sign | FHWA Series R (`R10-7 DO NOT BLOCK INTERSECTION`) | Code meaning mismatch. |
| `MUTCD_R7-8_NO_PARKING` | No parking | FHWA Series R (`R7-8 RESERVED PARKING (Accessible)`) | Code meaning mismatch. |
| `MUTCD_R3-1_NO_LEFT_TURN` | No left turn | FHWA Series R (`R3-1 No right turn`) | R3-1/R3-2 mapping reversed. |
| `MUTCD_R3-2_NO_RIGHT_TURN` | No right turn | FHWA Series R (`R3-2 No left turn`) | R3-1/R3-2 mapping reversed. |
| `MUTCD_R3-3_NO_U_TURN` | No U-turn | FHWA Series R (`R3-3 NO TURNS`) | Code meaning mismatch. |
| `MUTCD_R3-4_NO_TRUCKS` | No trucks | FHWA Series R (`R3-4 No U-turn`) | Code meaning mismatch. |
| `MUTCD_W1-6_WINDING_ROAD` | Winding road symbol | MUTCD Part 2C (`W1-5 Winding Road`, `W1-6 One-direction large arrow`) | Winding road should be W1-5; W1-6 is rectangular arrow sign. |
| `MUTCD_W2-1_SIDE_ROAD` | Side road symbol | MUTCD Part 2C (`W2-1 Cross Road`) | Code meaning mismatch. |
| `MUTCD_W2-2_Y_INTERSECTION` | Y intersection symbol | MUTCD Part 2C (`W2-2 Side Road`) | Code meaning mismatch. |
| `MUTCD_W2-3_T_INTERSECTION` | T intersection symbol | MUTCD Part 2C (`W2-3 Side Road variant`) | Code meaning mismatch. |
| `MUTCD_W3-1_MERGE` | Merge symbol | MUTCD Part 2C (`W3-1 Stop Ahead`; `W4-1 Merge`) | Code meaning mismatch. |
| `MUTCD_W8-1_HILL` | Hill/grade symbol | MUTCD Part 2C (`W8-1 BUMP`; hill is W7-1 family) | Code meaning mismatch. |
| `MUTCD_W11-1_PEDESTRIAN_CROSSING` | Pedestrian symbol | MUTCD Fig. 9B-3 (`W11-1 bicycle`, `W11-2 pedestrian`) | W11-1/W11-2 mapping reversed. |
| `MUTCD_W11-2_BICYCLE_CROSSING` | Bicycle symbol | MUTCD Fig. 9B-3 (`W11-1 bicycle`, `W11-2 pedestrian`) | W11-1/W11-2 mapping reversed. |
| `MUTCD_S4-3_SCHOOL_SPEED_LIMIT_20` | Full school speed limit sign | FHWA Series S (`S4-3P SCHOOL` plaque only) | S4-3 is a plaque code, not a complete speed-limit signface. |

## Action Plan
1. Keep current visual assets, but split into two tracks:
   - `code-correct` assets: ID and sign meaning align with FHWA/TxDOT.
   - `code-mismatch` assets: require renaming/remapping (or redraw under correct IDs).
2. For each mismatch above, either:
   - rename asset ID to official-equivalent code, or
   - redraw sign to match the current ID's official meaning.
3. Do not mark `ok` unless both conditions are true:
   - visual rendering quality is acceptable;
   - MUTCD/TxDOT code semantics match.
