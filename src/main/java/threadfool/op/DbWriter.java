package threadfool.op;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class DbWriter implements Runnable
{
	private final BlockingQueue<AircraftSnapshot> queue;
	private volatile boolean running = true;

	public DbWriter(BlockingQueue<AircraftSnapshot> queue)
	{
		this.queue = queue;
	}

	public void shutdown() {
		running = false;
	}

	@Override
	public void run()
	{
		List<AircraftSnapshot> batch = new ArrayList<>(100);

		try (Connection conn = DriverManager.getConnection("jdbc:h2:./data/aircraftdatabase", "sa",
				""); PreparedStatement ps = conn.prepareStatement("""
				    INSERT INTO flight (icao24, callsign, latitude, longitude, altitude, ts)
				    VALUES (?, ?, ?, ?, ?, ?)
				"""))
		{
			conn.setAutoCommit(false);
			long lastFlush = System.currentTimeMillis();

			while (running || !queue.isEmpty())
			{
				AircraftSnapshot s = queue.poll(200, TimeUnit.MILLISECONDS);
				System.out.println("KUUURWA" + queue.size());
				if (s != null)
				{
					batch.add(s);
				}

				boolean timeToFlush = !batch.isEmpty() && (batch.size() >= 100 || System.currentTimeMillis() - lastFlush > 500);

				if (timeToFlush)
				{
					for (AircraftSnapshot b : batch)
					{
						ps.setString(1, b.icao());
						ps.setString(2, b.callsign());
						ps.setDouble(3, b.latitude());
						ps.setDouble(4, b.longitude());

						if (b.altitude() != null) ps.setInt(5, b.altitude());
						else ps.setNull(5, Types.INTEGER);

						ps.setTimestamp(6, Timestamp.from(b.timestamp()));

						ps.addBatch();
					}

					ps.executeBatch();
					conn.commit();

					batch.clear();
					lastFlush = System.currentTimeMillis();
				}
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException("Database Problem");
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException("Thread in Database Problem");
		}
	}
}
