/* In this file we have defined a struct CompositeActor which
 * represent a composite actor, it is an actor plus a director
 * and a list of the contained actors
 *
 * @author William Lucas, Christopher Brooks
 * @version $Id$
 * source: ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/actor/_CompositeActor.h
 */

#ifndef COMPOSITE_ACTOR_H_
#define COMPOSITE_ACTOR_H_

#include "_ptTypes.h"
#include "_Director.h"

#define IS_COMPOSITEACTOR(a) ((a)->typeActor%10 == 2)
#define COMPOSITEACTOR 2

// Note that the order of fields in this struct should closely match
// the order in other files such as _AtomicActor.h
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

    // Place the debugging code toward the end of the structure to try
    // to minimize changes in the struct when debugging.
#ifdef _debugging
    char * _name;
    char *(*getFullName)(struct CompositeActor *);
    char *(*getName)(struct CompositeActor *);
    void (*setName)(struct CompositeActor *, char *);
#endif    

    // _CompositeActor-specific fields.
    bool (*isOpaque)(struct CompositeActor*);
    void (*_transferPortParameterInputs)(struct CompositeActor*);

    struct Director* _director;
    PblList* _inputPorts;
    PblList* _outputPorts;
    PblList* _containedEntities;
};

#ifdef _debugging
char *CompositeActor_GetFullName(struct CompositeActor *director);
char *CompositeActor_GetName(struct CompositeActor *director);
void CompositeActor_SetName(struct CompositeActor *director, char * name);
#endif


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
