/***variableDeclareBlock***/
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <stdbool.h>
#include "fmi.h"
#include "sim_support.h"

#define NUMBER_OF_FMUS 3
#define NUMBER_OF_EDGES 2

double tEnd = 1.0;
double tStart = 0;                       // start time


static fmiComponent initializeFMU(FMU *fmu, fmiBoolean visible, fmiBoolean loggingOn, int nCategories, char ** categories)
{

    fmiStatus fmiFlag;                       // return code of the fmu functions

    // instantiate the fmu
    fmu->callbacks.logger = fmuLogger;
    fmu->callbacks.allocateMemory = calloc;
    fmu->callbacks.freeMemory = free;
    fmu->callbacks.stepFinished = NULL; // fmiDoStep has to be carried out synchronously
    fmu->callbacks.componentEnvironment = fmu; // pointer to current fmu from the environment.

    fmiReal tolerance = 0;                   // used in setting up the experiment
    fmiBoolean toleranceDefined = fmiFalse;  // true if model description define tolerance
    ValueStatus vs = valueIllegal;

    // handle to the parsed XML file
    ModelDescription* md = fmu->modelDescription;
    // global unique id of the fmu
    const char *guid = getAttributeValue((Element *)md, att_guid);
    // check for ability to get and set state

    fmu->canGetAndSetFMUstate = getAttributeBool((Element*)getCoSimulation(md), att_canGetAndSetFMUstate, &vs);
    fmu->canGetMaxStepSize = getAttributeBool((Element*)getCoSimulation(md), att_canGetMaxStepSize, &vs);
    // instance name
    const char *instanceName = getAttributeValue((Element *)getCoSimulation(md), att_modelIdentifier);
    // path to the fmu resources as URL, "file://C:\QTronic\sales"
    char *fmuResourceLocation = getTempResourcesLocation();   // TODO: returns crap. got to save the location for every FMU somehow.
    // instance of the fmu
    fmiComponent comp = fmu->instantiate(instanceName, fmiCoSimulation, guid, fmuResourceLocation,
            &fmu->callbacks, visible, loggingOn);
    printf("instance name: %s, \nguid: %s, \nressourceLocation: %s\n", instanceName, guid, fmuResourceLocation);
    free(fmuResourceLocation);

    if (!comp)
        {
            printf("Could not initialize model with guid: %s\n", guid);
            return NULL;
        }

    Element *defaultExp = getDefaultExperiment(fmu->modelDescription);
    if (defaultExp) {
            tolerance = getAttributeDouble(defaultExp, att_tolerance, &vs);
    }
    if (vs == valueDefined) {
        toleranceDefined = fmiTrue;
    }


    if (nCategories > 0) {
        fmiFlag = fmu->setDebugLogging(comp, fmiTrue, nCategories, (const fmiString*) categories);
        if (fmiFlag > fmiWarning) {
            error("could not initialize model; failed FMI set debug logging");
            return NULL;
        }
    }

    fmiFlag = fmu->setupExperiment(comp, toleranceDefined, tolerance, tStart, fmiTrue, tEnd);
    if (fmiFlag > fmiWarning) {
        error("could not initialize model; failed FMI setup experiment");
        return NULL;
    }
    fmiFlag = fmu->enterInitializationMode(comp);
    if (fmiFlag > fmiWarning) {
        error("could not initialize model; failed FMI enter initialization mode");
        return NULL;
    }
    printf("initialization mode entered\n");
    fmiFlag = fmu->exitInitializationMode(comp);
    printf("successfully initialized.\n");

    if (fmiFlag > fmiWarning) {
        error("could not initialize model; failed FMI exit initialization mode");
        return NULL;
    }


    return comp;
}

static fmiStatus checkForLegacyFMUs(FMU* fmus, int numberOfFMUs, fmiBoolean *legacyFMU) {
    int i;

    printf("Rolling back FMUs!\n");

    for (i = 0; i < numberOfFMUs - 1; i++) {
        if (!fmus[i].canGetAndSetFMUstate) {
            printf("Legacy FMU has been detected and it's not at the end of the topological sort.\n");
            return fmiError;
        }
    }

    // Check for last FMU in topological sort and set boolean flag to fmiTrue, if legacy FMU is present
    if(!fmus[numberOfFMUs-1].canGetAndSetFMUstate) {
        *legacyFMU = fmiTrue;
        printf("Legacy FMU detected.\n");
    } else { *legacyFMU = fmiFalse; }

    return fmiOK;
}

static fmiStatus rollbackFMUs(FMU *fmus, int numberOfFMUs) {
        int i;
        fmiStatus fmiFlag;

        printf("Rolling back FMUs!\n");

        for (i = 0; i < numberOfFMUs; i++) {
            fmiFlag = fmus[i].setFMUstate(fmus[i].component, fmus[i].lastFMUstate);
            if (fmiFlag > fmiWarning) {
                    printf("Rolling back FMU %d failed!\n", i+1);
                    return fmiFlag;
            }
        }

        return fmiOK;
}

static fmiStatus setValue(portConnection* connection)
{
    fmiStatus fmiFlag;
    fmiInteger tempInt;
    fmiReal tempReal;
    fmiBoolean tempBoolean;
    fmiString tempString;

    // get source value and cast if neccessary
    switch (connection->sourceType) {
        case fmi_Integer :
            fmiFlag = connection->sourceFMU->getInteger(connection->sourceFMU->component, &connection->sourcePort, 1, &tempInt);
            tempReal = (fmiReal)tempInt;
            tempBoolean = (tempInt == 0 ? fmiFalse : fmiTrue);
            break;
        case fmi_Real :
            fmiFlag = connection->sourceFMU->getReal(connection->sourceFMU->component, &connection->sourcePort, 1, &tempReal);
            tempInt = (fmiInteger)round(tempReal);
            tempBoolean = (tempReal == 0.0 ? fmiFalse : fmiTrue);
            break;
        case fmi_Boolean :
            fmiFlag = connection->sourceFMU->getBoolean(connection->sourceFMU->component, &connection->sourcePort, 1, &tempBoolean);
            tempInt = (fmiInteger)tempBoolean;
            tempReal = (fmiReal)tempBoolean;
            break;
        case fmi_String :
            fmiFlag = connection->sourceFMU->getString(connection->sourceFMU->component, &connection->sourcePort, 1, &tempString);
            break;
        default :
            return fmiError;
    }

    if (fmiFlag > fmiWarning) {
        printf("Getting the value of an FMU caused an error.");
        return fmiFlag;
    }

    if (connection->sourceType != connection->sinkType && (connection->sinkType == fmi_String || connection ->sourceType == fmi_String)) {
        printf("A connection of FMUs has incompatible data types. Terminating simulation.\n");
        return fmiError;
    }

    // set sink value
    switch (connection->sinkType) {
        case fmi_Integer :
            fmiFlag = connection->sinkFMU->setInteger(connection->sinkFMU->component, &connection->sinkPort, 1, &tempInt);
            break;
        case fmi_Real :
            fmiFlag = connection->sinkFMU->setReal(connection->sinkFMU->component, &connection->sinkPort, 1, &tempReal);
            break;
        case fmi_Boolean :
            fmiFlag = connection->sinkFMU->setBoolean(connection->sinkFMU->component, &connection->sinkPort, 1, &tempBoolean);
            break;
        case fmi_String :
            fmiFlag = connection->sinkFMU->setString(connection->sinkFMU->component, &connection->sinkPort, 1, &tempString);
            break;
        default :
            return fmiError;
    }
    return fmiFlag;
}


// simulate the given FMUs from tStart = 0 to tEnd.
static int simulate(FMU *fmus, portConnection* connections, double h, fmiBoolean loggingOn, char separator) {

    // set up experiment
    double time = tStart;
    double stepSize = h;

    // temporary variables
    fmiBoolean doStepOnLegacy = fmiTrue;
    fmiBoolean legacyFMU = fmiFalse;
    fmiStatus fmiFlag = fmiOK;   // return code of the fmu functions
    fmiStatus simulationFlag = fmiOK; // status of the whole simulation
    int i = 0;
    int nSteps = 0;
    int returnValue = 1; // 1 = success
    FILE* file;

    // open result file
    if (!(file = fopen(RESULT_FILE, "w"))) {
        printf("could not write %s because:\n", RESULT_FILE);
        printf("    %s\n", strerror(errno));
        return 0; // failure
    }

    // Check for legacy FMUs
    if (checkForLegacyFMUs(fmus, NUMBER_OF_FMUS, &legacyFMU) > fmiWarning) {
        goto endSimulation;
    }

    // Set input values
    for (i = 0 ; i < NUMBER_OF_EDGES; i++) {
        setValue(&connections[i]);
    }

    // TODO: Should be done by an FMU
    // output solution for time t0
    outputRow(&fmus[NUMBER_OF_FMUS-2], fmus[NUMBER_OF_FMUS-2].component, time, file, separator, TRUE);  // output column names
    outputRow(&fmus[NUMBER_OF_FMUS-2], fmus[NUMBER_OF_FMUS-2].component, time, file, separator, FALSE); // output values

    // enter the simulation loop

    // TODO: Find consistent error checking!!
    while (time < tEnd) {
        // Run trough the topologically sorted list of FMUs (Master-Step)

        // Set input values
        for (i = 0 ; i < NUMBER_OF_EDGES; i++) {
            setValue(&connections[i]);
        }

/* TODO: getMaxStepSize() support. Needs change of FMI header files.
 *
        // Save current state of all FMUs. Skip legacy FMU if present.
        for (i = 0 ; i < NUMBER_OF_FMUS - legacyFMU ; i++) {
            if (fmus[i]->canGetMaxStepSize) {
                fmiFlag = fmus[i].getMaxStepSize(fmus[i].component, &tempMaxStepSize);

                if (fmiFlag > fmiWarning) {
                    printf("Saving state of FMU at index %d failed. Terminating simulation.", i);
                    goto endSimulation;
                }
                // Update step size if new step size is smaller
                stepSize = min(stepSize, tempMaxStepSize);
            }
        }
*/
        // Save current state of all FMUs. Skip legacy FMU if present.
        for (i = 0 ; i < NUMBER_OF_FMUS - legacyFMU ; i++) {
            fmiFlag = fmus[i].getFMUstate(fmus[i].component, &fmus[i].lastFMUstate);

            if (fmiFlag > fmiWarning) {
                printf("Saving state of FMU at index %d failed. Terminating simulation.", i);
                goto endSimulation;
            }
        }

        // reset simulationFlag
        simulationFlag = fmiOK;

        // Try doStep() for all FMUs and find acceptable stepSize (skip legacy FMU if present and doStepOnLegacy != fmiTrue)
        for (i = 0 ; i < NUMBER_OF_FMUS-(legacyFMU && !doStepOnLegacy); i++) {
            // Try doStep() and check if FMU accepts the step size
            fmiFlag = fmus[i].doStep(fmus[i].component, time, stepSize, fmiFalse);

            // Error checking
            if (fmiFlag > fmiDiscard) {
                    printf("Could not complete simulation of the model. doStep returned fmuStatus > Discard!\n");
                    returnValue = 0;
                    // Need to free up memory etc.
                    goto endSimulation;
            }

            // If stepSize of the current attempt is rejected set it to the minimum of all processed FMUs
            if (fmiFlag == fmiDiscard) {
                fmiReal lastSuccessfulTime;
                fmus[i].getRealStatus(fmus[i].component, fmiLastSuccessfulTime, &lastSuccessfulTime);
                stepSize = min(stepSize, (lastSuccessfulTime - time));  // setting step size to successful step size of current fmu if smaller
                simulationFlag = fmiDiscard;
            }
        }

        // Rolling back FMUs if step size was rejected. Skip legacy FMU if present.
        if (simulationFlag == fmiDiscard) {
            fmiFlag = rollbackFMUs(fmus, NUMBER_OF_FMUS - legacyFMU );
            if (fmiFlag > fmiWarning) {
                printf("Rolling back of FMUs failed. Terminating simulation.");
                goto endSimulation;
            }
        }

        if (simulationFlag != fmiDiscard) {
            time += stepSize;
        }

        // TODO: Should be done by FMU
        outputRow(&fmus[NUMBER_OF_FMUS-2], fmus[NUMBER_OF_FMUS-2].component, time, file, separator, FALSE); // output values for this step

        nSteps++;
    }

 endSimulation:
    // end simulation
    for (i = 0 ; i < NUMBER_OF_FMUS; i++)
        {
            fmus[i].terminate(fmus[i].component);
            if (fmus[i].lastFMUstate != NULL) {
                //FIXME: we are ignoring the return value of this call.
                fmus[i].freeFMUstate(fmus[i].component, &fmus[i].lastFMUstate);
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

    return returnValue; // 1=success, 0=not success
}

void setupConnections(FMU* fmus, portConnection* connections) {
    connections[0].sourceFMU = &fmus[0];
    connections[0].sourcePort = getValueReference(getScalarVariable(fmus[0].modelDescription, 0));
    connections[0].sourceType = fmi_Integer;
    connections[0].sinkFMU = &fmus[1];
    connections[0].sinkPort = getValueReference(getScalarVariable(fmus[1].modelDescription, 0));
    connections[0].sinkType = fmi_Real;

    connections[1].sourceFMU = &fmus[1];
    connections[1].sourcePort = getValueReference(getScalarVariable(fmus[1].modelDescription, 1));
    connections[1].sourceType = fmi_Real;
    connections[1].sinkFMU = &fmus[2];
    connections[1].sinkPort = getValueReference(getScalarVariable(fmus[2].modelDescription, 0));
    connections[1].sinkType = fmi_Real;
}

/**/
