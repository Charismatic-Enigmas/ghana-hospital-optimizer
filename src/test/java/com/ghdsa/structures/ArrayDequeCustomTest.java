package com.ghdsa.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArrayDequeCustomTest {

    @Test
    void normalCase_urgentRequestJumpsQueue() {
        ArrayDequeCustom<String> deque = new ArrayDequeCustom<>();
        deque.addRear("normal-req-1");
        deque.addRear("normal-req-2");
        deque.addFront("CRITICAL-req-99"); // urgent request jumps to the front
        assertEquals("CRITICAL-req-99", deque.removeFront());
        assertEquals("normal-req-1", deque.removeFront());
        assertEquals("normal-req-2", deque.removeFront());
    }

    @Test
    void boundaryCase_growsPastInitialCapacity() {
        ArrayDequeCustom<Integer> deque = new ArrayDequeCustom<>(4);
        for (int i = 0; i < 50; i++) deque.addRear(i);
        assertEquals(50, deque.size());
        assertEquals(0, deque.removeFront());
        assertEquals(49, deque.removeRear());
    }

    @Test
    void invalidInput_removeFrontOnEmptyThrows() {
        ArrayDequeCustom<Integer> deque = new ArrayDequeCustom<>();
        assertThrows(java.util.NoSuchElementException.class, deque::removeFront);
    }

    @Test
    void invalidInput_removeRearOnEmptyThrows() {
        ArrayDequeCustom<Integer> deque = new ArrayDequeCustom<>();
        assertThrows(java.util.NoSuchElementException.class, deque::removeRear);
    }
}
