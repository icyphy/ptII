#include "$ModelName()__Actor.h"

// Sets the actor with some parameters
void ActorSet(Actor * actor, CompositeActor * container, int nbPorts, int depth, int priority,
		void (*preinitializeFunction)(void), void (*initializeFunction)(void),
		boolean (*prefireFunction)(void), void (*fireFunction)(void),
		boolean (*postfireFunction)(void), void (*wrapupFunction)(void)) {

	if (actor == NULL) {
		perror("Trying to set a null actor !");
		exit(1);
	}

	actor->container = container;
	actor->depth = depth;
	actor->fireFunction = fireFunction;
	actor->initializeFunction = initializeFunction;
	actor->nbPorts = nbPorts;
	if ((actor->ports = calloc (nbPorts, sizeof(IOPort))) == NULL) {
		perror("Allocation problem (ActorSet)");
		exit(1);
	}
	actor->postfireFunction = postfireFunction;
	actor->prefireFunction = prefireFunction;
	actor->preinitializeFunction = preinitializeFunction;
	actor->priority = priority;
	actor->wrapupFunction = wrapupFunction;

	return;
}

// Delete properly an actor
void ActorDelete(Actor * a) {
	if (a == NULL)
		return;

	if (a->ports != NULL) {
		int i;
		for (i = 0 ; i < a->nbPorts ; i++)
			IOPortDelete(a->ports + i);
	}
	free (a->ports);

	return;
}

