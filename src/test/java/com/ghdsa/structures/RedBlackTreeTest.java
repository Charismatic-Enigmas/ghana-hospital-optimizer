package com.ghdsa.structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RedBlackTreeTest {

    @Test
    void normalCase_insertAndSearch() {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        for (int i = 1; i <= 15; i++) tree.insert(i, "v" + i);
        for (int i = 1; i <= 15; i++) assertEquals("v" + i, tree.search(i));
        assertNull(tree.search(999));
    }

    @Test
    void normalCase_stayBalancedOnSortedInsertOrder() {
        // Inserting 1..1000 IN ORDER into a plain BST gives height ~1000 (a
        // linked list). The whole point of the red-black tree is that it
        // does not: height should stay close to 2*log2(n).
        RedBlackTree<Integer, Integer> tree = new RedBlackTree<>();
        int n = 1000;
        for (int i = 0; i < n; i++) tree.insert(i, i);
        int maxAcceptableHeight = 2 * (int) Math.ceil(Math.log(n + 1) / Math.log(2)) + 2;
        assertTrue(tree.height() <= maxAcceptableHeight,
            "height=" + tree.height() + " exceeds balanced bound=" + maxAcceptableHeight);
    }

    @Test
    void boundaryCase_singleNode() {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        tree.insert(1, "only");
        assertEquals("only", tree.search(1));
        assertEquals(0, tree.height());
    }

    @Test
    void invalidInput_searchEmptyTreeReturnsNull() {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        assertNull(tree.search(1));
    }
}
