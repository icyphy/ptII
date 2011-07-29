/*** StructDefBlock ***/
#define LED             GPIO_PIN_0 /* PF0 */
#define SELECT          GPIO_PIN_1 /* PF1 */
#define UP              GPIO_PIN_0 /* PE0 */
#define DOWN            GPIO_PIN_1 /* PE1 */
#define LEFT            GPIO_PIN_2 /* PE2 */
#define RIGHT           GPIO_PIN_3 /* PE3 */
#define BUTTON         (UP | DOWN | LEFT | RIGHT)

/* Common Type Definitions */
typedef unsigned long long      uint64;
typedef unsigned long           uint32;
typedef unsigned int            uint16;
typedef unsigned char           uint8;
typedef uint8                   byte;
typedef signed long long        int64;
typedef signed long             int32;
typedef signed int              int16;
typedef signed char             int8;

/* Hardware library includes. */
#include "hw_ints.h"
#include "hw_memmap.h"
#include "hw_types.h"
#include "debug.h"
#include "gpio.h"
#include "interrupt.h"
#include "sysctl.h"
#include "uart.h"
#include "drivers/rit128x96x4.h"
#include "timer.h"
#include "systick.h"
#include "hw_nvic.h"
#include "ethernet.h"

// Number of cycles until timer rolls over
static unsigned long TIMER_ROLLOVER_CYCLES;

$super.StructDefBlock();
/**/

/*** FuncProtoBlock ***/
void addStack(void);
void saveState(void);
void loadState(void);
uint32 convertCyclesToNsecs(uint32);
uint32 convertNsecsToCycles(uint32);
void die(char*);
void disableInterrupts(void);
void enableInterrupts(void);
void getRealTime(Time*);
void setActuationInterrupt(int);
void setTimedInterrupt(const Time*);
void SysTickHandler(void);
void Timer0IntHandler(void);
void Timer1IntHandler(void);
void __svc(0)  restoreStack(void);

$super.FuncProtoBlock();
/**/

// If more int's are to be added to the argument of FuncBlock, change the
// maxNumSensorInputs field in PtidesBasicDirector.
/*** FuncBlock($dis1, $dis2, $dis3, $dis4, $dis5, $dis6, $dis7, $dis8,
$en1, $en2, $en3, $en4, $en5, $en6, $en7, $en8) ***/

#ifdef LCD_DEBUG
//Unsigned long to ASCII; fixed maxiumum output string length of 32 characters
char *_ultoa(uint32 value, char *string, uint16 radix){
        char digits[32];
        char * const string0 = string;
        int32 ii = 0;
        int32 n;

        do{
                n = value % radix;
                digits[ii++] = (n < 10 ? (char)n+'0' : (char)n-10+'a');
                value /= radix;
        } while(value != 0);

        while (ii > 0)
                *string++ = digits[--ii];
        *string = 0;
        return string0;
}

//Safely print a debug message to the screen; receive arbitrary length string,
//and print sequential messages on the screen, where each message is written to
//a screen line, and subsequent messages are written to the next line (wraps to the top)
//
//Uses all screenlines from STARTLINE
#define DBG_STARTLINE 2
void debugMessage(char * szMsg){
        static uint16 screenIndex = DBG_STARTLINE * 8;    //Screen line to write message (incremented by 8)
        static uint16 eventCount = 0;                     //Event count (0x00 - 0xFF, incremented by 1)
        static char screenBuffer[36] = {'\0'};

        const uint16 eventCountRadix0 = eventCount >> 4;
        const uint16 eventCountRadix1 = eventCount & 0x0F;
        register uint16 index = 0;

        screenBuffer[0] = eventCountRadix0 < 10 ? '0' + eventCountRadix0 : 'a' + eventCountRadix0 - 10;
        screenBuffer[1] = eventCountRadix1 < 10 ? '0' + eventCountRadix1 : 'a' + eventCountRadix1 - 10;
        screenBuffer[2] = ' ';
        while(index < 32 && szMsg[index]){
                screenBuffer[index+3] = szMsg[index];
                index++;
        }
        screenBuffer[index+3] = '\0';

        disableInterrupts();
        RIT128x96x4StringDraw("                      ", 0, screenIndex, 15);
        RIT128x96x4StringDraw(screenBuffer, 0, screenIndex, 15);

        enableInterrupts();
        screenIndex = screenIndex < 88 ? screenIndex + 8 : DBG_STARTLINE * 8;
        eventCount = (eventCount + 1) & 0xFF;
}

//Print a debug message, with a number appended at the end
void debugMessageNumber(char * szMsg, uint32 num){
        static char szNumberBuffer[32] = {'\0'};
        static char szResult[32] = {'\0'};
        register uint16 index = 0;
        register const char * ptrNum = &szNumberBuffer[0];

        _ultoa(num, szNumberBuffer, 10);
        while(index < 32 && szMsg[index]){
                szResult[index] = szMsg[index];
                index++;
        }
        while(index < 32 && *ptrNum){
                szResult[index] = *ptrNum;
                index++;
                ptrNum++;
        }
        szResult[index] = '\0';

        debugMessage(szResult);
}
#else
    //Debug messages will have no effect
    #define debugMessage(x)
    #define debugMessageNumber(x, y)
#endif

void exit(int zero) {
        die("program exit?");
}

// Convert processor cycle count to nanoseconds.
// This method assumes a fixed clock rate
// of 50 MHz
uint32 convertCyclesToNsecs(uint32 cycles){
    // nsec = cycles * 20 = cycles * (4+1) * 4
    return ((cycles << 2) + cycles) << 2;
}

// Convert nanoseconds to processor cycles.
// This method assumes a fixed clock rate
// of 50 MHz
uint32 convertNsecsToCycles(uint32 nsecs) {
        return nsecs / 20;
        // FIXME: Is there a way to make it less expensive?
}

/* error printout */
void die(char *mess) {
        IntMasterDisable();
        //FIXME: Can this be upped to 4KHz?
        RIT128x96x4Init(2000000);
        RIT128x96x4DisplayOn();
        RIT128x96x4StringDraw(mess, 0,88,15);
        while(1);
}

// Disable all peripheral and timer interrupts (does not include the systick)
// IntMasterDisable should not be called here, because the systick handler would be disabled.
// Instead, we disable each interrupt individually.
void disableInterrupts(void) {
        // disable safe to process timer
    IntDisable(INT_TIMER0A);
        IntDisable(INT_TIMER0B);
        // disable actuation timer
        IntDisable(INT_TIMER1A);
        IntDisable(INT_TIMER1B);
        // disable sensor input peripherals
        $dis1
        $dis2
        $dis3
        $dis4
        $dis5
        $dis6
        $dis7
        $dis8
}
// Enable all peripheral and timer interrupts (does not include the systick)
// IntMasterEnable should not be called here, because the systick handler would be disabled.
// Instead, we disable each interrupt individually.
void enableInterrupts(void) {
        // enable Timer0
        IntEnable(INT_TIMER0A);
        IntEnable(INT_TIMER0B);
        // enable actuation timer
        // FIXME: should probably not have this here....
        // We have it because there's a bug if this is taken out...
        if (actuatorRunning != -1) {
                IntEnable(INT_TIMER1A);
                IntEnable(INT_TIMER1B);
        }
        // enable sensor input peripherals
        $en1
        $en2
        $en3
        $en4
        $en5
        $en6
        $en7
        $en8
}

//Return the real physical time.
//external interrupts should be disabled when calling this function
//(however Systick interrupt should not be disabled.
void getRealTime(Time * const physicalTime){
    uint32 tick1;
    uint32 tick2;
    uint32 tempSecs;
    uint32 tempQuarterSecs;
    for (;;) {
    tick1 = SysTickValueGet();
    tempSecs = _secs;
    tempQuarterSecs = _quarterSecs;
    tick2 = SysTickValueGet();
    // If the system tick rolls over (the tick counts down) between accessing
    // the volatile variables _secs and _quartersecs, then we account for this here
    // by incrementing _quartersecs
            if(tick2 < tick1) {
                physicalTime->secs = tempSecs;
            switch(tempQuarterSecs){
                                case 0:                        physicalTime->nsecs = 0;                 break;
                case 1:         physicalTime->nsecs = 250000000; break;
                case 2:         physicalTime->nsecs = 500000000; break;
                case 3:         physicalTime->nsecs = 750000000; break;
                default:                die("quarterSecs is wrong");
            }
            // convertCyclesToNsecs assumes 50MHz clock
            physicalTime->nsecs += (convertCyclesToNsecs((TIMER_ROLLOVER_CYCLES >> 2) - tick2));
                break;
            }
    }
}

/* timer */
void setTimedInterrupt(const Time* safeToProcessTime) {
        Time currentTime, timeToWait;
    // it has already been checked, timer always needs to be set, so just set it.
    TimerConfigure(TIMER0_BASE, TIMER_CFG_32_BIT_OS);
    // interrupt 10 times per second
        getRealTime(&currentTime);
        if (-1 == timeSub(*safeToProcessTime, currentTime, &timeToWait)) {
                TimerLoadSet(TIMER0_BASE, TIMER_BOTH, 0);
                timerInterruptSecsLeft = 0;
        } else {
            TimerLoadSet(TIMER0_BASE, TIMER_BOTH, convertNsecsToCycles(timeToWait.nsecs));
            timerInterruptSecsLeft = timeToWait.secs;
    }

    //
    //Setup the interrupts for the timer timeouts
    //
    IntEnable(INT_TIMER0A);
    IntEnable(INT_TIMER0B);
    TimerIntEnable(TIMER0_BASE,TIMER_TIMA_TIMEOUT);

    // Enable the timers.
    TimerEnable(TIMER0_BASE, TIMER_BOTH);
    return;
}

void Timer0IntHandler(void) {
        TimerIntClear(TIMER0_BASE, TIMER_TIMA_TIMEOUT);
        if (timerInterruptSecsLeft > 0) {
                TimerLoadSet(TIMER0_BASE, TIMER_BOTH, TIMER_ROLLOVER_CYCLES);
                TimerEnable(TIMER0_BASE, TIMER_BOTH);
                timerInterruptSecsLeft--;
                return;
        }
                saveState();
        // need to push the currentModelTag onto the stack.
        stackedModelTagIndex++;
        if (stackedModelTagIndex > MAX_EVENTS) {
                die("MAX_EVENTS too small for stackedModelTagIndex");
        }
        executingModelTag[stackedModelTagIndex].microstep = currentMicrostep;
        executingModelTag[stackedModelTagIndex].timestamp = currentModelTime;

        // disable interrupts, then add stack to execute processEvents().
        lastTimerInterruptTime = MAX_TIME;
        TimerDisable(TIMER0_BASE, TIMER_BOTH);
        IntDisable(INT_TIMER0A);
        IntDisable(INT_TIMER0B);
        TimerIntDisable(TIMER0_BASE, TIMER_TIMA_TIMEOUT);
        TimerIntDisable(TIMER0_BASE, TIMER_TIMB_TIMEOUT);
        addStack();
}

//SysTickHandler ISR
//configured to execute every 1/4 second (
void SysTickHandler(void) {
 if(++_quarterSecs == 4){
     _quarterSecs = 0;
     _secs++;
 }
}

/* Event processing */
void processEvents() {
    Event* event = NULL;
    Time processTime;
    Time platformTime;
        // Increment the process version.
        processVersion++;
        // Get the current platform time. This time is later used to
        // perform safe-to-process. This function must be called
        // before interrupts are disabled to ensure DE semantics.
    getRealTime(&platformTime);
    disableInterrupts();
    while (true) {
        // If event is null, then return the highest priority event
        // from the queue. Otherwise, return the next event pointed
        // to by event. Return NULL if there are no more events
        // in the event queue. The event queue is sorted in
        // deadline order.
        event = peekNextEvent(event);
        // If there are no more events in the event queue, break
        // out of the while loop.
                if (!event) {
            break;
        }
        // If this event's priority is higher than the last priority
        // saved in storePriority(), then continue.
        // The priority here is the priority of the event,
        // not the priority of the interrupt.
        if (higherPriority(event)) {
            // check if this event is safe to process.
            safeToProcess(event, &processTime);
            if (timeCompare(platformTime, processTime) >= 0) {
                // Store the priority of the previous interrupt.
                // Stored priority is later used for comparison in
                // higherPriority().
                queuePriority(event);
                // Get all events of the same timestamp, and share
                // the same destination equivalence class.
                // Remove these events from the event queue.
                removeAndPropagateSameTagEvents(event);
                setCurrentModelTag(event);
                // Ready to process the next event, so first enable
                // interrupts.
                enableInterrupts();
                // Execute this event by firing the corresponding
                // actor. During this process more events may
                // be posted onto the queue
                fireActor(event);
                                // Get the current platform time. This time is later used to
                                // perform safe-to-process. This function must be called
                                // before interrupts are disabled to ensure DE semantics.
                            getRealTime(&platformTime);
                // We are ready to look at the next event in the
                // event queue. Before doing that interrupts need
                // to be disabled
                disableInterrupts();
                // The executed event can now be freed into the
                // pool of available events
                                freeEvent(event);
                                // This event has finished execution. The priority of
                                // this event can be forgotten. We forget by decrementing
                                // the stackedDeadlineIndex.
                                stackedDeadlineIndex--;
                                // Reset event to null so the next peekNextEvent()
                // looks at the top event.
                event = NULL;
            } else {
                // save the current process level
                uint32 localProcessVersion = processVersion;
                // This event is not safe to process yet. Set
                // timed interrupt to run this event when platform
                // time has passed for it to be safe to process.
                if (timeCompare(processTime, lastTimerInterruptTime) == LESS) {
                    lastTimerInterruptTime = processTime;
                    setTimedInterrupt(&processTime);
                }
                                // Enables higher priority events to preempt this processEvents().
                                // This is especially useful if the size of the event queue is
                                // very large, and takes a long time to traverse through.
                                enableInterrupts();
                                disableInterrupts();
                                if (localProcessVersion != processVersion) {
                                        // If processEvents ran during the last interrupt enable,
                                        // then we should traverse the event queue anymore.
                                        // Instead we simply return.
                                        break;
                                }
                // If processEvents did not run during the last interrupt enable,
                // then we keep analyze events in the event queue by going back
                // to the beginning of the loop.
                }
        } else {
            // This event is of lower priority than the one
            // currently executing, break out of the while loop.
            break;
        }//end while().
    }
    // restore the last executing stacked model tag.
    if (stackedModelTagIndex >= 0) {
        currentMicrostep = executingModelTag[stackedModelTagIndex].microstep;
        currentModelTime = executingModelTag[stackedModelTagIndex].timestamp;
        stackedModelTagIndex--;
    } else {
        die("cannot restore model tag");
    }
        // End of processEvents(), enable interrupts.
    enableInterrupts();
    // we do not need to disable interrupts for this routine, because it
    // is triggered through a SVC call, which has higher priority than
    // all other external interrupts in the system.
    restoreStack();
    die("should never get here");
}

$super.FuncBlock();

// Actuators use timer1.
void setActuationInterrupt(int actuatorToActuate) {
        // If timer already running
        // check if need to reload the interrupt value.
        // If not
        // Set it up to run.
        //set timer0 as a periodic 32 bit timer
        // FIXME: does TimerValueGet() return 0 if timer is not on?

        int actuatorID;
        Time actuationTime;
        Time actuationLeftOverTime;
        Time physicalTime;

        disableInterrupts();

        actuationTime = currentModelTime;

#ifdef LCD_DEBUG
        debugMessage("set act int");
#endif

        if (actuatorRunning < 0) {

                // Timer not running. setup a new timer value and start timer.

                // FIXME: move this to intialization.
                TimerConfigure(TIMER1_BASE, TIMER_CFG_A_ONE_SHOT);
                TimerConfigure(TIMER1_BASE, TIMER_CFG_B_ONE_SHOT);

#ifdef LCD_DEBUG
                //debugMessageNumber("Act secs : ", actuationTime.secs);
                //debugMessageNumber("Act nsecs: ", actuationTime.nsecs);
#endif

                // FIXME: there might be a concurrency issue here, actuatorTimerInterrupt is set to true,
                // yet we could have another interrupt coming in right after it that tries to set another timer interrupt,
                // in which case it would try to access the else{} part of this function.
                // FIXED: this is taken care by all the interrupts having the same priority, thus within this
                // ISR there wouldn't be any preemption.
                actuatorRunning = actuatorToActuate;
                lastActuateTime = actuationTime;

                getRealTime(&physicalTime);
                // FIXME: actually missed a deadline, but sets actuation signal anyway.
                if (timeSub(actuationTime, physicalTime, &actuationLeftOverTime) < 0) {
                        TimerLoadSet(TIMER1_BASE, TIMER_BOTH, 0);
                        actuatorTimerInterruptSecsLeft = 0;
                } else {
                        TimerLoadSet(TIMER1_BASE, TIMER_BOTH, convertNsecsToCycles(actuationLeftOverTime.nsecs));
                        actuatorTimerInterruptSecsLeft = actuationLeftOverTime.secs;
                }
                IntEnable(INT_TIMER1A);
                IntEnable(INT_TIMER1B);
                TimerIntEnable(TIMER1_BASE,TIMER_TIMA_TIMEOUT);
                TimerEnable(TIMER1_BASE, TIMER_BOTH);

        } else {        // the timer is already running

                // Timer already running, check to see if we need to reload timer value.
                if (timeCompare(lastActuateTime, actuationTime) == MORE) {
#ifdef LCD_DEBUG
                        debugMessage("replacing timer");
#endif

                        TimerDisable(TIMER1_BASE, TIMER_BOTH);
                        TimerIntDisable(TIMER1_BASE, TIMER_TIMA_TIMEOUT);
                        // replace timer. First, update the actuatorTimeValues of the queue.
                        // put it at the beginning of the queue.
                        // actuatorRunning is the ID of the last running actuator
                        // now insert the time into the queue of waiting timers for this particular actuator.
                        // if this actuator currently have more than one waiting time, update the head
                        // insert this timer to the previous element of where the head is pointing right now.
                        if (actuatorArrayHeadPtrs[actuatorRunning] == 0) {
                                actuatorArrayHeadPtrs[actuatorRunning] = MAX_ACTUATOR_TIMER_VALUES - 1;
                        } else {
                                actuatorArrayHeadPtrs[actuatorRunning]--;
                        }
                        // set the head to the previous lastActuateTime.
                        actuatorTimerValues[actuatorRunning][actuatorArrayHeadPtrs[actuatorRunning]] = lastActuateTime;
                        actuatorArrayCounts[actuatorRunning]++;

                        // replace timer.
                        actuatorRunning = actuatorToActuate;
                        lastActuateTime = actuationTime;

                        getRealTime(&physicalTime);
                        if (timeSub(actuationTime, physicalTime, &actuationLeftOverTime) < 0) {
                                TimerLoadSet(TIMER1_BASE, TIMER_BOTH, 0);
                                actuatorTimerInterruptSecsLeft = 0;
                        } else {
                                TimerLoadSet(TIMER1_BASE, TIMER_BOTH, convertNsecsToCycles(actuationLeftOverTime.nsecs));
                                actuatorTimerInterruptSecsLeft = actuationLeftOverTime.secs;
                        }
                        TimerIntEnable(TIMER1_BASE, TIMER_TIMA_TIMEOUT);
                        TimerEnable(TIMER1_BASE, TIMER_BOTH);
                } else {
                        // we don't need to reload the timer, but we need to put this event into the end of the queue.
                        // actuatorToActuate is now the ID of the actuator
                        // FIXME: this assumes each actuator has only one input port...
                        actuatorTimerValues[actuatorToActuate][actuatorArrayTailPtrs[actuatorToActuate]] = actuationTime;
                        actuatorArrayCounts[actuatorToActuate]++;

                        actuatorArrayTailPtrs[actuatorToActuate]++;
                        if (actuatorArrayTailPtrs[actuatorToActuate] == MAX_ACTUATOR_TIMER_VALUES) {
                                actuatorArrayTailPtrs[actuatorToActuate] = 0;
                        }
                }
        }

        // actuatorArrayHead/TailPtrs/Counts are only added here, so check that we didn't have a overflow
        for (actuatorID = 0; actuatorID < numActuators; actuatorID++) {
                if (actuatorArrayHeadPtrs[actuatorID] == actuatorArrayTailPtrs[actuatorID]
                && actuatorArrayCounts[actuatorID] != 0) {
                        die("MAX_ACTUATOR_TIMER_VALUES is not large enough.");
                }
                if (actuatorArrayHeadPtrs[actuatorID] != actuatorArrayTailPtrs[actuatorID]
                && actuatorArrayCounts[actuatorID] == 0) {
                        die("something wrong with actuator ptr algorithm.");
                }
        }
        enableInterrupts();
}

void Timer1IntHandler(void) {

        int i;
        Time physicalTime;

#ifdef LCD_DEBUG
        debugMessageNumber("Act int:", actuatorRunning);
#endif

        // Clear the timer interrupt.
        //
        TimerIntClear(TIMER1_BASE, TIMER_TIMA_TIMEOUT);
        if (actuatorTimerInterruptSecsLeft > 0) {
                actuatorTimerInterruptSecsLeft--;
                // setup this timer to run once more
                TimerLoadSet(TIMER1_BASE, TIMER_BOTH, TIMER_ROLLOVER_CYCLES);
                TimerEnable(TIMER1_BASE, TIMER_BOTH);
                return;
        }

        // run the actuator actuation function to assert the output signal.
        if (actuatorRunning == -1) {
                die("no actuator to actuate");
        }
        actuatorActuations[actuatorRunning]();

        // When the timer returns to signal a new interrupt has been written, we need to check to see if we have more interrupts
        lastActuateTime = MAX_TIME;
        for (i = 0; i < numActuators; i++) {
                if (actuatorArrayCounts[i] > 0){
                        if (timeCompare(actuatorTimerValues[i][actuatorArrayHeadPtrs[i]], lastActuateTime) == LESS) {
                                lastActuateTime = actuatorTimerValues[i][actuatorArrayHeadPtrs[i]];
                                actuatorRunning = i;
                        }
                }
        }

#ifdef LCD_DEBUG
        debugMessageNumber("To Act secs:", lastActuateTime.secs);
        debugMessageNumber("To Act nsecs:", lastActuateTime.nsecs);
#endif

        if (timeCompare(lastActuateTime, MAX_TIME) != EQUAL) {
                // there is another actuation to do.
                Time actuationLeftOverTime;

                                #ifdef LCD_DEBUG
                debugMessageNumber("next actuator", actuatorRunning);
                #endif
                //Setup the interrupts for the timer timeouts
                //
                TimerIntEnable(TIMER1_BASE,TIMER_TIMA_TIMEOUT);

                // one less off of the queue.
                actuatorArrayHeadPtrs[actuatorRunning]++;
                if (actuatorArrayHeadPtrs[actuatorRunning] == MAX_ACTUATOR_TIMER_VALUES) {
                        actuatorArrayHeadPtrs[actuatorRunning] = 0;
                }
                actuatorArrayCounts[actuatorRunning]--;

                // FIXME: there might be a concurrency issue here, actuatorTimerInterrupt is set to true,
                // yet we could have another interrupt coming in right after it that tries to set another timer interrupt,
                // in which case it would try to access the else{} part of this function.
                // FIXED: this is taken care by all the interrupts having the same priority, thus within this
                // ISR there wouldn't be any preemption.

                getRealTime(&physicalTime);
                if (timeSub(lastActuateTime, physicalTime, &actuationLeftOverTime) < 0) {
                        TimerLoadSet(TIMER1_BASE, TIMER_BOTH, 0);
                        actuatorTimerInterruptSecsLeft = 0;
                } else {
                        TimerLoadSet(TIMER1_BASE, TIMER_BOTH, convertNsecsToCycles(actuationLeftOverTime.nsecs));
                        actuatorTimerInterruptSecsLeft = actuationLeftOverTime.secs;
                }
                TimerEnable(TIMER1_BASE, TIMER_BOTH);
        } else {

                // no more actuation needed at this time.
                actuatorRunning = -1;
                // disable the timer
                TimerDisable(TIMER1_BASE, TIMER_BOTH);
                IntDisable(INT_TIMER1A);
                IntDisable(INT_TIMER1B);
                TimerIntDisable(TIMER1_BASE, TIMER_TIMA_TIMEOUT);
                TimerIntDisable(TIMER1_BASE, TIMER_TIMB_TIMEOUT);
        }
}
/**/

/*** initPDBlock***/
// the platform dependent initialization code goes here.
initializeTimers();
initializePDSystem();
initializeHardware();
initializeInterrupts();
/**/

/*** initPDCodeBlock ***/
void initializeTimers(void) {
        SysCtlPeripheralEnable(SYSCTL_PERIPH_TIMER0);
        SysCtlPeripheralEnable(SYSCTL_PERIPH_TIMER1);

        IntPrioritySet(INT_TIMER0A, 0x40);
        IntPrioritySet(INT_TIMER0B, 0x40);
        IntPrioritySet(INT_TIMER1A, 0x40);
        IntPrioritySet(INT_TIMER1B, 0x40);
}

void initializePDSystem() {
        // Set system clock to 50MHz
        SysCtlClockSet(SYSCTL_SYSDIV_4 | SYSCTL_USE_PLL | SYSCTL_OSC_MAIN | SYSCTL_XTAL_8MHZ);

        TIMER_ROLLOVER_CYCLES = SysCtlClockGet();

        // since systick register is only 24 bits, it can only hold values
        // between 0 and 1677xxxx, assuming the main system clock runs at 50MHz,
        // TIMER_ROLLOVER_CYCLES >> 2 gives 12500000. In other words, systick
        // rolls over 4 times every second.
        SysTickPeriodSet(TIMER_ROLLOVER_CYCLES >> 2);
        IntPrioritySet(FAULT_SYSTICK, 0x00);
        SysTickEnable();
        IntEnable(FAULT_SYSTICK);  //sys tick vector
        // SVC should have lower priority than systick, but SVC should have
        // higher or the same priority than all other peripheral interrupts.
        // It is ok for systick to preempt SVC, since the SVC does not
        // leave the stack in an unknown state.
            IntPrioritySet(FAULT_SVCALL, 0x20);
        // Initialize LCD at 4 MHz
        RIT128x96x4Init(4000000);
        RIT128x96x4StringDraw("PtidyOSv1.0", 36,  0, 15);

#ifndef LCD_DEBUG
        RIT128x96x4Disable();
        //RIT128x96x4DisplayOff();
#endif
}

void initializeInterrupts(void) {
        IntMasterEnable();
}
/**/

/*** preinitPDBlock()***/
/**/

/*** wrapupPDBlock() ***/
/**/


/*** mainLoopBlock ***/
void execute() {
    Event* event = NULL;
    Time processTime;
    Time platformTime;
        // Get the current platform time. This time is later used to
        // perform safe-to-process. This function must be called
        // before interrupts are disabled to ensure DE semantics.
    getRealTime(&platformTime);
    disableInterrupts();
    while (true) {
        // If event is null, then return the highest priority event
        // from the queue. Otherwise, return the next event pointed
        // to by event. Return NULL if there are no more events
        // in the event queue. The event queue is sorted in
        // deadline order.
        event = peekNextEvent(event);
        // If there are no more events in the event queue, break
        // out of the while loop.
                if (!event) {
            break;
        }
        // If this event's priority is higher than the last priority
        // saved in storePriority(), then continue.
        // The priority here is the priority of the event,
        // not the priority of the interrupt.
        if (higherPriority(event)) {
            // check if this event is safe to process.
            safeToProcess(event, &processTime);
            if (timeCompare(platformTime, processTime) >= 0) {
                // Store the priority of the previous interrupt.
                // Stored priority is later used for comparison in
                // higherPriority().
                queuePriority(event);
                // Get all events of the same timestamp, and share
                // the same destination equivalence class.
                // Remove these events from the event queue.
                removeAndPropagateSameTagEvents(event);
                setCurrentModelTag(event);
                // Ready to process the next event, so first enable
                // interrupts.
                enableInterrupts();
                // Execute this event by firing the corresponding
                // actor. During this process more events may
                // be posted onto the queue
                fireActor(event);
                                // Get the current platform time. This time is later used to
                                // perform safe-to-process. This function must be called
                                // before interrupts are disabled to ensure DE semantics.
                            getRealTime(&platformTime);
                // We are ready to look at the next event in the
                // event queue. Before doing that interrupts need
                // to be disabled
                disableInterrupts();
                // The executed event can now be freed into the
                // pool of available events
                                freeEvent(event);
                                // This event has finished execution. The priority of
                                // this event can be forgotten. We forget by decrementing
                                // the stackedDeadlineIndex.
                                stackedDeadlineIndex--;
                                // Reset event to null so the next peekNextEvent()
                // looks at the top event.
                event = NULL;
            } else {
                // save the current process level
                uint32 localProcessVersion = processVersion;
                // This event is not safe to process yet. Set
                // timed interrupt to run this event when platform
                // time has passed for it to be safe to process.
                if (timeCompare(processTime, lastTimerInterruptTime) == LESS) {
                    lastTimerInterruptTime = processTime;
                    setTimedInterrupt(&processTime);
                }
                                // Enables higher priority events to preempt this processEvents().
                                // This is especially useful if the size of the event queue is
                                // very large, and takes a long time to traverse through.
                                enableInterrupts();
                                disableInterrupts();
                                if (localProcessVersion != processVersion) {
                                        // If processEvents ran during the last interrupt enable,
                                        // then we should traverse the event queue anymore.
                                        // Instead we simply return.
                                        break;
                                }
                // If processEvents did not run during the last interrupt enable,
                // then we keep analyze events in the event queue by going back
                // to the beginning of the loop.
                }
        } else {
            // This event is of lower priority than the one
            // currently executing, break out of the while loop.
            break;
        }//end while().
    }
        // End of processEvents(), enable interrupts.
    enableInterrupts();
        // Go into an infinite loop to wait for a wakeup signal.
    while (1);
}
/**/



/*** assemblyFileBlock($externs, $GPIOAHandler, $GPIOBHandler, $GPIOCHandler, $GPIODHandler, $GPIOEHandler, $GPIOFHandler, $GPIOGHandler, $GPIOHHandler) ***/
;******************************************************************************
;
; startup_rvmdk.S - Startup code for use with Keil's uVision.
;
; Copyright (c) 2005-2011 Luminary Micro, Inc.  All rights reserved.
;
; Software License Agreement
;
; Luminary Micro, Inc. (LMI) is supplying this software for use solely and
; exclusively on LMI's microcontroller products.
;
; The software is owned by LMI and/or its suppliers, and is protected under
; applicable copyright laws.  All rights are reserved.  You may not combine
; this software with "viral" open-source software in order to form a larger
; program.  Any use in violation of the foregoing restrictions may subject
; the user to criminal sanctions under applicable laws, as well as to civil
; liability for the breach of the terms and conditions of this license.
;
; THIS SOFTWARE IS PROVIDED "AS IS".  NO WARRANTIES, WHETHER EXPRESS, IMPLIED
; OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, IMPLIED WARRANTIES OF
; MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE APPLY TO THIS SOFTWARE.
; LMI SHALL NOT, IN ANY CIRCUMSTANCES, BE LIABLE FOR SPECIAL, INCIDENTAL, OR
; CONSEQUENTIAL DAMAGES, FOR ANY REASON WHATSOEVER.
;
; This is part of revision 1900 of the Stellaris Peripheral Driver Library.
;
;******************************************************************************

;******************************************************************************
;
; <o> Stack Size (in Bytes) <0x0-0xFFFFFFFF:8>
;
;******************************************************************************
Stack   EQU     0x00001000

;******************************************************************************
;
; <o> Heap Size (in Bytes) <0x0-0xFFFFFFFF:8>
;
;******************************************************************************
Heap    EQU     0x00000000

;******************************************************************************
;
; Allocate space for the stack.
;
;******************************************************************************
        AREA    STACK, NOINIT, READWRITE, ALIGN=3
StackMem
        SPACE   Stack
__initial_sp

;******************************************************************************
;
; Allocate space for the heap.
;
;******************************************************************************
        AREA    HEAP, NOINIT, READWRITE, ALIGN=3
__heap_base
HeapMem
        SPACE   Heap
__heap_limit

;******************************************************************************
;
; Indicate that the code in this file preserves 8-byte alignment of the stack.
;
;******************************************************************************
        PRESERVE8

;******************************************************************************
;
; Place code into the reset code section.
;
;******************************************************************************
        AREA    RESET, CODE, READONLY
        THUMB

;******************************************************************************

;******************************************************************************
;
; External declaration for the interrupt handler used by the application.
;
;******************************************************************************

$externs
        EXTERN  Timer0IntHandler
                EXTERN  Timer1IntHandler
                EXTERN  SysTickHandler

;******************************************************************************
;
; The vector table.
;
;******************************************************************************
        EXPORT  __Vectors
__Vectors
        DCD     StackMem + Stack            ; Top of Stack
        DCD     Reset_Handler               ; Reset Handler
        DCD     NmiSR                       ; NMI Handler
        DCD     FaultISR                    ; Hard Fault Handler
        DCD     IntDefaultHandler           ; MPU Fault Handler
        DCD     IntDefaultHandler           ; Bus Fault Handler
        DCD     IntDefaultHandler           ; Usage Fault Handler
        DCD     0                           ; Reserved
        DCD     0                           ; Reserved
        DCD     0                           ; Reserved
        DCD     0                           ; Reserved
        DCD     SVCallHandler                    ; SVCall Handler
        DCD     IntDefaultHandler           ; Debug Monitor Handler
        DCD     0                           ; Reserved
        DCD     IntDefaultHandler           ; PendSV Handler
        DCD     SysTickHandler                     ; SysTick Handler
        DCD     $GPIOAHandler               ; GPIO Port A
        DCD     $GPIOBHandler               ; GPIO Port B
        DCD     $GPIOCHandler               ; GPIO Port C
        DCD     $GPIODHandler               ; GPIO Port D
        DCD     $GPIOEHandler               ; GPIO Port E
        DCD     IntDefaultHandler           ; UART0
        DCD     IntDefaultHandler           ; UART1
        DCD     IntDefaultHandler           ; SSI
        DCD     IntDefaultHandler           ; I2C
        DCD     IntDefaultHandler           ; PWM Fault
        DCD     IntDefaultHandler           ; PWM Generator 0
        DCD     IntDefaultHandler           ; PWM Generator 1
        DCD     IntDefaultHandler           ; PWM Generator 2
        DCD     IntDefaultHandler           ; Quadrature Encoder
        DCD     IntDefaultHandler           ; ADC Sequence 0
        DCD     IntDefaultHandler           ; ADC Sequence 1
        DCD     IntDefaultHandler           ; ADC Sequence 2
        DCD     IntDefaultHandler           ; ADC Sequence 3
        DCD     IntDefaultHandler           ; Watchdog
        DCD     Timer0IntHandler            ; Timer 0A
        DCD     IntDefaultHandler           ; Timer 0B
        DCD     Timer1IntHandler            ; Timer 1A
        DCD     IntDefaultHandler           ; Timer 1B
        DCD     IntDefaultHandler           ; Timer 2A
        DCD     IntDefaultHandler           ; Timer 2B
        DCD     IntDefaultHandler           ; Comp 0
        DCD     IntDefaultHandler           ; Comp 1
        DCD     IntDefaultHandler           ; Comp 2
        DCD     IntDefaultHandler           ; System Control
        DCD     IntDefaultHandler           ; Flash Control
        DCD     $GPIOFHandler               ; GPIO Port F
        DCD     $GPIOGHandler               ; GPIO Port G
        DCD     $GPIOHHandler               ; GPIO Port H
        DCD     IntDefaultHandler           ; UART2 Rx and Tx
        DCD     IntDefaultHandler           ; SSI1 Rx and Tx
        DCD     IntDefaultHandler           ; Timer 3 subtimer A
        DCD     IntDefaultHandler           ; Timer 3 subtimer B
        DCD     IntDefaultHandler           ; I2C1 Master and Slave
        DCD     IntDefaultHandler           ; Quadrature Encoder 1
        DCD     IntDefaultHandler           ; CAN0
        DCD     IntDefaultHandler           ; CAN1
        DCD     IntDefaultHandler           ; CAN2
        DCD     IntDefaultHandler           ; Ethernet
        DCD     IntDefaultHandler           ; Hibernate

;******************************************************************************
;
; This is the code that gets called when the processor first starts execution
; following a reset event.
;
;******************************************************************************
        EXPORT  Reset_Handler
Reset_Handler
        ;
        ; Call the C library enty point that handles startup.  This will copy
        ; the .data section initializers from flash to SRAM and zero fill the
        ; .bss section.
        ;
        IMPORT  __main
        B       __main

;******************************************************************************
;
; This is the code that gets called when the processor receives a NMI.  This
; simply enters an infinite loop, preserving the system state for examination
; by a debugger.
;
;******************************************************************************
NmiSR
        B       NmiSR

;******************************************************************************
;
; This is the code that gets called when the processor receives a fault
; interrupt.  This simply enters an infinite loop, preserving the system state
; for examination by a debugger.
;
;******************************************************************************
FaultISR
        B       FaultISR

;******************************************************************************
;
; This is the code that gets called when the processor receives an unexpected
; interrupt.  This simply enters an infinite loop, preserving the system state
; for examination by a debugger.
;
;******************************************************************************
IntDefaultHandler
        B       IntDefaultHandler

;******************************************************************************
;
; Function called to save state at the end of an ISR
;
;******************************************************************************
saveState
        POP                        {R0, R1}
        PUSH        {R4-R11}
        PUSH        {R4-R11}
        PUSH                {R0, R1}
        BX                        LR

;******************************************************************************
;
; Function called to add stack to the end of an ISR
;
;******************************************************************************
; IMPORTANT: this subroutine assumes addStack is compiled to run BEFORE popping out of ISR
addStack
        CPSID                        I
    MRS                R1, MSP            ; move stackpointer to R1
    ; Copy previous R4, LR to very top of future stack
    LDRD        R2, R3, [R1, #0]
    STRD         R2, R3, [R1, #-32]
    MOV                R3, #16777216        ; load R3 with the desired xPSR
    STR                R3, [R1, #4]                        ; store it for use of processEvents
    LDR                R3, =processEvents                ; find where "processEvents + 2" is
    ADD                R3, R3, #2
    STR                R3, [R1, #0]                        ; store this value into correct place in memory.
    SUB                R1, R1, #32
    MSR                MSP, R1            ; update Main Stack Pointer
        CPSIE                        I
    BX                LR

;******************************************************************************
;
; Function called to restore to the state before interrupt
;
;******************************************************************************
stackRestore
        ; we do not need to disable interrupts for this routine, because it
    ; is triggered through a SVC call, which has higher priority than
    ; all other external interrupts in the system.
    MRS                R0, MSP            ; move stackpointer to R0
    ADD                R0, R0, #64     ; the stack that was just pushed by the ISR is ignored
    ;ADD                R0, R0, #32     ; the stack that was just pushed by the ISR is ignored
        MSR                MSP, R0            ; instead we use the stack that was saved before the first ISR
    POP                {R4-R11}
        ; this doesn't pass the compiler: POP                                        {SP, PC}
        BX                LR                    ; branch back to end this ISR

loadState
        POP                        {R0, R1}
        POP        {R4-R11}
        POP        {R4-R11}
        PUSH                {R0, R1}
        BX                        LR

;******************************************************************************
;
; SVCall handler, right now since we only have one call handler, and we only use it
; to restore the stack, we just point it to that function.
; later on if additional ones needs to be added, we should follow the example in
; SWI to have a list of SWI functions we can call.
;
;******************************************************************************
SVCallHandler
                        B                stackRestore

;******************************************************************************
;
; External declaration for the stack manipulation used by the application.
;
;******************************************************************************
                                EXPORT addStack
                                EXPORT saveState
                                EXPORT loadState
                                EXPORT stackRestore
                                IMPORT processEvents

;******************************************************************************
;
; Make sure the end of this section is aligned.
;
;******************************************************************************
        ALIGN

;******************************************************************************
;
; Some code in the normal code section for initializing the heap and stack.
;
;******************************************************************************
        AREA    |.text|, CODE, READONLY

;******************************************************************************
;
; The function expected of the C library startup code for defining the stack
; and heap memory locations.  For the C library version of the startup code,
; provide this function so that the C library initialization code can find out
; the location of the stack and heap.
;
;******************************************************************************
    IF :DEF: __MICROLIB
        EXPORT  __initial_sp
        EXPORT  __heap_base
        EXPORT  __heap_limit
    ELSE
        IMPORT  __use_two_region_memory
        EXPORT  __user_initial_stackheap
__user_initial_stackheap
        LDR     R0, =HeapMem
        LDR     R1, =(StackMem + Stack)
        LDR     R2, =(HeapMem + Heap)
        LDR     R3, =StackMem
        BX      LR
    ENDIF

;******************************************************************************
;
; Make sure the end of this section is aligned.
;
;******************************************************************************
        ALIGN

;******************************************************************************
;
; Tell the assembler that we're done.
;
;******************************************************************************
        END
/**/
