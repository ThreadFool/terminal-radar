package threadfool.op;

public class Configuration
{
	private final double latitude;
	private final double longitude;
	private final String host;
	private final int port;

	public Configuration()
	{
		this.latitude = Double.parseDouble(System.getenv("MY_LAT"));
		this.longitude = Double.parseDouble(System.getenv("MY_LONG"));
		this.host = System.getenv().getOrDefault("HOST", "localhost");
		this.port = Integer.parseInt(System.getenv().getOrDefault("PORT", String.valueOf(30003)));
	}

	public double getLatitude()
	{
		return latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}
}
