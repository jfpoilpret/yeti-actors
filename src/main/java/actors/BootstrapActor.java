package actors;

import yeti.lang.FailureException;
import yeti.lang.Fun;

final class BootstrapActor extends AbstractActor
{
	BootstrapActor(long id, Fun init, Fun receiver)
	{
		super(id, init, receiver);
		_thread = Thread.currentThread();
	}
	
	@Override public void start()
	{
		eventLoop();
	}

	@Override protected void checkActorContext(String call)
	{
		if (Thread.currentThread() != _thread)
			throw new FailureException("Can't call " + call + " outside receive function");
	}

	final private Thread _thread;
}
