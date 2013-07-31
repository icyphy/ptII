#include "_PtidesDirector.h"

struct PtidesDirector* PtidesDirector_New() {
	struct PtidesDirector* newDirector = malloc(sizeof(struct PtidesDirector));
	if (newDirector == NULL) {
		fprintf(stderr, "Allocation error : PtidesDirector_New\n");
		exit(-1);
	}
	PtidesDirector_Init(newDirector);
	newDirector->free = PtidesDirector_New_Free;

	return newDirector;
}
void PtidesDirector_Init(struct PtidesDirector* director) {
	DEDirector_Init((struct DEDirector*)director);

}
void PtidesDirector_New_Free(struct PtidesDirector* director) {
	DEDirector_New_Free((struct DEDirector*) director);
}

void PtidesDirector_AddInputEvent(struct PtidesDirector* director, struct PtidesPort* sourcePort,
		struct PtidesEvent* event, double deviceDelay) {
	if (sourcePort->isNetworkReceiverPort(sourcePort)) {
		double networkDelayBound = sourcePort->networkDelayBound;
		double sourcePlatformDelayBound = sourcePort->sourcePlatformDelayBound;
		if (director->localClock->getLocalTime(director->localClock)
				- event->timeStamp(event) > sourcePlatformDelayBound
				+ networkDelayBound + director->clockSynchronizationErrorBound) {
			char* message;
			sprintf(message, "Event on this network receiver came in too late. \
(Physical time: \
%lf, Event timestamp: %lf, Source platform delay bound: \
%lf, Network delay bound: %lf)", director->localClock->getLocalTime(director->localClock),
event->timeStamp(event), sourcePlatformDelayBound, networkDelayBound);
			event = director->_handleTimingError(director,
					sourcePort,	event, message);
		}
	}

	if (event != NULL) {
		Time inputReady = director->getModelTime(director) + deviceDelay;
		PblList* list = *(pblMapGet(director->_inputEventQueue, &inputReady, sizeof(Time), NULL));
		if (list == NULL) {
			list = pblListNewArrayList();
		}

		pblListAdd(list, event);
		pblMapPut(director->_inputEventQueue, &inputReady, sizeof(Time), &list, sizeof(PblList*), NULL);
	}
}

void PtidesDirector_Fire(struct PtidesDirector* director) {
	// Transfer all inputs that are ready.
	List<PtidesEvent> list = _inputEventQueue.get(getModelTime());
	if (list != null) {
		for (PtidesEvent event : list) {
			if (event.ioPort() != null) {
				_currentLogicalTime = event.timeStamp();
				_currentSourceTimestamp = event.sourceTimestamp();
				_currentLogicalIndex = event.microstep();
				event.receiver().put(event.token());
				_currentLogicalTime = null;
				if (_debugging) {
					_debug("iiiiiiii - transfer inputs from "
							+ event.ioPort());
				}
			}
		}
		_inputEventQueue.remove(getModelTime());
	}

	super.fire();

	// Transfer all outputs to the ports that are ready.
	list = _outputEventDeadlines.get(getModelTime());
	if (list != null) {
		for (PtidesEvent event : list) {
			_currentLogicalTime = event.timeStamp();
			_currentSourceTimestamp = event.sourceTimestamp();
			_currentLogicalIndex = event.microstep();
			if (event.ioPort() instanceof PtidesPort) {
				double deviceDelay = _getDoubleParameterValue(
						event.ioPort(), "deviceDelay");

				Queue<PtidesEvent> ptidesOutputPortList = _ptidesOutputPortEventQueue
						.get(event.ioPort());
				if (ptidesOutputPortList == null) {
					ptidesOutputPortList = new LinkedList<PtidesEvent>();
				}

				// modify deadline of event such that it will be output after deviceDelay
				PtidesEvent newEvent = new PtidesEvent(event.ioPort(),
						event.channel(), event.timeStamp(),
						event.microstep(), event.depth(), event.token(),
						event.receiver(), localClock.getLocalTime().add(
								deviceDelay), event.sourceTimestamp());

				ptidesOutputPortList.add(newEvent);

				_ptidesOutputPortEventQueue.put(
						(PtidesPort) event.ioPort(), ptidesOutputPortList);
			}
			_currentLogicalTime = null;
		}
		_outputEventDeadlines.remove(getModelTime());
	}

	// Transfer all outputs from ports to the outside
	for (PtidesPort port : _ptidesOutputPortEventQueue.keySet()) {
		Queue<PtidesEvent> ptidesOutputPortList = _ptidesOutputPortEventQueue
				.get(port);
		if (ptidesOutputPortList != null && ptidesOutputPortList.size() > 0) {
			PtidesEvent event = ptidesOutputPortList.peek();
			if (event.absoluteDeadline().equals(localClock.getLocalTime())) {
				_currentLogicalTime = event.timeStamp();
				_currentSourceTimestamp = event.sourceTimestamp();
				_currentLogicalIndex = event.microstep();
				event.ioPort().send(0, event.token());
				_currentLogicalTime = null;
				ptidesOutputPortList.poll();
			}
		}
	}
}
Time DEDirector_FireAt(struct DEDirector* director, struct Actor* actor, Time time, int index) {
	Time result = time;
	if (director->_delegateFireAt) {
		if (result < (*(director->getModelTime))((struct Director*)director)) {
			result = (*(director->getModelTime))((struct Director*)director);
		}
		result = (*(director->fireContainerAt))((struct Director*)director, result, index);
	}
	else {
		if (result < (*(director->getModelTime))((struct Director*)director)) {
			result = (*(director->getModelTime))((struct Director*)director);
		}
	}

	if (result == (*(director->getModelTime))((struct Director*)director)
			&& index <= director->_microstep
			&& !director->_isInitializing) {
		index = director->_microstep + 1;

		if (index == INT_MAX) {
			fprintf(stderr, "Microstep has hit the maximum while scheduling a firing : DEDirector_FireAt\n");
			exit(-1);
		}
	}

	(*(director->_enqueueEvent))(director, actor, result, index);

	return result;
}
int DEDirector_GetMicrostep(struct DEDirector* director) {
	return director->_microstep;
}
Time DEDirector_GetNextEventTime(struct DEDirector* director) {
	if ((*(director->_eventQueue->size))(director->_eventQueue) == 0) {
		return -DBL_MAX;
	}
	struct DEEvent* nextEvent = (*(director->_eventQueue->get))(director->_eventQueue);
	return (*(nextEvent->timeStamp))(nextEvent);
}
void DEDirector_Initialize(struct DEDirector* director) {
	director->_isInitializing = true;

	(*(director->_eventQueue->clear))(director->_eventQueue);

	director->_disabledActors = NULL;
	director->_exceedStopTime = false;
	director->_noMoreActorsToFire = false;
	director->_microstep = 0;

	if ((*(director->isEmbedded))((struct Director*)director)) {
		struct CompositeActor* container = director->container;
		struct Director* executiveDirector = (*(container->getExecutiveDirector))(container);

		if (IS_DEDIRECTOR(executiveDirector) && !(*(director->isTopLevel))((struct Director*)director)) {
			struct DEDirector* executiveDirectorDE = (struct DEDirector*) executiveDirector;
			director->_microstep = (*(executiveDirectorDE->getMicrostep))(executiveDirectorDE);
		}
	}
	Director_Initialize((struct Director*)director);

	Time stopTime = (*(director->getModelStopTime))((struct Director*)director);
	if (stopTime != DBL_MAX) {
		(*(director->fireAt))(director, (struct Actor*)director->container, stopTime, 1);
	}

	if ((*(director->isEmbedded))((struct Director*)director) && !(*(director->_eventQueue->isEmpty))(director->_eventQueue)) {
		(*(director->_requestFiring))(director);
		director->_delegateFireAt = true;
	} else {
		director->_delegateFireAt = false;
	}

	director->_isInitializing = false;
}
bool DEDirector_Postfire(struct DEDirector* director) {
    bool result = Director_Postfire((struct Director*)director);

    struct CompositeActor* container = director->container;
    PblIterator* outports = pblIteratorNew((*(container->outputPortList))(container));
    bool moreOutputsToTransfer = false;
    while (pblIteratorHasNext(outports) && !moreOutputsToTransfer) {
        struct IOPort* outport = pblIteratorNext(outports);
        for (int i = 0; i < (*(outport->getWidthInside))(outport); i++) {
            if ((*(outport->hasTokenInside))(outport, i)) {
                moreOutputsToTransfer = true;
                break;
            }
        }
    }

    if (!(*(director->_eventQueue->isEmpty))(director->_eventQueue) && !moreOutputsToTransfer) {
    	struct DEEvent* next = (*(director->_eventQueue->get))(director->_eventQueue);
		if ((*(next->timeStamp))(next) > (*(director->getModelTime))((struct Director*)director)) {
			director->_microstep = 0;
		}
	}
	bool stop = director->stopWhenQueueIsEmpty;

	if (moreOutputsToTransfer) {
		(*(director->fireContainerAt))((struct Director*)director, (*(director->getModelTime))((struct Director*)director),
				(*(director->getMicrostep))(director));
	} else if (director->_noMoreActorsToFire
			&& (stop || (*(director->getModelTime))((struct Director*)director) == (*(director->getModelStopTime))((struct Director*)director))) {
		director->_exceedStopTime = true;
		result = false;
	} else if (director->_exceedStopTime) {
		result = false;
	} else if ((*(director->isEmbedded))((struct Director*)director)
			&& !(*(director->_eventQueue->isEmpty))(director->_eventQueue)) {
		(*(director->_requestFiring))(director);
	}

    if ((*(director->isEmbedded))((struct Director*)director) ) {
        director->_delegateFireAt = true;
    }
    return result;
}

bool DEDirector_Prefire(struct DEDirector* director) {
	Director_Prefire((struct Director*)director);

	if ((*(director->isEmbedded))((struct Director*)director)) {
		struct CompositeActor* container = director->container;
		struct Director* executiveDirector = (*(container->getExecutiveDirector))(container);
		if (IS_DEDIRECTOR(executiveDirector) && !(*(director->isTopLevel))((struct Director*)director)) {
			struct DEDirector* executiveDirectorDE = (struct DEDirector*) executiveDirector;
			director->_microstep = (*(executiveDirectorDE->getMicrostep))(executiveDirectorDE);
		}
	}

	// A top-level DE director is always ready to fire.
	if ((*(director->isTopLevel))((struct Director*)director)) {
		return true;
	}

	Time modelTime = (*(director->getModelTime))((struct Director*)director);
	Time nextEventTime = DBL_MAX;

	if (!(*(director->_eventQueue->isEmpty))(director->_eventQueue)) {
		struct DEEvent* nextEvent = (*(director->_eventQueue->get))(director->_eventQueue);
		nextEventTime = (*(nextEvent->timeStamp))(nextEvent);
	}

	while (modelTime > nextEventTime) {
		(*(director->_eventQueue->take))(director->_eventQueue);
		if (!(*(director->_eventQueue->isEmpty))(director->_eventQueue)) {
			struct DEEvent* nextEvent = (*(director->_eventQueue->get))(director->_eventQueue);
			nextEventTime = (*(nextEvent->timeStamp))(nextEvent);
		} else {
			nextEventTime = DBL_MAX;
		}
	}
	director->_delegateFireAt = false;
	return true;
}
void DEDirector_Preinitialize(struct DEDirector* director) {
	director->_eventQueue = CalendarQueue_New();

	director->_eventQueue->_logMinNumBuckets = log(director->minBinCount);
	director->_eventQueue->_logQueueBinCountFactor = log(director->binCountFactor);
	director->_eventQueue->_resizeEnabled = director->isCQAdaptive;

	Director_Preinitialize((struct Director*)director);
}
void DEDirector_Wrapup(struct DEDirector* director) {
	Director_Wrapup((struct Director*)director);
	if (director->_disabledActors)
		pblSetFree(director->_disabledActors);
	(*(director->_eventQueue->clear))(director->_eventQueue);

	director->_noMoreActorsToFire = false;
	director->_microstep = 0;
}
bool DEDirector__CheckForNextEvent(struct DEDirector* director) {
	if (!(*(director->_eventQueue->isEmpty))(director->_eventQueue)) {
		struct DEEvent* next = (*(director->_eventQueue->get))(director->_eventQueue);

		if ((*(next->timeStamp))(next) > (*(director->getModelTime))((struct Director*)director)) {
			return false;
		} else if ((*(next->microstep))(next) > director->_microstep) {
			return false;
		} else if ((*(next->timeStamp))(next) < (*(director->getModelTime))((struct Director*)director)
				|| (*(next->microstep))(next) < director->_microstep) {
			fprintf(stderr, "he tag of the next event can not be less than the current tag : DEDirector__CheckForNextEvent\n");
			exit(-1);
		}
	}
	return true;
}
void DEDirector__DisableActor(struct DEDirector* director, struct Actor* actor) {
	if (actor != NULL) {
		if (director->_disabledActors == NULL) {
			director->_disabledActors = pblSetNewHashSet();
		}

		pblSetAdd(director->_disabledActors, actor);
	}
}

void DEDirector__EnqueueEvent(struct DEDirector* director, struct Actor* actor, Time time, int defaultMicrostep) {
	if (director->_eventQueue == NULL || (director->_disabledActors != NULL
			&& pblSetContains(director->_disabledActors, actor))) {
		return;
	}

	int microstep = 1;
	if (time == (*(director->getModelTime))((struct Director*)director) && microstep <= director->_microstep) {
		if (!director->_isInitializing) {
			microstep = director->_microstep + 1;

			if (microstep == INT_MAX) {
				fprintf(stderr, "Microstep has hit the maximum while scheduling a firing : DEDirector__EnqueueEvent\n");
				exit(-1);
			}
		}
	} else if (time < (*(director->getModelTime))((struct Director*)director) ) {
		fprintf(stderr, "Attempt to queue an event in the past : DEDirector__EnqueueEvent\n");
		exit(-1);
	}

	int depth = * ((int*)pblMapGet(director->actorsDepths, &actor, sizeof (struct Actor*), NULL));

	struct DEEvent* newEvent = DEEvent_New();
	newEvent->_actor = actor;
	newEvent->_timestamp = time;
	newEvent->_microstep = microstep;
	newEvent->_depth = depth;
	(*(director->_eventQueue->put))(director->_eventQueue, newEvent);
}
void DEDirector__EnqueueTriggerEvent(struct DEDirector* director, struct IOPort* ioPort, Time time) {
	struct Actor* actor = ioPort->container;
	if (director->_eventQueue == NULL || (director->_disabledActors != NULL
			&& pblSetContains(director->_disabledActors, actor))) {
		return;
	}

	int depth = * ((int*)(pblMapGet(director->portsDepths, &ioPort, sizeof(struct IOPort*), NULL)));

	int microstep = director->_microstep;
	if (microstep < 1) {
		microstep = 1;
	}

	// Register this trigger event.
	struct DEEvent* newEvent = DEEvent_New();
	newEvent->_ioPort = ioPort;
	newEvent->_actor = ioPort->container;
	newEvent->_timestamp = time;
	newEvent->_microstep = microstep;
	newEvent->_depth = depth;
	(*(director->_eventQueue->put))(director->_eventQueue, newEvent);
}
int DEDirector__Fire(struct DEDirector* director) {
	struct Actor* actorToFire = DEDirector__GetNextActorToFire(director);

	if (actorToFire == NULL) {
		director->_noMoreActorsToFire = true;
		return -1;
	}

	if ((struct CompositeActor*)actorToFire == (struct CompositeActor*)director->container) {
		return 1;
	}

	if (!(*(actorToFire->prefire))(actorToFire)) {
		return 0;
	}

	(*(actorToFire->fire))(actorToFire);

	if (!(*(actorToFire->postfire))(actorToFire)) {
		(*(director->_disableActor))(director, actorToFire);
	}
	return 0;
}
struct Actor* DEDirector__GetNextActorToFire(struct DEDirector* director) {
	if (director->_eventQueue == NULL) {
		fprintf(stderr, "Fire method called before the preinitialize method : DEDirector__GetNextActorToFire\n");
		exit(-1);
	}

	struct Actor* actorToFire = NULL;
	struct DEEvent* lastFoundEvent = NULL;
	struct DEEvent* nextEvent = NULL;

	while (true) {
		if (director->stopWhenQueueIsEmpty) {
			if ((*(director->_eventQueue->isEmpty))(director->_eventQueue)) {
				break;
			}
		}

		if ((*(director->isEmbedded))((struct Director*)director)) {
			if ((*(director->_eventQueue->isEmpty))(director->_eventQueue)) {
				break;
			}
			nextEvent = (*(director->_eventQueue->get))(director->_eventQueue);
			if ((*(nextEvent->timeStamp))(nextEvent) < (*(director->getModelTime))((struct Director*)director)) {
				fprintf(stderr, "An event was missed : DEDirector__GetNextActorToFire\n");
				exit(-1);
			}

			bool microstepMatches = true;
			struct CompositeActor* container = director->container;
			struct Director* executiveDirector = (*(container->getExecutiveDirector))(container);
			if (IS_DEDIRECTOR(executiveDirector) && !(*(director->isTopLevel))((struct Director*)director)) {
				microstepMatches = (*(nextEvent->microstep))(nextEvent) <= director->_microstep;
			}

			int comparison = (*(nextEvent->timeStamp))(nextEvent) - (*(director->getModelTime))((struct Director*)director);
			if (comparison > 0 || (comparison == 0 && !microstepMatches)) {
				nextEvent = NULL;
				break;
			}
		} else {
			if ((*(director->_eventQueue->isEmpty))(director->_eventQueue)) {
				if (actorToFire != NULL
						|| (*(director->getModelTime))((struct Director*)director) == (*(director->getModelStopTime))((struct Director*)director)) {
					break;
				}
				return NULL;
			}
			nextEvent = (*(director->_eventQueue->get))(director->_eventQueue);
		}

		if (actorToFire == NULL) {
			Time currentTime;
			lastFoundEvent = (*(director->_eventQueue->take))(director->_eventQueue);
			currentTime = (*(lastFoundEvent->timeStamp))(lastFoundEvent);
			actorToFire = (*(lastFoundEvent->actor))(lastFoundEvent);

			if (director->_disabledActors != NULL
					&& pblSetContains(director->_disabledActors, actorToFire)) {
				actorToFire = NULL;
				continue;
			}

			director->localClock->_localTime = currentTime;
			director->_microstep = (*(lastFoundEvent->microstep))(lastFoundEvent);

			if (currentTime > (*(director->getModelStopTime))((struct Director*)director)) {
				director->_exceedStopTime = true;
				return NULL;
			}
		} else {
			if ((*(nextEvent->hasTheSameTagAs))(nextEvent, lastFoundEvent)
					&& (*(nextEvent->actor))(nextEvent) == actorToFire) {
				(*(director->_eventQueue->take))(director->_eventQueue);
			} else {
				break;
			}
		}
	}

	return actorToFire;
}
void DEDirector__RequestFiring(struct DEDirector* director) {
	struct DEEvent* nextEvent = NULL;
	nextEvent = (*(director->_eventQueue->get))(director->_eventQueue);

	(*(director->fireContainerAt))((struct Director*)director, (*(nextEvent->timeStamp))(nextEvent), (*(nextEvent->microstep))(nextEvent));
}

