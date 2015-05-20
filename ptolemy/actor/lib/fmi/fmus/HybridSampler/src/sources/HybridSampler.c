/* ---------------------------------------------------------------------------*
 * An FMU that increments its output, but also has an fmi2GetMaxStepSize() method.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// Define class name and unique id.
#define MODEL_IDENTIFIER HybridSampler
#define MODEL_GUID "{55af5e3e-eb24-4b17-bbc9-cc4c37455d93}"

// Define model size.
#define NUMBER_OF_REALS 3
#define NUMBER_OF_INTEGERS 1
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 1
#define NUMBER_OF_EVENT_INDICATORS 1

// Include fmu header files, typedefs and macros.
#include "fmuTemplate.h"
#include <limits.h>

// Define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define output_ 0
#define input_ 1
#define sampler_ 2 
#define resolution_ 0
#define STATES { output_ }

#define present_ 0
#define absent_ 1
#define unknown_ 2

// Ccalled by fmi2Instantiate.
// Set values for all variables that define a start value.
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode.
void setStartValues(ModelInstance *comp) {
    r(output_) = 0;
    hr(output_) = absent_;
    hr(input_) = absent_;
    hr(sampler_) = absent_;
    pos(0) = fmi2False;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        if (hr(sampler_) == present_) {
            r(output_) = r(input_);
            hr(output_) = hr(input_);
        }
    }
    else {
        if (hr(sampler_) == present_ && pos(0) == fmi2False) {
            r(output_) = r(input_);
            hr(output_) = hr(input_);
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
        case input_:
            return r(input_);
        case sampler_:
            return r(sampler_);
        default:
            return 0;
    }
}

fmi2Real getEventIndicator(ModelInstance* comp, int z) {
    if (hr(sampler_) == present_ && pos(0) == fmi2False) {
        pos(0) = fmi2True;
        return -1;
    }
    else {
        pos(0) = fmi2False;
    }
    return 0;
}

// Used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmi2EventInfo* eventInfo, int timeEvent, long h) {
    
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

fmi2Status fmi2HybridGetMaxStepSize (fmi2Component c, fmi2Integer *value) {
    ModelInstance *comp = (ModelInstance *)c;
    fmi2Integer max_step_size;
    if (pos(0) == fmi2True)
        max_step_size = 0;
    else
        max_step_size = LONG_MAX;
    *value = max_step_size;
    return fmi2OK;
}


/* END */

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

