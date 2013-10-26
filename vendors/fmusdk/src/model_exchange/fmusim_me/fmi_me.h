/* -------------------------------------------------------------------------
 * fmi_me.h
 * Function types for all function of the "FMI for Model Exchange 1.0"
 * and a struct with the corresponding function pointers.
 * Copyright 2011 QTronic GmbH. All rights reserved.
 * -------------------------------------------------------------------------
 */

#ifndef FMI_ME_H
#define FMI_ME_H

#ifdef _MSC_VER
#include <windows.h>
#define WINDOWS 1
#else
#include <errno.h>
#define WINDOWS 0
#define TRUE 1
#define FALSE 0
#define min(a,b) (a>b ? b : a)
#define HANDLE void *
/* See http://www.yolinux.com/TUTORIALS/LibraryArchives-StaticAndDynamic.html */
#include <dlfcn.h>
#endif

#include "fmiModelFunctions.h"
#include "xml_parser.h"

typedef const char* (*fGetModelTypesPlatform)();
typedef const char* (*fGetVersion)();
typedef fmiComponent (*fInstantiateModel)(fmiString instanceName, fmiString GUID,
                                        fmiCallbackFunctions functions, fmiBoolean loggingOn);
typedef void      (*fFreeModelInstance)  (fmiComponent c);
typedef fmiStatus (*fSetDebugLogging)    (fmiComponent c, fmiBoolean loggingOn);
typedef fmiStatus (*fSetTime)            (fmiComponent c, fmiReal time);
typedef fmiStatus (*fSetContinuousStates)(fmiComponent c, const fmiReal x[], size_t nx);
typedef fmiStatus (*fCompletedIntegratorStep)(fmiComponent c, fmiBoolean* callEventUpdate);
typedef fmiStatus (*fSetReal)   (fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiReal    value[]);
typedef fmiStatus (*fSetInteger)(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiInteger value[]);
typedef fmiStatus (*fSetBoolean)(fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiBoolean value[]);
typedef fmiStatus (*fSetString) (fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiString  value[]);
typedef fmiStatus (*fInitialize)(fmiComponent c, fmiBoolean toleranceControlled,
                               fmiReal relativeTolerance, fmiEventInfo* eventInfo);
typedef fmiStatus (*fGetDerivatives)    (fmiComponent c, fmiReal derivatives[]    , size_t nx);
typedef fmiStatus (*fGetEventIndicators)(fmiComponent c, fmiReal eventIndicators[], size_t ni);
typedef fmiStatus (*fGetReal)   (fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiReal    value[]);
typedef fmiStatus (*fGetInteger)(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiInteger value[]);
typedef fmiStatus (*fGetBoolean)(fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiBoolean value[]);
typedef fmiStatus (*fGetString) (fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiString  value[]);
typedef fmiStatus (*fEventUpdate)               (fmiComponent c, fmiBoolean intermediateResults, fmiEventInfo* eventInfo);
typedef fmiStatus (*fGetContinuousStates)       (fmiComponent c, fmiReal states[], size_t nx);
typedef fmiStatus (*fGetNominalContinuousStates)(fmiComponent c, fmiReal x_nominal[], size_t nx);
typedef fmiStatus (*fGetStateValueReferences)   (fmiComponent c, fmiValueReference vrx[], size_t nx);
typedef fmiStatus (*fTerminate)                 (fmiComponent c);

typedef struct {
    ModelDescription* modelDescription;
    HANDLE dllHandle;
    fGetModelTypesPlatform getModelTypesPlatform;
    fGetVersion getVersion;
    fInstantiateModel instantiateModel;
    fFreeModelInstance freeModelInstance;
    fSetDebugLogging setDebugLogging;
    fSetTime setTime;
    fSetContinuousStates setContinuousStates;
    fCompletedIntegratorStep completedIntegratorStep;
    fSetReal setReal;
    fSetInteger setInteger;
    fSetBoolean setBoolean;
    fSetString setString;
    fInitialize initialize;
    fGetDerivatives getDerivatives;
    fGetEventIndicators getEventIndicators;
    fGetReal getReal;
    fGetInteger getInteger;
    fGetBoolean getBoolean;
    fGetString getString;
    fEventUpdate eventUpdate;
    fGetContinuousStates getContinuousStates;
    fGetNominalContinuousStates getNominalContinuousStates;
    fGetStateValueReferences getStateValueReferences;
    fTerminate terminate;
/*
    fInstantiateSlave instantiateSlave;
    fInitializeSlave initializeSlave;
    fTerminateSlave terminateSlave;
    fResetSlave resetSlave;
    fFreeSlaveInstance freeSlaveInstance;
    fGetRealOutputDerivatives getRealOutputDerivatives;
    fSetRealInputDerivatives setRealInputDerivatives;
    fDoStep doStep;
    fCancelStep cancelStep;
    fGetStatus getStatus;
    fGetRealStatus getRealStatus;
    fGetIntegerStatus getIntegerStatus;
    fGetBooleanStatus getBooleanStatus;
    fGetStringStatus getStringStatus;
*/
} FMU;

#endif // FMI_ME_H

