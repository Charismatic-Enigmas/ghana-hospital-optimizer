package com.ghdsa.structures;

import java.util.function.BiConsumer;

/** Unbalanced BST.. */
public class BinarySearchTree<K extends Comparable<K>, V> {
    private static class Node<K, V> {
        K key; V value; Node<K, V> left, right;
        Node(K key, V value) { this.key = key; this.value = value; }
    }
    private Node<K, V> root;
    private int size;

    // path of keys visited during the last search() call - for your search-path trace evidence
    public java.util.List<K> lastSearchPath = new java.util.ArrayList<>();

    public void insert(K key, V value) { root = insert(root, key, value); }

    private Node<K, V> insert(Node<K, V> node, K key, V value) {
        if (node == null) { size++; return new Node<>(key, value); }
        int cmp = key.compareTo(node.key);
        if (cmp < 0) node.left = insert(node.left, key, value);
        else if (cmp > 0) node.right = insert(node.right, key, value);
        else node.value = value; // overwrite
        return node;
    }

    public V search(K key) {
        lastSearchPath.clear();
        Node<K, V> cur = root;
        while (cur != null) {
            lastSearchPath.add(cur.key);
            int cmp = key.compareTo(cur.key);
            if (cmp == 0) return cur.value;
            cur = cmp < 0 ? cur.left : cur.right;
        }
        return null;
    }

    public void inorderTraversal(BiConsumer<K, V> visitor) { inorder(root, visitor); }

    private void inorder(Node<K, V> node, BiConsumer<K, V> visitor) {
        if (node == null) return;
        inorder(node.left, visitor);
        visitor.accept(node.key, node.value);
        inorder(node.right, visitor);
    }

    /** Height of the tree — for comparing against RedBlackTree at the same input size. */
    public int height() { return height(root); }
    private int height(Node<K, V> n) {
        if (n == null) return -1;
        return 1 + Math.max(height(n.left), height(n.right));
    }

    public int size() { return size; }
}
