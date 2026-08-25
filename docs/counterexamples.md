# Counterexamples

Two required counterexamples (Section 10 of the brief): one greedy failure, one
invalid-precondition case. Both are reproducible live from the console app
(menu options 18 and 7 respectively) and both use real data from
`ghana_hospital.db`.

## 1. Greedy failure: earliest-deadline-first is not optimal for resource assignment

`GreedyResourceAssignment` (`src/main/java/com/ghdsa/algorithms/GreedyResourceAssignment.java`)
implements two greedy strategies for assigning a single shared ambulance to
competing jobs:

- `selectMaxNonOverlapping` — classic activity-selection: sort by **earliest
  finish time**, take every job that doesn't overlap the last one taken. This
  is provably optimal for "maximise the count of non-overlapping jobs on one
  resource" (see `proof_sketches.md` §3).
- `earliestDeadlineFirstGreedy` — sorts by **earliest deadline** instead, then
  applies the same non-overlap check. This looks like a reasonable
  "urgency-first" rule for a hospital dispatch system, but it is **not**
  optimal.

### Worked counterexample (menu option 18)

Six ambulance jobs compete for one ambulance:

| job | window | deadline |
|---|---|---|
| A | [0, 10] | 10 |
| B | [0, 2] | 20 |
| C | [2, 4] | 20 |
| D | [4, 6] | 20 |
| E | [6, 8] | 20 |
| F | [8, 10] | 20 |

Job A has the **earliest deadline** but occupies the entire [0,10] window.
Jobs B-F are short and back-to-back but individually have later deadlines.

- `selectMaxNonOverlapping` (sorts by finish time: B,C,D,E,F,A): serves
  **B, C, D, E, F — 5 jobs**.
- `earliestDeadlineFirstGreedy` (sorts by deadline: A first, since it's the
  only job with deadline 10): picks A, which then blocks the resource for
  the whole window — every one of B-F fails the `start >= lastEnd` check.
  Serves **A only — 1 job**.

**Why greedy fails here**: greedily committing to the "most urgent by
deadline" job ignores how long that job occupies the shared resource. A
short-sighted urgency rule can lock in one job at the cost of five others,
which is exactly the risk this counterexample is meant to make visible
before anyone is tempted to hard-code "earliest deadline first" into a real
dispatch system.

## 2. Invalid precondition: binary search requires a sorted array

`SearchAlgorithms.binarySearch` (`src/main/java/com/ghdsa/algorithms/SearchAlgorithms.java`)
assumes its input array is sorted ascending. It does not check this
precondition — violating it silently produces wrong answers instead of an
error, which is the point of this counterexample.

### Worked example (menu option 7), real data from `service_requests`

Eight real request ids, in the order they were actually submitted
(`ORDER BY time_submitted`, i.e. genuinely NOT sorted by id):

```
[119, 102, 256, 112, 296, 234, 294, 185]
```

Target: request **256** — which *is* present, at index 2.

Running `binarySearch` on this unsorted array:

| step | lo | hi | mid | arr[mid] | decision |
|---|---|---|---|---|---|
| 1 | 0 | 7 | 3 | 112 | 256 > 112 -> search right half |
| 2 | 4 | 7 | 5 | 234 | 256 > 234 -> search right half |
| 3 | 6 | 7 | 6 | 294 | 256 < 294 -> search left half |

`lo` (7) now exceeds `hi` (6): loop terminates, **result = -1 (not found)** —
even though 256 is sitting right there at index 2.

**Why this happens**: binary search only ever looks at the midpoint and
discards half the array based on the assumption that everything on the
"wrong" side of that midpoint is also on the wrong side of the target. On an
unsorted array that assumption is false, so it can discard the half that
actually contains the target. The failure is silent — no exception, no
warning, just a wrong answer — which is worse than a crash for a system
deciding whether request 256 exists and needs dispatching.

**The fix** is not a code change to `binarySearch` itself; it's an
**enforced precondition**: only ever call it on an array that has just been
sorted (see `SortAlgorithms`), and treat "is this array sorted?" as part of
the contract, documented in `pseudocode.md` §2.
