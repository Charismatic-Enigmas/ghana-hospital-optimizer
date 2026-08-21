package com.ghdsa.algorithms;

import java.util.*;

/**
 * M8: 0/1 knapsack-style dynamic programming — selecting the subset of
 * service requests that fits a resource/budget constraint (e.g. total
 * ambulance-minutes available today) while maximising total urgency-weighted
 * value served. Section 7.
 *
 * Uses tabulation (bottom-up) and reconstructs the chosen subset, per the
 * brief's requirement for a "memoisation or tabulation table and reconstruction".
 */
public final class DPRequestSelection {
    private DPRequestSelection() {}

    public record Request(String id, int costMinutes, int value) {}

    public record Result(int totalValue, List<Request> chosen, int[][] table) {}

    public static Result solve(List<Request> requests, int budgetMinutes) {
        int n = requests.size();
        int[][] table = new int[n + 1][budgetMinutes + 1];

        for (int i = 1; i <= n; i++) {
            Request r = requests.get(i - 1);
            for (int w = 0; w <= budgetMinutes; w++) {
                table[i][w] = table[i - 1][w]; // don't take request i
                if (r.costMinutes() <= w) {
                    table[i][w] = Math.max(table[i][w], table[i - 1][w - r.costMinutes()] + r.value());
                }
            }
        }

        // reconstruction: walk back through the table
        List<Request> chosen = new ArrayList<>();
        int w = budgetMinutes;
        for (int i = n; i > 0; i--) {
            if (table[i][w] != table[i - 1][w]) {
                Request r = requests.get(i - 1);
                chosen.add(r);
                w -= r.costMinutes();
            }
        }
        Collections.reverse(chosen);
        return new Result(table[n][budgetMinutes], chosen, table);
    }
}
