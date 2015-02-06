#include "_PtidesReceiver.h"

// Constructors of the basic receiver
struct PtidesReceiver* PtidesReceiver_New() {
    struct PtidesReceiver* newReceiver = calloc(1, sizeof(struct PtidesReceiver));
    if (newReceiver == NULL) {
        fprintf(stderr, "Allocation error : PtidesReceiverr_New (_PtidesReceiver.c)\n");
        exit(-1);
    }
    PtidesReceiver_Init(newReceiver);
    newReceiver->free = PtidesReceiver_New_Free;

    return newReceiver;
}

// Initialisation method
void PtidesReceiver_Init(struct PtidesReceiver* r) {
    DEReceiver_Init((struct DEReceiver*)r);
    r->typeReceiver = PTIDESRECEIVER;

    r->put = PtidesReceiver_Put;
    r->putToReceiver = PtidesReceiver_PutToReceiver;
    r->remove = PtidesReceiver_Remove;
}

// Destructors
void PtidesReceiver_New_Free(struct PtidesReceiver* r) {
    if (r) {
        pblListFree(r->_tokens);
        free(r);
    }
}

// Other methods
void PtidesReceiver_Put(struct PtidesReceiver* r, Token* token) {
    // FIXME : it is not a relevant comparison
    if (token->type == -1) {
        return;
    }
    (*(r->_director->_enqueueTriggerEvent1))(r->_director, r->container, token, (struct Receiver*)r);
}
void PtidesReceiver_PutToReceiver(struct PtidesReceiver* r, Token* token) {
    // FIXME : it is not a relevant comparison
    if (token->type != -1) {
        /*                 Token* dynToken = calloc(1, sizeof(Token)); */
        /*                 if (!dynToken) { */
        /*                         fprintf(stderr, "Allocation error : PtidesReceiver_PutToReceiver"); */
        /*                         exit(-1); */
        /*                 } */
        /*                 *dynToken = token; */
        /*                 pblListAdd(r->_tokens, dynToken); */
        // FIXME: Maybe copy?
        pblListAdd(r->_tokens, token);

    }
}

void PtidesReceiver_Remove(struct PtidesReceiver* r, Token* token) {
    PblIterator* iterator = pblIteratorNew(r->_tokens);
    while (pblIteratorHasNext(iterator)) {
        Token* dynToken = pblIteratorNext(iterator);
        //Token* tokenPtr = malloc(sizeof(Token));
        //*tokenPtr = token;
        if (memcmp(dynToken, token, sizeof(Token)) == 0) {
            pblListRemoveElement(r->_tokens, dynToken);
            // If we free here then valgrind reports that we are reading from freed memory later.
            // The test is $PTII/bin/ptcg -generatorPackage ptolemy.cg.kernel.generic.program.procedural.c $PTII/ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/lib/test/auto/SensorActuatorModelDelayOnly.xml
            //free(dynToken);
            //free(tokenPtr);
            break;
            //} else {
            //free(tokenPtr);
        }
    }
    // FIXME: Free the iterator.
}
