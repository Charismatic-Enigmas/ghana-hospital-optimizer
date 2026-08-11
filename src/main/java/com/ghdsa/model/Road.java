package com.ghdsa.model;

public record Road(int roadId, int fromLocationId, int toLocationId,
                    double distanceKm, double travelTimeMin, double roadConditionWeight) {}
