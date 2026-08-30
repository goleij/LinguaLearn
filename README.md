# LinguaLearn

[![CI](https://github.com/goleij/LinguaLearn/actions/workflows/ci.yml/badge.svg)](https://github.com/goleij/LinguaLearn/actions/workflows/ci.yml)
[![Backend](https://img.shields.io/badge/backend-Spring%20Boot%203.2-6DB33F?logo=springboot&logoColor=white)](pom.xml)
[![Frontend](https://img.shields.io/badge/frontend-React%2018%20%2B%20TypeScript-61DAFB?logo=react&logoColor=black)](frontend/package.json)
[![Tests](https://img.shields.io/badge/tests-92%20JUnit%20%7C%2019%20Vitest%20%7C%2012%20Playwright-blue)](#testing)

A German learning platform built around CEFR levels and a lesson engine that teaches
before it tests. A lesson is a sequence of **activities** that walks the learner from new
material to a graded checkpoint, and every piece of XP the interface shows is a number
the backend actually stored.

---

## Contents

- [What it does](#what-it-does)
- [Architecture](#architecture)
- [Tech stack](#tech-stack)
- [Getting started](#getting-started)
- [Project layout](#project-layout)
- [The lesson activity engine](#the-lesson-activity-engine)
- [XP, progress and unlocking](#xp-progress-and-unlocking)
- [External services](#external-services)
- [API reference](#api-reference)
- [Testing](#testing)
- [Continuous integration](#continuous-integration)

---

## What it does

- **Four CEFR levels.** A1 through B2, each with its own courses, units and lessons.
- **Lessons that teach.** Every lesson runs through seven stages: Learn, Context, Guided
  practice, Practice, Apply, Checkpoint, Result. Only the checkpoint decides whether the
  lesson is complete, so practice mistakes cost nothing.
- **Feedback worth reading.** A wrong answer says *why*: which word sat in the wrong
  position, how many pairs were right, which required word was missing.
- **Honest XP.** XP is paid once per activity. Replaying a finished lesson is practice,
  not a way to farm points.
- **Word Explorer.** Any German word in the interface can be clicked to look it up in the
  German Wiktionary.
- **Writing Coach.** Free writing tasks can be checked for grammar before submission.

---

## Architecture

```
+--------------------------+         +-------------------------------------+
|  React + TypeScript SPA  |  /api   |        Spring Boot backend          |
|  (Vite dev server 5173)  | ------> |             (port 8081)             |
|                          |  JSON   |                                     |
|  pages/  components/     | <------ |  controller -> service -> repository|
|  services/  hooks/       | session |                    |                |
+--------------------------+  +CSRF  |                    v                |
                                     |              SQLite file            |
                                     |                                     |
                                     |  service/external --> Tatoeba       |
                                     |                   --> de.wiktionary |
                                     |                   --> LanguageTool  |
                                     +-------------------------------------+
```

Two rules hold the design together:

1. **The frontend owns no business logic.** It renders what the API returns. XP, grading,
   progress and unlocking live in `ProgressService` and nowhere else.
2. **The browser never talks to a third party.** Tatoeba, Wiktionary and LanguageTool are
   reached from the backend, so no endpoint or key is ever exposed in client code, and any
   of them can be swapped or self-hosted without touching React.

Authentication is a server-side session (`JSESSIONID`). Spring Security's CSRF protection
is satisfied by echoing the `XSRF-TOKEN` cookie back in the `X-XSRF-TOKEN` header, which
the fetch wrapper in `frontend/src/services/api.ts` does on every call. An unauthenticated
API call returns `401` with a JSON body rather than a redirect.

---

## Tech stack

| Layer    | Choice                                                                 |
| -------- | ---------------------------------------------------------------------- |
| Backend  | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA             |
| Database | SQLite via `sqlite-jdbc`, Hibernate 6.3 with the community dialect     |
| Frontend | React 18, TypeScript 5.6, Vite 7, Tailwind CSS 3.4, React Router 7     |
| Testing  | JUnit 5 + Mockito + MockMvc, Vitest + React Testing Library, Playwright |

---

## Getting started

**Prerequisites:** JDK 17+, Maven 3.9+, Node.js 20+.

### 1. Backend

```bash
mvn spring-boot:run
```

Starts on <http://localhost:8081>. On first run it creates `germanlearning.db`, repairs
the schema if needed, and seeds the courses.

### 2. Frontend

In a second terminal:

```bash
cd frontend && npm install && npm run dev
```

Open <http://localhost:5173>. The dev server proxies `/api` to port 8081, so the browser
stays on one origin and the session and CSRF cookies work.

### Production build

```bash
mvn clean package
```

```bash
cd frontend && npm run build
```

---

## Project layout

```
src/main/java/com/germanlearning/
├── config/            Security, schema repair, seeding
│   └── content/       The authored courses, one class per course
├── controller/        REST endpoints; no business logic
├── dto/               Request and response records
├── model/             JPA entities and the enums that describe an activity
├── repository/        Spring Data interfaces
└── service/
    ├── activity/      One grader per answer shape (strategy pattern)
    └── external/      Tatoeba, Wiktionary and LanguageTool clients

frontend/
├── e2e/               Playwright specs
└── src/
    ├── components/
    │   └── activities/  One component per activity type, plus ActivityRenderer
    ├── hooks/           useAuth, useNotification, useWordExplorer
    ├── pages/           Login, Register, Dashboard, Lesson, Profile
    ├── services/        The fetch wrapper and the typed API clients
    ├── test/            Vitest setup
    └── types/           Mirrors of the backend DTOs
```

---

## The lesson activity engine

A lesson is an ordered list of `LessonActivity` rows. Each one carries its **type**, its
**phase**, and the metadata that makes later personalisation possible: skill, topic,
difficulty, CEFR level, XP reward. Type-specific data (options, pairs, dialogue lines)
lives in a JSON `payload` column, so a new activity type needs no schema change.

**Phases** — `LEARN`, `CONTEXT`, `GUIDED_PRACTICE`, `PRACTICE`, `APPLY`, `CHECKPOINT`.
Guided practice offers its hint before the answer; practice is forgiving; only the
checkpoint decides completion.

**Types** — six teaching types (`LEARN_CARD`, `VOCABULARY`, `GRAMMAR_TIP`, `DIALOGUE`,
`REAL_EXAMPLE`, `CHECKPOINT`) and eight graded ones (`MULTIPLE_CHOICE`, `FILL_BLANK`,
`SENTENCE_BUILDER`, `MATCH_PAIRS`, `TRANSLATION`, `CONTEXT_CHOICE`, `LISTENING`,
`SHORT_WRITING`).

Adding a type takes three steps: a value in `ActivityType`, a grader in `service/activity`
that declares the types it supports, and a component wired into `ActivityRenderer`.

Every answer is stored as an `ActivityAttempt` with its skill, topic and correctness.
Nothing reads that history yet; it is the foundation Smart Review will stand on.

---

## XP, progress and unlocking

`ProgressService.submitAnswer(userId, lessonId, activityId, answer)` is the only entry
point, and it is the single source of truth:

- XP is paid **once per activity**, tracked in `xpAwardedActivityIds` on the progress row.
- The **answer streak** counts consecutive correct answers within an attempt, resets on a
  wrong one and at the start of every attempt, and is what the XP bonus is scaled by. It
  lives on the progress row.
- The value returned to the interface is read back from the saved row, so what the navbar
  shows is what the database holds.
- **Completion** is decided by the checkpoint score against an 80% threshold; a lesson
  without a checkpoint falls back to its overall score. Passing unlocks the next lesson
  and locks XP, so replays are practice.

Two streaks are deliberately kept apart, because they answer different questions. The
**answer streak** above belongs to one attempt at one lesson. The **daily streak** on the
user — the flame in the navbar and the "N days" on the profile — counts days in a row and
is updated by `UserService.updateStreak` when a lesson attempt begins, so it follows real
learning rather than how long a browser tab stayed open. Coming back twice in a day does
not extend it; missing a day starts it over at one.

Content is versioned. `DataInitializer` compares each course's stored `contentVersion`
against the authored one and refreshes only what is behind, reusing the existing course,
units and lessons, so a content update never duplicates a course or discards accounts.

---

## External services

All three are optional, free, and called only from the backend. Each is wrapped in a
service with a timeout, a cache and a graceful fallback: if the service is down the
feature disappears quietly and the lesson still works.

| Service       | Used for                                                            | Configure with                       |
| ------------- | ------------------------------------------------------------------- | ------------------------------------ |
| Tatoeba       | Real example sentences (enrichment only, never core lesson content) | `lingualearn.external.tatoeba.*`     |
| de.wiktionary | The Word Explorer, via the standard MediaWiki API                   | `lingualearn.external.dictionary.*`  |
| LanguageTool  | The Writing Coach                                                   | `lingualearn.external.language-tool.*` |

The public LanguageTool endpoint is rate limited and meant for interactive use, which is
why a check only runs when the learner presses the button. For heavier use, point it at a
self-hosted instance:

```properties
lingualearn.external.language-tool.base-url=http://localhost:8010/v2
```

Set any `...enabled=false` to switch a service off.

---

## API reference

Everything under `/api` needs a session except the four auth endpoints.

### Auth

| Method | Path                 | Purpose                                      |
| ------ | -------------------- | -------------------------------------------- |
| `POST` | `/api/auth/register` | Create an account, returns `201` + the user  |
| `POST` | `/api/auth/login`    | Start a session, returns the user or `401`   |
| `POST` | `/api/auth/logout`   | End the session, returns `204`               |
| `GET`  | `/api/auth/me`       | The current user, or `401`                   |

### Learning

| Method | Path                                               | Purpose                                      |
| ------ | -------------------------------------------------- | -------------------------------------------- |
| `GET`  | `/api/levels`                                      | Every CEFR level with how much it holds      |
| `GET`  | `/api/courses?level=A1`                            | Courses, optionally by level                 |
| `GET`  | `/api/courses/{id}`                                | One course with its units and lessons        |
| `GET`  | `/api/dashboard?level=A1`                          | The dashboard for a level                    |
| `GET`  | `/api/lessons/{id}`                                | A lesson; a locked one returns no activities |
| `POST` | `/api/lessons/{id}/attempt`                        | Start a fresh attempt, returns the user      |
| `POST` | `/api/lessons/{id}/activities/{activityId}/answer` | Submit an answer, returns the graded result  |
| `POST` | `/api/lessons/{id}/complete`                       | Finish the attempt, returns the summary      |
| `GET`  | `/api/profile`                                     | Stats and recent activity                    |

### Tools

| Method | Path                     | Purpose                                 |
| ------ | ------------------------ | --------------------------------------- |
| `GET`  | `/api/dictionary/{word}` | Word Explorer lookup                    |
| `GET`  | `/api/examples?query=`   | Example sentences                       |
| `POST` | `/api/writing/check`     | Grammar feedback for a piece of writing |

---

## Testing

Three layers, each answering a different question.

| Layer                    | Tool                           | Count | Answers                       |
| ------------------------ | ------------------------------ | ----- | ----------------------------- |
| Unit, slice, integration | JUnit 5, Mockito, MockMvc      | 92    | Do the rules hold?            |
| Component                | Vitest + React Testing Library | 19    | Does the interface behave?    |
| End to end               | Playwright (Chromium)          | 12    | Does the whole thing work?    |

### Running them

```bash
mvn test
```

```bash
cd frontend && npm test
```

```bash
cd frontend && npm run test:e2e
```

The e2e command starts both servers itself; nothing needs to be running first. The first
run needs the browser:

```bash
cd frontend && npx playwright install chromium
```

Watch mode while working on a component:

```bash
cd frontend && npm run test:watch
```

### What is covered

**Backend** (`src/test/java`)

- `AuthServiceTest` — a username and an email may each be taken once; the stored password
  is a BCrypt hash and never the plain text; authentication fails for a wrong password and
  for an unknown user.
- `AuthControllerTest` (`@WebMvcTest`) — the HTTP contract of the auth endpoints: empty
  fields, a username under three characters and a password under six are all `400` before
  any user is created; a valid registration is `201` and never leaks the hash; a bad login
  is `401`; `/api/auth/me` without a session is `401`. It imports the real `SecurityConfig`,
  so the CSRF rules are part of what is tested.
- `SecurityIntegrationTest` (`@SpringBootTest`) — the rules as the browser meets them: an
  anonymous API call is `401` with a JSON body, registering is reachable without a session,
  a `POST` without a CSRF token is `403`, a logged-in session can read the API, and after
  logout it cannot.
- `LessonControllerTest` (`@WebMvcTest`) — the lock, which the controller is the only place
  to enforce: starting an attempt, answering and completing a locked lesson are each `403`
  and never reach `ProgressService`, and a locked lesson hands back no activities at all,
  so its answers cannot leak. Plus `404` for an unknown lesson and `400` for an activity
  that belongs to a different one.
- `LessonServiceTest` — the unlock chain: a course is one flat ordered run across its
  units, only the immediate predecessor counts, starting a lesson is not the same as
  completing it, and the dashboard tiles resolve the same rule the same way.
- `CourseControllerTest` — the level picker and the dashboard: every CEFR level is offered
  even when empty, the level filter filters, an unknown course is `404`.
- `ProgressServiceTest` — the XP and phase rules: one formula, a real streak, no double
  payout on retry, interface numbers equal to stored numbers, and a checkpoint that alone
  decides completion.
- `UserServiceTest` — the daily streak, including the awkward first day: a new account is
  created with `lastActiveAt` already set, so an implementation that only handles the null
  case leaves the learner on zero until tomorrow.
- `ActivityGradingTest` — every grader, including umlaut folding (`ae` for `ä`), accepted
  alternatives and partial-credit feedback.
- `ScoreServiceTest`, `SchemaMaintenanceTest`, `ExternalServicesTest`, `WiktionaryParserTest`.

**Frontend components** (`frontend/src/**/*.test.tsx`)

- `api.test.ts` — the fetch wrapper: the `/api` prefix, the session cookie, the
  `X-XSRF-TOKEN` header echoed from the cookie, a `204` returning nothing, and a failed
  response becoming an `ApiError` that carries the server's message.
- `ProtectedRoute.test.tsx` — including the case that matters most: while `/auth/me` is
  still in flight it waits instead of redirecting, so a reload does not eject a signed-in
  visitor.
- `RegisterPage.test.tsx` — each client-side rule shows its message *and* makes no request.
- `MainLayout.test.tsx` — the navbar badges read XP and streak from the auth context rather
  than from a copy taken at mount, and re-render when a lesson updates them.

**End to end** (`frontend/e2e`)

- `auth.spec.ts` — register, register with a taken username, log in, wrong password, log
  out (and confirm the session is really gone).
- `protected.spec.ts` — `/`, `/profile` and `/lesson/1` all redirect to the login page
  without a session.
- `lesson.spec.ts` — plays the seeded *Greetings* lesson to its result screen, checking
  that XP rose, that the daily streak moved to one and stayed there, that the next lesson
  unlocked, that both survive a reload and reach the profile page, and that replaying the
  finished lesson pays nothing a second time.

### Test databases

No test ever touches `germanlearning.db`.

- JUnit runs under the `test` profile (`src/test/resources/application-test.properties`),
  which points Spring at `target/lingualearn-test.db` and recreates it every run.
- Playwright starts the backend under the `e2e` profile
  (`src/main/resources/application-e2e.properties`) on port **8082** against
  `target/lingualearn-e2e.db`, and points the dev server's proxy there. Each spec
  registers its own account, so runs do not interfere.

Both profiles switch the external services off, so no test depends on the network.

---

## Continuous integration

`.github/workflows/ci.yml` runs on every push and pull request to `main`:

1. **Backend** — `mvn -B verify` on Temurin 17, with the surefire reports uploaded.
2. **Frontend** — `npm ci`, `npm test`, then `npm run build`, which type-checks first.
3. **End to end** — after both pass, installs Chromium and runs the Playwright suite,
   uploading the HTML report.
