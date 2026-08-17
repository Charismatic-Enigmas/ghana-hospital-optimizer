package com.ghdsa.structures;

/**
 * Left-leaning red-black tree (Sedgewick's formulation — simpler rotation
 * rules than a classic RB-tree, and the brief explicitly allows a clearly
 * documented simplified balanced tree).
 *
 * Invariant: red links lean left; no node has two red links; perfect black balance.
 */
public class RedBlackTree<K extends Comparable<K>, V> {
    private static final boolean RED = true, BLACK = false;

    private static class Node<K, V> {
        K key; V value; Node<K, V> left, right; boolean color;
        Node(K key, V value, boolean color) { this.key = key; this.value = value; this.color = color; }
    }
    private Node<K, V> root;

    // records each rotation performed during the last insert() - for your before/after diagrams
    public java.util.List<String> lastInsertRotationLog = new java.util.ArrayList<>();

    public void insert(K key, V value) {
        lastInsertRotationLog.clear();
        root = insert(root, key, value);
        root.color = BLACK;
    }

    private Node<K, V> insert(Node<K, V> h, K key, V value) {
        if (h == null) return new Node<>(key, value, RED);

        int cmp = key.compareTo(h.key);
        if (cmp < 0) h.left = insert(h.left, key, value);
        else if (cmp > 0) h.right = insert(h.right, key, value);
        else h.value = value;

        if (isRed(h.right) && !isRed(h.left)) { h = rotateLeft(h); lastInsertRotationLog.add("rotateLeft@" + h.key); }
        if (isRed(h.left) && isRed(h.left.left)) { h = rotateRight(h); lastInsertRotationLog.add("rotateRight@" + h.key); }
        if (isRed(h.left) && isRed(h.right)) { flipColors(h); lastInsertRotationLog.add("flipColors@" + h.key); }

        return h;
    }

    public V search(K key) {
        Node<K, V> cur = root;
        while (cur != null) {
            int cmp = key.compareTo(cur.key);
            if (cmp == 0) return cur.value;
            cur = cmp < 0 ? cur.left : cur.right;
        }
        return null;
    }

    public int height() { return height(root); }
    private int height(Node<K, V> n) {
        if (n == null) return -1;
        return 1 + Math.max(height(n.left), height(n.right));
    }

    private boolean isRed(Node<K, V> n) { return n != null && n.color == RED; }

    private Node<K, V> rotateLeft(Node<K, V> h) {
        Node<K, V> x = h.right;
        h.right = x.left;
        x.left = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }

    private Node<K, V> rotateRight(Node<K, V> h) {
        Node<K, V> x = h.left;
        h.left = x.right;
        x.right = h;
        x.color = h.color;
        h.color = RED;
        return x;
    }

    private void flipColors(Node<K, V> h) {
        h.color = !h.color;
        h.left.color = !h.left.color;
        h.right.color = !h.right.color;
    }
}
