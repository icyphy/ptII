#include "Actor.h"

// Create a new empty actor
Actor * newActor() {
	Actor * a = NULL;
	if ((a = malloc(sizeof(Actor))) == NULL)
		perror("Allocation Error (newActor)");

	a->name = NULL;
	a->inputPorts = NULL;
	a->outputPorts = NULL;
	a->depth = 0;
	a->priority = 0;
	return a;
}

// Create a new actor with the given parameters
Actor * newActorWithParam(char* name, IOPort ** inputPorts, IOPort ** outputPorts) {
	Actor * a = NULL;
	if ((a = malloc(sizeof(Actor))) == NULL)
		perror("Allocation Error (newActorWithParam)");

	a->name = name;
	a->inputPorts = inputPorts;
	a->outputPorts = outputPorts;
	a->depth = 0;
	a->priority = 0;

	return a;
}

// Delete properly an actor
void ActorDelete(Actor * a) {
	if (a == NULL)
		return;
	// Not needed because no malloc was made on the name
	//if (a->name != NULL)
		//free(a->name);
	// FIXME: here free all the ports, not only the table !
	if (a->inputPorts != NULL)
		free(a->inputPorts);
	if (a->outputPorts != NULL)
		free(a->outputPorts);
	free(a);
	return;
}

