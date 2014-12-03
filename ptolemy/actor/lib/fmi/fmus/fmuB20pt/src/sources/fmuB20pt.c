/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU - increments an int counter every second.
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER fmuB20pt
#define MODEL_GUID "{a4132926-448e-46d2-8afd-1510959f1f90}"

// define model size
#define NUMBER_OF_REALS 4
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 4
#define NUMBER_OF_EVENT_INDICATORS 0

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"
#include "fmi2FunctionTypes.h"

// define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define input1_ 0
#define input2_ 1
#define output1_ 2
#define output2_ 3

// define state vector as vector of value references
#define STATES { }

// called by fmiInstantiate
// Set values for all variables that define a start value
// Settings used unless changed by fmiSetX before fmiEnterInitializationMode
void setStartValues(ModelInstance *comp) {
          fprintf(stderr, "fmuB20pt.c: setStartValues()\n");
          fflush(stderr);
    r(output1_) = 0;
    r(input1_) = 0;
    r(output2_) = 0;
    r(input2_) = 0;
}

// called by fmiExitInitializationMode() after setting eventInfo to defaults
// Used to set the first time event, if any.
void initialize(ModelInstance* comp, fmi2EventInfo* eventInfo) {
          fprintf(stderr, "fmuB20pt.c: initialize()\n");
          fflush(stderr);
        // Calcualation is not event based, so no event time will be defined
    eventInfo->nextEventTimeDefined   = fmi2False;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        comp->eventInfo.nextEventTimeDefined   = fmi2False;
    }
}

// used to set the next time event, if any.
void eventUpdate(ModelInstance *comp, fmi2EventInfo *eventInfo, int isTimeEvent) {

}

// called by fmiGetReal, fmiGetContinuousStates and fmiGetDerivatives
fmi2Real getReal(ModelInstance* comp, fmi2ValueReference vr){
    switch (vr)
    {
            case output1_:
                    // Calculate output when output is requested
                    r(output1_) = 2*r(input2_);
                    // Log call to facilitate debugging
                    return r(output1_);
            case output2_:
                    // Calculate output when output is requested
                    r(output2_) = 3*r(input1_);
                    // Log call to facilitate debugging
                    return r(output2_);
        default: return 0;
    }
}

fmi2Status fmi2GetMaxStepSize (fmi2Component c, fmi2Real *value) {
}

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

