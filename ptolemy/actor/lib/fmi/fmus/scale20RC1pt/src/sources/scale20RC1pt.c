/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU - increments an int counter every second.
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER scale20RC1pt
#define MODEL_GUID "{48a4e32e-2c4c-41b4-b7ef-29b272fb4e02}"

// define model size
#define NUMBER_OF_REALS 3
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 1
#define NUMBER_OF_EVENT_INDICATORS 0

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"
#include "fmiFunctionTypes.h"

// define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define input_ 0
#define output_ 1
#define output_derivative_ 2

// define state vector as vector of value references
#define STATES { output_ }

// called by fmiInstantiate
// Set values for all variables that define a start value
// Settings used unless changed by fmiSetX before fmiEnterInitializationMode
void setStartValues(ModelInstance *comp) {
          fprintf(stderr, "scale20RC1pt.c: setStartValues()\n");
          fflush(stderr);
    r(output_) = 44;
    r(input_) = 0;
}

// called by fmiExitInitializationMode() after setting eventInfo to defaults
// Used to set the first time event, if any.
void initialize(ModelInstance* comp, fmiEventInfo* eventInfo) {
          fprintf(stderr, "scale20RC1pt.c: initialize()\n");
          fflush(stderr);
        // Calcualation is not event based, so no event time will be defined
    eventInfo->nextEventTimeDefined   = fmiFalse;
}

// used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmiEventInfo* eventInfo) {
}

// called by fmiGetReal, fmiGetContinuousStates and fmiGetDerivatives
fmiReal getReal(ModelInstance* comp, fmiValueReference vr){
    switch (vr)
    {
                case input_:
                        // return the input value
                        return r(input_);
            case output_:
                    // Calculate output when output is requested
                    r(output_) = 2*r(input_);
                    // Log call to facilitate debugging
                // comp->functions->logger(comp->componentEnvironment, comp->instanceName, fmiOK, "logFmiCall", "input: %F, output: %F", r(input_), r(output_));
                    return r(output_);
        default: return 0;
    }
}

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

