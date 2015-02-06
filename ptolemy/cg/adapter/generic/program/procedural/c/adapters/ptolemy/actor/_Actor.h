/* A C struct Actor which represent a generic actor.
 *
 * @author William Lucas, Christopher Brooks
 * @version $Id$
 * source: ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/_Actor.h
 */

#ifndef ACTOR_H_
#define ACTOR_H_

#include "_IOPort.h"
#include "_ptTypes.h"

#define ACTOR 0

// Note that the order of fields in this struct should closely match
// the order in other files such as _AtomicActor.h, _CompositeActor.h
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

    // Place the debugging code toward the end of the structure to try
    // to minimize changes in the struct when debugging.
#ifdef _debugging
    char * _name;
    char *(*getFullName)(struct Actor *);
    char *(*getName)(struct Actor *);
    void (*setName)(struct Actor *, char *);
#endif    

};

struct Actor* Actor_New();
void Actor_Init(struct Actor* actor);
void Actor_New_Free(struct Actor* actor);

#ifdef _debugging
char *Actor_GetFullName(struct Actor *actor);
char *Actor_GetName(struct Actor *actor);
void Actor_SetName(struct Actor *actor, char * name);
#endif

#endif
