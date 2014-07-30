/* ---------------------------------------------------------------------------*
 *
 * This FMU models the ball dynamics of the right ball of Fig 4 in
 * http://www.eecs.berkeley.edu/Pubs/TechRpts/2014/EECS-2014-15.html
 *
 * Authors: David Broman
 * ---------------------------------------------------------------------------*/


#include <stdio.h>
#include <string.h>

// The model identifier string.
#define MODEL_IDENTIFIER ballDynamics
// Globally unique ID used to make sure the XML file and the DLL match.
// The following was generated at http://guid.us
#define MODEL_GUID "{45759609-8478-4038-a4cd-42ecc2cc126b}"

// Used by FMI 2.0.  See fmiFunctions.h
//#define FMIAPI_FUNCTION_PREFIX ballDynamics_
//#define FMIAPI

// include fmu header files, typedefs and macros
#include "fmiFunctions.h"

#define VAR(varname) (component->varname)

#define STEP 0.01      // Euler step size


/*****************************************************************************************
 * Data structure for an instance of this FMU.
 */
typedef struct {

    //Parameters
    fmiReal m;
    fmiReal x_0;
    fmiReal v_0;

    //Input variables
    fmiReal F;                  // Force
    fmiReal F_i;                // Impulse force

    //Output variables
    fmiReal x;                  // Positon of the ball
    fmiReal v;                  // Velocity of the ball

    const fmiCallbackFunctions *functions;
    fmiString instanceName;
} ModelInstance;

// Indices of instance variables can be used to set or get by the master algorithm.
#define VAR_F   1
#define VAR_F_i 2
#define VAR_x   3
#define VAR_v   4
#define VAR_x_0 5
#define VAR_v_0 6
#define VAR_m   7


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
        // FIXME: Remove printfs. Replace with logger calls when they work.
        fprintf(stderr,"fmiInstantiateSlave: Wrong GUID %s. Expected %s.\n", GUID, modelGUID);
        fflush(stderr);
        //functions->logger(NULL, instanceName, fmiError, "error",
        //                  "fmiInstantiateSlave: Wrong GUID %s. Expected %s.", GUID, modelGUID);
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
fmiStatus FMIAPI fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint,
            fmiReal communicationStepSize, fmiBoolean noSetFMUStatePriorToCurrentPoint) {
    ModelInstance* component = (ModelInstance *) c;

    //Check if impulse
    if(communicationStepSize == 0.0 && VAR(F_i) != 0.0) {
        VAR(v) += VAR(F_i) / VAR(m);
        return fmiOK;
    }

    // Forward Euler for equatons
    // x' = x;
    // v' = F / MASS;

    fmiReal h;
    fmiReal step_left = communicationStepSize;
    for(;step_left > 0;){
        h = (step_left >= STEP) ? STEP : step_left;
        step_left -= STEP;
        VAR(x) = VAR(x) + h * VAR(v);
        VAR(v) = VAR(v) + h * (VAR(F) / VAR(m));
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
 *  Get the maximum next step size.
 *  If the last call to fmiDoStep() incremented the counter, then the maximum step
 *  size is zero. Otherwise, it is the time remaining until the next increment of the count.
 *  @param c The FMU.
 *  @param maxStepSize A pointer to a real into which to write the result.
 *  @return fmiOK.
 */
fmiStatus FMIAPI fmiGetMaxStepSize(fmiComponent c, fmiReal *maxStepSize) {
    ModelInstance* component = (ModelInstance *) c;
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
        switch(valueReference){
        case VAR_x:
          value[i] = component->x;
          break;
        case VAR_v:
          value[i] = component->v;
          break;
        case VAR_x_0:
          value[i] = component->x_0;
          break;
        case VAR_v_0:
          value[i] = component->v_0;
          break;
        case VAR_m:
          value[i] = component->m;
          break;
        default:
          return fmiError;
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


    component->functions = functions;

    // Need to allocate memory and copy the string because JNA stores the string
    // in a temporary buffer that gets GC'd.
    component->instanceName = (char*)functions->allocateMemory(1 + strlen(instanceName), sizeof(char));
    strcpy((char *)component->instanceName, instanceName);

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
fmiStatus FMIAPI fmiInitializeSlave(fmiComponent c,
                             fmiReal relativeTolerance,
                             fmiReal tStart,
                             fmiBoolean stopTimeDefined,
                             fmiReal tStop) {

    ModelInstance* component = (ModelInstance *) c;
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
        switch(valueReference){
        case VAR_F:
            component->F = value[i];
            break;
        case VAR_F_i:
            component->F_i = value[i];
            break;
        case VAR_x_0:
            component->x = value[i];
            break;
        case VAR_v_0:
            component->v = value[i];
            break;
        case VAR_m:
            component->m = value[i];
            break;
        default:
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
fmiStatus FMIAPI fmiTerminateSlave(fmiComponent c) {
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
