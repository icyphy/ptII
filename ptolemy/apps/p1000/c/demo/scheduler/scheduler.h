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

#ifndef SCHEDULER_H_
#define SCHEDULER_H_

#include "types.h"
#include "actor.h"
#include "port.h"

/**
 * SCHEDULER type for the discrete event scheduler.
 */
typedef struct SCHEDULER {
	/* SCHEDULER type is directly inherited from GENERAL_TYPE. */
	DECLARE_SUPER_TYPE(GENERAL_TYPE)
	
	/* Device file descriptor. */
	int fd;
	/* The first actor in the actor list. */
	struct ACTOR* first_actor;
	/* The last actor in the actor list. */
	struct ACTOR* last_actor;
	/* The first port in the port list. */
	struct PORT* first_port;
	/* The last port in the port list. */
	struct PORT* last_port;
} SCHEDULER;

/**
 * Initiate an object of the SCHEDULER type.
 * 
 * @param scheduler Reference to the SCHEDULER object to be initiated.
 * @param actual_ref The actual reference to the object.
 */
void SCHEDULER_init(SCHEDULER* scheduler, void* actual_ref);

/**
 * Register a port with the scheduler by adding the port into its port list.
 * 
 * @param scheduler Reference to the scheduler.
 * @param port Reference to the port to be registered.
 */
void SCHEDULER_register_port(SCHEDULER* scheduler, struct PORT* port);

/**
 * Execute the system with the given scheduler.
 * 
 * @param scheduler Reference to the scheduler to be used in the execution.
 */
void SCHEDULER_execute(SCHEDULER* scheduler);

#endif /*SCHEDULER_H_*/
