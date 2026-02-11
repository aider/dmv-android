# QA Re-Audit (Visual Risk Precheck)

Date: 2026-02-10
Total assets scanned: 116
Assets with heuristic risks: 32

## Top Risk Assets (Heuristic)

| Asset | Score | Risks |
|---|---:|---|
| INTERSECTION_MERGE_HIGHWAY | 3 | contains scaled transform (can cause renderer variance) |
| INTERSECTION_PEDESTRIAN_CROSSWALK | 3 | contains scaled transform (can cause renderer variance) |
| INTERSECTION_T_STOP | 3 | contains scaled transform (can cause renderer variance) |
| MUTCD_R7-8_NO_PARKING | 3 | contains scaled transform (can cause renderer variance) |
| PARKING_DISABLED_SPACE | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_ARROW_LEFT | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_ARROW_LEFT_STRAIGHT | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_ARROW_RIGHT | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_ARROW_RIGHT_STRAIGHT | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_ARROW_STRAIGHT | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_CROSSWALK_ZEBRA | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_DASHED_WHITE_LINE | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_DOUBLE_YELLOW_MIXED | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_DOUBLE_YELLOW_SOLID | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_HANDICAP_SYMBOL | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_HOV_DIAMOND | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_RAILROAD_CROSSING_X | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_SHARROW | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_SINGLE_YELLOW_DASHED | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_SOLID_WHITE_LINE | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_TURN_LANE_CENTER | 3 | contains scaled transform (can cause renderer variance) |
| PAVEMENT_YIELD_LINE | 3 | contains scaled transform (can cause renderer variance) |
| SIGNAL_FLASHING_RED | 3 | contains scaled transform (can cause renderer variance) |
| SIGNAL_FLASHING_YELLOW | 3 | contains scaled transform (can cause renderer variance) |
| SIGNAL_GREEN_ARROW_LEFT | 3 | contains scaled transform (can cause renderer variance) |
| SIGNAL_GREEN_ARROW_RIGHT | 3 | contains scaled transform (can cause renderer variance) |
| SIGNAL_RED_ARROW_LEFT | 3 | contains scaled transform (can cause renderer variance) |
| SIGNAL_RED_YELLOW_TOGETHER | 3 | contains scaled transform (can cause renderer variance) |
| SIGNAL_SOLID_GREEN | 3 | contains scaled transform (can cause renderer variance) |
| SIGNAL_SOLID_RED | 3 | contains scaled transform (can cause renderer variance) |
| SIGNAL_SOLID_YELLOW | 3 | contains scaled transform (can cause renderer variance) |
| SIGNAL_YELLOW_ARROW_LEFT | 3 | contains scaled transform (can cause renderer variance) |

## Method
- Heuristic text bounding-box checks for potential clipping.
- Detect scaled transforms and non-standard viewBox usage.
- Output is precheck only; final decision must be made by PNG visual review.

