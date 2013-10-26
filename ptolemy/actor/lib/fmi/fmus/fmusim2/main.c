/* -------------------------------------------------------------------------
 * main.c
 * Implements simulation of a single FMU instance
 * that implements the "FMI for Co-Simulation 1.0" interface.
 * Command syntax: see printHelp()
 * Simulates the given FMU from t = 0 .. tEnd with fixed step size h and
 * writes the computed solution to file 'result.csv'.
 * The CSV file (comma-separated values) may e.g. be plotted using
 * OpenOffice Calc or Microsoft Excel.
 * This progamm demonstrates basic use of an FMU.
 * Real applications may use advanced master algorithms to cosimulate
 * many FMUs, limit the numerical error using error estimation
 * and back-stepping, provide graphical plotting utilities, debug support,
 * and user control of parameter and start values, or perform a clean
 * error handling (e.g. call freeSlaveInstance when a call to the fmu
 * returns with error). All this is missing here.
 *
 * Revision history
 *  22.08.2011 initial version released in FMU SDK 1.0.2
 *
 * Free libraries and tools used to implement this simulator:
 *  - header files from the FMU specification
 *  - eXpat 2.0.1 XML parser, see http://expat.sourceforge.net
 *  - 7z.exe 4.57 zip and unzip tool, see http://www.7-zip.org
 * Author: Jakob Mauss
 * Copyright 2011 QTronic GmbH. All rights reserved.
 * -------------------------------------------------------------------------
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "fmi_cs.h"
#include "sim_support.h"

FMU fmu; // the fmu to simulate

// simulate the given FMU using the forward euler method.
// time events are processed by reducing step size to exactly hit tNext.
// state events are checked and fired only at the end of an Euler step.
// the simulator may therefore miss state events and fires state events typically too late.
static int simulate(FMU* fmu, double tEnd, double h, fmiBoolean loggingOn, char separator) {
    double time;
    double tStart = 0;               // start time
    const char* guid;                // global unique id of the fmu
    fmiComponent c;                  // instance of the fmu
    fmiStatus fmiFlag;               // return code of the fmu functions
    const char* fmuLocation = NULL;  // path to the fmu as URL, "file://C:\QTronic\sales"
#if FMI_VERSION < 2
    const char* mimeType = "application/x-fmu-sharedlibrary"; // denotes tool in case of tool coupling
    fmiReal timeout = 1000;          // wait period in milli seconds, 0 for unlimited wait period"
    fmiBoolean interactive = fmiFalse; // simulation run without user interaction
#endif
    fmiBoolean visible = fmiFalse;   // no simulator user interface

    fmiCallbackFunctions callbacks;  // called by the model during simulation
    ModelDescription* md;            // handle to the parsed XML file
    int nSteps = 0;
    FILE* file;

    // instantiate the fmu
    md = fmu->modelDescription;
    guid = getString(md, att_guid);
    callbacks.logger = fmuLogger;
    callbacks.allocateMemory = calloc;
    callbacks.freeMemory = free;
    callbacks.stepFinished = NULL; // fmiDoStep has to be carried out synchronously
#if FMI_VERSION >= 2
    c = fmu->instantiateSlave(getModelIdentifier(md), guid, fmuLocation, &callbacks,
                             visible, loggingOn);
#else
    c = fmu->instantiateSlave(getModelIdentifier(md), guid, fmuLocation, mimeType,
                              timeout, visible, interactive, callbacks, loggingOn);
#endif
    if (!c) return error("could not instantiate model");

    // open result file
    if (!(file=fopen(RESULT_FILE, "w"))) {
        printf("could not write %s because:\n", RESULT_FILE);
        printf("    %s\n", strerror(errno));
        return 0; // failure
    }

    // StopTimeDefined=fmiFalse means: ignore value of tEnd
#if FMI_VERSION >= 2
    // FIXME: ignoring relativeTolerance
    fmiFlag = fmu->initializeSlave(c, 0, tStart, fmiTrue, tEnd);
#else
    fmiFlag = fmu->initializeSlave(c, tStart, fmiTrue, tEnd);
#endif
    if (fmiFlag > fmiWarning)  return error("could not initialize model");

    // output solution for time t0
    outputRow(fmu, c, tStart, file, separator, TRUE);  // output column names
    outputRow(fmu, c, tStart, file, separator, FALSE); // output values

    // enter the simulation loop
    time = tStart;
    while (time < tEnd) {
        fmiFlag = fmu->doStep(c, time, h, fmiTrue);
        if (fmiFlag != fmiOK)  return error("could not complete simulation of the model");
        time += h;
        outputRow(fmu, c, time, file, separator, FALSE); // output values for this step
        nSteps++;
    }

    // end simulation
    fmiFlag = fmu->terminateSlave(c);
    fmu->freeSlaveInstance(c);
    fclose(file);

    // print simulation summary
    printf("Simulation from %g to %g terminated successful\n", tStart, tEnd);
    printf("  steps ............ %d\n", nSteps);
    printf("  fixed step size .. %g\n", h);
    return 1; // success
}

int main(int argc, char *argv[]) {
    char* fmuFileName;

    // parse command line arguments and load the FMU
    double tEnd = 1.0;
    double h=0.1;
    int loggingOn = 0;
    char csv_separator = ';';
    parseArguments(argc, argv, &fmuFileName, &tEnd, &h, &loggingOn, &csv_separator);
    loadFMU(fmuFileName);

    // run the simulation
    printf("FMU Simulator: run '%s' from t=0..%g with step size h=%g, loggingOn=%d, csv separator='%c'\n",
            fmuFileName, tEnd, h, loggingOn, csv_separator);
    simulate(&fmu, tEnd, h, loggingOn, csv_separator);
    printf("CSV file '%s' written\n", RESULT_FILE);

    // release FMU
#ifdef _MSC_VER
    FreeLibrary(fmu.dllHandle);
#else
    dlclose(fmu.dllHandle);
#endif
    freeElement(fmu.modelDescription);
    return EXIT_SUCCESS;
}

