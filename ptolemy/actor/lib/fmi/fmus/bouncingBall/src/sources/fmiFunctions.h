#ifndef fmiFunctions_h
#define fmiFunctions_h

/* This header file must be utilized when compiling a FMU.
   It defines all functions of Co-Simulation Interface.
   In order to have unique function names even if several FMUs
   are compiled together (e.g. for embedded systems), every "real" function name
   is constructed by prepending the function name by
   "MODEL_IDENTIFIER" + "_" where "MODEL_IDENTIFIER" is the short name
   of the model used as the name of the zip-file where the model is stored.
   Therefore, the typical usage is:

      #define MODEL_IDENTIFIER MyModel
      #include "fmiFunctions.h"

   As a result, a function that is defined as "fmiGetDerivatives" in this header file,
   is actually getting the name "MyModel_fmiGetDerivatives".

   Revisions:
   - November 4, 2010: Adapted to specification text:
                       o fmiGetModelTypesPlatform renamed to fmiGetTypesPlatform
                       o fmiInstantiateSlave: Argument GUID     replaced by fmuGUID
                                              Argument mimetype replaced by mimeType
                       o tabs replaced by spaces
   - October 16, 2010: First public Version


