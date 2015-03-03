/* ---------------------------------------------------------------------------*
 *
 * This FMU models a staircase as a Mealy machine with fixed time period.
 * The FMU is intended to be used togehter with a bouncing ball FMU, so
 * that the ball bounces down the staircase.
 *
 * Note that this FMU makes use of function:
 *
 *   fmiStatus  fmiGetMaxStepSize(fmiComponent c, fmiReal *maxStepSize);
 *
 * that was proposed as an extension to the FMU standard
 * (see EMSOFT 2013 paper by Broman et al.)
 *
 * This file is based on the template FMU 'stepCounter' developed by
 * Christopher Brooks and Edward A. Lee.
 *
 * Authors: David Broman
 * ---------------------------------------------------------------------------*/


#include <stdio.h>
#include <string.h>

// The model identifier string.
#define MODEL_IDENTIFIER levelStairs
// Globally unique ID used to make sure the XML file and the DLL match.
// The following was generated at http://guid.us
#define MODEL_GUID "{ebfb61fb-9c1c-4b1b-81de-f503fe893ae9}"

// Used by FMI 2.0.  See FMIFuctions.h
//#define FMIAPI_FUNCTION_PREFIX levelStairs_

// include fmu header files, typedefs and macros
#include "fmiFunctions.h"

#define EPSILON 1e-9

// Defines specific to this FSM
typedef enum {STAIR_STATE, FLOOR_STATE} state;
#define STAIRCASE_HEIGHT 2.0
#define STAIR_LENGTH 0.3
#define STAIR_HEIGHT 0.2

// Generic definitions used by Mealy machines
#define TICK_PERIOD STAIR_LENGTH
/*****************************************************************************************
 * Data structure for an instance of this FMU.
 */
typedef struct {

    //Input variables
    fmiReal reference;         // The reference signal.

    //Output variables
    fmiReal level;             // The output level of the stair/floor

    // States specific for this FMU
    fmiReal level_state;        // State of the current level from the ground (excluding crack)
    state current_state;        // Current state of the staircase (stairs or floor)

    // Generic Mealy machine state
    fmiBoolean atBreakpoint;    // Indicator that the first output at a step
                                // time has been produced.

    // General states
    fmiReal currentCount;       // The current count (the output).
    fmiReal lastSuccessfulTime; // The time to which this FMU has advanced.
    const fmiCallbackFunctions *functions;
    fmiString instanceName;
} ModelInstance;

// Indices of instance variables can be used to set or get by the master algorithm.
#define REFERENCE 1
#define LEVEL 2

/*****************************************************************************************/
/*  Give the inital states of the Mealy machine
 *  @param c The FMU.
 */
/* We add a prefix of the MODEL_IDENTIFIER so that when we run two models that
 * both have .so files that have a init_state() function, we get the right function.
 */
#define init_state fmiFullName(init_state)
void FMIAPI init_state(fmiComponent c) {
    ModelInstance* component = (ModelInstance *) c;

    component->level_state = STAIRCASE_HEIGHT;
    component->current_state = STAIR_STATE;
}


/*****************************************************************************************/
/*  Transition function that updates the state of the Mealy machine
 *  @param c The FMU.
 */
#define transition_function fmiFullName(transition_function)
void FMIAPI transition_function(fmiComponent c) {
    ModelInstance* component = (ModelInstance *) c;

    if(component->current_state == STAIR_STATE){
       component->level_state -= STAIR_HEIGHT;
       if(component->level_state < 0){
           component->level_state = 0;
           component->current_state = FLOOR_STATE;
       }
    }
}


/*****************************************************************************************/
/*  Output function of the Mealy machine
 *  @param c The FMU.
 */
#define output_function fmiFullName(output_function)
void FMIAPI output_function(fmiComponent c) {
    ModelInstance* component = (ModelInstance *) c;
    component->level = component->level_state + component->reference;
}


// FIXME: The following function is boilerplate that should be shared among FMUs.
/*****************************************************************************************
 *  Check various properties of this FMU. Return 0 if any requirement is violated, and 1 otherwise.
 *  @param instanceName The name of the instance.
 *  @param GUID The globally unique identifier for this FMU as understood by the master.
 *  @param modelGUID The globally unique identifier for this FMU as understood by this FMU.
 *  @param fmuResourceLocation A URI for the location of the unzipped FMU.
 *  @param functions The callback functions to allocate and free memory and log progress.
 *  @param visible Indicator of whether the FMU should run silently (fmiFalse) or interact
 *   with displays, etc. (fmiTrue) (ignored by this FMU).
 *  @param loggingOn Indicator of whether logging messages should be sent to the logger.
 *  @return The instance of this FMU, or null if there are required functions missing,
 *   if there is no instance name, or if the GUID does not match this FMU.
 */
#define checkFMU fmiFullName(checkFMU)
int FMIAPI checkFMU(
             fmiString instanceName,
             fmiString GUID,
             fmiString modelGUID,
             fmiString fmuResourceLocation,
             const fmiCallbackFunctions *functions,
             fmiBoolean visible,
             fmiBoolean loggingOn)  {
    /* fprintf(stderr, "checkFMU()\n"); */
    /* fprintf(stderr, "checkFMU() instanceName: %s \n", instanceName); */
    /* fprintf(stderr, "checkFMU() GUID: %s \n", GUID); */
    /* fprintf(stderr, "checkFMU() modelGUID: %s \n", modelGUID); */
    /* fprintf(stderr, "checkFMU() fmuResourceLocation: %s \n", fmuResourceLocation); */
    /* fprintf(stderr, "checkFMU() functions: %p\n", functions); */

    if (functions == NULL) {
        fprintf(stderr, "fmiInstantiateSlave: fmiCallbackFunctions is null?\n");
        return 0;
    }
    // Logger callback is required.
    if (!functions->logger) {
        return 0;
    }

    // fprintf(stderr, "checkFMU() functions->logger: %p\n", functions->logger);
    // Functions to allocate and free memory are required.
    if (!functions->allocateMemory || !functions->freeMemory) {
        fprintf(stderr, "fmiInstantiateSlave: Missing callback function?\n");
        functions->logger(NULL, instanceName, fmiError, "error",
                          "fmiInstantiateSlave: Missing callback function: freeMemory");
        return 0;
    }
    if (!instanceName || strlen(instanceName)==0) {
        fprintf(stderr, "fmiInstantiateSlave: Missing instance name\n");
        functions->logger(NULL, instanceName, fmiError, "error",
                          "fmiInstantiateSlave: Missing instance name.");
        return 0;
    }
    if (strcmp(GUID, modelGUID)) {
        // FIXME: Remove printfs. Replace with logger calls when they work.
        fprintf(stderr,"fmiInstantiateSlave: Wrong GUID %s. Expected %s.\n", GUID, modelGUID);
        fflush(stderr);
        //functions->logger(NULL, instanceName, fmiError, "error",
        //                  "fmiInstantiateSlave: Wrong GUID %s. Expected %s.", GUID, modelGUID);
        return 0;
    }
    //fprintf(stderr, "checkFMU() returning 1.\n");
    return 1;
}




/*****************************************************************************************
 *  Advance the state of this FMU from the current communication point to that point plus
 *  the specified step size.
 *  @param c The FMU.
 *  @param currentCommunicationPoint The time at the start of the step interval.
 *  @param communicationStepSize The width of the step interval.
 *  @param noSetFMUStatePriorToCurrentPoint True to assert that the master will not subsequently
 *   restore the state of this FMU or call fmiDoStep with a communication point less than the
 *   current one. An FMU may use this to determine that it is safe to take actions that have side
 *   effects, such as printing outputs. This FMU ignores this argument.
 *  @return fmiDiscard if the FMU rejects the step size, otherwise fmiOK.
 */
fmiStatus FMIAPI fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint,
            fmiReal communicationStepSize, fmiBoolean noSetFMUStatePriorToCurrentPoint) {
    ModelInstance* component = (ModelInstance *) c;

    // If current time is greater than period * (value + 1), then it is
    // time for another increment.
    double endOfStepTime = currentCommunicationPoint + communicationStepSize;
    double targetTime = TICK_PERIOD * (component->currentCount + 1);
    if (endOfStepTime >= targetTime - EPSILON) {
        // It is time for an increment.
        // Is it too late for the increment?
        if (endOfStepTime > targetTime + EPSILON) {
            // Indicate that the last successful time is
            // at the target time.
            component->lastSuccessfulTime = targetTime;
            fflush(stdout);
            return fmiDiscard;
        }
        // We are at the target time. Are we
        // ready for the increment yet? Have to have already
        // completed one firing at this time.
        if (component->atBreakpoint) {
            // Not the first firing. Go ahead an increment.
            component->currentCount++;
            // Update the state
            transition_function(c);
            // Reset the indicator that the increment is needed.
            component->atBreakpoint = fmiFalse;
        } else {
            // This will complete the first firing at the target time.
            // We don't want to increment yet, but we set an indicator
            // that we have had a firing at this time.
            fflush(stdout);
            component->atBreakpoint = fmiTrue;
        }
    }
    component->lastSuccessfulTime = endOfStepTime;
    fflush(stdout);
    return fmiOK;
}

/*****************************************************************************************
 *  Free memory allocated by this FMU instance.
 *  @param c The FMU.
 */
void FMIAPI fmiFreeSlaveInstance(fmiComponent c) {
    ModelInstance* component = (ModelInstance *) c;
    if (component != NULL) {
        if (component->functions != NULL) {
            if (component->functions->freeMemory != NULL) {
                component->functions->freeMemory(component);
            }
        }
    }
}

/*****************************************************************************************
 *  Get the maximum next step size.
 *  If the last call to fmiDoStep() incremented the counter, then the maximum step
 *  size is zero. Otherwise, it is the time remaining until the next increment of the count.
 *  @param c The FMU.
 *  @param maxStepSize A pointer to a real into which to write the result.
 *  @return fmiOK.
 */
fmiStatus FMIAPI fmiGetMaxStepSize(fmiComponent c, fmiReal *maxStepSize) {
    ModelInstance* component = (ModelInstance *) c;
    if (component->atBreakpoint) {
        *maxStepSize = 0.0;
    } else {
        double targetTime = TICK_PERIOD * (component->currentCount + 1);
        double step = targetTime - component->lastSuccessfulTime;
        *maxStepSize = step;
    }
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
fmiStatus FMIAPI fmiGetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiReal value[]) {
    int i, valueReference;
    ModelInstance* component = (ModelInstance *) c;

    output_function(c);

    for (i = 0; i < nvr; i++) {
        valueReference = vr[i];
        if (valueReference == LEVEL) {
          value[i] = component->level;
        }
    }
    return fmiOK;
}

/*****************************************************************************************
 *  Get the specified FMU status. This procedure only provides status kind
 *  fmiLastSuccessfulTime. All other requests result in returning fmiDiscard.
 *  @param c The FMU.
 *  @param s The kind of status to return, which must be fmiLastSuccessfulTime.
 *  @param value A pointer to the location in which to deposit the status.
 *  @return fmiDiscard if the kind is not fmiLastSuccessfulTime, otherwise fmiOK.
 */
fmiStatus FMIAPI fmiGetRealStatus(fmiComponent c, const fmiStatusKind s, fmiReal* value) {
    ModelInstance* component = (ModelInstance *) c;
    if (s == fmiLastSuccessfulTime) {
        *value = component->lastSuccessfulTime;

        printf("fmiGetRealStatus returns lastSuccessfulTime is %g\n", *value);
        fflush(stdout);

        return fmiOK;
    }
    // Since this FMU does not return fmiPending, there shouldn't be other queries of status.
    return fmiDiscard;
}

/*****************************************************************************************
 *  Create an instance of this FMU.
 *  @param instanceName The name of the instance.
 *  @param GUID The globally unique identifier for this FMU.
 *  @param fmuResourceLocation A URI for the location of the unzipped FMU.
 *  @param functions The callback functions to allocate and free memory and log progress.
 *  @param visible Indicator of whether the FMU should run silently (fmiFalse) or interact
 *   with displays, etc. (fmiTrue) (ignored by this FMU).
 *  @param loggingOn Indicator of whether logging messages should be sent to the logger.
 *  @return The instance of this FMU, or null if there are required functions missing,
 *   if there is no instance name, or if the GUID does not match this FMU.
 */
fmiComponent FMIAPI fmiInstantiateSlave(
                                 fmiString instanceName,
                                 fmiString GUID,
                                 fmiString fmuResourceLocation,
                                 const fmiCallbackFunctions *functions,
                                 fmiBoolean visible,
                                 fmiBoolean loggingOn)  {
    ModelInstance* component;

    // Perform checks.
    if (!checkFMU(instanceName, GUID, MODEL_GUID, fmuResourceLocation, functions, visible, loggingOn)) {
        return NULL;
    }
    component = (ModelInstance *)functions->allocateMemory(1, sizeof(ModelInstance));
    component->currentCount = 0.0;
    component->lastSuccessfulTime = -1.0;
    component->atBreakpoint = fmiFalse;
    component->functions = functions;

    // Need to allocate memory and copy the string because JNA stores the string
    // in a temporary buffer that gets GC'd.
    component->instanceName = (char*)functions->allocateMemory(1 + strlen(instanceName), sizeof(char));
    strcpy((char *)component->instanceName, instanceName);

    // FIXME: Use logger instead when this works.
    // functions->logger(component, instanceName, fmiOK, "message",
    //                  "Invoked fmiInstantiateSlave for instance %s.", instanceName);
    printf("%s: Invoked fmiInstantiateSlave.\n", component->instanceName);
    fflush(stdout);

    return component;
}

/*****************************************************************************************
 *  Initialize the FMU for co-simulation.
 *  @param c The FMU.
 *  @param relativeTolerance Suggested (local) tolerance in case the slave utilizes a
 *   numerical integrator with variable step size and error estimation (ignored by this FMU).
 *  @param tStart The start time (ignored by this FMU).
 *  @param stopTimeDefined fmiTrue to indicate that the stop time is defined (ignored by this FMU).
 *  @param tStop The stop time (ignored if stopTimeDefined is fmiFalse) (ignored by this FMU).
 *  @return fmiOK
 */
fmiStatus FMIAPI fmiInitializeSlave(fmiComponent c,
                             fmiReal relativeTolerance,
                             fmiReal tStart,
                             fmiBoolean stopTimeDefined,
                             fmiReal tStop) {

    ModelInstance* component = (ModelInstance *) c;
    // FIXME: Use logger instead.
    // component->functions->logger(c, component->instanceName, fmiOK, "message",
    //                             "Invoked fmiIntializeSlave: start: %g, StopTimeDefined: %d, tStop: %g.",
    //                             tStart, stopTimeDefined, tStop);
    printf("%s: Invoked fmiIntializeSlave: start: %g, StopTimeDefined: %d, tStop: %g..\n",
           component->instanceName, tStart, stopTimeDefined, tStop);
    fflush(stdout);

    component->lastSuccessfulTime = tStart;
    component->atBreakpoint = fmiFalse;
    init_state(c);

    return fmiOK;
}

/*****************************************************************************************
 *  Set the specified real values.
 *  @param c The FMU.
 *  @param vr An array of indexes of the real variables to be set (value references).
 *  @param nvr The number of values to be set (the length of the array vr).
 *  @param value The values to assign to these variables.
 *  @return fmiError if a value reference is out of range, otherwise fmiOK.
 */
fmiStatus FMIAPI fmiSetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiReal value[]){
    int i, valueReference;

    ModelInstance* component = (ModelInstance *) c;
    for (i = 0; i < nvr; i++) {
        valueReference = vr[i];
        if(valueReference == REFERENCE){
            component->reference = value[i];
        }
    }
    return fmiOK;
}

/*****************************************************************************************
 *  Terminate this FMU. This does nothing, since this FMU is passive.
 *  @param c The FMU.
 *  @return fmiOK if the FMU was non-null, otherwise return fmiError
 */
fmiStatus FMIAPI fmiTerminateSlave(fmiComponent c) {
    ModelInstance* component = (ModelInstance *) c;

    if (component == NULL) {
        printf("fmiTerminateSlave called with a null argument?  This can happen while exiting during a failure to construct the component\n");
        fflush(stdout);
        return fmiError;
    } else {
        printf("%s: fmiTerminateSlave\n", component->instanceName);
        fflush(stdout);
    }

    return fmiOK;
}
