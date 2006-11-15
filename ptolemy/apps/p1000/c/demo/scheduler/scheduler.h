#ifndef SCHEDULER_H_
#define SCHEDULER_H_

#include "type_defs.h"

/* Clock */

typedef struct CLOCK {
	DECLARE_SUPER_TYPE(ACTOR)
	
	TYPED_PORT fire_at;
	TYPED_PORT output;
	TIME time;
	TIME end_time;
	
	void (*fire)(struct CLOCK* clock);
	DECLARE_OVERRIDEN_METHOD(void, fire, ACTOR*);
} CLOCK;

void CLOCK_init(CLOCK* clock, void* actual_ref, SCHEDULER* scheduler,
	void* method_table);
void CLOCK_fire(CLOCK* clock);

/* Triggered Clock */

typedef struct TRIGGERED_CLOCK {
	DECLARE_SUPER_TYPE(CLOCK)
	
	TYPED_PORT trigger;
	TYPED_PORT output;
	TIME period;
	TIME phase;
	TIME start_time;
	
	void (*fire)(struct TRIGGERED_CLOCK* clock);
	DECLARE_OVERRIDEN_METHOD(void, fire, CLOCK*);
} TRIGGERED_CLOCK;

void TRIGGERED_CLOCK_init(TRIGGERED_CLOCK* triggered_clock, void* actual_ref,
	SCHEDULER* scheduler, void* method_table);
void TRIGGERED_CLOCK_initialize(TRIGGERED_CLOCK* triggered_clock);
void TRIGGERED_CLOCK_fire(TRIGGERED_CLOCK* triggered_clock);

/* Trigger Out */

typedef struct TRIGGER_OUT {
	DECLARE_SUPER_TYPE(ACTOR)
	
	TYPED_PORT input;
	TYPED_PORT output;

	void (*fire)(struct TRIGGER_OUT* clock);
	DECLARE_OVERRIDEN_METHOD(void, fire, CLOCK*);
} TRIGGER_OUT;

void TRIGGER_OUT_init(TRIGGER_OUT* trigger_out, void* actual_ref,
	SCHEDULER* scheduler, void* method_table);
void TRIGGER_OUT_initialize(TRIGGER_OUT* trigger_out);
void TRIGGER_OUT_fire(TRIGGER_OUT* trigger_out);

#endif /*SCHEDULER_H_*/
