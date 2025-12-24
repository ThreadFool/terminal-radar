package threadfool.op;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class App
{
	public static void main(String[] args)
	{
		Configuration configuration = new Configuration();
		ConcurrentLinkedQueue<Integer> freeIds = new ConcurrentLinkedQueue<>();
		Map<String, AircraftState> aircrafts = new ConcurrentHashMap<>();

		MessageReceiver messageReceiver = new MessageReceiver(aircrafts, freeIds);
		TerminalRenderer terminalRenderer = new TerminalRenderer(aircrafts, configuration, freeIds);

		Thread recieverThread = new Thread(messageReceiver);
		Thread terminalRendererThread = new Thread(terminalRenderer);

		recieverThread.start();
		terminalRendererThread.start();
	}
}
