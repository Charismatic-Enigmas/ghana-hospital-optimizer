package com.ghdsa.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MinHeapPriorityQueueTest {

    @Test
    void normalCase_dispatchOrderIsAscending() {
        MinHeapPriorityQueue<Integer> pq = new MinHeapPriorityQueue<>();
        pq.insert(5); pq.insert(1); pq.insert(3); pq.insert(2); pq.insert(4);
        int[] expected = {1, 2, 3, 4, 5};
        for (int e : expected) assertEquals(e, pq.extractMin());
        assertEquals(java.util.List.of(1, 2, 3, 4, 5), pq.extractionTrace);
    }

    @Test
    void boundaryCase_singleElement() {
        MinHeapPriorityQueue<Integer> pq = new MinHeapPriorityQueue<>();
        pq.insert(42);
        assertEquals(42, pq.extractMin());
        assertTrue(pq.isEmpty());
    }

    @Test
    void boundaryCase_growsPastInitialCapacity() {
        MinHeapPriorityQueue<Integer> pq = new MinHeapPriorityQueue<>();
        for (int i = 100; i > 0; i--) pq.insert(i);
        assertEquals(100, pq.size());
        assertEquals(1, pq.extractMin());
    }

    @Test
    void invalidInput_extractOnEmptyThrows() {
        MinHeapPriorityQueue<Integer> pq = new MinHeapPriorityQueue<>();
        assertThrows(java.util.NoSuchElementException.class, pq::extractMin);
    }
}
