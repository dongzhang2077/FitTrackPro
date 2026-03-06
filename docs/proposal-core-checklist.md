# Proposal Core Feature Checklist

Use this checklist after local verification passes (for example `./scripts/ci-check.sh`) to confirm proposal-aligned progress and avoid scope drift.

## How to Use

1. Run verification (`./scripts/ci-check.sh` or `./gradlew ciCheck`).
2. If checks are green, update this checklist.
3. Mark each item as:
   - `[x]` complete
   - `[~]` partial
   - `[ ]` missing
4. Add evidence path(s) for every status update.

## Core Features From Final Proposal

### 1) AI-Powered Workout Recommendations

- [x] Claude/OpenAI-compatible API integration exists.
  - Evidence: `app/src/main/java/com/domcheung/fittrackpro/data/recommendation/OpenAiWorkoutRecommendationService.kt`
  - Evidence: `app/build.gradle.kts`
- [x] Recommendation reason + source are shown in UI.
  - Evidence: `app/src/main/java/com/domcheung/fittrackpro/presentation/home/HomeScreen.kt`

### 2) Smart Workout Reminders

- [x] Customizable scheduling (days/times) exists.
  - Evidence: `app/src/main/java/com/domcheung/fittrackpro/presentation/profile/ProfileScreen.kt`
  - Evidence: `app/src/main/java/com/domcheung/fittrackpro/data/reminder/WorkoutReminderScheduler.kt`
- [x] Snooze functionality.
  - Evidence: `app/src/main/java/com/domcheung/fittrackpro/data/reminder/WorkoutReminderWorker.kt`
  - Evidence: `app/src/main/java/com/domcheung/fittrackpro/data/reminder/WorkoutReminderActionReceiver.kt`
  - Evidence: `app/src/main/java/com/domcheung/fittrackpro/data/reminder/WorkoutReminderScheduler.kt`
- [x] Direct deep link to workout tab from notification exists.
  - Evidence: `app/src/main/java/com/domcheung/fittrackpro/data/reminder/WorkoutReminderWorker.kt`
  - Evidence: `app/src/main/java/com/domcheung/fittrackpro/MainActivity.kt`
  - Evidence: `app/src/main/java/com/domcheung/fittrackpro/navigation/AppNavigation.kt`

### 3) Dark Mode and Theme Customization

- [~] Material3 dark theme definitions exist.
  - Evidence: `app/src/main/java/com/domcheung/fittrackpro/ui/theme/Theme.kt`
- [ ] Light/Dark/System user setting with DataStore persistence and UI entry point.
  - Current status: not wired end-to-end yet.

## Current Blocking Gaps (Mandatory Scope)

1. Theme customization: implement user-selectable Light/Dark/System + persistence.

## Reminder Module Completion Checklist (Detailed)

- [x] Enable/disable reminder schedule.
- [x] Configure reminder time.
- [x] Configure active reminder days.
- [x] Notification opens workout tab directly.
- [x] Test reminder trigger for fast local verification.
- [x] Snooze action from notification.

## Update Log

- 2026-03-05: Checklist created and baseline status recorded.
- 2026-03-05: Smart reminder snooze flow implemented and checklist updated.
