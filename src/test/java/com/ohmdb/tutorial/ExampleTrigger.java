package com.ohmdb.tutorial;

import com.ohmdb.api.Trigger;
import com.ohmdb.api.TriggerAction;

public class ExampleTrigger<T> implements Trigger<User> {

	@Override
	public void process(TriggerAction action, long id, User oldEntity,
			User newEntity) {
		// your trigger action goes here ...

	}

}
