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
#include "fmi2Functions.h"

// Data structure for an instance of this FMU. */
typedef struct {
    // cxh: Use a pointer to a fmi2Real so that we can allocate space for it.
    // cxh: call this 'r' instead of 'value' so it works with model exchange.
    fmi2Real    *r;
    // cxh: Needed if we call fmiSetString from another FMU
    // See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/FMU#ComplicationsWithLinuxSymbols
    // fmi2String  *s;
    // FIXME: This is non-standard.  Under Linux, if this FMU is loaded and then another FMU is loaded and calls
    // a method indirectly that dereferences ModelInstance, then there could be a crash
    // See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/FMU#ComplicationsWithLinuxSymbols
    fmi2Boolean mustComputeOutputs;
    const fmi2CallbackFunctions* functions;
    fmi2String instanceName;
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
// FIXME: The T should be 9 and modelDescription.xml updated.
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



FMI2_Export fmi2Component fmi2Instantiate(fmi2String instanceName,
        fmi2Type   fmuType,
        fmi2String fmuGUID,
        fmi2String fmuResourceLocation,
        const fmi2CallbackFunctions* functions,
        fmi2Boolean                  visible,
        fmi2Boolean                  loggingOn) {

    ModelInstance* component;

    // Perform checks.
    if (!functions->logger)
        return NULL;
    if (!functions->allocateMemory || !functions->freeMemory){
        functions->logger(NULL, instanceName, fmi2Error, "error",
                "fmiInstantiateSlave: Missing callback function: freeMemory");
        return NULL;
    }
    if (!instanceName || strlen(instanceName)==0) {
        functions->logger(NULL, instanceName, fmi2Error, "error",
                "fmiInstantiateSlave: Missing instance name.");
        return NULL;
    }
    if (strcmp(fmuGUID, MODEL_GUID)) {
        functions->logger(NULL, instanceName, fmi2Error, "error",
                "fmiInstantiateSlave: Wrong GUID %s. Expected %s.", fmuGUID, MODEL_GUID);
        return NULL;
    }
    component = (ModelInstance *)functions->allocateMemory(1, sizeof(ModelInstance));
    // cxh: One key change here was that we allocate memory for the pointer holding
    // the value.
    component->r = functions->allocateMemory(NVARS, sizeof(fmi2Real));
    component->functions = functions;
    component->instanceName = instanceName;
    // FIXME: This is non-standard.  Under Linux, if this FMU is loaded and then another FMU is loaded and calls
    // a method indirectly that dereferences ModelInstance, then there could be a crash
    // See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/FMU#ComplicationsWithLinuxSymbols
    component->mustComputeOutputs = fmi2True;
    return component;
}

void fmi2FreeInstance(fmi2Component c) {
    // cxh: I had to cast the c to a ModelInstance here.
    ModelInstance* component = (ModelInstance *) c;
    component->functions->freeMemory(component->r);
    component->functions->freeMemory(component);
}

FMI2_Export fmi2Status fmi2DoStep(fmi2Component c, fmi2Real currentCommunicationPoint,
        fmi2Real communicationStepSize, fmi2Boolean newStep) {
    ModelInstance* component = (ModelInstance *) c;
        component->functions->logger(NULL, component->instanceName, fmi2Error, "error",
            "fmiDoStep: This function must not be called, as the FMU is for model exchange.");
    return fmi2Error;
}

FMI2_Export fmi2Status fmi2EnterInitializationMode(fmi2Component c) {
    // fixme: Setting parameter values. This should probably be done by the master algorithm.
    // However, the fmuCheck program does not set parameter values.
    //ModelInstance* component = (ModelInstance *) c;
    return fmi2OK;
}

FMI2_Export fmi2Status fmi2ExitInitializationMode(fmi2Component c) {
    return fmi2OK;
}

FMI2_Export fmi2Status fmi2GetReal(fmi2Component c,
                                const fmi2ValueReference vr[],
                                size_t nvr,
                                fmi2Real value[]) {
    int i;
    double dp;
    // cxh: I had to cast the c to a ModelInstance here.
    ModelInstance* component = (ModelInstance *) c;

    if (nvr > NVARS) {
        // cxh: The logger tends to throw segmentation faults, so comment it out
        // component->functions->logger(component, component->instanceName, fmi2Error, "error",
        //        "fmiGetReal: Illegal value reference %u.", nvr);
        return fmi2Error;
    }
    if (nvr > 0) {
      // Check if the output must be computed.
      // This could be made more efficient using an alias as pOut=pAtm.

      // FIXME: This is non-standard.  Under Linux, if this FMU is loaded and then another FMU is loaded and calls
      // a method indirectly that dereferences ModelInstance, then there could be a crash
      // See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/FMU#ComplicationsWithLinuxSymbols
      if (component->mustComputeOutputs){
        component->r[pOut] = component->r[pAtm]; // this is constant
        dp = component->r[pIn] - component->r[pOut];
        component->r[mOut_flow] = (dp>0) ? sqrt(dp/component->r[k]) : -sqrt(-dp/component->r[k]);
        component->r[TOut] = component->r[T];
        component->r[pOut] = component->r[pAtm];
        component->r[der_T] = component->r[mIn_flow] / component->r[m]*
          ( component->r[TIn] - component->r[T] );
        component->mustComputeOutputs = fmi2False;
      }
      // Assign outputs
      for(i=0; i < nvr; i++){
        value[i] = component->r[vr[i]];
      }
    }
    return fmi2OK;
    }

FMI2_Export fmi2Status fmi2Terminate(fmi2Component c) {
    return fmi2OK;
}

FMI2_Export fmi2Status fmi2Reset(fmi2Component c) {
    return fmi2OK;
}


////////////////////////////////////////////////////////////
// Below here are functions that need to be in the FMU for it to
// pass the checker.  However, the all return fmi2Error or print a
// message so that we can be sure that they are not called accidentally.


FMI2_Export fmi2Status fmi2GetInteger(fmi2Component c, const fmi2ValueReference vr[],
        size_t nvr, fmi2Integer value[]) {
    return fmi2Error;
}

FMI2_Export fmi2Status fmi2GetBoolean(fmi2Component c, const fmi2ValueReference vr[],
        size_t nvr, fmi2Boolean value[]) {
    return fmi2Error;
}

FMI2_Export fmi2Status fmi2GetString(fmi2Component c, const fmi2ValueReference vr[],
        size_t nvr, fmi2String value[]) {
    return fmi2Error;
}

FMI2_Export const char* fmiGetTypesPlatform() {
    // We return a string literal, which does not require malloc.
    // Note that this is declared const char * because it is not safe to
    // modify a string literal in C.
    return "default";
}

FMI2_Export const char* fmi2GetVersion() {
    // We return a string literal, which does not require malloc.
    // Note that this is declared const char * because it is not safe to
    // modify a string literal in C.
    return "2.0";
}

const char* fmi2GetTypesPlatform() {
    return fmi2TypesPlatform;
}

FMI2_Export fmi2Status fmi2SetDebugLogging(fmi2Component c,
        fmi2Boolean      loggingOn,
        size_t          nCategories,
        const fmi2String categories[]) {
    return fmi2Error;
}

FMI2_Export fmi2Status fmi2SetReal(fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2Real value[]){
    int i;
    ModelInstance* component = (ModelInstance *) c;
    if (nvr > NVARS){
        component->functions->logger(NULL, component->instanceName, fmi2Error, "error",
                          "fmiSetReal: To many real arguments are provided.");
        return fmi2Error;
    }
    // Set values.
    for (i = 0; i < nvr; i++) {
        component->r[vr[i]] = value[i];
    }
    // Set a flag that indicates that the outputs must be re-computed.

    // FIXME: This is non-standard.  Under Linux, if this FMU is loaded and then another FMU is loaded and calls
    // fmiSetReal indirectly, then there will be a crash.
    // See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/FMU#ComplicationsWithLinuxSymbols
    component->mustComputeOutputs = fmi2True;
    return fmi2OK;
}

FMI2_Export fmi2Status fmi2SetInteger(fmi2Component c, const fmi2ValueReference vr[],
        size_t nvr, const fmi2Integer value[]) {
    return fmi2Error;
}

FMI2_Export fmi2Status fmi2SetBoolean(fmi2Component c, const fmi2ValueReference vr[],
        size_t nvr, const fmi2Boolean value[]) {
    return fmi2Error;
}

FMI2_Export fmi2Status fmi2SetString(fmi2Component c, const fmi2ValueReference vr[],
        size_t nvr, const fmi2String value[]) {
    return fmi2Error;
}

/* // We include a definition for fmiSetString() because values20RC1 calls setString() which invokes this method. */
/* // See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/FMU#ComplicationsWithLinuxSymbols */
/* fmi2Status fmi2SetString (fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2String value[]) { */
/*     int i; */
/*     ModelInstance *comp = (ModelInstance *)c; */
/*     fprintf(stderr, "values20RC1 fmuTemplate.c fmiSetString()\n"); */
/*     fflush(stderr); */
/*     //if (invalidState(comp, "fmiSetString", modelInstantiated|modelInitializationMode|modelInitialized|modelStepping)) */
/*     //    return fmi2Error; */
/*     if (nvr > 0 && !vr) { */
/*         comp->functions->logger(NULL, comp->instanceName, fmi2Error, "error", */
/*                 "fmiSetString: vr[] is null."); */
/*         return fmi2Error; */
/*     } */
/*     if (nvr > 0 && !value) { */
/*         comp->functions->logger(NULL, comp->instanceName, fmi2Error, "error", */
/*                 "fmiSetString: value is null."); */
/*         return fmi2Error; */
/*     } */
/*     comp->functions->logger(NULL, comp->instanceName, fmi2OK, "ok", */
/*             "fmiSetString: nvr = %d", nvr); */

/*     for (i = 0; i < nvr; i++) { */
/*         char *string = (char *)comp->s[vr[i]]; */
/*         //if (vrOutOfRange(comp, "fmiSetString", vr[i], NUMBER_OF_STRINGS)) */
/*         //    return fmi2Error; */
/*         comp->functions->logger(NULL, comp->instanceName, fmi2OK, "ok", */
/*                 "fmiSetString: #s%d# = '%s'", vr[i], value[i]); */

/*         if (!value[i]) { */
/*             comp->functions->logger(NULL, comp->instanceName, fmi2Error, "error", */
/*                     "fmiSetString: value[i] is null."); */
/*             return fmi2Error; */
/*         } */
/*         if (string == NULL || strlen(string) < strlen(value[i])) { */
/*             if (string) comp->functions->freeMemory(string); */
/*             comp->s[vr[i]] = comp->functions->allocateMemory(1 + strlen(value[i]), sizeof(char)); */
/*             if (!comp->s[vr[i]]) { */
/*                 //comp->state = modelError; */
/*                 //FILTERED_LOG(comp, fmi2Error, LOG_ERROR, "fmiSetString: Out of memory.") */
/*                 comp->functions->logger(NULL, comp->instanceName, fmi2Error, "error", */
/*                         "fmiSetString: Out of memory."); */
/*                 return fmi2Error; */
/*             } */
/*         } */
/*         strcpy((char *)comp->s[vr[i]], (char *)value[i]); */
/*     } */
/*     return fmi2OK; */
/* } */

FMI2_Export fmi2Status fmi2SetupExperiment(fmi2Component c,
        fmi2Boolean   toleranceDefined,
        fmi2Real      tolerance,
        fmi2Real      startTime,
        fmi2Boolean   stopTimeDefined,
        fmi2Real      stopTime) {
    // There is nothing to do here.
    return fmi2OK;
}

FMI2_Export fmi2Status fmi2GetFMUstate(fmi2Component c, fmi2FMUstate* FMUstate) {
    return fmi2Error;
}

FMI2_Export fmi2Status fmi2SetFMUstate(fmi2Component c, fmi2FMUstate FMUstate) {
    return fmi2Error;
}

FMI2_Export fmi2Status fmi2FreeFMUstate(fmi2Component c, fmi2FMUstate* FMUstate) {
    return fmi2Error;
}

FMI2_Export fmi2Status fmi2SerializedFMUstateSize(fmi2Component c, fmi2FMUstate FMUstate,
        size_t* size) {
    return fmi2Error;
}

FMI2_Export fmi2Status fmi2SerializedFMUstate(fmi2Component c, fmi2FMUstate FMUstate,
        fmi2Byte serializedState[], size_t size) {
    return fmi2Error;
}

FMI2_Export fmi2Status fmi2DeSerializedFMUstate(fmi2Component c,
        const fmi2Byte serializedState[],
        size_t size, fmi2FMUstate* FMUstate) {
    return fmi2Error;
}

FMI2_Export fmi2Status fmi2GetDirectionalDerivative(fmi2Component c,
        const fmi2ValueReference vUnknown_ref[], size_t nUnknown,
        const fmi2ValueReference vKnown_ref[],   size_t nKnown,
        const fmi2Real dvKnown[], fmi2Real dvUnknown[]) {
    printf("tankOpen.c: fmiGetDirectionalDerivative() called, even though the FMU does not provide them.\n");
    // The standard 2.0, RC 1 says on p. 26:
    // If the capability attribute “providesDirectionalDerivative” is true,
    // fmiGetDirectionalDerivative computes a linear combination of the partial derivatives of h
    // with respect to the selected input variables
    return fmi2Error;
}

// Start of Model Exchange functions.
// Alphabetical in this section.

FMI2_Export fmi2Status fmi2GetContinuousStates(fmi2Component c, fmi2Real x[],
        size_t nx) {
    ModelInstance* component = (ModelInstance *) c;
    if (nx == 1){
        x[0] = component->r[T];
        return fmi2OK;
    }
    else{
        printf("tankOpen.c: Called fmiGetContinuousStates with nx=%d.\n", (int)nx);
        component->functions->logger(NULL, component->instanceName, fmi2Error, "error",
                          "fmiGetContinuousStates: Call failed because nx is not 1.");
        return fmi2Error;
    }
}

FMI2_Export fmi2Status fmi2GetNominalsOfContinuousStates(fmi2Component c,
        fmi2Real x_nominal[],
        size_t nx) {
    int i;
    // The standard says to return 1.0 if the FMU has no information about
    // the nominal values
    for(i=0; i < nx; i++)
        x_nominal[i] = 1.0;
    return fmi2OK;
}

FMI2_Export fmi2Status fmi2CompletedIntegratorStep(fmi2Component c,
        fmi2Boolean   noSetFMUStatePriorToCurrentPoint,
        fmi2Boolean*  enterEventMode,
        fmi2Boolean*   terminateSimulation) {
    // Model Exchange
    // Return enterEventMode = fmi2False to indicate that this FMU
    // stays in the continuous-time mode.
    *enterEventMode = fmi2False;
    *terminateSimulation = fmi2False;
    return fmi2True;
}

FMI2_Export fmi2Status fmi2EnterContinuousTimeMode(fmi2Component c) {
    // Model Exchange
    return fmi2OK;
}

FMI2_Export fmi2Status fmi2EnterEventMode(fmi2Component c) {
    // Model Exchange
    return fmi2OK;
}

FMI2_Export fmi2Status fmi2GetDerivatives(fmi2Component c, fmi2Real derivatives[],
        size_t nx) {
    // Model Exchange
    ModelInstance* component = (ModelInstance *) c;
    if (nx == 1){
      // If outputs are not current, then just evaluate the derivative
      // as this is all that is needed in this call.
      if (component->mustComputeOutputs)
        derivatives[0] = component->r[mIn_flow] / component->r[m]*
          ( component->r[TIn] - component->r[T] );
      else
        derivatives[0] = component->r[der_T];
    }
    else{
        component->functions->logger(NULL, component->instanceName, fmi2Error, "error",
                          "fmiGetDerivatives: Call failed because nx is not 0 or 1.");
        return fmi2Error;
    }
    return fmi2OK;
}

FMI2_Export fmi2Status fmi2GetEventIndicators(fmi2Component c,
        fmi2Real eventIndicators[], size_t ni) {
    // Model Exchange
    printf("tankOpen.c: fmiGetEventIndicators() returning fmi2Error as it does not trigger events.\n");
    return fmi2Error;
}

FMI2_Export fmi2Status fmi2NewDiscreteStates(fmi2Component  c,
        fmi2EventInfo* fmi2EventInfo) {
    // Model Exchange
    fmi2EventInfo->newDiscreteStatesNeeded = fmi2False;
    return fmi2OK;
}

FMI2_Export fmi2Status fmi2SetContinuousStates(fmi2Component c, const fmi2Real x[],
        size_t nx) {
    // Model Exchange
    ModelInstance* component = (ModelInstance *) c;
    if (nx == 1){
        component->r[T] = x[0];
        // The standard says we need to re-initialize caching of variables
        // that depend on the states.
        component->r[TOut] = component->r[T];
    }
    else{
        component->functions->logger(NULL, component->instanceName, fmi2Error, "error",
                          "fmiSetContinuousStates: Call failed because nx is not 1.");
        return fmi2Error;
    }
    return fmi2OK;
}

FMI2_Export fmi2Status fmi2SetTime(fmi2Component c, fmi2Real time) {
    // Model Exchange
    return fmi2OK;
}



