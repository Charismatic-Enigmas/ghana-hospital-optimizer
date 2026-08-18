package com.ghdsa.structures;

import java.util.ArrayList;
import java.util.List;


public class BTree<K extends Comparable<K>, V> {
    private final int t; // minimum degree, derive from ParameterDerivation

    // log of node splits: [depth marker as string] — for your split-explanation evidence
    public List<String> splitLog = new ArrayList<>();

    private class Node {
        boolean leaf;
        List<K> keys = new ArrayList<>();
        List<V> values = new ArrayList<>();
        List<Node> children = new ArrayList<>();
        Node(boolean leaf) { this.leaf = leaf; }
    }

    private Node root;

    public BTree(int minDegree) {
        if (minDegree < 2) throw new IllegalArgumentException("minDegree must be >= 2");
        this.t = minDegree;
        root = new Node(true);
    }

    public V search(K key) { return search(root, key); }

    private V search(Node node, K key) {
        int i = 0;
        while (i < node.keys.size() && key.compareTo(node.keys.get(i)) > 0) i++;
        if (i < node.keys.size() && key.compareTo(node.keys.get(i)) == 0) return node.values.get(i);
        if (node.leaf) return null;
        return search(node.children.get(i), key);
    }

    public void insert(K key, V value) {
        Node r = root;
        if (r.keys.size() == 2 * t - 1) {
            Node newRoot = new Node(false);
            newRoot.children.add(r);
            splitChild(newRoot, 0);
            splitLog.add("root split, new height at root");
            root = newRoot;
        }
        insertNonFull(root, key, value);
    }

    private void insertNonFull(Node node, K key, V value) {
        int i = node.keys.size() - 1;
        if (node.leaf) {
            node.keys.add(null);
            node.values.add(null);
            while (i >= 0 && key.compareTo(node.keys.get(i)) < 0) {
                node.keys.set(i + 1, node.keys.get(i));
                node.values.set(i + 1, node.values.get(i));
                i--;
            }
            node.keys.set(i + 1, key);
            node.values.set(i + 1, value);
        } else {
            while (i >= 0 && key.compareTo(node.keys.get(i)) < 0) i--;
            i++;
            if (node.children.get(i).keys.size() == 2 * t - 1) {
                splitChild(node, i);
                splitLog.add("child " + i + " split, key promoted: " + node.keys.get(i));
                if (key.compareTo(node.keys.get(i)) > 0) i++;
            }
            insertNonFull(node.children.get(i), key, value);
        }
    }

    private void splitChild(Node parent, int index) {
        Node child = parent.children.get(index);
        Node sibling = new Node(child.leaf);

        // move the top t-1 keys/values of child into sibling
        for (int j = 0; j < t - 1; j++) {
            sibling.keys.add(child.keys.get(j + t));
            sibling.values.add(child.values.get(j + t));
        }
        if (!child.leaf) {
            for (int j = 0; j < t; j++) sibling.children.add(child.children.get(j + t));
            for (int j = 2 * t - 1; j >= t; j--) child.children.remove(j);
        }
        K midKey = child.keys.get(t - 1);
        V midValue = child.values.get(t - 1);
        for (int j = 2 * t - 2; j >= t - 1; j--) { child.keys.remove(j); child.values.remove(j); }

        parent.children.add(index + 1, sibling);
        parent.keys.add(index, midKey);
        parent.values.add(index, midValue);
    }
}
