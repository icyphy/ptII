#include "../includes/DEDirector.h"

void DEDirectorFireAt(DEDirector * director, Actor * actor,
		Time time, int microstep) {
	Time result = time;
	// We cannot schedule an event in the past !
	if (result < director->currentModelTime)
		result = director->currentModelTime;
	// Incrementing microstep is wrong during initialization
//	if (result == director->currentModelTime &&
//			microstep <= director->currentMicrostep &&
//			!director->isInitializing)
//		microstep = director->currentMicrostep + 1;

	int depth = actor->depth;
	int priority = actor->priority;
	DEEvent * newEvent = newDEEventWithParam(actor, NULL, depth,
			microstep, priority, result);

	CQueuePut(&(director->cqueue), newEvent);

	return;
}

int DEDirectorFire(DEDirector * director){
	// Find the next actor to be fired.
	Actor * actorToFire = DEDirectorGetNextActorToFire(director);
	director->currentActor = actorToFire;

	// Check whether the actor to be fired is null.
	// -- If the actor to be fired is null
	if (actorToFire == NULL) {
		// TODO : here we can deal with compositeActors
		// a null actor means that there are
		// no events in the event queue.
		director->noMoreActorToFire = true;
		return -1;
	}

	if (actorToFire == director->containerActor) {
		return 1;
	}

	// Keep firing the actor to be fired until there are no more input
	// tokens available in any of its input ports with the same tag, or its prefire()
	// method returns false.
	bool refire;

	do {
		refire = false;

		bool prefire = (*(actorToFire->prefireFunction))();
		if (!prefire) {
			break;
		}

		(*(actorToFire->fireFunction))();

		(*(actorToFire->postfireFunction))();

	} while (refire); // close the do {...} while () loop
	return 0;
}

Actor * DEDirectorGetNextActorToFire(DEDirector * director){
	Actor * actorToFire = NULL;
	DEEvent * lastFoundEvent = NULL;
	DEEvent * nextEvent = NULL;

	// Keep taking events out until there are no more events that have the
	// same tag and go to the same destination actor, or until the queue is
	// empty
	while (true) {
		// Get the next event from the event queue.
		if (director->stopWhenQueueIsEmpty) {
			if (CQueueIsEmpty(&(director->cqueue))) {
				// If the event queue is empty,
				// jump out of the loop
				break;
			}
		}

		//TODO : here we can deal with embedded actors

		// If the event queue is empty, normally
		// a blocking read is performed on the queue.
		// However, there are two conditions that the blocking
		// read is not performed, which are checked below.
		if (CQueueIsEmpty(&(director->cqueue))) {
			// The two conditions are:
			// 1. An actor to be fired has been found; or
			// 2. There are no more events in the event queue,
			// and the current time is equal to the stop time.
			if (actorToFire != NULL
					|| director->currentModelTime >= director->stopTime) {
				// jump out of the loop
				break;
			}
			else
				return NULL;
		}

		// To reach this point, either the event queue is not empty,
		// or _stopRequested or _stopFireRequested is true, or an interrupted exception
		// happened.

		// At least one event is found in the event queue.
		nextEvent = CQueueGet(&(director->cqueue));

		// This is the end of the different behaviors of embedded and
		// top-level directors on getting the next event.
		// When this point is reached, the nextEvent can not be null.
		// In the rest of this method, this is not checked any more.

		// If the actorToFire is null, find the destination actor associated
		// with the event just found. Store this event as lastFoundEvent and
		// go back to continue the GetNextEvent loop.
		// Otherwise, check whether the event just found goes to the
		// same actor to be fired. If so, dequeue that event and continue
		// the GetNextEvent loop. Otherwise, jump out of the GetNextEvent
		// loop.
		if (actorToFire == NULL) {
			// If the actorToFire is not set yet,
			// find the actor associated with the event just found,
			// and update the current tag with the event tag.
			Time currentTime;

			// TODO : here we can deal with the real time
			// if (!_synchronizeToRealTime)

			lastFoundEvent = CQueueTake(&(director->cqueue));
			currentTime = lastFoundEvent->timestamp;
			actorToFire = lastFoundEvent->actor;

			// Advance the current time to the event time.
			// NOTE: This is the only place that the model time changes.
			director->currentModelTime = currentTime;

			// Advance the current microstep to the event microstep.
			if (lastFoundEvent->microstep == 0)
				lastFoundEvent->microstep = 1;
			director->currentMicrostep = lastFoundEvent->microstep;

			// Exceeding stop time means the current time is strictly
			// bigger than the model stop time.
			if (director->currentModelTime > director->stopTime) {
				director->exceedStopTime = true;
				return NULL;
			}
		} else { // i.e., actorToFire != null
			// In a previous iteration of this while loop,
			// we have already found an event and the actor to react to it.
			// Check whether the newly found event has the same tag
			// and destination actor. If so, they are
			// handled at the same time. For example, a pure
			// event and a trigger event that go to the same actor.
			if (nextEvent->timestamp == lastFoundEvent->timestamp
					&& nextEvent->microstep == lastFoundEvent->microstep
					&& nextEvent->actor == actorToFire) {
				// Consume the event from the queue and discard it.
				// In theory, there should be no event with the same depth
				// as well as tag because
				// the DEEvent class equals() method returns true in this
				// case, and the CalendarQueue class does not enqueue an
				// event that is equal to one already on the queue.
				CQueueTake(&(director->cqueue));
			} else {
				// Next event has a future tag or a different destination.
				break;
			}
		}
	} // close the loop

	// Note that the actor to be fired can be null.
	return actorToFire;
}
