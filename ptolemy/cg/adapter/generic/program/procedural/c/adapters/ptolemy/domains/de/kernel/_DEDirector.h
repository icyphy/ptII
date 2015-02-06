/* In this file we have defined a struct DEDirector which represent a DE director
 *
 * @author : William Lucas
 */

#ifndef DEDIRECTOR_H_
#define DEDIRECTOR_H_

#include "_IOPort.h"
#include "_CalendarQueue.h"
#include "_Actor.h"
#include "_CompositeActor.h"
#include "_LocalClock.h"
#include "_DEReceiver.h"

#define IS_DEDIRECTOR(director) ((director)->typeDirector%10 == 1)

struct DEDirector {
        int typeDirector;

        struct CompositeActor* container;

        struct LocalClock* localClock;
        Time _startTime;
        Time _stopTime;

#ifdef _debugging
    char * _name;
    char *(*getName)(struct DEDirector *);
    void (*setName)(struct DEDirector *, char *);
#endif    

        void (*free)(struct DEDirector*);

        void (*fire)(struct DEDirector*);
        Time (*fireAt)(struct DEDirector*, struct Actor*, Time, int);
        Time (*fireContainerAt)(struct Director*, Time, int);
        Time (*getEnvironmentTime)(struct Director*);
        Time (*getGlobalTime)(struct Director*);
        Time (*getModelStartTime)(struct Director*);
        Time (*getModelStopTime)(struct Director*);
        Time (*getModelTime)(struct Director*);
        void (*initialize)(struct DEDirector*);
        void (*initialize1)(struct Director*, struct Actor*);
        bool (*isEmbedded)(struct Director*);
        int (*iterate)(struct Director*, int);
        bool (*postfire)(struct DEDirector*);
        bool (*prefire)(struct DEDirector*);
        void (*preinitialize)(struct DEDirector*);
        void (*preinitialize1)(struct Director*, struct Actor*);
        bool (*transferInputs)(struct Director*, struct IOPort*);
        bool (*transferOutputs)(struct Director*);
        bool (*transferOutputs1)(struct Director*, struct IOPort*);
        void (*wrapup)(struct DEDirector*);
        bool (*isTopLevel)(struct Director*);

        // new members
        int binCountFactor;
        bool isCQAdaptive;
        int minBinCount;
        bool stopWhenQueueIsEmpty;

        int (*getMicrostep)(struct DEDirector*);
        Time (*getNextEventTime)(struct DEDirector*);
        void (*setIndex)(struct DEDirector*, int);
        bool (*_checkForNextEvent)(struct DEDirector*);
        void (*_disableActor)(struct DEDirector*, struct Actor*);
        void (*_enqueueEvent)(struct DEDirector*, struct Actor*, Time, int);
        void (*_enqueueTriggerEvent)(struct DEDirector*, struct IOPort*, Time);
        int (*_fire)(struct DEDirector*);
        int (*_getDepthOfActor)(struct DEDirector*, struct Actor*);
        int (*_getDepthOfPort)(struct DEDirector*, struct IOPort*);
        struct Actor* (*_getNextActorToFire)(struct DEDirector*);
        void (*_requestFiring)(struct DEDirector*);

        bool _delegateFireAt;
        PblSet* _disabledActors;
        bool _exceedStopTime;
        struct CalendarQueue* _eventQueue;
        bool _isInitializing;
        int _microstep;
        bool _noMoreActorsToFire;
        PblMap* actorsDepths;
        PblMap* portsDepths;
};

struct DEDirector* DEDirector_New();
void DEDirector_Init(struct DEDirector* director);
void DEDirector_New_Free(struct DEDirector* director);

void DEDirector_Fire(struct DEDirector* director);
Time DEDirector_FireAt(struct DEDirector* director, struct Actor* actor, Time time, int index);
int DEDirector_GetMicrostep(struct DEDirector* director);
Time DEDirector_GetNextEventTime(struct DEDirector* director);
void DEDirector_Initialize(struct DEDirector* director);
bool DEDirector_Postfire(struct DEDirector* director);
bool DEDirector_Prefire(struct DEDirector* director);
void DEDirector_Preinitialize(struct DEDirector* director);
void DEDirector_Wrapup(struct DEDirector* director);
bool DEDirector__CheckForNextEvent(struct DEDirector* director);
void DEDirector__DisableActor(struct DEDirector* director, struct Actor* actor);
void DEDirector__EnqueueEvent(struct DEDirector* director, struct Actor* actor, Time time, int defaultMicrostep);
void DEDirector__EnqueueTriggerEvent(struct DEDirector* director, struct IOPort* ioPort, Time time);
int DEDirector__Fire(struct DEDirector* director);
struct Actor* DEDirector__GetNextActorToFire(struct DEDirector* director);
void DEDirector__RequestFiring(struct DEDirector* director);

#endif /* DEDIRECTOR_H_ */
