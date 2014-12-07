/* -------------------------------------------------------------------------
 * sim_support.h
 * Functions used by the FMU simulations fmusim_me and fmusim_cs.
 * Copyright QTronic GmbH. All rights reserved.
 * -------------------------------------------------------------------------*/

/* See $PTII/ptolemy/actor/lib/fmi/ma/fmusdk-license.htm for the complete FMUSDK License. */

// Used 7z options, version 4.57:
// -x   Extracts files from an archive with their full paths in the current dir, or in an output dir if specified
// -aoa Overwrite All existing files without prompt
// -o   Specifies a destination directory where files are to be extracted
#define UNZIP_CMD "7z x -aoa -o"
#define XML_FILE  "modelDescription.xml"
#define RESULT_FILE "result.csv"
#define BUFSIZE 4096

#ifdef _MSC_VER
#ifdef _WIN64
#define DLL_DIR   "binaries\\win64\\"
#define DLL_DIR2   "binaries\\win64\\"
#else
#define DLL_DIR   "binaries\\win32\\"
#define DLL_DIR2   "binaries\\win32\\"
#endif /* _WIN64 */

#define DLL_SUFFIX ".dll"
#define DLL_SUFFIX2 ".dll"

#else

#if __APPLE__

// Use these for platforms other than OpenModelica
#define DLL_DIR   "binaries/darwin64/"
#define DLL_SUFFIX ".dylib"

// Use these for OpenModelica 1.8.1
#define DLL_DIR2   "binaries/darwin-x86_64/"
#define DLL_SUFFIX2 ".so"


#else /*__APPLE__*/
// Linux
#ifdef __x86_64
#define DLL_DIR   "binaries/linux64/"
#define DLL_DIR2   "binaries/linux32/"
#else
// It may be necessary to compile with -m32, see ../Makefile
#define DLL_DIR   "binaries/linux32/"
#define DLL_DIR2   "binaries/linux64/"
#endif /*__x86_64*/
#define DLL_SUFFIX ".so"
#define DLL_SUFFIX2 ".so"
#endif /*__APPLE__*/
#endif /*WINDOWS*/

#define RESOURCES_DIR "resources/"

// return codes of the 7z command line tool
#define SEVEN_ZIP_NO_ERROR 0 // success
#define SEVEN_ZIP_WARNING 1  // e.g., one or more files were locked during zip
#define SEVEN_ZIP_ERROR 2
#define SEVEN_ZIP_COMMAND_LINE_ERROR 7
#define SEVEN_ZIP_OUT_OF_MEMORY 8
#define SEVEN_ZIP_STOPPED_BY_USER 255

//void fmuLogger(fmiComponent c, FMU *fmu, fmiString instanceName, fmiStatus status, fmiString category, fmiString message, ...);
void fmuLogger(fmiComponent c, fmiString instanceName, fmiStatus status, fmiString category, fmiString message, ...);
int unzip(const char *zipPath, const char *outPath);
void parseArguments(int argc, char *argv[], char **fmuFileNames, double *tEnd, double *h,
                    int *loggingOn, char *csv_separator, int *nCategories, char **logCategories[]);
void loadFMU(FMU *fmu, const char *fmuFileName);
#ifndef _MSC_VER
typedef int boolean;
#endif
void outputRow(FMU *fmu, fmiComponent c, double time, FILE* file, char separator, boolean header);
int error(const char *message);
void printHelp(const char *fmusim);
char *getTempResourcesLocation(); // caller has to free the result
