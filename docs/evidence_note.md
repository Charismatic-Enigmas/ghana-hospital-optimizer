# Evidence Note: Dataset Construction and Local Sourcing

Required by Section 2.i of the brief: how the dataset was obtained/
constructed from local knowledge, without exposing personal data.

## Chosen local context

Hospital/clinic operations across a network of real Greater Accra health
facilities (one of the brief's explicitly allowed contexts, Section 2).

## What is real vs synthetic

**Real**: the 10 facility names, their general locations, and their
geographic coordinates.

**Synthetic (generated, seeded, documented)**: everything about individual
department nodes, roads, service requests, and resources — no real patient
data, staff data, or operational records were used or needed anywhere.

### Facilities and coordinates

`db/seed/generate_data.py`'s `FACILITIES` table lists 10 real Greater Accra
health facilities. Coordinates went through **two verification passes**:
an initial lookup against public geocoding sources (Wikipedia, Wikidata,
latitude.to, distancesfrom.com, mapcarta, Google-Maps-derived DMS
conversions), then a second pass against OpenStreetMap via Nominatim and
Overpass — the same open data that the Geoplaner coordinate tool
(geoplaner.com) displays. Four facility positions were corrected as a
result; the current table reflects the OSM-verified positions:

| Facility | Coordinate | Confidence | Source(s) |
|---|---|---|---|
| University of Ghana Hospital, Legon | 5.651852, -0.178107 | facility-level, OSM way centroid | OSM way "University of Ghana Hospital" (Overpass); initial latitude.to point was ~830 m off, sitting among campus banks |
| Korle-Bu Teaching Hospital | 5.538317, -0.226815 | facility-level, OSM POI | OSM/Nominatim POI on Harley Street, within the real KBTH campus (~485 m from the initial latitude.to point, also inside campus) |
| Ridge Hospital (Greater Accra Regional Hospital) | 5.562111, -0.198931 | facility-level, OSM POI | Wikipedia + Wikidata (agree), confirmed by Nominatim (62 m apart) |
| 37 Military Hospital | 5.586892, -0.185047 | facility-level, OSM POI | distancesfrom.com, confirmed by Nominatim (37 m apart) |
| La General Hospital | 5.555504, -0.166564 | facility-level, OSM POI | public geocoding search, confirmed by Nominatim (14 m apart) |
| Tema General Hospital | 5.674168, -0.025012 | facility-level, OSM POI | Nominatim POI on Hospital Road (**corrected** — earlier version used the Tema town-centre stand-in, 1.05 km away) |
| Madina Polyclinic | 5.667584, -0.165230 | facility-level, OSM POI | Nominatim POI on New Road (**corrected** — earlier version used the Madina town-centre stand-in, 1.76 km away) |
| Achimota Hospital | 5.629622, -0.217244 | facility-level, OSM POI | Google-Maps-derived DMS conversion, confirmed by Nominatim (24 m apart) |
| Nyaho Medical Centre | 5.551490, -0.203939 | facility-level, OSM POI | Airport Residential / Kofi Annan St address match; exact match to Nominatim POI (0 m) |
| Dodowa Health Centre | 5.886047, -0.092318 | area-level (**no mapped POI exists**) | derived from the co-located "Hospital Junction" / "Old Hospital" bus stops on the R40 in Dodowa (Nominatim); OSM has no hospital polygon here yet |

These are **best-effort public-geocoding estimates**, not survey-grade GPS —
appropriate for a student project, but sourced honestly: nine of ten now sit
on an actual OSM-mapped health-facility object or within its real grounds,
and the one remaining estimate (Dodowa) is flagged rather than dressed up as
precise. Each of the 10 department nodes per facility (Emergency Ward, OPD,
Maternity, Pharmacy, Lab, ICU, Theatre, Radiology, Admin, Main Gate) is
placed with a small random offset (~250m, tightened down from an earlier
~1.1km version that could scatter a facility's own departments outside its
real grounds) around that facility's coordinate — representing "somewhere
on this hospital's real campus," not a claim about any specific building's
exact position.

### Roads, service requests, resources

None of these represent real operational data — no real ambulance routes,
no real patients, no real staff.

**Roads** are *not* independent random numbers — `distance_km` is the real
haversine (great-circle) distance between each pair of locations' actual
coordinates. `travel_time_min` is derived from that real distance using an
assumed average urban Accra travel speed of 20 km/h, then scaled by
`road_condition_weight` (still a random 1.0-3.5 multiplier, unchanged
semantics: 1.0 = ideal, higher = worse traffic/road quality). Connectivity
is built in two layers, both guaranteed connected via a minimum spanning
tree over real coordinates (Prim's algorithm):
- every facility's 10 departments get an MST over their real distances, plus
  extra edges for any pair within 0.4km, so any two departments on the same
  real campus are always a short, realistic hop apart;
- the 10 facilities themselves get an MST over their real coordinates (via
  each facility's Main Gate, a natural transfer point), plus each facility's
  2 nearest neighbours, so a referral between two real hospitals is always a
  realistically long trip matching their actual distance apart.

(An earlier version of this script assigned `distance_km` and
`travel_time_min` as pure independent random values with no connection to
real geography — this produced nonsensical "shortest paths" for same-campus
trips that routed through unrelated hospitals across the city. Fixed once
Phase 0's coordinate verification made real distances available to derive
road weights from.)

**Service requests and resources** are generated with plausible ranges
(request categories/urgency/deadlines matching realistic hospital-dispatch
categories; resource types matching what a Ghanaian hospital network would
actually operate: ambulances, porters, nurses, wheelchairs, courier bikes) —
these don't need to be geography-derived the way roads do, since a request's
source/destination pair is just "where this job needs to go," and the now-
realistic road network handles finding a sensible route between them.

## No personal data

At no point does this dataset include any real patient name, real staff
name, real medical record, real phone number, or any other personally
identifiable information. All 300 service-request records and all 30
resource records are synthetic.

## Reproducibility

`db/seed/generate_data.py` is deterministic: `random.seed(SEED)` where
`SEED = 680000` is derived from the team's 15 member index numbers (product
of `(index % 97 + 1)` across all members, mod 1,000,000 — mirrored exactly
in `ParameterDerivation.randomSeed()` on the Java side, per Section 2.iii's
requirement to derive at least 3 algorithm parameters from member index
numbers). Re-running the script reproduces the exact same CSVs.

The three (of four) parameters derived from member index numbers:
`ParameterDerivation.priorityWeight()`, `.routePenalty()`,
`.hashTableSize()`, and `.randomSeed()` — see
`src/main/java/com/ghdsa/app/ParameterDerivation.java`.
