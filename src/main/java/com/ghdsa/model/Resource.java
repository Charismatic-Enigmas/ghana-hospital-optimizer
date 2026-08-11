package com.ghdsa.model;

public record Resource(int resourceId, String type, int homeLocation,
                        int capacity, String availabilityStatus) {}
