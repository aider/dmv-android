# Texas DMV Question Bank v1.0

A comprehensive offline Texas driver-test question bank with **660 original questions** across 8 topics, accompanied by a reusable SVG asset library of **109 hand-coded traffic signs, signals, and diagrams**.

All content is **original** or **public domain** to ensure legal compliance. No copyrighted material from Texas DPS handbooks or practice websites.

---

## 📊 Project Statistics

| Metric | Value |
|--------|-------|
| **Total Questions** | 660 |
| **Topic Files** | 8 |
| **SVG Assets** | 109 (hand-coded) |
| **Questions with Images** | 136 (21%) |
| **Average Difficulty** | 2.66/5 |
| **Schema Compliance** | 100% |
| **Lines of Code** | 22,500+ |

---

## 📚 Topics & Question Distribution

| Topic | Questions | With Images | Avg Difficulty |
|-------|-----------|-------------|----------------|
| **Signs** | 120 | 50 (42%) | 2.82 |
| **Traffic Signals** | 60 | 15 (25%) | 2.72 |
| **Pavement Markings** | 70 | 22 (31%) | 2.90 |
| **Right-of-Way** | 120 | 14 (12%) | 2.58 |
| **Speed & Distance** | 80 | 9 (11%) | 2.81 |
| **Parking** | 60 | 9 (15%) | 2.53 |
| **Safe Driving** | 90 | 5 (6%) | 2.40 |
| **Special Situations** | 60 | 12 (20%) | 2.48 |

---

## 🗂️ Project Structure

```
dmv.tx/
├── data/tx/
│   ├── topics/              # Individual topic JSON files
│   │   ├── signs.json
│   │   ├── traffic_signals.json
│   │   ├── pavement_markings.json
│   │   ├── right_of_way.json
│   │   ├── speed_and_distance.json
│   │   ├── parking.json
│   │   ├── safe_driving.json
│   │   └── special_situations.json
│   ├── tx_v1.json           # Merged 660 questions with metadata
│   └── review/
│       ├── index_by_topic.md
│       └── questions_with_images.md
├── assets/
│   ├── manifest.json        # Asset registry with licensing
│   └── svg/                 # 109 hand-coded SVG images
├── scripts/
│   ├── validate_questions.js
│   ├── validate_assets.js
│   ├── merge_topics.js
│   └── generate_review_tables.js
└── README.md
```

---

## 🎨 SVG Asset Library (109 Assets)

### Regulatory Signs (20)
STOP, YIELD, speed limits, NO TURN signs, parking restrictions, one-way, etc.

### Warning Signs (20)
Curves, intersections, merge, pedestrian/bicycle crossings, school zones, construction, etc.

### Guide/Info Signs (9)
Interstate markers, US/State routes, exit signs, distance signs, service signs

### Traffic Signals (13)
Solid lights (red/yellow/green), arrow signals, flashing signals, pedestrian signals

### Intersection Diagrams (8)
4-way stop, uncontrolled, roundabout, merge, crosswalk, school bus, emergency vehicle

### Pavement Markings (22)
Lane lines, arrows, crosswalks, stop/yield lines, bike lanes, HOV, sharrows, special markings

### Speed/Distance Diagrams (6)
Following distance, stopping distance, school zones, passing clearance

### Parking/Safe Driving (11)
Parallel parking, hill parking, blind spots, space cushion, mirror adjustment, tire tread, work zones, railroad crossings

---

## 📖 Question Schema

Each question follows this exact structure:

```json
{
  "id": "TX-XXX-0001",
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

**Topic Codes:**
- `SIGNS` → TX-SIG-XXXX
- `TRAFFIC_SIGNALS` → TX-TRA-XXXX
- `PAVEMENT_MARKINGS` → TX-PAV-XXXX
- `RIGHT_OF_WAY` → TX-ROW-XXXX
- `SPEED_AND_DISTANCE` → TX-SPD-XXXX
- `PARKING` → TX-PRK-XXXX
- `SAFE_DRIVING` → TX-SAF-XXXX
- `SPECIAL_SITUATIONS` → TX-SPC-XXXX

---

## 🛠️ Validation & Scripts

### Validate Questions
```bash
node scripts/validate_questions.js
```
Checks: schema compliance, ID uniqueness, topic counts, required fields

### Validate Assets
```bash
node scripts/validate_assets.js
```
Checks: manifest integrity, file existence, asset references, licensing metadata

### Merge Topics
```bash
node scripts/merge_topics.js
```
Combines all 8 topic files into `data/tx/tx_v1.json` with metadata

### Generate Review Tables
```bash
node scripts/generate_review_tables.js
```
Creates markdown tables: `index_by_topic.md` and `questions_with_images.md`

---

## ✅ Quality Assurance

- ✅ All 660 questions validated
- ✅ All IDs unique
- ✅ 100% schema compliance
- ✅ All 109 assets in manifest
- ✅ All asset references valid
- ✅ No orphaned or missing assets
- ✅ Topic counts match targets exactly

---

## 📜 Legal Compliance

### Original Content
- All questions written in original language
- No copying from Texas DPS Driver Handbook (DL-7)
- Concepts based on Texas driving laws, but all wording is original
- Explanations are educational and originally written

### SVG Assets
- All 109 SVGs are **hand-coded** (100% original)
- Based on MUTCD (Manual on Uniform Traffic Control Devices) standards
- Public domain design patterns (traffic signs are standardized)
- No copyrighted images or artwork

### Licensing
- All generated assets marked as "generated" in manifest.json
- No external image sources or third-party assets
- Safe for educational use without licensing concerns

---

## 🚀 Usage

### Individual Topics
Load any topic file for targeted practice:
```javascript
const signs = require('./data/tx/topics/signs.json');
// Array of 120 sign questions
```

### Full Question Bank
Load the complete merged file:
```javascript
const txQuestions = require('./data/tx/tx_v1.json');
console.log(txQuestions.totalQuestions); // 660
console.log(txQuestions.topics); // Topic breakdown
console.log(txQuestions.questions); // All 660 questions
```

### Asset Manifest
Reference SVG assets:
```javascript
const manifest = require('./assets/manifest.json');
// Find asset by ID
const stopSign = manifest.find(a => a.assetId === 'MUTCD_R1-1_STOP');
console.log(stopSign.file); // assets/svg/MUTCD_R1-1_STOP.svg
```

---

## 🎯 Difficulty Distribution

- **Easy (1-2)**: ~30% - Basic knowledge and common rules
- **Medium (3)**: ~40% - Practical application and scenario-based
- **Hard (4-5)**: ~30% - Complex situations and edge cases

---

## 📝 Texas-Specific Rules Covered

- BAC limits (0.08% for 21+, 0.04% commercial, 0.00% under 21)
- School bus stopping (20 feet, both sides of undivided roads)
- School zone speed limits (15-20 mph when children present)
- Move Over Law (change lanes or slow 20 mph below limit)
- Headlight requirements (30 min after sunset to 30 min before sunrise)
- Seatbelt requirements (all occupants)
- Child safety seats (until age 8 or 4'9" height)
- 3-foot passing law for bicycles
- Railroad crossing distance (15-50 feet from tracks)
- Fire hydrant parking (15 feet minimum)
- Parallel parking distance (18 inches from curb)
- Work zone fines (doubled in active zones)

---

## 🔧 Development

### Requirements
- Node.js (for validation scripts)
- No external dependencies

### Adding Questions
1. Edit appropriate topic file in `data/tx/topics/`
2. Follow exact schema structure
3. Assign unique ID in sequence
4. Run validation: `node scripts/validate_questions.js`
5. Regenerate merged file: `node scripts/merge_topics.js`
6. Update review tables: `node scripts/generate_review_tables.js`

### Adding Assets
1. **Read the style guide**: `docs/growth/image-style-guide.md` (REQUIRED for all visual assets)
2. Create hand-coded SVG in `assets/svg/` following learner-focused design standards
3. Add entry to `assets/manifest.json`
4. Reference in question using `assetId`
5. Run validation: `node scripts/validate_assets.js`

**Important**: All SVG assets must follow the mobile readability standards and MUTCD accuracy guidelines in the style guide.

---

## 📊 Statistics

```
Total Files Created: 126
Total Lines: 22,500+
Development Sessions: 4
SVG Assets Hand-Coded: 109
Questions Generated: 660
Validation Scripts: 4
Review Documents: 2
```

---

## 🤝 Credits

**Question Content**: All original, based on Texas Transportation Code and MUTCD standards
**SVG Assets**: Hand-coded, following MUTCD design specifications
**Co-Authored-By**: Claude Opus 4.6 <noreply@anthropic.com>

---

## 📄 License

This project contains original educational content for Texas driver education.

**Questions**: Original content, free to use for educational purposes
**SVG Assets**: Original hand-coded graphics based on public domain MUTCD standards

---

## 🎓 Intended Use

This question bank is designed for:
- Texas driver education programs
- Student self-study and practice
- Driving school curriculum
- Offline driver test preparation
- Educational applications

**Not affiliated with or endorsed by the Texas Department of Public Safety (DPS)**

---

**Version**: 1.0
**Generated**: February 2026
**State**: Texas (TX)
**Total Questions**: 660
