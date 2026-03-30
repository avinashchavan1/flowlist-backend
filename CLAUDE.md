# Claude Code Instructions — FlowList Backend

This file is read automatically by Claude Code at the start of every session.

---

## Keep Frontend PROJECT.md in Sync

The canonical project documentation lives at:
`/Users/avinashchavan/claude_projects/tasks/flowlist/PROJECT.md`

After any meaningful backend change, update the relevant sections:

| Change | Sections to update |
|---|---|
| New endpoint | §4.3 API Endpoints |
| DB schema change | §4.4 Database Schema |
| New env variable | §4.6 Configuration, §6.3 Env Vars Reference |
| New dependency | §4.1 Tech Stack |
| Auth/security change | §4.5 Auth & Security |
| New bug fix / gotcha | §7 Learnings & Gotchas |
| Deployment change | §6.2 Backend → Railway |

---

## Project Context

- Spring Boot 3.4.4, Java 21, Gradle 8.12
- **No Lombok** — incompatible with Java 21. Use explicit getters/setters/builders.
- JWT via jjwt 0.12.6 (HS256). Secret must be ≥ 32 chars.
- All config via env vars with `${VAR:default}` in `application.properties`
- `PORT` env var is injected by Railway automatically — do not hardcode

## Security Rules

- All `/api/tasks/**` routes are auth-gated (default in SecurityConfig)
- New public routes must be explicitly whitelisted in `SecurityConfig.java`
- Every task mutation must call `ownerTask(email, taskId)` before writing to DB
- Ownership failures → `AccessDeniedException` → mapped to 404 (not 403) by GlobalExceptionHandler

## Deploy

```bash
export RAILWAY_API_TOKEN=<token>
railway up --service flowlist-api --detach
railway logs --service flowlist-api
```
