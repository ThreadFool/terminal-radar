package threadfool.op;

import java.time.Instant;

public class AircraftState
{
	String icaoHex;
	String callsign;
	Double latitude;
	Double longitude;
	Integer altitude;
	Integer speed;
	Integer heading;
	Instant lastSeen;
	int tempId;

	public AircraftState(int id){
		tempId = id;
	}

	boolean hasPosition() {
		return latitude != null && longitude != null;
	}

	AircraftSnapshot toSnapshot() {
		return new AircraftSnapshot(
				icaoHex,
				callsign,
				latitude,
				longitude,
				altitude,
				Instant.now()
		);
	}

	@Override
	public String toString()
	{
		return "AircraftState{" +
				"icaoHex='" + icaoHex + '\'' +
				", callsign='" + callsign + '\'' +
				", lat=" + latitude +
				", lon=" + longitude +
				", alt=" + altitude +
				", speed=" + speed +
				", heading=" + heading +
				'}';	}
}
