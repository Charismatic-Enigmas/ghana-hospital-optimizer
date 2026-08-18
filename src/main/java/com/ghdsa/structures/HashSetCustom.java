package com.ghdsa.structures;

/**
 * Set, built on top of the custom HashTable (Section 6 row 12: "custom
 * set/map on top of hash table or BST"). HashTable itself already covers
 * the map/put-get-remove use case; this class covers the membership
 * (add/contains/remove) use case by storing a sentinel value per key.
 */
public class HashSetCustom<T> {
    private static final Object PRESENT = new Object();
    private final HashTable<T, Object> table;

    public HashSetCustom(int capacity) {
        table = new HashTable<>(capacity);
    }

    public boolean add(T value) {
        boolean isNew = !contains(value);
        table.put(value, PRESENT);
        return isNew;
    }

    public boolean contains(T value) {
        return table.get(value) != null;
    }

    public boolean remove(T value) {
        return table.remove(value) != null;
    }

    public int size() {
        return table.size();
    }
}
