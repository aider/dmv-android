# Texas DMV Practice - Android App

An offline Android application for Texas driver's license practice tests, featuring 660 original questions across 8 topics.

## Features

- **660 Questions**: Comprehensive question bank covering all Texas DMV topics
- **8 Topic Categories**:
  - Signs (120 questions)
  - Traffic Signals (60 questions)
  - Pavement Markings (70 questions)
  - Right of Way (120 questions)
  - Speed & Distance (80 questions)
  - Parking (60 questions)
  - Safe Driving (90 questions)
  - Special Situations (60 questions)

- **Practice Modes**:
  - Practice by topic (30 questions per session)
  - Mixed practice from all topics
  - Randomized questions each time

- **Learning Features**:
  - Immediate feedback on answers
  - Detailed explanations for each question
  - Score tracking during quiz
  - Pass/fail results (70% passing threshold)

- **Offline Mode**: All questions and content stored locally - no internet required

## Technical Details

### Architecture
- **Language**: Java
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Dependencies**:
  - AndroidX AppCompat
  - Material Design Components
  - Gson (for JSON parsing)

### Data Structure
- Questions stored as JSON files in `app/src/main/assets/questions/`
- SVG traffic signs and diagrams in `app/src/main/assets/svg/`
- Total assets: 109 hand-coded SVG images

### Question Schema
```json
{
  "id": "TX-SIG-0001",
  "topic": "SIGNS",
  "difficulty": 1-5,
  "text": "Question text",
  "choices": ["A", "B", "C", "D"],
  "correctIndex": 0-3,
  "explanation": "Educational explanation",
  "reference": "DL-7: Section Name",
  "image": {
    "type": "svg",
    "assetId": "ASSET_ID"
  }
}
```

## Building the App

### Prerequisites
- Android Studio (Arctic Fox or newer)
- JDK 8 or higher
- Android SDK with API 34

### Build Steps

1. Clone the repository:
```bash
git clone https://github.com/aider/dmv-android.git
cd dmv-android
```

2. Open the project in Android Studio

3. Sync Gradle files (File → Sync Project with Gradle Files)

4. Build the app:
```bash
./gradlew assembleDebug
```

5. Install on device/emulator:
```bash
./gradlew installDebug
```

Or simply click "Run" in Android Studio.

## Project Structure

```
dmv-android/
├── app/
│   ├── src/main/
│   │   ├── java/com/dmv/texas/
│   │   │   ├── MainActivity.java          # Topic selection
│   │   │   ├── QuizActivity.java          # Quiz interface
│   │   │   ├── ResultsActivity.java       # Results screen
│   │   │   ├── Question.java              # Question model
│   │   │   └── QuestionBank.java          # Question loader
│   │   ├── res/
│   │   │   ├── layout/                    # UI layouts
│   │   │   └── values/                    # Strings, colors
│   │   ├── assets/
│   │   │   ├── questions/                 # 8 topic JSON files
│   │   │   └── svg/                       # 109 SVG assets
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

## Usage

1. **Select Topic**: Choose from 8 topics or "All Topics" for mixed practice
2. **Answer Questions**: Read each question and select your answer
3. **Submit**: Click submit to see if you're correct
4. **Learn**: Read the explanation to understand the correct answer
5. **Continue**: Progress through 30 questions per session
6. **View Results**: See your score and pass/fail status

## Content Sources

- **Questions**: All original content based on Texas Transportation Code and MUTCD standards
- **Images**: Hand-coded SVG assets following MUTCD specifications
- **No Copyright Issues**: All content is original or public domain

## Legal

This application is for educational purposes only and is not affiliated with or endorsed by the Texas Department of Public Safety (DPS).

**Content**: Original educational content for Texas driver education
**License**: See parent repository for licensing details

## Version

- **App Version**: 1.0
- **Question Bank**: v1.0 (660 questions)
- **Last Updated**: February 2026

---

**Note**: This app currently displays questions and handles quiz logic. SVG rendering for traffic signs/diagrams requires additional implementation (Android SVG library integration).

## Future Enhancements

- [ ] SVG image rendering for questions with visual assets
- [ ] Bookmarking difficult questions
- [ ] Study mode with unlimited attempts
- [ ] Statistics tracking across sessions
- [ ] Dark mode support
- [ ] Question search/filter
- [ ] Export results to PDF
