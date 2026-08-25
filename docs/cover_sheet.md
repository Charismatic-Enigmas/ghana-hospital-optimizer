# Submission Checklist and Cover Sheet (working copy)

Transcription of `Joint_DSA_Project_Checklist_Cover_Sheet.pdf` with every
field that's determinable from the repository filled in. Transfer these
into the actual PDF (or have the team fill the PDF directly using this as
reference) — the fields marked `[TEAM: ...]` need a human.

| Field | Value |
|---|---|
| Team name | Charismatic Enigmas |
| Selected Ghana context | Hospital/clinic operations |
| Organisation/problem modelled | Service dispatch/routing across a network of 10 real Greater Accra health facilities (see `evidence_note.md`) |
| Database used | SQLite (via `org.xerial:sqlite-jdbc`) |
| Programming language/version | Java 17 (`maven.compiler.release=17` in `pom.xml`) |
| Total records in dataset | 830 (100 locations + 370 roads + 300 service_requests + 30 resources + 30 algorithm_runs seed rows; `algorithm_runs` and `audit_events` also grow live as the console demos run) |
| Repository or submitted ZIP name | `[TEAM: fill in]` |

## Checklist

| Requirement | Tick | Evidence location |
|---|---|---|
| Local dataset with data dictionary | done | `docs/data_dictionary.md`, `docs/evidence_note.md` |
| Database schema and seed data | done | `db/schema.sql`, `db/seed/*.csv` |
| Custom data structures implemented | done | `src/main/java/com/ghdsa/structures/` (12 structures + tests) |
| Searching and sorting algorithms | done | `src/main/java/com/ghdsa/algorithms/SearchAlgorithms.java`, `SortAlgorithms.java` |
| Graph algorithms implemented | done | `src/main/java/com/ghdsa/structures/Graph.java` (BFS/DFS/Dijkstra/Prim/Kruskal) |
| Greedy and DP algorithms | done | `GreedyResourceAssignment.java`, `DPRequestSelection.java` |
| Correctness tests and trace tables | done | `docs/trace_tables.md`, `docs/proof_sketches.md`, `docs/counterexamples.md`, unit test suite |
| Performance CSV and graphs | done | `efficiency_results.csv`, `docs/graphs/*.svg` |
| Technical report | draft done, needs team finishing | `docs/report_draft.md` — screenshots, member names, individual contributions, machine spec still needed |
| Demo video / oral defense prepared | **not started** | human-only, see `docs/report_draft.md` §11 readiness checklist |
