/* -------------------------------------------------------------------------
 * main.c
 * Implements simulation of a single FMU instance
 * that implements the "FMI for Co-Simulation 2.0" interface.
 * Command syntax: see printHelp()
 * Simulates the given FMU from t = 0 .. tEnd with fixed step size h and
 * writes the computed solution to file 'result.csv'.
 * The CSV file (comma-separated values) may e.g. be plotted using
 * OpenOffice Calc or Microsoft Excel.
 * This program demonstrates basic use of an FMU.
 * Real applications may use advanced master algorithms to co-simulate
 * many FMUs, limit the numerical error using error estimation
 * and back-stepping, provide graphical plotting utilities, debug support,
 * and user control of parameter and start values, or perform a clean
 * error handling (e.g. call freeSlaveInstance when a call to the fmu
 * returns with error). All this is missing here.
 *
 * Revision history
 *  07.03.2014 initial version released in FMU SDK 2.0.0
 *
 * Free libraries and tools used to implement this simulator:
 *  - header files from the FMI specification
 *  - libxml2 XML parser, see http://xmlsoft.org
 *  - 7z.exe 4.57 zip and unzip tool, see http://www.7-zip.org
 * Author: Adrian Tirea
 * Copyright QTronic GmbH. All rights reserved.
 * -------------------------------------------------------------------------*/

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "fmi.h"
#include "sim_support.h"
#include "fmuTemplate.h"

#define NUMBER_OF_FMUS 2

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

/*static void freeFMU(FMU *fmu) {

  }*/


// simulate the given FMUs from tStart = 0 to tEnd.
static int simulate(FMU *fmus, double h, fmiBoolean loggingOn, char separator) {

    double time;
    fmiStatus fmiFlag;   // return code of the fmu functions

    int i;
    int nSteps = 0;
    FILE* file;

    // open result file
    if (!(file = fopen(RESULT_FILE, "w"))) {
        printf("could not write %s because:\n", RESULT_FILE);
        printf("    %s\n", strerror(errno));
        return 0; // failure
    }


    // output solution for time t0
    outputRow(&fmus[NUMBER_OF_FMUS-1], fmus[NUMBER_OF_FMUS-1].component, tStart, file, separator, TRUE);  // output column names
    outputRow(&fmus[NUMBER_OF_FMUS-1], fmus[NUMBER_OF_FMUS-1].component, tStart, file, separator, FALSE); // output values

    // enter the simulation loop
    time = tStart;
    fmiInteger tempInt;
    fmiReal tempReal;
    fmiValueReference inputTwo = getValueReference(getScalarVariable(fmus[1].modelDescription, 0));
    fmiValueReference outputOne = getValueReference(getScalarVariable(fmus[0].modelDescription, 0));


    while (time < tEnd) {
    	int i;
    	for (i = 0 ; i < NUMBER_OF_FMUS; i++)
            {


                //fmus[i].lastFMUstate =  calloc(1, sizeof(fmiFMUstate*));
    		//fmiFlag = fmus[i].getFMUstate(fmus[i].component, fmus[i].lastFMUstate);
    		//ModelInstance* inst = (ModelInstance*)(fmus[i].lastFMUstate);

                fmiFMUstate lastFMUstate = NULL;
    		fmiFlag = fmus[i].getFMUstate(fmus[i].component, &lastFMUstate);
    		ModelInstance* inst = (ModelInstance*) (lastFMUstate);
                fmus[i].lastFMUstate = lastFMUstate;

                //fmiFMUstate **lastFMUstate = calloc(1, sizeof(fmiFMUstate**));
                //*lastFMUstate = calloc(1, sizeof(fmiFMUstate*));
    		//fmiFlag = fmus[i].getFMUstate(fmus[i].component, *lastFMUstate);
    		//ModelInstance* inst = (ModelInstance*) (lastFMUstate);
                //fmus[i].lastFMUstate = *lastFMUstate;

                printf("fmus[%d].lastFMUstate = %p\n", i, fmus[i].lastFMUstate);
                if (fmiFlag <= fmiWarning) {
                    printf("The value of the last state is: %d\n", inst->i[i]);
                    fmiFlag = fmus[i].doStep(fmus[i].component, time, h, fmiFalse);
                    if (fmiFlag > fmiWarning)
                        {
                            return error("could not complete simulation of the model");
                        }
                }
            }

    	for (i = 0 ; i < NUMBER_OF_FMUS-1; i++)
            {
    		fmiFlag = fmus[i].getInteger(fmus[i].component, &outputOne, 1, &tempInt);
    		fmiFlag = fmus[i].setInteger(fmus[i].component, &outputOne, 1, &tempInt);
    		tempReal = (fmiReal)tempInt;
    		fmiFlag = fmus[i+1].setReal(fmus[i+1].component, &inputTwo, 1, &tempReal);
            }

    	if (time == 5) {
            printf("trying to set FMUstate\n");
            printf("fmus[0].lastFMUstate = %p\n", fmus[0].lastFMUstate);
            printf("*fmus[0].lastFMUstate = %p\n", *fmus[0].lastFMUstate);
            printf("*(fmus[0].lastFMUstate) = %p\n", *(fmus[0].lastFMUstate));
            printf("((ModelInstance*)(fmus[0].lastFMUstate))->i[0] = %d\n", ((ModelInstance*)fmus[0].lastFMUstate)->i[0]);
            fmiFlag = fmus[0].setFMUstate(fmus[0].component, fmus[0].lastFMUstate);
            printf("FMUstate set\n");
    	}

        time += h;
        outputRow(&fmus[NUMBER_OF_FMUS-1], fmus[NUMBER_OF_FMUS-1].component, time, file, separator, FALSE); // output values for this step

        nSteps++;
    }

    // end simulation
    for (i = 0 ; i < NUMBER_OF_FMUS; i++)
	{
	    fmus[i].terminate(fmus[i].component);
	    fmus[i].freeInstance(fmus[i].component);
	}

    // print simulation summary
    printf("Simulation from %g to %g terminated successful\n", tStart, tEnd);
    printf("  steps ............ %d\n", nSteps);
    printf("  fixed step size .. %g\n", h);

    fclose(file);

    return 1; // success
}

int main(int argc, char *argv[]) {
#if WINDOWS
    const char* fmuFileNames[NUMBER_OF_FMUS];
#else
    char* fmuFileNames[NUMBER_OF_FMUS];
#endif
    int i;
    
    // parse command line arguments and load the FMU
    // default arguments value
    double h=0.1;
    int loggingOn = 0;
    char csv_separator = ',';
    char **categories = NULL;
    int nCategories = 0;
    fmiBoolean visible = fmiFalse;           // no simulator user interface
    
    // Create FMU array and allocate memory
    FMU *fmus;
    fmus = calloc(NUMBER_OF_FMUS, sizeof(FMU));

    printf("Parsing arguments!\n");
    parseArguments(argc, argv, fmuFileNames, &tEnd, &h, &loggingOn, &csv_separator, &nCategories, &categories);

    // Load and initialize FMUs
    for (i = 0; i < NUMBER_OF_FMUS; i++) {
        printf("Loading FMU1\n");
        loadFMU(&fmus[i], fmuFileNames[i]);
        fmus[i].component = initializeFMU(&fmus[i], visible, loggingOn, nCategories, categories);
    }

    // run the simulation
    printf("FMU Simulator: run '%s' from t=0..%g with step size h=%g, loggingOn=%d, csv separator='%c' ",
            fmuFileNames[0], tEnd, h, loggingOn, csv_separator); // TODO: Should mention all FMUs
    printf("log categories={ ");
    for (i = 0; i < nCategories; i++) {
    	printf("%s ", categories[i]);
    }
    printf("}\n");

    simulate(fmus, h, loggingOn, csv_separator); // TODO: Create experiment settings struct

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

    return EXIT_SUCCESS;
}
