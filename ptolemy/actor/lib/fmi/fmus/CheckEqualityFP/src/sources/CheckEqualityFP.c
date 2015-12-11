/* ---------------------------------------------------------------------------*
 * An FMU that increments its output, but also has an fmi2GetMaxStepSize() method.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// Define class name and unique id.
#define MODEL_IDENTIFIER CheckEqualityFP
#define MODEL_GUID "{8cb21413-150b-4745-afb0-87a4da386fdf}"

// Define model size.
#define NUMBER_OF_REALS 2
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_BOOLEANS 1
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

#define STATES {  }

#define present_ 0
#define absent_ 1
#define unknown_ 2

#define output_ 0
#define input_a_ 0
#define input_b_ 1

// Ccalled by fmi2Instantiate.
// Set values for all variables that define a start value.
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode.
void setStartValues(ModelInstance *comp) {
    b(output_) = fmi2False;
    r(input_a_) = 0.0;
    r(input_b_) = 0.0;
    hb(output_) = absent_;
    hr(input_a_) = absent_;
    hr(input_b_) = absent_;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {

    }
    else {
        if ( hr(input_a_) == absent_ && hr(input_b_) == absent_) {
            hb(output_) = absent_;
            b(output_) = fmi2True;
        }
        else if ( hr(input_a_) == present_ && hr(input_b_) == present_ &&
                   r(input_a_) == r(input_b_)) {
            hb(output_) = present_;
            b(output_) = fmi2True;
        }
        else {
            hb(output_) = present_;
            b(output_) = fmi2False;
        }
    }
}

// called by fmiGetReal, fmiGetContinuousStates and fmiGetDerivatives
fmi2Real getReal(ModelInstance* comp, fmi2ValueReference vr){
    switch (vr)
    {
        case input_a_:
            return r(input_a_);
        case input_b_:
            return r(input_b_);
        default:
            return 0;
    }
}

// used to check if there is any state event
fmi2Real getEventIndicator(ModelInstance* comp, int z) {
    switch (z) {
        default: return 0;
    }
}

fmi2Boolean doStep(ModelInstance* comp, fmi2Real hLocal) {
    return fmi2OK;
}

// Used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmi2EventInfo* eventInfo, int timeEvent,
        fmi2Integer localStepSize, int inBetween) {

}

fmi2Real getMaxStepSize(ModelInstance *comp) {
    fmi2Real communicationStepSize = 1E10; // default value
    return communicationStepSize;
}


/* END */

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"
