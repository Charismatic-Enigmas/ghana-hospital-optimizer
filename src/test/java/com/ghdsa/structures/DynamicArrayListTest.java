package com.ghdsa.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Template for the required "normal / boundary / invalid input" test pattern
 * (Section 8.iii). Copy this structure for the other 11 data structures.
 */
class DynamicArrayListTest {

    @Test
    void normalCase_insertAndGet() {
        DynamicArrayList<String> list = new DynamicArrayList<>();
        list.insert(0, "OPD");
        list.insert(1, "ICU");
        assertEquals("OPD", list.get(0));
        assertEquals("ICU", list.get(1));
        assertEquals(2, list.size());
    }

    @Test
    void boundaryCase_growsPastDefaultCapacity() {
        DynamicArrayList<Integer> list = new DynamicArrayList<>();
        for (int i = 0; i < 100; i++) list.insert(i, i);
        assertEquals(100, list.size());
        assertEquals(0, list.get(0));
        assertEquals(99, list.get(99));
        assertFalse(list.resizeLog.isEmpty(), "expected at least one resize event");
    }

    @Test
    void boundaryCase_emptyListRemoveThrows() {
        DynamicArrayList<Integer> list = new DynamicArrayList<>();
        assertThrows(IndexOutOfBoundsException.class, () -> list.remove(0));
    }

    @Test
    void invalidInput_negativeIndexThrows() {
        DynamicArrayList<Integer> list = new DynamicArrayList<>();
        list.insert(0, 1);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(-1));
    }

    @Test
    void invalidInput_outOfRangeIndexThrows() {
        DynamicArrayList<Integer> list = new DynamicArrayList<>();
        list.insert(0, 1);
        assertThrows(IndexOutOfBoundsException.class, () -> list.get(5));
    }
}
