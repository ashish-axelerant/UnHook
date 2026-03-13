# UnHook — Android App Project Plan

## What is UnHook?
A couples digital wellness app that creates friction when either partner opens
addictive social media apps. Uses a gamified points system (Mix mode: Chore Wars
+ Wish List) to make self-control fun and relationship-strengthening.

## The Core Loop
1. Partner opens Twitter/Instagram/Facebook/etc.
2. UnHook intercepts → shows intervention screen with partner's live score
3. Partner resists → earns points. Partner scrolls anyway → loses points.
4. Weekly winner gets chores done OR a wish granted by the loser.

## Tech Stack
- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Architecture: MVVM (ViewModel + StateFlow)
- Local DB: Room
- Background: AccessibilityService + ForegroundService + WorkManager
- Notifications: NotificationManager
- Key Android APIs: UsageStatsManager, AccessibilityService
- Min SDK: Android 8.0 (API 26)

## Permissions Required
- PACKAGE_USAGE_STATS (track time in apps)
- BIND_ACCESSIBILITY_SERVICE (detect when blocked app opens)
- FOREGROUND_SERVICE (keep monitoring alive)
- POST_NOTIFICATIONS (send reminders)
- SYSTEM_ALERT_WINDOW (show overlay intervention screen)

## Reward / Penalty Mode
**Mix Mode: Chore Wars + Wish List**
- Each partner maintains a Chore List and a Wish List
- Weekly loser must complete 1 random chore from winner's chore list
- OR grant 1 wish from winner's wish list (winner picks which)
- Points reset every Monday 12:00 AM

## Couple Pairing
- User A creates a "room" → gets a 6-digit pairing code
- User B enters the code → both phones are linked via shared Room DB sync
- Each phone runs UnHook independently but shares score data

---

## PHASE 1 — Foundation & Couple Setup
**Goal:** App installs, two users can pair their phones, basic navigation works.
**Duration:** ~3 Claude Code sessions

### Deliverables
- [x] Android project created (Kotlin + Compose + Material 3)
- [x] Git repo initialized and pushed to GitHub
- [x] Bottom navigation: Dashboard / Duel / Settings
- [x] Onboarding screens: Name entry, avatar selection (emoji), pairing code
- [x] Room DB: User table, Partner table
- [x] Couple pairing flow: Generate code / Enter code
- [x] Basic dashboard shell (empty state is fine)

### Key Files Created
- MainActivity.kt
- ui/screens/OnboardingScreen.kt
- ui/screens/DashboardScreen.kt
- ui/screens/DuelScreen.kt
- ui/screens/SettingsScreen.kt
- data/db/AppDatabase.kt
- data/model/User.kt
- data/model/Partner.kt
- navigation/NavGraph.kt

---

## PHASE 2 — Detection Engine
**Goal:** App detects when a blocked social media app is opened.
**Duration:** ~2 Claude Code sessions

### Deliverables
- [x] AccessibilityService implemented and registered
- [x] ForegroundService keeping detection alive in background
- [x] Configurable blocked apps list (Twitter, Facebook, Instagram, YouTube, TikTok)
- [x] Settings screen: add/remove blocked apps
- [x] Permission request flow: Usage Stats + Accessibility + Notifications
- [x] Detection tested and working on real device

### Key Files Created
- service/UnHookAccessibilityService.kt
- service/MonitoringForegroundService.kt
- ui/screens/BlockedAppsScreen.kt
- data/model/BlockedApp.kt

---

## PHASE 3 — Intervention Screen
**Goal:** When blocked app opens, a friction screen appears with partner's score.
**Duration:** ~2 Claude Code sessions

### Deliverables
- [x] Overlay intervention screen (SYSTEM_ALERT_WINDOW)
- [x] Shows: app name, current session time, YOUR points vs PARTNER points
- [x] 5-second countdown before "Let me in" button becomes active
- [x] Breathing animation during countdown
- [x] Custom reminder message shown (rotates from message bank)
- [x] "Resist (+10 pts)" button → closes overlay, awards points
- [x] "Let me in" button → allows app, deducts points, logs event
- [x] Message bank: 20 built-in messages + user can add custom ones

### Key Files Created
- ui/overlay/InterventionOverlay.kt
- ui/overlay/InterventionViewModel.kt
- data/model/ReminderMessage.kt
- data/repository/MessageRepository.kt

### Sample Reminder Messages
- "Is the scroll worth the chore? 🧹"
- "Arti is watching your score right now 👀"
- "Put the phone down. Go find your person. 💑"
- "Future you is proud when you resist. 💪"
- "Your streak is on the line!"

---

## PHASE 4 — Points Engine & Chore/Wish System
**Goal:** Full gamification system working between both partners.
**Duration:** ~3 Claude Code sessions

### Deliverables
- [x] Points calculation engine (earn/lose based on actions)
- [x] Points stored in Room DB with timestamps
- [x] Weekly score calculation (resets Monday)
- [x] Chore list: each partner adds up to 10 chores
- [x] Wish list: each partner adds up to 10 wishes
- [x] Weekly winner determination + notification to both phones
- [x] Winner picks: random chore OR specific wish from loser's lists
- [x] "Pending obligations" screen showing open chores/wishes

### Points Rules
- Resist intervention: +10 pts
- Full day under limit: +25 pts
- 3-day clean streak: +50 pts bonus
- Scrolled past intervention: -15 pts
- Exceeded daily time limit: -20 pts
- Disabled app manually: -50 pts
- Starting balance each week: 200 pts (loss aversion design)

### Key Files Created
- data/model/PointEvent.kt
- data/repository/PointsRepository.kt
- data/model/ChoreItem.kt
- data/model/WishItem.kt
- ui/screens/DuelScreen.kt (updated with full points UI)
- ui/screens/ChoreWishScreen.kt
- service/WeeklyResetWorker.kt (WorkManager)

---

## PHASE 5 — Weekly Report & Notifications
**Goal:** Both partners get a rich weekly summary. Push reminders during day.
**Duration:** ~2 Claude Code sessions

### Deliverables
- [x] Weekly report notification (Sunday 8 PM) sent to both phones
- [x] Report shows: winner, point gap, apps resisted, streaks, obligation assigned
- [x] In-app weekly report screen with charts (usage over time)
- [x] Daily motivation notification (configurable time)
- [ ] "Your partner just resisted Instagram!" real-time notification
- [x] Usage trend graph (7-day bar chart using Canvas or MPAndroidChart)

### Key Files Created
- workers/WeeklyReportWorker.kt
- ui/screens/ReportScreen.kt
- ui/components/UsageChart.kt
- notification/NotificationHelper.kt

---

## PHASE 6 — Polish & Extras
**Goal:** App feels finished, delightful, and shareable.
**Duration:** ~2 Claude Code sessions

### Deliverables
- [ ] App icon designed (hook with a cross through it)
- [ ] Splash screen
- [x] Streak badges (3-day streak bonus +50 pts)
- [ ] Achievement system: "Iron Will" (resisted 50 times), "Team Player" etc.
- [x] Dark mode support
- [x] Haptic feedback on intervention screen
- [x] Onboarding tutorial — app selection + permissions steps added to onboarding flow
- [x] Share card: "I resisted social media X times this week!" (image export)

---

## PHASE 7 — Real Device Testing & UX Hardening
**Goal:** Test on a real device, fix bugs found during testing, make the intervention experience genuinely uncomfortable.

### Deliverables
- [x] Wireless ADB setup (no USB cable needed) — `adb pair` + `adb connect` over WiFi
- [x] App installed and tested on real phone (I2403) and emulator (Pixel 9a AVD)
- [x] Fixed: "Let me in" was re-triggering the overlay immediately after dismissal
  - Added 10-minute grace period via `UnHookAccessibilityService.tempAllowedPackages`
  - Grace period is configurable: 1, 5, 10, 15, 30, 60 minutes
- [x] Fixed: blocked apps list only showed hardcoded defaults
  - Added "+" FAB to BlockedAppsScreen that loads all installed non-system apps
  - Full search/filter support
  - Fixed Android 11+ `QUERY_ALL_PACKAGES` permission
- [x] Configurable intervention countdown (3, 5, 10, 15, 30 seconds) stored in SharedPreferences
- [x] Configurable reminder frequency during grace period (30s, 1min, 2min, 5min, 10min)
- [x] Fixed: "Resist" button was returning user to the blocked app
  - On resist → navigates to Home screen instead of back-stacking into blocked app
- [x] Periodic reminder notifications during grace period
  - Heads-up notification: `⛔ Put the phone down.` with BigTextStyle
  - `setOngoing(true)` — cannot be swiped away
  - Direct vibration via `Vibrator` service (guaranteed regardless of DND/volume)
  - `setFullScreenIntent` → re-launches the intervention screen mid-scroll
  - Notification auto-cancelled when grace period ends
- [x] Grace period auto-closes the blocked app on expiry
  - Navigates to Home screen when grace period timer runs out
  - Removes persistent notification on expiry
- [x] Added `USE_FULL_SCREEN_INTENT` and `VIBRATE` permissions to manifest

### Key Files Modified
- service/UnHookAccessibilityService.kt (grace period, reminder notifications, vibration)
- ui/overlay/InterventionActivity.kt (navigate home on resist)
- ui/overlay/InterventionViewModel.kt (grace period config, navigateHome flag)
- ui/screens/BlockedAppsScreen.kt (installed app picker with search)
- ui/screens/SettingsScreen.kt (countdown, grace period, reminder frequency settings)
- AndroidManifest.xml (QUERY_ALL_PACKAGES, USE_FULL_SCREEN_INTENT, VIBRATE)

---

## PHASE 8 — Onboarding UX Overhaul
**Goal:** Remove silent setup surprises; make first-run feel intentional and polished.

### Deliverables
- [x] Removed silent blocked-app seeding on first launch (`UnHookApplication`)
- [x] New onboarding step 3: "What should we block?" — user explicitly picks apps
  - Scrollable list of all installed non-system apps
  - Popular social apps pre-selected if installed (Twitter/X, Facebook, Instagram, YouTube, TikTok, Snapchat, Reddit, Pinterest, LinkedIn)
  - Search bar with leading Search icon and clear (×) button
  - Selected apps pinned to top under sticky "Selected · N" header
  - Unselected apps under sticky "More Apps" header
  - Row background animates to `primaryContainer` tint on selection
  - Continue button disabled until ≥1 app chosen; shows count ("Continue (3 selected)")
  - Haptic feedback on every toggle
- [x] New onboarding step 4: "Almost there!" — in-flow permission grants
  - 4 permission cards: Accessibility (Required), Overlay (Required), Usage Stats (Recommended), Notifications (Recommended)
  - Each card has a soft icon circle, title, Required/Recommended badge, and "Grant" button
  - Card background animates to `primaryContainer` tint when permission is granted
  - Live re-check via `repeatOnLifecycle(RESUMED)` — checkmark appears the moment user returns from Settings
  - Mini progress dots (● ● ○ ○  2 of 4 granted) in tertiary green
  - Warning banner (`errorContainer`) visible while Accessibility or Overlay is missing
  - Button reads "All set — let's go!" when required perms granted, "Skip for now" otherwise
- [x] Extracted `AppIcon` composable into `ui/components/AppIcon.kt` (shared by `BlockedAppsScreen` + onboarding)
- [x] Onboarding now 6 steps: Welcome → Profile → Pairing → Apps → Permissions → Confirmation

### Key Files Created / Modified
- `ui/components/AppIcon.kt` (new — shared launcher icon composable)
- `ui/screens/OnboardingScreen.kt` (steps 3 & 4 added; 4→5 dots→6)
- `UnHookApplication.kt` (removed `defaultBlockedApps` seeding)
- `MainActivity.kt` (`onComplete` now accepts `selectedApps: Map<String, String>`)
- `res/values/strings.xml` (11 new strings for both new steps)

---

## Success Metrics (How You Know It's Working)
- Both phones successfully paired
- Intervention triggers within 2 seconds of opening a blocked app
- Points sync between phones within 30 seconds
- Weekly report delivered every Sunday without fail
- You and Arti argue about who has to do dishes 😄

---

## Known Challenges & How Claude Code Will Handle Them
| Challenge | Approach |
|-----------|----------|
| AccessibilityService battery drain | Use event filtering, only listen for window state changes |
| Overlay permission varies by Android version | Handle API 23+ permission flow carefully |
| Keeping service alive on aggressive OEMs (Xiaomi, OnePlus) | ForegroundService + battery optimization whitelist prompt |
| Syncing scores between two phones | Start with local-only, Phase 2 can add Firebase sync |
| Play Store policies on Accessibility API | Document legitimate use case clearly in store listing |
