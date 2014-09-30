/* ---------------------------------------------------------------------------*
 * Minimal FMU. This FMU has one output that produces the constant 42.
 * ---------------------------------------------------------------------------*/
#include <stdio.h>
#include <string.h>

// Unfortunately this file will compile to different symbols if
// compiled in a static link library or compiled as a dll.
// See fmiFunctions.h
#ifdef FMI_STATIC_OR_C_FILE  // FMI_STATIC_OR_C_FILE is a Ptolemy-specific extension.
#define FMI_FUNCTION_PREFIX helloWorldME2_
#endif

// include fmu header files, typedefs and macros
#include "fmiFunctions.h"

// Data structure for an instance of this FMU. */
typedef struct {
    // cxh: Use a pointer to a fmiReal so that we can allocate space for it.
    // cxh: call this 'r' instead of 'value' so it works with model exchange.
    fmiReal    *r;
    const fmiCallbackFunctions* functions;
    fmiString instanceName;
} ModelInstance;


// Globally unique ID used to make sure the XML file and the DLL match.
// See also guid in modelDescription.xml
// The following was generated at http://guid.us
#define MODEL_GUID "{d192370b-a57a-487f-a6de-a0f8d07554c1}"



FMI_Export fmiComponent fmiInstantiate(fmiString instanceName,
        fmiType   fmuType,
        fmiString fmuGUID,
        fmiString fmuResourceLocation,
        const fmiCallbackFunctions* functions,
        fmiBoolean                  visible,
        fmiBoolean                  loggingOn) {


    // FMI-1.0 declaration here:
    //fmiComponent fmiInstantiateSlave(fmiString  instanceName, fmiString  GUID,
    //        fmiString  fmuLocation, fmiString  mimeType, fmiReal timeout, fmiBoolean visible,
    //        fmiBoolean interactive, fmiCallbackFunctions functions, fmiBoolean loggingOn) {

    ModelInstance* component;

    // Perform checks.
    if (!functions->logger)
        return NULL;
    if (!functions->allocateMemory || !functions->freeMemory){
        functions->logger(NULL, instanceName, fmiError, "error",
                "fmiInstantiateSlave: Missing callback function: freeMemory");
        return NULL;
    }
    if (!instanceName || strlen(instanceName)==0) {
        functions->logger(NULL, instanceName, fmiError, "error",
                "fmiInstantiateSlave: Missing instance name.");
        return NULL;
    }
    if (strcmp(fmuGUID, MODEL_GUID)) {
        functions->logger(NULL, instanceName, fmiError, "error",
                "fmiInstantiateSlave: Wrong GUID %s. Expected %s.", fmuGUID, MODEL_GUID);
        return NULL;
    }
    component = (ModelInstance *)functions->allocateMemory(1, sizeof(ModelInstance));
    // cxh: One key change here was that we allocate memory for the pointer holding
    // the value.
    component->r = functions->allocateMemory(1,    sizeof(fmiReal));
    component->r[0] = 42.0;
    component->functions = functions;
    component->instanceName = instanceName;

    return component;
}

void fmiFreeInstance(fmiComponent c) {
    // cxh: I had to cast the c to a ModelInstance here.
    ModelInstance* component = (ModelInstance *) c;
    component->functions->freeMemory(component);
}


fmiStatus fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint,
        fmiReal communicationStepSize, fmiBoolean newStep) {
    return fmiOK;
}

FMI_Export fmiStatus fmiEnterInitializationMode(fmiComponent c) {
    printf("helloWorldME.c: fmiEnterInitializationMode() returning fmiOK, though it is not implemented yet.\n");
    return fmiOK;
}

FMI_Export fmiStatus fmiExitInitializationMode(fmiComponent c) {
    printf("helloWorldME.c: fmiExitInitializationMode() returning fmiOK, though it is not implemented yet.\n");
    return fmiOK;
}


fmiStatus fmiGetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiReal value[]) {
    // cxh: I had to cast the c to a ModelInstance here.
    ModelInstance* component = (ModelInstance *) c;
    // FIXME:
    printf("Invoked fmiGetReal.");

    if (nvr > 1) {
        // cxh: The logger tends to throw segmentation faults, so comment it out
        // component->functions->logger(component, component->instanceName, fmiError, "error",
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

FMI_Export fmiStatus fmiTerminate(fmiComponent c) {
    return fmiOK;
}

FMI_Export fmiStatus fmiReset(fmiComponent c) {
    return fmiError;
}


////////////////////////////////////////////////////////////
// Below here are functions that need to be in the FMU for it to
// pass the checker.  However, the all return fmiError or print a
// message so that we can be sure that they are not called accidentally.


FMI_Export fmiStatus fmiGetInteger(fmiComponent c, const fmiValueReference vr[],
        size_t nvr, fmiInteger value[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiGetBoolean(fmiComponent c, const fmiValueReference vr[],
        size_t nvr, fmiBoolean value[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiGetString(fmiComponent c, const fmiValueReference vr[],
        size_t nvr, fmiString value[]) {
    return fmiError;
}

FMI_Export const char* fmiGetTypesPlatform() {
    // We return a string literal, which does not require malloc.
    // Note that this is declared const char * because it is not safe to
    // modify a string literal in C.
    return "default";
}

FMI_Export const char* fmiGetVersion() {
    // We return a string literal, which does not require malloc.
    // Note that this is declared const char * because it is not safe to
    // modify a string literal in C.
    return "2.0";
}

FMI_Export fmiStatus fmiSetDebugLogging(fmiComponent    c,
        fmiBoolean      loggingOn,
        size_t          nCategories,
        const fmiString categories[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiSetReal(fmiComponent c, const fmiValueReference vr[],
        size_t nvr, const fmiReal value[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiSetInteger(fmiComponent c, const fmiValueReference vr[],
        size_t nvr, const fmiInteger value[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiSetBoolean(fmiComponent c, const fmiValueReference vr[],
        size_t nvr, const fmiBoolean value[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiSetString(fmiComponent c, const fmiValueReference vr[],
        size_t nvr, const fmiString value[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiSetupExperiment(fmiComponent c,
        fmiBoolean   toleranceDefined,
        fmiReal      tolerance,
        fmiReal      startTime,
        fmiBoolean   stopTimeDefined,
        fmiReal      stopTime) {
    printf("helloWorldME2.c: Warning called fmiSetupExperiment, which is not yet implemented.\n");
    return fmiOK;
}

FMI_Export fmiStatus fmiGetFMUstate(fmiComponent c, fmiFMUstate* FMUstate) {
    return fmiError;
}

FMI_Export fmiStatus fmiSetFMUstate(fmiComponent c, fmiFMUstate FMUstate) {
    return fmiError;
}

FMI_Export fmiStatus fmiFreeFMUstate(fmiComponent c, fmiFMUstate* FMUstate) {
    return fmiError;
}

FMI_Export fmiStatus fmiSerializedFMUstateSize(fmiComponent c, fmiFMUstate FMUstate,
        size_t* size) {
    return fmiError;
}

FMI_Export fmiStatus fmiSerializedFMUstate(fmiComponent c, fmiFMUstate FMUstate,
        fmiByte serializedState[], size_t size) {
    return fmiError;
}

FMI_Export fmiStatus fmiDeSerializedFMUstate(fmiComponent c,
        const fmiByte serializedState[],
        size_t size, fmiFMUstate* FMUstate) {
    return fmiError;
}

FMI_Export fmiStatus fmiGetDirectionalDerivative(fmiComponent c,
        const fmiValueReference vUnknown_ref[], size_t nUnknown,
        const fmiValueReference vKnown_ref[],   size_t nKnown,
        const fmiReal dvKnown[], fmiReal dvUnknown[]) {
    return fmiError;
}

// Start of Model Exchange functions.
// Alphabetical in this section.

FMI_Export fmiStatus fmiGetContinuousStates(fmiComponent c, fmiReal x[],
        size_t nx) {
    printf("helloWorldME.c: fmiGetContinuousStates() returning fmiOk, it is not implemented yet");
    return fmiOK;
}

FMI_Export fmiStatus fmiGetNominalsOfContinuousStates(fmiComponent c,
        fmiReal x_nominal[],
        size_t nx) {
    // Model Exchange
    return fmiError;
}

FMI_Export fmiStatus fmiCompletedIntegratorStep(fmiComponent c,
        fmiBoolean   noSetFMUStatePriorToCurrentPoint,
        fmiBoolean*  enterEventMode,
        fmiBoolean*   terminateSimulation) {
    // Model Exchange
    return fmiError;
}

FMI_Export fmiStatus fmiEnterContinuousTimeMode(fmiComponent c) {
    // Model Exchange
    printf("helloWorldME.c: fmiEnterContinuousTimeMode() returning fmiOk, it is not implemented yet.\n");
    return fmiOK;
}

FMI_Export fmiStatus fmiEnterEventMode(fmiComponent c) {
    // Model Exchange
    printf("helloWorldME.c: fmiEnterEventMode() returning fmiOk, it is not implemented yet.\n");
    return fmiOK;
}

FMI_Export fmiStatus fmiGetDerivatives(fmiComponent c, fmiReal derivatives[],
        size_t nx) {
    // Model Exchange
    return fmiError;
}

FMI_Export fmiStatus fmiGetEventIndicators(fmiComponent c,
        fmiReal eventIndicators[], size_t ni) {
    // Model Exchange
    printf("helloWorldME.c: fmiGetEventIndicators() returning fmiOK, though it is not implemented yet.\n");
    return fmiOK;
}

FMI_Export fmiStatus fmiNewDiscreteStates(fmiComponent  c,
        fmiEventInfo* fmiEventInfo) {
    // Model Exchange
    return fmiError;
}

FMI_Export fmiStatus fmiSetContinuousStates(fmiComponent c, const fmiReal x[],
        size_t nx) {
    // Model Exchange
    return fmiError;
}

FMI_Export fmiStatus fmiSetTime(fmiComponent c, fmiReal time) {
    // Model Exchange
    printf("helloWorldME.c: fmiSetTime() returning fmiOK, though it is not implemented yet.\n");
    return fmiOK;
}



