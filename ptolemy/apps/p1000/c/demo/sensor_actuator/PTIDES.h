/** Utilities for the type infrastructure.

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

 @author Jia Zou
 @Parts of this code that serves to interface the hardware is copied from
 previous written code samples by Slobodan Matic and Agilent
 
 */

/** this is the head file for PTIDES.c
 The  constants, structures are defined, and functions are declared.
 */

/**
 BUFFER_SIZE is the size of the packet being sent from one platform to another.
 This number need to be greater or equal than the size of the data being sent,
 otherwise the packet is corrupted.
 */
#define BUFFER_SIZE 256
/**
 CLOCK_EVENTS is the number of events the clock actor will put onto the event 
 queue before another going sleep. After all these events have been executed, 
 CLOCK_EVENTS number of events will again be put onto the event queue.
 */
#define CLOCK_EVENTS 5
/**
 CLOCK_PERIOD_SECS and CLOCK_PERIOD_NSECS together define the cycle time 
 between two clock events.
 */
#define CLOCK_PERIOD_SECS 5
#define CLOCK_PERIOD_NSECS 0
/**
 MODEL_DELAY_SECS and MODEL_DELAY_NSECS together define the model delay of the
 modelDelay actor.
 */
#define MODEL_DELAY_SECS 0
#define MODEL_DELAY_NSECS 7000000       //7ms
/**
 BOUNDED_DELAY_SECS and BOUDNED_DELAY_NSECS together define the total real time
 delay between the sensor and the merge actor
 */
#define BOUNDED_DELAY_SECS 0
#define BOUNDED_DELAY_NSECS 5000000     //5ms

//ALL STRUCTs

typedef struct{
    int intValue;
} Value;

typedef struct{
    unsigned int secs;
    unsigned int nsecs;
    int microstep;
} Tag;

typedef struct act {

	//FIXME: should have a linked list of next_actors...
    struct act *nextActor1;
    struct act *nextActor2;

	//FIXME: type is used when fire_actors() is called, and which actor to fire needs to be decided
    char type[3];

    //actor methods
    //preinitialize();
    //initialize()
    //prefire() returns true to indicate that the actor is ready to fire
    //prefire();
    //fire();
    //postfire();
    //wrapup();
} Actor;

typedef struct{
    Value this_value;
    Tag orderTag;
    Tag realTag;

    Actor* actorFrom;
    //an event would only have 1 actorToFire
    Actor* actorToFire;

    //a bit that tell us whether static timing analysis has been done
    int staticTimingAnalysisDone;
} Event;

//building the linked list
//where when inserting, we insert by time stamp
typedef struct el{
    Event thisEvent;
    struct el *next;
} Event_Link;

//The following functions are in PTIDES.c
void actuator_fire(Actor*, Event*);
void clock_fire(Actor*, Event*);
void clock_init(Actor*);
void computation_fire(Actor*, Event*);
void Die(char*);
void event_add(Event);
void event_insert(Event);
void event_pop();
void execute_event();
void firing_actor(Event*);
void merge_fire(Actor*, Event*);
void model_delay_fire(Actor*, Event*);
void processEvents();
void receive_init(Actor*);
void sensor_init(Actor*);

void sensor_run(Actor*);
void static_timing_analysis(Event*);
void thread_timed_interrupt();
void transmit_fire(Actor*, Event*);

//The following functions are in EnDecode.c
void encodeHwNsec( FPGA_TIME*, const unsigned int, const unsigned int);
void decodeHwNsec( const FPGA_TIME*, unsigned int*, unsigned int*);
