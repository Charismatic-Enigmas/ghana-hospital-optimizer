package com.ghdsa.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DisjointSetTest {

    @Test
    void normalCase_unionConnectsComponents() {
        DisjointSet ds = new DisjointSet(5);
        assertTrue(ds.union(0, 1));
        assertTrue(ds.union(1, 2));
        assertEquals(ds.find(0), ds.find(2));
        assertNotEquals(ds.find(0), ds.find(3));
    }

    @Test
    void boundaryCase_unionOfAlreadyConnectedReturnsFalse() {
        DisjointSet ds = new DisjointSet(3);
        ds.union(0, 1);
        assertFalse(ds.union(0, 1)); // already in same set -- this IS Kruskal's cycle-rejection case
        assertEquals(2, ds.unionTrace.size()); // both attempts are logged, even the rejected one
    }

    @Test
    void boundaryCase_singletonElementIsItsOwnRoot() {
        DisjointSet ds = new DisjointSet(1);
        assertEquals(0, ds.find(0));
    }

    @Test
    void invalidInput_negativeSizeRejectedByArrayAllocation() {
        assertThrows(NegativeArraySizeException.class, () -> new DisjointSet(-1));
    }
}
