/* -------------------------------------------------------------------------
 * main.c
 * Implements simulation of a single FMU instance using the forward Euler
 * method for numerical integration.
 * Command syntax: see printHelp()
 * Simulates the given FMU from t = 0 .. tEnd with fixed step size h and
 * writes the computed solution to file 'result.csv'.
 * The CSV file (comma-separated values) may e.g. be plotted using
 * OpenOffice Calc or Microsoft Excel.
 * This progamm demonstrates basic use of an FMU.
 * Real applications may use advanced numerical solvers instead, means to
 * exactly locate state events in time, graphical plotting utilities, support
 * for coexecution of many FMUs, stepping and debug support, user control
 * of parameter and start values etc.
 * All this is missing here.
 *
 * Revision history
 *  07.02.2010 initial version released in FMU SDK 1.0
 *  05.03.2010 bug fix: removed strerror(GetLastError()) from error messages
 *  11.06.2010 bug fix: replaced win32 API call to OpenFile in getFmuPath
 *    which restricted path length to FMU to 128 chars. New limit is MAX_PATH.
 *  15.07.2010 fixed wrong label in xml parser: deault instead of default
 *  13.12.2010 added check for undefined 'declared type' to xml parser
 *  31.07.2011 bug fix: added missing freeModelInstance(c)
 *  31.07.2011 bug fix: added missing terminate(c)
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
#include "fmi_me.h"
#include "sim_support.h"

#ifdef __APPLE__
#include <string.h> //strerror()
#endif

FMU fmu; // the fmu to simulate

// simulate the given FMU using the forward euler method.
// time events are processed by reducing step size to exactly hit tNext.
// state events are checked and fired only at the end of an Euler step.
// the simulator may therefore miss state events and fires state events typically too late.
static int simulate(FMU* fmu, double tEnd, double h, fmiBoolean loggingOn, char separator) {
    int i;
    double dt, tPre;
    fmiBoolean timeEvent, stateEvent, stepEvent;
    double time;
    int nx;                          // number of state variables
    int nz;                          // number of state event indicators
    double *x;                       // continuous states
    double *xdot;                    // the crresponding derivatives in same order
    double *z = NULL;                // state event indicators
    double *prez = NULL;             // previous values of state event indicators
    fmiEventInfo eventInfo;          // updated by calls to initialize and eventUpdate
    ModelDescription* md;            // handle to the parsed XML file
    const char* guid;                // global unique id of the fmu
    fmiCallbackFunctions callbacks;  // called by the model during simulation
    fmiComponent c;                  // instance of the fmu
    fmiStatus fmiFlag;               // return code of the fmu functions
    fmiReal t0 = 0;                  // start time
    fmiBoolean toleranceControlled = fmiFalse;
    int nSteps = 0;
    int nTimeEvents = 0;
    int nStepEvents = 0;
    int nStateEvents = 0;
    FILE* file;

    // instantiate the fmu
    md = fmu->modelDescription;
    guid = getString(md, att_guid);
    callbacks.logger = fmuLogger;
    callbacks.allocateMemory = calloc;
    callbacks.freeMemory = free;
    c = fmu->instantiateModel(getModelIdentifier(md), guid, callbacks, loggingOn);
    if (!c) return error("could not instantiate model");

    // allocate memory
    nx = getNumberOfStates(md);
    nz = getNumberOfEventIndicators(md);
    x    = (double *) calloc(nx, sizeof(double));
    xdot = (double *) calloc(nx, sizeof(double));
    if (nz>0) {
        z    =  (double *) calloc(nz, sizeof(double));
        prez =  (double *) calloc(nz, sizeof(double));
    }
    if (!x || !xdot || (nz>0 && (!z || !prez))) return error("out of memory");

    // open result file
    if (!(file=fopen(RESULT_FILE, "w"))) {
        printf("could not write %s because:\n", RESULT_FILE);
        printf("    %s\n", strerror(errno));
        return 0; // failure
    }

    // set the start time and initialize
    time = t0;
    fmiFlag =  fmu->setTime(c, t0);
    if (fmiFlag > fmiWarning) return error("could not set time");
    fmiFlag =  fmu->initialize(c, toleranceControlled, t0, &eventInfo);
    if (fmiFlag > fmiWarning)  return error("could not initialize model");
    if (eventInfo.terminateSimulation) {
        printf("model requested termination at init");
        tEnd = time;
    }

    // output solution for time t0
    outputRow(fmu, c, t0, file, separator, TRUE);  // output column names
    outputRow(fmu, c, t0, file, separator, FALSE); // output values

    // enter the simulation loop
    while (time < tEnd) {
     // get current state and derivatives
     fmiFlag = fmu->getContinuousStates(c, x, nx);
     if (fmiFlag > fmiWarning) return error("could not retrieve states");
     fmiFlag = fmu->getDerivatives(c, xdot, nx);
     if (fmiFlag > fmiWarning) return error("could not retrieve derivatives");

     // advance time
     tPre = time;
     time = min(time+h, tEnd);
     timeEvent = eventInfo.upcomingTimeEvent && eventInfo.nextEventTime < time;
     if (timeEvent) time = eventInfo.nextEventTime;
     dt = time - tPre;
     fmiFlag = fmu->setTime(c, time);
     if (fmiFlag > fmiWarning) error("could not set time");

     // perform one step
     for (i=0; i<nx; i++) x[i] += dt*xdot[i]; // forward Euler method
     fmiFlag = fmu->setContinuousStates(c, x, nx);
     if (fmiFlag > fmiWarning) return error("could not set states");
     if (loggingOn) printf("Step %d to t=%.16g\n", nSteps, time);

     // Check for step event, e.g. dynamic state selection
     fmiFlag = fmu->completedIntegratorStep(c, &stepEvent);
     if (fmiFlag > fmiWarning) return error("could not complete intgrator step");

     // Check for state event
     for (i=0; i<nz; i++) prez[i] = z[i];
     fmiFlag = fmu->getEventIndicators(c, z, nz);
     if (fmiFlag > fmiWarning) return error("could not retrieve event indicators");
     stateEvent = FALSE;
     for (i=0; i<nz; i++)
         stateEvent = stateEvent || (prez[i] * z[i] < 0);

     // handle events
     if (timeEvent || stateEvent || stepEvent) {

        if (timeEvent) {
            nTimeEvents++;
            if (loggingOn) printf("time event at t=%.16g\n", time);
        }
        if (stateEvent) {
            nStateEvents++;
            if (loggingOn) for (i=0; i<nz; i++)
                printf("state event %s z[%d] at t=%.16g\n",
                        (prez[i]>0 && z[i]<0) ? "-\\-" : "-/-", i, time);
        }
        if (stepEvent) {
            nStepEvents++;
            if (loggingOn) printf("step event at t=%.16g\n", time);
        }

        // event iteration in one step, ignoring intermediate results
        fmiFlag = fmu->eventUpdate(c, fmiFalse, &eventInfo);
        if (fmiFlag > fmiWarning) return error("could not perform event update");

        // terminate simulation, if requested by the model
        if (eventInfo.terminateSimulation) {
            printf("model requested termination at t=%.16g\n", time);
            break; // success
        }

        // check for change of value of states
        if (eventInfo.stateValuesChanged && loggingOn) {
            printf("state values changed at t=%.16g\n", time);
        }

        // check for selection of new state variables
        if (eventInfo.stateValueReferencesChanged && loggingOn) {
            printf("new state variables selected at t=%.16g\n", time);
        }

     } // if event
     outputRow(fmu, c, time, file, separator, FALSE); // output values for this step
     nSteps++;
  } // while

  // cleanup
  if(! eventInfo.terminateSimulation) fmu->terminate(c);

  // When importing a BouncingBall from OpenModelica 1.8.1, calling
  // freeModelInstance will result in a segfault because free()
  // is called twice.
  //fmu->freeModelInstance(c);

  fclose(file);
  if (x!=NULL) free(x);
  if (xdot!= NULL) free(xdot);
  if (z!= NULL) free(z);
  if (prez!= NULL) free(prez);

  // print simulation summary
  printf("Simulation from %g to %g terminated successful\n", t0, tEnd);
  printf("  steps ............ %d\n", nSteps);
  printf("  fixed step size .. %g\n", h);
  printf("  time events ...... %d\n", nTimeEvents);
  printf("  state events ..... %d\n", nStateEvents);
  printf("  step events ...... %d\n", nStepEvents);

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
