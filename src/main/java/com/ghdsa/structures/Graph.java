package com.ghdsa.structures;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Graph with adjacency list AND adjacency matrix representations.
 * powers M7. Vertices are 0..numVertices-1; map your
 * location_id values to a dense 0-based index before using this class.
 */
public class Graph {
    private final int numVertices;
    private final List<List<double[]>> adjList;     // adjacency list: [neighbour, weight]
    private final double[][] adjacencyMatrix;        // adjacency matrix representation

    public Graph(int numVertices) {
        this.numVertices = numVertices;
        adjList = new ArrayList<>();
        for (int i = 0; i < numVertices; i++) adjList.add(new ArrayList<>());
        adjacencyMatrix = new double[numVertices][numVertices];
        for (double[] row : adjacencyMatrix) Arrays.fill(row, Double.POSITIVE_INFINITY);
        for (int i = 0; i < numVertices; i++) adjacencyMatrix[i][i] = 0;
    }

    /** Undirected by default (ambulance roads are two-way); pass directed=true for one-way. */
    public void addEdge(int from, int to, double weight) { addEdge(from, to, weight, false); }

    public void addEdge(int from, int to, double weight, boolean directed) {
        adjList.get(from).add(new double[]{to, weight});
        adjacencyMatrix[from][to] = weight;
        if (!directed) {
            adjList.get(to).add(new double[]{from, weight});
            adjacencyMatrix[to][from] = weight;
        }
    }

    public List<double[]> neighbours(int v) { return adjList.get(v); }
    public int size() { return numVertices; }

    public List<Integer> bfs(int start) {
        List<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[numVertices];
        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(start);
        visited[start] = true;
        while (!queue.isEmpty()) {
            int v = queue.poll();
            order.add(v);
            for (double[] edge : adjList.get(v)) {
                int neighbour = (int) edge[0];
                if (!visited[neighbour]) { visited[neighbour] = true; queue.add(neighbour); }
            }
        }
        return order;
    }

    public List<Integer> dfs(int start) {
        List<Integer> order = new ArrayList<>();
        boolean[] visited = new boolean[numVertices];
        dfsVisit(start, visited, order);
        return order;
    }

    private void dfsVisit(int v, boolean[] visited, List<Integer> order) {
        visited[v] = true;
        order.add(v);
        for (double[] edge : adjList.get(v)) {
            int neighbour = (int) edge[0];
            if (!visited[neighbour]) dfsVisit(neighbour, visited, order);
        }
    }

    /** Dijkstra shortest paths from start. Returns distance map; use predecessors for the path itself. */
    public Map<Integer, Double> dijkstra(int start) { return dijkstra(start, new HashMap<>()); }

    public Map<Integer, Double> dijkstra(int start, Map<Integer, Integer> predecessorsOut) {
        double[] dist = new double[numVertices];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        dist[start] = 0;
        boolean[] visited = new boolean[numVertices];

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> dist[a[0]]));
        pq.add(new int[]{start});

        while (!pq.isEmpty()) {
            int u = pq.poll()[0];
            if (visited[u]) continue;
            visited[u] = true;
            for (double[] edge : adjList.get(u)) {
                int v = (int) edge[0];
                double weight = edge[1];
                if (!visited[v] && dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    predecessorsOut.put(v, u);
                    pq.add(new int[]{v});
                }
            }
        }
        Map<Integer, Double> result = new HashMap<>();
        for (int i = 0; i < numVertices; i++) if (dist[i] != Double.POSITIVE_INFINITY) result.put(i, dist[i]);
        return result;
    }

    /** Prim's MST starting from vertex 0. Returns list of [from, to, weight-as-int-scaled] edges. */
    public List<double[]> primMST() {
        List<double[]> mstEdges = new ArrayList<>();
        boolean[] inMst = new boolean[numVertices];
        PriorityQueue<double[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[2]));
        inMst[0] = true;
        for (double[] edge : adjList.get(0)) pq.add(new double[]{0, edge[0], edge[1]});

        while (!pq.isEmpty() && mstEdges.size() < numVertices - 1) {
            double[] e = pq.poll();
            int to = (int) e[1];
            if (inMst[to]) continue;
            inMst[to] = true;
            mstEdges.add(e);
            for (double[] edge : adjList.get(to)) {
                int next = (int) edge[0];
                if (!inMst[next]) pq.add(new double[]{to, next, edge[1]});
            }
        }
        return mstEdges;
    }

    /** Kruskal's MST using DisjointSet. Returns list of [from, to, weight] edges plus total cost via the caller. */
    public List<double[]> kruskalMST() {
        List<double[]> allEdges = new ArrayList<>();
        for (int v = 0; v < numVertices; v++)
            for (double[] edge : adjList.get(v))
                if (v < edge[0]) allEdges.add(new double[]{v, edge[0], edge[1]}); // avoid double-adding undirected edges

        allEdges.sort(Comparator.comparingDouble(a -> a[2]));
        DisjointSet ds = new DisjointSet(numVertices);
        List<double[]> mstEdges = new ArrayList<>();
        for (double[] e : allEdges) {
            int a = (int) e[0], b = (int) e[1];
            if (ds.union(a, b)) mstEdges.add(e);
            if (mstEdges.size() == numVertices - 1) break;
        }
        return mstEdges;
    }
}
