/* Main program of the scheduler demo with some actor definitions.

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
typedef struct Clock {
	/* Clock is directly inherited from Actor. */
	Actor super;
	
	/* The fireAt port. */
	TypedPort fireAt;
	/* The output port. */
	TypedPort output;
	/* The time. */
	Time time;
	/* The endTime */
	Time endTime;
} Clock;

/*
 * Clock's static type data.
 */
typedef struct Clock_TypeData {
	Actor_TypeData inheritedTypeData;
} Clock_TypeData;

extern Clock_TypeData Clock_typeData;

/*
 * Initiate Clock_TypeData.
 * 
 * @param type_data The type data to be initiated.
 */
void Clock_TypeData_init(Clock_TypeData* type_data);

/**
 * Initiate a clock, and assign a scheduler to it.
 * 
 * @param clock The clock to be initiated.
 * @param actual_type_data The type data of the clock's actual type, or NULL.
 *  When NULL is given (which is usually the case when called by the user),
 *  Clock_typeData is used.
 * @param scheduler The scheduler.
 */
void Clock_init(Clock* clock, Clock_TypeData* actual_type_data,
	Scheduler* scheduler);

/**
 * Fire the Clock.
 * 
 * @param clock The clock to be fired.
 */
void Clock_fire(Actor* actor);

/**
 * Initialize the clock actor. This method should be called before the execution
 * starts.
 * 
 * @param actor The clock actor to be initialized.
 */
void Clock_initialize(Actor* actor);

/**
 * TriggeredClock actor.
 */
typedef struct TriggeredClock {
	/* TriggeredClock is directly inherited from CLOCK. */
	Clock super;
	
	/* The trigger port. */
	TypedPort trigger;
	/* The output port. */
	TypedPort output;
	/* The period. */
	Time period;
	/* The phase. */
	Time phase;
	/* The startTime. */
	Time startTime;
} TriggeredClock;

/*
 * TriggeredClock's static type data.
 */
typedef struct TriggeredClock_TypeData {
	Clock_TypeData inheritedTypeData;
} TriggeredClock_TypeData;

extern TriggeredClock_TypeData TriggeredClock_typeData;

/*
 * Initiate TriggeredClock_TypeData.
 * 
 * @param type_data The type data to be initiated.
 */
void TriggeredClock_TypeData_init(TriggeredClock_TypeData* type_data);

/**
 * Initiate a triggered clock, and assign a scheduler to it.
 * 
 * @param triggered_clock The triggered clock to be initiated.
 * @param actual_type_data The type data of the triggered clock's actual type,
 *  or NULL. When NULL is given (which is usually the case when called by the
 *  user), TriggeredClock_typeData is used.
 * @param scheduler The scheduler.
 */
void TriggeredClock_init(TriggeredClock* triggered_clock,
	TriggeredClock_TypeData* actual_type_data, Scheduler* scheduler);

/**
 * Fire the triggered clock.
 * 
 * @param triggered_clock The triggered clock to be fired.
 */
void TriggeredClock_fire(Actor* actor);

/**
 * Initialize the triggered clock actor. This method should be called before the
 * execution starts.
 * 
 * @param actor The triggered clock actor to be initialized.
 */
void TriggeredClock_initialize(Actor* actor);

/**
 * TriggerOut actor.
 */
typedef struct TriggerOut {
	/* TriggerOut is directly inherited from Clock. */
	Clock super;
	
	/* The input port. */
	TypedPort input;
	/* The output port. */
	TypedPort output;
} TriggerOut;

/*
 * TriggerOut's static type data.
 */
typedef struct TriggerOut_TypeData {
	Clock_TypeData inheritedTypeData;
} TriggerOut_TypeData;

extern TriggerOut_TypeData TriggerOut_typeData;

/*
 * Initiate TriggerOut_TypeData.
 * 
 * @param type_data The type data to be initiated.
 */
void TriggerOut_TypeData_init(TriggerOut_TypeData* type_data);

/**
 * Initiate a trigger out actor, and assign a scheduler to it.
 * 
 * @param trigger_out The trigger out actor to be initiated.
 * @param actual_type_data The type data of the triggered out actor 's actual
 *  type, or NULL. When NULL is given (which is usually the case when called by
 *  the user), TriggerOut_typeData is used.
 * @param scheduler The scheduler.
 */
void TriggerOut_init(TriggerOut* trigger_out,
	TriggerOut_TypeData* actual_type_data, Scheduler* scheduler);

/**
 * Fire the trigger out actor.
 * 
 * @param trigger_out The trigger out actor to be fired.
 */
void TriggerOut_fire(Actor* actor);

/**
 * Initialize the trigger out actor. This method should be called before the
 * execution starts.
 * 
 * @param actor The trigger out actor to be initialized.
 */
void TriggerOut_initialize(Actor* actor);

#endif /*MAIN_H_*/
