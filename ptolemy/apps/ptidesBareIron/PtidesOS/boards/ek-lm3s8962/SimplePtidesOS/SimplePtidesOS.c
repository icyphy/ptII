/******************************************************************************/
/* BLINKY.C: LED Flasher                                                      */
/******************************************************************************/
/* This file is part of the uVision/ARM development tools.                    */
/* Copyright (c) 2005-2007 Keil Software. All rights reserved.                */
/* This software may only be used under the terms of a valid, current,        */
/* end user licence from KEIL for a compatible version of KEIL software       */
/* development tools. Nothing else gives you the right to use this software.  */
/******************************************************************************/

#include <stdio.h>

#include "../../../uart/hw_ints.h"
#include "../../../uart/hw_memmap.h"
#include "../../../uart/hw_types.h"
#include "../../../src/debug.h"
#include "../../../src/gpio.h"
#include "../../../src/interrupt.h"
#include "../../../src/sysctl.h"
#include "../../../src/uart.h"
#include "../rit128x96x4.h"

#include "structures.h"
#include "functions.h"
#include "actors.h"

/* Status LED and Push Buttons pin definitions */
#define LED             GPIO_PIN_0 /* PF0 */
#define SELECT          GPIO_PIN_1 /* PF1 */
#define UP              GPIO_PIN_0 /* PE0 */
#define DOWN            GPIO_PIN_1 /* PE1 */
#define LEFT            GPIO_PIN_2 /* PE2 */
#define RIGHT           GPIO_PIN_3 /* PE3 */
#define BUTTON         (UP | DOWN | LEFT | RIGHT)

/* Global variables */
volatile unsigned char Tick;    // Tick Counter (0..99)
volatile unsigned long Time;    // Time Counter (10ms)
volatile unsigned char Buttons; // Button States
volatile unsigned char TraceB;  // Trace Buttons

#define MAX_BUFFER_LIMIT 10
#define CLOCK_PERIOD 100000

Event* EVENT_QUEUE_HEAD = NULL;
int CK1_BUF_SIZE = 0;
long CK1_TIME = 0;

unsigned int STOP_SOURCE_PROCESS;

long COMPUTATION_MODEL_TIME_ADJUSTMENT = 0;
long MERGE_MODEL_TIME_ADJUSTMENT = 0;

#define FALSE 0
#define TRUE  1

Actor* SOURCE1;
Actor* SOURCE2;

/**
 * This is the fire method for actuator
 * The actuator is used to check if the event received output has met the 
 * deadline by comparing the current time with the timestamp.
 * Note that it actually use the hardware to do the actuation.
 * Note that this actually is not a terminating actor, actuation is not done in
 * this method, instead, it's done by actuator_run().
 * Since it's not a terminating actor, the event is not popped.
 *
 */
void actuator_fire(Actor* this_actuator, Event* thisEvent) {

	long currentTime = getCurrentPhysicalTime();
    Tag *stampedTag = &(thisEvent->Tag);

    // Compare time with taged time, if not safe to actuate, then add events
    // and wait until time to actuate. If safe to actuate, actuate now.
	if (stampedTag->timestamp > currentTime)
    {
//        printf("\nthe actuator was able to produce event ON TIME!!!! \n");
//        printf("the timestamped tag is: %.9d.%9.9d %i \n", stampedTag->secs, stampedTag->nsecs, stampedTag->microstep);
//        printf("the current time is:    %.9d.%9.9d %i \n", secs, nsecs, microstep);

		setActuationInterrupt(stampedTag->timestamp);		
	}											   
    else
    {
//        printf("\nthe timing of the system was NOT MET!!!!! \n");
//        printf("the timestamped tag is: %.9d.%9.9d %i \n", stampedTag->secs, stampedTag->nsecs, stampedTag->microstep);
//        printf("the current time is:    %.9d.%9.9d %i \n", secs, nsecs, microstep);

		// FIXME: do something!
    }
    return;
}

/**
 * This function is basically the same as clock_init(), expect only this 
 * function is called through an event, thus we use event_add() instead 
 * of event_insert() in this case.
 */
void clock_fire(Actor* this_clock, Event* thisEvent) {

	long time = CK1_TIME;
	Event* newEvent = (Event*) malloc(sizeof(Event));
	//Event* new_event2;
		    
    time += CLOCK_PERIOD;

    //Notice orderTag and realTag are set to different things:
    //since the event queue is ordered by orderTag, we these events to 
	//execute now.
	//However realTag is the real timestamp to appear at the output.
    newEvent->thisValue.doubleValue = 0;
    newEvent->Tag.timestamp = time;
    newEvent->Tag.microstep = 0;   //FIXME
    newEvent->actorToFire = this_clock->nextActor1;
    newEvent->actorFrom = this_clock;

    event_add(newEvent);
	CK1_BUF_SIZE++;

    if (this_clock->nextActor2 != NULL) {
        Event* newEvent2 = (Event*) malloc(sizeof(Event));
		*newEvent2 = *newEvent;
        newEvent2->actorToFire = this_clock->nextActor2;
        event_add(newEvent2);
    }

    //Do not need to call processEvents() in this case
    //since processEvents() called clock_fire
    return;
}

/** This is the initilization for the clock actor.
 * its functionality is to provide events with intervals specified by 
 * CLOCK_PERIOD_SECS/NSECS.
 * At one time, CLOCK_EVNTS number of events are produced at a time, and they 
 * can be executed momentarily.
 * when the deadline of the last event that was produced has passed
 * CLOCK_EVENTS number of events are again produced by a event added into the event queue
 */
void clock_init(Actor *this_clock)
{
    long currentTime = getCurrentPhysicalTime();

    Event* newEvent = (Event*) malloc(sizeof(Event));

    //CLOCK_PERIOD specifies the period
    currentTime += CLOCK_PERIOD;   

    //notice orderTag and realTag are set to different things:
    //since the event queue is ordered by orderTag, we these events to execute NOW
    //however realTag is the real timestamp to appear at the output
    newEvent->Tag.timestamp = currentTime;
    newEvent->Tag.microstep = 0;   //FIXME
    newEvent->thisValue.doubleValue = 0;
    newEvent->actorToFire = this_clock->nextActor1;
    newEvent->actorFrom = this_clock;

    event_add(newEvent);

	CK1_TIME = currentTime;

    if (this_clock->nextActor2 != NULL)
    {
        Event* newEvent2 = (Event*) malloc(sizeof(Event));
		*newEvent2 = *newEvent;
        newEvent2->actorToFire = this_clock->nextActor2;
        event_add(newEvent2);
    }

    return;
}

/** 
 * This is the fire method for computation.
 * Computation method do not have any real functionality here, where we simply
 * set our event queue to fire the next actor.
 */
void computation_fire(Actor* this_computation, Event* thisEvent) {

	double thisDouble;
	Event* newEvent = (Event*) malloc(sizeof(Event));;
        
	thisDouble = thisEvent->thisValue.doubleValue;
    thisDouble++;

	newEvent->thisValue.doubleValue = thisDouble;
    newEvent->actorToFire = this_computation->nextActor1;
    newEvent->actorFrom = this_computation;

    //arbitrarily delay either 1/4 of a sec or 1/2 of a sec or none.
	event_add(newEvent);

    if (this_computation->nextActor2 != NULL)
    {
        Event* newEvent2 = (Event*) malloc(sizeof(Event));
		*newEvent2= *newEvent;
        newEvent2->actorToFire = this_computation->nextActor2;
        event_add(newEvent2);
    }
}

/**
 This function is called when an error occures.
 */
void die(char *mess) { 
	perror(mess); 
	return; 
}

/** 
 * The method event_add() is only used when an actor that's not the source 
 * within a platform.
 * In this case there's no need to lock the event queue since processEvents() 
 * only execute one event at a time.
 * When events are added, make sure the event queue is ordered by orderTag of the events.
 * Currently, we simply go through the queue one event at a time and check the tags.
 * FIXME: use either binary search or calendar to improve performance.
 */
void event_add(Event* newEvent)
{

    Tag stampedTag = newEvent->Tag;

    //add an event
    Event *compare_event = EVENT_QUEUE_HEAD;
    Event *before_event = EVENT_QUEUE_HEAD;
    //move across the link until we find an event with larger tag, or stop at the end.

    while (1){
        if (compare_event == NULL)
            break;
        else if (stampedTag.timestamp < compare_event->Tag.timestamp)
                break;
        else if ((stampedTag.timestamp == compare_event->Tag.timestamp) && (
			stampedTag.microstep < compare_event->Tag.microstep))
                break;
        else {
            if (compare_event != before_event)
                before_event = before_event->next;
            compare_event = compare_event->next;
        }
    }
            
    newEvent->next = compare_event;
    if (compare_event == before_event){
        EVENT_QUEUE_HEAD = newEvent;
    }
    else if (compare_event != before_event){
        before_event->next = newEvent;
    }
    else {
//        printf("something wrong with compare and before _event\n");
        die("");
    }

}

/** 
 * event_remove() is called to pop the most recent event in the event queue.
 * it is not interlocked because it can only be called by sinks within a 
 * platform, which is the called by processEvents().
 * Here the first event on the event queue is popped.
 */
void event_remove()
{
    if (EVENT_QUEUE_HEAD != NULL){
        EVENT_QUEUE_HEAD = EVENT_QUEUE_HEAD -> next;
    } 
    else printf("event queue is already empty\n");
}

/** 
 * execute_event() checks if the event is valid. If it is, then fire actor
 * is called.
 */
void execute_event(){

    if (EVENT_QUEUE_HEAD == NULL) {
        die("EVENT_QUEUE_HEAD should never be NULL\n");
    }
    else {
        if (EVENT_QUEUE_HEAD->actorToFire == NULL){
            die("executing an event where the actors are NULL!!\n");
        }
        else {
            fire_actor(EVENT_QUEUE_HEAD);
        }
    }
}

/** 
 * fire_actor checks if static timing analysis is needed.
 * if it is, static timing analysis is called, and returns
 * if it's not, firing method of the actor specified by the event is called
 */
void fire_actor(Event* currentEvent)
{

    Actor* fire_this = currentEvent->actorToFire;
	//FIXME: USE THIS INSTEAD!! char temp_type[3] = fire_this->type;
	if (fire_this->fire_method != NULL) {
		(fire_this->fire_method)(fire_this, currentEvent);
	} else {
		die("no such method, cannot fire\n");
	}
	
    return;
}

///**fmod(x,y)
// * Code referenced from: http://www.koders.com/c/fid5C58ABB379EC1695145D6C137E954EAB07297C5C.aspx
// * changes were made to eliminate the need of floor function.
// */
//
//double fmod(double x, double y){
//    double f;
//    unsigned int i;
//    if (y == 0.0){
//        die("dividing by 0.0");
//    }
//
//    i = (unsigned int) (x/y);
//    f = x - (double)(i)*y;
//    if ((x < 0.0) != (y < 0.0))
//        f = f - y;
//    return f;
//}

/** 
 * This is the fire method for merge actor.
 * This firing method transfer the event at the input and put it to the output.
 * It also prints the current timestamp, and which actor it is from.
 * Since it is not a terminating actor, no need to pop event.
 * Only possibility to add event is when it has more than one output.
 * A merge actor may have more than one input.
 */
void merge_fire(Actor* this_merge, Event* thisEvent) {

//    printf("THIS IS THE FINAL OUTPUT OF THE MERGE Actor: \n");
//    printf("MAKE SURE THE TagS OF THESE EVENTS ARE IN ORDER: the tag on the current value are: %.9d.%9.9d %i \n", 
//		thisEvent->realTag.secs, thisEvent->Tag.nsecs, thisEvent->Tag.microstep);
//    printf("THIS OUTPUT WAS FROM Actor: %c%c%c\n", 
//		thisEvent->actorFrom->type[0], thisEvent->actorFrom->type[1], thisEvent->actorFrom->type[2]);

	Event* newEvent = (Event*)malloc(sizeof(Event));
	*newEvent = *thisEvent;
    newEvent->actorToFire = this_merge->nextActor1;
    newEvent->actorFrom = this_merge;

	event_add(newEvent);

    if (this_merge->nextActor2 != NULL)
    {
        Event* newEvent2 = (Event*)malloc(sizeof(Event));
		*newEvent2 = *newEvent;
        newEvent2->actorToFire = this_merge->nextActor2;
        event_add(newEvent2);
    }

    return;
}

/** 
 * This is the fire method for model_delay actor.
 * model_delay actor increase the current realTag timestamp by the the 
 * parameter MODEL_DELAY_SECS, where MODEL_DELAY_SECS is greater or equal 
 * to maximum delay from the source of the event to the sink.
 * At the same time, the orderTag is increased by BOUNDED_DELAY, since
 * this is the delay between the sensor actor and the merge actor.
 * Since timestamps are modified in this process, event_pop() and event_add() 
 * needs to be called.
 * Because model_delay is also a transmitter through the network, socket
 * programming is used.
 */
void model_delay_fire(Actor* this_model_delay, Event* thisEvent) {

	unsigned int model_delay = 0;
/*
	if (this_model_delay ->type[2] == '1') {
		model_delay_secs = MODEL_DELAY1_SECS;
		model_delay_nsecs = MODEL_DELAY1_NSECS;
		bounded_delay_secs = BOUNDED_DELAY1_SECS;
		bounded_delay_nsecs = BOUNDED_DELAY1_NSECS;
	}
	else if (this_model_delay ->type[2] == '2') {
		model_delay_secs = MODEL_DELAY2_SECS;
		model_delay_nsecs = MODEL_DELAY2_NSECS;
		bounded_delay_secs = BOUNDED_DELAY2_SECS;
		bounded_delay_nsecs = BOUNDED_DELAY2_NSECS;
	}
	else if (this_model_delay ->type[2] == '3') {
		model_delay_secs = MODEL_DELAY3_SECS;
		model_delay_nsecs = MODEL_DELAY3_NSECS;
		bounded_delay_secs = BOUNDED_DELAY3_SECS;
		bounded_delay_nsecs = BOUNDED_DELAY3_NSECS;
	}
	else {
		printf("this model delay does not exist!\n");
		exit(1);
	}
*/

	Event* newEvent = (Event*)malloc(sizeof(Event));
		
	newEvent->Tag.timestamp = thisEvent->Tag.timestamp + model_delay;
    newEvent->Tag.microstep = 0;   //FIXME
    newEvent->actorToFire = this_model_delay->nextActor1;
    newEvent->actorFrom = this_model_delay;

	event_add(newEvent);
    if (this_model_delay->nextActor2 != NULL)
    {
        Event* newEvent2 = (Event*)malloc(sizeof(Event));
		*newEvent2 = *newEvent;
        newEvent2->actorToFire = this_model_delay->nextActor2;
        event_add(newEvent2);
    }

    return;
}

void processEvents()
{

    //  To ensure this function is thread safe, we make sure only one processEvents() IS BEING EXECUTED AT A TIME
    //  WHILE ANOTHER SENSOR MIGHT TRIGGER ANOTHER processEvents()
    //  IF ANOTHER processEvents() is currently being executed, we don't really need to have another being executed at the same time...
	Actor* a;

    while (1) {
        disableInterrupt();
        // If event queue is not empty.
        if (EVENT_QUEUE_HEAD != NULL) {
            Event* event = EVENT_QUEUE_HEAD;
            long processTime = safeToProcess(event);
            if (getCurrentPhysicalTime() >= processTime) {
                event_remove();
                enableInterrupt();
                // Execute the event. During
                // this process more events may
                // be posted to the queue.
                fire_actor(event);
				free(event);

                // Check which actor produced the
                // event that we have processed
                a = event->actorFrom;
                // If the event just executed is
                // produced by a source actor
                if (a->sourceActor != 0) {
                    // Decrement the buffer size
                    // by one.
					if (a == SOURCE1) {
                    	CK1_BUF_SIZE--;
					}
                    // Make sure sourceBuffer is
                    // non-empty. If it is, fire
                    // the source actor once to
                    // produce some events.
                    if (CK1_BUF_SIZE == 0) {
						//dummyEVent is never used.
						Event* dummyEvent;
                        clock_fire(SOURCE1, dummyEvent);
						// firing of the actor should update the number of events in the buffer
                    }
                }
            }
            else {
                // There are no events safe to
                // process, so setup sources
                // to process.
                STOP_SOURCE_PROCESS = FALSE;
                // Set timed interrupt to run
                // the event at the top of the
                // event queue when physical time
                // has passed for it to be
                // safe to process
                setTimedInterrupt(processTime);
                enableInterrupt();
            }
        } else {
            // There are no events safe to
            // process, so setup sources
            // to process.
            STOP_SOURCE_PROCESS = FALSE;
            enableInterrupt();
        }
        // If there is no event to process and we
        // have not reached the buffer limit, fire
        // the source actor.
        // for (each source actors a) {
        if (STOP_SOURCE_PROCESS == FALSE) {
			if (CK1_BUF_SIZE < MAX_BUFFER_LIMIT) {
				Event* dummyEvent;
            	clock_fire(SOURCE1, dummyEvent);
			}
        }
    }
} 

/** 
 * This is the initialization method for sensor actor.
 * Initialize the sensor actor to setup receiving function through another 
 * thread, it then create a new event and calls processEvent()
 */
void sensor_init(Actor* this_sensor)     //this listens for the next signal to sense again
{
	long time;
    Event* newEvent = (Event*)malloc(sizeof(Event));
    //get the current time
    time = getCurrentPhysicalTime();

    // Set a time trigger from now

    newEvent->Tag.timestamp = time;
    newEvent->Tag.microstep = 0;	//FIXEM
    newEvent->thisValue.doubleValue = time;
    newEvent->actorToFire = this_sensor->nextActor1;
    newEvent->actorFrom = this_sensor;

    //now put the created event into the queue
    //BUT HOW DO I TRACK THE START OF THE EVENT QUEUE??? --global variable
    event_add(newEvent);

    if (this_sensor->nextActor2 != NULL)
    {
        Event* newEvent2 = (Event*)malloc(sizeof(Event));
		*newEvent2 = *newEvent;
        newEvent2->actorToFire = this_sensor->nextActor2;
        event_add(newEvent2);
    }

    return;
}

/** 
 * Static timing analysis is called to set the timestamp of the event by a 
 * specific amount in order to fire the event at an instance that ensures 
 * all events are exectued in order.
 * In this analysis, the clock event can be executed when real time exceeds
 * tau - model_delay3
 */
long safeToProcess(Event* thisEvent) {

    long safeTimestamp = thisEvent->Tag.timestamp;

	if (thisEvent->actorToFire->multipleInputs != 0) {
	    if (thisEvent->actorToFire->fire_method == merge_fire)
		{
			safeTimestamp -= MERGE_MODEL_TIME_ADJUSTMENT; 
		} else if (thisEvent->actorToFire->fire_method == computation_fire)
	    {        
	    	safeTimestamp -= COMPUTATION_MODEL_TIME_ADJUSTMENT;
		}
    } else {
//        printf("always safe to execute, so set safeTag to 0\n");
        safeTimestamp = 0;
    }

    return safeTimestamp;
}

void setActuationInterrupt(long actuationTime) {
	return;
}

void setTimedInterrupt(long safeToProcessTime) {
	return;
}

long getCurrentPhysicalTime() {
	return 0;
}

//*****************************************************************************
//
// The UART interrupt handler.
//
//*****************************************************************************
void
UARTIntHandler(void)
{
    unsigned long ulStatus;
	
	diableInterrupt();

	Event* dummyEvent;
    clock_fire(SOURCE1, dummyEvent);

    //
    // Get the interrrupt status.
    //
    ulStatus = UARTIntStatus(UART0_BASE, true);
    //
    // Clear the asserted interrupts.
    //
    UARTIntClear(UART0_BASE, ulStatus);

	STOP_SOURCE_PROCESS = TRUE;
}

//FIXME: which interrupt(s) should I disable/enable?
void disableInterrupt() {
	IntDisable(INT_UART0);
	UARTIntDisable(UART0_BASE, UART_INT_RX | UART_INT_RT);
	return;
}

void enableInterrupt() {
    IntEnable(INT_UART0);
    UARTIntEnable(UART0_BASE, UART_INT_RX | UART_INT_RT);
	return;
} 


/**
 * A thread is setup to keep listening for timed interrupt.
 * This scheme makes sure that no interrupts are missed.
 * Asumming: if real time has passed the timed trigger, the timebomb would still explode.
 */
/*
void timed_interrupt_init(){
    // Block until the next interrupt
    unsigned int status;
    printf("\nSTART LISTENING FOR INTERRUPTS.\n\n");
    while (1) {
        do {
            //HOW DO I PASS THE fd TO THIS FUNCTION? --> by global variable
            int num = read( fd, &status, sizeof(status));
            if (num != sizeof( status))
            {
                fprintf(stderr, "Error reading status, %d\n", num);
                die();
            }
        } while ((status & TIMEBOMB_0_FIRE) == 0); // Got it!
        
        printf("wake up man! I got an event to execute!!\n");
        pthread_t  p_thread;
        //Time has passed to allow safe execution of events on event queue.
        int thr_id;
        //FIXME FIXME FIXME: after a while I just can't create anymore threads???
        thr_id = pthread_create(&p_thread, NULL, (void*)processEvents, (void*)NULL);

    }
}
*/
/**
 * main() has the following functionalities:
 * declare all actors
 * setup dependencies between the actors
 * intialize actors
 */
int main(int argc, char *argv[])
{

//    GLOBAL_SERVER = argv[1];
//    GLOBAL_PORT1 = argv[2];
//    GLOBAL_PORT2 = argv[3];

    //**************************************************
    //Declare all actors
    Actor sensor1;
	Actor sensor2;
    Actor clock1;
	Actor computation1;
	Actor computation2;
	Actor computation3;
    Actor merge1;
    Actor model_delay1;
	Actor model_delay2;
    Actor model_delay3;
    Actor actuator1;

	/*************************************************************************/
	// Utilities to setup interrupts
    //
    // Enable the peripherals used by this example.
    //
    SysCtlPeripheralEnable(SYSCTL_PERIPH_UART0);
    SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIOA);
	//
    // Enable processor interrupts.
    //
    IntMasterEnable();	
    //
    // Set GPIO A0 and A1 as UART pins.
    //
    GPIOPinTypeUART(GPIO_PORTA_BASE, GPIO_PIN_0 | GPIO_PIN_1);
	//
    // Configure the UART for 115,200, 8-N-1 operation. 
	// FIXME: don't know what this is for...
    //
    UARTConfigSetExpClk(UART0_BASE, SysCtlClockGet(), 115200,
                        (UART_CONFIG_WLEN_8 | UART_CONFIG_STOP_ONE |
                         UART_CONFIG_PAR_NONE));
    //
    // Enable the UART interrupt.
    //
    IntEnable(INT_UART0);
    UARTIntEnable(UART0_BASE, UART_INT_RX | UART_INT_RT);
	/*************************************************************************/

    if (argc != 5) {
        fprintf(stderr, "USAGE: %s <server_ip> <port1> <port2> <s or a>\n", argv[0]);
        //s stands for sensor side
        //a stands for actuator side
        die("");
    }

	SOURCE1 = &clock1;

	clock1.fire_method = clock_fire;

	computation1.fire_method = computation_fire;

	computation2.fire_method = computation_fire;

	computation3.fire_method = computation_fire;

	model_delay1.fire_method = model_delay_fire;

	model_delay2.fire_method = model_delay_fire;

	model_delay3.fire_method = model_delay_fire;

	merge1.fire_method = merge_fire;

	actuator1.fire_method = actuator_fire;
  
    //Dependencies between all the actors
	//FIXME: HOW ARE PORTS NUMBERED??
    sensor1.nextActor1 = &computation1;
    sensor1.nextActor2 = NULL;
    computation1.nextActor1 = &model_delay1;
    computation1.nextActor2 = NULL;
	model_delay1.nextActor1 = &merge1;
	model_delay1.nextActor2 = NULL;

    sensor2.nextActor1 = &computation2;
    sensor2.nextActor2 = NULL;
    computation2.nextActor1 = &model_delay2;
    computation2.nextActor2 = NULL;
	model_delay2.nextActor1 = &merge1;
	model_delay2.nextActor2 = NULL;
    
    clock1.nextActor1 = &computation3;
    clock1.nextActor2 = NULL;
    computation3.nextActor1 = &merge1;
    computation3.nextActor2 = NULL;
    
    merge1.nextActor1 = &actuator1;
    merge1.nextActor2 = NULL;

    actuator1.nextActor1 = NULL;
    actuator1.nextActor2 = NULL;
    
    //initialize all actors
    //if a, it's an actuator
    //if s, it's a sensor
    //if neither, error
/*    pthread_t  p_thread;
    //Time has passed to allow safe execution of events on event queue.
    int thr_id;
    thr_id = pthread_create(&p_thread, NULL, (void*)timed_interrupt_init, (void*)NULL);
*/
    //initializeAllActors();

    if (*argv[4] == 's'){
        //sensor_init is used to startup the system by sending an event.
        sensor_init(&sensor1);
    }
    else if (*argv[4] == 'a'){
        //sensor_run is used to listen for events that comes from the sensor platform.
		clock_init(&clock1);
        sensor_init(&sensor2);
    }
    else
        printf("didn't not specify if this is an actuator or sensor!!!\n");

    //go idle
    processEvents();
    
//    close(fd);

    return 0;
}

//  /* Set the clocking to run from PLL at 50 MHz */
//  SysCtlClockSet(SYSCTL_SYSDIV_4 | SYSCTL_USE_PLL | SYSCTL_OSC_MAIN | SYSCTL_XTAL_8MHZ);
//
//  /* Enable peripherals */
//  SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIOE);
//  SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIOF);
//
//  /* Configure Status LED as output */
//  GPIOPadConfigSet(GPIO_PORTF_BASE, LED,    GPIO_STRENGTH_2MA, GPIO_PIN_TYPE_STD);
//  GPIODirModeSet  (GPIO_PORTF_BASE, LED,    GPIO_DIR_MODE_OUT);
//
//  /* Configure push buttons as inputs */
//  GPIOPadConfigSet(GPIO_PORTE_BASE, BUTTON, GPIO_STRENGTH_2MA, GPIO_PIN_TYPE_STD_WPU);
//  GPIODirModeSet  (GPIO_PORTE_BASE, BUTTON, GPIO_DIR_MODE_IN);
//  GPIOPadConfigSet(GPIO_PORTF_BASE, SELECT, GPIO_STRENGTH_2MA, GPIO_PIN_TYPE_STD_WPU);
//  GPIODirModeSet  (GPIO_PORTF_BASE, SELECT, GPIO_DIR_MODE_IN);
//
//  /* Initialize LCD */
//  RIT128x96x4Init(1000000);
//  RIT128x96x4Clear();
//
//  /* Display initial message */
//  sprintf(buf, "      Keil Demo      ");
//  RIT128x96x4StringDraw(buf, 0,      6, 11);
//  sprintf(buf, "     EK-LM3S8962     ");
//  RIT128x96x4StringDraw(buf, 0, 10*1+6, 11);
//  sprintf(buf, "   Blinky Example    ");
//  RIT128x96x4StringDraw(buf, 0, 10*2+6, 11);
//  sprintf(buf, "  Time =             ");
//  RIT128x96x4StringDraw(buf, 0, 10*4+6, 11);
//  sprintf(buf, "  Button Up:         ");
//  RIT128x96x4StringDraw(buf, 0, 10*5+8, 11);
//  sprintf(buf, "  Button Down:       ");
//  RIT128x96x4StringDraw(buf, 0, 10*6+8, 11);
//  sprintf(buf, "  Button Left:       ");
//  RIT128x96x4StringDraw(buf, 0, 10*7+8, 11);
//  sprintf(buf, "  Button Right:      ");
//  RIT128x96x4StringDraw(buf, 0, 10*8+8, 11);
//
//  /* Setup and enable SysTick with interrupt (100Hz) */
//  SysTickPeriodSet(SysCtlClockGet() / 100);
//  SysTickEnable();
//  SysTickIntEnable();
//
//  time    = ~Time;
//  buttons = ~Buttons;
//
//  /* Endless Loop */
//  while (1) {
//    if (time != Time) {
//      time = Time;
//      sprintf(buf, "%1.2f s", (float)time / 100);
//      RIT128x96x4StringDraw(buf,                          9*6, 10*4+6, 11);
//    }
//    b = Buttons;
//    if ((buttons ^ b) & UP) {
//      RIT128x96x4StringDraw((b & UP)    ? "Off" : "On ", 16*6, 10*5+8, 11);
//    }
//    if ((buttons ^ b) & DOWN) {
//      RIT128x96x4StringDraw((b & DOWN)  ? "Off" : "On ", 16*6, 10*6+8, 11);
//    }
//    if ((buttons ^ b) & LEFT) {
//      RIT128x96x4StringDraw((b & LEFT)  ? "Off" : "On ", 16*6, 10*7+8, 11);
//    }
//    if ((buttons ^ b) & RIGHT) {
//      RIT128x96x4StringDraw((b & RIGHT) ? "Off" : "On ", 16*6, 10*8+8, 11);
//    }
//    buttons = b;
//  }
