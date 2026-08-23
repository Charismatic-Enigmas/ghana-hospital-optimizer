package com.ghdsa.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ghdsa.algorithms.DPRequestSelection;
import com.ghdsa.algorithms.GreedyResourceAssignment;
import com.ghdsa.algorithms.SearchAlgorithms;
import com.ghdsa.algorithms.SortAlgorithms;
import com.ghdsa.db.DatabaseManager;
import com.ghdsa.experiments.EfficiencyExperimentRunner;
import com.ghdsa.model.Location;
import com.ghdsa.model.Resource;
import com.ghdsa.model.Road;
import com.ghdsa.model.ServiceRequest;
import com.ghdsa.structures.ArrayDequeCustom;
import com.ghdsa.structures.ArrayStack;
import com.ghdsa.structures.BTree;
import com.ghdsa.structures.BinarySearchTree;
import com.ghdsa.structures.CircularQueue;
import com.ghdsa.structures.DisjointSet;
import com.ghdsa.structures.Graph;
import com.ghdsa.structures.HashSetCustom;
import com.ghdsa.structures.HashTable;
import com.ghdsa.structures.MinHeapPriorityQueue;
import com.ghdsa.structures.RedBlackTree;

public class Main {
    private static DatabaseManager db;
    private static List<Location> locations;
    private static List<Road> roads;
    private static List<ServiceRequest> requests;
    private static List<Resource> resources;
    private static Map<Integer, Integer> locationIdToIndex = new HashMap<>();
    private static Graph graph;
    private static Path projectRoot;

    private static Path resolveProjectRoot() {
        try {
            Path codeLocation = Paths.get(
                    Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            Path dir = Files.isDirectory(codeLocation) ? codeLocation : codeLocation.getParent();
            Path candidate = dir;
            for (int i = 0; i < 6 && candidate != null; i++) {
                if (Files.exists(candidate.resolve("db").resolve("schema.sql"))) {
                    return candidate;
                }
                candidate = candidate.getParent();
            }
        } catch (Exception ignored) {

        }

        Path candidate = Paths.get("").toAbsolutePath();
        for (int i = 0; i < 6 && candidate != null; i++) {
            if (Files.exists(candidate.resolve("db").resolve("schema.sql"))) {
                return candidate;
            }
            candidate = candidate.getParent();
        }

        return Paths.get("").toAbsolutePath();
    }

    public static void main(String[] args) throws Exception {
        projectRoot = resolveProjectRoot();

        String dbFile = args != null && args.length > 0 && args[0] != null && !args[0].isBlank()
                ? args[0]
                : projectRoot.resolve("ghana_hospital.db").toString();
        db = new DatabaseManager("jdbc:sqlite:" + dbFile);
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("""
                    ==================================================
                     Ghana Smart Hospital Service Operations Optimizer
                    ==================================================
                     1. Initialise schema + load CSV data into database
                     2. Load data from database into memory + build graph
                     --- Graph engine (M7) ---
                     3. Dispatch demo (priority queue by urgency)
                     4. Find route between two locations (Dijkstra)
                     5. Show reachable locations from a point (BFS/DFS)
                     6. Build minimum connection network (Kruskal)
                     --- Search/sort engine (M4) ---
                     7. Search demo (linear vs binary + invalid-precondition case)
                     8. Sort demo (selection/insertion/merge/quicksort, timed)
                     --- Indexing engine (M6) ---
                     9. Hash table demo (put/get/remove + collision stats)
                    10. Set demo (HashSetCustom membership/lookup)
                    11. BST demo (insert + search path + inorder traversal)
                    12. Red-black tree demo (insert + rotation trace + height)
                    13. B-tree demo (insert + node-split trace + search)
                    --- Connectivity/scheduling structures (M3/M5) ---
                    14. Disjoint-set / Kruskal connectivity trace
                    15. Circular queue demo (wrap-around trace)
                    16. Deque demo (urgent request jumps the queue)
                    17. Stack undo/audit-log demo
                    --- Optimisation engine (M8) ---
                    18. Greedy resource assignment demo (+ counterexample)
                    19. Dynamic programming request-selection demo (knapsack)
                    --- Efficiency lab (M10) ---
                    20. Run full efficiency experiment suite (writes CSV)
                    21. Exit
                    ==================================================
                    """);
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1" -> initAndImport();
                    case "2" -> loadIntoMemory();
                    case "3" -> dispatchDemo();
                    case "4" -> routeDemo(scanner);
                    case "5" -> reachabilityDemo(scanner);
                    case "6" -> mstDemo();
                    case "7" -> searchDemo();
                    case "8" -> sortDemo();
                    case "9" -> hashTableDemo();
                    case "10" -> setDemo();
                    case "11" -> bstDemo();
                    case "12" -> redBlackTreeDemo();
                    case "13" -> bTreeDemo();
                    case "14" -> disjointSetDemo();
                    case "15" -> circularQueueDemo();
                    case "16" -> dequeDemo();
                    case "17" -> undoDemo();
                    case "18" -> greedyDemo();
                    case "19" -> dpDemo();
                    case "20" -> efficiencyLabDemo();
                    case "21" -> running = false;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        db.close();
        System.out.println("Goodbye.");
    }

    private static void initAndImport() throws Exception {
        db.initSchema(projectRoot.resolve("db").resolve("schema.sql").toString());
        db.importLocationsCsv(projectRoot.resolve("db").resolve("seed").resolve("locations.csv").toString());
        db.importRoadsCsv(projectRoot.resolve("db").resolve("seed").resolve("roads.csv").toString());
        db.importServiceRequestsCsv(
                projectRoot.resolve("db").resolve("seed").resolve("service_requests.csv").toString());
        db.importResourcesCsv(projectRoot.resolve("db").resolve("seed").resolve("resources.csv").toString());
        System.out.println("Schema created and CSV data imported.");
    }

    private static void loadIntoMemory() throws Exception {
        // If the database schema hasn't been initialised, give a helpful message
        if (!db.tableExists("locations") || !db.tableExists("roads") || !db.tableExists("service_requests")) {
            System.out.println(
                    "Database schema not found. Run option 1 to initialise the schema and import CSV data first.");
            return;
        }

        locations = db.loadLocations();
        roads = db.loadRoads();
        requests = db.loadServiceRequests();
        resources = db.loadResources();

        locationIdToIndex.clear();
        for (int i = 0; i < locations.size(); i++)
            locationIdToIndex.put(locations.get(i).locationId(), i);

        graph = new Graph(locations.size());
        for (Road r : roads) {
            Integer from = locationIdToIndex.get(r.fromLocationId());
            Integer to = locationIdToIndex.get(r.toLocationId());
            if (from != null && to != null)
                graph.addEdge(from, to, r.travelTimeMin());
        }
        System.out.printf("Loaded %d locations, %d roads, %d requests, %d resources. Graph built.%n",
                locations.size(), roads.size(), requests.size(), resources.size());
    }

    private static void dispatchDemo() {
        if (requests == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        MinHeapPriorityQueue<PriorityRequest> pq = new MinHeapPriorityQueue<>();
        int urgencyWeight = ParameterDerivation.priorityWeight();
        for (ServiceRequest r : requests) {
            if (!"PENDING".equals(r.status()))
                continue;
            pq.insert(new PriorityRequest(r.requestId(), urgencyRank(r.urgency()) * urgencyWeight));
        }
        System.out.println("Dispatch order (top 10, lower score = dispatched first):");
        for (int i = 0; i < 10 && !pq.isEmpty(); i++) {
            PriorityRequest pr = pq.extractMin();
            System.out.printf("  request #%d (score=%d)%n", pr.requestId, pr.score);
        }
    }

    private record PriorityRequest(int requestId, int score) implements Comparable<PriorityRequest> {
        public int compareTo(PriorityRequest o) {
            return Integer.compare(score, o.score);
        }
    }

    private static int urgencyRank(String urgency) {
        return switch (urgency) {
            case "CRITICAL" -> 0;
            case "HIGH" -> 1;
            case "MEDIUM" -> 2;
            default -> 3;
        };
    }

    private static void routeDemo(Scanner scanner) {
        if (graph == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        System.out.print("From location_id: ");
        int fromId = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("To location_id: ");
        int toId = Integer.parseInt(scanner.nextLine().trim());
        Integer from = locationIdToIndex.get(fromId), to = locationIdToIndex.get(toId);
        if (from == null || to == null) {
            System.out.println("Unknown location id.");
            return;
        }

        Map<Integer, Integer> predecessors = new HashMap<>();
        Map<Integer, Double> dist = graph.dijkstra(from, predecessors);
        if (!dist.containsKey(to)) {
            System.out.println("No path found.");
            return;
        }
        System.out.printf("Shortest travel time: %.1f min%n", dist.get(to));

        LinkedList<Integer> path = new LinkedList<>();
        Integer cur = to;
        while (cur != null) {
            path.addFirst(cur);
            cur = predecessors.get(cur);
        }
        System.out.print("Path (by graph index): ");
        for (int idx : path)
            System.out.print(locations.get(idx).name() + " -> ");
        System.out.println("(end)");
    }

    private static void reachabilityDemo(Scanner scanner) {
        if (graph == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        System.out.print("From location_id: ");
        int fromId = Integer.parseInt(scanner.nextLine().trim());
        Integer from = locationIdToIndex.get(fromId);
        if (from == null) {
            System.out.println("Unknown location id.");
            return;
        }
        List<Integer> bfsOrder = graph.bfs(from);
        List<Integer> dfsOrder = graph.dfs(from);
        System.out.println("Reachable via BFS: " + bfsOrder.size() + " of " + locations.size() + " locations");
        System.out.println("Reachable via DFS: " + dfsOrder.size() + " of " + locations.size() + " locations");
        System.out.println("BFS visit order (first 10): " + namesOf(bfsOrder.subList(0, Math.min(10, bfsOrder.size()))));
        System.out.println("DFS visit order (first 10): " + namesOf(dfsOrder.subList(0, Math.min(10, dfsOrder.size()))));
    }

    private static String namesOf(List<Integer> graphIndices) {
        StringBuilder sb = new StringBuilder();
        for (int idx : graphIndices) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(locations.get(idx).name());
        }
        return sb.toString();
    }

    private static void mstDemo() throws Exception {
        if (graph == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        List<double[]> mst = graph.kruskalMST();
        double total = 0;
        for (double[] e : mst)
            total += e[2];
        System.out.printf("Kruskal MST: %d edges, total weight %.1f%n", mst.size(), total);
    }

    private static void searchDemo() {
        if (requests == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        Integer[] sorted = requests.stream().map(ServiceRequest::requestId).toArray(Integer[]::new);
        Arrays.sort(sorted);
        int target = sorted[sorted.length / 3];

        long t0 = System.nanoTime();
        int linearIdx = SearchAlgorithms.linearSearch(sorted, target);
        long linearNs = System.nanoTime() - t0;

        t0 = System.nanoTime();
        int binaryIdx = SearchAlgorithms.binarySearch(sorted, target);
        long binaryNs = System.nanoTime() - t0;

        System.out.printf("Sorted array of %d request ids. Target = request #%d%n", sorted.length, target);
        System.out.printf("Linear search: found at index %d in %d ns%n", linearIdx, linearNs);
        System.out.printf("Binary search: found at index %d in %d ns%n", binaryIdx, binaryNs);

        System.out.println("Invalid-precondition counterexample (Section 10): binary search requires a SORTED array.");
        Integer[] unsorted = sorted.clone();
        java.util.Random shuffleRng = new java.util.Random(ParameterDerivation.randomSeed());
        for (int i = unsorted.length - 1; i > 0; i--) {
            int j = shuffleRng.nextInt(i + 1);
            Integer tmp = unsorted[i];
            unsorted[i] = unsorted[j];
            unsorted[j] = tmp;
        }
        int wrongIdx = SearchAlgorithms.binarySearch(unsorted, target);
        boolean lookedRight = wrongIdx >= 0 && unsorted[wrongIdx].equals(target);
        System.out.printf(
                "Running binary search on the UNSORTED array returns index %d (%s) -- this is why the precondition matters.%n",
                wrongIdx, lookedRight ? "happened to look right by chance"
                        : "wrong / inconsistent result (target is actually present but not found, or the wrong index is returned)");
    }

    private interface SortFn {
        void sort(Integer[] arr);
    }

    private static void sortDemo() throws Exception {
        if (requests == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        Integer[] ids = requests.stream().map(ServiceRequest::requestId).toArray(Integer[]::new);

        timeAndLogSort("SelectionSort", ids.clone(), SortAlgorithms::selectionSort);
        timeAndLogSort("InsertionSort", ids.clone(), SortAlgorithms::insertionSort);
        timeAndLogSort("MergeSort", ids.clone(), SortAlgorithms::mergeSort);
        timeAndLogSort("QuickSort", ids.clone(), SortAlgorithms::quickSort);
        System.out.println("All four runs logged to the algorithm_runs table.");
    }

    private static void timeAndLogSort(String name, Integer[] arr, SortFn fn) throws Exception {
        long start = System.nanoTime();
        fn.sort(arr);
        long elapsed = System.nanoTime() - start;
        System.out.printf("%-14s sorted %d request ids in %d ns%n", name, arr.length, elapsed);
        db.insertAlgorithmRun(name, arr.length, elapsed, 0);
    }

    private static void hashTableDemo() {
        if (requests == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        int capacity = ParameterDerivation.hashTableSize();
        HashTable<Integer, String> table = new HashTable<>(capacity);
        for (ServiceRequest r : requests)
            table.put(r.requestId(), r.urgency());

        System.out.printf(
                "HashTable(capacity=%d, from ParameterDerivation.hashTableSize()): %d entries, loadFactor=%.3f, collisions=%d%n",
                capacity, table.size(), table.loadFactor(), table.collisionCount);

        int sampleId = requests.get(0).requestId();
        System.out.printf("get(%d) -> %s%n", sampleId, table.get(sampleId));
        table.remove(sampleId);
        System.out.printf("boundary case: after remove(%d), get(...) -> %s%n", sampleId, table.get(sampleId));
    }

    private static void setDemo() {
        if (locations == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        HashSetCustom<String> areas = new HashSetCustom<>(64);
        for (Location loc : locations)
            areas.add(loc.area());
        System.out.printf("HashSetCustom of distinct service areas: %d unique areas among %d locations%n",
                areas.size(), locations.size());
        String sampleArea = locations.get(0).area();
        System.out.printf("contains(\"%s\") -> %b%n", sampleArea, areas.contains(sampleArea));
        System.out.printf("contains(\"Nonexistent Area\") -> %b (membership/lookup use case)%n",
                areas.contains("Nonexistent Area"));
    }

    private static void bstDemo() {
        if (requests == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        BinarySearchTree<Integer, String> bst = new BinarySearchTree<>();
        List<ServiceRequest> sample = requests.subList(0, Math.min(15, requests.size()));
        for (ServiceRequest r : sample)
            bst.insert(r.requestId(), r.category());

        int target = sample.get(sample.size() / 2).requestId();
        String found = bst.search(target);
        System.out.printf("BST search(%d) -> %s%n", target, found);
        System.out.println("Search path (evidence required by Section 6): " + bst.lastSearchPath);

        System.out.print("Inorder traversal (sorted keys): ");
        bst.inorderTraversal((k, v) -> System.out.print(k + " "));
        System.out.println();
        System.out.println("Height: " + bst.height());
    }

    private static void redBlackTreeDemo() {
        if (requests == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        RedBlackTree<Integer, String> rbt = new RedBlackTree<>();
        List<ServiceRequest> sample = requests.subList(0, Math.min(12, requests.size()));
        System.out.println("Red-black tree insert trace (rotation/recolouring evidence required by Section 6):");
        for (ServiceRequest r : sample) {
            rbt.insert(r.requestId(), r.category());
            String log = rbt.lastInsertRotationLog.isEmpty() ? "no rebalancing" : rbt.lastInsertRotationLog.toString();
            System.out.printf("  insert(%d) -> %s (height now %d)%n", r.requestId(), log, rbt.height());
        }
        int target = sample.get(0).requestId();
        System.out.printf("search(%d) -> %s%n", target, rbt.search(target));
    }

    private static void bTreeDemo() {
        if (requests == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        BTree<Integer, String> btree = new BTree<>(2); // small minDegree so splits happen quickly and are visible
        List<ServiceRequest> sample = requests.subList(0, Math.min(12, requests.size()));
        System.out.println("B-tree insert trace (minDegree=2, node-split evidence required by Section 6):");
        for (ServiceRequest r : sample) {
            int before = btree.splitLog.size();
            btree.insert(r.requestId(), r.category());
            for (int i = before; i < btree.splitLog.size(); i++) {
                System.out.println("  insert(" + r.requestId() + ") -> " + btree.splitLog.get(i));
            }
        }
        int target = sample.get(0).requestId();
        System.out.printf("search(%d) -> %s%n", target, btree.search(target));
    }

    private static void disjointSetDemo() {
        if (graph == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        List<Road> sorted = new ArrayList<>(roads);
        sorted.sort(Comparator.comparingDouble(Road::travelTimeMin));
        DisjointSet ds = new DisjointSet(locations.size());

        System.out.println("Kruskal connectivity trace (union-find over roads sorted by travel time, first 15 shown):");
        int edgesKept = 0, shown = 0;
        for (Road r : sorted) {
            Integer a = locationIdToIndex.get(r.fromLocationId());
            Integer b = locationIdToIndex.get(r.toLocationId());
            if (a == null || b == null)
                continue;
            boolean unioned = ds.union(a, b);
            if (shown < 15) {
                System.out.printf("  union(%s, %s) -> %s%n", locations.get(a).name(), locations.get(b).name(),
                        unioned ? "connected, edge kept" : "already connected, cycle, edge skipped");
                shown++;
            }
            if (unioned)
                edgesKept++;
        }
        System.out.printf(
                "Total union() calls: %d, edges kept in MST: %d (full trace available in DisjointSet.unionTrace)%n",
                ds.unionTrace.size(), edgesKept);
    }

    private static void circularQueueDemo() {
        CircularQueue<Integer> cq = new CircularQueue<>(5);
        System.out.println("CircularQueue(capacity=5) front/rear wrap-around trace (Section 6 evidence):");
        for (int i = 1; i <= 5; i++) {
            cq.enqueue(i);
            System.out.printf("  enqueue(%d) -> size=%d, isFull=%b%n", i, cq.size(), cq.isFull());
        }
        for (int i = 0; i < 2; i++) {
            int v = cq.dequeue();
            System.out.printf("  dequeue() -> %d, size=%d%n", v, cq.size());
        }
        for (int i = 6; i <= 7; i++) {
            cq.enqueue(i);
            System.out.printf("  enqueue(%d) -> size=%d (index wrapped past the array end)%n", i, cq.size());
        }
        System.out.println("Final state: isFull=" + cq.isFull() + ", isEmpty=" + cq.isEmpty() + ", size=" + cq.size());
    }

    private static void dequeDemo() {
        if (requests == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        ArrayDequeCustom<Integer> deque = new ArrayDequeCustom<>();
        int added = 0;
        ServiceRequest urgent = null;
        for (ServiceRequest r : requests) {
            if ("CRITICAL".equals(r.urgency()) && urgent == null) {
                urgent = r;
                continue;
            }
            if (added < 4) {
                deque.addRear(r.requestId());
                System.out.println("addRear(" + r.requestId() + ") [normal-priority request]");
                added++;
            }
        }
        if (urgent != null) {
            deque.addFront(urgent.requestId());
            System.out.println(
                    "addFront(" + urgent.requestId() + ") [CRITICAL request jumps the queue -- Section 6 evidence]");
        }
        System.out.println("removeFront() -> " + deque.removeFront()
                + (urgent != null ? " (the urgent one is served first)" : ""));
        System.out.println("removeRear() -> " + deque.removeRear());
        System.out.println("deque size now: " + deque.size());
    }

    private static void undoDemo() throws Exception {
        if (resources == null || resources.isEmpty()) {
            System.out.println("Load data first (option 2).");
            return;
        }
        ArrayStack<String> undoLog = new ArrayStack<>();
        Resource target = resources.get(0);

        System.out.printf("Assigning resource #%d (%s), currently %s...%n", target.resourceId(), target.type(),
                target.availabilityStatus());
        undoLog.push(target.availabilityStatus()); // remember prior status so we can undo
        db.insertAuditEvent("ASSIGN", "RESOURCE", target.resourceId(),
                "assigned to dispatch demo (was " + target.availabilityStatus() + ")");
        System.out.println(
                "  pushed \"" + target.availabilityStatus() + "\" onto the undo stack, logged ASSIGN to audit_events");

        System.out.println("Undoing last action...");
        String previousStatus = undoLog.pop();
        db.insertAuditEvent("UNDO", "RESOURCE", target.resourceId(), "reverted to " + previousStatus);
        System.out.println("  popped \"" + previousStatus + "\" off the undo stack, logged UNDO to audit_events");
        System.out.println("  undo stack isEmpty=" + undoLog.isEmpty());
    }

    private static void greedyDemo() {
        // Six ambulance jobs competing for one shared resource. Job A has the
        // EARLIEST deadline but occupies the whole [0,10] window; jobs B-F are
        // short, back-to-back, and individually less urgent by deadline. This
        // is the counterexample documented in GreedyResourceAssignment
        // (Section 10): earliest-deadline-first greedily locks in A first and
        // then has no room left for anything else, while the optimal
        // (earliest-finish-time) schedule serves five short jobs instead.
        GreedyResourceAssignment.Job a = new GreedyResourceAssignment.Job("A", 0, 10);
        GreedyResourceAssignment.Job b = new GreedyResourceAssignment.Job("B", 0, 2);
        GreedyResourceAssignment.Job c = new GreedyResourceAssignment.Job("C", 2, 4);
        GreedyResourceAssignment.Job d = new GreedyResourceAssignment.Job("D", 4, 6);
        GreedyResourceAssignment.Job e = new GreedyResourceAssignment.Job("E", 6, 8);
        GreedyResourceAssignment.Job f = new GreedyResourceAssignment.Job("F", 8, 10);
        List<GreedyResourceAssignment.Job> jobs = List.of(a, b, c, d, e, f);
        Map<GreedyResourceAssignment.Job, Double> deadlines = Map.of(
                a, 10.0, b, 20.0, c, 20.0, d, 20.0, e, 20.0, f, 20.0);

        List<GreedyResourceAssignment.Job> optimal = GreedyResourceAssignment.selectMaxNonOverlapping(jobs);
        List<GreedyResourceAssignment.Job> edfGreedy = GreedyResourceAssignment.earliestDeadlineFirstGreedy(jobs,
                deadlines);

        System.out.println("Activity-selection greedy (optimal for max non-overlapping jobs): " + optimal.size()
                + " served -> " + jobNames(optimal));
        System.out.println("Earliest-deadline-first greedy (COUNTEREXAMPLE):                   " + edfGreedy.size()
                + " served -> " + jobNames(edfGreedy));
        if (edfGreedy.size() < optimal.size()) {
            System.out.println(
                    "-> earliest-deadline-first is NOT optimal: it locks in job A (deadline=10) first, which occupies the whole [0,10]"
                            + " window and leaves no room for B-F, so only 1 job is served instead of the optimal "
                            + optimal.size() + ".");
        }
    }

    private static String jobNames(List<GreedyResourceAssignment.Job> jobs) {
        StringBuilder sb = new StringBuilder();
        for (GreedyResourceAssignment.Job j : jobs)
            sb.append(j.id()).append(" ");
        return sb.toString().trim();
    }

    private static void dpDemo() {
        if (requests == null) {
            System.out.println("Load data first (option 2).");
            return;
        }
        List<DPRequestSelection.Request> pool = new ArrayList<>();
        List<ServiceRequest> sample = requests.subList(0, Math.min(10, requests.size()));
        for (ServiceRequest r : sample) {
            int cost = urgencyRank(r.urgency()) * 10 + 15; // rough estimated minutes to serve
            int value = (4 - urgencyRank(r.urgency())) * ParameterDerivation.priorityWeight();
            pool.add(new DPRequestSelection.Request("req-" + r.requestId(), cost, value));
        }
        int budgetMinutes = 60; // e.g. one ambulance's available minutes this hour
        DPRequestSelection.Result result = DPRequestSelection.solve(pool, budgetMinutes);

        System.out.printf("0/1 knapsack request selection: budget=%d minutes, %d candidate requests%n", budgetMinutes,
                pool.size());
        System.out.println("Chosen subset (maximises total urgency-weighted value within budget):");
        for (DPRequestSelection.Request r : result.chosen())
            System.out.printf("  %s (cost=%d, value=%d)%n", r.id(), r.costMinutes(), r.value());
        System.out.println("Total value achieved: " + result.totalValue());
        System.out.println("DP table, last row (reconstruction evidence required by Section 7): "
                + Arrays.toString(result.table()[pool.size()]));
    }

    private static void efficiencyLabDemo() throws Exception {
        String outPath = projectRoot.resolve("efficiency_results.csv").toString();
        System.out.println(
                "Running all 6 Section 9 experiments (search, sort, hash load factor, tree comparison, heap dispatch, graph algorithms)...");
        EfficiencyExperimentRunner.runAll(outPath);
    }
}
