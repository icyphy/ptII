/* ---------------------------------------------------------------------------*
 * An FMU that increments its output, but also has an fmi2GetMaxStepSize() method.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// Define class name and unique id.
#define MODEL_IDENTIFIER HybridSequenceCheck
#define MODEL_GUID "{187457f1-90b3-4a50-af06-d4d7c7897050}"

// Define model size.
#define NUMBER_OF_REALS 1
#define NUMBER_OF_INTEGERS 2
#define NUMBER_OF_BOOLEANS 1
#define NUMBER_OF_STRINGS 1
#define NUMBER_OF_STATES 1
#define NUMBER_OF_EVENT_INDICATORS 0

// Include fmu header files, typedefs and macros.
#include "fmuTemplate.h"
#include <limits.h>
#include <string.h>
#include <stdlib.h>

// Define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define output_ 0
#define input_ 0
#define sequence_ 0
#define resolution_ 0
#define i_ 1

#define present_ 0
#define absent_ 1
#define unknown_ 2

#define STATES { output_ }

 typedef struct node {
    fmi2Real val;
    fmi2Integer t;
    struct node * next;
} node_t;

node_t *eventQueue;

void addEvent(fmi2Real event, fmi2Integer t) {
    node_t * current = eventQueue;
    while (current->next != NULL) {
        current = current->next;
    }
    current->next = malloc(sizeof(node_t));
    current->val = event;
    current->t = t;
    current->next->next = NULL;
    current->next->val = 0;
    current->next->t = 0;
}

fmi2Real getEvent() {
    fmi2Real event = eventQueue->val;
    node_t *tmp = eventQueue->next;
    free(eventQueue);
    eventQueue = tmp;
    return event;
}

fmi2Integer getTime() {
    return eventQueue->t;
}

fmi2Integer getNextTime() {
    if (eventQueue->next != NULL)
        return eventQueue->next->t;
    return eventQueue->t;
}

fmi2Real getNextValue() {
    if (eventQueue->next != NULL)
        return eventQueue->next->val;
    return eventQueue->val;
}

fmi2Integer getValue() {
    return eventQueue->val;
}

fmi2Boolean isEmpty() {
    if (eventQueue->next == NULL) return fmi2True;
    else return fmi2False;
}

void deleteQueue() {
    node_t * current = eventQueue;
    while (current->next != NULL) {
        node_t *tmp = current;
        current = current->next;
        free(tmp);
    }
    free(current);
}

void parseSequence(ModelInstance *comp) {
    fmi2String sequence = s(sequence_);
    int i = 0;
    if (sequence == NULL) {
        // This could happen if we are running fmusdk2 on this fmu.
        fprintf(stderr, "HybridSequenceCheck parseSequence(): sequence was null?\n");
        return;
    }
    char * pEnd;
    if (sizeof(*sequence) > 0) { 
        long t = strtol(sequence, &pEnd, 10);
        double v = strtod(pEnd, &pEnd);
        addEvent(v, t);
    }
    for (i = 1; i < strlen(s(sequence_)); i++) {
        long t = strtol(pEnd, &pEnd, 10);
        double v = strtod(pEnd, &pEnd);
        addEvent(v, t);
    }
}

// Ccalled by fmi2Instantiate.
// Set values for all variables that define a start value.
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode.
void setStartValues(ModelInstance *comp) {
    b(output_) = fmi2False;
    r(input_) = 0.0;
    hb(output_) = present_;
    hr(input_) = absent_;
    i(i_) = 0;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        eventQueue = malloc(sizeof(node_t));
        eventQueue->next = NULL;
        parseSequence(comp);
    }
    else {
        if (!isEmpty()) {
            double epsilon = 0.00000000001;
            long t = getTime();
            double v = getValue();
            while (!isEmpty() && comp->time > t) {
                getEvent();
                t = getTime();
                v = getValue();
                i(i_) = 0;
            }
            if ( comp->time < t )
                return;
            if (comp->time == t && i(i_) == 0) {
                if ( v < (r(input_) + epsilon) &&
                     v > (r(input_) - epsilon) ) {
                    b(output_) = fmi2True;
                    i(i_) = 1;
                }
                else {
                    b(output_) = fmi2False;
                    i(i_) = 1;
                }
            }
            else if (comp->time == t && i(i_) == 1) {
                if ( v < (r(input_) + epsilon) &&
                     v > (r(input_) - epsilon) ) {
                    b(output_) = fmi2True;
                }
                else {
                    if (getTime() == getNextTime()) {
                        getEvent();
                        t = getTime();
                        v = getValue();
                        if ( v < (r(input_) + epsilon) && v > (r(input_) - epsilon) ) {
                            b(output_) = fmi2True;
                        }
                        else {
                            b(output_) = fmi2False;
                        }
                    }
                }
            }
        }
    }
}

// called by fmiGetReal, fmiGetContinuousStates and fmiGetDerivatives
fmi2Real getReal(ModelInstance* comp, fmi2ValueReference vr){
    switch (vr)
    {
        case input_:
            return r(input_);
        default:
            return 0;
    }
}

// Used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmi2EventInfo* eventInfo, int timeEvent) {

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
    *value = LONG_MAX;
    return fmi2OK;
}


/* END */

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

