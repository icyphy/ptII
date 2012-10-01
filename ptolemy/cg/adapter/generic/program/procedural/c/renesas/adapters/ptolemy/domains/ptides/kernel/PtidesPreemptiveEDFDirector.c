
/*** StructDefBlock ***/

// ----------------------------------------------------------------
// ---------- Hardware library includes ---------------------------
#include <machine.h>
#include "epl.h"
#include "vect.h"
#include "okmac.h"
#include "dp.h"
#include "iodefine.h"
#include "rsksh7216.h"
#include "usb_hal.h"
#include "usb_cdc.h"
#include "usb_cdc_app.h"
#include "usb_common.h"
#include "r_usbf_cdc.h"

#define LESS -1
#define MORE 1
#define EQUAL 0
#define MAX_EVENTS 120
#define false 0
#define true 1
#define exit(0) ;
/**/

/*** CommonTypeDefinitions ***/
typedef unsigned long        uint32;
typedef unsigned int        uint16;
typedef unsigned char        uint8;
typedef signed long                int32;
typedef signed int                int16;
typedef signed char                int8;
//typedef boolean                        uint8;

typedef struct {
    uint32 secs;
    uint32 nsecs;
} Time;
typedef struct {
        int32 secs;
        int32 nsecs;
} SignedTime;
typedef struct {
    Time timestamp;
    uint16 microstep;
} Tag;
typedef struct event {
    union {
        int16 int_Value;
        double double_Value;
        int32 long_Value;
        int8 char_Value;
        uint32 boolean_Value;
    } Val;
    Tag tag;
    uint32 depth;
    void (*fireMethod)();
    struct event** sinkEvent;
    Time deadline;
    SignedTime offsetTime;
    struct event* nextEvent;
    struct event* prevEvent;
} Event;

Event eventMemory[MAX_EVENTS];
Event* DEADLINE_QUEUE_HEAD = NULL;
Event* DEADLINE_QUEUE_TAIL = NULL;
Event* FREE_EVENT_LIST = NULL;

static Time MAX_TIME = {(uint32)-1, (uint32)-1};

/**/

/*** globalVars($actuatorGlobalVars, $interruptPragmas) ***/

volatile Time currentModelTime;
volatile int16 currentMicrostep;
volatile int16 stackedDeadlineIndex = -1;
volatile Time executingDeadlines[MAX_EVENTS];
volatile void (*executingActors[MAX_EVENTS])();
//volatile Tag executingModelTag[MAX_EVENTS];
//volatile int16 stackedModelTagIndex = -1;
volatile Time lastTimerInterruptTime = {(uint32)-1, (uint32)-1};

uint32 divideByValue, intDel = 84;
uint32 Seconds = 0, nanoSeconds = 0;
uint32 xxxS, xxx;
uint32 zzzS[10], zzz[10];
uint32 zRd = 0, zWr = 0;

$actuatorGlobalVars

uint32 actWidth = 2000;


$interruptPragmas
/**/

/*** FuncProtoBlock ***/
// empty prototype block
/**/


/*** FuncBlock ***/
double multiply_Int_Double(int a1, double a2) {
    return a1 * a2;
}
double divide_Double_Double(double a1, double a2) {
    return a1 / a2;
}
int16 compareTime(const Time time1, const Time time2) {
    if (time1.secs < time2.secs) {
        return LESS;
    } else if (time1.secs == time2.secs && time1.nsecs < time2.nsecs) {
        return LESS;
    } else if (time1.secs == time2.secs && time1.nsecs == time2.nsecs) {
        return EQUAL;
    }
    return MORE;
}

void addTime(const Time time1, const Time time2, Time* timeSum) {
    timeSum->secs = time1.secs + time2.secs;
    timeSum->nsecs = time1.nsecs + time2.nsecs;
    if (timeSum->nsecs >= 1000000000) {
        timeSum->nsecs -= 1000000000;
        timeSum->secs++;
    }
}

int16 subTime(const Time time1, const Time time2, Time* timeSub) {
    if (compareTime(time1, time2) == LESS) {
        return -1;
    }
    timeSub->secs = time1.secs - time2.secs;
    if (time1.nsecs < time2.nsecs) {
        timeSub->secs--;
        timeSub->nsecs = time1.nsecs + 1000000000 - time2.nsecs;
    } else {
        timeSub->nsecs = time1.nsecs - time2.nsecs;
    }
    return 0;
}

Event* newEvent(void) {
    Event* result;
        set_imask(15);

    result = FREE_EVENT_LIST;
    FREE_EVENT_LIST = FREE_EVENT_LIST->nextEvent;

        set_imask(0);
    return result;
}

void freeEvent(Event* thisEvent) {
    thisEvent->prevEvent->nextEvent = FREE_EVENT_LIST;
    FREE_EVENT_LIST = thisEvent;
}

int16 compareEvent(const Event* event1, const Event* event2) {
    int16 compare;
    if (event1 == NULL || event2 == NULL) {
        DBG(("compare NULL events!\r\n"));
    }
    compare = compareTime(event1->deadline, event2->deadline);
    if (compare != EQUAL) {
        return compare;
    } else {
        compare = compareTime(event1->tag.timestamp, event2->tag.timestamp);
        if (compare != EQUAL) {
            return compare;
        } else {
            if (event1->tag.microstep < event2->tag.microstep) {
                return LESS;
            } else if (event1->tag.microstep > event2->tag.microstep) {
                return MORE;
            } else {
                if (event1->depth < event2->depth) {
                    return LESS;
                } else if (event1->depth > event2->depth) {
                    return MORE;
                } else {
                    return EQUAL;
                }
            }
        }
    }
}

void addEvent(Event* newEvent) {
    Event* compareDeadline;
        set_imask(15);

    compareDeadline = DEADLINE_QUEUE_HEAD;
    if (compareDeadline == NULL) {
        DEADLINE_QUEUE_HEAD = newEvent;
        DEADLINE_QUEUE_TAIL = newEvent;
        newEvent->prevEvent = NULL;
        newEvent->nextEvent = NULL;
    } else {
        if (compareEvent(newEvent, DEADLINE_QUEUE_HEAD) <= 0) {
            newEvent->nextEvent = DEADLINE_QUEUE_HEAD;
            DEADLINE_QUEUE_HEAD->prevEvent = newEvent;
            newEvent->prevEvent = NULL;
            DEADLINE_QUEUE_HEAD = newEvent;
        } else {
                        compareDeadline = DEADLINE_QUEUE_TAIL;
            while (compareEvent(newEvent, compareDeadline) < 0) {
                                compareDeadline = compareDeadline->prevEvent;
                if (compareDeadline == NULL) {
                    DBG(("compareDeadline == NULL!\r\n"));
                                }
            }
            newEvent->prevEvent = compareDeadline;
            newEvent->nextEvent = compareDeadline->nextEvent;
            compareDeadline->nextEvent = newEvent;
            if (compareDeadline != DEADLINE_QUEUE_TAIL) {
                newEvent->nextEvent->prevEvent = newEvent;
            } else {
                DEADLINE_QUEUE_TAIL = newEvent;
            }
        }
    }

        set_imask(0);
}

void removeEvent(Event* event) {
        if(event->prevEvent == NULL && event->nextEvent == NULL) {
                DEADLINE_QUEUE_HEAD = NULL;
                DEADLINE_QUEUE_TAIL = NULL;
        } else {
            if (event->prevEvent != NULL) {
                event->prevEvent->nextEvent = event->nextEvent;
            } else {
                DEADLINE_QUEUE_HEAD = event->nextEvent;
                        DEADLINE_QUEUE_HEAD->prevEvent = NULL;
            }
            if (event->nextEvent != NULL) {
                event->nextEvent->prevEvent = event->prevEvent;
            } else {
                DEADLINE_QUEUE_TAIL = event->prevEvent;
                        DEADLINE_QUEUE_TAIL->nextEvent = NULL;
            }
        }
}

// Remove this event from the event queue, as well as all other
// events that share the same timestamp, as well as destination actor.
void removeAndPropagateSameTagEvents(Event* thisEvent) {
    Event* nextEvent = thisEvent->nextEvent;
    Event* lastEvent = thisEvent;
    *(thisEvent->sinkEvent) = (Event*)thisEvent;
    removeEvent(thisEvent);
    // Now find the next event see we should process it at the same time.
    while(nextEvent && (compareTime(nextEvent->tag.timestamp, thisEvent->tag.timestamp) == EQUAL)
                        && (nextEvent->tag.microstep == thisEvent->tag.microstep) && nextEvent->fireMethod == thisEvent->fireMethod) {
            *(nextEvent->sinkEvent) = (Event*)nextEvent;
                removeEvent(nextEvent);
        lastEvent = nextEvent;
        nextEvent = nextEvent->nextEvent;
        }
    // Make this linked list semi-circular by pointing
    // the prevEvent of thisEvent to the end of the list.
    // This is used later in freeEvent().
    thisEvent->prevEvent = lastEvent;
}

Event* peekEvent(Event* thisEvent) {
    if (thisEvent == NULL) {
        return DEADLINE_QUEUE_HEAD;
    } else {
        return thisEvent->nextEvent;
    }
}

void queuePriority(Event* event) {
    stackedDeadlineIndex++;
    if (stackedDeadlineIndex == MAX_EVENTS) {
        DBG(("stackedDeadlineIndex exceeds MAX_EVENTS\r\n"));
    }
    executingDeadlines[stackedDeadlineIndex] = event->deadline;
    executingActors[stackedDeadlineIndex] = event->fireMethod;
}

void setCurrentModelTag(Event* currentEvent) {
    currentModelTime = currentEvent->tag.timestamp;
    currentMicrostep = currentEvent->tag.microstep;
}

void fireActor(Event* thisEvent) {
    if (thisEvent->fireMethod != NULL){
        (thisEvent->fireMethod)();
    } else {
        DBG(("incorrect fire method\r\n"));
    }
}

uint16 higherPriority(const Event* const event) {
    int16 i;
    if (stackedDeadlineIndex < 0) {
        return TRUE;
    } else if (compareTime(executingDeadlines[stackedDeadlineIndex], event->deadline) == LESS) {
        return FALSE;
    } else {
        for (i = 0; i <= stackedDeadlineIndex; i++) {
            if (executingActors[i] == event->fireMethod) {
                return FALSE;
            }
        }
        return TRUE;
    }
}

void safeToProcess(const Event* const thisEvent, Time* safeTimestamp) {
    Time tempTime;
        if (thisEvent->offsetTime.secs < 0 || (thisEvent->offsetTime.secs == 0
                        && thisEvent->offsetTime.nsecs < 0)) {
                tempTime.secs = (uint32) (-thisEvent->offsetTime.secs);
                tempTime.nsecs = (uint32) (-thisEvent->offsetTime.nsecs);
                addTime(thisEvent->tag.timestamp, tempTime, safeTimestamp);
        } else {
                int16 out;
                tempTime.secs = (uint32) (thisEvent->offsetTime.secs);
                tempTime.nsecs = (uint32) (thisEvent->offsetTime.nsecs);
                out = subTime(thisEvent->tag.timestamp, tempTime, safeTimestamp);
                if (out == -1) {
                        safeTimestamp->secs = 0;
                        safeTimestamp->nsecs = 0;
                }
        }
}

void getRealTime(Time * const physicalTime){
        physicalTime->secs = Seconds;
        physicalTime->nsecs = nanoSeconds + MTU23.TCNT*(4*divideByValue/2);
}





void setTimedInterrupt(const Time* safeToProcessTime) {
        zzzS[0] = safeToProcessTime->secs - Seconds;
    if (safeToProcessTime->nsecs < nanoSeconds) {
        zzzS[0]--;
        zzz[0] = safeToProcessTime->nsecs + 1000000000 - nanoSeconds;
    } else {
                zzz[0] = safeToProcessTime->nsecs - nanoSeconds;
    }

        if((zzzS[0] == 0) && (zzz[0] < ((4*divideByValue/2)*(65536 + intDel)))){
                MTU20.TGRE = zzz[0]/(4*divideByValue/2);
                MTU20.TSR2.BIT.TGFE = 0;
                MTU20.TIER2.BIT.TGIEE = 1;
        }
}

void processEvents() {
    Event* event = NULL;
    Time processTime;
    Time platformTime;
    getRealTime(&platformTime);
    set_imask(15);
    event = peekEvent(NULL);
    while (event && higherPriority(event)) {
        safeToProcess(event, &processTime);
        if (compareTime(platformTime, processTime) >= 0) {
            queuePriority(event);
            removeAndPropagateSameTagEvents(event);
            setCurrentModelTag(event);
            set_imask(0);
            fireActor(event);
            getRealTime(&platformTime);
            set_imask(15);
            freeEvent(event);
            stackedDeadlineIndex--;
            event = NULL;
        }
        else {
            if (compareTime(processTime, lastTimerInterruptTime) == LESS) {
                lastTimerInterruptTime.secs = processTime.secs;
                lastTimerInterruptTime.nsecs = processTime.nsecs;
                setTimedInterrupt(&processTime);
            }
        }
        event = peekEvent(event);
    }
    /*
    if (stackedModelTagIndex >= 0) {
        currentMicrostep = executingModelTag[stackedModelTagIndex].microstep;
        currentModelTime = executingModelTag[stackedModelTagIndex].timestamp;
        stackedModelTagIndex--;
    } else {
        DBG(("cannot restore model tag\r\n"));
    }
    */
    set_imask(0);
}

/**/

/*** actuationBlock($actuationFunction, $Letter) ***/
void $actuationFunction(void) {
        while(MTU20.TSR.BIT.TGF$Letter != 1)
                ;
        MTU20.TSR.BIT.TGF$Letter = 0;
        set_imask(15);
        actRd$Letter = actRd$Letter+1;
        if (actRd$Letter == 10)
                actRd$Letter = 0;
        if(actSt$Letter == 0) {
                actSt$Letter = 1;
        }
        else {
                actSt$Letter = 0;
        }

        if((actWr$Letter != actRd$Letter) && (actNs$Letter[actRd$Letter] <
                        ((4*divideByValue/2)*(65536 + intDel)))) {
                MTU20.TGR$Letter = actNs$Letter[actRd$Letter]/(4*divideByValue/2);
                MTU20.TSR.BIT.TGF$Letter = 0;
                MTU20.TIER.BIT.TGIE$Letter = 1;

                if(actSt$Letter == 0) {
                        MTU20.TIOR.BIT.IO$Letter = 2;
                } else {
                        MTU20.TIOR.BIT.IO$Letter = 5;
                }
        } else {
                MTU20.TIER.BIT.TGIE$Letter = 0;
                MTU20.TIOR.BIT.IO$Letter = 0;
        }
        set_imask(0);
}
/**/

void CG_3_T_Rex_noNetwork_v1_ContactController_ContactControllerMicro_ActuatorSetup2(void) {
/*$P*/
//void xxxxx(void) {
    /* Fire CG_3_T_Rex_noNetwork_v1_ContactController_ContactControllerMicro_ActuatorSetup2 */
    set_imask(15);
        actSA[actWrA] = currentModelTime.secs - Seconds;
    if (currentModelTime.nsecs < nanoSeconds) {
                actSA[actWrA]--;
                actNsA[actWrA] = currentModelTime.nsecs + 1000000000 - nanoSeconds;
    } else {
                actNsA[actWrA] = currentModelTime.nsecs - nanoSeconds;
    }
    set_imask(0);

    if((MTU20.TIOR.BIT.IOA == 0) && (actSA[actWrA] == 0) && (actNsA[actWrA] < ((4*divideByValue/2)*(65536 + intDel)))) {
        MTU20.TGRA = actNsA[actWrA]/(4*divideByValue/2);
        MTU20.TSR.BIT.TGFA = 0;
        MTU20.TIER.BIT.TGIEA = 1;
        if(actStA == 0)
        MTU20.TIOR.BIT.IOA = 2;
        else
        MTU20.TIOR.BIT.IOA = 5;
    }
        actWrA = actWrA+1;
        if(actWrA == 10)
                actWrA = 0;

        actSA[actWrA] = currentModelTime.secs - Seconds;
    if (currentModelTime.nsecs + actWidth < nanoSeconds) {
                actSA[actWrA]--;
                actNsA[actWrA] = currentModelTime.nsecs + actWidth + 1000000000 - nanoSeconds;
    } else {
                actNsA[actWrA] = currentModelTime.nsecs + actWidth - nanoSeconds;
    }
        actWrA = actWrA+1;
        if(actWrA == 10)
                actWrA = 0;

    /* generate code for clearing Event Head buffer. */
    Event_Head_CG_3_T_Rex_noNetwork_v1_ContactController_ContactControllerMicro_ActuatorSetup2_input[0] = NULL;
}

/*** InterruptBlock(
$emptyFunctions,
$systick1,
$systick2) ***/

// ----------------------------------------------------------------
// ---------- Interrupt block -------------------------------------

extern void SysTickHandler(void);
extern void SafeToProcessInterruptHandler(void);
$emptyFunctions


void SysTickHandler(void) {
        unsigned char dummy;

        while(MTU20.TSR.BIT.TCFV != 1)
                ;

        MTU20.TSR.BIT.TCFV = 0;

        nanoSeconds += (4*divideByValue/2) << 16;

        if(nanoSeconds >= 1000000000) {
                Seconds++;
                nanoSeconds -=  1000000000;
        }

        for(dummy = 0; dummy<10; dummy++) {
                if(zzz[dummy] < ((4*divideByValue/2) << 16)) {
                        zzzS[dummy] = zzzS[dummy]-1;
                        zzz[dummy] = 1000000000+zzz[dummy]-((4*divideByValue/2) << 16);
                }
                else
                        zzz[dummy] = zzz[dummy] - ((4*divideByValue/2) << 16);

                $systick1
        }

        if((MTU20.TIER2.BIT.TGIEE == 0) && (zzzS[0] == 0) && (zzz[0] < ((4*divideByValue/2)*(65536 + intDel)))){
                MTU20.TGRE = zzz[0]/(4*divideByValue/2);
                MTU20.TSR2.BIT.TGFE = 0;
                MTU20.TIER2.BIT.TGIEE = 1;
        }

        $systick2
}



void SafeToProcessInterruptHandler(void) {

        while(MTU20.TSR2.BIT.TGFE != 1)
                ;

        MTU20.TSR2.BIT.TGFE = 0;

        set_imask(15);

        MTU20.TIER2.BIT.TGIEE = 0;

        lastTimerInterruptTime.secs = (NS_UINT32)-1;
        lastTimerInterruptTime.nsecs = (NS_UINT32)-1;

        set_imask(0);


        processEvents();
}

/**/

/*** InterruptVectorTable(
$InterruptHandler156,
$InterruptHandler157,
$InterruptHandler158,
$InterruptHandler159,
$InterruptHandler162,
$InterruptHandler180,
$InterruptHandler181,
$InterruptHandler182,
$InterruptHandler183,
$externDeclarations) ***/

// ----------------------------------------------------------------
// ---------- Vector Table ----------------------------------------

#include "vect.h"

$externDeclarations

#pragma section VECTTBL
void *RESET_Vectors[] = {
//;<<VECTOR DATA START (POWER ON RESET)>>
// 0 Power On Reset PC
    (void*)        PowerON_Reset_PC,
//;<<VECTOR DATA END (POWER ON RESET)>>
// 1 Power On Reset SP
    __secend("S"),
//;<<VECTOR DATA START (MANUAL RESET)>>
// 2 Manual Reset PC
    (void*)        Manual_Reset_PC,
//;<<VECTOR DATA END (MANUAL RESET)>>
// 3 Manual Reset SP
    __secend("S")
};

#pragma section INTTBL
void *INT_Vectors[] = {
// 4 Illegal code
    (void*) INT_Illegal_code,
// 5 Reserved
    (void*) Dummy,
// 6 Illegal slot
        (void*) INT_Illegal_slot,
// 7 Reserved
    (void*) Dummy,
// 8 Reserved
    (void*) Dummy,
// 9 CPU Address error
        (void*) INT_CPU_Address,
// 10 DMAC Address error
        (void*) INT_DMAC_Address,
// 11 NMI
        (void*) INT_NMI,
// 12 User breakpoint trap
        (void*) INT_User_Break,
// 13 Reserved
    (void*) Dummy,
// 14 H-UDI
        (void*) INT_HUDI,
// 15 Register bank over
    (void*) INT_Bank_Overflow,
// 16 Register bank under
    (void*) INT_Bank_Underflow,
// 17 ZERO_DIV
    (void*) INT_Divide_by_Zero,
// 18 OVER_DIV
    (void*) INT_Divide_Overflow,
// 19 Reserved
    (void*) Dummy,
// 20 Reserved
    (void*) Dummy,
// 21 Reserved
    (void*) Dummy,
// 22 Reserved
    (void*) Dummy,
// 23 Reserved
    (void*) Dummy,
// 24 Reserved
    (void*) Dummy,
// 25 Reserved
    (void*) Dummy,
// 26 Reserved
    (void*) Dummy,
// 27 Reserved
    (void*) Dummy,
// 28 Reserved
    (void*) Dummy,
// 29 Reserved
    (void*) Dummy,
// 30 Reserved
    (void*) Dummy,
// 31 Reserved
    (void*) Dummy,
// 32 TRAPA (User Vecter)
    (void*) INT_TRAPA32,
// 33 TRAPA (User Vecter)
    (void*) INT_TRAPA33,
// 34 TRAPA (User Vecter)
    (void*) INT_TRAPA34,
// 35 TRAPA (User Vecter)
    (void*) INT_TRAPA35,
// 36 TRAPA (User Vecter)
    (void*) INT_TRAPA36,
// 37 TRAPA (User Vecter)
    (void*) INT_TRAPA37,
// 38 TRAPA (User Vecter)
    (void*) INT_TRAPA38,
// 39 TRAPA (User Vecter)
    (void*) INT_TRAPA39,
// 40 TRAPA (User Vecter)
    (void*) INT_TRAPA40,
// 41 TRAPA (User Vecter)
    (void*) INT_TRAPA41,
// 42 TRAPA (User Vecter)
    (void*) INT_TRAPA42,
// 43 TRAPA (User Vecter)
    (void*) INT_TRAPA43,
// 44 TRAPA (User Vecter)
    (void*) INT_TRAPA44,
// 45 TRAPA (User Vecter)
    (void*) INT_TRAPA45,
// 46 TRAPA (User Vecter)
    (void*) INT_TRAPA46,
// 47 TRAPA (User Vecter)
    (void*) INT_TRAPA47,
// 48 TRAPA (User Vecter)
    (void*) INT_TRAPA48,
// 49 TRAPA (User Vecter)
    (void*) INT_TRAPA49,
// 50 TRAPA (User Vecter)
    (void*) INT_TRAPA50,
// 51 TRAPA (User Vecter)
    (void*) INT_TRAPA51,
// 52 TRAPA (User Vecter)
    (void*) INT_TRAPA52,
// 53 TRAPA (User Vecter)
    (void*) INT_TRAPA53,
// 54 TRAPA (User Vecter)
    (void*) INT_TRAPA54,
// 55 TRAPA (User Vecter)
    (void*) INT_TRAPA55,
// 56 TRAPA (User Vecter)
    (void*) INT_TRAPA56,
// 57 TRAPA (User Vecter)
    (void*) INT_TRAPA57,
// 58 TRAPA (User Vecter)
    (void*) INT_TRAPA58,
// 59 TRAPA (User Vecter)
    (void*) INT_TRAPA59,
// 60 TRAPA (User Vecter)
    (void*) INT_TRAPA60,
// 61 TRAPA (User Vecter)
    (void*) INT_TRAPA61,
// 62 TRAPA (User Vecter)
    (void*) INT_TRAPA62,
// 63 TRAPA (User Vecter)
    (void*) INT_TRAPA63,
// 64 Interrupt IRQ0
        (void*) INT_IRQ0,
// 65 Interrupt IRQ1
        (void*) INT_IRQ1,
// 66 Interrupt IRQ2
        (void*) INT_IRQ2,
// 67 Interrupt IRQ3
        (void*) INT_IRQ3,
// 68 Interrupt IRQ4
        (void*) INT_IRQ4,
// 69 Interrupt IRQ5
        (void*) INT_IRQ5,
// 70 Interrupt IRQ6
        (void*) INT_IRQ6,
// 71 Interrupt IRQ7
        (void*) INT_IRQ7,
// 72 Reserved
    (void*) Dummy,
// 73 Reserved
    (void*) Dummy,
// 74 Reserved
    (void*) Dummy,
// 75 Reserved
    (void*) Dummy,
// 76 Reserved
    (void*) Dummy,
// 77 Reserved
    (void*) Dummy,
// 78 Reserved
    (void*) Dummy,
// 79 Reserved
    (void*) Dummy,
// 80 Interrupt PINT0
        (void*) INT_PINT0,
// 81 Interrupt PINT1
        (void*) INT_PINT1,
// 82 Interrupt PINT2
        (void*) INT_PINT2,
// 83 Interrupt PINT3
        (void*) INT_PINT3,
// 84 Interrupt PINT4
        (void*) INT_PINT4,
// 85 Interrupt PINT5
        (void*) INT_PINT5,
// 86 Interrupt PINT6
        (void*) INT_PINT6,
// 87 Interrupt PINT7
        (void*) INT_PINT7,
// 88 Reserved
    (void*) Dummy,
// 89 Reserved
    (void*) Dummy,
// 90 Reserved
    (void*) Dummy,
// 91 ROM FIFE
    (void*) INT_ROM_FIFE,
// 92 A/D ADI0
        (void*) INT_AD_ADI0,
// 93 Reserved
    (void*) Dummy,
// 94 Reserved
    (void*) Dummy,
// 95 Reserved
    (void*) Dummy,
// 96 A/D ADI1
        (void*) INT_AD_ADI1,
// 97 Reserved
    (void*) Dummy,
// 98 Reserved
    (void*) Dummy,
// 99 Reserved
    (void*) Dummy,
// 100 Reserved
    (void*) Dummy,
// 101 Reserved
    (void*) Dummy,
// 102 Reserved
    (void*) Dummy,
// 103 Reserved
    (void*) Dummy,
// 104 RCANET0 ERS_0
    (void*) INT_RCANET0_ERS_0,
// 105 RCANET0 OVR_0
    (void*) INT_RCANET0_OVR_0,
// 106 RCANET0 RM01_0
    (void*) INT_RCANET0_RM01_0,
// 107 RCANET0 SLE_0
    (void*) INT_RCANET0_SLE_0,
// 108 DMAC0 DEI0
        (void*) INT_DMAC0_DEI0,
// 109 DMAC0 HEI0
        (void*) INT_DMAC0_HEI0,
// 110 Reserved
    (void*) Dummy,
// 111 Reserved
    (void*) Dummy,
// 112 DMAC1 DEI1
        (void*) INT_DMAC1_DEI1,
// 113 DMAC1 HEI1
        (void*) INT_DMAC1_HEI1,
// 114 Reserved
    (void*) Dummy,
// 115 Reserved
    (void*) Dummy,
// 116 DMAC2 DEI2
        (void*) INT_DMAC2_DEI2,
// 117 DMAC2 HEI2
        (void*) INT_DMAC2_HEI2,
// 118 Reserved
    (void*) Dummy,
// 119 Reserved
    (void*) Dummy,
// 120 DMAC3 DEI3
        (void*) INT_DMAC3_DEI3,
// 121 DMAC3 HEI3
        (void*) INT_DMAC3_HEI3,
// 122 Reserved
    (void*) Dummy,
// 123 Reserved
    (void*) Dummy,
// 124 DMAC4 DEI4
        (void*) INT_DMAC4_DEI4,
// 125 DMAC4 HEI4
        (void*) INT_DMAC4_HEI4,
// 126 Reserved
    (void*) Dummy,
// 127 Reserved
    (void*) Dummy,
// 128 DMAC5 DEI5
        (void*) INT_DMAC5_DEI5,
// 129 DMAC5 HEI5
        (void*) INT_DMAC5_HEI5,
// 130 Reserved
    (void*) Dummy,
// 131 Reserved
    (void*) Dummy,
// 132 DMAC6 DEI6
        (void*) INT_DMAC6_DEI6,
// 133 DMAC6 HEI6
        (void*) INT_DMAC6_HEI6,
// 134 Reserved
    (void*) Dummy,
// 135 Reserved
    (void*) Dummy,
// 136 DMAC7 DEI7
        (void*) INT_DMAC7_DEI7,
// 137 DMAC7 HEI7
        (void*) INT_DMAC7_HEI7,
// 138 Reserved
    (void*) Dummy,
// 139 Reserved
    (void*) Dummy,
// 140 CMT CMI0
        (void*) INT_CMT_CMI0,
// 141 Reserved
    (void*) Dummy,
// 142 Reserved
    (void*) Dummy,
// 143 Reserved
    (void*) Dummy,
// 144 CMT CMI1
        (void*) INT_CMT_CMI1,
// 145 Reserved
    (void*) Dummy,
// 146 Reserved
    (void*) Dummy,
// 147 Reserved
    (void*) Dummy,
// 148 BSC CMTI
        (void*) INT_BSC_CMTI,
// 149 Reserved
    (void*) Dummy,
// 150 USB EP4FULL
    (void*) INT_USB_EP4FULL,
// 151 USB EP5EMPTY
    (void*) INT_USB_EP5EMPTY,
// 152 WDT ITI
        (void*) INT_WDT_ITI,
// 153 E-DMAC EINT0
    (void*) INT_EDMAC_EINT0,
// 154 USB EP1FULL
    (void*) INT_USB_EP1FULL,
// 155 USB EP2EMPTY
    (void*) INT_USB_EP2EMPTY,
// 156 MTU2 MTU0 TGI0A
        // (void*) INT_MTU2_MTU0_TGI0A,
    (void*) $InterruptHandler156,
// 157 MTU2 MTU0 TGI0B
        // (void*) INT_MTU2_MTU0_TGI0B,
    (void*) $InterruptHandler157,
// 158 MTU2 MTU0 TGI0C
        // (void*) INT_MTU2_MTU0_TGI0C,
    (void*) $InterruptHandler158,
// 159 MTU2 MTU0 TGI0D
        // (void*) INT_MTU2_MTU0_TGI0D,
    (void*) $InterruptHandler159,
// 160 MTU2 MTU0 TGI0V
        // (void*) INT_MTU2_MTU0_TGI0V,
    (void*) SysTickHandler,
// 161 MTU2 MTU0 TGI0E
        // (void*) INT_MTU2_MTU0_TGI0E,
    (void*) SafeToProcessInterruptHandler,
// 162 MTU2 MTU0 TGI0F
        // (void*) INT_MTU2_MTU0_TGI0F,
    (void*) $InterruptHandler162,
// 163 Reserved
    (void*) Dummy,
// 164 MTU2 MTU1 TGI1A
    (void*) INT_MTU2_MTU1_TGI1A,
// 165 MTU2 MTU1 TGI1B
        (void*) INT_MTU2_MTU1_TGI1B,
// 166 Reserved
    (void*) Dummy,
// 167 Reserved
    (void*) Dummy,
// 168 MTU2 MTU1 TGI1V
        (void*) INT_MTU2_MTU1_TGI1V,
// 169 MTU2 MTU1 TGI1U
        (void*) INT_MTU2_MTU1_TGI1U,
// 170 Reserved
    (void*) Dummy,
// 171 Reserved
    (void*) Dummy,
// 172 MTU2 MTU2 TGI2A
        (void*) INT_MTU2_MTU2_TGI2A,
// 173 MTU2 MTU2 TGI2B
        (void*) INT_MTU2_MTU2_TGI2B,
// 174 Reserved
    (void*) Dummy,
// 175 Reserved
    (void*) Dummy,
// 176 MTU2 MTU2 TGI2V
        (void*) INT_MTU2_MTU2_TGI2V,
// 177 MTU2 MTU2 TGI2U
        (void*) INT_MTU2_MTU2_TGI2U,
// 178 Reserved
    (void*) Dummy,
// 179 Reserved
    (void*) Dummy,
// 180 MTU2 MTU3 TGI3A
        // (void*) INT_MTU2_MTU3_TGI3A,
    (void*) $InterruptHandler180,
// 181 MTU2 MTU3 TGI3B
        // (void*) INT_MTU2_MTU3_TGI3B,
    (void*) $InterruptHandler181,
// 182 MTU2 MTU3 TGI3C
        // (void*) INT_MTU2_MTU3_TGI3C,
    (void*) $InterruptHandler182,
// 183 MTU2 MTU3 TGI3D
        // (void*) INT_MTU2_MTU3_TGI3D,
    (void*) $InterruptHandler183,
// 184 MTU2 MTU3 TGI3V
        (void*) INT_MTU2_MTU3_TGI3V,
// 185 Reserved
    (void*) Dummy,
// 186 Reserved
    (void*) Dummy,
// 187 Reserved
    (void*) Dummy,
// 188 MTU2 MTU4 TGI4A
        (void*) INT_MTU2_MTU4_TGI4A,
// 189 MTU2 MTU4 TGI4B
        (void*) INT_MTU2_MTU4_TGI4B,
// 190 MTU2 MTU4 TGI4C
        (void*) INT_MTU2_MTU4_TGI4C,
// 191 MTU2 MTU4 TGI4D
        (void*) INT_MTU2_MTU4_TGI4D,
// 192 MTU2 MTU4 TGI4V
        (void*) INT_MTU2_MTU4_TGI4V,
// 193 Reserved
    (void*) Dummy,
// 194 Reserved
    (void*) Dummy,
// 195 Reserved
    (void*) Dummy,
// 196 MTU2 MTU5 TGI5U
        (void*) INT_MTU2_MTU5_TGI5U,
// 197 MTU2 MTU5 TGI5V
        (void*) INT_MTU2_MTU5_TGI5V,
// 198 MTU2 MTU5 TGI5W
        (void*) INT_MTU2_MTU5_TGI5W,
// 199 Reserved
    (void*) Dummy,
// 200 POE2 OEI1
        (void*) INT_POE2_OEI1,
// 201 POE2 OEI2
        (void*) INT_POE2_OEI2,
// 202 Reserved
    (void*) Dummy,
// 203 Reserved
    (void*) Dummy,
// 204 MTU2S MTU3S TGI3A
        (void*) INT_MTU2S_MTU3S_TGI3A,
// 205 MTU2S MTU3S TGI3B
        (void*) INT_MTU2S_MTU3S_TGI3B,
// 206 MTU2S MTU3S TGI3C
        (void*) INT_MTU2S_MTU3S_TGI3C,
// 207 MTU2S MTU3S TGI3D
        (void*) INT_MTU2S_MTU3S_TGI3D,
// 208 MTU2S MTU3S TGI3V
        (void*) INT_MTU2S_MTU3S_TGI3V,
// 209 Reserved
    (void*) Dummy,
// 210 Reserved
    (void*) Dummy,
// 211 Reserved
    (void*) Dummy,
// 212 MTU2S MTU4S TGI4A
        (void*) INT_MTU2S_MTU4S_TGI4A,
// 213 MTU2S MTU4S TGI4B
        (void*) INT_MTU2S_MTU4S_TGI4B,
// 214 MTU2S MTU4S TGI4C
        (void*) INT_MTU2S_MTU4S_TGI4C,
// 215 MTU2S MTU4S TGI4D
        (void*) INT_MTU2S_MTU4S_TGI4D,
// 216 MTU2S MTU4S TGI4V
        (void*) INT_MTU2S_MTU4S_TGI4V,
// 217 Reserved
    (void*) Dummy,
// 218 Reserved
    (void*) Dummy,
// 219 Reserved
    (void*) Dummy,
// 220 MTU2S MTU5S TGI5U
        (void*) INT_MTU2S_MTU5S_TGI5U,
// 221 MTU2S MTU5S TGI5V
        (void*) INT_MTU2S_MTU5S_TGI5V,
// 222 MTU2S MTU5S TGI5W
        (void*) INT_MTU2S_MTU5S_TGI5W,
// 223 Reserved
    (void*) Dummy,
// 224 POE2 OEI3
        (void*) INT_POE2_OEI3,
// 225 Reserved
    (void*) Dummy,
// 226 USB USI0
    (void*) INT_USB_USI0,
// 227 USB USI1
    (void*) INT_USB_USI1,
// 228 IIC3 STPI
        (void*) INT_IIC3_STPI,
// 229 IIC3 NAKI
        (void*) INT_IIC3_NAKI,
// 230 IIC3 RXI
        (void*) INT_IIC3_RXI,
// 231 IIC3 TXI
        (void*) INT_IIC3_TXI,
// 232 IIC3 TEI
        (void*) INT_IIC3_TEI,
// 233 RSPI SPERI
    (void*) INT_RSPI_SPERI,
// 234 RSPI SPRXI
    (void*) INT_RSPI_SPRXI,
// 235 RSPI SPTXI
    (void*) INT_RSPI_SPTXI,
// 236 SCI SCI4 ERI4
    (void*) INT_SCI_SCI4_ERI4,
// 237 SCI SCI4 RXI4
    (void*) INT_SCI_SCI4_RXI4,
// 238 SCI SCI4 TXI4
    (void*) INT_SCI_SCI4_TXI4,
// 239 SCI SCI4 TEI4
    (void*) INT_SCI_SCI4_TEI4,
// 240 SCI SCI0 ERI0
        (void*) INT_SCI_SCI0_ERI0,
// 241 SCI SCI0 RXI0
        (void*) INT_SCI_SCI0_RXI0,
// 242 SCI SCI0 TXI0
        (void*) INT_SCI_SCI0_TXI0,
// 243 SCI SCI0 TEI0
        (void*) INT_SCI_SCI0_TEI0,
// 244 SCI SCI1 ERI1
        (void*) INT_SCI_SCI1_ERI1,
// 245 SCI SCI1 RXI1
        (void*) INT_SCI_SCI1_RXI1,
// 246 SCI SCI1 TXI1
        (void*) INT_SCI_SCI1_TXI1,
// 247 SCI SCI1 TEI1
        (void*) INT_SCI_SCI1_TEI1,
// 248 SCI SCI2 ERI2
        (void*) INT_SCI_SCI2_ERI2,
// 249 SCI SCI2 RXI2
        (void*) INT_SCI_SCI2_RXI2,
// 250 SCI SCI2 TXI2
        (void*) INT_SCI_SCI2_TXI2,
// 251 SCI SCI2 TEI2
        (void*) INT_SCI_SCI2_TEI2,
// 252 SCIF SCIF3 BRI3
        (void*) INT_SCIF_SCIF3_BRI3,
// 253 SCIF SCIF3 ERI3
        (void*) INT_SCIF_SCIF3_ERI3,
// 254 SCIF SCIF3 RXI3
        (void*) INT_SCIF_SCIF3_RXI3,
// 255 SCIF SCIF3 TXI3
        (void*) INT_SCIF_SCIF3_TXI3,
// xx Reserved
    (void*) Dummy
};

/**/

/*** initPDBlock***/
// the platform dependent initialization code goes here.
        eth_init();
        usb_init();
        initTimer();
        epl_test();
        initEventMemory();
/**/

/*** initPDCodeBlock($initInterruptHandlers) ***/
void initTimer(void) {
        MTU2.TSTR.BIT.CST0 = 0;
        MTU2.TSTR.BIT.CST3 = 0;

        MTU20.TMDR.BYTE = 0x00; /// MTU20 ... timer 0 (actuaotrs), MTU23 ... timer 3 (sensors)
         MTU20.TCR.BYTE = 0x14;

         MTU20.TSR.BYTE = 0xC0;
        MTU20.TSR2.BYTE = 0xC0;

        MTU20.TIER2.BYTE = 0x00;
        MTU20.TCNT = 0;
        INTC.IPR09.BIT._MTU20G = 2;
        INTC.IPR09.BIT._MTU20C = 3;

        MTU23.TMDR.BYTE = 0x00;
        MTU23.TCR.BYTE = 0x16;

        MTU23.TSR.BYTE = 0xC0;

        MTU23.TCNT = 0;
        INTC.IPR10.BIT._MTU23G = 2;

        MTU2.TSTR.BIT.CST0 = 1;
        MTU2.TSTR.BIT.CST3 = 1;

        $initInterruptHandlers
        MTU20.TIER.BIT.TCIEV = 1; // systickhandler
}

void initEventMemory() {
    int16 i;
    for(i = 1; i < MAX_EVENTS; i++) {
        eventMemory[i-1].nextEvent = &eventMemory[i];
    }
    FREE_EVENT_LIST = &eventMemory[0];
}
/**/

/*** preinitPDBlock()***/
// empty
/**/

/*** wrapupPDBlock() ***/
// empty
/**/


/*** mainLoopBlock ***/
void execute(void) {

        processEvents();
          while(1)
                ;
}
/**/
