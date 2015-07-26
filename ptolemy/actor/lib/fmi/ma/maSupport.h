/*
@Copyright (c) 2014-2015 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


*/
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


