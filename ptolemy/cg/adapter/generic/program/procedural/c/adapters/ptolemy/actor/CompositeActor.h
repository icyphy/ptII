/* In this file we have defined a struct CompositeActor which
 * represent a composite actor, it is an actor plus a director
 * and a list of the contained actors
 *
 * @author : William Lucas
 */

#ifndef COMPOSITE_ACTOR_H_
#define COMPOSITE_ACTOR_H_

#include "$ModelName()_types.h"
#include "$ModelName()_Director.h"


struct CompositeActor {
	Actor actor;
	Director * director;
	Actor ** containedAtomicActors;
	CompositeActor ** containedCompositeActors;
	int nbAtomicActors;
	int nbCompositeActors;
	void (*constructorActorsFunction)(void);
	void (*constructorPortsFunction)(void);
	void (*constructorReceiversFunction)(void);
};

void CompositeActorSet(CompositeActor * actor, Director * director,
		int nbAtomicActors, int nbCompositeActors, void (*constructorActorsFunction)(void),
		void (*constructorPortsFunction)(void), void (*constructorReceiversFunction)(void));
void CompositeActorDelete(CompositeActor * a);

#endif
