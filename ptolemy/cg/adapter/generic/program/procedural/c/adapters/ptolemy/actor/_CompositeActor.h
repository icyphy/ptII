/* In this file we have defined a struct CompositeActor which
 * represent a composite actor, it is an actor plus a director
 * and a list of the contained actors
 *
 * @author : William Lucas
 */

#ifndef COMPOSITE_ACTOR_H_
#define COMPOSITE_ACTOR_H_

#include "_ptTypes.h"
#include "_Director.h"

#define IS_COMPOSITEACTOR(a) ((a)->typeActor%10 == 2)
#define COMPOSITEACTOR 2

struct CompositeActor {
    int typeActor;

    struct CompositeActor* container;

    void (*free)(struct CompositeActor*);

    void (*fire)(struct CompositeActor*);
    struct Director* (*getDirector)(struct CompositeActor*);
    struct Director* (*getExecutiveDirector)(struct CompositeActor*);
    void (*initialize)(struct CompositeActor*);
    int (*iterate)(struct CompositeActor*, int);
    PblList* (*inputPortList)(struct CompositeActor*);
    PblList* (*outputPortList)(struct CompositeActor*);
    bool (*postfire)(struct CompositeActor*);
    bool (*prefire)(struct CompositeActor*);
    void (*preinitialize)(struct CompositeActor*);
    void (*wrapup)(struct CompositeActor*);

    // FIXME : temp solution for the decorators of Ptides domain
    double delayOffset;
    double _clockSynchronizationBound;

    // new members
    bool (*isOpaque)(struct CompositeActor*);
    void (*_transferPortParameterInputs)(struct CompositeActor*);

    struct Director* _director;
    PblList* _inputPorts;
    PblList* _outputPorts;
    PblList* _containedEntities;
};

struct CompositeActor* CompositeActor_New();
void CompositeActor_Init(struct CompositeActor* actor);
void CompositeActor_New_Free(struct CompositeActor* actor);

struct Director* CompositeActor_GetDirector(struct CompositeActor* actor);
struct Director* CompositeActor_GetExecutiveDirector(struct CompositeActor* actor);
PblList* CompositeActor_InputPortList(struct CompositeActor* actor);
PblList* CompositeActor_OutputPortList(struct CompositeActor* actor);
int CompositeActor_Iterate(struct CompositeActor* actor, int count);
bool CompositeActor_IsOpaque(struct CompositeActor* actor);
void CompositeActor__TransferPortParameterInputs(struct CompositeActor* actor);

#endif
