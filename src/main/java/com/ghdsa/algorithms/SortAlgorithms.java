package com.ghdsa.algorithms;

/** M4: selection, insertion, merge, quicksort — all implemented from scratch. Section 7. */
public final class SortAlgorithms {
    private SortAlgorithms() {}

    /** Not stable, in-place, O(n^2) always. */
    public static <T extends Comparable<T>> void selectionSort(T[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < arr.length; j++)
                if (arr[j].compareTo(arr[minIdx]) < 0) minIdx = j;
            swap(arr, i, minIdx);
        }
    }

    /** Stable, in-place, O(n^2) worst case, O(n) best case (already sorted). */
    public static <T extends Comparable<T>> void insertionSort(T[] arr) {
        for (int i = 1; i < arr.length; i++) {
            T key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j].compareTo(key) > 0) { arr[j + 1] = arr[j]; j--; }
            arr[j + 1] = key;
        }
    }

    /** Stable, O(n log n) always, needs O(n) extra space. */
    public static <T extends Comparable<T>> void mergeSort(T[] arr) {
        if (arr.length <= 1) return;
        mergeSort(arr, 0, arr.length - 1);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> void mergeSort(T[] arr, int lo, int hi) {
        if (lo >= hi) return;
        int mid = lo + (hi - lo) / 2;
        mergeSort(arr, lo, mid);
        mergeSort(arr, mid + 1, hi);

        T[] tmp = (T[]) new Comparable[hi - lo + 1];
        int i = lo, j = mid + 1, k = 0;
        while (i <= mid && j <= hi) tmp[k++] = arr[i].compareTo(arr[j]) <= 0 ? arr[i++] : arr[j++];
        while (i <= mid) tmp[k++] = arr[i++];
        while (j <= hi) tmp[k++] = arr[j++];
        System.arraycopy(tmp, 0, arr, lo, tmp.length);
    }

    /** Not stable, in-place, O(n log n) average, O(n^2) worst case (e.g. already-sorted input with this pivot choice). */
    public static <T extends Comparable<T>> void quickSort(T[] arr) {
        if (arr.length <= 1) return;
        quickSort(arr, 0, arr.length - 1);
    }

    private static <T extends Comparable<T>> void quickSort(T[] arr, int lo, int hi) {
        if (lo >= hi) return;
        int p = partition(arr, lo, hi);
        quickSort(arr, lo, p - 1);
        quickSort(arr, p + 1, hi);
    }

    private static <T extends Comparable<T>> int partition(T[] arr, int lo, int hi) {
        T pivot = arr[hi]; // last-element pivot -- this is *why* already-sorted input is worst case
        int i = lo - 1;
        for (int j = lo; j < hi; j++) if (arr[j].compareTo(pivot) < 0) swap(arr, ++i, j);
        swap(arr, i + 1, hi);
        return i + 1;
    }

    private static <T> void swap(T[] arr, int i, int j) { T t = arr[i]; arr[i] = arr[j]; arr[j] = t; }
}
