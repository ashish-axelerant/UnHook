# UnHook — Backend Plan

> This document captures the full backend architecture plan for UnHook.
> Phases are ordered by dependency and build on each other.
> Tasks are written to be converted into GitHub Issues in the future.
> No backend development has started yet — this is planning only.

---

## Vision

Transform UnHook from a local couples-only app into a social accountability
platform where families and friend groups compete together, share progress,
and keep each other honest — without spammy notifications.

**Core principle:** The app is pull-based. Users check status when they
open the app. Backend validates all events — the client is never trusted.

---

## Architecture Overview

```
Android App (Kotlin)
    │
    │  HTTPS (batch sync on app open)
    ▼
Supabase Edge Functions (Deno/TypeScript)
    │  validates, rate-limits, computes scores
    ▼
Supabase PostgreSQL
    ├── Auth (email, Google, Apple)
    ├── Row Level Security (multi-tenant isolation)
    └── Storage (share card images)
```

**Stack:** Supabase (Postgres + Auth + Edge Functions + Storage)

**Why Supabase over Firebase:**
- SQL is better for leaderboards and complex ranking queries
- Row Level Security handles group data isolation elegantly
- Predictable pricing (~$25/month Pro vs Firebase pay-per-read surprises)
- Free tier handles up to 50k MAU

---

## Database Schema

```sql
-- Core identity
users
  id uuid PK (= Supabase Auth UID)
  email text
  display_name text
  avatar_url text
  device_fingerprint text        -- anti-cheat: bind account to device
  created_at timestamptz

-- Social groups (family / friend circles)
groups
  id uuid PK
  name text
  invite_code text UNIQUE        -- 6-char alphanumeric, e.g. "XK92PL"
  created_by uuid → users.id
  settings jsonb                 -- { weekly_reset_day, challenge_active, etc. }
  created_at timestamptz

group_members
  id uuid PK
  group_id uuid → groups.id
  user_id uuid → users.id
  role text CHECK IN ('admin', 'member')
  joined_at timestamptz
  is_active bool DEFAULT true

-- Immutable event log — server validates every event
point_events
  id uuid PK
  user_id uuid → users.id
  event_type text CHECK IN (
    'resist', 'slip', 'daily_clean', 'streak_bonus', 'service_disabled',
    'service_gap', 'challenge_complete'
  )
  points_delta int
  context_data jsonb             -- { package_name, streak_day, etc. }
  occurred_at timestamptz        -- from device clock
  synced_at timestamptz          -- when server received it
  device_hash text               -- must match users.device_fingerprint
  client_checksum text           -- HMAC(user_secret, user_id+event_type+occurred_at)
  server_validated bool DEFAULT false
  rejected_reason text           -- null if valid

-- Weekly computed scores (materialized every Monday 00:00)
weekly_scores
  id uuid PK
  user_id uuid → users.id
  group_id uuid → groups.id
  week_start date                -- always Monday
  total_points int
  resist_count int
  slip_count int
  streak_days int
  rank int                       -- within the group for that week
  computed_at timestamptz

-- Anti-cheat: accessibility service health
service_heartbeats
  id uuid PK
  user_id uuid → users.id
  device_hash text
  last_seen_at timestamptz
  service_running bool

-- Group challenges (optional weekly goals)
challenges
  id uuid PK
  group_id uuid → groups.id
  title text
  description text
  target_type text               -- 'no_app', 'under_minutes', 'streak_days'
  target_value jsonb             -- { package_name, minutes, days }
  week_start date
  bonus_points int
  created_by uuid → users.id

challenge_completions
  id uuid PK
  challenge_id uuid → challenges.id
  user_id uuid → users.id
  completed_at timestamptz

-- Achievements (persistent badges)
achievements
  id uuid PK
  key text UNIQUE                -- 'streak_7', 'first_500_pts', 'most_improved'
  title text
  description text
  icon_name text

user_achievements
  id uuid PK
  user_id uuid → users.id
  achievement_key text → achievements.key
  earned_at timestamptz
  group_id uuid                  -- context where it was earned (nullable)
```

---

## API Surface (Edge Functions)

```
Auth
  POST /auth/register            -- create user + device fingerprint
  POST /auth/login
  POST /auth/device-verify       -- re-verify device on new install

Event Sync (core anti-cheat path)
  POST /events/sync              -- batch upload events, server validates each

Groups
  POST /groups/create
  POST /groups/join              -- body: { invite_code }
  GET  /groups/:id/feed          -- activity feed + rankings (pull on app open)
  GET  /groups/:id/leaderboard   -- ?week=2026-03-10 or ?range=alltime
  GET  /groups/:id/members

Scores & Stats
  GET  /users/:id/stats          -- ?range=weekly|monthly|alltime
  GET  /users/:id/achievements

Social
  POST /share-cards/generate     -- returns signed Storage URL to PNG
  GET  /share-cards/:id          -- public, no auth (for social media preview)

Challenges
  POST /groups/:id/challenges    -- admin creates a challenge
  POST /challenges/:id/complete  -- mark self as completed
```

---

## Anti-Cheat Design

| Attack Vector | Mitigation |
|--------------|-----------|
| Fake resist events from modified APK | HMAC checksum signed with per-user server secret; server recomputes and rejects mismatches |
| Backdating events | Reject `occurred_at` older than 48h from `synced_at` |
| Flooding fake resists | Rate limit: max 20 point events per user per hour |
| Killing service to avoid slips | `service_heartbeats` — gaps >6h during waking hours trigger automatic `service_gap` penalty event |
| Second device / fresh install | `device_fingerprint` mismatch requires re-auth + 24h cooldown before events are trusted |
| Forging usage stats | Android `UsageStatsManager` dump sent with sync; server cross-checks claimed resists vs actual app usage time |
| Score injection via MITM | All writes go through Edge Functions; client never writes directly to DB (Row Level Security blocks it) |
| Sync offline events after cheating | Offline event window: max 48h; events older than that are silently dropped |
| Account sharing | Device fingerprint is unique per install; two active devices = flag |

---

## Sync Strategy (Battery-Friendly)

```
App open
  → upload pending events (batch, max 100)
  → fetch activity feed + rankings
  → update local Room DB

Background (every 6h via WorkManager)
  → heartbeat only: POST { service_running, device_hash }  (~50 bytes)

Monday 12:00 AM (WorkManager exact alarm)
  → trigger weekly reset locally
  → immediate full sync
  → fetch new weekly standings
```

No WebSockets or push notifications for every event. Users see fresh data
on each app open. Optional: silent push notification (data-only, no UI) to
wake the app for sync after a group member posts a big event.

---

## Cost Estimate

| MAU | Supabase Tier | Est. Monthly Cost |
|-----|-------------|------------------|
| < 50k | Free | $0 |
| 50k–200k | Pro | $25 |
| 200k+ | Team | ~$150–300 |

---

## Migration from Local-Only App

Current Room DB entities map directly to Supabase:

| Local (Room) | Backend (Supabase) |
|-------------|-------------------|
| `User` / `Partner` | `users` + `group_members` |
| `PointEvent` | `point_events` (server-validated copy) |
| `BlockedApp` | stays local + synced as user config |
| `ChoreItem` / `WishItem` | stays local; optional group sync later |
| `WeeklyResetWorker` | triggers sync; server owns canonical score |

---

---

# Phases

---

## Phase B1 — Foundation (Auth + DB + Basic Sync)

**Goal:** Users can create accounts, form a group, and have their local
events synced to the server. Server validates and stores events.

### Tasks

- [ ] **B1-1** Set up Supabase project (free tier), enable Auth providers (email + Google)
- [ ] **B1-2** Create all database tables and indexes per schema above
- [ ] **B1-3** Configure Row Level Security policies for all tables
- [ ] **B1-4** Edge Function: `POST /auth/register` — create user record + generate per-user HMAC secret
- [ ] **B1-5** Edge Function: `POST /events/sync` — validate checksum, rate-limit, store events, compute `server_validated`
- [ ] **B1-6** Android: add Supabase Kotlin client dependency
- [ ] **B1-7** Android: `AuthRepository` — register / login / refresh token
- [ ] **B1-8** Android: `SyncRepository` — batch-upload pending Room events on app open
- [ ] **B1-9** Android: generate `device_fingerprint` on first install (store in EncryptedSharedPrefs)
- [ ] **B1-10** Android: generate HMAC `client_checksum` for every event before storing in Room
- [ ] **B1-11** Android: `OnboardingScreen` — add "Create Account" step after existing permission steps
- [ ] **B1-12** Write integration tests for event validation Edge Function (valid, tampered, backdated, rate-limited)

**Exit criteria:** A fresh install can register, events sync to server, server rejects tampered events.

---

## Phase B2 — Groups & Invites

**Goal:** Users can create groups, invite others via invite code, and see
members in a group.

### Tasks

- [ ] **B2-1** Edge Function: `POST /groups/create` — create group + add creator as admin
- [ ] **B2-2** Edge Function: `POST /groups/join` — validate invite code, add member
- [ ] **B2-3** Edge Function: `GET /groups/:id/members` — return members with display names + avatars
- [ ] **B2-4** Android: `GroupRepository` — create, join, fetch members
- [ ] **B2-5** Android: `GroupScreen` — new screen to create or join a group (enter invite code)
- [ ] **B2-6** Android: show invite code in Settings with copy + share buttons
- [ ] **B2-7** Android: `DuelScreen` — extend to show all group members (not just one partner)
- [ ] **B2-8** Invite link deep link: `unhook://join?code=XK92PL` → opens app → joins group
- [ ] **B2-9** Invite link fallback: web landing page (single static HTML) for non-installed users
- [ ] **B2-10** Handle edge cases: user already in group, invalid code, group at max size (20 members)

**Exit criteria:** Ashish can invite Arti and family members; all appear in the group.

---

## Phase B3 — Leaderboard & Activity Feed

**Goal:** On app open, users see the group's weekly leaderboard and a
recent activity feed (who resisted what, streaks earned, etc.).

### Tasks

- [ ] **B3-1** Edge Function: `GET /groups/:id/leaderboard` — weekly + monthly + all-time rankings
- [ ] **B3-2** DB: scheduled function (Supabase cron) every Monday 00:00 to compute `weekly_scores` + ranks
- [ ] **B3-3** Edge Function: `GET /groups/:id/feed` — last 50 events across all members, formatted
- [ ] **B3-4** Android: `FeedRepository` — fetch feed + leaderboard on app open
- [ ] **B3-5** Android: `DashboardScreen` — add group feed section below personal score
- [ ] **B3-6** Android: `LeaderboardScreen` — new screen, podium-style top 3 + list below
- [ ] **B3-7** Android: `LeaderboardViewModel` + StateFlow for rankings data
- [ ] **B3-8** Android: skeleton loading states for feed + leaderboard (no spinners)
- [ ] **B3-9** Android: offline fallback — show last-cached feed with "last updated X ago" label
- [ ] **B3-10** Add ranking delta indicator (↑2 ↓1 — from last week)

**Exit criteria:** App open shows live group rankings and "Ashish resisted YouTube · 2h ago" style feed.

---

## Phase B4 — Anti-Cheat Enforcement

**Goal:** Server actively detects and penalizes cheating. Users can see if
someone in their group has a flagged account.

### Tasks

- [ ] **B4-1** Edge Function: heartbeat endpoint `POST /heartbeat` — update `service_heartbeats`
- [ ] **B4-2** Android: WorkManager task every 6h to send heartbeat
- [ ] **B4-3** Server-side cron: scan `service_heartbeats` for gaps >6h during 8am–11pm; auto-insert `service_gap` penalty event
- [ ] **B4-4** Server-side: cross-reference synced `UsageStatsManager` dump with claimed resist events
- [ ] **B4-5** Android: include `UsageStatsManager` dump (per-app daily totals) in every sync payload
- [ ] **B4-6** DB: `integrity_flags` table — log anomalies per user (too many resists, stat mismatch, device change)
- [ ] **B4-7** Android: `ProfileScreen` — show "verified" badge for users with clean integrity score
- [ ] **B4-8** Group admin: ability to see if a member has active integrity flags (not the raw data)
- [ ] **B4-9** Edge Function: device re-verification flow — new install must wait 24h before events count
- [ ] **B4-10** Write tests: service gap detection, usage stat cross-check, device fingerprint mismatch

**Exit criteria:** Uninstalling and reinstalling doesn't reset cheating history; gaps are penalized.

---

## Phase B5 — Achievements & Challenges

**Goal:** Persistent badges and weekly group challenges add long-term
motivation beyond the weekly reset.

### Tasks

- [ ] **B5-1** DB: seed `achievements` table with initial set (streak_7, streak_30, first_500_pts, clean_week, most_improved, group_champion)
- [ ] **B5-2** Edge Function: achievement evaluation — called after every event sync, awards new badges
- [ ] **B5-3** Edge Function: `GET /users/:id/achievements` — return earned badges with dates
- [ ] **B5-4** Edge Function: `POST /groups/:id/challenges` — admin creates a weekly challenge
- [ ] **B5-5** Edge Function: `POST /challenges/:id/complete` — mark completion (server validates against event log)
- [ ] **B5-6** Android: `AchievementsScreen` — grid of badges, earned vs locked
- [ ] **B5-7** Android: `ChallengeCard` component — show active challenge on DashboardScreen
- [ ] **B5-8** Android: push notification (local, not server) when a new achievement is earned
- [ ] **B5-9** Android: achievement unlock animation (lottie or custom Compose animation)
- [ ] **B5-10** Design: icon set for all achievements (vector drawables)

**Exit criteria:** Completing a 7-day streak awards a permanent badge visible to the whole group.

---

## Phase B6 — Social Sharing

**Goal:** Users can share weekly results as a polished image card to any
social media platform.

### Tasks

- [ ] **B6-1** Design share card template (1080x1080): rank, points, streak, week date, UnHook branding
- [ ] **B6-2** Edge Function: `POST /share-cards/generate` — server-side card generation using Satori (HTML→PNG) or similar
- [ ] **B6-3** Supabase Storage: bucket for share cards, signed URLs, auto-expire after 7 days
- [ ] **B6-4** Edge Function: `GET /share-cards/:id` — public endpoint with Open Graph meta tags for social preview
- [ ] **B6-5** Android: update existing share button in Settings to call server API instead of local canvas
- [ ] **B6-6** Android: share group leaderboard card (top 3 members this week)
- [ ] **B6-7** Android: share personal achievement card when badge is earned
- [ ] **B6-8** Android: use Android `ShareSheet` with image + text + deep link back to group
- [ ] **B6-9** Static web page (`unhook.app/s/:card_id`) — renders share card as Open Graph page for Twitter/Instagram previews
- [ ] **B6-10** Analytics: track share card views (simple counter, no PII)

**Exit criteria:** Tapping "Share" generates a real image, shares to Instagram Stories with UnHook branding.

---

## Phase B7 — Polish & Production Readiness

**Goal:** Harden the backend for real users — monitoring, rate limiting,
privacy compliance, and app store requirements.

### Tasks

- [ ] **B7-1** Rate limiting: global + per-user limits on all Edge Functions (Upstash Redis or Supabase built-in)
- [ ] **B7-2** Error monitoring: integrate Sentry for Edge Function errors
- [ ] **B7-3** Logging: structured logs for all sync events (no PII in logs)
- [ ] **B7-4** Privacy: GDPR/data deletion endpoint `DELETE /users/:id` — purge all data
- [ ] **B7-5** Privacy policy page (simple static HTML) covering usage data collected
- [ ] **B7-6** Android: account deletion flow in Settings
- [ ] **B7-7** Android: "export my data" option (JSON download of all point events)
- [ ] **B7-8** Backend: automated DB backups (Supabase handles this on Pro tier — verify config)
- [ ] **B7-9** Load test: simulate 1000 concurrent syncs, verify Edge Function cold start times
- [ ] **B7-10** Security audit: review all RLS policies, check for privilege escalation paths
- [ ] **B7-11** Android: handle auth token expiry gracefully (silent refresh, not logout)
- [ ] **B7-12** Android: offline-first — all features work without internet; sync when reconnected

**Exit criteria:** App passes Play Store review, handles 10k users without errors.

---

## Future Ideas (Backlog)

These are not scoped to any phase yet:

- **Streak Recovery:** Spend 30 points to "heal" a missed day — adds engagement loop
- **Anonymous Mode:** Show rank (1st/2nd/3rd) without revealing exact usage numbers
- **Couples Mode migration:** Existing partner setup becomes a 2-person group automatically
- **Apple Watch / Wear OS companion:** Resist button on wrist
- **Web dashboard:** Read-only view of group standings at `unhook.app/groups/:id`
- **AI-powered reminder messages:** Personalized messages based on your most-slipped apps
- **Corporate wellness tier:** Teams/organizations, manager dashboard, no personal data exposed

---

## Open Questions

1. **Identity verification:** Should users verify with phone number to prevent throwaway accounts?
2. **Group size limit:** 20 members feels right — is there a use case for larger groups?
3. **Cross-group competition:** Can you be in multiple groups? (e.g., family AND work colleagues)
4. **Monetization:** Free forever? Premium for groups >5 members? No ads — that would be ironic.
5. **iOS:** Same backend works for an iOS app. Worth planning for from day one in the schema.
