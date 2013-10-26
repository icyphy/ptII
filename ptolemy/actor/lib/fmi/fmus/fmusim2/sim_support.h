/* -------------------------------------------------------------------------
 * sim_support.h
 * Functions used by the FMU simulatios fmusim_me and fmusim_cs.
 * Copyright 2011 QTronic GmbH. All rights reserved.
 * -------------------------------------------------------------------------*/

// Used 7z options, version 4.57:
// -x   Extracts files from an archive with their full paths in the current dir, or in an output dir if specified
// -aoa Overwrite All existing files without prompt
// -o   Specifies a destination directory where files are to be extracted
#define UNZIP_CMD "7z x -aoa -o"

#define XML_FILE  "modelDescription.xml"

#if WINDOWS
#define DLL_DIR   "binaries\\win32\\"
#define DLL_SUFFIX ".dll"
#else
#if __APPLE__
#define DLL_DIR   "binaries/darwin64/"
#define DLL_SUFFIX ".dylib"
#else /*__APPLE__*/
// Linux
#ifdef __x86_64
#define DLL_DIR   "binaries/linux64/"
#else
#define DLL_DIR   "binaries/linux32/"
#endif /*__x86_64*/
#define DLL_SUFFIX ".so"
#endif /*__APPLE__*/
#endif /*WINDOWS*/

#define RESULT_FILE "result.csv"
#define BUFSIZE 4096

// return codes of the 7z command line tool
#define SEVEN_ZIP_NO_ERROR 0 // success
#define SEVEN_ZIP_WARNING 1  // e.g., one or more files were locked during zip
#define SEVEN_ZIP_ERROR 2
#define SEVEN_ZIP_COMMAND_LINE_ERROR 7
#define SEVEN_ZIP_OUT_OF_MEMORY 8
#define SEVEN_ZIP_STOPPED_BY_USER 255

void fmuLogger(fmiComponent c, fmiString instanceName, fmiStatus status, fmiString category, fmiString message, ...);
int unzip(const char *zipPath, const char *outPath);
void parseArguments(int argc, char *argv[], char** fmuFileName, double* tEnd, double* h, int* loggingOn, char* csv_separator);
void loadFMU(const char* fmuFileName);
// Windows wants boolean to be an unsigned char.
typedef unsigned char boolean;
void outputRow(FMU *fmu, fmiComponent c, double time, FILE* file, char separator, boolean header);
int error(const char* message);
void printHelp(const char* fmusim);
