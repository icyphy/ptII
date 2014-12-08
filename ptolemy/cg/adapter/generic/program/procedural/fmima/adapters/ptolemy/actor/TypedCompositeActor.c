/***variableDeclareBlock***/
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <stdbool.h>
#include "fmi2.h"
#include "sim_support.h"

double tEnd = 1.0;
double tStart = 0;

/**/

/***staticDeclareBlock***/

static fmi2Component initializeFMU(FMU *fmu, fmi2Boolean visible,
		fmi2Boolean loggingOn, int nCategories, char ** categories) {

	fmi2Status fmi2Flag;                     // return code of the fmu functions

	// instantiate the fmu
	fmu->callbacks.logger = fmuLogger;
	fmu->callbacks.allocateMemory = calloc;
	fmu->callbacks.freeMemory = free;
	fmu->callbacks.stepFinished = NULL; // fmi2DoStep has to be carried out synchronously
	fmu->callbacks.componentEnvironment = fmu; // pointer to current fmu from the environment.

	fmi2Real tolerance = 0;                 // used in setting up the experiment
	fmi2Boolean toleranceDefined = fmi2False; // true if model description define tolerance
	ValueStatus vs = valueIllegal;

	// handle to the parsed XML file
	ModelDescription* md = fmu->modelDescription;
	// global unique id of the fmu
	const char *guid = getAttributeValue((Element *) md, att_guid);
	// check for ability to get and set state

	fmu->canGetAndSetFMUstate = getAttributeBool((Element*) getCoSimulation(md),
			att_canGetAndSetFMUstate, &vs);
	fmu->canGetMaxStepSize = getAttributeBool((Element*) getCoSimulation(md),
			att_canGetMaxStepSize, &vs);
	// instance name
	const char *instanceName = getAttributeValue(
			(Element *) getCoSimulation(md), att_modelIdentifier);
	// path to the fmu resources as URL, "file://C:\QTronic\sales"
	char *fmuResourceLocation = getTempResourcesLocation(); // TODO: returns crap. got to save the location for every FMU somehow.
	// instance of the fmu
	fmi2Component comp = fmu->instantiate(instanceName, fmi2CoSimulation, guid,
			fmuResourceLocation, &fmu->callbacks, visible, loggingOn);
	printf("instance name: %s, \nguid: %s, \nressourceLocation: %s\n",
			instanceName, guid, fmuResourceLocation);
	free(fmuResourceLocation);

	if (!comp) {
		printf("Could not initialize model with guid: %s\n", guid);
		return NULL;
	}

	Element *defaultExp = getDefaultExperiment(fmu->modelDescription);
	if (defaultExp) {
		tolerance = getAttributeDouble(defaultExp, att_tolerance, &vs);
	}
	if (vs == valueDefined) {
		toleranceDefined = fmi2True;
	}

	if (nCategories > 0) {
		fmi2Flag = fmu->setDebugLogging(comp, fmi2True, nCategories,
				(const fmi2String*) categories);
		if (fmi2Flag > fmi2Warning) {
			error("could not initialize model; failed FMI set debug logging");
			return NULL;
		}
	}

	fmi2Flag = fmu->setupExperiment(comp, toleranceDefined, tolerance, tStart,
	fmi2True, tEnd);
	if (fmi2Flag > fmi2Warning) {
		error("could not initialize model; failed FMI setup experiment");
		return NULL;
	}
	fmi2Flag = fmu->enterInitializationMode(comp);
	if (fmi2Flag > fmi2Warning) {
		error("could not initialize model; failed FMI enter initialization mode");
		return NULL;
	}
	printf("initialization mode entered\n");
	fmi2Flag = fmu->exitInitializationMode(comp);
	printf("successfully initialized.\n");

	if (fmi2Flag > fmi2Warning) {
		error("could not initialize model; failed FMI exit initialization mode");
		return NULL;
	}

	return comp;
}

static fmi2Status checkForLegacyFMUs(FMU* fmus, bool *isLegacyFmu, int*legacyFmuIndex) {

	printf("Checking for legacy FMUs!\n");
	int legacyFmus = 0;

	for (int i = 0; i < NUMBER_OF_FMUS; i++) {
		if (!fmus[i].canGetAndSetFMUstate) {
			legacyFmus++;
			*legacyFmuIndex = i;
			*isLegacyFmu = true;
			if (legacyFmus > 1) {
				printf("More than one legacy FMU detected. The system cannot be simulated.\n");
				return fmi2Error;
			}
		}
	}

	return fmi2OK;
}

static fmi2Status setValue(portConnection* connection) {
	fmi2Status fmi2Flag;
	fmi2Integer tempInt;
	fmi2Real tempReal;
	fmi2Boolean tempBoolean;
	fmi2String tempString;

	// get source value and cast if necessary
	switch (connection->sourceType) {
	case fmi2_Integer:
		fmi2Flag = connection->sourceFMU->getInteger(connection->sourceFMU->component, &connection->sourcePort, 1, &tempInt);
		tempReal = (fmi2Real) tempInt;
		tempBoolean = (tempInt == 0 ? fmi2False : fmi2True);
		break;
	case fmi2_Real:
		fmi2Flag = connection->sourceFMU->getReal(connection->sourceFMU->component, &connection->sourcePort, 1, &tempReal);
		tempInt = (fmi2Integer) round(tempReal);
		tempBoolean = (tempReal == 0.0 ? fmi2False : fmi2True);
		break;
	case fmi2_Boolean:
		fmi2Flag = connection->sourceFMU->getBoolean(connection->sourceFMU->component, &connection->sourcePort, 1, &tempBoolean);
		tempInt = (fmi2Integer) tempBoolean;
		tempReal = (fmi2Real) tempBoolean;
		break;
	case fmi2_String:
		fmi2Flag = connection->sourceFMU->getString(connection->sourceFMU->component, &connection->sourcePort, 1, &tempString);
		break;
	default:
		return fmi2Error;
	}

	if (fmi2Flag > fmi2Warning) {
		printf("Getting the value of an FMU caused an error.");
		return fmi2Flag;
	}

	if (connection->sourceType != connection->sinkType && (connection->sinkType == fmi2_String || connection->sourceType == fmi2_String)) {
		printf("A connection of FMUs has incompatible data types. Terminating simulation.\n");
		return fmi2Error;
	}

	// set sink value
	switch (connection->sinkType) {
	case fmi2_Integer:
		fmi2Flag = connection->sinkFMU->setInteger(connection->sinkFMU->component, &connection->sinkPort, 1, &tempInt);
		break;
	case fmi2_Real:
		fmi2Flag = connection->sinkFMU->setReal(connection->sinkFMU->component, &connection->sinkPort, 1, &tempReal);
		break;
	case fmi2_Boolean:
		fmi2Flag = connection->sinkFMU->setBoolean(connection->sinkFMU->component, &connection->sinkPort, 1, &tempBoolean);
		break;
	case fmi2_String:
		fmi2Flag = connection->sinkFMU->setString(connection->sinkFMU->component, &connection->sinkPort, 1,	&tempString);
		break;
	default:
		return fmi2Error;
	}
	return fmi2Flag;
}

void terminateSimulation(FMU *fmus, int returnValue, FILE* file, double h,
		int nSteps) {

	const char* STATUS[] = { "fmi2OK", "fmi2Warning", "fmi2Discard",
			"fmi2Error", "fmi2Fatal", "fmi2Pending" };

	for (int i = 0; i < NUMBER_OF_FMUS; i++) {
		fmus[i].terminate(fmus[i].component);
		if (fmus[i].lastFMUstate != NULL) {
			fmi2Status status = fmus[i].freeFMUstate(fmus[i].component,
					&fmus[i].lastFMUstate);
			printf("Terminating with status: %s\n", STATUS[status]);
		}

		fmus[i].freeInstance(fmus[i].component);
	}

	// print simulation summary
	if (returnValue == 1) {
		printf("Simulation from %g to %g terminated successful\n", tStart,
				tEnd);
	} else {
		printf("Simulation from %g to %g terminated early!\n", tStart, tEnd);
	}
	printf("  steps ............ %d\n", nSteps);
	printf("  fixed step size .. %g\n", h);
	fclose(file);

	return;
}

static fmi2Status rollbackFMUs(FMU *fmus) {
	for (int i = 0; i < NUMBER_OF_FMUS; i++) {
		if (fmus[i].canGetAndSetFMUstate && !fmus[i].canGetMaxStepSize) {
			fmi2Status localStatus = fmus[i].setFMUstate(fmus[i].component, fmus[i].lastFMUstate);
			if (localStatus > fmi2Warning) {
				printf("Rolling back FMU %s failed!\n", NAMES_OF_FMUS[i]);
				return localStatus;
			}
		}
	}
	return fmi2OK;
}

// simulate the given FMUs from tStart = 0 to tEnd.
static int simulate(FMU *fmus, portConnection* connections, double h,
		fmi2Boolean loggingOn, char separator) {

	// Simulation variables
	fmi2Real time = tStart;
	fmi2Real stepSize = h;
	int nSteps = 0;
	FILE* file;
	int legacyFmuIndex = 0;
	bool isLegacyFmu = false;

	// Open result file
	if (!(file = fopen(RESULT_FILE, "w"))) {
		printf("could not write %s because:\n", RESULT_FILE);
		printf("    %s\n", strerror(errno));
		return 0;
	}

	// Check for legacy FMUs
	if (checkForLegacyFMUs(fmus, &isLegacyFmu, &legacyFmuIndex) > fmi2Warning) {
		terminateSimulation(fmus, 0, file, h, nSteps);
		return 0;
	}

	// output solution for time t0
	outputRow(fmus, NUMBER_OF_FMUS, NAMES_OF_FMUS, time, file, separator, TRUE);
	outputRow(fmus, NUMBER_OF_FMUS, NAMES_OF_FMUS, time, file, separator, FALSE);

	// Simulation loop
	while (time < tEnd) {

		int *fmusToStep = NULL;
		int numberOfFmusToStep = 0;

		// Set connection values
		for (int i = 0; i < NUMBER_OF_EDGES; i++) {
			setValue(&connections[i]);
		}

		// Compute the maximum step size
		// (I) Predictable FMUs
		for (int i = 0; i < NUMBER_OF_FMUS; i++) {
			if (fmus[i].canGetMaxStepSize) {
				fmi2Real maxStepSize;
				fmi2Status currentStatus = fmus[i].getMaxStepSize(fmus[i].component, &maxStepSize);
				if (currentStatus > fmi2Warning) {
					printf("Could get the MaxStepSize: getMaxStepSize returned fmuStatus > Discard for FMU, %s\n",
							NAMES_OF_FMUS[i]);
					free (fmusToStep);
					terminateSimulation(fmus, 0, file, h, nSteps);
					return 0;
				}
				numberOfFmusToStep++;
				int* tmpFmusToStep = NULL;
				tmpFmusToStep = (int*) realloc(fmusToStep, numberOfFmusToStep * sizeof(int));
				if (tmpFmusToStep != NULL) {
					fmusToStep = tmpFmusToStep;
					fmusToStep[numberOfFmusToStep-1] = i;
				} else {
					printf("Error while storing index of an FMU to Step");
					free (fmusToStep);
					terminateSimulation(fmus, 0, file, h, nSteps);
					return 0;
				}
				stepSize = min(stepSize, maxStepSize);
			}
		}

		// Compute the maximum step size
		// (II) Rollback FMUs
		for (int i = 0; i < NUMBER_OF_FMUS; i++) {
			if (fmus[i].canGetAndSetFMUstate && !fmus[i].canGetMaxStepSize) {

				fmi2Real maxStepSize;
				fmi2Status currentStatus = fmus[i].getFMUstate(
						fmus[i].component, &fmus[i].lastFMUstate);
				if (currentStatus > fmi2Warning) {
					printf("Saving state of FMU (%s) failed. Terminating simulation.", NAMES_OF_FMUS[i]);
					free (fmusToStep);
					terminateSimulation(fmus, 0, file, h, nSteps);
					return 0;
				}
				currentStatus = fmus[i].doStep(fmus[i].component, time,
						stepSize, fmi2False);
				if (currentStatus > fmi2Discard) {
					printf("Could not complete simulation of the model. doStep returned fmuStatus > Discard!\n");
					free (fmusToStep);
					terminateSimulation(fmus, 0, file, h, nSteps);
					return 0;
				}
				fmi2Real lastSuccessfulTime;
				fmus[i].getRealStatus(fmus[i].component, fmi2LastSuccessfulTime, &lastSuccessfulTime);
				maxStepSize = lastSuccessfulTime - time;
				if (maxStepSize < stepSize) {
					numberOfFmusToStep++;
					int* tmpFmusToStep = NULL;
					tmpFmusToStep = (int*) realloc(fmusToStep, numberOfFmusToStep * sizeof(int));
					if (tmpFmusToStep != NULL) {
						fmusToStep = tmpFmusToStep;
						fmusToStep[numberOfFmusToStep-1] = i;
					} else {
						printf("Error while storing index of an FMU to Step");
						free (fmusToStep);
						terminateSimulation(fmus, 0, file, h, nSteps);
						return 0;
					}
				}

				stepSize = min(stepSize, maxStepSize);
			}
		}

		// Compute the maximum step size
		// (III) Legacy FMUs
		if (isLegacyFmu) {
			fmi2Real maxStepSize;
			fmi2Status currentStatus = fmus[legacyFmuIndex].doStep(fmus[legacyFmuIndex].component, time,
					stepSize, fmi2False);
			if (currentStatus > fmi2Discard) {
				printf("Could not complete simulation of the model. doStep returned fmuStatus > Discard!\n");
				free (fmusToStep);
				terminateSimulation(fmus, 0, file, h, nSteps);
				return 0;
			}
			fmi2Real lastSuccessfulTime;
			currentStatus = fmus[legacyFmuIndex].getRealStatus(fmus[legacyFmuIndex].component, fmi2LastSuccessfulTime, &lastSuccessfulTime);
			if (currentStatus > fmi2Discard) {
				printf("Could not complete simulation of the model. doStep returned fmuStatus > Discard!\n");
				free (fmusToStep);
				terminateSimulation(fmus, 0, file, h, nSteps);
				return 0;
			}
			maxStepSize = lastSuccessfulTime - time;
			stepSize = min(stepSize, maxStepSize);
		}

		// Rolling back FMUs of type (II)
		{
			fmi2Status currentStatus = rollbackFMUs(fmus);
			if (currentStatus > fmi2Discard) {
				printf("Rolling back of FMUs failed. Terminating simulation.");
				free (fmusToStep);
				terminateSimulation(fmus, 0, file, h, nSteps);
				return 0;
			}
		}

		// Perform doStep() for all FMUs with the discovered stepSize
		for (int i = 0; i < numberOfFmusToStep; i++) {
			int idx = fmusToStep[i];
			fmi2Status currentStatus = fmus[idx].doStep(fmus[idx].component, time,
					stepSize, fmi2False);
			if (currentStatus > fmi2Discard) {
				printf("Could not complete simulation of the model. doStep returned fmuStatus > Discard!\n");
				free (fmusToStep);
				terminateSimulation(fmus, 0, file, h, nSteps);
				return 0;
			}
		}

		free (fmusToStep);
		time += stepSize;
		outputRow(fmus, NUMBER_OF_FMUS, NAMES_OF_FMUS, time, file, separator,FALSE);
		nSteps++;
	}

	terminateSimulation(fmus, 1, file, h, nSteps);
	return 1;
}

/**/
