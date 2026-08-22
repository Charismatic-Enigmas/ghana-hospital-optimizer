package com.ghdsa.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CircularQueueTest {

    @Test
    void normalCase_fifoOrder() {
        CircularQueue<String> q = new CircularQueue<>(3);
        q.enqueue("req-1");
        q.enqueue("req-2");
        assertEquals("req-1", q.dequeue());
        assertEquals("req-2", q.dequeue());
    }

    @Test
    void boundaryCase_wrapAround() {
        CircularQueue<Integer> q = new CircularQueue<>(3);
        q.enqueue(1); q.enqueue(2); q.enqueue(3);
        assertEquals(1, q.dequeue()); // front advances
        q.enqueue(4);                // wraps around to index 0
        assertEquals(2, q.dequeue());
        assertEquals(3, q.dequeue());
        assertEquals(4, q.dequeue());
        assertTrue(q.isEmpty());
    }

    @Test
    void invalidInput_enqueueOnFullQueueThrows() {
        CircularQueue<Integer> q = new CircularQueue<>(2);
        q.enqueue(1); q.enqueue(2);
        assertThrows(IllegalStateException.class, () -> q.enqueue(3));
    }

    @Test
    void invalidInput_dequeueOnEmptyQueueThrows() {
        CircularQueue<Integer> q = new CircularQueue<>(2);
        assertThrows(java.util.NoSuchElementException.class, q::dequeue);
    }

    @Test
    void invalidInput_zeroCapacityRejected() {
        assertThrows(IllegalArgumentException.class, () -> new CircularQueue<Integer>(0));
    }
}
