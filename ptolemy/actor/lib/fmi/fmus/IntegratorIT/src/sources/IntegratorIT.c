/* ---------------------------------------------------------------------------*
 * An FMU that increments its output, but also has an fmi2GetMaxStepSize() method.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// Define class name and unique id.
#define MODEL_IDENTIFIER IntegratorIT
#define MODEL_GUID "{bca775cc-143c-4873-b090-b2efc1ae40f9}"

// Define model size.
#define NUMBER_OF_REALS 4
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

#define output_ 0
#define der_ 1
#define start_value_ 2
#define reset_ 3

// Ccalled by fmi2Instantiate.
// Set values for all variables that define a start value.
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode.
void setStartValues(ModelInstance *comp) {
    r(output_) = r(start_value_);
    r(der_) = r(start_value_);
    r(reset_) = 0.0;
    hr(output_) = present_;
    hr(der_) = absent_;
    hr(reset_) = absent_;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        r(output_) = r(start_value_);
        comp->eventInfo.nextEventTimeDefined = fmi2False;
    }
    else {
        if (hr(reset_) == present_ /*&& comp->microstep == 1*/) {
            r(output_) = 0.0;
        }
    }
}

// called by fmiGetReal, fmiGetContinuousStates and fmiGetDerivatives
fmi2Real getReal(ModelInstance* comp, fmi2ValueReference vr){
    switch (vr)
    {
        case output_:
            return r(output_);
        case der_:
            return r(der_);
        case start_value_:
            return r(start_value_);
        case reset_:
            return r(reset_);
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
    r(output_) = r(output_) + hLocal * r(der_);
    comp->time += hLocal;
    if (hLocal > 0) comp->microstep = 0;
    else comp->microstep++;
    return fmi2OK;
}

// Used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmi2EventInfo* eventInfo, int timeEvent,
        fmi2Integer localStepSize, int inBetween) {

}

fmi2Real getMaxStepSize(ModelInstance *comp) {
    fmi2Real communicationStepSize = 1000.0; // default value
    return communicationStepSize;
}


/* END */

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"
