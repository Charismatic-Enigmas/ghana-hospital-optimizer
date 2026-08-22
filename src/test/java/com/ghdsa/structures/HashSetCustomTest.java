package com.ghdsa.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HashSetCustomTest {

    @Test
    void normalCase_addAndContains() {
        HashSetCustom<String> set = new HashSetCustom<>(16);
        assertTrue(set.add("L001"));
        assertTrue(set.contains("L001"));
        assertEquals(1, set.size());
    }

    @Test
    void boundaryCase_duplicateAddDoesNotGrowSize() {
        HashSetCustom<String> set = new HashSetCustom<>(16);
        set.add("L001");
        assertFalse(set.add("L001"));
        assertEquals(1, set.size());
    }

    @Test
    void boundaryCase_emptySetContainsIsFalse() {
        HashSetCustom<String> set = new HashSetCustom<>(16);
        assertFalse(set.contains("missing"));
        assertEquals(0, set.size());
    }

    @Test
    void invalidInput_removeMissingValueReturnsFalse() {
        HashSetCustom<String> set = new HashSetCustom<>(16);
        assertFalse(set.remove("never-added"));
    }

    @Test
    void normalCase_removeThenContainsIsFalse() {
        HashSetCustom<String> set = new HashSetCustom<>(16);
        set.add("L001");
        assertTrue(set.remove("L001"));
        assertFalse(set.contains("L001"));
    }
}
