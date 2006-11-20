#include <stdlib.h>
#include <string.h>
#include "event.h"

EVENT* EVENT_duplicate(const EVENT* event)
{
	EVENT* heap_event;
	void* heap_token;
	size_t token_size;
	
	token_size = UPCAST(event->token, GENERAL_TYPE)->size;
	heap_event = (EVENT*) malloc(sizeof(EVENT) + token_size);

	// Forward compatible with future event definitions, which may contain more
	// fields.
	*heap_event = *event;
	
	// Copy the token in to heap so that the original event's token can be freed
	// immediately.
	heap_token = ((void*) heap_event) + sizeof(EVENT);
	memcpy(heap_token, event->token->_actual_ref, token_size);
	heap_event->token = UPCAST(heap_token, TOKEN);
	
	return heap_event;
}
