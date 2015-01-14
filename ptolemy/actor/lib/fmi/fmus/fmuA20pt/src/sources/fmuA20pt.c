/* ---------------------------------------------------------------------------*
 * An FMU that increments its output, but also has an fmi2GetMaxStepSize() method.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// Define class name and unique id.
#define MODEL_IDENTIFIER fmuA20pt
#define MODEL_GUID "{9e2d9cd2-b4b3-47e0-a827-56380d65d638}"

// Define model size.
#define NUMBER_OF_REALS 0
#define NUMBER_OF_INTEGERS 1
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 0
#define NUMBER_OF_EVENT_INDICATORS 0

// Include fmu header files, typedefs and macros.
#include "fmuTemplate.h"

// Define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define counter_ 0

// Ccalled by fmi2Instantiate.
// Set values for all variables that define a start value.
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode.
void setStartValues(ModelInstance *comp) {
    if (comp->loggingOn) {
        comp->functions->logger(comp, comp->instanceName, fmi2OK, "message",
                "setStartValues()");
    }
    i(counter_) = 1;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        if (comp->loggingOn) {
            comp->functions->logger(comp, comp->instanceName, fmi2OK, "message",
                    "initialize()");
        }
        comp->eventInfo.nextEventTimeDefined   = fmi2True;
        comp->eventInfo.nextEventTime          = 1 + comp->time;
        if (comp->loggingOn) {
            comp->functions->logger(comp, comp->instanceName, fmi2OK, "message",
                    "initialized");
        }
    }
}

// Used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmi2EventInfo* eventInfo, int timeEvent) {
    if (comp->loggingOn) {
        comp->functions->logger(comp, comp->instanceName, fmi2OK, "message",
                "eventUpdate()");
    }
    if (timeEvent) {
        i(counter_) += 1;
        if (i(counter_) == 13) {
            eventInfo->terminateSimulation  = fmi2True;
        } else {
            eventInfo->nextEventTimeDefined   = fmi2True;
            eventInfo->nextEventTime          = 1 + comp->time;
        }
    } 
}

// We are adding fmi2GetMaxStepSize as a global, so we need to set up the exports.

// Lines like this appear in fmi2FunctionTypes.h for other fmi2* functions.
typedef fmi2Status fmi2GetMaxStepSizeTYPE                  (fmi2Component, fmi2Real *);

// Lines like thes appear in fmi2Functions.h for other fmi2* functions.
#define fmi2GetMaxStepSize fmi2FullName(fmi2GetMaxStepSize)
FMI2_Export fmi2GetMaxStepSizeTYPE fmi2GetMaxStepSize;

// FMI function for getting max step size as proposed in the EMSOFT Paper of 2013
fmi2Status fmi2GetMaxStepSize (fmi2Component c, fmi2Real *value) {
    *value = 1.0;
    return fmi2OK;
}

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

