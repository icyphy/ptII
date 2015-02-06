/* $Id$
 * Source: $PTII/ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/_PtidesPlatformDirector.c
 */

#include "_PtidesPlatformDirector.h"

struct PtidesPlatformDirector* PtidesPlatformDirector_New() {
    struct PtidesPlatformDirector* newDirector = calloc(1, sizeof(struct PtidesPlatformDirector));
    if (newDirector == NULL) {
        fprintf(stderr, "Allocation error : PtidesPlatformDirector_New\n");
        exit(-1);
    }
    PtidesPlatformDirector_Init(newDirector);
    newDirector->free = PtidesPlatformDirector_New_Free;

    return newDirector;
}
void PtidesPlatformDirector_Init(struct PtidesPlatformDirector* director) {
    Director_Init((struct Director*)director);

    director->typeDirector = PTIDESPLATFORMDIRECTOR;

    director->fire = PtidesPlatformDirector_Fire;
//        director->fireContainerAt = PtidesPlatformDirector_FireContainerAt;
    director->getEnvironmentTime = PtidesPlatformDirector_GetEnvironmentTime;
    director->postfire = PtidesPlatformDirector_Postfire;
    director->prefire = PtidesPlatformDirector_Prefire;
    director->transferInputs = PtidesPlatformDirector_TransferInputs;
    director->transferOutputs1 = PtidesPlatformDirector_TransferOutputs1;
    director->_getEmbeddedPtidesDirector = PtidesPlatformDirector__GetEmbeddedPtidesDirector;
}
void PtidesPlatformDirector_New_Free(struct PtidesPlatformDirector* director) {
    Director_New_Free((struct Director*) director);
}

void PtidesPlatformDirector_Fire(struct PtidesPlatformDirector* director) {
    struct Director* ptidesDirector = director->_getEmbeddedPtidesDirector(director);
    ptidesDirector->fire(ptidesDirector);
}
Time PtidesPlatformDirector_FireContainerAt(struct PtidesPlatformDirector* director, Time time, int microstep) {
    struct Actor* container = (struct Actor*) director->container;
#ifdef _debugging
    fprintf(stderr, "%s:%d: PtidesPlatformDirector_FireContainerAt(%p) %s start\n", __FILE__, __LINE__, director, ((struct Director *) director)->_name);
#endif
    if (container != NULL && container->container != NULL) {
        container = (struct Actor*) container->container;
        struct Director* pDirector = container->getDirector(container);
        if (pDirector != NULL) {
            return pDirector->fireContainerAt(pDirector, time, microstep);
        }
    }
    return -DBL_MAX;
}
Time PtidesPlatformDirector_GetEnvironmentTime(struct PtidesPlatformDirector* director) {
    struct Actor* container = (struct Actor*) director->container;
    if (container != NULL && container->container != NULL) {
        container = (struct Actor*) container->container;
        struct Director* director = container->getDirector(container);
        if (director != NULL) {
            return director->getModelTime(director);
        }
    }
    return -DBL_MAX;
}
bool PtidesPlatformDirector_Postfire(struct PtidesPlatformDirector* director) {
    struct Director* ptidesDirector = director->_getEmbeddedPtidesDirector(director);
    return ptidesDirector->postfire(ptidesDirector);
}
bool PtidesPlatformDirector_Prefire(struct PtidesPlatformDirector* director) {
    Director_Prefire((struct Director*) director);
    struct Director* ptidesDirector = director->_getEmbeddedPtidesDirector(director);
    return ptidesDirector->prefire(ptidesDirector);
}
bool PtidesPlatformDirector_TransferInputs(struct PtidesPlatformDirector* director, struct IOPort* port) {
    bool result = false;
    struct PtidesDirector* ptidesDirector = (struct PtidesDirector*) director->_getEmbeddedPtidesDirector(director);

#ifdef _debugging
    fprintf(stderr, "%s:%d: PtidesPlatformDirector_TransferInputs(%p) %s start\n", __FILE__, __LINE__, director, ((struct Director *) director)->_name);
#endif
    for (int channelIndex = 0; channelIndex < port->getWidth(port); channelIndex++) {
        if (port->hasToken(port, channelIndex)) {
            Token* t = port->get(port, channelIndex);

            struct PtidesPort* associatedPort = ((struct PtidesPort*) port)->_associatedPort;
            if (associatedPort->isNetworkReceiverPort(associatedPort)) {
                RecordToken r = t->payload.Record;
                Time recordTimestamp = r->timestamp;
                int recordMicrostep = r->microstep;
                Time sourceTimestamp = director->getModelTime(director);

                PblList* farReceivers = associatedPort->deepGetReceivers(associatedPort);
                if (pblListSize(farReceivers) >= channelIndex) {
                    PblList* receivers = pblListGet(farReceivers, channelIndex);
                    for (int i = 0; i < pblListSize(receivers); i++) {
                        struct PtidesEvent* newEvent = PtidesEvent_New();
                        newEvent->_ioPort = (struct IOPort*)associatedPort;
                        newEvent->_channel = channelIndex;
                        newEvent->_timestamp = recordTimestamp;
                        newEvent->_microstep = recordMicrostep;
                        newEvent->_depth = -1;
                        newEvent->_token = (r->payload);
                        newEvent->_receiver = pblListGet(receivers, i);
                        newEvent->_sourceTimestamp = sourceTimestamp;
                        ptidesDirector->addInputEvent(ptidesDirector, associatedPort,
                                                      newEvent, associatedPort->deviceDelay);
                    }
                }
            } else if (associatedPort->isSensorPort(associatedPort)) {
                PblList* farReceivers = associatedPort->deepGetReceivers(associatedPort);
                if (pblListSize(farReceivers) >= channelIndex) {
                    PblList* receivers = pblListGet(farReceivers, channelIndex);
                    for (int i = 0; i < pblListSize(receivers); i++) {
                        struct PtidesEvent* newEvent = PtidesEvent_New();
                        newEvent->_ioPort = (struct IOPort*)associatedPort;
                        newEvent->_channel = channelIndex;
                        newEvent->_timestamp = ptidesDirector->getModelTime(ptidesDirector);
                        newEvent->_microstep = 1;
                        newEvent->_depth = -1;
                        newEvent->_token = t;
                        newEvent->_receiver = pblListGet(receivers, i);
                        newEvent->_sourceTimestamp = ptidesDirector->getModelTime(ptidesDirector);
                        ptidesDirector->addInputEvent(ptidesDirector, associatedPort,
                                                      newEvent, associatedPort->deviceDelay);
                    }
                }
            } else {
                associatedPort->sendInside(associatedPort, channelIndex, t);
            }
            result = true;
        }
    }

#ifdef _debugging
    fprintf(stderr, "%s:%d: PtidesPlatformDirector_TransferInputs(%p) %s end\n", __FILE__, __LINE__, director, ((struct Director *) director)->_name);
#endif
    return result;
}
bool PtidesPlatformDirector_TransferOutputs1(struct PtidesPlatformDirector* director, struct PtidesPort* port) {
    bool result = false;
#ifdef _debugging
    fprintf(stderr, "%s:%d: PtidesPlatformDirector_TransferOutputs(%p) %s start\n", __FILE__, __LINE__, director, ((struct Director *) director)->_name);
#endif
    for (int i = 0; i < port->getWidthInside(port); i++) {
        while (port->hasTokenInside(port, i)) {
            Token* t = port->getInside(port, i);
            struct PtidesPort* associatedPort = port->_associatedPort;
            if (associatedPort->isNetworkTransmitterPort(associatedPort)) {
                struct PtidesDirector* ptidesDirector =
                    (struct PtidesDirector*) associatedPort->container->getDirector(associatedPort->container);


                Time timestamps[2];
                associatedPort->_getTimeStampForToken(associatedPort, t, timestamps);
                Time timestamp = timestamps[0];
                //Time sourceTimestamp = timestamps[1];
                Token* record = Record_new(timestamp, ptidesDirector->getMicrostep(ptidesDirector), t);
                port->send(port, i, record);
            } else {
                port->send(port, i, t);
            }
        }
        result = true;
    }
#ifdef _debugging
    fprintf(stderr, "%s:%d: PtidesPlatformDirector_TransferOutputs(%p) %s end\n", __FILE__, __LINE__, director, ((struct Director *) director)->_name);
#endif
    return result;
}
struct Director* PtidesPlatformDirector__GetEmbeddedPtidesDirector(struct PtidesPlatformDirector* director) {
    struct CompositeActor* container = director->container;

    PblIterator* actors = pblIteratorNew(container->_containedEntities);
    while (pblIteratorHasNext(actors)) {
        struct CompositeActor* actor = (struct CompositeActor*) pblIteratorNext(actors);
        pblIteratorFree(actors);
        return actor->getDirector(actor);
    }
    pblIteratorFree(actors);
    return NULL;
}
