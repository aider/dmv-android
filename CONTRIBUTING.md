# Contributing to Texas DMV Question Bank

Thank you for your interest in improving the Texas DMV question bank! This guide will help you contribute effectively.

---

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [How to Contribute](#how-to-contribute)
3. [Question Contribution Guidelines](#question-contribution-guidelines)
4. [SVG Asset Contribution Guidelines](#svg-asset-contribution-guidelines)
5. [Pull Request Process](#pull-request-process)

---

## Code of Conduct

This project is dedicated to providing a harassment-free experience for everyone. We expect all contributors to:
- Be respectful and inclusive
- Focus on constructive feedback
- Prioritize educational value and accuracy
- Respect copyright and licensing requirements

---

## How to Contribute

### Reporting Issues
- **Inaccurate questions**: Open an issue with the question ID and the specific inaccuracy
- **SVG rendering problems**: Include screenshots and device/browser information
- **Schema violations**: Report validation failures with error messages

### Suggesting Improvements
- **New questions**: Describe the topic area and learning objective
- **Asset enhancements**: Explain what's unclear or could be improved
- **Documentation**: Suggest areas that need clarification

---

## Question Contribution Guidelines

### Before Adding Questions

1. **Verify originality**: All question text must be originally written (no copying from DPS handbooks or other sources)
2. **Check for duplicates**: Search existing questions to avoid redundant content
3. **Confirm Texas specificity**: Ensure the question covers Texas-specific laws or MUTCD standards

### Question Requirements

All questions must follow the exact schema:

```json
{
  "id": "TX-XXX-####",
  "topic": "TOPIC_NAME",
  "difficulty": 1-5,
  "text": "Question text",
  "choices": ["Choice A", "Choice B", "Choice C", "Choice D"],
  "correctIndex": 0-3,
  "explanation": "Educational explanation (1-3 sentences)",
  "reference": "DL-7: Section Name",
  "image": {
    "type": "svg",
    "assetId": "ASSET_ID"
  }
}
```

**Key Guidelines:**
- Use proper topic code prefix (TX-SIG, TX-TRA, TX-PAV, TX-ROW, TX-SPD, TX-PRK, TX-SAF, TX-SPC)
- Assign sequential ID within topic
- Provide clear, concise explanations (1-3 sentences)
- Reference DL-7 handbook section where applicable
- Include image only if it enhances learning (not decoration)

### Topic-Specific Counts

| Topic | Target | Current |
|-------|--------|---------|
| Signs | 120 | ✓ |
| Traffic Signals | 60 | ✓ |
| Pavement Markings | 70 | ✓ |
| Right-of-Way | 120 | ✓ |
| Speed & Distance | 80 | ✓ |
| Parking | 60 | ✓ |
| Safe Driving | 90 | ✓ |
| Special Situations | 60 | ✓ |

If adding questions beyond these targets, justify the need in your PR description.

---

## SVG Asset Contribution Guidelines

### **REQUIRED READING**

Before creating or modifying any SVG asset, you **MUST** read:

📖 **[Image Style Guide for Learner-Focused DMV Visuals](docs/growth/image-style-guide.md)**

This guide contains:
- 3 good examples (what to emulate)
- 3 bad examples (what to avoid)
- 8 actionable rules for mobile readability
- Asset creation workflow
- Quality metrics and validation checklist

### Critical Standards (Summary)

**Mobile Readability Minimums:**
- Stroke widths: 6px minimum (200×200 viewBox), 8px minimum (300×200 viewBox)
- Text sizes: 20px minimum (200×200 viewBox), 24px minimum (300×200 viewBox)
- Test at 96dp and 48dp widths before submitting

**Sign Geometry Accuracy:**
- STOP signs: True regular octagon (all angles 135°)
- DO NOT ENTER: Square with rounded corners (NOT circle)
- Speed limit signs: Portrait 150×200 viewBox (NOT 200×200)
- All MUTCD signs must match real-world specifications

**Padding Standards:**
- Minimum: 8% of primary dimension
- Recommended: 10% for regulatory signs
- Portrait signs (150×200): 15px horizontal, 20px vertical

**Color Palette:**
- Use ONLY the standard colors defined in the style guide
- Road surface: #4A4A4A
- Ego vehicle: #3366CC (always blue)
- Other vehicles: #666666 (always gray)
- MUTCD colors: Red #C1272D, Yellow #FFCC00, etc.

**Forbidden:**
- ❌ Decorative shadows or texture overlays
- ❌ Transform wrappers with scale()
- ❌ Colors outside the standard palette
- ❌ Hand-drawn sign geometry (use mathematical formulas)
- ❌ External font dependencies

### Asset Submission Checklist

Before submitting SVG assets, verify:

- [ ] Read the complete style guide (`docs/growth/image-style-guide.md`)
- [ ] ViewBox matches category standard
- [ ] All colors from standard palette
- [ ] Stroke widths ≥ minimum (6px for 200×200, 8px for 300×200)
- [ ] Text sizes ≥ minimum (20px for 200×200, 24px for 300×200)
- [ ] No visual noise (shadows, textures, gradients)
- [ ] Content within viewBox bounds (no clipping)
- [ ] Padding ≥ 8% (recommend 10%)
- [ ] Renders clearly at 96dp width in browser
- [ ] Recognizable at 48dp width
- [ ] Manifest entry added with proper metadata
- [ ] Validation script passes (`node scripts/validate_assets.js`)

### Manifest Entry Format

```json
{
  "assetId": "CATEGORY_DESCRIPTIVE_NAME",
  "description": "Brief learner-friendly description",
  "file": "assets/svg/CATEGORY_DESCRIPTIVE_NAME.svg",
  "purpose": "icon",
  "tags": ["category", "topic", "purpose"],
  "status": "ok",
  "lastReviewedAt": "YYYY-MM-DD",
  "sourceUrl": "generated",
  "license": "generated",
  "notes": "Optional implementation notes"
}
```

---

## Pull Request Process

### 1. Fork and Clone
```bash
git clone https://github.com/YOUR_USERNAME/dmv.tx.git
cd dmv.tx
```

### 2. Create a Feature Branch
```bash
git checkout -b feature/add-railroad-questions
# or
git checkout -b fix/stop-sign-geometry
```

### 3. Make Your Changes

**For Questions:**
- Edit topic file in `data/tx/topics/`
- Follow schema exactly
- Run validation: `node scripts/validate_questions.js`
- Regenerate merged file: `node scripts/merge_topics.js`

**For SVG Assets:**
- Create/edit SVG in `assets/svg/`
- Update `assets/manifest.json`
- Run validation: `node scripts/validate_assets.js`
- Test at 96dp and 48dp widths in browser

### 4. Validate Your Changes

```bash
# Validate all questions
node scripts/validate_questions.js

# Validate all assets
node scripts/validate_assets.js

# Regenerate merged question bank
node scripts/merge_topics.js

# Update review tables
node scripts/generate_review_tables.js
```

**All validation must pass before submitting PR.**

### 5. Commit Your Changes

Use clear, descriptive commit messages:

```bash
# Good examples:
git commit -m "Add 10 railroad crossing questions to Special Situations"
git commit -m "Fix STOP sign geometry - regenerate as regular octagon"
git commit -m "Increase stroke widths in PAVEMENT_BIKE_LANE to meet 2.9dp minimum"

# Bad examples:
git commit -m "Update files"
git commit -m "Fix stuff"
git commit -m "Changes"
```

### 6. Submit Pull Request

**PR Title Format:**
- `[Questions] Add 10 railroad crossing questions`
- `[Assets] Fix STOP sign geometry (Issue #XX)`
- `[Docs] Update style guide with parking scenario examples`

**PR Description Must Include:**
- Summary of changes
- Validation results (copy/paste output)
- For assets: screenshots at 96dp and 48dp
- For questions: list of question IDs added/modified
- Justification for any deviations from standards

**PR Checklist:**
- [ ] All validation scripts pass
- [ ] Questions follow exact schema
- [ ] Assets follow style guide (for SVG changes)
- [ ] Manifest updated (for asset changes)
- [ ] No copyrighted content
- [ ] Educational value is clear
- [ ] Tested rendering (for assets)

### 7. Code Review

Expect feedback on:
- Accuracy of content
- Schema compliance
- Style guide adherence
- Mobile readability
- Educational effectiveness

Be prepared to:
- Make revisions based on feedback
- Provide additional testing screenshots
- Clarify educational rationale
- Adjust geometry/sizing for mobile readability

---

## Testing Your Changes

### Question Testing
1. Load questions in JSON validator
2. Verify all required fields present
3. Check that correct answer makes sense
4. Ensure explanation is clear and concise
5. Verify reference is accurate

### Asset Testing
1. Open SVG in browser at full size
2. View at 96dp width (mobile standard)
3. View at 48dp width (list item size)
4. Check for clipping or overflow
5. Verify colors match palette
6. Test rendering in Android app (if possible)

### Validation Commands
```bash
# Run all validation
npm test

# Or run individually:
node scripts/validate_questions.js
node scripts/validate_assets.js
```

---

## Style Guide Resources

### Must-Read Documentation
- **Main style guide**: `docs/growth/image-style-guide.md` ⭐ **REQUIRED**
- Technical specs: `assets/review/style_guide.md`
- Audit reports: `assets/review/svg_audit_report.md`
- Fix examples: `assets/review/issue_53_fix_report.md`

### Quick References
- MUTCD sign colors: See style guide Rule 5
- ViewBox standards: See style guide Rule 7
- Mobile readability minimums: See style guide Rule 1
- Padding standards: See style guide Rule 3
- Training cues: See style guide Rule 4

---

## Legal & Licensing

### Content Originality
- **All question text must be original** - no copying from DPS handbooks or other sources
- Concepts based on Texas law, but wording must be unique
- Explanations must be educational and originally written

### Asset Originality
- All SVG assets must be hand-coded (no copied images)
- Based on MUTCD public domain standards
- No copyrighted artwork or external images
- Mark as "generated" in manifest.json

### Attribution
If you contribute significant content:
- Your name/handle will be added to CONTRIBUTORS.md
- Commits will be attributed to you
- You agree to educational use license

---

## Questions?

If you're unsure about:
- **Question accuracy**: Ask in issue comments before submitting
- **Sign geometry**: Consult MUTCD specs or existing correct assets
- **Mobile readability**: Test at 96dp/48dp, if unclear it's wrong
- **Style guide compliance**: Read the examples in `docs/growth/image-style-guide.md`

**Need Help?** Open an issue with the "question" label or comment on an existing relevant issue.

---

## Thank You!

Your contributions help Texas learners prepare for their driver's license exam. Quality matters—take time to follow the guidelines, and we'll help you polish your contribution.

**Remember:** When in doubt, prioritize clarity and accuracy over cleverness. Learners need clear, correct information.

---

**Last Updated:** 2026-02-14
**Contact:** Open an issue for questions
