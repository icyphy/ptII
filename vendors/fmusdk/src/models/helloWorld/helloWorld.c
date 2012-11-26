/* ---------------------------------------------------------------------------*
 * Minimal Co-simulation FMU. This FMU has one output that produces the constant 42.
 *
 * To run: ./fmusim cs fmu/cs/helloWorld.fmu 5 0.1 0 s
 * or
 * bin/fmusim_cs fmu/cs/helloWorld.fmu 5 0.1 0 s
 * then look in result.csv
 *
 * Note that this file will not work with model exchange.
 * ---------------------------------------------------------------------------*/
#include <stdio.h>
#include <string.h>

// The model identifier string.
#define MODEL_IDENTIFIER helloWorld

// include fmu header files, typedefs and macros
#include "fmiFunctions.h"

// Data structure for an instance of this FMU. */
typedef struct {
    // cxh: Use a pointer to a fmiReal so that we can allocate space for it.
    // cxh: call this 'r' instead of 'value' so it works with model exchange.
    fmiReal    *r;
    fmiCallbackFunctions functions;
    fmiString instanceName;
} ModelInstance;

// Globally unique ID used to make sure the XML file and the DLL match.
// The following was generated at http://guid.us
#define MODEL_GUID "{7b2d6d2e-ac4d-4aa8-93eb-d53357dc58ec}"

fmiComponent fmiInstantiateSlave(fmiString  instanceName, fmiString  GUID,
    	fmiString  fmuLocation, fmiString  mimeType, fmiReal timeout, fmiBoolean visible,
    	fmiBoolean interactive, fmiCallbackFunctions functions, fmiBoolean loggingOn) {
    ModelInstance* component;

    printf("Invoked fmiInstantiateSlave.\n");
    // Perform checks.
    if (!functions.logger) 
        return NULL;
    if (!functions.allocateMemory || !functions.freeMemory){ 
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
    component->r = functions.allocateMemory(1,    sizeof(fmiReal));
    component->r[0] = 42.0;
    component->functions = functions;
    component->instanceName = instanceName;
    
    return component;
}

fmiStatus fmiInitializeSlave(fmiComponent c, fmiReal tStart, fmiBoolean stopTimeDefined, fmiReal tStop) {
    printf("Invoked fmiIntializeSlave: start: %g, StopTimeDefined: %d, tStop: %g.\n",
            tStart, stopTimeDefined, tStop);
    return fmiOK;
}

fmiStatus fmiTerminateSlave(fmiComponent c) {
    return fmiOK;
}

void fmiFreeSlaveInstance(fmiComponent c) {
    // cxh: I had to cast the c to a ModelInstance here.
    ModelInstance* component = (ModelInstance *) c;

    printf("Invoked fmiFreeSlaveInstance.\n");
    component->functions.freeMemory(component);
}

fmiStatus fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint, 
    	fmiReal communicationStepSize, fmiBoolean newStep) {
    printf("Invoked fmiDoStep: %g, %g, newStep: %g\n", currentCommunicationPoint,
            communicationStepSize, newStep);
    return fmiOK;
}

fmiStatus fmiGetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiReal value[]) {
    // cxh: I had to cast the c to a ModelInstance here.
    ModelInstance* component = (ModelInstance *) c;
    printf("Invoked fmiGetReal: %d ", (int)nvr);

    if (nvr > 1) {
        // cxh: The logger tends to throw segmentation faults, so comment it out
        // component->functions.logger(component, component->instanceName, fmiError, "error",
        //        "fmiGetReal: Illegal value reference %u.", nvr);
        return fmiError;
    }
    if (nvr > 0) {
        // FIXME:
        printf("Assigning value %g.\n", component->r[nvr-1]);
        // cxh: FIXME: not sure about how to use nvr here.
    	value[0] = component->r[nvr-1];
    }
    return fmiOK;
}

fmiStatus fmiSetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiReal value[]){
    printf("Invoked fmiSetReal: %d ", (int)nvr);
    return fmiOK;
}
