/* ---------------------------------------------------------------------------*
 * An FMU that increments its output, but also has an fmi2GetMaxStepSize() method.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// Define class name and unique id.
#define MODEL_IDENTIFIER HybridDelay
#define MODEL_GUID "{de1523d6-794e-4a03-be6c-3f95c5f4fef1}"

// Define model size.
#define NUMBER_OF_REALS 3
#define NUMBER_OF_INTEGERS 3
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 1
#define NUMBER_OF_EVENT_INDICATORS 1

// Include fmu header files, typedefs and macros.
#include "fmuTemplate.h"
#include <limits.h>
#include <stdbool.h>

// Define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define output_ 0
#define input_ 1
#define status_ 2
#define delay_ 0
#define resolution_ 1
#define microstep_ 2
#define STATES { output_ }

#define present_ 0
#define absent_ 1
#define unknown_ 2

typedef struct node {
    fmi2Real value;
    fmi2Integer time;
    struct node *next;
} Event;

Event *eventQueue;

void _addEvent(fmi2Real event, fmi2Integer t) {
    Event * current = eventQueue;
    while (current->next != NULL) {
        current = current->next;
    }
    current->next = malloc(sizeof(Event));
    current->value = event;
    current->time = t;
    current->next->next = NULL;
}

Event _getLast() {
    // Event event;
    // event.value = eventQueue->value;
    // event.time = eventQueue->time;
    // event.next = NULL;
    return *eventQueue;
}

void _removeLast() {
    Event *tmp = eventQueue->next;
    free(eventQueue);
    eventQueue = tmp;
    return;
}

fmi2Integer _getTime() {
    return eventQueue->time;
}

fmi2Boolean _isEmpty() {
    if (eventQueue->next == NULL) return fmi2True;
    else return fmi2False;
}

void deleteQueue() {
    Event * current = eventQueue;
    while (current->next != NULL) {
        Event *tmp = current;
        current = current->next;
        free(tmp);
    }
    free(current);
}

// Ccalled by fmi2Instantiate.
// Set values for all variables that define a start value.
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode.
void setStartValues(ModelInstance *comp) {
    r(output_) = 0;
    r(status_) = 0;
    i(microstep_) = 1;
    hr(output_) = absent_;
    hr(input_) = absent_;
    hr(delay_) = present_;
    hr(status_) = absent_;
    eventQueue = malloc(sizeof(Event));
    eventQueue->next = NULL;
}

bool _isTime(ModelInstance *comp) {
    if (_isEmpty()) {
        return false;
    }
    if (comp->time == _getLast().time){
        return true;
    } else {
        return false;
    }
}
// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        r(output_) = 0;
        hr(output_) = absent_;
        comp->eventInfo.nextEventTime = comp->time;
    } else if (_isTime(comp)){
        Event tmp = _getLast();
        r(output_) = tmp.value;
        hr(output_) = present_;
    } else {
        hr(output_) = absent_;
    }
}

// called by fmiGetReal, fmiGetContinuousStates and fmiGetDerivatives
fmi2Real getReal(ModelInstance* comp, fmi2ValueReference vr){
    switch (vr)
    {
        case output_:
            return r(output_);
        case input_:
            return r(input_);
        case status_:
            return r(status_);
        default:
            return 0;
    }
}

fmi2Real getEventIndicator(ModelInstance* comp, int z) {
    return (hr(input_) == present_) ? -1 : 1;
}

// Used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmi2EventInfo* eventInfo, int isTimeEvent) {
    long currentTime = comp->time;
    if (_isTime(comp)) {
        _removeLast();
        i(microstep_) = 1;
        eventInfo->nextEventTimeDefined  = fmi2False;
    }
    if (hr(input_) != absent_) {
        _addEvent(r(input_), currentTime + i(delay_));
        eventInfo->nextEventTimeDefined  = fmi2True;
    }
    if (!_isEmpty()) {
        Event nextEvent = _getLast();
        comp->eventInfo.nextEventTime = nextEvent.time;
        eventInfo->nextEventTimeDefined  = fmi2True;
    }
}

/***************************************************
Functions for FMI2 for Hybrid Co-Simulation
****************************************************/

fmi2Status fmi2RequiredTimeResolution (fmi2Component c, fmi2Integer *value) {
    ModelInstance *comp = (ModelInstance *)c;
    *value = i(resolution_);
    return fmi2OK;
}

fmi2Status fmi2SetTimeResolution (fmi2Component c, fmi2Integer value) {
    return fmi2OK;
}

fmi2Status fmi2GetMaxStepSize (fmi2Component c, fmi2Real *value) {
    return fmi2OK;
}

fmi2Status fmi2HybridGetMaxStepSize (fmi2Component c, fmi2Integer *value) {
    ModelInstance *comp = (ModelInstance *)c;
    fmi2Integer max_step_size;

    if (comp->eventInfo.nextEventTimeDefined) {
        max_step_size = comp->eventInfo.nextEventTime - comp->time;
    }
    else {
        max_step_size = 2;
    }

    *value = max_step_size;
    return fmi2OK;
}


/* END */

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"
