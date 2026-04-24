package threadfool.op;

import java.time.Instant;

public record AircraftSnapshot(
		String icao,
		String callsign,
		Double latitude,
		Double longitude,
		Integer altitude,
		Instant timestamp
) {}
