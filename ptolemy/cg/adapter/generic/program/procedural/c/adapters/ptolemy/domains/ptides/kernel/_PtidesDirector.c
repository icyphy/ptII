#include "_PtidesDirector.h"

#define MAX(a,b) ((a) > (b) ? (a) : (b))

struct PtidesDirector* PtidesDirector_New() {
        struct PtidesDirector* newDirector = calloc(1, sizeof(struct PtidesDirector));
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

        director->addInputEvent = PtidesDirector_AddInputEvent;
        director->fire = PtidesDirector_Fire;
        director->fireAt = PtidesDirector_FireAt;
        director->getModelTime = PtidesDirector_GetModelTime;
        director->getMicrostep = PtidesDirector_GetMicrostep;
        director->postfire = PtidesDirector_Postfire;
        director->prefire = PtidesDirector_Prefire;
        director->preinitialize = PtidesDirector_Preinitialize;
        director->_checkForNextEvent = PtidesDirector__CheckForNextEvent;
        director->_enqueueTriggerEvent1 = PtidesDirector__EnqueueTriggerEvent;
        director->_getNextActorToFire = PtidesDirector__GetNextActorToFire;
        director->_getNextActorFrom = PtidesDirector__GetNextActorFrom;
        director->_getSuperdenseDependencyPair = PtidesDirector__GetSuperdenseDependencyPair;
        director->_isSafeToProcess = PtidesDirector__IsSafeToProcess;
        director->_removeEventsFromQueue = PtidesDirector__RemoveEventsFromQueue;
        director->_setNextFireTime = PtidesDirector__SetNextFireTime;

        director->clockSynchronizationErrorBound = 0.0;
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
                        fprintf(stderr, "Event on this network receiver came in too late. \
(Physical time: \
%lf, Event timestamp: %lf, Source platform delay bound: \
%lf, Network delay bound: %lf)", director->localClock->getLocalTime(director->localClock),
event->timeStamp(event), sourcePlatformDelayBound, networkDelayBound);
                        exit(-1);
//                        event = director->_handleTimingError(director,
//                                        sourcePort,        event, message);
                }
        }

        if (event != NULL) {
                Time inputReady = director->getModelTime(director) + deviceDelay;
                PblList** list = (PblList**)pblMapGet(director->_inputEventQueue, &inputReady, sizeof(Time), NULL);
                PblList* newList;
                if (list == NULL)
                        newList = pblListNewArrayList();
                else
                        newList = *list;

                pblListAdd(newList, event);
                pblMapPut(director->_inputEventQueue, &inputReady, sizeof(Time), &newList, sizeof(PblList*), NULL);
        }
}

void PtidesDirector_Fire(struct PtidesDirector* director) {
        // Transfer all inputs that are ready.
        Time currentTime = director->getModelTime(director);
        PblList** list = (PblList**)pblMapGet(director->_inputEventQueue, &currentTime, sizeof(Time), NULL);
        if (list != NULL) {
                PblIterator* eventIterator = pblIteratorNew(*list);
                while (pblIteratorHasNext(eventIterator)) {
                        struct PtidesEvent* event = pblIteratorNext(eventIterator);
                        if (event->ioPort(event) != NULL) {
                                director->_currentLogicalTime = event->timeStamp(event);
                                director->_currentSourceTimestamp = event->sourceTimestamp(event);
                                director->_currentLogicalIndex = event->microstep(event);
                                struct Receiver* r = event->receiver(event);
                                r->put(r, event->token(event));
                                director->_currentLogicalTime = -DBL_MAX;
                        }
                }
                pblIteratorFree(eventIterator);
                pblMapRemove(director->_inputEventQueue, &currentTime, sizeof(Time), NULL);
        }

        DEDirector_Fire((struct DEDirector*) director);

        // Transfer all outputs to the ports that are ready.
        list = (PblList**)pblMapGet(director->_outputEventDeadlines, &currentTime, sizeof(Time), NULL);
        if (list != NULL) {
                PblIterator* eventIterator = pblIteratorNew(*list);
                while (pblIteratorHasNext(eventIterator)) {
                        struct PtidesEvent* event = pblIteratorNext(eventIterator);
                        director->_currentLogicalTime = event->timeStamp(event);
                        director->_currentSourceTimestamp = event->sourceTimestamp(event);
                        director->_currentLogicalIndex = event->microstep(event);
                        if (IS_PTIDESPORT(event->ioPort(event))) {
                                struct PtidesPort* port = (struct PtidesPort*) event->ioPort(event);
                                double deviceDelay = port->deviceDelay;

                                PblList** ptidesOutputPortList = (PblList**)pblMapGet(director->_ptidesOutputPortEventQueue, &port,
                                                sizeof(struct PtidesPort*), NULL);
                                PblList* newList;
                                if (ptidesOutputPortList == NULL)
                                        newList = pblListNewLinkedList();
                                else
                                        newList = *ptidesOutputPortList;

                                // modify deadline of event such that it will be output after deviceDelay
                                struct PtidesEvent* newEvent = PtidesEvent_New();
                                newEvent->_ioPort = event->ioPort(event);
                                newEvent->_channel = event->channel(event);
                                newEvent->_timestamp = event->timeStamp(event);
                                newEvent->_microstep = event->microstep(event);
                                newEvent->_depth = event->depth(event);
                                newEvent->_token = event->token(event);
                                newEvent->_receiver = event->receiver(event);
                                newEvent->_absoluteDeadline = director->localClock->getLocalTime(director->localClock) + deviceDelay;
                                newEvent->_sourceTimestamp = event->sourceTimestamp(event);

                                pblListAdd(newList, newEvent);

                                pblMapPut(director->_ptidesOutputPortEventQueue, &port, sizeof(struct PtidesPort*),
                                                &newList, sizeof(PblList*), NULL);
                        }
                        director->_currentLogicalTime = -DBL_MAX;
                }
                pblMapRemove(director->_outputEventDeadlines, &currentTime, sizeof(Time), NULL);
        }

        // Transfer all outputs from ports to the outside
        PblIterator* outputIterator = pblMapIteratorNew(director->_ptidesOutputPortEventQueue);
        while (pblIteratorHasNext(outputIterator)) {
                PblMapEntry* entry = pblIteratorNext(outputIterator);
                //struct PtidesPort* port = *((struct PtidesPort**)pblMapEntryKey(entry));
                PblList** ptidesOutputPortListPointer = (PblList**)pblMapEntryValue(entry);
                if (ptidesOutputPortListPointer != NULL && pblListSize(*ptidesOutputPortListPointer) > 0) {
                        struct PtidesEvent* event = pblListPeek(*ptidesOutputPortListPointer);
                        if (event->absoluteDeadline(event) == director->localClock->getLocalTime(director->localClock)) {
                                director->_currentLogicalTime = event->timeStamp(event);
                                director->_currentSourceTimestamp = event->sourceTimestamp(event);
                                director->_currentLogicalIndex = event->microstep(event);
                                struct IOPort* port = event->ioPort(event);
                                port->send(port, 0, event->token(event));
                                director->_currentLogicalTime = -DBL_MAX;
                                pblListPoll(*ptidesOutputPortListPointer);
                        }
                }
        }
}

Time PtidesDirector_FireAt(struct PtidesDirector* director, struct Actor* actor, Time time, int index) {
        if (actor == (struct Actor*)director->container) {
                director->fireContainerAt(director, time, 1);
                return time;
        }
        int newIndex = index;
        if (director->_currentLogicalTime != -DBL_MAX
                        && director->_currentLogicalTime == time
                        && index <= director->getMicrostep(director)) {
                newIndex = MAX(director->getMicrostep(director), index) + 1;
        }

        if (director->_isInitializing) {
                director->_currentSourceTimestamp = time;
        }

        struct PtidesEvent* newEvent = PtidesEvent_New();
        newEvent->_actor = actor;
        newEvent->_timestamp = time;
        newEvent->_microstep = newIndex;
        int depth = * ((int*)pblMapGet(director->actorsDepths, &actor, sizeof (struct Actor*), NULL));
        newEvent->_depth = depth;
        newEvent->_absoluteDeadline = 0.0;
        newEvent->_sourceTimestamp = director->_currentSourceTimestamp;

        director->_pureEvents->put(director->_pureEvents, newEvent);
        director->_currentSourceTimestamp = -DBL_MAX;

        Time environmentTime = director->getEnvironmentTime(director);
        if (environmentTime <= time) {
                director->fireContainerAt(director, time, 1);
        }
        return time;
}
Time PtidesDirector_GetModelTime(struct PtidesDirector* director) {
        if (director->_currentLogicalTime != -DBL_MAX) {
                return director->_currentLogicalTime;
        }
        return Director_GetModelTime((struct Director*) director);
}


int PtidesDirector_GetMicrostep(struct PtidesDirector* director) {
        if (director->_currentLogicalTime != -DBL_MAX) {
                return director->_currentLogicalIndex;
        }
        return DEDirector_GetMicrostep((struct DEDirector*) director);
}

bool PtidesDirector_Postfire(struct PtidesDirector* director) {
        bool result = true;
        if (director->getModelTime(director) >= director->getModelStopTime(director)) {
                if (director->_eventQueue->size(director->_eventQueue) == 0) {
                        result = false;
                } else {
                        struct PtidesEvent* event = (struct PtidesEvent*) director->_eventQueue->get(director->_eventQueue);
                        if (!(event->_timestamp == director->getModelStopTime(director)))
                                result = false;
                }
        }

        // Potentially set next fire time from _outputEventQueue.
        PblIterator* deliveryIterator = pblMapIteratorNew(director->_outputEventDeadlines);
        Time first = DBL_MAX;
        while (pblIteratorHasNext(deliveryIterator)) {
                PblMapEntry* entry = pblIteratorNext(deliveryIterator);
                Time time = *((Time*)pblMapEntryKey(entry));
                if (time < first)
                        first = time;
        }
        pblIteratorFree(deliveryIterator);

        if (first < DBL_MAX) {
                PblList* events = *((PblList**)pblMapGet(director->_outputEventDeadlines, &first, sizeof(Time), NULL));
                PblIterator* eventsIterator = pblIteratorNew(events);
                while (pblIteratorHasNext(eventsIterator)) {
                        struct PtidesEvent* event = pblIteratorNext(eventsIterator);
                        if (IS_PTIDESPORT(event->ioPort(event))) {
                                struct PtidesPort* port = (struct PtidesPort*)event->ioPort(event);
                                if (port->isActuatorPort(port)
                                                && director->getEnvironmentTime(director) > event->timeStamp(event)) {
                                        fprintf(stderr, "Missed Deadline at platform time \
%lf with logical time %lf at port %s!", director->localClock->getLocalTime(director->localClock),
director->getModelTime(director), port->name);
                                        exit(-1);
                                }
                        }
                }
                director->_setNextFireTime(director, first);
        }

        //... or from _inputEventQueue
        deliveryIterator = pblMapIteratorNew(director->_inputEventQueue);
        first = DBL_MAX;
        while (pblIteratorHasNext(deliveryIterator)) {
                PblMapEntry* entry = pblIteratorNext(deliveryIterator);
                Time time = *((Time*)pblMapEntryKey(entry));
                if (time < first)
                        first = time;
        }
        pblIteratorFree(deliveryIterator);
        if (first < DBL_MAX)
                director->_setNextFireTime(director, first);

        // ... or from ptides output port queue
        PblIterator* outputIterator = pblMapIteratorNew(director->_ptidesOutputPortEventQueue);
        while (pblIteratorHasNext(outputIterator)) {
                PblMapEntry* entry = pblIteratorNext(outputIterator);
                struct PtidesPort* port = *((struct PtidesPort**)pblMapEntryKey(entry));
                PblList* ptidesOutputPortList = *((PblList**)pblMapEntryValue(entry));
                if (ptidesOutputPortList != NULL && pblListSize(ptidesOutputPortList) > 0) {
                        struct PtidesEvent* event = pblListPeek(ptidesOutputPortList);
                        if (port->isActuatorPort(port)
                                        && event->absoluteDeadline(event) < director->getEnvironmentTime(director)) {
                                fprintf(stderr, "Missed Deadline at port %s!", port->name);
                                exit(-1);
                        }
                        director->_setNextFireTime(director, event->absoluteDeadline(event));
                }
        }
        // ... or could also have already been set in safeToProcess().

        // If not null, request refiring.
        if (director->_nextFireTime != -DBL_MAX) {
                director->fireContainerAt(director, director->_nextFireTime, 1);
        }

        return result;
}

bool PtidesDirector_Prefire(struct PtidesDirector* director) {
        Time currentTime = director->localClock->getLocalTimeForCurrentEnvironmentTime(director->localClock);
        director->localClock->setLocalTime(director->localClock, currentTime);
        director->_microstep = 1;
        director->_nextFireTime = -DBL_MAX;
        return true;
}
void PtidesDirector_Preinitialize(struct PtidesDirector* director) {
        DEDirector_Preinitialize((struct DEDirector*)director);
        director->_inputEventQueue = pblMapNewHashMap();
        director->_outputEventDeadlines = pblMapNewHashMap();
        director->_ptidesOutputPortEventQueue = pblMapNewHashMap();
        director->_nextFireTime = DBL_MAX;
        director->_pureEvents = CalendarQueue_New();
        director->_currentLogicalTime = -DBL_MAX;
}

bool PtidesDirector__CheckForNextEvent(struct PtidesDirector* director) {
        return true;
}
void PtidesDirector__EnqueueTriggerEvent(struct PtidesDirector* director, struct IOPort* ioPort, Token* token,
        struct Receiver* receiver) {
        struct Actor* actor = (struct Actor*) ioPort->container;

        if (director->_eventQueue == NULL || (director->_disabledActors != NULL
                        && pblSetContains(director->_disabledActors, actor))) {
                return;
        }
        int depth = * ((int*)(pblMapGet(director->portsDepths, &ioPort, sizeof(struct IOPort*), NULL)));

        struct PtidesEvent* newEvent = PtidesEvent_New();
        newEvent->_ioPort = ioPort;
        newEvent->_channel = ioPort->getChannelForReceiver(ioPort, receiver);
        newEvent->_actor = ioPort->container;
        newEvent->_timestamp = director->getModelTime(director);
        newEvent->_microstep = 1;
        newEvent->_depth = depth;
        newEvent->_token = token;
        newEvent->_receiver = receiver;
        newEvent->_isPureEvent = false;
        newEvent->_sourceTimestamp = director->_currentSourceTimestamp;

        if (ioPort->isOutput(ioPort)) {

                Time deliveryTime;
                deliveryTime = director->localClock->getLocalTime(director->localClock);
                if (((struct PtidesPort*) ioPort)->isActuatorPort((struct PtidesPort*)ioPort)) {
                        if (((struct PtidesPort*) ioPort)->actuateAtEventTimestamp) {
                                deliveryTime = director->getModelTime(director) - ((struct PtidesPort*)ioPort)->deviceDelay;
                        }

                        if (director->getModelTime(director) < deliveryTime) {
                                fprintf(stderr, "Missed Deadline at %s!\n At %lf which is smaller than current platform time %lf",
                                                ((struct PtidesPort*)ioPort)->name, director->getModelTime(director),
                                                director->localClock->getLocalTime(director->localClock));
                                exit(-1);
                        }
                } else if (((struct PtidesPort*) ioPort)->isNetworkTransmitterPort((struct PtidesPort*)ioPort)) {
                        if (director->localClock->getLocalTime(director->localClock) - director->getModelTime(director)
                                        > ((struct PtidesPort*)ioPort)->platformDelayBound) {
                                fprintf(stderr, "Token is being sent out onto the network too late.\
Current platform time: %lf Event timestamp: %lf Platform delay: %lf",
                                                director->localClock->getLocalTime(director->localClock),
                                                director->getModelTime(director),
                                                ((struct PtidesPort*)ioPort)->platformDelayBound);
                        }
                }

                if (newEvent != NULL) {
                        PblList** list = (PblList**)pblMapGet(director->_outputEventDeadlines, &deliveryTime, sizeof(Time), NULL);
                        PblList* newList;
                        if (list == NULL)
                                newList = pblListNewArrayList();
                        else
                                newList = *list;
                        pblListAdd(newList, newEvent);
                        pblMapPut(director->_outputEventDeadlines, &deliveryTime, sizeof(Time), &newList, sizeof(PblList*), NULL);
                }
        } else {
                director->_eventQueue->put(director->_eventQueue, newEvent);
        }
}
struct Actor* PtidesDirector__GetNextActorToFire(struct PtidesDirector* director) {
        struct Actor* actor = director->_getNextActorFrom(director, director->_pureEvents);
        if (actor != NULL) {
                return actor;
        }
        actor = director->_getNextActorFrom(director, director->_eventQueue);
        if (actor != NULL) {
                return actor;
        }
        director->_currentLogicalTime = -DBL_MAX;
        return NULL;
}

struct Actor* PtidesDirector__GetNextActorFrom(struct PtidesDirector* director, struct CalendarQueue* queue) {
        struct PtidesEvent** eventArray = (struct PtidesEvent**)queue->toArray(queue);
        for (int foo = 0 ; foo < queue->size(queue) ; foo++) {
                struct PtidesEvent* ptidesEvent = eventArray[foo];
                if (director->_isSafeToProcess(director, ptidesEvent)) {
                        struct Actor* actor = ptidesEvent->actor(ptidesEvent);
                        Time timestamp = ptidesEvent->timeStamp(ptidesEvent);

                        if (queue == director->_pureEvents) {
                                struct CalendarQueue* queue2 = director->_pureEvents;
                                struct PtidesEvent** eventArray2 = (struct PtidesEvent**)queue2->toArray(queue2);
                                for (int bar = 0 ; bar < queue2->size(queue2) ; bar++) {
                                        struct PtidesEvent* triggeredEvent = eventArray2[bar];
                                        if (triggeredEvent->actor(triggeredEvent) == actor
                                                        && triggeredEvent->timeStamp(triggeredEvent) < timestamp) {
                                                ptidesEvent = triggeredEvent;
                                        }
                                }
                        }
                        actor = ptidesEvent->actor(ptidesEvent);
                        timestamp = ptidesEvent->timeStamp(ptidesEvent);

                        PblList* sameTagEvents = pblListNewArrayList();
                        int i = 0;
                        while (i < queue->size(queue)) {
                                struct PtidesEvent* eventInQueue = eventArray[i];

                                if (eventInQueue->hasTheSameTagAs(eventInQueue, ptidesEvent)
                                                && eventInQueue->actor(eventInQueue) == actor) {
                                        pblListAdd(sameTagEvents, eventInQueue);
                                        if (eventInQueue->receiver(eventInQueue) != NULL) {
                                                struct Receiver* receiver = eventInQueue->receiver(eventInQueue);
                                                if (IS_PTIDESRECEIVER(receiver)) {
                                                        ((struct PtidesReceiver*)receiver)->putToReceiver((struct PtidesReceiver*)receiver,
                                                                        eventInQueue->token(eventInQueue));
                                                }
                                        }
                                }
                                i++;
                        }

                        director->_currentLogicalTime = timestamp;
                        director->_currentLogicalIndex = ptidesEvent->microstep(ptidesEvent);
                        director->_currentSourceTimestamp = ptidesEvent->sourceTimestamp(ptidesEvent);
                        bool prefire = actor->prefire(actor);
                        director->_currentLogicalTime = -DBL_MAX;

                        PblIterator* sameTagIterator = pblIteratorNew(sameTagEvents);
                        while (pblIteratorHasNext(sameTagIterator)) {
                                struct PtidesEvent* sameTagEvent = pblIteratorNext(sameTagIterator);
                                if (sameTagEvent->receiver(sameTagEvent) != NULL) {
                                        struct Receiver* receiver = sameTagEvent->receiver(sameTagEvent);
                                        if (IS_PTIDESRECEIVER(receiver)) {
                                                struct PtidesReceiver* pReceiver = (struct PtidesReceiver*) receiver;
                                                pReceiver->remove(pReceiver, sameTagEvent->token(sameTagEvent));
                                        }
                                }
                        }
                        pblIteratorFree(sameTagIterator);
                        if (prefire) {
                                director->_currentLogicalTime = timestamp;
                                director->_currentLogicalIndex = ptidesEvent->microstep(ptidesEvent);
                                director->_currentSourceTimestamp = ptidesEvent->sourceTimestamp(ptidesEvent);

                                // remove all events with same tag from all queues.
                                director->_removeEventsFromQueue(director, director->_eventQueue, ptidesEvent);
                                director->_removeEventsFromQueue(director, director->_pureEvents, ptidesEvent);
                                return actor;
                        }
                }
        }
        return NULL;
}

struct SuperdenseDependency* PtidesDirector__GetSuperdenseDependencyPair(struct PtidesDirector* director,
                struct IOPort* source, struct IOPort* destination) {
        struct SuperdenseDependency* result = NULL;
        if (pblMapContainsKey(director->_superdenseDependencyPair, &source, sizeof(struct IOPort*))) {
                PblMap* map = pblMapGet(director->_superdenseDependencyPair, &source, sizeof(struct IOPort*), NULL);
                if (pblMapContainsKey(map, &destination, sizeof(struct IOPort*))) {
                       result = (struct SuperdenseDependency*)pblMapGet(map, &destination, sizeof(struct IOPort*), NULL);
                }
        } else {
            struct SuperdenseDependency* result = calloc(1, sizeof(struct SuperdenseDependency));
            result->time = DBL_MAX;
            result->microstep = 0;
        }
        return result;
}

bool PtidesDirector__IsSafeToProcess(struct PtidesDirector* director, struct PtidesEvent* event) {
        struct PtidesEvent** eventArray = (struct PtidesEvent**)director->_eventQueue->toArray(director->_eventQueue);
        for (int foo = 0 ; foo < director->_eventQueue->size(director->_eventQueue) ; foo++) {
                struct PtidesEvent* ptidesEvent = eventArray[foo];
                if (event->timeStamp(event) > ptidesEvent->timeStamp(ptidesEvent)) {
                        break;
                }
                if (ptidesEvent->actor(ptidesEvent) != event->actor(event)
                                && ptidesEvent->ioPort(ptidesEvent) != NULL && event->ioPort(event) != NULL) {
                        struct SuperdenseDependency* minDelay = director->_getSuperdenseDependencyPair(
                                        director,
                                        ptidesEvent->ioPort(ptidesEvent),
                                        event->ioPort(event));
                        if (event->timeStamp(event) - ptidesEvent->timeStamp(ptidesEvent) >= minDelay->time) {
                                return false;
                        }
                }
        }

        double delayOffset = -DBL_MAX;
        Time eventTimestamp = event->timeStamp(event);
        struct IOPort* port = event->ioPort(event);

        if (port != NULL) {
                struct Actor* actor = port->container;
                for (int i = 0; i < pblListSize(actor->inputPortList(actor)); i++) {
                        struct PtidesPort* ioPort = pblListGet(actor->inputPortList(actor), i);
                        double ioPortDelayOffset = ioPort->delayOffset;
                        if (ioPortDelayOffset != -DBL_MAX
                                        && (delayOffset == -DBL_MAX || ioPortDelayOffset < delayOffset)) {
                                delayOffset = ioPortDelayOffset;
                        }
                }
        } else {
                if (event->_actor->delayOffset != -DBL_MAX) {
                        delayOffset = event->_actor->delayOffset;
                }

        }
        if (delayOffset == -DBL_MAX
                        || director->localClock->getLocalTime(director->localClock) >=
                                        eventTimestamp - delayOffset) {
                return true;
        }

        director->_setNextFireTime(director, eventTimestamp - delayOffset);
        return false;
}

PblList* PtidesDirector__RemoveEventsFromQueue(struct PtidesDirector* director,
                        struct CalendarQueue* queue,
            struct PtidesEvent* event) {
        PblList* eventList = pblListNewArrayList();
        int i = 0;
        struct PtidesEvent** queueArray = (struct PtidesEvent**) queue->toArray(queue);
        int queueSize = queue->size(queue);
        while (i < queueSize) {
            struct PtidesEvent* eventInQueue = queueArray[i];
            if (eventInQueue->hasTheSameTagAs(eventInQueue, event)
                    && eventInQueue->actor(eventInQueue) == event->actor(event)) {
                pblListAdd(eventList, eventInQueue);
                queue->remove(queue, eventInQueue);
                //continue;
            }
            i++;
        }
        return eventList;
    }

void PtidesDirector__SetNextFireTime(struct PtidesDirector* director, Time time) {
        if (director->_nextFireTime == -DBL_MAX || director->_nextFireTime > time) {
                director->_nextFireTime = time;
        }
}
