# Development Log

Weekly progress, challenges, and decisions (Section 15.iv of the brief).
**The team needs to fill in Weeks 1-3 below** — this file only has visibility
into the session logged in "Week 4 (partial)"; earlier history should be
backfilled from memory, commit history if the repo is later put under git,
or chat/meeting notes.

## Week 1: _______________

- Local context and dataset plan (M1)
- _(team: fill in — problem statement decisions, initial data dictionary
  draft, schema draft, who worked on what)_

## Week 2: _______________

- Data-structure library (M2/M3)
- _(team: fill in — which structures were built, what tests were written,
  any design decisions on e.g. LLRB vs classic red-black tree)_

## Week 3: _______________

- Searching/sorting, graph, optimisation engines (M4/M7/M8)
- _(team: fill in)_

## Week 4 (partial — this session's work, 2026-08-18)

Starting state: a substantial existing codebase (all 12 required structures,
search/sort algorithms, greedy + DP, database schema/seed data, 72 passing
tests) but with three concrete gaps closed this session:

1. **Data correctness**: the 10 facility base coordinates in
   `generate_data.py` were approximate/unverified ("real-ish" per the
   original code comment); looked each one up against public geocoding
   sources and replaced them with cited, confidence-labelled values (see
   `evidence_note.md`). Also found and fixed a jitter radius bug (~1.1km,
   large enough to scatter one hospital's own departments outside its real
   grounds) — tightened to ~250m.
2. **Console menu coverage**: the app only exposed 6 of ~17
   structures/algorithms as live demos before this session (most of the
   library was implemented and tested but never wired to anything runnable
   without editing source — a gap against Section 8.iv). Expanded `Main.java`
   from an 8-option to a 20-option menu covering every structure/algorithm.
   Also added `HashSetCustom` (the "custom set/map" structure Section 6
   explicitly requires, which didn't exist yet).
3. **Two real bugs found while building the demos** (not just wiring):
   - `GreedyResourceAssignment`'s documented 3-job counterexample didn't
     actually diverge under the real `>=` boundary check in the code —
     replaced with a 6-job dataset that genuinely demonstrates the failure
     (5 jobs served optimally vs 1 via earliest-deadline-first).
   - The search demo's "unsorted array" precondition-violation example was
     accidentally still sorted, because it was loaded straight from the DB
     in `request_id` order — fixed by explicitly shuffling with a seeded RNG.
4. Ran the full Section 9 efficiency lab (`efficiency_results.csv`) and
   generated the 6 required graphs (dependency-free SVG renderer, since the
   environment's C: drive had ~0 bytes free for a `pip install matplotlib`).
5. Generated `trace_tables.md` (6 required traces), `proof_sketches.md` (3
   required), `counterexamples.md` (2 required), `data_dictionary.md`,
   `pseudocode.md` (6 operations), `redblack_rotations.md`, `btree_trace.md`,
   and this evidence note — all computed/verified programmatically against
   real data or a faithful simulation of the actual Java logic, not
   hand-derived.

Challenge worth recording: the environment's C: drive was almost completely
full (0.01 GB free out of 276 GB) partway through this session, which broke
a `pip install matplotlib` silently at first (looked like it succeeded, then
failed on actual import), and later broke `mvn test` outright (JVM couldn't
allocate its page file). Worth the team checking disk space on whatever
machine runs the final performance experiments (Section 9.ii requires
recording the machine spec anyway).

## Week 4, continued: real bug found via manual testing (2026-08-18)

After the above, manually ran the console app and tried menu option 4
(Dijkstra route) from location 2 to location 7 — two University of Ghana
Hospital departments on the same real campus, ~260m apart. Got back a
35-minute, 6-hop path through Dodowa, Achimota, Korle-Bu, and Nyaho — real
hospitals scattered tens of km across Accra. Root cause: `gen_roads()` in
`generate_data.py` assigned `distance_km`/`travel_time_min` as pure
independent random numbers, completely disconnected from the real
coordinates Phase 0 had already verified — so Dijkstra was correctly finding
the cheapest path through genuinely nonsensical edge weights. Same-facility
departments mostly had no direct edge at all (random neighbour sampling
across all 100 locations, no facility-awareness).

Fixed by rewriting `gen_roads()` to derive `distance_km` from real haversine
distance between each pair's actual coordinates, `travel_time_min` from that
distance at an assumed 20 km/h, and connectivity via minimum spanning trees
(guaranteed-connected, both within each facility and across the 10
facilities' Main Gates) instead of unweighted random sampling. Regenerated
`roads.csv` (370 edges now, was 397), reimported, re-verified manually
(2->7 now returns 1.5 min via a 2-hop intra-campus path; UGH->Korle-Bu now
returns a plausible ~35 min cross-city referral). 77/77 tests still pass.
Regenerated `trace_tables.md` §4-5 (the Dijkstra/Kruskal examples used real
road data) and updated `evidence_note.md`'s description of the method — see
those files for detail. Also had to fix `docs/generate_traces.py` itself:
it had hardcoded which 7 nodes to use for the example (found once via BFS on
the old graph), which silently produced an empty/disconnected example once
the graph topology changed — replaced with dynamic BFS discovery at
generation time so it can't go stale again.

Worth noting for the report (Section 8/10): this is a good concrete example
of validating a system empirically rather than trusting that "the algorithm
ran without errors" means "the algorithm is doing something sensible" —
Dijkstra was implemented correctly the whole time; the bug was entirely in
the data feeding it.

**Still open** (see `docs/report_draft.md` for the running list): report
assembly, individual contribution statements, demo video, oral defense
prep, cover sheet team-name/signature fields.
