/* ---------------------------------------------------------------------------*
 * fmuTemplate.c
 * Implementation of the FMI interface based on functions and macros to
 * be defined by the includer of this file.
 * If FMI_COSIMULATION is defined, this implements "FMI for Co-Simulation 2.0",
 * otherwise "FMI for Model Exchange 2.0".
 * The "FMI for Co-Simulation 2.0", implementation assumes that exactly the
 * following capability flags are set to fmiTrue:
 *    canHandleVariableCommunicationStepSize, i.e. fmiDoStep step size can vary
 * and all other capability flags are set to default, i.e. to fmiFalse or 0.
 *
 * Revision history
 *  07.03.2014 initial version released in FMU SDK 2.0.0
 *  02.04.2014 allow modules to request termination of simulation, better time
 *             event handling, initialize() moved from fmiEnterInitialization to
 *             fmiExitInitialization, correct logging message format in fmiDoStep.
 *
 * Author: Adrian Tirea
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

fmiBoolean isCategoryLogged(ModelInstance *comp, int categoryIndex);

// macro to be used to log messages. The macro check if current
// log category is valid and, if true, call the logger provided by simulator.

// ## is special to gcc, see http://gcc.gnu.org/onlinedocs/cpp/Variadic-Macros.html
#define FILTERED_LOG(instance, status, categoryIndex, message, ...) if (isCategoryLogged(instance, categoryIndex)) \
        instance->functions->logger(instance->functions->componentEnvironment, instance->instanceName, status, \
                logCategoriesNames[categoryIndex], message, ## __VA_ARGS__);

static fmiString logCategoriesNames[] = {"logAll", "logError", "logFmiCall", "logEvent"};

// array of value references of states
#if NUMBER_OF_REALS>0
fmiValueReference vrStates[NUMBER_OF_STATES] = STATES;
#endif

#ifndef max
#define max(a,b) ((a)>(b) ? (a) : (b))
#endif

// ---------------------------------------------------------------------------
// Private helpers used below to validate function arguments
// ---------------------------------------------------------------------------

#ifndef FMI_COSIMULATION
static fmiBoolean invalidNumber(ModelInstance *comp, const char *f, const char *arg, int n, int nExpected) {
    if (n != nExpected) {
        comp->state = modelError;
        FILTERED_LOG(comp, fmiError, LOG_ERROR, "%s: Invalid argument %s = %d. Expected %d.", f, arg, n, nExpected)
            return fmiTrue;
    }
    return fmiFalse;
}
#endif

static fmiBoolean invalidState(ModelInstance *comp, const char *f, int statesExpected) {
    if (!comp)
        return fmiTrue;
    if (!(comp->state & statesExpected)) {
        comp->state = modelError;
        FILTERED_LOG(comp, fmiError, LOG_ERROR, "%s: Illegal call sequence.", f)
            return fmiTrue;
    }
    return fmiFalse;
}

static fmiBoolean nullPointer(ModelInstance* comp, const char *f, const char *arg, const void *p) {
    if (!p) {
        comp->state = modelError;
        FILTERED_LOG(comp, fmiError, LOG_ERROR, "%s: Invalid argument %s = NULL.", f, arg)
            return fmiTrue;
    }
    return fmiFalse;
}

static fmiBoolean vrOutOfRange(ModelInstance *comp, const char *f, fmiValueReference vr, int end) {
    if (vr >= end) {
        FILTERED_LOG(comp, fmiError, LOG_ERROR, "%s: Illegal value reference %u.", f, vr)
            comp->state = modelError;
        return fmiTrue;
    }
    return fmiFalse;
}

static fmiStatus unsupportedFunction(fmiComponent c, const char *fName, int statesExpected) {
    ModelInstance *comp = (ModelInstance *)c;
    fmiCallbackLogger log = comp->functions->logger;
    if (invalidState(comp, fName, statesExpected))
        return fmiError;
    if (comp->loggingOn) log(c, comp->instanceName, fmiOK, "log", fName);
    FILTERED_LOG(comp, fmiError, LOG_ERROR, "%s: Function not implemented.", fName)
        return fmiError;
}

fmiStatus setString(fmiComponent comp, fmiValueReference vr, fmiString value){
    return fmiSetString(comp, &vr, 1, &value);
}

// ---------------------------------------------------------------------------
// Private helpers logger
// ---------------------------------------------------------------------------

// return fmiTrue if logging category is on. Else return fmiFalse.
fmiBoolean isCategoryLogged(ModelInstance *comp, int categoryIndex) {
    if (categoryIndex < NUMBER_OF_CATEGORIES
            && (comp->logCategories[categoryIndex] || comp->logCategories[LOG_ALL])) {
        return fmiTrue;
    }
    return fmiFalse;
}

// ---------------------------------------------------------------------------
// FMI functions
// ---------------------------------------------------------------------------
fmiComponent fmiInstantiate(fmiString instanceName, fmiType fmuType, fmiString fmuGUID,
        fmiString fmuResourceLocation, const fmiCallbackFunctions *functions,
        fmiBoolean visible, fmiBoolean loggingOn) {
    // ignoring arguments: fmuResourceLocation, visible
    ModelInstance *comp;
    if (!functions->logger) {
        return NULL;
    }

    if (!functions->allocateMemory || !functions->freeMemory) {
        functions->logger(functions->componentEnvironment, instanceName, fmiError, "error",
                "fmiInstantiate: Missing callback function.");
        return NULL;
    }
    if (!instanceName || strlen(instanceName) == 0) {
        functions->logger(functions->componentEnvironment, instanceName, fmiError, "error",
                "fmiInstantiate: Missing instance name.");
        return NULL;
    }
    if (strcmp(fmuGUID, MODEL_GUID)) {
        functions->logger(functions->componentEnvironment, instanceName, fmiError, "error",
                "fmiInstantiate: Wrong GUID %s. Expected %s.", fmuGUID, MODEL_GUID);
        return NULL;
    }
    comp = (ModelInstance *)functions->allocateMemory(1, sizeof(ModelInstance));
    if (comp) {
        int i;
        comp->r = (fmiReal *)   functions->allocateMemory(NUMBER_OF_REALS,    sizeof(fmiReal));
        comp->i = (fmiInteger *)functions->allocateMemory(NUMBER_OF_INTEGERS, sizeof(fmiInteger));
        comp->b = (fmiBoolean *)functions->allocateMemory(NUMBER_OF_BOOLEANS, sizeof(fmiBoolean));
        comp->s = (fmiString *) functions->allocateMemory(NUMBER_OF_STRINGS,  sizeof(fmiString));
        comp->isPositive = (fmiBoolean *)functions->allocateMemory(NUMBER_OF_EVENT_INDICATORS,
                sizeof(fmiBoolean));
        comp->instanceName = functions->allocateMemory(1 + strlen(instanceName), sizeof(char));
        comp->GUID = functions->allocateMemory(1 + strlen(fmuGUID), sizeof(char));

        // set all categories to on or off. fmiSetDebugLogging should be called to choose specific categories.
        for (i = 0; i < NUMBER_OF_CATEGORIES; i++) {
            comp->logCategories[i] = loggingOn;
        }
    }
    if (!comp || !comp->r || !comp->i || !comp->b || !comp->s || !comp->isPositive
            || !comp->instanceName || !comp->GUID) {

        functions->logger(functions->componentEnvironment, instanceName, fmiError, "error",
                "fmiInstantiate: Out of memory.");
        return NULL;
    }
    strcpy((char *)comp->instanceName, (char *)instanceName);
    comp->type = fmuType;
    strcpy((char *)comp->GUID, (char *)fmuGUID);
    comp->functions = functions;
    comp->componentEnvironment = functions->componentEnvironment;
    comp->loggingOn = loggingOn;
    comp->state = modelInstantiated;
    setStartValues(comp); // to be implemented by the includer of this file

    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiInstantiate: GUID=%s", fmuGUID)

        return comp;
}

fmiStatus fmiSetupExperiment(fmiComponent c, fmiBoolean toleranceDefined, fmiReal tolerance,
        fmiReal startTime, fmiBoolean stopTimeDefined, fmiReal stopTime) {

    // ignore arguments: stopTimeDefined, stopTime
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiSetupExperiment", modelInstantiated))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetupExperiment: toleranceDefined=%d tolerance=%g",
            toleranceDefined, tolerance)

        comp->time = startTime;
    return fmiOK;
}

fmiStatus fmiEnterInitializationMode(fmiComponent c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiEnterInitializationMode", modelInstantiated))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiEnterInitializationMode")

        comp->state = modelInitializationMode;
    return fmiOK;
}

fmiStatus fmiExitInitializationMode(fmiComponent c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiExitInitializationMode", modelInitializationMode))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiExitInitializationMode")

        initialize(comp, &comp->eventInfo); // to be implemented by the includer of this file
    comp->state = modelInitialized;
    return fmiOK;
}

fmiStatus fmiTerminate(fmiComponent c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiTerminate", modelInitialized|modelStepping))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiTerminate")

        comp->state = modelTerminated;
    return fmiOK;
}

fmiStatus fmiReset(fmiComponent c) {
    ModelInstance* comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiReset", modelInitialized|modelStepping))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiReset")

        comp->state = modelInstantiated;
    setStartValues(comp); // to be implemented by the includer of this file
    return fmiOK;
}

void fmiFreeInstance(fmiComponent c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (!comp) return;
    if (invalidState(comp, "fmiFreeInstance", modelTerminated))
        return;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiFreeInstance")

    if (comp->r) comp->functions->freeMemory(comp->r);
    if (comp->i) comp->functions->freeMemory(comp->i);
    if (comp->b) comp->functions->freeMemory(comp->b);
    if (comp->s) {
        int i;
        for (i = 0; i < NUMBER_OF_STRINGS; i++){
            if (comp->s[i]) comp->functions->freeMemory((void *)comp->s[i]);
        }
        comp->functions->freeMemory(comp->s);
    }
    if (comp->isPositive) comp->functions->freeMemory(comp->isPositive);
    if (comp->instanceName) comp->functions->freeMemory((void *)comp->instanceName);
    if (comp->GUID) comp->functions->freeMemory((void *)comp->GUID);
    comp->functions->freeMemory(comp);
}

// ---------------------------------------------------------------------------
// FMI functions: class methods not depending of a specific model instance
// ---------------------------------------------------------------------------

const char* fmiGetVersion() {
    return fmiVersion;
}

const char* fmiGetTypesPlatform() {
    return fmiTypesPlatform;
}

// ---------------------------------------------------------------------------
// FMI functions: logging control, setters and getters for Real, Integer,
// Boolean, String
// ---------------------------------------------------------------------------

fmiStatus fmiSetDebugLogging(fmiComponent c, fmiBoolean loggingOn, size_t nCategories, const fmiString categories[]) {
    // ignore arguments: nCategories, categories
    int i, j;
    ModelInstance *comp = (ModelInstance *)c;
    comp->loggingOn = loggingOn;

    for (j = 0; j < NUMBER_OF_CATEGORIES; j++) {
        comp->logCategories[j] = fmiFalse;
    }
    for (i = 0; i < nCategories; i++) {
        fmiBoolean categoryFound = fmiFalse;
        for (j = 0; j < NUMBER_OF_CATEGORIES; j++) {
            if (strcmp(logCategoriesNames[j], categories[i]) == 0) {
                comp->logCategories[j] = loggingOn;
                categoryFound = fmiTrue;
                break;
            }
        }
        if (!categoryFound) {
            comp->functions->logger(comp->componentEnvironment, comp->instanceName, fmiWarning,
                    logCategoriesNames[LOG_ERROR],
                    "logging category '%s' is not supported by model", categories[i]);
        }
    }

    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetDebugLogging")
        return fmiOK;
}

fmiStatus fmiGetReal (fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiReal value[]) {
#if NUMBER_OF_REALS > 0
    int i;
#endif
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiGetReal", modelInitializationMode|modelInitialized|modelStepping|modelError))
        return fmiError;
    if (nvr > 0 && nullPointer(comp, "fmiGetReal", "vr[]", vr))
        return fmiError;
    if (nvr > 0 && nullPointer(comp, "fmiGetReal", "value[]", value))
        return fmiError;
#if NUMBER_OF_REALS > 0
    for (i = 0; i < nvr; i++) {
        if (vrOutOfRange(comp, "fmiGetReal", vr[i], NUMBER_OF_REALS))
            return fmiError;
        value[i] = getReal(comp, vr[i]); // to be implemented by the includer of this file

        FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiGetReal: #r%u# = %.16g", vr[i], value[i])
            }
#endif
    return fmiOK;
}

fmiStatus fmiGetInteger(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiInteger value[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiGetInteger", modelInitializationMode|modelInitialized|modelStepping|modelError))
        return fmiError;
    if (nvr > 0 && nullPointer(comp, "fmiGetInteger", "vr[]", vr))
        return fmiError;
    if (nvr > 0 && nullPointer(comp, "fmiGetInteger", "value[]", value))
        return fmiError;
    for (i = 0; i < nvr; i++) {
        if (vrOutOfRange(comp, "fmiGetInteger", vr[i], NUMBER_OF_INTEGERS))
            return fmiError;
        value[i] = comp->i[vr[i]];
        FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiGetInteger: #i%u# = %d", vr[i], value[i])
            }
    return fmiOK;
}

fmiStatus fmiGetBoolean(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiBoolean value[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiGetBoolean", modelInitializationMode|modelInitialized|modelStepping|modelError))
        return fmiError;
    if (nvr > 0 && nullPointer(comp, "fmiGetBoolean", "vr[]", vr))
        return fmiError;
    if (nvr > 0 && nullPointer(comp, "fmiGetBoolean", "value[]", value))
        return fmiError;
    for (i = 0; i < nvr; i++) {
        if (vrOutOfRange(comp, "fmiGetBoolean", vr[i], NUMBER_OF_BOOLEANS))
            return fmiError;
        value[i] = comp->b[vr[i]];
        FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiGetBoolean: #b%u# = %s", vr[i], value[i]? "true" : "false")
            }
    return fmiOK;
}

fmiStatus fmiGetString (fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiString value[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiGetString", modelInitializationMode|modelInitialized|modelStepping|modelError))
        return fmiError;
    if (nvr>0 && nullPointer(comp, "fmiGetString", "vr[]", vr))
        return fmiError;
    if (nvr>0 && nullPointer(comp, "fmiGetString", "value[]", value))
        return fmiError;
    for (i=0; i<nvr; i++) {
        if (vrOutOfRange(comp, "fmiGetString", vr[i], NUMBER_OF_STRINGS))
            return fmiError;
        value[i] = comp->s[vr[i]];
        FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiGetString: #s%u# = '%s'", vr[i], value[i])
            }
    return fmiOK;
}

fmiStatus fmiSetReal (fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiReal value[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiSetReal", modelInstantiated|modelInitializationMode|modelInitialized|modelStepping))
        return fmiError;
    if (nvr > 0 && nullPointer(comp, "fmiSetReal", "vr[]", vr))
        return fmiError;
    if (nvr > 0 && nullPointer(comp, "fmiSetReal", "value[]", value))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetReal: nvr = %d", nvr)
        // no check whether setting the value is allowed in the current state
        for (i = 0; i < nvr; i++) {
            if (vrOutOfRange(comp, "fmiSetReal", vr[i], NUMBER_OF_REALS))
                return fmiError;
            FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetReal: #r%d# = %.16g", vr[i], value[i])
                comp->r[vr[i]] = value[i];
        }
    return fmiOK;
}

fmiStatus fmiSetInteger(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiInteger value[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiSetInteger", modelInstantiated|modelInitializationMode|modelInitialized|modelStepping))
        return fmiError;
    if (nvr > 0 && nullPointer(comp, "fmiSetInteger", "vr[]", vr))
        return fmiError;
    if (nvr > 0 && nullPointer(comp, "fmiSetInteger", "value[]", value))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetInteger: nvr = %d", nvr)

        for (i = 0; i < nvr; i++) {
            if (vrOutOfRange(comp, "fmiSetInteger", vr[i], NUMBER_OF_INTEGERS))
                return fmiError;
            FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetInteger: #i%d# = %d", vr[i], value[i])
                comp->i[vr[i]] = value[i];
        }
    return fmiOK;
}

fmiStatus fmiSetBoolean(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiBoolean value[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiSetBoolean", modelInstantiated|modelInitializationMode|modelInitialized|modelStepping))
        return fmiError;
    if (nvr>0 && nullPointer(comp, "fmiSetBoolean", "vr[]", vr))
        return fmiError;
    if (nvr>0 && nullPointer(comp, "fmiSetBoolean", "value[]", value))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetBoolean: nvr = %d", nvr)

        for (i = 0; i < nvr; i++) {
            if (vrOutOfRange(comp, "fmiSetBoolean", vr[i], NUMBER_OF_BOOLEANS))
                return fmiError;
            FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetBoolean: #b%d# = %s", vr[i], value[i] ? "true" : "false")
                comp->b[vr[i]] = value[i];
        }
    return fmiOK;
}

fmiStatus fmiSetString (fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiString value[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiSetString", modelInstantiated|modelInitializationMode|modelInitialized|modelStepping))
        return fmiError;
    if (nvr>0 && nullPointer(comp, "fmiSetString", "vr[]", vr))
        return fmiError;
    if (nvr>0 && nullPointer(comp, "fmiSetString", "value[]", value))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetString: nvr = %d", nvr)

        for (i = 0; i < nvr; i++) {
            char *string = (char *)comp->s[vr[i]];
            if (vrOutOfRange(comp, "fmiSetString", vr[i], NUMBER_OF_STRINGS))
                return fmiError;
            FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetString: #s%d# = '%s'", vr[i], value[i])

                if (nullPointer(comp, "fmiSetString", "value[i]", value[i]))
                    return fmiError;
            if (string == NULL || strlen(string) < strlen(value[i])) {
                if (string) comp->functions->freeMemory(string);
                comp->s[vr[i]] = comp->functions->allocateMemory(1 + strlen(value[i]), sizeof(char));
                if (!comp->s[vr[i]]) {
                    comp->state = modelError;
                    FILTERED_LOG(comp, fmiError, LOG_ERROR, "fmiSetString: Out of memory.")
                        return fmiError;
                }
            }
            strcpy((char *)comp->s[vr[i]], (char *)value[i]);
        }
    return fmiOK;
}

// TODO: Write detailed documentation

/** From the spec: "fmi2GetFMUstate makes a copy of the internal FMU state and returns a pointer to this copy
 * (FMUstate). If on entry *FMUstate == NULL, a new allocation is required. If *FMUstate !=
 * NULL, then *FMUstate points to a previously returned FMUstate that has not been modified
 * since. In particular, fmi2FreeFMUstate had not been called with this FMUstate as an argument.
 * [Function fmi2GetFMUstate typically reuses the memory of this FMUstate in this case and
 * returns the same pointer to it, but with the actual FMUstate.]"
 */
fmiStatus fmiGetFMUstate (fmiComponent c, fmiFMUstate* FMUstate) {
    ModelInstance *source = (ModelInstance*)c;
    int i;

    // allocating memory for pointers in ModelInstance struct
    ModelInstance *dest;

    if (!*FMUstate) {
        dest = (ModelInstance *)source->functions->allocateMemory(1, sizeof(ModelInstance));
        dest->r = (fmiReal *)source->functions->allocateMemory(NUMBER_OF_REALS, sizeof(fmiReal));
        dest->i = (fmiInteger *)source->functions->allocateMemory(NUMBER_OF_INTEGERS, sizeof(fmiInteger));
        dest->b = (fmiBoolean *)source->functions->allocateMemory(NUMBER_OF_BOOLEANS, sizeof(fmiBoolean));
        dest->s = (fmiString *)source->functions->allocateMemory(NUMBER_OF_STRINGS, sizeof(fmiString));
        dest->isPositive = (fmiBoolean *)source->functions->allocateMemory(NUMBER_OF_EVENT_INDICATORS,
                sizeof(fmiBoolean));
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
                source->functions->freeMemory((void *)dest->s[i]);
            dest->s[i] = source->functions->allocateMemory(1 + strlen(source->s[i]), sizeof(char));
            strcpy((char*)dest->s[i], (char*)source->s[i]);
        }
    }

    if (NUMBER_OF_EVENT_INDICATORS > 0) {
        for (i = 0; i < NUMBER_OF_EVENT_INDICATORS; i++) {
            dest->r[i] = source->r[i];
        }
    }

    *FMUstate = (fmiFMUstate)dest;

    return fmiOK;
}

fmiStatus fmiSetFMUstate (fmiComponent c, fmiFMUstate FMUstate) {
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
            dest->s[i] = source->functions->allocateMemory(1 + strlen(source->s[i]), sizeof(char));
            strcpy((char*)dest->s[i], (char*)source->s[i]);
        }
    }
    if (NUMBER_OF_EVENT_INDICATORS > 0) {
        for (i = 0; i < NUMBER_OF_EVENT_INDICATORS; i++) {
            dest->r[i] = source->r[i];
        }
    }

    return fmiOK;
}

fmiStatus fmiFreeFMUstate(fmiComponent c, fmiFMUstate* FMUstate) {
    ModelInstance *comp = (ModelInstance *)c;
    ModelInstance *state = (ModelInstance *)*FMUstate;
    if (!state) return fmiOK;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiFreeFMUstate")
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
    return fmiOK;
}

fmiStatus fmiSerializedFMUstateSize(fmiComponent c, fmiFMUstate FMUstate, size_t *size) {
    return unsupportedFunction(c, "fmiSerializedFMUstateSize",
            modelInstantiated|modelInitializationMode|modelInitialized|modelStepping|modelTerminated|modelError);
}
fmiStatus fmiSerializeFMUstate (fmiComponent c, fmiFMUstate FMUstate, fmiByte serializedState[], size_t size) {
    return unsupportedFunction(c, "fmiSerializeFMUstate",
            modelInstantiated|modelInitializationMode|modelInitialized|modelStepping|modelTerminated|modelError);
}
fmiStatus fmiDeSerializeFMUstate (fmiComponent c, const fmiByte serializedState[], size_t size, fmiFMUstate* FMUstate) {
    return unsupportedFunction(c, "fmiDeSerializeFMUstate",
            modelInstantiated|modelInitializationMode|modelInitialized|modelStepping|modelTerminated|modelError);
}

fmiStatus fmiGetDirectionalDerivative(fmiComponent c, const fmiValueReference vUnknown_ref[], size_t nUnknown,
        const fmiValueReference vKnown_ref[] , size_t nKnown, const fmiReal dvKnown[], fmiReal dvUnknown[]) {
    return unsupportedFunction(c, "fmiGetDirectionalDerivative",
            modelInitializationMode|modelInitialized|modelStepping|modelTerminated|modelError);
}

// ---------------------------------------------------------------------------
// Functions for FMI for Co-Simulation
// ---------------------------------------------------------------------------
#ifdef FMI_COSIMULATION
/* Simulating the slave */
fmiStatus fmiSetRealInputDerivatives(fmiComponent c, const fmiValueReference vr[], size_t nvr,
        const fmiInteger order[], const fmiReal value[]) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiSetRealInputDerivatives",
                    modelInstantiated|modelInitializationMode|modelInitialized|modelStepping)) {
        return fmiError;
    }
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetRealInputDerivatives: nvr= %d", nvr)
        FILTERED_LOG(comp, fmiError, LOG_ERROR, "fmiSetRealInputDerivatives: ignoring function call."
                " This model cannot interpolate inputs: canInterpolateInputs=\"fmiFalse\"")
        return fmiError;
}

fmiStatus fmiGetRealOutputDerivatives(fmiComponent c, const fmiValueReference vr[], size_t nvr,
        const fmiInteger order[], fmiReal value[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiGetRealOutputDerivatives", modelInitialized|modelStepping|modelError))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiGetRealOutputDerivatives: nvr= %d", nvr)
        FILTERED_LOG(comp, fmiError, LOG_ERROR,"fmiGetRealOutputDerivatives: ignoring function call."
                " This model cannot compute derivatives of outputs: MaxOutputDerivativeOrder=\"0\"")
        for (i = 0; i < nvr; i++) value[i] = 0;
    return fmiError;
}

fmiStatus fmiCancelStep(fmiComponent c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiCancelStep", modelStepping))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiCancelStep")
        FILTERED_LOG(comp, fmiError, LOG_ERROR,"fmiCancelStep: Can be called when fmiDoStep returned fmiPending."
                " This is not the case.");
    return fmiError;
}

fmiStatus fmiDoStep(fmiComponent c, fmiReal currentCommunicationPoint,
        fmiReal communicationStepSize, fmiBoolean noSetFMUStatePriorToCurrentPoint) {
    ModelInstance *comp = (ModelInstance *)c;
    comp->communicationStepSize = communicationStepSize;
    double h = communicationStepSize / 10;
    int k;
#if NUMBER_OF_EVENT_INDICATORS>0 || NUMBER_OF_REALS>0
    int i;
#endif
    const int n = 10; // how many Euler steps to perform for one do step
#if NUMBER_OF_REALS>0
    double prevState[max(NUMBER_OF_STATES, 1)];
#endif
#if NUMBER_OF_EVENT_INDICATORS>0
    double prevEventIndicators[max(NUMBER_OF_EVENT_INDICATORS, 1)];
    int stateEvent = 0;
#endif
    if (invalidState(comp, "fmiDoStep", modelInitialized|modelStepping))
        return fmiError;

    // model is in stepping state
    comp->state = modelStepping;

    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiDoStep: "
            "currentCommunicationPoint = %g, "
            "communicationStepSize = %g, "
            "noSetFMUStatePriorToCurrentPoint = fmi%s",
            currentCommunicationPoint, communicationStepSize, noSetFMUStatePriorToCurrentPoint ? "True" : "False")

        if (communicationStepSize <= 0) {
            FILTERED_LOG(comp, fmiError, LOG_ERROR,
                    "fmiDoStep: communication step size must be > 0. Found %g.", communicationStepSize)
                return fmiError;
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
            fmiValueReference vr = vrStates[i];
            r(vr) += h * getReal(comp, vr + 1); // forward Euler step
        }
#endif

#if NUMBER_OF_EVENT_INDICATORS>0
        // check for state event
        for (i = 0; i < NUMBER_OF_EVENT_INDICATORS; i++) {
            double ei = getEventIndicator(comp, i);
            if (ei * prevEventIndicators[i] < 0) {
                FILTERED_LOG(comp, fmiOK, LOG_EVENT,
                        "fmiDoStep: state event at %g, z%d crosses zero -%c-", comp->time, i, ei < 0 ? '\\' : '/')
                    stateEvent++;
            }
            prevEventIndicators[i] = ei;
        }
        if (stateEvent) {
            eventUpdate(comp, &comp->eventInfo);
            stateEvent = 0;
        }
#endif
        // check for time event
        if (comp->eventInfo.nextEventTimeDefined && ((comp->time - comp->eventInfo.nextEventTime) > -0.0000000001)) {
            FILTERED_LOG(comp, fmiOK, LOG_EVENT, "fmiDoStep: time event detected at %g", comp->time)
                eventUpdate(comp, &comp->eventInfo);
        }

        // terminate simulation, if requested by the model in the previous step
        if (comp->eventInfo.terminateSimulation) {
            FILTERED_LOG(comp, fmiDiscard, LOG_ALL, "fmiDoStep: model requested termination at t=%g", comp->time)
                return fmiDiscard; // enforce termination of the simulation loop
        }
    }
    return fmiOK;
}

/* Inquire slave status */
static fmiStatus getStatus(char* fname, fmiComponent c, const fmiStatusKind s) {
    const char *statusKind[3] = {"fmiDoStepStatus","fmiPendingStatus","fmiLastSuccessfulTime"};
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, fname, modelInitialized|modelStepping))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "$s: fmiStatusKind = %s", fname, statusKind[s])

        switch(s) {
        case fmiDoStepStatus: FILTERED_LOG(comp, fmiError, LOG_ERROR,
                "%s: Can be called with fmiDoStepStatus when fmiDoStep returned fmiPending."
                " This is not the case.", fname)
                break;
        case fmiPendingStatus: FILTERED_LOG(comp, fmiError, LOG_ERROR,
                "%s: Can be called with fmiPendingStatus when fmiDoStep returned fmiPending."
                " This is not the case.", fname)
                break;
        case fmiLastSuccessfulTime: FILTERED_LOG(comp, fmiError, LOG_ERROR,
                "%s: Can be called with fmiLastSuccessfulTime when fmiDoStep returned fmiDiscard."
                " This is not the case.", fname)
                break;
        case fmiTerminated: FILTERED_LOG(comp, fmiError, LOG_ERROR,
                "%s: Can be called with fmiTerminated when fmiDoStep returned fmiDiscard."
                " This is not the case.", fname)
                break;
        }
    return fmiError;
}

fmiStatus fmiGetStatus(fmiComponent c, const fmiStatusKind s, fmiStatus *value) {
    return getStatus("fmiGetStatus", c, s);
}

fmiStatus fmiGetRealStatus(fmiComponent c, const fmiStatusKind s, fmiReal *value) {
    if (s == fmiLastSuccessfulTime) {
        ModelInstance *comp = (ModelInstance *)c;
        *value = comp->time;
        return fmiOK;
    }
    return getStatus("fmiGetRealStatus", c, s);
}

fmiStatus fmiGetIntegerStatus(fmiComponent c, const fmiStatusKind s, fmiInteger *value) {
    return getStatus("fmiGetIntegerStatus", c, s);
}

fmiStatus fmiGetBooleanStatus(fmiComponent c, const fmiStatusKind s, fmiBoolean *value) {
    if (s == fmiTerminated) {
        ModelInstance *comp = (ModelInstance *)c;
        *value = comp->eventInfo.terminateSimulation;
        return fmiOK;
    }
    return getStatus("fmiGetBooleanStatus", c, s);
}

fmiStatus fmiGetStringStatus(fmiComponent c, const fmiStatusKind s, fmiString *value) {
    return getStatus("fmiGetStringStatus", c, s);
}

// ---------------------------------------------------------------------------
// Functions for FMI for Model Exchange
// ---------------------------------------------------------------------------
#else
/* Enter and exit the different modes */
fmiStatus fmiEnterEventMode(fmiComponent c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiEnterEventMode", modelStepping))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiEnterEventMode")

        return fmiOK;
}

fmiStatus fmiNewDiscreteStates(fmiComponent c, fmiEventInfo *eventInfo) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiNewDiscreteStates", modelInitialized|modelStepping))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiNewDiscreteStates")

        if (comp->state == modelStepping) {
            comp->eventInfo.newDiscreteStatesNeeded = fmiFalse;
            comp->eventInfo.terminateSimulation = fmiFalse;
            comp->eventInfo.nominalsOfContinuousStatesChanged = fmiFalse;
            comp->eventInfo.valuesOfContinuousStatesChanged = fmiFalse;
            comp->eventInfo.nextEventTimeDefined = fmiFalse;
            comp->eventInfo.nextEventTime = 0; // next time event if nextEventTimeDefined = fmiTrue

            eventUpdate(comp, &comp->eventInfo);
        }

    // model in stepping state
    comp->state = modelStepping;

    // copy internal eventInfo of component to output eventInfo
    eventInfo->newDiscreteStatesNeeded = comp->eventInfo.newDiscreteStatesNeeded;
    eventInfo->terminateSimulation = comp->eventInfo.terminateSimulation;
    eventInfo->nominalsOfContinuousStatesChanged = comp->eventInfo.nominalsOfContinuousStatesChanged;
    eventInfo->valuesOfContinuousStatesChanged = comp->eventInfo.valuesOfContinuousStatesChanged;
    eventInfo->nextEventTimeDefined = comp->eventInfo.nextEventTimeDefined;
    eventInfo->nextEventTime = comp->eventInfo.nextEventTime;

    return fmiOK;
}

fmiStatus fmiEnterContinuousTimeMode(fmiComponent c) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiEnterContinuousTimeMode", modelStepping))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL,"fmiEnterContinuousTimeMode")
        return fmiOK;
}

fmiStatus fmiCompletedIntegratorStep(fmiComponent c, fmiBoolean noSetFMUStatePriorToCurrentPoint,
        fmiBoolean *enterEventMode, fmiBoolean *terminateSimulation) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiCompletedIntegratorStep", modelStepping))
        return fmiError;
    if (nullPointer(comp, "fmiCompletedIntegratorStep", "enterEventMode", enterEventMode))
        return fmiError;
    if (nullPointer(comp, "fmiCompletedIntegratorStep", "terminateSimulation", terminateSimulation))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL,"fmiCompletedIntegratorStep")
        *enterEventMode = fmiFalse;
    *terminateSimulation = fmiFalse;
    return fmiOK;
}

/* Providing independent variables and re-initialization of caching */
fmiStatus fmiSetTime(fmiComponent c, fmiReal time) {
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiSetTime", modelInstantiated|modelInitialized|modelStepping))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetTime: time=%.16g", time)
        comp->time = time;
    return fmiOK;
}

fmiStatus fmiSetContinuousStates(fmiComponent c, const fmiReal x[], size_t nx){
    ModelInstance *comp = (ModelInstance *)c;
#if NUMBER_OF_REALS>0
    int i;
#endif
    if (invalidState(comp, "fmiSetContinuousStates", modelStepping))
        return fmiError;
    if (invalidNumber(comp, "fmiSetContinuousStates", "nx", nx, NUMBER_OF_STATES))
        return fmiError;
    if (nullPointer(comp, "fmiSetContinuousStates", "x[]", x))
        return fmiError;
#if NUMBER_OF_REALS>0
    for (i = 0; i < nx; i++) {
        fmiValueReference vr = vrStates[i];
        FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetContinuousStates: #r%d#=%.16g", vr, x[i])
            assert(vr >= 0 && vr < NUMBER_OF_REALS);
        comp->r[vr] = x[i];
    }
#endif
    return fmiOK;
}

/* Evaluation of the model equations */
fmiStatus fmiGetDerivatives(fmiComponent c, fmiReal derivatives[], size_t nx) {
#if NUMBER_OF_STATES>0
    int i;
#endif
    ModelInstance* comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiGetDerivatives", modelInitialized|modelStepping|modelTerminated))
        return fmiError;
    if (invalidNumber(comp, "fmiGetDerivatives", "nx", nx, NUMBER_OF_STATES))
        return fmiError;
    if (nullPointer(comp, "fmiGetDerivatives", "derivatives[]", derivatives))
        return fmiError;
#if NUMBER_OF_STATES>0
    for (i = 0; i < nx; i++) {
        fmiValueReference vr = vrStates[i] + 1;
        derivatives[i] = getReal(comp, vr); // to be implemented by the includer of this file
        FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiGetDerivatives: #r%d# = %.16g", vr, derivatives[i])
            }
#endif
    return fmiOK;
}

fmiStatus fmiGetEventIndicators(fmiComponent c, fmiReal eventIndicators[], size_t ni) {
#if NUMBER_OF_EVENT_INDICATORS>0
    int i;
#endif
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiGetEventIndicators", modelInitialized|modelStepping|modelTerminated))
        return fmiError;
    if (invalidNumber(comp, "fmiGetEventIndicators", "ni", ni, NUMBER_OF_EVENT_INDICATORS))
        return fmiError;
#if NUMBER_OF_EVENT_INDICATORS>0
    for (i = 0; i < ni; i++) {
        eventIndicators[i] = getEventIndicator(comp, i); // to be implemented by the includer of this file
        FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiGetEventIndicators: z%d = %.16g", i, eventIndicators[i])
            }
#endif
    return fmiOK;
}

fmiStatus fmiGetContinuousStates(fmiComponent c, fmiReal states[], size_t nx) {
#if NUMBER_OF_REALS>0
    int i;
#endif
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiGetContinuousStates", modelInitialized|modelStepping|modelTerminated))
        return fmiError;
    if (invalidNumber(comp, "fmiGetContinuousStates", "nx", nx, NUMBER_OF_STATES))
        return fmiError;
    if (nullPointer(comp, "fmiGetContinuousStates", "states[]", states))
        return fmiError;
#if NUMBER_OF_REALS>0
    for (i = 0; i < nx; i++) {
        fmiValueReference vr = vrStates[i];
        states[i] = getReal(comp, vr); // to be implemented by the includer of this file
        FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiGetContinuousStates: #r%u# = %.16g", vr, states[i])
            }
#endif
    return fmiOK;
}

fmiStatus fmiGetNominalsOfContinuousStates(fmiComponent c, fmiReal x_nominal[], size_t nx) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    if (invalidState(comp, "fmiGetNominalContinuousStates", modelInstantiated|modelInitialized|modelStepping|modelTerminated))
        return fmiError;
    if (invalidNumber(comp, "fmiGetNominalContinuousStates", "nx", nx, NUMBER_OF_STATES))
        return fmiError;
    if (nullPointer(comp, "fmiGetNominalContinuousStates", "x_nominal[]", x_nominal))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiGetNominalContinuousStates: x_nominal[0..%d] = 1.0", nx-1)
        for (i = 0; i < nx; i++)
            x_nominal[i] = 1;
    return fmiOK;
}
#endif // Model Exchange
