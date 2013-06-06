/* ---------------------------------------------------------------------------*
 * Test Co-simulation FMU conformant with an extension of FMI 2.0 beta 5.
 *
 * This FMU has one output that produces a piecewise
 * constant signal that starts at value 0.0 and increments by 1.0 every p
 * time units, where p is a parameter. It indicates to the orchestrator to
 * execute twice at the time of each step increment by setting the
 * capability flag canProvideNextEventTime="true" and implementing
 * a method missing from the FMI standard:
 *
 *   fmiStatus  fmiGetMaxStepSize(fmiComponent c, fmiReal *maxStepSize);
 *
 * This FMU tests the following features of FMI:
 *  - an FMU that controls step sizes via the above extension to the standard.
 *  - an FMU that requires a zero step size.
 *
 * To build the FMU file, do this:
 *
 *  > cd $PTII/ptolemy/actor/lib/fmi/fmus
 *  > make
 *
 * The resulting .fmu file will be
 * be in $PTII/actor/lib/fmi/fmus/stepCounterExtendedFMI/stepCounterExtendedFMI.fmu
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
#define MODEL_IDENTIFIER stepCounterExtendedFMI
// Globally unique ID used to make sure the XML file and the DLL match.
// The following was generated at http://guid.us
#define MODEL_GUID "{381b81b5-3ccd-4abd-8fb0-f1434b7d8643}"

// Used by FMI 2.0.  See FMIFuctions.h
#define FMIAPI_FUNCTION_PREFIX stepCounterExtendedFMI_
#define FMIAPI

// include fmu header files, typedefs and macros
#include "fmiFunctions.h"

/*****************************************************************************************
 * Data structure for an instance of this FMU.
 */
typedef struct {
    // Pointer to the state variables of this FMU, which are all reals.
    fmiReal currentCount;       // The current count (the output).
    fmiReal period;             // The period parameter value.
    fmiReal lastSuccessfulTime; // The time to which this FMU has advanced.
    fmiBoolean atBreakpoint;    // Indicator that the first output at a step time has been produced.
    fmiReal relativeTolerance;  // The tolerance specifying accuracy of time of transition.
    const fmiCallbackFunctions *functions;
    fmiString instanceName;
} ModelInstance;

// Indexes of instance variables that can be set and gotten by the master.
#define CURRENT_COUNT 0
#define PERIOD 1


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
        functions->logger(NULL, instanceName, fmiError, "error",
                          "fmiInstantiateSlave: Missing callback function: freeMemory");
        return 0;
    }
    if (!instanceName || strlen(instanceName)==0) {
        functions->logger(NULL, instanceName, fmiError, "error",
                          "fmiInstantiateSlave: Missing instance name.");
        return 0;
    }
    if (strcmp(GUID, model_GUID)) {
        // FIXME: Remove printfs. Replace with logger calls when they work.
        fprintf(stderr,"fmiInstantiateSlave: Wrong GUID %s. Expected %s.\n", GUID, model_GUID);
	fflush(stderr);
        //functions->logger(NULL, instanceName, fmiError, "error",
        //                  "fmiInstantiateSlave: Wrong GUID %s. Expected %s.", GUID, model_GUID);
        return 0;
    }
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
fmiStatus fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint,
            fmiReal communicationStepSize, fmiBoolean noSetFMUStatePriorToCurrentPoint) {
    ModelInstance* component = (ModelInstance *) c;
    // FIXME: Remove printfs. Replace with logger calls when they work.
    printf("%s: Invoked fmiDoStep: %g, %g, noSetFMUStatePriorToCurrentPoint: %s\n", component->instanceName,
           currentCommunicationPoint,
           communicationStepSize,
           (noSetFMUStatePriorToCurrentPoint)?"true":"false");
    fflush(stdout);

    // If current time is greater than period * (value + 1), then it is
    // time for another increment.
    double endOfStepTime = currentCommunicationPoint + communicationStepSize;
    double targetTime = component->period * (component->currentCount + 1);
    if (endOfStepTime >= targetTime - component->relativeTolerance) {
        // It is time for an increment.
        // Is it too late for the increment?
        if (endOfStepTime > targetTime + component->relativeTolerance) {
            // Indicate that the last successful time is
            // at the target time.
            component->lastSuccessfulTime = targetTime;
            printf("%s: Discarding step. endOfStepTime = %g, targetTime = %g, atBreakpoint = %s\n",
                   component->instanceName, endOfStepTime, targetTime, component->atBreakpoint?"true":"false");
            fflush(stdout);
            return fmiDiscard;
        }
        // We are at the target time. Are we
        // ready for the increment yet? Have to have already
        // completed one firing at this time.
        if (component->atBreakpoint) {
            // Not the first firing. Go ahead an increment.
            component->currentCount++;
            printf("%s: Incrementing count to %g\n", component->instanceName, component->currentCount);
            fflush(stdout);
            // Reset the indicator that the increment is needed.
            component->atBreakpoint = fmiFalse;
        } else {
            // This will complete the first firing at the target time.
            // We don't want to increment yet, but we set an indicator
            // that we have had a firing at this time.
            printf("%s: At time for count to increment, but leaving at %g\n",
                   component->instanceName, component->currentCount);
            fflush(stdout);
            component->atBreakpoint = fmiTrue;
        }
    }
    component->lastSuccessfulTime = endOfStepTime;
    printf("%s: fmiDoStep succeeded.\n", component->instanceName);
    fflush(stdout);
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
 *  Get the maximum next step size.
 *  If the last call to fmiDoStep() incremented the counter, then the maximum step
 *  size is zero. Otherwise, it is the time remaining until the next increment of the count.
 *  @param c The FMU.
 *  @param maxStepSize A pointer to a real into which to write the result.
 *  @return fmiOK.
 */
fmiStatus  fmiGetMaxStepSize(fmiComponent c, fmiReal *maxStepSize) {
    ModelInstance* component = (ModelInstance *) c;
    if (component->atBreakpoint) {
        *maxStepSize = 0.0;
    } else {
        double targetTime = component->period * (component->currentCount + 1);
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
fmiStatus fmiGetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiReal value[]) {
    int i, valueReference;
    ModelInstance* component = (ModelInstance *) c;

    for (i = 0; i < nvr; i++) {
        valueReference = vr[i];
        if (valueReference == PERIOD) {
            value[i] = component->period;
        } else if (valueReference == CURRENT_COUNT) {
            value[i] = component->currentCount;
        } else {
            // FIXME: Use logger instead when this works.
            // component->functions->logger(component, component->instanceName, fmiError, "error",
            //                                 "fmiGetReal: Value reference out of range: %u.", nvr);
            printf("%s: fmiGetReal: Value reference out of range: %lu.\n", component->instanceName, nvr);
            fflush(stdout);

            return fmiError;
        }
        printf("%s: Invoked fmiGetReal on index %d, which has value %g\n",
               component->instanceName,
               valueReference,
               value[i]);
        fflush(stdout);
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
fmiStatus fmiGetRealStatus(fmiComponent c, const fmiStatusKind s, fmiReal* value) {
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
    component->currentCount = 0.0;
    component->period = 1.0;
    component->lastSuccessfulTime = -1.0;
    component->atBreakpoint = fmiFalse;
    component->functions = functions;
    
    // Need to allocate memory and copy the string because JNA stores the string
    // in a temporary buffer that gets GC'd.
    component->instanceName = (char*)functions->allocateMemory(1 + strlen(instanceName), sizeof(char));
    strcpy(component->instanceName, instanceName);

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
fmiStatus fmiInitializeSlave(fmiComponent c,
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
    component->relativeTolerance = relativeTolerance;
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
fmiStatus fmiSetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiReal value[]){
    int i, valueReference;

    ModelInstance* component = (ModelInstance *) c;
    for (i = 0; i < nvr; i++) {
        valueReference = vr[i];

        printf("%s: Setting real value with index %d and value %g.\n", component->instanceName,
               valueReference, value[i]);
        fflush(stdout);

        if (valueReference == PERIOD) {
            component->period = value[i];
        } else if (valueReference == CURRENT_COUNT) {
            component->currentCount = value[i];
        } else {
            // FIXME: Use logger instead.
            // component->functions->logger(component, component->instanceName, fmiError, "error",
            //            "fmiGetReal: Value reference out of range: %u.", valueReference);
            printf("%s: fmiGetReal: Value reference out of range: %u.\n",
                   component->instanceName, valueReference);
            fflush(stdout);

            return fmiError;
        }
    }
    return fmiOK;
}

/*****************************************************************************************
 *  Terminate this FMU. This does nothing, since this FMU is passive.
 *  @param c The FMU.
 *  @return fmiOK if the FMU was non-null, otherwise return fmiError
 */
fmiStatus fmiTerminateSlave(fmiComponent c) {
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
