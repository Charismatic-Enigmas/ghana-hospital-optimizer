#!/usr/bin/env python3
"""
Generates seed CSVs for the Ghana Smart Hospital Service Operations Optimizer.
Meets the brief's minimums: locations>=50, roads>=100, service_requests>=300,
resources>=30, algorithm_runs seeded with a starter batch (>=30; more will be
appended by your own experiment runs in Section 9).

USAGE:
    python3 generate_data.py
Outputs: locations.csv, roads.csv, service_requests.csv, resources.csv, algorithm_runs.csv
in the same folder.

IMPORTANT (Section 2.iii of the brief): each team must derive at least three
algorithm parameters from member index numbers (priority weight, route penalty,
hash-table size, random seed, budget constraint, etc). This script seeds a
default random SEED — replace it with a value derived from your team's index
numbers before your final run, and document how you derived it in your report.
"""

import csv
import math
import random
from datetime import datetime, timedelta

SEED = 680000  # derived value, matches ParameterDerivation.randomSeed() in the Java app
               # (product of (index_number % 97 + 1) across all 15 member index numbers, mod 1_000_000)
random.seed(SEED)

# ---------------------------------------------------------------------------
# Reference data: real Ghanaian health facilities in Greater Accra, plus
# department/ward types found within each. Base coordinates below were
# verified against public geocoding sources -- originally Wikipedia/Wikidata,
# latitude.to, mapcarta and Google-Maps-derived DMS conversions, then
# re-verified against OpenStreetMap/Nominatim/Overpass (the same data the
# Geoplaner coordinate tool displays). Four facilities were corrected to the
# OSM-mapped facility positions; Dodowa has no mapped POI so its coordinate
# comes from the co-located "Hospital Junction"/"Old Hospital" bus stops on
# the R40. See docs/evidence_note.md for the per-facility source citations
# and precision notes.
# ---------------------------------------------------------------------------
FACILITIES = [
    ("University of Ghana Hospital, Legon", "Legon", 5.6518522, -0.1781069),  # OSM way centroid
    ("Korle-Bu Teaching Hospital", "Korle-Bu", 5.5383167, -0.2268145),        # OSM POI (within real campus)
    ("Ridge Hospital", "Ridge", 5.5621112, -0.1989306),                       # OSM POI (62m from previous)
    ("37 Military Hospital", "Cantonments", 5.5868922, -0.1850474),           # OSM POI (37m from previous)
    ("La General Hospital", "La", 5.5555041, -0.1665637),                     # OSM POI (14m from previous)
    ("Tema General Hospital", "Tema", 5.6741683, -0.0250123),                 # OSM POI (was town-centre stand-in)
    ("Madina Polyclinic", "Madina", 5.6675839, -0.1652296),                   # OSM POI (was town-centre stand-in)
    ("Achimota Hospital", "Achimota", 5.629622, -0.2172443),                  # OSM POI (24m from previous)
    ("Nyaho Medical Centre", "Airport Residential", 5.5514896, -0.2039394),   # OSM POI (exact match)
    ("Dodowa Health Centre", "Dodowa", 5.8860469, -0.0923176),                # no POI; from adjacent bus stops
]

DEPT_TYPES = [
    ("Emergency Ward", "WARD"),
    ("Out-Patient Department", "OPD"),
    ("Maternity Ward", "WARD"),
    ("Pharmacy", "PHARMACY"),
    ("Laboratory", "LAB"),
    ("Intensive Care Unit", "ICU"),
    ("Operating Theatre", "THEATRE"),
    ("Radiology", "LAB"),
    ("Administration", "ADMIN"),
    ("Main Gate", "GATE"),
]

REQUEST_CATEGORIES = ["AMBULANCE", "REFERRAL", "PHARMACY", "LAB_SAMPLE", "EQUIPMENT", "PORTER"]
URGENCY_LEVELS = ["CRITICAL", "HIGH", "MEDIUM", "LOW"]
STATUSES = ["PENDING", "ASSIGNED", "IN_PROGRESS", "COMPLETED", "CANCELLED"]
RESOURCE_TYPES = ["AMBULANCE", "PORTER", "NURSE", "WHEELCHAIR", "COURIER_BIKE"]


def jitter(coord, spread=0.0025):
    # spread=0.0025 deg =~ 250-280 m at Accra's latitude -- keeps every
    # department node within the real hospital compound/grounds instead of
    # scattering across unrelated parts of the city.
    return round(coord + random.uniform(-spread, spread), 6)


def gen_locations(min_count=50):
    rows = []
    loc_id = 1
    for fac_name, area, lat, lon in FACILITIES:
        for dept_name, dept_type in DEPT_TYPES:
            rows.append({
                "location_id": loc_id,
                "name": f"{fac_name.split(',')[0]} - {dept_name}",
                "facility": fac_name,
                "area": area,
                "type": dept_type,
                "latitude": jitter(lat),
                "longitude": jitter(lon),
            })
            loc_id += 1
    assert len(rows) >= min_count, f"only {len(rows)} locations generated"
    return rows


AVG_SPEED_KMH = 20.0  # assumed average urban Accra travel speed (documented in
                       # docs/evidence_note.md) -- road_condition_weight then
                       # scales this baseline up for traffic/road quality.


def haversine_km(lat1, lon1, lat2, lon2):
    """Great-circle distance between two real coordinates, in km."""
    r = 6371.0
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi = math.radians(lat2 - lat1)
    dlambda = math.radians(lon2 - lon1)
    a = math.sin(dphi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlambda / 2) ** 2
    return 2 * r * math.asin(math.sqrt(a))


def mst_edges(points):
    """points: list of (id, lat, lon). Returns MST edges as (id_a, id_b, distance_km)
    via Prim's algorithm over real haversine distances -- guarantees every point
    is connected to every other, using the minimum total real-world distance."""
    n = len(points)
    if n < 2:
        return []
    in_tree = [False] * n
    in_tree[0] = True
    dist_to_tree = [haversine_km(points[0][1], points[0][2], p[1], p[2]) for p in points]
    nearest_in_tree = [0] * n
    edges = []
    for _ in range(n - 1):
        best = min((j for j in range(n) if not in_tree[j]), key=lambda j: dist_to_tree[j])
        in_tree[best] = True
        edges.append((points[nearest_in_tree[best]][0], points[best][0], dist_to_tree[best]))
        for j in range(n):
            if not in_tree[j]:
                d = haversine_km(points[best][1], points[best][2], points[j][1], points[j][2])
                if d < dist_to_tree[j]:
                    dist_to_tree[j] = d
                    nearest_in_tree[j] = best
    return edges


def threshold_edges(points, threshold_km, existing_pairs):
    """Extra edges for every pair within threshold_km of each other and not
    already connected -- redundancy for a more Kruskal/Prim-interesting graph."""
    seen = set(existing_pairs)
    edges = []
    for i in range(len(points)):
        id_i, lat_i, lon_i = points[i]
        for j in range(i + 1, len(points)):
            id_j, lat_j, lon_j = points[j]
            key = tuple(sorted((id_i, id_j)))
            if key in seen:
                continue
            d = haversine_km(lat_i, lon_i, lat_j, lon_j)
            if d <= threshold_km:
                seen.add(key)
                edges.append((id_i, id_j, d))
    return edges


def nearest_neighbour_edges(points, k, existing_pairs):
    """Extra edges: each point's k nearest neighbours, skipping pairs already
    connected -- used for the inter-facility referral backbone."""
    seen = set(existing_pairs)
    edges = []
    for i, (id_i, lat_i, lon_i) in enumerate(points):
        candidates = sorted(
            (haversine_km(lat_i, lon_i, lat_j, lon_j), id_j)
            for j, (id_j, lat_j, lon_j) in enumerate(points) if j != i
        )
        added = 0
        for d, id_j in candidates:
            key = tuple(sorted((id_i, id_j)))
            if key in seen:
                continue
            seen.add(key)
            edges.append((id_i, id_j, d))
            added += 1
            if added >= k:
                break
    return edges


def gen_roads(locations, min_count=100):
    """Builds the road network from real geography (haversine distance between
    each location's actual lat/lon), not independent random numbers -- see
    docs/evidence_note.md for why (fixes shortest-path results that used to
    route through unrelated hospitals across the city for a same-campus trip).

    Structure: every facility's 10 departments get a guaranteed-connected local
    network (MST + short-distance redundancy), and the 10 facilities themselves
    get a guaranteed-connected inter-facility referral backbone (MST over each
    facility's Main Gate + nearest-neighbour redundancy) -- both built from real
    coordinates, so a same-campus trip is always short and a cross-city
    referral is always realistically long.
    """
    rows = []
    road_id = 1
    all_pairs_seen = set()

    def add_edge(a_id, b_id, distance_km):
        nonlocal road_id
        key = tuple(sorted((a_id, b_id)))
        if key in all_pairs_seen:
            return
        all_pairs_seen.add(key)
        road_condition_weight = round(random.uniform(1.0, 3.5), 2)
        travel_time_min = round((distance_km / AVG_SPEED_KMH) * 60 * road_condition_weight, 2)
        rows.append({
            "road_id": road_id,
            "from_location_id": a_id,
            "to_location_id": b_id,
            "distance_km": round(distance_km, 2),
            "travel_time_min": travel_time_min,
            "road_condition_weight": road_condition_weight,
        })
        road_id += 1

    by_facility = {}
    for loc in locations:
        by_facility.setdefault(loc["facility"], []).append(loc)

    main_gates = []
    for locs in by_facility.values():
        points = [(l["location_id"], l["latitude"], l["longitude"]) for l in locs]
        for a_id, b_id, d in mst_edges(points):
            add_edge(a_id, b_id, d)
        for a_id, b_id, d in threshold_edges(points, threshold_km=0.4, existing_pairs=all_pairs_seen):
            add_edge(a_id, b_id, d)
        gate = next(l for l in locs if l["type"] == "GATE")
        main_gates.append((gate["location_id"], gate["latitude"], gate["longitude"]))

    for a_id, b_id, d in mst_edges(main_gates):
        add_edge(a_id, b_id, d)
    for a_id, b_id, d in nearest_neighbour_edges(main_gates, k=2, existing_pairs=all_pairs_seen):
        add_edge(a_id, b_id, d)

    # safety net -- shouldn't be needed at this dataset's scale, but guarantees
    # the brief's roads>=100 minimum is met even if the topology above ever
    # produces fewer edges for a smaller/different dataset
    ids = [l["location_id"] for l in locations]
    by_id = {l["location_id"]: l for l in locations}
    while len(rows) < min_count:
        a, b = random.sample(ids, 2)
        if tuple(sorted((a, b))) in all_pairs_seen:
            continue
        d = haversine_km(by_id[a]["latitude"], by_id[a]["longitude"], by_id[b]["latitude"], by_id[b]["longitude"])
        add_edge(a, b, d)

    return rows


def gen_service_requests(locations, min_count=300):
    rows = []
    ids = [l["location_id"] for l in locations]
    base_time = datetime(2026, 1, 6, 7, 0, 0)  # a Tuesday morning
    for i in range(1, min_count + 1):
        src, dst = random.sample(ids, 2)
        submitted = base_time + timedelta(minutes=random.randint(0, 60 * 24 * 21))
        urgency = random.choices(URGENCY_LEVELS, weights=[10, 25, 40, 25])[0]
        sla_minutes = {"CRITICAL": 15, "HIGH": 60, "MEDIUM": 180, "LOW": 720}[urgency]
        deadline = submitted + timedelta(minutes=sla_minutes)
        rows.append({
            "request_id": i,
            "source_location": src,
            "dest_location": dst,
            "category": random.choice(REQUEST_CATEGORIES),
            "urgency": urgency,
            "time_submitted": submitted.isoformat(sep=" "),
            "deadline": deadline.isoformat(sep=" "),
            "status": random.choices(STATUSES, weights=[15, 15, 15, 45, 10])[0],
        })
    return rows


def gen_resources(locations, min_count=30):
    rows = []
    ids = [l["location_id"] for l in locations]
    for i in range(1, min_count + 1):
        rtype = random.choice(RESOURCE_TYPES)
        capacity = {"AMBULANCE": random.randint(1, 2), "PORTER": 1, "NURSE": 1,
                    "WHEELCHAIR": 1, "COURIER_BIKE": 1}[rtype]
        rows.append({
            "resource_id": i,
            "type": rtype,
            "home_location": random.choice(ids),
            "capacity": capacity,
            "availability_status": random.choices(
                ["AVAILABLE", "BUSY", "OFF_DUTY", "MAINTENANCE"], weights=[55, 30, 10, 5])[0],
        })
    return rows


def gen_algorithm_runs_starter(min_count=30):
    """A small starter batch so the table isn't empty; your Section 9 experiments
    will insert the real measured rows as you build M4/M7/M10."""
    algos = ["LinearSearch", "BinarySearch", "SelectionSort", "InsertionSort",
             "MergeSort", "QuickSort", "BFS", "DFS", "Dijkstra", "Kruskal"]
    sizes = [100, 500, 1000, 5000, 10000]
    rows = []
    run_id = 1
    for algo in algos:
        for size in random.sample(sizes, k=3):
            rows.append({
                "run_id": run_id,
                "algorithm_name": algo,
                "input_size": size,
                "time_ns": random.randint(1000, 50_000_000),
                "memory_kb": round(random.uniform(10, 5000), 2),
                "date_run": datetime(2026, 2, 1).isoformat(sep=" "),
            })
            run_id += 1
    return rows[:max(min_count, len(rows))]


def write_csv(filename, rows, fieldnames):
    with open(filename, "w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(rows)
    print(f"wrote {len(rows):5d} rows -> {filename}")


if __name__ == "__main__":
    locations = gen_locations()
    roads = gen_roads(locations)
    requests = gen_service_requests(locations)
    resources = gen_resources(locations)
    algo_runs = gen_algorithm_runs_starter()

    write_csv("locations.csv", locations,
              ["location_id", "name", "facility", "area", "type", "latitude", "longitude"])
    write_csv("roads.csv", roads,
              ["road_id", "from_location_id", "to_location_id", "distance_km",
               "travel_time_min", "road_condition_weight"])
    write_csv("service_requests.csv", requests,
              ["request_id", "source_location", "dest_location", "category", "urgency",
               "time_submitted", "deadline", "status"])
    write_csv("resources.csv", resources,
              ["resource_id", "type", "home_location", "capacity", "availability_status"])
    write_csv("algorithm_runs.csv", algo_runs,
              ["run_id", "algorithm_name", "input_size", "time_ns", "memory_kb", "date_run"])

    print("\nDone. Minimums required by the brief: locations>=50, roads>=100, "
          "service_requests>=300, resources>=30, algorithm_runs>=30 (this is a starter "
          "batch -- your real experiment runs from Section 9 will add many more rows).")
