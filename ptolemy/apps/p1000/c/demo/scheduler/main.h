/* Main program of the scheduler demo with some actor definitions.

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

#ifndef MAIN_H_
#define MAIN_H_

#include "types.h"
#include "actor.h"
#include "typed_port.h"
#include "scheduler.h"

/**
 * Clock actor, the super-class of triggered clock and trigger out actors.
 */
typedef struct CLOCK {
	/* CLOCK actor is directly inherited from ACTOR. */
	DECLARE_SUPER_TYPE(ACTOR)
	
	/* The fire_at port. */
	TYPED_PORT fire_at;
	/* The output port. */
	TYPED_PORT output;
	/* The time. */
	TIME time;
	/* The end_time */
	TIME end_time;
} CLOCK;

/**
 * CLOCK's method table.
 */
typedef struct CLOCK_METHOD_TABLE {
	/* Fire method. */
	void (*fire)(struct CLOCK* clock);
} CLOCK_METHOD_TABLE;

/**
 * Constant of CLOCK's method table, defined in main.c.
 */
extern CLOCK_METHOD_TABLE CLOCK_method_table;

/**
 * Initiate an object of the CLOCK type, and assign a scheduler to it.
 * 
 * @param clock Reference to the CLOCK object to be initiated.
 * @param actual_ref The actual reference to the object.
 * @param scheduler Reference to the scheduler.
 */
void CLOCK_init(CLOCK* clock, void* actual_ref, SCHEDULER* scheduler);

/**
 * Fire the CLOCK.
 * 
 * @param clock Reference to the CLOCK object.
 */
void CLOCK_fire(CLOCK* clock);

/**
 * TRIGGERED_CLOCK actor.
 */
typedef struct TRIGGERED_CLOCK {
	/* TRIGGERED_CLOCK actor is directly inherited from CLOCK. */
	DECLARE_SUPER_TYPE(CLOCK)
	
	/* The trigger port. */
	TYPED_PORT trigger;
	/* The output port. */
	TYPED_PORT output;
	/* The period. */
	TIME period;
	/* The phase. */
	TIME phase;
	/* The start_time. */
	TIME start_time;
} TRIGGERED_CLOCK;

/**
 * TRIGGERED_CLOCK's method table.
 */
typedef struct TRIGGERED_CLOCK_METHOD_TABLE {
	/* Fire method. */
	void (*fire)(struct TRIGGERED_CLOCK* clock);
} TRIGGERED_CLOCK_METHOD_TABLE;

/**
 * Constant for TRIGGERED_CLOCK's method table, defined in main.c.
 */
extern TRIGGERED_CLOCK_METHOD_TABLE TRIGGERED_CLOCK_method_table;

/**
 * Initiate an object of the TRIGGERED_CLOCK type, and assign a scheduler to it.
 * 
 * @param triggered_clock Reference to the TRIGGERED_CLOCK object to be
 *  initiated.
 * @param actual_ref The actual reference to the object.
 * @param scheduler Reference to the scheduler.
 */
void TRIGGERED_CLOCK_init(TRIGGERED_CLOCK* triggered_clock, void* actual_ref,
	SCHEDULER* scheduler);

/**
 * Initialize the TRIGGERED_CLOCK object. This method is called before the
 * execution starts.
 * 
 * @param triggered_clock Reference to the TRIGGERED_CLOCK object.
 */
void TRIGGERED_CLOCK_initialize(TRIGGERED_CLOCK* triggered_clock);

/**
 * Fire the TRIGGERED_CLOCK.
 * 
 * @param triggered_clock Reference to the TRIGGERED_CLOCK object.
 */
void TRIGGERED_CLOCK_fire(TRIGGERED_CLOCK* triggered_clock);

/**
 * TRIGGER_OUT actor.
 */
typedef struct TRIGGER_OUT {
	/* TRIGGER_OUT actor is directly inherited from CLOCK. */
	DECLARE_SUPER_TYPE(ACTOR)
	
	/* The input port. */
	TYPED_PORT input;
	/* The output port. */
	TYPED_PORT output;
} TRIGGER_OUT;

/**
 * TRIGGER_OUT's method table.
 */
typedef struct TRIGGER_OUT_METHOD_TABLE {
	/* Fire method. */
	void (*fire)(struct TRIGGER_OUT* clock);
} TRIGGER_OUT_METHOD_TABLE;

/**
 * Constant for TRIGGER_OUT's method table, defined in main.c.
 */
extern TRIGGER_OUT_METHOD_TABLE TRIGGER_OUT_method_table;

/**
 * Initiate an object of the TRIGGER_OUT type, and assign a scheduler to it.
 * 
 * @param trigger_out Reference to the TRIGGER_OUT object to be initiated.
 * @param actual_ref The actual reference to the object.
 * @param scheduler Reference to the scheduler.
 */
void TRIGGER_OUT_init(TRIGGER_OUT* trigger_out, void* actual_ref,
	SCHEDULER* scheduler);

/**
 * Initialize the TRIGGER_OUT object. This method is called before the execution
 * starts.
 * 
 * @param trigger_out Reference to the TRIGGER_OUT object.
 */
void TRIGGER_OUT_initialize(TRIGGER_OUT* trigger_out);

/**
 * Fire the TRIGGER_OUT.
 * 
 * @param trigger_out Reference to the TRIGGER_OUT object.
 */
void TRIGGER_OUT_fire(TRIGGER_OUT* trigger_out);

#endif /*MAIN_H_*/
