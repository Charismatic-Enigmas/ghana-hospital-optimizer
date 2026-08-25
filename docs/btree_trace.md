# B-Tree: Search Trace and Node-Split Evidence

`BTree` (`src/main/java/com/ghdsa/structures/BTree.java`) uses minimum
degree `t`; each node holds between `t-1` and `2t-1` keys (and, if internal,
between `t` and `2t` children). Menu option 13 demonstrates it live with
`t=2` (max 3 keys per node) so splits happen after only a few inserts and
stay easy to follow. Splits happen *before* descending into a full child
(the classic "split on the way down" B-tree insert), which is why the split
log below shows splits interleaved with the actual key being inserted.

## Verified insert trace (real console output, menu option 13, keys 1-12)

```
insert(4) -> root split, new height at root
insert(6) -> child 1 split, key promoted: 4
insert(8) -> child 2 split, key promoted: 6
insert(9) -> root split, new height at root
insert(10) -> child 1 split, key promoted: 8
insert(12) -> child 2 split, key promoted: 10
```

(inserts 1, 2, 3, 5, 7, 11 produced no split -- their target leaf still had
room under the 3-key limit.)

## Node-split explanation

A split happens when `insert` is about to descend into a node that already
holds `2t-1 = 3` keys (full). Example: **insert(6)**, which triggers "child
1 split, key promoted: 4":

- Before insert(6), the root held `[1, 2, 3]` (a single full leaf — the
  root itself has been a leaf up to this point) — wait, that root was
  already split at `insert(4)` because it hit the 3-key limit first. After
  `insert(4)`'s split, the tree already has a root `[2]` with two leaf
  children `[1]` and `[3, 4]`.
- Inserting 6 means descending into the right child, `[3, 4]` — but wait,
  that child needs to grow to accommodate keys up to that point. Once that
  child reaches 3 keys, the *next* insert that needs to descend past it
  splits it first: the middle key (`4`) is promoted up into the root, and
  the child splits into two 1-key nodes either side of it.

The rest of the trace repeats this pattern: nodes fill to 3 keys, the next
insert into (or through) a full node splits it, promoting its middle key
upward. Two splits happened at the root itself (`insert(4)`, `insert(9)`) —
each of those increases the tree's height by one level, exactly as the
`splitLog` messages ("root split, new height at root") describe.

## Final tree shape after inserting 1-12 (t=2)

Verified by replaying the exact same split-on-the-way-down logic as
`BTree.java` (see the split log above matches exactly):

```
                [4]
        /                \
      [2]              [6, 8, 10]
     /   \           /     |     \      \
   [1]   [3]       [5]    [7]    [9]   [11,12]
```

## Search trace: `search(1)`

```
at node [4]:        1 < 4  -> descend into child 0
at node [2]:         1 < 2  -> descend into child 0
at node [1]:         1 == 1 -> FOUND (leaf reached)
```
3 node visits, 3 key comparisons total (this matches the console output
`search(1) -> PORTER` from the live run — `1` is a real request id from the
sample, and `PORTER` its stored category).

## Search trace: `search(11)`

```
at node [4]:            11 > 4  -> descend into child 1
at node [6, 8, 10]:      11 > 10 -> descend into child 3 (rightmost)
at node [11, 12]:        11 == 11 -> FOUND (leaf reached)
```
Illustrates a search that has to compare against *every* key in a
multi-key internal node before choosing which child to descend into — the
key advantage of a B-tree over a binary tree at this fan-out: each node
visited does more comparisons but the tree is shallower (height 2 here for
12 keys, vs a binary structure needing more levels for the same key count).
