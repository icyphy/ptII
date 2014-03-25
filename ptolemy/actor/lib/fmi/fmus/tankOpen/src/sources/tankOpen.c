/* ---------------------------------------------------------------------------*
 * FMU for a fluid tank that is open to the atmosphere
 * and whose fluid is completely mixed.
 * The only energy exchange with the environment is through
 * the fluid inlet and outlet.
 *
 * This FMU has the following properties:
 *  - It has one continuous state variable.
 *  - It has two variables with direct feedthrough.
 *  - Its outputs do not depend on time.
 *
 * This file is based on the template FMU 'helloWorldME2' developed by
 * Christopher Brooks and Edward A. Lee. 
 *
 * Authors: Michael Wetter.
 * ---------------------------------------------------------------------------*/
#include <stdio.h>
#include <string.h>
#include <math.h>

// Unfortunately this file will compile to different symbols if
// compiled in a static link library or compiled as a dll.
// See fmiFunctions.h
#ifdef FMI_STATIC_OR_C_FILE  // FMI_STATIC_OR_C_FILE is a Ptolemy-specific extension.
#define FMI_FUNCTION_PREFIX tankOpen_
#endif

// Include fmu header files, typedefs and macros
#include "fmiFunctions.h"

// Data structure for an instance of this FMU. */
typedef struct {
    // cxh: Use a pointer to a fmiReal so that we can allocate space for it.
    // cxh: call this 'r' instead of 'value' so it works with model exchange.
    fmiReal    *r;
    fmiBoolean mustComputeOutputs;
    const fmiCallbackFunctions* functions;
    fmiString instanceName;
} ModelInstance;

// Value references.
// This could be optimized by using the same value reference
// for outputs that are indentical to the inputs.
#define mIn_flow 0
#define TIn 1
#define pIn 2
#define mOut_flow 3
#define TOut 4
#define pOut 5
#define T 6
#define m 7
#define k 8
#define pAtm 9
#define der_T 10

// Number of variables
#define NVARS 11

// Globally unique ID used to make sure the XML file and the DLL match.
// See also guid in modelDescription.xml
// The following was generated at http://guid.us
#define MODEL_GUID "{991fbb90-9760-4e48-9e66-11bee8a6e8d3}"



FMI_Export fmiComponent fmiInstantiate(fmiString instanceName,
        fmiType   fmuType, 
        fmiString fmuGUID, 
        fmiString fmuResourceLocation, 
        const fmiCallbackFunctions* functions, 
        fmiBoolean                  visible,
        fmiBoolean                  loggingOn) {
                                           
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
    component->r = functions->allocateMemory(NVARS, sizeof(fmiReal));
    component->functions = functions;
    component->instanceName = instanceName;
    component->mustComputeOutputs = fmiTrue;
    return component;
}

void fmiFreeInstance(fmiComponent c) {
    // cxh: I had to cast the c to a ModelInstance here.
    ModelInstance* component = (ModelInstance *) c;
    component->functions->freeMemory(component->r);
    component->functions->freeMemory(component);
}

FMI_Export fmiStatus fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint,
        fmiReal communicationStepSize, fmiBoolean newStep) {
    ModelInstance* component = (ModelInstance *) c;
        component->functions->logger(NULL, component->instanceName, fmiError, "error",
            "fmiDoStep: This function must not be called, as the FMU is for model exchange.");
    return fmiError;
}

FMI_Export fmiStatus fmiEnterInitializationMode(fmiComponent c) {
    // fixme: Setting parameter values. This should probably be done by the master algorithm.
    // However, the fmuCheck program does not set parameter values.
    ModelInstance* component = (ModelInstance *) c;
    return fmiOK;
}

FMI_Export fmiStatus fmiExitInitializationMode(fmiComponent c) {
    return fmiOK;
}

FMI_Export fmiStatus fmiGetReal(fmiComponent c, 
                                const fmiValueReference vr[], 
                                size_t nvr, 
                                fmiReal value[]) {
    int i;
    double dp;
    // cxh: I had to cast the c to a ModelInstance here.
    ModelInstance* component = (ModelInstance *) c;

    if (nvr > NVARS) {
        // cxh: The logger tends to throw segmentation faults, so comment it out
        // component->functions->logger(component, component->instanceName, fmiError, "error",
        //        "fmiGetReal: Illegal value reference %u.", nvr);
        return fmiError;
    }
    if (nvr > 0) {
      // Check if the output must be computed.
      // This could be made more efficient using an alias as pOut=pAtm.
      if (component->mustComputeOutputs){
	component->r[pOut] = component->r[pAtm]; // this is constant
	dp = component->r[pIn] - component->r[pOut];
	component->r[mOut_flow] = (dp>0) ? sqrt(dp/component->r[k]) : -sqrt(-dp/component->r[k]);
	component->r[TOut] = component->r[T];
	component->r[pOut] = component->r[pAtm];
        component->r[der_T] = component->r[mIn_flow] / component->r[m]*
	  ( component->r[TIn] - component->r[T] );
	component->mustComputeOutputs = fmiFalse;
      }
      // Assign outputs
      for(i=0; i < nvr; i++){
        value[i] = component->r[vr[i]];
      }
    }
    return fmiOK;
    }

FMI_Export fmiStatus fmiTerminate(fmiComponent c) {
    return fmiOK;
}

FMI_Export fmiStatus fmiReset(fmiComponent c) {
    return fmiOK;
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

FMI_Export fmiStatus fmiSetDebugLogging(fmiComponent c,
        fmiBoolean      loggingOn, 
        size_t          nCategories, 
        const fmiString categories[]) {
    return fmiError;
}

FMI_Export fmiStatus fmiSetReal(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiReal value[]){
    int i;
    ModelInstance* component = (ModelInstance *) c;
    if (nvr > NVARS){
        component->functions->logger(NULL, component->instanceName, fmiError, "error",
                          "fmiSetReal: To many real arguments are provided.");
        return fmiError;
    }
    // Set values.
    for (i = 0; i < nvr; i++) {
      //      printf("tankOpen.c: setReal() called, r[%2i] = %5.2f.\n", vr[i], value[i]);
        component->r[vr[i]] = value[i];
    }
    // Set a flag that indicates that the outputs must be re-computed.
    component->mustComputeOutputs = fmiTrue;
    return fmiOK;
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
    // There is nothing to do here.
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
    printf("tankOpen.c: fmiGetDirectionalDerivative() called, even though the FMU does not provide them.\n");
    // The standard 2.0, RC 1 says on p. 26:
    // If the capability attribute “providesDirectionalDerivative” is true, 
    // fmiGetDirectionalDerivative computes a linear combination of the partial derivatives of h 
    // with respect to the selected input variables
    return fmiError;
}

// Start of Model Exchange functions.
// Alphabetical in this section.

FMI_Export fmiStatus fmiGetContinuousStates(fmiComponent c, fmiReal x[],
        size_t nx) {
    ModelInstance* component = (ModelInstance *) c;
    if (nx == 1){
        x[0] = component->r[T];
    }
    else if(nx == 0){
        printf("tankOpen.c: Called fmiGetContinuousStates with nx=%d.\n", (int)nx);
    }
    else{
        printf("tankOpen.c: Called fmiGetContinuousStates with nx=%d.\n", (int)nx);
        component->functions->logger(NULL, component->instanceName, fmiError, "error",
                          "fmiGetContinuousStates: Call failed because nx is not 1.");
        return fmiError;
    }
    return fmiOK;
}

FMI_Export fmiStatus fmiGetNominalsOfContinuousStates(fmiComponent c, 
        fmiReal x_nominal[], 
        size_t nx) {
    int i;
    // The standard says to return 1.0 if the FMU has no information about
    // the nominal values
    for(i=0; i < nx; i++)
        x_nominal[i] = 1.0;
    return fmiOK;
}

FMI_Export fmiStatus fmiCompletedIntegratorStep(fmiComponent c,
        fmiBoolean   noSetFMUStatePriorToCurrentPoint, 
        fmiBoolean*  enterEventMode, 
        fmiBoolean*   terminateSimulation) {
    // Model Exchange
    // Return enterEventMode = fmiFalse to indicate that this FMU
    // stays in the continuous-time mode.
    *enterEventMode = fmiFalse;
    *terminateSimulation = fmiFalse;
    return fmiTrue;
}

FMI_Export fmiStatus fmiEnterContinuousTimeMode(fmiComponent c) {
    // Model Exchange
    return fmiOK;
}

FMI_Export fmiStatus fmiEnterEventMode(fmiComponent c) {
    // Model Exchange
    return fmiOK;
}

FMI_Export fmiStatus fmiGetDerivatives(fmiComponent c, fmiReal derivatives[],
        size_t nx) {
    // Model Exchange
    ModelInstance* component = (ModelInstance *) c;
    //    printf("tankOpen.c: Called fmiGetDerivatives\n"); // fixme
    if (nx == 1){
      // If outputs are not current, then just evaluate the derivative
      // as this is all that is needed in this call.
      if (component->mustComputeOutputs)
        derivatives[0] = component->r[mIn_flow] / component->r[m]*
	  ( component->r[TIn] - component->r[T] );
      else
        derivatives[0] = component->r[der_T];
    }
    else if(nx == 0){
        printf("tankOpen.c: Called fmiGetDerivatives with nx=%d.\n", (int)nx);
    }
    else{
        component->functions->logger(NULL, component->instanceName, fmiError, "error",
                          "fmiGetDerivatives: Call failed because nx is not 0 or 1.");
        return fmiError;
    }
    return fmiOK;
}

FMI_Export fmiStatus fmiGetEventIndicators(fmiComponent c, 
        fmiReal eventIndicators[], size_t ni) {
    // Model Exchange
    printf("tankOpen.c: fmiGetEventIndicators() returning fmiError as it does not trigger events.\n");
    return fmiError;
}

FMI_Export fmiStatus fmiNewDiscreteStates(fmiComponent  c,
        fmiEventInfo* fmiEventInfo) {
    // Model Exchange
    fmiEventInfo->newDiscreteStatesNeeded = fmiFalse;
    return fmiOK;
}

FMI_Export fmiStatus fmiSetContinuousStates(fmiComponent c, const fmiReal x[],
        size_t nx) {
    // Model Exchange
    ModelInstance* component = (ModelInstance *) c;
    if (nx == 1){
        component->r[T] = x[0];
	// The standard says we need to re-initialize caching of variables
        // that depend on the states.
        component->r[TOut] = component->r[T];
    }
    else if(nx == 0){
        printf("tankOpen.c: Called fmiSetContinuousStates with nx=%d.\n", (int)nx);
    }
    else{
        component->functions->logger(NULL, component->instanceName, fmiError, "error",
                          "fmiSetContinuousStates: Call failed because nx is not 1.");
        return fmiError;
    }
    return fmiOK;
}

FMI_Export fmiStatus fmiSetTime(fmiComponent c, fmiReal time) {
    // Model Exchange
    return fmiOK;
}



