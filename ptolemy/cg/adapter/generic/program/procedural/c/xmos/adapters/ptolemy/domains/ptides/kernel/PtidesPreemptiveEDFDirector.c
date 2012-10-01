/*** StructDefBlock ***/
#include <stdio.h>
#include <xccompat.h>
#define streaming
#include "Tests.h"

//-- PtidyOS.h --//
typedef unsigned long long uint64;
typedef unsigned int uint32;
typedef unsigned short uint16;
typedef unsigned char uint8;
typedef signed long long int64;
typedef signed int int32;
typedef signed short int16;
typedef signed char int8;
typedef uint8 boolean;

#define TRUE 1
#define FALSE 0
#define true TRUE
#define false FALSE
#define error(x) debug("Error: %s\n", x);

inline static unsigned streaming_chan_inuint(chanend c)
{
  unsigned value;
  asm (
    "in %0, res[%1]" :
    "=r"(value) :
    "r"(c)
  );
  return value;
}

inline static void streaming_chan_outuint(chanend c, unsigned value)
{
  asm (
    "out res[%0], %1" :
    /* no outputs */ :
    "r"(c), "r"(value)
  );
}

inline static unsigned char streaming_chan_inuchar(chanend c)
{
  unsigned value;
  asm (
    "int %0, res[%1]" :
    "=r"(value) :
    "r"(c)
  );
  return value;
}

inline static void streaming_chan_outuchar(chanend c, unsigned char value)
{
  asm (
    "outt res[%0], %1" :
    /* no outputs */ :
    "r"(c), "r"(value)
  );
}


//-- PlatformClock.h --//

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

uint32 countUntilTimestamp(const Time *timestamp,
        streaming chanend platformClockChannel);
void getTimestamp(Time *timestamp,
        streaming chanend platformClockChannel);
void getTimestampFromCount(Time *timestamp,
        streaming chanend platformClockChannel, uint32 count);

//-- Scheduler.h --//

typedef struct Event {
    union {
        char boolean_Value;
        char charValue;
        int16 shortValue;
        uint16 ushortValue;
        int32 intValue;
        uint32 uintValue;
        float floatValue;
        double double_Value;
        struct {
            uint32 u;
            uint32 l;
        } dataValue;
    } Val;
    Tag tag;
    uint32 depth;
    Time deadline;
    SignedTime offset;
    void (*fire)();
    struct Event** sinkEvent;
    struct Event* nextEvent;
    struct Event* prevEvent;

} Event;

#define NO_RESTART 0
#define RESTART 1

//TODO: figure out max
/* Maximum number of events. */
#define MAX_EVENTS 20

/* Maximum number of processing events. */
#define MAX_PROCESSING 10

enum {
    CMD_EVENT_ADD,
    CMD_EVENT_FREE,
};

typedef struct ActuationEvent {

    Time timestamp;
    union {
        char boolean_Value;
        char charValue;
        int16 shortValue;
        uint16 ushortValue;
        int32 intValue;
        uint32 uintValue;
        float floatValue;
        double double_Value;
        struct {
            uint32 u;
            uint32 l;
        } dataValue;
    } Val;
    void (*fire)();
    struct ActuationEvent* nextEvent;

} ActuationEvent;

/* Maximum number of actuation events on queue. */
#define MAX_ACTUATION_EVENTS 10

/**/

/*** CommonTypeDefinitions ***/
/**/

/*** FuncProtoBlock ***/
uint8 checkChannels(streaming chanend schedulerChannels[], uint8 numChan);
int8 compareEvents(const Event* a, const Event* b);
int8 compareTimestamps(const Time* a, const Time* b);
Event* getFreeEvent();
void initalizeFreeEventList();
Event* peekProcessing();
uint8 currProcessing();
void popProcessing();
void printEvent(const Event* event);
void processEvent(Event* event,
        streaming chanend eventProcessingChannel);
void pushProcessing(Event* event);
uint8 safeToProcess(const Event* event, const Time* platformTime);
void timestampAdd(const Time* a, const Time* b, Time* result);
void timestampSub(const Time* a, const Time* b, Time* result);

double multiply_Int_Double(int a1, double a2);
inline double divide_Double_Double(double a1, double a2);
double multiply_Double_Double(double a1, double a2);

void addActuationEvent(streaming chanend actuatorChannel,
        Event* event, uint32 type);
ActuationEvent* getFreeActuationEvent();
/**/

/*** ActuationBlock($Actuators, $actuationCaseStatement) ***/
enum {
    //ACT_CONTACT, ACT_CUT, ACT_RESERVE_PAPER_VELOCITY, TEST
        $Actuators
};


// Chanend to platform clock.
uint32 platformClockChanend;

// Timer count value for actuation event at front of queue.
uint32 nextActuationEventCount;

// Allocate memory for actuation events.
ActuationEvent actuationEventMemory[MAX_ACTUATION_EVENTS];

/* Singly linked list of free actuation events. */
ActuationEvent* freeActuationEventList = NULL;

/* Singly linked list for actuation event queue. */
ActuationEvent* actuationEventQueueHead = NULL;

/* Build singly linked list of free actuation events. */
void initializeFreeActuationEventList() {

    uint16 i;
    for(i = 1; i < MAX_ACTUATION_EVENTS; i++) {
        actuationEventMemory[i - 1].nextEvent = &actuationEventMemory[i];
    }
    actuationEventMemory[i - 1].nextEvent = NULL;
    freeActuationEventList = &actuationEventMemory[0];

}

// Store chanend to platform clock to global variable.
void initializePlatformClockChanend(streaming chanend c) {
    platformClockChanend = c;
}

ActuationEvent* getFreeActuationEvent() {

    // Get event from front of list.
    ActuationEvent* event = freeActuationEventList;

    // Remove from front of list.
    freeActuationEventList = event->nextEvent;

    // Check list.
    if(freeActuationEventList == NULL) {
        error("getFreeActuationEvent(): Out of free events.");
    }

    return event;

}

// Return timer count of actuation event at front of queue.
uint32 getNextActuationEventCount() {
    //return 0;
    return nextActuationEventCount;
}

/* Add actuation event queue by sending pertinent event information to
 * actuator thread. Actuator thread will add an actuation event to the
 * correct location in the actuation event queue.
 */
void addActuationEvent(streaming chanend actuatorChannel, Event* event,
        uint32 type) {

    // Send event information.
    streaming_chan_outuint(actuatorChannel, type);
    streaming_chan_outuint(actuatorChannel, event->tag.timestamp.secs);
    streaming_chan_outuint(actuatorChannel, event->tag.timestamp.nsecs);
    streaming_chan_outuint(actuatorChannel, event->Val.dataValue.u);
    streaming_chan_outuint(actuatorChannel, event->Val.dataValue.l);
}

/* Add actuation event to actuation event queue by receiving pertinent
 * event information from the event processing thread, creating
 * an acutation event, and adding to correct location on acutation queue.
 * If it is added to front of the queue, the timer count is updated.
 */
void processAddActuationEvent(streaming chanend actuatorChannel, uint32 type) {

    ActuationEvent* newEvent = getFreeActuationEvent();
    ActuationEvent* currEvent;
    ActuationEvent* prevEvent;

    // Receive event information.
    newEvent->timestamp.secs = streaming_chan_inuint(actuatorChannel);
    newEvent->timestamp.nsecs = streaming_chan_inuint(actuatorChannel);
    newEvent->Val.dataValue.u = streaming_chan_inuint(actuatorChannel);
    newEvent->Val.dataValue.l = streaming_chan_inuint(actuatorChannel);

    // Assign correct function.

    $actuationCaseStatement

    // Add to correct location in event actuation queue.
    // If actuation event queue is empty, add to front.
    if(actuationEventQueueHead == NULL) {
        actuationEventQueueHead = newEvent;
        newEvent->nextEvent = NULL;
        nextActuationEventCount = countUntilTimestamp(&newEvent->timestamp,
                platformClockChanend);
        //TODO what if more than 42s in future?

    } else {
        currEvent = actuationEventQueueHead;
        prevEvent = NULL;
        // Find location to insert by comparing timestamps.
        while(currEvent != NULL) {
            if(compareTimestamps(&newEvent->timestamp, &currEvent->timestamp) <= 0) {
                break;
            }
            prevEvent = currEvent;
            currEvent = currEvent->nextEvent;
        }

        if(prevEvent == NULL) {
            // Insertion at front.
            newEvent->nextEvent = actuationEventQueueHead;
            actuationEventQueueHead = newEvent;
            nextActuationEventCount = countUntilTimestamp(&newEvent->timestamp, platformClockChanend);
            //TODO what if more than 42s in future?
        } else {
            // Insert between prev and curr.
            prevEvent->nextEvent = newEvent;
            newEvent->nextEvent = currEvent;
        }

    }



}


//TODO: figure out stack requirement. It is the worst case of all
// actuation fire functions.
#pragma stackfunction 10
uint8 processActuationEvent() {

    ActuationEvent* event;

    if(actuationEventQueueHead == NULL) {
        error("processActuationEvent(): Queue empty")
    }
    event = actuationEventQueueHead;

    // Remove from actuation queue.
    actuationEventQueueHead = event->nextEvent;

    // Add to free actuation event list.
    event->nextEvent = freeActuationEventList;
    freeActuationEventList = event;

    // Fire function to perform actuation.
    event->fire();

    if(actuationEventQueueHead == NULL) {
        return FALSE;
    } else {
        //TODO what if more than 42s in future?
        nextActuationEventCount = countUntilTimestamp(&actuationEventQueueHead->timestamp, platformClockChanend);
        return TRUE;
    }

}

/**/

/*** SchedulerBlock ***/
// Allocate memory for events.
Event eventMemory[MAX_EVENTS];

/* Singly linked list of free events. */
Event* freeEventList = NULL;

/* Doubly linked list for event queue. */
Event* eventQueueHead = NULL;
Event* eventQueueTail = NULL;

// Keep track of processing events.
Event* processingEvents[MAX_PROCESSING];
uint16 processingEventsIndex = 0;


/* Operation of event safe-to-process and scheduling. Also handles requests
 * to add events to event queue and free events when they are done processing.
 * Checks for request commands are at specified locations in the program,
 * so another thread by be forced to block for at most the time interval
 * between checks.
 * getTimestamp and processEvent limit responsiveness of modification to
 * event queue.
 * TODO: pause execution?
 * TODO: switch to interrupts
 */
void runScheduler(streaming chanend schedulerChannels[], uint8 numChan,
        streaming chanend eventProcessingChannel,
        streaming chanend platformClockChannel) {

    Event* event;
    Time platformTime;

    // Initialize free event list.
    initalizeFreeEventList();

    while(1) {
        // Check channels for any event queue requests.
        checkChannels(schedulerChannels, numChan);
        // Start at front of event queue.
        event = eventQueueHead;
        // No reason to get timestamp if no events in event queue.
        // But what about update?
        //if(event == NULL) {
        //    continue;
        //}
        // Get current platform time for safe-to-process.
        getTimestamp(&platformTime, platformClockChannel);
        // Search queue for highest priority event which is safe to process.
        while(event != NULL) {
            // Check channels for any event requests.
            // If request results in event queue modification, need to
            // restart search.
            //storeTestTime(7);
            if(checkChannels(schedulerChannels, numChan) == RESTART) {
                break;
            }
            //storeTestTime(8);
            // If the event has a higher priority than the currently
            // processing event, then safe-to-process can be checked.
            if((!currProcessing() ||
                    compareEvents(event, peekProcessing()) < 0)) {
                if(safeToProcess(event, &platformTime)) {
                    //storeTestTime(9);
                    // Store event as being currently processed.
                    pushProcessing(event);
                    // Process the event(s).
                    processEvent(event, eventProcessingChannel);
                    // No events past this will have a higher priority, so
                    // restart search.
                    //storeTestTime(10);
                    break;
                }
                // Check next event in event queue.
                event = event->nextEvent;
            // No events past this will have a higher priority, so
            // restart search.
            } else {
                break;
            }
        }
    }

}

// a < b -> -1 :: a == b -> 0 :: a > b -> 1
int8 compareEvents(const Event* a, const Event* b) {

    uint8 compare;
    if(a == NULL || b == NULL) {
        error("compareEvents(): NULL input.");
    }
    compare = compareTimestamps(&a->deadline, &b->deadline);
    if(compare != 0) {
        return compare;
    } else {
        compare = compareTimestamps(&a->tag.timestamp, &b->tag.timestamp);
        if(compare != 0) {
            return compare;
        } else {
            if(a->tag.microstep < b->tag.microstep) {
                return -1;
            } else if(a->tag.microstep > b->tag.microstep) {
                return 1;
            } else {
                if(a->depth < b->depth) {
                    return -1;
                } else if(a->depth > b->depth) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }
}

// a < b -> -1 :: a == b -> 0 :: a > b -> 1
int8 compareTimestamps(const Time* a, const Time* b) {

    if(a->secs < b->secs) {
        return -1;
    } else if((a->secs == b->secs) && (a->nsecs < b->nsecs)) {
        return -1;
    } else if((a->secs == b->secs) && (a->nsecs == b->nsecs)) {
        return 0;
    } else {
        return 1;
    }
}

uint8 currProcessing() {
    if(processingEventsIndex > 0) {
        return TRUE;
    } else {
        return FALSE;
    }
}

/* Send command to add event and event pointer to scheduler thread.
 * Used by calling thread.
 */
void addEvent(chanend schedulerChannel, const Event* event) {

    // Send command to add event to event queue.
    streaming_chan_outuint(schedulerChannel, CMD_EVENT_ADD);
    //streaming_chan_outuchar(schedulerChannel, CMD_EVENT_ADD);

    // Send contents of event.
    streaming_chan_outuint(schedulerChannel, event->Val.dataValue.u);
    streaming_chan_outuint(schedulerChannel, event->Val.dataValue.l);
    streaming_chan_outuint(schedulerChannel, event->tag.timestamp.secs);
    streaming_chan_outuint(schedulerChannel, event->tag.timestamp.nsecs);
    streaming_chan_outuint(schedulerChannel, (uint32) event->tag.microstep);
    streaming_chan_outuint(schedulerChannel, event->depth);
    streaming_chan_outuint(schedulerChannel, event->deadline.secs);
    streaming_chan_outuint(schedulerChannel, event->deadline.nsecs);
    streaming_chan_outuint(schedulerChannel, (uint32) event->offset.secs);
    streaming_chan_outuint(schedulerChannel, (uint32) event->offset.nsecs);
    streaming_chan_outuint(schedulerChannel, (uint32) event->fire);
    streaming_chan_outuint(schedulerChannel, (uint32) event->sinkEvent);

}

/* Remove event from free event list and return pointer to it.
 * Used only by scheduler thread.
 */
Event* getFreeEvent() {

    // Get event from front of list.
    Event* event = freeEventList;

    // Remove from front of list.
    freeEventList = event->nextEvent;

    // Check list.
    if(freeEventList == NULL) {
        error("getFreeEvent(): Out of free events.");
    }

    return event;

}

/* Build singly linked list of free events. */
void initalizeFreeEventList() {

    uint16 i;
    for(i = 1; i < MAX_EVENTS; i++) {
        eventMemory[i - 1].nextEvent = &eventMemory[i];
    }
    eventMemory[i - 1].nextEvent = NULL;
    freeEventList = &eventMemory[0];

}

Event* peekProcessing() {
    return processingEvents[processingEventsIndex];
}

void popProcessing() {
    if(processingEventsIndex > 0) {
        processingEventsIndex--;
    } else {
        error("popProcessing(): No processing events to remove.");
    }
}

/* Receive pointer of event to add and add to correct location in event
 * queue. Used only by scheduler thread.
 */
// TODO: is comparing with head, the searching from tail best way? < or <=?
void processAddEvent(chanend schedulerChannel) {

    Event* currEvent;
    // Get pointer to event.
    Event* newEvent = getFreeEvent();

    //storeTestTime(4);
    // Get contents of event to add from channel.
    newEvent->Val.dataValue.u = streaming_chan_inuint(schedulerChannel);
    newEvent->Val.dataValue.l = streaming_chan_inuint(schedulerChannel);
    newEvent->tag.timestamp.secs = streaming_chan_inuint(schedulerChannel);
    newEvent->tag.timestamp.nsecs = streaming_chan_inuint(schedulerChannel);
    newEvent->tag.microstep = (uint16) streaming_chan_inuint(schedulerChannel);
    newEvent->depth = streaming_chan_inuint(schedulerChannel);
    newEvent->deadline.secs = streaming_chan_inuint(schedulerChannel);
    newEvent->deadline.nsecs = streaming_chan_inuint(schedulerChannel);
    newEvent->offset.secs = (int32)streaming_chan_inuint(schedulerChannel);
    newEvent->offset.nsecs = (int32)streaming_chan_inuint(schedulerChannel);
    newEvent->fire = (void*)streaming_chan_inuint(schedulerChannel);
    newEvent->sinkEvent = (Event**)streaming_chan_inuint(schedulerChannel);

    //storeTestTime(5);
    // Add to correct location in event queue.
    // If event queue is empty, add to front.
    if(eventQueueHead == NULL) {
        eventQueueHead = newEvent;
        eventQueueTail = newEvent;
        newEvent->prevEvent = NULL;
        newEvent->nextEvent = NULL;
    } else {
        // Check if it has earliest deadline.
        if(compareEvents(newEvent, eventQueueHead) <= 0) {
            newEvent->nextEvent = eventQueueHead;
            eventQueueHead->prevEvent = newEvent;
            newEvent->prevEvent = NULL;
            eventQueueHead = newEvent;
        // Otherwise start at tail and put in correct deadline order.
        } else {
            currEvent = eventQueueTail;
            while(compareEvents(newEvent, currEvent) < 0) {
                currEvent = currEvent->prevEvent;
                if(currEvent == NULL) {
                    error("processAddEvent(): Insert event failed.");
                }
            }
            // newEvent needs to go after currEvent.
            newEvent->prevEvent = currEvent;
            newEvent->nextEvent = currEvent->nextEvent;
            currEvent->nextEvent = newEvent;
            if(currEvent != eventQueueTail) {
                newEvent->nextEvent->prevEvent = newEvent;
            } else {
                // Adding to end.
                eventQueueTail = newEvent;
            }
        }

    }
    //storeTestTime(6);

    //TODO: Check whether search in event queue should be restarted.

}

void printEvent(const Event* event) {

    debug("Event = %X\n", (uint32)event);
    debug("\tvalue.uintValue = %u\n", event->Val.uintValue);
    debug("\ttag.timestamp.secs = %u\n", event->tag.timestamp.secs);
    debug("\ttag.timestamp.nsecs = %u\n", event->tag.timestamp.nsecs);
    debug("\ttag.microstep = %u\n", event->tag.microstep);
    debug("\tdepth = %u\n", event->depth);
    debug("\tdeadline.secs = %u\n", event->deadline.secs);
    debug("\tdeadline.nsecs = %u\n", event->deadline.nsecs);
    debug("\toffset.secs = %d\n", event->offset.secs);
    debug("\toffset.nsecs = %d\n", event->offset.nsecs);
    debug("\tfire = %X\n", (uint32)event->fire);
    debug("\tsinkEvent = %X\n", (uint32)event->sinkEvent);
    debug("\tnextEvent = %X\n", (uint32)event->nextEvent);
    debug("\tprevEvent = %X\n", (uint32)event->prevEvent);

}

void printEventQueue() {

    Event* currEvent = eventQueueHead;
    while(currEvent != NULL) {
        printEvent(currEvent);
        currEvent = currEvent->nextEvent;
    }

}

/* Process event. */
// TODO doesn't work for all actors (different input port groups)
// Assumes all events that need to be passed to actor are next
// to each other in event queue.
void processEvent(Event* event,
        streaming chanend eventProcessingChannel) {

    Event* currEvent;
    Event* lastEvent;

    // Send all input events to the actor, which are all events with same
    // tag and same destination actor. Also need to be same inport port
    // group, but currently doesn't check.
    currEvent = event;
    do {

        // Provide pointer to input port.
        streaming_chan_outuint(eventProcessingChannel,
                (uint32) currEvent->sinkEvent);

        // Provide currEvent.
        streaming_chan_outuint(eventProcessingChannel,
                currEvent->Val.dataValue.u);
        streaming_chan_outuint(eventProcessingChannel,
                currEvent->Val.dataValue.l);
        streaming_chan_outuint(eventProcessingChannel,
                currEvent->tag.timestamp.secs);
        streaming_chan_outuint(eventProcessingChannel,
                currEvent->tag.timestamp.nsecs);
        streaming_chan_outuint(eventProcessingChannel,
                (uint32) currEvent->tag.microstep);
        streaming_chan_outuint(eventProcessingChannel,
                currEvent->depth);
        streaming_chan_outuint(eventProcessingChannel,
                currEvent->deadline.secs);
        streaming_chan_outuint(eventProcessingChannel,
                currEvent->deadline.nsecs);
        streaming_chan_outuint(eventProcessingChannel,
                (uint32) currEvent->offset.secs);
        streaming_chan_outuint(eventProcessingChannel,
                (uint32) currEvent->offset.nsecs);

        lastEvent = currEvent;
        // Check next event.
        currEvent = currEvent->nextEvent;


    } while(currEvent != NULL && (compareTimestamps(&event->tag.timestamp,
                    &currEvent->tag.timestamp) == 0) &&
            event->fire == currEvent->fire);


    // Tell actor it can fire.
    streaming_chan_outuint(eventProcessingChannel, 0);
    streaming_chan_outuint(eventProcessingChannel, (uint32) event->fire);

    // Remove event(s) from event queue.
    if(event->prevEvent != NULL) {
        (event->prevEvent)->nextEvent = lastEvent->nextEvent;
    } else {
        // Removing event(s) at head.
        eventQueueHead = lastEvent->nextEvent;
        if(eventQueueHead != NULL) {
            // Not removing only event(s).
            eventQueueHead->prevEvent = NULL;
        }
    }
    if(lastEvent->nextEvent != NULL) {
        (lastEvent->nextEvent)->prevEvent = event->prevEvent;
    } else {
        // Removing event(s) at tail.
        eventQueueTail = event->prevEvent;
        if(eventQueueTail != NULL) {
            // Not removing only event(s).
            eventQueueTail->nextEvent = NULL;
        }
    }

    // For list.
    event->prevEvent = lastEvent;

}

/* Receive pointer of event to free and add it to free event list.
 * Used only by scheduler thread.
 */
void processFreeEvent(streaming chanend schedulerChannel) {

    // Get pointer to currently processing event.
    Event* event = peekProcessing();

    if(event == NULL) {
        error("processFreeEvent(): No processing events to remove.");
    }

    // Add list of events to front of free event list.
    // The prevEvent pointer of the head event is used to point to
    // the last event in the list.
    (event->prevEvent)->nextEvent = freeEventList;
    freeEventList = event;

    // Remove this processing event.
    popProcessing();

}

void pushProcessing(Event* event) {
    processingEventsIndex++;
    processingEvents[processingEventsIndex] = event;
}

/* Determine whether an event is safe-to-process at provided platform time.
 * Returns TRUE if (event.tag - offset) <= platform time
 */
uint8 safeToProcess(const Event* event, const Time* platformTime) {

    Time tempTimestamp;
    Time safeTimestamp;
    // addTimestamp and subTimestamp require Timestamp, not SignedTimestamp.
    if((event->offset.secs < 0) ||
            ((event->offset.secs == 0) && (event->offset.nsecs < 0))) {
        tempTimestamp.secs = (uint32) (-event->offset.secs);
        tempTimestamp.nsecs = (uint32) (-event->offset.nsecs);
        timestampAdd(&event->tag.timestamp, &tempTimestamp, &safeTimestamp);

    } else {
        tempTimestamp.secs = (uint32) (event->offset.secs);
        tempTimestamp.nsecs = (uint32) (event->offset.nsecs);
        timestampSub(&event->tag.timestamp, &tempTimestamp, &safeTimestamp);
    }
    if(compareTimestamps(&safeTimestamp, platformTime) <= 0) {
        return TRUE;
    } else {
        return FALSE;
    }

}

void timestampAdd(const Time* a, const Time* b, Time* result) {

    result->secs = a->secs + b->secs;
    result->nsecs = a->nsecs + b->nsecs;
    if(result->nsecs >= 1000000000) {
        result->nsecs -= 1000000000;
        result->secs++;
    }

}

//TODO: negative result?
void timestampSub(const Time* a, const Time* b, Time* result) {

    if(compareTimestamps(a, b) == -1) {
        error("timestampSub(): Negative result.");
        result->secs = 0;
        result->nsecs = 0;
    }
    result->secs = a->secs - b->secs;
    result->nsecs = a->nsecs - b->nsecs;
    if(a->nsecs < b->nsecs) {
        result->nsecs += 1000000000;
        result->secs -= 1;
    }

}

//-- EventProcessing.h --//

// Chanend to allow ISR to get input event(s).
uint32 eventProcessingChanend;

// Chanend to allow actors to add events.
uint32 schedulerChanend;

// Chanend to allow actors to add actuation event.
uint32 actuatorChanend;


double multiply_Int_Double(int a1, double a2) {
    return a1 * a2;
}
inline double divide_Double_Double(double a1, double a2) {
    return a1 / a2;
}
double multiply_Double_Double(double a1, double a2) {
    return a1 * a2;
}


/**/

// If more int's are to be added to the argument of FuncBlock, change the
// maxNumSensorInputs field in PtidesBasicDirector.
/*** FuncBlock($dis1, $dis2, $dis3, $dis4, $dis5, $dis6, $dis7, $dis8,
$en1, $en2, $en3, $en4, $en5, $en6, $en7, $en8) ***/


/**/

/*** initPDBlock***/

/**/

/*** preinitPDBlock()***/
/**/

/*** wrapupPDBlock() ***/
/**/


/*** mainLoopBlock ***/
void execute() {
        // FIXME ADD XMOS CODE
}

void processEvents() {
        // FIXME ADD XMOS CODE
}
/**/


/*** XCCodeBlock(
$sensorDefinition,
$sensorHandlerPrototypes,
$actuatorDefinition,
$sensorReadyFlags,
$sensorSwitch,
$actuationFunctions,
$initActuators) ***/



#include <platform.h>
#include <xs1.h>
#include <stdio.h>

//-- PtidyOS.h --//

typedef unsigned int uint32;
typedef unsigned short uint16;
typedef unsigned char uint8;
typedef signed int int32;
typedef signed short int16;
typedef signed char int8;
typedef uint8 boolean;

#define TRUE 1
#define FALSE 0
#define true TRUE
#define false FALSE
#define error(x) debug("Error: %s\n", x);

void runPtidyOS(streaming chanend debugChannel);


//-- PlatformClock.h --//

typedef struct {
    uint32 secs;
    uint32 nsecs;
} Time;

/* Clock information for software clock. Since tick rate of hardware timer
 * is driven by an oscillator which cannot be controlled, a rate adjustment
 * (conversion factor) is required to convert duration in ticks to duration
 * of time. The reference timestamp and count needs to be updated at least
 * every 2^32 ticks and when rate adjustment is modified.
 */
typedef struct {
    Time lastTimestamp; /*< Reference timestamp. */
    uint32 lastCount; /*< Timer count value at reference timestamp. */
    uint32 rateAdj; /*< Conversion factor to change timer ticks
    * to time duration. Expressed as fixed point with 31 fractional bits.
    * Fixed point is used because floating point in software is too slow.
    */
    uint32 rateAdjInv; /*< Inverse of rateAdj (2^62 / rateAdj). */
} Clock;

/* Commands for interaction with platform clock. */
enum {
    CMD_CLOCK_GET, /*< Get clock information. */
    CMD_CLOCK_UPDATE, /*<  Update reference timestamp and timer count. */
    CMD_CLOCK_RATEADJ /*< Update rate adjustment (conversion factor). */
};

void countToTimestamp(Time &timestamp,
        streaming chanend platformClockChannel,
        uint32 count, uint8 rateAdjFlag);
void getPlatformClock(streaming chanend platformClockChannel,
        Clock &localClk);
void getTimestamp(Time &timestamp,
        streaming chanend platformClockChannel);
void getTimestampFromCount(Time &timestamp,
        streaming chanend platformClockChannel, uint32 count);
void initPlatformClock();
void runPlatformClock(streaming chanend platformClockChannels[],
        uint8 numChan);
void updatePlatformClock(streaming chanend platformClockChannel,
        const Time &timestamp, uint32 count);
void updateRateAdj(streaming chanend platformClockChannel, uint32 newRateAdj,
        uint32 newRateAdjInv);


//-- Scheduler.h --//

#define NO_RESTART 0
#define RESTART 1

enum {
    CMD_EVENT_ADD,
    CMD_EVENT_FREE,
};

void processAddEvent(streaming chanend schedulerChannel);
void processFreeEvent(streaming chanend schedulerChannel);
void runScheduler(streaming chanend schedulerChannels[], uint8 numChan,
        streaming chanend eventProcessingChannel,
        streaming chanend platformClockChannel);


//-- Sensors.h --//

$sensorDefinition

void runSensors(streaming chanend platformClockChannel,
        streaming chanend schedulerChannel);


//-- EventProcessing.h --//

void runEventProcessing(streaming chanend eventProcessingChannel,
        streaming chanend schedulerChannel, streaming chanend actuatorChannel);

$sensorHandlerPrototypes


//-- Actuators.h --//

$actuatorDefinition

uint32 getNextActuationEventCount();
void initializeActuators();
void initializeFreeActuationEventList();
void initializePlatformClockChanend(streaming chanend c);
void processAddActuationEvent(streaming chanend actuatorChannel, uint32 type);
uint8 processActuationEvent();
void runActuators(streaming chanend platformClockChannel,
        streaming chanend actuatorChannel);

#include "Tests.h"

//-- PtidyOS.xc --//

/* Main function. Run PtidyOS and support functions for debug and testing. */
int main() {

    // Channels for cores to communicate debug strings to core responsible
    // with printing debug strings to computer.
    streaming chan debugChannels[2];

    par {
        on stdcore[0]: runDebug(debugChannels, 2);
        on stdcore[1]: runPtidyOS(debugChannels[0]);
        on stdcore[2]: testPtidyOS(debugChannels[1]);
    }
    return 0;
}

/* Perform initialization and start threads to operate PtidyOS. */
void runPtidyOS(streaming chanend debugChannel) {

    // Channels to allow threads access to platform clock.
    streaming chan platformClockChannels[3];
    // Channels to allow threads to communicate with scheduler thread.
    streaming chan schedulerChannels[2];
    // Channel from scheduler thread to interrupt event processing thread.
    streaming chan eventProcessingChannel;
    // Channel from event processing thread to deliver acutation event
    // to acutator thread.
    streaming chan actuatorChannel;

    // Set chanend for communication with debug core.
    setDebug(debugChannel);

    // Initialize software timestamping of points during execution.
    initTestTime();

    debug("Starting PtidyOS\n");

    // Start threads.
    par {

        // Run platform clock.
        runPlatformClock(platformClockChannels, 3);

        // Run sensors.
        runSensors(platformClockChannels[0], schedulerChannels[0]);
        //runTests(platformClockChannels[0], schedulerChannels[0]);

        // Run scheduler.
        runScheduler(schedulerChannels, 2,
                eventProcessingChannel, platformClockChannels[1]);

        // Run event processing.
        runEventProcessing(eventProcessingChannel, schedulerChannels[1],
                actuatorChannel);

        // Run actuators.
        runActuators(platformClockChannels[2], actuatorChannel);

        // Test thread.
        //testPlatformClock(platformClockChannels[3]);

    }

}

//-- PlatformClock.xc --//

// Platform clock.
Clock clk;

/* Start operation of the platform clock. Platform clock is a thread which
 * maintains platform clock information and provides it to other threads
 * when requested though channels. Threads can also request last timestamp
 * and count to be updated, as well as rate adjustment.
 */
#pragma unsafe arrays // Disable generation of run-time safety checks
                      // for accessing invalid index of array.
void runPlatformClock(streaming chanend platformClockChannels[],
        uint8 numChan) {

    uint32 cmd;

    // Initialize platform clock.
    initPlatformClock();

    while(1) {
        select {
        // Loop through all channel connections and react to requests.
        case (int i = 0; i < numChan; i++) platformClockChannels[i] :> cmd:
            switch(cmd) {

            // Send clock information.
            case CMD_CLOCK_GET:

                platformClockChannels[i] <: clk.lastTimestamp.secs;
                platformClockChannels[i] <: clk.lastTimestamp.nsecs;
                platformClockChannels[i] <: clk.lastCount;
                platformClockChannels[i] <: clk.rateAdj;
                platformClockChannels[i] <: clk.rateAdjInv;
                break;

            // Update rate adjustment (receive clock information).
            case CMD_CLOCK_RATEADJ:

                platformClockChannels[i] :> clk.lastTimestamp.secs;
                platformClockChannels[i] :> clk.lastTimestamp.nsecs;
                platformClockChannels[i] :> clk.lastCount;
                platformClockChannels[i] :> clk.rateAdj;
                platformClockChannels[i] :> clk.rateAdjInv;
                break;

            // Update last timestamp and last count.
            case CMD_CLOCK_UPDATE:

                platformClockChannels[i] :> clk.lastTimestamp.secs;
                platformClockChannels[i] :> clk.lastTimestamp.nsecs;
                platformClockChannels[i] :> clk.lastCount;
                break;

            }
            break;
        }
    }
}

/* Convert timer count value to timestamp using clock information.
* If rateAdjFlag is false and too much time has elasped since the last update,
* the last timestamp and last count are updated.
*/
void countToTimestamp(Time &timestamp,
        streaming chanend platformClockChannel,
        uint32 count, uint8 rateAdjFlag) {

    Clock localClk;
    uint32 ticks;
    uint32 ns_h, ns_l;
    uint32 q, r;
    uint32 ns_per_s = 1000000000;
    uint8 zero = 0;

    // Get clock information.
    getPlatformClock(platformClockChannel, localClk);

    // Number of timer ticks since last time lastCount and
    // lastTimestamp was updated.
    ticks = count - localClk.lastCount;

    // Convert ticks to nanoseconds.
    //ns = ((10 * ticks * clk->rateAdj) / 2^31);
    // Note: update threshold must be set to prevent overflow.
    // For 31 fractional bits:
    // ticks * 10 (ticks to ~ns) * 2 (max rateAdj) * 2^31 < 2^64
    // ticks < 429496729
    ns_l = ticks * 10;
    asm("lmul %0,%1,%2,%3,%4,%5" : "=r"(ns_h), "=r"(ns_l) :
            "r"(ns_l), "r"(localClk.rateAdj), "r"(zero), "r"(zero));
    ns_l = (ns_h << 1) | (ns_l >> 31);
    ns_h = (ns_h >> 31);

    // Add nanoseconds to last timestamp.
    //timestamp.secs = clk.lastTimestamp.secs + (ns / 1000000000);
    //timestamp.nsecs = clk.lastTimestamp.nsecs + (ns % 1000000000);
    asm("ldivu %0,%1,%2,%3,%4" : "=r"(q), "=r"(r) :
            "r"(ns_h), "r"(ns_l), "r"(ns_per_s));
    timestamp.secs = localClk.lastTimestamp.secs + q;
    timestamp.nsecs = localClk.lastTimestamp.nsecs + r;

    // Check for ns field overflow.
    if(timestamp.nsecs >= ns_per_s) {
        timestamp.secs += 1;
        timestamp.nsecs -= ns_per_s;
    }

    // If too much time has passed since last time lastCount and
    // lastTimestamp was updated, then update clock information.
    // If rate is being adjusted, this will happen elsewhere, so
    // don't do twice.
    // For 31 fractional bits: ticks < 429496729/2 = 0x0CCCCCCC
    if(!rateAdjFlag && (ticks > 0x0CCCCCCC)) {
        updatePlatformClock(platformClockChannel, timestamp, count);
    }

}

/* Return timer count value which corresponds to provided future timestamp. */
uint32 countUntilTimestamp(const Time &timestamp,
        streaming chanend platformClockChannel) {

    Clock localClk;
    uint32 ns_h, ns_l;
    uint32 count_h, count_l;
    uint32 ten = 10;
    uint32 q, r;

    // Get clock information.
    getPlatformClock(platformClockChannel, localClk);

    // Convert difference between timestamps to nanoseconds.
    ns_l = timestamp.nsecs - localClk.lastTimestamp.nsecs - 514; //FIXME: manual d_n
    {ns_h, ns_l} = lmul(timestamp.secs - localClk.lastTimestamp.secs,
            1000000000, ns_l, 0);
    if(timestamp.nsecs < localClk.lastTimestamp.nsecs) {
        ns_h--;
    }

    // Divide by 10.
    asm("ldivu %0,%1,%2,%3,%4" : "=r"(q), "=r"(r) :
                "r"(ns_h), "r"(ns_l), "r"(ten));

    // Rate adjust.
    {count_h, count_l} = lmul(q, localClk.rateAdjInv, 0, 0);
    count_l = (count_h << 1) | (count_l >> 31);
    count_h = (count_h >> 31);

    if(count_h != 0) {
        error("countUntilTimestamp(): Too far in future.");
    }

    //TODO make sure count returned isn't before current count? This occurs if
    // actuation event is send too close to deadline. Should be taken care
    // of in PTIDES model.
    return (localClk.lastCount + count_l);

}

/* Return the platform clock information by requesting it from the platform
 * clock thread through a channel.
 * Note: Can be temporarily blocked if other thread is requesting data from
 * platform clock thread.
 */
void getPlatformClock(streaming chanend platformClockChannel,
        Clock &localClk) {

    // Send command to receive clock information.
    platformClockChannel <: (uint32) CMD_CLOCK_GET;

    // Receive clock information.
    platformClockChannel :> localClk.lastTimestamp.secs;
    platformClockChannel :> localClk.lastTimestamp.nsecs;
    platformClockChannel :> localClk.lastCount;
    platformClockChannel :> localClk.rateAdj;
    platformClockChannel :> localClk.rateAdjInv;

}


/* Return current timestamp of the platform clock. Since no count is provided,
 * the current count of the timer is read, clock information is requested from
 * platform clock thread, and the timer count is converted to a timestamp.
 * Used by calling thread.
 * Note: Can be temporarily blocked if other thread is requesting data from
 * platform clock thread.
 */
void getTimestamp(Time &timestamp,
        streaming chanend platformClockChannel) {

    timer time;
    uint32 count;

    // Get current timer count.
    time :> count;

    countToTimestamp(timestamp, platformClockChannel, count, FALSE);

}

/* Return timestamp of the platform clock at specific count value of timer,
 * which is provided as an argument. This is done by requesting clock
 * information from the platform clock thread, then coverting count to a
 * timestamp. Used by calling thread.
 * Note: Can be temporarily blocked if other thread is requesting data from
 * platform clock thread.
 */
void getTimestampFromCount(Time &timestamp,
        streaming chanend platformClockChannel, uint32 count) {
    countToTimestamp(timestamp, platformClockChannel, count, FALSE);
}

/* Initialize the platform clock information to default settings. Used by
 * platform clock thread. */
void initPlatformClock() {

    timer time;

    //TODO: allow to customize?
    clk.lastTimestamp.secs = 0;
    clk.lastTimestamp.nsecs = 0;
    time :> clk.lastCount;
    clk.rateAdj = 0x80000000;
    clk.rateAdjInv = 0x80000000;

}

/* Update last timestamp and last count of the platform clock by sending the
 * values to the platform clock thread through a channel.
 * Note: Can be temporarily blocked if other thread is requesting data from
 * platform clock thread.
 */
void updatePlatformClock(streaming chanend platformClockChannel,
        const Time &timestamp, uint32 count) {

    // Send command to update last timestamp and last count.
    platformClockChannel <: (uint32) CMD_CLOCK_UPDATE;
    //    platformClockChannel <: (uint8) CMD_CLOCK_UPDATE;

    // Send updated last timestamp and last count.
    platformClockChannel <: timestamp.secs;
    platformClockChannel <: timestamp.nsecs;
    platformClockChannel <: count;

}

/* Update the rate adjustment value of the platform clock. Since the rate
 * adjustment value is being changed, the last count and last timestamp of
 * the platform clock must be updated at the same time. Updated values
 * are sent to the platform clock thread through a channel. Used by calling
 * thread.
 * Note: Can be temporarily blocked if other thread is requesting data from
 * platform clock thread.
 */
void updateRateAdj(streaming chanend platformClockChannel, uint32 newRateAdj,
        uint32 newRateAdjInv) {

    Time timestamp;
    timer time;
    uint32 count;

    // Get current timer count.
    time :> count;

    // Convert count to timestamp (and force update).
    countToTimestamp(timestamp, platformClockChannel, count, TRUE);

    // Send command to update rate adjustment.
    platformClockChannel <: (uint32) CMD_CLOCK_RATEADJ;

    // Send rate adjustment (clock information).
    platformClockChannel <: timestamp.secs;
    platformClockChannel <: timestamp.nsecs;
    platformClockChannel <: count;
    platformClockChannel <: newRateAdj;
    platformClockChannel <: newRateAdjInv;

}


//-- Scheduler.xc --//

/* Check all channels for request to add event, or free event when done
 * processing. Will keep processing requests until none are left, then
* returns. Will return RESTART or NO_RESTART for event queue search.
* Note: Can be temporarily blocked if other thread is requesting an
* operation on the scheduler thread.
*/
#pragma unsafe arrays
uint8 checkChannels(streaming chanend schedulerChannels[], uint8 numChan) {

    uint32 cmd;
    uint8 res = NO_RESTART;

    while(1) {
        #pragma ordered
        select {
        // Check all channels for command.
        case (int i = 0; i < numChan; i++) schedulerChannels[i] :> cmd:
            switch(cmd) {
            #pragma fallthrough
            case CMD_EVENT_ADD:
                processAddEvent(schedulerChannels[i]);
                // Event queue modified, so restart.
                res = RESTART;
                //TODO res |= ...
                continue; // Check again.
            #pragma fallthrough
            case CMD_EVENT_FREE:
                processFreeEvent(schedulerChannels[i]);
                // Currently processing event is changing, so restart.
                res = RESTART;
                continue; // Check again.
            }
            break;
        default:
            break;
        }
        // Can only get here from break in default, need to return.
        break;
    }

    return res;
}


//-- Sensors.xc --//


void runSensors(streaming chanend platformClockChannel, streaming chanend schedulerChannel) {

    Time timestamp;

    $sensorReadyFlags

    // If ready signal is TRUE for a sensor, an event will occur when the
    // pin goes high, and the sensor adds the event and sets the ready
    // signal to FALSE. Once the ready signal is FALSE, an event will occur
    // when the pin goes low again, where the ready signal is set to TRUE
    // again. This allows other sensors event to be handled while a senor
    // input is still high.
    $sensorSwitch



}


//-- Actuators.xc --//

/* Start operation of actuators. A timer will produce an event to the thread
 * at the time the earliest acutation event on the acutation event queue needs
 * to occur. This takes priority over events to the thread generated by a
 * channel connected to the event processing thread, which allows actors
 * running in the event processing thread to add an actuation event to the
 * actuation event queue.
 */
// TODO: If clock synchronization changes, the timer count will be incorrect.
// Currently output accuracy would be bounded by synchronization error.
// Could either update count when synchronization changes, or trigger sooner
// and block.
void runActuators(streaming chanend platformClockChannel,
        streaming chanend actuatorChannel) {

    uint32 type;
    timer time;
    uint32 actuationEventExists = FALSE;

    // Initialize outputs to zero.
    initializeActuators();

    // Initialize free actuation event list.
    initializeFreeActuationEventList();

    // Initialize chanend to platform clock channel (stored for C code)
    // TODO: could also pass as parameter.
    initializePlatformClockChanend(platformClockChannel);

    while(1) {
        #pragma ordered
        select {
        // Check if actuation needs to occur (only if actuation event exists).
        case actuationEventExists => time when
        timerafter(getNextActuationEventCount()):> void:
            actuationEventExists = processActuationEvent();
            break;
        // Check if actuation event needs to be added to actuation event queue.
        case actuatorChannel :> type:
            storeTestTime(13);
            processAddActuationEvent(actuatorChannel, type);
            actuationEventExists = TRUE;
            break;

        }
    }

}



// Actuation functions to create output pulse on GPIO pins.
// TODO: These functions will block any other actuations
// from occuring for the duration of the pulse. Use port
// timer to turn off pulse without software intervention.
$actuationFunctions


void initializeActuators() {
        $initActuators
}



/**/
