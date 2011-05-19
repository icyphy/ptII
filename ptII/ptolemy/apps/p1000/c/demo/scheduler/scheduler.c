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

Scheduler_TypeData Scheduler_typeData;

/*
 * Initiate Scheduler_TypeData.
 * 
 * @param type_data The type data to be initiated.
 */
void Scheduler_TypeData_init(Scheduler_TypeData* type_data) {
	// Call the initiate method of the super-type.
	TypeData_init((TypeData*) type_data);
	
	// Override super-type.
	*((TypeData*) type_data) = (TypeData) {
		&GeneralType_typeData,		// superType
		"Scheduler",				// typeName
		sizeof(Scheduler),			// size
	};
	
	// Initiate data for the current type.
	type_data->execute = Scheduler_execute;
}

/**
 * Initiate a scheduler.
 * 
 * @param scheduler The scheduler to be initiated.
 * @param actual_type_data The type data of the scheduler's actual type, or
 *  NULL. When NULL is given (which is usually the case when called by the
 *  user), Scheduler_typeData is used.
 */
void Scheduler_init(Scheduler* scheduler,
	Scheduler_TypeData* actual_type_data) {
	
	GeneralType_init((GeneralType*) scheduler,
		(TypeData*) (actual_type_data == NULL ?
				&Scheduler_typeData : actual_type_data));
	
	scheduler->fd = 0;
	BidirList_init(&(scheduler->actorList));
	BidirList_init(&(scheduler->portList));
}

/**
 * Register a port with the scheduler by adding the port into its port list.
 * 
 * @param scheduler The scheduler.
 * @param port The port to be registered.
 */
void Scheduler_registerPort(Scheduler* scheduler, Port* port) {
	BidirList_pushBack(&(scheduler->portList), port);
}

/**
 * Execute the system with the given scheduler.
 * 
 * @param scheduler The scheduler to be used in the execution.
 */
void Scheduler_execute(Scheduler* scheduler) {
	Port* port;
	TypedPort* typed_port;
	Actor* actor;
	BidirListElement* current_element;
	
	while(1) {
		current_element = scheduler->portList.first;
		while (current_element != NULL) {
			port = (Port*) current_element->payload;
			typed_port = (TypedPort*) port;
			if (typed_port->eventQueue.first != NULL) {
				BidirList_pushBack(&(scheduler->actorList), port->container);
			}
			current_element = current_element->next;
		}
		
		actor = (Actor*) BidirList_removeFirst(&(scheduler->actorList));
		while (actor != NULL) {
			INVOKE_VIRTUAL_METHOD(Actor, fire, actor);
			actor = (Actor*) BidirList_removeFirst(&(scheduler->actorList));
		}
	}
}
