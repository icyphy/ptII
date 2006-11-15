#ifndef MAIN_H_
#define MAIN_H_

#include "types.h"
#include "actor.h"
#include "typed_port.h"
#include "scheduler.h"

/* Clock */

typedef struct CLOCK {
	DECLARE_SUPER_TYPE(ACTOR)
	
	TYPED_PORT fire_at;
	TYPED_PORT output;
	TIME time;
	TIME end_time;
} CLOCK;

typedef struct CLOCK_METHOD_TABLE {
	void (*fire)(struct CLOCK* clock);
} CLOCK_METHOD_TABLE;

extern CLOCK_METHOD_TABLE CLOCK_method_table;

void CLOCK_init(CLOCK* clock, void* actual_ref, SCHEDULER* scheduler);
void CLOCK_fire(CLOCK* clock);

/* Triggered Clock */

typedef struct TRIGGERED_CLOCK {
	DECLARE_SUPER_TYPE(CLOCK)
	
	TYPED_PORT trigger;
	TYPED_PORT output;
	TIME period;
	TIME phase;
	TIME start_time;
} TRIGGERED_CLOCK;

typedef struct TRIGGERED_CLOCK_METHOD_TABLE {
	void (*fire)(struct TRIGGERED_CLOCK* clock);
} TRIGGERED_CLOCK_METHOD_TABLE;

extern TRIGGERED_CLOCK_METHOD_TABLE TRIGGERED_CLOCK_method_table;

void TRIGGERED_CLOCK_init(TRIGGERED_CLOCK* triggered_clock, void* actual_ref,
	SCHEDULER* scheduler);
void TRIGGERED_CLOCK_initialize(TRIGGERED_CLOCK* triggered_clock);
void TRIGGERED_CLOCK_fire(TRIGGERED_CLOCK* triggered_clock);

/* Trigger Out */

typedef struct TRIGGER_OUT {
	DECLARE_SUPER_TYPE(ACTOR)
	
	TYPED_PORT input;
	TYPED_PORT output;
} TRIGGER_OUT;

typedef struct TRIGGER_OUT_METHOD_TABLE {
	void (*fire)(struct TRIGGER_OUT* clock);
} TRIGGER_OUT_METHOD_TABLE;

extern TRIGGER_OUT_METHOD_TABLE TRIGGER_OUT_method_table;

void TRIGGER_OUT_init(TRIGGER_OUT* trigger_out, void* actual_ref,
	SCHEDULER* scheduler);
void TRIGGER_OUT_initialize(TRIGGER_OUT* trigger_out);
void TRIGGER_OUT_fire(TRIGGER_OUT* trigger_out);

#endif /*MAIN_H_*/
