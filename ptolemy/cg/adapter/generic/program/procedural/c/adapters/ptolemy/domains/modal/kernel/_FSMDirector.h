/* In this file we have defined a struct FSMDirector which represent a FSM director
 *
 * @author William Lucas, Christopher Brooks
 * @version $Id$
 * source: ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/modal/kernel/_FSMDirector.h
 */

#ifndef FSMDIRECTOR_H_
#define FSMDIRECTOR_H_

#include "_Director.h"
#include "_FSMActor.h"
#include "_FSMReceiver.h"

#define IS_FSMDIRECTOR(director) ((director)->typeDirector%10 == 4)

// Note that the order of fields in this struct should closely match
// the order in other files such as _DEDirector.h, _Director.h,
// _FSMDirector.h, SDFDirector.h
struct FSMDirector {
    int typeDirector;

    struct CompositeActor* container;

    struct LocalClock* localClock;
    Time _startTime;
    Time _stopTime;

    void (*free)(struct FSMDirector*);

    void (*fire)(struct FSMDirector*);
    Time (*fireAt)(struct FSMDirector*, struct Actor*, Time, int);
    Time (*fireContainerAt)(struct FSMDirector*, Time, int);
    Time (*getEnvironmentTime)(struct FSMDirector*);
    Time (*getGlobalTime)(struct FSMDirector*);
    Time (*getModelStartTime)(struct FSMDirector*);
    Time (*getModelStopTime)(struct FSMDirector*);
    Time (*getModelTime)(struct FSMDirector*);
    void (*initialize)(struct FSMDirector*);
    void (*initialize1)(struct FSMDirector*, struct Actor*);
    bool (*isEmbedded)(struct FSMDirector*);
    int (*iterate)(struct FSMDirector*, int);
    bool (*postfire)(struct FSMDirector*);
    bool (*prefire)(struct FSMDirector*);
    void (*preinitialize)(struct FSMDirector*);
    void (*preinitialize1)(struct FSMDirector*, struct Actor*);
    bool (*transferInputs)(struct FSMDirector*, struct IOPort*);
    bool (*transferOutputs)(struct FSMDirector*);
    bool (*transferOutputs1)(struct FSMDirector*, struct IOPort*);
    void (*wrapup)(struct FSMDirector*);
    bool (*isTopLevel)(struct FSMDirector*);

    // Place the debugging code toward the end of the structure to try
    // to minimize changes in the struct when debugging.
#ifdef _debugging
    char * _name;
    char *(*getFullName)(struct FSMDirector *);
    char *(*getName)(struct FSMDirector *);
    void (*setName)(struct FSMDirector *, char *);
#endif    

    // _FSMDirector-specific fields.
    
    //PblMap* _currentLocalReceiverMap;
    //int _indexOffset;
    //PblMap* _localReceiverMaps;
    struct FSMActor* _controller;

    void (*makeTransitions)(struct FSMDirector*);
    void (*transferModalInputs)(PblMap*);
    void (*transferModalOutputs)(PblMap*);
    bool (*directorTransferModalOutputs)(struct FSMDirector*);
    bool (*directorTransferModalOutputs1)(struct FSMDirector*, struct IOPort*);

};

struct FSMDirector* FSMDirector_New();
void FSMDirector_Init(struct FSMDirector* director);
void FSMDirector_New_Free(struct FSMDirector* director);

Time FSMDirector_GetModelNextIterationTime(struct FSMDirector* director);
void FSMDirector_Fire(struct FSMDirector* director);
Time FSMDirector_FireAt(struct FSMDirector* director, struct Actor* actor, Time time, int microstep);
void FSMDirector_Initialize(struct FSMDirector* director);
bool FSMDirector_Postfire(struct FSMDirector* director);
bool FSMDirector_Prefire(struct FSMDirector* director);
bool FSMDirector_TransferInputs(struct FSMDirector* director, struct IOPort* port);
bool FSMDirector_TransferOutputs1(struct FSMDirector* director, struct IOPort* port);
bool FSMDirector_DirectorTransferModalOutputs(struct FSMDirector* director);
bool FSMDirector_DirectorTransferModalOutputs1(struct FSMDirector* director, struct IOPort* port);

#endif /* FSMDIRECTOR_H_ */
