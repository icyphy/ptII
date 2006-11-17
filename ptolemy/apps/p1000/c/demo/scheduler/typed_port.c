#include "typed_port.h"
#include "event.h"

void TYPED_PORT_init(TYPED_PORT* typed_port, void* actual_ref,
	struct ACTOR* container)
{
	INIT_SUPER_TYPE(TYPED_PORT, PORT, typed_port, actual_ref, NULL, container);
	
	typed_port->first_event = typed_port->last_event = NULL;
}

void TYPED_PORT_send(TYPED_PORT* typed_port, const EVENT* event)
{
	TYPED_PORT* connected_port;

	connected_port = CAST(UPCAST(typed_port, PORT)->connected_port, TYPED_PORT);
	if (connected_port != NULL)
		PUSH_BACK(connected_port->first_event, connected_port->last_event,
			EVENT_duplicate(event));
}
