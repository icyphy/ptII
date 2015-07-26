/* ---------------------------------------------------------------------------*
 * fmuTemplate.c
 * Implementation of the FMI interface based on functions and macros to
 * be defined by the includer of this file.
 * If FMI_COSIMULATION is defined, this implements "FMI for Co-Simulation 2.0",
 * otherwise "FMI for Model Exchange 2.0".
 * The "FMI for Co-Simulation 2.0", implementation assumes that exactly the
 * following capability flags are set to fmi2True:
 *    canHandleVariableCommunicationStepSize, i.e. fmi2DoStep step size can vary
 * and all other capability flags are set to default, i.e. to fmi2False or 0.
 *
 * Revision history
 *  07.03.2014 initial version released in FMU SDK 2.0.0
 *  02.04.2014 allow modules to request termination of simulation, better time
 *             event handling, initialize() moved from fmi2EnterInitialization to
 *             fmi2ExitInitialization, correct logging message format in fmi2DoStep.
 *  10.04.2014 use FMI 2.0 headers that prefix function and types names with 'fmi2'.
 *  13.06.2014 when fmi2setDebugLogging is called with 0 categories, set all
 *             categories to loggingOn value.
 *  09.07.2014 track all states of Model-exchange and Co-simulation and check
 *             the allowed calling sequences, explicit isTimeEvent parameter for
 *             eventUpdate function of the model, lazy computation of computed values.
 *
 * Author: Adrian Tirea
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// macro to be used to log messages. The macro check if current
// log category is valid and, if true, call the logger provided by simulator.
#define FILTERED_LOG(instance, status, categoryIndex, message, ...) if (isCategoryLogged(instance, categoryIndex)) \
        instance->functions->logger(instance->functions->componentEnvironment, instance->instanceName, status, \
        logCategoriesNames[categoryIndex], message, ##__VA_ARGS__);

static fmi2String logCategoriesNames[] = {"logAll", "logError", "logFmiCall", "logEvent"};

// array of value references of states
#if NUMBER_OF_REALS>0
fmi2ValueReference vrStates[NUMBER_OF_STATES] = STATES;
#endif

#ifndef max
#define max(a,b) ((a)>(b) ? (a) : (b))
#endif

// ---------------------------------------------------------------------------
// Private helpers used below to validate function arguments
// ---------------------------------------------------------------------------

fmi2Boolean isCategoryLogged(ModelInstance *comp, int categoryIndex);

#ifndef FMI_COSIMULATION
static fmi2Boolean invalidNumber(ModelInstance *comp, const char *f, const char *arg, int n, int nExpected) {
    if (n != nExpected) {
        comp->state = modelError;
        FILTERED_LOG(comp, fmi2Error, LOG_ERROR, "%s: Invalid argument %s = %d. Expected %d.", f, arg, n, nExpected)
        return fmi2True;
    }
    return fmi2False;
}
#endif

static fmi2Boolean invalidState(ModelInstance *comp, const char *f, int statesExpected) {
    if (!comp)
        return fmi2True;
    if (!(comp->state & statesExpected)) {
        comp->state = modelError;
        FILTERED_LOG(comp, fmi2Error, LOG_ERROR, "%s: Illegal call sequence.", f)
        return fmi2True;
    }
    return fmi2False;
}

static fmi2Boolean nullPointer(ModelInstance* comp, const char *f, const char *arg, const void *p) {
    if (!p) {
        comp->state = modelError;
        FILTERED_LOG(comp, fmi2Error, LOG_ERROR, "%s: Invalid argument %s = NULL.", f, arg)
        return fmi2True;
    }
    return fmi2False;
}

static fmi2Boolean vrOutOfRange(ModelInstance *comp, const char *f, fmi2ValueReference vr, int end) {
    if (vr >= end) {
        FILTERED_LOG(comp, fmi2Error, LOG_ERROR, "%s: Illegal value reference %u.", f, vr)
        comp->state = modelError;
        return fmi2True;
    }
    return fmi2False;
}

static fmi2Status unsupportedFunction(fmi2Component c, const char *fName, int statesExpected) {
    ModelInstance *comp = (ModelInstance *)c;
    //fmi2CallbackLogger log = comp->functions->logger;
    if (invalidState(comp, fName, statesExpected))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, fName);
    FILTERED_LOG(comp, fmi2Error, LOG_ERROR, "%s: Function not implemented.", fName)
    return fmi2Error;
}

fmi2Status setString(fmi2Component comp, fmi2ValueReference vr, fmi2String value, const fmi2Integer hybridValue) {
    return fmi2SetHybridString(comp, &vr, 1, &value, &hybridValue);
}

// ---------------------------------------------------------------------------
// Private helpers logger
// ---------------------------------------------------------------------------

// return fmi2True if logging category is on. Else return fmi2False.
fmi2Boolean isCategoryLogged(ModelInstance *comp, int categoryIndex) {
    if (categoryIndex < NUMBER_OF_CATEGORIES
        && (comp->logCategories[categoryIndex] || comp->logCategories[LOG_ALL])) {
        return fmi2True;
    }
    return fmi2False;
}

// ---------------------------------------------------------------------------
// FMI functions
// ---------------------------------------------------------------------------
fmi2Component fmi2Instantiate(fmi2String instanceName, fmi2Type fmuType, fmi2String fmuGUID,
                            fmi2String fmuResourceLocation, const fmi2CallbackFunctions *functions,
                            fmi2Boolean visible, fmi2Boolean loggingOn) {
    // ignoring arguments: fmuResourceLocation, visible
    ModelInstance *comp;
    if (!functions->logger) {
        return NULL;
    }

    if (!functions->allocateMemory || !functions->freeMemory) {
        functions->logger(functions->componentEnvironment, instanceName, fmi2Error, "error",
                "fmi2Instantiate: Missing callback function.");
        return NULL;
    }
    if (!instanceName || strlen(instanceName) == 0) {
        functions->logger(functions->componentEnvironment, "?", fmi2Error, "error",
                "fmi2Instantiate: Missing instance name.");
        return NULL;
    }
    if (!fmuGUID || strlen(fmuGUID) == 0) {
        functions->logger(functions->componentEnvironment, instanceName, fmi2Error, "error",
                "fmi2Instantiate: Missing GUID.");
        return NULL;
    }
    if (strcmp(fmuGUID, MODEL_GUID)) {
        functions->logger(functions->componentEnvironment, instanceName, fmi2Error, "error",
                "fmi2Instantiate: Wrong GUID %s. Expected %s.", fmuGUID, MODEL_GUID);
        return NULL;
    }
    comp = (ModelInstance *)functions->allocateMemory(1, sizeof(ModelInstance));
    if (comp) {
        int i;
        comp->r = (fmi2Real *)   functions->allocateMemory(NUMBER_OF_REALS,    sizeof(fmi2Real));
        comp->i = (fmi2Integer *)functions->allocateMemory(NUMBER_OF_INTEGERS, sizeof(fmi2Integer));
        comp->b = (fmi2Boolean *)functions->allocateMemory(NUMBER_OF_BOOLEANS, sizeof(fmi2Boolean));
        comp->s = (fmi2String *) functions->allocateMemory(NUMBER_OF_STRINGS,  sizeof(fmi2String));
        comp->isPositive = (fmi2Boolean *)functions->allocateMemory(NUMBER_OF_EVENT_INDICATORS,
            sizeof(fmi2Boolean));
        comp->hr = (fmi2Integer *)functions->allocateMemory(NUMBER_OF_REALS,     sizeof(fmi2Integer));
        comp->hi = (fmi2Integer *)functions->allocateMemory(NUMBER_OF_INTEGERS,  sizeof(fmi2Integer));
        comp->hb = (fmi2Integer *)functions->allocateMemory(NUMBER_OF_BOOLEANS,  sizeof(fmi2Integer));
        comp->hs = (fmi2Integer *)functions->allocateMemory(NUMBER_OF_STRINGS,   sizeof(fmi2Integer));
        comp->instanceName = functions->allocateMemory(1 + strlen(instanceName), sizeof(char));
        comp->GUID = functions->allocateMemory(1 + strlen(fmuGUID), sizeof(char));

        // set all categories to on or off. fmi2SetDebugLogging should be called to choose specific categories.
        for (i = 0; i < NUMBER_OF_CATEGORIES; i++) {
            comp->logCategories[i] = loggingOn;
        }
    }
    if (!comp || !comp->r || !comp->i || !comp->b || !comp->s || !comp->hr || !comp->hi || !comp->hb
        || !comp->hs || !comp->isPositive || !comp->instanceName || !comp->GUID) {

        functions->logger(functions->componentEnvironment, instanceName, fmi2Error, "error",
            "fmi2Instantiate: Out of memory.");
        return NULL;
    }
    comp->time = 0; // overwrite in fmi2SetupExperiment, fmi2SetTime
    strcpy((char *)comp->instanceName, (char *)instanceName);
    comp->type = fmuType;
    strcpy((char *)comp->GUID, (char *)fmuGUID);
    comp->functions = functions;
    comp->componentEnvironment = functions->componentEnvironment;
    comp->loggingOn = loggingOn;
    comp->state = modelInstantiated;
    setStartValues(comp); // to be implemented by the includer of this file
    comp->isDirtyValues = 1; // because we just called setStartValues

    comp->eventInfo.newDiscreteStatesNeeded = fmi2False;
    comp->eventInfo.terminateSimulation = fmi2False;
    comp->eventInfo.nominalsOfContinuousStatesChanged = fmi2False;
    comp->eventInfo.valuesOfContinuousStatesChanged = fmi2False;
    comp->eventInfo.nextEventTimeDefined = fmi2False;
    comp->eventInfo.nextEventTime = 0;

    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2Instantiate: GUID=%s", fmuGUID)

    return comp;
}

fmi2Status fmi2SetupExperiment(fmi2Component c, fmi2Boolean toleranceDefined, fmi2Real tolerance,
                            fmi2Real startTime, fmi2Boolean stopTimeDefined, fmi2Real stopTime) {
    return fmi2OK;
}

fmi2Status fmi2HybridSetupExperiment(fmi2Component c, fmi2Boolean toleranceDefined, fmi2Integer tolerance,
                            fmi2Integer startTime, fmi2Boolean stopTimeDefined, fmi2Integer stopTime) {

    // ignore arguments: stopTimeDefined, stopTime
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2SetupExperiment", MASK_fmi2SetupExperiment))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetupExperiment: toleranceDefined=%d tolerance=%u",
        toleranceDefined, tolerance)

    comp->time = startTime;
    return fmi2OK;
}

fmi2Status fmi2EnterInitializationMode(fmi2Component c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2EnterInitializationMode", MASK_fmi2EnterInitializationMode))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2EnterInitializationMode")

    comp->state = modelInitializationMode;
    return fmi2OK;
}

fmi2Status fmi2ExitInitializationMode(fmi2Component c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2ExitInitializationMode", MASK_fmi2ExitInitializationMode))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2ExitInitializationMode")

    // if values were set and no fmi2GetXXX triggered update before,
    // ensure calculated values are updated now
    if (comp->isDirtyValues) {
        calculateValues(comp);
        comp->isDirtyValues = 0;
    }

    if (comp->type == fmi2ModelExchange) comp->state = modelEventMode;
    else comp->state = modelStepComplete;
    return fmi2OK;
}

fmi2Status fmi2Terminate(fmi2Component c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2Terminate", MASK_fmi2Terminate))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2Terminate")

    comp->state = modelTerminated;
    return fmi2OK;
}

fmi2Status fmi2Reset(fmi2Component c) {
    ModelInstance* comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2Reset", MASK_fmi2Reset))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2Reset")

    comp->state = modelInstantiated;
    setStartValues(comp); // to be implemented by the includer of this file
    comp->isDirtyValues = 1; // because we just called setStartValues
    return fmi2OK;
}

void fmi2FreeInstance(fmi2Component c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (!comp) return;
    if (invalidState(comp, "fmi2FreeInstance", MASK_fmi2FreeInstance))
        return;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2FreeInstance")

    if (comp->r) comp->functions->freeMemory(comp->r);
    if (comp->i) comp->functions->freeMemory(comp->i);
    if (comp->b) comp->functions->freeMemory(comp->b);
    if (comp->s) {
        int i;
        for (i = 0; i < NUMBER_OF_STRINGS; i++){
            if (comp->s[i]) comp->functions->freeMemory((void *)comp->s[i]);
        }
        comp->functions->freeMemory((void *)comp->s);
    }
    if (comp->isPositive) comp->functions->freeMemory(comp->isPositive);
    if (comp->instanceName) comp->functions->freeMemory((void *)comp->instanceName);
    if (comp->GUID) comp->functions->freeMemory((void *)comp->GUID);
    comp->functions->freeMemory(comp);
}

// ---------------------------------------------------------------------------
// FMI functions: class methods not depending of a specific model instance
// ---------------------------------------------------------------------------

const char* fmi2GetVersion() {
    return fmi2Version;
}

const char* fmi2GetTypesPlatform() {
    return fmi2TypesPlatform;
}

// ---------------------------------------------------------------------------
// FMI functions: logging control, setters and getters for Real, Integer,
// Boolean, String
// ---------------------------------------------------------------------------

fmi2Status fmi2SetDebugLogging(fmi2Component c, fmi2Boolean loggingOn, size_t nCategories, const fmi2String categories[]) {
    // ignore arguments: nCategories, categories
    int i, j;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2SetDebugLogging", MASK_fmi2SetDebugLogging))
        return fmi2Error;
    comp->loggingOn = loggingOn;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetDebugLogging")

    // reset all categories
    for (j = 0; j < NUMBER_OF_CATEGORIES; j++) {
        comp->logCategories[j] = fmi2False;
    }

    if (nCategories == 0) {
        // no category specified, set all categories to have loggingOn value
        for (j = 0; j < NUMBER_OF_CATEGORIES; j++) {
            comp->logCategories[j] = loggingOn;
        }
    } else {
        // set specific categories on
        for (i = 0; i < nCategories; i++) {
            fmi2Boolean categoryFound = fmi2False;
            for (j = 0; j < NUMBER_OF_CATEGORIES; j++) {
                if (strcmp(logCategoriesNames[j], categories[i]) == 0) {
                    comp->logCategories[j] = loggingOn;
                    categoryFound = fmi2True;
                    break;
                }
            }
            if (!categoryFound) {
                comp->functions->logger(comp->componentEnvironment, comp->instanceName, fmi2Warning,
                    logCategoriesNames[LOG_ERROR],
                    "logging category '%s' is not supported by model", categories[i]);
            }
        }
    }

    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetDebugLogging")
    return fmi2OK;
}

// fmi2Status fmi2GetReal (fmi2Component c, const fmi2ValueReference vr[], size_t nvr, fmi2Real value[]) {
// #if NUMBER_OF_REALS > 0
//     int i;
// #endif
//     ModelInstance *comp = (ModelInstance *)c;
//     if (invalidState(comp, "fmi2GetReal", MASK_fmi2GetReal))
//         return fmi2Error;
//     if (nvr > 0 && nullPointer(comp, "fmi2GetReal", "vr[]", vr))
//         return fmi2Error;
//     if (nvr > 0 && nullPointer(comp, "fmi2GetReal", "value[]", value))
//         return fmi2Error;
//     if (nvr > 0 && comp->isDirtyValues) {
//         calculateValues(comp);
//         comp->isDirtyValues = 0;
//     }
// #if NUMBER_OF_REALS > 0
//     for (i = 0; i < nvr; i++) {
//         if (vrOutOfRange(comp, "fmi2GetReal", vr[i], NUMBER_OF_REALS))
//             return fmi2Error;
//         value[i] = getReal(comp, vr[i]); // to be implemented by the includer of this file
//         FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2GetReal: #r%u# = %.16g", vr[i], value[i])
//     }
// #endif
//     return fmi2OK;
// }

fmi2Status fmi2GetHybridReal (fmi2Component c, const fmi2ValueReference vr[], size_t nvr, fmi2Real value[], fmi2Integer hybridValue[]) {
#if NUMBER_OF_REALS > 0
    int i;
#endif
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2GetHybridReal", MASK_fmi2GetReal))
        return fmi2Error;
    if (nvr > 0 && nullPointer(comp, "fmi2GetHybridReal", "vr[]", vr))
        return fmi2Error;
    if (nvr > 0 && nullPointer(comp, "fmi2GetHybridReal", "value[]", value))
        return fmi2Error;
    if (nvr > 0 && comp->isDirtyValues) {
        calculateValues(comp);
        comp->isDirtyValues = 0;
    }
#if NUMBER_OF_REALS > 0
    for (i = 0; i < nvr; i++) {
        if (vrOutOfRange(comp, "fmi2GetHybridReal", vr[i], NUMBER_OF_REALS))
            return fmi2Error;
        hybridValue[i] = comp->hr[vr[i]];
        if (comp->hr[vr[i]] == 1) {
            value[i] = getReal(comp, vr[i]); // to be implemented by the includer of this file
        }
        FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2GetHybridReal: #r%u# = %.16g", vr[i], value[i])
    }
#endif
    return fmi2OK;
}

// fmi2Status fmi2GetInteger(fmi2Component c, const fmi2ValueReference vr[], size_t nvr, fmi2Integer value[]) {
//     int i;
//     ModelInstance *comp = (ModelInstance *)c;
//     if (invalidState(comp, "fmi2GetInteger", MASK_fmi2GetInteger))
//         return fmi2Error;
//     if (nvr > 0 && nullPointer(comp, "fmi2GetInteger", "vr[]", vr))
//             return fmi2Error;
//     if (nvr > 0 && nullPointer(comp, "fmi2GetInteger", "value[]", value))
//             return fmi2Error;
//     if (nvr > 0 && comp->isDirtyValues) {
//         calculateValues(comp);
//         comp->isDirtyValues = 0;
//     }
//     for (i = 0; i < nvr; i++) {
//         if (vrOutOfRange(comp, "fmi2GetInteger", vr[i], NUMBER_OF_INTEGERS))
//             return fmi2Error;
//         value[i] = comp->i[vr[i]];
//         FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2GetInteger: #i%u# = %d", vr[i], value[i])
//     }
//     return fmi2OK;
// }

fmi2Status fmi2GetHybridInteger(fmi2Component c, const fmi2ValueReference vr[], size_t nvr, fmi2Integer value[], fmi2Integer hybridValue[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2GetHybridInteger", MASK_fmi2GetInteger))
        return fmi2Error;
    if (nvr > 0 && nullPointer(comp, "fmi2GetHybridInteger", "vr[]", vr))
            return fmi2Error;
    if (nvr > 0 && nullPointer(comp, "fmi2GetHybridInteger", "value[]", value))
            return fmi2Error;
    if (nvr > 0 && comp->isDirtyValues) {
        calculateValues(comp);
        comp->isDirtyValues = 0;
    }
    for (i = 0; i < nvr; i++) {
        if (vrOutOfRange(comp, "fmi2GetHybridInteger", vr[i], NUMBER_OF_INTEGERS))
            return fmi2Error;
        hybridValue[i] = comp->hi[vr[i]];
        value[i] = comp->i[vr[i]];
        FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2GetHybridInteger: #i%u# = %d", vr[i], value[i])
    }
    return fmi2OK;
}

// fmi2Status fmi2GetBoolean(fmi2Component c, const fmi2ValueReference vr[], size_t nvr, fmi2Boolean value[]) {
//     int i;
//     ModelInstance *comp = (ModelInstance *)c;
//     if (invalidState(comp, "fmi2GetBoolean", MASK_fmi2GetBoolean))
//         return fmi2Error;
//     if (nvr > 0 && nullPointer(comp, "fmi2GetBoolean", "vr[]", vr))
//             return fmi2Error;
//     if (nvr > 0 && nullPointer(comp, "fmi2GetBoolean", "value[]", value))
//             return fmi2Error;
//     if (nvr > 0 && comp->isDirtyValues) {
//         calculateValues(comp);
//         comp->isDirtyValues = 0;
//     }
//     for (i = 0; i < nvr; i++) {
//         if (vrOutOfRange(comp, "fmi2GetBoolean", vr[i], NUMBER_OF_BOOLEANS))
//             return fmi2Error;
//         value[i] = comp->b[vr[i]];
//         FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2GetBoolean: #b%u# = %s", vr[i], value[i]? "true" : "false")
//     }
//     return fmi2OK;
// }

fmi2Status fmi2GetHybridBoolean(fmi2Component c, const fmi2ValueReference vr[], size_t nvr, fmi2Boolean value[], fmi2Integer hybridValue[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2GetHybridBoolean", MASK_fmi2GetBoolean))
        return fmi2Error;
    if (nvr > 0 && nullPointer(comp, "fmi2GetHybridBoolean", "vr[]", vr))
            return fmi2Error;
    if (nvr > 0 && nullPointer(comp, "fmi2GetHybridBoolean", "value[]", value))
            return fmi2Error;
    if (nvr > 0 && comp->isDirtyValues) {
        calculateValues(comp);
        comp->isDirtyValues = 0;
    }
    for (i = 0; i < nvr; i++) {
        if (vrOutOfRange(comp, "fmi2GetHybridBoolean", vr[i], NUMBER_OF_BOOLEANS))
            return fmi2Error;
        hybridValue[i] = comp->hb[vr[i]];
        value[i] = comp->b[vr[i]];
        FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2GetHybridBoolean: #b%u# = %s", vr[i], value[i]? "true" : "false")
    }
    return fmi2OK;
}

// fmi2Status fmi2GetString (fmi2Component c, const fmi2ValueReference vr[], size_t nvr, fmi2String value[]) {
//     int i;
//     ModelInstance *comp = (ModelInstance *)c;
//     if (invalidState(comp, "fmi2GetString", MASK_fmi2GetString))
//         return fmi2Error;
//     if (nvr>0 && nullPointer(comp, "fmi2GetString", "vr[]", vr))
//             return fmi2Error;
//     if (nvr>0 && nullPointer(comp, "fmi2GetString", "value[]", value))
//             return fmi2Error;
//     if (nvr > 0 && comp->isDirtyValues) {
//         calculateValues(comp);
//         comp->isDirtyValues = 0;
//     }
//     for (i=0; i<nvr; i++) {
//         if (vrOutOfRange(comp, "fmi2GetString", vr[i], NUMBER_OF_STRINGS))
//             return fmi2Error;
//         value[i] = comp->s[vr[i]];
//         FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2GetString: #s%u# = '%s'", vr[i], value[i])
//     }
//     return fmi2OK;
// }

fmi2Status fmi2GetHybridString (fmi2Component c, const fmi2ValueReference vr[], size_t nvr, fmi2String value[], fmi2Integer hybridValue[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2GetHybridString", MASK_fmi2GetString))
        return fmi2Error;
    if (nvr>0 && nullPointer(comp, "fmi2GetHybridString", "vr[]", vr))
            return fmi2Error;
    if (nvr>0 && nullPointer(comp, "fmi2GetHybridString", "value[]", value))
            return fmi2Error;
    if (nvr > 0 && comp->isDirtyValues) {
        calculateValues(comp);
        comp->isDirtyValues = 0;
    }
    for (i=0; i<nvr; i++) {
        if (vrOutOfRange(comp, "fmi2GetHybridString", vr[i], NUMBER_OF_STRINGS))
            return fmi2Error;
        hybridValue[i] = comp->hs[vr[i]];
        value[i] = comp->s[vr[i]];
        FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2GetHybridString: #s%u# = '%s'", vr[i], value[i])
    }
    return fmi2OK;
}

// fmi2Status fmi2SetReal (fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2Real value[]) {
//     int i;
//     ModelInstance *comp = (ModelInstance *)c;
//     if (invalidState(comp, "fmi2SetReal", MASK_fmi2SetReal))
//         return fmi2Error;
//     if (nvr > 0 && nullPointer(comp, "fmi2SetReal", "vr[]", vr))
//         return fmi2Error;
//     if (nvr > 0 && nullPointer(comp, "fmi2SetReal", "value[]", value))
//         return fmi2Error;
//     FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetReal: nvr = %d", nvr)
//     // no check whether setting the value is allowed in the current state
//     for (i = 0; i < nvr; i++) {
//         if (vrOutOfRange(comp, "fmi2SetReal", vr[i], NUMBER_OF_REALS))
//             return fmi2Error;
//         FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetReal: #r%d# = %.16g", vr[i], value[i])
//         comp->r[vr[i]] = value[i];
//     }
//     if (nvr > 0) comp->isDirtyValues = 1;
//     return fmi2OK;
// }

fmi2Status fmi2SetHybridReal (fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2Real value[], const fmi2Integer hybridValue[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2SetHybridReal", MASK_fmi2SetReal))
        return fmi2Error;
    if (nvr > 0 && nullPointer(comp, "fmi2SetHybridReal", "vr[]", vr))
        return fmi2Error;
    if (nvr > 0 && nullPointer(comp, "fmi2SetHybridReal", "value[]", value))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetHybridReal: nvr = %d", nvr)
    // no check whether setting the value is allowed in the current state
    for (i = 0; i < nvr; i++) {
        if (vrOutOfRange(comp, "fmi2SetHybridReal", vr[i], NUMBER_OF_REALS))
            return fmi2Error;
        FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetHybridReal: #r%d# = %.16g", vr[i], value[i])
        comp->r[vr[i]] = value[i];
        comp->hr[vr[i]] = hybridValue[i];
    }
    if (nvr > 0) comp->isDirtyValues = 1;
    return fmi2OK;
}

// fmi2Status fmi2SetInteger(fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2Integer value[]) {
//     int i;
//     ModelInstance *comp = (ModelInstance *)c;
//     if (invalidState(comp, "fmi2SetInteger", MASK_fmi2SetInteger))
//         return fmi2Error;
//     if (nvr > 0 && nullPointer(comp, "fmi2SetInteger", "vr[]", vr))
//         return fmi2Error;
//     if (nvr > 0 && nullPointer(comp, "fmi2SetInteger", "value[]", value))
//         return fmi2Error;
//     FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetInteger: nvr = %d", nvr)

//     for (i = 0; i < nvr; i++) {
//         if (vrOutOfRange(comp, "fmi2SetInteger", vr[i], NUMBER_OF_INTEGERS))
//             return fmi2Error;
//         FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetInteger: #i%d# = %d", vr[i], value[i])
//         comp->i[vr[i]] = value[i];
//     }
//     if (nvr > 0) comp->isDirtyValues = 1;
//     return fmi2OK;
// }

fmi2Status fmi2SetHybridInteger(fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2Integer value[], const fmi2Integer hybridValue[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2SetHybridInteger", MASK_fmi2SetInteger))
        return fmi2Error;
    if (nvr > 0 && nullPointer(comp, "fmi2SetHybridInteger", "vr[]", vr))
        return fmi2Error;
    if (nvr > 0 && nullPointer(comp, "fmi2SetHybridInteger", "value[]", value))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetHybridInteger: nvr = %d", nvr)

    for (i = 0; i < nvr; i++) {
        if (vrOutOfRange(comp, "fmi2SetHybridInteger", vr[i], NUMBER_OF_INTEGERS))
            return fmi2Error;
        FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetHybridInteger: #i%d# = %d", vr[i], value[i])
        comp->i[vr[i]] = value[i];
        comp->hi[vr[i]] = hybridValue[i];
    }
    if (nvr > 0) comp->isDirtyValues = 1;
    return fmi2OK;
}

// fmi2Status fmi2SetBoolean(fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2Boolean value[]) {
//     int i;
//     ModelInstance *comp = (ModelInstance *)c;
//     if (invalidState(comp, "fmi2SetBoolean", MASK_fmi2SetBoolean))
//         return fmi2Error;
//     if (nvr>0 && nullPointer(comp, "fmi2SetBoolean", "vr[]", vr))
//         return fmi2Error;
//     if (nvr>0 && nullPointer(comp, "fmi2SetBoolean", "value[]", value))
//         return fmi2Error;
//     FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetBoolean: nvr = %d", nvr)

//     for (i = 0; i < nvr; i++) {
//         if (vrOutOfRange(comp, "fmi2SetBoolean", vr[i], NUMBER_OF_BOOLEANS))
//             return fmi2Error;
//         FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetBoolean: #b%d# = %s", vr[i], value[i] ? "true" : "false")
//         comp->b[vr[i]] = value[i];
//     }
//     if (nvr > 0) comp->isDirtyValues = 1;
//     return fmi2OK;
// }

fmi2Status fmi2SetHybridBoolean(fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2Boolean value[], const fmi2Integer hybridValue[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2SetHybridBoolean", MASK_fmi2SetBoolean))
        return fmi2Error;
    if (nvr>0 && nullPointer(comp, "fmi2SetHybridBoolean", "vr[]", vr))
        return fmi2Error;
    if (nvr>0 && nullPointer(comp, "fmi2SetHybridBoolean", "value[]", value))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetHybridBoolean: nvr = %d", nvr)

    for (i = 0; i < nvr; i++) {
        if (vrOutOfRange(comp, "fmi2SetHybridBoolean", vr[i], NUMBER_OF_BOOLEANS))
            return fmi2Error;
        FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetHybridBoolean: #b%d# = %s", vr[i], value[i] ? "true" : "false")
        comp->b[vr[i]] = value[i];
        comp->hb[vr[i]] = hybridValue[i];
    }
    if (nvr > 0) comp->isDirtyValues = 1;
    return fmi2OK;
}

// fmi2Status fmi2SetString (fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2String value[]) {
//     int i;
//     ModelInstance *comp = (ModelInstance *)c;
//     if (invalidState(comp, "fmi2SetString", MASK_fmi2SetString))
//         return fmi2Error;
//     if (nvr>0 && nullPointer(comp, "fmi2SetString", "vr[]", vr))
//         return fmi2Error;
//     if (nvr>0 && nullPointer(comp, "fmi2SetString", "value[]", value))
//         return fmi2Error;
//     FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetString: nvr = %d", nvr)

//     for (i = 0; i < nvr; i++) {
//         char *string = (char *)comp->s[vr[i]];
//         if (vrOutOfRange(comp, "fmi2SetString", vr[i], NUMBER_OF_STRINGS))
//             return fmi2Error;
//         FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetString: #s%d# = '%s'", vr[i], value[i])

//         if (value[i] == NULL) {
//             if (string) comp->functions->freeMemory(string);
//             comp->s[vr[i]] = NULL;
//             FILTERED_LOG(comp, fmi2Warning, LOG_ERROR, "fmi2SetString: string argument value[%d] = NULL.", i);
//         } else {
//             if (string == NULL || strlen(string) < strlen(value[i])) {
//                 if (string) comp->functions->freeMemory(string);
//                 comp->s[vr[i]] = comp->functions->allocateMemory(1 + strlen(value[i]), sizeof(char));
//                 if (!comp->s[vr[i]]) {
//                     comp->state = modelError;
//                     FILTERED_LOG(comp, fmi2Error, LOG_ERROR, "fmi2SetString: Out of memory.")
//                     return fmi2Error;
//                 }
//             }
//             strcpy((char *)comp->s[vr[i]], (char *)value[i]);
//         }
//     }
//     if (nvr > 0) comp->isDirtyValues = 1;
//     return fmi2OK;
// }

fmi2Status fmi2SetHybridString (fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2String value[], const fmi2Integer hybridValue[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2SetHybridString", MASK_fmi2SetString))
        return fmi2Error;
    if (nvr>0 && nullPointer(comp, "fmi2SetHybridString", "vr[]", vr))
        return fmi2Error;
    if (nvr>0 && nullPointer(comp, "fmi2SetHybridString", "value[]", value))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetHybridString: nvr = %d", nvr)

    for (i = 0; i < nvr; i++) {
        char *string = (char *)comp->s[vr[i]];
        if (vrOutOfRange(comp, "fmi2SetHybridString", vr[i], NUMBER_OF_STRINGS))
            return fmi2Error;
        FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetHybridString: #s%d# = '%s'", vr[i], value[i])

        if (value[i] == NULL) {
            if (string) comp->functions->freeMemory(string);
            comp->s[vr[i]] = NULL;
            comp->hs[vr[i]] = hybridValue[i];
            FILTERED_LOG(comp, fmi2Warning, LOG_ERROR, "fmi2SetHybridString: string argument value[%d] = NULL.", i);
        } else {
            if (string == NULL || strlen(string) < strlen(value[i])) {
                if (string) comp->functions->freeMemory(string);
                comp->s[vr[i]] = comp->functions->allocateMemory(1 + strlen(value[i]), sizeof(char));
                if (!comp->s[vr[i]]) {
                    comp->state = modelError;
                    FILTERED_LOG(comp, fmi2Error, LOG_ERROR, "fmi2SetHybridString: Out of memory.")
                    return fmi2Error;
                }
            }
            strcpy((char *)comp->s[vr[i]], (char *)value[i]);
            comp->hs[vr[i]] = hybridValue[i];
        }
    }
    if (nvr > 0) comp->isDirtyValues = 1;
    return fmi2OK;
}

fmi2Status fmi2GetFMUstate (fmi2Component c, fmi2FMUstate* FMUstate) {
    ModelInstance *source = (ModelInstance*)c;
    int i;

    // allocating memory for pointers in ModelInstance struct
    ModelInstance *dest;

    if (!*FMUstate) {
        dest = (ModelInstance *)source->functions->allocateMemory(1, sizeof(ModelInstance));
        dest->r = (fmi2Real *)source->functions->allocateMemory(NUMBER_OF_REALS, sizeof(fmi2Real));
        dest->i = (fmi2Integer *)source->functions->allocateMemory(NUMBER_OF_INTEGERS, sizeof(fmi2Integer));
        dest->b = (fmi2Boolean *)source->functions->allocateMemory(NUMBER_OF_BOOLEANS, sizeof(fmi2Boolean));
        dest->s = (fmi2String *)source->functions->allocateMemory(NUMBER_OF_STRINGS, sizeof(fmi2String));
        dest->isPositive = (fmi2Boolean *)source->functions->allocateMemory(NUMBER_OF_EVENT_INDICATORS,
                                                                           sizeof(fmi2Boolean));
    }
    else {
        dest = (ModelInstance *)*FMUstate;
    }


    if (NUMBER_OF_REALS > 0) {
        for (i = 0; i < NUMBER_OF_REALS; i++) {
            dest->r[i] = source->r[i];
        }
    }

    if (NUMBER_OF_INTEGERS > 0) {
        for (i = 0; i < NUMBER_OF_INTEGERS; i++) {
            dest->i[i] = source->i[i];
        }
    }

    if (NUMBER_OF_BOOLEANS > 0) {
        for (i = 0; i < NUMBER_OF_BOOLEANS; i++) {
            dest->b[i] = source->b[i];
        }
    }

    if (NUMBER_OF_STRINGS > 0) {
        for (i = 0; i < NUMBER_OF_STRINGS; i++) {
            if (dest->s[i])
                dest->functions->freeMemory((void *)dest->s[i]);
            dest->s[i] = dest->functions->allocateMemory(1 + strlen(source->s[i]), sizeof(char));
            strcpy((char*)dest->s[i], (char*)source->s[i]);
        }
    }

    if (NUMBER_OF_EVENT_INDICATORS > 0) {
        for (i = 0; i < NUMBER_OF_EVENT_INDICATORS; i++) {
            dest->r[i] = source->r[i];
        }
    }

    *FMUstate = (fmi2FMUstate)dest;

    return fmi2OK;
}

fmi2Status fmi2SetFMUstate (fmi2Component c, fmi2FMUstate FMUstate) {
    ModelInstance *dest = (ModelInstance*)c;
    // allocating memory for pointers in ModelInstance struct

    ModelInstance* source = (ModelInstance*) FMUstate;
    int i;
    if (NUMBER_OF_REALS > 0) {
        for (i = 0; i < NUMBER_OF_REALS; i++) {
            dest->r[i] = source->r[i];
        }
    }
    if (NUMBER_OF_INTEGERS > 0) {
        for (i = 0; i < NUMBER_OF_INTEGERS; i++) {
            dest->i[i] = source->i[i];
        }
    }
    if (NUMBER_OF_BOOLEANS > 0) {
        for (i = 0; i < NUMBER_OF_BOOLEANS; i++) {
            dest->b[i] = source->b[i];
        }
    }
    if (NUMBER_OF_STRINGS > 0) {
        for (i = 0; i < NUMBER_OF_STRINGS; i++) {
            // FIXME: Where does this get freed?
            dest->s[i] = dest->functions->allocateMemory(1 + strlen(source->s[i]), sizeof(char));
            strcpy((char*)dest->s[i], (char*)source->s[i]);
        }
    }
    if (NUMBER_OF_EVENT_INDICATORS > 0) {
        for (i = 0; i < NUMBER_OF_EVENT_INDICATORS; i++) {
            dest->r[i] = source->r[i];
        }
    }

    return fmi2OK;
}

fmi2Status fmi2FreeFMUstate(fmi2Component c, fmi2FMUstate* FMUstate) {
    ModelInstance *comp = (ModelInstance *)c;
    ModelInstance *state = (ModelInstance *)*FMUstate;
    if (!state) return fmi2OK;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmiFreeFMUstate")
    if (state->r) comp->functions->freeMemory(state->r);
    if (state->i) comp->functions->freeMemory(state->i);
    if (state->b) comp->functions->freeMemory(state->b);
    if (state->s) {
        int i;
        for (i = 0; i < NUMBER_OF_STRINGS; i++){
            if (state->s[i]) comp->functions->freeMemory((void *)state->s[i]);
        }
        comp->functions->freeMemory(state->s);
    }
    if (state->isPositive) comp->functions->freeMemory(state->isPositive);
    if (state->instanceName) comp->functions->freeMemory((void *)state->instanceName);
    if (state->GUID) comp->functions->freeMemory((void *)state->GUID);
    comp->functions->freeMemory(state);
    return fmi2OK;
}

fmi2Status fmi2SerializedFMUstateSize(fmi2Component c, fmi2FMUstate FMUstate, size_t *size) {
    return unsupportedFunction(c, "fmi2SerializedFMUstateSize", MASK_fmi2SerializedFMUstateSize);
}
fmi2Status fmi2SerializeFMUstate (fmi2Component c, fmi2FMUstate FMUstate, fmi2Byte serializedState[], size_t size) {
    return unsupportedFunction(c, "fmi2SerializeFMUstate", MASK_fmi2SerializeFMUstate);
}
fmi2Status fmi2DeSerializeFMUstate (fmi2Component c, const fmi2Byte serializedState[], size_t size,
                                    fmi2FMUstate* FMUstate) {
    return unsupportedFunction(c, "fmi2DeSerializeFMUstate", MASK_fmi2DeSerializeFMUstate);
}

fmi2Status fmi2GetDirectionalDerivative(fmi2Component c, const fmi2ValueReference vUnknown_ref[], size_t nUnknown,
                                        const fmi2ValueReference vKnown_ref[] , size_t nKnown,
                                        const fmi2Real dvKnown[], fmi2Real dvUnknown[]) {
    return unsupportedFunction(c, "fmi2GetDirectionalDerivative", MASK_fmi2GetDirectionalDerivative);
}

// ---------------------------------------------------------------------------
// Functions for FMI for Co-Simulation
// ---------------------------------------------------------------------------
#ifdef FMI_COSIMULATION
/* Simulating the slave */
fmi2Status fmi2SetRealInputDerivatives(fmi2Component c, const fmi2ValueReference vr[], size_t nvr,
                                     const fmi2Integer order[], const fmi2Real value[]) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2SetRealInputDerivatives", MASK_fmi2SetRealInputDerivatives)) {
        return fmi2Error;
    }
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetRealInputDerivatives: nvr= %d", nvr)
    FILTERED_LOG(comp, fmi2Error, LOG_ERROR, "fmi2SetRealInputDerivatives: ignoring function call."
        " This model cannot interpolate inputs: canInterpolateInputs=\"fmi2False\"")
    return fmi2Error;
}

fmi2Status fmi2GetRealOutputDerivatives(fmi2Component c, const fmi2ValueReference vr[], size_t nvr,
                                      const fmi2Integer order[], fmi2Real value[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2GetRealOutputDerivatives", MASK_fmi2GetRealOutputDerivatives))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2GetRealOutputDerivatives: nvr= %d", nvr)
    FILTERED_LOG(comp, fmi2Error, LOG_ERROR,"fmi2GetRealOutputDerivatives: ignoring function call."
        " This model cannot compute derivatives of outputs: MaxOutputDerivativeOrder=\"0\"")
    for (i = 0; i < nvr; i++) value[i] = 0;
    return fmi2Error;
}

fmi2Status fmi2CancelStep(fmi2Component c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2CancelStep", MASK_fmi2CancelStep)) {
        // always fmi2CancelStep is invalid, because model is never in modelStepInProgress state.
        return fmi2Error;
    }
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2CancelStep")
    FILTERED_LOG(comp, fmi2Error, LOG_ERROR,"fmi2CancelStep: Can be called when fmi2DoStep returned fmi2Pending."
        " This is not the case.");
    // comp->state = modelStepCanceled;
    return fmi2Error;
}

fmi2Status fmi2DoStep(fmi2Component c, fmi2Real currentCommunicationPoint,
                    fmi2Real communicationStepSize, fmi2Boolean noSetFMUStatePriorToCurrentPoint) {
    return fmi2OK;
}

fmi2Status fmi2HybridDoStep(fmi2Component c, fmi2Integer currentCommunicationPoint,
                    fmi2Integer communicationStepSize, fmi2Boolean noSetFMUStatePriorToCurrentPoint) {

    ModelInstance *comp = (ModelInstance *)c;
    double h = communicationStepSize;
    int k;
#if NUMBER_OF_EVENT_INDICATORS>0 || NUMBER_OF_REALS>0
    int i;
#endif
    const int n = 1; // how many Euler steps to perform for one do step
#if NUMBER_OF_REALS>0
    double prevState[max(NUMBER_OF_STATES, 1)];
#endif
#if NUMBER_OF_EVENT_INDICATORS>0
    double prevEventIndicators[max(NUMBER_OF_EVENT_INDICATORS, 1)];
#endif
    int stateEvent = 0;
    int timeEvent = 0;

    comp->communicationStepSize = communicationStepSize;

    if (invalidState(comp, "fmi2HybridDoStep", MASK_fmi2DoStep))
        return fmi2Error;

    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2HybridDoStep: "
        "currentCommunicationPoint = %u, "
        "communicationStepSize = %u, "
        "noSetFMUStatePriorToCurrentPoint = fmi2%s",
        currentCommunicationPoint, communicationStepSize, noSetFMUStatePriorToCurrentPoint ? "True" : "False")

    if (communicationStepSize < 0) {
        FILTERED_LOG(comp, fmi2Error, LOG_ERROR,
            "fmi2HybridDoStep: communication step size must be >= 0. Fount %u.", communicationStepSize)
        comp->state = modelError;
        return fmi2Error;
    }

#if NUMBER_OF_EVENT_INDICATORS>0
    // initialize previous event indicators with current values
    for (i = 0; i < NUMBER_OF_EVENT_INDICATORS; i++) {
        prevEventIndicators[i] = getEventIndicator(comp, i);
    }
#endif

    // break the step into n steps and do forward Euler.
    comp->time = currentCommunicationPoint;
    for (k = 0; k < n; k++) {
        comp->time += h;

#if NUMBER_OF_REALS>0
        for (i = 0; i < NUMBER_OF_STATES; i++) {
            prevState[i] = r(vrStates[i]);
        }
        for (i = 0; i < NUMBER_OF_STATES; i++) {
            fmi2ValueReference vr = vrStates[i];
            r(vr) += h * getReal(comp, vr + 1); // forward Euler step
        }
#endif

#if NUMBER_OF_EVENT_INDICATORS>0
        // check for state event
        for (i = 0; i < NUMBER_OF_EVENT_INDICATORS; i++) {
            double ei = getEventIndicator(comp, i);
            if (ei * prevEventIndicators[i] < 0) {
                FILTERED_LOG(comp, fmi2OK, LOG_EVENT,
                    "fmi2HybridDoStep: state event at %u, z%d crosses zero -%c-", comp->time, i, ei < 0 ? '\\' : '/')
                stateEvent++;
            }
            prevEventIndicators[i] = ei;
        }
#endif
        // check for time event
        if (comp->eventInfo.nextEventTimeDefined && ((comp->time - comp->eventInfo.nextEventTime) == 0)) {
            FILTERED_LOG(comp, fmi2OK, LOG_EVENT, "fmi2HybridDoStep: time event detected at %g", comp->time)
            timeEvent = 1;
        }

        if (stateEvent || timeEvent) {
            eventUpdate(comp, &comp->eventInfo, timeEvent);
            timeEvent = 0;
            stateEvent = 0;
        }

        // terminate simulation, if requested by the model in the previous step
        if (comp->eventInfo.terminateSimulation) {
            FILTERED_LOG(comp, fmi2Discard, LOG_ALL, "fmi2HybridDoStep: model requested termination at t=%g", comp->time)
            comp->state = modelStepFailed;
            return fmi2Discard; // enforce termination of the simulation loop
        }
    }
    return fmi2OK;
}

/* Inquire slave status */
static fmi2Status getStatus(char* fname, fmi2Component c, const fmi2StatusKind s) {
    const char *statusKind[3] = {"fmi2DoStepStatus","fmi2PendingStatus","fmi2LastSuccessfulTime"};
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, fname, MASK_fmi2GetStatus)) // all get status have the same MASK_fmi2GetStatus
            return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "$s: fmi2StatusKind = %s", fname, statusKind[s])

    switch(s) {
        case fmi2DoStepStatus: FILTERED_LOG(comp, fmi2Error, LOG_ERROR,
            "%s: Can be called with fmi2DoStepStatus when fmi2DoStep returned fmi2Pending."
            " This is not the case.", fname)
            break;
        case fmi2PendingStatus: FILTERED_LOG(comp, fmi2Error, LOG_ERROR,
            "%s: Can be called with fmi2PendingStatus when fmi2DoStep returned fmi2Pending."
            " This is not the case.", fname)
            break;
        case fmi2LastSuccessfulTime: FILTERED_LOG(comp, fmi2Error, LOG_ERROR,
            "%s: Can be called with fmi2LastSuccessfulTime when fmi2DoStep returned fmi2Discard."
            " This is not the case.", fname)
            break;
        case fmi2Terminated: FILTERED_LOG(comp, fmi2Error, LOG_ERROR,
            "%s: Can be called with fmi2Terminated when fmi2DoStep returned fmi2Discard."
            " This is not the case.", fname)
            break;
    }
    return fmi2Discard;
}

fmi2Status fmi2GetStatus(fmi2Component c, const fmi2StatusKind s, fmi2Status *value) {
    return fmi2OK;
}

fmi2Status fmi2GetRealStatus(fmi2Component c, const fmi2StatusKind s, fmi2Real *value) {
    if (s == fmi2LastSuccessfulTime) {
        ModelInstance *comp = (ModelInstance *)c;
        if (invalidState(comp, "fmi2GetRealStatus", MASK_fmi2GetRealStatus))
            return fmi2Error;
        *value = comp->time;
        return fmi2OK;
    }
    return getStatus("fmi2GetRealStatus", c, s);
}

fmi2Status fmi2GetIntegerStatus(fmi2Component c, const fmi2StatusKind s, fmi2Integer *value) {
    return getStatus("fmi2GetIntegerStatus", c, s);
}

fmi2Status fmi2GetBooleanStatus(fmi2Component c, const fmi2StatusKind s, fmi2Boolean *value) {
    if (s == fmi2Terminated) {
        ModelInstance *comp = (ModelInstance *)c;
        if (invalidState(comp, "fmi2GetBooleanStatus", MASK_fmi2GetBooleanStatus))
            return fmi2Error;
        *value = comp->eventInfo.terminateSimulation;
        return fmi2OK;
    }
    return getStatus("fmi2GetBooleanStatus", c, s);
}

fmi2Status fmi2GetStringStatus(fmi2Component c, const fmi2StatusKind s, fmi2String *value) {
    return getStatus("fmi2GetStringStatus", c, s);
}

// ---------------------------------------------------------------------------
// Functions for FMI2 for Model Exchange
// ---------------------------------------------------------------------------
#else
/* Enter and exit the different modes */
fmi2Status fmi2EnterEventMode(fmi2Component c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2EnterEventMode", MASK_fmi2EnterEventMode))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2EnterEventMode")

    comp->state = modelEventMode;
    return fmi2OK;
}

fmi2Status fmi2NewDiscreteStates(fmi2Component c, fmi2EventInfo *eventInfo) {
    ModelInstance *comp = (ModelInstance *)c;
    int timeEvent = 0;
    if (invalidState(comp, "fmi2NewDiscreteStates", MASK_fmi2NewDiscreteStates))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2NewDiscreteStates")

    comp->eventInfo.newDiscreteStatesNeeded = fmi2False;
    comp->eventInfo.terminateSimulation = fmi2False;
    comp->eventInfo.nominalsOfContinuousStatesChanged = fmi2False;
    comp->eventInfo.valuesOfContinuousStatesChanged = fmi2False;

    if (comp->eventInfo.nextEventTimeDefined && comp->eventInfo.nextEventTime <= comp->time) {
        timeEvent = 1;
    }
    eventUpdate(comp, &comp->eventInfo, timeEvent);

    // copy internal eventInfo of component to output eventInfo
    eventInfo->newDiscreteStatesNeeded = comp->eventInfo.newDiscreteStatesNeeded;
    eventInfo->terminateSimulation = comp->eventInfo.terminateSimulation;
    eventInfo->nominalsOfContinuousStatesChanged = comp->eventInfo.nominalsOfContinuousStatesChanged;
    eventInfo->valuesOfContinuousStatesChanged = comp->eventInfo.valuesOfContinuousStatesChanged;
    eventInfo->nextEventTimeDefined = comp->eventInfo.nextEventTimeDefined;
    eventInfo->nextEventTime = comp->eventInfo.nextEventTime;

    return fmi2OK;
}

fmi2Status fmi2EnterContinuousTimeMode(fmi2Component c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2EnterContinuousTimeMode", MASK_fmi2EnterContinuousTimeMode))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL,"fmi2EnterContinuousTimeMode")

    comp->state = modelContinuousTimeMode;
    return fmi2OK;
}

fmi2Status fmi2CompletedIntegratorStep(fmi2Component c, fmi2Boolean noSetFMUStatePriorToCurrentPoint,
                                     fmi2Boolean *enterEventMode, fmi2Boolean *terminateSimulation) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2CompletedIntegratorStep", MASK_fmi2CompletedIntegratorStep))
        return fmi2Error;
    if (nullPointer(comp, "fmi2CompletedIntegratorStep", "enterEventMode", enterEventMode))
        return fmi2Error;
    if (nullPointer(comp, "fmi2CompletedIntegratorStep", "terminateSimulation", terminateSimulation))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL,"fmi2CompletedIntegratorStep")
    *enterEventMode = fmi2False;
    *terminateSimulation = fmi2False;
    return fmi2OK;
}

/* Providing independent variables and re-initialization of caching */
fmi2Status fmi2SetTime(fmi2Component c, fmi2Integer time) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2SetTime", MASK_fmi2SetTime))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetTime: time=%u", time)
    comp->time = time;
    return fmi2OK;
}

fmi2Status fmi2SetContinuousStates(fmi2Component c, const fmi2Real x[], size_t nx){
    ModelInstance *comp = (ModelInstance *)c;
#if NUMBER_OF_REALS>0
    int i;
#endif
    if (invalidState(comp, "fmi2SetContinuousStates", MASK_fmi2SetContinuousStates))
        return fmi2Error;
    if (invalidNumber(comp, "fmi2SetContinuousStates", "nx", nx, NUMBER_OF_STATES))
        return fmi2Error;
    if (nullPointer(comp, "fmi2SetContinuousStates", "x[]", x))
        return fmi2Error;
#if NUMBER_OF_REALS>0
    for (i = 0; i < nx; i++) {
        fmi2ValueReference vr = vrStates[i];
        FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2SetContinuousStates: #r%d#=%.16g", vr, x[i])
        assert(vr < NUMBER_OF_REALS);
        comp->r[vr] = x[i];
    }
#endif
    return fmi2OK;
}

/* Evaluation of the model equations */
fmi2Status fmi2GetDerivatives(fmi2Component c, fmi2Real derivatives[], size_t nx) {
#if NUMBER_OF_STATES>0
    int i;
#endif
    ModelInstance* comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2GetDerivatives", MASK_fmi2GetDerivatives))
        return fmi2Error;
    if (invalidNumber(comp, "fmi2GetDerivatives", "nx", nx, NUMBER_OF_STATES))
        return fmi2Error;
    if (nullPointer(comp, "fmi2GetDerivatives", "derivatives[]", derivatives))
        return fmi2Error;
#if NUMBER_OF_STATES>0
    for (i = 0; i < nx; i++) {
        fmi2ValueReference vr = vrStates[i] + 1;
        derivatives[i] = getReal(comp, vr); // to be implemented by the includer of this file
        FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2GetDerivatives: #r%d# = %.16g", vr, derivatives[i])
    }
#endif
    return fmi2OK;
}

fmi2Status fmi2GetEventIndicators(fmi2Component c, fmi2Real eventIndicators[], size_t ni) {
#if NUMBER_OF_EVENT_INDICATORS>0
    int i;
#endif
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2GetEventIndicators", MASK_fmi2GetEventIndicators))
        return fmi2Error;
    if (invalidNumber(comp, "fmi2GetEventIndicators", "ni", ni, NUMBER_OF_EVENT_INDICATORS))
        return fmi2Error;
#if NUMBER_OF_EVENT_INDICATORS>0
    for (i = 0; i < ni; i++) {
        eventIndicators[i] = getEventIndicator(comp, i); // to be implemented by the includer of this file
        FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2GetEventIndicators: z%d = %.16g", i, eventIndicators[i])
    }
#endif
    return fmi2OK;
}

fmi2Status fmi2GetContinuousStates(fmi2Component c, fmi2Real states[], size_t nx) {
#if NUMBER_OF_REALS>0
    int i;
#endif
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2GetContinuousStates", MASK_fmi2GetContinuousStates))
        return fmi2Error;
    if (invalidNumber(comp, "fmi2GetContinuousStates", "nx", nx, NUMBER_OF_STATES))
        return fmi2Error;
    if (nullPointer(comp, "fmi2GetContinuousStates", "states[]", states))
        return fmi2Error;
#if NUMBER_OF_REALS>0
    for (i = 0; i < nx; i++) {
        fmi2ValueReference vr = vrStates[i];
        states[i] = getReal(comp, vr); // to be implemented by the includer of this file
        FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2GetContinuousStates: #r%u# = %.16g", vr, states[i])
    }
#endif
    return fmi2OK;
}

fmi2Status fmi2GetNominalsOfContinuousStates(fmi2Component c, fmi2Real x_nominal[], size_t nx) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmi2GetNominalsOfContinuousStates", MASK_fmi2GetNominalsOfContinuousStates))
        return fmi2Error;
    if (invalidNumber(comp, "fmi2GetNominalContinuousStates", "nx", nx, NUMBER_OF_STATES))
        return fmi2Error;
    if (nullPointer(comp, "fmi2GetNominalContinuousStates", "x_nominal[]", x_nominal))
        return fmi2Error;
    FILTERED_LOG(comp, fmi2OK, LOG_FMI_CALL, "fmi2GetNominalContinuousStates: x_nominal[0..%d] = 1.0", nx-1)
    for (i = 0; i < nx; i++)
        x_nominal[i] = 1;
    return fmi2OK;
}
#endif // Model Exchange
