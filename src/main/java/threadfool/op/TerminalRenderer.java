package threadfool.op;

import java.util.Arrays;
import java.util.Map;

public class TerminalRenderer implements Runnable
{
	final Map<String, AircraftState> airCrafts;
	private static final int SIZE = 40;
	static final double MY_LAT = 50.2945;
	static final double MY_LON = 18.6714;
	static final double RANGE_KM = 200;

	public TerminalRenderer(Map<String, AircraftState> airCrafts)
	{
		this.airCrafts = airCrafts;
	}

	@Override
	public void run()
	{
		while (true) {
			clearScreen();
			drawMap();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

	private void drawMap() {
		char[][] grid = new char[SIZE][SIZE];

		for (char[] row : grid)
			Arrays.fill(row, ' ');

		int max = SIZE - 1;

		for (int x = 0; x < SIZE; x++) {
			grid[0][x] = '-';
			grid[max][x] = '-';
		}

		for (int y = 0; y < SIZE; y++) {
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

		for (AircraftState a : airCrafts.values()) {
			if (a.latitude == null || a.longitude == null)
				continue;

			double dxKm = (a.longitude - MY_LON) * kmPerDegLon;
			double dyKm = (a.latitude  - MY_LAT) * kmPerDegLat;

			int x = (int)(center + dxKm / kmPerCell);
			int y = (int)(center - dyKm / kmPerCell);

			if (x > 0 && x < max && y > 0 && y < max)
				grid[y][x] = (char) ('0' + a.tempId);
		}

		for (char[] row : grid)
			System.out.println(row);
	}

	static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
		double R = 6371.0;
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lon2 - lon1);

		double a = Math.sin(dLat/2)*Math.sin(dLat/2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2))
				* Math.sin(dLon/2)*Math.sin(dLon/2);

		return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	}

	private void clearScreen() {
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
}

