package com.ghdsa.model;

import java.time.LocalDateTime;

public record ServiceRequest(int requestId, int sourceLocation, int destLocation,
                              String category, String urgency, LocalDateTime timeSubmitted,
                              LocalDateTime deadline, String status) {}
