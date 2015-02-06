/* C implementation of IOPort
 *
 * @author William Lucas, Christopher Brooks
 * @version $Id$
 * source: ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/_IOPort.c
 */
#include "_IOPort.h"

struct IOPort* IOPort_New() {
    struct IOPort* newIOPort = calloc(1, sizeof(struct IOPort));
    if (newIOPort == NULL) {
        fprintf(stderr, "%s, line: %d: Allocation error : IOPort_New\n", __FILE__, __LINE__);
        ptExit(-1);
    }
    IOPort_Init(newIOPort);
    newIOPort->free = IOPort_New_Free;

    return newIOPort;
}


void IOPort_Init(struct IOPort* port) {
    port->typePort = IOPORT;

    port->_isInsideConnected = false;
    port->_isOutsideConnected = false;
    port->_isInput = false;
    port->_isOutput = false;
    port->_isMultiport = false;
    port->_width = 0;
    port->_insideWidth = 0;
    port->_farReceivers = pblListNewArrayList();
    port->_localReceivers = pblListNewArrayList();
    port->_localInsideReceivers = pblListNewArrayList();
    port->_insideReceivers = pblListNewArrayList();
    port->_numberOfSinks = 0;
    port->_numberOfSources = 0;

    port->broadcast = IOPort_Broadcast;
    port->broadcast1 = IOPort_Broadcast1;
    port->deepGetReceivers = IOPort_DeepGetReceivers;
    port->get = IOPort_Get;
    port->get1 = IOPort_Get1;
    port->getChannelForReceiver = IOPort_GetChannelForReceiver;
    port->getInside = IOPort_GetInside;
    port->getInsideReceivers = IOPort_GetInsideReceivers;
    port->getModelTime = IOPort_GetModelTime;
    port->getReceivers = IOPort_GetReceivers;
    port->getRemoteReceivers = IOPort_GetRemoteReceivers;
    port->getWidth = IOPort_GetWidth;
    port->getWidthInside = IOPort_GetWidthInside;
    port->hasRoom = IOPort_HasRoom;
    port->hasRoomInside = IOPort_HasRoomInside;
    port->hasToken = IOPort_HasToken;
    port->hasToken1 = IOPort_HasToken1;
    port->hasTokenInside = IOPort_HasTokenInside;
    port->isInput = IOPort_IsInput;
    port->isMultiport = IOPort_IsMultiport;
    port->isOutput = IOPort_IsOutput;
    port->isOutsideConnected = IOPort_IsOutsideConnected;
    port->numberOfSinks = IOPort_NumberOfSinks;
    port->numberOfSources = IOPort_NumberOfSources;
    port->send = IOPort_Send;
    port->send1 = IOPort_Send1;
    port->sendInside = IOPort_SendInside;
    port->sendLocalInside = IOPort_SendLocalInside;

#ifdef _debugging
    port->_name = NULL;
    port->getFullName = IOPort_GetFullName;
    port->getName = IOPort_GetName;
    port->setName = IOPort_SetName;
#endif
}

void IOPort_New_Free(struct IOPort* port) {
    if (port) {
        pblListFree(port->_farReceivers);
        pblListFree(port->_localReceivers);
        pblListFree(port->_localInsideReceivers);
        pblListFree(port->_insideReceivers);
        free(port);
    }
}

void IOPort_Broadcast(struct IOPort* port, Token* token) {
    PblList* farReceivers;
    farReceivers = (*(port->getRemoteReceivers))(port);

    if (farReceivers == NULL) {
        return;
    }

    PblIterator* farReceiversIterator = pblIteratorNew(farReceivers);
    while (pblIteratorHasNext(farReceiversIterator)) {
        PblList* farReceiver = pblIteratorNext(farReceiversIterator);
        if (farReceiver == NULL) {
            continue;
        }

        if (pblListSize(farReceiver) > 0) {
            struct Receiver* receiver = pblListPeek(farReceiver);
            (*(receiver->putToAll))(receiver, token, farReceiver);
        }
    }
    pblIteratorFree(farReceiversIterator);
}
void IOPort_Broadcast1(struct IOPort* port, Token** tokenArray, int sizeTokenArray, int vectorLength) {
    PblList* farReceivers;
    farReceivers = (*(port->getRemoteReceivers))(port);

    if (farReceivers == NULL) {
        return;
    }

    PblIterator* farReceiversIterator = pblIteratorNew(farReceivers);
    while (pblIteratorHasNext(farReceiversIterator)) {
        PblList* farReceiver = pblIteratorNext(farReceiversIterator);
        if (farReceiver == NULL) {
            continue;
        }

        if (pblListSize(farReceiver) > 0) {
            struct Receiver* receiver = pblListPeek(farReceiver);
            (*(receiver->putArrayToAll))(receiver, tokenArray, vectorLength, farReceiver);
        }
    }
    pblIteratorFree(farReceiversIterator);
}
PblList* IOPort_DeepGetReceivers(struct IOPort* port) {
    if (!(*(port->isInput))(port)) {
        return NULL;
    }

    int width = (*(port->getWidthInside))(port);

    if (width <= 0) {
        return NULL;
    }

    return port->_insideReceivers;
}
Token* IOPort_Get(struct IOPort* port, int channelIndex) {
    PblList* localReceivers;
    localReceivers = (*(port->getReceivers))(port);

    if (channelIndex >= pblListSize(localReceivers)) {
        if (!(*(port->isInput))(port)) {
            fprintf(stderr, "%s, line: %d: Port is not an input port : IOPort_Get\n", __FILE__, __LINE__);
            ptExit(-1);
        } else {
            fprintf(stderr, "%s, line: %d: Channel Index is out of range! : IOPort_Get\n", __FILE__, __LINE__);
            ptExit(-1);
        }
    }

    PblList* localReceiver = pblListGet(localReceivers, channelIndex);
    if (localReceiver == NULL) {
        fprintf(stderr, "%s, line: %d: No receiver at index:%d! : IOPort_Get\n", __FILE__, __LINE__, channelIndex);
        ptExit(-1);
    }

    Token* token = NULL;

    PblIterator* receiverIterator = pblIteratorNew(localReceiver);
    while (pblIteratorHasNext(receiverIterator)) {
        struct Receiver* receiver = pblIteratorNext(receiverIterator);
        Token* localToken = (*(receiver->get))(receiver);

        if (token == NULL) {
            token = localToken;
        }
    }
    pblIteratorFree(receiverIterator);

    if (token == NULL) {
        fprintf(stderr, "%s, line: %d: IOPort_Get(): No token to return ! : IOPort_Get\n", __FILE__, __LINE__);
        ptExit(-1);
    }

    return token;
}
Token** IOPort_Get1(struct IOPort* port, int channelIndex, int vectorLength) {
    PblList* localReceivers;
    localReceivers = (*(port->getReceivers))(port);

    if (channelIndex >= pblListSize(localReceivers)) {
        if (!(*(port->isInput))(port)) {
            fprintf(stderr, "%s, line: %d: Port is not an input port : IOPort_Get1\n", __FILE__, __LINE__);
            ptExit(-1);
        } else {
            fprintf(stderr, "%s, line: %d: Channel Index is out of range! : IOPort_Get1\n", __FILE__, __LINE__);
            ptExit(-1);
        }
    }
    PblList* localReceiver = pblListGet(localReceivers, channelIndex);
    if (localReceiver == NULL) {
        fprintf(stderr, "%s, line: %d: No receiver at index:%d! : IOPort_Get1\n", __FILE__, __LINE__, channelIndex);
        ptExit(-1);
    }
    struct Receiver* receiver = pblListPeek(localReceiver);

    Token** retArray = (*(receiver->getArray))(receiver, vectorLength);

    if (retArray == NULL) {
        fprintf(stderr, "%s, line: %d: No token array to return : IOPort_Get1\n", __FILE__, __LINE__);
        ptExit(-1);
    }

    PblIterator* receiverIterator = pblIteratorNew(localReceiver);
    pblIteratorNext(receiverIterator);

    while (pblIteratorHasNext(receiverIterator)) {
        struct Receiver* receiver = pblIteratorNext(receiverIterator);
        (*(receiver->getArray))(receiver, vectorLength);
    }
    pblIteratorFree(receiverIterator);

    return retArray;
}
int IOPort_GetChannelForReceiver(struct IOPort* port, struct Receiver* receiver) {
    PblList* receivers;
    if ((*(port->isInput))(port)) {
        receivers = (*(port->getReceivers))(port);
    } else {
        receivers = (*(port->getInsideReceivers))(port);
    }

    for (int channel = 0; channel < pblListSize(receivers); channel++) {
        if (pblListGet(receivers, channel) != NULL) {
            for (int copy = 0; copy < pblListSize(pblListGet(receivers, channel)); copy++) {
                if (pblListGet(pblListGet(receivers, channel), copy) == receiver) {
                    // pointer comparison is OK.
                    return channel;
                }
            }
        }
    }

    fprintf(stderr, "%s, line: %d: Attempt to get a channel for a receiver that is not \
                        related to this port : IOPort_GetChannelForReceiver\n", __FILE__, __LINE__);
    ptExit(-1);
    return -1;
}
Token* IOPort_GetInside(struct IOPort* port, int channelIndex) {
    PblList* localReceivers;
    localReceivers = (*(port->getInsideReceivers))(port);

    if (channelIndex >= pblListSize(localReceivers)) {
        if (!(*(port->isOutput))(port)) {
            fprintf(stderr, "%s, line: %d: Port is not an output port : IOPort_GetInside\n", __FILE__, __LINE__);
            ptExit(-1);
        } else {
            fprintf(stderr, "%s, line: %d: Channel Index is out of range! : IOPort_GetInside\n", __FILE__, __LINE__);
            ptExit(-1);
        }
    }

    PblList* localReceiver = pblListGet(localReceivers, channelIndex);
    if (localReceiver == NULL) {
        fprintf(stderr, "%s, line: %d: No receiver found at index %d : IOPort_GetInside\n", __FILE__, __LINE__, channelIndex);
        ptExit(-1);
    }

    Token* token = NULL;

    for (int j = 0; j < pblListSize(localReceiver); j++) {
        struct Receiver* receiver = pblListGet(localReceiver, j);
        Token* localToken = (*(receiver->get))(receiver);

        if (token == NULL) {
            token = localToken;
        }
        break;
    }

    if (token == NULL) {
        fprintf(stderr, "%s, line: %d: No token to return ! : IOPort_GetInside\n", __FILE__, __LINE__);
        ptExit(-1);
    }

    return token;
}
PblList* IOPort_GetInsideReceivers(struct IOPort* port) {
    if (!(*(port->isOutput))(port) /*|| !(*(port->isOpaque))(port)*/) {
        return NULL;
    }

    return port->_localInsideReceivers;
}
Time IOPort_GetModelTime(struct IOPort* port, int channelIndex) {
    PblList* localReceivers;
    localReceivers = (*(port->getReceivers))(port);

    if (pblListGet(localReceivers, channelIndex) == NULL) {
        fprintf(stderr, "%s, line: %d: No receiver found at index %d : IOPort_GetModelTime\n", __FILE__, __LINE__, channelIndex);
        ptExit(-1);
    }

    struct Receiver* receiver = pblListGet(pblListGet(localReceivers, channelIndex), 0);
    return (*(receiver->getModelTime))(receiver);
}
PblList* IOPort_GetReceivers(struct IOPort* port) {
    if (!(*(port->isInput))(port)) {
        return NULL;
    }

//        if ((*(port->isOpaque))(port)) {
    return port->_localReceivers;
//        } else {
//                return (*(port->deepGetReceivers))(port);
//        }
}
PblList* IOPort_GetRemoteReceivers(struct IOPort* port) {
    if (!(*(port->isOutput))(port)) {
        return NULL;
    }

    int width = (*(port->getWidth))(port);

    if (width <= 0) {
        return NULL;
    }

//        if ((*(port->isOpaque))(port)) {
    return port->_farReceivers;
//        }

    fprintf(stderr, "%s, line: %d: FIXME: transparent ports not accepted here : IOPort_GetRemoteReceivers\n", __FILE__, __LINE__);
    ptExit(-1);
}
int IOPort_GetWidth(struct IOPort* port) {
    return port->_width;
}
int IOPort_GetWidthInside(struct IOPort* port) {
    return port->_insideWidth;
}
bool IOPort_HasRoom(struct IOPort* port, int channelIndex) {
    boolean result = true;

    PblList* farReceivers = (*(port->getRemoteReceivers))(port);

    if (farReceivers == NULL || pblListGet(farReceivers, channelIndex) == NULL) {
        result = false;
    } else {
        for (int j = 0; j < pblListSize(pblListGet(farReceivers, channelIndex)); j++) {
            struct Receiver* receiver = pblListGet(pblListGet(farReceivers, channelIndex), j);
            if (!(*(receiver->hasRoom))(receiver)) {
                result = false;
                break;
            }
        }
    }

    return result;
}
bool IOPort_HasRoomInside(struct IOPort* port, int channelIndex) {
    boolean result = true;

    PblList* farReceivers = (*(port->getInsideReceivers))(port);

    if (farReceivers == NULL || pblListGet(farReceivers, channelIndex) == NULL) {
        result = false;
    } else {
        for (int j = 0; j < pblListSize(pblListGet(farReceivers, channelIndex)); j++) {
            struct Receiver* receiver = pblListGet(pblListGet(farReceivers, channelIndex), j);
            if (!(*(receiver->hasRoom))(receiver)) {
                result = false;
                break;
            }
        }
    }

    return result;
}
bool IOPort_HasToken(struct IOPort* port, int channelIndex) {
    PblList* receivers = (*(port->getReceivers))(port);
    boolean result = false;

    if (receivers != NULL && channelIndex >= pblListSize(receivers)) {
        if (!(*(port->isInput))(port)) {
            fprintf(stderr, "%s, line: %d: Port is not an input port : IOPort_HasToken\n", __FILE__, __LINE__);
            ptExit(-1);
        } else {
            fprintf(stderr, "%s, line: %d: Channel Index is out of range! : IOPort_HasToken\n", __FILE__, __LINE__);
            ptExit(-1);
        }
    }

    if (receivers != NULL && pblListGet(receivers, channelIndex) != NULL) {
        for (int j = 0; j < pblListSize(pblListGet(receivers, channelIndex)); j++) {
            struct Receiver* receiver = pblListGet(pblListGet(receivers, channelIndex), j);
            if ((*(receiver->hasToken))(receiver)) {
                result = true;
                break;
            }
        }
    }

    return result;
}
bool IOPort_HasToken1(struct IOPort* port, int channelIndex, int tokens) {
    PblList* receivers = (*(port->getReceivers))(port);
    boolean result = false;

    if (receivers != NULL && channelIndex >= pblListSize(receivers)) {
        if (!(*(port->isInput))(port)) {
            fprintf(stderr, "%s, line: %d: Port is not an input port : IOPort_HasToken1\n", __FILE__, __LINE__);
            ptExit(-1);
        } else {
            fprintf(stderr, "%s, line: %d: Channel Index is out of range! : IOPort_HasToken1\n", __FILE__, __LINE__);
            ptExit(-1);
        }
    }

    if (receivers != NULL && pblListGet(receivers, channelIndex) != NULL) {
        for (int j = 0; j < pblListSize(pblListGet(receivers, channelIndex)); j++) {
            struct Receiver* receiver = pblListGet(pblListGet(receivers, channelIndex), j);
            if ((*(receiver->hasToken1))(receiver, tokens)) {
                result = true;
                break;
            }
        }
    }

    return result;
}
bool IOPort_HasTokenInside(struct IOPort* port, int channelIndex) {
    PblList* receivers = (*(port->getInsideReceivers))(port);
    boolean result = false;

    if (receivers != NULL && channelIndex >= pblListSize(receivers)) {
        if (!(*(port->isInput))(port)) {
            fprintf(stderr, "%s, line: %d: Port is not an input port : IOPort_HasToken\n", __FILE__, __LINE__);
            ptExit(-1);
        } else {
            fprintf(stderr, "%s, line: %d: Channel Index is out of range! : IOPort_HasToken\n", __FILE__, __LINE__);
            ptExit(-1);
        }
    }

    if (receivers != NULL && pblListGet(receivers, channelIndex) != NULL) {
        for (int j = 0; j < pblListSize(pblListGet(receivers, channelIndex)); j++) {
            struct Receiver* receiver = pblListGet(pblListGet(receivers, channelIndex), j);
            if ((*(receiver->hasToken))(receiver)) {
                result = true;
                break;
            }
        }
    }

    return result;
}
bool IOPort_IsInput(struct IOPort* port) {
    return port->_isInput;
}
bool IOPort_IsMultiport(struct IOPort* port) {
    return port->_isMultiport;
}
bool IOPort_IsOutput(struct IOPort* port) {
    return port->_isOutput;
}
bool IOPort_IsOutsideConnected(struct IOPort* port) {
    return port->_isOutsideConnected;
}
int IOPort_NumberOfSinks(struct IOPort* port) {
    return port->_numberOfSinks;
}
int IOPort_NumberOfSources(struct IOPort* port) {
    return port->_numberOfSources;
}
void IOPort_Send(struct IOPort* port, int channelIndex, Token* token) {
    PblList* farReceivers = (*(port->getRemoteReceivers))(port);

    if (farReceivers == NULL || pblListSize(farReceivers) <= channelIndex
            || pblListGet(farReceivers, channelIndex) == NULL) {
        return;
    }
    if (pblListSize(pblListGet(farReceivers, channelIndex)) > 0) {
        struct Receiver* receiver = pblListGet(pblListGet(farReceivers, channelIndex), 0);
        (*(receiver->putToAll))(receiver, token, pblListGet(farReceivers, channelIndex));
    }
}
void IOPort_Send1(struct IOPort* port, int channelIndex, Token** tokenArray, int vectorLength) {
    PblList* farReceivers = (*(port->getRemoteReceivers))(port);

    if (farReceivers == NULL || pblListSize(farReceivers) <= channelIndex
            || pblListGet(farReceivers, channelIndex) == NULL) {
        return;
    }
    if (pblListSize(pblListGet(farReceivers, channelIndex)) > 0) {
        struct Receiver* receiver = pblListGet(pblListGet(farReceivers, channelIndex), 0);
        (*(receiver->putArrayToAll))(receiver, tokenArray, vectorLength, pblListGet(farReceivers, channelIndex));
    }
}
void IOPort_SendInside(struct IOPort* port, int channelIndex, Token* token) {
    PblList* farReceivers = (*(port->deepGetReceivers))(port);

    if (farReceivers == NULL || pblListSize(farReceivers) <= channelIndex
            || pblListGet(farReceivers, channelIndex) == NULL) {
        return;
    }
    if (pblListSize(pblListGet(farReceivers, channelIndex)) > 0) {
        struct Receiver* receiver = pblListGet(pblListGet(farReceivers, channelIndex), 0);
        (*(receiver->putToAll))(receiver, token, pblListGet(farReceivers, channelIndex));
    }
}

void IOPort_SendLocalInside(struct IOPort* port, int channelIndex, Token* token) {
    PblList* farReceivers = (*(port->getInsideReceivers))(port);

    if (farReceivers == NULL || pblListSize(farReceivers) <= channelIndex
            || pblListGet(farReceivers, channelIndex) == NULL) {
        return;
    }
    if (pblListSize(pblListGet(farReceivers, channelIndex)) > 0) {
        struct Receiver* receiver = pblListGet(pblListGet(farReceivers, channelIndex), 0);
        (*(receiver->putToAll))(receiver, token, pblListGet(farReceivers, channelIndex));
    }
}

#ifdef _debugging
// To enable debugging, recompile the code with:
//    make -f *.mk "USER_CC_FLAGS=-D _debugging" run

/* Return the full name of the IOPort.
 * The caller should free the results returned by this method.
 */
char *IOPort_GetFullName(struct IOPort *ioPort) {
    char *containerFullName = ioPort->container->getFullName(ioPort->container);
    char *results = malloc(strlen(containerFullName) + 1 /* For the dot */ + strlen(ioPort->_name) + 1 /* for the null */);
    strcpy(results, containerFullName);
    free(containerFullName);
    strcat(results, ".");
    strcat(results, ioPort->_name);
    return results;
}

/* Return the name of this composite.
 * The caller should free the results returned by this method.
 */
char *IOPort_GetName(struct IOPort *ioPort) {
    return strdup(ioPort->_name);
}
void IOPort_SetName(struct IOPort *ioPort, char * name) {
    ioPort->_name = strdup(name);
}
#endif // _debugging
