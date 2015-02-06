/* In this file we have defined a struct FSMDirector which represent a FSM director
 *
 * @author : William Lucas
 */

#ifndef FSMDIRECTOR_H_
#define FSMDIRECTOR_H_

#include "_Director.h"
#include "_FSMActor.h"
#include "_FSMReceiver.h"

#define IS_FSMDIRECTOR(director) ((director)->typeDirector%10 == 4)

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

    // new members
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
