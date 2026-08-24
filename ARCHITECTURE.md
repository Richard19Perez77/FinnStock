# FinnStock — Architecture

A high-level record of what this app does, why it is built the way it is, and what changes when a
details screen is added. Written for someone joining the project, not as an API reference.

---

## 1. What the app does today

FinnStock is a single-screen market dashboard backed by Finnhub's free REST API.

The screen has two independent sections:

- **A ticker banner** across the top, showing the current price and daily change for a fixed set of
  US equities (TSM, AAPL, NVDA, GOOGL, MSFT, AMZN). It scrolls horizontally.
- **A market news feed** below it, rendered as a lazy list of headlines with source and relative
  time.

On top of that there are three interactions:

- **Pull to refresh**, which reloads both sections.
- **A shuffle toggle**, which randomises the order of the news feed for people who want to skim
  rather than read top-down.
- **Per-section retry**, shown when a section fails while the other may still be fine.

That is the whole product surface. Everything below exists to make those few behaviours correct,
predictable, and cheap to extend.

---

## 2. The data source, and the constraints it creates

Finnhub's free tier is generous but shapes several decisions:

| Constraint | Consequence in the code |
| --- | --- |
| ~60 calls per minute | Refresh must not be able to stack up; each dashboard load costs 7 calls |
| No batch quote endpoint | One request per symbol, fanned out in parallel |
| Zeroed 200 responses for inaccessible symbols | A zero price has to be treated as "no data", not a real quote |
| No sort or date range on general news | Ordering and any windowing are client-side concerns |
| Key travels per request | Auth belongs in an interceptor, not at call sites |

The important point is that most of the "unusual" logic in this codebase traces directly back to a
row in that table. None of it is speculative generality.

---

## 3. The shape of the codebase

The app follows Clean Architecture layering inside a single Gradle module.

```
app/            Application class, MainActivity, theme
core/           Cross-cutting infrastructure: DI qualifiers, dispatchers, networking
feature/stocks/
  data/         Retrofit API, DTOs, mappers, repository implementation
  domain/       Models, error types, repository interface, use cases
  presentation/ MVI contract, reducer, ViewModel, Compose UI
```

### The dependency rule

Dependencies point inward. `presentation` and `data` both depend on `domain`; `domain` depends on
nothing. This is the single rule that makes the rest of the structure worth having.

Concretely, it means the domain layer has no knowledge of Retrofit, Moshi, OkHttp, HTTP status
codes, Android, or Compose. When the repository catches an `HttpException`, it converts it into a
domain error at that boundary and nothing further in has to know HTTP exists.

### Why one module, one feature package

There is exactly one feature and one screen, so the code lives in one module. Splitting into
`:core`, `:data`, `:feature-stocks` Gradle modules buys parallel builds and enforced boundaries —
neither of which pays for itself at this size, and both of which add friction to every change.

The package layout already mirrors the module split that would come later, so if the app grows to
justify it, the move is mechanical rather than a rewrite. That is the deliberate trade: adopt the
*structure* now, defer the *ceremony*.

---

## 4. Clean Architecture, and what it actually buys here

Three layers, three jobs:

**Domain** owns the vocabulary of the app — `Quote`, `NewsArticle`, `MarketError` — plus the
repository interface and use cases. It describes *what* the app can do without saying how.

**Data** implements that interface. It knows Finnhub's field names, its quirks, its error codes, and
translates all of it into domain terms. DTOs never leave this layer; mappers are the border guard.

**Presentation** turns domain objects into pixels and user gestures into intents. It knows nothing
about where data came from.

### On use cases being thin

`GetQuotesUseCase` and `GetMarketNewsUseCase` currently do nothing but call the repository. That is
a fair criticism, and the honest justification is not "purity" — it is that use cases are where
per-operation logic lands the moment there is any, and having the seam already in place means it
never has to be retrofitted through a ViewModel.

Concrete near-term examples for this app: a minimum interval between refreshes, merging multiple
news categories, filtering the feed to the last 24 hours, or combining a quote with a company
profile for the details screen. Each belongs in a use case, not in the ViewModel and not in the
repository.

If they were still empty pass-throughs after the app had grown considerably, deleting them would be
the right call. At two use cases, the cost is negligible.

---

## 5. MVI, and how the loop runs

The presentation layer is Model-View-Intent: state flows one way, events flow the other, and there
is exactly one place where state changes.

```
User gesture ──▶ Intent ──▶ ViewModel ──▶ PartialChange ──▶ Reducer ──▶ State ──▶ UI
                              │                                                    │
                              └──────────────── use cases ◀────────────────────────┘
```

Four pieces, each with one job:

**State** is a single immutable data class describing everything the screen shows. There is no
second source of truth — no `mutableStateOf` in the composables holding screen data, no fields the
UI derives and then forgets.

**Intent** is the complete list of things a user can do: `Load`, `Refresh`, `RetryQuotes`,
`RetryNews`, `ToggleShuffle`. Reading the sealed interface tells you the screen's entire input
surface.

**PartialChange** is the complete list of things that can happen to state — loading started, data
arrived, request failed, shuffle toggled. Intents are *requests*; partial changes are *facts*.

**Reducer** is a pure function `(State, PartialChange) -> State`. No coroutines, no injection, no
clock, no randomness.

### Why the reducer is kept pure

This is the highest-leverage decision in the presentation layer. Because `reduce` is pure:

- Every state transition can be unit tested without Android, Hilt, coroutines, or a fake API.
- There is exactly one place to look when the screen shows something unexpected.
- The ViewModel shrinks to orchestration — launching work, mapping results into partial changes —
  which is the part that genuinely needs coroutines.

Purity is why randomness lives in the ViewModel rather than the reducer. When shuffle is toggled,
the ViewModel generates a seed and passes it *into* the partial change. The reducer just stores it.
A test can pass a fixed seed and assert an exact ordering.

### Why PartialChange exists at all

A simpler design would let the ViewModel call `_state.update { it.copy(...) }` directly. The extra
type earns its place by making transitions enumerable and testable: the set of legal state changes
is a sealed interface you can read in one screen, rather than scattered `copy` calls across
coroutines.

---

## 6. The decisions that shaped the current code

### 6.1 One state per section, not loose flags

Each section is a `SectionState<T>` — `Loading`, `Content`, or `Failure` — rather than a trio of
`isLoading` / `data` / `errorMessage` fields.

The original design allowed contradictions: loading and errored simultaneously, or content present
alongside an error with no rule about which wins. Those states were unreachable in practice only
because the reducer happened to be careful. Now they are unrepresentable, and the UI has one
exhaustive `when` per section instead of chained emptiness checks.

Two helpers encode a deliberate behaviour: content already on screen survives both a reload and a
failed reload, so a refresh never blanks a section you were reading. The trade-off is that a failed
refresh over existing content is currently silent — see §9.

### 6.2 Errors are a type, not a string

`MarketError` enumerates what can go wrong in market terms: `MissingApiKey`, `Unauthorized`,
`RateLimited`, `Network`, `Unknown`.

The data layer classifies; the UI decides wording. This matters because the two failures users
actually hit — a missing key during setup, and the rate limit during enthusiastic pull-to-refresh —
both deserve specific, actionable copy. Passing `Throwable.message` up the stack produces
`HTTP 429`, which tells a user nothing and leaks transport details into the view.

It also means error copy can move to `strings.xml` for localisation without touching any logic.

### 6.3 A result type instead of exceptions or `Result`

The repository returns `MarketResult<T>` — `Success` or `Failure(MarketError)`.

Kotlin's built-in `Result` was the obvious alternative, but it carries a `Throwable`, which would
either push HTTP knowledge into the ViewModel or require wrapping domain errors in exceptions to
smuggle them across. Using exceptions for expected outcomes — a rate limit is not exceptional — also
makes the happy path harder to read.

`MarketResult` costs about ten lines and makes failure a normal, exhaustively-handled return value.

### 6.4 The banner survives partial failure

Quotes are fetched per symbol in a `supervisorScope`, each with its own catch. One rejected ticker
drops one chip; the section only fails when every symbol fails.

The earlier version used `awaitAll` inside a plain `coroutineScope`, so a single 403 cancelled its
siblings and emptied the whole banner. With a free tier where individual symbols may be restricted,
all-or-nothing is the wrong default.

### 6.5 Absent data is absent, not zero

Finnhub answers `200` with zeroed fields for symbols a key cannot access. The mapper returns `null`
for those rather than coercing to `0.0`, and the repository filters them out.

Coercion produced the worst possible outcome: a row reading `0.00` with a green `+0.00%`, visually
indistinguishable from a real flat quote. A missing chip is honest; a fake one is not.

### 6.6 Shuffle is a seed, not a second list

State stores `shuffleSeed: Long`; the UI derives the shuffled order inside a `remember`.

Storing a pre-shuffled copy meant two lists in state that could drift apart, and made the same state
render differently depending on when the shuffle happened. A seed keeps one list, makes rendering
deterministic for a given state, and makes the behaviour testable.

The product rule behind it: refresh means "fetch the latest", not "reshuffle to look new". Shuffle
is opt-in, off by default, and only re-randomises on an explicit toggle or a refresh while enabled.

### 6.7 One in-flight job per section

Each section owns a `Job`. A request for a section already in flight reuses it rather than starting
a second one.

This exists because of the rate limit — each dashboard load is 7 calls, and pull-to-refresh is a
gesture people repeat. An earlier attempt used a single shared job, which fixed the spam but made
the Retry button a silent no-op whenever the *other* section was still loading. Per-section jobs
solve both.

### 6.8 Auth is a header, and it is redacted

The key travels as `X-Finnhub-Token` rather than a `token` query parameter, and the logging
interceptor redacts that header.

As a query parameter, the key appeared in full in every logged request line — visible in logcat,
bug reports, and screen recordings. Finnhub supports both forms, so the header costs nothing.

The key itself comes from `local.properties` (gitignored) into `BuildConfig`. Worth being clear
about the limit: this keeps the key out of version control, not out of the APK. Anyone can decompile
a shipped build and read it. A real product would proxy Finnhub through a backend.

### 6.9 Qualified bindings and an injected dispatcher

DI uses typed qualifier annotations (`@FinnhubApiKey`, `@AuthInterceptor`, `@LoggingInterceptor`,
`@IoDispatcher`) rather than `@Named("string")`, so a typo is a compile error rather than a runtime
one, and a second `Interceptor` binding cannot become ambiguous.

The repository takes its dispatcher by injection rather than hardcoding `Dispatchers.IO`, which lets
tests run everything on a test dispatcher and control timing.

---

## 7. What is deliberately missing

Naming these matters as much as naming what exists — each is a decision, not an oversight.

- **Navigation.** One screen, so `MainActivity` hosts it directly. Adding a NavHost before there is
  a second destination is pure ceremony.
- **Persistence and caching.** Every load hits the network. There is nothing to read offline and no
  freshness policy. Correct for a dashboard whose entire value is being current.
- **WebSockets.** Finnhub offers live trade streaming. Polling on refresh is enough for a screen
  people glance at.
- **Paging.** The news endpoint returns a single batch, and nobody scrolls a market feed for
  hundreds of items.
- **Tests.** The most significant gap. The reducer is a pure function with real logic and no
  dependencies — it is the highest-value, lowest-effort test target in the codebase, and it is
  currently untested.

---

## 8. Adding a details screen

This is where the architecture stops being theoretical, because a second destination exercises
several seams that a single screen never touches.

### 8.1 What the screen is

Tapping a ticker chip opens a detail view for that symbol: current quote with change, company
profile (name, logo, industry, exchange), and recent company-specific news.

### 8.2 What has to change structurally

**Navigation becomes real.** Add `androidx.navigation:navigation-compose` and a NavHost with two
destinations. `hilt-navigation-compose` is already a dependency, so `hiltViewModel()` keeps working
per destination. Prefer type-safe routes over string concatenation so the symbol argument is checked
at compile time.

**The Effect channel comes back.** It was deleted as dead code, and a details screen is exactly what
justifies it. One-shot events — navigate to detail, open an article URL in a browser, show a
snackbar — are not state: replaying them on rotation would re-navigate or re-open a browser. They
belong in a `Channel` consumed once, not in the `StateFlow`.

Adding it also fixes an existing gap: `NewsArticle.url` is mapped but unused, so headlines are not
tappable today.

**The detail screen gets its own MVI triple.** Its own `Contract`, `Reducer`, and `ViewModel`, with
the symbol read from `SavedStateHandle`. Sharing a ViewModel between list and detail is the common
shortcut and the common regret — it couples two screens' lifecycles and makes state a union of both.

Note the arrival of a second `SectionState` consumer: that is the moment to promote it from the
feature package into `core/ui`, where it can be shared. Not before.

### 8.3 What the domain gains

New models (`CompanyProfile`), new repository methods, new use cases:

- `GET /stock/profile2?symbol=` — company profile, free tier.
- `GET /company-news?symbol=&from=&to=` — symbol news, free tier, and unlike general news it *does*
  take a date range, so the last-30-days window is a server-side concern here.
- Price history for a chart is the one to verify before designing around it. Candle data has moved
  between free and paid tiers historically, so confirm current access on your plan first rather than
  building a chart you cannot feed.

`MarketError` likely gains a `NotFound` case for unknown symbols.

### 8.4 The repository question worth thinking about

Today the dashboard fetches a quote for AAPL, and tapping AAPL would fetch that same quote again a
second later. Fine at two screens, but it is the first sign that the repository should become a
**single source of truth** rather than a pass-through.

The natural evolution is for the repository to hold quotes in memory and expose `Flow<Quote>`, with
explicit refresh triggers. Both screens observe the same data, a refresh anywhere updates
everywhere, and duplicate calls disappear. If offline support ever matters, Room slots in behind the
same interface without the presentation layer noticing — which is precisely the payoff the layering
was set up for.

Worth doing **when** the duplication becomes visible, not preemptively.

### 8.5 Naming will need one more pass

The `stocks` feature already hosts news, and a details screen makes `StocksScreen`, `StocksContract`,
and `StocksViewModel` describe a dashboard rather than "stocks". The clearer end state is a `market`
feature with `presentation/dashboard` and `presentation/detail`, sharing one domain and data layer.

Similarly, `presentation/mvi` currently holds Compose UI alongside MVI plumbing; with two screens it
is worth separating.

### 8.6 The rate-limit budget

Dashboard load is 7 calls. A detail screen adds roughly 2–3 more. Opening several symbols in quick
succession, each with a pull-to-refresh, approaches 60 per minute faster than it looks.

The per-section job guard handles gesture spam within a screen but does nothing across screens. If
`RateLimited` starts appearing in normal use, the fix is a short-lived cache in the repository —
which is the same change described in §8.4, arriving for a second reason.

### 8.7 A sensible order of work

1. **Write reducer tests first.** They are cheap now and become the safety net for everything below.
2. **Reintroduce `Effect`** and make news headlines open their URL. Small, self-contained, proves
   the channel works before navigation depends on it.
3. **Add navigation** with the dashboard as the only destination. Structural change in isolation,
   nothing user-visible.
4. **Build the detail screen** — contract, reducer, ViewModel, UI — with quote and profile only.
5. **Add company news** to the detail screen, reusing the existing news row composable.
6. **Introduce repository caching** once the duplicate-fetch smell is real.

Each step ships independently and leaves the app working.

---

## 9. Open risks and known rough edges

- **A failed refresh over existing content is silent.** Deliberate — it avoids blanking a feed you
  are reading — but it means a rate limit during refresh produces no feedback at all. The fix is a
  snackbar via the `Effect` channel, which is why §8.2 sequences `Effect` early.
- **No empty state.** If the news endpoint returns nothing, the screen shows a header over blank
  space.
- **Relative timestamps do not tick.** `formatRelativeTime` reads the clock during composition, so
  "2m ago" stays "2m ago" until something else recomposes.
- **Edge-to-edge insets.** The `Scaffold` applies its padding to the whole screen, so the list
  cannot scroll under the system bars.
- **The API key ships in the APK.** See §6.8. Acceptable for a portfolio or learning app; not for a
  product.
- **The ticker list is hardcoded.** A user-editable watchlist means persistence, which is the first
  genuine reason to add a database.
