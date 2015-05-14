/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU -

 * Based on bouncingBall.c from FMUSDK2.0
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER fourInputsFourOutputsME20
#define MODEL_GUID "{5d012dd2-3205-4c98-8b96-671350b2d73c}"

// define model size
#define NUMBER_OF_REALS 8
#define NUMBER_OF_INTEGERS 0
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
#define u_      0
#define u2_     1
#define u3_     2
#define u4_     3
#define y_      4
#define y1_     5
#define y2_     6
#define y3_     7

// define initial state vector as vector of value references
#define STATES {}

// called by fmi2Instantiate
// Set values for all variables that define a start value
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode
void setStartValues(ModelInstance *comp) {
    r(u_)     =  0.0;
    r(u2_)     =  0.0;
    r(u3_)     =  0.0;
    r(u4_)     =  0.0;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        r(y_) = r(u2_);
        r(y1_) = r(u3_);
        r(y2_) = r(u4_);
        r(y3_) = r(u_);
        // set first time event, if any, using comp->eventInfo.nextEventTime
    }
}

// called by fmi2GetReal, fmi2GetContinuousStates and fmi2GetDerivatives
fmi2Real getReal(ModelInstance* comp, fmi2ValueReference vr) {
    switch (vr) {
        case u_     : return r(u_);
        case u2_     : return r(u2_);
        case u3_     : return r(u3_);
        case u4_     : return r(u4_);
        case y_     : return r(u2_);
        case y1_     : return r(u3_);
        case y2_     : return r(u4_);
        case y3_     : return r(u_);
        default: return 0;
    }
}

// offset for event indicator, adds hysteresis and prevents z=0 at restart
#define EPS_INDICATORS 1e-14

fmi2Real getEventIndicator(ModelInstance* comp, int z) {
  return 0;
}

// used to set the next time event, if any.
void eventUpdate(ModelInstance *comp, fmi2EventInfo *eventInfo, int isTimeEvent) {
    /* pos(0) = r(h_) > 0; */
    /* if (!pos(0)) { */
    /*     r(v_) = - r(e_) * r(v_); */
    /* } */
    /* eventInfo->valuesOfContinuousStatesChanged   = fmi2True; */
    /* eventInfo->nominalsOfContinuousStatesChanged = fmi2False; */
    /* eventInfo->terminateSimulation   = fmi2False; */
    /* eventInfo->nextEventTimeDefined  = fmi2False; */
}

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"
