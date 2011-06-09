/* Actor type, the common super-type of all the actors.

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

#ifndef ACTOR_H_
#define ACTOR_H_

#include "types.h"

/**
 * Scheduler type, defined in scheduler.h.
 */
struct Scheduler;

/**
 * Actor type, the common super-type of all the actors.
 */
typedef struct Actor {
	/* Actor is directly inherited from GeneralType. */
	GeneralType super;
	
	/* The scheduler. */
	struct Scheduler* scheduler;
} Actor;

/**
 * Actor's static type data.
 */
typedef struct Actor_TypeData {
	TypeData inheritedTypeData;
	
	// fire method.
	void (*fire)(Actor* actor);
	void (*initialize)(Actor* actor);
} Actor_TypeData;

extern Actor_TypeData Actor_typeData;

/*
 * Initiate Actor_TypeData.
 * 
 * @param type_data The type data to be initiated.
 */
void Actor_TypeData_init(Actor_TypeData* type_data);

/**
 * Initiate an actor, and assign a scheduler to it.
 * 
 * @param actor The actor to be initiated.
 * @param actual_type_data The type data of the actor's actual type, or NULL.
 *  When NULL is given (which is usually the case when called by the user),
 *  Actor_typeData is used.
 * @param scheduler The scheduler.
 */
void Actor_init(Actor* actor, Actor_TypeData* actual_type_data,
	struct Scheduler* scheduler);

/**
 * Fire the actor.
 * 
 * @param actor The actor to be fired.
 */
void Actor_fire(Actor* actor);

/**
 * Initialize the actor. This method should be called before the execution
 * starts.
 * 
 * @param actor The actor to be initialized.
 */
void Actor_initialize(Actor* actor);

#endif /*ACTOR_H_*/
