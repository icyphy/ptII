#include "$ModelName()__IOPort.h"

// Sets a port with some parameters
void IOPortSet(IOPort * port, Actor * containingActor, char * name, char * type,
		bool isInput, bool isOutput, bool isMultiport, int widthInside, int widthOutside) {
	port->containingActor = containingActor;
	port->name = name;
	port->type = type;
	port->isInput = isInput;
	port->isOutput = isOutput;
	port->isMultiport = isMultiport;
	port->width = widthInside;
	if ((port->farReceivers = calloc(widthOutside, sizeof(Receiver*))) == NULL) {
		perror("Allocation Error (IOPortSet)");
		exit(1);
	}

	if ((port->receivers = calloc(widthInside, sizeof(Receiver))) == NULL) {
		perror("Allocation Error (IOPortSet)");
		exit(1);
	}
	return;
}

// delete properly a port
void IOPortDelete(IOPort * p) {
	if (p == NULL)
		return;

	if (p->receivers != NULL) {
		int i;
		for (i = 0 ; i < p->width ; i++)
			ReceiverClear(p->receivers + i);
		free (p->receivers);
	}
	free(p);
}

