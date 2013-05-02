#include "DEEvent.h"

// Initialize an empty event
DEEvent * newDEEvent() {
	DEEvent * e = NULL;
	if ((e = malloc(sizeof(DEEvent))) == NULL)
		perror("Allocation Error (DEEventInitialize)");

	e->depth = 0;
	e->microstep = 0;
	e->priority = 0;
	e->timestamp = 0.0;

	e->actor = newActor();
	e->ioPort = newIOPort();

	e->actor = NULL;
	e->ioPort = NULL;

	return e;
}

// Initialize with parameters a DEEvent
DEEvent * newDEEventWithParam(Actor* actor, IOPort* ioPort,
		int depth, int microstep, int priority, Time timestamp) {
	DEEvent * e = NULL;
	if ((e = malloc(sizeof(DEEvent))) == NULL)
		perror("Allocation Error (DEEventInitialize)");

	e->depth = depth;
	e->microstep = microstep;
	e->priority = priority;
	e->timestamp = timestamp;

	e->actor = actor;
	e->ioPort = ioPort;
	return e;
}

// Delete properly an event
void DEEventDelete(DEEvent * e) {
	if (e == NULL)
		return;
	ActorDelete(e->actor);
	IOPortDelete(e->ioPort);
	free(e);
}

bool DEEventEquals (const struct DEEvent * e1, const struct DEEvent * e2) {
	if (e1 == NULL || e2 == NULL)
		return false;
	return (DEEventCompare(e1,e2) == 0 && e1->actor == e2->actor);
}

int DEEventCompare(const struct DEEvent * e1, const struct DEEvent * e2) {
	if (e1 == NULL || e2 == NULL)
		return -1;
	if (e1->timestamp > e2->timestamp) {
    return 1;
  } else if (e1->timestamp < e2->timestamp) {
    return -1;
  } else if (e1->microstep > e2->microstep) {
    return 1;
  } else if (e1->microstep < e2->microstep) {
    return -1;
  } else if (e1->depth > e2->depth) {
    return 1;
  } else if (e1->depth < e2->depth) {
    return -1;
  } else if (e1->priority > e2->priority) {
    return 1;
  } else if (e1->priority < e2->priority) {
    return -1;
  } else {
    return 0;
  }
}



