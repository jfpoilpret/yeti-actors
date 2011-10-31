//  Copyright 2011 Jean-Francois Poilpret
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package actors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import yeti.lang.FailureException;
import yeti.lang.Fun;

//FIXME _currentActor is never set!!!
public final class Actors
{
	// Methods to bootstrap the system and create actors
	//===================================================
	
	static public synchronized void bootstrap(Fun init, Fun receive)
	{
		// First check this function was never called before
		if (_bootstrapId != null)
			throw new FailureException("Can't call bootstrap more than once");	
		_bootstrapId = newId();
		//FIXME all accesses to _actors should be synchronized!
		Actor actor = new BootstrapActor(_bootstrapId, init, receive);
		_actors.put(_bootstrapId, actor);
		actor.start();
	}
	
	static public long newActor(Fun receive)
	{
		// First check that we are in an actor context (otherwise throw exception)
		checkCurrent("newActor");
		// Create new id and actor
		long id = newId();
		Actor actor = new ThreadActor(id, receive);
		//FIXME all accesses to _actors should be synchronized!
		_actors.put(id, actor);
		actor.start();
		return id;
	}
	
	static public long newEdtActor(Fun receive)
	{
		//TODO
		return -1;
	}

	// Messaging methods
	//===================
	static public void send(long recipient, Object message)
	{
		Actor current = checkCurrent("(!)");
		Actor target = checkActor("(!)", recipient);
		current.send(target, message);
	}
	
	static public void reply(Object message)
	{
		checkCurrent("reply").reply(message);
	}
	
	// Information methods that can be called during message processing
	//==================================================================
	
	static public long self()
	{
		return checkCurrent("self").id();
	}
	
	static public long source()
	{
		return checkCurrent("source").source().id();
	}

	static public void changeReceiver(Fun receive)
	{
		checkCurrent("changeReceiver").changeReceiver(receive);
	}

	// Internal utility methods
	//==========================
	
	static private Actor checkCurrent(String call)
	{
		Long id = _currentActor.get();
		if (id == null)
			throw new FailureException("Can't call " + call + " outside an actor context");	
		return _actors.get(id);
	}
	
	static private Actor checkActor(String call, long id)
	{
		Actor actor = _actors.get(id);
		if (actor == null)
			throw new FailureException(call + " must be passed a valid actor");	
		return actor;
	}
	
	static private long newId()
	{
		return System.nanoTime() * 1000L + _idSequence.getAndIncrement();
	}

	static void setId(Actor actor)
	{
		_currentActor.set(actor.id());
	}

	static private Long _bootstrapId = null;
	static final private ThreadLocal<Long> _currentActor = new ThreadLocal<Long>();
	static final private Map<Long, Actor> _actors = new HashMap<Long, Actor>();
	static final private AtomicLong _idSequence = new AtomicLong(1L);
}
