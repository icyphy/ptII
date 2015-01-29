/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU - Writes all inputs to an output file (results.csv).
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER out20pt
#define MODEL_GUID "{aeba48d6-5f7e-46ae-b275-635d7704a125}"

// define model size
#define NUMBER_OF_REALS 1
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 1
#define NUMBER_OF_EVENT_INDICATORS 0

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"
#include <stdio.h>
#include <errno.h>
#include <stdbool.h>

// define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define input_ 0

// define state vector as vector of value references
#define STATES { input_ }

// called by fmi2Instantiate
// Set values for all variables that define a start value
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode
void setStartValues(ModelInstance *comp) {
    fprintf(stderr, "out20pt.c: setStartValues()\n");
    fflush(stderr);
    r(input_) = 0;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {

        comp->eventInfo.nextEventTimeDefined   = fmi2True;
        comp->eventInfo.nextEventTime          = 1 + comp->time;

        fprintf(stderr, "out20pt.c: initialized\n");
        fflush(stderr);
    }
}

// used to set the next time event, if any.
void eventUpdate(ModelInstance *comp, fmi2EventInfo *eventInfo, int isTimeEvent) {
    eventInfo->nextEventTimeDefined   = fmi2True;
    eventInfo->nextEventTime          = 1 + comp->time;
}

// called by fmi2GetReal, fmi2GetContinuousStates and fmi2GetDerivatives
fmi2Real getReal(ModelInstance* comp, fmi2ValueReference vr){
    switch (vr)
    {
        case input_:
            // return the input value
            return r(input_);
        default: return 0;
    }
}

fmi2Status fmi2GetMaxStepSize (fmi2Component c, fmi2Real *value) {
    *value = 1;
    return fmi2OK;
}

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

