 /* ---------------------------------------------------------------------------*
 * FMU wrapper for TuLiP-control toolbox
 *
 * This file is based on the template FMU 'stepCounter' developed by
 * Christopher Brooks and Edward A. Lee and the template FMU 'stairsB'
 * developed by David Broman
 *
 * Authors: Yilin Mo
 * ---------------------------------------------------------------------------*/


#include <stdio.h>
#include <string.h>
#include "TuLiPFMU.h"

// include fmu header files, typedefs and macros
#include "fmi2Functions.h"
//#include "TuLiPControl.h"

// Include the C file so that we have a single large C file.  This makes the makefiles easier.
#include "TulipControl.c"
#include "FSM.c"
#include "data.c"
#include "mealydata.c"
#include "polytope.c"
#include "pppdata.c"

// Used by FMI 2.0.  See FMIFuctions.h
#define FMIAPI_FUNCTION_PREFIX TuLiPFMU_
#define FMIAPI


/*****************************************************************************************
 * Data structure for an instance of this FMU.
 */
typedef struct {
        Controller *controller;
        pfloat* input;
        pfloat* output;
        idxint* dInput;
        fmi2Boolean atBreakpoint;    // Indicator that the first output at a step
        // time has been produced.
        // General states
        fmi2Real currentCount;       // The current count (the output).
        fmi2Real lastSuccessfulTime; // The time to which this FMU has advanced.
        const fmi2CallbackFunctions *functions;
        fmi2String instanceName;
} ModelInstance;

/*****************************************************************************************
 *  Check various properties of this FMU. Return 0 if any requirement is violated, and 1 otherwise.
 *  @param instanceName The name of the instance.
 *  @param GUID The globally unique identifier for this FMU as understood by the master.
 *  @param modelGUID The globally unique identifier for this FMU as understood by this FMU.
 *  @param fmuResourceLocation A URI for the location of the unzipped FMU.
 *  @param functions The callback functions to allocate and free memory and log progress.
 *  @param visible Indicator of whether the FMU should run silently (fmi2False) or interact
 *   with displays, etc. (fmi2True) (ignored by this FMU).
 *  @param loggingOn Indicator of whether logging messages should be sent to the logger.
 *  @return The instance of this FMU, or null if there are required functions missing,
 *   if there is no instance name, or if the GUID does not match this FMU.
 */
int checkFMU(
                fmi2String instanceName,
                fmi2String GUID,
                fmi2String modelGUID,
                fmi2String fmuResourceLocation,
                const fmi2CallbackFunctions *functions,
                fmi2Boolean visible,
                fmi2Boolean loggingOn)  {
        // Logger callback is required.
        if (!functions->logger) {
                return 0;
        }
        // Functions to allocate and free memory are required.
        if (!functions->allocateMemory || !functions->freeMemory) {
                functions->logger(NULL, instanceName, fmi2Error, "error",
                                "fmiInstantiateSlave: Missing callback function: freeMemory");
                return 0;
        }
        if (!instanceName || strlen(instanceName)==0) {
                functions->logger(NULL, instanceName, fmi2Error, "error",
                                "fmiInstantiateSlave: Missing instance name.");
                return 0;
        }
        if (strcmp(GUID, modelGUID)) {
                // FIXME: Remove printfs. Replace with logger calls when they work.
                fprintf(stderr,"fmiInstantiateSlave: Wrong GUID %s. Expected %s.\n", GUID, modelGUID);
                fflush(stderr);
                //functions->logger(NULL, instanceName, fmi2Error, "error",
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
 *  @return fmi2Discard if the FMU rejects the step size, otherwise fmi2OK.
 */
fmi2Status fmiDoStep(fmi2Component c, fmi2Real currentCommunicationPoint,
                fmi2Real communicationStepSize, fmi2Boolean noSetFMUStatePriorToCurrentPoint) {
        ModelInstance* component = (ModelInstance *) c;

        // If current time is greater than period * (value + 1), then it is
        // time for another increment.
        double endOfStepTime = currentCommunicationPoint + communicationStepSize;
        double targetTime = TICK_PERIOD * (component->currentCount + 1);
        if (endOfStepTime >= targetTime - EPSILON) {
                // It is time for an increment.
                // Is it too late for the increment?
                if (endOfStepTime > targetTime + EPSILON) {
                        // Indicate that the last successful time is
                        // at the target time.
                        component->lastSuccessfulTime = targetTime;
                        fflush(stdout);
                        return fmi2Discard;
                }
                // We are at the target time. Are we
                // ready for the increment yet? Have to have already
                // completed one firing at this time.
                if (component->atBreakpoint) {
                        // Not the first firing. Go ahead an increment.
                        component->currentCount++;

                        input_function(component->controller,component->input,component->dInput);

                        transition_function(component->controller);

                        output_function(component->controller,component->output);
                        // Reset the indicator that the increment is needed.
                        component->atBreakpoint = fmi2False;
                } else {
                        // This will complete the first firing at the target time.
                        // We don't want to increment yet, but we set an indicator
                        // that we have had a firing at this time.
                        fflush(stdout);
                        component->atBreakpoint = fmi2True;
                }
        }
        component->lastSuccessfulTime = endOfStepTime;
        fflush(stdout);
        return fmi2OK;
}

/*****************************************************************************************
 *  Free memory allocated by this FMU instance.
 *  @param c The FMU.
 */
void fmiFreeSlaveInstance(fmi2Component c) {
        ModelInstance* component = (ModelInstance *) c;
        free_controller(component->controller);
        component->functions->freeMemory(component->input);
        component->functions->freeMemory(component->output);
        component->functions->freeMemory(component->dInput);
        component->functions->freeMemory(component);
}

/*****************************************************************************************
 *  Get the maximum next step size.
 *  If the last call to fmiDoStep() incremented the counter, then the maximum step
 *  size is zero. Otherwise, it is the time remaining until the next increment of the count.
 *  @param c The FMU.
 *  @param maxStepSize A pointer to a real into which to write the result.
 *  @return fmi2OK.
 */
fmi2Status fmiGetMaxStepSize(fmi2Component c, fmi2Real *maxStepSize) {
        ModelInstance* component = (ModelInstance *) c;
        if (component->atBreakpoint) {
                *maxStepSize = 0.0;
        } else {
                double targetTime = TICK_PERIOD * (component->currentCount + 1);
                double step = targetTime - component->lastSuccessfulTime;
                *maxStepSize = step;
        }
        return fmi2OK;
}

/*****************************************************************************************
 *  Get the values of the specified real variables.
 *  @param c The FMU.
 *  @param vr An array of value references (indices) for the desired values.
 *  @param nvr The number of values desired (the length of vr).
 *  @param value The array into which to put the results.
 *  @return fmi2Error if a value reference is out of range, otherwise fmi2OK.
 */
fmi2Status fmiGetReal(fmi2Component c, const fmi2ValueReference vr[], size_t nvr, fmi2Real value[]) {
        int i, valueReference;
        ModelInstance* component = (ModelInstance *) c;

        for (i = 0; i < nvr; i++) {
                valueReference = vr[i];
                if (valueReference >= 0 && valueReference <p)
                {
                        value[i] = (fmi2Real) (*(component->output+valueReference));
                }
        }
        return fmi2OK;
}

/*****************************************************************************************
 *  Get the specified FMU status. This procedure only provides status kind
 *  fmi2LastSuccessfulTime. All other requests result in returning fmi2Discard.
 *  @param c The FMU.
 *  @param s The kind of status to return, which must be fmi2LastSuccessfulTime.
 *  @param value A pointer to the location in which to deposit the status.
 *  @return fmi2Discard if the kind is not fmi2LastSuccessfulTime, otherwise fmi2OK.
 */
fmi2Status fmiGetRealStatus(fmi2Component c, const fmi2StatusKind s, fmi2Real* value) {
        ModelInstance* component = (ModelInstance *) c;
        if (s == fmi2LastSuccessfulTime) {
                *value = component->lastSuccessfulTime;

                printf("fmiGetRealStatus returns lastSuccessfulTime is %g\n", *value);
                fflush(stdout);

                return fmi2OK;
        }
        // Since this FMU does not return fmiPending, there shouldn't be other queries of status.
        return fmi2Discard;
}

/*****************************************************************************************
 *  Create an instance of this FMU.
 *  @param instanceName The name of the instance.
 *  @param GUID The globally unique identifier for this FMU.
 *  @param fmuResourceLocation A URI for the location of the unzipped FMU.
 *  @param functions The callback functions to allocate and free memory and log progress.
 *  @param visible Indicator of whether the FMU should run silently (fmi2False) or interact
 *   with displays, etc. (fmi2True) (ignored by this FMU).
 *  @param loggingOn Indicator of whether logging messages should be sent to the logger.
 *  @return The instance of this FMU, or null if there are required functions missing,
 *   if there is no instance name, or if the GUID does not match this FMU.
 */
fmi2Component fmiInstantiateSlave(
                fmi2String instanceName,
                fmi2String GUID,
                fmi2String fmuResourceLocation,
                const fmi2CallbackFunctions *functions,
                fmi2Boolean visible,
                fmi2Boolean loggingOn)  {
        ModelInstance* component;

        // Perform checks.
        if (!checkFMU(instanceName, GUID, MODEL_GUID, fmuResourceLocation, functions, visible, loggingOn)) {
                return NULL;
        }
        component = (ModelInstance *)functions->allocateMemory(1, sizeof(ModelInstance));
        component->currentCount = 0.0;
        component->lastSuccessfulTime = -1.0;
        component->atBreakpoint = fmi2False;
        component->functions = functions;

        component->controller = instantiate_controller();
        component->input = (pfloat*)functions->allocateMemory(m,sizeof(pfloat));
        component->output= (pfloat*)functions->allocateMemory(p,sizeof(pfloat));
        component->dInput= (idxint*)functions->allocateMemory(nInputVariable,sizeof(idxint));

        // Need to allocate memory and copy the string because JNA stores the string
        // in a temporary buffer that gets GC'd.
        component->instanceName = (char*)functions->allocateMemory(1 + strlen(instanceName), sizeof(char));
        strcpy((char *)component->instanceName, instanceName);

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
 *  @param stopTimeDefined fmi2True to indicate that the stop time is defined (ignored by this FMU).
 *  @param tStop The stop time (ignored if stopTimeDefined is fmi2False) (ignored by this FMU).
 *  @return fmi2OK
 */
fmi2Status fmiInitializeSlave(fmi2Component c,
                fmi2Real relativeTolerance,
                fmi2Real tStart,
                fmi2Boolean stopTimeDefined,
                fmi2Real tStop) {

        ModelInstance* component = (ModelInstance *) c;
        printf("%s: Invoked fmiIntializeSlave: start: %g, StopTimeDefined: %d, tStop: %g..\n",
                        component->instanceName, tStart, stopTimeDefined, tStop);
        fflush(stdout);

        component->lastSuccessfulTime = tStart;
        component->atBreakpoint = fmi2False;

        init_controller(component->controller);

        printf("successful init controller\n");
        fflush(stdout);
        output_function(component->controller,component->output);

        return fmi2OK;
}

/*****************************************************************************************
 *  Set the specified real values.
 *  @param c The FMU.
 *  @param vr An array of indexes of the real variables to be set (value references).
 *  @param nvr The number of values to be set (the length of the array vr).
 *  @param value The values to assign to these variables.
 *  @return fmi2Error if a value reference is out of range, otherwise fmi2OK.
 */
fmi2Status fmiSetReal(fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2Real value[]){
        int i, valueReference;
        ModelInstance* component = (ModelInstance *) c;
        for (i = 0; i < nvr; i++) {
                valueReference = vr[i];
                if (valueReference >= p && valueReference < p+m)
                {
                        *(component->input+valueReference-p)=(pfloat) value[i];
                }
        }
        return fmi2OK;
}

/*****************************************************************************************
 *  Set the specified integer values.
 *  @param c The FMU.
 *  @param vr An array of indexes of the integer variables to be set (value references).
 *  @param nvr The number of values to be set (the length of the array vr).
 *  @param value The values to assign to these variables.
 *  @return fmi2Error if a value reference is out of range, otherwise fmi2OK.
 */
fmi2Status fmiSetInteger(fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2Integer value[]){
        int i, valueReference;
        ModelInstance* component = (ModelInstance *) c;
        for (i = 0; i < nvr; i++) {
                valueReference = vr[i];
                if (valueReference >= p+m && valueReference < p+m+nInputVariable)
                {
                        *(component->dInput+valueReference-p-m)=(idxint) value[i];
                }
        }
        return fmi2OK;
}

/*****************************************************************************************
 *  Terminate this FMU. This does nothing, since this FMU is passive.
 *  @param c The FMU.
 *  @return fmi2OK if the FMU was non-null, otherwise return fmi2Error
 */
fmi2Status fmiTerminateSlave(fmi2Component c) {
        ModelInstance* component = (ModelInstance *) c;

        if (component == NULL) {
                printf("fmiTerminateSlave called with a null argument?  This can happen while exiting during a failure to construct the component\n");
                fflush(stdout);
                return fmi2Error;
        } else {
                printf("%s: fmiTerminateSlave\n", component->instanceName);
                fflush(stdout);
        }

        return fmi2OK;
}
