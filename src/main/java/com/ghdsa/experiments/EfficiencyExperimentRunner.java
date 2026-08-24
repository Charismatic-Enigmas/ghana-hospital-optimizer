package com.ghdsa.experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

import com.ghdsa.algorithms.SearchAlgorithms;
import com.ghdsa.algorithms.SortAlgorithms;
import com.ghdsa.app.ParameterDerivation;
import com.ghdsa.structures.BinarySearchTree;
import com.ghdsa.structures.Graph;
import com.ghdsa.structures.HashTable;
import com.ghdsa.structures.MinHeapPriorityQueue;
import com.ghdsa.structures.RedBlackTree;

public class EfficiencyExperimentRunner {

    private static final int RUNS_PER_SIZE = 3;
    private static final Random RNG = new Random(ParameterDerivation.randomSeed());

    public static void main(String[] args) throws IOException {
        runAll("efficiency_results.csv");
    }

    /** Runs all 6 required Section 9 experiments and writes results to outputCsvPath. */
    public static void runAll(String outputCsvPath) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(outputCsvPath))) {
            out.println("experiment,algorithm,input_size,run_number,time_ns");

            runSearchComparison(out);
            runSortComparison(out);
            runHashLoadFactor(out);
            runTreeComparison(out);
            runHeapDispatch(out);
            runGraphAlgorithms(out);
        }
        System.out.println("Wrote " + outputCsvPath + " -- import into Excel/Python and plot per Section 9.iii.");
    }

    // --- Experiment 1: Search comparison 
    private static void runSearchComparison(PrintWriter out) {
        int[] sizes = {100, 500, 1000, 5000, 10000};
        for (int size : sizes) {
            Integer[] sorted = randomSortedArray(size);
            for (int run = 1; run <= RUNS_PER_SIZE; run++) {
                int target = sorted[RNG.nextInt(size)];
                long t0 = System.nanoTime();
                SearchAlgorithms.linearSearch(sorted, target);
                out.printf("search_comparison,LinearSearch,%d,%d,%d%n", size, run, System.nanoTime() - t0);

                t0 = System.nanoTime();
                SearchAlgorithms.binarySearch(sorted, target);
                out.printf("search_comparison,BinarySearch,%d,%d,%d%n", size, run, System.nanoTime() - t0);
            }
        }
    }

    // --- Experiment 2: Sorting comparison 
    private static void runSortComparison(PrintWriter out) {
        int[] sizes = {100, 500, 1000, 5000, 10000};
        for (int size : sizes) {
            for (int run = 1; run <= RUNS_PER_SIZE; run++) {
                timeSort(out, "SelectionSort", size, run, SortAlgorithms::selectionSort);
                timeSort(out, "InsertionSort", size, run, SortAlgorithms::insertionSort);
                timeSort(out, "MergeSort", size, run, SortAlgorithms::mergeSort);
                timeSort(out, "QuickSort", size, run, SortAlgorithms::quickSort);
            }
        }
    }

    private interface SortFn { void sort(Integer[] arr); }

    private static void timeSort(PrintWriter out, String name, int size, int run, SortFn fn) {
        Integer[] arr = randomArray(size);
        long t0 = System.nanoTime();
        fn.sort(arr);
        out.printf("sorting_comparison,%s,%d,%d,%d%n", name, size, run, System.nanoTime() - t0);
    }

    // --- Experiment 3: Hash table load factor 
    private static void runHashLoadFactor(PrintWriter out) {
        int[] keyCounts = {100, 1000, 5000, 10000, 20000};
        int tableSize = 1024; // fixed table size so load factor actually varies with keyCount
        for (int keys : keyCounts) {
            for (int run = 1; run <= RUNS_PER_SIZE; run++) {
                HashTable<Integer, Integer> table = new HashTable<>(tableSize);
                long t0 = System.nanoTime();
                for (int i = 0; i < keys; i++) table.put(RNG.nextInt(), i);
                long elapsed = System.nanoTime() - t0;
                out.printf("hash_load_factor,HashTablePut(loadFactor=%.2f;collisions=%d),%d,%d,%d%n",
                        table.loadFactor(), table.collisionCount, keys, run, elapsed);
            }
        }
    }

    // --- Experiment 4: BST vs balanced tree 
    private static void runTreeComparison(PrintWriter out) {
        int[] sizes = {100, 1000, 5000, 10000};
        for (int size : sizes) {
            for (int run = 1; run <= RUNS_PER_SIZE; run++) {
                BinarySearchTree<Integer, Integer> bst = new BinarySearchTree<>();
                Integer[] sortedInput = randomSortedArray(size); // worst case for an unbalanced BST
                long t0 = System.nanoTime();
                for (int v : sortedInput) bst.insert(v, v);
                out.printf("bst_vs_balanced,BST(height=%d),%d,%d,%d%n", bst.height(), size, run, System.nanoTime() - t0);

                RedBlackTree<Integer, Integer> rbt = new RedBlackTree<>();
                t0 = System.nanoTime();
                for (int v : sortedInput) rbt.insert(v, v);
                out.printf("bst_vs_balanced,RedBlackTree(height=%d),%d,%d,%d%n", rbt.height(), size, run, System.nanoTime() - t0);
            }
        }
    }

    // --- Experiment 5: Heap priority dispatch 
    private static void runHeapDispatch(PrintWriter out) {
        int[] sizes = {100, 1000, 5000, 10000, 20000};
        for (int size : sizes) {
            for (int run = 1; run <= RUNS_PER_SIZE; run++) {
                MinHeapPriorityQueue<Integer> pq = new MinHeapPriorityQueue<>();
                long t0 = System.nanoTime();
                for (int i = 0; i < size; i++) pq.insert(RNG.nextInt());
                while (!pq.isEmpty()) pq.extractMin();
                out.printf("heap_dispatch,InsertThenExtractAll,%d,%d,%d%n", size, run, System.nanoTime() - t0);
            }
        }
    }

    // --- Experiment 6: Graph algorithms
    private static void runGraphAlgorithms(PrintWriter out) {
        int[] sizes = {50, 100, 200, 500};
        for (int size : sizes) {
            for (int run = 1; run <= RUNS_PER_SIZE; run++) {
                Graph g = randomConnectedGraph(size);

                long t0 = System.nanoTime();
                g.bfs(0);
                out.printf("graph_algorithms,BFS,%d,%d,%d%n", size, run, System.nanoTime() - t0);

                t0 = System.nanoTime();
                g.dfs(0);
                out.printf("graph_algorithms,DFS,%d,%d,%d%n", size, run, System.nanoTime() - t0);

                t0 = System.nanoTime();
                g.dijkstra(0);
                out.printf("graph_algorithms,Dijkstra,%d,%d,%d%n", size, run, System.nanoTime() - t0);

                t0 = System.nanoTime();
                g.kruskalMST();
                out.printf("graph_algorithms,Kruskal,%d,%d,%d%n", size, run, System.nanoTime() - t0);
            }
        }
    }

    // --- helpers ---
    private static Integer[] randomArray(int size) {
        Integer[] arr = new Integer[size];
        for (int i = 0; i < size; i++) arr[i] = RNG.nextInt(1_000_000);
        return arr;
    }

    private static Integer[] randomSortedArray(int size) {
        Integer[] arr = randomArray(size);
        Arrays.sort(arr); 
        return arr;
    }

    private static Graph randomConnectedGraph(int numVertices) {
        Graph g = new Graph(numVertices);
        // spanning path guarantees connectivity, then add extra random edges
        for (int i = 0; i < numVertices - 1; i++) g.addEdge(i, i + 1, 1 + RNG.nextDouble() * 10);
        int extraEdges = numVertices * 2;
        for (int i = 0; i < extraEdges; i++) {
            int a = RNG.nextInt(numVertices), b = RNG.nextInt(numVertices);
            if (a != b) g.addEdge(a, b, 1 + RNG.nextDouble() * 10);
        }
        return g;
    }
}
