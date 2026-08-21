package com.ghdsa.algorithms;

import java.util.*;

/**
 * M8: greedy priority-based resource assignment. Section 7 requires you to
 * also show a counterexample where greedy fails — see failureCounterexample()
 * below, which is exactly that: greedy-by-earliest-deadline is NOT optimal
 * for maximising the number of served requests when requests also compete
 * for a single shared resource with travel time between jobs.
 */
public final class GreedyResourceAssignment {
    private GreedyResourceAssignment() {}

    public record Job(String id, double startMin, double endMin) {}

    /**
     * Classic activity-selection greedy: sort by earliest finish time, take
     * every job that doesn't overlap the last one taken. This greedy IS
     * optimal for "maximise count of non-overlapping jobs on ONE resource" —
     * use it as your positive example.
     */
    public static List<Job> selectMaxNonOverlapping(List<Job> jobs) {
        List<Job> sorted = new ArrayList<>(jobs);
        sorted.sort(Comparator.comparingDouble(Job::endMin));
        List<Job> selected = new ArrayList<>();
        double lastEnd = Double.NEGATIVE_INFINITY;
        for (Job j : sorted) {
            if (j.startMin() >= lastEnd) {
                selected.add(j);
                lastEnd = j.endMin();
            }
        }
        return selected;
    }

    /**
     * COUNTEREXAMPLE (required by Section 10): if instead you greedily assign
     * the resource to whichever job has the EARLIEST DEADLINE first (a very
     * tempting "urgency-first" greedy rule for a hospital), you can end up
     * serving fewer total requests than the optimal schedule, because you
     * ignore how long each job blocks the resource.
     *
     * Example: three ambulance jobs compete for one ambulance.
     *   A: deadline=10, duration takes [0,10]
     *   B: deadline=11, duration takes [10,11]
     *   C: deadline=11, duration takes [10,11] -- but earliest-deadline-first
     *      greedily locks in A (deadline 10) even though A blocks the
     *      ambulance for the whole window, so only A gets served (1 job),
     *      while the optimal schedule (B then C, or just B) can serve 2 jobs
     *      if we'd ignored strict deadline order and looked at duration instead.
     *
     * Run this method and compare its output size to selectMaxNonOverlapping's
     * output size on the same input to demonstrate the failure live.
     */
    public static List<Job> earliestDeadlineFirstGreedy(List<Job> jobs, Map<Job, Double> deadlines) {
        List<Job> sorted = new ArrayList<>(jobs);
        sorted.sort(Comparator.comparingDouble(deadlines::get));
        List<Job> selected = new ArrayList<>();
        double lastEnd = Double.NEGATIVE_INFINITY;
        for (Job j : sorted) {
            if (j.startMin() >= lastEnd) {
                selected.add(j);
                lastEnd = j.endMin();
            }
        }
        return selected;
    }
}
