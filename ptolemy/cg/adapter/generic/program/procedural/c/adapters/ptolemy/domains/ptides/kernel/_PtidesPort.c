// $PTII/ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/_PtidesPort.c
// $Id$
#include "_PtidesPort.h"
#include "_PtidesDirector.h"

struct PtidesPort* PtidesPort_New() {
    struct PtidesPort* newPtidesPort = calloc(1, sizeof(struct PtidesPort));
    if (newPtidesPort == NULL) {
        fprintf(stderr, "Allocation error : PtidesPort_New\n");
        exit(-1);
    }
    PtidesPort_Init(newPtidesPort);
    newPtidesPort->free = PtidesPort_New_Free;

    return newPtidesPort;
}
void PtidesPort_Init(struct PtidesPort* port) {
    TypedIOPort_Init((struct TypedIOPort*)port);

    port->typePort = PTIDESPORT;

    port->setAssociatedPort = PtidesPort_SetAssociatedPort;
    port->isActuatorPort = PtidesPort_IsActuatorPort;
    port->isSensorPort = PtidesPort_IsSensorPort;
    port->isNetworkReceiverPort = PtidesPort_IsNetworkReceiverPort;
    port->isNetworkTransmitterPort = PtidesPort_IsNetworkTransmitterPort;
    port->send = PtidesPort_Send;
    port->_getTimeStampForToken = PtidesPort__GetTimeStampForToken;
}
void PtidesPort_New_Free(struct PtidesPort* port) {
    TypedIOPort_New_Free((struct TypedIOPort*)port);
}

void PtidesPort_SetAssociatedPort(struct PtidesPort* port, struct PtidesPort* port1) {
    port->_associatedPort = port1;
    port1->_associatedPort = port;
}

void PtidesPort__GetTimeStampForToken(struct PtidesPort* port, Token* t, Time* times ) {
    //Time times[2];
    Time* result = (Time*)pblMapGet(port->_transmittedTokenTimestamps, t, sizeof(Token), NULL);
    times[0] = result[0];
    times[1] = result[1];
    int cpt = *((int*)pblMapGet(port->_transmittedTokenCnt, t, sizeof(Token), NULL));
    cpt--;
    pblMapPut(port->_transmittedTokenCnt, t, sizeof(Token), &cpt, sizeof(int), NULL);
    if (cpt == 0) {
        pblMapGet(port->_transmittedTokenTimestamps, t, sizeof(Token), NULL);
        pblMapGet(port->_transmittedTokenCnt, t, sizeof(Token), NULL);
    }
    //return times;
}
bool PtidesPort_IsActuatorPort(struct PtidesPort* port) {
    return port->isOutput(port) && !port->isNetworkPort;
}
bool PtidesPort_IsSensorPort(struct PtidesPort* port) {
    return port->isInput(port) && !port->isNetworkPort;
}
bool PtidesPort_IsNetworkReceiverPort(struct PtidesPort* port) {
    return port->isInput(port) && port->isNetworkPort;
}
bool PtidesPort_IsNetworkTransmitterPort(struct PtidesPort* port) {
    return port->isOutput(port) && port->isNetworkPort;
}
void PtidesPort_Send(struct PtidesPort* port, int channelIndex, Token* token) {
    struct CompositeActor* container = (struct CompositeActor*) port->container;
    struct PtidesDirector* director = (struct PtidesDirector*) container->getDirector(container);
    Time timestamp = director->getModelTime(director);
    Time sourceTimestamp = director->_currentSourceTimestamp;
    if (sourceTimestamp == -DBL_MAX) {
        sourceTimestamp = timestamp;
    }
    if (port->_transmittedTokenTimestamps == NULL) {
        port->_transmittedTokenTimestamps = pblMapNewHashMap();
        port->_transmittedTokenCnt = pblMapNewHashMap();
    }
    //Token* tokenPtr = calloc(1, sizeof(Token));
    //*tokenPtr = token;

    if (pblMapGet(port->_transmittedTokenTimestamps, token, sizeof(Token), NULL) == NULL) {
        // FIXME: Leak?
        int* xPtr = calloc(1, sizeof(int));
        *xPtr = 0;
        pblMapAdd(port->_transmittedTokenCnt, token, sizeof(Token), xPtr, sizeof(int));
    }
    //Time toPut[2] = {timestamp, sourceTimestamp};
    // FIXME: when to free this
    Time * toPut = calloc(2, sizeof(Time));
    toPut[0] = timestamp;
    toPut[1] = sourceTimestamp;
    pblMapAdd(port->_transmittedTokenTimestamps, token, sizeof(Token), toPut, sizeof(Time[2]));

    int cpt = *((int*)pblMapGet(port->_transmittedTokenCnt, token, sizeof(Token), NULL));
    cpt++;

    int *cptPtr = calloc(1, sizeof(int));
    *cptPtr = cpt;

    pblMapAdd(port->_transmittedTokenCnt, token, sizeof(Token), cptPtr, sizeof(int));
    IOPort_Send((struct IOPort*)port, channelIndex, token);
}
