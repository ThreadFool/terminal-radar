package threadfool.op;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageReceiver implements Runnable
{

	final Map<String, AircraftState> airCrafts;
	private final AtomicInteger nextId = new AtomicInteger();

	public MessageReceiver(Map<String, AircraftState> airCrafts)
	{
		this.airCrafts = airCrafts;
	}

	@Override
	public void run()
	{
		String host = "localhost";
		int port = 30003;

		try
		{
			Socket socket = new Socket(host, port);

			InputStream in = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

			String line;
			while ((line = reader.readLine()) != null)
			{
				handleMessage(line);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Mistake Happened");
		}
	}

	void handleMessage(String line)
	{

		if (line == null || line.isBlank())
		{
			return;
		}

		String[] p = line.split(",", -1);

		if (p.length < 5 || !"MSG".equals(p[0]))
		{
			return;
		}

		String type = p[1];
		String icao = p[4];

		AircraftState a = airCrafts.computeIfAbsent(icao, k -> new AircraftState(nextId.getAndIncrement()));
		a.icaoHex = icao;

		switch (type)
		{
			case "1" ->
			{
				a.callsign = field(p, 10);
				a.lastSeen = Instant.now();
			}

			case "3" ->
			{
				a.altitude = parseInt(field(p, 11));
				a.latitude = parseDouble(field(p, 14));
				a.longitude = parseDouble(field(p, 15));
				a.lastSeen = Instant.now();
			}

			case "4" ->
			{
				a.speed = parseInt(field(p, 12));
				a.heading = parseInt(field(p, 13));
				a.lastSeen = Instant.now();
			}
		}
	}

	Integer parseInt(String s)
	{
		return s != null ? Integer.parseInt(s) : null;
	}

	Double parseDouble(String s)
	{
		return s != null ? Double.parseDouble(s) : null;
	}

	private static String field(String[] p, int idx)
	{
		return idx < p.length && !p[idx].isEmpty() ? p[idx] : null;
	}
}