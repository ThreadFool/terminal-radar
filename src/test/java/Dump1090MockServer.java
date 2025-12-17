import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Dump1090MockServer {

	private static final int PORT = 30002;

	private static final List<String> MESSAGES = List.of(
			"MSG,3,1,1,4BB862,1,2025/12/16,19:05:54.321,2025/12/16,19:05:54.369,,37025,,,,,,,0,,0,0",
			"MSG,4,1,1,406012,1,2025/12/16,19:05:55.123,2025/12/16,19:05:55.200,,38000,450,180,,,0,,0,0",
			"MSG,3,1,1,3C6444,1,2025/12/16,19:05:56.000,2025/12/16,19:05:56.050,,41000,,,,,,,0,,0,0"
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