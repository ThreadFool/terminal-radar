import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Dump1090MockServer {

	private static final int PORT = 30002;

	private static final List<String> MESSAGES = List.of(
			"MSG,1,1,1,4CA8E4,1,,,,,,EZY45AB,,,,,,,,",

			"MSG,3,1,1,4CA8E4,1,,,,,,50.2500,18.2000,35000,,,,,,",
			"MSG,4,1,1,4CA8E4,1,,,,,,450,90,,,,,,",

			"MSG,3,1,1,4CA8E4,1,,,,,,50.2500,18.2600,35000,,,,,,",
			"MSG,4,1,1,4CA8E4,1,,,,,,450,90,,,,,,",

			"MSG,3,1,1,4CA8E4,1,,,,,,50.2500,18.3200,35000,,,,,,",
			"MSG,4,1,1,4CA8E4,1,,,,,,450,90,,,,,,",

			"MSG,3,1,1,4CA8E4,1,,,,,,50.2500,18.3800,35000,,,,,,",
			"MSG,4,1,1,4CA8E4,1,,,,,,450,90,,,,,,",

			"MSG,3,1,1,4CA8E4,1,,,,,,50.2500,18.4400,35000,,,,,,",
			"MSG,4,1,1,4CA8E4,1,,,,,,450,90,,,,,,",

			"MSG,3,1,1,4CA8E4,1,,,,,,50.2500,18.5000,35000,,,,,,",
			"MSG,4,1,1,4CA8E4,1,,,,,,450,90,,,,,,",

			"MSG,3,1,1,4CA8E4,1,,,,,,50.2500,18.5600,35000,,,,,,",
			"MSG,4,1,1,4CA8E4,1,,,,,,450,90,,,,,,"
	);

	public static void main(String[] args) throws Exception {
		try (ServerSocket serverSocket = new ServerSocket(PORT)) {
			System.out.println("Mock dump1090 listening on port " + PORT);

			Socket client = serverSocket.accept();
			System.out.println("Client connected");

			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(client.getOutputStream())
			);

			while (true) {
				for (String msg : MESSAGES) {
					writer.write(msg);
					writer.newLine();
					writer.flush();

					TimeUnit.MILLISECONDS.sleep(500);
				}
			}
		}
	}
}