/* In this file we have defined a struct Actor which represent a generic actor
 *
 * @author : William Lucas
 */

#ifndef ACTOR_H_
#define ACTOR_H_

#include "$ModelName()__IOPort.h"
#include "$ModelName()_types.h"


struct Actor {
	IOPort * ports;
	CompositeActor * container;
	int nbPorts;
	int depth;
	int priority;
	void (*preinitializeFunction)(void);
	void (*initializeFunction)(void);
	boolean (*prefireFunction)(void);
	void (*fireFunction)(void);
	boolean (*postfireFunction)(void);
	void (*wrapupFunction)(void);
};

void ActorSet(Actor * actor, CompositeActor * container, int nbPorts, int depth, int priority,
		void (*preinitializeFunction)(void), void (*initializeFunction)(void),
		boolean (*prefireFunction)(void), void (*fireFunction)(void),
		boolean (*postfireFunction)(void), void (*wrapupFunction)(void));
void ActorDelete(Actor * a);

#endif
