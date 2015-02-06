/* In this file we have defined a struct Actor which represent a generic actor
 *
 * @author : William Lucas
 */

#ifndef ACTOR_H_
#define ACTOR_H_

#include "_IOPort.h"
#include "_ptTypes.h"

#define ACTOR 0

struct Actor {
    int typeActor;

    struct CompositeActor* container;

    void (*free)(struct Actor*);

    void (*fire)(struct Actor*);
    struct Director* (*getDirector)(struct Actor*);
    struct Director* (*getExecutiveDirector)(struct Actor*);
    void (*initialize)(struct Actor*);
    int (*iterate)(struct Actor*, int);
    PblList* (*inputPortList)(struct Actor*);
    PblList* (*outputPortList)(struct Actor*);
    bool (*postfire)(struct Actor*);
    bool (*prefire)(struct Actor*);
    void (*preinitialize)(struct Actor*);
    void (*wrapup)(struct Actor*);

    // FIXME : temp solution for the decorators of Ptides domain
    double delayOffset;
    double _clockSynchronizationBound;
};

struct Actor* Actor_New();
void Actor_Init(struct Actor* actor);
void Actor_New_Free(struct Actor* actor);

#endif
