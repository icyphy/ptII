/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#ifndef _FMIPP_FMUINTEGRATORSTEPPER_H
#define _FMIPP_FMUINTEGRATORSTEPPER_H


#include "FMUIntegrator.h"

/**
 * The actual integration methods are implemented by integrator steppers.
 **/


class FMUIntegratorStepper
{

public:

        virtual ~FMUIntegratorStepper();

        typedef FMUIntegrator::IntegratorType IntegratorType;

        /** Invokes integration method. **/
        virtual void invokeMethod( FMUIntegrator* fmuint, FMUIntegrator::state_type& states,
                                   fmiReal time, fmiReal step_size, fmiReal dt ) = 0;

        /** Returns the integrator type. **/
        virtual IntegratorType type() const = 0;

        /** Factory: creates a new integrator stepper. **/
        static FMUIntegratorStepper* createStepper( IntegratorType type );
};


#endif // _FMIPP_FMUINTEGRATORSTEPPER_H
