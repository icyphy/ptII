/* ---------------------------------------------------------------------------*
 * An FMU that increments its output, but also has an fmi2GetMaxStepSize() method.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// Define class name and unique id.
#define MODEL_IDENTIFIER HybridPerPiecCosSigGen
#define MODEL_GUID "{b44d7772-66a4-4ec2-b9f6-fad3625d51a2}"

// Define model size.
#define NUMBER_OF_REALS 3
#define NUMBER_OF_INTEGERS 3
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
#define value_b_ 1
#define value_a_ 2
#define period_ 0
#define resolution_ 1
#define n_ 2
#define STATES { output_ }

#define present_ 0
#define absent_ 1
#define unknown_ 2

// Ccalled by fmi2Instantiate.
// Set values for all variables that define a start value.
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode.
void setStartValues(ModelInstance *comp) {
    r(output_) = r(value_a_);
    i(n_) = 0;
    hr(output_) = present_;
    hr(value_b_) = present_;
    hr(value_a_) = present_;
    hi(period_) = present_;
    hi(n_) = present_;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        i(n_) = 0;
        comp->eventInfo.nextEventTimeDefined   = fmi2True;
        comp->eventInfo.nextEventTime          = i(period_) + comp->time;
    }
    else {
        fmi2Integer t = comp->time;
        fmi2Integer k = t / i(period_);
        fmi2Integer r = t % i(period_);
        fmi2Boolean is_even = !(fmi2Boolean)(k % 2);

        if ( t > (k * i(period_))  && t < ((k + 1) * i(period_)) ) {
            if (is_even) {
                r(output_) = r(value_a_);
            }
            if (!is_even) {
                r(output_) = r(value_b_);
            }
        }
        else if ( (k > 0) &&  (!is_even) && (i(n_) >= 1) ) {
            r(output_) = r(value_b_);
        }
        else if ( (t > 0) && (k > 0) &&  (is_even) && (i(n_) == 0) ) {
            r(output_) = r(value_b_);
        }
        else {
            r(output_) = r(value_a_);
        }
    }    
}

// called by fmiGetReal, fmiGetContinuousStates and fmiGetDerivatives
fmi2Real getReal(ModelInstance* comp, fmi2ValueReference vr){
    switch (vr)
    {
        case output_:
            return r(output_);
        case value_b_:
            return r(value_b_);
        case value_a_:
            return r(value_a_); 
        default:
            return 0;
    }
}

// Used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmi2EventInfo* eventInfo, int timeEvent, long h) {

    if ( h == 0 ) {
        i (n_) = i(n_) + 1;
        eventInfo->nextEventTimeDefined   = fmi2True;
        eventInfo->nextEventTime          = i(period_) + comp->time;
    }
    else {
        i (n_) = 0;
        eventInfo->nextEventTimeDefined   = fmi2True;
        eventInfo->nextEventTime          = comp->time;
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
    max_step_size = comp->eventInfo.nextEventTime - comp->time;
    *value = max_step_size;
    return fmi2OK;
}


/* END */

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

