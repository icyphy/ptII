/* ---------------------------------------------------------------------------*
 * Sample implementation of an FMU.
 * This demonstrates the use of all FMU variable types.
 * Copyright QTronic GmbH. All rights reserved.
 * ---------------------------------------------------------------------------*/

// define class name and unique id
#define MODEL_IDENTIFIER values20RC1
#define MODEL_GUID "{8c4e810f-3df3-4a00-8276-176fa3c9a201}"

// define model size
#define NUMBER_OF_REALS 2
#define NUMBER_OF_INTEGERS 2
#define NUMBER_OF_BOOLEANS 2
#define NUMBER_OF_STRINGS 2
#define NUMBER_OF_STATES 1
#define NUMBER_OF_EVENT_INDICATORS 0

// include fmu header files, typedefs and macros
#include "fmuTemplate.h"

// define all model variables and their value references
// conventions used here:
// - if x is a variable, then macro x_ is its variable reference
// - the vr of a variable is its index in array  r, i, b or s
// - if k is the vr of a real state, then k+1 is the vr of its derivative
#define x_          0
#define der_x_      1
#define int_in_     0
#define int_out_    1
#define bool_in_    0
#define bool_out_   1
#define string_in_  0
#define string_out_ 1

// define state vector as vector of value references
#define STATES { x_ }

const char* month[] = {
    "jan","feb","march","april","may","june","july",
    "august","sept","october","november","december"
};

#define PTOLEMY_SPECIFIC_CHANGES
#ifdef PTOLEMY_SPECIFIC_CHANGES
// Under Linux, the setString() function might invoke the fmiSetString() function from another FMU, so we use a copy.
// See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/FMU#ComplicationsWithLinuxSymbols
fmiStatus values20RC1_fmiSetString (fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiString value[]);

fmiStatus values20RC1_setString(fmiComponent comp, fmiValueReference vr, fmiString value){
    return values20RC1_fmiSetString(comp, &vr, 1, &value);
}
#define copy(vr, value) values20RC1_setString(comp, vr, value)
#endif //PTOLEMY_SPECIFIC_CHANGES

// called by fmiInstantiate
// Set values for all variables that define a start value
// Settings used unless changed by fmiSetX before fmiEnterInitializationMode
void setStartValues(ModelInstance *comp) {
    //fprintf(stderr, "values20RC1.c: setStartValues() start\n");
    r(x_) = 1;
    i(int_in_) = 2;
    i(int_out_) = 0;
    b(bool_in_) = fmiTrue;
    b(bool_out_) = fmiFalse;
    copy(string_in_, "a string");
    copy(string_out_, month[0]);
    //fprintf(stderr, "values20RC1.c: setStartValues() end\n");
}

// called by fmiExitInitializationMode() after setting eventInfo to defaults
// Used to set the first time event, if any.
void initialize(ModelInstance* comp, fmiEventInfo* eventInfo) {
    eventInfo->nextEventTimeDefined = fmiTrue;
    eventInfo->nextEventTime        = 1 + comp->time;
}

// called by fmiGetReal, fmiGetContinuousStates and fmiGetDerivatives
fmiReal getReal(ModelInstance* comp, fmiValueReference vr){
    switch (vr) {
        case x_     : return   r(x_);
        case der_x_ : return - r(x_);
        default: return 0;
    }
}

// used to set the next time event, if any.
void eventUpdate(ModelInstance* comp, fmiEventInfo* eventInfo) {
    eventInfo->nextEventTimeDefined = fmiTrue;
    eventInfo->nextEventTime        = 1 + comp->time;
    i(int_out_) += 1;
    b(bool_out_) = !b(bool_out_);
    //fprintf(stderr, "values20RC1.c eventUpdate: i(int_out_): %d, month[i(int_out_)]: <%s>\n", i(int_out_), month[i(int_out_)]);
    if (i(int_out_)<12) {
      copy(string_out_, month[i(int_out_)]);
    } else {
      eventInfo->terminateSimulation = fmiTrue;
    }
}


// include code that implements the FMI based on the above definitions
#include "fmuTemplate.c"

#ifdef PTOLEMY_SPECIFIC_CHANGES
fmiStatus values20RC1_fmiSetString (fmiComponent c, const fmiValueReference vr[], size_t nvr, const fmiString value[]) {
    int i;
    ModelInstance *comp = (ModelInstance *)c;
    //fprintf(stderr, "values20RC1 fmuTemplate.c fmiSetString()\n");
    //fflush(stderr);
    if (invalidState(comp, "fmiSetString", modelInstantiated|modelInitializationMode|modelInitialized|modelStepping))
        return fmiError;
    if (nvr>0 && nullPointer(comp, "fmiSetString", "vr[]", vr))
        return fmiError;
    if (nvr>0 && nullPointer(comp, "fmiSetString", "value[]", value))
        return fmiError;
    FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetString: nvr = %d", nvr)

    for (i = 0; i < nvr; i++) {
        char *string = (char *)comp->s[vr[i]];
        if (vrOutOfRange(comp, "fmiSetString", vr[i], NUMBER_OF_STRINGS))
            return fmiError;
        //fprintf(stdout, "values20RC1 fmuTemplate.c setString %d <%s>\n", vr[i], value[i]);
        FILTERED_LOG(comp, fmiOK, LOG_FMI_CALL, "fmiSetString: #s%d# = '%s'", vr[i], value[i])

        if (nullPointer(comp, "fmiSetString", "value[i]", value[i]))
            return fmiError;
        if (string == NULL || strlen(string) < strlen(value[i])) {
            if (string) comp->functions->freeMemory(string);
            comp->s[vr[i]] = comp->functions->allocateMemory(1 + strlen(value[i]), sizeof(char));
            if (!comp->s[vr[i]]) {
                comp->state = modelError;
                FILTERED_LOG(comp, fmiError, LOG_ERROR, "fmiSetString: Out of memory.")
                return fmiError;
            }
        }
        strcpy((char *)comp->s[vr[i]], (char *)value[i]);
    }
    return fmiOK;
}
#endif //PTOLEMY_SPECIFIC_CHANGES
