#include "_AtomicActor.h"

struct AtomicActor* AtomicActor_New() {
    struct AtomicActor* newActor = calloc(1, sizeof(struct AtomicActor));
    if (newActor == NULL) {
        fprintf(stderr, "Allocation error : AtomicActor_New\n");
        exit(-1);
    }
    AtomicActor_Init(newActor);
    newActor->free = AtomicActor_New_Free;

    return newActor;
}
void AtomicActor_Init(struct AtomicActor* actor) {
    Actor_Init((struct Actor*)actor);

    actor->_inputPorts = pblListNewArrayList();
    actor->_outputPorts = pblListNewArrayList();

    actor->getDirector = AtomicActor_GetDirector;
    actor->getExecutiveDirector = AtomicActor_GetExecutiveDirector;
    actor->inputPortList = AtomicActor_InputPortList;
    actor->iterate = AtomicActor_Iterate;
    actor->outputPortList = AtomicActor_OutputPortList;
}
void AtomicActor_New_Free(struct AtomicActor* actor) {
    if (actor) {
        pblListFree(actor->_inputPorts);
        pblListFree(actor->_outputPorts);
    }
    Actor_New_Free((struct Actor*)actor);
}

struct Director* AtomicActor_GetDirector(struct AtomicActor* actor) {
    struct CompositeActor* container = actor->container;

    return (*(container->getDirector))(container);
}
struct Director*  AtomicActor_GetExecutiveDirector(struct AtomicActor* actor) {
    return (*(actor->getDirector))(actor);
}
PblList* AtomicActor_InputPortList(struct AtomicActor* actor) {
    return actor->_inputPorts;
}
int AtomicActor_Iterate(struct AtomicActor* actor, int count) {
    int n = 0;

#ifdef _debugging
    fprintf(stderr, "The actor %s will be iterated\n", ((struct Actor *) actor)->getFullName((struct Actor *)actor));
#endif
    while (n++ < count) {
        if ((*(actor->prefire))(actor)) {
            (*(actor->fire))(actor);
            if (!(*(actor->postfire))(actor))
                return n;
        } else {
            return -1;
        }
    }
    return count;
}

PblList* AtomicActor_OutputPortList(struct AtomicActor* actor) {
    return actor->_outputPorts;
}

