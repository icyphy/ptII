#include <stdlib.h>
#include "type_defs.h"

void GENERAL_TYPE_init(GENERAL_TYPE* general, void* actual_ref)
{
	*general = (GENERAL_TYPE) {
		GENERAL_TYPE_UID,	// type_uid
		actual_ref			// actual_ref
	};
}

GENERAL_TYPE* _upcast(GENERAL_TYPE* object_ref,
	TYPE_UID_TYPE type_uid)
{
	while (object_ref != NULL && object_ref->type_uid != type_uid)
		object_ref = SUPER(object_ref);
	return object_ref;
}

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

void TOKEN_init(TOKEN* token, void* actual_ref)
{
	INIT_SUPER_TYPE(TOKEN, GENERAL_TYPE, token, actual_ref);
	
	token->prev = token->next = NULL;
}

void INT_TOKEN_init(INT_TOKEN* int_token, void* actual_ref)
{
	INIT_SUPER_TYPE(INT_TOKEN, TOKEN, int_token, actual_ref);
	
	int_token->value = 0;
}

void SCHEDULER_init(SCHEDULER* scheduler, void* actual_ref)
{
	INIT_SUPER_TYPE(SCHEDULER, GENERAL_TYPE, scheduler, actual_ref);
	
	scheduler->fd = 0;
	scheduler->first_actor = scheduler->last_actor = NULL;
	scheduler->first_port = scheduler->last_port = NULL;
}

void SCHEDULER_register_port(SCHEDULER* scheduler, PORT* port)
{
	if (scheduler->last_port == NULL) {
		scheduler->first_port = scheduler->last_port = port;
		port->prev = port->next = NULL;
	} else
		PUSH_BACK(scheduler->last_port, port);
}

void SCHEDULER_execute(SCHEDULER* scheduler)
{
	int i = 0;
	int empty = 0;
	PORT* port;
	TYPED_PORT* typed_port;
	ACTOR* container;
	
	while(1) {
	//while(!empty) {
		empty = 1;
		
		port = scheduler->first_port;
		while (port != NULL) {
			typed_port = CAST(port, TYPED_PORT);
			if (typed_port->last_event != NULL) {
				container = port->container;
				if (scheduler->last_actor == NULL) {
					scheduler->first_actor = scheduler->last_actor = container;
					container->prev = container->next = NULL;
				} else
					PUSH_BACK(scheduler->last_actor, container);
				port->has_token = 1;
			}
			port = port->next;
		}
		/*std::vector<Actor*>::iterator actorsIter;
		for(portsIter = _ports.begin();
				portsIter != _ports.end();
				portsIter++) {
			Port* port = (Port*) *portsIter;
			if (!port->isQueueEmpty()) {
				empty = false;
				if (port->hasMinimalEvent()) {
					//FIXME: should check whether the actor is already in the list
					//(for actors with more than one input port).
					_actors.push_back(port->getContainer());
					port->enable();
				}
			}
		}*/
		
		/*for(actorsIter = _actors.begin();
				actorsIter != _actors.end();
				actorsIter++) {
			Actor* actor = (Actor*) *actorsIter;
			actor->fire();
		}*/
	}
}

void PORT_init(PORT* port, void* actual_ref, struct ACTOR* container)
{
	INIT_SUPER_TYPE(PORT, GENERAL_TYPE, port, actual_ref);
	
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

void TYPED_PORT_init(TYPED_PORT* typed_port, void* actual_ref,
	struct ACTOR* container)
{
	INIT_SUPER_TYPE(TYPED_PORT, PORT, typed_port, actual_ref, container);
	
	typed_port->first_event = typed_port->last_event = NULL;
}

void TYPED_PORT_send(TYPED_PORT* typed_port, const EVENT* event)
{
	EVENT* heap_event = (EVENT*) malloc(sizeof(EVENT));
	*heap_event = *event;
	if (typed_port->last_event == NULL) {
		typed_port->first_event = typed_port->last_event = heap_event;
		heap_event->prev = heap_event->next = NULL;
	} else
		PUSH_BACK(typed_port->last_event, heap_event);
}

void ACTOR_init(ACTOR* actor, void* actual_ref, SCHEDULER* scheduler,
	const void *method_table)
{
	INIT_SUPER_TYPE(ACTOR, GENERAL_TYPE, actor, actual_ref);
	
	actor->scheduler = scheduler;
	actor->prev = actor->next = NULL;
	
	if (method_table == NULL)
		actor->fire = ACTOR_fire;
	else
		COPY_METHOD_TABLE(ACTOR, actor, actor->fire, method_table);
}

void ACTOR_fire(ACTOR* actor)
{
	// Nothing to be done.
}
