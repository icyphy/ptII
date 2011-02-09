/*** StructDefBlock ***/
//#define LCD_DEBUG

#define FALSE 0
#define TRUE  1

#define LESS -1
#define MORE 1
#define EQUAL 0

#define MAX_EVENTS 100
#define MAX_ACTUATOR_TIMER_VALUES 10

/* structures */
typedef struct {
    uint32 secs;
    uint32 nsecs;
} Time;

typedef struct {
    Time timestamp;
    uint16 microstep;
} Tag;

typedef struct event {
    union {
        int int_Value; 
        double double_Value;
        long long_Value;
        char char_Value;
        unsigned int boolean_Value;
    } Val;
    Tag tag;
    uint32 depth;
    void (*fireMethod)();

    struct event** sinkEvent;
    Time deadline;
    Time offsetTime;

    char inUse;

    struct event* nextEvent;
    // we need a port to indicate where this is pointing to.
} Event;
/**/

/*** FuncProtoBlock ***/
void addEvent(Event*);
void fireActor(Event*);
void freeEvent(Event*);
unsigned int higherPriority(const Event*);
void initializeEvents(void);
Event* newEvent(void);
void propagateDataToken(Event*);
void queuePriority(Event*);
void removeEvent(Event*);
void safeToProcess(const Event* const, Time*);
void setCurrentModelTag(Event*);
void timeAdd(const Time, const Time, Time*);
int timeCompare(const Time, const Time);
int timeSub(const Time, const Time, Time*);

/* static variables */
Event eventMemory[MAX_EVENTS];
volatile Event* DEADLINE_QUEUE_HEAD = NULL;
volatile int locationCounter;
volatile Time currentModelTime;
volatile int currentMicrostep;

// Global variable to keep track of number of times the timer needs to interrupt before physical
// time has exceeded safe to process time.
volatile unsigned long timerInterruptSecsLeft;
volatile unsigned long actuatorTimerInterruptSecsLeft;

// ID of the current actuator that's causing the timer to run.
volatile int actuatorRunning = -1;
volatile Time lastActuateTime;

volatile int stackedDeadlineIndex = -1;
volatile Time executingDeadlines[MAX_EVENTS];
volatile void (*executingActors[MAX_EVENTS])();

volatile Tag executingModelTag[MAX_EVENTS];
volatile int stackedModelTagIndex = -1;

volatile Time lastTimerInterruptTime;
volatile uint32 _secs = 0;
volatile uint32 _quarterSecs = 0;

// actuator queue
// Head points to the head of the array.
volatile int actuatorArrayHeadPtrs[numActuators];
// Tail points to the last element array.
volatile int actuatorArrayTailPtrs[numActuators];
volatile int actuatorArrayCounts[numActuators];
volatile Time actuatorTimerValues[numActuators][MAX_ACTUATOR_TIMER_VALUES];

// Times.
static Time MAX_TIME = {(uint32)-1, (uint32)-1};
static Time ZERO_TIME = {0, 0};

/**/

/*** FuncBlock ***/
/* event memory manipulations*/

// this method compares events in lexicographical order based on:
// deadline, timestamp, microstep, and depth.
int compareEvents(Event* event1, Event* event2) {
    int compare;
	if (event1 == NULL || event2 == NULL) {
		die("compare NULL events");
	}
    compare = timeCompare(event1->deadline, event2->deadline);
    if (compare != 0) {
        return compare;
    } else {
        compare = timeCompare(event1->tag.timestamp, event2->tag.timestamp);
        if (compare != 0) {
            return compare;
        } else {
            if (event1->tag.microstep < event2->tag.microstep) {
                return -1;
            } else if (event1->tag.microstep > event2->tag.microstep) {
                return 1;
            } else {
                if (event1->depth < event2->depth) {
                    return -1;
                } else if (event1->depth > event2->depth) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
}

void addEvent(Event* newEvent) {
    // now add event to the deadline queue
    Event *compare_deadline;
    Event *before_deadline;
    disableInterrupts();
    before_deadline  = NULL;
    compare_deadline = DEADLINE_QUEUE_HEAD;
    if (newEvent == NULL) {
    	die("NULL new event");
    }
    while (1) {
        // We've reached the end of the queue.
        if (compare_deadline == NULL) {
            break;
        } else if (compareEvents(newEvent, compare_deadline) <= 0) {
            break;
        } else {
            before_deadline = compare_deadline;
            compare_deadline = compare_deadline->nextEvent;
        }
    }
    newEvent->nextEvent = compare_deadline;
    if (before_deadline == NULL) {
        DEADLINE_QUEUE_HEAD = newEvent;
    } else {
        before_deadline->nextEvent = newEvent;
    }
    #ifdef LCD_DEBUG
    //sprintf(str,"addedEvent: %d",addeventcount);
    //RIT128x96x4StringDraw(str,   12,80,15);
    #endif
	enableInterrupts();
}

Event* peekEvent(unsigned int peekingIndex) {
    int i = 0;
    Event* deadline = DEADLINE_QUEUE_HEAD;
	while (deadline != NULL && i < peekingIndex) {
		deadline = deadline->nextEvent;
		i++;
    }
    return deadline;
}

int notSameTag(const Event* event1, const Event* event2) {
    if (timeCompare(event1->tag.timestamp, event2->tag.timestamp) == EQUAL
        && event1->tag.microstep == event2->tag.microstep) {
            return false;
    } else {
        return true;
    }
}

int sameDestination(const Event* event1, const Event* event2) {
    // for now, assume if two events are destined to the same actor,
    // then they should be processed together. This is however not
    // true in general. It should be two events destined to the same
    // equivalence class should be processed togetherd.
    return event1->fireMethod == event2->fireMethod;
}

void removeAndPropagateSameTagEvents(int peekingIndex) {
    int i;
    Event* event = DEADLINE_QUEUE_HEAD;
    Event* prevEvent = NULL;
    Event* refEvent = NULL;
    if (peekingIndex == 0) {
		// remove event from the event queue.
        DEADLINE_QUEUE_HEAD = event->nextEvent;
    } else {
        for (i = 0; i < peekingIndex; i++) {
            prevEvent = event;
            event = event->nextEvent;
        }
		// remove event from the event queue.
		prevEvent->nextEvent = event->nextEvent;
    }
    // propagate data
    propagateDataToken(event);

    // Now find the next event see we should process it at the same time.
    refEvent = event;
    while (true) {
        event = event->nextEvent;
		if (event == NULL) {
			break;
		} else if (notSameTag(refEvent, event)) {
            break;
        } else {
            // tags are the same, but only propagate if the destination
            // actor for these two events are the same.
            if (sameDestination(refEvent, event)) {
                propagateDataToken(event);
				// remove event from the event queue,
				if (prevEvent == NULL) {
					DEADLINE_QUEUE_HEAD = event->nextEvent;
				} else {	
                	prevEvent->nextEvent = event->nextEvent;
				}
            } else {
                prevEvent = event;
            }
        }
    }
}

Event* newEvent(void) {
    // counter counts the number of times we loop around the memory.
    int counter = 0;
	disableInterrupts();
    while (eventMemory[locationCounter].inUse != MAX_EVENTS + 1) {
        // If ran out of memory, then die.
        if (counter++ >= MAX_EVENTS) {
            die("ran out of memory");
        }
        locationCounter++;
        // The event list is circular.
        if (locationCounter >= MAX_EVENTS) {
            locationCounter -= MAX_EVENTS;
        }
        // The following code is less efficient?
        // locationCounter %= MAX_EVENTS;
    }
    //      RIT128x96x4StringDraw(itoa(locationCounter,10), 0,0,15);
	counter = locationCounter;
    eventMemory[counter].inUse = counter;
	locationCounter++;
    // The event list is circular.
    if (locationCounter >= MAX_EVENTS) {
        locationCounter -= MAX_EVENTS;
    }
	// The following code is less efficient?    
    // locationCounter %= MAX_EVENTS;
	enableInterrupts();
    return &eventMemory[counter];
}

void freeEvent(Event* thisEvent) {
	disableInterrupts();
    if (thisEvent->inUse == MAX_EVENTS+1) {
        die("already free");
	}
	if (&(eventMemory[thisEvent->inUse]) != thisEvent) {
		die("wrong event");
	}
	thisEvent->inUse = MAX_EVENTS+1;
    enableInterrupts();
}

/* time manipulation */
void timeAdd(const Time time1, const Time time2, Time* timeSum) {
    timeSum->secs = time1.secs + time2.secs;
    timeSum->nsecs = time1.nsecs + time2.nsecs;
    if (timeSum->nsecs >= 1000000000) {
        timeSum->nsecs -= 1000000000;
        timeSum->secs++;
    }
}

//compare two time values
int timeCompare(const Time time1, const Time time2) {
    if (time1.secs < time2.secs) {
        return LESS;
    } else if (time1.secs == time2.secs && time1.nsecs < time2.nsecs) {
        return LESS;
    } else if (time1.secs == time2.secs && time1.nsecs == time2.nsecs) {
        return EQUAL;
    }
    return MORE;       
}

/* subtract two time values
 * 
 */
int timeSub(const Time time1, const Time time2, Time* timeSub) {
    if (timeCompare(time1, time2) == -1) {
        return -1;
    }
    timeSub->secs = time1.secs - time2.secs;
    if (time1.nsecs < time2.nsecs) {
        timeSub->secs--;
        timeSub->nsecs = time1.nsecs + 1000000000 - time2.nsecs;
    } else {
        timeSub->nsecs = time1.nsecs - time2.nsecs;
    }
    return 1;
}

/* event processing */

void processEvents() {
    // peekingPoint keeps track which event to peek at.
    int peekingIndex = 0;
    // keeps track of how many events in the event queue cannot be processed because their
    // buffer has been blocked.
    Event* event;
    int whilecount = 0;
    Time processTime;
    Time physicalTime;

    disableInterrupts();

    while (true) {
        event = peekEvent(peekingIndex);
        if (event == NULL) {
            break;
        }
        // If event queue is not empty, keep going.
        whilecount++;
#ifdef LCD_DEBUG
        debugMessageNumber("wc = ", whilecount);
#endif

        if (higherPriority(event)) {
            safeToProcess(event, &processTime);

#ifdef LCD_DEBUG
            //debugMessageNumber("hpwc=", whilecount);
#endif
            getRealTime(&physicalTime);

            if (timeCompare(physicalTime, processTime) >= 0) {
                // We have decided to process this event, thus we let the rest
                // of the system know this decision by queuing the priority, and
                // setting the tag of this event.
                queuePriority(event);
                removeAndPropagateSameTagEvents(peekingIndex);
                setCurrentModelTag(event);
                enableInterrupts();
                // Execute the event. During
                // this process more events may
                // be posted to the queue.

                fireActor(event);
                // After an actor fires, we not sure which event is safe to process,
                // so we start examine the queue again start from the beginning.
                disableInterrupts();
				peekingIndex = 0;
            } else {
                // Set timed interrupt to run
                // the event at the top of the
                // event queue when physical time
                // has passed for it to be
                // safe to process
                if (timeCompare(processTime, lastTimerInterruptTime) == LESS) {
                    lastTimerInterruptTime = processTime;
                    setTimedInterrupt(&processTime);
                }
                // this event is not safe to process, we look at the next one.
                //peekingIndex++;
                break;

            }// end !(curentPhysicalTime >= processTime)
        } else {
            // This is the only place for us to break out of the while loop. The only other possibility
            // is that we have looked all all events in the queue, and none of them are safe to process.
#ifdef LCD_DEBUG
            debugMessageNumber("endwc=", peekingIndex);
#endif
            break;
        }//end EVENT_QUEUE_HEAD != NULL
    }

    // restore the last executing stacked model tag.
    if (stackedModelTagIndex >= 0) {
        currentMicrostep = executingModelTag[stackedModelTagIndex].microstep;
        currentModelTime = executingModelTag[stackedModelTagIndex].timestamp;
        stackedModelTagIndex--;
    } else {
    	die("cannot restore model tag");
    }
    // This implies within the while loop, if all events cannot be processed, 
    // we need to go through the entire event queue before another event can come in.
    enableInterrupts();
    // if all events are blocked because buffers are full
    // if (numBufferBlocked != 0 && numBufferBlocked == peekingIndex) 
#ifdef LCD_DEBUG
    //debugMessage("ret PE()");
#endif
	// we do not need to disable interrupts for this routine, because it
    // is triggered through a SVC call, which has higher priority than
    // all other external interrupts in the system.
    restoreStack();
    die("should never get here");
}

/* 
* Check if static timing analysis is needed.
* if it is, static timing analysis is called, and returns
* if it's not, firing method of the actor specified by the event is called
*/
void fireActor(Event* currentEvent) {
    if (currentEvent->fireMethod != NULL){
        (currentEvent->fireMethod)();
    } else {
        die("no such method, cannot fire\n");
    }

    freeEvent(currentEvent);
    stackedDeadlineIndex--;
}

/* Determines whether the event to fire this current actor is of higher priority than
*  whatever even that's currently being executed.
*/                                                                    
unsigned int higherPriority(const Event* const event) {
    int i;
    if (stackedDeadlineIndex < 0) {
        // there are no events on the stack, so it's always true.
        return TRUE;
    } else if (timeCompare(executingDeadlines[stackedDeadlineIndex], event->deadline) == LESS) {
#ifdef LCD_DEBUG
        debugMessageNumber("exDe sec=",
                executingDeadlines[stackedDeadlineIndex].secs);
        debugMessageNumber("exDe nsec=",
                executingDeadlines[stackedDeadlineIndex].nsecs); 
#endif
        return FALSE;
    } else {
        // check for all actors that are currently firing, and make sure we
        // don't fire an actor that's already firing.
        for (i = 0; i <= stackedDeadlineIndex; i++) {
            if (executingActors[i] == event->fireMethod) {
                return FALSE;
            }
        }
        return TRUE;
    }
}

/*
* Add the priority of the current event into queue
* Set the firing flag of the actor, indicate that the actor is currenting being fired.
*/
void queuePriority(Event* event) {
    stackedDeadlineIndex++;
    if (stackedDeadlineIndex == MAX_EVENTS) {
        die("stackedDeadlineIndex exceeds MAX_EVENTS");
    }
    executingDeadlines[stackedDeadlineIndex] = event->deadline;
    executingActors[stackedDeadlineIndex] = event->fireMethod;
}

/*
 * Set the current model time.
 */
void setCurrentModelTag(Event* currentEvent) {
    currentModelTime = currentEvent->tag.timestamp;
    currentMicrostep = currentEvent->tag.microstep;
}

/*
 * Propagate the data token to the downstream input port Event pointer.
 */
void propagateDataToken(Event* currentEvent){
    *(currentEvent->sinkEvent) = currentEvent;
}

/* 
* Static timing analysis to set the timestamp of the event by a 
* specific amount in order to fire the event at an instance that ensures 
* all events are exectued in order.
* In this analysis, the clock event can be executed when real time exceeds
* tau - model_delay3
*/
void safeToProcess(const Event* const thisEvent, Time* safeTimestamp) {

    //   safeTimestamp = physical time - offset;

    //      unsigned int safeThroughPropagation = TRUE;

    //FIXME: we only check the first event in the event queue.
    /*
    while (inputPort != NULL) {
    if (inputPort != thisEvent->atPort) {
    if (timeCompare(&(inputPort->modelTime), &(thisEvent->tag.timestamp)) == LESS) {
    safeThroughPropagation = FALSE;
    break;
    }
    }
    inputPort = inputPort->next;
    }*/


    //      if (safeThroughPropagation == TRUE) {
    //          timeSet(safeTimestamp, &ZERO_TIME);
    //      } else {
    // if (thisEvent->tag.timestamp = MAX_LONG_LONG) {
    //      safeTimestamp = 0; //always safe
    // } else {
	int out = timeSub(thisEvent->tag.timestamp, thisEvent->offsetTime, safeTimestamp);
	if (out == -1) {
		safeTimestamp->secs = 0;
		safeTimestamp->nsecs = 0;
	}
    // }
    //      }

#ifdef LCD_DEBUG
    //sprintf(str, "STP=%d", safeTimestamp->secs);
    //RIT128x96x4StringDraw(str, 0,40,15);
#endif
}
/**/

/*** initPIBlock ***/
// The platform independent initialization code goes here.
initializeMemory();
initializeEvents();
initializePISystem();
/**/

/*** initPICodeBlock ***/
// The platform independent initialization code goes here.
void initializeEvents(void) {
    // no event initialization is needed here... for now.
}

/* initialize global variables
 */
void initializeMemory() {
    int i;
    locationCounter = 0;
    _secs = 0;
    _quarterSecs = 0;

    for(i = 0; i < MAX_EVENTS; i++) {
        // event is "freed and can be returned by newEvent"
        eventMemory[i].inUse = MAX_EVENTS + 1; 
    }
}

void initializePISystem() {
    lastTimerInterruptTime = MAX_TIME;
}
/**/

/*** preinitPIBlock() ***/
// This is the platform independent preinitialization code
/**/

/*** mainLoopBlock ***/
void execute() {
    // peekingPoint keeps track which event to peek at.
    int peekingIndex = 0;
    // keeps track of how many events in the event queue cannot be processed because their
    // buffer has been blocked.
    Event* event;
    int whilecount = 0;
    Time processTime;
    Time physicalTime;
    disableInterrupts();
    while (true) {
        event = peekEvent(peekingIndex);
        if (event == NULL) {
            break;
        }
        // If event queue is not empty, keep going.
        whilecount++;
        #ifdef LCD_DEBUG
        //debugMessageNumber("wc = ", whilecount);
        #endif
        if (higherPriority(event) == TRUE) {
            safeToProcess(event, &processTime);
            #ifdef LCD_DEBUG
            //debugMessageNumber("hpwc=", whilecount);
            #endif
            getRealTime(&physicalTime);
            if (timeCompare(physicalTime, processTime) >= 0) {
                // We have decided to process this event, thus we let the rest
                // of the system know this decision by queuing the priority, and
                // setting the tag of this event.
                queuePriority(event);
                removeAndPropagateSameTagEvents(peekingIndex);
                setCurrentModelTag(event);
                enableInterrupts();
                // Execute the event. During
                // this process more events may
                // be posted to the queue.
                fireActor(event);
                // After an actor fires, we not sure which event is safe to process,
                // so we start examine the queue again start from the beginning.
                disableInterrupts();
                peekingIndex = 0;
            } else {
                // Set timed interrupt to run
                // the event at the top of the
                // event queue when physical time
                // has passed for it to be
                // safe to process
                if (timeCompare(processTime, lastTimerInterruptTime) == LESS) {
                    lastTimerInterruptTime = processTime;
                    setTimedInterrupt(&processTime);
                }
                // this event is not safe to process, we look at the next one.
                //peekingIndex++;
                break;
            }// end !(curentPhysicalTime >= processTime)
        } else {
            // This is the only place for us to break out of the while loop. The only other possibility
            // is that we have looked all all events in the queue, and none of them are safe to process.
            #ifdef LCD_DEBUG
            //debugMessageNumber("endwc=", peekingIndex);
            #endif
            break;
        }//end EVENT_QUEUE_HEAD != NULL
    }
    // This implies within the while loop, if all events cannot be processed,
    // we need to go through the entire event queue before another event can come in.
    enableInterrupts();
    // if all events are blocked because buffers are full
    // if (numBufferBlocked != 0 && numBufferBlocked == peekingIndex)
    #ifdef LCD_DEBUG
    //debugMessage("ret PE()");
    #endif

    while (1);
}
/**/
