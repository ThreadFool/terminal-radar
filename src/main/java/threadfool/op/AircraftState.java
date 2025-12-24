package threadfool.op;

import java.time.LocalDateTime;

public class AircraftState
{
	String icaoHex;
	String callsign;
	Double latitude;
	Double longitude;
	Integer altitude;
	Integer speed;
	Integer heading;
	LocalDateTime lastSeen;
	int tempId;

	public AircraftState(int id){
		tempId = id;
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
