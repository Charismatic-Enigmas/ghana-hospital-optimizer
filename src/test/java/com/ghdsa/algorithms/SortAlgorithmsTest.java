package com.ghdsa.algorithms;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class SortAlgorithmsTest {

    private static final Integer[] SAMPLE = {5, 2, 9, 1, 5, 6};
    private static final Integer[] EXPECTED = {1, 2, 5, 5, 6, 9};

    @Test
    void normalCase_selectionSort() {
        Integer[] a = SAMPLE.clone();
        SortAlgorithms.selectionSort(a);
        assertArrayEquals(EXPECTED, a);
    }

    @Test
    void normalCase_insertionSort() {
        Integer[] a = SAMPLE.clone();
        SortAlgorithms.insertionSort(a);
        assertArrayEquals(EXPECTED, a);
    }

    @Test
    void normalCase_mergeSort() {
        Integer[] a = SAMPLE.clone();
        SortAlgorithms.mergeSort(a);
        assertArrayEquals(EXPECTED, a);
    }

    @Test
    void normalCase_quickSort() {
        Integer[] a = SAMPLE.clone();
        SortAlgorithms.quickSort(a);
        assertArrayEquals(EXPECTED, a);
    }

    @Test
    void boundaryCase_emptyAndSingleElementArrays() {
        Integer[] empty = {};
        Integer[] single = {7};
        SortAlgorithms.mergeSort(empty);
        SortAlgorithms.quickSort(single);
        assertArrayEquals(new Integer[]{}, empty);
        assertArrayEquals(new Integer[]{7}, single);
    }

    @Test
    void boundaryCase_alreadySortedInput() {
        Integer[] a = {1, 2, 3, 4, 5};
        SortAlgorithms.quickSort(a);
        assertArrayEquals(new Integer[]{1, 2, 3, 4, 5}, a);
        // NOTE for your report: already-sorted input is quickSort's WORST CASE
        // with this last-element pivot choice (O(n^2)) even though the output
        // is correct -- pair this test with a timing run in the efficiency lab.
    }

    @Test
    void invalidInput_arrayWithDuplicatesAndNegatives() {
        Integer[] a = {-3, 0, -3, 5, -1};
        SortAlgorithms.mergeSort(a);
        assertArrayEquals(new Integer[]{-3, -3, -1, 0, 5}, a);
    }
}
