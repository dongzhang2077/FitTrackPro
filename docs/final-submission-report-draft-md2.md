# Final Project Report: FitTrack Pro (Mobile Dev 2)

## Student Information
- Name: `<YOUR_NAME>`
- Student ID: `<YOUR_STUDENT_ID>`
- Project Title: `FitTrack Pro: AI-Enhanced Fitness Tracking`
- Date: `<YYYY-MM-DD>`

---

## 1) Technical and Testing Information

### 1.1 Development Environment
- Android Studio Version: `<FILL_IN>`
- Build Tools / Gradle: `<FILL_IN_IF_NEEDED>`
- Kotlin Version: `<FILL_IN_IF_NEEDED>`

### 1.2 Test Devices
- Emulator / Device 1: `<name, model>`
- API Level: `<API_XX>`
- Emulator / Device 2 (optional): `<name, model>`
- API Level: `<API_XX>`

### 1.3 Technologies and APIs Used
- Language: Kotlin
- UI: Jetpack Compose (Material 3)
- Architecture: Clean Architecture (UI / Domain / Data)
- Dependency Injection: Hilt
- Async: Kotlin Coroutines + Flow
- Navigation: Jetpack Navigation (Compose)
- Local Storage: Room + DataStore
- Backend / Cloud: Firebase Authentication, Firestore (sync support)
- Networking: Retrofit + OkHttp
- External API / AI:
  - OpenAI-compatible chat endpoint for recommendation ranking
  - Fallback deterministic recommendation when AI is unavailable

### 1.4 Test Account Credentials (if login is required)
- Email: `<TEST_EMAIL>`
- Password: `<TEST_PASSWORD>`

> If your app login is required for testing, ensure these credentials are valid before submission.

---

## 2) Build and Run Status

The submitted Android Studio project is runnable and testable.

- Local verification command: `./scripts/ci-check.sh`
- Result: `BUILD SUCCESSFUL` (latest local verification before packaging)

Any additional run notes for evaluator:
- `<Add any startup steps if needed>`
- `<Mention if internet is required for specific AI path tests>`

---

## 3) Work Completed Since Second Submission

This submission focuses on proposal-core feature completion and product polish.

### 3.1 AI-Powered Workout Recommendation (Completed)
- Implemented OpenAI-compatible recommendation integration.
- Added robust fallback recommendation logic when AI is unavailable or response is invalid.
- Recommendation result now includes:
  - selected plan
  - recommendation source (`AI` / `SMART_FALLBACK` / `NONE`)
  - user-readable reason text

### 3.2 Smart Workout Reminders (Completed)
- Configurable reminder days and time.
- Notification deep-link to Workout tab.
- Added Snooze flow (`Snooze 30m`) with one-time delayed reminder rescheduling.
- Added quick "Send test reminder now" path for easier validation.

### 3.3 Dark Mode and Theme Customization (Completed)
- Added user-selectable theme mode: `System` / `Light` / `Dark`.
- Added dynamic color toggle.
- Persisted theme preferences using DataStore.
- Applied persisted theme values on app startup.
- Improved dark theme readability (text/background contrast and status bar icon behavior).

### 3.4 Recommendation Pool and Template Quality Improvements
- Expanded starter workout template pool to support varied goals/frequency/intensity.
- Added template tags and filtering UX support in Workout screen.
- Ensured baseline templates are seeded/backfilled for users without duplicating existing templates.

---

## 4) Relation to Original Proposal

### Proposal Core Feature Status
- AI-Powered Workout Recommendations: **Completed**
- Smart Workout Reminders (including snooze and deep link): **Completed**
- Dark Mode and Theme Customization: **Completed**
- Exercise Form Analysis (optional in proposal): **Not required for core completion**

Current conclusion: proposal-core mandatory functionality is completed.

---

## 5) Substantial Changes Since Second Submission

If applicable, document substantial changes (especially technology choices) and reasons:

1. `<Change item>`
   - Reason: `<why>`
   - Impact: `<scope/performance/stability>`

2. `<Change item>`
   - Reason: `<why>`
   - Impact: `<scope/performance/stability>`

> Note: App idea remains aligned with the approved proposal.

---

## 6) How to Test This Build (Evaluator-Friendly)

1. Open project in Android Studio (`<version>`).
2. Sync Gradle.
3. Run app on `<emulator/device + API>`.
4. Sign in with provided test account.
5. Validate key flows:
   - AI recommendation card and reason/source label
   - Reminder setup + test reminder + snooze + workout deep-link
   - Theme mode switch and dynamic color persistence after app restart

---

## 7) Screen Captures (Placeholders)

> Replace each placeholder with your final screenshot before exporting to Word/PDF.

- `[INSERT SCREENSHOT 1 HERE]` Login/Register flow
- `[INSERT SCREENSHOT 2 HERE]` Home recommendation card (source + reason)
- `[INSERT SCREENSHOT 3 HERE]` Profile reminder settings (time/days)
- `[INSERT SCREENSHOT 4 HERE]` Reminder notification with `Snooze 30m`
- `[INSERT SCREENSHOT 5 HERE]` Notification deep-link result (Workout tab)
- `[INSERT SCREENSHOT 6 HERE]` Theme settings in Profile (System/Light/Dark + dynamic color)
- `[INSERT SCREENSHOT 7 HERE]` Dark theme readability example
- `[INSERT SCREENSHOT 8 HERE]` Workout templates with filter chips

---

## 8) Remaining Work (if any)

- Optional enhancements only (non-core), for example:
  - deeper recommendation science/ML pipeline
  - optional posture/form analysis module
  - additional UI polish and analytics depth

---

## 9) Final Submission Files Checklist

- [ ] Project zip (`.zip`, not `.rar`)
- [ ] User Guide (Word)
- [ ] User Guide (PDF)
- [ ] Project Report (Word or PDF; PDF recommended)
- [ ] Correct test account credentials included (if login required)
- [ ] Android Studio version + emulator/device/API listed
