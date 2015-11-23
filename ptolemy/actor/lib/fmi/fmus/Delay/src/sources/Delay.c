/* ---------------------------------------------------------------------------*
 * An FMU that increments its output, but also has an fmi2GetMaxStepSize() method.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// Define class name and unique id.
#define MODEL_IDENTIFIER Delay
#define MODEL_GUID "{99cf6785-472d-470d-a3e1-3be18ef78d8e}"

// Define model size.
#define NUMBER_OF_REALS 3
#define NUMBER_OF_INTEGERS 2
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
#define STATES { output_ }

#define present_ 0
#define absent_ 1
#define unknown_ 2


void _addEvent(ModelInstance *comp, fmi2Real event, fmi2Integer t) {
    Event * current = comp->eventQueue;
    while (current->next != NULL) {
        current = current->next;
    }
    current->next = malloc(sizeof(Event));
    current->value = event;
    current->time = t;
    if (comp->time ==  t) {
        current->index = comp->microstep + 1;
    } else {
        current->index = comp->microstep;
    }
    current->next->next = NULL;
}

Event _getLast(ModelInstance *comp) {
    return *comp->eventQueue;
}

void _removeLast(ModelInstance *comp) {
    Event *tmp = comp->eventQueue->next;
    free(comp->eventQueue);
    comp->eventQueue = tmp;
    return;
}

fmi2Integer _getTime(ModelInstance *comp) {
    return comp->eventQueue->time;
}

fmi2Integer _getIndex(ModelInstance *comp) {
    return comp->eventQueue->index;
}

fmi2Boolean _isEmpty(ModelInstance *comp) {
    if (comp->eventQueue->next == NULL) {
        return fmi2True;
    }
    else {
        return fmi2False;
    }
}

void deleteQueue(ModelInstance *comp) {
    Event * current = comp->eventQueue;
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
    comp->microstep = 0;
    hr(output_) = absent_;
    hr(input_) = absent_;
    hr(delay_) = present_;
    hr(status_) = absent_;
    comp->eventQueue = malloc(sizeof(Event));
    comp->eventQueue->next = NULL;
}

bool _isTime(ModelInstance *comp) {
    if (_isEmpty(comp)) {
        return false;
    }
    if (comp->time == _getTime(comp) && comp->microstep == _getIndex(comp)){
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
    } else if (_isTime(comp)){
        Event tmp = _getLast(comp);
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

fmi2Real getEventIndicator(ModelInstance* comp) {
    return (hr(input_) == present_) ? -1 : 1;
}

// Used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmi2EventInfo* eventInfo, int isTimeEvent) {
    long currentTime = comp->time;
    if (_isTime(comp)) {
        _removeLast(comp);
        eventInfo->nextEventTimeDefined  = fmi2False;
    }
    if (!_isEmpty(comp)) {
        Event nextEvent = _getLast(comp);
        comp->eventInfo.nextEventTime = nextEvent.time;
        eventInfo->nextEventTimeDefined  = fmi2True;
    }
    if (hr(input_) == present_) {
        _addEvent(comp, r(input_), currentTime + i(delay_));
        eventInfo->nextEventTimeDefined  = fmi2True;
        comp->eventInfo.nextEventTime = _getTime(comp);
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


fmi2Integer computeStepSize(ModelInstance *comp) {
    fmi2Integer communicationStepSize;

    if (comp->eventInfo.nextEventTimeDefined) {
        communicationStepSize = comp->eventInfo.nextEventTime - comp->time;
    } else if (hr(input_) == present_ && i(delay_) == 0) {
        communicationStepSize = 0;
    } else if (hr(input_) == present_ && i(delay_) < 2) {
        communicationStepSize = i(delay_);
    } else {
        communicationStepSize = 2;
    }

    return communicationStepSize;
}

/* END */

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"
