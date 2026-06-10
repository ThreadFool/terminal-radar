package threadfool.op;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataWriter implements Runnable
{
	public static final String FILE_PATH = "/tmp/radar-data.txt";

	private static final String ANSI_RESET  = "\033[0m";
	private static final String ANSI_CYAN   = "\033[96m";
	private static final String ANSI_YELLOW = "\033[93m";
	private static final String ANSI_RED    = "\033[91m";

	private final Map<String, AircraftState> airCrafts;
	private final ConcurrentLinkedQueue<Integer> freeIds;

	public DataWriter(Map<String, AircraftState> airCrafts, ConcurrentLinkedQueue<Integer> freeIds)
	{
		this.airCrafts = airCrafts;
		this.freeIds   = freeIds;
	}

	@Override
	public void run()
	{
		while (true)
		{
			try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH)))
			{
				pw.println(" ID  ALT      HDG   LAT       LON      TIME      ICAO     CALLSIGN");
				pw.println("────────────────────────────────────────────────────────────────────");

				List<AircraftState> visible = new ArrayList<>(airCrafts.values());
				for (AircraftState a : visible)
				{
					if (a.lastSeen != null && Duration.between(a.lastSeen, Instant.now()).toMinutes() >= 10)
					{
						airCrafts.remove(a.icaoHex);
						freeIds.offer(a.tempId);
						continue;
					}

					String hdg   = a.heading != null ? String.format("%3d°", a.heading) : " ---";
					String color = altitudeColor(a.altitude);

					String reset = color.isEmpty() ? "" : ANSI_RESET;
					char idChar = (char) ('0' + a.tempId);
					String idCol = String.format("%2c", idChar);
					pw.printf("%s%s%s  %6dft  %4s  %7.4f  %8.4f  %s  %-7s  %s%n",
							color,
							idCol,
							reset,
							a.altitude  != null ? a.altitude  : 0,
							hdg,
							a.latitude  != null ? a.latitude  : 0.0,
							a.longitude != null ? a.longitude : 0.0,
							Utils.toHms(a.lastSeen),
							a.icaoHex,
							a.callsign != null ? a.callsign : ""
					);
				}
				pw.flush();
			}
			catch (IOException ignored) {}

			try { Thread.sleep(500); }
			catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
		}
	}

	private String altitudeColor(Integer altitude)
	{
		if (altitude == null)   return "";
		if (altitude < 10_000) return ANSI_CYAN;
		if (altitude < 25_000) return ANSI_YELLOW;
		return ANSI_RED;
	}
}
