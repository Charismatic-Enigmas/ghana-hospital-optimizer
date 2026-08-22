package com.ghdsa.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArrayStackTest {

    @Test
    void normalCase_pushPeekPop() {
        ArrayStack<String> stack = new ArrayStack<>();
        stack.push("assign req-1");
        stack.push("assign req-2");
        assertEquals("assign req-2", stack.peek());
        assertEquals("assign req-2", stack.pop());
        assertEquals("assign req-1", stack.pop());
        assertTrue(stack.isEmpty());
    }

    @Test
    void boundaryCase_growsPastInitialCapacity() {
        ArrayStack<Integer> stack = new ArrayStack<>();
        for (int i = 0; i < 100; i++) stack.push(i);
        assertEquals(100, stack.size());
        assertEquals(99, stack.pop());
    }

    @Test
    void invalidInput_popOnEmptyStackThrows() {
        ArrayStack<Integer> stack = new ArrayStack<>();
        assertThrows(java.util.EmptyStackException.class, stack::pop);
    }

    @Test
    void invalidInput_peekOnEmptyStackThrows() {
        ArrayStack<Integer> stack = new ArrayStack<>();
        assertThrows(java.util.EmptyStackException.class, stack::peek);
    }
}
