/* EVENT type.

 Copyright (c) 1997-2005 The Regents of the University of California.
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

#ifndef EVENT_H_
#define EVENT_H_

#include "types.h"
#include "token.h"

/**
 * EVENT type for events in the discrete event domain.
 */
typedef struct EVENT {
	/* The token in the event. */
	TOKEN* token;
	/* The time. */
	TIME time;
	/* Whether the event is a timer event. */
	int is_timer_event : 1;
	/* The previous event in a list. */
	struct EVENT* prev;
	/* The next event in a list. */
	struct EVENT* next;
} EVENT;

/**
 * Duplicate an event in the heap, and return the duplicated event. If the given
 * event contains a token, memory will be allocated from the heap to store a
 * copy of that token. The user need not explicitly free the token, though
 * he/she do need to free the event, because the event and the token are
 * allocated in a continuous trunk of memory.
 * 
 * @param event Reference to the event to be duplicated.
 * @return Reference to the duplicated event in the heap.
 */
EVENT* EVENT_duplicate(const EVENT* event);

#endif /*EVENT_H_*/
