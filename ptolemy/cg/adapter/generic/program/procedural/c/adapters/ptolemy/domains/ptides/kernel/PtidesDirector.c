/***variableDeclareBlock***/
Time _clockSynchronizationErrorBound;
Time _currentLogicalTime;
Time _currentSourceTimestamp;
int _currentLogicalIndex;

//HashMap<Time, List<PtidesEvent>> _inputEventQueue;
PblMap _inputEventQueue;

//Map<TypedIOPort, Set<TypedIOPort>> _inputPortsForPureEvent;
PblMap _inputPortsForPureEvent;

//Map<TypedIOPort, Double> _relativeDeadlineForPureEvent;
PblMap _relativeDeadlineForPureEvent;

//HashMap<Time, List<PtidesEvent>> _outputEventDeadlines;
PblMap _outputEventDeadlines;

//HashMap<PtidesPort, Queue<PtidesEvent>> _ptidesOutputPortEventQueue;
PblMap _ptidesOutputPortEventQueue;

CalendarQueue _pureEvents;
/**/

/***preinitBlock***/
//_eventQueue = new PtidesListEventQueue();
_inputEventQueue = pblMapNewHashMap();
_outputEventDeadlines = pblMapNewHashMap();
_ptidesOutputPortEventQueue = pblMapNewHashMap();
_nextFireTime = DBL_MAX;
//_pureEvents = new PtidesListEventQueue();
_currentLogicalTime = -DBL_MAX;

_inputPortsForPureEvent = pblMapNewHashMap();
_relativeDeadlineForPureEvent = pblMapNewHashMap();
/**/

/***prefireBlock($directorName)***/
$directorName.currentMicrostep = 1;
_nextFireTime = -DBL_MAX;
return true;
/**/

/***postfireBlock($directorName)**/
boolean result = true;
if ($directorName.currentModelTime >=  $directorName.stopTime)) {
    if (!CQueueIsEmpty(&($directorName.cqueue))
                || !(CQueueGet(&($directorName.cqueue))->timeStamp == $directorName.stopTime)) {
        result = false;
    }
}

PblSet * orderedKeySet = pblSetNewTreeSet();
// Sort the keys of _outputEventDeadlines map
PblIterator * iterator = pblMapIteratorNew(&_outputEventDeadlines);
while (pblIteratorHasNext(iterator) > 0) {
        PblMapEntry * entry = pblIteratorNext(iterator);
        pblSetAdd(orderedKeySet, pblMapEntryKey(entry));
}
pblIteratorFree(iterator);

// Retrieves the first element of the set if it is not empty
if (!pblSetIsEmpty(orderedKeySet)) {
        Time * firstOutputDeadline = pblSetHead(orderedKeySet);
        PblList * firstOutputEventDeadlineList = pblMapGet(&_outputEventDeadlines, firstOutputDeadline, sizeof(Time), NULL);

        // Now we need to iterate on the PtidesEvents of this list
        PblIterator * firstEvents = pblListIterator(firstOutputEventDeadlineList);
        while (pblIteratorHasNext(firstEvents) > 0) {
                PtidesEvent event = *(pblIteratorNext(firstEvents));

                if (event._ioPort._isActuatorPort &&
                                event.timestamp < $directorName.currentModelTime) {
                        printf("Missed Deadline at port %s", event._ioPort.name);
                        exit(-1);
                }
        }
        pblIteratorFree(firstEvents);
        $directorName_setNextFireTime(*firstOutputDeadline);
}

//... or from _inputEventQueue
orderedKeySet = pblSetNewTreeSet();
iterator = pblMapIteratorNew(&_inputEventQueue);
while (pblIteratorHasNext(iterator) > 0) {
        PblMapEntry * entry = pblIteratorNext(iterator);
        pblSetAdd(orderedKeySet, pblMapEntryKey(entry));
}
pblIteratorFree(iterator);
if (!pblSetIsEmpty(orderedKeySet)) {
        firstOutputDeadline = pblSetHead(orderedKeySet);
        $directorName_setNextFireTime(*firstOutputDeadline);
}


// ... or from ptides output port queue
iterator = pblMapIteratorNew(&_ptidesOutputPortEventQueue);
while (pblIteratorHasNext(iterator) > 0) {
        PblMapEntry * entry = pblIteratorNext(iterator);
        PblList * ptidesOutputPortList = pblMapEntryValue(entry);
        PtidesPort * port = pblMapEntryKey(entry);
        if (!pblListIsEmpty(ptidesOutputPortList)) {
                PtidesEvent * event = pblListPeek(ptidesOutputPortList);

                if (port->_isActuatorPort &&
                                event->_absoluteDeadline < $directorName.currentModelTime) {
                        printf("Missed Deadline at port %s", port->name);
                        exit(-1);
                }

                $directorName_setNextFireTime(event->_absoluteDeadline);
        }
}
// ... or could also have already been set in safeToProcess().

// If not null, request refiring.
if (_nextFireTime != -DBL_MAX) {
        $directorName_fireAt($directorName.container, _nextFireTime, 1);
}

return result;
/**/

/***getNextActorToFireBlock***/
Actor * actor = _getNextActorFrom(_pureEvents);
if (actor != NULL) {
        return actor;
}
actor = _getNextActorFrom(_eventQueue);
if (actor != NULL) {
        return actor;
}
_currentLogicalTime = -DBL_MAX;
return NULL;
/**/

/***getNextActorFromBlock***/
PblIterator * iterator = pblIteratorNew(queue);
while (pblIteratorHasNext(iterator) > 0) {
        PtidesEvent ptidesEvent = *(pblIteratorNext(iterator));
        if (_isSafeToProcess(ptidesEvent)) {
                Actor * actor = ptidesEvent.actor;
                Time timestamp = ptidesEvent.timestamp;

                // I am aware that this is a pointer comparison
                // But in this context this is exactly what we want
                if (queue == &_pureEvents) {
                        PblIterator * iteratorEventQueue = pblIteratorNew(&_eventQueue);
                        while (pblIteratorHasNext(iteratorEventQueue) > 0) {
                                PtidesEvent triggeredEvent = *(pblIteratorNext(iteratorEventQueue));
                                // Another pointer comparison : Okay because we do not copy actors
                                if (triggeredEvent.actor == actor
                                                && triggeredEvent.timestamp < timestamp) {
                                        // Here it's a clone copy (not a pointer copy)
                                        ptidesEvent = triggeredEvent;
                                }
                        }
                        pblIteratorFree(iteratorEventQueue);
                }
                actor = ptidesEvent.actor;
                timestamp = ptidesEvent.timeStamp;

                PblList * sameTagEvents = pblListNewArrayList();
                PblIterator * iteratorQueue = pblIteratorNew(queue);
                int i = 0;
                while (pblIteratorHasNext(iteratorQueue)) {
                        PtidesEvent eventInQueue = *(pblIteratorNext(iteratorQueue));
                        if (DEEventCompare(&eventInQueue, &ptidesEvent) == 0
                                        && eventInQueue.actor == actor) {
                                pblListAdd(sameTagEvents, &eventInQueue);
                                if (eventInQueue._receiver != NULL) {
                                        if (eventInQueue._receiver->typeReceiver == PTIDES_RECEIVER) {
                                                ptidesReceiverPutToReceiver(eventInQueue._receiver, eventInQueue._token);
                                        }
                                }
                        }
                }
                pblIteratorFree(iteratorQueue);

                _currentLogicalTime = timestamp;
                _currentLogicalIndex = ptidesEvent._microstep;
                _currentSourceTimestamp = ptidesEvent._sourceTimestamp;
                boolean prefire = (*(actor->prefireFunction))();
                _currentLogicalTime = -DBL_MAX;

                PblIterator * iteratorSameTagEvents = pblIteratorNew(sameTagEvents);
                while (pblIteratorHasNext(iteratorSameTagEvents)) {
                        PtidesEvent sameTagEvent = *(pblIteratorNext(iteratorSameTagEvent));
                        if (sameTagEvent._receiver != NULL) {
                                if (sameTagEvent._receiver->typeReceiver == PTIDES_RECEIVER) {
                                        ptidesReceiverRemove(sameTagEvent._receiver, sameTagEvent._token);
                                }
                        }
                }
                pblIteratorFree(iteratorSameTagEvents);

                if (prefire) {
                        _currentLogicalTime = timestamp;
                        _currentLogicalIndex = ptidesEvent._microstep;
                        _currentSourceTimestamp = ptidesEvent._sourceTimestamp;

                        // remove all events with same tag from all queues.
                        CQueueRemove(&_eventQueue, &ptidesEvent);
                        CQueueRemove(&_pureEvents, &ptidesEvent);
                        return actor;
                }
        }
}
pblIteratorFree(iterator);
return NULL;
/**/

/***isSafeToProcessBlock($DirectorName)***/
// Check if there are any events upstream that have to be
// processed before this one.
PblIterator * iteratorEventQueue = pblIteratorNew(&_eventQueue);
while (pblIteratorHasNext(iteratorEventQueue) > 0) {
        PtidesEvent ptidesEvent = *(pblIteratorNext(iteratorEventQueue));

        if (event._timeStamp > ptidesEvent._timeStamp) {
                break;
        }
        if (ptidesEvent._actor != event._actor
                        && ptidesEvent._ioPort() != NULL && event._ioPort != NULL) {
                SuperdenseDependency minDelay = _getSuperdenseDependencyPair(ptidesEvent._ioPort, event._ioPort);
                if (event._timeStamp() - ptidesEvent._timeStamp >= minDelay._timeValue) {
                        return false;
                }
        }
}
pblIteratorFree(iteratorEventQueue);

// FIXME: ThrottleAttributes ?

double delayOffset = -DBL_MAX;
Time eventTimestamp = event._timeStamp;
IOPort port = event._ioPort;

if (port != NULL) {
        Actor * actor = port._container;
        PblIterator * inputPortIterator = pblIteratorNew(actor->inputPortList);
        while (pblIteratorHasNext(inputPortIterator)) {
                IOPort ioPort = *(pblIteratorNext(inputPortIterator));
                double ioPortDelayOffset = ioPort.delayOffset.payload.Double;

                if (ioPortDelayOffset != -DBL_MAX
                                && (delayOffset == -DBL_MAX || ioPortDelayOffset < delayOffset)) {
                        delayOffset = ioPortDelayOffset;
                }
        }
} else {
        if (event.actor->delayOffset != -DBL_MAX)
                delayOffset = event.actor->delayOffset;
}
if (delayOffset == -DBL_MAX
                || $DirectorName.currentModelTime >= (eventTimestamp - delayOffset)) {
        return true;
}

_setNextFireTime(eventTimestamp - delayOffset);

return false;
/**/

/***setNextFireTimeBlock***/
if (_nextFireTime == -DBL_MAX) {
        _nextFireTime = time;
} else if (_nextFireTime > time) {
        _nextFireTime = time;
}
/**/
