# Data Dictionary

Derived from `db/schema.sql`. Six tables; see `docs/evidence_note.md` for how
the seed data (`db/seed/*.csv`) was constructed and sourced.

## locations

Every node in the service network — a department/ward within one of 10 real
Greater Accra health facilities.

| field | type | notes |
|---|---|---|
| location_id | INTEGER PK | |
| name | VARCHAR(120) | e.g. "University of Ghana Hospital - Emergency Ward" |
| facility | VARCHAR(120) | the parent hospital/clinic, e.g. "Korle-Bu Teaching Hospital" |
| area | VARCHAR(80) | suburb/town, e.g. "Legon", "Korle-Bu" |
| type | VARCHAR(40) | WARD, OPD, LAB, PHARMACY, ICU, THEATRE, ADMIN, GATE |
| latitude | DECIMAL(9,6) | verified real-world coordinate, see evidence_note.md |
| longitude | DECIMAL(9,6) | verified real-world coordinate, see evidence_note.md |

100 records (minimum required: 50). 10 facilities x 10 department types.

## roads

Weighted edges between locations — the routes an ambulance/porter/courier
travels.

| field | type | notes |
|---|---|---|
| road_id | INTEGER PK | |
| from_location_id | INTEGER FK -> locations | |
| to_location_id | INTEGER FK -> locations | |
| distance_km | DECIMAL(6,2) | |
| travel_time_min | DECIMAL(6,2) | used as the Dijkstra/Prim/Kruskal edge weight |
| road_condition_weight | DECIMAL(4,2) | 1.0 = ideal condition, higher = worse (traffic/road quality) |

370 records (minimum required: 100). Undirected in practice — `Graph`
adds both directions unless told otherwise.

## service_requests

Jobs to be queued, prioritised, searched, and sorted — ambulance dispatch,
referrals, pharmacy runs, lab samples, equipment/porter requests.

| field | type | notes |
|---|---|---|
| request_id | INTEGER PK | |
| source_location | INTEGER FK -> locations | |
| dest_location | INTEGER FK -> locations | |
| category | VARCHAR(40) | AMBULANCE, REFERRAL, PHARMACY, LAB_SAMPLE, EQUIPMENT, PORTER |
| urgency | VARCHAR(20) | CRITICAL, HIGH, MEDIUM, LOW — drives the priority-queue dispatch order |
| time_submitted | TIMESTAMP | |
| deadline | TIMESTAMP | derived from urgency: CRITICAL=+15min, HIGH=+60min, MEDIUM=+180min, LOW=+720min |
| status | VARCHAR(20) | PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED |

300 records (minimum required: 300).

## resources

Ambulances, porters, nurses, wheelchairs, couriers that can be assigned to
requests.

| field | type | notes |
|---|---|---|
| resource_id | INTEGER PK | |
| type | VARCHAR(40) | AMBULANCE, PORTER, NURSE, WHEELCHAIR, COURIER_BIKE |
| home_location | INTEGER FK -> locations | |
| capacity | INTEGER | e.g. an ambulance may carry 1-2 patients |
| availability_status | VARCHAR(20) | AVAILABLE, BUSY, OFF_DUTY, MAINTENANCE |

30 records (minimum required: 30).

## algorithm_runs

Empirical runtime measurements for the efficiency lab (Section 9).

| field | type | notes |
|---|---|---|
| run_id | INTEGER PK | |
| algorithm_name | VARCHAR(60) | e.g. "MergeSort", "Dijkstra" |
| input_size | INTEGER | |
| time_ns | BIGINT | |
| memory_kb | DECIMAL(10,2) | |
| date_run | TIMESTAMP | |

Seeded with a 30-row starter batch; the real measured rows come from menu
option 8 (sort demo, logs one row per algorithm per run) and option 20 (full
Section 9 suite, writes `efficiency_results.csv` — see
`docs/graphs/plot_efficiency_results.py` for turning that CSV into the
required graphs).

## audit_events

Stack-based undo/audit trail (menu option 17 demonstrates push/pop against
this table via `ArrayStack`).

| field | type | notes |
|---|---|---|
| event_id | INTEGER PK | |
| event_type | VARCHAR(40) | ASSIGN, REASSIGN, CANCEL, UNDO, STATUS_CHANGE |
| entity_type | VARCHAR(30) | REQUEST, RESOURCE, ROAD |
| entity_id | INTEGER | |
| detail | VARCHAR(255) | |
| event_time | TIMESTAMP | |

## Indexes (Section 6 / M6 indexing engine)

- `idx_requests_status` on `service_requests(status)`
- `idx_requests_urgency` on `service_requests(urgency)`
- `idx_roads_from` / `idx_roads_to` on `roads(from_location_id)` / `roads(to_location_id)`
