# TX Legal Correctness Matrix Audit — February 2026

**Audit date:** 2026-02-15
**Auditor:** pr-requirements-ux-reviewer agent
**Question bank:** `data/tx/tx_v1.json` (v1, 660 questions, 8 topics)
**Authoritative sources:** Texas Transportation Code (TTC), Texas DPS DL-7 Driver Handbook, Texas Penal Code

---

## Executive Summary

| Status | Count | Percentage |
|--------|-------|------------|
| PASS   | 650   | 98.5%      |
| REVIEW | 4     | 0.6%       |
| FAIL   | 6     | 0.9%       |
| **Total** | **660** | **100%** |

**Risk breakdown (non-PASS items only):**

| Risk Level | Count | Description |
|-----------|-------|-------------|
| P0        | 5     | Legally incorrect — could mislead test-takers |
| P1        | 3     | Misleading or incomplete — should be corrected |
| P2        | 2     | Minor imprecision — nice to fix |

---

## Summary by Topic

| Topic | Total | PASS | REVIEW | FAIL |
|-------|-------|------|--------|------|
| PARKING | 60 | 60 | 0 | 0 |
| PAVEMENT_MARKINGS | 70 | 69 | 1 | 0 |
| RIGHT_OF_WAY | 120 | 117 | 1 | 2 |
| SAFE_DRIVING | 90 | 88 | 1 | 1 |
| SIGNS | 120 | 119 | 0 | 1 |
| SPECIAL_SITUATIONS | 60 | 59 | 0 | 1 |
| SPEED_AND_DISTANCE | 80 | 78 | 1 | 1 |
| TRAFFIC_SIGNALS | 60 | 60 | 0 | 0 |

---

## FAIL Items (6 questions)

### FAIL-1: TX-SAF-0061 — Headlights when wipers are on (P0)

- **Question:** "When should you use headlights during rain in Texas?"
- **Current answer:** "Anytime you need windshield wipers"
- **Current explanation:** "Texas law requires headlights when using windshield wipers."
- **Issue:** **Texas does NOT have a law tying headlight use to windshield wipers.** Texas Transportation Code §547.302 requires headlights when visibility is reduced to less than 1,000 feet, which often coincides with rain/wiper use but is NOT the legal trigger. Several other states (e.g., Arkansas) have this wiper-headlight law — Texas does not.
- **Authoritative source:** [TX Transportation Code §547.302](https://texas.public.law/statutes/tex._transp._code_section_547.302)
- **Proposed fix:** Change answer to "When visibility is reduced to less than 1,000 feet ahead" and update explanation to cite §547.302 correctly. Mention that rain often reduces visibility below this threshold, making headlights necessary in practice.

### FAIL-2: TX-ROW-0045 — 3-foot bicycle passing distance (P0)

- **Question:** "When passing a cyclist on the road, how much space should you give?"
- **Current answer:** "At least 3 feet of clearance"
- **Current explanation:** "Texas law requires drivers to give at least 3 feet of clearance when passing a cyclist."
- **Issue:** **Texas does NOT have a statewide 3-foot safe passing law.** Texas is one of 15 states that does not codify a specific passing distance for bicycles. The Texas Transportation Code only requires passing at a "safe distance" (general requirement under §545.053). The 3-foot recommendation is promoted by cycling advocacy organizations like BikeTexas, but it is NOT codified in Texas state law. Multiple legislative attempts (HB 962, HB 554, HB 2459 "Iris Stagner Safe Passing Act") have been proposed but have not passed.
- **Authoritative source:** [BikeTexas Safe Passing FAQ](https://www.biketexas.org/advocacy/safe-passing-2012/safe-passing-ordinance-faqs/); [TX Transportation Code §545.053](https://codes.findlaw.com/tx/transportation-code/transp-sect-545-053/)
- **Proposed fix:** Change to "Pass at a safe distance (at least 3 feet is recommended as best practice)" and update explanation to note that while 3 feet is widely recommended and required by some local ordinances, it is not a statewide Texas law.

### FAIL-3: TX-ROW-0053 — 3-foot bicycle passing (duplicate claim) (P0)

- **Question:** "A cyclist is riding in the center of a narrow lane. Can you pass them?"
- **Current answer:** "Only if you can change lanes completely or provide at least 3 feet of clearance"
- **Issue:** Same as FAIL-2. The "3 feet" claim is not codified in Texas law.
- **Proposed fix:** Same as FAIL-2. Replace "3 feet" with "a safe distance (3 feet recommended)."

### FAIL-4: TX-SPD-0078 — 3-foot bicycle passing (duplicate claim) (P0)

- **Question:** "What is the minimum clearance required when passing a bicycle in Texas?"
- **Current answer:** "3 feet"
- **Current explanation:** "Texas law requires drivers to maintain at least 3 feet of clearance when passing a bicycle."
- **Issue:** Same as FAIL-2. No statewide 3-foot law in Texas.
- **Proposed fix:** Same as FAIL-2.

### FAIL-5: TX-SPC-0026 — Railroad parking distance (P0)

- **Question:** "What is the minimum distance you must park from railroad tracks?"
- **Current answer:** "100 feet"
- **Current explanation:** "Texas law requires parking at least 100 feet away from railroad tracks."
- **Issue:** **The correct distance is 50 feet**, not 100 feet. Texas Transportation Code §545.302(c)(1) prohibits parking within 50 feet of the nearest rail of a railroad crossing. Notably, question TX-PRK-0008 in the same question bank correctly states 50 feet, creating an **internal contradiction**.
- **Authoritative source:** [TX Transportation Code §545.302](https://texas.public.law/statutes/tex._transp._code_section_545.302)
- **Proposed fix:** Change answer from "100 feet" to "50 feet" and update explanation to cite §545.302 correctly.

### FAIL-6: TX-SIG-0112 — "Slower Traffic Keep Right" enforceability (P1)

- **Question:** "A sign reading 'SLOWER TRAFFIC KEEP RIGHT' is:"
- **Current answer:** "A suggestion for courtesy but not legally enforceable"
- **Issue:** **This is legally enforceable in Texas.** Texas Transportation Code §545.051 requires slower drivers to drive in the right lane on multi-lane highways. Violations can result in fines up to $200. The answer stating it's "not legally enforceable" is incorrect.
- **Authoritative source:** [TX Transportation Code §545.051](https://texas.public.law/statutes/tex._transp._code_section_545.051); [Texas Left Lane Law](https://liggettlawgroup.com/blog/car-accidents/texas-left-lane-law/)
- **Proposed fix:** Change answer to "A legal requirement in Texas — slower traffic must use the right lane" and reference §545.051.

---

## REVIEW Items (4 questions)

### REVIEW-1: TX-SPD-0003 — Maximum rural highway speed (P1)

- **Question:** "What is the maximum speed limit on rural Texas highways?"
- **Current answer:** "70-75 mph depending on the road"
- **Issue:** While 70-75 mph is the most common range, **Texas allows speed limits up to 85 mph** under §545.353. SH 130 (toll road between Austin and San Antonio) is posted at 85 mph, the highest in the US. The answer understates the legal maximum.
- **Authoritative source:** [TX Transportation Code §545.353](https://codes.findlaw.com/tx/transportation-code/transp-sect-545-353/); [TxDOT Speed Limits](https://www.txdot.gov/safety/driving-laws/speed-limits/limits.html)
- **Proposed fix:** Update to "Up to 85 mph on certain highways, though most rural highways are 70-75 mph."

### REVIEW-2: TX-PAV-0068 — Bike lane merge distance (P1)

- **Question:** "Can you drive in a bike lane to prepare for a right turn?"
- **Current answer:** "Yes, you should merge into the bike lane within 200 feet of the turn when safe"
- **Issue:** The 200-foot figure comes from TxDOT design guidelines (50-200 feet for bike lane transitions), not from the Texas Transportation Code. The state code does not specify a merge distance for bike lanes. The answer presents a design guideline as law.
- **Authoritative source:** [TxDOT Bicycle Facilities Design Guide](https://www.txdot.gov/manuals/des/rdw/chapter-18-bicycle-facilities-/18-5-intersections-and-crossings.html)
- **Proposed fix:** Change to "Yes, you may merge into the bike lane when preparing for a right turn" without specifying a distance, or qualify that 200 feet is a design recommendation.

### REVIEW-3: TX-ROW-0077 — Emergency vehicle following distance scope (P2)

- **Question:** "Can you follow closely behind an emergency vehicle with lights and sirens to get through traffic?"
- **Current answer:** "No, this is illegal and dangerous"
- **Current explanation:** "It is illegal to follow within 500 feet of an emergency vehicle responding to a call."
- **Issue:** §545.407 specifically applies to **fire apparatus and ambulances**, not all emergency vehicles. The question generalizes to "emergency vehicle" which is slightly overbroad. The 500-foot distance is correct for fire/EMS.
- **Authoritative source:** [TX Transportation Code §545.407](https://texas.public.law/statutes/tex._transp._code_section_545.407)
- **Proposed fix:** Minor — either specify "fire apparatus or ambulance" in the explanation, or note that the statute specifically covers these vehicle types.

### REVIEW-4: TX-SAF-0056 — High beam dimming distance (P2)

- **Question:** "When should you use high beams, and when should you dim them?"
- **Current answer:** "Use high beams in rural areas, dim within 500 feet of oncoming traffic"
- **Issue:** The 500-foot figure for oncoming traffic is correct (§547.333). However, the answer omits that **you must also dim within 300 feet when following another vehicle**. The answer is correct but incomplete.
- **Authoritative source:** [TX Transportation Code §547.333](https://texas.public.law/statutes/tex._transp._code_section_547.333)
- **Proposed fix:** Add "or within 300 feet of a vehicle you are following" to the answer.

---

## Internal Inconsistencies

### Inconsistency 1: Railroad parking distance

- **TX-PRK-0008** says: "50 feet" (CORRECT per §545.302)
- **TX-SPC-0026** says: "100 feet" (INCORRECT — should be 50 feet)
- **Impact:** A test-taker could encounter both questions and get contradictory information.
- **Resolution:** Fix TX-SPC-0026 to match the correct 50-foot distance.

### Inconsistency 2: School zone speed limits

- **TX-SIG-0022** says: "20 mph"
- **TX-SPC-0004** says: "15 to 20 mph"
- **TX-SPD-0001** says: "15-20 mph"
- **Impact:** Minor inconsistency. Texas school zones vary; 15-20 mph is more accurate. 20 mph is the most common but not universal.
- **Resolution:** Standardize to "15-20 mph" across all questions, or specify "typically 20 mph" in TX-SIG-0022.

---

## Verified Correct Claims (sampling of high-risk items)

The following high-risk numerical claims were **verified as correct** against Texas law:

| Claim | Question(s) | Source | Status |
|-------|------------|--------|--------|
| 15 feet from fire hydrant | TX-PRK-0001, TX-PRK-0030 | §545.302(b)(2) | PASS |
| 20 feet from crosswalk | TX-PRK-0002 | §545.302(b)(1) | PASS |
| 30 feet from stop sign | TX-PRK-0005 | §545.302(b)(3) | PASS |
| 50 feet from railroad crossing (parking) | TX-PRK-0008 | §545.302(c)(1) | PASS |
| 20 feet from fire station (same side) | TX-PRK-0012 | §545.302(b)(6) | PASS |
| 75 feet from fire station (opposite side) | TX-PRK-0013 | §545.302(b)(7) | PASS |
| 18 inches from curb (parallel parking) | TX-PRK-0025 | §545.303 | PASS |
| $500+ fine for disabled parking violation | TX-PRK-0042 | §681.011 | PASS |
| BAC 0.08% limit (21+) | TX-SAF-0026 | TX Penal Code §49.01 | PASS |
| BAC 0.04% limit (commercial) | TX-SAF-0032 | TX Penal Code §49.01 | PASS |
| Zero tolerance under 21 | TX-SAF-0027 | TX Alcoholic Beverage Code §106 | PASS |
| 30 mph residential prima facie | TX-SPD-0002, TX-SPD-0024 | §545.352(b)(1) | PASS |
| 15 mph alley prima facie | TX-SPD-0014 | §545.352(b)(2) | PASS |
| Headlights 30 min after sunset / before sunrise | TX-SAF-0055 | §547.302(a) | PASS |
| 20 feet from school bus | TX-ROW-0064, TX-SPC-0003 | §545.066 | PASS |
| $500-$1,250 fine for passing school bus | TX-SPC-0006 | §545.066 | PASS |
| 15-50 feet from railroad tracks (stopping) | TX-SPC-0016 | §545.251 | PASS |
| Move Over: 20 mph below / 5 mph if <=25 mph | TX-SPC-0034, TX-SPC-0036 | §545.157 | PASS |
| $2,000 max fine for Move Over violation | TX-SPC-0038 | §545.157 | PASS |
| $1,000 property damage crash report threshold | TX-SPC-0051 | §550.062 | PASS |
| 500 feet following fire/ambulance | TX-ROW-0077 | §545.407 | PASS (scope slightly overbroad) |
| Child safety seat until age 8 / 4'9" | TX-SAF-0084 | §545.412 | PASS |
| Left turn on red: one-way to one-way | TX-TRA-0005 | §544.007 | PASS |
| 2/32" tire tread minimum | TX-SAF-0066 | TX inspection standards | PASS |
| Doubled fines in work zones | TX-SPC-0032 | §542.404 | PASS |

---

## Methodology

1. **Extraction:** All 660 questions extracted from `data/tx/tx_v1.json` with correct answers and explanations.
2. **Risk triage:** Questions containing specific numerical legal claims (distances, fines, BAC levels, speed limits, ages) identified as high-risk — 67 questions flagged for detailed verification.
3. **Verification:** High-risk claims cross-referenced against:
   - Texas Transportation Code (primary statutory authority)
   - Texas Penal Code (DWI/intoxication offenses)
   - Texas DPS official publications
   - TxDOT safety guidance
   - Legal analysis websites (FindLaw, Justia, texas.public.law)
   - Cycling/pedestrian advocacy organizations (BikeTexas, BikeD/FW)
4. **Remaining questions:** Questions without specific numerical legal claims reviewed for general correctness of traffic rules, sign meanings, and driving practices against the DL-7 handbook content.

---

## Recommendations

### Immediate (P0 — before next app release)
1. **Fix 3-foot bicycle passing claims** (TX-ROW-0045, TX-ROW-0053, TX-SPD-0078) — Texas has no statewide 3-foot law.
2. **Fix headlight/wiper claim** (TX-SAF-0061) — Texas does not require headlights specifically when wipers are on.
3. **Fix railroad parking distance** (TX-SPC-0026) — correct from 100 feet to 50 feet.

### Soon (P1 — within next update cycle)
4. **Fix "Slower Traffic Keep Right" enforceability** (TX-SIG-0112) — it IS legally enforceable.
5. **Update maximum rural speed limit** (TX-SPD-0003) — include 85 mph possibility.
6. **Qualify bike lane merge distance** (TX-PAV-0068) — 200 feet is a guideline, not law.

### Nice to have (P2 — low priority)
7. **Narrow emergency vehicle following scope** (TX-ROW-0077) — specify fire/ambulance.
8. **Add 300-foot dimming distance for following** (TX-SAF-0056).
9. **Standardize school zone speed** (TX-SIG-0022 vs TX-SPC-0004/TX-SPD-0001).

---

## Companion Files

- **CSV matrix:** `docs/quality/tx-legal-correctness-matrix-2026-02.csv`
  - All 660 questions with status, risk level, and proposed fixes
  - Columns: `question_id`, `topic`, `claim_summary`, `reference_in_question`, `authoritative_source`, `status`, `risk_level`, `proposed_fix`
