#include "$ModelName()__CompositeActor.h"

// Sets the actor with some parameters
void CompositeActorSet(CompositeActor * actor, Director * director,
		int nbAtomicActors, int nbCompositeActors, void (*constructorActorsFunction)(void),
		void (*constructorPortsFunction)(void), void (*constructorReceiversFunction)(void)) {

	if (actor == NULL) {
		perror("Trying to set a null composite actor !");
		exit(1);
	}

	actor->director = director;
	actor->nbAtomicActors = nbAtomicActors;
	if ((actor->containedAtomicActors = calloc (nbAtomicActors, sizeof(Actor*))) == NULL) {
		perror("Allocation problem (CompositeActorSet)");
		exit(1);
	}
	actor->nbCompositeActors = nbCompositeActors;
	if ((actor->containedCompositeActors = calloc (nbCompositeActors, sizeof(CompositeActor*))) == NULL) {
		perror("Allocation problem (CompositeActorSet)");
		exit(1);
	}
	actor->constructorActorsFunction = constructorActorsFunction;
	actor->constructorPortsFunction = constructorPortsFunction;
	actor->constructorReceiversFunction = constructorReceiversFunction;

	return;
}

// Delete properly an actor
void CompositeActorDelete(CompositeActor * a) {
	if (a == NULL)
		return;

	int i;
	if (a->containedAtomicActors != NULL) {
		for (i = 0 ; i < a->nbAtomicActors ; i++)
			ActorDelete(a->containedAtomicActors[i]);
	}
	free (a->containedCompositeActors);
	if (a->containedCompositeActors != NULL) {
		for (i = 0 ; i < a->nbCompositeActors ; i++);
			//ActorDelete(a->containedCompositeActors[i]);
			//FIXME : Add compositeActorDelete
	}
	free (a->containedCompositeActors);

	ActorDelete(&(a->actor));
	return;
}

