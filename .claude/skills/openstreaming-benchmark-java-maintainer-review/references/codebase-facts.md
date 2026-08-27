# Codebase facts — openstreaming-benchmark-java, real state at mining time

Mined directly from the repository contents and its 3 real merged PRs
(`redis-performance/openstreaming-benchmark-java`, mined 2026-08-26/27).
Everything below is what was actually observed, not inferred house style.

## What the tool is

A Java port of `redis-performance/openstreaming-benchmark`: a load-generator
CLI for benchmarking Redis Streams (producer/consumer throughput and
latency), built on `com.redis.streams:redis-streams-java` and Jedis
(`JedisPooled`). Single Maven module, no multi-module structure.

## Real shape of the code (3 files under `src/main/java/com/redis/`, no
`src/test` at all)

- **`BenchmarkRunner.java`** (~255 lines) — the picocli `@Command` entry
  point and `Runnable.run()`. Owns all CLI flags as `@Option`-annotated
  fields (e.g. `-s/--server`, `-m/--mode` with `producer`/`consumer` string
  values, `-c/--clients`, `--consumer-groups-per-topic`,
  `--consumers-per-stream-min/-max`, `--rps`, `--zipfian`, `--seed`,
  `--retention-time-secs`, `--max-stream-length`, `--verbose`). `run()`
  builds a `GenericObjectPoolConfig<Connection>`, spins up one
  `ProducerThread` or `ConsumerThread` per logical client (`Thread.start()`
  directly — no `ExecutorService`, no thread pool abstraction beyond the
  Jedis connection pool), shares one `ConcurrentHistogram` (HdrHistogram)
  across all threads for latency, and polls it once a second in the main
  thread to print progress/RPS until either `numberRequests` is reached or
  no threads remain alive.
- **`ProducerThread.java`** (~130 lines) / **`ConsumerThread.java`** (~84
  lines) — `extends Thread` directly, each holds its own `JedisPooled`
  handle, and use `com.redis.streams` topic/consumer APIs
  (`TopicManager`, `TopicProducer`, `SerialTopicConfig`,
  `TopicNotFoundException`/`InvalidTopicException`/`InvalidMessageException`)
  to produce/consume from Redis Streams topics.
- Mode selection is a raw `mode.equals("producer")` string check in
  `BenchmarkRunner.run()`, not an enum — a real, observable pattern in this
  codebase (not a criticism to necessarily force a change on, just note it
  as the existing convention when reviewing something that touches mode
  dispatch).
- `--rps` combined with `--zipfian` drives an Apache Commons Math
  `ZipfDistribution` to derive a per-client rate passed into a Guava
  `RateLimiter`; this is exactly what PR#2 added (see below).

## Dependencies (from `pom.xml`)

Java 17 (`maven.compiler.source/target`), Maven build producing a
`-jar-with-dependencies` shaded jar (per `README.md`'s usage instructions).
Key dependencies: `com.redis.streams:redis-streams-java` (an external jar,
installed locally per the README's `mvn ... install-file` step before
`mvn package` — this is a real, load-bearing build prerequisite, not
optional), `redis.clients:jedis`, `com.google.guava` (`RateLimiter`),
`org.apache.commons:commons-math3` (`ZipfDistribution`),
`org.apache.commons:commons-text`, `org.hdrhistogram:HdrHistogram`,
`me.tongfei:progressbar`, `picocli`, `com.fasterxml.jackson.core:jackson-databind`.

## The 3 real PRs, in full (this is the entire mined history)

| PR | Title | Author | Files changed | +/- lines | Description body |
|----|-------|--------|----------------|-----------|-------------------|
| #1 | Updates towards redis-streams-java v0.2.0 | filipecosta90 | 3 (`pom.xml`, `ConsumerThread.java`, `ProducerThread.java`) | +10/-17 | Title restated, no further text |
| #2 | Setting a zipfian rps distribution when rps is specified | filipecosta90 | 2 (`pom.xml`, `BenchmarkRunner.java`) | +18/-1 | Title restated, no further text |
| #3 | Added the --consumer-groups-per-topic feature | filipecosta90 | 4 (`README.md`, `pom.xml`, `BenchmarkRunner.java`, `ProducerThread.java`) | +29/-17 | Title restated, no further text |

All three: opened and merged by the same person, same day (in most cases
within minutes), zero review comments/reviews/issue comments on any of
them (`gh api .../pulls/<n>/reviews`, `/comments`, and `/issues/<n>/comments`
all returned `[]` for #1, #2, #3). Zero GitHub issues have ever been filed.

## What does NOT exist in this repo (don't cite these as if they did)

- No `CONTRIBUTING.md`.
- No `AGENTS.md`.
- No `.github/` directory of any kind before this change (no prior CI, no
  prior issue templates, no prior PR template).
- No test suite (`src/test` does not exist).
- No written or de facto coverage rule, style guide, or review checklist.
- No second contributor, reviewer, or maintainer has ever appeared in this
  repo's PR or issue history — it is a single-author repo to date.
- The repo has been dormant since PR#3 merged (last push February 2024).

Treat all of the above as an honest gap, not something to paper over with
invented doctrine borrowed from a different, larger repo in the
`redis-performance` org.
