/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU - increments an int counter every second.
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER inc20RC1pt
#define MODEL_GUID "{8c4e810f-3df3-4a00-8276-176fa3c9f00b}"

// define model size
#define NUMBER_OF_REALS 0
#define NUMBER_OF_INTEGERS 1
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 0
#define NUMBER_OF_EVENT_INDICATORS 0

// Used by FMI 2.0.  See FMIFuctions.h
#define FMI_FUNCTION_PREFIX inc20RC1pt_

// We require that functions have prefixes for Linux.
#if defined _WIN32 || defined __CYGWIN__
/* Note: both gcc & MSVC on Windows support this syntax. */
#define FMI_Export __declspec(dllexport)
#else
  #if __GNUC__ >= 4
#define FMI_Export __attribute__ ((visibility ("default")))
  #else
    #define FMI_Export
  #endif
#endif // _WIN32 || defined __CYGWIN__

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

// Ptolemy specific define:
#define setStartValues fmiFullName(setStartValues)
void setStartValues(ModelInstance *comp) {
    i(counter_) = 1;
}

// called by fmiExitInitializationMode() after setting eventInfo to defaults
// Used to set the first time event, if any.

// Ptolemy specific define:
#define initialize fmiFullName(initialize)
void initialize(ModelInstance* comp, fmiEventInfo* eventInfo) {
    eventInfo->nextEventTimeDefined   = fmiTrue;
    eventInfo->nextEventTime          = 1 + comp->time;
}

// used to set the next time event, if any.

// Ptolemy specific define:
#define eventUpdate fmiFullName(eventUpdate)
void eventUpdate(ModelInstance* comp, fmiEventInfo* eventInfo) {
    i(counter_) += 1;
    if (i(counter_) == 13) 
        eventInfo->terminateSimulation = fmiTrue;
    else {
        eventInfo->nextEventTimeDefined   = fmiTrue;
        eventInfo->nextEventTime          = 1 + comp->time;
    }
}

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

