package threadfool.op;

public class Configuration
{
	private final double latitude;
	private final double longitude;

	public Configuration()
	{
		this.latitude = Double.parseDouble(System.getenv("MY_LAT"));
		this.longitude = Double.parseDouble(System.getenv("MY_LONG"));
	}

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}
}
