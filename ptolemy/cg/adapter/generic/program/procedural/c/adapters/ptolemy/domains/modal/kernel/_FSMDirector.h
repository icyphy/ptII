/* In this file we have defined a struct FSMDirector which represent a FSM director
 *
 * @author : William Lucas
 */

#ifndef FSMDIRECTOR_H_
#define FSMDIRECTOR_H_

#include "_IOPort.h"
#include "_Actor.h"
#include "_CompositeActor.h"
#include "_LocalClock.h"

#define FSMDIRECTOR 3

#define IS_FSMDIRECTOR(director) ((director)->typeDirector%10 == 3)

struct FSMDirector {
	int typeDirector;

	struct CompositeActor* container;

	struct LocalClock* localClock;
	Time _startTime;
	Time _stopTime;

	void (*free)(struct FSMDirector*);

	void (*fire)(struct FSMDirector*);
	Time (*fireAt)(struct Director*, struct Actor*, Time, int);
	Time (*fireContainerAt)(struct Director*, Time, int);
	Time (*getEnvironmentTime)(struct Director*);
	Time (*getGlobalTime)(struct Director*);
	Time (*getModelStartTime)(struct Director*);
	Time (*getModelStopTime)(struct Director*);
	Time (*getModelTime)(struct Director*);
	void (*initialize)(struct SDFDirector*);
	void (*initialize1)(struct Director*, struct Actor*);
	bool (*isEmbedded)(struct Director*);
	int (*iterate)(struct Director*, int);
	bool (*postfire)(struct FSMDirector*);
	bool (*prefire)(struct FSMDirector*);
	void (*preinitialize)(struct Director*);
	void (*preinitialize1)(struct Director*, struct Actor*);
	bool (*transferInputs)(struct FSMDirector*, struct IOPort*);
	bool (*transferOutputs)(struct Director*);
	bool (*transferOutputs1)(struct FSMDirector*, struct IOPort*);
	void (*wrapup)(struct Director*);
	bool (*isTopLevel)(struct Director*);

	// new members
	PblMap* _currentLocalReceiverMap;
	int _indexOffset;
	PblMap* _localReceiverMaps;
	struct FSMActor* _controller;

};

struct SDFDirector* SDFDirector_New();
void SDFDirector_Init(struct SDFDirector* director);
void SDFDirector_New_Free(struct SDFDirector* director);

int SDFDirector_GetIterations(struct SDFDirector* director);
Time SDFDirector_GetModelNextIterationTime(struct SDFDirector* director);
void SDFDirector_Fire(struct SDFDirector* director);
void SDFDirector_Initialize(struct SDFDirector* director);
bool SDFDirector_Postfire(struct SDFDirector* director);
bool SDFDirector_Prefire(struct SDFDirector* director);
bool SDFDirector_TransferInputs(struct SDFDirector* director, struct IOPort* port);
bool SDFDirector_TransferOutputs1(struct SDFDirector* director, struct IOPort* port);

#endif /* DEDIRECTOR_H_ */
