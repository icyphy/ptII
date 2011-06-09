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
#include "bidir_list.h"

/**
 * Scheduler for the discrete event scheduler.
 */
typedef struct Scheduler {
	/* Scheduler is directly inherited from GeneralType. */
	GeneralType super;
	
	/* Device file descriptor. */
	int fd;
	/* The list of actors. */
	BidirList actorList;
	/* The list of ports. */
	BidirList portList;
} Scheduler;

/**
 * Scheduler's static type data.
 */
typedef struct Scheduler_TypeData {
	TypeData inheritedTypeData;
	
	// execute method.
	void (*execute)(Scheduler* scheduler);
} Scheduler_TypeData;

extern Scheduler_TypeData Scheduler_typeData;

/*
 * Initiate Scheduler_TypeData.
 * 
 * @param type_data The type data to be initiated.
 */
void Scheduler_TypeData_init(Scheduler_TypeData* type_data);

/**
 * Initiate a scheduler.
 * 
 * @param scheduler The scheduler to be initiated.
 * @param actual_type_data The type data of the scheduler's actual type, or
 *  NULL. When NULL is given (which is usually the case when called by the
 *  user), Scheduler_typeData is used.
 */
void Scheduler_init(Scheduler* scheduler, Scheduler_TypeData* actual_type_data);

/**
 * Register a port with the scheduler by adding the port into its port list.
 * 
 * @param scheduler The scheduler.
 * @param port The port to be registered.
 */
void Scheduler_registerPort(Scheduler* scheduler, struct Port* port);

/**
 * Execute the system with the given scheduler.
 * 
 * @param scheduler The scheduler to be used in the execution.
 */
void Scheduler_execute(Scheduler* scheduler);

#endif /*SCHEDULER_H_*/
