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
	double  lastRevealedAngle = -1;   // bearing (0=N, CW) when sweep last passed; -1 = never seen
	Instant lastRevealTime    = null; // wall-clock time of last sweep reveal

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
