#include "_SDFDirector.h"

struct SDFDirector* SDFDirector_New() {
    struct SDFDirector* newDirector = calloc(1, sizeof(struct SDFDirector));
    if (newDirector == NULL) {
        fprintf(stderr, "%s, line: %d : Allocation error : SDFDirector_New\n", __FILE__, __LINE__);
        \
        ptExit(-1);
        \
    }
    SDFDirector_Init(newDirector);
    newDirector->free = SDFDirector_New_Free;

    return newDirector;
}
void SDFDirector_Init(struct SDFDirector* director) {
    Director_Init((struct Director*)director);

    director->fire = SDFDirector_Fire;
    director->initialize = SDFDirector_Initialize;
    director->postfire = SDFDirector_Postfire;
    director->prefire = SDFDirector_Prefire;
    director->transferInputs = SDFDirector_TransferInputs;
    director->transferOutputs1 = SDFDirector_TransferOutputs1;

    director->getIterations = SDFDirector_GetIterations;
    director->getModelNextIterationTime = SDFDirector_GetModelNextIterationTime;

    director->iterationCount = 0;
    director->iterations = 0;
    director->period = 0.0;
    director->_actorFinished = false;
    director->_prefire = false;
    director->_postfireReturns = false;
}
void SDFDirector_New_Free(struct SDFDirector* director) {
    Director_New_Free((struct Director*) director);
}

int SDFDirector_GetIterations(struct SDFDirector* director) {
    int iterationsValue = director->iterations;
    if (iterationsValue > 0) {
        return iterationsValue;
    }
    if (!(*(director->isEmbedded))((struct Director*)director)) {
        if (iterationsValue == -1) {
            return 1;
        }
    }
    return 0;
}

Time SDFDirector_GetModelNextIterationTime(struct SDFDirector* director) {
    if (!(*(director->isTopLevel))((struct Director*)director)) {
        fprintf(stderr, "%s, line: %d: TODO : add the GetModelNextIterationTime method !\n", __FILE__, __LINE__);
        ptExit(-1);
//                return super.getModelNextIterationTime();
    }
    double periodValue = director->period;

    if (periodValue > 0.0) {
        return (*(director->getModelTime))((struct Director*) director) + periodValue;
    } else {
        return (*(director->getModelTime))((struct Director*) director);
    }
}

void SDFDirector_Fire(struct SDFDirector* director) {
    director->_prefire = false;
    director->schedule();
}
void SDFDirector_Initialize(struct SDFDirector* director) {
    Director_Initialize((struct Director*) director);
    director->iterationCount = 0;

    struct CompositeActor* container = director->container;
    PblList* outputPortList = (*(container->outputPortList))(container);
    PblIterator* outputPorts = pblIteratorNew(outputPortList);

    while (pblIteratorHasNext(outputPorts)) {
        struct IOPort* port = (struct IOPort*) pblIteratorNext(outputPorts);

        // FIXME : Create a map with the init rates productions
//                int rate = DFUtilities.getTokenInitProduction(port);
        int rate = 0;

        for (int i = 0; i < (*(port->getWidthInside))(port); i++) {
            for (int k = 0; k < rate; k++) {
                if ((*(port->hasTokenInside))(port, i)) {
                    Token* t = (*(port->getInside))(port, i);
                    (*(port->send))(port, i, t);
                } else {
                    fprintf(stderr, "%s, line: %d: Port should produce 1 token, but there were only no tokens available.\n", __FILE__, __LINE__);
                    ptExit(-1);
                }
            }
        }
    }
    pblIteratorFree(outputPorts);
}
bool SDFDirector_Postfire(struct SDFDirector* director) {

    int iterationsValue = (*(director->getIterations))(director);
    director->iterationCount++;

    if (iterationsValue > 0 && director->iterationCount >= iterationsValue) {
        director->iterationCount = 0;
        return false;
    }

    bool result = Director_Postfire((struct Director*)director);

    return result && director->_postfireReturns;
}

bool SDFDirector_Prefire(struct SDFDirector* director) {
    director->_postfireReturns = true;
    director->_prefire = Director_Prefire((struct Director*)director);

    if (!director->_prefire) {
        return false;
    }

    //double periodValue = director->period;

    struct CompositeActor* container = director->container;
    PblList* inputPortList = (*(container->inputPortList))(container);
    PblIterator* inputPorts = pblIteratorNew(inputPortList);
    while (pblIteratorHasNext(inputPorts)) {
        struct IOPort* inputPort = (struct IOPort*) pblIteratorNext(inputPorts);

//                if (inputPort instanceof ParameterPort) {
//                        continue;
//                }

        // FIXME : idem que pour le postfire : créer une hashmap
//                int threshold = DFUtilities.getTokenConsumptionRate(inputPort);
        int threshold = 1;

        for (int channel = 0; channel < (*(inputPort->getWidth))(inputPort); channel++) {
            if (threshold > 0 && !(*(inputPort->hasToken1))(inputPort, channel, threshold)) {
                return false;
            }
        }
    }

    return true;
}
bool SDFDirector_TransferInputs(struct SDFDirector* director, struct IOPort* port) {
    if (!port->isInput(port) /*|| !port->isOpaque(port)*/) {
        fprintf(stderr, "%s, line: %d: Attempted to transferInputs on a port is not an opaque input port.\n", __FILE__, __LINE__);
        ptExit(-1);
    }
    //int rate = DFUtilities.getTokenConsumptionRate(port);
    int rate = 1;
    bool wasTransferred = false;

    for (int i = 0; i < (*(port->getWidth))(port); i++) {
        if (i < (*(port->getWidthInside))(port)) {
            for (int k = 0; k < rate; k++) {
                if ((*(port->hasToken))(port, i)) {
                    Token* t = (*(port->get))(port, i);
                    (*(port->sendInside))(port, i, t);
                    wasTransferred = true;
                } else {
                    fprintf(stderr, "%s, line: %d: SDFDirector_TransferInputs(): Port should consume 1 token, but there were only 0 tokens available.\n", __FILE__, __LINE__);
                    ptExit(-1);
                }
            }
        } else {
            if ((*(port->hasToken))(port, i)) {
                (*(port->get))(port, i);
            }
        }
    }

    return wasTransferred;
}
bool SDFDirector_TransferOutputs1(struct SDFDirector* director, struct IOPort* port) {
    if (!port->isOutput(port) /*|| !port->isOpaque(port)*/) {
        fprintf(stderr, "%s, line: %d: Attempted to transferOutputs on a port that is not an opaque input port.\n", __FILE__, __LINE__);
        ptExit(-1);
    }

    //int rate = DFUtilities.getTokenProductionRate(port);
    int rate = 1;
    bool wasTransferred = false;

    for (int i = 0; i < port->getWidthInside(port); i++) {
        for (int k = 0; k < rate; k++) {
            if ((*(port->hasTokenInside))(port, i)) {
                Token* t = (*(port->getInside))(port, i);
                (*(port->send))(port, i, t);
                wasTransferred = true;
            } else {
                fprintf(stderr, "%s, line: %d: Port should produce 1 token, but there were only 0 tokens available.\n", __FILE__, __LINE__);
                ptExit(-1);
            }
        }
    }

    return wasTransferred;
}
