#include "actor.h"
#include "scheduler.h"

ACTOR_METHOD_TABLE ACTOR_method_table = {
	ACTOR_fire	// fire
};

void ACTOR_init(ACTOR* actor, void* actual_ref, SCHEDULER* scheduler)
{
	INIT_SUPER_TYPE(ACTOR, GENERAL_TYPE, actor, actual_ref,
		&ACTOR_method_table);
	
	actor->scheduler = scheduler;
	actor->prev = actor->next = NULL;
}

void ACTOR_fire(ACTOR* actor)
{
	// Nothing to be done.
}
