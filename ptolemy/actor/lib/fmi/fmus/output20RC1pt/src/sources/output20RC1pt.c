/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU - Writes all inputs to an output file (results.csv).
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER output20RC1pt
#define MODEL_GUID "{347de34e-28a1-4374-bcb7-293e55bced8b}"

// define model size
#define NUMBER_OF_REALS 1
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_STATES 1
#define NUMBER_OF_EVENT_INDICATORS 0

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"
#include <stdio.h>
#include <errno.h>
#include <stdbool.h>

// define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define value_ 0

// define state vector as vector of value references
#define STATES { value_ }

#define RESULT_FILE "result2.csv"

// local variables
char separator = ',';
FILE* file;


static void doubleToCommaString(char* buffer, double r){
    char* comma;
    sprintf(buffer, "%.16g", r);
    comma = strchr(buffer, '.');
    if (comma) *comma = ',';
}

// output time and all variables in CSV format
// if separator is ',', columns are separated by ',' and '.' is used for floating-point numbers.
// otherwise, the given separator (e.g. ';' or '\t') is to separate columns, and ',' is used
// as decimal dot in floating-point numbers.
void outputRow(double value, double time, FILE* file, char separator, bool header) {
    fmiReal r = value;
    char buffer[32];

    // print first column
    if (header) {
        fprintf(file, "time");
    }
    else {
        if (separator==',') {
            fprintf(file, "%.16g", time);
        }
        else {
            // separator is e.g. ';' or '\t'
            doubleToCommaString(buffer, time);
            fprintf(file, "%s", buffer);
        }
    }

    // print result column
    if (header) {
        // output names only
        if (separator == ',') {
            // treat array element, e.g. print a[1, 2] as a[1.2]
            const char* s = "output";
            fprintf(file, "%c", separator);
            while (*s) {
                if (*s != ' ') {
                    fprintf(file, "%c", *s == ',' ? '.' : *s);
                }
                s++;
            }
        }
        else {
            fprintf(file, "%c%s", separator, "output");
        }
    }
    else {
            // output values
            if (separator == ',') {
                fprintf(file, ",%.16g", r);
            }
            else {
                // separator is e.g. ';' or '\t'
                doubleToCommaString(buffer, r);
                fprintf(file, "%c%s", separator, buffer);
            }
    }
    // terminate this row
    fprintf(file, "\n");
}


// called by fmiInstantiate
// Set values for all variables that define a start value
// Settings used unless changed by fmiSetX before fmiEnterInitializationMode
void setStartValues(ModelInstance *comp) {
    fprintf(stderr, "output20RC1pt.c: setStartValues()\n");
    fflush(stderr);
    r(value_) = -1;
}

// called by fmiExitInitializationMode() after setting eventInfo to defaults
// Used to set the first time event, if any.
void initialize(ModelInstance* comp, fmiEventInfo* eventInfo) {
    eventInfo->nextEventTimeDefined   = fmiTrue;
    eventInfo->nextEventTime          = 1 + comp->time;

    // open result file
    if (!(file = fopen(RESULT_FILE, "w"))) {
        printf("Could not write %s because:\n", RESULT_FILE);
        printf("    %s\n", strerror(errno));
        eventInfo->terminateSimulation = fmiTrue;
    }

    // output solution for time t0
    outputRow(r(value_), comp->time, file, separator, true);  // output column names
    outputRow(r(value_), comp->time, file, separator, false); // output values

    fprintf(stderr, "output20RC1pt.c: initialized\n");
    fflush(stderr);
}

// used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmiEventInfo* eventInfo) {
    outputRow(r(value_), comp->time, file, separator, false); // output values
    eventInfo->nextEventTimeDefined   = fmiTrue;
    eventInfo->nextEventTime          = 1 + comp->time;
}

// called by fmiGetReal, fmiGetContinuousStates and fmiGetDerivatives
fmiReal getReal(ModelInstance* comp, fmiValueReference vr){
    switch (vr)
    {
        case value_:
            // return the input value
            return r(value_);
        default: return 0;
    }
}

// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

