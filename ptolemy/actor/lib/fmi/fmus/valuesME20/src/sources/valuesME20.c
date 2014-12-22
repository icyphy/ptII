/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU.
 * This demonstrates the use of all FMU variable types.
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER valuesME20
#define MODEL_GUID "{8c4e810f-3df3-4a00-8276-176fa3c9f034}"

// define model size
#define NUMBER_OF_REALS 2
#define NUMBER_OF_INTEGERS 2
#define NUMBER_OF_BOOLEANS 2
#define NUMBER_OF_STRINGS 2
#define NUMBER_OF_STATES 1
#define NUMBER_OF_EVENT_INDICATORS 0

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"

// define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define x_          0
#define der_x_      1
#define int_in_     0
#define int_out_    1
#define bool_in_    0
#define bool_out_   1
#define string_in_  0
#define string_out_ 1

// define state vector as vector of value references
#define STATES { x_ }

const char *month[] = {
    "jan","feb","march","april","may","june","july",
    "august","sept","october","november","december"
};

// called by fmi2Instantiate
// Set valuesME20 for all variables that define a start value
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode
void setStartValues(ModelInstance *comp) {
    r(x_) = 1;
    i(int_in_) = 2;
    i(int_out_) = 0;
    b(bool_in_) = fmi2True;
    b(bool_out_) = fmi2False;
    copy(string_in_, "QTronic");
    copy(string_out_, month[0]);
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new valuesME20 through fmi2SetXXX.
// Lazy set valuesME20 for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        // set first time event
        comp->eventInfo.nextEventTimeDefined = fmi2True;
        comp->eventInfo.nextEventTime        = 1 + comp->time;
    }
}

// called by fmi2GetReal, fmi2GetContinuousStates and fmi2GetDerivatives
fmi2Real getReal(ModelInstance *comp, fmi2ValueReference vr){
    switch (vr) {
        case x_     : return   r(x_);
        case der_x_ : return - r(x_);
        default: return 0;
    }
}

// used to set the next time event, if any.
void eventUpdate(ModelInstance *comp, fmi2EventInfo *eventInfo, int isTimeEvent) {
    if (isTimeEvent) {
        eventInfo->nextEventTimeDefined = fmi2True;
        eventInfo->nextEventTime        = 1 + comp->time;
        i(int_out_) += 1;
        b(bool_out_) = !b(bool_out_);
        if (i(int_out_) < 12) copy(string_out_, month[i(int_out_)]);
        else eventInfo->terminateSimulation = fmi2True;
    }
}

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"
