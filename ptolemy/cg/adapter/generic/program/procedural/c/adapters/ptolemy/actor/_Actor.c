/* A C struct Actor which represent a generic actor.
 *
 * @author William Lucas, Christopher Brooks
 * @version $Id$
 * source: ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/_Actor.c
 */

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

#ifdef _debugging
    actor->getFullName = Actor_GetFullName;
    actor->getName = Actor_GetName;
    actor->setName = Actor_SetName;
#endif    

}
void Actor_New_Free(struct Actor* actor) {
    if (actor)
        free(actor);
}

#ifdef _debugging
// To enable debugging, recompile the code with:
//    make -f *.mk "USER_CC_FLAGS=-D _debugging" run

/* Return the full name of the actor
 * The caller should free the results returned by this method.
 */
char *Actor_GetFullName(struct Actor *actor) {
    char *containerFullName = actor->container->getFullName(actor->container);
    char *results = NULL;
    if (actor->_name != NULL) {
        results = malloc(strlen(containerFullName) + 1 /* For the dot */ + strlen(actor->_name) + 1 /* for the null */);
    } else {
        results = malloc(strlen(containerFullName) + 1 /* For the dot */ + strlen("UnnamedActor") + 1 /* for the null */);
    }
    strcpy(results, containerFullName);
    free(containerFullName);
    strcat(results, ".");
    if (actor->_name != NULL) {
        strcat(results, actor->_name);
    } else {
        strcat(results, "UnnamedActor");
    }
    return results;
}
char *Actor_GetName(struct Actor *actor) {
    return strdup(actor->_name);
}
void Actor_SetName(struct Actor *actor, char * name) {
    actor->_name = strdup(name);
}
#endif
