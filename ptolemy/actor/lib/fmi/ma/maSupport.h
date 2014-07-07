/*
 * maSupport.h
 *
 *  Created on: Jun 25, 2014
 *      Author: fabian
 */

#ifndef MASUPPORT_H_
#define MASUPPORT_H_

#include "fmi.h"

typedef struct {
	FMU *fmu;
	fmiComponent *component;
	fmiCallbackFunctions *callbacks;

} fmuInstance;

typedef struct {
	double tStart;
	double tEnd;
	double stepSize;
	int loggingOn;
	char csvSeparator;
	char **categories;
	int nCategories;
} simulationSettings;

typedef struct {
	FMU **fmuInstances;
	fmiFMUstate **FMUstates;
	fmiValueReference **valueReference;
} fmiModel;


#endif /* MASUPPORT_H_ */


