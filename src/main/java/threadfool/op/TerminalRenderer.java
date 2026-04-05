package threadfool.op;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class TerminalRenderer implements Runnable
{
	final Map<String, AircraftState> airCrafts;
	final double MY_LAT;
	final double MY_LON;
	static final double RANGE_KM = 100;
	private final ConcurrentLinkedQueue<Integer> freeIds;

	private static final int FALLBACK_SIZE = 40;
	private static final int SIDEBAR_WIDTH = 52;
	private static final String ANSI_RESET  = "\033[0m";
	private static final String ANSI_CYAN   = "\033[96m";  // low  < 10,000 ft
	private static final String ANSI_YELLOW = "\033[93m";  // mid  10,000–25,000 ft
	private static final String ANSI_RED    = "\033[91m";  // high > 25,000 ft
	private int size = FALLBACK_SIZE;
	private int sizeRefreshCountdown = 0;

	public TerminalRenderer(Map<String, AircraftState> airCrafts, Configuration configuration,
			ConcurrentLinkedQueue<Integer> freeIds)
	{
		this.airCrafts = airCrafts;
		this.MY_LAT = configuration.getLatitude();
		this.MY_LON = configuration.getLongitude();
		this.freeIds = freeIds;
	}

	@Override
	public void run()
	{
		while (true)
		{
			clearScreen();
			drawMap();

			try
			{
				Thread.sleep(1000);
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private void drawMap()
	{
		// Refresh terminal size every 5 cycles
		if (sizeRefreshCountdown <= 0)
		{
			size = readTerminalSize();
			sizeRefreshCountdown = 5;
		}
		else
		{
			sizeRefreshCountdown--;
		}

		char[][] grid = new char[size][size];
		String[][] colorGrid = new String[size][size];

		for (char[] row : grid)
		{
			Arrays.fill(row, ' ');
		}

		int max = size - 1;

		for (int x = 0; x < size; x++)
		{
			grid[0][x] = '-';
			grid[max][x] = '-';
		}

		for (int y = 0; y < size; y++)
		{
			grid[y][0] = '|';
			grid[y][max] = '|';
		}

		grid[0][0] = '+';
		grid[0][max] = '+';
		grid[max][0] = '+';
		grid[max][max] = '+';

		int center = size / 2;
		grid[center][center] = '+';

		double kmPerDegLat = 111.0;
		double kmPerDegLon = 111.0 * Math.cos(Math.toRadians(MY_LAT));
		double kmPerCell = RANGE_KM / (size / 2.0);

		// Render aircraft current positions (overwrites trail chars — correct priority)
		for (AircraftState a : airCrafts.values())
		{
			if (a.latitude == null || a.longitude == null)
			{
				continue;
			}

			double dxKm = (a.longitude - MY_LON) * kmPerDegLon;
			double dyKm = (a.latitude - MY_LAT) * kmPerDegLat;

			int x = (int) (center + dxKm / kmPerCell);
			int y = (int) (center - dyKm / kmPerCell);

			if (x > 0 && x < max && y > 0 && y < max)
			{
				grid[y][x] = (char) ('0' + a.tempId);
				colorGrid[y][x] = altitudeColor(a.altitude);
			}
		}

		List<AircraftState> visible = new ArrayList<>(airCrafts.values());

		for (int y = 0; y < size; y++)
		{
			StringBuilder mapRow = new StringBuilder();
			for (int x = 0; x < size; x++)
			{
				String color = colorGrid[y][x];
				if (color != null)
				{
					mapRow.append(color).append(grid[y][x]).append(ANSI_RESET);
				}
				else
				{
					mapRow.append(grid[y][x]);
				}
			}

			String info = "";
			if (y < visible.size())
			{
				AircraftState a = visible.get(y);
				String hdg = a.heading != null ? String.format("%3d°", a.heading) : " ---";

				String color = altitudeColor(a.altitude);
				info = String.format("  %s%2c%s  %6dft  %4s  %7.4f  %8.4f  %s  %s  %s",//
						color, //
						(char) ('0' + a.tempId), //
						ANSI_RESET, //
						a.altitude != null ? a.altitude : 0, //
						hdg, //
						a.latitude, //
						a.longitude, //
						Utils.toHms(a.lastSeen), //
						a.icaoHex, //
						a.callsign //
				);

				if (a.lastSeen != null)
				{
					if (Duration.between(a.lastSeen, Instant.now()).toMinutes() >= 10)
					{
						airCrafts.remove(a.icaoHex);
						freeIds.offer(a.tempId);
					}
				}
			}

			System.out.println(mapRow + info);
		}
	}

	private int readTerminalSize()
	{
		try
		{
			ProcessBuilder pb = new ProcessBuilder("stty", "size");
			pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
			Process p = pb.start();
			if (!p.waitFor(200, TimeUnit.MILLISECONDS))
			{
				p.destroyForcibly();
				return FALLBACK_SIZE;
			}
			String output = new String(p.getInputStream().readAllBytes()).trim();
			String[] parts = output.split("\\s+");
			if (parts.length == 2)
			{
				int rows = Integer.parseInt(parts[0]);
				int cols = Integer.parseInt(parts[1]);
				int gridDim = Math.min(rows - 2, cols - SIDEBAR_WIDTH);
				return Math.max(10, Math.min(gridDim, 200));
			}
		}
		catch (Exception ignored) {}
		return FALLBACK_SIZE;
	}

	private String altitudeColor(Integer altitude)
	{
		if (altitude == null) return "";
		if (altitude < 10_000)  return ANSI_CYAN;
		if (altitude < 25_000)  return ANSI_YELLOW;
		return ANSI_RED;
	}

	private void clearScreen()
	{
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
}
