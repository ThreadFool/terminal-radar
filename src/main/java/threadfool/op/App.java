package threadfool.op;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class App
{
	public static void main(String[] args)
	{
		Configuration configuration = new Configuration();
		Map<String, AircraftState> aircrafts = new ConcurrentHashMap<>();

		MessageReceiver messageReceiver = new MessageReceiver(aircrafts);
		TerminalRenderer terminalRenderer = new TerminalRenderer(aircrafts, configuration);

		Thread recieverThread = new Thread(messageReceiver);
		Thread terminalRendererThread = new Thread(terminalRenderer);

		recieverThread.start();
		terminalRendererThread.start();
	}
}
