#include "_SDFReceiver.h"

// Constructors of the basic receiver
struct SDFReceiver* SDFReceiver_New() {
    struct SDFReceiver* newReceiver = calloc(1, sizeof(struct SDFReceiver));
    if (newReceiver == NULL) {
        fprintf(stderr, "Allocation error : SDFReceiver_New (_SDFReceiver.c)\n");
        exit(-1);
    }
    SDFReceiver_Init(newReceiver);
    newReceiver->free = SDFReceiver_New_Free;

    return newReceiver;
}

// Initialisation method
void SDFReceiver_Init(struct SDFReceiver* r) {
    Receiver_Init((struct Receiver*)r);
    r->typeReceiver = SDFRECEIVER;

    r->clear = SDFReceiver_Clear;
    r->elementList = SDFReceiver_ElementList;
    r->get = SDFReceiver_Get;
    r->hasRoom = SDFReceiver_HasRoom;
    r->hasRoom1 = SDFReceiver_HasRoom1;
    r->hasToken = SDFReceiver_HasToken;
    r->hasToken1 = SDFReceiver_HasToken1;
    r->put = SDFReceiver_Put;

    r->_queue = pblListNewLinkedList();
    r->_waitingTokens = 0;
}

// Destructors
void SDFReceiver_New_Free(struct SDFReceiver* r) {
    if (r) {
        pblListFree(r->_queue);
        free(r);
    }
}

// Other methods
void SDFReceiver_Clear(struct SDFReceiver* r) {
    pblListClear(r->_queue);
    r->_waitingTokens = 0;
}
PblList* SDFReceiver_ElementList(struct SDFReceiver* r) {
    return pblListClone(r->_queue);
}
Token* SDFReceiver_Get(struct SDFReceiver* r) {
    if (pblListIsEmpty(r->_queue)) {
        return NULL;
    }

    Token* retour = (Token*)(pblListPoll(r->_queue));
    //Token nonDynToken = *retour;
    //free(retour);
    return retour;
}
bool SDFReceiver_HasRoom(struct SDFReceiver* r) {
    return true;
}
bool SDFReceiver_HasRoom1(struct SDFReceiver* r, int numberOfTokens) {
    return true;
}
bool SDFReceiver_HasToken(struct SDFReceiver* r) {
    return !pblListIsEmpty(r->_queue);
}
bool SDFReceiver_HasToken1(struct SDFReceiver* r, int numberOfTokens) {
    return pblListSize(r->_queue) >= numberOfTokens;
}
void SDFReceiver_Put(struct SDFReceiver* r, Token* token) {
    // FIXME : it is not a relevant comparison
    if (token->type == -1) {
        return;
    }
    Token* dynToken = calloc(1, sizeof(Token));
    //MEMORY_FIX: Added this line to free memory
    free(dynToken);
    if (!dynToken) {
        fprintf(stderr, "Allocation Problem : DEReceiver_Put");
        exit(-1);
    }
    dynToken = convert(token, ((struct TypedIOPort*)r->container)->_type);
    pblListAdd(r->_queue, dynToken);
    //MEMORY_FIX: Added this line to free memory
    //free(dynToken);
}
