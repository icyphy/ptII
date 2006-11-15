#include "typed_port.h"

void TYPED_PORT_init(TYPED_PORT* typed_port, void* actual_ref,
	struct ACTOR* container)
{
	INIT_SUPER_TYPE(TYPED_PORT, PORT, typed_port, actual_ref, NULL, container);
	
	typed_port->first_event = typed_port->last_event = NULL;
}

void TYPED_PORT_send(TYPED_PORT* typed_port, const EVENT* event)
{
	EVENT* heap_event;
	TYPED_PORT* connected_port;

	heap_event = (EVENT*) malloc(sizeof(EVENT));
	*heap_event = *event;
	
	connected_port = CAST(UPCAST(typed_port, PORT)->connected_port, TYPED_PORT);
	if (connected_port != NULL)
		PUSH_BACK(connected_port->first_event, connected_port->last_event,
			heap_event);
}
