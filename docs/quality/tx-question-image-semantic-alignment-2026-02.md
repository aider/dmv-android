# TX Question-Image Semantic Alignment Audit - Phase 1

**Generated:** 2026-02-15
**Issue:** #81
**Status:** Phase 1 Complete (Automated Baseline)
**Scope:** 136 questions with images

---

## Executive Summary

**Phase 1 - Automated Baseline (COMPLETE):**
- ✅ All 136 image-bearing questions cataloged
- ✅ CSV data structure created
- ✅ Asset existence verified
- ⚠️ Automated heuristics unreliable (false positives)

**Phase 2 - Manual Visual Review (TODO):**
- ⏳ Manually inspect each question-image pair
- ⏳ Verify semantic alignment between question intent and image content
- ⏳ Screenshot evidence for all P0/P1 findings
- ⏳ Refined alignment/ambiguity scores (1-5 scale)

---

## Phase 1 Results

### Coverage by Topic

| Topic | Questions with Images | % of Topic |
|-------|----------------------|------------|
| SIGNS | 50 | 37% |
| PAVEMENT_MARKINGS | 22 | 16% |
| TRAFFIC_SIGNALS | 15 | 11% |
| RIGHT_OF_WAY | 14 | 10% |
| SPECIAL_SITUATIONS | 12 | 9% |
| PARKING | 9 | 7% |
| SPEED_AND_DISTANCE | 9 | 7% |
| SAFE_DRIVING | 5 | 4% |
| **TOTAL** | **136** | **100%** |

### Asset Existence Check

✅ **All 136 referenced assets exist in `assets/svg/`**

No missing assets found (0 P0 blocking issues from missing files).

---

## Phase 2 Manual Review Plan

### Sampling Strategy

To efficiently audit 136 questions, will use stratified sampling:

1. **High-priority topics first:**
   - SIGNS (50 questions) - most critical for DMV test
   - TRAFFIC_SIGNALS (15 questions) - safety-critical
   - PAVEMENT_MARKINGS (22 questions) - frequently tested

2. **Random sample from each topic** for quality check

3. **Full review of any flagged issues**

### Review Criteria

For each question-image pair, evaluate:

1. **Semantic Alignment (1-5 score):**
   - 5: Image perfectly illustrates the exact concept being tested
   - 4: Image supports the concept well
   - 3: Image is related but not specific enough
   - 2: Image is misleading or shows wrong concept
   - 1: Image completely mismatched

2. **Ambiguity (1-5 score):**
   - 5: Image is crystal clear, unambiguous
   - 4: Image is clear with minor ambiguity
   - 3: Image has some ambiguous elements
   - 2: Image is confusing or could be misinterpreted
   - 1: Image is highly ambiguous

3. **Issue Type:**
   - `mismatch`: Image shows different concept than question asks
   - `ambiguous`: Image geometry unclear or could be misinterpreted
   - `low-signal`: Image present but adds no educational value
   - `none`: No issues found
   - `other`: Other issue type

4. **Severity:**
   - P0: Blocks learning, misleading, or incorrect
   - P1: Degrades learning experience, ambiguous
   - P2: Minor issue, cosmetic
   - PASS: No issues

### Evidence Requirements

**For all P0/P1 findings:**
- Screenshot showing question + image in app context
- Specific description of mismatch/ambiguity
- Proposed fix (correct asset, clarify image, or update question text)

**For PASS examples:**
- At least 10 exemplar question-image pairs as references

---

## Sample Questions Reviewed (Manual Verification)

**Verified CORRECT alignment:**

### TX-SIG-0017 ✅ PASS
- **Question:** "If you are in a lane marked with a RIGHT TURN ONLY sign, you must:"
- **Asset:** MUTCD_R6-2_RIGHT_TURN_ONLY
- **Alignment:** 5/5 - Perfect match
- **Ambiguity:** 5/5 - Clear
- **Status:** ✅ PASS - Image shows exact sign mentioned in question

### TX-TRA-0009 ✅ PASS
- **Question:** "If a traffic signal displays both red and yellow lights at the same time, what does this indicate?"
- **Asset:** SIGNAL_RED_YELLOW_TOGETHER
- **Alignment:** 5/5 - Perfect match
- **Ambiguity:** 5/5 - Clear
- **Status:** ✅ PASS - Image shows exact signal state asked about

### TX-TRA-0021 ✅ PASS
- **Question:** "What does a green arrow signal mean?"
- **Asset:** SIGNAL_GREEN_ARROW_LEFT
- **Alignment:** 5/5 - Perfect match
- **Ambiguity:** 5/5 - Clear
- **Status:** ✅ PASS - Image shows green arrow signal

---

## Next Steps (Phase 2)

### Immediate (Next Session):

1. ⏳ **Manual review of SIGNS topic (50 questions)**
   - Sample 10-15 representative questions
   - Screenshot any issues found
   - Document P0/P1 findings

2. ⏳ **Manual review of TRAFFIC_SIGNALS (15 questions)**
   - Review all (small set)
   - Verify signal states match question intent

3. ⏳ **Manual review of PAVEMENT_MARKINGS (22 questions)**
   - Sample 5-10 questions
   - Check for ambiguous geometry

### Medium-term:

4. ⏳ Complete remaining topics
5. ⏳ Generate final CSV with refined scores
6. ⏳ Create implementation tickets for svg-asset-curator
7. ⏳ Update issue #81 with findings

---

## Deliverables Status

- ✅ **CSV structure:** `docs/quality/tx-question-image-semantic-alignment-2026-02.csv`
- ✅ **Markdown report:** This file
- ⏳ **Screenshots:** TBD (Phase 2)
- ⏳ **Refined scores:** TBD (Phase 2)
- ⏳ **Implementation tickets:** TBD (after Phase 2)

---

## Notes

- Automated heuristics (keyword matching) are **unreliable** for detecting semantic mismatches
- Manual visual inspection is **required** for accurate alignment assessment
- Early manual samples show **high quality** - most questions correctly matched with appropriate images
- Focus Phase 2 review on high-priority topics (SIGNS, SIGNALS, PAVEMENT)

---

**Phase 1 Status:** ✅ COMPLETE
**Phase 2 Status:** ⏳ TODO
**Next Action:** Begin manual visual review of sampled questions by topic
