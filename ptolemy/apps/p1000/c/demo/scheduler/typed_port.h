/* TYPED_PORT, a sub-type of PORT.

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

 @author Thomas Huining Feng, Yang Zhao
 
 */

#ifndef TYPED_PORT_H_
#define TYPED_PORT_H_

#include "types.h"
#include "port.h"
#include "event.h"

/**
 * TYPED_PORT is a kind of ports that are typed (specifying the tokens that they
 * are allowed to receive or send). Each typed port also has an event queue for
 * the discrete event execution.
 */
typedef struct TYPED_PORT {
	/* TYPED_PORT type is directly inherited from PORT. */
	DECLARE_SUPER_TYPE(PORT)
	
	/* The first event in the event queue. */
	EVENT* first_event;
	/* The last event in the event queue. */
	EVENT* last_event;
} TYPED_PORT;

/* ACTOR is defined in actor.h. */
struct ACTOR;

/**
 * Initiate an object of the TYPED_PORT type, and assign a container actor to
 * it.
 * 
 * @param typed_port Reference to the TYPED_PORT object to be initiated.
 * @param actual_ref The actual reference to the object.
 * @param container Reference to the actor that contains the typed port.
 */
void TYPED_PORT_init(TYPED_PORT* typed_port, void* actual_ref,
	struct ACTOR* container);

/**
 * Send an event via the typed port. The event will be duplicated in the heap
 * before it is sent. The given typed port must be an output port. If it is
 * connected to input port(s), then the input ports will laster receive the
 * event.
 * 
 * @param typed_port The output typed port where the event will be sent.
 * @param event The event that will be duplicated in the heap and be sent.
 */
void TYPED_PORT_send(TYPED_PORT* typed_port, const EVENT* event);

#endif /*TYPED_PORT_H_*/
