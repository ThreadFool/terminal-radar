package threadfool.op;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MessageReceiver implements Runnable
{

	final Map<String, AircraftState> airCrafts;

	public MessageReceiver(Map<String, AircraftState> airCrafts){
		this.airCrafts = airCrafts;
	}

	@Override
	public void run()
	{
		String host = "localhost";
		int port = 30002;

		try
		{
			Socket socket = new Socket(host, port);

			InputStream in = socket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

			String line;
			while ((line = reader.readLine()) != null) {
				handleMessage(line);
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException("Mistake Happened");
		}
	}

	void handleMessage(String line) {
		String[] p = line.split(",", -1);
		String type = p[1];
		String icao = p[4];
		System.out.println(airCrafts.size());
		AircraftState a = airCrafts.computeIfAbsent(icao, k -> new AircraftState(airCrafts.size()));
		a.icaoHex = icao;

		switch (type) {
			case "1": //callsign
				a.callsign = p[10].trim();
				break;

			case "3": //position
				a.altitude  = p[11].isEmpty() ? null : Integer.parseInt(p[11]);
				a.latitude  = p[14].isEmpty() ? null : Double.parseDouble(p[14]);
				a.longitude = p[15].isEmpty() ? null : Double.parseDouble(p[15]);
				break;

			case "4": //velocity
				a.speed   = p[12].isEmpty() ? null : Integer.parseInt(p[12]);
				a.heading = p[13].isEmpty() ? null : Integer.parseInt(p[13]);
				break;
		}
	}
}