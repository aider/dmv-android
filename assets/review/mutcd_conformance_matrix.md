# MUTCD Conformance Matrix

Date: 2026-02-10
Basis: per-asset review against MUTCD-style geometry, border, text hierarchy, and mobile legibility.

- MUTCD assets reviewed: 49
- `ok`: 30
- `needs_fix`: 19

| Asset | Status | Missing / Needs Improvement |
|---|---|---|
| `MUTCD_D1-1_EXIT_SIGN` | `needs_fix` | Guide sign typography and spacing are stylized; needs closer highway guide layout and hierarchy. |
| `MUTCD_D3-1_DISTANCE_SIGN` | `needs_fix` | Destination/distance composition is stylized; text rhythm and spacing need SHS-like proportions. |
| `MUTCD_D9-1_REST_AREA` | `needs_fix` | Service icon simplification too custom; align icon geometry with standard service symbol style. |
| `MUTCD_D9-2_HOSPITAL` | `needs_fix` | Hospital symbol is oversimplified; refine cross proportions and panel padding. |
| `MUTCD_D9-3_GAS_STATION` | `needs_fix` | Fuel pump symbol requires cleaner canonical silhouette and hose/handle proportions. |
| `MUTCD_I-5_INTERSTATE_10` | `needs_fix` | Interstate shield shape and typography are approximate; needs stricter shield geometry. |
| `MUTCD_I-5_INTERSTATE_35` | `needs_fix` | Interstate shield shape and typography are approximate; needs stricter shield geometry. |
| `MUTCD_M1-1_US_ROUTE_90` | `needs_fix` | US route shield proportions/lettering need SHS-conformant geometry. |
| `MUTCD_M1-4_STATE_ROUTE_71` | `needs_fix` | State route marker was improved but still needs final SHS-accurate silhouette/text tuning. |
| `MUTCD_OM1-1_KEEP_RIGHT` | `needs_fix` | Object marker semantics were improved but require strict MUTCD OM panel geometry and arrow proportions. |
| `MUTCD_OM2-1_KEEP_LEFT` | `needs_fix` | Object marker semantics were improved but require strict MUTCD OM panel geometry and arrow proportions. |
| `MUTCD_R1-1_STOP` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_R1-2_YIELD` | `needs_fix` | Yield triangle improved; requires final border spacing and text treatment parity. |
| `MUTCD_R10-6_ONE_WAY` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_R10-7_DO_NOT_PASS` | `needs_fix` | Sign + lane illustration is didactic but not canonical regulatory sign face; should be simplified. |
| `MUTCD_R15-1_RAILROAD_CROSSING` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_R2-1_SPEED_LIMIT_30` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_R2-1_SPEED_LIMIT_65` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_R2-1_SPEED_LIMIT_70` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_R3-1_NO_LEFT_TURN` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_R3-2_NO_RIGHT_TURN` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_R3-3_NO_U_TURN` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_R3-4_NO_TRUCKS` | `needs_fix` | Truck symbol stroke/shape still custom; needs closer MUTCD symbol proportions. |
| `MUTCD_R4-7_KEEP_RIGHT` | `needs_fix` | Arrow symbol and copy spacing need stricter R4-series layout proportions. |
| `MUTCD_R5-1_DO_NOT_ENTER` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_R5-1a_WRONG_WAY` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_R6-1_LEFT_TURN_ONLY` | `needs_fix` | Improved; final typography and arrow symbol should match MUTCD signface proportions. |
| `MUTCD_R6-2_RIGHT_TURN_ONLY` | `needs_fix` | Improved; final typography and arrow symbol should match MUTCD signface proportions. |
| `MUTCD_R7-107_HANDICAPPED_PARKING` | `needs_fix` | Improved; ISA icon requires final standard wheel/body geometry pass. |
| `MUTCD_R7-8_NO_PARKING` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_R8-3a_NO_PARKING_ANYTIME` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_S1-1_SCHOOL_CROSSING` | `needs_fix` | School crossing symbol is simplified; needs more canonical MUTCD pedestrian pair silhouette. |
| `MUTCD_S4-3_SCHOOL_SPEED_LIMIT_20` | `ok` | Passes current baseline review; keep for later micro-tuning only. |
| `MUTCD_W1-1_CURVE_RIGHT` | `ok` | Arrow style is close but still custom; refine to MUTCD warning symbol proportions. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W1-2_CURVE_LEFT` | `ok` | Arrow style is close but still custom; refine to MUTCD warning symbol proportions. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W1-6_WINDING_ROAD` | `ok` | Winding arrow remains stylized; needs standard S-curve symbol proportions. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W11-1_PEDESTRIAN_CROSSING` | `ok` | Pedestrian symbol improved but still custom; finalize to MUTCD pictogram proportions. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W11-2_BICYCLE_CROSSING` | `ok` | Bicycle/rider pictogram is custom; needs MUTCD-like icon balance and stroke profile. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W14-1_TRUCK_CROSSING` | `ok` | Truck pictogram requires canonical silhouette and wheel/body proportions. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W14-2_FARM_EQUIPMENT` | `ok` | Farm equipment icon remains stylized; needs cleaner standard symbol language. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W16-2_SLIPPERY_WHEN_WET` | `ok` | Skid symbol stroke rhythm/proportions need canonical warning icon treatment. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W2-1_SIDE_ROAD` | `ok` | Intersection glyph proportions need stricter MUTCD symbol geometry. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W2-2_Y_INTERSECTION` | `ok` | Y-intersection glyph should be normalized to canonical branch angles/stroke widths. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W2-3_T_INTERSECTION` | `ok` | T-intersection symbol needs stricter MUTCD geometry and stroke balance. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W20-1_ROAD_CONSTRUCTION` | `ok` | Work symbol icon is illustrative; needs standard MUTCD worker silhouette style. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W20-7_FLAGGER_AHEAD` | `ok` | Flagger icon requires canonical paddle/pose geometry for consistency. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W3-1_MERGE` | `ok` | Merge symbol lane geometry requires stricter canonical lane taper form. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W4-2_LANE_ENDS_MERGE` | `ok` | Lane-ends symbol lanes and merge branch should follow standard lane geometry. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
| `MUTCD_W8-1_HILL` | `ok` | Grade symbol is simplified; hill/truck treatment needs MUTCD-like profile and spacing. Batch A (W-series) redesigned to canonical warning-sign template and standardized symbol strokes. |
