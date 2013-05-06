/* In this file we implement a simple DE director in C
 *
 * @author : William Lucas
 */

#ifndef DEDIRECTOR_H_
#define DEDIRECTOR_H_

#include "Actor.h"
#include "IOPort.h"
#include "DEEvent.h"
#include "CalendarQueue.h"

typedef struct DEDirector DEDirector;


struct DEDirector {
	Time startTime;
	Time stopTime;
	Actor ** actors;
	Actor * containerActor;
	Actor * currentActor;
	CalendarQueue cqueue;
	Time currentModelTime;
	int currentMicrostep;
	bool isInitializing;
	bool noMoreActorToFire;
	bool stopWhenQueueIsEmpty;
	bool exceedStopTime;
};

void DEDirectorPreinitialize();
void DEDirectorInitialize();
bool DEDirectorPrefire();
int DEDirectorFire(DEDirector * director);
void DEDirectorFireLoop();
bool DEDirectorPostfire();
void DEDirectorWrapup();

void DEDirectorFireAt(DEDirector * director, Actor * actor,
		Time time, int microstep);
Actor * DEDirectorGetNextActorToFire(DEDirector * director);

#endif /* DEDIRECTOR_H_ */
