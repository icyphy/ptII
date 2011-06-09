/* Event that can be sent and received via connections between ports.

 Copyright (c) 1997-2006 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 @author Thomas Huining Feng

 */

#include <stdlib.h>
#include <string.h>
#include "event.h"

/**
 * Duplicate an event in the heap, and return the duplicated event. If the given
 * event contains a token, memory will be allocated from the heap to store a
 * copy of that token. The user need not explicitly free the token, though
 * he/she do need to free the event, because the event and the token are stored
 * in a continuous trunk of memory.
 * 
 * @param event Reference to the event to be duplicated.
 * @return Reference to the duplicated event in the heap.
 */
Event* Event_duplicate(const Event* event) {
	Event* heap_event;
	void* heap_token;
	size_t token_size;
	
	token_size = event->token == NULL ? 0
			: ((GeneralType*) event->token)->typeData->size;
	heap_event = (Event*) malloc(sizeof(Event) + token_size);

	// Forward compatible with future event definitions, which may contain more
	// fields.
	*heap_event = *event;
	
	if (event->token != NULL) {
		// Copy the token in to heap so that the original event's token can be
		// freed immediately.
		heap_token = ((void*) heap_event) + sizeof(Event);
		memcpy(heap_token, event->token, token_size);
		heap_event->token = (Token*) heap_token;
	}
	
	return heap_event;
}
