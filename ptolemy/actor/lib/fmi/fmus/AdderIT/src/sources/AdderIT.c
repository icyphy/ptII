/* ---------------------------------------------------------------------------*
 * An FMU that increments its output, but also has an fmi2GetMaxStepSize() method.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// Define class name and unique id.
#define MODEL_IDENTIFIER AdderIT
#define MODEL_GUID "{5fc43b20-985e-4258-beab-5f29332209cc}"

// Define model size.
#define NUMBER_OF_REALS 5
#define NUMBER_OF_INTEGERS 0
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

#define STATES {  }

#define present_ 0
#define absent_ 1
#define unknown_ 2

#define RESOLUTION 6

#define output_ 0
#define input_a_ 1
#define input_b_ 2
#define input_a_der_ 3
#define input_b_der_ 4

// Ccalled by fmi2Instantiate.
// Set values for all variables that define a start value.
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode.
void setStartValues(ModelInstance *comp) {
    r(output_) = 0.0;
    r(input_a_) = 0.0;
    r(input_b_) = 0.0;
    r(input_a_der_) = 0.0;
    r(input_b_der_) = 0.0;
    hr(output_) = absent_;
    hr(input_a_) = absent_;
    hr(input_b_) = absent_;}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (hr(input_a_) == present_ && hr(input_b_) == present_) {
        hr(output_) = present_;
        r(output_) = r(input_a_) + r(input_b_);
    }
    else if (hr(input_a_) == absent_ && hr(input_b_) == present_) {
        hr(output_) = present_;
        r(output_) = r(input_b_);
    }
    else if (hr(input_a_) == present_ && hr(input_b_) == absent_) {
        hr(output_) = present_;
        r(output_) = r(input_a_);
    }
    else if (hr(input_a_) == absent_ && hr(input_b_) == absent_) {
        hr(output_) = absent_;
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
        case output_:
            return r(output_);
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

void doStep(ModelInstance* comp, fmi2IntegerTime hLocal, int inBetween) {

    if (inBetween == 0) {
        comp->time += hLocal;



        if (hLocal > 0) comp->microstep = 0;
        else comp->microstep++;

    } else {

    }

}

// Used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmi2EventInfo* eventInfo, int timeEvent,
        fmi2Integer localStepSize, int inBetween) {

}

fmi2IntegerTime getMaxStepSize(ModelInstance *comp) {
    fmi2IntegerTime communicationStepSize = 1000; // default value
    return communicationStepSize;
}


/* END */

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"
