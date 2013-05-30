/* In this file we have defined a struct DIRECTOR which represent a generic director
 *
 * @author : William Lucas
 */

#ifndef DIRECTOR_H_
#define DIRECTOR_H_

#include "$ModelName()_IOPort.h"
#include "$ModelName()_CalendarQueue.h"
#include "$ModelName()_Actor.h"
#include "$ModelName()_CompositeActor.h"


struct Director {
	Time startTime;
	Time stopTime;
	CompositeActor * containerActor;
	Actor * currentActor;
	Time currentModelTime;
	int currentMicrostep;
	bool isInitializing;
	void (*preinitializeFunction)(void);
	void (*initializeFunction)(void);
	boolean (*prefireFunction)(void);
	void (*fireFunction)(void);
	boolean (*postfireFunction)(void);
	void (*wrapupFunction)(void);
	void (*transferInputs)(void);
	void (*transferOutputs)(void);

	// FIXME : DE Specific
	void (*fireAtFunction)(Actor *, Time, int);
	CalendarQueue cqueue;
	bool noMoreActorToFire;
	bool stopWhenQueueIsEmpty;
	bool exceedStopTime;

	// FIXME : SDF Specific
	int iterations;
	int iterationsCount;

};


#endif /* DEDIRECTOR_H_ */
