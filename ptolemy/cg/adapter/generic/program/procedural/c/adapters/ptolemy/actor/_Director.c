#include "_Director.h"

struct Director* Director_New() {
    struct Director* newDirector = calloc(1, sizeof(struct Director));
    if (newDirector == NULL) {
        fprintf(stderr, "Allocation error : Director_New\n");
        exit(-1);
    }
    Director_Init(newDirector);
    newDirector->free = Director_New_Free;

    return newDirector;
}
void Director_Init(struct Director* director) {
    director->typeDirector = DIRECTOR;

    director->localClock = LocalClock_New();
    director->_startTime = -DBL_MAX;
    director->_stopTime = -DBL_MAX;

#ifdef _debugging
    director->setName = Director_SetName;
    director->getName = Director_GetName;
#endif

    director->fire = Director_Fire;
    director->fireAt = Director_FireAt;
    director->fireContainerAt = Director_FireContainerAt;
    director->getEnvironmentTime = Director_GetEnvironmentTime;
    director->getGlobalTime = Director_GetGlobalTime;
    director->getModelStartTime = Director_GetModelStartTime;
    director->getModelStopTime = Director_GetModelStopTime;
    director->getModelTime = Director_GetModelTime;
    director->initialize = Director_Initialize;
    director->initialize1 = Director_Initialize1;
    director->isEmbedded = Director_IsEmbedded;
    director->iterate = Director_Iterate;
    director->postfire = Director_Postfire;
    director->prefire = Director_Prefire;
    director->preinitialize = Director_Preinitialize;
    director->preinitialize1 = Director_Preinitialize1;
    director->transferInputs = Director_TransferInputs;
    director->transferOutputs = Director_TransferOutputs;
    director->transferOutputs1 = Director_TransferOutputs1;
    director->wrapup = Director_Wrapup;
    director->isTopLevel = Director_IsTopLevel;
}

#ifdef _debugging
// To enable debugging, recompile the code with:
//    make -f *.mk "USER_CC_FLAGS=-D _debugging" run

char *Director_GetName(struct Director *director) {
    return strdup(director->_name);
}
void Director_SetName(struct Director *director, char * name) {
    director->_name = strdup(name);
}
#endif

void Director_New_Free(struct Director* director) {
    if (director) {
        LocalClock_New_Free(director->localClock);
        free(director);
    }
}

void Director_Fire(struct Director* director) {
    PblList* actorsList = director->container->_containedEntities;
    PblIterator* actors = pblIteratorNew(actorsList);

    while (pblIteratorHasNext(actors)) {
        struct Actor* actor = pblIteratorNext(actors);

        if (actor->iterate(actor, 1) == -1) {
            break;
        }
    }
    pblIteratorFree(actors);
}
Time Director_FireAt(struct Director* director, struct Actor* actor, Time time, int microstep) {
    return director->fireContainerAt(director, time, microstep);
}
Time Director_FireContainerAt(struct Director* director, Time time, int microstep) {
    struct Actor* container = (struct Actor*)director->container;
    struct LocalClock* localClock = director->localClock;
    if (container != NULL && !director->isTopLevel(director)) {
        struct Director* directorE = container->getExecutiveDirector(container);
        if (directorE != NULL) {
            Time environmentTime = localClock->getEnvironmentTimeForLocalTime(localClock, time);
            Time result = directorE->fireAt(directorE, container, environmentTime,
                                            microstep);
            if (result != environmentTime) {
                fprintf(stderr, "Timing incompatibility error: Director_FireContainerAt");
                exit(-1);
            }
            return localClock->getLocalTimeForEnvironmentTime(localClock, result);
        }
    }
    return localClock->getLocalTime(localClock);
}
Time Director_GetEnvironmentTime(struct Director* director) {
    if (director == NULL) {
        return -666.0;
    }

    struct Actor* container = (struct Actor*) director->container;
    struct LocalClock* localClock = director->localClock;
    if (container != NULL && container->container != NULL) {
        struct Director* executiveDirector = container->getExecutiveDirector(container);
        if (executiveDirector != NULL && !director->isTopLevel(director)) {
            return executiveDirector->getModelTime(executiveDirector);
        }
    }
    return localClock->getLocalTime(localClock);
}
Time Director_GetGlobalTime(struct Director* director) {
    struct CompositeActor* container = director->container;
    if (container == NULL) {
        return -DBL_MAX;
    }
    while (container->container != NULL) {
        container = container->container;
    }
    struct Director* topDirector = container->getDirector(container);
    return topDirector->getModelTime(topDirector);
}
Time Director_GetModelStartTime(struct Director* director) {
    if (director->_startTime == -DBL_MAX) {
        if (director->isEmbedded(director)) {
            struct Director* executiveDirector = director->container->getExecutiveDirector(director->container);
            if (executiveDirector != NULL && !director->isTopLevel(director)) {
                return executiveDirector->getModelTime(executiveDirector);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
    return director->_startTime;
}
Time Director_GetModelStopTime(struct Director* director) {
    if (director->_stopTime != -DBL_MAX) {
        return director->_stopTime;
    }
    return DBL_MAX;
}
Time Director_GetModelTime(struct Director* director) {
    return director->localClock->getLocalTime(director->localClock);
}
void Director_Initialize(struct Director* director) {
    director->localClock->resetLocalTime(director->localClock, director->getModelStartTime(director));
    director->localClock->start(director->localClock);

    struct CompositeActor* container = director->container;
    PblIterator* actors = pblIteratorNew(container->_containedEntities);

    while (pblIteratorHasNext(actors)) {
        struct Actor* actor = pblIteratorNext(actors);
        director->initialize1(director, actor);
    }
    pblIteratorFree(actors);
}
void Director_Initialize1(struct Director* director, struct Actor* actor) {
    actor->initialize(actor);
}
bool Director_IsEmbedded(struct Director* director) {
    return !director->isTopLevel(director);
}
int Director_Iterate(struct Director* director, int count) {
    int n = 0;

    while (n++ < count) {
        if (director->prefire(director)) {
            director->fire(director);

            if (!director->postfire(director)) {
                return -1;
            }
        } else {
            return -1;
        }
    }

    return 0;
}
bool Director_Postfire(struct Director* director) {
    return true;
}
bool Director_Prefire(struct Director* director) {
    Time modifiedTime = director->localClock->getLocalTimeForCurrentEnvironmentTime(director->localClock);
    director->localClock->setLocalTime(director->localClock, modifiedTime);
    return true;
}
void Director_Preinitialize(struct Director* director) {
    director->localClock->initialize(director->localClock);
    struct CompositeActor* container = director->container;

    PblIterator* actors = pblIteratorNew(container->_containedEntities);
    while (pblIteratorHasNext(actors)) {
        struct Actor* actor = pblIteratorNext(actors);
        director->preinitialize1(director, actor);
    }
    pblIteratorFree(actors);
}
void Director_Preinitialize1(struct Director* director, struct Actor* actor) {
    actor->preinitialize(actor);
}
bool Director_TransferInputs(struct Director* director, struct IOPort* port) {
    if (!port->isInput(port) /*|| !port->isOpaque(port)*/) {
        fprintf(stderr, "Attempted to transferInputs on a port is not an opaque input port.");
        exit(-1);
    }
    bool wasTransferred = false;
    for (int i = 0; i < port->getWidth(port); i++) {
        if (i < port->getWidthInside(port)) {
            if (port->hasToken(port, i)) {
                Token* t = port->get(port, i);
                if (t == NULL) {
                    fprintf(stderr, "%s: %d: Null token port %p?\n", __FILE__, __LINE__, port);
                    return false;
                }
                port->sendInside(port, i, t);
                wasTransferred = true;
            }
        } else {
            if (port->hasToken(port, i)) {
                Token * t = port->get(port, i);
                if (t == NULL) {
                    fprintf(stderr, "%s: %d: Null token port %p?\n", __FILE__, __LINE__, port);
                    return false;
                }
            }
        }
    }
    return wasTransferred;
}
bool Director_TransferOutputs(struct Director* director) {
    struct CompositeActor* container = director->container;
    PblIterator* outports = pblIteratorNew(container->outputPortList(container));
    while (pblIteratorHasNext(outports)) {
        struct IOPort* port = pblIteratorNext(outports);
        director->transferOutputs1(director, port);
    }
    pblIteratorFree(outports);
    return true;
}
bool Director_TransferOutputs1(struct Director* director, struct IOPort* port) {
    bool result = false;
    if (!port->isOutput(port) /*|| !port->isOpaque(port)*/) {
        fprintf(stderr, "Attempted to transferOutputs on a port that is not an opaque input port.");
        exit(-1);
    }

    for (int i = 0; i < port->getWidthInside(port); i++) {
        if (port->hasTokenInside(port, i)) {
            Token* t = port->getInside(port, i);
            port->send(port, i, t);
            result = true;
        }
    }
    return result;
}
void Director_Wrapup(struct Director* director) {
    struct CompositeActor* container = director->container;
    PblIterator* actors = pblIteratorNew(container->_containedEntities);
    while (pblIteratorHasNext(actors)) {
        struct Actor* actor = pblIteratorNext(actors);
        actor->wrapup(actor);
    }
    pblIteratorFree(actors);
}
bool Director_IsTopLevel(struct Director* director) {
    struct CompositeActor* container = director->container;
    if (container->getExecutiveDirector(container) == NULL) {
        return true;
    } else {
        return false;
    }
}
