/* ---------------------------------------------------------------------------*
 * An FMU that increments its output, but also has an fmi2GetMaxStepSize() method.
 * Based on the FMUSDK inc fmu Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// Define class name and unique id.
#define MODEL_IDENTIFIER ZeroCrossingDetectorIT
#define MODEL_GUID "{fa372ac2-41d5-4e81-a4d4-526b68fed7b0}"

// Define model size.
#define NUMBER_OF_REALS 3
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_BOOLEANS 6
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 0
#define NUMBER_OF_EVENT_INDICATORS 1

// Include fmu header files, typedefs and macros.
#include "fmuTemplate.h"

// Define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative

#define output_ 0
#define input_ 1
#define input_der_ 2

#define was_zero_t_e_ 0
#define was_pos_t_e_ 1
#define was_neg_t_e_ 2
#define was_zero_n_1_ 3
#define was_pos_n_1_ 4
#define was_neg_n_1_ 5

#define STATES {  }

#define present_ 0
#define absent_ 1
#define unknown_ 2

#define epsilon 0.000000000001

#define RESOLUTION 6

// Ccalled by fmi2Instantiate.
// Set values for all variables that define a start value.
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode.
void setStartValues(ModelInstance *comp) {
    r(output_) = 0;
    hr(output_) = absent_;
    hr(input_) = absent_;
    pos(0) = 0;
    b(was_zero_t_e_)    = 0;
    b(was_pos_t_e_)     = 0;
    b(was_neg_t_e_)     = 0;
    b(was_zero_n_1_)    = 0;
    b(was_pos_n_1_)     = 0;
    b(was_neg_n_1_)     = 0;
}

fmi2Boolean _isTime(ModelInstance *comp) {
    return comp->eventInfo.nextEventTimeDefined; &&
        (comp->eventInfo.nextEventTime - comp->time < epsilon);// &&
        // (comp->eventInfo.nextEventTime - comp->time > - epsilon);
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode || (comp->microstep == 0 && comp->time == 0)) {
        comp->eventInfo.nextEventTimeDefined = fmi2False;
        fmi2Integer is_zero     = (r(input_) - epsilon) < 0 && (r(input_) + epsilon) > 0;
        fmi2Integer is_pos      = (r(input_) + epsilon) > 0 && !is_zero;
        fmi2Integer is_neg      = (r(input_) - epsilon) < 0 && !is_zero;
        b(was_zero_t_e_)    = is_zero;
        b(was_pos_t_e_)     = is_pos;
        b(was_neg_t_e_)     = is_neg;
    }
    else {
        if (_isTime(comp)) {
            r(output_)  = 0;
            hr(output_) = present_;
        }
        else {
            r(output_)  = 0;
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

// used to check if there is any state event
fmi2Real getEventIndicator(ModelInstance* comp) {
        fmi2Boolean is_zero     = (r(input_) - epsilon) < 0 && (r(input_) + epsilon) > 0;
        fmi2Boolean is_pos      = (r(input_) + epsilon) > 0 && !is_zero;
        fmi2Boolean is_neg      = (r(input_) - epsilon) < 0 && !is_zero;
        fmi2Boolean event = fmi2False;
        // condition 1
        if (is_zero && comp->microstep == 0 && !b(was_zero_t_e_)) {
            event = fmi2True;
        }
        // condition 2
        else if (comp->microstep > 0 &&
                ((is_pos && b(was_neg_n_1_)) || (is_neg && b(was_pos_n_1_)))) {
            event = fmi2True;
        }
        // condition 3
        else if (comp->microstep > 0 && (is_zero) && !b(was_zero_n_1_)) {
            event = fmi2True;
        }
        else {
            event = fmi2False;
        }
        return event ? -1 : 1;
}

fmi2Boolean doStep(ModelInstance* comp, fmi2Real hLocal) {
    if (_isTime(comp) || getEventIndicator(comp) < 0) {
        if (comp->eventInfo.nextEventTimeDefined/* && getEventIndicator(comp) && comp->microstep == 1*/) {
            comp->eventInfo.nextEventTimeDefined = fmi2False;
        }
        if ( getEventIndicator(comp) < 0) {
            comp->eventInfo.nextEventTime = comp->time;
            comp->eventInfo.nextEventTimeDefined = fmi2True;
        }
    }
    fmi2Integer is_zero     = (r(input_) - epsilon) < 0 && (r(input_) + epsilon) > 0;
    fmi2Integer is_pos      = (r(input_) + epsilon) > 0 && !is_zero;
    fmi2Integer is_neg      = (r(input_) - epsilon) < 0 && !is_zero;

    if (hLocal > 0) {
        b(was_zero_t_e_)    = is_zero;
        b(was_pos_t_e_)     = is_pos;
        b(was_neg_t_e_)     = is_neg;
    } else if (hLocal == 0) {
        b(was_zero_n_1_)    = is_zero;
        b(was_pos_n_1_)     = is_pos;
        b(was_neg_n_1_)     = is_neg;
    }
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
    fmi2Real communicationStepSize;
    if (comp->eventInfo.nextEventTimeDefined || (getEventIndicator(comp) < 0)) {
        communicationStepSize = 0;
    } else if (!((r(input_der_) - epsilon < 0) && (r(input_der_) + epsilon > 0))) {
        // r(input_) + r(input_der_) * h = 0
        fmi2Real h = -r(input_) / r(input_der_);
        if (h > 0) {
            communicationStepSize = h;
        } else {
            communicationStepSize = 1000.0;
        }
    }
    else {
        communicationStepSize = 1000.0;
    }
    return communicationStepSize;
}


/* END */

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"
