package com.ghdsa.model;

public record Location(int locationId, String name, String facility, String area,
                        String type, double latitude, double longitude) {}
