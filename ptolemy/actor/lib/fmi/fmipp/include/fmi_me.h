/* -------------------------------------------------------------------------
 * fmi_me.h
 * Function types for all function of the "FMI for Model Exchange 1.0"
 * and a struct with the corresponding function pointers.
 *
 * Copyright 2010 QTronic GmbH. All rights reserved.
 *
 * The FmuSdk is licensed by the copyright holder under the BSD License
 * (http://www.opensource.org/licenses/bsd-license.html):
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY QTRONIC GMBH "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL QTRONIC GMBH BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * -------------------------------------------------------------------------*/

#ifndef _FMIPP_FMIME_H
#define _FMIPP_FMIME_H


extern "C"
{

#include "fmiModelFunctions.h"
#include "xml_parser.h"

        typedef const char* (*fGetModelTypesPlatform)();
        typedef const char* (*fGetVersion)();
        typedef fmiComponent (*fInstantiateModel)( fmiString instanceName, fmiString GUID,
                                                   fmiCallbackFunctions functions, fmiBoolean loggingOn );
        typedef void      (*fFreeModelInstance)  ( fmiComponent c );
        typedef fmiStatus (*fSetDebugLogging)    ( fmiComponent c, fmiBoolean loggingOn );
        typedef fmiStatus (*fSetTime)            ( fmiComponent c, fmiReal time );
        typedef fmiStatus (*fSetContinuousStates)( fmiComponent c, const fmiReal x[], size_t nx );
        typedef fmiStatus (*fCompletedIntegratorStep)( fmiComponent c, fmiBoolean* callEventUpdate );
        typedef fmiStatus (*fSetReal)   ( fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiReal    value[]);
        typedef fmiStatus (*fSetInteger)( fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiInteger value[] );
        typedef fmiStatus (*fSetBoolean)( fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiBoolean value[] );
        typedef fmiStatus (*fSetString) ( fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiString  value[] );
        typedef fmiStatus (*fInitialize)( fmiComponent c, fmiBoolean toleranceControlled,
                                          fmiReal relativeTolerance, fmiEventInfo* eventInfo );
        typedef fmiStatus (*fGetDerivatives)    ( fmiComponent c, fmiReal derivatives[]    , size_t nx );
        typedef fmiStatus (*fGetEventIndicators)( fmiComponent c, fmiReal eventIndicators[], size_t ni );
        typedef fmiStatus (*fGetReal)   ( fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiReal    value[] );
        typedef fmiStatus (*fGetInteger)( fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiInteger value[] );
        typedef fmiStatus (*fGetBoolean)( fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiBoolean value[] );
        typedef fmiStatus (*fGetString) ( fmiComponent c, const fmiValueReference vr[], size_t nvr, fmiString  value[] );
        typedef fmiStatus (*fEventUpdate)               ( fmiComponent c, fmiBoolean intermediateResults, fmiEventInfo* eventInfo );
        typedef fmiStatus (*fGetContinuousStates)       ( fmiComponent c, fmiReal states[], size_t nx );
        typedef fmiStatus (*fGetNominalContinuousStates)( fmiComponent c, fmiReal x_nominal[], size_t nx );
        typedef fmiStatus (*fGetStateValueReferences)   ( fmiComponent c, fmiValueReference vrx[], size_t nx );
        typedef fmiStatus (*fTerminate)                 ( fmiComponent c );

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
        } FMU_functions;

} // extern "C"


#endif // _FMIPP_FMIME_H
