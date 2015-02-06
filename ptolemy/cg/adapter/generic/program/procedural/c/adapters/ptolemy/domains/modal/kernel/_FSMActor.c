#include "_FSMActor.h"

struct FSMActor* FSMActor_New() {
    struct FSMActor* newActor = calloc(1, sizeof(struct FSMActor));
    if (newActor == NULL) {
        fprintf(stderr, "Allocation error : FSMActor_New\n");
        exit(-1);
    }
    FSMActor_Init(newActor);
    newActor->free = FSMActor_New_Free;

    return newActor;
}
void FSMActor_Init(struct FSMActor* actor) {
    CompositeActor_Init((struct CompositeActor*)actor);

    actor->typeActor = FSMACTOR;

    actor->fire = FSMActor_Fire;
}
void FSMActor_New_Free(struct FSMActor* actor) {
    CompositeActor_New_Free((struct CompositeActor*)actor);
}

void FSMActor_Fire(struct FSMActor* actor) {
    actor->makeTransitions();
}

