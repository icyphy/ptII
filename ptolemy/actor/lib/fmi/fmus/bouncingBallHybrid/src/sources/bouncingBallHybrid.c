/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU - a bouncing ball.
 * This demonstrates the use of state events and reinit of states.
 * Equations:
 *  der(h) = v;
 *  der(v) = -g;
 *  when h<0 then v := -e * v;
 *  where
 *    h      height [m], used as state, start = 1
 *    v      velocity of ball [m/s], used as state
 *    der(h) velocity of ball [m/s]
 *    der(v) acceleration of ball [m/s2]
 *    g      acceleration of gravity [m/s2], a parameter, start = 9.81
 *    e      a dimensionless parameter, start = 0.7
 *
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER bouncingBallHybrid
#define MODEL_GUID "{2796a519-9e5f-4c17-ab6a-c932c9a338c6}"

// define model size
#define NUMBER_OF_REALS 6
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_BOOLEANS 6
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 2
#define NUMBER_OF_EVENT_INDICATORS 1

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"
#include "math.h"

// define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define h_      0
#define der_h_  1
#define v_      2
#define der_v_  3
#define g_      4
#define e_      5

#define was_zero_t_e_ 0
#define was_pos_t_e_  1
#define was_neg_t_e_  2
#define was_zero_n_1_ 3
#define was_pos_n_1_  4
#define was_neg_n_1_  5

#define present_ 0
#define absent_ 1
#define unknown_ 2

// offset for event indicator, adds hysteresis and prevents z=0 at restart
#define EPS_INDICATORS 1e-6

// define initial state vector as vector of value references
#define STATES { h_, v_ }

fmi2Boolean _isTime(ModelInstance *comp) {

    fmi2Boolean isTime = comp->eventInfo.nextEventTimeDefined &&
        ((comp->eventInfo.nextEventTime - comp->time < (1.0/comp->timeResolution)) &&
        (comp->eventInfo.nextEventTime - comp->time > - (1.0/comp->timeResolution)));
    return isTime;
}

// called by fmi2Instantiate
// Set values for all variables that define a start value
// Settings used unless changed by fmi2SetX before fmi2EnterInitializationMode
void setStartValues(ModelInstance *comp) {
    r(h_)     =  100;
    r(v_)     =  0;
    r(g_)     =  9.81;
    r(e_)     =  0.7;
    b(was_zero_t_e_)    = 0;
    b(was_pos_t_e_)     = 0;
    b(was_neg_t_e_)     = 0;
    b(was_zero_n_1_)    = 0;
    b(was_pos_n_1_)     = 0;
    b(was_neg_n_1_)     = 0;
}

// called by fmi2GetReal, fmi2GetInteger, fmi2GetBoolean, fmi2GetString, fmi2ExitInitialization
// if setStartValues or environment set new values through fmi2SetXXX.
// Lazy set values for all variable that are computed from other variables.
void calculateValues(ModelInstance *comp) {
    if (comp->state == modelInitializationMode) {
        fmi2Boolean is_zero = (fmi2Boolean) (r(h_) - EPS_INDICATORS) < 0 && (r(h_) + EPS_INDICATORS) > 0;
        fmi2Boolean is_pos  = (fmi2Boolean) (r(h_) - EPS_INDICATORS) > 0 && !is_zero;
        fmi2Boolean is_neg  = (fmi2Boolean) (r(h_) + EPS_INDICATORS) < 0 && !is_zero;
        b(was_zero_t_e_)    = is_zero;
        b(was_pos_t_e_)     = is_pos;
        b(was_neg_t_e_)     = is_neg;

        r(der_v_) = -r(g_);
        pos(0) = r(h_) > 0;

        // set first time event, if any, using comp->eventInfo.nextEventTime
    }
}

// called by fmi2GetReal, fmi2GetContinuousStates and fmi2GetDerivatives
fmi2Real getReal(ModelInstance* comp, fmi2ValueReference vr) {
    switch (vr) {
        case h_     : return r(h_);
        case der_h_ : return r(v_);
        case v_     : return r(v_);
        case der_v_ : return r(der_v_);
        case g_     : return r(g_);
        case e_     : return r(e_);
        default: return 0;
    }
}

fmi2Real getEventIndicator(ModelInstance* comp) {
        fmi2Boolean is_zero     = (r(h_) - EPS_INDICATORS) < 0 && (r(h_) + EPS_INDICATORS) > 0;
        fmi2Boolean is_pos      = (r(h_) - EPS_INDICATORS) > 0 && !is_zero;
        fmi2Boolean is_neg      = (r(h_) + EPS_INDICATORS) < 0 && !is_zero;
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
        // condition 4: negative and then positive
        else if (comp->microstep == 0 && is_pos && b(was_neg_t_e_)) {
            event = fmi2True;
        }
        else {
            event = fmi2False;
        }
        return event ? -1 : 1;
}

fmi2Boolean doStep(ModelInstance* comp, fmi2Real hLocal) {

    if (_isTime(comp) || getEventIndicator(comp) < 0) {
        if (comp->eventInfo.nextEventTimeDefined) {
            comp->eventInfo.nextEventTimeDefined = fmi2False;
        }
        if (getEventIndicator(comp) < 0) {
            comp->eventInfo.nextEventTime = comp->time;
            comp->eventInfo.nextEventTimeDefined = fmi2True;
        }
    }
    pos(0) = r(h_) > 0;
    if (!pos(0)) {
        r(v_) = - r(e_) * r(v_);
    }
    fmi2ValueReference vrStates0[NUMBER_OF_STATES] = STATES;
    for (int i = 0; i < NUMBER_OF_STATES; i++) {
        fmi2ValueReference vr = vrStates0[i];
        r(vr) += hLocal * getReal(comp, vr + 1); // forward Euler step
    }

    

    fmi2Boolean is_zero = (r(h_) - EPS_INDICATORS) < 0 && (r(h_) + EPS_INDICATORS) > 0;
    fmi2Boolean is_pos  = (r(h_) - EPS_INDICATORS) > 0 && !is_zero;
    fmi2Boolean is_neg  = (r(h_) + EPS_INDICATORS) < 0 && !is_zero;
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

typedef struct {
    fmi2Real communicationStepSize;
    fmi2Status status;
} stepSize;

stepSize getMaxStepSize(ModelInstance *comp) {
    stepSize communicationStepSize;
    fmi2Boolean is_zero = (r(h_) - EPS_INDICATORS) < 0 && (r(h_) + EPS_INDICATORS) > 0;
    fmi2Boolean is_pos  = (r(h_) - EPS_INDICATORS) > 0 && !is_zero;
    fmi2Boolean is_neg  = (r(h_) + EPS_INDICATORS) < 0 && !is_zero;

    if (comp->eventInfo.nextEventTimeDefined || (getEventIndicator(comp) < 0)) {
        communicationStepSize.communicationStepSize = 0;
        communicationStepSize.status = fmi2OK;
        return communicationStepSize;
    }
    else if (is_neg) {
        communicationStepSize.status = fmi2Error;
        return communicationStepSize;
    }
    /*else if (is_pos)*/ {
        communicationStepSize.communicationStepSize = 1.0;
        communicationStepSize.status = fmi2OK;
        return communicationStepSize;
    }
}

// used to set the next time event, if any.
void eventUpdate(ModelInstance *comp, fmi2EventInfo *eventInfo, int isTimeEvent) {
    pos(0) = r(h_) > 0;
    if (!pos(0)) {
        r(v_) = - r(e_) * r(v_);
    }
    eventInfo->valuesOfContinuousStatesChanged   = fmi2True;
    eventInfo->nominalsOfContinuousStatesChanged = fmi2False;
    eventInfo->terminateSimulation   = fmi2False;
    eventInfo->nextEventTimeDefined  = fmi2False;
}

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"
