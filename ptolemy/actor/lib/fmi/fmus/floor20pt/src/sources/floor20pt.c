/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU - increments an int counter every second.
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER floor20pt
#define MODEL_GUID "{f27c6078-5f9a-44ac-b93f-5d2b962e3867}"

// define model size
#define NUMBER_OF_REALS 1
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 1
#define NUMBER_OF_EVENT_INDICATORS 0

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"
#include "fmi2FunctionTypes.h"
#include <stdio.h>
#include <errno.h>
#include <stdbool.h>

// define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define output_ 0

// define state vector as vector of value references
#define STATES { output_ }

#define RESULT_FILE "floor20pt.csv"

// called by fmi2Instantiate
// Set values for all variables that define a start value
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode
void setStartValues(ModelInstance *comp) {
          fprintf(stderr, "floor20pt.c: setStartValues()\n");
          fflush(stderr);
    r(output_) = 0;
}


// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {

        fprintf(stderr, "floor20pt.c: initialize()\n");
        fflush(stderr);
        
        comp->eventInfo.nextEventTimeDefined   = fmi2False;

        comp->eventInfo.nextEventTimeDefined   = fmi2True;
        comp->eventInfo.nextEventTime          = 1 + comp->time;

        fprintf(stderr, "floor20pt.c: initialized\n");
        fflush(stderr);
    }
 }

// used to set the next time event, if any.
void eventUpdate(ModelInstance *comp, fmi2EventInfo *eventInfo, int isTimeEvent) {
    r(output_) += comp->time - r(output_);
    if (isTimeEvent){
        eventInfo->nextEventTimeDefined   = fmi2True;
        eventInfo->nextEventTime          = 1 + comp->time;
    } 
}

// called by fmi2GetReal, fmi2GetContinuousStates and fmi2GetDerivatives
fmi2Real getReal(ModelInstance* comp, fmi2ValueReference vr){
    switch (vr)
    {
        case output_:
        {
            // Return the value of the state variable
            return (int)r(output_);
        }
        default: return 0;
    }
}

// FMI function for getting max step size as proposed in the EMSOFT Paper of 2013
fmi2Status fmiGetMaxStepSize (fmi2Component c, fmi2Real *maxStepSize) {
    *maxStepSize = 1;
    return fmi2OK;
}

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

