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
- **Database:** PostgreSQL (Neon free tier). Switched from MySQL on Railway.
- **Hosting:** Koyeb free eco service (no spindown). Switched from Railway.
- **No Lombok** — incompatible with Java 21. Use explicit getters/setters/builders.
- JWT via jjwt 0.12.6 (HS256). Secret must be ≥ 32 chars.
- All config via env vars with `${VAR:default}` in `application.properties`
- `PORT` env var is injected by Koyeb automatically — do not hardcode

## Security Rules

- All `/api/tasks/**` routes are auth-gated (default in SecurityConfig)
- New public routes must be explicitly whitelisted in `SecurityConfig.java`
- Every task mutation must call `ownerTask(email, taskId)` before writing to DB
- Ownership failures → `AccessDeniedException` → mapped to 404 (not 403) by GlobalExceptionHandler

## Deploy (Koyeb)

Koyeb auto-builds from `Dockerfile` on every push to the connected GitHub branch.

Manual trigger via CLI:
```bash
koyeb service redeploy <service-name>
koyeb service logs <service-name>
```

Required env vars on Koyeb:
- `DATABASE_URL` — `jdbc:postgresql://<host>/<db>?sslmode=require` (Neon)
- `DB_USERNAME` — Neon role name
- `DB_PASSWORD` — Neon role password
- `JWT_SECRET` — ≥32 char random string
- `CORS_ALLOWED_ORIGINS` — `https://flowlist-app.netlify.app`
- `VAPID_PUBLIC_KEY` / `VAPID_PRIVATE_KEY` / `VAPID_SUBJECT`
- `RESEND_API_KEY` (optional, for password reset emails)
- `APP_BASE_URL` — `https://flowlist-app.netlify.app`
- `LOG_LEVEL` — `INFO` (default) or `DEBUG`
