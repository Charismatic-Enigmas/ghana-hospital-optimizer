# Pseudocode and Problem Specifications

Six major operations (minimum required: 5), each with inputs, outputs,
assumptions/constraints, and pseudocode. Matches the live implementations in
`src/main/java/com/ghdsa/`.

## 1. Priority dispatch (heap-based)

**Input**: a list of PENDING service requests, each with an urgency level.
**Output**: requests in dispatch order (most urgent first).
**Assumptions**: urgency in {CRITICAL, HIGH, MEDIUM, LOW}; ties broken
arbitrarily (heap is not required to be stable).
**Constraint**: must run faster than sorting the whole list when only the
top few requests are needed (this is the heap's whole reason for existing
over e.g. sorting everything).

```
DISPATCH-ORDER(requests):
    heap <- empty MinHeapPriorityQueue
    for each r in requests where r.status == PENDING:
        score <- urgencyRank(r.urgency) * priorityWeight   # priorityWeight from ParameterDerivation
        heap.insert((r.id, score))
    order <- []
    while heap not empty:
        order.append(heap.extractMin())
    return order
```
Complexity: O(n log n) to build and drain the heap (n insertions + n
extractions, each O(log n)).

## 2. Binary search

**Input**: array `arr` sorted ascending, `target` value.
**Output**: index of `target` in `arr`, or -1 if absent.
**Precondition** (load-bearing, see `counterexamples.md` §2): `arr` MUST
already be sorted ascending. Violating this precondition does not raise an
error — it silently returns wrong results.

```
BINARY-SEARCH(arr, target):
    lo, hi <- 0, length(arr) - 1
    while lo <= hi:
        mid <- floor((lo + hi) / 2)
        if arr[mid] == target: return mid
        else if target < arr[mid]: hi <- mid - 1
        else: lo <- mid + 1
    return -1
```
Complexity: O(log n). See `proof_sketches.md` §1 for the loop-invariant
correctness proof.

## 3. BFS reachability

**Input**: graph `G` (adjacency list), start vertex `s`.
**Output**: the set of vertices reachable from `s`, in BFS visit order.
**Assumptions**: `G` may be disconnected — unreachable vertices are simply
absent from the output (an explicit edge case, see `docs/report_draft.md`
§7 edge cases).

```
BFS(G, s):
    visited <- {s}
    queue <- [s]
    order <- []
    while queue not empty:
        v <- queue.dequeue()
        order.append(v)
        for each neighbour u of v in G:
            if u not in visited:
                visited.add(u)
                queue.enqueue(u)
    return order
```
Complexity: O(V + E).

## 4. Dijkstra shortest path

**Input**: graph `G` with non-negative edge weights, start vertex `s`.
**Output**: `dist[v]` = shortest distance from `s` to every reachable `v`,
and `pred[v]` = the predecessor on that shortest path (used to reconstruct
the actual route, not just its length).
**Precondition**: all edge weights (`travel_time_min`) must be non-negative
— true by construction in this dataset (travel times can't be negative).

```
DIJKSTRA(G, s):
    dist[v] <- infinity for all v; dist[s] <- 0
    pred[v] <- null for all v
    visited <- {}
    while visited != all vertices:
        u <- the unvisited vertex with smallest dist[u]
        if no such u (dist = infinity for all remaining): break   # disconnected
        visited.add(u)
        for each (v, weight) in neighbours(u):
            if v not in visited and dist[u] + weight < dist[v]:
                dist[v] <- dist[u] + weight
                pred[v] <- u
    return dist, pred
```
Complexity: O(V^2) with a linear scan for the minimum (as implemented here —
acceptable at this dataset's scale); O((V+E) log V) with a binary-heap
priority queue. See `trace_tables.md` §4 for a full worked distance table.

## 5. Kruskal minimum spanning tree

**Input**: graph `G`, connected (or per-component minimum spanning forest if
not).
**Output**: a set of `V-1` edges forming a minimum-total-weight spanning
tree (or `components-1` fewer edges per extra component if disconnected).
**Assumptions**: uses `DisjointSet` (union-by-rank + path compression) to
detect cycles in O(alpha(V)) amortised per operation.

```
KRUSKAL(G):
    ds <- new DisjointSet(V)
    mst <- []
    edges <- all edges of G, sorted ascending by weight
    for each (u, v, weight) in edges:
        if ds.union(u, v):        # union() returns false if u,v already connected (would form a cycle)
            mst.append((u, v, weight))
        if length(mst) == V - 1: break
    return mst
```
Complexity: O(E log E) for the sort, dominating the near-linear union-find
work. See `trace_tables.md` §5 for the union-find connectivity trace.

## 6. Dynamic programming: 0/1 knapsack request selection

**Input**: a list of candidate requests, each with `(cost, value)`; a total
`budget`.
**Output**: the subset of requests maximising total value with total cost
`<= budget`, plus the reconstructed list of which requests were chosen.
**Assumptions**: each request is taken 0 or 1 times (can't partially serve a
request); costs and budget are non-negative integers (minutes).

```
KNAPSACK(requests, budget):
    n <- length(requests)
    table[0..n][0..budget] <- 0
    for i from 1 to n:
        (cost, value) <- requests[i-1]
        for w from 0 to budget:
            table[i][w] <- table[i-1][w]                       # don't take request i
            if cost <= w:
                table[i][w] <- max(table[i][w], table[i-1][w-cost] + value)

    # reconstruction: walk back through the table
    chosen <- []
    w <- budget
    for i from n downto 1:
        if table[i][w] != table[i-1][w]:
            chosen.append(requests[i-1])
            w <- w - requests[i-1].cost
    reverse(chosen)
    return table[n][budget], chosen
```
Complexity: O(n * budget) time and space for tabulation, O(n) for
reconstruction. See `trace_tables.md` §6 for a full worked table.
