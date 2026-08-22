package com.ghdsa.structures;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    private Graph sampleGraph() {
        // 0-1-2-3 line plus a shortcut 0-2, mimicking a small local road network
        Graph g = new Graph(4);
        g.addEdge(0, 1, 5);
        g.addEdge(1, 2, 3);
        g.addEdge(2, 3, 2);
        g.addEdge(0, 2, 6);
        return g;
    }

    @Test
    void normalCase_bfsVisitsAllReachableNodes() {
        Graph g = sampleGraph();
        List<Integer> order = g.bfs(0);
        assertEquals(4, order.size());
        assertEquals(0, order.get(0));
    }

    @Test
    void normalCase_dijkstraFindsShortestPath() {
        Graph g = sampleGraph();
        Map<Integer, Integer> predecessors = new HashMap<>();
        Map<Integer, Double> dist = g.dijkstra(0, predecessors);
        // 0->1->2 (5+3=8) is shorter than 0->2 direct (6)? No: 6 < 8, so direct edge wins.
        assertEquals(6.0, dist.get(2));
        assertEquals(8.0, dist.get(3)); // 0->2 (6) + 2->3 (2)
    }

    @Test
    void normalCase_kruskalMstHasNMinusOneEdges() {
        Graph g = sampleGraph();
        List<double[]> mst = g.kruskalMST();
        assertEquals(3, mst.size()); // n=4 vertices -> 3 edges in a spanning tree
    }

    @Test
    void boundaryCase_disconnectedGraphDijkstraOmitsUnreachable() {
        Graph g = new Graph(3);
        g.addEdge(0, 1, 1); // vertex 2 is isolated
        Map<Integer, Double> dist = g.dijkstra(0);
        assertFalse(dist.containsKey(2), "unreachable vertex should not appear in distance map");
    }

    @Test
    void boundaryCase_singleVertexGraph() {
        Graph g = new Graph(1);
        List<Integer> order = g.bfs(0);
        assertEquals(List.of(0), order);
        assertTrue(g.kruskalMST().isEmpty());
    }

    @Test
    void invalidInput_edgeToOutOfRangeVertexThrows() {
        Graph g = new Graph(3);
        assertThrows(IndexOutOfBoundsException.class, () -> g.addEdge(0, 5, 1));
    }
}
