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

	private static final int FALLBACK_SIZE  = 40;
	private static final int SIDEBAR_WIDTH  = 52;
	private static final String ANSI_RESET  = "\033[0m";
	private static final String ANSI_CYAN   = "\033[96m";   // low  < 10,000 ft
	private static final String ANSI_YELLOW = "\033[93m";   // mid  10,000–25,000 ft
	private static final String ANSI_RED    = "\033[91m";   // high > 25,000 ft
	private static final String ANSI_DIM    = "\033[2m";    // faded aircraft

	// Sweep: 6° per frame at 50 ms → ~3 s per revolution (360 / 6 * 0.05 = 3 s)
	private static final long   FRAME_MS        = 50;
	private static final double DEG_PER_FRAME   = 6.0;
	private static final double SWEEP_PERIOD_MS = (360.0 / DEG_PER_FRAME) * FRAME_MS; // ~3000 ms

	// Trailing fade: 5 ghost lines, each 9° apart → 45° tail
	// Intensity 5 = beam tip (brightest), 1 = tail end (dimmest)
	private static final int    TRAIL_STEPS    = 5;
	private static final double TRAIL_STEP_DEG = 9.0;

	// Green gradient by intensity level (index 0 unused, 1–5 used)
	private static final String[] SWEEP_COLORS = {
			"",
			"\033[2;32m",   // 1 — dim green (tail end)
			"\033[2;32m",   // 2 — dim green
			"\033[32m",     // 3 — normal green
			"\033[32m",     // 4 — normal green
			"\033[92m",     // 5 — bright green (beam tip)
	};

	// Reveal window: aircraft is "hit" when beam passes within this many degrees
	private static final double REVEAL_WINDOW_DEG = DEG_PER_FRAME * 1.5;

	private int    size                 = FALLBACK_SIZE;
	private int    sizeRefreshCountdown = 0;
	private double sweepAngleDeg        = 0.0;   // 0 = north, advances CW
	private double prevSweepAngleDeg    = 0.0;

	public TerminalRenderer(Map<String, AircraftState> airCrafts, Configuration configuration,
			ConcurrentLinkedQueue<Integer> freeIds)
	{
		this.airCrafts = airCrafts;
		this.MY_LAT    = configuration.getLatitude();
		this.MY_LON    = configuration.getLongitude();
		this.freeIds   = freeIds;
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
				Thread.sleep(FRAME_MS);
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
		// Refresh terminal size every 20 cycles (~1 s at 50 ms)
		if (sizeRefreshCountdown <= 0)
		{
			size = readTerminalSize();
			sizeRefreshCountdown = 20;
		}
		else
		{
			sizeRefreshCountdown--;
		}

		// Advance sweep
		prevSweepAngleDeg = sweepAngleDeg;
		sweepAngleDeg     = (sweepAngleDeg + DEG_PER_FRAME) % 360.0;

		char[][]  grid           = new char[size][size];
		String[][]  colorGrid    = new String[size][size];
		int[][]  sweepIntensity  = new int[size][size];   // 0 = no sweep, 1–5 = green intensity

		for (char[] row : grid) Arrays.fill(row, ' ');

		int max    = size - 1;
		int center = size / 2;

		// Border
		for (int x = 0; x < size; x++) { grid[0][x] = '-'; grid[max][x] = '-'; }
		for (int y = 0; y < size; y++) { grid[y][0] = '|'; grid[y][max] = '|'; }
		grid[0][0] = '+'; grid[0][max] = '+'; grid[max][0] = '+'; grid[max][max] = '+';
		grid[center][center] = '+';

		// Draw trailing sweep lines: intensity 5 at tip, decreasing into the tail
		for (int i = 0; i < TRAIL_STEPS; i++)
		{
			double angle     = (sweepAngleDeg - i * TRAIL_STEP_DEG + 360.0) % 360.0;
			int    intensity = TRAIL_STEPS - i;   // 5 → 4 → 3 → 2 → 1
			drawBeamLine(sweepIntensity, center, angle, intensity);
		}

		double kmPerDegLat = 111.0;
		double kmPerDegLon = 111.0 * Math.cos(Math.toRadians(MY_LAT));
		double kmPerCell   = RANGE_KM / (size / 2.0);

		// Place aircraft
		for (AircraftState a : airCrafts.values())
		{
			if (a.latitude == null || a.longitude == null) continue;

			double dxKm = (a.longitude - MY_LON) * kmPerDegLon;
			double dyKm = (a.latitude  - MY_LAT) * kmPerDegLat;

			int x = (int) (center + dxKm / kmPerCell);
			int y = (int) (center - dyKm / kmPerCell);

			if (x <= 0 || x >= max || y <= 0 || y >= max) continue;

			// Bearing of aircraft from center (0=N, CW)
			double bearingDeg = Math.toDegrees(Math.atan2(dxKm, dyKm));
			if (bearingDeg < 0) bearingDeg += 360.0;

			// Did beam cross this aircraft this frame?
			double angularDist = ((bearingDeg - prevSweepAngleDeg) + 360.0) % 360.0;
			if (angularDist <= REVEAL_WINDOW_DEG)
			{
				a.lastRevealedAngle = bearingDeg;
				a.lastRevealTime    = Instant.now();
			}

			// Only draw if ever revealed by sweep
			if (a.lastRevealTime == null) continue;

			// Age in ms since last reveal, mapped to 0/1/2
			long   ageMs   = Duration.between(a.lastRevealTime, Instant.now()).toMillis();
			int    sweepAge = (int) (ageMs / (SWEEP_PERIOD_MS / 3.0));  // 0, 1, or 2+

			char   dot   = (char) ('0' + a.tempId);
			String color;
			if (sweepAge == 0)
			{
				color = altitudeColor(a.altitude);            // bright — full altitude color
			}
			else if (sweepAge == 1)
			{
				color = ANSI_DIM + altitudeColor(a.altitude); // one sweep old — dimmed altitude color
			}
			else
			{
				color = ANSI_DIM;                             // faded — just dim, no color
			}

			grid[y][x]      = dot;
			colorGrid[y][x] = color;
		}

		List<AircraftState> visible = new ArrayList<>(airCrafts.values());

		for (int y = 0; y < size; y++)
		{
			StringBuilder mapRow = new StringBuilder();
			for (int x = 0; x < size; x++)
			{
				String color    = colorGrid[y][x];
				int    intensity = sweepIntensity[y][x];

				if (intensity > 0 && color != null)
				{
					// Aircraft on sweep: altitude color wins, no green overlay
					mapRow.append(color).append(grid[y][x]).append(ANSI_RESET);
				}
				else if (intensity > 0)
				{
					// Empty sweep cell: green beam character
					mapRow.append(SWEEP_COLORS[intensity]).append('|').append(ANSI_RESET);
				}
				else if (color != null)
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
				AircraftState a    = visible.get(y);
				String        hdg  = a.heading != null ? String.format("%3d°", a.heading) : " ---";
				String        color = altitudeColor(a.altitude);

				info = String.format("  %s%2c%s  %6dft  %4s  %7.4f  %8.4f  %s  %s  %s",
						color,
						(char) ('0' + a.tempId),
						ANSI_RESET,
						a.altitude  != null ? a.altitude  : 0,
						hdg,
						a.latitude  != null ? a.latitude  : 0.0,
						a.longitude != null ? a.longitude : 0.0,
						Utils.toHms(a.lastSeen),
						a.icaoHex,
						a.callsign
				);

				if (a.lastSeen != null && Duration.between(a.lastSeen, Instant.now()).toMinutes() >= 10)
				{
					airCrafts.remove(a.icaoHex);
					freeIds.offer(a.tempId);
				}
			}

			System.out.println(mapRow + info);
		}
	}

	/** Draw a single Bresenham line from center at the given angle, recording sweep intensity. */
	private void drawBeamLine(int[][] sweepIntensity, int center, double angleDeg, int intensity)
	{
		double rad  = Math.toRadians(angleDeg);
		int    endX = center + (int) (Math.sin(rad) * (size / 2.0 - 1));
		int    endY = center - (int) (Math.cos(rad) * (size / 2.0 - 1));

		int x0 = center, y0 = center;
		int x1 = endX,   y1 = endY;

		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);
		int sx = x0 < x1 ? 1 : -1;
		int sy = y0 < y1 ? 1 : -1;
		int err = dx - dy;
		int max = size - 1;

		while (true)
		{
			if (x0 > 0 && x0 < max && y0 > 0 && y0 < max)
			{
				// Keep highest intensity if multiple trail lines overlap
				if (intensity > sweepIntensity[y0][x0])
					sweepIntensity[y0][x0] = intensity;
			}
			if (x0 == x1 && y0 == y1) break;
			int e2 = 2 * err;
			if (e2 > -dy) { err -= dy; x0 += sx; }
			if (e2 <  dx) { err += dx; y0 += sy; }
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
				int rows    = Integer.parseInt(parts[0]);
				int cols    = Integer.parseInt(parts[1]);
				int gridDim = Math.min(rows - 2, cols - SIDEBAR_WIDTH);
				return Math.max(10, Math.min(gridDim, 200));
			}
		}
		catch (Exception ignored) {}
		return FALLBACK_SIZE;
	}

	private String altitudeColor(Integer altitude)
	{
		if (altitude == null)   return "";
		if (altitude < 10_000) return ANSI_CYAN;
		if (altitude < 25_000) return ANSI_YELLOW;
		return ANSI_RED;
	}

	private void clearScreen()
	{
		System.out.print("\033[H\033[2J");
		System.out.flush();
	}
}
