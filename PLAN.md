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
- [ ] Android project created (Kotlin + Compose + Material 3)
- [ ] Git repo initialized and pushed to GitHub
- [ ] Bottom navigation: Dashboard / Duel / Settings
- [ ] Onboarding screens: Name entry, avatar selection (emoji), pairing code
- [ ] Room DB: User table, Partner table
- [ ] Couple pairing flow: Generate code / Enter code
- [ ] Basic dashboard shell (empty state is fine)

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
- [ ] AccessibilityService implemented and registered
- [ ] ForegroundService keeping detection alive in background
- [ ] Configurable blocked apps list (Twitter, Facebook, Instagram, YouTube, TikTok)
- [ ] Settings screen: add/remove blocked apps
- [ ] Permission request flow: Usage Stats + Accessibility + Notifications
- [ ] Detection tested and working on real device

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
- [ ] Overlay intervention screen (SYSTEM_ALERT_WINDOW)
- [ ] Shows: app name, current session time, YOUR points vs PARTNER points
- [ ] 5-second countdown before "Let me in" button becomes active
- [ ] Breathing animation during countdown
- [ ] Custom reminder message shown (rotates from message bank)
- [ ] "Resist (+10 pts)" button → closes overlay, awards points
- [ ] "Let me in" button → allows app, deducts points, logs event
- [ ] Message bank: 20 built-in messages + user can add custom ones

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
- [ ] Points calculation engine (earn/lose based on actions)
- [ ] Points stored in Room DB with timestamps
- [ ] Weekly score calculation (resets Monday)
- [ ] Chore list: each partner adds up to 10 chores
- [ ] Wish list: each partner adds up to 10 wishes
- [ ] Weekly winner determination + notification to both phones
- [ ] Winner picks: random chore OR specific wish from loser's lists
- [ ] "Pending obligations" screen showing open chores/wishes

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
- [ ] Weekly report notification (Sunday 8 PM) sent to both phones
- [ ] Report shows: winner, point gap, apps resisted, streaks, obligation assigned
- [ ] In-app weekly report screen with charts (usage over time)
- [ ] Daily motivation notification (configurable time)
- [ ] "Your partner just resisted Instagram!" real-time notification
- [ ] Usage trend graph (7-day bar chart using Canvas or MPAndroidChart)

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
- [ ] Streak badges (3-day, 7-day, 30-day)
- [ ] Achievement system: "Iron Will" (resisted 50 times), "Team Player" etc.
- [ ] Dark mode support
- [ ] Haptic feedback on intervention screen
- [ ] Onboarding tutorial (first-time use)
- [ ] Share card: "I resisted social media X times this week!" (image export)

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
