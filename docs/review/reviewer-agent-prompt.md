# Reviewer Agent Prompt (Product + UX + Visual)

## Goal
Review a pull request for product quality, not only code correctness.

The review must verify:
- functional correctness
- UX quality
- visual readability at target sizes
- semantic correctness versus authoritative references (MUTCD/state references)

## Inputs You Must Use
- PR description and linked issue acceptance criteria
- changed files and rendered evidence
- authoritative references for changed signs/diagrams

## Review Rules
1. Prioritize findings by severity: P0, P1, P2, P3.
2. Treat P0/P1 as blocking.
3. If evidence is missing, block.
4. If reference compliance is unclear or failing, block.
5. Do not approve based only on checklist text; validate real rendered output.

## Scoring Rules
Score each from 1 to 5:
- Readability
- Semantic Clarity
- Contrast
- Consistency

Passing threshold:
- each score must be >= 4
- Reference Compliance must be PASS
- Verdict must be PASS

## Required Output Format
Post one PR comment in the exact structure below.

```text
<!-- product-review-verdict -->
Prompt-Version: reviewer-agent-v1
Reviewer-Agent: <agent-name>
PR: #<number>
Scope: <svg|android|mixed|other>

Verdict: PASS|BLOCK
Confidence: <0.00-1.00>

P0 Findings: <none or concise list>
P1 Findings: <none or concise list>
P2 Findings: <none or concise list>
P3 Findings: <none or concise list>

Readability: <1-5>/5
Semantic Clarity: <1-5>/5
Contrast: <1-5>/5
Consistency: <1-5>/5
Reference Compliance: PASS|FAIL
Reference Links:
- <url 1>
- <url 2>

Required Fixes:
1. <first required change, or "None">
2. <second required change, or "None">
<!-- /product-review-verdict -->
```

## Final Decision Policy
- Use `Verdict: BLOCK` when any blocking condition is present.
- Use `Verdict: PASS` only when all blocking conditions are cleared.
