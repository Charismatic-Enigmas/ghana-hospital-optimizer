-- Ghana Smart Hospital Service Operations Optimizer
-- DCIT 204/308 Joint DSA Semester Project
-- Local context: hospital/clinic network operations (Greater Accra Region)
-- Works on SQLite, MySQL, or PostgreSQL with minor type tweaks (see notes at bottom).

DROP TABLE IF EXISTS audit_events;
DROP TABLE IF EXISTS algorithm_runs;
DROP TABLE IF EXISTS resources;
DROP TABLE IF EXISTS service_requests;
DROP TABLE IF EXISTS roads;
DROP TABLE IF EXISTS locations;

-- 1. LOCATIONS: departments/wards/pharmacies across a network of Accra-area
--    health facilities. Every service request originates or terminates here.
CREATE TABLE locations (
    location_id     INTEGER PRIMARY KEY,
    name            VARCHAR(120) NOT NULL,      -- e.g. "UGH Legon - Emergency Ward"
    facility        VARCHAR(120) NOT NULL,      -- e.g. "University of Ghana Hospital, Legon"
    area            VARCHAR(80)  NOT NULL,      -- e.g. "Legon", "Osu", "Madina"
    type            VARCHAR(40)  NOT NULL,      -- WARD, OPD, LAB, PHARMACY, ICU, THEATRE, ADMIN, GATE
    latitude        DECIMAL(9,6),
    longitude       DECIMAL(9,6)
);

-- 2. ROADS: weighted edges used for ambulance/porter/sample-courier routing
--    between locations (can be within one facility or between facilities).
CREATE TABLE roads (
    road_id                 INTEGER PRIMARY KEY,
    from_location_id        INTEGER NOT NULL REFERENCES locations(location_id),
    to_location_id          INTEGER NOT NULL REFERENCES locations(location_id),
    distance_km             DECIMAL(6,2) NOT NULL,
    travel_time_min         DECIMAL(6,2) NOT NULL,
    road_condition_weight   DECIMAL(4,2) NOT NULL   -- 1.0 = ideal, higher = worse (traffic/road quality)
);

-- 3. SERVICE_REQUESTS: patient-related service jobs to be queued, prioritised,
--    searched and sorted (ambulance dispatch, referrals, pharmacy runs, lab samples).
CREATE TABLE service_requests (
    request_id       INTEGER PRIMARY KEY,
    source_location  INTEGER NOT NULL REFERENCES locations(location_id),
    dest_location    INTEGER NOT NULL REFERENCES locations(location_id),
    category         VARCHAR(40) NOT NULL,   -- AMBULANCE, REFERRAL, PHARMACY, LAB_SAMPLE, EQUIPMENT, PORTER
    urgency          VARCHAR(20) NOT NULL,   -- CRITICAL, HIGH, MEDIUM, LOW
    time_submitted   TIMESTAMP NOT NULL,
    deadline          TIMESTAMP NOT NULL,
    status            VARCHAR(20) NOT NULL   -- PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
);

-- 4. RESOURCES: ambulances, porters, nurses, wheelchairs, couriers that can be assigned.
CREATE TABLE resources (
    resource_id        INTEGER PRIMARY KEY,
    type                VARCHAR(40) NOT NULL,   -- AMBULANCE, PORTER, NURSE, WHEELCHAIR, COURIER_BIKE
    home_location       INTEGER NOT NULL REFERENCES locations(location_id),
    capacity             INTEGER NOT NULL,
    availability_status  VARCHAR(20) NOT NULL   -- AVAILABLE, BUSY, OFF_DUTY, MAINTENANCE
);

-- 5. ALGORITHM_RUNS: empirical performance measurements for the efficiency lab (Section 9).
CREATE TABLE algorithm_runs (
    run_id       INTEGER PRIMARY KEY,
    algorithm_name  VARCHAR(60) NOT NULL,   -- e.g. "MergeSort", "Dijkstra", "HashTablePut"
    input_size      INTEGER NOT NULL,
    time_ns         BIGINT NOT NULL,
    memory_kb       DECIMAL(10,2),
    date_run        TIMESTAMP NOT NULL
);

-- 6. AUDIT_EVENTS: stack-based undo/audit log for system actions (Section 4).
CREATE TABLE audit_events (
    event_id     INTEGER PRIMARY KEY,
    event_type   VARCHAR(40) NOT NULL,   -- ASSIGN, REASSIGN, CANCEL, UNDO, STATUS_CHANGE
    entity_type  VARCHAR(30) NOT NULL,   -- REQUEST, RESOURCE, ROAD
    entity_id    INTEGER NOT NULL,
    detail        VARCHAR(255),
    event_time   TIMESTAMP NOT NULL
);

-- Indexes to support the indexing engine module (M6)
CREATE INDEX idx_requests_status  ON service_requests(status);
CREATE INDEX idx_requests_urgency ON service_requests(urgency);
CREATE INDEX idx_roads_from       ON roads(from_location_id);
CREATE INDEX idx_roads_to         ON roads(to_location_id);

-- NOTES:
-- SQLite:  remove DECIMAL precision args if desired (SQLite is dynamically typed, this schema still works as-is).
-- MySQL:   change TIMESTAMP defaults if you want auto-now behaviour; BIGINT/DECIMAL types are native.
-- Postgres: INTEGER PRIMARY KEY works, but consider SERIAL/GENERATED ALWAYS AS IDENTITY for auto-increment.
