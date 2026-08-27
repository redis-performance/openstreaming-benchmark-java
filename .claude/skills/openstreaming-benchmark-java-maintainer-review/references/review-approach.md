# Review approach — openstreaming-benchmark-java

There is no mined maintainer voice or nitpick taxonomy for this repo (see
`codebase-facts.md` — zero review comments exist in its entire history).
This file is therefore a **generic, honest checklist grounded in what this
codebase actually contains**, not a set of citable precedents. Don't present
any item below as "this project's convention" or "what the maintainer
usually asks for" — present it as ordinary, sound review practice for this
kind of code, and say plainly that this repo's own history doesn't yet give
you a real precedent to cite when a check is generic rather than
repo-specific.

## What to actually check

1. **Does it plausibly build?** This is a Maven project with an external
   jar dependency (`redis-streams-java`) installed via a manual
   `mvn install-file` step documented in `README.md` — not resolved from
   Maven Central. If a PR changes `pom.xml` (a dependency version bump, a
   new dependency), check whether the change is consistent with that
   install step and whether `README.md`'s documented version/commands still
   match (PR#1's title — "Updates towards redis-streams-java v0.2.0" — is a
   real example of exactly this kind of change).

2. **New or changed CLI flags** (`@Option` fields in `BenchmarkRunner.java`):
   - Does the flag have a sensible `defaultValue` and a description that
     actually describes what it does? (Two existing flags,
     `--max-stream-length` and `--retention-time-secs`, both currently carry
     the description "Retention time secs" — a real, pre-existing copy-paste
     artifact in this codebase. Don't invent a rule that this must never
     happen; just don't let a NEW instance of the same slip through
     unnoticed if you spot one.)
   - If the flag changes producer or consumer behavior, is it actually
     wired into both the `producer` and `consumer` branches of
     `BenchmarkRunner.run()` where relevant, or silently only affecting one
     mode?
   - Is the new flag's usage documented in `README.md`'s `--help` output
     block and one of its sample-command sections? PR#3
     (`--consumer-groups-per-topic`) is a real example of a PR that touched
     `README.md` alongside the code change — treat a new user-facing flag
     landing without any README update as worth a comment, since the
     precedent for keeping them in sync is real (even though it's a
     precedent from the PR author's own practice, not a reviewer's
     enforcement — be precise about that provenance if you cite it).

3. **Threading and shared state.** Producer/consumer clients are plain
   `Thread` subclasses started directly in a loop, sharing one
   `ConcurrentHistogram` and one `GenericObjectPoolConfig`. Check that:
   - Any new shared mutable state introduced across threads is either
     immutable, thread-safe (like the existing `ConcurrentHistogram`), or
     explicitly synchronized — this codebase's existing pattern relies on
     per-thread `JedisPooled` handles precisely to avoid shared-connection
     races; a change that starts sharing a single connection or a mutable
     field across threads without synchronization is a real correctness
     risk given this architecture, not a hypothetical one.
   - Rate-limiting math (`rps`, `--zipfian`, per-client division) still
     produces sane values — e.g. no obvious divide-by-zero when `clients`
     or `rps` is 0 (the existing code already guards `rps > 0` before
     constructing a `ZipfDistribution`/`RateLimiter`; a change nearby should
     preserve that guard).

4. **Exit codes / error handling.** `main()` propagates picocli's exit code
   via `System.exit(exitCode)`; benchmark threads currently swallow
   `InterruptedException` with `e.printStackTrace()` rather than
   propagating it. There's no evidence this repo has an opinion on richer
   error handling than that — don't invent one — but do flag a change that
   makes an existing failure mode (a thread dying silently, an exception
   swallowed without at least a printed trace) worse than what's already
   there.

5. **Scale-match the review to the PR.** Every real PR in this repo's
   history is small (10–30 changed lines, 2–4 files, single-purpose,
   one-line description). A routine, similarly-scoped change (a new flag, a
   dependency bump, a small behavior tweak) warrants a short review, not an
   exhaustive audit. Reserve a longer, more structured comment for a PR that
   is unusually large or touches multiple subsystems relative to this
   repo's real historical norm — and even then, keep it to a small number of
   concrete, numbered points rather than a formal multi-section essay; nothing
   in this repo's (admittedly nonexistent) review record suggests that register.

## What NOT to do

- Don't claim any of the above is "what the maintainer wants" or "this
  project's convention" — it is a reasonable generic check for this kind of
  threaded Java CLI tool, applied honestly, not a mined precedent. Only the
  README-sync observation in item 2 and the copy-pasted-description note
  have a real, specific example behind them; say so precisely rather than
  implying broader institutional weight.
- Don't apply `redisbench-admin`'s taxonomy (argparse mutual-exclusion
  flags, RedisTimeSeries retry/backoff arithmetic, Codecov gating) — that
  taxonomy is mined from a different, Python codebase with actual review
  history behind it; none of those specific citations exist here.
- Don't apply `memtier_benchmark`'s C/C++-specific categories.
- Don't fabricate a coverage percentage, a CI status, or a "maintainers
  usually ask for tests here" claim — there are no tests and no CI history
  to point to.
- Don't `@`-mention any GitHub username in a review comment.
- Don't manufacture a "verdict" block, bolded summary line, or TL;DR — end
  in plain prose, same as every other skill in this org.
