/* ---------------------------------------------------------------------------*
 * An FMU that increments its output, but also has an fmi2GetMaxStepSize() method.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// Define class name and unique id.
#define MODEL_IDENTIFIER CTClockW
#define MODEL_GUID "{85674241-411d-4f6a-972c-d21523321550}"

// Define model size.
#define NUMBER_OF_REALS 3
#define NUMBER_OF_INTEGERS 1
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
#define value_b_ 1
#define value_a_ 2
#define period_ 0

// Ccalled by fmi2Instantiate.
// Set values for all variables that define a start value.
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode.
void setStartValues(ModelInstance *comp) {
    r(output_) = r(value_a_);
    comp->microstep = 0;
    hr(output_) = present_;
    hr(value_b_) = present_;
    hr(value_a_) = present_;
    hi(period_) = present_;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        comp->microstep = 0;
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
        else if ( (k > 0) &&  (!is_even) && (comp->microstep >= 1) ) {
            r(output_) = r(value_b_);
        }
        else if ( (t > 0) && (k > 0) &&  (is_even) && (comp->microstep == 0) ) {
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

// used to check if there is any state event
fmi2Real getEventIndicator(ModelInstance* comp, int z) {
    switch (z) {
        default: return 0;
    }
}

void doStep(ModelInstance* comp, fmi2IntegerTime hLocal, int inBetween) {
    if (inBetween == 0) {

    } else {
        if (comp->eventInfo.nextEventTimeDefined && (comp->time  == comp->eventInfo.nextEventTime)) {
            if (hLocal == 0) {
                comp->eventInfo.nextEventTimeDefined   = fmi2True;
                comp->eventInfo.nextEventTime          = i(period_) + comp->time;
            }
            else {
                comp->eventInfo.nextEventTimeDefined   = fmi2True;
                comp->eventInfo.nextEventTime          = comp->time;
            }
        }
        comp->time += hLocal;
        if (hLocal > 0) comp->microstep = 0;
        else comp->microstep++;
    }

}

// Used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmi2EventInfo* eventInfo, int timeEvent,
        fmi2Integer localStepSize, int inBetween) {

}

fmi2IntegerTime getMaxStepSize(ModelInstance *comp) {
    fmi2IntegerTime communicationStepSize;
    communicationStepSize = comp->eventInfo.nextEventTime - comp->time;
    return communicationStepSize;
}


/* END */

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"
