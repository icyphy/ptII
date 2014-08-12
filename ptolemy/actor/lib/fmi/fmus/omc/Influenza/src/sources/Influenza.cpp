// define class name and unique id
#define FMI2_FUNCTION_PREFIX Influenza_
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
#define time (data->Time)
#define $PDeceased_ 0 
#define $PDeceased (data->real_vars[0]) 
#define $PInfectious_ 1 
#define $PInfectious (data->real_vars[1]) 
#define $PRecovered_ 2 
#define $PRecovered (data->real_vars[2]) 
#define $PRemoved_ 3 
#define $PRemoved (data->real_vars[3]) 
#define $PSusceptible_ 4 
#define $PSusceptible (data->real_vars[4]) 
#define $P$DER$PDeceased_ 5 
#define $P$DER$PDeceased (data->real_vars[5]) 
#define $P$DER$PInfectious_ 6 
#define $P$DER$PInfectious (data->real_vars[6]) 
#define $P$DER$PRecovered_ 7 
#define $P$DER$PRecovered (data->real_vars[7]) 
#define $P$DER$PRemoved_ 8 
#define $P$DER$PRemoved (data->real_vars[8]) 
#define $P$DER$PSusceptible_ 9 
#define $P$DER$PSusceptible (data->real_vars[9]) 
#define $PPopulation_ 10 
#define $PPopulation (data->real_vars[10]) 
#define $PR_ 11 
#define $PR (data->real_vars[11]) 
#define $PEncounterRate_ 12 
#define $PEncounterRate (data->real_vars[12]) 
#define $PMortalityProb_ 13 
#define $PMortalityProb (data->real_vars[13]) 
#define $PMortalityTime_ 14 
#define $PMortalityTime (data->real_vars[14]) 
#define $PRecoveryTime_ 15 
#define $PRecoveryTime (data->real_vars[15]) 
#define $PTransmissionProb_ 16 
#define $PTransmissionProb (data->real_vars[16]) 

// define initial state vector as vector of value references
static const fmi2ValueReference STATES[NUMBER_OF_STATES] = { $PDeceased_, $PInfectious_, $PRecovered_, $PRemoved_, $PSusceptible_ };
static const fmi2ValueReference STATESDERIVATIVES[NUMBER_OF_STATES] = { $P$DER$PDeceased_, $P$DER$PInfectious_, $P$DER$PRecovered_, $P$DER$PRemoved_, $P$DER$PSusceptible_ };


// equation functions


/*
 equation index: 13
 type: SIMPLE_ASSIGN
 der(Removed) = (DIVISION(MortalityProb, MortalityTime) + DIVISION(1.0 - MortalityProb, RecoveryTime)) * Infectious
 */
static void eqFunction_13(model_data *data)
{
    static const int equationIndexes = 13;
    $P$DER$PRemoved = ((DIVISION_SIM($PMortalityProb,$PMortalityTime,"MortalityTime",equationIndexes) + DIVISION_SIM((1.0 - $PMortalityProb),$PRecoveryTime,"RecoveryTime",equationIndexes)) * $PInfectious);
}
/*
 equation index: 14
 type: SIMPLE_ASSIGN
 der(Deceased) = MortalityProb * der(Removed)
 */
static void eqFunction_14(model_data *data)
{
    static const int equationIndexes = 14;
    $P$DER$PDeceased = ($PMortalityProb * $P$DER$PRemoved);
}
/*
 equation index: 15
 type: SIMPLE_ASSIGN
 der(Recovered) = (1.0 - MortalityProb) * der(Removed)
 */
static void eqFunction_15(model_data *data)
{
    static const int equationIndexes = 15;
    $P$DER$PRecovered = ((1.0 - $PMortalityProb) * $P$DER$PRemoved);
}
/*
 equation index: 16
 type: SIMPLE_ASSIGN
 Population = Recovered + Infectious + Susceptible
 */
static void eqFunction_16(model_data *data)
{
    static const int equationIndexes = 16;
    $PPopulation = ($PRecovered + ($PInfectious + $PSusceptible));
}
/*
 equation index: 17
 type: SIMPLE_ASSIGN
 R = TransmissionProb * EncounterRate * DIVISION(Susceptible, Population)
 */
static void eqFunction_17(model_data *data)
{
    static const int equationIndexes = 17;
    $PR = ($PTransmissionProb * ($PEncounterRate * DIVISION_SIM($PSusceptible,$PPopulation,"Population",equationIndexes)));
}
/*
 equation index: 18
 type: SIMPLE_ASSIGN
 der(Susceptible) = (-R) * Infectious
 */
static void eqFunction_18(model_data *data)
{
    static const int equationIndexes = 18;
    $P$DER$PSusceptible = ((-$PR) * $PInfectious);
}
/*
 equation index: 19
 type: SIMPLE_ASSIGN
 der(Infectious) = 1.0 + R * Infectious + sin(0.01095890410958904 * time) - der(Removed)
 */
static void eqFunction_19(model_data *data)
{
    static const int equationIndexes = 19;
    $P$DER$PInfectious = (1.0 + (($PR * $PInfectious) + (sin((0.01095890410958904 * time)) - $P$DER$PRemoved)));
}

static void setupEquationGraph(model_data *data)
{
    data->link(eqFunction_13,&$P$DER$PRemoved);
    data->link(&$PMortalityProb,eqFunction_13);
    data->link(&$PMortalityTime,eqFunction_13);
    data->link(&$PMortalityProb,eqFunction_13);
    data->link(&$PRecoveryTime,eqFunction_13);
    data->link(&$PInfectious,eqFunction_13);
    data->link(eqFunction_14,&$P$DER$PDeceased);
    data->link(&$PMortalityProb,eqFunction_14);
    data->link(&$PRemoved,eqFunction_14);
    data->link(eqFunction_15,&$P$DER$PRecovered);
    data->link(&$PMortalityProb,eqFunction_15);
    data->link(&$PRemoved,eqFunction_15);
    data->link(eqFunction_16,&$PPopulation);
    data->link(&$PRecovered,eqFunction_16);
    data->link(&$PInfectious,eqFunction_16);
    data->link(&$PSusceptible,eqFunction_16);
    data->link(eqFunction_17,&$PR);
    data->link(&$PTransmissionProb,eqFunction_17);
    data->link(&$PEncounterRate,eqFunction_17);
    data->link(&$PSusceptible,eqFunction_17);
    data->link(&$PPopulation,eqFunction_17);
    data->link(eqFunction_18,&$P$DER$PSusceptible);
    data->link(&$PR,eqFunction_18);
    data->link(&$PInfectious,eqFunction_18);
    data->link(eqFunction_19,&$P$DER$PInfectious);
    data->link(&$PR,eqFunction_19);
    data->link(&$PInfectious,eqFunction_19);
    data->link(&time,eqFunction_19);
    data->link(&$PRemoved,eqFunction_19);
}

// Set values for all variables that define a start value
static void setDefaultStartValues(model_data *comp)
{
    comp->Time = 0.0;
    comp->real_vars[$PDeceased_] = 0.0;
    comp->real_vars[$PInfectious_] = 0.0;
    comp->real_vars[$PRecovered_] = 0.0;
    comp->real_vars[$PRemoved_] = 0.0;
    comp->real_vars[$PSusceptible_] = 499000.0;
    comp->real_vars[$P$DER$PDeceased_] = 0;
    comp->real_vars[$P$DER$PInfectious_] = 0;
    comp->real_vars[$P$DER$PRecovered_] = 0;
    comp->real_vars[$P$DER$PRemoved_] = 0;
    comp->real_vars[$P$DER$PSusceptible_] = 0;
    comp->real_vars[$PPopulation_] = 0;
    comp->real_vars[$PR_] = 0;
    comp->real_vars[$PEncounterRate_] = 4.0;
    comp->real_vars[$PMortalityProb_] = 0.01;
    comp->real_vars[$PMortalityTime_] = 1.0;
    comp->real_vars[$PRecoveryTime_] = 3.0;
    comp->real_vars[$PTransmissionProb_] = 0.15;
}


static void updateAll(model_data* data)
{
    eqFunction_13(data);
    eqFunction_14(data);
    eqFunction_15(data);
    eqFunction_16(data);
    eqFunction_17(data);
    eqFunction_18(data);
    eqFunction_19(data);

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
    data->update();
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
    updateAll(data);
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

 fmi2Status fmi2NewDiscreteStates(fmi2Component, fmi2EventInfo*)
 {
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
        NUMBER_OF_REALS,NUMBER_OF_INTEGERS,NUMBER_OF_STRINGS,NUMBER_OF_BOOLEANS);
    setupEquationGraph(data);
    setDefaultStartValues(data);
    updateAll(data);
    return static_cast<fmi2Component>(data);
}

void
fmi2FreeInstance(fmi2Component c)
{
    model_data* data = static_cast<model_data*>(c);
    if (data != NULL) delete data;
}


