# Home Flow Redesign Spec (Feb 2026)

## Problem
The current home screen is a long configuration surface (mode, topics, questions, difficulty) that does not guide first-time users toward a fast, confident start. Activation (time-to-first-quiz) suffers because users must understand all options before they can begin.

## Goal
Redesign home into a guided flow that improves activation while preserving advanced controls for returning users.

## UX Objectives
- First-time users can start a quiz in **one tap** (Quick Start).
- Advanced users can still configure mode, topics, difficulty, and question count.
- Reduce cognitive load above the fold.

## User State Model

Home adapts based on quiz history:

| State | Condition | Behavior |
|---|---|---|
| **FirstRun** | No attempts in DB | Quick Start prominent, helper text "Jump right in" |
| **Returning** | Has attempt history | Progress Card (last score, mistakes, weakest topic) + Quick Start |

## Screen Zones (top to bottom)

| Zone | Content | Visibility |
|---|---|---|
| **A: Header** | Title + value prop + content summary | Always |
| **B: Quick Start** | Filled Button "Quick Start" + config summary (20q, All topics, Practice) | Always, above fold |
| **C: Progress Card** | ElevatedCard: last score %, mistake count, weakest topic + action CTAs | Returning users only |
| **D: Customize** | Expandable section with all existing config controls + "Start Custom Quiz" | Collapsed by default |
| **E: Secondary** | View Stats, Debug (debug builds), About | Always |

## Zone Details

### A: Header (unchanged)
```
TX DMV Practice
Pass your Texas permit test on the first try
660 questions . 8 topics . 100% offline
```

### B: Quick Start
- Full-width filled Button, 56dp height
- Label: "Quick Start . 20 Questions"
- Config: Practice mode, 20 questions, all topics, difficulty 1-3
- Ignores any Customize settings
- One tap -> quiz starts immediately

### C: Progress Card (Returning users)
- Material 3 `ElevatedCard`
- Content:
  - "Last score: 85%" (from most recent attempt)
  - "X mistakes to review" (if mistakeCount > 0)
  - Weakest topic name + accuracy % (lowest accuracy from getAccuracyByTopic)
- Action buttons (up to 2):
  - "Review Mistakes" -> starts Mistakes mode quiz (if mistakeCount > 0)
  - "Drill Weak Topic" -> starts Topic Drill for weakest topic

### D: Customize (Expandable)
- Collapsed by default with "Customize Quiz" header + expand icon
- Contains ALL existing controls (zero regression):
  - Mode selector (Practice/Exam/Topic Drill/Mistakes)
  - Topic checkboxes with All/None
  - Question count chips (10/20/30/50)
  - Difficulty range slider
- "Start Custom Quiz" button at bottom of section
- Sticky bottom bar with "Start Quiz" removed (replaced by in-section CTA)

### E: Secondary (unchanged styling)
- "View Stats" OutlinedButton
- "Debug" TextButton (debug builds only)
- "About" TextButton

## Telemetry Events

| Event | Trigger | Properties |
|---|---|---|
| `home_viewed` | Home screen displayed | question_count |
| `quick_start_tapped` | Quick Start button tapped | - |
| `customize_expanded` | Customize section opened | - |
| `customize_collapsed` | Customize section closed | - |
| `custom_quiz_started` | Start Custom Quiz tapped | mode, question_count, topic_count |
| `review_mistakes_tapped` | Review Mistakes from Progress Card | mistake_count |
| `drill_weak_topic_tapped` | Drill Weak Topic from Progress Card | topic |
| `stats_opened` | View Stats tapped | - |

## User Journeys

### Journey 1: First-time user
1. Opens app -> Import screen (first run)
2. Import completes -> Home screen
3. Sees "Quick Start . 20 Questions" button above fold
4. Taps Quick Start -> Quiz begins immediately
5. Time to first quiz: **1 tap** after import

### Journey 2: Returning user wants quick practice
1. Opens app -> Home screen
2. Sees Progress Card ("Last score: 85%", "3 mistakes to review")
3. Taps Quick Start -> Quiz begins
4. OR taps "Review Mistakes" -> Mistakes quiz begins

### Journey 3: Advanced user wants custom config
1. Opens app -> Home screen
2. Taps "Customize Quiz" to expand
3. Selects Topic Drill, picks 2 topics, sets 30 questions, difficulty 2-3
4. Taps "Start Custom Quiz" -> Quiz begins

## Files Changed
- `HomeScreen.kt` -- major restructure (zones A-E)
- `HomeViewModel.kt` -- add progress context queries (last attempt, weakest topic)
- `QuestionStatsDao.kt` -- add getTotalSeen query
- `AttemptDao.kt` -- add getMostRecent suspend query
- `AnalyticsEvents.kt` -- add new event constants
- New: no new files needed (all composables inline in HomeScreen)

## Acceptance Criteria
| AC | Verification |
|---|---|
| Quick Start visible without scrolling | Screenshot on emulator (within first 300dp) |
| User can start quiz in <=1 tap | Quick Start -> quiz launch |
| Customize remains functional | All modes/topics/difficulty still work |
| Progress context shown for returning users | After completing a quiz, return to home, card visible |
