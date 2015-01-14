/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU - increments an int counter every second.
 * The Ptolemy extension here is that This version has a fmi2GetMaxStepSize() that
 * sets the maximum step size to 1.
 * Based on FMUSDK inc FMU, Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER scale20pt
#define MODEL_GUID "{8e15847a-0268-49a1-9b80-0df510534d61}"

// define model size
#define NUMBER_OF_REALS 3
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 1
#define NUMBER_OF_EVENT_INDICATORS 0

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"
#include "fmi2FunctionTypes.h"

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
    fprintf(stderr, "scale20pt.c: setStartValues()\n");
    fflush(stderr);
    r(output_) = 0;
    r(input_) = 0;
}

// called by fmiExitInitializationMode() after setting eventInfo to defaults
// Used to set the first time event, if any.
void initialize(ModelInstance* comp, fmi2EventInfo* eventInfo) {
    fprintf(stderr, "scale20pt.c: initialize()\n");
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
    switch (vr) {
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

// We are adding fmi2GetMaxStepSize as a global, so we need to set up the exports.

// Lines like this appear in fmi2FunctionTypes.h for other fmi2* functions.
typedef fmi2Status fmi2GetMaxStepSizeTYPE                  (fmi2Component, fmi2Real *);

// Lines like thes appear in fmi2Functions.h for other fmi2* functions.
#define fmi2GetMaxStepSize fmi2FullName(fmi2GetMaxStepSize)
FMI2_Export fmi2GetMaxStepSizeTYPE fmi2GetMaxStepSize;

// FMI function for getting max step size as proposed in the EMSOFT Paper of 2013.
fmi2Status fmi2GetMaxStepSize (fmi2Component c, fmi2Real *maxStepSize) {
   *maxStepSize = 1;
    return fmi2OK;
}
// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

