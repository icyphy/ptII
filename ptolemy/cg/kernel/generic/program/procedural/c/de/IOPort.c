#include "IOPort.h"

// Create a new empty port
IOPort * newIOPort() {
	IOPort * p = NULL;
	if ((p = malloc(sizeof(IOPort))) == NULL)
			perror("Allocation Error (newIOPort)");
	p->name = NULL;
	p->type = NULL;
	p->eventsToSend = NULL;
	p->isInput = false;
	p->isMultiport = false;
	p->width = 0;

	return p;
}

// Create a new port with some parameters
IOPort * newIOPortWithParam(char* name, char* type,
		bool isInput, bool isMultiport, int width) {
	IOPort * p = NULL;
	if ((p = malloc(sizeof(IOPort))) == NULL)
		perror("Allocation Error (newIOPortWithParam)");

	p->name = name;
	p->type = type;
	p->eventsToSend = NULL;
	p->isInput = isInput;
	p->isMultiport = isMultiport;
	p->width = width;
	return p;
}

// delete properly a port
void IOPortDelete(IOPort * p) {
	if (p == NULL)
		return;

	if (p->name != NULL)
		free(p->name);
	if (p->type != NULL)
		free(p->type);
	// TODO : delete the events left ?
	if (p->eventsToSend != NULL)
		free(p->eventsToSend);

	free(p);
}



