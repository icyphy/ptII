#include "_Actor.h"

struct Actor* Actor_New() {
    struct Actor* newActor = calloc(1, sizeof(struct Actor));
    if (newActor == NULL) {
        fprintf(stderr, "Allocation error : Actor_New\n");
        exit(-1);
    }
    Actor_Init(newActor);
    newActor->free = Actor_New_Free;

    return newActor;
}
void Actor_Init(struct Actor* actor) {
    actor->typeActor = ACTOR;

    actor->fire = NULL;
    actor->getDirector = NULL;
    actor->getExecutiveDirector = NULL;
    actor->initialize = NULL;
    actor->iterate = NULL;
    actor->inputPortList = NULL;
    actor->outputPortList = NULL;
    actor->postfire = NULL;
    actor->prefire = NULL;
    actor->preinitialize = NULL;
    actor->wrapup = NULL;
}
void Actor_New_Free(struct Actor* actor) {
    if (actor)
        free(actor);
}
