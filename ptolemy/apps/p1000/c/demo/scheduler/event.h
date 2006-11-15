#ifndef EVENT_H_
#define EVENT_H_

#include "types.h"
#include "token.h"

typedef struct EVENT {
	TOKEN* token;
	TIME time;
	int is_timer_event : 1;
	struct EVENT* prev;
	struct EVENT* next;
} EVENT;

#endif /*EVENT_H_*/
