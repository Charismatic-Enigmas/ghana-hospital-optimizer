package com.ghdsa.algorithms;

/** M4: linear and binary search. Section 7. */
public final class SearchAlgorithms {
    private SearchAlgorithms() {}

    /** O(n). Works on unsorted arrays. Returns index or -1. */
    public static <T extends Comparable<T>> int linearSearch(T[] arr, T target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].compareTo(target) == 0) return i;
        }
        return -1;
    }

    /**
     * O(log n). PRECONDITION: arr must already be sorted ascending — this is
     * exactly the "invalid precondition" counterexample the brief wants in
     * Section 10 (try it on an unsorted array and show it returns a wrong answer).
     */
    public static <T extends Comparable<T>> int binarySearch(T[] arr, T target) {
        int lo = 0, hi = arr.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int cmp = arr[mid].compareTo(target);
            if (cmp == 0) return mid;
            if (cmp < 0) lo = mid + 1; else hi = mid - 1;
        }
        return -1;
    }
}
