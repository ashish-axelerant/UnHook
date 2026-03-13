# ship — Sync PLAN.md and push changes

Update PLAN.md to reflect all work done this session, then commit and push.

## Steps

1. **Review what changed** — run `git diff --stat HEAD` and `git status` to see all modified/new files.

2. **Update PLAN.md** — reflect the current state of the project:
   - Mark any newly completed deliverables with `[x]`
   - Add a new Phase section if meaningful new work was done this session that isn't already captured
   - Keep the "Current Phase" section in CLAUDE.md accurate (update if phase changed)
   - Do NOT rewrite history — only add/update what changed this session

3. **Stage and commit** — include all modified files (PLAN.md + code changes):
   ```
   git add <specific files — never git add -A blindly>
   git commit -m "[Phase X] <concise description of what was built>"
   ```
   Derive the phase number and description from the work done. Follow the existing commit message style in the repo.

4. **Push** — `git push origin main`

5. **Report back** — summarize: commit hash, files changed, what was marked complete in PLAN.md.
