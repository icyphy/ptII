#include "port.h"

void DEPENDENCY_LINK_init(DEPENDENCY_LINK* link, struct PORT* port,
	double dependency)
{
	*link = (DEPENDENCY_LINK) {
		port,		// port
		dependency,	// dependency
		NULL,		// prev
		NULL		// next
	};
}

void PORT_init(PORT* port, void* actual_ref, struct ACTOR* container)
{
	INIT_SUPER_TYPE(PORT, GENERAL_TYPE, port, actual_ref, NULL);
	
	port->connected_port = NULL;
	port->container = container;
	port->has_token = 0;
	port->is_realtime = 0;
	port->last_link = NULL;
	port->prev = port->next = NULL;
}

void PORT_connect(PORT* port, PORT* to_port)
{
	port->connected_port = to_port;
}
