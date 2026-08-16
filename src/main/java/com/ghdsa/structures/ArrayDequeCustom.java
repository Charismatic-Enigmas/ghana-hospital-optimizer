package com.ghdsa.structures;

import java.util.NoSuchElementException;

/**
 * Double-ended queue, resizable circular array. 
 * Use in this project: CRITICAL-urgency requests addFront, normal requests addRear.
 */
public class ArrayDequeCustom<T> {
    private Object[] data;
    private int front = 0, size = 0;

    public ArrayDequeCustom() { this(16); }
    public ArrayDequeCustom(int initialCapacity) { data = new Object[initialCapacity]; }

    private int indexOf(int offset) { return (front + offset) % data.length; }

    private void growIfFull() {
        if (size == data.length) {
            Object[] bigger = new Object[data.length * 2];
            for (int i = 0; i < size; i++) bigger[i] = data[indexOf(i)];
            data = bigger;
            front = 0;
        }
    }

    public void addFront(T value) {
        growIfFull();
        front = (front - 1 + data.length) % data.length;
        data[front] = value;
        size++;
    }

    public void addRear(T value) {
        growIfFull();
        data[indexOf(size)] = value;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T removeFront() {
        if (isEmpty()) throw new NoSuchElementException("deque is empty");
        T v = (T) data[front];
        data[front] = null;
        front = (front + 1) % data.length;
        size--;
        return v;
    }

    @SuppressWarnings("unchecked")
    public T removeRear() {
        if (isEmpty()) throw new NoSuchElementException("deque is empty");
        int idx = indexOf(size - 1);
        T v = (T) data[idx];
        data[idx] = null;
        size--;
        return v;
    }

    public boolean isEmpty() { return size == 0; }
    public int size() { return size; }
}
