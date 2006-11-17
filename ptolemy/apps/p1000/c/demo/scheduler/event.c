#include <stdlib.h>
#include <string.h>
#include "event.h"

EVENT* EVENT_duplicate(const EVENT* event)
{
	EVENT* heap_event;
	void* heap_token;
	
	heap_event = (EVENT*) malloc(sizeof(EVENT) + event->token->size);

	// Forward compatible with future event definitions, which may contain more
	// fields.
	*heap_event = *event;
	
	// Copy the token in to heap so that the original event's token can be freed
	// immediately.
	heap_token = ((void*) heap_event) + sizeof(EVENT);
	memcpy(heap_token, event->token->actual_ref, event->token->size);
	heap_event->token = UPCAST(heap_token, TOKEN);
	
	return heap_event;
}
