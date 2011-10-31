package actors;

import yeti.lang.FailureException;
import yeti.lang.Fun;

final class BootstrapActor extends AbstractActor
{
	BootstrapActor(long id, Fun init, Fun receiver)
	{
		super(id, receiver);
		_thread = Thread.currentThread();
		_init = init;
	}
	
	@Override public void start()
	{
		Actors.setId(this);
		_init.apply(null);
		eventLoop();
	}

	@Override protected void checkActorContext(String call)
	{
		if (Thread.currentThread() != _thread)
			throw new FailureException("Can't call " + call + " outside receive function");
	}

	final private Thread _thread;
	final private Fun _init;
}
