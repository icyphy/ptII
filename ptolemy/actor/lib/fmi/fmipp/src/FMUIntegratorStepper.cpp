/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#include <iostream>
#include <cstdio>
#include <boost/numeric/odeint.hpp>

#include "FMUIntegratorStepper.h"


using namespace boost::numeric::odeint;

typedef FMUIntegrator::state_type state_type;


FMUIntegratorStepper::~FMUIntegratorStepper() {}


// Forward Euler method with constant step size.
class Euler : public FMUIntegratorStepper
{
public:

	void invokeMethod( FMUIntegrator* fmuint, state_type& states,
			   fmiReal time, fmiReal step_size, fmiReal dt )
{
		// Runge-Kutta 4 stepper.
		static euler< state_type > stepper; // Static: initialize only once.

		// Integrator function with constant step size.
		integrate_const( stepper, *fmuint, states, time, time+step_size, dt, *fmuint );
	}

	virtual IntegratorType type() const { return FMUIntegrator::eu; }
};


// 4th order Runge-Kutta method with constant step size.
class RungeKutta : public FMUIntegratorStepper
{

public:

	void invokeMethod( FMUIntegrator* fmuint, state_type& states,
			   fmiReal time, fmiReal step_size, fmiReal dt )
	{
		// Runge-Kutta 4 stepper.
		static runge_kutta4< state_type > stepper; // Static: initialize only once.

		// Integrator function with constant step size.
		integrate_const( stepper, *fmuint, states, time, time+step_size, dt, *fmuint );
	}

	virtual IntegratorType type() const { return FMUIntegrator::rk; }

};


// 5th order Runge-Kutta-Dormand-Prince method with controlled step size.
class DormandPrince : public FMUIntegratorStepper
{
public:

	void invokeMethod( FMUIntegrator* fmuint, state_type& states,
			   fmiReal time, fmiReal step_size, fmiReal dt )
	{
		// Runge-Kutta-Dormand-Prince controlled stepper.
		typedef runge_kutta_dopri5< state_type > error_stepper_type;
		typedef controlled_runge_kutta< error_stepper_type > controlled_stepper_type;
		static controlled_stepper_type stepper; // Static: initialize only once.

		// Integrator function with adaptive step size.
		integrate_adaptive( stepper, *fmuint, states, time, time+step_size, dt, *fmuint );
	}

	virtual IntegratorType type() const { return FMUIntegrator::dp; }

};


// 7th order Runge-Kutta-Fehlberg method with controlled step size.
class Fehlberg : public FMUIntegratorStepper
{
public:

	void invokeMethod( FMUIntegrator* fmuint, state_type& states,
			   fmiReal time, fmiReal step_size, fmiReal dt )
	{
		// Runge-Kutta-Fehlberg controlled stepper.
		typedef runge_kutta_fehlberg78< state_type > error_stepper_type;
		typedef controlled_runge_kutta< error_stepper_type > controlled_stepper_type;
		static controlled_stepper_type stepper; // Static: initialize only once.

		// Integrator function with adaptive step size.
		integrate_adaptive( stepper, *fmuint, states, time, time+step_size, dt, *fmuint );
	}

	virtual IntegratorType type() const { return FMUIntegrator::fe; }
};


// Bulirsch-Stoer method with controlled step size.
class BulirschStoer : public FMUIntegratorStepper
{
public:

	void invokeMethod( FMUIntegrator* fmuint, state_type& states,
			   fmiReal time, fmiReal step_size, fmiReal dt )
	{
		// Bulirsch-Stoer controlled stepper.
		typedef bulirsch_stoer< state_type > controlled_stepper_type;
		static controlled_stepper_type stepper; // Static: initialize only once.

		// Integrator function with adaptive step size.
		integrate_adaptive( stepper, *fmuint, states, time, time+step_size, dt, *fmuint );
	}

	virtual IntegratorType type() const { return FMUIntegrator::bs; }
};



// FIXME: Doesn't work properly, something with the step size?
// Adams-Bashforth-Moulton multistep method with adjustable order and adaptive step size.
class AdamsBashforthMoulton : public FMUIntegratorStepper
{
public:

	void invokeMethod( FMUIntegrator* fmuint, state_type& states,
			   fmiReal time, fmiReal step_size, fmiReal dt )
	{
		// Adams-Bashforth-Moulton stepper, first argument is the order of the method.
		adams_bashforth_moulton< 5, state_type > abm; // Static: initialize only once.

		// Initialization step for the multistep method.
		abm.initialize( *fmuint, states, time, dt );

		// Integrator function with adaptive step size.
		integrate_adaptive( abm, *fmuint, states, time, time+step_size, dt, *fmuint );
	}

	virtual IntegratorType type() const { return FMUIntegrator::abm; }
};



FMUIntegratorStepper* FMUIntegratorStepper::createStepper( IntegratorType type )
{
	switch ( type ) {
	case FMUIntegrator::eu: return new Euler;
	case FMUIntegrator::rk: return new RungeKutta;
	case FMUIntegrator::dp: return new DormandPrince;
	case FMUIntegrator::fe: return new Fehlberg;
	case FMUIntegrator::bs: return new BulirschStoer;
	case FMUIntegrator::abm: return new AdamsBashforthMoulton;
	}

	return 0;
}
