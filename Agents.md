# Agents.md (Index & Always-On Agent Rules)

This document defines **all operational rules and requirements that every non-interactive coding agent must follow** for the Supernova IPTV project. Meta/orchestration or "ask for clarification" rules are excluded.

---
## Project Structure

```text
/app
  /src
    /main
      /java/com/supernova
        /api
        /data
        /sync
        /workers
        /ui
      /res
        /layout
        /values
/tests
/docs
  /agent
  /architecture
    /01-goal_scope
    /04-data
    /05-flow
    /06-ui
    /07-apis
```
- **Data layer:** Place entities, DAOs, and repositories in `data/`.
- **API/services:** Place in `api/`.
- **Workers/sync logic:** Place in `workers/` or `sync/` as appropriate.
- **UI/screens:** Place in `ui/`.
- **Tests:** Mirror package in `/test/`.
- **Docs:** All reference docs in `/docs/`.

**Never guess file locations. Always follow this map unless explicitly instructed otherwise.**

--

## Always-Needed Agent Rules
- chmod +x ./build.py
- The repository **must compile and all unit tests must pass** after every commit **`./build.py build`**, never just `assembleDebug`).
- No breaking changes between task boundaries—each task must leave the repo compilable and all tests passing.
- **Never modify or delete existing code/scripts unless explicitly instructed.**
- **Remove all unused or deprecated code immediately.** Regular lint and static analysis must be run.
- **Commit message:** Must include summary of changes, issue references, error log for any build/test failures, and steps taken to resolve errors.
- **Test coverage:** All new or changed code must have ≥70% unit/UI test coverage.
- **Unit test conventions:** Use MockK for ViewModel/unit tests and in-memory Room DB for repository tests.
- **UI navigation:** D-pad navigation must work across all screens; set `nextFocus*` attributes as required.
- **PRs:** Must include clear description, screenshots for UI, feedback log, and be minimal in scope.

---

## Reference-By-Need Policies

Reference these subdocs **only if your current task requires the details**:

- **Pull Requests & Commit Details:** [`Agents_PR.md`](./docs/agent/Agents_PR.md)
- **Feedback Log Requirements:** [`Agents_Feedback.md`](./docs/agent/Agents_Feedback.md)
- **UI/UX & Navigation Principles:** [`Agents_UI.md`](./docs/agent/Agents_UI.md)
- **Pre-Run Environment & Build Steps:** [`Agents_Environment.md`](./docs/agent/Agents_Environment.md)
- **Architecture Reference Policy:** [`Agents_Architecture.md`](./docs/agent/Agents_Architecture.md)

---

**Agents must reference subdocs only if their current task requires those principles or workflows. Never load or summarize a section unless absolutely necessary.**
