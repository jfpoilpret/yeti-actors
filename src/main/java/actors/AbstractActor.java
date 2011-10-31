package actors;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import yeti.lang.Fun;

abstract class AbstractActor implements Actor
{
	protected AbstractActor(long id, Fun receiver)
	{
		_id = id;
		_receiver = receiver;
	}
	
	final protected void eventLoop()
	{
		//TODO don't we ever stop looping forever?
		while (true)
		{
			try
			{
				_current = _queue.take();
				callReceiver();
			}
			catch (InterruptedException e)
			{
				//FIXME what to do if this occurs?
				e.printStackTrace();
			}
		}
	}

	protected void callReceiver()
	{
		try
		{
			_receiver.apply(_current.message);
		}
		catch (RuntimeException e)
		{
			//FIXME what to do if this occurs?
			e.printStackTrace();
		}
	}

	@Override final public long id()
	{
		return _id;
	}

	@Override final public void changeReceiver(Fun receive)
	{
		checkActorContext("changeReceiver");
		_receiver = receive;
	}

	@Override final public void send(Actor target, Object message)
	{
		checkActorContext("(!)");
		target.push(this, message);
	}

	@Override final public void push(Actor source, Object message)
	{
		_queue.add(new MessageInfo(source, message));
	}

	@Override final public void reply(Object message)
	{
		checkActorContext("reply");
		_current.source.push(this, message);
	}

	@Override final public Actor source()
	{
		checkActorContext("source()");
		return _current.source;
	}

	@Override final public Object message()
	{
		checkActorContext("message()");
		return _current.message;
	}

	abstract protected void checkActorContext(String call);

	protected static final class MessageInfo
	{
		MessageInfo(Actor source, Object message)
		{
			this.source = source;
			this.message = message;
		}
		
		final private Actor source;
		final private Object message;
	}

	protected final long _id;
	private final BlockingQueue<MessageInfo> _queue = new LinkedBlockingQueue<MessageInfo>();
	private Fun _receiver;
	private MessageInfo _current = null;
}
