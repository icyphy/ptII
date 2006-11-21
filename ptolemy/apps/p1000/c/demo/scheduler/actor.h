/* ACTOR type, the general super-type of all the actor types.

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

#ifndef ACTOR_H_
#define ACTOR_H_

#include "types.h"

/**
 * SCHEDULER type, defined in scheduler.h.
 */
struct SCHEDULER;

/**
 * ACTOR type, which is the super-type of all actors.
 */
typedef struct ACTOR {
	DECLARE_SUPER_TYPE(GENERAL_TYPE)
	
	/* The scheduler. */
	struct SCHEDULER* scheduler;
	/* The previous actor in a list. */
	struct ACTOR* prev;
	/* The next actor in a list. */
	struct ACTOR* next;
} ACTOR;

/**
 * ACTOR type's method table, which defines all the virtual methods belonging to
 * an actor. Sub-classes may override these methods by redefining them in their
 * method tables, or may add new virtual methods to their method tables.
 */
typedef struct ACTOR_METHOD_TABLE {
	/* Fire method of the actor. */
	void (*fire)(struct ACTOR* actor);
} ACTOR_METHOD_TABLE;

/**
 * Constant for ACTOR type's method table, defined in actor.c.
 */
extern ACTOR_METHOD_TABLE ACTOR_method_table;

/**
 * Initiate an object of the ACTOR type, and assign a scheduler to it.
 * 
 * @param actor Reference to the ACTOR object to be initiated.
 * @param actual_ref The actual reference to the object.
 * @param scheduler Reference to the scheduler.
 */
void ACTOR_init(ACTOR* actor, void* actual_ref, struct SCHEDULER* scheduler);

/**
 * Fire the ACTOR.
 * 
 * @param actor Reference to the ACTOR object.
 */
void ACTOR_fire(ACTOR* actor);

#endif /*ACTOR_H_*/
