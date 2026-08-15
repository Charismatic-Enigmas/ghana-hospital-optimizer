package com.ghdsa.structures;

import java.util.EmptyStackException;

/** Stack. Used in this project as the undo log for dispatch reassignments */
public class ArrayStack<T> {
    private Object[] data = new Object[16];
    private int top = -1;

    public void push(T value) {
        if (top + 1 == data.length) {
            Object[] bigger = new Object[data.length * 2];
            System.arraycopy(data, 0, bigger, 0, data.length);
            data = bigger;
        }
        data[++top] = value;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) throw new EmptyStackException();
        T v = (T) data[top];
        data[top--] = null;
        return v;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) throw new EmptyStackException();
        return (T) data[top];
    }

    public boolean isEmpty() { return top == -1; }
    public int size() { return top + 1; }
}
