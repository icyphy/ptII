/*
@Copyright (c) 2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

						PT_COPYRIGHT_VERSION_2
						COPYRIGHTENDKEY


*/

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

double tEnd = 1.0;
double tStart = 0;

#define NUMBER_OF_FMUS 3
#define NUMBER_OF_EDGES 2
#define MODEL_NAME "demo"
const char* NAMES_OF_FMUS[] = {"inc20pt", "scale20pt", "out20pt"};


static fmi2Component initializeFMU(FMU *fmu, fmi2Boolean visible,
        fmi2Boolean loggingOn, int nCategories, char ** categories) {

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
    // instance of the fmu
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
    fmi2Flag = fmu->enterInitializationMode(comp);
    if (fmi2Flag > fmi2Warning) {
        error("could not initialize model; failed FMI enter initialization mode");
        return NULL;
    }
    printf("initialization mode entered\n");
    fmi2Flag = fmu->exitInitializationMode(comp);
    printf("successfully initialized.\n");

    if (fmi2Flag > fmi2Warning) {
        error("could not initialize model; failed FMI exit initialization mode");
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

    // get source value and cast if necessary
    switch (connection->sourceType) {
    case fmi2_Integer:
        fmi2Flag = connection->sourceFMU->getInteger(connection->sourceFMU->component, &connection->sourcePort, 1, &tempInt);
        tempReal = (fmi2Real) tempInt;
        tempBoolean = (tempInt == 0 ? fmi2False : fmi2True);
        break;
    case fmi2_Real:
        fmi2Flag = connection->sourceFMU->getReal(connection->sourceFMU->component, &connection->sourcePort, 1, &tempReal);
        tempInt = (fmi2Integer) round(tempReal);
        tempBoolean = (tempReal == 0.0 ? fmi2False : fmi2True);
        break;
    case fmi2_Boolean:
        fmi2Flag = connection->sourceFMU->getBoolean(connection->sourceFMU->component, &connection->sourcePort, 1, &tempBoolean);
        tempInt = (fmi2Integer) tempBoolean;
        tempReal = (fmi2Real) tempBoolean;
        break;
    case fmi2_String:
        fmi2Flag = connection->sourceFMU->getString(connection->sourceFMU->component, &connection->sourcePort, 1, &tempString);
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
        fmi2Flag = connection->sinkFMU->setInteger(connection->sinkFMU->component, &connection->sinkPort, 1, &tempInt);
        break;
    case fmi2_Real:
        fmi2Flag = connection->sinkFMU->setReal(connection->sinkFMU->component, &connection->sinkPort, 1, &tempReal);
        break;
    case fmi2_Boolean:
        fmi2Flag = connection->sinkFMU->setBoolean(connection->sinkFMU->component, &connection->sinkPort, 1, &tempBoolean);
        break;
    case fmi2_String:
        fmi2Flag = connection->sinkFMU->setString(connection->sinkFMU->component, &connection->sinkPort, 1, &tempString);
        break;
    default:
        return fmi2Error;
    }
    return fmi2Flag;
}

void terminateSimulation(FMU *fmus, int returnValue, FILE* file, double h,
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
        printf("Simulation from %g to %g terminated successful\n", tStart,
                tEnd);
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
static int simulate(FMU *fmus, portConnection* connections, double h,
        fmi2Boolean loggingOn, char separator) {

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
        return 0;
    }

    // Check for legacy FMUs
    if (checkForLegacyFMUs(fmus, &isLegacyFmu, &legacyFmuIndex) > fmi2Warning) {
        terminateSimulation(fmus, 0, file, h, nSteps);
        return 0;
    }

    // output solution for time t0
    outputRow(fmus, NUMBER_OF_FMUS, NAMES_OF_FMUS, time, file, separator, TRUE);
    outputRow(fmus, NUMBER_OF_FMUS, NAMES_OF_FMUS, time, file, separator, FALSE);

    // Simulation loop
    while (time < tEnd) {

        // Set connection values
        for (int i = 0; i < NUMBER_OF_EDGES; i++) {
            setValue(&connections[i]);
        }

        // Compute the maximum step size
        // (I) Predictable FMUs
        for (int i = 0; i < NUMBER_OF_FMUS; i++) {
            if (fmus[i].canGetMaxStepSize) {
                fmi2Real maxStepSize;
                fmi2Status currentStatus = fmus[i].getMaxStepSize(fmus[i].component, &maxStepSize);
                if (currentStatus > fmi2Warning) {
                    printf("Could get the MaxStepSize for FMU (%s). Terminating simulation.\n", NAMES_OF_FMUS[i]);
                    terminateSimulation(fmus, 0, file, h, nSteps);
                    return 0;
                }
                stepSize = min(stepSize, maxStepSize);
            }
        }

        // Compute the maximum step size
        // (II) Rollback FMUs
        for (int i = 0; i < NUMBER_OF_FMUS; i++) {
            if (fmus[i].canGetAndSetFMUstate && !fmus[i].canGetMaxStepSize) {
                fmi2Real maxStepSize;
                fmi2Status currentStatus = fmus[i].getFMUstate(fmus[i].component, &fmus[i].lastFMUstate);
                if (currentStatus > fmi2Warning) {
                    printf("Saving state of FMU (%s) failed. Terminating simulation. Terminating simulation.\n", NAMES_OF_FMUS[i]);
                    terminateSimulation(fmus, 0, file, h, nSteps);
                    return 0;
                }
                currentStatus = fmus[i].doStep(fmus[i].component, time, stepSize, fmi2False);
                if (currentStatus > fmi2Discard) {
                    printf("Could not step FMU (%s) while determining the step size. Terminating simulation.\n", NAMES_OF_FMUS[i]);
                    terminateSimulation(fmus, 0, file, h, nSteps);
                    return 0;
                }
                fmi2Real lastSuccessfulTime;
                currentStatus = fmus[i].getRealStatus(fmus[i].component, fmi2LastSuccessfulTime, &lastSuccessfulTime);
                if (currentStatus > fmi2Warning) {
                    printf("Could get the last successful time instant for FMU (%s). Terminating simulation.\n", NAMES_OF_FMUS[i]);
                    terminateSimulation(fmus, 0, file, h, nSteps);
                    return 0;
                }
                maxStepSize = lastSuccessfulTime - time;
                stepSize = min(stepSize, maxStepSize);
            }
        }

        // Compute the maximum step size
        // (III) Legacy FMUs
        if (isLegacyFmu) {
            fmi2Real maxStepSize;
            fmi2Status currentStatus = fmus[legacyFmuIndex].doStep(fmus[legacyFmuIndex].component, time, stepSize, fmi2False);
            if (currentStatus > fmi2Discard) {
                printf("Could not step FMU (%s) [Legacy FMU]. Terminating simulation.\n", NAMES_OF_FMUS[legacyFmuIndex]);
                terminateSimulation(fmus, 0, file, h, nSteps);
                return 0;
            }
            fmi2Real lastSuccessfulTime;
            currentStatus = fmus[legacyFmuIndex].getRealStatus(fmus[legacyFmuIndex].component, fmi2LastSuccessfulTime, &lastSuccessfulTime);
            if (currentStatus > fmi2Warning) {
                printf("Could get the last successful time instant for FMU (%s). Terminating simulation.\n", NAMES_OF_FMUS[legacyFmuIndex]);
                terminateSimulation(fmus, 0, file, h, nSteps);
                return 0;
            }
            maxStepSize = lastSuccessfulTime - time;
            stepSize = min(stepSize, maxStepSize);
        }

        // Rolling back FMUs of type (II)
        {
            fmi2Status currentStatus = rollbackFMUs(fmus);
            if (currentStatus > fmi2Warning) {
                printf("Error while rolling back FMUs. Terminating simulation.");
                terminateSimulation(fmus, 0, file, h, nSteps);
                return 0;
            }
        }

        // Perform doStep() for all FMUs with the discovered stepSize
        for (int i = 0; i < NUMBER_OF_FMUS; i++) {
            if (fmus[i].canGetMaxStepSize || fmus[i].canGetAndSetFMUstate) {
                fmi2Status currentStatus = fmus[i].doStep(fmus[i].component, time, stepSize, fmi2False);
                if (currentStatus > fmi2Discard) {
                    printf("Could not step FMU (%s) after minimum step has been determined. Terminating simulation.\n", NAMES_OF_FMUS[i]);
                    terminateSimulation(fmus, 0, file, h, nSteps);
                    return 0;
                }
            }
        }

        time += stepSize;
        outputRow(fmus, NUMBER_OF_FMUS, NAMES_OF_FMUS, time, file, separator,FALSE);
        nSteps++;
    }

    terminateSimulation(fmus, 1, file, h, nSteps);
    return 1;
}

void setupConnections(FMU* fmus, portConnection* connections) {
    connections[0].sourceFMU = &fmus[0];
    connections[0].sourcePort = getValueReference(getScalarVariable(fmus[0].modelDescription, 0));
    connections[0].sourceType = fmi2_Integer;
    connections[0].sinkFMU = &fmus[1];
    connections[0].sinkPort = getValueReference(getScalarVariable(fmus[1].modelDescription, 0));
    connections[0].sinkType = fmi2_Real;

    connections[1].sourceFMU = &fmus[1];
    connections[1].sourcePort = getValueReference(getScalarVariable(fmus[1].modelDescription, 1));
    connections[1].sourceType = fmi2_Real;
    connections[1].sinkFMU = &fmus[2];
    connections[1].sinkPort = getValueReference(getScalarVariable(fmus[2].modelDescription, 0));
    connections[1].sinkType = fmi2_Real;
}


int main(int argc, char *argv[]) {
    #if WINDOWS
    const char* fmuFileNames[NUMBER_OF_FMUS];
    #else
    char* fmuFileNames[NUMBER_OF_FMUS];
    #endif
    int i;

    double h = 0.1;
    int loggingOn = 0;
    char csv_separator = ',';
    char **categories = NULL;
    int nCategories = 0;
    fmi2Boolean visible = fmi2False;

    // Create and allocate arrays for FMUs and port mapping
    FMU *fmus = calloc(NUMBER_OF_FMUS, sizeof(FMU));
    portConnection* connections = calloc(NUMBER_OF_EDGES, sizeof(portConnection));
    printf("-> Parsing arguments...\n");
    parseArgumentsLegacy(argc, argv, fmuFileNames, &tEnd, &h, &loggingOn, &csv_separator, &nCategories, &categories);
    // Load and initialize FMUs
    for (i = 0; i < NUMBER_OF_FMUS; i++) {
        printf("Loading FMU %d\n", i+1);
        loadFMU(&fmus[i], fmuFileNames[i]);
        fmus[i].component = initializeFMU(&fmus[i], visible, loggingOn, nCategories, categories);
    }
    
    // Set up port connections
    setupConnections(fmus, connections);

    // run the simulation
    printf("FMU Simulator: run '%s' from t=0..%g with step size h=%g, loggingOn=%d, csv separator='%c' ", MODEL_NAME, tEnd, h, loggingOn, csv_separator);
    printf("log categories={ ");
        for (i = 0; i < nCategories; i++) {
            printf("%s ", categories[i]);
        }
    printf("}\n");
    simulate( fmus, connections, h, loggingOn, csv_separator);
    printf("CSV file '%s' written\n", RESULT_FILE);

    // release FMUs
    #ifdef _MSC_VER
    for (i = 0; i < NUMBER_OF_FMUS; i++) {
        FreeLibrary(fmus[i]->dllHandle);
    }
    #else
    for (i = 0; i < NUMBER_OF_FMUS; i++) {
        dlclose(fmus[i].dllHandle);
    }
    #endif
    for (i = 0; i < NUMBER_OF_FMUS; i++) {
        freeModelDescription(fmus[i].modelDescription);
    }
    if (categories) {
        free(categories);
    }
    free( fmus);

    return 0;
}

