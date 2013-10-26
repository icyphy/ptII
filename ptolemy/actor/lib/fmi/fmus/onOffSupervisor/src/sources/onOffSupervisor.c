/* ---------------------------------------------------------------------------*
 * Co-simulation FMU for a simple discrete supervisory controller.
 *
 * This FMU has two boolean valued inputs and one output.
 * It implements a simple state machine with two states, on and off.
 * It starts in the off state.
 * In the off state, if the onOff input is true and the fault input is false, then
 * it transitions to the the on state and outputs a true.
 * Otherwise, it outputs false.
 * In the on state, if the onOff input is false or the fault input is true,
 * then it transitions to the off state and outputs false.
 * Otherwise, it outputs true.
 *
 * To build the FMU file, do this:
 *
 *  > cd $PTII/ptolemy/actor/lib/fmi/fmus
 *  > make
 *
 * The resulting .fmu file will be
 * be in $PTII/actor/lib/fmi/fmus/onOffSupervisor/onOffSupervisor.fmu
 *
 * To run: import the FMU file into Ptolemy and build a model.
 *
 * Note that this file will not work with model exchange.
 *
 * Authors: Christopher Brooks and Edward A. Lee
 * ---------------------------------------------------------------------------*/
#include <stdio.h>
#include <string.h>

// The model identifier string.
#define MODEL_IDENTIFIER onOffSupervisor
// Globally unique ID used to make sure the XML file and the DLL match.
// The following was generated at http://guid.us
#define MODEL_GUID "{7081763b-e37f-404a-967a-9f6fe0fc6072}"

// Used by FMI 2.0.  See FMIFuctions.h
//#define FMIAPI_FUNCTION_PREFIX onOffSupervisor_
#ifdef _MSC_VER
#define FMIAPI __declspec( dllexport )
#else
#define FMIAPI
#endif

// FIXME: Don't use the logger, it causes the JVM to exit.
#define USE_LOGGER 0

// include fmu header files, typedefs and macros
#include "fmiFunctions.h"

/*****************************************************************************************
 * Data structure for an instance of this FMU.
 */
typedef struct {
    fmiBoolean state;       // The current state (false for off, true for on).
    fmiBoolean onOff;       // The current onOff input.
    fmiBoolean fault;       // The current fault input.
    const fmiCallbackFunctions *functions;
    fmiString instanceName;
} ModelInstance;

// Indexes of instance variables that can be set and gotten by the master.
#define OUTPUT 0
#define ONOFF 1
#define FAULT 2


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
int checkFMU(
             fmiString instanceName,
             fmiString GUID,
             fmiString modelGUID,
             fmiString fmuResourceLocation,
             const fmiCallbackFunctions *functions,
             fmiBoolean visible,
             fmiBoolean loggingOn)  {
    // Logger callback is required.
    if (!functions->logger) {
        return 0;
    }
    // Functions to allocate and free memory are required.
    if (!functions->allocateMemory || !functions->freeMemory) {
#if USE_LOGGER
        functions->logger(NULL, instanceName, fmiError, "error",
                          "fmiInstantiateSlave: Missing callback function: allocateMemory or freeMemory.");
#else
        fprintf(stderr, "fmiInstantiateSlave: Missing callback function: allocateMemory or freeMemory.\n");
        fflush(stderr);
#endif
        return 0;
    }
    if (!instanceName || strlen(instanceName)==0) {
#if USE_LOGGER
        functions->logger(NULL, instanceName, fmiError, "error",
#else
        fprintf(stderr, "fmiInstantiateSlave: Missing instance name.\n");
        fflush(stderr);
#endif
        return 0;
    }
    if (strcmp(GUID, MODEL_GUID)) {
#if USE_LOGGER
        functions->logger(NULL, instanceName, fmiError, "error",
                          "fmiInstantiateSlave: Wrong GUID %s. Expected %s.", GUID, MODEL_GUID);
#else
        fprintf(stderr, "fmiInstantiateSlave: Wrong GUID\n");
        fflush(stderr);
#endif

        return 0;
    }
    return 1;
}

/*****************************************************************************************
 *  Advance the state of this FMU from the current communication point to that point plus
 *  the specified step size. This state machine has no timed behavior; it just reacts to the
 *  current inputs. So this procedure uses the current inputs to set the new state.
 *  @param c The FMU.
 *  @param currentCommunicationPoint The time at the start of the step interval.
 *  @param communicationStepSize The width of the step interval.
 *  @param noSetFMUStatePriorToCurrentPoint True to assert that the master will not subsequently
 *   restore the state of this FMU or call fmiDoStep with a communication point less than the
 *   current one. An FMU may use this to determine that it is safe to take actions that have side
 *   effects, such as printing outputs.
 *  @return fmiOK.
 */
fmiStatus fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint,
            fmiReal communicationStepSize, fmiBoolean noSetFMUStatePriorToCurrentPoint) {
    ModelInstance* component = (ModelInstance *) c;
    if (component->state) {
        // Current state is "on".
        if (component->fault || !component->onOff) {
            component->state = fmiFalse;
        }
    } else {
        // Current state is "off".
        if (!component->fault && component->onOff) {
            component->state = fmiTrue;
        }
    }
    /*
    printf("%s: Invoked fmiDoStep and updated state to %d\n",
           component->instanceName,
           component->state);
    fflush(stdout);
    */

    return fmiOK;
}

/*****************************************************************************************
 *  Free memory allocated by this FMU instance.
 *  @param c The FMU.
 */
void fmiFreeSlaveInstance(fmiComponent c) {
    ModelInstance* component = (ModelInstance *) c;
    component->functions->freeMemory(component);
}

/*****************************************************************************************
 *  Get the values of the specified boolean variables.
 *  @param c The FMU.
 *  @param vr An array of value references (indices) for the desired values.
 *  @param nvr The number of values desired (the length of vr).
 *  @param value The array into which to put the results.
 *  @return fmiError if a value reference is out of range, otherwise fmiOK.
 */
fmiStatus fmiGetBoolean(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiBoolean value[]) {
    int i, valueReference;
    ModelInstance* component = (ModelInstance *) c;

    for (i = 0; i < nvr; i++) {
        valueReference = vr[i];
        if (valueReference == OUTPUT) {
            // The request is for the current value of the output.
            // Note that it is important that this code not change the
            // state of the state machine so that this FMU will work in
            // a continuous or synchronous-reactive model of computation
            // without having to provide procedures to get and set state
            // (supporting rollback). Note that the logic here duplicates
            // that realized in fmiDoStep(), but that duplication is
            // unavoidable to avoid having to provide rollback.
            if (component->state) {
                // Current state is "on".
                if (component->fault || !component->onOff) {
                    // A state transition is enabled and will be taken in fmiDoStep.
                    value[i] = fmiFalse;
                } else {
                    // A state transition is not enabled.
                    value[i] = fmiTrue;
                }
            } else {
                // Current state is "off".
                if (!component->fault && component->onOff) {
                    // A state transition is enabled and will be taken in fmiDoStep.
                    value[i] = fmiTrue;
                } else {
                    // A state transition is not enabled.
                    value[i] = fmiFalse;
                }
            }
        } else if (valueReference == ONOFF) {
            value[i] = component->onOff;
        } else if (valueReference == FAULT) {
            value[i] = component->fault;
        } else {
#if USE_LOGGER
            component->functions->logger(component, component->instanceName, fmiError, "error",
                                             "fmiGetBoolean: Value reference out of range: %u.", nvr);
#else
            fprintf(stderr, "fmiGetBoolean: Value referenced out of range.\n");
            fflush(stderr);
#endif
            return fmiError;
        }
        /*
        printf("%s: Invoked fmiGetBoolean on index %d, which has value %d\n",
                component->instanceName,
                valueReference,
                value[i]);
                // (value[i])?"true":"false");
        fflush(stdout);
        */
    }
    return fmiOK;
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
fmiComponent fmiInstantiateSlave(
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
    component->state = fmiFalse;
    component->onOff = fmiFalse;
    component->fault = fmiFalse;
    component->functions = functions;
    component->instanceName = instanceName;

    if (loggingOn) {
#if USE_LOGGER
        functions->logger(component, instanceName, fmiOK, "message",
                      "Invoked fmiInstantiateSlave for instance %s.", instanceName);
#else
        fprintf(stderr, "Invoked fmiInstantiateSlave.\n");
        fflush(stderr);
#endif
    }

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
fmiStatus fmiInitializeSlave(fmiComponent c,
                             fmiReal relativeTolerance,
                             fmiReal tStart,
                             fmiBoolean stopTimeDefined,
                             fmiReal tStop) {
    ModelInstance* component = (ModelInstance *) c;

#if USE_LOGGER
    component->functions->logger(c, component->instanceName, fmiOK, "message",
                                 "Invoked fmiIntializeSlave: start: %g, StopTimeDefined: %d, tStop: %g.",
                                 tStart, stopTimeDefined, tStop);
#else
    fprintf(stderr, "Invoked fmiInitializeSlave.\n");
    fflush(stderr);
#endif

    component->state = fmiFalse;
    component->onOff = fmiFalse;
    component->fault = fmiFalse;
    return fmiOK;
}

/*****************************************************************************************
 *  Set the specified boolean values.
 *  @param c The FMU.
 *  @param vr An array of indexes of the variables to be set (value references).
 *  @param nvr The number of values to be set (the length of the array vr).
 *  @param value The values to assign to these variables.
 *  @return fmiError if a value reference is out of range, otherwise fmiOK.
 */
fmiStatus fmiSetBoolean(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiBoolean value[]){
    int i, valueReference;
    ModelInstance* component = (ModelInstance *) c;
    for (i = 0; i < nvr; i++) {
        valueReference = vr[i];

        /*
        printf("%s: Setting boolean value with index %d and value %s.\n", component->instanceName,
                valueReference, (value[i])?"true":"false");
        fflush(stdout);
        */

        if (valueReference == ONOFF) {
            component->onOff = value[i];
        } else if (valueReference == FAULT) {
            component->fault = value[i];
        } else {
#if USE_LOGGER
            component->functions->logger(component, component->instanceName, fmiError, "error",
                        "fmiGetBoolean: Value reference out of range: %u.", valueReference);
#else
            fprintf(stderr, "fmiGetBoolean: Value reference out of range\n");
            fflush(stderr);
#endif
            return fmiError;
        }
    }
    return fmiOK;
}

/*****************************************************************************************
 *  Terminate this FMU. This does nothing, since this FMU is passive.
 *  @param c The FMU.
 *  @return fmiOK.
 */
fmiStatus fmiTerminateSlave(fmiComponent c) {
    return fmiOK;
}
