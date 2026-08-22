package com.ghdsa.structures;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BinarySearchTreeTest {

    @Test
    void normalCase_insertSearchInorder() {
        BinarySearchTree<Integer, String> bst = new BinarySearchTree<>();
        bst.insert(5, "five"); bst.insert(2, "two"); bst.insert(8, "eight"); bst.insert(1, "one");
        assertEquals("two", bst.search(2));
        assertNull(bst.search(99));

        List<Integer> keysInOrder = new ArrayList<>();
        bst.inorderTraversal((k, v) -> keysInOrder.add(k));
        assertEquals(List.of(1, 2, 5, 8), keysInOrder);
    }

    @Test
    void normalCase_searchRecordsPath() {
        BinarySearchTree<Integer, String> bst = new BinarySearchTree<>();
        bst.insert(5, "a"); bst.insert(2, "b"); bst.insert(8, "c");
        bst.search(2);
        assertEquals(List.of(5, 2), bst.lastSearchPath);
    }

    @Test
    void boundaryCase_singleNodeHeightZero() {
        BinarySearchTree<Integer, String> bst = new BinarySearchTree<>();
        bst.insert(1, "a");
        assertEquals(0, bst.height());
    }

    @Test
    void boundaryCase_duplicateKeyOverwrites() {
        BinarySearchTree<Integer, String> bst = new BinarySearchTree<>();
        bst.insert(1, "first");
        bst.insert(1, "second");
        assertEquals("second", bst.search(1));
        assertEquals(1, bst.size());
    }

    @Test
    void invalidInput_searchEmptyTreeReturnsNull() {
        BinarySearchTree<Integer, String> bst = new BinarySearchTree<>();
        assertNull(bst.search(1));
    }
}
