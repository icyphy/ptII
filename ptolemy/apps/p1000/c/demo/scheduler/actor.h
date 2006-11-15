#ifndef ACTOR_H_
#define ACTOR_H_

#include "types.h"

struct SCHEDULER;
typedef struct ACTOR {
	DECLARE_SUPER_TYPE(GENERAL_TYPE)
	
	struct SCHEDULER* scheduler;
	struct ACTOR* prev;
	struct ACTOR* next;
} ACTOR;

typedef struct ACTOR_METHOD_TABLE {
	void (*fire)(struct ACTOR* actor);
} ACTOR_METHOD_TABLE;

extern ACTOR_METHOD_TABLE ACTOR_method_table;

void ACTOR_init(ACTOR* actor, void* actual_ref, struct SCHEDULER* scheduler);
void ACTOR_fire(ACTOR* actor);

#endif /*ACTOR_H_*/
