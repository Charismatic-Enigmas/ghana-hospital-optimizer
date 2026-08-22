package com.ghdsa.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HashTableTest {

    @Test
    void normalCase_putGetRemove() {
        HashTable<String, Integer> table = new HashTable<>(16);
        table.put("req-1", 100);
        table.put("req-2", 200);
        assertEquals(100, table.get("req-1"));
        assertEquals(200, table.remove("req-2"));
        assertNull(table.get("req-2"));
    }

    @Test
    void boundaryCase_duplicateKeyOverwrites() {
        HashTable<String, Integer> table = new HashTable<>(16);
        table.put("req-1", 1);
        table.put("req-1", 2);
        assertEquals(2, table.get("req-1"));
        assertEquals(1, table.size());
    }

    @Test
    void boundaryCase_collisionIsCounted() {
        // capacity 1 forces every key into the same bucket
        HashTable<String, Integer> table = new HashTable<>(1);
        table.put("a", 1);
        table.put("b", 2);
        assertEquals(1, table.collisionCount);
    }

    @Test
    void invalidInput_getMissingKeyReturnsNull() {
        HashTable<String, Integer> table = new HashTable<>(16);
        assertNull(table.get("does-not-exist"));
    }

    @Test
    void invalidInput_zeroCapacityRejected() {
        assertThrows(IllegalArgumentException.class, () -> new HashTable<String, Integer>(0));
    }
}
