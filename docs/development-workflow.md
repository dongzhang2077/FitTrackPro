# Development Workflow

This project uses an automated local + CI verification loop so feature branches stay stable before merge.

## Branch Strategy

1. Start from `main`.
2. Create a feature branch: `feature/<topic>`.
3. Make small commits with tests passing.
4. Open a pull request.
5. Merge only when CI is green.

## One-Time Local Setup

Install the pre-push hook so verification runs automatically before every push.

- Git Bash / WSL:

```bash
./scripts/install-git-hooks.sh
```

- PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\install-git-hooks.ps1
```

## Daily Commands

- Run full local verification:

```bash
./scripts/ci-check.sh
```

- Direct Gradle task (same checks):

```bash
./gradlew ciCheck
```

- Windows fallback:

```powershell
cmd.exe /C "gradlew.bat ciCheck --no-daemon --stacktrace"
```

`ciCheck` runs:

- `:app:assembleDebug`
- `:app:testDebugUnitTest`

## Post-Check Confirmation

After `ciCheck` is green, update the proposal alignment checklist before moving to the next module:

- Checklist: `docs/proposal-core-checklist.md`
- Rule: only treat a feature as complete when test/build checks are green and checklist evidence paths are recorded.
- If an item is partial (`[~]`), finish that gap before starting unrelated module work.

## CI Behavior

GitHub Actions workflow: `.github/workflows/android-ci.yml`

- Triggered on:
  - Pull requests
  - Push to `main`
  - Manual dispatch
- Uses JDK 21
- Runs `./scripts/ci-check.sh`

## Temporary Bypass (Use Sparingly)

- Skip pre-push checks for one command:

```bash
SKIP_PRE_PUSH_CHECKS=1 git push
```

- Skip `ci-check.sh` internals (rare troubleshooting):

```bash
SKIP_CI_CHECK=1 ./scripts/ci-check.sh
```

Use bypass only for emergency hotfixes; follow up immediately with full checks.
