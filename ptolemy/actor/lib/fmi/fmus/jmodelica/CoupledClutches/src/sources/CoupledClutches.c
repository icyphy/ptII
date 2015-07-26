#include "CoupledClutches_base.h"
/* Run-time. */
#include "stdio.h"
#include "stdlib.h"
#include "math.h"
#include "jmi.h"
#include "jmi_block_residual.h"
#include "jmi_log.h"
#include "ModelicaUtilities.h"
#include "ModelicaStandardTables.h"

#include "fmi2_me.h"
#include "fmi2_cs.h"
#include "fmi2Functions.h"
#include "fmi2FunctionTypes.h"
#include "fmi2TypesPlatform.h"

/* FMI Funcitons. */
/* FMI 2.0 functions common for both ME and CS.*/

FMI2_Export const char* fmi2GetTypesPlatform() {
    return fmi2_get_types_platform();
}

FMI2_Export const char* fmi2GetVersion() {
    return fmi2_get_version();
}

FMI2_Export fmi2Status fmi2SetDebugLogging(fmi2Component    c,
                                           fmi2Boolean      loggingOn,
                                           size_t           nCategories,
                                           const fmi2String categories[]) {
    return fmi2_set_debug_logging(c, loggingOn, nCategories, categories);
}

FMI2_Export fmi2Component fmi2Instantiate(fmi2String instanceName,
                                          fmi2Type   fmuType,
                                          fmi2String fmuGUID,
                                          fmi2String fmuResourceLocation,
                                          const fmi2CallbackFunctions* functions,
                                          fmi2Boolean                 visible,
                                          fmi2Boolean                 loggingOn) {
    if (!can_instantiate(fmuType, instanceName, functions))
        return NULL;

    return fmi2_instantiate(instanceName, fmuType, fmuGUID, fmuResourceLocation,
                            functions, visible, loggingOn);
}

FMI2_Export void fmi2FreeInstance(fmi2Component c) {
    fmi2_free_instance(c);
}

FMI2_Export fmi2Status fmi2SetupExperiment(fmi2Component c,
                                           fmi2Boolean   toleranceDefined,
                                           fmi2Real      tolerance,
                                           fmi2Real      startTime,
                                           fmi2Boolean   stopTimeDefined,
                                           fmi2Real      stopTime) {
    return fmi2_setup_experiment(c, toleranceDefined, tolerance, startTime,
                                 stopTimeDefined, stopTime);
}

FMI2_Export fmi2Status fmi2EnterInitializationMode(fmi2Component c) {
    return fmi2_enter_initialization_mode(c);
}

FMI2_Export fmi2Status fmi2ExitInitializationMode(fmi2Component c) {
    return fmi2_exit_initialization_mode(c);
}

FMI2_Export fmi2Status fmi2Terminate(fmi2Component c) {
    return fmi2_terminate(c);
}

FMI2_Export fmi2Status fmi2Reset(fmi2Component c) {
    return fmi2_reset(c);
}

FMI2_Export fmi2Status fmi2GetReal(fmi2Component c, const fmi2ValueReference vr[],
                                   size_t nvr, fmi2Real value[]) {
    return fmi2_get_real(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2GetInteger(fmi2Component c, const fmi2ValueReference vr[],
                                      size_t nvr, fmi2Integer value[]) {
    return fmi2_get_integer(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2GetBoolean(fmi2Component c, const fmi2ValueReference vr[],
                                      size_t nvr, fmi2Boolean value[]) {
    return fmi2_get_boolean(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2GetString(fmi2Component c, const fmi2ValueReference vr[],
                                     size_t nvr, fmi2String value[]) {
    return fmi2_get_string(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2SetReal(fmi2Component c, const fmi2ValueReference vr[],
                                   size_t nvr, const fmi2Real value[]) {
    return fmi2_set_real(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2SetInteger(fmi2Component c, const fmi2ValueReference vr[],
                                      size_t nvr, const fmi2Integer value[]) {
    return fmi2_set_integer(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2SetBoolean(fmi2Component c, const fmi2ValueReference vr[],
                                      size_t nvr, const fmi2Boolean value[]) {
    return fmi2_set_boolean(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2SetString(fmi2Component c, const fmi2ValueReference vr[],
                                     size_t nvr, const fmi2String value[]) {
    return fmi2_set_string(c, vr, nvr, value);
}

FMI2_Export fmi2Status fmi2GetFMUstate(fmi2Component c, fmi2FMUstate* FMUstate) {
    return fmi2_get_fmu_state(c, FMUstate);
}

FMI2_Export fmi2Status fmi2SetFMUstate(fmi2Component c, fmi2FMUstate FMUstate) {
    return fmi2_set_fmu_state(c, FMUstate);
}

FMI2_Export fmi2Status fmi2FreeFMUstate(fmi2Component c, fmi2FMUstate* FMUstate) {
    return fmi2_free_fmu_state(c, FMUstate);
}

FMI2_Export fmi2Status fmi2SerializedFMUstateSize(fmi2Component c, fmi2FMUstate FMUstate,
                                                  size_t* size) {
    return fmi2_serialized_fmu_state_size(c, FMUstate, size);
}

FMI2_Export fmi2Status fmi2SerializedFMUstate(fmi2Component c, fmi2FMUstate FMUstate,
                                  fmi2Byte serializedState[], size_t size) {
    return fmi2_serialized_fmu_state(c, FMUstate, serializedState, size);
}

FMI2_Export fmi2Status fmi2DeSerializedFMUstate(fmi2Component c,
                                  const fmi2Byte serializedState[],
                                  size_t size, fmi2FMUstate* FMUstate) {
    return fmi2_de_serialized_fmu_state(c, serializedState, size, FMUstate);
}

FMI2_Export fmi2Status fmi2GetDirectionalDerivative(fmi2Component c,
                 const fmi2ValueReference vUnknown_ref[], size_t nUnknown,
                 const fmi2ValueReference vKnown_ref[],   size_t nKnown,
                 const fmi2Real dvKnown[], fmi2Real dvUnknown[]) {
        return fmi2_get_directional_derivative(c, vUnknown_ref, nUnknown,
                                           vKnown_ref, nKnown, dvKnown, dvUnknown);
}

#ifdef FMUME20
/* FMI 2.0 functions specific for ME.*/

FMI2_Export fmi2Status fmi2EnterEventMode(fmi2Component c) {
        return fmi2_enter_event_mode(c);
}

FMI2_Export fmi2Status fmi2NewDiscreteStates(fmi2Component  c,
                                            fmi2EventInfo* fmiEventInfo) {
        return fmi2_new_discrete_state(c, fmiEventInfo);
}

FMI2_Export fmi2Status fmi2EnterContinuousTimeMode(fmi2Component c) {
        return fmi2_enter_continuous_time_mode(c);
}

FMI2_Export fmi2Status fmi2CompletedIntegratorStep(fmi2Component c,
                                                   fmi2Boolean   noSetFMUStatePriorToCurrentPoint,
                                                   fmi2Boolean*  enterEventMode,
                                                   fmi2Boolean*   terminateSimulation) {
        return fmi2_completed_integrator_step(c, noSetFMUStatePriorToCurrentPoint,
                                          enterEventMode, terminateSimulation);
}

FMI2_Export fmi2Status fmi2SetTime(fmi2Component c, fmi2Real time) {
        return fmi2_set_time(c, time);
}

FMI2_Export fmi2Status fmi2SetContinuousStates(fmi2Component c, const fmi2Real x[],
                                               size_t nx) {
        return fmi2_set_continuous_states(c, x, nx);
}

FMI2_Export fmi2Status fmi2GetDerivatives(fmi2Component c, fmi2Real derivatives[],
                                          size_t nx) {
        return fmi2_get_derivatives(c, derivatives, nx);
}

FMI2_Export fmi2Status fmi2GetEventIndicators(fmi2Component c,
                                              fmi2Real eventIndicators[], size_t ni) {
        return fmi2_get_event_indicators(c, eventIndicators, ni);
}

FMI2_Export fmi2Status fmi2GetContinuousStates(fmi2Component c, fmi2Real x[],
                                               size_t nx) {
        return fmi2_get_continuous_states(c, x, nx);
}

FMI2_Export fmi2Status fmi2GetNominalsOfContinuousStates(fmi2Component c,
                                                         fmi2Real x_nominal[],
                                                         size_t nx) {
        return fmi2_get_nominals_of_continuous_states(c, x_nominal, nx);
}

#endif
#ifdef FMUCS20
/* FMI 2.0 functions specific for CS.*/

FMI2_Export fmi2Status fmi2SetRealInputDerivatives(fmi2Component c,
                                                   const fmi2ValueReference vr[],
                                                   size_t nvr, const fmi2Integer order[],
                                                   const fmi2Real value[]) {
        return fmi2_set_real_input_derivatives(c, vr, nvr, order, value);
}

FMI2_Export fmi2Status fmi2GetRealOutputDerivatives(fmi2Component c,
                                                    const fmi2ValueReference vr[],
                                                    size_t nvr, const fmi2Integer order[],
                                                    fmi2Real value[]) {
        return fmi2_get_real_output_derivatives(c, vr, nvr, order, value);
}

FMI2_Export fmi2Status fmi2DoStep(fmi2Component c, fmi2Real currentCommunicationPoint,
                                  fmi2Real    communicationStepSize,
                                  fmi2Boolean noSetFMUStatePriorToCurrentPoint) {
        return fmi2_do_step(c, currentCommunicationPoint, communicationStepSize,
                        noSetFMUStatePriorToCurrentPoint);
}

FMI2_Export fmi2Status fmi2CancelStep(fmi2Component c) {
        return fmi2_cancel_step(c);
}

FMI2_Export fmi2Status fmi2GetStatus(fmi2Component c, const fmi2StatusKind s,
                                     fmi2Status* value) {
        return fmi2_get_status(c, s, value);
}

FMI2_Export fmi2Status fmi2GetRealStatus(fmi2Component c, const fmi2StatusKind s,
                                         fmi2Real* value) {
        return fmi2_get_real_status(c, s, value);
}

FMI2_Export fmi2Status fmi2GetIntegerStatus(fmi2Component c, const fmi2StatusKind s,
                                            fmi2Integer* values) {
        return fmi2_get_integer_status(c, s, values);
}

FMI2_Export fmi2Status fmi2GetBooleanStatus(fmi2Component c, const fmi2StatusKind s,
                                            fmi2Boolean* value) {
        return fmi2_get_boolean_status(c, s, value);
}

FMI2_Export fmi2Status fmi2GetStringStatus(fmi2Component c, const fmi2StatusKind s,
                                           fmi2String* value) {
        return fmi2_get_string_status(c, s, value);

}

#endif

/* Helper function for instantiating the FMU. */
int can_instantiate(fmi2Type fmuType, fmi2String instanceName,
                    const fmi2CallbackFunctions* functions) {
    if (fmuType == fmi2CoSimulation) {
#ifndef FMUCS20
        functions->logger(0, instanceName, fmi2Error, "ERROR", "The model is not compiled as a Co-Simulation FMU.");
        return 0;
#endif
    } else if (fmuType == fmi2ModelExchange) {
#ifndef FMUME20
        functions->logger(0, instanceName, fmi2Error, "ERROR", "The model is not compiled as a Model Exchange FMU.");
        return 0;
#endif
    }
    return 1;
}
