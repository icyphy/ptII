/* In this file we have defined a struct Actor which represent a generic actor
 *
 * @author : William Lucas
 */

#ifndef ATOMICACTOR_H_
#define ATOMICACTOR_H_

#include "_IOPort.h"
#include "_ptTypes.h"

#define ATOMICACTOR 1

struct AtomicActor {
    int typeActor;

    struct CompositeActor* container;

    void (*free)(struct AtomicActor*);

    void (*fire)(struct AtomicActor*);
    struct Director* (*getDirector)(struct AtomicActor*);
    struct Director* (*getExecutiveDirector)(struct AtomicActor*);
    void (*initialize)(struct AtomicActor*);
    int (*iterate)(struct AtomicActor*, int);
    PblList* (*inputPortList)(struct AtomicActor*);
    PblList* (*outputPortList)(struct AtomicActor*);
    bool (*postfire)(struct AtomicActor*);
    bool (*prefire)(struct AtomicActor*);
    void (*preinitialize)(struct AtomicActor*);
    void (*wrapup)(struct AtomicActor*);

    // FIXME : temp solution for the decorators of Ptides domain
    double delayOffset;
    double _clockSynchronizationBound;

    // new members
    PblList* _inputPorts;
    PblList* _outputPorts;

};

struct AtomicActor* AtomicActor_New();
void AtomicActor_Init(struct AtomicActor* actor);
void AtomicActor_New_Free(struct AtomicActor* actor);

struct Director* AtomicActor_GetDirector(struct AtomicActor* actor);
struct Director* AtomicActor_GetExecutiveDirector(struct AtomicActor* actor);
PblList* AtomicActor_InputPortList(struct AtomicActor* actor);
int AtomicActor_Iterate(struct AtomicActor* actor, int count);
PblList* AtomicActor_OutputPortList(struct AtomicActor* actor);

#endif
