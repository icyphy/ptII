#ifndef fmi2FunctionTypes_h
#define fmi2FunctionTypes_h

#include "fmi2TypesPlatform.h"

/* This header file must be utilized when compiling an FMU or an FMI master.
   It declares data and function types for FMI 2.0

   Revisions:
   - Apr.  9, 2014: all prefixes "fmi" renamed to "fmi2" (decision from April 8)
   - Apr.  3, 2014: Added #include <stddef.h> for size_t definition
   - Mar. 27, 2014: Added #include "fmiTypesPlatform.h" (#179)
   - Mar. 26, 2014: Introduced function argument "void" for the functions (#171)
                      fmiGetTypesPlatformTYPE and fmiGetVersionTYPE
   - Oct. 11, 2013: Functions of ModelExchange and CoSimulation merged:
                      fmiInstantiateModelTYPE , fmiInstantiateSlaveTYPE  -> fmiInstantiateTYPE
                      fmiFreeModelInstanceTYPE, fmiFreeSlaveInstanceTYPE -> fmiFreeInstanceTYPE
                      fmiEnterModelInitializationModeTYPE, fmiEnterSlaveInitializationModeTYPE -> fmiEnterInitializationModeTYPE
                      fmiExitModelInitializationModeTYPE , fmiExitSlaveInitializationModeTYPE  -> fmiExitInitializationModeTYPE
                      fmiTerminateModelTYPE , fmiTerminateSlaveTYPE  -> fmiTerminate
                      fmiResetSlave -> fmiReset (now also for ModelExchange and not only for CoSimulation)
                    Functions renamed
                      fmiUpdateDiscreteStatesTYPE -> fmiNewDiscreteStatesTYPE
                    Renamed elements of the enumeration fmiEventInfo
                      upcomingTimeEvent             -> nextEventTimeDefined // due to generic naming scheme: varDefined + var
                      newUpdateDiscreteStatesNeeded -> newDiscreteStatesNeeded;
   - June 13, 2013: Changed type fmiEventInfo
                    Functions removed:
                       fmiInitializeModelTYPE
                       fmiEventUpdateTYPE
                       fmiCompletedEventIterationTYPE
                       fmiInitializeSlaveTYPE
                    Functions added:
                       fmiEnterModelInitializationModeTYPE
                       fmiExitModelInitializationModeTYPE
                       fmiEnterEventModeTYPE
                       fmiUpdateDiscreteStatesTYPE
                       fmiEnterContinuousTimeModeTYPE
                       fmiEnterSlaveInitializationModeTYPE;
                       fmiExitSlaveInitializationModeTYPE;
   - Feb. 17, 2013: Added third argument to fmiCompletedIntegratorStepTYPE
                    Changed function name "fmiTerminateType" to "fmiTerminateModelType" (due to #113)
                    Changed function name "fmiGetNominalContinuousStateTYPE" to
                                          "fmiGetNominalsOfContinuousStatesTYPE"
                    Removed fmiGetStateValueReferencesTYPE.
   - Nov. 14, 2011: First public Version


