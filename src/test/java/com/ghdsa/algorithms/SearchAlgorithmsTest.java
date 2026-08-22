package com.ghdsa.algorithms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SearchAlgorithmsTest {

    @Test
    void normalCase_linearSearchFindsElement() {
        Integer[] arr = {5, 3, 8, 1, 9};
        assertEquals(2, SearchAlgorithms.linearSearch(arr, 8));
    }

    @Test
    void normalCase_binarySearchFindsElementInSortedArray() {
        Integer[] sorted = {1, 3, 5, 8, 9};
        assertEquals(3, SearchAlgorithms.binarySearch(sorted, 8));
    }

    @Test
    void boundaryCase_emptyArrayReturnsMinusOne() {
        Integer[] empty = {};
        assertEquals(-1, SearchAlgorithms.linearSearch(empty, 1));
        assertEquals(-1, SearchAlgorithms.binarySearch(empty, 1));
    }

    @Test
    void invalidInput_binarySearchOnUnsortedArrayCanGiveWrongAnswer() {
        // REQUIRED COUNTEREXAMPLE (Section 10): binary search's precondition
        // is that the array is sorted. Violate it and show the wrong answer.
        Integer[] unsorted = {9, 8, 1, 3, 5}; // 8 IS present, but binary search will still fail
        int result = SearchAlgorithms.binarySearch(unsorted, 8);
        assertNotEquals(2, result, "binary search on unsorted input should NOT reliably find the element");
    }

    @Test
    void invalidInput_missingElementReturnsMinusOne() {
        Integer[] arr = {1, 3, 5};
        assertEquals(-1, SearchAlgorithms.linearSearch(arr, 99));
        assertEquals(-1, SearchAlgorithms.binarySearch(arr, 99));
    }
}
