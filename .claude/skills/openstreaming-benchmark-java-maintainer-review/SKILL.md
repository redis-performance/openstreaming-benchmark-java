---
name: openstreaming-benchmark-java-maintainer-review
description: Review a redis-performance/openstreaming-benchmark-java pull request, branch, or diff for correctness and fit with this small Java streaming-benchmark tool. Use this whenever the user asks to review an openstreaming-benchmark-java PR, wants a repo-specific pre-merge check, or is deciding accept/reject on this repo's PRs. Grounded in this repo's ACTUAL (very thin) history: 3 merged PRs, zero review comments, zero issues, no CONTRIBUTING.md/AGENTS.md, no tests, all authored and self-merged by a single maintainer (filipecosta90). This is not a "maintainer voice" skill in the sense the org's other review skills are — there is no mined voice or precedent to imitate here, and this file says so rather than inventing one.
---

# openstreaming-benchmark-java review

## Read this first: what this repo's history actually is

As of the time this skill was written (mining `gh pr list --state all` and
`gh api .../pulls/<n>/reviews` + `/comments` + `/issues/<n>/comments` against
`redis-performance/openstreaming-benchmark-java`):

- **Exactly 3 pull requests exist, ever**, all merged: #1 ("Updates towards
  redis-streams-java v0.2.0"), #2 ("Setting a zipfian rps distribution when
  rps is specified"), #3 ("Added the --consumer-groups-per-topic feature").
- **All three were authored AND merged by the same person**, `filipecosta90`
  (Filipe Oliveira), each within minutes of opening (#1: opened and merged
  same minute range; #2 and #3 similarly same-day/same-session).
- **Zero review comments, zero PR review objects, zero issue comments exist
  on any of the three PRs.** `pulls/<n>/reviews`, `pulls/<n>/comments`, and
  `issues/<n>/comments` all returned empty arrays for #1, #2, and #3.
- **Zero GitHub issues have ever been opened** on this repo.
- **No `CONTRIBUTING.md`, no `AGENTS.md`, no `.github/` directory at all**
  existed before this change — no prior CI, no prior review automation, no
  written contribution rules to cite as doctrine.
- **No test suite** — `src/` contains only `src/main/java/...`, no
  `src/test`. There is nothing to cite about "coverage rules" here because
  none exist, written or de facto.
- All three PRs are small (10–29 lines changed, 2–4 files), and all three PR
  descriptions are a single-line restatement of the title — no design notes,
  no self-review sections, no rationale for defaults.
- The repo itself has been dormant since the last of these three PRs
  (last push February 2024).

**Do not manufacture a maintainer voice, a review culture, or institutional
doctrine that this repo's real history does not show.** Unlike
`redisbench-admin` (thin but real precedent exists) or `memtier_benchmark`
(a dense, dialectic review history), this repo has **no review text to mine
at all** — no tone to imitate, no recurring nitpick category with a real
citation, no "here's how a maintainer here actually phrased X." Say that
plainly in any review you write rather than inventing quotes, a persona, or
a false sense of established convention. `references/codebase-facts.md` and
`references/review-approach.md` go into more detail — read both before
writing a review.

## What this means for how you review

Because there is no repo-specific precedent to lean on, review PRs here the
way a careful, generic reviewer would review a small Java CLI benchmark
tool — grounded in what's actually in this codebase (see
`references/codebase-facts.md` for its real shape: picocli-based CLI,
`BenchmarkRunner`/`ProducerThread`/`ConsumerThread`, Jedis/redis-streams-java
dependency, no tests, Java 17, Maven), not in generic "best practices for
Java" or a fabricated house style. `references/review-approach.md` lays out
what to actually check (does it build, does it match the existing threading
and CLI-flag patterns, does a new flag get documented in `README.md`'s usage
block the way #2 and #3 did, is there an obvious correctness issue) and, just
as importantly, what NOT to claim (a coverage rule, a maintainer preference,
a "this project always does X" pattern) because none of that exists in this
repo's real record.

## Process

1. **Get the material.** For a PR: `gh pr view <n> --repo
   redis-performance/openstreaming-benchmark-java --json body,commits,files,author`
   and `gh pr diff <n> --repo redis-performance/openstreaming-benchmark-java`.
   These PRs have historically been small and undocumented (a one-line title
   repeated as the body) — don't assume a design write-up will be there to
   read, and don't penalize the PR for not having one; that's simply this
   repo's own established (if thin) norm.

2. **Scope gate.** If the PR touches nothing under `src/`, `pom.xml`, or
   `README.md`'s usage documentation (e.g. it's a totally unrelated asset),
   say so in one sentence and treat it as out of scope rather than
   force-fitting a Java-code checklist onto it.

3. **Work the checklist** in `references/review-approach.md` — it is a
   generic-but-grounded checklist (build correctness, thread-safety in the
   producer/consumer model, CLI flag wiring via picocli, README consistency),
   not a mined-precedent taxonomy, because no mined precedent exists here.

4. **Write the review.** Keep it short and factual, matching the size of the
   PRs this repo actually receives (10–30 line diffs). Do not adopt a
   "maintainer persona" — there's no real voice to imitate. A plain,
   professional first-pass review is the honest choice here. If nothing
   stands out on a routine/small change, say so briefly rather than padding
   the review to look thorough.

5. **Land on a verdict** in plain prose (approve / raise a concern / ask a
   question) — no bolded "Verdict:" line, no `@`-mention of the (single)
   maintainer, no fabricated citation to "how this project's maintainers
   usually handle this."

## What NOT to do

- Don't claim this repo has a maintainer review culture, a house style, or a
  recurring nitpick pattern — the mined history has none. Say so honestly
  instead.
- Don't invent quotes or a "voice" attributed to `filipecosta90` or anyone
  else — no review comment from any person exists anywhere in this repo's
  history to quote.
- Don't cite a test-coverage rule, a CONTRIBUTING.md rule, or an AGENTS.md
  rule — none of those files exist in this repo.
- Don't apply `redisbench-admin`'s Python-specific taxonomy (argparse
  mutual-exclusion, RedisTimeSeries retry/backoff) or `memtier_benchmark`'s
  C/C++ categories here — this is a small Java/Maven/picocli codebase with
  its own real, much smaller shape (see `references/codebase-facts.md`).
- Don't literally `@`-mention any GitHub username in a review comment.
- Don't write a long, formally-sectioned "code review essay" for a PR this
  repo's own real history shows arrives small and undocumented — match the
  actual scale of what's being reviewed.
