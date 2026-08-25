# Ghana Smart Hospital Service Operations Optimizer — Technical Report

*Final report, assembled from the working system. All
numbers cited (record counts, tree heights, timings, test totals) were taken
from the repository itself: `mvn test` output, `efficiency_results.csv`,
and the trace documents in this folder.*

---

## Table of contents

1. Cover page
2. Problem statement, assumptions, input/output definitions, system boundaries
3. Dataset description, data dictionary, database schema
4. System architecture and module design
5. Data-structure implementation section
6. Algorithm implementation section
7. Correctness evidence
8. Performance analysis
9. Database integration evidence
10. Responsible algorithm selection
11. Individual contribution statements and oral-defense preparation
12. References and appendices

---

## 1. Cover page

**Title:** Ghana Smart Hospital Service Operations Optimizer

**Local context:** Hospital/clinic operations.

**Organisation modelled:** a service-dispatch network of ten real Greater
Accra health facilities — University of Ghana Hospital Legon, Korle-Bu
Teaching Hospital, Ridge Hospital (Greater Accra Regional Hospital), 37
Military Hospital, La General Hospital, Tema General Hospital, Madina
Polyclinic, Achimota Hospital, Nyaho Medical Centre, and Dodowa Health
Centre. Each facility contributes ten department-level nodes (Emergency
Ward, OPD, Maternity, Pharmacy, Lab, ICU, Theatre, Radiology, Admin, Main
Gate), giving 100 network locations in total. How these facilities and their
coordinates were sourced and verified is documented in Appendix E,
`evidence_note.md`.

**Technology stack:** Java 17, Maven, SQLite via `sqlite-jdbc`
(single-file database `ghana_hospital.db`, no external DB server), console
application entry point (`Main.java`), JUnit 5 test suite.

**Team:** Charismatic Enigmas

| # | Member | Index number |
|---|---|---|
| 1 | Precious Bassey Inyang | 22059210 |
| 2 | Priscilla Kwakyewaa | 22150993 |
| 3 | Edmund Mulcahy Amonoo | 22057575 |
| 4 | Asante Kojo Boakye | 22065837 |
| 5 | Kwabena Ntiamoah Boateng | 22046431 |
| 6 | Owusu Sarfo Kwadwo | 22078485 |
| 7 | Daniel Okuluri Tetteh | 22297380 |
| 8 | Daniel Kubi Adjetey Junior | 22059983 |
| 9 | Joel Asante | 22407542 |
| 10 | Daniel Ofori Boateng | 22408351 |
| 11 | Prince Oppong | 22375768 |
| 12 | Christian Kwabena Asante | 22387357 |
| 13 | Evans Osei | 22411279 |
| 14 | Gillis Abraham Nii Akwei Mclutason | 22387934 |
| 15 | Bryan Acquah Annor | 22368027 |

**Course:** DCIT 308 `[TEAM: add section number, lecturer name, submission date]`

All fifteen index numbers above are exactly the set used by
`ParameterDerivation.java` to derive the project's four algorithm parameters
(§4), as the brief requires.

---

## 2. Problem statement, assumptions, input/output definitions, system boundaries

### 2.1 Problem statement

A hospital network is modelled as a weighted graph: vertices are departments
and wards across the ten facilities; edges are roads carrying two weights —
a physical distance (km) and a travel-time cost (minutes, adjusted by a road
condition factor). Onto this graph arrives a stream of service requests —
ambulance dispatches, inter-facility referrals, pharmacy runs, lab-sample
collections, equipment moves, and porter jobs — each with a category, an
urgency level (CRITICAL/HIGH/MEDIUM/LOW), a submission timestamp, and a
deadline derived from urgency. A finite pool of resources (ambulances,
porters, nurses, wheelchairs, courier bikes) serves these requests one at a
time.

The system must answer six operational questions:

1. **Dispatch ordering** — which request should be handled next, and does an
   urgency-aware order beat plain FIFO?
2. **Routing** — what is the fastest route between any two locations under
   the real travel-time weights?
3. **Reachability** — which locations can be reached from a dispatch point
   (e.g. after a road closure)?
4. **Budgeted selection** — given a fixed time/resource budget, which subset
   of requests maximises total served value?
5. **Scalability** — how do the underlying structures and algorithms behave
   as the dataset grows by orders of magnitude?
6. **Persistence** — how is all state saved to and reloaded from a relational
   store?

### 2.2 Assumptions

- All edge weights (travel times) are non-negative — a precondition Dijkstra's
  algorithm genuinely requires, and one satisfied by construction since a
  travel time cannot be negative.
- Each request belongs to exactly one category and one urgency level;
  deadlines are derived deterministically from urgency
  (CRITICAL = +15 min, HIGH = +60 min, MEDIUM = +180 min, LOW = +720 min).
- A resource serves exactly one request at a time.
- The system runs single-machine against a local SQLite file; concurrent
  multi-user access is out of scope.
- Request "cost" for budgeted selection is estimated service time in whole
  minutes; "value" is an urgency-weighted score.

### 2.3 Input/output definitions and system boundaries

The formal input/precondition/output contract of each of the six major
operations (priority dispatch, binary search, BFS reachability, Dijkstra
shortest path, Kruskal MST, 0/1-knapsack request selection) is specified with
pseudocode in Appendix B, `pseudocode.md`. Two preconditions there are
load-bearing and are deliberately *not* enforced inside the algorithms:
binary search requires sorted input (see §7.3 for why violating this fails
silently rather than loudly), and Dijkstra requires non-negative weights.

The system boundary is the console application: `Main.java` presents a
21-option numbered menu and is the only entry point. Per the brief this is
not a UI-design project — every assessed operation is demonstrated through
the menu against the real loaded dataset, not through a GUI. Persistence
crosses the boundary only through `DatabaseManager` (JDBC/SQLite); seed data
crosses it only through the deterministic generator script `db/seed/generate_data.py`.

---

## 3. Dataset description, data dictionary, database schema

### 3.1 Dataset construction (summary)

Full detail is in Appendix E, `evidence_note.md`. In brief:

- **Real:** the ten facility names, their general locations, and their
  geographic coordinates. Coordinates were verified in two passes — initial
  public-geocoding lookups, then a second pass against OpenStreetMap
  (Nominatim/Overpass, the data behind geoplaner.com) which corrected four
  positions; nine of ten now sit on an actual OSM-mapped facility object or
  within its real grounds, with Dodowa's honestly flagged as area-level.
- **Synthetic but realistic:** department node positions (small ~250 m offset
  around the real facility coordinate), roads, requests, resources. No real
  patient, staff, or operational data anywhere in the dataset — no personal
  data of any kind.
- **Roads are geography-derived, not independent noise:** `distance_km` is
  the true haversine distance between endpoint coordinates;
  `travel_time_min` derives from it at an assumed 20 km/h urban Accra speed,
  scaled by a road-condition multiplier. Connectivity is guaranteed in two
  layers (intra-campus MST + proximity edges; inter-facility MST over real
  coordinates + nearest-neighbour links), both built with Prim's algorithm.
- **Deterministic and reproducible:** the generator seeds Python's RNG with
  680000, derived from the team's fifteen index numbers; the identical value
  is mirrored in Java by `ParameterDerivation.randomSeed()`. Re-running the
  script reproduces byte-identical CSVs.

### 3.2 Data dictionary

Six tables (full field-by-field dictionary in Appendix A, `data_dictionary.md`;
schema in `db/schema.sql`):

| table | purpose | records | brief minimum |
|---|---|---|---|
| `locations` | graph vertices — departments within the 10 facilities | 100 | 50 |
| `roads` | weighted undirected edges (distance, travel time, condition) | 370 | 100 |
| `service_requests` | jobs to prioritise/route/select | 300 | 300 |
| `resources` | ambulances/porters/nurses/wheelchairs/couriers | 30 | 30 |
| `algorithm_runs` | empirical timing rows for the efficiency lab | 30 seed + live growth | 30 |
| `audit_events` | undo/audit trail exercised by the stack demo | grows live | — |

Every minimum in the brief is met or exceeded (830 total seed records).
Three indexes support the query patterns: `idx_requests_status`,
`idx_requests_urgency`, and `idx_roads_from`/`idx_roads_to` on the road
endpoints.

---

## 4. System architecture and module design

```
db/schema.sql, db/seed/*.csv            persistent storage + seed data
        │
DatabaseManager (JDBC/SQLite)           M2: load/save
        │
Location/Road/ServiceRequest/Resource   immutable model records
        │
structures/*   (13 custom structures)    M3/M5/M7: core DS implementations
        │
algorithms/*   (search/sort/greedy/DP)   M4/M8: algorithm layer
        │
Main.java console menu (21 options)     M5/M6/M7/M9: live demonstrations
        │
experiments/EfficiencyExperimentRunner  M10: Section 9 suite -> CSV
```

Module responsibilities:

- **M2 persistence** — `db/DatabaseManager.java`: creates the schema, imports
  the seed CSVs, loads all four entity tables into memory, and appends live
  rows to `algorithm_runs`/`audit_events`.
- **M3 structures** — `structures/`: thirteen hand-built data structures.
  Per Section 8.ii of the brief, built-in Java collections are used *only*
  for file parsing, JDBC plumbing and test scaffolding — every assessed core
  structure is written from scratch.
- **M4/M8 algorithms** — `algorithms/`: searching (linear, binary), sorting
  (selection, insertion, merge, quick), greedy resource assignment, DP
  knapsack request selection. Graph algorithms (BFS, DFS, Dijkstra, Prim,
  Kruskal) live inside `structures/Graph.java`.
- **M5–M9 application** — `app/Main.java`: 21 menu options grouped into six
  engines (graph, search/sort, indexing, connectivity/scheduling,
  optimisation, efficiency lab). `app/ParameterDerivation.java` derives four
  algorithm parameters from the team's fifteen index numbers (Section 2.iii
  of the brief):

  | parameter | derivation | value |
  |---|---|---|
  | priority weight | digit-sum of all indexes, mod 10, +1 | 2 |
  | route penalty | 1 + (first index mod 100)/100 | 1.42 |
  | hash-table size | next prime above (index sum mod 500) + 101 | 257 |
  | RNG seed | Π(index mod 97 + 1) mod 10⁶ | 680000 |

- **M10 experiments** — `experiments/EfficiencyExperimentRunner`: runs all six
  required Section 9 experiments at increasing input sizes, three repeats
  each, writing `efficiency_results.csv`; `docs/graphs/plot_efficiency_results.py`
  renders dependency-free SVG charts.

---

## 5. Data-structure implementation section

Thirteen custom structures are implemented in `src/main/java/com/ghdsa/structures/`,
each with unit tests and a live console demonstration:

| structure | class | console demo | design notes |
|---|---|---|---|
| dynamic array | `DynamicArrayList` | (tested) | amortised-O(1) append via doubling |
| doubly linked list | `DoublyLinkedList` | (tested) | O(1) head/tail insert/remove |
| stack | `ArrayStack` | option 17 | backs the audit-log push/pop demo |
| queue / circular queue | `CircularQueue` | option 15 | ring buffer with wrap-around trace |
| deque | `ArrayDequeCustom` | option 16 | urgent-request jumps-the-queue demo |
| priority queue | `MinHeapPriorityQueue` | option 3 | array-backed binary min-heap; drives dispatch |
| BST | `BinarySearchTree` | option 11 | insert + search-path + inorder printout |
| red-black tree | `RedBlackTree` | option 12 | left-leaning formulation; prints rotation traces |
| B-tree | `BTree` | option 13 | node-split traces on insert |
| hash table | `HashTable` | option 9 | separate chaining; reports collision statistics |
| set/map | `HashSetCustom` | option 10 | membership/lookup demo |
| disjoint set | `DisjointSet` | option 14 | union-by-rank + path compression; powers Kruskal |
| graph | `Graph` | options 4–6, 14 | adjacency list (+ matrix view); hosts BFS/DFS/Dijkstra/Prim/Kruskal |

`[TEAM: insert one screenshot per demo option here — console output of options
3–20 against the loaded dataset]`

---

## 6. Algorithm implementation section

Java implementations: `algorithms/SearchAlgorithms.java`,
`algorithms/SortAlgorithms.java`, `algorithms/GreedyResourceAssignment.java`,
`algorithms/DPRequestSelection.java`, and `structures/Graph.java`. Formal
pseudocode with inputs/outputs/preconditions for all six major operations is
in Appendix B (`pseudocode.md`). Summary of the six assessed operations:

1. **Priority dispatch** (`MinHeapPriorityQueue`) — urgency rank × derived
   priority weight forms the key; build-and-drain costs O(n log n), and the
   heap's advantage over full sorting is that the top-k requests can be had
   in O(n + k log n).
2. **Binary search** (`SearchAlgorithms.binarySearch`) — O(log n); carries an
   explicit documented precondition (sorted input) whose violation is studied
   in §7.3.
3. **BFS reachability** (`Graph`) — O(V + E); tolerant of disconnected
   graphs by simply omitting unreachable vertices.
4. **Dijkstra shortest path** (`Graph`) — O(V²) with a linear min-scan as
   implemented (acceptable at this scale; O((V+E) log V) with a binary-heap
   frontier); returns both distances and predecessor pointers so the actual
   route, not just its length, can be reconstructed.
5. **Kruskal MST** (`Graph` + `DisjointSet`) — O(E log E) dominated by the
   edge sort; cycle detection in near-linear time via union-by-rank with path
   compression.
6. **0/1-knapsack request selection** (`DPRequestSelection`) — bottom-up
   tabulation in O(n · budget), with backtracking reconstruction of the
   chosen subset.

Sorting covers four strategies (selection, insertion, merge, quicksort) so
that the empirical Section 9 comparison can show the quadratic-vs-nlogn gap
directly on real timings.

---

## 7. Correctness evidence

### 7.1 Trace tables (6 required)

Appendix C (`trace_tables.md`) contains seven fully worked traces, generated
by `generate_traces.py` against real rows from `ghana_hospital.db` so every
value is reproducible execution output rather than hand arithmetic:

1. Binary search over twelve real request ids — target found at index 7 in
   4 comparisons.
2. Insertion sort — 7 real request ids, showing shift counts per pass.
3. Merge sort — same input, six merges from singletons to the final run.
4. Quicksort — Lomuto partitions on the same input.
5. Dijkstra over an 11-node subgraph (10 UGH departments + Ridge Hospital
   Main Gate) — full distance-table snapshots, final distances, and
   reconstructed paths (e.g. Emergency Ward → Pharmacy in 2.2 min via
   Administration; Emergency Ward → Ridge Main Gate in 34.6 min).
6. Kruskal on the same subgraph — per-edge union-find accept/reject
   decisions; MST of exactly V−1 = 10 edges totalling 39.78 minutes.
7. DP knapsack — six real candidate requests against a 25-minute budget;
   full tabulation table and reconstruction choosing req-91 + req-16 for
   total value 40.

### 7.2 Proof sketches (3 required)

Appendix D (`proof_sketches.md`):

1. **Loop invariant for binary search** — "if target is present, its index
   lies in [lo, hi]"; initialisation, maintenance, and termination argued,
   including precisely why the maintenance step collapses without sortedness.
2. **Strong-induction proof for merge sort** — base case n ≤ 1, inductive
   step relying on correct sorting of both halves plus a two-pointer merge
   whose own invariant ("output prefix ≤ both remaining fronts") is stated.
3. **Exchange-argument proof for activity-selection greedy** — the "greedy
   stays ahead" lemma (finish(gᵢ) ≤ finish(oᵢ)) followed by the contradiction
   step, closing with the observation that swapping the sort key to deadline
   voids step 1 — which is exactly the failure demonstrated below.

### 7.3 Counterexamples (2 required)

Appendix F (`counterexamples.md`):

1. **Greedy failure** — earliest-deadline-first scheduling on one shared
   ambulance: job A has the earliest deadline but occupies the entire
   [0,10] window while five short back-to-back jobs have later deadlines.
   Earliest-deadline-first serves 1 job; earliest-finish-time serves 5.
   Reproducible live via menu option 18.
2. **Invalid precondition** — binary search on eight genuine request ids in
   submission order (`[119, 102, 256, 112, 296, 234, 294, 185]`) for target
   256, which is present at index 2: the search discards the wrong half and
   silently returns −1. No exception is raised — a wrong answer, which for a
   dispatch system asking "does this request exist?" is worse than a crash.
   Reproducible via menu option 7.

### 7.4 Automated tests

The full suite passes on the current tree:

```
mvn test  →  Tests run: 77, Failures: 0, Errors: 0, Skipped: 0 — BUILD SUCCESS
```

across seventeen classes — sixteen per-structure/algorithm suites (normal,
boundary, and invalid-input cases: empty structures, single elements,
duplicate keys overwriting in `HashTable`, collision counting, queue
full/empty wrap-around, disconnected-graph behaviour in `GraphTest`,
greedy-vs-DP agreement cases, etc.) plus one end-to-end acceptance test that
drives the console app through schema import, data load, and representative
menu options.

---

## 8. Performance analysis

**Method.** Menu option 20 runs `EfficiencyExperimentRunner`: all six required
experiments, at escalating input sizes, three timed repetitions each
(`System.nanoTime()` around warmed-up work), appending to
`efficiency_results.csv`. Charts are rendered from that CSV by
`docs/graphs/plot_efficiency_results.py` into `docs/graphs/*.svg`.

Official run environment (Section 9.ii) — `efficiency_results.csv` was
generated on this machine via menu option 20 immediately before submission:

| component | specification |
|---|---|
| CPU | AMD Ryzen 5 PRO 3500U w/ Radeon Vega Mobile Gfx |
| RAM | 13.9 GB |
| OS | Windows 11 Pro, 64-bit |
| JVM | OpenJDK 21.0.9 LTS |
| Build tool | Apache Maven 3.9.9 |

### 8.1 Results summary

Representative figures from `efficiency_results.csv` (medians of 3 runs;
µs unless stated):

| experiment | sizes | observed behaviour |
|---|---|---|
| linear vs binary search | n = 100…10 000 | linear grows ~85 µs (n = 1 000) → ~800 µs (n = 10 000); binary flat at ~4–12 µs (~100× gap at n = 5 000) |
| sorting (selection/insertion/merge/quick) | n = 100…10 000 | at n = 10 000: selection ≈ 405–425 **ms**, insertion ≈ 151–195 **ms**, merge ≈ 3.1–3.9 **ms**, quick ≈ 1.8–4.1 **ms** |
| hash load factor (fixed 1024-slot table) | n = 100…20 000 | α: 0.10 → 19.53; collisions: ~5 → ~18 976; put-time rises accordingly |
| BST vs red-black (sorted-order inserts) | n = 100…10 000 | BST height 99 → 9 944–9 960 (degenerate); RBT height 6 → 13; build time 590–1 165 ms vs 5.9–10.2 ms (~100×) |
| heap dispatch (insert-n + extract-all) | n = 100…20 000 | ~0.28 ms → ~9.1–14.0 ms; ×20 input ⇒ ×~6 time (n log n regime) |
| graph algorithms (BFS/DFS/Dijkstra/Kruskal) | V = 50…500 | BFS ~0.16–1.75 ms and DFS ~0.08–0.51 ms scale gently with V+E; Dijkstra ~0.4–2.9 ms and Kruskal ~0.6–5.0 ms carry heavier constants |

### 8.2 Theory vs observation, experiment by experiment

**Search.** Binary search's cost is indistinguishable across the whole size
range — doubling n adds a single extra iteration, exactly the O(log n)
prediction (log₂ 10 000 ≈ 13.3 iterations). Linear search tracks n: from
n = 1 000 to n = 10 000 its median time climbs roughly tenfold (~85 µs →
~800 µs). At n = 5 000 binary search answers in ~4–5 µs where linear needs
~410–470 µs — a ~100× gap that widens with size. The n = 100 row is
dominated by JVM warm-up noise (first repetition ran 1.37 ms, later ones
7–26 µs), which is why three repetitions are taken and medians reported.

**Sorting.** The quadratic pair behaves quadratically: selection sort grows
from ~11 ms at n = 1 000 to ~405–425 ms at n = 10 000 — about ×37 time for
×10 input, consistent with the predicted ×100 modulo cache effects. Merge
sort grows only ~×5 over the same range (~0.7 ms → ~3.5 ms), textbook n log
n. Two instructive deviations: (i) insertion sort consistently beats
selection sort despite equal O(n²) asymptotics — insertion does O(n)
comparisons on nearly-sorted prefixes and far fewer writes than selection's
swap-per-position pattern, a constant-factor effect worth reporting rather
than hiding; (ii) quicksort edges out merge sort in median time even though
both are O(n log n), because quicksort's inner partition loop is
contiguous-memory and allocation-free, whereas this merge implementation
allocates scratch arrays. No asymptotic surprises; all mismatches are
constant-factor effects explainable from the implementations.

**Hash load factor.** With the table held fixed, average collisions per op
follow the separate-chaining prediction: chain length ≈ α = n/m. Observed
collisions climb ~5 → 368 (α ≈ 1) → ~3 985 (α ≈ 4.9) → 8 976 (α ≈ 9.8) →
18 976 (α ≈ 19.5) — i.e. beyond saturation essentially every additional
insert collides, and put-time degrades in step. This quantifies *why* a real
hash table resizes (or, in our case, why `ParameterDerivation.hashTableSize()`
= 257 is sized for the actual workload rather than arbitrarily).

**BST vs balanced tree.** The most dramatic result. Inserting sorted keys
(the worst case) yields a BST that *is* a linked list: height 99 at n = 100,
9 944–9 960 at n = 10 000. The red-black tree under identical input holds
heights of 6, 9, 12, 13 at n = 100, 1 000, 5 000, 10 000 — within one or two
of ⌈log₂ n⌉ (13.3 at n = 10 000), comfortably inside the theoretical
2·log₂(n+1) red-black bound. The consequence is directly measurable:
building the degenerate BST at n = 10 000 takes 590–1 165 ms versus
5.9–10.2 ms for the red-black tree (~100×). This single experiment
justifies the entire balancing mechanism.

**Heap dispatch.** Building a heap of n requests and draining all n scales
as predicted for O(n log n): ×20 input (1 000 → 20 000) produces only ×~6
time. Practically, the dispatch demo shows the heap returning CRITICAL
requests first without ever fully sorting lower-priority traffic — the
top-k extraction property heaps exist for.

**Graph algorithms.** BFS and DFS stay cheapest and track V + E closely
(BFS ~0.16–0.29 ms at V = 50 up to ~1.3–1.8 ms at V = 500). Dijkstra's
linear-scan-min implementation shows its O(V²) core: it costs several times
BFS even though it explores the same edges, because every iteration rescans
all vertices. Kruskal is the heaviest at each size — its O(E log E) global
edge sort pays upfront even when the resulting tree touches each vertex once.
At this dataset's real scale (100 vertices, 370 edges) all four answer in
milliseconds, so the simpler-to-verify implementations were kept deliberately.

---

## 9. Database integration evidence

- Schema: `db/schema.sql` — six tables, foreign keys, three secondary
  indexes (§3.2 above).
- Seed import and in-memory load are demonstrated by menu options 1–2; a
  clean cold start prints: *"Loaded 100 locations, 370 roads, 300 requests,
  30 resources. Graph built."*
- Live accumulation: `algorithm_runs` gains a row per timed sort (option 8)
  and per experiment-suite run (option 20); `audit_events` gains rows from
  the stack-based undo demo (option 17) — the persistence layer is exercised
  by normal operation, not just at startup.
- Sample queries used for verification (also embedded in the acceptance test):
  row counts per table, a join of requests to location names, and spot checks
  of `algorithm_runs` after option 20.

`[TEAM: insert screenshots here — sqlite3 ghana_hospital.db "SELECT * FROM
locations LIMIT 5;" and equivalents for the other five tables, plus console
captures of menu options 1 and 2 completing]`

---

## 10. Responsible algorithm selection

This section states, for each major decision, why the chosen algorithm fits
the problem — and where tempting alternatives would fail.

- **Greedy is provably right for activity-selection assignment** (exchange
  argument, §7.2.3) but **provably wrong as an "urgency-first" dispatch rule**
  when urgency means deadline alone: §7.3.1 shows earliest-deadline-first
  serving five times fewer requests than optimal by ignoring how long a job
  occupies the shared ambulance. We therefore use greedy only where its proof
  applies, and use the heap for ordering instead of pretending a greedy rule
  optimises throughput.
- **Binary search is adopted only behind an enforced precondition** — callers
  must pass freshly sorted data (the sort demos do exactly this). §7.3.2
  shows the failure mode is silent misinformation, not an error; documenting
  and enforcing the precondition is the fix, not defensive checks inside the
  O(log n) inner loop.
- **Unbalanced BSTs are rejected for uncontrolled insert orders.** Our own
  Section 8 data makes the abstract warning concrete: sorted inserts produce
  height ≈ n (9 950 at n = 10 000) and a ~100× slowdown versus the red-black
  tree. Wherever insert order isn't guaranteed random, we ship the red-black
  tree (in-memory ordered indexing) or the B-tree (the disk-page-friendly
  analogue that databases like SQLite actually use).
- **Brute force was considered and rejected** for budgeted request selection:
  2ⁿ subsets is intractable past a few dozen requests, while the DP
  formulation solves the full instance in O(n · budget) — measured, not
  theorised, by menu option 19 on the real 300-request dataset.
- **Dijkstra's simple O(V²) variant was kept deliberately** at this scale
  (V = 100): the binary-heap frontier version's constant factors and extra
  complexity buy nothing measurable here, and the simple version is easier
  to defend orally line-by-line. The Section 8 graph timings record the
  crossover pressure that would justify switching as V grows.

---

## 11. Individual contribution statements and oral-defense preparation

Per-member contribution statement (Section 2.v of the brief — each member
states the ONE data structure and the ONE algorithm they personally
implemented/tested and can defend):

| # | Member | Index no. | Structure defended | Algorithm defended | What I'd change with more time |
|---|---|---|---|---|---|
| 1 | Precious Bassey Inyang | 22059210 | `[fill in]` | `[fill in]` | `[fill in]` |
| 2 | Priscilla Kwakyewaa | 22150993 | `[fill in]` | `[fill in]` | `[fill in]` |
| 3 | Edmund Mulcahy Amonoo | 22057575 | `[fill in]` | `[fill in]` | `[fill in]` |
| 4 | Asante Kojo Boakye | 22065837 | `[fill in]` | `[fill in]` | `[fill in]` |
| 5 | Kwabena Ntiamoah Boateng | 22046431 | `[fill in]` | `[fill in]` | `[fill in]` |
| 6 | Owusu Sarfo Kwadwo | 22078485 | `[fill in]` | `[fill in]` | `[fill in]` |
| 7 | Daniel Okuluri Tetteh | 22297380 | `[fill in]` | `[fill in]` | `[fill in]` |
| 8 | Daniel Kubi Adjetey Junior | 22059983 | `[fill in]` | `[fill in]` | `[fill in]` |
| 9 | Joel Asante | 22407542 | `[fill in]` | `[fill in]` | `[fill in]` |
| 10 | Daniel Ofori Boateng | 22408351 | `[fill in]` | `[fill in]` | `[fill in]` |
| 11 | Prince Oppong | 22375768 | `[fill in]` | `[fill in]` | `[fill in]` |
| 12 | Christian Kwabena Asante | 22387357 | `[fill in]` | `[fill in]` | `[fill in]` |
| 13 | Evans Osei | 22411279 | `[fill in]` | `[fill in]` | `[fill in]` |
| 14 | Gillis Abraham Nii Akwei Mclutason | 22387934 | `[fill in]` | `[fill in]` | `[fill in]` |
| 15 | Bryan Acquah Annor | 22368027 | `[fill in]` | `[fill in]` | `[fill in]` |

Oral-defense readiness checklist (examiners typically ask you to modify a
priority rule, add a location, change the hash-table size, or re-run an
algorithm live):

- [ ] Every member can run their assigned menu option from a cold `mvn exec:java` unaided.
- [ ] Every member can explain what changes if `ParameterDerivation.hashTableSize()`
      (currently 257) is altered and option 9 re-runs — collision counts and
      chain lengths move with m, per the §8 hash analysis.
- [ ] Every member can add one location row to `db/seed/locations.csv`,
      re-import (option 1), rebuild (option 2), and show the new node
      appearing in a BFS/Dijkstra demo (options 4–5).
- [ ] Every member can narrate one rotation from `redblack_rotations.md` and
      one split from `btree_trace.md` at the whiteboard.
- [ ] Demo video recorded and linked: `[TEAM: link/attachment]`.

---

## 12. References and appendices

**References**

1. Cormen, T. H., Leiserson, C. E., Rivest, R. L., & Stein, C. — *Introduction
   to Algorithms* (MIT Press): loop-invariant method, greedy exchange
   arguments, Dijkstra/Kruskal analyses, red-black properties.
2. Sedgewick, R., & Wayne, K. — *Algorithms* (Princeton University Press):
   source of the left-leaning red-black tree formulation in `RedBlackTree.java`.
3. SQLite documentation (sqlite.org): file-format and JDBC usage.
4. Coordinate sources per facility: see the citation table in Appendix E
   (`evidence_note.md`) — Wikipedia/Wikidata, latitude.to, distancesfrom.com,
   mapcarta, Google-Maps-derived DMS conversions; confidence honestly graded.

**Appendices (all in `docs/` unless noted)**

- A. `data_dictionary.md` — field-by-field dictionary, six tables.
- B. `pseudocode.md` — formal specs + pseudocode of the six major operations.
- C. `trace_tables.md` — seven worked traces on real data.
- D. `proof_sketches.md` — invariant, induction, and exchange-argument proofs.
- E. `evidence_note.md` — dataset sourcing, real-vs-synthetic split,
  reproducibility, no-personal-data statement.
- F. `counterexamples.md` — greedy failure + invalid-precondition case.
- G. `redblack_rotations.md`, `btree_trace.md` — balancing-operation traces.
- H. `dev_log.md` — development history.
- I. `../efficiency_results.csv` and `graphs/*.svg` — raw measurements and charts.
- J. `ai_disclosure.md` — AI-assistance disclosure.
