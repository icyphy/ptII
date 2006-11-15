#ifndef SCHEDULER_H_
#define SCHEDULER_H_

#include "types.h"
#include "actor.h"
#include "port.h"

typedef struct SCHEDULER {
	DECLARE_SUPER_TYPE(GENERAL_TYPE)
	
	int fd;
	struct ACTOR* first_actor;
	struct ACTOR* last_actor;
	struct PORT* first_port;
	struct PORT* last_port;
} SCHEDULER;

void SCHEDULER_init(SCHEDULER* scheduler, void* actual_ref);
void SCHEDULER_register_port(SCHEDULER* scheduler, struct PORT* port);
void SCHEDULER_execute(SCHEDULER* scheduler);

#endif /*SCHEDULER_H_*/
