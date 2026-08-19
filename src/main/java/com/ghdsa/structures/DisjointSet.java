package com.ghdsa.structures;

/** Union-find with union-by-rank and path compression. Section 6 row 11 — powers Kruskal's MST. */
public class DisjointSet {
    private final int[] parent;
    private final int[] rank;

    // trace of union() calls: [a, b, unionedSuccessfully(1/0)] — for your Kruskal connectivity trace
    public java.util.List<int[]> unionTrace = new java.util.ArrayList<>();

    public DisjointSet(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;
    }

    public int find(int x) {
        if (parent[x] != x) parent[x] = find(parent[x]); // path compression
        return parent[x];
    }

    public boolean union(int a, int b) {
        int rootA = find(a), rootB = find(b);
        if (rootA == rootB) {
            unionTrace.add(new int[]{a, b, 0});
            return false;
        }
        if (rank[rootA] < rank[rootB]) { int t = rootA; rootA = rootB; rootB = t; }
        parent[rootB] = rootA;
        if (rank[rootA] == rank[rootB]) rank[rootA]++;
        unionTrace.add(new int[]{a, b, 1});
        return true;
    }
}
