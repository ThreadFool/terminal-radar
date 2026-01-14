package threadfool.op;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class App
{
	public static void main(String[] args)
	{
		Configuration configuration = new Configuration();

		if(!configuration.getSearchedAircraft().equals("none")){
			createAircraftTable();
		}

		ConcurrentLinkedQueue<Integer> freeIds = new ConcurrentLinkedQueue<>();
		Map<String, AircraftState> aircrafts = new ConcurrentHashMap<>();

		MessageReceiver messageReceiver = new MessageReceiver(aircrafts, freeIds, configuration);
		TerminalRenderer terminalRenderer = new TerminalRenderer(aircrafts, configuration, freeIds);

		Thread recieverThread = new Thread(messageReceiver);
		Thread terminalRendererThread = new Thread(terminalRenderer);

		recieverThread.start();
		terminalRendererThread.start();
	}

	private static void createAircraftTable(){
		String url = "jdbc:h2:./data/aircraftdatabase";
		String user = "sa";
		String pass = "";

		try (Connection conn = DriverManager.getConnection(url, user, pass);
			 Statement stmt = conn.createStatement()) {

			stmt.execute("""
                CREATE TABLE IF NOT EXISTS flight (
                    id IDENTITY PRIMARY KEY,
                    icao24 VARCHAR(6),
                    callsign VARCHAR(16),
                    ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
		}
		catch (SQLException e)
		{
			throw new RuntimeException(e);
		}
	}
}
