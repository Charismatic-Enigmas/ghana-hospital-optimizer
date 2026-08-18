package com.ghdsa.structures;

/**
 * Hash table with separate chaining. 
 * Table size should come from ParameterDerivation.hashTableSize().
 */
public class HashTable<K, V> {
    private static class Entry<K, V> {
        K key; V value; Entry<K, V> next;
        Entry(K key, V value) { this.key = key; this.value = value; }
    }

    @SuppressWarnings("unchecked")
    private Entry<K, V>[] buckets;
    private int size;
    public int collisionCount = 0;

    @SuppressWarnings("unchecked")
    public HashTable(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        buckets = (Entry<K, V>[]) new Entry[capacity];
    }

    private int hash(K key) {
        int h = key == null ? 0 : key.hashCode();
        return Math.floorMod(h, buckets.length);
    }

    public void put(K key, V value) {
        int idx = hash(key);
        Entry<K, V> cur = buckets[idx];
        boolean bucketOccupied = cur != null;
        while (cur != null) {
            if (java.util.Objects.equals(cur.key, key)) { cur.value = value; return; }
            cur = cur.next;
        }
        Entry<K, V> newEntry = new Entry<>(key, value);
        newEntry.next = buckets[idx];
        buckets[idx] = newEntry;
        size++;
        if (bucketOccupied) collisionCount++;
    }

    public V get(K key) {
        Entry<K, V> cur = buckets[hash(key)];
        while (cur != null) {
            if (java.util.Objects.equals(cur.key, key)) return cur.value;
            cur = cur.next;
        }
        return null;
    }

    public V remove(K key) {
        int idx = hash(key);
        Entry<K, V> cur = buckets[idx], prev = null;
        while (cur != null) {
            if (java.util.Objects.equals(cur.key, key)) {
                if (prev == null) buckets[idx] = cur.next; else prev.next = cur.next;
                size--;
                return cur.value;
            }
            prev = cur; cur = cur.next;
        }
        return null;
    }

    public double loadFactor() { return (double) size / buckets.length; }
    public int size() { return size; }
}
