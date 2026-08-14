package com.ghdsa.structures;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/** Doubly linked list. */
public class DoublyLinkedList<T> implements Iterable<T> {
    private static class Node<T> {
        T value;
        Node<T> prev, next;
        Node(T value) { this.value = value; }
    }

    private Node<T> head, tail;
    private int size;

    public void addFirst(T value) {
        Node<T> n = new Node<>(value);
        if (head == null) { head = tail = n; }
        else { n.next = head; head.prev = n; head = n; }
        size++;
    }

    public void addLast(T value) {
        Node<T> n = new Node<>(value);
        if (tail == null) { head = tail = n; }
        else { n.prev = tail; tail.next = n; tail = n; }
        size++;
    }

    public void insertAfter(T target, T value) {
        Node<T> cur = head;
        while (cur != null && !Objects.equals(cur.value, target)) cur = cur.next;
        if (cur == null) throw new NoSuchElementException("target not found: " + target);
        Node<T> n = new Node<>(value);
        n.prev = cur; n.next = cur.next;
        if (cur.next != null) cur.next.prev = n; else tail = n;
        cur.next = n;
        size++;
    }

    public boolean remove(T value) {
        Node<T> cur = head;
        while (cur != null && !Objects.equals(cur.value, value)) cur = cur.next;
        if (cur == null) return false;
        if (cur.prev != null) cur.prev.next = cur.next; else head = cur.next;
        if (cur.next != null) cur.next.prev = cur.prev; else tail = cur.prev;
        size--;
        return true;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> cur = head;
            public boolean hasNext() { return cur != null; }
            public T next() {
                if (cur == null) throw new NoSuchElementException();
                T v = cur.value;
                cur = cur.next;
                return v;
            }
        };
    }
}
