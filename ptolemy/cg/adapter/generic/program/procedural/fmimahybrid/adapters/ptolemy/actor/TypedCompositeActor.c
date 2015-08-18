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

fmi2Integer tEnd = 10;
fmi2Integer tStart = 0;

/**/

/***staticDeclareBlock***/

static fmi2Component initializeFMU(FMU *fmu, fmi2Boolean visible,
fmi2Boolean loggingOn, int nCategories, fmi2String * categories[], const char* name) {
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
    // global unique id of the fmu
    const char *guid = getAttributeValue((Element *) md, att_guid);
    // check for ability to get and set state
    fmu->canGetAndSetFMUstate = getAttributeBool((Element*) getCoSimulation(md),
    att_canGetAndSetFMUstate, &vs);
    fmu->canGetMaxStepSize = getAttributeBool((Element*) getCoSimulation(md),
    att_canGetMaxStepSize, &vs);
    // instance name
    const char *instanceName = getAttributeValue(
    (Element *) getCoSimulation(md), att_modelIdentifier);
    // path to the fmu resources as URL, "file://C:\QTronic\sales"
    char *fmuResourceLocation = getTempResourcesLocation(); // TODO: returns crap. got to save the location for every FMU somehow.
    fmu->handleIntegerTime = (fmi2Boolean) getAttributeValue((Element *) getCoSimulation(md), att_canHandleIntegerTime);
    fmi2Component comp = fmu->instantiate(instanceName, fmi2CoSimulation, guid,
    fmuResourceLocation, &fmu->callbacks, visible, loggingOn);
    printf("instance name: %s, \nguid: %s, \nressourceLocation: %s\n",
    instanceName, guid, fmuResourceLocation);
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
        fmi2Flag = fmu->setDebugLogging(comp, fmi2True, nCategories,
        (const fmi2String*) categories);
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
        printf("Getting the value of an FMU caused an error.");
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
        fmi2Flag = connection->sinkFMU->setHybridString(connection->sinkFMU->component, &connection->sinkPort, 1,        &tempString, &tmpAbsent);
        break;
        default:
        return fmi2Error;
    }
    return fmi2Flag;
}
void terminateSimulation(FMU *fmus, int returnValue, FILE* file, fmi2Integer stepSize,
int nSteps) {
    const char* STATUS[] = { "fmi2OK", "fmi2Warning", "fmi2Discard",
    "fmi2Error", "fmi2Fatal", "fmi2Pending" };
    for (int i = 0; i < NUMBER_OF_FMUS; i++) {
        fmus[i].terminate(fmus[i].component);
        if (fmus[i].lastFMUstate != NULL) {
            fmi2Status status = fmus[i].freeFMUstate(fmus[i].component,
            &fmus[i].lastFMUstate);
            printf("Terminating with status: %s\n", STATUS[status]);
        }
        fmus[i].freeInstance(fmus[i].component);
    }
    // print simulation summary
    if (returnValue == 1) {
        printf("Simulation from %ld to %ld terminated successful\n", tStart,
        tEnd);
    } else {
        printf("Simulation from %ld to %ld terminated early!\n", tStart, tEnd);
    }
    printf("  steps ............ %d\n", nSteps);
    printf("  fixed step size .. %ld\n", stepSize);
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

// Determine the minimum resolution among the User-Defined and the resolution required by the FMUs
// It also return the scaleFactor for each FMU, in order to perform conversion of time between
// the MA resolution and the FMU resolution
void setTimingResolutions(FMU *fmus, fmi2Integer *resolution, fmi2Integer scaleFactor[]) {
    // Determine the minimum resolution
    fmi2Integer ma_resolution = *resolution;
    for (int i = 0; i < NUMBER_OF_FMUS; i++) {
        FMU fmu = fmus[i];
        if (fmu.handleIntegerTime) {
            fmi2Integer fmu_resolution;
            fmi2Status currentStatus = fmu.getTimeResolution(fmu.component, &fmu_resolution);
            if (currentStatus > fmi2OK)
            	printf("FMU (%s) does generated an error while retrieving the time resolution\n", NAMES_OF_FMUS[i]);
            ma_resolution = min(ma_resolution, fmu_resolution);
        }
    }
    *resolution = ma_resolution;
    for (int i = 0; i < NUMBER_OF_FMUS; i++) {
        FMU fmu = fmus[i];
        if (fmu.handleIntegerTime) {
            fmi2Integer fmu_resolution;
            fmi2Status currentStatus = fmu.getTimeResolution(fmu.component, &fmu_resolution);
            if (currentStatus > fmi2OK)
            	printf("FMU (%s) does generated an error while retrieving the time resolution\n", NAMES_OF_FMUS[i]);
            fmi2Integer precision_mismatch = fmu_resolution - *resolution;
            scaleFactor[i] = (fmi2Integer) pow(10, precision_mismatch);
        }
    }
//    // Set the resolution
//    for (int i = 0; i < NUMBER_OF_FMUS; i++) {
//        FMU fmu = fmus[i];
//        if (fmu.handleIntegerTime) {
//            fmi2Integer precision;
//            fmi2Status currentStatus = fmu.getTimeResolution(fmu.component, &precision);
//            scaleFactor[i] = (fmi2Integer) pow(10,  precision - *resolution);
//            currentStatus = fmu.setTimeResolution(fmu.component, precision);
//        }
//        else {
//            scaleFactor[i] = (fmi2Integer) pow(10, - *resolution);
//        }
//    }
}

fmi2Status getMaxStepSize(FMU *fmu, fmi2Integer scaleFactor, fmi2Integer *maxStepSize) {
    fmi2Status currentStatus = fmi2OK;
    if ((*fmu).handleIntegerTime) {
        fmi2Integer tmp;
        currentStatus = (*fmu).getHybridMaxStepSize((*fmu).component, &tmp);
        *maxStepSize = tmp * scaleFactor;
    }
//    else {
//        fmi2Real maxStepSize_tmp;
//        currentStatus = (*fmu).getMaxStepSize((*fmu).component, &maxStepSize_tmp);
//        maxStepSize_tmp = maxStepSize_tmp * scaleFactor;
//        *maxStepSize = (fmi2Integer) maxStepSize_tmp;
//    }
    return currentStatus;
}
fmi2Status doStep(FMU *fmu, fmi2Integer time, fmi2Integer stepSize, fmi2Boolean stateAction, fmi2Integer localTime, fmi2Integer scaleFactor) {
    fmi2Status currentStatus = fmi2OK;
    if ((*fmu).handleIntegerTime) {
        fmi2Integer stepSize_c = stepSize + (time - localTime);
        fmi2Integer stepSize_c_local = stepSize_c / scaleFactor;
        if (stepSize_c >= scaleFactor || stepSize == 0) {
            currentStatus = (*fmu).doHybridStep((*fmu).component, time, stepSize_c_local, stateAction);
        }
    }
    //    else {
        //        fmi2Real time_tmp = (time / 1.0) / scaleFactor;
        //        fmi2Real stepSize_tmp = (fmi2Real) (stepSize / 1.0);
        //        stepSize_tmp = stepSize_tmp / scaleFactor;
        //        currentStatus = (*fmu).doStep((*fmu).component, time_tmp, stepSize_tmp, noSetFMUStatePriorToCurrentPoint);
    //    }
    return currentStatus;
}

//fmi2Status getRealStatus(FMU *fmu, fmi2StatusKind fmi2LastSuccessfulTime, fmi2Integer *lastSuccessfulTime, fmi2Integer scaleFactor) {
//    fmi2Status currentStatus = fmi2OK;
//    if ((*fmu).handleIntegerTime) {
//        currentStatus = fmu->getRealStatus((*fmu).component, fmi2LastSuccessfulTime, &lastSuccessfulTime);
//        *lastSuccessfulTime = *lastSuccessfulTime * scaleFactor;
//    }
//    else {
//        fmi2Real lastSuccessfulTime_tmp;
//        currentStatus = (*fmu).getRealStatus(fmu->component, fmi2LastSuccessfulTime, &lastSuccessfulTime_tmp);
//        lastSuccessfulTime_tmp = lastSuccessfulTime_tmp * scaleFactor;
//        *lastSuccessfulTime = (fmi2Integer) lastSuccessfulTime_tmp;
//    }
//    return currentStatus;
//}

// simulate the given FMUs from tStart = 0 to tEnd.
static int simulate(FMU *fmus, portConnection* connections, fmi2Integer requiredResolution,
fmi2Boolean loggingOn, char separator) {
    // Simulation variables
    fmi2Integer time = 0;
    fmi2Integer nSteps = 0;
    fmi2Integer stepSize = 1;
    fmi2Integer resolution = requiredResolution;
    fmi2Integer scaleFactor[NUMBER_OF_FMUS];
    fmi2Integer localTime[NUMBER_OF_FMUS]; // keep track of the FMU local time
    FILE* file;
    int legacyFmuIndex = 0;
    bool isLegacyFmu = false;

    // Open result file
    if (!(file = fopen(RESULT_FILE, "w"))) {
        printf("could not write %s because:\n", RESULT_FILE);
        printf("    %s\n", strerror(errno));
        return 0;
    }
    // Check for legacy FMUs
    fmi2Status legacyFmuStatus = checkForLegacyFMUs(fmus, &isLegacyFmu, &legacyFmuIndex);
    if ( legacyFmuStatus > fmi2Warning) {
        terminateSimulation(fmus, 0, file, stepSize, nSteps);
        return 0;
    }
    // Set FMU parameters from PtolemyII model
    for (int i = 0; i < NUMBER_OF_FMUS; i++) {
        setupParameters(&fmus[i]);
    }
    // Set FMUs local time to 0 (locally)
    for (int i = 0; i < NUMBER_OF_FMUS; i++) {
        localTime[i] = 0;
    }

    // Determine the simulation resolution
    setTimingResolutions(fmus, &resolution, scaleFactor);

    // output solution for time t0
    outputRow(fmus, NUMBER_OF_FMUS, NAMES_OF_FMUS, time, 0, file, separator, TRUE);
    // Simulation loop
    while (time <= tEnd) {
        // Set connection values
        for (int i = 0; i < NUMBER_OF_EDGES; i++) {
            setValue(&connections[i]);
        }
        outputRow(fmus, NUMBER_OF_FMUS, NAMES_OF_FMUS, time, 0, file, separator, FALSE);
        // Compute the maximum step size
        // (I) Predictable FMUs
        for (int i = 0; i < NUMBER_OF_FMUS; i++) {
            if (fmus[i].canGetMaxStepSize) {
                fmi2Integer maxStepSize;
                fmi2Status currentStatus = getMaxStepSize(&(fmus[i]), scaleFactor[i], &maxStepSize);
                if (currentStatus > fmi2Warning) {
                    terminateSimulation(fmus, 0, file, stepSize, nSteps);
                    return 0;
                }
                maxStepSize = maxStepSize * scaleFactor[i] - (time - localTime[i]);
                stepSize = min(stepSize, maxStepSize);
            }
        }
        // Compute the maximum step size
        // (II) Rollback FMUs
        for (int i = 0; i < NUMBER_OF_FMUS; i++) {
            if (fmus[i].canGetAndSetFMUstate && !fmus[i].canGetMaxStepSize) {
                fmi2Status currentStatus = fmus[i].getFMUstate(fmus[i].component, &fmus[i].lastFMUstate);
                if (currentStatus > fmi2Warning) {
                    terminateSimulation(fmus, 0, file, stepSize, nSteps);
                    return 0;
                }
                currentStatus = doStep(&fmus[i], time, stepSize, fmi2False, localTime[i], scaleFactor[i]);
                if (currentStatus > fmi2Discard) {
                    terminateSimulation(fmus, 0, file, stepSize, nSteps);
                    return 0;
                }
                fmi2Integer lastSuccessfulTime = time + stepSize; // This is an artifact! We need a method to retrive the integer time
                fmi2Integer maxStepSize;
                maxStepSize = (lastSuccessfulTime - localTime[i]) - (time - localTime[i]);
                if (maxStepSize != 0) {
                    stepSize = min(stepSize, maxStepSize);
                }
            }
        }
        // Compute the maximum step size
        // (III) Legacy FMUs
           if (isLegacyFmu) {
                           fmi2Status currentStatus = doStep(&fmus[legacyFmuIndex], time, stepSize, fmi2False, localTime[i], scaleFactor[legacyFmuIndex]);
                           if (currentStatus > fmi2Warning) {
                                           printf("Could not step FMU (%s) [Legacy FMU]. Terminating simulation.\n", NAMES_OF_FMUS[legacyFmuIndex]);
                                           terminateSimulation(fmus, 0, file, stepSize, nSteps);
                                           return 0;
                           }
                           fmi2Real lastSuccessfulTime;
                           currentStatus = fmus[i].getRealStatus(fmus[i].component, fmi2LastSuccessfulTime, &lastSuccessfulTime);
                           if (currentStatus > fmi2Warning) {
                                           printf("Could get the last successful time instant for FMU (%s). Terminating simulation.\n", NAMES_OF_FMUS[legacyFmuIndex]);
                                           terminateSimulation(fmus, 0, file, stepSize, nSteps);
                                           return 0;
                           }
                           fmi2Integer maxStepSize;
                           maxStepSize = lastSuccessfulTime - time;
                           if (maxStepSize == 0) {
                                           stepSize = min(stepSize, maxStepSize);
                           }
           }
        // Rolling back FMUs of type (II)
        {
            fmi2Status currentStatus = rollbackFMUs(fmus);
            if (currentStatus > fmi2Warning) {
                terminateSimulation(fmus, 0, file, stepSize, nSteps);
                return 0;
            }
        }
        // Perform doStep() for all FMUs with the discovered stepSize
        for (int i = 0; i < NUMBER_OF_FMUS; i++) {
            if (fmus[i].canGetMaxStepSize || fmus[i].canGetAndSetFMUstate) {
                fmi2Status currentStatus = doStep(&fmus[i], time, stepSize, fmi2True, localTime[i], scaleFactor[i]);
                if (currentStatus > fmi2Warning) {
                    terminateSimulation(fmus, 0, file, stepSize, nSteps);
                    return 0;
                }
                fmi2Integer stepSize_c = stepSize - (time - localTime[i]);
//                fmi2Integer stepSize_c_local = stepSize_c / scaleFactor[i];
                if (stepSize_c >= scaleFactor[i]) {
                    localTime[i] += stepSize;
                }
            }
        }
        nSteps++;
        time += stepSize;
        stepSize = 1;
    }
    terminateSimulation(fmus, 1, file, stepSize, nSteps);
    return 1;
}
/**/
