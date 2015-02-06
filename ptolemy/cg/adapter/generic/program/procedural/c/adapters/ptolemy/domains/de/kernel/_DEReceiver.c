#include "_DEReceiver.h"

// Constructors of the basic receiver
struct DEReceiver* DEReceiver_New() {
    struct DEReceiver* newReceiver = calloc(1, sizeof(struct DEReceiver));
    if (newReceiver == NULL) {
        fprintf(stderr, "Allocation error : DEReceiver_New ($ModelName()__DEReceiver.c)\n");
        exit(-1);
    }
    DEReceiver_Init(newReceiver);
    newReceiver->free = DEReceiver_New_Free;

    return newReceiver;
}

// Initialisation method
void DEReceiver_Init(struct DEReceiver* r) {
    Receiver_Init((struct Receiver*)r);
    r->typeReceiver = DERECEIVER;

    r->clear = DEReceiver_Clear;
    r->elementList = DEReceiver_ElementList;
    r->get = DEReceiver_Get;
    r->hasRoom = DEReceiver_HasRoom;
    r->hasRoom1 = DEReceiver_HasRoom1;
    r->hasToken = DEReceiver_HasToken;
    r->hasToken1 = DEReceiver_HasToken1;
    r->put = DEReceiver_Put;

    r->_tokens = pblListNewLinkedList();
    r->_director = NULL;
}

// Destructors
void DEReceiver_New_Free(struct DEReceiver* r) {
    if (r) {
        pblListFree(r->_tokens);
        free(r);
    }
}

// Other methods
void DEReceiver_Clear(struct DEReceiver* r) {
    pblListClear(r->_tokens);
}
PblList* DEReceiver_ElementList(struct DEReceiver* r) {
    return pblListClone(r->_tokens);
}
Token* DEReceiver_Get(struct DEReceiver* r) {
    if (pblListIsEmpty(r->_tokens)) {
        fprintf(stderr, "No more Tokens in the DE Receiver \
                                : DEReceiver_Get ($ModelName()__DEReceiver.c)\n");
        exit(-1);
    }

    Token* retour = (Token*)(pblListPoll(r->_tokens));
    //Token nonDynToken = *retour;
    //free(retour);
    //return nonDynToken;
    return retour;
}
bool DEReceiver_HasRoom(struct DEReceiver* r) {
    return true;
}
bool DEReceiver_HasRoom1(struct DEReceiver* r, int numberOfTokens) {
    return true;
}
bool DEReceiver_HasToken(struct DEReceiver* r) {
    return !pblListIsEmpty(r->_tokens);
}
bool DEReceiver_HasToken1(struct DEReceiver* r, int numberOfTokens) {
    return pblListSize(r->_tokens) >= numberOfTokens;
}
void DEReceiver_Put(struct DEReceiver* r, Token* token) {
    // FIXME : it is not a relevant comparison
    if (token->type == -1) {
        return;
    }
    struct DEDirector* director = r->_director;
    // FIXME : quick patch to use the DEReceiver instead of the QueueReceiver
    if (director != NULL && IS_DEDIRECTOR(director))
        (*(director->_enqueueTriggerEvent))(director, r->container,
                                            (*(director->getModelTime))((struct Director*) director));
    Token* dynToken = calloc(1, sizeof(Token));
    if (!dynToken) {
        fprintf(stderr, "Allocation Problem : DEReceiver_Put");
        exit(-1);
    }
    dynToken = convert(token, ((struct TypedIOPort*)r->container)->_type);
    pblListAdd(r->_tokens, dynToken);
}
