package threadfool.op;

import java.util.concurrent.BlockingQueue;

public class DbWriter implements Runnable
{

	private final BlockingQueue<AircraftSnapshot> queue;

	public DbWriter(BlockingQueue<AircraftSnapshot> queue) {
		this.queue = queue;
	}

	@Override
	public void run()
	{

	}
}
