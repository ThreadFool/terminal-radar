package threadfool.op;

import java.util.function.Consumer;

public class App
{
	public static void main(String[] args)
	{
		MessageReceiver messageReceiver = new MessageReceiver();
		Thread recieverThread = new Thread(messageReceiver);

		recieverThread.start();
	}
}
