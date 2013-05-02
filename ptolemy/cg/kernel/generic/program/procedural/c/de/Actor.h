/* In this file we have defined a struct Actor which represent a generic actor
 *
 * @author : William Lucas
 */

#ifndef ACTOR_H_
#define ACTOR_H_

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <errno.h>
#include <string.h>

#include "IOPort.h"

typedef struct IOPort IOPort;

struct Actor {
	char * name;
	IOPort ** inputPorts;
	IOPort ** outputPorts;
	int depth;
	int priority;
	void (*preInitializeFunction)(void);
	void (*initializeFunction)(void);
	bool (*prefireFunction)(void);
	void (*fireFunction)(void);
	void (*postfireFunction)(void);
};
typedef struct Actor Actor;

Actor * newActor();
Actor * newActorWithParam(char* name, IOPort ** inputPorts, IOPort ** outputPorts);
void ActorDelete(Actor * a);

#endif
