/*
 * maSupport.h
 *
 *  Created on: Jun 25, 2014
 *      Author: fabian
 */

#ifndef MASUPPORT_H_
#define MASUPPORT_H_

#include "fmi2.h"

typedef struct {
        FMU *fmu;
        fmi2Component *component;
        fmi2CallbackFunctions *callbacks;

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
        fmi2FMUstate **FMUstates;
        fmi2ValueReference **valueReference;
} fmiModel;


#endif /* MASUPPORT_H_ */


