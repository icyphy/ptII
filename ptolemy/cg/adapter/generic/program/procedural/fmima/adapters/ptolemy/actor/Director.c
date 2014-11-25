/***mainStartBlock***/
#if WINDOWS
    const char* fmuFileNames[NUMBER_OF_FMUS];
#else
    char* fmuFileNames[NUMBER_OF_FMUS];
#endif
    int i;

    // parse command line arguments and load the FMU
    // default arguments value
    double h = 0.1;
    int loggingOn = 0;
    char csv_separator = ',';
    char **categories = NULL;
    int nCategories = 0;
    fmi2Boolean visible = fmi2False;           // no simulator user interface

    // Create and allocate arrays for FMUs and port mapping
    FMU *fmus = calloc(NUMBER_OF_FMUS, sizeof(FMU));
    portConnection* connections = calloc(NUMBER_OF_EDGES, sizeof(portConnection));

    printf("Parsing arguments!\n");
    parseArguments(argc, argv, fmuFileNames, &tEnd, &h, &loggingOn, &csv_separator, &nCategories, &categories);

    // Set up port connections
    //setupConnections(fmus, connections);

/**/

/***mainEndBlock***/
    // run the simulation
    printf("FMU Simulator: run '%s' from t=0..%g with step size h=%g, loggingOn=%d, csv separator='%c' ",
            fmuFileNames[0], tEnd, h, loggingOn, csv_separator); // TODO: Should mention all FMUs
    printf("log categories={ ");
    for (i = 0; i < nCategories; i++) {
            printf("%s ", categories[i]);
    }
    printf("}\n");

    simulate(fmus, connections, h, loggingOn, csv_separator); // TODO: Create experiment settings struct

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

    free(fmus);

    return EXIT_SUCCESS;
/**/
