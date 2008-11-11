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
#include <assert.h>
#include <string.h>

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
#include "ethernet.h"

#include "structures.h"
#include "functions.h"
#include "actors.h"

#include "globals.h"
#include "lwip/opt.h"
#include "lwip/def.h"
#include "lwip/sys.h"
#include "ptpd.h"
#include "random.h"

//#include "timer.h"
//#include "clock-arch.h"

// FIXME: constants.h is from PTPd program, which is open source as long as we provide
// the copywrite statement.
#include "dep-lmi/constants_dep.h"
#include "dep-lmi/datatypes_dep.h"
#include "constants.h"
#include "datatypes.h"

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
// Define the system clock rate here.  One of the following must be defined to
// choose the system clock rate.
//
//*****************************************************************************
//#define SYSTEM_CLOCK_8MHZ
//#define SYSTEM_CLOCK_20MHZ
//#define SYSTEM_CLOCK_25MHZ
#define SYSTEM_CLOCK_50MHZ

//*****************************************************************************
//
// Clock and PWM dividers used depend on which system clock rate is chosen.
//
//*****************************************************************************
#if defined(SYSTEM_CLOCK_8MHZ)
#define SYSDIV      SYSCTL_SYSDIV_1
#define PWMDIV      SYSCTL_PWMDIV_1
#define CLKUSE      SYSCTL_USE_OSC
#define TICKNS      125

#elif defined(SYSTEM_CLOCK_20MHZ)
#define SYSDIV      SYSCTL_SYSDIV_10
#define PWMDIV      SYSCTL_PWMDIV_2
#define CLKUSE      SYSCTL_USE_PLL
#define TICKNS      50

#elif defined(SYSTEM_CLOCK_25MHZ)
#define SYSDIV      SYSCTL_SYSDIV_8
#define PWMDIV      SYSCTL_PWMDIV_2
#define CLKUSE      SYSCTL_USE_PLL
#define TICKNS      40

#elif defined(SYSTEM_CLOCK_50MHZ)
#define SYSDIV      SYSCTL_SYSDIV_4
#define PWMDIV      SYSCTL_PWMDIV_2
#define CLKUSE      SYSCTL_USE_PLL
#define TICKNS      20

#else
#error "System clock speed is not defined properly!"

#endif

//*****************************************************************************
//
// Select button GPIO definitions. The GPIO defined here is assumed to be
// attached to a button which, when pressed during application initialization,
// signals that Ethernet packet timestamping hardware is not to be used.  If
// the button is not pressed, the hardware timestamp feature will be used if
// it is available on the target IC.
//
//*****************************************************************************
#define SEL_BTN_GPIO_PERIPHERAL SYSCTL_PERIPH_GPIOF
#define SEL_BTN_GPIO_BASE       GPIO_PORTF_BASE
#define SEL_BTN_GPIO_PIN        GPIO_PIN_1

//*****************************************************************************
//
// Pulse Per Second (PPS) Output Definitions
//
//*****************************************************************************
#define PPS_GPIO_PERIPHERAL     SYSCTL_PERIPH_GPIOB
#define PPS_GPIO_BASE           GPIO_PORTB_BASE
#define PPS_GPIO_PIN            GPIO_PIN_0

//*****************************************************************************
//
// The following group of labels define the priorities of each of the interrupt
// we use in this example.  SysTick must be high priority and capable of
// preempting other interrupts to minimize the effect of system loading on the
// timestamping mechanism.
//
// The application uses the default Priority Group setting of 0 which means
// that we have 8 possible preemptable interrupt levels available to us using
// the 3 bits of priority available on the Stellaris microcontrollers with
// values from 0xE0 (lowest priority) to 0x00 (highest priority).
//
//*****************************************************************************
#define SYSTICK_INT_PRIORITY  0x00
#define ETHERNET_INT_PRIORITY 0x80

//*****************************************************************************
//
// The clock rate for the SysTick interrupt.  All events in the application
// occur at some fraction of this clock rate.
//
//*****************************************************************************
#define SYSTICKHZ               100
#define SYSTICKMS               (1000 / SYSTICKHZ)
#define SYSTICKUS               (1000000 / SYSTICKHZ)
#define SYSTICKNS               (1000000000 / SYSTICKHZ)

//*****************************************************************************
//
// System Time - Internal representaion.
//
//*****************************************************************************
volatile unsigned long g_ulSystemTimeSeconds;
volatile unsigned long g_ulSystemTimeNanoSeconds;

//*****************************************************************************
//
// System Run Time - Ticks
//
//*****************************************************************************
volatile unsigned long g_ulSystemTimeTicks;

//*****************************************************************************
//
// These debug variables track the number of times the getTime function reckons
// it detected a SysTick wrap occurring during the time when it was reading the
// time.  We also record the second value of the timestamp when the wrap was
// detected in case we want to try to correlate this with any external
// measurements.
//
//*****************************************************************************
#ifdef DEBUG
unsigned long g_ulSysTickWrapDetect = 0;
unsigned long g_ulSysTickWrapTime = 0;
unsigned long g_ulGetTimeWrapCount = 0;
#endif

//*****************************************************************************
//
// Local data for clocks and timers.
//
//*****************************************************************************
static volatile unsigned long g_ulNewSystemTickReload = 0;
static volatile unsigned long g_ulSystemTickHigh = 0;
static volatile unsigned long g_ulSystemTickReload = 0;

//*****************************************************************************
//
// Statically allocated runtime options and parameters for PTPd.
//
//*****************************************************************************
static PtpClock g_sPTPClock;
static ForeignMasterRecord g_psForeignMasterRec[DEFUALT_MAX_FOREIGN_RECORDS];
static RunTimeOpts g_sRtOpts;

//*****************************************************************************
//
// External references.
//
//*****************************************************************************
extern void httpd_init(void);
extern void fs_init(void);
extern void lwip_init(void);
extern void lwip_tick(unsigned long ulTickMS);

//*****************************************************************************
//
// Local function prototypes.
//
//*****************************************************************************
void adjust_rx_timestamp(TimeInternal *psRxTime, unsigned long ulRxTime,
                         unsigned long ulNow);


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
// Event flags (bit positions defined in globals.h)
//
//*****************************************************************************
volatile unsigned long g_ulFlags;

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
// The interrupt handler for the SystemTick
//
//*****************************************************************************
void SysTickHandler(void)
{
  //what should I do to clear the SysTick Interrupt?
  //does it not need to be cleared?

  //RIT128x96x4StringDraw("in systick handler",            12,  0, 15);
  
   seconds++;

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
    /*IntMasterDisable();
	//eightmicroseconds++;
  	if(tenthofsecond == 10)
  	{
   		seconds++;	/// if seconds are incremented here we'll have to limit the granularity
   		tenthofsecond = 0;
  	}
	  */
/*	nanoseconds++;
	if(nanoseconds == 100000000)
	{
	seconds ++;
	nanoseconds = 0;
	RIT128x96x4StringDraw("seconds ",        12, 36, 15);
	RIT128x96x4StringDraw(itoa(seconds,10),  50, 36, 15);
	}	*/
    //RIT128x96x4StringDraw(HWREGBITW(&g_ulFlags, 0) ? "1" : "0",        12, 36, 15);
	
    //IntMasterEnable();
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
void initializeClock(Actor *this_clock)
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
RIT128x96x4StringDraw("eaddE",   75,72,15);
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
			// FIXME: since there is only one instance of processing events running, we don't need
			// the check for alreadyFiring(), in other words, alreadyFiring should always be false.
			if (alreadyFiring(actor)) {
				//enableInterrupts();
				//break;
				die("alreadyFiring should always be false within processEvents()");
			} else {
	            long processTime = safeToProcess(event);
	            if (getCurrentPhysicalTime() >= processTime) {
	                removeEvent();
					currentlyFiring(actor);
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
    RIT128x96x4StringDraw("bottomOfWhile", 12,72,15);
	}
}
 
/**
 * This function is almost the same as processEvents, only that this function is called within interrupt service
 * routines. When an event is not safe to process, or alreadyingFiring() is true, we return from this function,
 * instead of waiting for events to process. We also do not deal with source actors within this process
 *
 * Also, maybe we should have a seperate event queue for events generated here...? Depends on what we really want.
 */
processAvailableEvents()
{

    //  To ensure this function is thread safe, we make sure only one processEvents() IS BEING EXECUTED AT A TIME
    //  WHILE ANOTHER SENSOR MIGHT TRIGGER ANOTHER processEvents()
    //  IF ANOTHER processEvents() is currently being executed, we don't really need to have another being executed at the same time...
	Actor* actor;

    while (1) {
        disableInterrupts();
        // If event queue is not empty.
        if (EVENT_QUEUE_HEAD != NULL) {
            Event* event = EVENT_QUEUE_HEAD;
            // Check which actor produced the event that we have processed			
			actor = event->actorFrom;
			// FIXME: ALSO check if an event is of higher priority here.
			if (alreadyFiring(actor)) {
				enableInterrupts();
				break;
			} else {
	            long processTime = safeToProcess(event);
	            if (getCurrentPhysicalTime() >= processTime) {
	                removeEvent();
					currentlyFiring(actor);
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
					// leave it for processEvents() to figure out what to do with source processes.
					enableInterrupts();
					break;
//	                // There are no events safe to
//	                // process, so setup sources
//	                // to process.
//	                STOP_SOURCE_PROCESS = FALSE;
//	                // Set timed interrupt to run
//	                // the event at the top of the
//	                // event queue when physical time
//	                // has passed for it to be
//	                // safe to process
//	                setTimedInterrupt(processTime);
//	                enableInterrupts();
	            }
			}
        } else {
			// leave it for processEvents() to figure out what to do with source processes.
			enableInterrupts();
			break;
//            // There are no events safe to
//            // process, so setup sources
//            // to process.
//            STOP_SOURCE_PROCESS = FALSE;
//            enableInterrupts();
        }
		// leave it for processEvents() to figure out what to do with source processes.
//        // If there is no event to process and we
//        // have not reached the buffer limit, fire
//        // the source actor.
//        // for (each source actors a) {
//        if (STOP_SOURCE_PROCESS == FALSE) {
//			if (CK1_BUF_SIZE < MAX_BUFFER_LIMIT) {
//				Event* dummyEvent;
//            	fireClock(SOURCE1, dummyEvent);
//			}
//        }
    }
} 

/** 
 * This is the initialization method for sensor actor.
 * Initialize the sensor actor to setup receiving function through another 
 * thread, it then create a new event and calls processEvent()
 */
void initializeSensor(Actor* this_sensor)     //this listens for the next signal to sense again
{
	long time;
    Event* myNewEvent;
 RIT128x96x4StringDraw("start",   12,72,15);


	myNewEvent = newEvent();

RIT128x96x4StringDraw("begin",   40,72,15);


    //get the current time
    time = getCurrentPhysicalTime();

    // Set a time trigger from now

    myNewEvent->Tag.timestamp = time;
    myNewEvent->Tag.microstep = 0;	//FIXEM
    myNewEvent->thisValue.doubleValue = time;
    myNewEvent->actorToFire = this_sensor->nextActor1;
    myNewEvent->actorFrom = this_sensor;
	RIT128x96x4StringDraw("**",   70,72,15);
    //now put the created event into the queue
    //BUT HOW DO I TRACK THE START OF THE EVENT QUEUE??? --global variable
   // addEvent(myNewEvent);
	 RIT128x96x4StringDraw("??",   70,72,15);
    if (this_sensor->nextActor2 != NULL)
    {
        Event* newEvent2 = newEvent();
		//*newEvent2 = *newEvent;     //Jia, I'm nto wure what this is for
        newEvent2->actorToFire = this_sensor->nextActor2;
       addEvent(newEvent2);
    }

		RIT128x96x4StringDraw("end",   70,72,15);

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
{  // returns seconds elapsed.. number of times the counter has rolled over.
	return (seconds*8000000) + SysTickValueGet();
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
	
	processAvailableEvents();	

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



Event * newEvent(void)
{
RIT128x96x4StringDraw("bne",12,12,15);
//RIT128x96x4StringDraw(itoa(locationCounter,10), 30,12,15);
//printf("location counter is %d",locationCounter);
	while(eventMemory[locationCounter].inUse != 36 )
	{  
	   ASSERT(locationCounter != 35)  // if you've run out of memory just stop
	//  printf("locationCounter %d",locationCounter);
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

    // Enable the timers.
    //
    TimerEnable(TIMER0_BASE, TIMER_A);
    //TimerEnable(TIMER1_BASE, TIMER_A);
	IEEE1588Init();
	
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

    RIT128x96x4StringDraw("beforeinit", 12,24,15);

    initializeClock(&clock1);
     RIT128x96x4StringDraw("afterinitClk", 12,36,15);
	initializeSensor(&sensor1);
	RIT128x96x4StringDraw("afterinitS1",   12,48,15);
	RIT128x96x4StringDraw("!!!!!!!!!!!!!!!!!!!!",      12,72,15);					  
 	initializeSensor(&sensor2);
	RIT128x96x4StringDraw("afterinitS2",   12,60,15);
    processEvents();

	return 0;
}

void IEEE1588Init(void) {

    unsigned long ulUser0, ulUser1;
    unsigned char pucMACArray[8];
    volatile unsigned long ulDelay;

    //
    // Enable and Reset the Ethernet Controller.
    //
    SysCtlPeripheralEnable(SYSCTL_PERIPH_ETH);
    SysCtlPeripheralReset(SYSCTL_PERIPH_ETH);
    IntPrioritySet(INT_ETH, ETHERNET_INT_PRIORITY);

    //
    // Enable Port F for Ethernet LEDs.
    //  LED0        Bit 3   Output
    //  LED1        Bit 2   Output
    //
    SysCtlPeripheralEnable(SYSCTL_PERIPH_GPIOF);
    GPIODirModeSet(GPIO_PORTF_BASE, GPIO_PIN_2 | GPIO_PIN_3, GPIO_DIR_MODE_HW);
    GPIOPadConfigSet(GPIO_PORTF_BASE, GPIO_PIN_2 | GPIO_PIN_3,
                     GPIO_STRENGTH_2MA, GPIO_PIN_TYPE_STD);


// we always do hardware timestamping

    //
    // Enable timer 3 to capture the timestamps of the incoming packets.
    //
    HWREGBITW(&g_ulFlags, FLAG_HWTIMESTAMP) = 1;
    SysCtlPeripheralEnable(SYSCTL_PERIPH_TIMER3);
    SysCtlPeripheralReset(SYSCTL_PERIPH_TIMER3);

    //
    // Configure Timer 3 as 2 16 bit counters.  Timer B is used to capture
    // the time of the last Ethernet RX interrupt and we leave Timer A free
    // running to allow us to determine how much time has passed between
    // the interrupt firing and the ISR actually reading the packet.  Had
    // we been interested in transmit timestamps, time 3A would have been
    // used for this and a second timer block would be needed to provide
    // the free-running reference count.
    //
    TimerConfigure(TIMER3_BASE, (TIMER_CFG_16_BIT_PAIR |
                                 TIMER_CFG_A_PERIODIC |
                                 TIMER_CFG_B_CAP_TIME));
    TimerPrescaleSet(TIMER3_BASE, TIMER_BOTH, 0);
    TimerLoadSet(TIMER3_BASE, TIMER_BOTH, 0xFFFF);
    TimerControlEvent(TIMER3_BASE, TIMER_B, TIMER_EVENT_POS_EDGE);

    //
    // Start the timers running.
    //
    TimerEnable(TIMER3_BASE, TIMER_BOTH);

// FIXME: what is systick's position in all of this? Do we have to use it to use PTP?

    //
    // Configure SysTick for a periodic interrupt in PTPd system.
    //
    ptpd_systick_init();
    //
    // Enable processor interrupts.
    //
    IntMasterEnable();

    //
    // Configure the hardware MAC address for Ethernet Controller filtering of
    // incoming packets.
    //
    // For the Luminary Micro Evaluation Kits, the MAC address will be stored
    // in the non-volatile USER0 and USER1 registers.  These registers can be
    // read using the FlashUserGet function, as illustrated below.
    //
    FlashUserGet(&ulUser0, &ulUser1);
    if((ulUser0 == 0xffffffff) || (ulUser1 == 0xffffffff))
    {
        //
        // We should never get here.  This is an error if the MAC address has
        // not been programmed into the device.  Exit the program.
        //
        RIT128x96x4StringDraw("MAC Address", 0, 16, 15);
        RIT128x96x4StringDraw("Not Programmed!", 0, 24, 15);
        die("");
    }

    //
    // Convert the 24/24 split MAC address from NV ram into a 32/16 split MAC
    // address needed to program the hardware registers, then program the MAC
    // address into the Ethernet Controller registers.
    //
    pucMACArray[0] = ((ulUser0 >> 0) & 0xff);
    pucMACArray[1] = ((ulUser0 >> 8) & 0xff);
    pucMACArray[2] = ((ulUser0 >> 16) & 0xff);
    pucMACArray[3] = ((ulUser1 >> 0) & 0xff);
    pucMACArray[4] = ((ulUser1 >> 8) & 0xff);
    pucMACArray[5] = ((ulUser1 >> 16) & 0xff);

    //
    // Program the hardware with it's MAC address (for filtering).
    //
    EthernetMACAddrSet(ETH_BASE, pucMACArray);

    //
    // Initialize all of the lwIP code, as needed, which will also initialize
    // the low-level Ethernet code.
    //
    lwip_init();

    //
    // Initialize a sample web server application.
    //
    httpd_init();
}

//*****************************************************************************
//
// This function will set the local time (provided in PTPd internal time
// format).  This time is maintained by the SysTick interrupt.
//
//*****************************************************************************
void setTime(TimeInternal *time)
{
    sys_prot_t sProt;

    //
    // Update the System Tick Handler time values from the given PTPd time
    // (fine-tuning is handled in the System Tick handler). We need to update
    // these variables with interrupts disabled since the update must be
    // atomic.
    //
#ifdef DEBUG
    UARTprintf("Setting time %d.%09d\n", time->seconds, time->nanoseconds);
#endif
    sProt = sys_arch_protect();
    g_ulSystemTimeSeconds = time->seconds;
    g_ulSystemTimeNanoSeconds = time->nanoseconds;
    sys_arch_unprotect(sProt);
}

//*****************************************************************************
//
// This function returns the local time (in PTPd internal time format).  This
// time is maintained by the SysTick interrupt.
//
// Note: It is very important to ensure that we detect cases where the system
// tick rolls over during this function. If we don't do this, there is a race
// condition that will cause the reported time to be off by a second or so
// once in a blue moon. This, in turn, causes large perturbations in the
// 1588 time controller resulting in large deltas for many seconds as the
// controller tries to compensate.
//
//*****************************************************************************
void
getTime(TimeInternal *time)
{
    unsigned long ulTime1;
    unsigned long ulTime2;
    unsigned long ulSeconds;
    unsigned long ulPeriod;
    unsigned long ulNanoseconds;

    //
    // We read the SysTick value twice, sandwiching taking snapshots of
    // the seconds, nanoseconds and period values. If the second SysTick read
    // gives us a higher number than the first read, we know that it wrapped
    // somewhere between the two reads so our seconds and nanoseconds
    // snapshots are suspect. If this occurs, we go round again. Note that
    // it is not sufficient merely to read the values with interrupts disabled
    // since the SysTick counter keeps counting regardless of whether or not
    // the wrap interrupt has been serviced.
    //
    do
    {
        ulTime1 = SysTickValueGet();
        ulSeconds = g_ulSystemTimeSeconds;
        ulNanoseconds = g_ulSystemTimeNanoSeconds;
        ulPeriod = SysTickPeriodGet();
        ulTime2 = SysTickValueGet();

#ifdef DEBUG
        //
        // In debug builds, keep track of the number of times this function was
        // called just as the SysTick wrapped.
        //
        if(ulTime2 > ulTime1)
        {
            g_ulSysTickWrapDetect++;
            g_ulSysTickWrapTime = ulSeconds;
        }
#endif
    }
    while(ulTime2 > ulTime1);

    //
    // Fill in the seconds field from the snapshot we just took.
    //
    time->seconds = ulSeconds;

    //
    // Fill in the nanoseconds field from the snapshots.
    //
    time->nanoseconds = (ulNanoseconds + (ulPeriod - ulTime2) * TICKNS);

    //
    // Adjust for any case where we accumulate more than 1 second's worth of
    // nanoseconds.
    //
    if(time->nanoseconds >= 1000000000)
    {
#ifdef DEBUG
        g_ulGetTimeWrapCount++;
#endif
        time->seconds++;
        time->nanoseconds -= 1000000000;
    }
}

//*****************************************************************************
//
// Get the RX Timestamp. This is called from the lwIP low_level_input function
// when configured to include PTPd support.
//
//*****************************************************************************
void
getRxTime(TimeInternal *psRxTime)
{
    unsigned long ulNow;
    unsigned long ulTimestamp;
    //
    // Get the current IEEE1588 time.
    //
    getTime(psRxTime);

    //
    // If we are using the hardware timestamp mechanism, get the timestamp and
    // use it to adjust the packet timestamp accordingly.
    //
    if(HWREGBITW(&g_ulFlags, FLAG_HWTIMESTAMP))
    {
        //
        // Read the (now frozen) timer value and the still-running timer.
        //
        ulTimestamp = TimerValueGet(TIMER3_BASE, TIMER_B);
        ulNow = TimerValueGet(TIMER3_BASE, TIMER_A);

        //
        // Adjust the current time with the difference between now and the
        // actual timestamp.
        //
        adjust_rx_timestamp(psRxTime, ulTimestamp, ulNow);
    }

    return;
}


//*****************************************************************************
//
// Initialization code for PTPD software.
//
//*****************************************************************************
void
ptpd_init(void)
{
    unsigned long ulTemp;

    //
    // Clear out all of the run time options and protocol stack options.
    //
    memset(&g_sRtOpts, 0, sizeof(g_sRtOpts));
    memset(&g_sPTPClock, 0, sizeof(g_sPTPClock));

    //
    // Initialize all PTPd run time options to a valid, default value.
    //
    g_sRtOpts.syncInterval = DEFUALT_SYNC_INTERVAL;
    memcpy(g_sRtOpts.subdomainName, DEFAULT_PTP_DOMAIN_NAME,
           PTP_SUBDOMAIN_NAME_LENGTH);
    memcpy(g_sRtOpts.clockIdentifier, IDENTIFIER_DFLT, PTP_CODE_STRING_LENGTH);
    g_sRtOpts.clockVariance = DEFAULT_CLOCK_VARIANCE;
    g_sRtOpts.clockStratum = DEFAULT_CLOCK_STRATUM;
    g_sRtOpts.clockPreferred = FALSE;
    g_sRtOpts.currentUtcOffset = DEFAULT_UTC_OFFSET;
    g_sRtOpts.epochNumber = 0;
    memcpy(g_sRtOpts.ifaceName, "LMI", strlen("LMI"));
    g_sRtOpts.noResetClock = DEFAULT_NO_RESET_CLOCK;
    g_sRtOpts.noAdjust = FALSE;
    g_sRtOpts.displayStats = FALSE;
    g_sRtOpts.csvStats = FALSE;
    g_sRtOpts.unicastAddress[0] = 0;
    g_sRtOpts.ap = DEFAULT_AP;
    g_sRtOpts.ai = DEFAULT_AI;
    g_sRtOpts.s = DEFAULT_DELAY_S;
    g_sRtOpts.inboundLatency.seconds = 0;
    g_sRtOpts.inboundLatency.nanoseconds = DEFAULT_INBOUND_LATENCY;
    g_sRtOpts.outboundLatency.seconds = 0;
    g_sRtOpts.outboundLatency.nanoseconds = DEFAULT_OUTBOUND_LATENCY;
    g_sRtOpts.max_foreign_records = DEFUALT_MAX_FOREIGN_RECORDS;
    g_sRtOpts.slaveOnly = TRUE;
    g_sRtOpts.probe = FALSE;
    g_sRtOpts.probe_management_key = 0;
    g_sRtOpts.probe_record_key = 0;
    g_sRtOpts.halfEpoch = FALSE;

    //
    // Initialize the PTP Clock Fields.
    //
    g_sPTPClock.foreign = &g_psForeignMasterRec[0];

    //
    // Configure port "uuid" parameters.
    //
    g_sPTPClock.port_communication_technology = PTP_ETHER;
    EthernetMACAddrGet(ETH_BASE, (unsigned char *)g_sPTPClock.port_uuid_field);

    //
    // Enable Ethernet Multicast Reception (required for PTPd operation).
    // Note:  This must follow lwIP/Ethernet initialization.
    //
    ulTemp = EthernetConfigGet(ETH_BASE);
    ulTemp |= ETH_CFG_RX_AMULEN;
    if(HWREGBITW(&g_ulFlags, FLAG_HWTIMESTAMP))
    {
        ulTemp |= ETH_CFG_TS_TSEN;
    }
    EthernetConfigSet(ETH_BASE, ulTemp);

    //
    // Run the protocol engine for the first time to initialize the state
    // machines.
    //
    protocol_first(&g_sRtOpts, &g_sPTPClock);
}


//*****************************************************************************
//
// Initialization code for PTPD software system tick timer.
//
//*****************************************************************************
void
ptpd_systick_init(void)
{
    //
    // Initialize the System Tick Timer to run at specified frequency.
    //
    SysTickPeriodSet(SysCtlClockGet() / SYSTICKHZ);

    //
    // Initialize the timer reload values for fine-tuning in the handler.
    //
    g_ulSystemTickReload = SysTickPeriodGet();
    g_ulNewSystemTickReload = g_ulSystemTickReload;

    //
    // Enable the System Tick Timer.
    //
    SysTickEnable();
    SysTickIntEnable();
}

//*****************************************************************************
//
// Run the protocol engine loop/poll.
//
//*****************************************************************************
void
ptpd_tick(void)
{
    //
    // Run the protocol engine for each pass through the main process loop.
    //
    protocol_loop(&g_sRtOpts, &g_sPTPClock);
}

//*****************************************************************************
//
// Adjust the supplied timestamp to account for interrupt latency.
//
//*****************************************************************************
void
adjust_rx_timestamp(TimeInternal *psRxTime, unsigned long ulRxTime,
                    unsigned long ulNow)
{
    unsigned long ulCorrection;

    //
    // Time parameters ulNow and ulRxTime are assumed to have originated from
    // a 16 bit down counter operating over its full range.
    //

    //
    // Determine the number of cycles between the receive timestamp and the
    // point that it was read.
    //
    if(ulNow < ulRxTime)
    {
        //
        // The timer didn't wrap between the timestamp and now.
        //
        ulCorrection = ulRxTime - ulNow;
    }
    else
    {
        //
        // The timer wrapped between the timestamp and now
        //
        ulCorrection = ulRxTime + (0x10000 - ulNow);
    }

    //
    // Convert the correction from cycles to nanoseconds.
    //
    ulCorrection *= TICKNS;

    //
    // Subtract the correction from the supplied timestamp value.
    //
    if(psRxTime->nanoseconds >= ulCorrection)
    {
        //
        // In this case, we need only adjust the nanoseconds value since there
        // is no borrow from the seconds required.
        //
        psRxTime->nanoseconds -= ulCorrection;
    }
    else
    {
        //
        // Here, the adjustment affects both the seconds and nanoseconds
        // fields. The correction cannot be more than 1 second (16 bit counter
        // maximum offset and minimum cycle time of 125nS gives a maximum
        // correction of 8.192mS) so we don't need to perform any nasty, slow
        // modulo calculations here.
        //
        psRxTime->seconds--;
        psRxTime->nanoseconds += (1000000000 - ulCorrection);
    }
}

//*****************************************************************************
//
// The interrupt handler for the SysTick interrupt.
//
//*****************************************************************************
void
SysTickIntHandler(void)
{
    //
    // Update internal time and set PPS output, if needed.
    //
    g_ulSystemTimeNanoSeconds += SYSTICKNS;
    if(g_ulSystemTimeNanoSeconds >= 1000000000)
    {							   
        GPIOPinWrite(PPS_GPIO_BASE, PPS_GPIO_PIN, PPS_GPIO_PIN);
        g_ulSystemTimeNanoSeconds -= 1000000000;
        g_ulSystemTimeSeconds += 1;
        HWREGBITW(&g_ulFlags, FLAG_PPSOUT) = 1;
    }

    //
    // Set a new System Tick Reload Value.
    //
    if(g_ulSystemTickReload != g_ulNewSystemTickReload)
    {
        g_ulSystemTickReload = g_ulNewSystemTickReload;

        g_ulSystemTimeNanoSeconds = ((g_ulSystemTimeNanoSeconds / SYSTICKNS) *
                                     SYSTICKNS);
    }

    //
    // For each tick, set the next reload value for fine tuning the clock.
    //
    if((g_ulSystemTimeTicks % TICKNS) < g_ulSystemTickHigh)
    {
        SysTickPeriodSet(g_ulSystemTickReload + 1);
    }
    else
    {
        SysTickPeriodSet(g_ulSystemTickReload);
    }

    //
    // Service the PTPd Timer.
    //
    timerTick(SYSTICKMS);

    //
    // Increment the run-time tick counter.
    //
    g_ulSystemTimeTicks++;

    //
    // Clear the SysTick interrupt flag.
    //
    HWREGBITW(&g_ulFlags, FLAG_SYSTICK) = 0;

    //
    // Run the Luminary lwIP system tick.
    //
    lwip_tick(SYSTICKMS);

// Do we want this? each second provide an GPIO output?
//    //
//    // Setup to disable the PPS output on the next pass.
//    //
//    if(HWREGBITW(&g_ulFlags, FLAG_PPSOUT))
//    {
//        //
//        // Setup to turn off the PPS output.
//        //
//        HWREGBITW(&g_ulFlags, FLAG_PPSOUT) = 0;
//        HWREGBITW(&g_ulFlags, FLAG_PPSOFF) = 1;
//    }

    //
    // Check if an RX Packet was received.
    //
    if(HWREGBITW(&g_ulFlags, FLAG_RXPKT))
    {
        //
        // Clear the Rx Packet interrupt flag.
        //
        HWREGBITW(&g_ulFlags, FLAG_RXPKT) = 0;

        //
        // Run the Luminary lwIP system tick, but with no time, to indicate
        // an RX or TX packet has occurred.
        //
        lwip_tick(0);
    }

    //
    // Check if a TX Packet was sent.
    //
    if(HWREGBITW(&g_ulFlags, FLAG_TXPKT))
    {
        //
        // Clear the Tx Packet interrupt flag.
        //
        HWREGBITW(&g_ulFlags, FLAG_TXPKT) = 0;

        //
        // Run the Luminary lwIP system tick, but with no time, to indicate
        // an RX or TX packet has occurred.
        //
        lwip_tick(0);

        //
        // Enable Ethernet TX Packet Interrupts.
        //
        EthernetIntEnable(ETH_BASE, ETH_INT_TX);
    }

    //
    // If IP address has been assigned, initialize the PTPD software (if
    // not already initialized).
    //
    if(HWREGBITW(&g_ulFlags, FLAG_IPADDR) &&
       !HWREGBITW(&g_ulFlags, FLAG_PTPDINIT))
    {
        ptpd_init();
        HWREGBITW(&g_ulFlags, FLAG_PTPDINIT) = 1;
    }

    //
    // If PTPD software has been initialized, run the ptpd tick.
    //
    if(HWREGBITW(&g_ulFlags, FLAG_PTPDINIT))
    {
        ptpd_tick();
    }
}

//*****************************************************************************
//
// Based on the value (adj) provided by the PTPd Clock Servo routine, this
// function will adjust the SysTick periodic interval to allow fine-tuning of
// the PTP Clock.
//
//*****************************************************************************
Boolean
adjFreq(Integer32 adj)
{
    unsigned long ulTemp;

    //
    // Check for max/min value of adjustment.
    //
    if(adj > ADJ_MAX)
    {
        adj = ADJ_MAX;
    }
    else if(adj < -ADJ_MAX)
    {
        adj = -ADJ_MAX;
    }

    //
    // Convert input to nanoseconds / systick.
    //
    adj = adj / SYSTICKHZ;

    //
    // Get the nominal tick reload value and convert to nano seconds.
    //
    ulTemp = (SysCtlClockGet() / SYSTICKHZ) * TICKNS;

    //
    // Factor in the adjustment.
    //
    ulTemp -= adj;

    //
    // Get a modulo count of nanoseconds for fine tuning.
    //
    g_ulSystemTickHigh = ulTemp % TICKNS;

    //
    // Set the reload value.
    //
    g_ulNewSystemTickReload = ulTemp / TICKNS;

    //
    // Return.
    //
    return(TRUE);
}

//*****************************************************************************
//
// This function returns a random number, using the functions in random.c.
//
//*****************************************************************************
UInteger16
getRand(UInteger32 *seed)
{
    unsigned long ulTemp;
    UInteger16 uiTemp;

    //
    // Re-seed the random number generator.
    //
    RandomAddEntropy(*seed);
    RandomSeed();

    //
    // Get a random number and return a 16-bit, truncated version.
    //
    ulTemp = RandomNumber();
    uiTemp = (UInteger16)(ulTemp & 0xFFFF);
    return(uiTemp);
}

//*****************************************************************************
//
// Display Statistics.  For now, do nothing, but this could be used to either
// update a web page, send data to the serial port, or to the OLED display.
//
// Refer to the ptpd software "src/dep/sys.c" for example code.
//
//*****************************************************************************
void
displayStats(RunTimeOpts *rtOpts, PtpClock *ptpClock)
{
}

//*****************************************************************************
//
// Display Date and Time.
//
//*****************************************************************************
static char g_pucBuf[23];
const char *g_ppcDay[7] =
{
    "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
};
const char *g_ppcMonth[12] =
{
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
};