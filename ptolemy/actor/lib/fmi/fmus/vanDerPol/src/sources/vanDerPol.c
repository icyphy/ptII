/* ---------------------------------------------------------------------------*
 * Copyright QTronic GmbH. All rights reserved.
 * Sample implementation of an FMU - the Van der Pol oscillator.
 * See http://en.wikipedia.org/wiki/Van_der_Pol_oscillator
 *  
 *   der(x0) = x1
 *   der(x1) = mu * ((1 - x0 ^ 2) * x1) - x0;
 *
 *   start values: x0=2, x1=0, mue=1
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER vanDerPol
#define MODEL_GUID "{8c4e810f-3da3-4a00-8276-176fa3c9f000}"

// define model size
#define NUMBER_OF_REALS 5
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 2
#define NUMBER_OF_EVENT_INDICATORS 0

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"

// define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define x0_     0
#define der_x0_ 1
#define x1_     2
#define der_x1_ 3
#define mu_     4

// define state vector as vector of value references
#define STATES { x0_, x1_ }

// called by fmiInstantiateModel
// Set values for all variables that define a start value
// Settings used unless changed by fmiSetX before fmiInitialize
void setStartValues(ModelInstance *comp) {
    r(x0_) = 2;
    r(x1_) = 0;
    r(mu_) = 1;
}

// called by fmiInitialize() after setting eventInfo to defaults
// Used to set the first time event, if any.
void initialize(ModelInstance* comp, fmiEventInfo* eventInfo) {
}

// called by fmiGetReal, fmiGetContinuousStates and fmiGetDerivatives
fmiReal getReal(ModelInstance* comp, fmiValueReference vr){
    switch (vr) {
        case x0_     : return r(x0_);
        case x1_     : return r(x1_);
        case der_x0_ : return r(x1_);
        case der_x1_ : return r(mu_) * ((1.0-r(x0_)*r(x0_))*r(x1_)) - r(x0_);
        case mu_     : return r(mu_);
        default: return 0;
    }
}

// Used to set the next time event, if any.
void eventUpdate(fmiComponent comp, fmiEventInfo* eventInfo) {
} 

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"


