package threadfool.op;

public class AircraftState
{
	String icaoHex;
	String callsign;
	Double latitude;
	Double longitude;
	Integer altitude;
	Integer speed;
	Integer heading;

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
