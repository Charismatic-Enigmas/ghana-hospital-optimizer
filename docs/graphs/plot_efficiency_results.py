#!/usr/bin/env python3
"""
Plots the 6 required Section 9 efficiency graphs from efficiency_results.csv
(produced by menu option 20 / EfficiencyExperimentRunner.runAll()).

Pure Python standard library only (no pip install required -- see
svg_chart.py) -- outputs SVG, viewable in any browser and embeddable
directly in the report.

USAGE:
    python3 plot_efficiency_results.py [path/to/efficiency_results.csv]

Outputs 6 SVGs into this directory: search_runtime.svg, sort_comparison.svg,
hash_load_factor.svg, bst_vs_balanced.svg, heap_dispatch.svg,
graph_algorithms.svg.
"""
import csv
import re
import sys
from collections import defaultdict
from pathlib import Path

from svg_chart import line_chart, dual_axis_chart

HERE = Path(__file__).parent
CSV_PATH = Path(sys.argv[1]) if len(sys.argv) > 1 else HERE.parent.parent / "efficiency_results.csv"


def load_rows():
    with open(CSV_PATH, newline="") as f:
        return list(csv.DictReader(f))


def mean_by(rows, experiment, key_fn):
    """Groups rows for `experiment` by key_fn(row) -> series label, then by
    input_size, averaging time_ns across the repeated runs."""
    buckets = defaultdict(lambda: defaultdict(list))
    for r in rows:
        if r["experiment"] != experiment:
            continue
        label = key_fn(r)
        size = int(r["input_size"])
        buckets[label][size].append(int(r["time_ns"]))
    series = {}
    for label, by_size in buckets.items():
        sizes = sorted(by_size)
        series[label] = (sizes, [sum(by_size[s]) / len(by_size[s]) for s in sizes])
    return series


def main():
    rows = load_rows()

    # 1. Search comparison: linear vs binary search runtime
    series = mean_by(rows, "search_comparison", lambda r: r["algorithm"])
    line_chart(series, "Search comparison: linear vs binary search",
               "input size (records)", "mean time (ns, log scale)",
               HERE / "search_runtime.svg", ylog=True)

    # 2. Sorting comparison: selection/insertion/merge/quicksort
    series = mean_by(rows, "sorting_comparison", lambda r: r["algorithm"])
    line_chart(series, "Sorting comparison",
               "input size (requests)", "mean time (ns, log scale)",
               HERE / "sort_comparison.svg", ylog=True)

    # 3. Hash table load factor vs collisions/time
    hash_rows = [r for r in rows if r["experiment"] == "hash_load_factor"]
    by_size = defaultdict(lambda: {"load_factors": [], "collisions": [], "times": []})
    for r in hash_rows:
        m = re.search(r"loadFactor=([\d.]+);collisions=(\d+)", r["algorithm"])
        size = int(r["input_size"])
        by_size[size]["load_factors"].append(float(m.group(1)))
        by_size[size]["collisions"].append(int(m.group(2)))
        by_size[size]["times"].append(int(r["time_ns"]))
    sizes = sorted(by_size)
    labels = [f"{sum(by_size[s]['load_factors'])/len(by_size[s]['load_factors']):.1f}" for s in sizes]
    collisions = [sum(by_size[s]["collisions"]) / len(by_size[s]["collisions"]) for s in sizes]
    times = [sum(by_size[s]["times"]) / len(by_size[s]["times"]) for s in sizes]
    dual_axis_chart(labels, ("collisions", collisions), ("insert time (ns)", times),
                     "Hash table: load factor vs collisions/time",
                     "load factor (keys / table size=1024)", "mean collision count", "mean insert time (ns)",
                     HERE / "hash_load_factor.svg")

    # 4. BST vs balanced tree: height and time
    tree_rows = [r for r in rows if r["experiment"] == "bst_vs_balanced"]
    heights = defaultdict(lambda: defaultdict(list))
    times2 = defaultdict(lambda: defaultdict(list))
    for r in tree_rows:
        m = re.match(r"(\w+)\(height=(\d+)\)", r["algorithm"])
        name, height = m.group(1), int(m.group(2))
        size = int(r["input_size"])
        heights[name][size].append(height)
        times2[name][size].append(int(r["time_ns"]))

    height_series = {}
    for name in heights:
        sizes_h = sorted(heights[name])
        height_series[name] = (sizes_h, [sum(heights[name][s]) / len(heights[name][s]) for s in sizes_h])
    line_chart(height_series, "BST vs balanced tree: height (sorted-order inserts, worst case for BST)",
               "input size", "mean height", HERE / "bst_vs_balanced_height.svg")

    time_series = {}
    for name in times2:
        sizes_t = sorted(times2[name])
        time_series[name] = (sizes_t, [sum(times2[name][s]) / len(times2[name][s]) for s in sizes_t])
    line_chart(time_series, "BST vs balanced tree: total insert time",
               "input size", "mean time (ns, log scale)", HERE / "bst_vs_balanced_time.svg", ylog=True)

    # 5. Heap priority dispatch
    series = mean_by(rows, "heap_dispatch", lambda r: r["algorithm"])
    line_chart(series, "Heap priority dispatch: insert-then-extract-all",
               "input size (requests)", "mean time (ns)", HERE / "heap_dispatch.svg")

    # 6. Graph algorithms: BFS/DFS/Dijkstra/Kruskal
    series = mean_by(rows, "graph_algorithms", lambda r: r["algorithm"])
    line_chart(series, "Graph algorithms runtime vs graph size",
               "input size (locations/vertices)", "mean time (ns, log scale)",
               HERE / "graph_algorithms.svg", ylog=True)


if __name__ == "__main__":
    main()
