/* ---------------------------------------------------------------------------*
 * Minimal FMU. This FMU has one output that produces the constant 42.
 * ---------------------------------------------------------------------------*/
#include <stdio.h>
#include <string.h>

// The model identifier string.
#define MODEL_IDENTIFIER helloWorld

#include "fmiFunctions.h"

// Globally unique ID used to make sure the XML file and the DLL match.
// The following was generated at http://guid.us
#define MODEL_GUID "{7b2d6d2e-ac4d-4aa8-93eb-d53357dc58ec}"

// Data structure for an instance of this FMU.
typedef struct {
    fmiReal    value;
    fmiCallbackFunctions functions;
    fmiString instanceName;
} ModelInstance;

fmiComponent fmiInstantiateSlave(fmiString  instanceName, fmiString  GUID,
    	fmiString  fmuLocation, fmiString  mimeType, fmiReal timeout, fmiBoolean visible,
    	fmiBoolean interactive, fmiCallbackFunctions functions, fmiBoolean loggingOn) {
    ModelInstance* component;

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
    component->value = 42;
    component->functions = functions;
    component->instanceName = instanceName;
    
    return component;
}

fmiStatus fmiInitializeSlave(fmiComponent c, fmiReal tStart, fmiBoolean StopTimeDefined, fmiReal tStop) {
	return fmiOK;
}

fmiStatus fmiTerminateSlave(fmiComponent c) {
	return fmiOK;
}

void fmiFreeSlaveInstance(fmiComponent c) {
    ModelInstance* component;
    component->functions.freeMemory(component);
}

fmiStatus fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint, 
    	fmiReal communicationStepSize, fmiBoolean newStep) {
    return fmiOK;
}

fmiStatus fmiGetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiReal value[]) {
    ModelInstance* component;
    // FIXME:
	printf("Invoked fmiGetReal.");

    if (nvr > 1) {
		component->functions.logger(component, component->instanceName, fmiError, "error",
                "fmiGetReal: Illegal value reference %u.", nvr);
        return fmiError;
    }
    if (nvr > 0) {
        // FIXME:
		printf("Assigning value %d.", component->value);

    	value[0] = component->value;
    }
    return fmiOK;
}

fmiStatus fmiSetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiReal value[]){
	return fmiOK;
}
