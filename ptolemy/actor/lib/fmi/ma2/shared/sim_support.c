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

/* See $PTII/ptolemy/actor/lib/fmi/ma2/fmusdk-license.htm for the complete FMUSDK License. */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <stdarg.h>
#include <unistd.h>  // mkdtemp(), getopt()

#include "fmi2.h"
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
    fmu->getTypesPlatform          = (fmi2GetTypesPlatformTYPE *)      getAdr(&s, h, "fmi2GetTypesPlatform");
    fmu->getVersion                = (fmi2GetVersionTYPE *)            getAdr(&s, h, "fmi2GetVersion");
    fmu->setDebugLogging           = (fmi2SetDebugLoggingTYPE *)       getAdr(&s, h, "fmi2SetDebugLogging");
    fmu->instantiate               = (fmi2InstantiateTYPE *)           getAdr(&s, h, "fmi2Instantiate");
    fmu->freeInstance              = (fmi2FreeInstanceTYPE *)          getAdr(&s, h, "fmi2FreeInstance");
    fmu->setupExperiment           = (fmi2SetupExperimentTYPE *)       getAdr(&s, h, "fmi2SetupExperiment");
    fmu->enterInitializationMode   = (fmi2EnterInitializationModeTYPE *) getAdr(&s, h, "fmi2EnterInitializationMode");
    fmu->exitInitializationMode    = (fmi2ExitInitializationModeTYPE *) getAdr(&s, h, "fmi2ExitInitializationMode");
    fmu->terminate                 = (fmi2TerminateTYPE *)             getAdr(&s, h, "fmi2Terminate");
    fmu->reset                     = (fmi2ResetTYPE *)                 getAdr(&s, h, "fmi2Reset");
    fmu->getReal                   = (fmi2GetRealTYPE *)               getAdr(&s, h, "fmi2GetReal");
    fmu->getMaxStepSize            = (fmi2GetMaxStepSizeTYPE *)        getAdr(&s, h, "fmi2GetMaxStepSize");
    fmu->getInteger                = (fmi2GetIntegerTYPE *)            getAdr(&s, h, "fmi2GetInteger");
    fmu->getBoolean                = (fmi2GetBooleanTYPE *)            getAdr(&s, h, "fmi2GetBoolean");
    fmu->getString                 = (fmi2GetStringTYPE *)             getAdr(&s, h, "fmi2GetString");
    fmu->setReal                   = (fmi2SetRealTYPE *)               getAdr(&s, h, "fmi2SetReal");
    fmu->setInteger                = (fmi2SetIntegerTYPE *)            getAdr(&s, h, "fmi2SetInteger");
    fmu->setBoolean                = (fmi2SetBooleanTYPE *)            getAdr(&s, h, "fmi2SetBoolean");
    fmu->setString                 = (fmi2SetStringTYPE *)             getAdr(&s, h, "fmi2SetString");
    fmu->getFMUstate               = (fmi2GetFMUstateTYPE *)           getAdr(&s, h, "fmi2GetFMUstate");
    fmu->setFMUstate               = (fmi2SetFMUstateTYPE *)           getAdr(&s, h, "fmi2SetFMUstate");
    fmu->freeFMUstate              = (fmi2FreeFMUstateTYPE *)          getAdr(&s, h, "fmi2FreeFMUstate");
    fmu->serializedFMUstateSize    = (fmi2SerializedFMUstateSizeTYPE *) getAdr(&s, h, "fmi2SerializedFMUstateSize");
    fmu->serializeFMUstate         = (fmi2SerializeFMUstateTYPE *)     getAdr(&s, h, "fmi2SerializeFMUstate");
    fmu->deSerializeFMUstate       = (fmi2DeSerializeFMUstateTYPE *)   getAdr(&s, h, "fmi2DeSerializeFMUstate");
    fmu->getDirectionalDerivative  = (fmi2GetDirectionalDerivativeTYPE *) getAdr(&s, h, "fmi2GetDirectionalDerivative");
#ifdef FMI_COSIMULATION
    fmu->setRealInputDerivatives   = (fmi2SetRealInputDerivativesTYPE *) getAdr(&s, h, "fmi2SetRealInputDerivatives");
    fmu->getRealOutputDerivatives  = (fmi2GetRealOutputDerivativesTYPE *) getAdr(&s, h, "fmi2GetRealOutputDerivatives");
    fmu->doStep                    = (fmi2DoStepTYPE *)                getAdr(&s, h, "fmi2DoStep");
    fmu->cancelStep                = (fmi2CancelStepTYPE *)            getAdr(&s, h, "fmi2CancelStep");
    fmu->getStatus                 = (fmi2GetStatusTYPE *)             getAdr(&s, h, "fmi2GetStatus");
    fmu->getRealStatus             = (fmi2GetRealStatusTYPE *)         getAdr(&s, h, "fmi2GetRealStatus");
    fmu->getIntegerStatus          = (fmi2GetIntegerStatusTYPE *)      getAdr(&s, h, "fmi2GetIntegerStatus");
    fmu->getBooleanStatus          = (fmi2GetBooleanStatusTYPE *)      getAdr(&s, h, "fmi2GetBooleanStatus");
    fmu->getStringStatus           = (fmi2GetStringStatusTYPE *)       getAdr(&s, h, "fmi2GetStringStatus");
#else // FMI for Model Exchange
    fmu->enterEventMode            = (fmi2EnterEventModeTYPE *)        getAdr(&s, h, "fmi2EnterEventMode");
    fmu->newDiscreteStates         = (fmi2NewDiscreteStatesTYPE *)     getAdr(&s, h, "fmi2NewDiscreteStates");
    fmu->enterContinuousTimeMode   = (fmi2EnterContinuousTimeModeTYPE *) getAdr(&s, h, "fmi2EnterContinuousTimeMode");
    fmu->completedIntegratorStep   = (fmi2CompletedIntegratorStepTYPE *) getAdr(&s, h, "fmi2CompletedIntegratorStep");
    fmu->setTime                   = (fmi2SetTimeTYPE *)               getAdr(&s, h, "fmi2SetTime");
    fmu->setContinuousStates       = (fmi2SetContinuousStatesTYPE *)   getAdr(&s, h, "fmi2SetContinuousStates");
    fmu->getDerivatives            = (fmi2GetDerivativesTYPE *)        getAdr(&s, h, "fmi2GetDerivatives");
    fmu->getEventIndicators        = (fmi2GetEventIndicatorsTYPE *)    getAdr(&s, h, "fmi2GetEventIndicators");
    fmu->getContinuousStates       = (fmi2GetContinuousStatesTYPE *)   getAdr(&s, h, "fmi2GetContinuousStates");
    fmu->getNominalsOfContinuousStates = (fmi2GetNominalsOfContinuousStatesTYPE *) getAdr(&s, h, "fmi2GetNominalsOfContinuousStates");
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
void outputRow(FMU *fmus, int numberOfFMUs, char* NAMES_OF_FMUS[], double time, FILE* file, char separator, boolean header) {

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
    int j = 0;
    for (j = 0; j < numberOfFMUs; j++)
    {   
        int k;
        fmi2Real r;
        fmi2Integer i;
        fmi2Boolean b;
        fmi2String s;
        fmi2ValueReference vr;

        FMU *fmu = &fmus[j];
        int n = getScalarVariableSize(fmu->modelDescription);
        fmi2Component c = fmu->component;

        for (k = 0; k < n; k++) {
            ScalarVariable* sv = getScalarVariable(fmu->modelDescription, k);
            if (header) {
                // output names only
                if (separator == ',') {
                    // treat array element, e.g. print a[1, 2] as a[1.2]
                    const char* s = getAttributeValue((Element *)sv, att_name);
                    fprintf(file, "%c", separator);
                    fprintf(file, "%s_", NAMES_OF_FMUS[j]);
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
        } // for fmus variables
    } // for fmus

    

    // terminate this row
    fprintf(file, "\n");
}
// void outputRow(FMU *fmu, fmi2Component c, double time, FILE* file, char separator, boolean header) {
//     int k;
//     fmi2Real r;
//     fmi2Integer i;
//     fmi2Boolean b;
//     fmi2String s;
//     fmi2ValueReference vr;
//     int n = getScalarVariableSize(fmu->modelDescription);
//     char buffer[32];

//     // print first column
//     if (header) {
//         fprintf(file, "time");
//     }
//     else {
//         if (separator==',') {
//             fprintf(file, "%.16g", time);
//         }
//         else {
//             // separator is e.g. ';' or '\t'
//             doubleToCommaString(buffer, time);
//             fprintf(file, "%s", buffer);
//         }
//     }

//     // print all other columns
//     for (k = 0; k < n; k++) {
//         ScalarVariable* sv = getScalarVariable(fmu->modelDescription, k);
//         if (header) {
//             // output names only
//             if (separator == ',') {
//                 // treat array element, e.g. print a[1, 2] as a[1.2]
//                 const char* s = getAttributeValue((Element *)sv, att_name);
//                 fprintf(file, "%c", separator);
//                 while (*s) {
//                     if (*s != ' ') {
//                         fprintf(file, "%c", *s == ',' ? '.' : *s);
//                     }
//                     s++;
//                 }
//             }
//             else {
//                 fprintf(file, "%c%s", separator, getAttributeValue((Element *)sv, att_name));
//             }
//         }
//         else {
//             // output values
//             vr = getValueReference(sv);
//             switch (getElementType(getTypeSpec(sv))) {
//                 case elm_Real:
//                     fmu->getReal(c, &vr, 1, &r);
//                     if (separator == ',') {
//                         fprintf(file, ",%.16g", r);
//                     }
//                     else {
//                         // separator is e.g. ';' or '\t'
//                         doubleToCommaString(buffer, r);
//                         fprintf(file, "%c%s", separator, buffer);
//                     }
//                     break;
//                 case elm_Integer:
//                 case elm_Enumeration:
//                     fmu->getInteger(c, &vr, 1, &i);
//                     fprintf(file, "%c%d", separator, i);
//                     break;
//                 case elm_Boolean:
//                     fmu->getBoolean(c, &vr, 1, &b);
//                     fprintf(file, "%c%d", separator, b);
//                     break;
//                 case elm_String:
//                     fmu->getString(c, &vr, 1, &s);
//                     fprintf(file, "%c%s", separator, s);
//                     break;
//                 default:
//                     fprintf(file, "%cNoValueForType=%d", separator, getElementType(getTypeSpec(sv)));
//             }
//         }
//     } // for

//     // terminate this row
//     fprintf(file, "\n");
// }

static const char* fmi2StatusToString(fmi2Status status) {
    switch (status) {
        case fmi2OK:      return "ok";
        case fmi2Warning: return "warning";
        case fmi2Discard: return "discard";
        case fmi2Error:   return "error";
        case fmi2Fatal:   return "fatal";
    #ifdef FMI_COSIMULATION
        case fmi2Pending: return "fmi2Pending";
    #endif
        default:         return "?";
    }
}

// search a fmu for the given variable, matching the type specified.
// return NULL if not found
static ScalarVariable* getSV(FMU* fmu, char type, fmi2ValueReference vr) {
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
                fmi2ValueReference vr;
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
void fmuLogger(void *componentEnvironment, /*FMU *fmu,*/ fmi2String instanceName, fmi2Status status,
               fmi2String category, fmi2String message, ...) {
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
    printf("%s %s (%s): %s\n", fmi2StatusToString(status), instanceName, category, msg);
}

int error(const char* message){
    printf("%s\n", message);
    return 0;
}

// TODO: Implement log categories
//void parseArguments(int argc, char *argv[], char **fmuFileNames, double *tEnd, double *h,
//                    int *loggingOn, char *csv_separator, int *nCategories, char **logCategories[]) {
//
//    int option = 0;
//    int i;
//
//    while ((option = getopt(argc, argv, "t:h:ls:f:")) != -1) {
//        switch(option) {
//            case 't' :
//                if (sscanf(optarg,"%lf", tEnd) != 1) {
//                    printf("error: The given stepsize (%s) is not a number\n", optarg);
//                    exit(EXIT_FAILURE);
//                }
//                break;
//            case 'h' :
//                if (sscanf(optarg,"%lf", h) != 1) {
//                    printf("error: The given stepsize (%s) is not a number\n", optarg);
//                    exit(EXIT_FAILURE);
//                }
//                break;
//            case 'l' : *loggingOn = 1;
//                break;
//            case 's' :
//                if (strlen(optarg) != 1) {
//                    printf("error: The given CSV separator char (%s) is not valid\n", optarg);
//                    exit(EXIT_FAILURE);
//                } else {
//                    switch (*optarg) {
//                        case 'c': *csv_separator = ','; break; // comma
//                        case 's': *csv_separator = ';'; break; // semicolon
//                        default:  *csv_separator = *optarg; break; // any other char
//                }
//                break;
//            }
//        }
//    }
//    // number of positional arguments (arguments without a dash)
//    int posArgs = argc - optind;
//
//    // parse FMU files
//    if (posArgs > 0) {
//        for (i = 0; i < posArgs; i++) {
//            if (strstr(argv[optind], ".fmu") || strstr(argv[optind], ".FMU")) {
//                // save fmu file path and name to array
//                fmuFileNames[i] = argv[optind];
//                // set optind to next element after current fmu file
//                optind++;
//            }
//            else {
//                break;
//            }
//        }
//    } else {
//        printf("Error: No FMU file specified.\n");
//        printHelp(argv[0]);
//        exit(EXIT_FAILURE);
//    }
//
//    *nCategories = argc - optind;
//    // parse log categories
//    if (*nCategories > 0) {
//        *logCategories = (char **)calloc(sizeof(char *), *nCategories);
//        for (i = 0; i < *nCategories; i++) {
//            (*logCategories)[i] = argv[optind];
//            optind++;
//        }
//    }
//}

void parseArguments(int argc, char *argv[], double *tEnd, double *h,
        int *loggingOn, char *csv_separator, int *nCategories, /*const*/ fmi2String *logCategories[]) {
    // parse command line arguments
    if (argc > 1) {
        if (sscanf(argv[1],"%lf", tEnd) != 1) {
            printf("error: The given end time (%s) is not a number\n", argv[1]);
            exit(EXIT_FAILURE);
        }
    }
    if (argc > 2) {
        if (sscanf(argv[2],"%lf", h) != 1) {
            printf("error: The given stepsize (%s) is not a number\n", argv[2]);
            exit(EXIT_FAILURE);
        }
    }
    if (argc > 3) {
        if (sscanf(argv[3],"%d", loggingOn) != 1 || *loggingOn < 0 || *loggingOn > 1) {
            printf("error: The given logging flag (%s) is not boolean\n", argv[3]);
            exit(EXIT_FAILURE);
        }
    }
    if (argc > 4) {
        if (strlen(argv[4]) != 1) {
            printf("error: The given CSV separator char (%s) is not valid\n", argv[4]);
            exit(EXIT_FAILURE);
        }
        switch (argv[4][0]) {
            case 'c': *csv_separator = ','; break; // comma
            case 's': *csv_separator = ';'; break; // semicolon
            default:  *csv_separator = argv[5][0]; break; // any other char
        }
    }
    if (argc > 5) {
        int i;
        *nCategories = argc - 5;
        *logCategories = (/*const*/ fmi2String *)calloc(sizeof(char *), *nCategories);
        for (i = 0; i < *nCategories; i++) {
            (*logCategories)[i] = argv[i + 5];
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
