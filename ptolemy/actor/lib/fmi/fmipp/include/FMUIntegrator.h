/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#ifndef _FMIPP_FMUINTEGRATOR_H
#define _FMIPP_FMUINTEGRATOR_H


#include <vector>

#include "FMUBase.h"

class FMUIntegratorStepper;


class FMUIntegrator
{

public:

        /** Enum IntegratorType defines the integration method:
         *   - eu: Forward Euler method.
         *   - rk: 4th order Runge-Kutta method with constant step size.
         *   - dp: 5th order Runge-Kutta-Dormand-Prince method with controlled step size.
         *   - fe: 7th order Runge-Kutta-Fehlberg method with controlled step size.
         *   - bs: Bulirsch-Stoer method with controlled step size.
         *   - abm: Adams-Bashforth-Moulton multistep method with adjustable order and adaptive
         *          step size. FIXME: Doesn't work properly, something with the step size?
         **/
        enum IntegratorType { eu, rk, dp, fe, bs, abm };

        typedef std::vector<fmiReal> state_type;

        /** Constructor. **/
        FMUIntegrator( FMUBase* fmu, IntegratorType type = dp );

        /** Copy constructor. **/
        FMUIntegrator( const FMUIntegrator& );

        /** Destructor. **/
        ~FMUIntegrator();

        /** Return the integration algorithm type (i.e. the stepper type). **/
        IntegratorType type() const;

        /** Integrate FMU state. **/
        void integrate( fmiReal step_size, fmiReal dt );

        /** Evaluates the right hand side of the ODE. **/
        void operator()( const state_type& x, state_type& dx, fmiReal time );

        /** ODEINT solvers call observer function with two parameters after each succesfull step. **/
        void operator()( const state_type& state, fmiReal time );

        /** Clone this instance of FMUIntegrator (not a copy). **/
        FMUIntegrator* clone() const;

private:

        // Pointer to FMU.
        FMUBase* fmu_;

        // The stepper implements the actual integration method.
        FMUIntegratorStepper* stepper_;

        // Is this just a copy of another instance of FMUIntegrator? -> See destructor.
        bool is_copy_;

};


#endif // _FMIPP_FMUINTEGRATOR_H
