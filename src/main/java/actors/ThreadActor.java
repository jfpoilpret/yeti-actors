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

import yeti.lang.FailureException;
import yeti.lang.Fun;

class ThreadActor extends AbstractActor
{
	ThreadActor(long id, Fun init, Fun receiver)
	{
		super(id, init, receiver);
		_thread = new Thread()
		{
			@Override public void run()
			{
				eventLoop();
			}
		};
	}

	@Override public void start()
	{
		_thread.start();
	}
	
	@Override protected void checkActorContext(String call)
	{
		if (Thread.currentThread() != _thread)
			throw new FailureException("Can't call " + call + " outside receive function");
	}

	private final Thread _thread;
}
