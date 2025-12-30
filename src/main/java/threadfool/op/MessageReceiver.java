package threadfool.op;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageReceiver implements Runnable
{
	private final Map<String, AircraftState> airCrafts;
	private final AtomicInteger nextId = new AtomicInteger();
	private final ConcurrentLinkedQueue<Integer> freeIds;
	private final String host;
	private final int port;

	public MessageReceiver(Map<String, AircraftState> airCrafts, ConcurrentLinkedQueue<Integer> freeIds, Configuration configuration)
	{
		this.airCrafts = airCrafts;
		this.freeIds = freeIds;
		this.host = configuration.getHost();
		this.port = configuration.getPort();
	}

	@Override
	public void run()
	{
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

		AircraftState a = airCrafts.computeIfAbsent(icao, k -> {
			Integer id = freeIds.poll();
			if (id == null) {
				id = nextId.getAndIncrement();
			}
			return new AircraftState(id);
		});		a.icaoHex = icao;
		a.lastSeen = Instant.now();

		switch (type)
		{
			case "1" ->
			{
				a.callsign = field(p, 10);
			}

			case "3" ->
			{
				a.altitude = parseInt(field(p, 11));
				a.latitude = parseDouble(field(p, 14));
				a.longitude = parseDouble(field(p, 15));
			}

			case "4" ->
			{
				a.speed = parseInt(field(p, 12));
				a.heading = parseInt(field(p, 13));
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