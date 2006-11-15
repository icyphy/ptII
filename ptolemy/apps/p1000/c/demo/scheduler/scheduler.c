#include "scheduler.h"
#include "int_token.h"
#include "typed_port.h"

void SCHEDULER_init(SCHEDULER* scheduler, void* actual_ref)
{
	INIT_SUPER_TYPE(SCHEDULER, GENERAL_TYPE, scheduler, actual_ref, NULL);
	
	scheduler->fd = 0;
	scheduler->first_actor = scheduler->last_actor = NULL;
	scheduler->first_port = scheduler->last_port = NULL;
}

void SCHEDULER_register_port(SCHEDULER* scheduler, PORT* port)
{
	PUSH_BACK(scheduler->first_port, scheduler->last_port, port);
}

void SCHEDULER_execute(SCHEDULER* scheduler)
{
	PORT* port;
	TYPED_PORT* typed_port;
	ACTOR* actor;
	
	while(1) {
		port = scheduler->first_port;
		while (port != NULL) {
			typed_port = CAST(port, TYPED_PORT);
			if (typed_port->first_event != NULL) {
				PUSH_BACK(scheduler->first_actor, scheduler->last_actor,
					port->container);
				port->has_token = 1;
			}
			port = port->next;
		}
		
		actor = scheduler->first_actor;
		while (actor != NULL) {
			INVOKE_VIRTUAL_METHOD(ACTOR, fire, actor);
			REMOVE_FIRST(scheduler->first_actor, scheduler->last_actor);
			actor = scheduler->first_actor;
		}
	}
}
