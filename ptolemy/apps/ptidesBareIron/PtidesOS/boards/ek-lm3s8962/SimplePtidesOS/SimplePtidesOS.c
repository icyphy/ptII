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
#include <stdlib.h>
#include<assert.h>

#include "../../../hw_ints.h"
#include "../../../hw_memmap.h"
#include "../../../hw_types.h"
#include "../../../src/debug.h"
#include "../../../src/gpio.h"
#include "../../../src/interrupt.h"
#include "../../../src/sysctl.h"
#include "../../../src/uart.h"
#include "../rit128x96x4.h"
#include "../../../src/timer.h"
#include "../../../src/systick.h"
#include "../../../hw_nvic.h"


#include "structures.h"
#include "functions.h"
#include "actors.h"



//#include "timer.h"
//#include "clock-arch.h"

/* Status LED and Push Buttons pin definitions */
#define LED             GPIO_PIN_0 /* PF0 */
#define SELECT          GPIO_PIN_1 /* PF1 */
#define UP              GPIO_PIN_0 /* PE0 */
#define DOWN            GPIO_PIN_1 /* PE1 */
#define LEFT            GPIO_PIN_2 /* PE2 */
#define RIGHT           GPIO_PIN_3 /* PE3 */
#define BUTTON         (UP | DOWN | LEFT | RIGHT)

/* Global variables */
volatile unsigned char Tick;    // Tick Counter (0..99)	  // what's the difference between a time counter and a tick counter?
volatile unsigned long Time;    // Time Counter (10ms)
volatile unsigned char Buttons; // Button States
volatile unsigned char TraceB;  // Trace Buttons
Event eventMemory[35];

unsigned int locationCounter;

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


//*****************************************************************************
//
// Flags that contain the current value of the interrupt indicator as displayed
// on the OLED display.
//
//*****************************************************************************
unsigned long g_ulFlags;

//****************************************************************************
//
// Real-time and model-time variables
//
//unsigned long eightmicroseconds;	 // microseconds
unsigned long seconds; // in seconds (currently set to be twice as fast as real-time)
unsigned long systickseconds;
unsigned long tenthofsecond;

//*****************************************************************************
//
// The count of interrupts received.  This is incremented as each interrupt
// handler runs, and its value saved into interrupt handler specific values to
// determine the order in which the interrupt handlers were executed.
//
//*****************************************************************************
volatile unsigned long g_ulIndex;

//*****************************************************************************
//
// The value of g_ulIndex when the INT_GPIOA interrupt was processed.
//
//*****************************************************************************
volatile unsigned long g_ulGPIOa;

//*****************************************************************************
//
// The value of g_ulIndex when the INT_GPIOB interrupt was processed.
//
//*****************************************************************************
volatile unsigned long g_ulGPIOb;

//*****************************************************************************
//
// The value of g_ulIndex when the INT_GPIOC interrupt was processed.
//
//*****************************************************************************
volatile unsigned long g_ulGPIOc;





//*****************************************************************************
//
// The error routine that is called if the driver library encounters an error.
//
//*****************************************************************************
#ifdef DEBUG
void
__error__(char *pcFilename, unsigned long ulLine)
{
}
#endif


 //implementation of itoa from (http://www.jb.man.ac.uk/~slowe/cpp/itoa.html)
char* itoa(int val, int base){
	
	static char buf[32] = {0};
	
	int i = 30;
	
	for(; val && i ; --i, val /= base)
	
		buf[i] = "0123456789abcdef"[val % base];
	
	return &buf[i+1];
	
}


 //*****************************************************************************
//
// Display the interrupt state on the LCD.  The currently active and pending
// interrupts are displayed.
//
//*****************************************************************************
// mthomas: added static
static void DisplayIntStatus(void)
{
    unsigned long ulTemp;
    char pcBuffer[4];

    //
    // Display the currently active interrupts.
    //
    ulTemp = HWREG(NVIC_ACTIVE0);
    pcBuffer[0] = (ulTemp & 1) ? '1' : ' ';
    pcBuffer[1] = (ulTemp & 2) ? '2' : ' ';
    pcBuffer[2] = (ulTemp & 4) ? '3' : ' ';
    pcBuffer[3] = '\0';
	RIT128x96x4Clear();
    RIT128x96x4StringDraw(pcBuffer, 12,16,15);
	
    //
    // Display the currently pending interrupts.
    //
    ulTemp = HWREG(NVIC_PEND0);
    pcBuffer[0] = (ulTemp & 1) ? '1' : ' ';
    pcBuffer[1] = (ulTemp & 2) ? '2' : ' ';
    pcBuffer[2] = (ulTemp & 4) ? '3' : ' ';
    RIT128x96x4StringDraw(pcBuffer, 12,24,15);
}


//*****************************************************************************
//
// Delay for the specified number of seconds.  Depending upon the current
// SysTick value, the delay will be between N-1 and N seconds (i.e. N-1 full
// seconds are guaranteed, along with the remainder of the current second).
//
//*****************************************************************************
// mthomas: added static 
static void Delay(unsigned long ulSeconds)
{
    //
    // Loop while there are more seconds to wait.
    //
    while(ulSeconds--)
    {
        //
        // Wait until the SysTick value is less than 1000.
        //
        while(SysTickValueGet() > 1000)
        {
        }

        //
        // Wait until the SysTick value is greater than 1000.
        //
        while(SysTickValueGet() < 1000)
        {
        }
    }
}



//*****************************************************************************
//
// The UART interrupt handler.
//
//*****************************************************************************
/*void
UARTIntHandler(void)
{
    unsigned long ulStatus;

    //
    // Get the interrrupt status.
    //
    ulStatus = UARTIntStatus(UART0_BASE, true);

    //
    // Clear the asserted interrupts.
    //
    UARTIntClear(UART0_BASE, ulStatus);

    //
    // Loop while there are characters in the receive FIFO.
    //
    while(UARTCharsAvail(UART0_BASE))
    {
        //
        // Read the next character from the UART and write it back to the UART.
        //
        UARTCharPutNonBlocking(UART0_BASE, UARTCharGetNonBlocking(UART0_BASE));
    }
}*/

//*****************************************************************************
//
// The interrupt handler for the SystemTick
//
//*****************************************************************************
void SysTickHandler(void)
{
  //what should I do to clear the SysTick Interrupt?
  //does it not need to be cleared?

  //RIT128x96x4StringDraw("in systick handler",            12,  0, 15);
  
   systickseconds++;

}

//*****************************************************************************
//
// The interrupt handler for the first timer interrupt.
//
//*****************************************************************************
// mthomas: attribute for stack-aligment (see README_mthomas.txt)
//#ifdef __GNUC__
//void Timer0IntHandler(void) __attribute__((__interrupt__));
//#endif
void Timer0IntHandler(void)
{
    //
    // Clear the timer interrupt.
    //
    TimerIntClear(TIMER0_BASE, TIMER_TIMA_TIMEOUT);
//	RIT128x96x4Clear();

    //
    // Toggle the flag for the first timer.
    //
    HWREGBITW(&g_ulFlags, 0) ^= 1;

    //
    // Update the interrupt status on the display.
    //
    IntMasterDisable();
	//eightmicroseconds++;
  	if(tenthofsecond == 10)
  	{
   		seconds++;	/// if seconds are incremented here we'll have to limit the granularity
   		tenthofsecond = 0;
  	}

/*	nanoseconds++;
	if(nanoseconds == 100000000)
	{
	seconds ++;
	nanoseconds = 0;
	RIT128x96x4StringDraw("seconds ",        12, 36, 15);
	RIT128x96x4StringDraw(itoa(seconds,10),  50, 36, 15);
	}	*/
    //RIT128x96x4StringDraw(HWREGBITW(&g_ulFlags, 0) ? "1" : "0",        12, 36, 15);
	
    IntMasterEnable();
}

//*****************************************************************************
//
// The interrupt handler for the second timer interrupt.
//
//*****************************************************************************
// mthomas: attribute for stack-aligment (see README_mthomas.txt)
//#ifdef __GNUC__
//void Timer1IntHandler(void) __attribute__((__interrupt__));
//#endif
void Timer1IntHandler(void)
{
    //
    // Clear the timer interrupt.
    //
    TimerIntClear(TIMER1_BASE, TIMER_TIMA_TIMEOUT);
    // RIT128x96x4Clear();
    //
    // Toggle the flag for the second timer.
    //
    HWREGBITW(&g_ulFlags, 1) ^= 1;

    //
    // Update the interrupt status on the display.
    //
  //  IntMasterDisable();
//	modeltime++;
    //RIT128x96x4StringDraw(HWREGBITW(&g_ulFlags, 1) ? "1" : "0",        12, 48, 15);
//	RIT128x96x4StringDraw("modeltime ",        12, 48, 15);
//	RIT128x96x4StringDraw(itoa(modeltime,10),  50, 48, 15);
    //IntMasterEnable();
}


//*****************************************************************************
//
// This is the handler for INT_GPIOA.  It simply saves the interrupt sequence
// number.
//
//*****************************************************************************
void IntGPIOa(void)
{
    //
    // Set PB0 high to indicate entry to this interrupt handler.
    //
    GPIOPinWrite(GPIO_PORTD_BASE, GPIO_PIN_0, GPIO_PIN_0);

    //
    // Put the current interrupt state on the LCD.
    //
    DisplayIntStatus();

    //
    // Wait two seconds.
    //
    Delay(2);

    //
    // Save and increment the interrupt sequence number.
    //
    g_ulGPIOa = g_ulIndex++;

    //
    // Set PB0 low to indicate exit from this interrupt handler.
    //
    GPIOPinWrite(GPIO_PORTD_BASE, GPIO_PIN_0, 0);
}

//*****************************************************************************
//
// This is the handler for INT_GPIOB.  It triggers INT_GPIOA and saves the
// interrupt sequence number.
//
//*****************************************************************************

void IntGPIOb(void)
{
    //
    // Set PB1 high to indicate entry to this interrupt handler.
    //
    GPIOPinWrite(GPIO_PORTD_BASE, GPIO_PIN_1, GPIO_PIN_1);

    //
    // Put the current interrupt state on the LCD.
    //
    DisplayIntStatus();

    //
    // Trigger the INT_GPIOA interrupt.
    //
    HWREG(NVIC_SW_TRIG) = INT_GPIOA - 16;

    //
    // Put the current interrupt state on the LCD.
    //
    DisplayIntStatus();

    //
    // Wait two seconds.
    //
    Delay(2);

    //
    // Save and increment the interrupt sequence number.
    //
    g_ulGPIOb = g_ulIndex++;

    //
    // Set PB1 low to indicate exit from this interrupt handler.
    //
    GPIOPinWrite(GPIO_PORTD_BASE, GPIO_PIN_1, 0);
}

//*****************************************************************************
//
// This is the handler for INT_GPIOC.  It triggers INT_GPIOB and saves the
// interrupt sequence number.
//
//*****************************************************************************
void IntGPIOc(void)
{
    //
    // Set PB2 high to indicate entry to this interrupt handler.
    //
    GPIOPinWrite(GPIO_PORTD_BASE, GPIO_PIN_2, GPIO_PIN_2);

    //
    // Put the current interrupt state on the LCD.
    //
    DisplayIntStatus();

    //
    // Trigger the INT_GPIOB interrupt.
    //
    HWREG(NVIC_SW_TRIG) = INT_GPIOB - 16;

    //
    // Put the current interrupt state on the LCD.
    //
    DisplayIntStatus();

    //
    // Wait two seconds.
    //
    Delay(2);

    //
    // Save and increment the interrupt sequence number.
    //
    g_ulGPIOc = g_ulIndex++;

    //
    // Set PB2 low to indicate exit from this interrupt handler.
    //
    GPIOPinWrite(GPIO_PORTD_BASE, GPIO_PIN_2, 0);
}





//*****************************************************************************
//
// Send a string to the UART.
//
//*****************************************************************************
void UARTSend(const unsigned char *pucBuffer, unsigned long ulCount)
{
    //
    // Loop while there are more characters to send.
    //
    while(ulCount--)
    {
        //
        // Write the next character to the UART.
        //
        UARTCharPutNonBlocking(UART0_BASE, *pucBuffer++);
    }
}

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
void fireActuator(Actor* this_actuator, Event* thisEvent) 
{
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
 * This function should only be called when the interrupt has been disabled
 */
unsigned int alreadyFiring(Actor* actor) {
	return actor->firing;
}

/**
 * This function is basically the same as clock_init(), expect only this 
 * function is called through an event, thus we use addEvent() instead 
 * of event_insert() in this case.
 */
void fireClock(Actor* this_clock, Event* thisEvent) {

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

    addEvent(newEvent);
	CK1_BUF_SIZE++;

    if (this_clock->nextActor2 != NULL) {
        Event* newEvent2 = (Event*) malloc(sizeof(Event));
		*newEvent2 = *newEvent;
        newEvent2->actorToFire = this_clock->nextActor2;
        addEvent(newEvent2);
    }

    //Do not need to call processEvents() in this case
    //since processEvents() called fireClock
    return;
}

/** 
 * This is the initilization for the clock actor.
 * its functionality is to provide events with intervals specified by 
 * CLOCK_PERIOD_SECS/NSECS.
 * At one time, CLOCK_EVNTS number of events are produced at a time, and they 
 * can be executed momentarily.
 * when the deadline of the last event that was produced has passed
 * CLOCK_EVENTS number of events are again produced by a event added into the event queue
 */
void initClock(Actor *this_clock)
{
    long currentTime = getCurrentPhysicalTime();

    Event* myNewEvent = newEvent();				/// I don't know if malloc is defined

    //CLOCK_PERIOD specifies the period
    currentTime += CLOCK_PERIOD;   

    //notice orderTag and realTag are set to different things:
    //since the event queue is ordered by orderTag, we these events to execute NOW
    //however realTag is the real timestamp to appear at the output
    myNewEvent->Tag.timestamp = currentTime;
    myNewEvent->Tag.microstep = 0;   //FIXME
    myNewEvent->thisValue.doubleValue = 0;
    myNewEvent->actorToFire = this_clock->nextActor1;
    myNewEvent->actorFrom = this_clock;

    addEvent(myNewEvent);

	CK1_TIME = currentTime;

    if (this_clock->nextActor2 != NULL)
    {
        Event* newEvent2 = newEvent();
		//*newEvent2 = *newEvent;    //Jia I'm  not sure what this line was attempting to do
        newEvent2->actorToFire = this_clock->nextActor2;
        addEvent(newEvent2);
    }
}

/**
 * Set the firing flag of the actor, indicate that the actor is currenting being fired.
 */
void currentlyFiring(Actor* actor) 
{
	actor->firing = 1;
}

/** 
 * This is the fire method for computation.
 * Computation method do not have any real functionality here, where we simply
 * set our event queue to fire the next actor.
 */
void fireComputation(Actor* this_computation, Event* thisEvent) 
{

	double thisDouble;
	Event* myNewEvent = newEvent();
        
	thisDouble = thisEvent->thisValue.doubleValue;
    thisDouble++;

	myNewEvent->thisValue.doubleValue = thisDouble;
    myNewEvent->actorToFire = this_computation->nextActor1;
    myNewEvent->actorFrom = this_computation;

    //arbitrarily delay either 1/4 of a sec or 1/2 of a sec or none.
	addEvent(myNewEvent);

    if (this_computation->nextActor2 != NULL)
    {
        Event* newEvent2 = (Event*) malloc(sizeof(Event));
		//*newEvent2= *newEvent;   //Jia I'm not sure what this line is supposed to do
        newEvent2->actorToFire = this_computation->nextActor2;
        addEvent(newEvent2);
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
 * The method addEvent() is only used when an actor that's not the source 
 * within a platform.
 * In this case there's no need to lock the event queue since processEvents() 
 * only execute one event at a time.
 * When events are added, make sure the event queue is ordered by orderTag of the events.
 * Currently, we simply go through the queue one event at a time and check the tags.
 * FIXME: use either binary search or calendar to improve performance.
 */
void addEvent(Event* newEvent)
{

    Tag stampedTag = newEvent->Tag;

    //add an event
    Event *compare_event = EVENT_QUEUE_HEAD;
    Event *before_event  = EVENT_QUEUE_HEAD;
    //move across the link until we find an event with larger tag, or stop at the end.

    while (1)
	{
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
    if (compare_event == before_event)
	{
        EVENT_QUEUE_HEAD = newEvent;
    }
    else if (compare_event != before_event)
	{
        before_event->next = newEvent;
    }
    else {

        die("");
    }

}

/** 
 * removeEvent() is called to pop the most recent event in the event queue.
 * it is not interlocked because it can only be called by sinks within a 
 * platform, which is the called by processEvents().
 * Here the first event on the event queue is popped.
 */
void removeEvent()
{
    if (EVENT_QUEUE_HEAD != NULL)
	{
		//should I call freeEvent(EVENT_QUEUE_HEAD) here? I'm not sure.
		freeEvent(EVENT_QUEUE_HEAD);
        EVENT_QUEUE_HEAD = EVENT_QUEUE_HEAD -> next;
    } 
    else printf("event queue is already empty\n");
}

/** 
 * execute_event() checks if the event is valid. If it is, then fire actor
 * is called.
 */
void executeEvent(){

    if (EVENT_QUEUE_HEAD == NULL) 
	{
        die("EVENT_QUEUE_HEAD should never be NULL\n");
    }
    else 
	{
        if (EVENT_QUEUE_HEAD->actorToFire == NULL)
		{
            die("executing an event where the actors are NULL!!\n");
        }
        else 
		{
            fireActor(EVENT_QUEUE_HEAD);
        }
    }
}

/** 
 * fire_actor checks if static timing analysis is needed.
 * if it is, static timing analysis is called, and returns
 * if it's not, firing method of the actor specified by the event is called
 */
void fireActor(Event* currentEvent)
{

    Actor* fire_this = currentEvent->actorToFire;
	//FIXME: USE THIS INSTEAD!! char temp_type[3] = fire_this->type;
	if (fire_this->fireMethod != NULL) {
		(fire_this->fireMethod)(fire_this, currentEvent);
	} else {
		die("no such method, cannot fire\n");
	}
	fire_this->firing = 0;

    return;
}

/** 
 * This is the fire method for merge actor.
 * This firing method transfer the event at the input and put it to the output.
 * It also prints the current timestamp, and which actor it is from.
 * Since it is not a terminating actor, no need to pop event.
 * Only possibility to add event is when it has more than one output.
 * A merge actor may have more than one input.
 */
void fireMerge(Actor* this_merge, Event* thisEvent) {

//    printf("THIS IS THE FINAL OUTPUT OF THE MERGE Actor: \n");
//    printf("MAKE SURE THE TagS OF THESE EVENTS ARE IN ORDER: the tag on the current value are: %.9d.%9.9d %i \n", 
//		thisEvent->realTag.secs, thisEvent->Tag.nsecs, thisEvent->Tag.microstep);
//    printf("THIS OUTPUT WAS FROM Actor: %c%c%c\n", 
//		thisEvent->actorFrom->type[0], thisEvent->actorFrom->type[1], thisEvent->actorFrom->type[2]);

	Event* myNewEvent = newEvent();
	*myNewEvent = *thisEvent;	    // why is this assignment done?
    myNewEvent->actorToFire = this_merge->nextActor1;
    myNewEvent->actorFrom = this_merge;

	addEvent(myNewEvent);

    if (this_merge->nextActor2 != NULL)
    {
        Event* newEvent2 = newEvent();
		//*newEvent2 = *newEvent;     //Jia, I'm not sure what this is supposed to do
        newEvent2->actorToFire = this_merge->nextActor2;
        addEvent(newEvent2);
    }
}

/** 
 * This is the fire method for model_delay actor.
 * model_delay actor increase the current realTag timestamp by the the 
 * parameter MODEL_DELAY_SECS, where MODEL_DELAY_SECS is greater or equal 
 * to maximum delay from the source of the event to the sink.
 * At the same time, the orderTag is increased by BOUNDED_DELAY, since
 * this is the delay between the sensor actor and the merge actor.
 * Since timestamps are modified in this process, event_pop() and addEvent() 
 * needs to be called.
 * Because model_delay is also a transmitter through the network, socket
 * programming is used.
 */
void fireModelDelay(Actor* this_model_delay, Event* thisEvent) {

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

	Event* myNewEvent = newEvent();
		
	myNewEvent->Tag.timestamp = thisEvent->Tag.timestamp + model_delay;
    myNewEvent->Tag.microstep = 0;   //FIXME
    myNewEvent->actorToFire = this_model_delay->nextActor1;
    myNewEvent->actorFrom = this_model_delay;

	addEvent(myNewEvent);
    if (this_model_delay->nextActor2 != NULL)
    {
        Event* newEvent2 = newEvent();
		//*newEvent2 = *newEvent;    // Jia, I'm not sure what this is supposed to do
        newEvent2->actorToFire = this_model_delay->nextActor2;
        addEvent(newEvent2);
    }
}

void processEvents()
{
   
    //  To ensure this function is thread safe, we make sure only one processEvents() IS BEING EXECUTED AT A TIME
    //  WHILE ANOTHER SENSOR MIGHT TRIGGER ANOTHER processEvents()
    //  IF ANOTHER processEvents() is currently being executed, we don't really need to have another being executed at the same time...
	Actor* actor;
	RIT128x96x4StringDraw("beginningPE", 12,72,15);
    while (1) 
	{
        disableInterrupts();
        // If event queue is not empty.
        if (EVENT_QUEUE_HEAD != NULL) {
            Event* event = EVENT_QUEUE_HEAD;
            // Check which actor produced the event that we have processed
			actor = event->actorFrom;
			if (alreadyFiring(actor))
			 {
				enableInterrupts();
				break;
			} 
			else 
			{
	            long processTime = safeToProcess(event);
	            if (getCurrentPhysicalTime() >= processTime&&(1)) 
				 // (1) should be replaced with a check to see if this is the highest priority event
				 {
	                removeEvent();
	                enableInterrupts();
	                // Execute the event. During
	                // this process more events may
	                // be posted to the queue.
	                fireActor(event);
					free(event);
	                // If the event just executed is
	                // produced by a source actor
	                if (actor->sourceActor != 0) {
	                    // Decrement the buffer size
	                    // by one.
						if (actor == SOURCE1) {
	                    	CK1_BUF_SIZE--;
						}
	                    // Make sure sourceBuffer is
	                    // non-empty. If it is, fire
	                    // the source actor once to
	                    // produce some events.
	                    if (CK1_BUF_SIZE == 0) {
							//dummyEVent is never used.
							Event* dummyEvent;
	                        fireClock(SOURCE1, dummyEvent);
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
	                enableInterrupts();
	            }
			}
        } else {
            // There are no events safe to
            // process, so setup sources
            // to process.
            STOP_SOURCE_PROCESS = FALSE;
            enableInterrupts();
        }
        // If there is no event to process and we
        // have not reached the buffer limit, fire
        // the source actor.
        // for (each source actors a) {
        if (STOP_SOURCE_PROCESS == FALSE) {
			if (CK1_BUF_SIZE < MAX_BUFFER_LIMIT) {
				Event* dummyEvent;
            	fireClock(SOURCE1, dummyEvent);
			}
        }
    }
} 

/** 
 * This is the initialization method for sensor actor.
 * Initialize the sensor actor to setup receiving function through another 
 * thread, it then create a new event and calls processEvent()
 */
void initSensor(Actor* this_sensor)     //this listens for the next signal to sense again
{
	long time;
    Event* myNewEvent;
 //RIT128x96x4StringDraw("start",   12,72,15);


	myNewEvent = newEvent();

//RIT128x96x4StringDraw("begin",   40,72,15);


    //get the current time
    time = getCurrentPhysicalTime();

    // Set a time trigger from now

    myNewEvent->Tag.timestamp = time;
    myNewEvent->Tag.microstep = 0;	//FIXEM
    myNewEvent->thisValue.doubleValue = time;
    myNewEvent->actorToFire = this_sensor->nextActor1;
    myNewEvent->actorFrom = this_sensor;

    //now put the created event into the queue
    //BUT HOW DO I TRACK THE START OF THE EVENT QUEUE??? --global variable
    addEvent(myNewEvent);

    if (this_sensor->nextActor2 != NULL)
    {
        Event* newEvent2 = newEvent();
		//*newEvent2 = *newEvent;     //Jia, I'm nto wure what this is for
        newEvent2->actorToFire = this_sensor->nextActor2;
        addEvent(newEvent2);
    }

//		RIT128x96x4StringDraw("end",   70,72,15);

 }

/** 
 * Static timing analysis is called to set the timestamp of the event by a 
 * specific amount in order to fire the event at an instance that ensures 
 * all events are exectued in order.
 * In this analysis, the clock event can be executed when real time exceeds
 * tau - model_delay3
 */
long safeToProcess(Event* thisEvent) 
{
    long safeTimestamp = thisEvent->Tag.timestamp;

	if (thisEvent->actorToFire->multipleInputs != 0) 
	{
	    if (thisEvent->actorToFire->fireMethod == fireMerge)
		{
			safeTimestamp -= MERGE_MODEL_TIME_ADJUSTMENT; 
		} 
		else if (thisEvent->actorToFire->fireMethod == fireComputation)
	    {        
	    	safeTimestamp -= COMPUTATION_MODEL_TIME_ADJUSTMENT;
		}
    }
	 else 
	 {
//        printf("always safe to execute, so set safeTag to 0\n");
        safeTimestamp = 0;
    }

    return safeTimestamp;
}

void setActuationInterrupt(long actuationTime) 
{
	return;
}

void setTimedInterrupt(long safeToProcessTime) 
{
	return;
}

long getCurrentPhysicalTime() 
{
	return seconds;
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
	Event* dummyEvent;
	
	disableInterrupts();

    //
    // Get the interrrupt status.
    //
    ulStatus = UARTIntStatus(UART0_BASE, true);
    //
    // Clear the asserted interrupts.
    //
    UARTIntClear(UART0_BASE, ulStatus);

	fireClock(SOURCE1, dummyEvent);

	enableInterrupts();
	
	processEvents();	

	// If processEvents() is currently trying
    // to fill the output buffer of source
    // actors, go back to the start of
    // while(TRUE) in processEvents().		
	STOP_SOURCE_PROCESS = TRUE;
}

//FIXME: which interrupt(s) should I disable/enable?
void disableInterrupts() {
	IntMasterDisable();
	IntDisable(INT_UART0);
	UARTIntDisable(UART0_BASE, UART_INT_RX | UART_INT_RT);
	return;
}

void enableInterrupts() {
	IntMasterEnable();
    IntEnable(INT_UART0);
    UARTIntEnable(UART0_BASE, UART_INT_RX | UART_INT_RT);
	return;
} 



Event * newEvent()
{
RIT128x96x4StringDraw("bne",12,12,15);
//RIT128x96x4StringDraw(itoa(locationCounter,10), 30,12,15);
printf("location counter is %d",locationCounter);
	while(eventMemory[locationCounter].inUse != 36 )
	{  
	   ASSERT(locationCounter != 35)  // if you've run out of memory just stop
	  printf("locationCounter %d",locationCounter);
	   locationCounter++;
	}
	locationCounter%=35;  // make it circular
//	RIT128x96x4StringDraw(itoa(locationCounter,10), 0,0,15);
	return &eventMemory[locationCounter];
	
}

void freeEvent(Event * thisEvent)
{
	eventMemory[thisEvent->inUse].inUse = 36;

}
void initializeMemory()
{
	int i;
	locationCounter = 0;
						  
	for(i =0; i< 35; i++)
	{
	// event is "freed and can be returned by newEvent"
			eventMemory[i].inUse = 36; 
	}
}

/**
 * main() has the following functionalities:
 * declare all actors
 * setup dependencies between the actors
 * intialize actors
 */
int main(void)
{

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
	locationCounter=0;

	/*************************************************************************/
	// Utilities to setup interrupts
    //
    // Enable the peripherals used by this example.
    //
    SysCtlPeripheralEnable(SYSCTL_PERIPH_UART0);
    SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIOA);
	SysCtlPeripheralEnable(SYSCTL_PERIPH_TIMER0);



	//
    // Set the clocking to run directly from the crystal.
    //
    SysCtlClockSet(SYSCTL_SYSDIV_1 | SYSCTL_USE_OSC | SYSCTL_OSC_MAIN |
                   SYSCTL_XTAL_8MHZ);	   // 8MHZ is the fastest the clock can run.
	 //
    // Initialize the OLED display and write status.
    //
    RIT128x96x4Init(1000000);
    RIT128x96x4StringDraw("PtidyRTOS",            36,  0, 15);


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
	
	//setup and enable the SysTickTimer. It will be used as a reference for delay loops in interrupt handlers. 
	//The SysTick timer period will be set for one second
	SysTickPeriodSet(SysCtlClockGet());  
	SysTickEnable();
	IntEnable(15);  //sys tick vector
	//set timer0 as a periodic 32 bit timer
	TimerConfigure(TIMER0_BASE, TIMER_CFG_32_BIT_PER);
    // interrupt 10 times per second
    TimerLoadSet(TIMER0_BASE, TIMER_A, SysCtlClockGet()/10);

	//
	//Setup the interrupts for the timer timeouts
	//
    IntEnable(INT_TIMER0A);
	//IntEnable(INT_TIMER1A);
	TimerIntEnable(TIMER0_BASE,TIMER_TIMA_TIMEOUT);
	//TimerIntEnable(TIMER1_BASE,TIMER_TIMA_TIMEOUT);

		 //
    // Enable the timers.
    //
    TimerEnable(TIMER0_BASE, TIMER_A);
    //TimerEnable(TIMER1_BASE, TIMER_A);

	
	initializeMemory();
	
	SOURCE1 = &clock1;

	clock1.fireMethod = fireClock;

	computation1.fireMethod = fireComputation;

	computation2.fireMethod = fireComputation;

	computation3.fireMethod = fireComputation;

	model_delay1.fireMethod = fireModelDelay;

	model_delay2.fireMethod = fireModelDelay;

	model_delay3.fireMethod = fireModelDelay;	// should model delay be used in this example?

	merge1.fireMethod = fireMerge;

	actuator1.fireMethod = fireActuator;
   
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
	//printf("beforeinit/n");
    RIT128x96x4StringDraw("beforeinit", 12,24,15);

    initClock(&clock1);
	RIT128x96x4StringDraw("afterinitClk", 12,36,15);
	initSensor(&sensor2);
	RIT128x96x4StringDraw("afterinitS2",   12,48,15);					  
 	//initSensor(&sensor1);
	RIT128x96x4StringDraw("afterinitS1",   12,60,15);
    RIT128x96x4StringDraw("bPE",      12,65,15);
	//printf("afterinit/n");
  // processEvents();

   // RIT128x96x4StringDraw("afterprocessEvents", 12,48,15);
	
     
	return 0;
}

