// define class name and unique id
#ifdef FMI_FROM_SOURCE
#define FMI2_FUNCTION_PREFIX Linsys_
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
#define NUMBER_OF_STATES 2
#define NUMBER_OF_EVENT_INDICATORS 0
#define NUMBER_OF_REALS 8
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_EXTERNALFUNCTIONS 0

// define variable data for model
#define timeValue (data->Time)
#define _x_0__ 0 
#define _x_0_ (data->real_vars[0]) 
#define _x_1__ 1 
#define _x_1_ (data->real_vars[1]) 
#define _DER_x_0__ 2 
#define _DER_x_0_ (data->real_vars[2]) 
#define _DER_x_1__ 3 
#define _DER_x_1_ (data->real_vars[3]) 
#define _A_0_0__ 4 
#define _A_0_0_ (data->real_vars[4]) 
#define _A_0_1__ 5 
#define _A_0_1_ (data->real_vars[5]) 
#define _A_1_0__ 6 
#define _A_1_0_ (data->real_vars[6]) 
#define _A_1_1__ 7 
#define _A_1_1_ (data->real_vars[7]) 

#define PRE_x_0__ 0 
#define PRE_x_0_ (data->pre_real_vars[0]) 
#define PRE_x_1__ 1 
#define PRE_x_1_ (data->pre_real_vars[1]) 
#define PRE_DER_x_0__ 2 
#define PRE_DER_x_0_ (data->pre_real_vars[2]) 
#define PRE_DER_x_1__ 3 
#define PRE_DER_x_1_ (data->pre_real_vars[3]) 
#define PRE_A_0_0__ 4 
#define PRE_A_0_0_ (data->pre_real_vars[4]) 
#define PRE_A_0_1__ 5 
#define PRE_A_0_1_ (data->pre_real_vars[5]) 
#define PRE_A_1_0__ 6 
#define PRE_A_1_0_ (data->pre_real_vars[6]) 
#define PRE_A_1_1__ 7 
#define PRE_A_1_1_ (data->pre_real_vars[7]) 

// define initial state vector as vector of value references
static const fmi2ValueReference STATES[NUMBER_OF_STATES] = { _x_0__, _x_1__ };
static const fmi2ValueReference STATESDERIVATIVES[NUMBER_OF_STATES] = { _DER_x_0__, _DER_x_1__ };


// dynamic equation functions


/*
 equation index: 7
 type: SIMPLE_ASSIGN
 der(x[2]) = A[2,1] * x[1] + A[2,2] * x[2]
 */
static void eqFunction_7(model_data *data)
{
    _DER_x_1_ = ((_A_1_0_ * _x_0_) + (_A_1_1_ * _x_1_));
}
/*
 equation index: 8
 type: SIMPLE_ASSIGN
 der(x[1]) = A[1,1] * x[1] + A[1,2] * x[2]
 */
static void eqFunction_8(model_data *data)
{
    _DER_x_0_ = ((_A_0_0_ * _x_0_) + (_A_0_1_ * _x_1_));
}

// Zero crossing functions


// Dependency graph for sparse updates
static void setupEquationGraph(model_data *data)
{
    data->link(eqFunction_7,&_DER_x_1_);
    data->link(&_A_1_0_,eqFunction_7);
    data->link(&_x_0_,eqFunction_7);
    data->link(&_A_1_1_,eqFunction_7);
    data->link(&_x_1_,eqFunction_7);
    data->link(eqFunction_8,&_DER_x_0_);
    data->link(&_A_0_0_,eqFunction_8);
    data->link(&_x_0_,eqFunction_8);
    data->link(&_A_0_1_,eqFunction_8);
    data->link(&_x_1_,eqFunction_8);
}

// initial condition equations


/*
 equation index: 1
 type: SIMPLE_ASSIGN
 x[2] = 2.0
 */
static void eqFunction_1(model_data *data)
{
    _x_1_ = 2.0;
}
/*
 equation index: 2
 type: SIMPLE_ASSIGN
 x[1] = 1.0
 */
static void eqFunction_2(model_data *data)
{
    _x_0_ = 1.0;
}
/*
 equation index: 3
 type: SIMPLE_ASSIGN
 der(x[2]) = A[2,1] * x[1] + A[2,2] * x[2]
 */
static void eqFunction_3(model_data *data)
{
    _DER_x_1_ = ((_A_1_0_ * _x_0_) + (_A_1_1_ * _x_1_));
}
/*
 equation index: 4
 type: SIMPLE_ASSIGN
 der(x[1]) = A[1,1] * x[1] + A[1,2] * x[2]
 */
static void eqFunction_4(model_data *data)
{
    _DER_x_0_ = ((_A_0_0_ * _x_0_) + (_A_0_1_ * _x_1_));
}

// Set values for all variables that define a start value
static void setDefaultStartValues(model_data *comp)
{
    comp->Time = 0.0;
    comp->real_vars[_x_0__] = 0;
    comp->real_vars[_x_1__] = 0;
    comp->real_vars[_DER_x_0__] = 0;
    comp->real_vars[_DER_x_1__] = 0;
    comp->real_vars[_A_0_0__] = -0.5;
    comp->real_vars[_A_0_1__] = 0.0;
    comp->real_vars[_A_1_0__] = 0.0;
    comp->real_vars[_A_1_1__] = -1.0;
}


// Solve for unknowns in the model's initial equations
static void initialEquations(model_data* data)
{
    eqFunction_1(data);
    eqFunction_2(data);
    eqFunction_3(data);
    eqFunction_4(data);

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


