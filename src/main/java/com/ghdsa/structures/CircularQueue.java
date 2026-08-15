package com.ghdsa.structures;

import java.util.NoSuchElementException;

/** Fixed-capacity circular queue (FIFO). Section 6 row 4 — powers M5's FIFO dispatch rule. */
public class CircularQueue<T> {
    private final Object[] data;
    private int front = 0, rear = -1, size = 0;

    public CircularQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        data = new Object[capacity];
    }

    public void enqueue(T value) {
        if (isFull()) throw new IllegalStateException("queue is full (capacity=" + data.length + ")");
        rear = (rear + 1) % data.length;
        data[rear] = value;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T dequeue() {
        if (isEmpty()) throw new NoSuchElementException("queue is empty");
        T v = (T) data[front];
        data[front] = null;
        front = (front + 1) % data.length;
        size--;
        return v;
    }

    public boolean isFull() { return size == data.length; }
    public boolean isEmpty() { return size == 0; }
    public int size() { return size; }
}
