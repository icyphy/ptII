/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#include <iostream>
#include <cstdio>
#include <cassert>
#include "FMUIntegrator.h"
#include "FMUIntegratorStepper.h"

using namespace std;


FMUIntegrator::FMUIntegrator( FMUBase* fmu, IntegratorType type ) :
	fmu_( fmu ),
	stepper_( FMUIntegratorStepper::createStepper( type ) ),
	is_copy_( false )
{
	assert( 0 != stepper_ );
}


FMUIntegrator::FMUIntegrator( const FMUIntegrator& other ) :
	fmu_( other.fmu_ ),
	stepper_( other.stepper_ ),
	is_copy_( true )
{}


FMUIntegrator::~FMUIntegrator()
{
	// Internally, ODEINT makes copies of this class (when using
	// boost::fusion::for_each). This copies can however all use
	// the same stepper. Only when the destructor of the original
	// instance is called, the stepper is deleted.
	if ( false == is_copy_ ) delete stepper_;
}


FMUIntegrator::IntegratorType
FMUIntegrator::type() const
{
	return stepper_->type();
}


void
FMUIntegrator::operator()( const state_type& x, state_type& dx, fmiReal time )
{
	// if there has been an event, then the integrator shall do nothing
	if ( ! fmu_->getStateEventFlag() ) {
		// Update to current time.
		fmu_->setTime( time );

		// Update to current states.
		fmu_->setContinuousStates( &x.front() );

		// Evaluate derivatives and store them to vector dx.
		fmu_->getDerivatives( &dx.front() );
	}
}


void
FMUIntegrator::operator()( const state_type& state, fmiReal time )
{
	// if there has been an event, then the integrator shall do nothing
	if ( ! fmu_->getStateEventFlag() ) {
		// set new state of FMU after each step
		fmu_->setContinuousStates( &state.front() );

		// Call "fmiCompletedIntegratorStep" and handle events.
		fmu_->handleEvents( fmu_->getTime(), true );
	}
}


void
FMUIntegrator::integrate( fmiReal step_size, fmiReal dt )
{
	// This vector holds (temporarily) the values of the FMU's continuous states.
	static state_type states( fmu_->nStates() );

	// Get current continuous states.
	fmu_->getContinuousStates( &states.front() );

	// Invoke integration method.
  	stepper_->invokeMethod( this, states, fmu_->getTime(), step_size, dt );
}


FMUIntegrator* FMUIntegrator::clone() const
{
	return new FMUIntegrator( this->fmu_, this->type() );
}
