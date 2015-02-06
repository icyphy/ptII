#include "_Receiver.h"

// Constructors of the basic receiver
struct Receiver* Receiver_New() {
    struct Receiver* newReceiver = calloc(1, sizeof(struct Receiver));
    if (newReceiver == NULL) {
        fprintf(stderr, "Allocation error : Receiver_New ($ModelName()__Receiver.c)\n");
        exit(-1);
    }
    Receiver_Init(newReceiver);
    newReceiver->free = Receiver_New_Free;

    return newReceiver;
}

// Initialisation method
void Receiver_Init(struct Receiver* r) {
    r->typeReceiver = RECEIVER;
    r->container = NULL;

    r->getModelTime = Receiver_GetModelTime;
    r->clear = NULL;
    r->elementList = NULL;
    r->get = NULL;
    r->hasRoom = NULL;
    r->hasRoom1 = NULL;
    r->hasToken = NULL;
    r->hasToken1 = NULL;
    r->put = NULL;
    r->putArray = Receiver_PutArray;
    r->putArrayToAll = Receiver_PutArrayToAll;
    r->putToAll = Receiver_PutToAll;
}

// Destructors
void Receiver_New_Free(struct Receiver* r) {
    if (r)
        free(r);
}

// Other methods
Time Receiver_GetModelTime(struct Receiver* r) {
    struct CompositeActor* containerActor = (struct CompositeActor*)(r->container->container);
    struct Director* containerDirector = (struct Director*)(containerActor->_director);
    return (*(containerDirector->getModelTime))(containerDirector);
}
void Receiver_PutArray(struct Receiver* r, Token** tokenArray, int numberOfTokens) {
    // If there is no container, then perform no conversion.
    if (r->container == NULL) {
        for (int i = 0; i < numberOfTokens; i++) {
            (*(r->put))(r, tokenArray[i]);
        }
    } else {
        for (int i = 0; i < numberOfTokens; i++) {
            (*(r->put))(r, tokenArray[i]);
        }
    }
}
void Receiver_PutArrayToAll(struct Receiver* r, Token** tokens,
                            int numberOfTokens, PblList* receivers) {

    PblIterator* receiversIterator = pblIteratorNew(receivers);
    while (pblIteratorHasNext(receiversIterator)) {
        struct Receiver* receiver = pblIteratorNext(receiversIterator);
        //struct IOPort* container = receiver->container;
        (*(receiver->putArray))(receiver, tokens, numberOfTokens);
    }
}
void Receiver_PutToAll(struct Receiver* r, Token* token, PblList* receivers) {
    PblIterator* receiversIterator = pblIteratorNew(receivers);
    while (pblIteratorHasNext(receiversIterator)) {
        struct Receiver* receiver = pblIteratorNext(receiversIterator);
        (*(receiver->put))(receiver, token);
    }
}
