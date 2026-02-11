# UX/Growth Audit — TX DMV Practice App
**Date:** 2026-02-10
**Baseline screenshots:** `dmv-android/docs/evidence/` (captured 2026-02-09)

---

## 1. Screen-by-Screen Findings

### 1.1 Home Screen
| # | Finding | Funnel Stage | Priority | Impact | Effort |
|---|---------|-------------|----------|--------|--------|
| H1 | **Start Quiz CTA is below the fold.** Users must scroll past 8 topic checkboxes, question count, and difficulty before seeing the primary action button. First-time users may not realize the page scrolls. | Activation | P0 | High | Low |
| H2 | **Topic names use raw enum keys** (`PAVEMENT_MARKINGS`, `RIGHT_OF_WAY`). These look like developer constants, not user-friendly labels. Hurts perceived quality and comprehension. | Activation | P0 | High | Low |
| H3 | **No onboarding value proposition.** Nothing tells a first-time user what this app does, who it's for, or how quickly they can get value (e.g., "Pass your TX permit test — practice 660 real questions"). | Acquisition | P1 | Medium | Medium |
| H4 | **No quick-start shortcut.** New users must understand Mode, Topics, Questions, Difficulty before starting. A "Quick Start" one-tap path (20 random questions, all topics, practice mode) would reduce time-to-first-quiz. | Activation | P1 | Medium | Medium |
| H5 | **Topic list has no visual density.** Each topic row is tall with a full checkbox, consuming vertical space. Compact chips or a collapsible section would reduce scroll depth. | Activation | P2 | Low | Medium |

### 1.2 Quiz Screen
| # | Finding | Funnel Stage | Priority | Impact | Effort |
|---|---------|-------------|----------|--------|--------|
| Q1 | **Topic badge uses raw enum key** (`SAFE_DRIVING`). Same issue as H2 — should display "Safe Driving". | Activation | P0 | High | Low |
| Q2 | Quiz flow is otherwise clean: progress bar, answer feedback, explanation cards work well. No critical issues. | — | — | — | — |

### 1.3 Results Screen
| # | Finding | Funnel Stage | Priority | Impact | Effort |
|---|---------|-------------|----------|--------|--------|
| R1 | **No "next best action" guidance.** After seeing the score, users get only "Retry Quiz" and "Back to Home". There's no nudge toward targeted practice (e.g., "You missed 3 Signs questions — drill those next?"). | Retention | P0 | High | Low |
| R2 | **No encouragement messaging.** A simple motivational line based on score (e.g., "Great job!" or "Keep practicing!") creates positive reinforcement. | Retention | P1 | Medium | Low |
| R3 | Topic breakdown shows raw names in results screen (e.g., "PAVEMENT MARKINGS" — partially formatted, missing title case for multi-word). Already somewhat handled by `TopicAccuracyBar.formatTopicName`, which produces title case. | Activation | P2 | Low | — |

### 1.4 Stats Screen
| # | Finding | Funnel Stage | Priority | Impact | Effort |
|---|---------|-------------|----------|--------|--------|
| S1 | **No goal/target indicator.** Users don't know what "good" looks like. Showing a 70% pass threshold line on the accuracy ring would anchor the goal. | Retention | P1 | Medium | Low |
| S2 | **No streak/consistency metric.** Streaks (days practiced) are the #1 retention mechanic in quiz apps. Currently there's no tracking. | Retention | P2 | Medium | High |
| S3 | Stats screen is otherwise solid: accuracy ring, per-topic bars, recent attempts. | — | — | — | — |

### 1.5 Import Screen
| # | Finding | Funnel Stage | Priority | Impact | Effort |
|---|---------|-------------|----------|--------|--------|
| I1 | Import is automated on first launch — no issues observed. Transparent to the user. | — | — | — | — |

---

## 2. Funnel Event Tracking Plan

Define these events for local instrumentation (Room or logcat initially; wire to analytics SDK later):

| Event | Trigger | Properties |
|-------|---------|------------|
| `home_viewed` | HomeScreen becomes visible | `question_count_available` |
| `quiz_started` | User taps "Start Quiz" | `mode`, `topic_count`, `question_count`, `difficulty_range` |
| `quiz_completed` | Quiz finishes (all questions answered or time expires) | `mode`, `score_pct`, `correct`, `total`, `duration_ms`, `passed` |
| `stats_opened` | StatsScreen becomes visible | `overall_accuracy`, `attempt_count` |
| `retry_quiz_clicked` | User taps "Retry Quiz" on Results | `previous_score_pct` |

Implementation approach: Create a simple `AnalyticsLogger` interface with a `logEvent(name, params)` method. Start with a `LogcatAnalyticsLogger` implementation that writes to `Log.d("Analytics", ...)`. This allows swapping in Firebase/Amplitude later without changing call sites.

---

## 3. Top-10 Backlog (Prioritized)

| Rank | ID | Hypothesis | Funnel Stage | Impact | Effort | Status |
|------|----|-----------|-------------|--------|--------|--------|
| 1 | H2/Q1 | **Human-readable topic names** improve perceived quality and comprehension, increasing activation rate. | Activation | High | Low | Implementing |
| 2 | H1 | **CTA above the fold** ensures first-time users see "Start Quiz" without scrolling, reducing bounce. | Activation | High | Low | Implementing |
| 3 | R1/R2 | **Encouragement + next action on Results** increases retry rate and return sessions. | Retention | High | Low | Implementing |
| 4 | — | **Funnel event logging** (local) enables measurement of all subsequent experiments. | Measurement | High | Low | Implementing |
| 5 | H3 | **Subtitle/value prop** below title: "Pass your Texas permit test" grounds the user. | Acquisition | Medium | Low | Backlog |
| 6 | H4 | **Quick Start button** — one-tap path to a 20-question practice quiz. | Activation | Medium | Medium | Backlog |
| 7 | S1 | **70% pass line on stats ring** — visual goal anchor. | Retention | Medium | Low | Backlog |
| 8 | S2 | **Daily streak counter** with simple "days practiced" tracking. | Retention | Medium | High | Backlog |
| 9 | H5 | **Collapsible topic section** to reduce home screen scroll depth. | Activation | Low | Medium | Backlog |
| 10 | — | **Share results** button — social proof + organic installs. | Acquisition | Medium | Medium | Backlog |

---

## 4. Quick Wins Being Implemented

### QW-1: Human-readable topic names (H2, Q1)
- Extract `formatTopicName()` from `TopicAccuracyBar.kt` to a shared utility.
- Apply to HomeScreen topic list and QuizScreen topic badge.
- Before: `PAVEMENT_MARKINGS` / After: `Pavement Markings`

### QW-2: Move CTA above the fold (H1)
- Restructure HomeScreen: place "Start Quiz" button in a sticky bottom bar outside the scroll container.
- Topics, questions, difficulty remain scrollable.

### QW-3: Results screen encouragement + next action (R1, R2)
- Add motivational message based on score bracket.
- Add "Practice Weak Topics" button when missed questions exist.
