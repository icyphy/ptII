/* ---------------------------------------------------------------------------*
 * An FMU that increments its output, but also has an fmi2GetMaxStepSize() method.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// Define class name and unique id.
#define MODEL_IDENTIFIER HybridZeroCrossingDetector
#define MODEL_GUID "{dc8ac9fb-977b-4f97-a890-4d88eb618d66}"

// Define model size.
#define NUMBER_OF_REALS 2
#define NUMBER_OF_INTEGERS 5
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 0
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

#define resolution_ 0
#define was_zero_ 1
#define was_pos_ 2
#define was_neg_ 3
#define microstep_ 4


#define present_ 0
#define absent_ 1
#define unknown_ 2

#define STATES {}

// Ccalled by fmi2Instantiate.
// Set values for all variables that define a start value.
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode.
void setStartValues(ModelInstance *comp) {
    r(output_) = 0;
    hr(output_) = absent_;
    hr(input_) = absent_;
    pos(0) = 0;
    i(was_zero_)    = 0;
    i(was_neg_)     = 0;
    i(was_pos_)     = 0;
    i(microstep_)   = 0;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.

#define epsilon 0.000000000001

void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        // pos(0) = (r(input_) + epsilon) > 0;
    }
    else {
        int is_zero = (r(input_) - epsilon) < 0 && (r(input_) + epsilon) > 0;
        int is_pos  = (r(input_) + epsilon) > 0 && !is_zero;
        int is_neg  = (r(input_) - epsilon) < 0 && !is_zero;

        if ( ((is_neg && i(was_pos_)) || (is_pos && i(was_neg_)) || (is_zero && !i(was_zero_)))
                && (hr(input_) == present_) ) {
            r(output_)  = 0;
            hr(output_) = present_;
        }
        else {
            r(output_)  = -1;
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
        default:
            return 0;
    }
}

fmi2Real getEventIndicator(ModelInstance* comp, int z) {
    switch (z) {
        case 0 : return (r(input_) + epsilon) > 0 ? 1 : -1;
        default: return 0;
    }
}

// Used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmi2EventInfo* eventInfo, int isTimeEvent) {
    // Sanity check:
    // This component only works with continuous time input signals
    if (hr(input_) == absent_) {
        comp->eventInfo.terminateSimulation = fmi2True;
    }
    if (i(microstep_) == 0) {
        int is_zero     = (r(input_) - epsilon) < 0 && (r(input_) + epsilon) > 0;
        int is_pos      = (r(input_) + epsilon) > 0 && !is_zero;
        int is_neg      = (r(input_) - epsilon) < 0 && !is_zero;

        i(was_zero_)    = is_zero;
        i(was_neg_)     = is_neg;
        i(was_pos_)     = is_pos;
    } else {
        i(was_zero_)    = 0;
        i(was_neg_)     = 0;
        i(was_pos_)     = 0;
    }

    comp->eventInfo.nextEventTimeDefined = fmi2False;

    if (i(microstep_) == 1 || (isTimeEvent && i(microstep_) < 2)) {
        comp->eventInfo.nextEventTime = comp->time;
        comp->eventInfo.nextEventTimeDefined = fmi2True;
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

fmi2Status fmi2HybridGetMaxStepSize (fmi2Component c, fmi2Integer *value) {
    ModelInstance *comp = (ModelInstance *)c;
    fmi2Integer max_step_size;
    printf("HybridZeroCrossingDetector-fmi2HybridGetMaxStepSize, pos(0): %d, getEventIndicator: %f, i(microstep_): %ld\n", pos(0), getEventIndicator(comp, 0), i(microstep_));
    if ((getEventIndicator(comp, 0) * (pos(0) ? 1 : -1) > 0)
                || (i(microstep_) >= 2)) {
        max_step_size = 2;
    }
    else if (((getEventIndicator(comp, 0) * (pos(0) ? 1 : -1) < 0)
                && i(microstep_) == 0)
                || (i(microstep_) == 1 && comp->eventInfo.nextEventTimeDefined)) {
        max_step_size = 0;
    } else {
        max_step_size = 2;
    }
    printf("HybridZeroCrossingDetector-fmi2HybridGetMaxStepSize, time: %ld, h: %ld\n", comp->time, max_step_size);
    *value = max_step_size;
    return fmi2OK;
}


/* END */

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

