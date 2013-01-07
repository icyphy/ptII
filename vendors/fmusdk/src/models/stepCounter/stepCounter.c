/* ---------------------------------------------------------------------------*
 * Test Co-simulation FMU. This FMU has one output that produces a piecewise
 * constant signal that starts at value 0.0 and increments by 1.0 every p
 * time units, where p is a parameter. It forces the orchestrator to
 * execute twice at the time of each step increment by discarding an fmiDoStep()
 * that steps over the transition time and then suggesting a step size that
 * hits the transition time, and then discarding any step size greater than 0
 * for the next iteration, so that it fires twice at the transition time.
 *
 * To build the FMU file, do this:
 *
 *  > cd $PTII/vendors/fmusdk/src/models
 *  > make
 *
 * The resulting .fmu file for cosimulation will
 * be in $PTII/vendors/fmusdk/fmu/cs.
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

// include fmu header files, typedefs and macros
#include "fmiFunctions.h"

// Data structure for an instance of this FMU. */
typedef struct {
    // cxh: Use a pointer to a fmiReal so that we can allocate space for it.
    // cxh: call this 'r' instead of 'value' so it works with model exchange.
    // eal: FIXME: But we don't want it to work with model exchange.
    fmiReal    *r;
    fmiCallbackFunctions functions;
    fmiString instanceName;
} ModelInstance;

// Globally unique ID used to make sure the XML file and the DLL match.
// The following was generated at http://guid.us
#define MODEL_GUID "{c157b371-f7d4-4133-8c29-8e78a9468674}"

fmiComponent fmiInstantiateSlave(fmiString  instanceName, fmiString  GUID,
    	fmiString  fmuLocation, fmiString  mimeType, fmiReal timeout, fmiBoolean visible,
    	fmiBoolean interactive, fmiCallbackFunctions functions, fmiBoolean loggingOn) {
    ModelInstance* component;
    
    // Perform checks.
    // FIXME: Boilerplate below is shared among all test FMUs. Consolidate to one file.
    // Logger callback is required.
    if (!functions.logger) {
        return NULL;
    }
    // Functions to allocate and free memory are required.
    if (!functions.allocateMemory || !functions.freeMemory) { 
        functions.logger(NULL, instanceName, fmiError, "error", 
                "fmiInstantiateSlave: Missing callback function: freeMemory");
        return NULL;
    }
    if (!instanceName || strlen(instanceName)==0) { 
        functions.logger(NULL, instanceName, fmiError, "error", 
                "fmiInstantiateSlave: Missing instance name.");
        return NULL;
    }
    if (strcmp(GUID, MODEL_GUID)) {
        functions.logger(NULL, instanceName, fmiError, "error", 
                "fmiInstantiateSlave: Wrong GUID %s. Expected %s.", GUID, MODEL_GUID);
        return NULL;
    }
    component = (ModelInstance *)functions.allocateMemory(1, sizeof(ModelInstance));
    // cxh: One key change here was that we allocate memory for the pointer holding
    // the value.
    component->r = functions.allocateMemory(4, sizeof(fmiReal));
    component->r[0] = 0.0;
    component->r[1] = 1.0;
    component->r[2] = -1.0;   // Last successful firing time.
    component->r[3] = 0.0;    // Flag counting firings at breakpoints.
    component->functions = functions;
    component->instanceName = instanceName;
    
    functions.logger(component, instanceName, fmiOK, "message",
                     "Invoked fmiInstantiateSlave for instance %s.", instanceName);
    
    return component;
}

fmiStatus fmiInitializeSlave(fmiComponent c, fmiReal tStart, fmiBoolean stopTimeDefined, fmiReal tStop) {
    ModelInstance* component = (ModelInstance *) c;
    (component->functions).logger(c, component->instanceName, fmiOK, "message",
            "Invoked fmiIntializeSlave: start: %g, StopTimeDefined: %d, tStop: %g.",
            tStart, stopTimeDefined, tStop);
    component->r[2] = 0.0;
    component->r[3] = 0.0;
    return fmiOK;
}

fmiStatus fmiTerminateSlave(fmiComponent c) {
    return fmiOK;
}

void fmiFreeSlaveInstance(fmiComponent c) {
    ModelInstance* component = (ModelInstance *) c;
    printf("Invoked fmiFreeSlaveInstance.\n");
    component->functions.freeMemory(component);
}

fmiStatus fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint, 
    	fmiReal communicationStepSize, fmiBoolean newStep) {
    printf("Invoked fmiDoStep: %g, %g, newStep: %s\n", currentCommunicationPoint,
            communicationStepSize, (newStep)?"true":"false");
    ModelInstance* component = (ModelInstance *) c;
    // If current time is greater than period * (value + 1), then it is
    // time for another increment.
    // FIXME: We need a parameter for the precision here, because otherwise
    // we are going to be insisting on hitting the time of the increment
    // exactly!
    double endOfStepTime = currentCommunicationPoint + communicationStepSize;
    double targetTime = component->r[1] * (component->r[0] + 1);
    double precision = 0.0001; // FIXME: Hardwired parameter.
    if (endOfStepTime >= targetTime - precision) {
        // It is time for an increment.
        // Is it too late for the increment?
        if (endOfStepTime > targetTime + precision) {
            // Indicate that the last successful time
            // at the target time.
            component->r[2] = targetTime;
            printf("Discarding step. endOfStepTime = %g, targetTime = %g, component->r[3] = %g\n", endOfStepTime, targetTime, component->r[3]);
            return fmiDiscard;
        }
        // We are at the target time. Are we
        // ready for the increment yet? Have to have already
        // completed one firing at this time.
        if (component->r[3] > 0.0) {
            // Not the first firing. Go ahead an increment.
            component->r[0]++;
            // Reset the indicator that the increment is needed.
            component->r[3] = 0.0;
        } else {
            // This will complete the first firing at the target time.
            // We don't want to increment yet, but we set an indicator
            // that we have had a firing at this time.
            component->r[3] = 1.0;
        }
    }
    component->r[2] = endOfStepTime;
    printf("fmiDoStep succeeded.\n");
    return fmiOK;
}

fmiStatus fmiGetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiReal value[]) {
    int i, valueReference;
    ModelInstance* component = (ModelInstance *) c;
    printf("Invoked fmiGetReal: %d ", (int)nvr);

    if (nvr > 2) {
        component->functions.logger(component, component->instanceName, fmiError, "error",
                "fmiGetReal: Value reference out of range: %u.", nvr);
        return fmiError;
    }
    for (i = 0; i < nvr; i++) {
        valueReference = vr[i];
        printf("Retrieving real value with index %d and value %g.\n", valueReference, component->r[valueReference]);
    	value[i] = component->r[valueReference];
    }
    return fmiOK;
}

fmiStatus fmiGetRealStatus(fmiComponent c, const fmiStatusKind s, fmiReal* value) {
    ModelInstance* component = (ModelInstance *) c;
    if (s == fmiLastSuccessfulTime) {
        *value = component->r[2];
        return fmiOK;
    }
    // Since this FMU does not return fmiPending, there shouldn't be other queries of status.
    return fmiDiscard;
}

fmiStatus fmiSetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiReal value[]){
    int i, valueReference;
    ModelInstance* component = (ModelInstance *) c;
    printf("Invoked fmiSetReal: %d ", (int)nvr);
    if (nvr > 2) {
        component->functions.logger(component, component->instanceName, fmiError, "error",
                                    "fmiGetReal: Value reference out of range: %u.", nvr);
        return fmiError;
    }
    for (i = 0; i < nvr; i++) {
        valueReference = vr[i];
        printf("Setting real value with index %d and value %g.\n", valueReference, value[i]);
    	component->r[valueReference] = value[i];
    }
    return fmiOK;
}
