# Proof Sketches

Three required proof sketches (Section 10 of the brief): a loop invariant for
search/sort, an induction/recursion proof, and a greedy-or-DP correctness
argument.

## 1. Loop invariant: binary search (`SearchAlgorithms.binarySearch`)

```java
int lo = 0, hi = arr.length - 1;
while (lo <= hi) {
    int mid = (lo + hi) / 2;
    if (arr[mid] == target) return mid;
    else if (target < arr[mid]) hi = mid - 1;
    else lo = mid + 1;
}
return -1;
```

**Invariant** (holds at the top of every loop iteration, given `arr` sorted
ascending and `target` actually present in `arr`):

> If `target` is in `arr`, its index is within the closed range `[lo, hi]`.

**Initialisation**: before the first iteration, `lo = 0`, `hi = arr.length-1`
— the whole array. If `target` is in `arr` at all, its index is trivially in
`[0, arr.length-1]`. Invariant holds.

**Maintenance**: assume the invariant holds at the start of an iteration
(target's index, if it exists, is in `[lo, hi]`). Because `arr` is sorted:
- if `arr[mid] == target`, we return `mid` immediately — done, correctly.
- if `target < arr[mid]`, every index `> mid` holds a value `>= arr[mid] >
  target` (sortedness), so target's index (if it exists) must be in
  `[lo, mid-1]`. Setting `hi = mid - 1` preserves the invariant.
- if `target > arr[mid]`, symmetric argument: target's index must be in
  `[mid+1, hi]`. Setting `lo = mid + 1` preserves the invariant.

**Termination**: `hi - lo` strictly decreases every iteration (at least one
of `lo`/`hi` moves past `mid`), so the loop terminates. When it terminates via
`lo > hi`, the invariant says target's index would have to be in an empty
range — so target is not in `arr`, and returning `-1` is correct.

This is exactly why the precondition matters (`counterexamples.md` §2): the
maintenance step relies entirely on `arr` being sorted. Break that
assumption and the invariant is never established in the first place.

## 2. Induction / recursion proof: merge sort (`SortAlgorithms.mergeSort`)

**Claim**: `mergeSort(arr)` produces a sorted permutation of `arr`, for any
`arr` of length `n >= 0`.

**Proof by strong induction on `n = arr.length`.**

**Base case** (`n <= 1`): an array of length 0 or 1 is trivially sorted;
`mergeSort` returns it unchanged. Claim holds.

**Inductive step**: assume the claim holds for all arrays of length `< n`
(strong induction hypothesis). For an array of length `n > 1`, `mergeSort`:
1. splits `arr` into `left = arr[0..n/2)` and `right = arr[n/2..n)`, both of
   length `< n`;
2. recursively sorts each half — by the induction hypothesis, `mergeSort`
   correctly sorts both, since both have length `< n`;
3. merges the two now-sorted halves via `merge()`, which repeatedly takes
   the smaller of the two current fronts. Because both inputs to `merge` are
   already sorted (step 2), the standard two-pointer merge produces a fully
   sorted, length-preserving interleaving of `left` and `right` (this is
   itself provable by a loop invariant: at every step, everything already
   appended to the output is `<=` both remaining fronts).

So the result is a sorted permutation of `arr`. By strong induction, the
claim holds for all `n >= 0`. QED.

This mirrors the trace in `trace_tables.md` §3: each merge combines two
already-sorted runs, and the recursion bottoms out at single-element
(trivially sorted) arrays.

## 3. Greedy correctness: activity-selection (`GreedyResourceAssignment.selectMaxNonOverlapping`)

**Claim**: sorting jobs by earliest finish time and greedily taking every job
that doesn't overlap the last one taken produces a **maximum-size** set of
non-overlapping jobs.

**Proof sketch (exchange argument)**: let `G = {g1, g2, ..., gk}` be the
greedy solution (in finish-time order) and let `O = {o1, o2, ..., om}` be
some optimal solution (also in finish-time order), with `m >= k` assumed for
contradiction.

1. **Greedy stays ahead**: by induction on `i`, `finish(gi) <= finish(oi)`
   for every `i` up to `min(k,m)`. Base case `i=1`: `g1` is chosen as the
   job with the single earliest finish time among *all* jobs, so
   `finish(g1) <= finish(o1)`. Inductive step: given `finish(gi) <=
   finish(oi)`, `o_{i+1}` starts after `o_i` finishes, hence after `g_i`
   finishes too (since `finish(gi) <= finish(oi)`) — so `o_{i+1}` was a
   valid candidate when the greedy algorithm picked `g_{i+1}`, meaning the
   greedy choice `g_{i+1}` has `finish(g_{i+1}) <= finish(o_{i+1})` (greedy
   always takes the earliest-finishing valid candidate).
2. If `m > k`, then `o_{k+1}` exists and, by the "greedy stays ahead"
   result, starts after `g_k` finishes — so it was a valid, available
   candidate for the greedy algorithm after picking `g_k`. But the greedy
   loop only stops when no more jobs are available, so it would have picked
   it too. Contradiction: `G` would have `k+1` jobs, not `k`.

So `m <= k`; combined with `G` being a valid non-overlapping set (`m >= k`
by optimality of `O`), `m = k` — greedy's solution is optimal in size.

**Where this breaks**: the proof leans entirely on sorting by *finish time*.
`earliestDeadlineFirstGreedy` swaps the sort key to *deadline*, which
invalidates step 1 ("greedy stays ahead") — a job with an early deadline can
still have a very late finish time, so the exchange argument no longer
applies. That gap is exactly what `counterexamples.md` §1 demonstrates
concretely (5 jobs served vs 1).
