# UnHook — Phase 1 Claude Code Prompts
# Copy and paste these one at a time into your Claude Code terminal session.
# Wait for each one to complete before sending the next.

==========================================================================
PROMPT 1 — PROJECT SETUP (Send this FIRST)
==========================================================================

Read the CLAUDE.md file in this project carefully. Then help me set up the
UnHook Android project from scratch.

Create a new Android project with:
- App name: UnHook
- Package name: com.unhook.app
- Language: Kotlin
- UI: Jetpack Compose with Material 3
- Min SDK: 26
- Use Gradle Kotlin DSL (.kts files)

Set up the build.gradle.kts with these dependencies:
- Jetpack Compose BOM (latest stable)
- Material 3
- Navigation Compose
- Room (with KSP processor)
- ViewModel + StateFlow
- WorkManager
- Kotlin Coroutines

Create the base project structure exactly as shown in CLAUDE.md.
Create empty placeholder files for all the files listed in the structure
so I can see the full skeleton.

==========================================================================
PROMPT 2 — NAVIGATION (Send after Prompt 1 is done)
==========================================================================

Now implement the bottom navigation bar with 3 tabs:
- Dashboard (home icon) → DashboardScreen
- Duel (sword/battle icon) → DuelScreen
- Settings (gear icon) → SettingsScreen

Use Jetpack Navigation Compose with a NavGraph.
Each screen should just show a centered Text for now (empty state is fine).
The bottom nav should highlight the active tab correctly.
Follow Material 3 NavigationBar component.

==========================================================================
PROMPT 3 — ROOM DATABASE (Send after Prompt 2 is done)
==========================================================================

Set up Room database for UnHook with these entities:

User:
- id (primary key, auto-generate)
- name (String)
- emojiAvatar (String — stores a single emoji character)
- isMe (Boolean — true for this phone's user)
- weeklyPoints (Int — starts at 200, resets Monday)
- totalResists (Int)
- currentStreak (Int)

Partner:
- id (primary key)
- name (String)
- emojiAvatar (String)
- pairingCode (String)
- weeklyPoints (Int)
- totalResists (Int)
- currentStreak (Int)

PointEvent:
- id (primary key, auto-generate)
- userId (Int)
- points (Int — can be negative)
- reason (String — e.g. "Resisted Instagram", "Scrolled past Twitter")
- appPackageName (String)
- timestamp (Long)

Create DAOs, the AppDatabase singleton, and Repository classes for each.
Use Kotlin coroutines (suspend functions) for all DB operations.

==========================================================================
PROMPT 4 — ONBOARDING FLOW (Send after Prompt 3 is done)
==========================================================================

Build the onboarding flow shown on first app launch.

Screen 1 — Welcome:
- UnHook logo (just a large 🎣 emoji for now)
- Title: "UnHook"
- Subtitle: "Get unhooked. Together."
- "Get Started" button

Screen 2 — Your Profile:
- "What's your name?" text field
- Emoji avatar picker: a row of 8 emojis to choose from
  (😊 🦁 🐯 🦊 🐺 🦅 🏄 🧗)
- "Continue" button

Screen 3 — Pair with Partner:
- Two options:
  A) "I'll create a room" → generates a random 6-digit code, shows it large
     with a "Share code" button and "Waiting for partner..." loading state
  B) "I have a code" → text field to enter 6-digit code, "Connect" button

Screen 4 — All Set!:
- "You're paired with [Partner Name]! 🎉"
- "Let the battle begin 😄"
- "Start UnHooking" button → goes to main Dashboard

Save user data to Room DB. Show onboarding only on first launch
(check if User exists in DB — if yes, skip to Dashboard).

==========================================================================
PROMPT 5 — DASHBOARD SCREEN (Send after Prompt 4 is done)
==========================================================================

Build the Dashboard screen with real data from Room DB.

Layout (top to bottom):
1. Header: "UnHook" title + current date
2. Score card (the main card — make it prominent):
   - Left side: YOUR name + emoji + points (big number)
   - Center: "VS"
   - Right side: PARTNER name + emoji + points (big number)
   - Who's winning shown with a small crown emoji 👑
3. Your stats row:
   - Current streak (🔥 X days)
   - Resists today (✊ X)
   - Time saved today (⏱️ X min)
4. Recent activity list (last 5 PointEvents from DB)
   - Each item: emoji + reason + points (green if positive, red if negative)
5. Bottom: motivational message (rotate daily from a hardcoded list of 7)

Use placeholder/mock data if DB is empty. Make it look clean with
Material 3 cards and proper padding. Support dark mode.

==========================================================================
PROMPT 6 — END OF PHASE 1 PR (Send after Prompt 5 is done)
==========================================================================

Phase 1 is complete! Please:

1. Make sure all files compile without errors
2. Create a git branch: feature/phase-1-foundation
3. Stage all files and create a commit:
   "[Phase 1] Foundation — navigation, Room DB, onboarding, dashboard"
4. Push to GitHub
5. Create a PR with this description:
   ## Phase 1 — Foundation & Couple Setup

   ### What was built
   - Project skeleton with Kotlin + Jetpack Compose + Material 3
   - Bottom navigation (Dashboard, Duel, Settings)
   - Room DB with User, Partner, PointEvent entities
   - Onboarding flow: profile setup + couple pairing code
   - Dashboard screen with VS score card and activity feed

   ### How to test
   - Install on device, go through onboarding
   - Enter name and pick an emoji avatar
   - Test "Create room" to see generated pairing code
   - Verify Dashboard shows VS card with placeholder data

   ### Next phase
   Phase 2 will implement the AccessibilityService to detect
   when blocked apps are opened.

Also update the CLAUDE.md file:
- Move all Phase 1 checkboxes to checked [x]
- Update "What's Already Done" section
- Update "Current Phase" to PHASE 2

==========================================================================
TIPS FOR EACH SESSION
==========================================================================

START every Claude Code session with:
"Read the CLAUDE.md file. We are on [Phase X]. Today I want to work on [task]."

IF something breaks, say:
"Here's the error: [paste error]. Fix it and explain what was wrong."

IF you don't understand something Claude wrote:
"Explain what [specific code] does like I'm learning Kotlin for the first time."

IF you want to understand a concept before building:
"Before we implement AccessibilityService, explain how it works in Android
and what permissions are needed."

END every session with:
"Summarize what we built today and update CLAUDE.md. Then create a PR."
