/* Discrete event scheduler.

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

#include "scheduler.h"
#include "int_token.h"
#include "typed_port.h"

/**
 * Initiate an object of the SCHEDULER type.
 * 
 * @param scheduler Reference to the SCHEDULER object to be initiated.
 * @param actual_ref The actual reference to the object.
 */
void SCHEDULER_init(SCHEDULER* scheduler, void* actual_ref) {
	INIT_SUPER_TYPE(SCHEDULER, GENERAL_TYPE, scheduler, actual_ref, NULL);
	
	scheduler->fd = 0;
	scheduler->first_actor = scheduler->last_actor = NULL;
	scheduler->first_port = scheduler->last_port = NULL;
}

/**
 * Register a port with the scheduler by adding the port into its port list.
 * 
 * @param scheduler Reference to the scheduler.
 * @param port Reference to the port to be registered.
 */
void SCHEDULER_register_port(SCHEDULER* scheduler, PORT* port) {
	PUSH_BACK(scheduler->first_port, scheduler->last_port, port);
}

/**
 * Execute the system with the given scheduler.
 * 
 * @param scheduler Reference to the scheduler to be used in the execution.
 */
void SCHEDULER_execute(SCHEDULER* scheduler) {
	PORT* port;
	TYPED_PORT* typed_port;
	ACTOR* actor;
	
	while(1) {
		port = scheduler->first_port;
		while (port != NULL) {
			typed_port = CAST(port, TYPED_PORT);
			if (typed_port->first_event != NULL) {
				PUSH_BACK(scheduler->first_actor, scheduler->last_actor,
					port->container);
			}
			port = port->next;
		}
		
		actor = scheduler->first_actor;
		while (actor != NULL) {
			INVOKE_VIRTUAL_METHOD(ACTOR, fire, actor);
			REMOVE_FIRST(scheduler->first_actor, scheduler->last_actor);
			actor = scheduler->first_actor;
		}
	}
}
