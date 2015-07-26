#include "_FSMReceiver.h"

// Constructors of the basic receiver
struct FSMReceiver* FSMReceiver_New() {
    struct FSMReceiver* newReceiver = calloc(1, sizeof(struct FSMReceiver));
    if (newReceiver == NULL) {
        fprintf(stderr, "Allocation error : FSMReceiver_New (_FSMReceiver.c)\n");
        exit(-1);
    }
    FSMReceiver_Init(newReceiver);
    newReceiver->free = FSMReceiver_New_Free;

    return newReceiver;
}

// Initialisation method
void FSMReceiver_Init(struct FSMReceiver* r) {
    Receiver_Init((struct Receiver*)r);
    r->typeReceiver = FSMRECEIVER;

    r->clear = FSMReceiver_Clear;
    r->elementList = FSMReceiver_ElementList;
    r->get = FSMReceiver_Get;
    r->hasRoom = FSMReceiver_HasRoom;
    r->hasRoom1 = FSMReceiver_HasRoom1;
    r->hasToken = FSMReceiver_HasToken;
    r->hasToken1 = FSMReceiver_HasToken1;
    r->put = FSMReceiver_Put;

    r->_token = NULL;
}

// Destructors
void FSMReceiver_New_Free(struct FSMReceiver* r) {
    if (r) {
        free(r);
    }
}

// Other methods
void FSMReceiver_Clear(struct FSMReceiver* r) {
        //MEMORY_FIX: Used to assign r->_token to NULL
    free(r->_token);
}
PblList* FSMReceiver_ElementList(struct FSMReceiver* r) {
    PblList* list = pblListNewArrayList();
    pblListAdd(list, &(r->_token));
    return list;
}
Token* FSMReceiver_Get(struct FSMReceiver* r) {
    if (r->_token->type == -1) {
        fprintf(stderr, "No Token in the FSM Receiver \
                                : FSMReceiver_Get (_FSMReceiver.c)\n");
        exit(-1);
    }

    // FIXME: A memory leak.
    Token* retour = calloc(1, sizeof(Token));
    retour->type = r->_token->type;
    retour->payload = r->_token->payload;

    r->_token->type = -1;
    return retour;
}
bool FSMReceiver_HasRoom(struct FSMReceiver* r) {
    return true;
}
bool FSMReceiver_HasRoom1(struct FSMReceiver* r, int numberOfTokens) {
    return numberOfTokens == 1;
}
bool FSMReceiver_HasToken(struct FSMReceiver* r) {
    if (r == NULL) {
        fprintf(stderr, "FSM_Receiver_HasToken: receiver was null?\n");
        return false;
    }
    if (r->_token == NULL) {
        fprintf(stderr, "FSM_Receiver_HasToken: receiver %p: token was null?\n", (void *)r);
        return false;
    }
    return r->_token->type != -1;
}
bool FSMReceiver_HasToken1(struct FSMReceiver* r, int numberOfTokens) {
    return numberOfTokens == 1 && r->_token->type != -1;
}
void FSMReceiver_Put(struct FSMReceiver* r, Token* token) {
    // FIXME : it is not a relevant comparison
    if (token->type == -1) {
        return;
    }
    r->_token = convert(token, ((struct TypedIOPort*)r->container)->_type);
}
