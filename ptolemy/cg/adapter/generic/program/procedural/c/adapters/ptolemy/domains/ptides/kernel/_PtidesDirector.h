/* In this file we have defined a struct PtidesDirector which represent a Ptides director.
 *
 * @author William Lucas, Christopher Brooks
 * @version $Id$
 * source: ptolemy/cg/adapter/generic/program/procedural/c/adapters/ptolemy/domains/ptides/kernel/_PtidesDirector.h
 */

#ifndef PTIDESDIRECTOR_H_
#define PTIDESDIRECTOR_H_

#include "_DEDirector.h"
#include "_PtidesPort.h"
#include "_PtidesEvent.h"
#include "_PtidesReceiver.h"

#define IS_PTIDESDIRECTOR(d) ((d)->typeDirector%100 == 11)

// Note that the order of fields in this struct should closely match
// the order in other files such as _DEDirector.h, _Director.h,
// _FSMDirector.h, _SDFDirector.h, _PtidesPlatformDirector.h
struct PtidesDirector {
    int typeDirector;

    struct CompositeActor* container;

    struct LocalClock* localClock;
    Time _startTime;
    Time _stopTime;

    void (*free)(struct PtidesDirector*);

    void (*fire)(struct PtidesDirector*);
    Time (*fireAt)(struct PtidesDirector*, struct Actor*, Time, int);
    Time (*fireContainerAt)(struct PtidesDirector*, Time, int);
    Time (*getEnvironmentTime)(struct PtidesDirector*);
    Time (*getGlobalTime)(struct PtidesDirector*);
    Time (*getModelStartTime)(struct PtidesDirector*);
    Time (*getModelStopTime)(struct PtidesDirector*);
    Time (*getModelTime)(struct PtidesDirector*);
    void (*initialize)(struct PtidesDirector*);
    void (*initialize1)(struct PtidesDirector*, struct Actor*);
    bool (*isEmbedded)(struct PtidesDirector*);
    int (*iterate)(struct PtidesDirector*, int);
    bool (*postfire)(struct PtidesDirector*);
    bool (*prefire)(struct PtidesDirector*);
    void (*preinitialize)(struct PtidesDirector*);
    void (*preinitialize1)(struct PtidesDirector*, struct Actor*);
    bool (*transferInputs)(struct PtidesDirector*, struct IOPort*);
    bool (*transferOutputs)(struct PtidesDirector*);
    bool (*transferOutputs1)(struct PtidesDirector*, struct IOPort*);
    void (*wrapup)(struct PtidesDirector*);
    bool (*isTopLevel)(struct PtidesDirector*);

    // Place the debugging code toward the end of the structure to try
    // to minimize changes in the struct when debugging.
#ifdef _debugging
    char * _name;
    char *(*getFullName)(struct PtidesDirector *);
    char *(*getName)(struct PtidesDirector *);
    void (*setName)(struct PtidesDirector *, char *);
#endif    

    // _PtidesDirector-specific fields

    int binCountFactor;
    bool isCQAdaptive;
    int minBinCount;
    bool stopWhenQueueIsEmpty;

    int (*getMicrostep)(struct PtidesDirector*);
    Time (*getNextEventTime)(struct PtidesDirector*);
    void (*setIndex)(struct PtidesDirector*, int);
    bool (*_checkForNextEvent)(struct PtidesDirector*);
    void (*_disableActor)(struct PtidesDirector*, struct Actor*);
    void (*_enqueueEvent)(struct PtidesDirector*, struct Actor*, Time, int);
    void (*_enqueueTriggerEvent)(struct PtidesDirector*, struct IOPort*, Time);
    int (*_fire)(struct PtidesDirector*);
    int (*_getDepthOfActor)(struct PtidesDirector*, struct Actor*);
    int (*_getDepthOfPort)(struct PtidesDirector*, struct IOPort*);
    struct Actor* (*_getNextActorToFire)(struct PtidesDirector*);
    void (*_requestFiring)(struct PtidesDirector*);

    bool _delegateFireAt;
    PblSet* _disabledActors;
    bool _exceedStopTime;
    struct CalendarQueue* _eventQueue;
    bool _isInitializing;
    int _microstep;
    bool _noMoreActorsToFire;
    PblMap* actorsDepths;
    PblMap* portsDepths;

    // new members
    void (*addInputEvent)(struct PtidesDirector*, struct PtidesPort*, struct PtidesEvent*, double);
    Time (*getDeadline)(struct PtidesDirector*, struct Actor*, Time);

    Time _nextFireTime;
    PblMap* _superdenseDependencyPair;

    void (*_enqueueTriggerEvent1)(struct PtidesDirector*, struct IOPort*, Token*, struct Receiver*);
    struct Actor* (*_getNextActorFrom)(struct PtidesDirector*, struct CalendarQueue*);
    struct SuperdenseDependency* (*_getSuperdenseDependencyPair)(struct PtidesDirector*,
            struct IOPort*, struct IOPort*);
    bool (*_isSafeToProcess)(struct PtidesDirector*, struct PtidesEvent*);
    PblList* (*_removeEventsFromQueue)(struct PtidesDirector*, struct CalendarQueue*, struct PtidesEvent*);
    void (*_setNextFireTime)(struct PtidesDirector*, Time);

    Time clockSynchronizationErrorBound;
    Time _currentLogicalTime;
    Time _currentSourceTimestamp;
    int _currentLogicalIndex;
    PblMap* _inputEventQueue;
    PblMap* _outputEventDeadlines;
    PblMap* _ptidesOutputPortEventQueue;
    struct CalendarQueue* _pureEvents;
};

struct PtidesDirector* PtidesDirector_New();
void PtidesDirector_Init(struct PtidesDirector*);
void PtidesDirector_New_Free(struct PtidesDirector* r);

void PtidesDirector_AddInputEvent(struct PtidesDirector* director, struct PtidesPort* sourcePort,
                                  struct PtidesEvent* event, double deviceDelay);
void PtidesDirector_Fire(struct PtidesDirector* director);
Time PtidesDirector_FireAt(struct PtidesDirector* director, struct Actor* actor, Time time, int index);
Time PtidesDirector_GetModelTime(struct PtidesDirector* director);
int PtidesDirector_GetMicrostep(struct PtidesDirector* director);
bool PtidesDirector_Postfire(struct PtidesDirector* director);
bool PtidesDirector_Prefire(struct PtidesDirector* director) ;
void PtidesDirector_Preinitialize(struct PtidesDirector* director);
bool PtidesDirector__CheckForNextEvent(struct PtidesDirector* director);
void PtidesDirector__EnqueueTriggerEvent(struct PtidesDirector* director, struct IOPort* ioPort, Token* token,
        struct Receiver* receiver);
struct Actor* PtidesDirector__GetNextActorToFire(struct PtidesDirector* director);
struct Actor* PtidesDirector__GetNextActorFrom(struct PtidesDirector* director, struct CalendarQueue* queue);
struct SuperdenseDependency* PtidesDirector__GetSuperdenseDependencyPair(struct PtidesDirector* director,
        struct IOPort* source, struct IOPort* destination);
bool PtidesDirector__IsSafeToProcess(struct PtidesDirector* director, struct PtidesEvent* event);
PblList* PtidesDirector__RemoveEventsFromQueue(struct PtidesDirector* director,
        struct CalendarQueue* queue,
        struct PtidesEvent* event);
void PtidesDirector__SetNextFireTime(struct PtidesDirector* director, Time time);

struct SuperdenseDependency {
    Time time;
    int microstep;
};

#endif /* PTIDESDIRECTOR_H_ */
