/* ---------------------------------------------------------------------------*
 * fmuTemplate.h
 * Definitions by the includer of this file
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

#include <stdio.h>
#include <string.h>
#include <assert.h>

#include "fmiFunctions.h"

// macros used to define variables
#define  r(vr) comp->r[vr]
#define  i(vr) comp->i[vr]
#define  b(vr) comp->b[vr]
#define  s(vr) comp->s[vr]
#define pos(z) comp->isPositive[z]
fmiStatus setString(fmiComponent comp, fmiValueReference vr, fmiString value);
#define copy(vr, value) setString(comp, vr, value)

// categories of logging supported by model.
// Value is the index in logCategories of a ModelInstance.
#define LOG_ALL       0
#define LOG_ERROR     1
#define LOG_FMI_CALL  2
#define LOG_EVENT     3

#define NUMBER_OF_CATEGORIES 4

typedef enum {
    modelInstantiated       = 1<<0,
    modelInitializationMode = 1<<1,
    modelInitialized        = 1<<2, // state just after fmiExitInitializationMode
    modelStepping           = 1<<3, // state after initialization
    modelTerminated         = 1<<4,
    modelError              = 1<<5
} ModelState;

typedef struct {
    fmiReal    *r;
    fmiInteger *i;
    fmiBoolean *b;
    fmiString  *s;
    fmiBoolean *isPositive;

    fmiReal time;
    fmiString instanceName;
    fmiType type;
    fmiString GUID;
    const fmiCallbackFunctions *functions;
    fmiBoolean loggingOn;
    fmiBoolean logCategories[NUMBER_OF_CATEGORIES];

    fmiComponentEnvironment componentEnvironment;
    ModelState state;
    fmiEventInfo eventInfo;
} ModelInstance;
