package com.ghdsa.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DoublyLinkedListTest {

    @Test
    void normalCase_addFirstAddLastIterate() {
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        list.addLast("ICU");
        list.addLast("OPD");
        list.addFirst("Emergency");
        StringBuilder sb = new StringBuilder();
        for (String s : list) sb.append(s).append(",");
        assertEquals("Emergency,ICU,OPD,", sb.toString());
        assertEquals(3, list.size());
    }

    @Test
    void normalCase_insertAfter() {
        DoublyLinkedList<Integer> list = new DoublyLinkedList<>();
        list.addLast(1);
        list.addLast(3);
        list.insertAfter(1, 2);
        StringBuilder sb = new StringBuilder();
        for (int v : list) sb.append(v);
        assertEquals("123", sb.toString());
    }

    @Test
    void boundaryCase_removeOnlyElementEmptiesList() {
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        list.addLast("only");
        assertTrue(list.remove("only"));
        assertEquals(0, list.size());
        assertTrue(list.isEmpty());
    }

    @Test
    void invalidInput_insertAfterMissingTargetThrows() {
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        list.addLast("a");
        assertThrows(java.util.NoSuchElementException.class, () -> list.insertAfter("not-present", "x"));
    }

    @Test
    void invalidInput_removeMissingValueReturnsFalse() {
        DoublyLinkedList<String> list = new DoublyLinkedList<>();
        list.addLast("a");
        assertFalse(list.remove("not-present"));
    }
}
