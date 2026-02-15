# Product Review Rubric

## Purpose
This rubric defines a blocking product-quality gate for pull requests. The goal is to catch regressions in code, design, UX, and visual output before merge.

## Scope
Use this rubric for:
- Android UI changes
- SVG/sign asset changes
- Question-image pairing changes
- Any PR that changes the user-visible result

## Blocking Rules
The PR is blocked when any of the following is true:
- Any P0 or P1 issue is found.
- Required evidence is missing.
- Any rubric score is below 4/5.
- A changed sign/diagram has no authoritative reference link.

## Severity Definitions
- P0: Critical correctness issue, dangerous misunderstanding, or major accessibility failure.
- P1: Significant readability/UX regression that can confuse users or reduce task success.
- P2: Noticeable quality issue with workaround.
- P3: Minor polish issue.

## Required Evidence
### For SVG/Sign PRs
- Before and after visual comparison.
- 96dp render for each changed asset.
- 48dp render for each changed asset.
- At least one in-context screenshot from the question screen.
- Reference link to authoritative source (MUTCD or state-specific source).

### For Android UI PRs
- Before and after app screenshots.
- At least one in-context screenshot per changed user flow.
- Device and OS info used for screenshots.

## Rubric (1-5)
### 1. Readability
- 5: Instantly readable at minimum supported size (including 48dp for signs).
- 4: Readable with minor strain; acceptable for release.
- 3: Borderline readability; causes hesitation.
- 2: Frequent readability problems.
- 1: Unreadable or misleading.

### 2. Semantic Clarity
- 5: Visual meaning is unambiguous and matches expected real-world interpretation.
- 4: Mostly clear, no meaningful risk of user misunderstanding.
- 3: Ambiguous in common scenarios.
- 2: Frequent misinterpretation risk.
- 1: Meaning is wrong or contradictory.

### 3. Contrast
- 5: Strong contrast in all tested contexts and states.
- 4: Passes common use cases with minor edge-case weakness.
- 3: Contrast is inconsistent or borderline.
- 2: Frequent low-contrast failures.
- 1: Fails basic accessibility expectations.

### 4. Consistency
- 5: Fully aligned with existing style system and authoritative references.
- 4: Minor deviations only.
- 3: Noticeable inconsistency.
- 2: Significant style or standard mismatch.
- 1: Conflicts with product language or reference standard.

## Review Process
1. Confirm evidence exists in the PR body/comments.
2. Compare visuals at 96dp and 48dp (for sign assets).
3. Validate against authoritative references.
4. Score all four rubric dimensions.
5. Leave findings ordered by severity (P0 to P3).
6. Approve only if all blocking rules pass.

## Reviewer Output Format
- Verdict: PASS or BLOCK
- Findings: ordered list with severity and file/asset reference
- Scores: Readability, Semantic Clarity, Contrast, Consistency
- Required follow-ups (if blocked)
