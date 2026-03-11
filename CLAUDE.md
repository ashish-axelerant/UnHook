# CLAUDE.md — UnHook Android App

> This file is read by Claude Code at the start of every session.
> Always read this before doing anything. Never delete or modify this file
> unless explicitly asked by the developer.

---

## Project Identity
- **App Name:** UnHook
- **Tagline:** Get unhooked. Together.
- **Purpose:** Couples accountability app that creates friction when either
  partner opens addictive social media apps. Gamified points system makes
  resisting fun.
- **Developer:** Ashish Singh (ashish.singh@axelerant.com)
- **Partner in app:** Arti (wife)

---

## Tech Stack (Do Not Change Without Asking)
- **Language:** Kotlin (no Java)
- **UI:** Jetpack Compose + Material 3 (no XML layouts)
- **Architecture:** MVVM — ViewModel + StateFlow + Repository pattern
- **Database:** Room (local, on-device only for now)
- **Background:** AccessibilityService + ForegroundService + WorkManager
- **Navigation:** Jetpack Navigation Compose
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35
- **Build:** Gradle with Kotlin DSL (.kts files)

---

## Project Structure (Always Follow This)
```
app/src/main/java/com/unhook/app/
├── MainActivity.kt
├── navigation/
│   └── NavGraph.kt
├── ui/
│   ├── screens/
│   │   ├── DashboardScreen.kt
│   │   ├── DuelScreen.kt
│   │   ├── SettingsScreen.kt
│   │   ├── OnboardingScreen.kt
│   │   ├── BlockedAppsScreen.kt
│   │   ├── ChoreWishScreen.kt
│   │   └── ReportScreen.kt
│   ├── overlay/
│   │   ├── InterventionOverlay.kt
│   │   └── InterventionViewModel.kt
│   └── components/
│       └── UsageChart.kt
├── service/
│   ├── UnHookAccessibilityService.kt
│   └── MonitoringForegroundService.kt
├── workers/
│   ├── WeeklyResetWorker.kt
│   └── WeeklyReportWorker.kt
├── data/
│   ├── db/
│   │   └── AppDatabase.kt
│   ├── model/
│   │   ├── User.kt
│   │   ├── Partner.kt
│   │   ├── BlockedApp.kt
│   │   ├── PointEvent.kt
│   │   ├── ChoreItem.kt
│   │   ├── WishItem.kt
│   │   └── ReminderMessage.kt
│   └── repository/
│       ├── UserRepository.kt
│       ├── PointsRepository.kt
│       └── MessageRepository.kt
└── notification/
    └── NotificationHelper.kt
```

---

## Coding Conventions (Always Follow)
- All ViewModels use `StateFlow`, never `LiveData`
- All DB operations run on `Dispatchers.IO`
- Use `sealed class` for UI state (Loading / Success / Error)
- Composables are stateless where possible — state hoisted to ViewModel
- No hardcoded strings — use `strings.xml`
- No hardcoded colors — use Material 3 theme tokens
- Every file gets a `// UnHook —` comment at the top describing its purpose
- Prefer extension functions over utility classes
- Use `data class` for all models

---

## App Features Summary

### Gamification — Mix Mode (Chore Wars + Wish List)
- Both partners start each week at **200 points**
- Points are EARNED by resisting, LOST by scrolling
- Weekly winner (higher points on Monday 12:00 AM) wins
- Loser must: do 1 chore from winner's list OR grant 1 wish from winner's list
- Winner chooses which (chore or wish) after seeing the weekly report

### Points Rules
| Action | Points |
|--------|--------|
| Resist intervention screen | +10 |
| Full day under time limit | +25 |
| 3-day clean streak | +50 bonus |
| Scrolled past intervention | -15 |
| Exceeded daily limit | -20 |
| Manually disabled UnHook | -50 |

### Intervention Screen Behavior
- Triggers within 2 seconds of opening a blocked app
- Shows partner's current score vs your score
- 5-second countdown before "Let me in" activates
- Breathing animation during countdown
- Rotating reminder messages (personal + built-in)
- "Resist (+10 pts)" → closes overlay, awards points, logs event
- "Let me in" → allows app, deducts points, logs event

### Default Blocked Apps
- Twitter/X (com.twitter.android)
- Facebook (com.facebook.katana)
- Instagram (com.instagram.android)
- YouTube (com.google.android.youtube)
- TikTok (com.zhiliaoapp.musically)
- Snapchat (com.snapchat.android)

---

## Current Phase
**ALL PHASES COMPLETE** — App is feature-complete for local-only mode.

---

## What's Already Done
- **Phase 1:** Project scaffold, Gradle KTS, Compose + Material 3 theme, bottom nav, Room DB (User/Partner/PointEvent), onboarding flow, dashboard screen
- **Phase 2:** AccessibilityService detection, ForegroundService, BlockedApps screen, Settings with permission management, BootReceiver
- **Phase 3:** Intervention overlay with breathing animation, 5s countdown, resist/let-me-in buttons, 20 built-in reminder messages, points logging
- **Phase 4:** Points engine (+10 resist, -15 scroll), ChoreItem/WishItem entities, ChoreWishScreen with tabs, DuelScreen with battle card, WeeklyResetWorker
- **Phase 5:** WeeklyReportWorker, NotificationHelper, ReportScreen with 7-day usage chart, notification channels
- **Phase 6:** Streak tracking (+50 bonus every 3 resists), haptic feedback on buttons, share card in Settings

---

## Known Issues / Watch Out For
- AccessibilityService needs to be explicitly enabled in Android Settings
  by the user — we cannot enable it programmatically
- SYSTEM_ALERT_WINDOW permission needs special handling on Android 10+
- Xiaomi/OnePlus/Oppo phones aggressively kill background services —
  always prompt user to whitelist UnHook in battery settings
- PACKAGE_USAGE_STATS is a "special" permission — must send user to
  Settings > Apps > Special app access > Usage access

---

## Git Workflow
- Branch naming: `feature/phase-X-description`
  Examples: `feature/phase-1-navigation`, `feature/phase-2-detection`
- Commit after every working feature (not every file)
- PR title format: `[Phase X] Description of what was built`
- Always run the app before committing — no broken commits

---

## Session Start Checklist (Run These Every Time)
1. Read this CLAUDE.md fully
2. Check "Current Phase" and "What's Already Done" sections
3. Ask developer: "What are we building today?"
4. After completing work: update "What's Already Done"
5. Create PR at end of session if meaningful features were added

---

## How to Create a PR (End of Session)
```bash
# 1. Check current branch
git branch

# 2. Stage and commit
git add .
git commit -m "[Phase X] What was built"

# 3. Push
git push origin feature/phase-X-description

# 4. Create PR via GitHub CLI
gh pr create \
  --title "[Phase X] Description" \
  --body "## What was built\n- Feature 1\n- Feature 2\n\n## How to test\n- Step 1\n- Step 2" \
  --base main
```

---

## Do Not
- Do NOT use XML layouts (Compose only)
- Do NOT use LiveData (StateFlow only)
- Do NOT hardcode colors or strings
- Do NOT create God classes — keep files focused and small
- Do NOT use Java files
- Do NOT modify CLAUDE.md without asking Ashish first
