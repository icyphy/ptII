/* ---------------------------------------------------------------------------*
 * Minimal FMU. This FMU has one output that produces the constant 42.
 * ---------------------------------------------------------------------------*/
#include <stdio.h>
#include <string.h>

// The model identifier string.
#define MODEL_IDENTIFIER helloWorld

// cxh: Check to see if FMU_COSIMULATION is defined because this file is used
// in fmusdk to create both a co-simulation fmu and a model exchange fmu.
#ifdef FMU_COSIMULATION

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

#else // FMU_COSIMULATION
// cxh: This is used for model exchange, which is built when make is run in fmusdk/
#include "fmuTemplate.h"
#endif // FMU_COSIMULATION

// Globally unique ID used to make sure the XML file and the DLL match.
// The following was generated at http://guid.us
#define MODEL_GUID "{7b2d6d2e-ac4d-4aa8-93eb-d53357dc58ec}"

fmiComponent DllExport fmiInstantiateSlave(fmiString  instanceName, fmiString  GUID,
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
    // cxh: One key change here was that we allocate memory for the pointer holding
    // the value.
    component->r = functions.allocateMemory(1,    sizeof(fmiReal));
    component->r[0] = 42.0;
    component->functions = functions;
    component->instanceName = instanceName;

    return component;
}

fmiStatus DllExport fmiInitializeSlave(fmiComponent c, fmiReal tStart, fmiBoolean StopTimeDefined, fmiReal tStop) {
        return fmiOK;
}

fmiStatus DllExport fmiTerminateSlave(fmiComponent c) {
        return fmiOK;
}

void DllExport fmiFreeSlaveInstance(fmiComponent c) {
    // cxh: I had to cast the c to a ModelInstance here.
    ModelInstance* component = (ModelInstance *) c;
    component->functions.freeMemory(component);
}

fmiStatus DllExport fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint,
            fmiReal communicationStepSize, fmiBoolean newStep) {
    return fmiOK;
}

fmiStatus DllExport fmiGetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiReal value[]) {
    // cxh: I had to cast the c to a ModelInstance here.
    ModelInstance* component = (ModelInstance *) c;
    // FIXME:
        printf("Invoked fmiGetReal.");

    if (nvr > 1) {
        // cxh: The logger tends to throw segmentation faults, so comment it out
        // component->functions.logger(component, component->instanceName, fmiError, "error",
        //        "fmiGetReal: Illegal value reference %u.", nvr);
        return fmiError;
    }
    if (nvr > 0) {
        // FIXME:
        //printf("Assigning value %d.", component->r[nvr]);
        // cxh: FIXME: not sure about how to use nvr here.
            value[0] = component->r[0];
    }
    return fmiOK;
}

fmiStatus DllExport fmiSetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiReal value[]){
        return fmiOK;
}
