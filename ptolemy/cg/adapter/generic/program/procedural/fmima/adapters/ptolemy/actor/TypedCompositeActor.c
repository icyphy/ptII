/***variableDeclareBlock***/

#include <stdio.h>

/* Check for __linux__ after including stdio.h, otherwise it is sometimes not defined. */
#ifdef __linux__
/* Needed for strdup and mkdtemp under RHEL 6.1 */
#define __USE_BSD

/* Needed for strdup and mkdtemp under Gentoo.
 * see http://polarhome.com/service/man/?qf=STRDUP&af=0&tf=2&of=Gentoo
 * If you change this file, then please change
 * ptolemy/actor/lib/fmi/ma2/shared/sim_support.c
 * On 01/27/2015 Marten wrote: Doubtful whether this is still true after
 * the -std=gnu99 flag was turned on. Moreover, glibc 2.20, the
 * _BSD_SOURCE macro is deprecated, see:
 * http://man7.org/linux/man-pages/man7/feature_test_macros.7.html.
 */
#define _BSD_SOURCE
#endif
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <stdbool.h>
#include "fmi2.h"
#include "sim_support.h"

double tEnd = 100.0;
double tStart = 0;
#define DEFAULT_COMM_STEP_SIZE 10
#define ERROR 0
#define OK 1
/**/

/***staticDeclareBlock***/

static fmi2Component initializeFMU(FMU *fmu, fmi2Boolean visible, fmi2Boolean loggingOn, int nCategories, fmi2String * categories[], const char* name) {
    fmi2Status fmi2Flag;                     // return code of the fmu functions
    // instantiate the fmu
    fmu->callbacks.logger = fmuLogger;
    fmu->callbacks.allocateMemory = calloc;
    fmu->callbacks.freeMemory = free;
    fmu->callbacks.stepFinished = NULL; // fmi2DoStep has to be carried out synchronously
    fmu->callbacks.componentEnvironment = fmu; // pointer to current fmu from the environment.
    fmi2Real tolerance = 0;                 // used in setting up the experiment
    fmi2Boolean toleranceDefined = fmi2False; // true if model description define tolerance
    ValueStatus vs = valueIllegal;
    // handle to the parsed XML file
    ModelDescription* md = fmu->modelDescription;
    const char *guid = getAttributeValue((Element *) md, att_guid);
    fmu->canGetAndSetFMUstate = getAttributeBool((Element*) getCoSimulation(md), att_canGetAndSetFMUstate, &vs);
    fmu->canGetMaxStepSize = getAttributeBool((Element*) getCoSimulation(md), att_canGetMaxStepSize, &vs);
    fmu->maxOutputDerivativeOrder = getAttributeInt((Element *) getCoSimulation(md), att_maxOutputDerivativeOrder, &vs);
    fmu->canInterpolateInputs = getAttributeBool((Element *) getCoSimulation(md), att_canInterpolateInputs, &vs);
    const char *instanceName = getAttributeValue((Element *) getCoSimulation(md), att_modelIdentifier);
    char *fmuResourceLocation = getTempResourcesLocation(); // TODO: returns crap. got to save the location for every FMU somehow.
    // instance of the fmu
    fmi2Component comp = fmu->instantiate(name, fmi2CoSimulation, guid, fmuResourceLocation, &fmu->callbacks, visible, loggingOn);
    printf("instance name: %s, \nguid: %s, \nressourceLocation: %s\n", instanceName, guid, fmuResourceLocation);
    free(fmuResourceLocation);
    if (!comp) {
        printf("Could not initialize model with guid: %s\n", guid);
        return NULL;
    }
    Element *defaultExp = getDefaultExperiment(fmu->modelDescription);
    if (defaultExp) {
        tolerance = getAttributeDouble(defaultExp, att_tolerance, &vs);
    }
    if (vs == valueDefined) {
        toleranceDefined = fmi2True;
    }
    if (nCategories > 0) {
        fmi2Flag = fmu->setDebugLogging(comp, fmi2True, nCategories, (const fmi2String*) categories);
        if (fmi2Flag > fmi2Warning) {
            error("could not initialize model; failed FMI set debug logging");
            return NULL;
        }
    }
    fmi2Flag = fmu->setupExperiment(comp, toleranceDefined, tolerance, tStart,
    fmi2True, tEnd);
    if (fmi2Flag > fmi2Warning) {
        error("could not initialize model; failed FMI setup experiment");
        return NULL;
    }
    return comp;
}

static fmi2Status checkForLegacyFMUs(FMU* fmus, bool *isLegacyFmu, int*legacyFmuIndex) {
    printf("Checking for legacy FMUs!\n");
    int legacyFmus = 0;
    for (int i = 0; i < NUMBER_OF_FMUS; i++) {
        if (!fmus[i].canGetAndSetFMUstate) {
            legacyFmus++;
            *legacyFmuIndex = i;
            *isLegacyFmu = true;
            if (legacyFmus > 1) {
                printf("More than one legacy FMU detected. The system cannot be simulated.\n");
                return fmi2Error;
            }
        }
    }
    return fmi2OK;
}

static fmi2Status setValue(portConnection* connection) {
    fmi2Status fmi2Flag;
    fmi2Integer tempInt;
    fmi2Real tempReal;
    fmi2Boolean tempBoolean;
    fmi2String tempString;
    fmi2Integer tmpAbsent;
    // get source value and cast if necessary
    switch (connection->sourceType) {
        case fmi2_Integer:
            fmi2Flag = connection->sourceFMU->getHybridInteger(connection->sourceFMU->component, &connection->sourcePort, 1, &tempInt, &tmpAbsent);
            tempReal = (fmi2Real) tempInt;
            tempBoolean = (tempInt == 0 ? fmi2False : fmi2True);
            break;
        case fmi2_Real:
            fmi2Flag = connection->sourceFMU->getHybridReal(connection->sourceFMU->component, &connection->sourcePort, 1, &tempReal, &tmpAbsent);
            tempInt = (fmi2Integer) round(tempReal);
            tempBoolean = (tempReal == 0.0 ? fmi2False : fmi2True);
            break;
        case fmi2_Boolean:
            fmi2Flag = connection->sourceFMU->getHybridBoolean(connection->sourceFMU->component, &connection->sourcePort, 1, &tempBoolean, &tmpAbsent);
            tempInt = (fmi2Integer) tempBoolean;
            tempReal = (fmi2Real) tempBoolean;
            break;
        case fmi2_String:
            fmi2Flag = connection->sourceFMU->getHybridString(connection->sourceFMU->component, &connection->sourcePort, 1, &tempString, &tmpAbsent);
            break;
        default:
            return fmi2Error;
    }
    if (fmi2Flag > fmi2Warning) {
        printf("Getting the value from the FMU caused an error.\n");
        return fmi2Flag;
    }
    if (connection->sourceType != connection->sinkType && (connection->sinkType == fmi2_String || connection->sourceType == fmi2_String)) {
        printf("A connection of FMUs has incompatible data types. Terminating simulation.\n");
        return fmi2Error;
    }
    // set sink value
    switch (connection->sinkType) {
        case fmi2_Integer:
            fmi2Flag = connection->sinkFMU->setHybridInteger(connection->sinkFMU->component, &connection->sinkPort, 1, &tempInt, &tmpAbsent);
            break;
        case fmi2_Real:
            fmi2Flag = connection->sinkFMU->setHybridReal(connection->sinkFMU->component, &connection->sinkPort, 1, &tempReal, &tmpAbsent);
            break;
        case fmi2_Boolean:
            fmi2Flag = connection->sinkFMU->setHybridBoolean(connection->sinkFMU->component, &connection->sinkPort, 1, &tempBoolean, &tmpAbsent);
            break;
        case fmi2_String:
            fmi2Flag = connection->sinkFMU->setHybridString(connection->sinkFMU->component, &connection->sinkPort, 1, &tempString, &tmpAbsent);
            break;
        default:
            return fmi2Error;
    }
    if (fmi2Flag > fmi2Warning) {
        printf("Setting the value from the FMU caused an error.\n");
        return fmi2Flag;
    }
    return fmi2Flag;
}
static fmi2Status setDerivatives(portConnection* connection) {
    fmi2Status fmi2Flag;
    FMU *source = connection->sourceFMU;
    FMU *sink   = connection->sinkFMU;
    fmi2Real tempDer;
    fmi2Integer derOrder = 1;
    if (source->maxOutputDerivativeOrder > 0 && sink->canInterpolateInputs == fmi2True) {
        // (fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2Integer order[], fmi2Real value[])
        fmi2Flag = source->getRealOutputDerivatives(source->component, &connection->sourcePort, 1, &derOrder, &tempDer);
        if (fmi2Flag > fmi2Warning) {
            printf("Getting the derivatives from the FMU caused an error.\n");
            return fmi2Flag;
        }
        // (fmi2Component c, const fmi2ValueReference vr[], size_t nvr, const fmi2Integer order[], const fmi2Real value[])
        fmi2Flag = sink->setRealInputDerivatives(sink->component, &connection->sinkPort, 1, &derOrder, &tempDer);
        if (fmi2Flag > fmi2Warning) {
            printf("Setting the derivatives from the FMU caused an error.\n");
            return fmi2Flag;
        }
    }
    return fmi2OK;
}
void terminateSimulation(FMU *fmus, int returnValue, FILE* file, double h, int nSteps) {
    const char* STATUS[] = { "fmi2OK", "fmi2Warning", "fmi2Discard", "fmi2Error", "fmi2Fatal", "fmi2Pending" };
    for (int i = 0; i < NUMBER_OF_FMUS; i++) {
        fmus[i].terminate(fmus[i].component);
        if (fmus[i].lastFMUstate != NULL) {
            fmi2Status status = fmus[i].freeFMUstate(fmus[i].component, &fmus[i].lastFMUstate);
            printf("Terminating with status: %s\n", STATUS[status]);
        }
        fmus[i].freeInstance(fmus[i].component);
    }

    // print simulation summary
    if (returnValue == 1) {
        printf("Simulation from %g to %g terminated successful\n", tStart, tEnd);
    } else {
        printf("Simulation from %g to %g terminated early!\n", tStart, tEnd);
    }
    printf("  steps ............ %d\n", nSteps);
    printf("  fixed step size .. %g\n", h);
    fclose(file);
    return;
}

static fmi2Status rollbackFMUs(FMU *fmus) {
    for (int i = 0; i < NUMBER_OF_FMUS; i++) {
        if (fmus[i].canGetAndSetFMUstate && !fmus[i].canGetMaxStepSize) {
            fmi2Status localStatus = fmus[i].setFMUstate(fmus[i].component, fmus[i].lastFMUstate);
            if (localStatus > fmi2Warning) {
                printf("Rolling back FMU (%s) failed!\n", NAMES_OF_FMUS[i]);
                return localStatus;
            }
        }
    }
    return fmi2OK;
}

// simulate the given FMUs from tStart = 0 to tEnd.
static int simulate(FMU *fmus, portConnection* connections, double h, fmi2Boolean loggingOn, char separator) {
    // Simulation variables
    fmi2Real time = tStart;
    fmi2Real stepSize = h;
    int nSteps = 0;
    FILE* file;
    int legacyFmuIndex = 0;
    bool isLegacyFmu = false;
    // Open result file
    if (!(file = fopen(RESULT_FILE, "w"))) {
        printf("could not write %s because:\n", RESULT_FILE);
        printf("    %s\n", strerror(errno));
        return ERROR;
    }
    // Check for legacy FMUs
    if (checkForLegacyFMUs(fmus, &isLegacyFmu, &legacyFmuIndex) > fmi2Warning) {
        terminateSimulation(fmus, ERROR, file, stepSize, nSteps);
        return ERROR;
    }
    for (int i = 0; i < NUMBER_OF_FMUS; i++) {
        setupParameters(&fmus[i]);
    }
    // output solution for time t0
    outputRow(fmus, NUMBER_OF_FMUS, NAMES_OF_FMUS, time, file, separator, TRUE);
    // Simulation loop
    while (time < (tEnd - h)) {
        // Set connection values
        for (int i = 0; i < NUMBER_OF_EDGES; i++) {
            setValue(&connections[i]);
                        setDerivatives(&connections[i]);
        }
        outputRow(fmus, NUMBER_OF_FMUS, NAMES_OF_FMUS, time, file, separator, FALSE);
        // Compute the maximum step size
        // (I) Predictable FMUs
        for (int i = 0; i < NUMBER_OF_FMUS; i++) {
            if (fmus[i].canGetMaxStepSize) {
                fmi2Real maxStepSize;
                fmi2Status currentStatus = fmus[i].getMaxStepSize(fmus[i].component, &maxStepSize);
                if (currentStatus > fmi2Warning) {
                    terminateSimulation(fmus, ERROR, file, stepSize, nSteps);
                    return ERROR;
                }
                stepSize = min(stepSize, maxStepSize);
            }
        }
        // Compute the maximum step size
        // (II) Rollback FMUs
        for (int i = 0; i < NUMBER_OF_FMUS; i++) {
            if (fmus[i].canGetAndSetFMUstate && !fmus[i].canGetMaxStepSize) {
                fmi2Status currentStatus = fmus[i].getFMUstate(fmus[i].component, &fmus[i].lastFMUstate);
                if (currentStatus > fmi2Warning) {
                    terminateSimulation(fmus, ERROR, file, stepSize, nSteps);
                    return ERROR;
                }
                currentStatus = fmus[i].doStep(fmus[i].component, time, stepSize, fmi2False);
                if (currentStatus > fmi2Discard) {
                    terminateSimulation(fmus, ERROR, file, stepSize, nSteps);
                    return ERROR;
                }
                if (currentStatus >= fmi2Discard) {
                        fmi2Real lastSuccessfulTime;
                        currentStatus = fmus[i].getRealStatus(fmus[i].component, fmi2LastSuccessfulTime, &lastSuccessfulTime);
                        if (currentStatus > fmi2Warning) {
                                    terminateSimulation(fmus, ERROR, file, stepSize, nSteps);
                                    return ERROR;
                        }
                        fmi2Real maxStepSize = lastSuccessfulTime - time;
                        stepSize = min(stepSize, maxStepSize);
                }
            }
        }
        // Compute the maximum step size
        // (III) Legacy FMUs
        if (isLegacyFmu) {
            fmi2Real maxStepSize;
            fmi2Status currentStatus = fmus[legacyFmuIndex].doStep(fmus[legacyFmuIndex].component, time, stepSize, fmi2False);
            if (currentStatus > fmi2Discard) {
                printf("Could not step FMU (%s) [Legacy FMU]. Terminating simulation.\n", NAMES_OF_FMUS[legacyFmuIndex]);
                terminateSimulation(fmus, ERROR, file, stepSize, nSteps);
                return ERROR;
            }
            fmi2Real lastSuccessfulTime;
            currentStatus = fmus[legacyFmuIndex].getRealStatus(fmus[legacyFmuIndex].component, fmi2LastSuccessfulTime, &lastSuccessfulTime);
            if (currentStatus > fmi2Warning) {
                printf("Could not get the last successful time instant for FMU (%s). Terminating simulation.\n", NAMES_OF_FMUS[legacyFmuIndex]);
                terminateSimulation(fmus, ERROR, file, stepSize, nSteps);
                return ERROR;
            }
            maxStepSize = lastSuccessfulTime - time;
            stepSize = min(stepSize, maxStepSize);
        }
        // Rolling back FMUs of type (II)
        {
            fmi2Status currentStatus = rollbackFMUs(fmus);
            if (currentStatus > fmi2Warning) {
                printf("Error while rolling back FMUs. Terminating simulation.");
                terminateSimulation(fmus, ERROR, file, stepSize, nSteps);
                return ERROR;
            }
        }
        // Perform doStep() for all FMUs with the discovered stepSize
        for (int i = 0; i < NUMBER_OF_FMUS; i++) {
            if (fmus[i].canGetMaxStepSize || fmus[i].canGetAndSetFMUstate) {
                fmi2Status currentStatus = fmus[i].doStep(fmus[i].component, time, stepSize, fmi2True);
                if (currentStatus > fmi2Discard) {
                    printf("Could not step FMU (%s) after minimum step has been determined. Terminating simulation.\n", NAMES_OF_FMUS[i]);
                    terminateSimulation(fmus, ERROR, file, stepSize, nSteps);
                    return ERROR;
                }
            }
        }
        time += stepSize;
        nSteps++;
        stepSize = h;
    }
    terminateSimulation(fmus, OK, file, stepSize, nSteps);
    return OK;
}

/**/
