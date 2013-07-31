/* In this file we have defined a struct PtidesDirector which represent a Ptides director
 *
 * @author : William Lucas
 */

#ifndef PTIDESDIRECTOR_H_
#define PTIDESDIRECTOR_H_

#include "_IOPort.h"
#include "_CalendarQueue.h"
#include "_Actor.h"
#include "_CompositeActor.h"
#include "_LocalClock.h"
#include "_PtidesReceiver.h"

#define PTIDESDIRECTOR 11

struct PtidesDirector {
	int typeDirector;

	struct CompositeActor* container;

	struct LocalClock* localClock;
	Time _startTime;
	Time _stopTime;

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

	// new members
	void (*addInputEvent)(struct PtidesPort*, struct PtidesEvent*, double);
	Time (*getCurrentSourceTimestamp)();
	Time (*getDeadline)(struct Actor* actor, Time timestamp);

	PblList* _inputPorts;
	PblMap* _inputPortGroups;
	Time _nextFireTime;
	PblMap* _superdenseDependencyPair;

	struct Actor* (*_getNextActorFrom)(struct DEEventQueue*);
	int (*_getNumberOfFutureEventsFrom)(struct Actor*);
	double (*_getRelativeDeadline)(struct TypedIOPort*);
	bool (*_isSafeToProcess)(struct PtidesEvent*);
	PblList* (*_removeEventsFromQueue)(struct DEEventQueue*, struct PtidesEvent*);
	void (*_setNextFireTime)(Time);

	Time clockSynchronizationErrorBound;
	Time _currentLogicalTime;
	Time _currentSourceTimestamp;
	int _currentLogicalIndex;
	PblMap* _inputEventQueue;
	PblMap* _outputEventDeadlines;
	PblMap* _ptidesOutputPortEventQueue;
	struct DEEventQueue* _pureEvents;
};

struct PtidesDirector* PtidesDirector_New();
void PtidesDirector_Init(struct PtidesDirector*);
void PtidesDirector_New_Free(struct PtidesDirector* r);

#endif /* PTIDESDIRECTOR_H_ */
