/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU - increments an int counter every second.
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER stairsA20RC1F
#define MODEL_GUID "{b1ca019f-8d2c-4ccb-84a4-457849d1560a}"

// define model size
#define NUMBER_OF_REALS 1
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 1
#define NUMBER_OF_EVENT_INDICATORS 0

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"

// Defines specific to this FSM
typedef enum {STAIR_STATE, FLOOR_STATE} state;
state current_state;

#define STAIRCASE_HEIGHT 2.0
#define STAIR_LENGTH 0.3
#define STAIR_HEIGHT 0.2

// Generic definitions used by Mealy machines
#define TICK_PERIOD STAIR_LENGTH

// Indices of instance variables can be used to set or get by the master algorithm.
#define LEVEL 0

// define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative

// Define reals
#define level_ 0

// Define initial state vector as vector of value references
#define STATES { 0 }

// Define local variables
// Reals
fmiReal level_state;
fmiReal currentCount;
fmiReal lastSuccessfulTime;
fmiReal relativeTolerance;

// Define booleans
fmiBoolean atBreakpoint;


/*****************************************************************************************/
/*  Give the inital states of the Mealy machine
 *  @param c The FMU.
 */
/* We add a prefix of the MODEL_IDENTIFIER so that when we run two models that
 * both have .so files that have a init_state() function, we get the right function.
 */
#define init_state fmiFullName(init_state)
void init_state(fmiComponent c) {
    level_state = STAIRCASE_HEIGHT;
    current_state = STAIR_STATE;
}


/*****************************************************************************************/
/*  Transition function that updates the state of the Mealy machine
 *  @param c The FMU.
 */
#define transition_function fmiFullName(transition_function)
void transition_function(fmiComponent c) {

    if(current_state == STAIR_STATE){
       level_state -= STAIR_HEIGHT;
       if(level_state < 0){
           level_state = 0;
           current_state = FLOOR_STATE;
       }
    }
}


/*****************************************************************************************/
/*  Output function of the Mealy machine
 *  @param c The FMU.
 */
#define output_function fmiFullName(output_function)
void output_function(fmiComponent c) {
    ModelInstance* comp = (ModelInstance *) c;
    r(level_) = level_state;
}



// called by fmiInstantiate
// Set values for all variables that define a start value
// Settings used unless changed by fmiSetX before fmiEnterInitializationMode
void setStartValues(ModelInstance *comp) {
        init_state(comp);
}

// called by fmiExitInitializationMode() after setting eventInfo to defaults
// Used to set the first time event, if any.
void initialize(ModelInstance* c, fmiEventInfo* eventInfo) {
    ModelInstance* comp = (ModelInstance *) c;

    eventInfo->nextEventTimeDefined   = fmiTrue;
    eventInfo->nextEventTime          = 1 + comp->time;

    // FIXME: Use logger instead.
    // component->functions->logger(c, component->instanceName, fmiOK, "message",
    //                             "Invoked fmiIntializeSlave: start: %g, StopTimeDefined: %d, tStop: %g.",
    //                             tStart, stopTimeDefined, tStop);
    printf("%s: Invoked fmiIntializeSlave: start: %g, StopTimeDefined: %d, tStop: %g..\n",
           comp->instanceName, comp->time, fmiFalse, 0.0);
    fflush(stdout);

    lastSuccessfulTime = comp->time; // comp->time was set to start time by fmiSetupExperiment
    atBreakpoint = fmiFalse;
    relativeTolerance = 0.0001; //FIXME: relativeTolerance should be given by component
    init_state(comp);
}

// used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmiEventInfo* eventInfo) {

    // If current time is greater than period * (value + 1), then it is
    // time for another increment.
    double endOfStepTime = eventInfo->nextEventTime;
    double targetTime = TICK_PERIOD * (currentCount + 1);
    if (endOfStepTime >= targetTime - relativeTolerance) {
        // It is time for an increment.
        // Is it too late for the increment?
        if (endOfStepTime > targetTime + relativeTolerance) {
            // Indicate that the last successful time is
            // at the target time.
            lastSuccessfulTime = targetTime;
            fflush(stdout);
        }
        //Go ahead an increment.
        currentCount++;
        // Update the state
        transition_function(comp);
    }
    lastSuccessfulTime = endOfStepTime;
    fflush(stdout);
}


/*****************************************************************************************
 *  Get the maximum next step size.
 *  If the last call to fmiDoStep() incremented the counter, then the maximum step
 *  size is zero. Otherwise, it is the time remaining until the next increment of the count.
 *  @param c The FMU.
 *  @param maxStepSize A pointer to a real into which to write the result.
 *  @return fmiOK.
 */
FMI_Export fmiStatus  fmiGetMaxStepSize(fmiComponent c, fmiReal *maxStepSize) {
    double targetTime = TICK_PERIOD * (currentCount + 1);
    double step = targetTime - lastSuccessfulTime;
    *maxStepSize = step;
    return fmiOK;
}


/*****************************************************************************************
 *  Get the values of the specified real variables.
 *  @param c The FMU.
 *  @param vr An array of value references (indices) for the desired values.
 *  @param nvr The number of values desired (the length of vr).
 *  @param value The array into which to put the results.
 *  @return fmiError if a value reference is out of range, otherwise fmiOK.
 */
fmiReal getReal(fmiComponent c, const fmiValueReference vr) {
    ModelInstance* comp = (ModelInstance *) c;

    output_function(c);
    return r(vr);
}


// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

