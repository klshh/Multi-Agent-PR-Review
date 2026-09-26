# PR Review Bot — Phase 1

Single-agent code reviewer: Spring Boot service that sends pasted code/diffs to
Claude and returns a structured review. No GitHub integration yet — that's Phase 2.

## Prerequisites

- Java 11 (this build targets Spring Boot 2.7.18, the last version supporting JDK 11 —
  Spring Boot 3.x requires JDK 17+)
- Maven 3.6+
- An Anthropic API key (create a free account at https://platform.claude.com — new
  accounts get a small amount of trial credit, no card required to claim it)

## Setup

1. Set your API key as an environment variable (never commit it):

   ```bash
   export ANTHROPIC_API_KEY=sk-ant-xxxxxxxx
   ```

2. Build and run:

   ```bash
   mvn spring-boot:run
   ```

   The app starts on `http://localhost:8080`.

## Try it

Send a diff for review:

```bash
curl -X POST http://localhost:8080/review \
  -H "Content-Type: application/json" \
  -d '{
    "diff": "public String getUser(String id) { String sql = \"SELECT * FROM users WHERE id = \" + id; return jdbcTemplate.queryForObject(sql, String.class); }"
  }'
```

You should get back a JSON review pointing out the SQL injection risk, among
other things.

View review history:

```bash
curl http://localhost:8080/reviews
```

Inspect the H2 database directly (optional): open `http://localhost:8080/h2-console`
in a browser, JDBC URL `jdbc:h2:mem:prreviewdb`, username `sa`, blank password.

## What's here

| File | Purpose |
|---|---|
| `ClaudeClient` | Talks to the Anthropic Messages API directly via `RestTemplate` |
| `ReviewService` | Holds the reviewer's system prompt, calls `ClaudeClient`, saves to DB |
| `ReviewController` | Exposes `POST /review` and `GET /reviews` |
| `ReviewRecord` / `ReviewRecordRepository` | JPA entity + repo for review history |
| `GlobalExceptionHandler` | Turns validation errors and Claude API failures into clean JSON responses |

## Cost note

This uses `claude-haiku-4-5-20251001` by default (cheapest current model) to
stretch your trial credits as far as possible. Change `claude.api.model` in
`application.properties` if you want to compare review quality against a
stronger model later.

## Next: Phase 2

- Split the single reviewer into specialized agents (quality/security/tests/docs)
- Pull real diffs from a GitHub PR instead of pasting them
- Post the review back as an actual PR comment via GitHub's webhook + REST API
# Multi-Agent-PR-Review
