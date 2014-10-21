/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU - increments an int counter every second.
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER inc20RC1pt
#define MODEL_GUID "{8c4e810f-3df3-4a00-8276-176fb3c9f009}"

// define model size
#define NUMBER_OF_REALS 0
#define NUMBER_OF_INTEGERS 1
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 0
#define NUMBER_OF_EVENT_INDICATORS 0

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"

// define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define counter_ 0

// called by fmiInstantiate
// Set values for all variables that define a start value
// Settings used unless changed by fmiSetX before fmiEnterInitializationMode
void setStartValues(ModelInstance *comp) {
  fprintf(stderr, "inc20RC1pt.c: setStartValues()\n");
  fflush(stderr);
    i(counter_) = 1;
}

// called by fmiExitInitializationMode() after setting eventInfo to defaults
// Used to set the first time event, if any.
void initialize(ModelInstance* comp, fmiEventInfo* eventInfo) {
  fprintf(stderr, "inc20RC1pt.c: initialize()\n");
  fflush(stderr);
    eventInfo->nextEventTimeDefined   = fmiTrue;
    eventInfo->nextEventTime          = 1 + comp->time;
  fprintf(stderr, "inc20RC1pt.c: initialized\n");
  fflush(stderr);
}

// used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmiEventInfo* eventInfo) {
    int floorTime = (int)comp->time;
    if (i(counter_) >= 13 && comp->communicationStepSize >= 0.5) {
        eventInfo->terminateSimulation = fmiTrue;
    }
    if (comp->time - floorTime <= 0.000000001) {
        i(counter_) += 1;
    }
    else {
        eventInfo->nextEventTimeDefined   = fmiTrue;
        eventInfo->nextEventTime          = 1 + comp->time;
    }
}

// FMI function for getting max step size as proposed in the EMSOFT Paper of 2013
fmiStatus fmiGetMaxStepSize (fmiComponent c, fmiReal *maxStepSize) {
    ModelInstance *comp = (ModelInstance*)c;
    if (i(counter_)<13) {
        *maxStepSize = 1;
    } else {
        *maxStepSize = 0.5;
    }
    return fmiOK;
}

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

