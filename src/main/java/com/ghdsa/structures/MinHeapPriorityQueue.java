package com.ghdsa.structures;

import java.util.NoSuchElementException;

/** Binary min-heap. Section 6 row 6 — drives priority-based dispatch in M5. */
public class MinHeapPriorityQueue<T extends Comparable<T>> {
    private Object[] data = new Object[16];
    private int size = 0;

    // trace of extractMin() results, for your dispatch-order trace evidence
    public java.util.List<T> extractionTrace = new java.util.ArrayList<>();

    public void insert(T value) {
        if (size == data.length) {
            Object[] bigger = new Object[data.length * 2];
            System.arraycopy(data, 0, bigger, 0, data.length);
            data = bigger;
        }
        data[size] = value;
        heapifyUp(size);
        size++;
    }

    @SuppressWarnings("unchecked")
    public T extractMin() {
        if (isEmpty()) throw new NoSuchElementException("heap is empty");
        T min = (T) data[0];
        data[0] = data[size - 1];
        data[size - 1] = null;
        size--;
        if (size > 0) heapifyDown(0);
        extractionTrace.add(min);
        return min;
    }

    @SuppressWarnings("unchecked")
    private void heapifyUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (((T) data[index]).compareTo((T) data[parent]) < 0) {
                swap(index, parent);
                index = parent;
            } else break;
        }
    }

    @SuppressWarnings("unchecked")
    private void heapifyDown(int index) {
        while (true) {
            int left = 2 * index + 1, right = 2 * index + 2, smallest = index;
            if (left < size && ((T) data[left]).compareTo((T) data[smallest]) < 0) smallest = left;
            if (right < size && ((T) data[right]).compareTo((T) data[smallest]) < 0) smallest = right;
            if (smallest == index) break;
            swap(index, smallest);
            index = smallest;
        }
    }

    private void swap(int i, int j) {
        Object tmp = data[i]; data[i] = data[j]; data[j] = tmp;
    }

    public boolean isEmpty() { return size == 0; }
    public int size() { return size; }
}
