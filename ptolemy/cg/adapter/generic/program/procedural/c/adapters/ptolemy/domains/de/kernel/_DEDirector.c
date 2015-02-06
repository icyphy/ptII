#include "_DEDirector.h"

struct DEDirector* DEDirector_New() {
    struct DEDirector* newDirector = calloc(1, sizeof(struct DEDirector));
    if (newDirector == NULL) {
        fprintf(stderr, "Allocation error : DEDirector_New\n");
        exit(-1);
    }
    DEDirector_Init(newDirector);
    newDirector->free = DEDirector_New_Free;

    return newDirector;
}
void DEDirector_Init(struct DEDirector* director) {
    Director_Init((struct Director*)director);

    director->typeDirector = DEDIRECTOR;

    director->binCountFactor = 2;
    director->isCQAdaptive = true;
    director->minBinCount = 2;
    director->stopWhenQueueIsEmpty = true;

    director->_delegateFireAt = false;
    director->_exceedStopTime = false;
    director->_disabledActors = NULL;
    director->_eventQueue = NULL;
    director->_isInitializing = false;
    director->_microstep = 1;
    director->_noMoreActorsToFire = false;
    director->actorsDepths = pblMapNewHashMap();
    director->portsDepths = pblMapNewHashMap();

    director->fire = DEDirector_Fire;
    director->fireAt = DEDirector_FireAt;
    director->getMicrostep = DEDirector_GetMicrostep;
    director->getNextEventTime = DEDirector_GetNextEventTime;
    director->initialize = DEDirector_Initialize;
    director->postfire = DEDirector_Postfire;
    director->prefire = DEDirector_Prefire;
    director->preinitialize = DEDirector_Preinitialize;
    director->wrapup = DEDirector_Wrapup;
    director->_checkForNextEvent = DEDirector__CheckForNextEvent;
    director->_disableActor = DEDirector__DisableActor;
    director->_enqueueEvent = DEDirector__EnqueueEvent;
    director->_enqueueTriggerEvent = DEDirector__EnqueueTriggerEvent;
    director->_fire = DEDirector__Fire;
    director->_getNextActorToFire = DEDirector__GetNextActorToFire;
    director->_requestFiring = DEDirector__RequestFiring;
}
void DEDirector_New_Free(struct DEDirector* director) {
    if (director) {
        pblSetFree(director->_disabledActors);
        (*(director->_eventQueue->free))(director->_eventQueue);
    }
    Director_New_Free((struct Director*) director);
}

void DEDirector_Fire(struct DEDirector* director) {
    while (true) {
        int result = (*(director->_fire))(director);
        if (result == 1) {
            continue;
        } else if (result == -1) {
            return;
        }
        if (!(*(director->_checkForNextEvent))(director)) {
            break;
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
    if (stopTime != DBL_MAX && stopTime != Infinity) {
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
    struct Actor* actorToFire = director->_getNextActorToFire(director);

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

