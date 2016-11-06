/***mainStartBlock***/
#if WINDOWS
const char* fmuFileNames[NUMBER_OF_FMUS];
#else
char* fmuFileNames[NUMBER_OF_FMUS];
#endif
int i;

// parse command line arguments and load the FMU
// default arguments value
fmi2Integer requiredResolution = DEFAULT_RESOLUTION;
fmi2IntegerTime h = DEFAULT_COMM_STEP_SIZE;
int loggingOn = 0;
char csv_separator = ',';
fmi2String *categories = NULL;
int nCategories = 0;
fmi2Boolean visible = fmi2False;           // no simulator user interface

// Create and allocate arrays for FMUs and port mapping
FMU *fmus = calloc(NUMBER_OF_FMUS, sizeof(FMU));
WRAPPER *wrps = calloc(NUMBER_OF_FMUS, sizeof(WRAPPER));
portConnection* connections = calloc(NUMBER_OF_EDGES, sizeof(portConnection));

printf("-> Parsing arguments...\n");
parseArguments(argc, argv, &tEnd, &h, &loggingOn, &csv_separator, &nCategories, &categories);

/**/

/***mainEndBlock***/
// run the simulation
    printf("FMU Simulator: run '%s' from t=0..%llu with step size h=%llu, loggingOn=%d, csv separator='%c' ", MODEL_NAME, tEnd, h, loggingOn, csv_separator);
    printf("log categories={ ");
    for (i = 0; i < nCategories; i++) {
        printf("%s ", categories[i]);
    }
    printf("}\n");
	printInfo(wrps, NAMES_OF_FMUS, NUMBER_OF_FMUS);
    simulate(wrps, fmus, connections, requiredResolution, loggingOn, csv_separator);
    printf("CSV file '%s' written\n", RESULT_FILE);
    // release FMUs
    #ifdef _MSC_VER
    for (i = 0; i < NUMBER_OF_FMUS; i++) {
        FreeLibrary(fmus[i]->dllHandle);
    }
    #else
    for (i = 0; i < NUMBER_OF_FMUS; i++) {
        dlclose(fmus[i].dllHandle);
    }
    #endif
    for (i = 0; i < NUMBER_OF_FMUS; i++) {
        freeModelDescription(fmus[i].modelDescription);
    }
    if (categories) {
        free(categories);
    }
    for (int i = 0; i < NUMBER_OF_FMUS; i++) {
        free(fmuFileNames[i]);
    }
    free(fmus);
    free(wrps);
/**/

/***mainGnuplot***/
// Generate GnuPlot graph
FILE* plotfile = NULL;
    if (!(plotfile = fopen("graph.sh", "w"))) {
        printf("could not write graph.sh because:\n");
        printf("    %s\n", strerror(errno));
        return ERROR;
    }
/**/

/***mainPtplot***/
// Generate PtPlot graph
FILE* source = NULL;
FILE* sink = NULL;
if (!(source = fopen("result.csv", "r"))) {
    printf("could not write result.csv because:\n");
    printf("    %s\n", strerror(errno));
    return ERROR;
}
/**/

/***endPtplot***/
    printf("could not write plot.xml because:\n");
    printf("    %s\n", strerror(errno));
    return ERROR;
}
fprintf(sink, "<?xml version=\"1.0\" standalone=\"yes\"?>\n");
fprintf(sink, "<!DOCTYPE plot PUBLIC \"-//UC Berkeley//DTD PlotML 1//EN\"\n");
fprintf(sink, "\t\"http://ptolemy.eecs.berkeley.edu/xml/dtd/PlotML_1.dtd\">\n");
/**/
