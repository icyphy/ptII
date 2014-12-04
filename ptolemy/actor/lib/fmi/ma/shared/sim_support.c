/* -------------------------------------------------------------------------
 * sim_support.c
 * Functions used by both FMU simulators fmu20sim_me and fmu20sim_cs
 * to parse command-line arguments, to unzip and load an fmu,
 * to write CSV file, and more.
 *
 * Revision history
 *  07.03.2014 initial version released in FMU SDK 2.0.0
 *
 * Author: Adrian Tirea
 * Copyright QTronic GmbH. All rights reserved.
 * -------------------------------------------------------------------------*/

/* See $PTII/ptolemy/actor/lib/fmi/ma/fmusdk-license.htm for the complete FMUSDK License. */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <stdarg.h>
#include <unistd.h>  // mkdtemp(), getopt()

#include "fmi.h"
#include "sim_support.h"

#ifndef _MSC_VER
#define MAX_PATH 1024
#include <dlfcn.h> //dlsym()
#endif


#if WINDOWS
int unzip(const char *zipPath, const char *outPath) {
    int code;
    char cwd[BUFSIZE];
    char binPath[BUFSIZE];
    int n = strlen(UNZIP_CMD) + strlen(outPath) + 3 +  strlen(zipPath) + 9;
    char* cmd = (char*)calloc(sizeof(char), n);

    // remember current directory
    if (!GetCurrentDirectory(BUFSIZE, cwd)) {
        printf ("error: Could not get current directory\n");
        return 0; // error
    }

    // change to %FMUSDK_HOME%\bin to find 7z.dll and 7z.exe
    if (!GetEnvironmentVariable("FMUSDK_HOME", binPath, BUFSIZE)) {
        if (GetLastError() == ERROR_ENVVAR_NOT_FOUND) {
            printf ("error: Environment variable FMUSDK_HOME not defined\n");
        }
        else {
            printf ("error: Could not get value of FMUSDK_HOME\n");
        }
        return 0; // error
    }
    strcat(binPath, "\\bin");
    if (!SetCurrentDirectory(binPath)) {
        printf ("error: could not change to directory '%s'\n", binPath);
        return 0; // error
    }

    // run the unzip command
    // remove "> NUL" to see the unzip protocol
    sprintf(cmd, "%s\"%s\" \"%s\" > NUL", UNZIP_CMD, outPath, zipPath);
    // printf("cmd='%s'\n", cmd);
    code = system(cmd);
    free(cmd);
    if (code != SEVEN_ZIP_NO_ERROR) {
        printf("7z: ");
        switch (code) {
            case SEVEN_ZIP_WARNING:            printf("warning\n"); break;
            case SEVEN_ZIP_ERROR:              printf("error\n"); break;
            case SEVEN_ZIP_COMMAND_LINE_ERROR: printf("command line error\n"); break;
            case SEVEN_ZIP_OUT_OF_MEMORY:      printf("out of memory\n"); break;
            case SEVEN_ZIP_STOPPED_BY_USER:    printf("stopped by user\n"); break;
            default: printf("unknown problem\n");
        }
    }

    // restore current directory
    SetCurrentDirectory(cwd);
    return (code == SEVEN_ZIP_NO_ERROR || code == SEVEN_ZIP_WARNING) ? 1 : 0;
}

#else /* WINDOWS */

int unzip(const char *zipPath, const char *outPath) {
    int code;
    char cwd[BUFSIZE];
    int n;
    char* cmd;

    // remember current directory
    if (!getcwd(cwd, BUFSIZE)) {
      printf ("error: Could not get current directory\n");
      return 0; // error
    }

    // run the unzip command
    n = strlen(UNZIP_CMD) + strlen(outPath) + 1 +  strlen(zipPath) + 16;
    cmd = (char*)calloc(sizeof(char), n);
    sprintf(cmd, "%s%s \"%s\" > /dev/null", UNZIP_CMD, outPath, zipPath);
    printf("cmd='%s'\n", cmd);
    code = system(cmd);
    free(cmd);
    if (code!=SEVEN_ZIP_NO_ERROR) {
        switch (code) {
            printf("7z: ");
            case SEVEN_ZIP_WARNING:            printf("warning\n"); break;
            case SEVEN_ZIP_ERROR:              printf("error\n"); break;
            case SEVEN_ZIP_COMMAND_LINE_ERROR: printf("command line error\n"); break;
            case SEVEN_ZIP_OUT_OF_MEMORY:      printf("out of memory\n"); break;
            case SEVEN_ZIP_STOPPED_BY_USER:    printf("stopped by user\n"); break;
            default: printf("unknown problem\n");
        }
    }

    // restore current directory
    chdir(cwd);

    return (code==SEVEN_ZIP_NO_ERROR || code==SEVEN_ZIP_WARNING) ? 1 : 0;
}
#endif /* WINDOWS */

#ifdef _MSC_VER
// fileName is an absolute path, e.g. C:\test\a.fmu
// or relative to the current dir, e.g. ..\test\a.fmu
// Does not check for existence of the file
static char* getFmuPath(const char* fileName){
    char pathName[MAX_PATH];
    int n = GetFullPathName(fileName, MAX_PATH, pathName, NULL);
    return n ? strdup(pathName) : NULL;
}

static char* getTmpPath() {
    char tmpPath[BUFSIZE];
    if(! GetTempPath(BUFSIZE, tmpPath)) {
        printf ("error: Could not find temporary disk space\n");
        return NULL;
    }
    strcat(tmpPath, "fmu\\");
    return strdup(tmpPath);
}

#else
// fmuFileName is an absolute path, e.g. "C:\test\a.fmu"
// or relative to the current dir, e.g. "..\test\a.fmu"
static char* getFmuPath(const char* fmuFileName){
  /* Not sure why this is useful.  Just returning the filename. */
  return strdup(fmuFileName);
}
static char* getTmpPath() {
        // TODO: replace with strdupa()
  char template[13];  // Lenght of "fmuTmpXXXXXX" + null
  sprintf(template, "%s", "fmuTmpXXXXXX");
  //char *tmp = mkdtemp(strdup("fmuTmpXXXXXX"));
  char *tmp = mkdtemp(template);
  if (tmp==NULL) {
    fprintf(stderr, "Couldn't create temporary directory\n");
    exit(1);
  }
  char * results = calloc(sizeof(char), strlen(tmp) + 2);
  strncat(results, tmp, strlen(tmp));
  return strcat(results, "/");
}
#endif

char *getTempResourcesLocation() {
    char *tempPath = getTmpPath();
    char *resourcesLocation = (char *)calloc(sizeof(char), 8 + strlen(RESOURCES_DIR) + strlen(tempPath));
    strcpy(resourcesLocation, "file://");
    strcat(resourcesLocation, tempPath);
    strcat(resourcesLocation, RESOURCES_DIR);
    free(tempPath);
    return resourcesLocation;
}

static void *getAdr(int *success, HMODULE dllHandle, const char *functionName) {
    void* fp;
#ifdef _MSC_VER
    fp = GetProcAddress(dllHandle, functionName);
#else
    fp = dlsym(dllHandle, functionName);
#endif
    if (!fp) {
        printf("warning: Function %s not found in dll\n", functionName);
#ifdef _MSC_VER
#else
        printf ("Error was: %s\n", dlerror());
#endif
        *success = 0;
    }
    return fp;
}

// Load the given dll and set function pointers in fmu
// Return 0 to indicate failure
static int loadDll(const char* dllPath, FMU *fmu) {
    int s = 1;
#ifdef _MSC_VER
    HMODULE h = LoadLibrary(dllPath);
#else
    printf("dllPath = %s\n", dllPath);
    HMODULE h = dlopen(dllPath, RTLD_LAZY);
#endif

    if (!h) {
        printf("error: Could not load %s\n", dllPath);
#ifdef _MSC_VER
#else
        printf("The error was: %s\n", dlerror());
#endif
        return 0; // failure
    }
    fmu->dllHandle = h;
    fmu->getTypesPlatform          = (fmiGetTypesPlatformTYPE *)      getAdr(&s, h, "fmiGetTypesPlatform");
    fmu->getVersion                = (fmiGetVersionTYPE *)            getAdr(&s, h, "fmiGetVersion");
    fmu->setDebugLogging           = (fmiSetDebugLoggingTYPE *)       getAdr(&s, h, "fmiSetDebugLogging");
    fmu->instantiate               = (fmiInstantiateTYPE *)           getAdr(&s, h, "fmiInstantiate");
    fmu->freeInstance              = (fmiFreeInstanceTYPE *)          getAdr(&s, h, "fmiFreeInstance");
    fmu->setupExperiment           = (fmiSetupExperimentTYPE *)       getAdr(&s, h, "fmiSetupExperiment");
    fmu->enterInitializationMode   = (fmiEnterInitializationModeTYPE *) getAdr(&s, h, "fmiEnterInitializationMode");
    fmu->exitInitializationMode    = (fmiExitInitializationModeTYPE *) getAdr(&s, h, "fmiExitInitializationMode");
    fmu->terminate                 = (fmiTerminateTYPE *)             getAdr(&s, h, "fmiTerminate");
    fmu->reset                     = (fmiResetTYPE *)                 getAdr(&s, h, "fmiReset");
    fmu->getReal                   = (fmiGetRealTYPE *)               getAdr(&s, h, "fmiGetReal");
    fmu->getInteger                = (fmiGetIntegerTYPE *)            getAdr(&s, h, "fmiGetInteger");
    fmu->getBoolean                = (fmiGetBooleanTYPE *)            getAdr(&s, h, "fmiGetBoolean");
    fmu->getString                 = (fmiGetStringTYPE *)             getAdr(&s, h, "fmiGetString");
    fmu->setReal                   = (fmiSetRealTYPE *)               getAdr(&s, h, "fmiSetReal");
    fmu->setInteger                = (fmiSetIntegerTYPE *)            getAdr(&s, h, "fmiSetInteger");
    fmu->setBoolean                = (fmiSetBooleanTYPE *)            getAdr(&s, h, "fmiSetBoolean");
    fmu->setString                 = (fmiSetStringTYPE *)             getAdr(&s, h, "fmiSetString");
    fmu->getFMUstate               = (fmiGetFMUstateTYPE *)           getAdr(&s, h, "fmiGetFMUstate");
    fmu->setFMUstate               = (fmiSetFMUstateTYPE *)           getAdr(&s, h, "fmiSetFMUstate");
    fmu->freeFMUstate              = (fmiFreeFMUstateTYPE *)          getAdr(&s, h, "fmiFreeFMUstate");
    fmu->serializedFMUstateSize    = (fmiSerializedFMUstateSizeTYPE *) getAdr(&s, h, "fmiSerializedFMUstateSize");
    fmu->serializeFMUstate         = (fmiSerializeFMUstateTYPE *)     getAdr(&s, h, "fmiSerializeFMUstate");
    fmu->deSerializeFMUstate       = (fmiDeSerializeFMUstateTYPE *)   getAdr(&s, h, "fmiDeSerializeFMUstate");
    fmu->getDirectionalDerivative  = (fmiGetDirectionalDerivativeTYPE *) getAdr(&s, h, "fmiGetDirectionalDerivative");
#ifdef FMI_COSIMULATION
    fmu->setRealInputDerivatives   = (fmiSetRealInputDerivativesTYPE *) getAdr(&s, h, "fmiSetRealInputDerivatives");
    fmu->getRealOutputDerivatives  = (fmiGetRealOutputDerivativesTYPE *) getAdr(&s, h, "fmiGetRealOutputDerivatives");
    fmu->doStep                    = (fmiDoStepTYPE *)                getAdr(&s, h, "fmiDoStep");
    fmu->cancelStep                = (fmiCancelStepTYPE *)            getAdr(&s, h, "fmiCancelStep");
    fmu->getStatus                 = (fmiGetStatusTYPE *)             getAdr(&s, h, "fmiGetStatus");
    fmu->getRealStatus             = (fmiGetRealStatusTYPE *)         getAdr(&s, h, "fmiGetRealStatus");
    fmu->getIntegerStatus          = (fmiGetIntegerStatusTYPE *)      getAdr(&s, h, "fmiGetIntegerStatus");
    fmu->getBooleanStatus          = (fmiGetBooleanStatusTYPE *)      getAdr(&s, h, "fmiGetBooleanStatus");
    fmu->getStringStatus           = (fmiGetStringStatusTYPE *)       getAdr(&s, h, "fmiGetStringStatus");
#else // FMI for Model Exchange
    fmu->enterEventMode            = (fmiEnterEventModeTYPE *)        getAdr(&s, h, "fmiEnterEventMode");
    fmu->newDiscreteStates         = (fmiNewDiscreteStatesTYPE *)     getAdr(&s, h, "fmiNewDiscreteStates");
    fmu->enterContinuousTimeMode   = (fmiEnterContinuousTimeModeTYPE *) getAdr(&s, h, "fmiEnterContinuousTimeMode");
    fmu->completedIntegratorStep   = (fmiCompletedIntegratorStepTYPE *) getAdr(&s, h, "fmiCompletedIntegratorStep");
    fmu->setTime                   = (fmiSetTimeTYPE *)               getAdr(&s, h, "fmiSetTime");
    fmu->setContinuousStates       = (fmiSetContinuousStatesTYPE *)   getAdr(&s, h, "fmiSetContinuousStates");
    fmu->getDerivatives            = (fmiGetDerivativesTYPE *)        getAdr(&s, h, "fmiGetDerivatives");
    fmu->getEventIndicators        = (fmiGetEventIndicatorsTYPE *)    getAdr(&s, h, "fmiGetEventIndicators");
    fmu->getContinuousStates       = (fmiGetContinuousStatesTYPE *)   getAdr(&s, h, "fmiGetContinuousStates");
    fmu->getNominalsOfContinuousStates = (fmiGetNominalsOfContinuousStatesTYPE *) getAdr(&s, h, "fmiGetNominalsOfContinuousStates");
#endif

    return s;
}

static void printModelDescription(ModelDescription* md){
    Element* e = (Element*)md;
    int i;
    int n; // number of attributes
    const char **attributes = getAttributesAsArray(e, &n);
    Component *component;

    printf("%s\n", getElementTypeName(e));
    for (i = 0; i < n; i += 2) {
        printf("  %s=%s\n", attributes[i], attributes[i+1]);
    }
    if (attributes) free(attributes);

#ifdef FMI_COSIMULATION
    component = getCoSimulation(md);
    if (!component) {
        printf("error: No CoSimulation element found in model description. This FMU is not for Co-Simulation.\n");
        exit(EXIT_FAILURE);
    }
#else // FMI_MODEL_EXCHANGE
    component = getModelExchange(md);
    if (!component) {
        printf("error: No ModelExchange element found in model description. This FMU is not for Model Exchange.\n");
        exit(EXIT_FAILURE);
    }
#endif
    printf("%s\n", getElementTypeName((Element *)component));
    attributes = getAttributesAsArray((Element *)component, &n);
    for (i = 0; i < n; i += 2) {
        printf("  %s=%s\n", attributes[i], attributes[i+1]);
    }
    if (attributes) free(attributes);
}

void loadFMU(FMU *fmu, const char* fmuFileName) {
    char* fmuPath;
    char* tmpPath;
    char* xmlPath;
    char* dllPath;
    const char *modelId;

    // get absolute path to FMU, NULL if not found
    fmuPath = getFmuPath(fmuFileName); // TODO: Is this function call really neccessary?
    if (!fmuPath) {
        printf("Strange function call for FMU file %s failed!\n", fmuFileName);
        exit(EXIT_FAILURE);
    }

    // unzip the FMU to the tmpPath directory
    tmpPath = getTmpPath();
    if (!unzip(fmuPath, tmpPath)) {
        printf("Could not unzip FMU at %s. Does this file exist?\n", fmuFileName);
        exit(EXIT_FAILURE);
    }

    // parse tmpPath\modelDescription.xml
    xmlPath = calloc(sizeof(char), strlen(tmpPath) + strlen(XML_FILE) + 1);
    sprintf(xmlPath, "%s%s", tmpPath, XML_FILE);
    fmu->modelDescription = parse(xmlPath);
    free(xmlPath);
    if (!fmu->modelDescription) exit(EXIT_FAILURE);
    printModelDescription(fmu->modelDescription);
#ifdef FMI_COSIMULATION
    modelId = getAttributeValue((Element *)getCoSimulation(fmu->modelDescription), att_modelIdentifier);
#else // FMI_MODEL_EXCHANGE
    modelId = getAttributeValue((Element *)getModelExchange(fmu->modelDescription), att_modelIdentifier);
#endif
    // load the FMU dll
    dllPath = calloc(sizeof(char), strlen(tmpPath) + strlen(DLL_DIR)
            + strlen(modelId) +  strlen(DLL_SUFFIX) + 1);
    sprintf(dllPath,"%s%s%s%s", tmpPath, DLL_DIR, modelId, DLL_SUFFIX);
    if (!loadDll(dllPath, fmu)) {
        // try the alternative directory and suffix
        dllPath = calloc(sizeof(char), strlen(tmpPath) + strlen(DLL_DIR2)
                + strlen(modelId) +  strlen(DLL_SUFFIX2) + 1);
        sprintf(dllPath,"%s%s%s%s", tmpPath, DLL_DIR2, modelId, DLL_SUFFIX2);
        if (!loadDll(dllPath, fmu)) exit(EXIT_FAILURE);
    }

    if (!loadDll(dllPath, fmu)) exit(EXIT_FAILURE);
    free(dllPath);
    free(fmuPath);
    free(tmpPath);
}

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
void outputRow(FMU *fmu, fmiComponent c, double time, FILE* file, char separator, boolean header) {
    int k;
    fmiReal r;
    fmiInteger i;
    fmiBoolean b;
    fmiString s;
    fmiValueReference vr;
    int n = getScalarVariableSize(fmu->modelDescription);
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

    // print all other columns
    for (k = 0; k < n; k++) {
        ScalarVariable* sv = getScalarVariable(fmu->modelDescription, k);
        if (header) {
            // output names only
            if (separator == ',') {
                // treat array element, e.g. print a[1, 2] as a[1.2]
                const char* s = getAttributeValue((Element *)sv, att_name);
                fprintf(file, "%c", separator);
                while (*s) {
                    if (*s != ' ') {
                        fprintf(file, "%c", *s == ',' ? '.' : *s);
                    }
                    s++;
                }
            }
            else {
                fprintf(file, "%c%s", separator, getAttributeValue((Element *)sv, att_name));
            }
        }
        else {
            // output values
            vr = getValueReference(sv);
            switch (getElementType(getTypeSpec(sv))) {
                case elm_Real:
                    fmu->getReal(c, &vr, 1, &r);
                    if (separator == ',') {
                        fprintf(file, ",%.16g", r);
                    }
                    else {
                        // separator is e.g. ';' or '\t'
                        doubleToCommaString(buffer, r);
                        fprintf(file, "%c%s", separator, buffer);
                    }
                    break;
                case elm_Integer:
                case elm_Enumeration:
                    fmu->getInteger(c, &vr, 1, &i);
                    fprintf(file, "%c%d", separator, i);
                    break;
                case elm_Boolean:
                    fmu->getBoolean(c, &vr, 1, &b);
                    fprintf(file, "%c%d", separator, b);
                    break;
                case elm_String:
                    fmu->getString(c, &vr, 1, &s);
                    fprintf(file, "%c%s", separator, s);
                    break;
                default:
                    fprintf(file, "%cNoValueForType=%d", separator, getElementType(getTypeSpec(sv)));
            }
        }
    } // for

    // terminate this row
    fprintf(file, "\n");
}

static const char* fmiStatusToString(fmiStatus status) {
    switch (status) {
        case fmiOK:      return "ok";
        case fmiWarning: return "warning";
        case fmiDiscard: return "discard";
        case fmiError:   return "error";
        case fmiFatal:   return "fatal";
    #ifdef FMI_COSIMULATION
        case fmiPending: return "fmiPending";
    #endif
        default:         return "?";
    }
}

// search a fmu for the given variable, matching the type specified.
// return NULL if not found
static ScalarVariable* getSV(FMU* fmu, char type, fmiValueReference vr) {
    int i;
    int n = getScalarVariableSize(fmu->modelDescription);
    Elm tp;

    switch (type) {
        case 'r': tp = elm_Real;    break;
        case 'i': tp = elm_Integer; break;
        case 'b': tp = elm_Boolean; break;
        case 's': tp = elm_String;  break;
        default : tp = elm_BAD_DEFINED;
    }
    for (i = 0; i < n; i++) {
        ScalarVariable* sv = getScalarVariable(fmu->modelDescription ,i);
        if (vr == getValueReference(sv) && tp == getElementType(getTypeSpec(sv))) {
            return sv;
        }
    }
    return NULL;
}

// replace e.g. #r1365# by variable name and ## by # in message
// copies the result to buffer
static void replaceRefsInMessage(const char* msg, char* buffer, int nBuffer, FMU* fmu){
    int i = 0; // position in msg
    int k = 0; // position in buffer
    int n;
    char c = msg[i];
    while (c != '\0' && k < nBuffer) {
        if (c != '#') {
            buffer[k++] = c;
            i++;
            c = msg[i];
        } else {

            char* end = strchr(msg + i + 1, '#');
            if (!end) {
                printf("unmatched '#' in '%s'\n", msg);
                buffer[k++] = '#';
                break;
            }
            n = end - (msg + i);
            if (n == 1) {
                // ## detected, output #
                buffer[k++] = '#';
                i += 2;
                c = msg[i];

            } else {
                char type = msg[i + 1]; // one of ribs
                fmiValueReference vr;
                int nvr = sscanf(msg + i + 2, "%u", &vr);
                if (nvr == 1) {
                    // vr of type detected, e.g. #r12#
                    ScalarVariable* sv = getSV(fmu, type, vr);
                    const char* name = sv ? getAttributeValue((Element *)sv, att_name) : "?";
                    sprintf(buffer + k, "%s", name);
                    k += strlen(name);
                    i += (n+1);
                    c = msg[i];

                } else {
                    // could not parse the number
                    printf("illegal value reference at position %d in '%s'\n", i + 2, msg);
                    buffer[k++] = '#';
                    break;
                }
            }
        }
    } // while
    buffer[k] = '\0';
}

#define MAX_MSG_SIZE 1000
void fmuLogger(void *componentEnvironment, /*FMU *fmu,*/ fmiString instanceName, fmiStatus status,
               fmiString category, fmiString message, ...) {
    char msg[MAX_MSG_SIZE];
    char* copy;
    va_list argp;

    // replace C format strings
    va_start(argp, message);
    vsprintf(msg, message, argp);

    // replace e.g. ## and #r12#
    copy = strdup(msg);
    replaceRefsInMessage(copy, msg, MAX_MSG_SIZE, (FMU*)componentEnvironment);
    free(copy);

    // print the final message
    if (!instanceName) instanceName = "?";
    if (!category) category = "?";
    printf("%s %s (%s): %s\n", fmiStatusToString(status), instanceName, category, msg);
}

int error(const char* message){
    printf("%s\n", message);
    return 0;
}

// TODO: Implement log categories

void parseArguments(int argc, char *argv[], char **fmuFileNames, double *tEnd, double *h,
                    int *loggingOn, char *csv_separator, int *nCategories, char **logCategories[]) {

    int option = 0;
    int i;

    while ((option = getopt(argc, argv, "t:h:ls:f:")) != -1) {
        switch(option) {
            case 't' :
                if (sscanf(optarg,"%lf", tEnd) != 1) {
                    printf("error: The given stepsize (%s) is not a number\n", optarg);
                    exit(EXIT_FAILURE);
                }
                break;
            case 'h' :
                if (sscanf(optarg,"%lf", h) != 1) {
                    printf("error: The given stepsize (%s) is not a number\n", optarg);
                    exit(EXIT_FAILURE);
                }
                break;
            case 'l' : *loggingOn = 1;
                break;
            case 's' :
                if (strlen(optarg) != 1) {
                    printf("error: The given CSV separator char (%s) is not valid\n", optarg);
                    exit(EXIT_FAILURE);
                } else {
                    switch (*optarg) {
                        case 'c': *csv_separator = ','; break; // comma
                        case 's': *csv_separator = ';'; break; // semicolon
                        default:  *csv_separator = *optarg; break; // any other char
                }
                break;
            }
        }
    }
    // number of positional arguments (arguments without a dash)
    int posArgs = argc - optind;

    // parse FMU files
    if (posArgs > 0) {
        for (i = 0; i < posArgs; i++) {
            if (strstr(argv[optind], ".fmu") || strstr(argv[optind], ".FMU")) {
                // save fmu file path and name to array
                fmuFileNames[i] = argv[optind];
                // set optind to next element after current fmu file
                optind++;
            }
            else {
                break;
            }
        }
    } else {
        printf("Error: No FMU file specified.\n");
        printHelp(argv[0]);
        exit(EXIT_FAILURE);
    }

    *nCategories = argc - optind;
    // parse log categories
    if (*nCategories > 0) {
        *logCategories = (char **)calloc(sizeof(char *), *nCategories);
        for (i = 0; i < *nCategories; i++) {
            (*logCategories)[i] = argv[optind];
            optind++;
        }
    }
}

void printHelp(const char *fmusim) {
    printf("command syntax: %s <model1.fmu> [<modelX.fmu> ...] -t<tEnd> -h<h> -s<csvSeparator> -l <logCategories>\n", fmusim);
    printf("   <model.fmu> .... path to FMU, relative to current dir or absolute, required\n");
    printf("   -t<tEnd> ......... end time of simulation,  optional, defaults to 1.0 sec\n");
    printf("   -h<h> ............ step size of simulation, optional, defaults to 0.1 sec\n");
    printf("   -l ............... activate logging,        optional, no parameter needed\n");
    printf("   -s<csvSeparator> . separator in csv file,   optional, c for ',', s for';', defaults to c\n");
    printf("   <logCategories> .. list of log categories,  optional, see modelDescription.xml for possible values\n");
}
