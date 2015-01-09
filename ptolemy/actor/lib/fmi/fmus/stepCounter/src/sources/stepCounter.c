/* ---------------------------------------------------------------------------*
 * Test Co-simulation FMU conformant with FMI 2.0 beta 4.
 *
 * This FMU has one output that produces a piecewise
 * constant signal that starts at value 0.0 and increments by 1.0 every p
 * time units, where p is a parameter. It forces the orchestrator to
 * execute twice at the time of each step increment by discarding an fmiDoStep()
 * that steps over the transition time and then suggesting a step size that
 * hits the transition time, and then discarding any step size greater than 0
 * for the next iteration, so that it fires twice at the transition time.
 *
 * This FMU tests the following features of FMI:
 *  - an FMU that rejects step sizes.
 *  - an FMU that requires a zero step size.
 *  - functions that get and set state, and canGetAndSetFMUstate attribute.
 *
 * This FMU is designed to work with rollback.
 *
 * To build the FMU file, do this:
 *
 *  > cd $PTII/ptolemy/actor/lib/fmi/fmus
 *  > make
 *
 * The resulting .fmu file will be
 * be in $PTII/actor/lib/fmi/fmus/stepCounter/stepCounter.fmu
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
#define MODEL_IDENTIFIER stepCounter
// Globally unique ID used to make sure the XML file and the DLL match.
// The following was generated at http://guid.us
#define MODEL_GUID "{136ff03f-fb93-4a90-bb88-8dbf92948dde}"

// Used by FMI 2.0.  See fmiFunctions.h
//#define FMIAPI_FUNCTION_PREFIX stepCounter_
//#define FMIAPI

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
    fmiBoolean loggingOn;
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
int FMIAPI checkFMU(
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
    if (strcmp(GUID, modelGUID)) {
        functions->logger(NULL, instanceName, fmiError, "error",
                          "fmiInstantiateSlave: Wrong GUID %s. Expected %s.", GUID, modelGUID);
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
 *   effects, such as printing outputs.
 *  @return fmiDiscard if the FMU rejects the step size, otherwise fmiOK.
 */
fmiStatus FMIAPI fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint,
            fmiReal communicationStepSize, fmiBoolean noSetFMUStatePriorToCurrentPoint) {
    ModelInstance* component = (ModelInstance *) c;

    if (component->loggingOn) {
        component->functions->logger(component, component->instanceName, fmiOK, "message",
                "Invoked fmiDoStep: %g, %g, noSetFMUStatePriorToCurrentPoint: %s",
                currentCommunicationPoint,
                communicationStepSize,
                (noSetFMUStatePriorToCurrentPoint)?"true":"false");
    }
    // The following is extremely tricky.
    // If a step is being restarted, then we have to reset the
    // indicator that we have reached the time of the next incrment.
    // The following test does that, but it relies on the orchestrator
    // to correctly call this method with noSetFMUStatePriorToCurrentPoint == false each time
    // there is a rollback in time, and only when there is a rollback
    // in time. Seems not very robust.
    if (!noSetFMUStatePriorToCurrentPoint) {
        component->atBreakpoint = fmiFalse;
    }
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
            if (component->loggingOn) {
                component->functions->logger(component, component->instanceName, fmiOK, "message",
                        "Discarding step. endOfStepTime = %g, targetTime = %g, atBreakpoint = %s",
                        endOfStepTime, targetTime, component->atBreakpoint?"true":"false");
            }
            return fmiDiscard;
        }
        // We are at the target time. Are we
        // ready for the increment yet? Have to have already
        // completed one firing at this time.
        // FIXME: Unfortunately, with the Pt II solver, this is not sufficient
        // because it will appear as if there were a zero-step-size increment,
        // but actually there hasn't been. The Pt II solver fires the actor at
        // end of a time interval, then postfires it so its outputs become visible.
        // But then it fires it again at the beginning of the next interval, which
        // is the same communication point, so it will appear to this FMU that it
        // is seeing a zero step size.
        // But the firing at the beginning of the next iterval does not produce any
        // outputs because the actor does not get postfired then.
        // So outputs produced at this microstep 1 will not be visible to,
        // for example, a plotter, which looks for inputs in postfire.
        // Maybe the Continuous director can only use model-exchange FMUs.
        if (component->atBreakpoint) {
            // Not the first firing. Go ahead an increment.
            component->currentCount++;
            if (component->loggingOn) {
                component->functions->logger(component, component->instanceName, fmiOK, "message",
                        "Incrementing count to %g", component->currentCount);
            }
            // Reset the indicator that the increment is needed.
            component->atBreakpoint = fmiFalse;
        } else {
            // This will complete the first firing at the target time.
            // We don't want to increment yet, but we set an indicator
            // that we have had a firing at this time.
            if (component->loggingOn) {
                component->functions->logger(component, component->instanceName, fmiOK, "message",
                        "At time for count to increment, but leaving at %g",
                        component->currentCount);
            }
            component->atBreakpoint = fmiTrue;
        }
    }
    component->lastSuccessfulTime = endOfStepTime;
    if (component->loggingOn) {
        component->functions->logger(component, component->instanceName, fmiOK, "message",
                "fmiDoStep succeeded.");
    }
    return fmiOK;
}

/*****************************************************************************************
 *  Free the memory used to store a snapshot of the FMU.
 *  If *FMUstate is null, ignore this call.
 *  @param c The FMU.
 *  @param FMUstate A pointer to a pointer to the data structure into which the state is stored.
 *  @return fmiOK.
 */
fmiStatus FMIAPI fmiFreeFMUstate (fmiComponent c, fmiFMUstate* FMUstate) {
    ModelInstance* component = (ModelInstance *) c;
    ModelInstance* snapshot = *FMUstate;
    if (snapshot != NULL) {
        component->functions->freeMemory(snapshot);
        *FMUstate = NULL;
    }
    return fmiOK;
}

/*****************************************************************************************
 *  Free memory allocated by this FMU instance.
 *  @param c The FMU.
 */
void FMIAPI fmiFreeSlaveInstance(fmiComponent c) {
    ModelInstance* component = (ModelInstance *) c;
    component->functions->freeMemory(component);
}

/*****************************************************************************************
 *  Record the current state of the FMU.
 *  If *FMUstate is null, then allocate memory (using
 *  the allocateMemory() callback) and store the current state of
 *  this FMU in it, and set *FMUstate to point to that memory.
 *  If *FMUstate is not null, then store the state of this FMU
 *  in the memory pointed to.
 *  @param c The FMU.
 *  @param FMUstate A pointer to a pointer to the data structure into which to store the state.
 *  @return fmiOK.
 */
fmiStatus FMIAPI fmiGetFMUstate (fmiComponent c, fmiFMUstate* FMUstate) {
    ModelInstance* component = (ModelInstance *) c;
    ModelInstance* snapshot = *FMUstate;
    if (snapshot == NULL) {
        // No previously allocated memory for the snapshot. Allocate it.
        snapshot = (ModelInstance *)component->functions->allocateMemory(1, sizeof(ModelInstance));
        *FMUstate = snapshot;
    }
    snapshot->currentCount = component->currentCount;
    snapshot->period = component->period;
    snapshot->lastSuccessfulTime = component->lastSuccessfulTime;
    snapshot->atBreakpoint = component->atBreakpoint;
    snapshot->relativeTolerance = component->relativeTolerance;
    snapshot->functions = component->functions;
    snapshot->instanceName = component->instanceName;

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

    for (i = 0; i < nvr; i++) {
        valueReference = vr[i];
        if (valueReference == PERIOD) {
            value[i] = component->period;
        } else if (valueReference == CURRENT_COUNT) {
            value[i] = component->currentCount;
        } else {
            component->functions->logger(component, component->instanceName, fmiError, "error",
                    "fmiGetReal: Value reference out of range: %u.", nvr);
            return fmiError;
        }
        if (component->loggingOn) {
            component->functions->logger(component, component->instanceName, fmiOK, "message",
                    "Invoked fmiGetReal on index %d, which has value %g",
                    valueReference,
                    value[i]);
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

        if (component->loggingOn) {
            component->functions->logger(component, component->instanceName, fmiOK, "message",
                    "fmiGetRealStatus returns lastSuccessfulTime is %g", *value);
        }

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
    component->period = 1.0;
    component->lastSuccessfulTime = -1.0;
    component->atBreakpoint = fmiFalse;
    component->functions = functions;
    component->loggingOn = loggingOn;

    // Need to allocate memory and copy the string because JNA stores the string
    // in a temporary buffer that gets GC'd.
    component->instanceName = (char*)functions->allocateMemory(1 + strlen(instanceName), sizeof(char));
    strcpy((char*)component->instanceName, instanceName);

    if (component->loggingOn) {
        functions->logger(component, instanceName, fmiOK, "message",
                "Invoked fmiInstantiateSlave for instance %s.", instanceName);
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
fmiStatus FMIAPI fmiInitializeSlave(fmiComponent c,
                             fmiReal relativeTolerance,
                             fmiReal tStart,
                             fmiBoolean stopTimeDefined,
                             fmiReal tStop) {

    ModelInstance* component = (ModelInstance *) c;
    if (component->loggingOn) {
        component->functions->logger(c, component->instanceName, fmiOK, "message",
                "Invoked fmiIntializeSlave: start: %g, StopTimeDefined: %d, tStop: %g.",
                tStart, stopTimeDefined, tStop);
    }

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
fmiStatus FMIAPI fmiSetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiReal value[]){
    int i, valueReference;

    ModelInstance* component = (ModelInstance *) c;
    for (i = 0; i < nvr; i++) {
        valueReference = vr[i];

        if (component->loggingOn) {
            component->functions->logger(component, component->instanceName, fmiOK, "message",
                    "Setting real value with index %d and value %g.",
                    valueReference, value[i]);
        }

        if (valueReference == PERIOD) {
            component->period = value[i];
        } else if (valueReference == CURRENT_COUNT) {
            component->currentCount = value[i];
        } else {
            component->functions->logger(component, component->instanceName, fmiError, "error",
                    "fmiGetReal: Value reference out of range: %u.", valueReference);
            return fmiError;
        }
    }
    return fmiOK;
}

/*****************************************************************************************
 *  Restore the current state of the FMU from the specified data structure.
 *  @param c The FMU.
 *  @param FMUstate A pointer to the data structure from which to restore.
 *  @return fmiOK.
 */
fmiStatus FMIAPI fmiSetFMUstate (fmiComponent c, fmiFMUstate FMUstate) {

    ModelInstance* component = (ModelInstance *) c;
    ModelInstance* snapshot = (ModelInstance *) FMUstate;

    if (component->loggingOn) {
        component->functions->logger(component, component->instanceName, fmiOK, "message",
                "fmiSetFMUState");
    }

    component->currentCount = snapshot->currentCount;
    component->period = snapshot->period;
    component->lastSuccessfulTime = snapshot->lastSuccessfulTime;
    component->atBreakpoint = snapshot->atBreakpoint;
    component->relativeTolerance = snapshot->relativeTolerance;
    component->functions = snapshot->functions;
    component->instanceName = snapshot->instanceName;

    return fmiOK;
}

/*****************************************************************************************
 *  Terminate this FMU. This does nothing, since this FMU is passive.
 *  @param c The FMU.
 *  @return fmiOK.
 */
fmiStatus FMIAPI fmiTerminateSlave(fmiComponent c) {
    ModelInstance* component = (ModelInstance *) c;

    if (component->loggingOn) {
        component->functions->logger(component, component->instanceName, fmiOK, "message",
                "fmiTerminateSlave");
    }

    return fmiOK;
}
