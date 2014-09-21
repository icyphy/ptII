// define class name and unique id
#ifdef FMI_FROM_SOURCE
#define FMI2_FUNCTION_PREFIX Influenza_
#endif
#include <fmi2TypesPlatform.h>
#include <fmi2Functions.h>
#define MODEL_GUID "{8c4e810f-3df3-4a00-8276-176fa3c9f9e0}"
#include <cstdio>
#include <cstring>
#include <cassert>
#include "sfmi_runtime.h"
using namespace std;
using namespace sfmi;

// define model size
#define NUMBER_OF_STATES 5
#define NUMBER_OF_EVENT_INDICATORS 0
#define NUMBER_OF_REALS 17
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_EXTERNALFUNCTIONS 0

// define variable data for model
#define timeValue (data->Time)
#define _Deceased_ 0 
#define _Deceased (data->real_vars[0]) 
#define _Infectious_ 1 
#define _Infectious (data->real_vars[1]) 
#define _Recovered_ 2 
#define _Recovered (data->real_vars[2]) 
#define _Removed_ 3 
#define _Removed (data->real_vars[3]) 
#define _Susceptible_ 4 
#define _Susceptible (data->real_vars[4]) 
#define _DER_Deceased_ 5 
#define _DER_Deceased (data->real_vars[5]) 
#define _DER_Infectious_ 6 
#define _DER_Infectious (data->real_vars[6]) 
#define _DER_Recovered_ 7 
#define _DER_Recovered (data->real_vars[7]) 
#define _DER_Removed_ 8 
#define _DER_Removed (data->real_vars[8]) 
#define _DER_Susceptible_ 9 
#define _DER_Susceptible (data->real_vars[9]) 
#define _Population_ 10 
#define _Population (data->real_vars[10]) 
#define _R_ 11 
#define _R (data->real_vars[11]) 
#define _EncounterRate_ 12 
#define _EncounterRate (data->real_vars[12]) 
#define _MortalityProb_ 13 
#define _MortalityProb (data->real_vars[13]) 
#define _MortalityTime_ 14 
#define _MortalityTime (data->real_vars[14]) 
#define _RecoveryTime_ 15 
#define _RecoveryTime (data->real_vars[15]) 
#define _TransmissionProb_ 16 
#define _TransmissionProb (data->real_vars[16]) 

#define PRE_Deceased_ 0 
#define PRE_Deceased (data->pre_real_vars[0]) 
#define PRE_Infectious_ 1 
#define PRE_Infectious (data->pre_real_vars[1]) 
#define PRE_Recovered_ 2 
#define PRE_Recovered (data->pre_real_vars[2]) 
#define PRE_Removed_ 3 
#define PRE_Removed (data->pre_real_vars[3]) 
#define PRE_Susceptible_ 4 
#define PRE_Susceptible (data->pre_real_vars[4]) 
#define PRE_DER_Deceased_ 5 
#define PRE_DER_Deceased (data->pre_real_vars[5]) 
#define PRE_DER_Infectious_ 6 
#define PRE_DER_Infectious (data->pre_real_vars[6]) 
#define PRE_DER_Recovered_ 7 
#define PRE_DER_Recovered (data->pre_real_vars[7]) 
#define PRE_DER_Removed_ 8 
#define PRE_DER_Removed (data->pre_real_vars[8]) 
#define PRE_DER_Susceptible_ 9 
#define PRE_DER_Susceptible (data->pre_real_vars[9]) 
#define PRE_Population_ 10 
#define PRE_Population (data->pre_real_vars[10]) 
#define PRE_R_ 11 
#define PRE_R (data->pre_real_vars[11]) 
#define PRE_EncounterRate_ 12 
#define PRE_EncounterRate (data->pre_real_vars[12]) 
#define PRE_MortalityProb_ 13 
#define PRE_MortalityProb (data->pre_real_vars[13]) 
#define PRE_MortalityTime_ 14 
#define PRE_MortalityTime (data->pre_real_vars[14]) 
#define PRE_RecoveryTime_ 15 
#define PRE_RecoveryTime (data->pre_real_vars[15]) 
#define PRE_TransmissionProb_ 16 
#define PRE_TransmissionProb (data->pre_real_vars[16]) 

// define initial state vector as vector of value references
static const fmi2ValueReference STATES[NUMBER_OF_STATES] = { _Deceased_, _Infectious_, _Recovered_, _Removed_, _Susceptible_ };
static const fmi2ValueReference STATESDERIVATIVES[NUMBER_OF_STATES] = { _DER_Deceased_, _DER_Infectious_, _DER_Recovered_, _DER_Removed_, _DER_Susceptible_ };


// dynamic equation functions


/*
 equation index: 13
 type: SIMPLE_ASSIGN
 der(Removed) = (DIVISION(MortalityProb, MortalityTime) + DIVISION(1.0 - MortalityProb, RecoveryTime)) * Infectious
 */
static void eqFunction_13(model_data *data)
{
    modelica_real tmp0;
    modelica_real tmp1;
    tmp0 = DIVISION(_MortalityProb, _MortalityTime);
    tmp1 = DIVISION((1.0 - _MortalityProb), _RecoveryTime);
    _DER_Removed = ((tmp0 + tmp1) * _Infectious);
}
/*
 equation index: 14
 type: SIMPLE_ASSIGN
 der(Deceased) = MortalityProb * der(Removed)
 */
static void eqFunction_14(model_data *data)
{
    _DER_Deceased = (_MortalityProb * _DER_Removed);
}
/*
 equation index: 15
 type: SIMPLE_ASSIGN
 der(Recovered) = (1.0 - MortalityProb) * der(Removed)
 */
static void eqFunction_15(model_data *data)
{
    _DER_Recovered = ((1.0 - _MortalityProb) * _DER_Removed);
}
/*
 equation index: 16
 type: SIMPLE_ASSIGN
 Population = Recovered + Infectious + Susceptible
 */
static void eqFunction_16(model_data *data)
{
    _Population = (_Recovered + (_Infectious + _Susceptible));
}
/*
 equation index: 17
 type: SIMPLE_ASSIGN
 R = TransmissionProb * EncounterRate * DIVISION(Susceptible, Population)
 */
static void eqFunction_17(model_data *data)
{
    modelica_real tmp2;
    tmp2 = DIVISION(_Susceptible, _Population);
    _R = (_TransmissionProb * (_EncounterRate * tmp2));
}
/*
 equation index: 18
 type: SIMPLE_ASSIGN
 der(Susceptible) = (-R) * Infectious
 */
static void eqFunction_18(model_data *data)
{
    _DER_Susceptible = ((-_R) * _Infectious);
}
/*
 equation index: 19
 type: SIMPLE_ASSIGN
 der(Infectious) = 1.0 + R * Infectious + sin(0.01095890410958904 * time) - der(Removed)
 */
static void eqFunction_19(model_data *data)
{
    modelica_real tmp3;
    tmp3 = sin((0.01095890410958904 * timeValue));
    _DER_Infectious = (1.0 + ((_R * _Infectious) + (tmp3 - _DER_Removed)));
}

// Zero crossing functions


// Dependency graph for sparse updates
static void setupEquationGraph(model_data *data)
{
    data->link(eqFunction_13,&_DER_Removed);
    data->link(&_MortalityProb,eqFunction_13);
    data->link(&_MortalityTime,eqFunction_13);
    data->link(&_MortalityProb,eqFunction_13);
    data->link(&_RecoveryTime,eqFunction_13);
    data->link(&_Infectious,eqFunction_13);
    data->link(eqFunction_14,&_DER_Deceased);
    data->link(&_MortalityProb,eqFunction_14);
    data->link(&_Removed,eqFunction_14);
    data->link(eqFunction_15,&_DER_Recovered);
    data->link(&_MortalityProb,eqFunction_15);
    data->link(&_Removed,eqFunction_15);
    data->link(eqFunction_16,&_Population);
    data->link(&_Recovered,eqFunction_16);
    data->link(&_Infectious,eqFunction_16);
    data->link(&_Susceptible,eqFunction_16);
    data->link(eqFunction_17,&_R);
    data->link(&_TransmissionProb,eqFunction_17);
    data->link(&_EncounterRate,eqFunction_17);
    data->link(&_Susceptible,eqFunction_17);
    data->link(&_Population,eqFunction_17);
    data->link(eqFunction_18,&_DER_Susceptible);
    data->link(&_R,eqFunction_18);
    data->link(&_Infectious,eqFunction_18);
    data->link(eqFunction_19,&_DER_Infectious);
    data->link(&_R,eqFunction_19);
    data->link(&_Infectious,eqFunction_19);
    data->link(&timeValue,eqFunction_19);
    data->link(&_Removed,eqFunction_19);
}

// initial condition equations


/*
 equation index: 1
 type: SIMPLE_ASSIGN
 Removed = $_start(Removed)
 */
static void eqFunction_1(model_data *data)
{
    modelica_real tmp4;
    tmp4 = $__start(_Removed);
    _Removed = tmp4;
}
/*
 equation index: 2
 type: SIMPLE_ASSIGN
 Deceased = $_start(Deceased)
 */
static void eqFunction_2(model_data *data)
{
    modelica_real tmp5;
    tmp5 = $__start(_Deceased);
    _Deceased = tmp5;
}
/*
 equation index: 3
 type: SIMPLE_ASSIGN
 Susceptible = $_start(Susceptible)
 */
static void eqFunction_3(model_data *data)
{
    modelica_real tmp6;
    tmp6 = $__start(_Susceptible);
    _Susceptible = tmp6;
}
/*
 equation index: 4
 type: SIMPLE_ASSIGN
 Recovered = $_start(Recovered)
 */
static void eqFunction_4(model_data *data)
{
    modelica_real tmp7;
    tmp7 = $__start(_Recovered);
    _Recovered = tmp7;
}
/*
 equation index: 5
 type: SIMPLE_ASSIGN
 Infectious = $_start(Infectious)
 */
static void eqFunction_5(model_data *data)
{
    modelica_real tmp8;
    tmp8 = $__start(_Infectious);
    _Infectious = tmp8;
}
/*
 equation index: 6
 type: SIMPLE_ASSIGN
 Population = Recovered + Infectious + Susceptible
 */
static void eqFunction_6(model_data *data)
{
    _Population = (_Recovered + (_Infectious + _Susceptible));
}
/*
 equation index: 7
 type: SIMPLE_ASSIGN
 R = TransmissionProb * EncounterRate * DIVISION(Susceptible, Population)
 */
static void eqFunction_7(model_data *data)
{
    modelica_real tmp9;
    tmp9 = DIVISION(_Susceptible, _Population);
    _R = (_TransmissionProb * (_EncounterRate * tmp9));
}
/*
 equation index: 8
 type: SIMPLE_ASSIGN
 der(Removed) = (DIVISION(MortalityProb, MortalityTime) + DIVISION(1.0 - MortalityProb, RecoveryTime)) * Infectious
 */
static void eqFunction_8(model_data *data)
{
    modelica_real tmp10;
    modelica_real tmp11;
    tmp10 = DIVISION(_MortalityProb, _MortalityTime);
    tmp11 = DIVISION((1.0 - _MortalityProb), _RecoveryTime);
    _DER_Removed = ((tmp10 + tmp11) * _Infectious);
}
/*
 equation index: 9
 type: SIMPLE_ASSIGN
 der(Deceased) = MortalityProb * $DER.Removed
 */
static void eqFunction_9(model_data *data)
{
    _DER_Deceased = (_MortalityProb * _DER_Removed);
}
/*
 equation index: 10
 type: SIMPLE_ASSIGN
 der(Recovered) = (1.0 - MortalityProb) * $DER.Removed
 */
static void eqFunction_10(model_data *data)
{
    _DER_Recovered = ((1.0 - _MortalityProb) * _DER_Removed);
}
/*
 equation index: 11
 type: SIMPLE_ASSIGN
 der(Susceptible) = (-R) * Infectious
 */
static void eqFunction_11(model_data *data)
{
    _DER_Susceptible = ((-_R) * _Infectious);
}
/*
 equation index: 12
 type: SIMPLE_ASSIGN
 der(Infectious) = 1.0 + R * Infectious + sin(0.01095890410958904 * time) - $DER.Removed
 */
static void eqFunction_12(model_data *data)
{
    modelica_real tmp12;
    tmp12 = sin((0.01095890410958904 * timeValue));
    _DER_Infectious = (1.0 + ((_R * _Infectious) + (tmp12 - _DER_Removed)));
}

// Set values for all variables that define a start value
static void setDefaultStartValues(model_data *comp)
{
    comp->Time = 0.0;
    comp->real_vars[_Deceased_] = 0.0;
    comp->real_vars[_Infectious_] = 0.0;
    comp->real_vars[_Recovered_] = 0.0;
    comp->real_vars[_Removed_] = 0.0;
    comp->real_vars[_Susceptible_] = 499000.0;
    comp->real_vars[_DER_Deceased_] = 0;
    comp->real_vars[_DER_Infectious_] = 0;
    comp->real_vars[_DER_Recovered_] = 0;
    comp->real_vars[_DER_Removed_] = 0;
    comp->real_vars[_DER_Susceptible_] = 0;
    comp->real_vars[_Population_] = 0;
    comp->real_vars[_R_] = 0;
    comp->real_vars[_EncounterRate_] = 4.0;
    comp->real_vars[_MortalityProb_] = 0.01;
    comp->real_vars[_MortalityTime_] = 1.0;
    comp->real_vars[_RecoveryTime_] = 3.0;
    comp->real_vars[_TransmissionProb_] = 0.15;
}


// Solve for unknowns in the model's initial equations
static void initialEquations(model_data* data)
{
    eqFunction_1(data);
    eqFunction_2(data);
    eqFunction_3(data);
    eqFunction_4(data);
    eqFunction_5(data);
    eqFunction_6(data);
    eqFunction_7(data);
    eqFunction_8(data);
    eqFunction_9(data);
    eqFunction_10(data);
    eqFunction_11(data);
    eqFunction_12(data);

}

// model exchange functions

const char* fmi2GetTypesPlatform() { return fmi2TypesPlatform; }
const char* fmi2GetVersion() { return fmi2Version; }

fmi2Status fmi2SetDebugLogging(fmi2Component, fmi2Boolean, size_t, const fmi2String*)
{
    return fmi2OK;
}

fmi2Status fmi2SetupExperiment(fmi2Component c, fmi2Boolean, fmi2Real, fmi2Real startTime, fmi2Boolean, fmi2Real)
{
    return fmi2SetTime(c,startTime);
}

fmi2Status fmi2EnterInitializationMode(fmi2Component)
{
    return fmi2OK;
}

fmi2Status fmi2ExitInitializationMode(fmi2Component c)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    initialEquations(data);
    return fmi2OK;
}

fmi2Status fmi2Terminate(fmi2Component)
{
    return fmi2OK;
}

fmi2Status fmi2Reset(fmi2Component c)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    setDefaultStartValues(data);
    initialEquations(data);
    return fmi2OK;
}

fmi2Status fmi2GetFMUstate(fmi2Component, fmi2FMUstate*) { return fmi2Error; }
fmi2Status fmi2SetFMUstate(fmi2Component, fmi2FMUstate) { return fmi2Error; }
fmi2Status fmi2FreeFMUstate(fmi2Component, fmi2FMUstate*) { return fmi2Error; }
fmi2Status fmi2SerializedFMUstateSize(fmi2Component, fmi2FMUstate, size_t*) { return fmi2Error; }
fmi2Status fmi2SerializeFMUstate(fmi2Component, fmi2FMUstate, fmi2Byte*, size_t) { return fmi2Error; }
fmi2Status fmi2DeSerializeFMUstate(fmi2Component, const fmi2Byte[], size_t, fmi2FMUstate*) { return fmi2Error; }
fmi2Status fmi2GetDirectionalDerivative(fmi2Component, const fmi2ValueReference*, size_t,
                                                 const fmi2ValueReference*, size_t,
                                                 const fmi2Real*, fmi2Real*) { return fmi2Error; }

fmi2Status fmi2GetDerivatives(fmi2Component c, fmi2Real* der, size_t nvr)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL || nvr > NUMBER_OF_STATES) return fmi2Error;
    for (size_t i = 0; i < nvr; i++) der[i] = data->real_vars[STATESDERIVATIVES[i]];
    return fmi2OK;
}

fmi2Status fmi2GetEventIndicators(fmi2Component, fmi2Real[], size_t)
{
    if (NUMBER_OF_EVENT_INDICATORS == 0) return fmi2OK;
    return fmi2Error;
}

fmi2Status fmi2GetContinuousStates(fmi2Component c, fmi2Real* states, size_t nvr)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL || nvr > NUMBER_OF_STATES) return fmi2Error;
    for (size_t i = 0; i < nvr; i++) states[i] = data->real_vars[STATES[i]];
    return fmi2OK;
}

fmi2Status fmi2SetContinuousStates(fmi2Component c, const fmi2Real* states, size_t nvr)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL || nvr > NUMBER_OF_STATES) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        data->real_vars[STATES[i]] = states[i];
        data->modify(&(data->real_vars[STATES[i]]));
    }
    return fmi2OK;
}

fmi2Status fmi2GetNominalsOfContinuousStates(fmi2Component c, fmi2Real* nominals, size_t nvr)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL || nvr > NUMBER_OF_STATES) return fmi2Error;
    for (size_t i = 0; i < nvr; i++) nominals[i] = 1.0;
    return fmi2OK;
}

 fmi2Status fmi2EnterEventMode(fmi2Component)
 {
    if (NUMBER_OF_EVENT_INDICATORS == 0) return fmi2OK;
    return fmi2Error;
 }

 fmi2Status fmi2NewDiscreteStates(fmi2Component, fmi2EventInfo* event_info)
 {
    event_info->newDiscreteStatesNeeded = fmi2False;
    event_info->terminateSimulation = fmi2False;
    event_info->nominalsOfContinuousStatesChanged = fmi2False;
    event_info->valuesOfContinuousStatesChanged = fmi2False;
    event_info->nextEventTimeDefined = fmi2False;
    event_info->nextEventTime = 0.0;
    if (NUMBER_OF_EVENT_INDICATORS == 0) return fmi2OK;
    return fmi2Error;
 }

 fmi2Status fmi2EnterContinuousTimeMode(fmi2Component) { return fmi2OK; }

 fmi2Status fmi2CompletedIntegratorStep(fmi2Component c, fmi2Boolean,
     fmi2Boolean* enterEventMode, fmi2Boolean* terminateSimulation)
 {
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL || enterEventMode == NULL || terminateSimulation == NULL) return fmi2Error;
    data->update();
    *enterEventMode = fmi2False;
    *terminateSimulation = fmi2False;
    return fmi2OK;
 }

fmi2Status
fmi2SetTime(fmi2Component c, fmi2Real t)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    data->Time = t;
    data->modify(&(data->Time));
    return fmi2OK;
}

fmi2Status
fmi2GetReal(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, fmi2Real* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_REALS) return fmi2Error;
        value[i] = data->real_vars[vr[i]];
    }
    return fmi2OK;
}

fmi2Status
fmi2SetReal(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, const fmi2Real* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_REALS) return fmi2Error;
        data->real_vars[vr[i]] = value[i];
        data->modify((&data->real_vars[vr[i]]));
    }
    return fmi2OK;
}

fmi2Status
fmi2GetInteger(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, fmi2Integer* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_INTEGERS) return fmi2Error;
        value[i] = data->int_vars[vr[i]];
    }
    return fmi2OK;
}

fmi2Status
fmi2SetInteger(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, const fmi2Integer* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_INTEGERS) return fmi2Error;
        data->int_vars[vr[i]] = value[i];
        data->modify((&data->int_vars[vr[i]]));
    }
    return fmi2OK;
}

fmi2Status
fmi2GetBoolean(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, fmi2Boolean* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_BOOLEANS) return fmi2Error;
        value[i] = data->bool_vars[vr[i]];
    }
    return fmi2OK;
}

fmi2Status
fmi2SetBoolean(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, const fmi2Boolean* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_BOOLEANS) return fmi2Error;
        data->bool_vars[vr[i]] = value[i];
        data->modify((&data->bool_vars[vr[i]]));
    }
    return fmi2OK;
}

fmi2Status
fmi2GetString(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, fmi2String* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_STRINGS) return fmi2Error;
        value[i] = data->str_vars[vr[i]].c_str();
    }
    return fmi2OK;
}

fmi2Status
fmi2SetString(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, const fmi2String* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_STRINGS) return fmi2Error;
        data->str_vars[vr[i]] = value[i];
        data->modify((&data->str_vars[vr[i]]));
    }
    return fmi2OK;
}

fmi2Component
fmi2Instantiate(
  fmi2String instanceName,
  fmi2Type fmuType,
  fmi2String fmuGUID,
  fmi2String fmuResourceLocation,
  const fmi2CallbackFunctions* functions,
  fmi2Boolean visible,
  fmi2Boolean loggingOn)
{
    model_data* data = new model_data(
        NUMBER_OF_REALS,NUMBER_OF_INTEGERS,NUMBER_OF_STRINGS,NUMBER_OF_BOOLEANS,NUMBER_OF_EVENT_INDICATORS);
    setupEquationGraph(data);
    setDefaultStartValues(data);
    initialEquations(data);
    return static_cast<fmi2Component>(data);
}

void
fmi2FreeInstance(fmi2Component c)
{
    model_data* data = static_cast<model_data*>(c);
    if (data != NULL) delete data;
}


