package com.ghdsa.structures;

/**
 * Custom dynamic (resizable) array.
 * Growth policy: double capacity when full (amortised O(1) insertAtEnd).
 */
public class DynamicArrayList<T> {
    private Object[] data;
    private int size;
    private static final int DEFAULT_CAPACITY = 8;

    // Resize trace log: capacityBefore, capacityAfter, sizeAtResize
    public java.util.List<int[]> resizeLog = new java.util.ArrayList<>();

    public DynamicArrayList() {
        data = new Object[DEFAULT_CAPACITY];
        size = 0;
    }

    public void insert(int index, T value) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException("index=" + index + " size=" + size);
        if (size == data.length) resize(data.length * 2);
        for (int i = size; i > index; i--) data[i] = data[i - 1];
        data[index] = value;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("index=" + index + " size=" + size);
        return (T) data[index];
    }

    public void set(int index, T value) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("index=" + index + " size=" + size);
        data[index] = value;
    }

    @SuppressWarnings("unchecked")
    public T remove(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("index=" + index + " size=" + size);
        T removed = (T) data[index];
        for (int i = index; i < size - 1; i++) data[i] = data[i + 1];
        data[size - 1] = null;
        size--;
        if (size > 0 && size == data.length / 4) resize(Math.max(DEFAULT_CAPACITY, data.length / 2));
        return removed;
    }

    private void resize(int newCapacity) {
        int before = data.length;
        Object[] newData = new Object[newCapacity];
        System.arraycopy(data, 0, newData, 0, size);
        data = newData;
        resizeLog.add(new int[]{before, newCapacity, size});
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
    public int capacity() { return data.length; }
}
