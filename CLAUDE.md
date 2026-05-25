# Claude Code Instructions — FlowList Backend

This file is read automatically by Claude Code at the start of every session.

---

## ⛔ PRODUCTION SAFETY — READ FIRST, NON-NEGOTIABLE

This app has REAL users with REAL data on a live Supabase Postgres. Treat every
change as production. Breaking prod is the worst outcome — worse than a slow fix.

**Database rules:**
- **NEVER rename a `@Table` / `@Column` on a live DB.** Hibernate `ddl-auto=update`
  does NOT rename — it CREATES a new empty table/column and ORPHANS all existing
  data. This already caused a full lockout once (table prefix rename → empty tables
  → every user logged out). If a rename is truly needed: write an explicit SQL
  migration (`ALTER TABLE ... RENAME`), run it on the DB FIRST, then change the entity.
- **Before ANY schema change or data migration on prod: take a backup** (dump the
  affected tables) and **confirm the plan with the user**.
- **Never `TRUNCATE`/`DROP`/`DELETE` on prod tables** without an explicit, in-chat
  user OK for that specific operation. Prefer `INSERT … SELECT` (non-destructive).
- Schema changes go live the moment they deploy (Render auto-builds on push +
  `ddl-auto=update` runs on boot). There is no staging. Double-check entity edits.
- DB connection: Supabase pooler `aws-1-ap-south-1.pooler.supabase.com:5432`,
  user `postgres.<ref>`. Use `psql` (libpq) to inspect/back up before touching data.

**Deploy rules:**
- Backend auto-deploys on push to `main` (Render, Dockerfile). Verify the build
  compiles + smoke-test a live endpoint after every push.
- Don't change `@Table` names, auth/JWT secret, or DB URL casually — they lock users out.

When unsure whether something is destructive: STOP and ask.

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
