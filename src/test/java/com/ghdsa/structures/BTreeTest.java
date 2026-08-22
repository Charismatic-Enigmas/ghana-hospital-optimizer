package com.ghdsa.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BTreeTest {

    @Test
    void normalCase_insertAndSearch() {
        BTree<Integer, String> tree = new BTree<>(2); // minimum degree 2
        for (int i = 1; i <= 20; i++) tree.insert(i, "v" + i);
        for (int i = 1; i <= 20; i++) assertEquals("v" + i, tree.search(i));
        assertNull(tree.search(999));
    }

    @Test
    void boundaryCase_triggersNodeSplit() {
        BTree<Integer, Integer> tree = new BTree<>(2); // 2t-1 = 3 keys max per node before split
        for (int i = 1; i <= 4; i++) tree.insert(i, i); // forces at least one split
        assertFalse(tree.splitLog.isEmpty(), "expected at least one split for this input size");
    }

    @Test
    void invalidInput_minDegreeBelowTwoRejected() {
        assertThrows(IllegalArgumentException.class, () -> new BTree<Integer, Integer>(1));
    }

    @Test
    void invalidInput_searchEmptyTreeReturnsNull() {
        BTree<Integer, String> tree = new BTree<>(2);
        assertNull(tree.search(1));
    }
}
