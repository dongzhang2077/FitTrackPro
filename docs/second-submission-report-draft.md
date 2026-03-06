# FitTrack Pro: AI-Enhanced Fitness Tracking

## Student Information
- Name: <Your Name>
- Student ID: <Your Student ID>

## Build and Run Status
- Compile status: Compiles successfully.
- Run status (emulator): Runnable.
- Verification run used for this submission:
  - `./scripts/ci-check.sh` -> `BUILD SUCCESSFUL`
  - Reminder-related unit tests -> `BUILD SUCCESSFUL`

## If Not Runnable
Not applicable for this submission. The current project build/test checks pass.

## Progress Since Proposal Approval
The following work was completed after proposal approval:

1. Baseline stabilization and workflow hardening
   - Stabilized DataStore-related tests and updated preference handling.
   - Added automated verification workflow (`ciCheck`, local scripts, and CI workflow support).

2. AI-powered recommendation feature (core requirement)
   - Implemented OpenAI-compatible recommendation service and fallback logic.
   - Added recommendation reason/source display in Home screen.
   - Improved recommendation quality with richer template plans and lightweight scoring rules.

3. Smart workout reminder feature (core requirement)
   - Implemented WorkManager-based scheduling with configurable days and time.
   - Implemented notification deep link to Workout tab.
   - Implemented snooze flow (`Snooze 30m`) with one-time delayed reminder re-trigger.

4. UX/testing support improvements
   - Added "Send test reminder now" path for quick reminder testing.
   - Added/updated unit tests for reminder and recommendation-related logic.

## Mapping to Original Proposal

- Proposal Feature 1: AI-Powered Workout Recommendations -> **Done**
  - API integration exists (OpenAI-compatible endpoint), fallback exists, and recommendation source/reason are shown.

- Proposal Feature 2: Smart Workout Reminders -> **Done**
  - Custom schedule (day/time), snooze functionality, and direct workout deep linking are implemented.

- Proposal Feature 3: Dark Mode and Theme Customization -> **Partial**
  - Material3 dark theme definitions exist.
  - Full Light/Dark/System user setting + dynamic color preference persistence is not fully wired yet.

- Proposal Feature 4: Exercise Form Analysis (Optional) -> **Not started (optional)**

## Substantial Changes and Reasons
- No substantial change to approved app direction.
- Implementation detail choices were adjusted for delivery stability:
  - AI recommendation implemented as OpenAI-compatible API integration with robust fallback.
  - Priority was given to completing core features and keeping build/test pipeline stable.

These changes preserve proposal goals while reducing integration risk during iterative submissions.

## Remaining Work to Final Submission
1. Complete Dark Mode and Theme Customization end-to-end:
   - Light/Dark/System selection
   - Dynamic color option
   - DataStore-backed preference persistence and UI wiring

2. Final quality and delivery work:
   - Additional manual QA and edge-case checks
   - UI polish and consistency improvements
   - Final documentation cleanup and screenshot package

3. Optional scope (time permitting):
   - Exercise form analysis module (camera/pose path)

## Difficulties and Design Updates
- Difficulty 1: Early baseline instability in tests (especially preference/DataStore related).
  - Mitigation: Refactored preference management and test setup, then re-verified with automated checks.

- Difficulty 2: Balancing recommendation intelligence and development cost.
  - Mitigation: Adopted a practical V1 strategy (AI + deterministic fallback + richer plan pool) instead of heavy ML infrastructure.

- Design update: Recommendation strategy was explicitly staged (V1 now, deeper science/ML later) to keep current submission reliable and testable.

## Screenshots (to include before final submit)
- Figure 1: Home screen recommendation card showing source and reason.
- Figure 2: Profile reminder settings (day/time customization).
- Figure 3: Reminder notification with `Snooze 30m` action.
- Figure 4: Notification deep link result opening Workout tab.

## Submission Notes
- Included files for Submission 2:
  1. Zipped Android Studio project (this repo state).
  2. This report exported as Word/PDF (with name and student ID filled).
