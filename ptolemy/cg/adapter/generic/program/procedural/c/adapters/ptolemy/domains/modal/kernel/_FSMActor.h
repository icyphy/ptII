/* In this file we have defined a structFSMActor which
 * represent a composite actor, in a modal model
 * @author : William Lucas
 */

#ifndef FSMACTOR_H_
#define FSMACTOR_H_

#include "_CompositeActor.h"
#include "_FSMDirector.h"

#define IS_FSMACTOR(a) ((a)->typeActor%100 == 12)
#define FSMACTOR 12

struct FSMActor {
    int typeActor;

    struct CompositeActor* container;

    void (*free)(struct FSMActor*);

    void (*fire)(struct FSMActor*);
    struct Director* (*getDirector)(struct FSMActor*);
    struct Director* (*getExecutiveDirector)(struct FSMActor*);
    void (*initialize)(struct FSMActor*);
    int (*iterate)(struct FSMActor*, int);
    PblList* (*inputPortList)(struct FSMActor*);
    PblList* (*outputPortList)(struct FSMActor*);
    bool (*postfire)(struct FSMActor*);
    bool (*prefire)(struct FSMActor*);
    void (*preinitialize)(struct FSMActor*);
    void (*wrapup)(struct FSMActor*);

    // FIXME : temp solution for the decorators of Ptides domain
    double delayOffset;
    double _clockSynchronizationBound;

    bool (*isOpaque)(struct FSMActor*);
    void (*_transferPortParameterInputs)(struct FSMActor*);

    struct Director* _director;
    PblList* _inputPorts;
    PblList* _outputPorts;
    PblList* _containedEntities;

    void (*makeTransitions)();
};

struct FSMActor* FSMActor_New();
void FSMActor_Init(struct FSMActor* actor);
void FSMActor_New_Free(struct FSMActor* actor);

void FSMActor_Fire(struct FSMActor* actor);

#endif
