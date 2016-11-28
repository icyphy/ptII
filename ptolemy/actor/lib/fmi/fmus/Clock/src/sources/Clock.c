/* ---------------------------------------------------------------------------*
 * An FMU that increments its output, but also has an fmi2GetMaxStepSize() method.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// Define class name and unique id.
#define MODEL_IDENTIFIER Clock
#define MODEL_GUID "{bbd37e9a-7433-4dda-8b6c-84c32acb8bba}"

// Define model size.
#define NUMBER_OF_REALS 2
#define NUMBER_OF_INTEGERS 2
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 1
#define NUMBER_OF_EVENT_INDICATORS 0

// Include fmu header files, typedefs and macros.
#include "fmuTemplate.h"

// Define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define output_ 0
#define value_ 1
#define period_ 0
#define resolution_ 1
#define STATES { output_ }

#define present_ 0
#define absent_ 1
#define unknown_ 2

fmi2Boolean _isTime(ModelInstance *comp) {
    fmi2Integer t = comp->time;
    fmi2Integer k = t / i(period_);
    fmi2Integer r = t % i(period_);
    if ((t == k * i(period_)) && (r == 0) && (comp->microstep == 1)) {
        return fmi2True;
    } else {
        return fmi2False;
    }
}
// Ccalled by fmi2Instantiate.
// Set values for all variables that define a start value.
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode.
void setStartValues(ModelInstance *comp) {
    r(output_)      = r(value_);
    hr(output_)     = present_;

    comp->eventInfo.nextEventTimeDefined   = fmi2True;
    comp->eventInfo.nextEventTime          = comp->time;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        comp->eventInfo.nextEventTimeDefined   = fmi2True;
        comp->eventInfo.nextEventTime          = comp->time;
        comp->time                             = 0;
        comp->microstep                        = 0;
    }
    else {
        if ( _isTime(comp) ) {
            r(output_) = r(value_);
            hr(output_) = present_;
        }
        else {
            hr(output_) = absent_;
        }
    }
}

// called by fmiGetReal, fmiGetContinuousStates and fmiGetDerivatives
fmi2Real getReal(ModelInstance* comp, fmi2ValueReference vr){
    switch (vr)
    {
        case output_:
            return r(output_);
        case value_:
            return r(value_);
        default:
            return 0;
    }
}

// Used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmi2EventInfo* eventInfo, int timeEvent) {
    if ( comp->microstep == 0 ) {
        eventInfo->nextEventTimeDefined   = fmi2True;
        eventInfo->nextEventTime          = comp->time;
    } else if ( comp->microstep == 1 ) {
        eventInfo->nextEventTimeDefined   = fmi2True;
        eventInfo->nextEventTime          = comp->time + i(period_);
    } else {

    }
}

/***************************************************
Functions for FMI2 for Hybrid Co-Simulation
****************************************************/

fmi2Status fmi2RequiredTimeResolution (fmi2Component c, fmi2Integer *value) {
    ModelInstance *comp = (ModelInstance *)c;
    *value = i(resolution_);
    return fmi2OK;
}

fmi2Status fmi2SetTimeResolution (fmi2Component c, fmi2Integer value) {
    return fmi2OK;
}

fmi2Status fmi2GetMaxStepSize (fmi2Component c, fmi2Real *value) {
    return fmi2OK;
}

fmi2Integer computeStepSize(ModelInstance *comp) {
    fmi2Integer communicationStepSize;
    communicationStepSize = comp->eventInfo.nextEventTime - comp->time;
    return communicationStepSize;
}


/* END */

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"
