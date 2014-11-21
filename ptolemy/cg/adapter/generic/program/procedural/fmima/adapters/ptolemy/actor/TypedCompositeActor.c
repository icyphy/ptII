/***variableDeclareBlock***/
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <stdbool.h>
#include "fmi2.h"
#include "sim_support.h"

double tEnd = 1.0;
double tStart = 0;

/**/

/***staticDeclareBlock***/

static fmi2Component initializeFMU(FMU *fmu, fmi2Boolean visible, fmi2Boolean loggingOn, int nCategories, char ** categories)
{

    fmi2Status fmi2Flag;                       // return code of the fmu functions

    // instantiate the fmu
    fmu->callbacks.logger = fmuLogger;
    fmu->callbacks.allocateMemory = calloc;
    fmu->callbacks.freeMemory = free;
    fmu->callbacks.stepFinished = NULL; // fmi2DoStep has to be carried out synchronously
    fmu->callbacks.componentEnvironment = fmu; // pointer to current fmu from the environment.

    fmi2Real tolerance = 0;                   // used in setting up the experiment
    fmi2Boolean toleranceDefined = fmi2False;  // true if model description define tolerance
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
    fmi2Component comp = fmu->instantiate(instanceName, fmi2CoSimulation, guid, fmuResourceLocation,
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
        toleranceDefined = fmi2True;
    }


    if (nCategories > 0) {
        fmi2Flag = fmu->setDebugLogging(comp, fmi2True, nCategories, (const fmi2String*) categories);
        if (fmi2Flag > fmi2Warning) {
            error("could not initialize model; failed FMI set debug logging");
            return NULL;
        }
    }

    fmi2Flag = fmu->setupExperiment(comp, toleranceDefined, tolerance, tStart, fmi2True, tEnd);
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

static fmi2Status checkForLegacyFMUs(FMU* fmus, int numberOfFMUs, fmi2Boolean *legacyFMU) {
    int i;

    printf("Rolling back FMUs!\n");

    for (i = 0; i < numberOfFMUs - 1; i++) {
        if (!fmus[i].canGetAndSetFMUstate) {
            printf("Legacy FMU has been detected and it's not at the end of the topological sort.\n");
            return fmi2Error;
        }
    }

    // Check for last FMU in topological sort and set boolean flag to fmi2True, if legacy FMU is present
    if(!fmus[numberOfFMUs-1].canGetAndSetFMUstate) {
        *legacyFMU = fmi2True;
        printf("Legacy FMU detected.\n");
    } else { *legacyFMU = fmi2False; }

    return fmi2OK;
}

static fmi2Status rollbackFMUs(FMU *fmus, int numberOfFMUs) {
        int i;
        fmi2Status fmi2Flag;

        printf("Rolling back FMUs!\n");

        for (i = 0; i < numberOfFMUs; i++) {
            fmi2Flag = fmus[i].setFMUstate(fmus[i].component, fmus[i].lastFMUstate);
            if (fmi2Flag > fmi2Warning) {
                    printf("Rolling back FMU %d failed!\n", i+1);
                    return fmi2Flag;
            }
        }

        return fmi2OK;
}

static fmi2Status setValue(portConnection* connection)
{
    fmi2Status fmi2Flag;
    fmi2Integer tempInt;
    fmi2Real tempReal;
    fmi2Boolean tempBoolean;
    fmi2String tempString;

    // get source value and cast if neccessary
    switch (connection->sourceType) {
        case fmi2_Integer :
            fmi2Flag = connection->sourceFMU->getInteger(connection->sourceFMU->component, &connection->sourcePort, 1, &tempInt);
            tempReal = (fmi2Real)tempInt;
            tempBoolean = (tempInt == 0 ? fmi2False : fmi2True);
            break;
        case fmi2_Real :
            fmi2Flag = connection->sourceFMU->getReal(connection->sourceFMU->component, &connection->sourcePort, 1, &tempReal);
            tempInt = (fmi2Integer)round(tempReal);
            tempBoolean = (tempReal == 0.0 ? fmi2False : fmi2True);
            break;
        case fmi2_Boolean :
            fmi2Flag = connection->sourceFMU->getBoolean(connection->sourceFMU->component, &connection->sourcePort, 1, &tempBoolean);
            tempInt = (fmi2Integer)tempBoolean;
            tempReal = (fmi2Real)tempBoolean;
            break;
        case fmi2_String :
            fmi2Flag = connection->sourceFMU->getString(connection->sourceFMU->component, &connection->sourcePort, 1, &tempString);
            break;
        default :
            return fmi2Error;
    }

    if (fmi2Flag > fmi2Warning) {
        printf("Getting the value of an FMU caused an error.");
        return fmi2Flag;
    }

    if (connection->sourceType != connection->sinkType && (connection->sinkType == fmi2_String || connection ->sourceType == fmi2_String)) {
        printf("A connection of FMUs has incompatible data types. Terminating simulation.\n");
        return fmi2Error;
    }

    // set sink value
    switch (connection->sinkType) {
        case fmi2_Integer :
            fmi2Flag = connection->sinkFMU->setInteger(connection->sinkFMU->component, &connection->sinkPort, 1, &tempInt);
            break;
        case fmi2_Real :
            fmi2Flag = connection->sinkFMU->setReal(connection->sinkFMU->component, &connection->sinkPort, 1, &tempReal);
            break;
        case fmi2_Boolean :
            fmi2Flag = connection->sinkFMU->setBoolean(connection->sinkFMU->component, &connection->sinkPort, 1, &tempBoolean);
            break;
        case fmi2_String :
            fmi2Flag = connection->sinkFMU->setString(connection->sinkFMU->component, &connection->sinkPort, 1, &tempString);
            break;
        default :
            return fmi2Error;
    }
    return fmi2Flag;
}


// simulate the given FMUs from tStart = 0 to tEnd.
static int simulate(FMU *fmus, portConnection* connections, double h, fmi2Boolean loggingOn, char separator) {

    // set up experiment
    double time = tStart;
    double stepSize = h;

    // temporary variables
    fmi2Boolean doStepOnLegacy = fmi2True;
    fmi2Boolean legacyFMU = fmi2False;
    fmi2Status fmi2Flag = fmi2OK;   // return code of the fmu functions
    fmi2Status simulationFlag = fmi2OK; // status of the whole simulation
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
    if (checkForLegacyFMUs(fmus, NUMBER_OF_FMUS, &legacyFMU) > fmi2Warning) {
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
                fmi2Flag = fmus[i].getMaxStepSize(fmus[i].component, &tempMaxStepSize);

                if (fmi2Flag > fmi2Warning) {
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
            fmi2Flag = fmus[i].getFMUstate(fmus[i].component, &fmus[i].lastFMUstate);

            if (fmi2Flag > fmi2Warning) {
                printf("Saving state of FMU at index %d failed. Terminating simulation.", i);
                goto endSimulation;
            }
        }

        // reset simulationFlag
        simulationFlag = fmi2OK;

        // Try doStep() for all FMUs and find acceptable stepSize (skip legacy FMU if present and doStepOnLegacy != fmi2True)
        for (i = 0 ; i < NUMBER_OF_FMUS-(legacyFMU && !doStepOnLegacy); i++) {
            // Try doStep() and check if FMU accepts the step size
            fmi2Flag = fmus[i].doStep(fmus[i].component, time, stepSize, fmi2False);

            // Error checking
            if (fmi2Flag > fmi2Discard) {
                    printf("Could not complete simulation of the model. doStep returned fmuStatus > Discard!\n");
                    returnValue = 0;
                    // Need to free up memory etc.
                    goto endSimulation;
            }

            // If stepSize of the current attempt is rejected set it to the minimum of all processed FMUs
            if (fmi2Flag == fmi2Discard) {
                fmi2Real lastSuccessfulTime;
                fmus[i].getRealStatus(fmus[i].component, fmi2LastSuccessfulTime, &lastSuccessfulTime);
                stepSize = min(stepSize, (lastSuccessfulTime - time));  // setting step size to successful step size of current fmu if smaller
                simulationFlag = fmi2Discard;
            }
        }

        // Rolling back FMUs if step size was rejected. Skip legacy FMU if present.
        if (simulationFlag == fmi2Discard) {
            fmi2Flag = rollbackFMUs(fmus, NUMBER_OF_FMUS - legacyFMU );
            if (fmi2Flag > fmi2Warning) {
                printf("Rolling back of FMUs failed. Terminating simulation.");
                goto endSimulation;
            }
        }

        if (simulationFlag != fmi2Discard) {
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

/**/
