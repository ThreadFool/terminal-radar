package threadfool.op;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TerminalRenderer implements Runnable
{
	final Map<String, AircraftState> airCrafts;
	private static final int SIZE = 40;
	static final double MY_LAT = 50.2945;
	static final double MY_LON = 18.6714;
	static final double RANGE_KM = 100;

	public TerminalRenderer(Map<String, AircraftState> airCrafts)
	{
		this.airCrafts = airCrafts;
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
		char[][] grid = new char[SIZE][SIZE];

		for (char[] row : grid)
		{
			Arrays.fill(row, ' ');
		}

		int max = SIZE - 1;

		for (int x = 0; x < SIZE; x++)
		{
			grid[0][x] = '-';
			grid[max][x] = '-';
		}

		for (int y = 0; y < SIZE; y++)
		{
			grid[y][0] = '|';
			grid[y][max] = '|';
		}

		grid[0][0] = '+';
		grid[0][max] = '+';
		grid[max][0] = '+';
		grid[max][max] = '+';

		int center = SIZE / 2;
		grid[center][center] = '+';

		double kmPerDegLat = 111.0;
		double kmPerDegLon = 111.0 * Math.cos(Math.toRadians(MY_LAT));
		double kmPerCell = RANGE_KM / (SIZE / 2.0);

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
			}
		}

		List<AircraftState> visible = new ArrayList<>(airCrafts.values());

		for (int y = 0; y < SIZE; y++)
		{
			String mapRow = new String(grid[y]);

			String info = "";
			if (y < visible.size())
			{
				AircraftState a = visible.get(y);
				String hdg = a.heading != null ? String.format("%3d°", a.heading) : " ---";

				info = String.format("  %2d  %6dm  %4s  %7.4f  %8.4f  %s",//
						a.tempId, //
						a.altitude != null ? a.altitude : 0, //
						hdg, //
						a.latitude, //
						a.longitude, //
						a.icaoHex //
				);
			}

			System.out.println(mapRow + info);
		}
	}

	private void clearScreen()
	{
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
}

