/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#ifdef FMI_DEBUG
#include <iostream>
#endif

#include <stdio.h>
#include <stdarg.h>
#include <cassert>
#include <limits>

#include "RollbackFMU.h"


using namespace std;


RollbackFMU::RollbackFMU( const string& modelName ) :
	FMU( modelName ),
	rollbackState_( getTime(), nStates(), 0 ),
	rollbackStateSaved_( false )
{
#ifdef FMI_DEBUG
	cout << "[RollbackFMU::ctor] MODEL_IDENTIFIER = " << modelName.c_str() << endl; fflush( stdout );
#endif
}


RollbackFMU::RollbackFMU( const string& fmuPath,
			  const string& modelName ) :
	FMU( fmuPath, modelName ),
	rollbackState_( getTime(), nStates(), 0 ),
	rollbackStateSaved_( false )
{
#ifdef FMI_DEBUG
	cout << "[RollbackFMU::ctor] MODEL_IDENTIFIER = " << modelName.c_str() << endl; fflush( stdout );
#endif
}

RollbackFMU::RollbackFMU( const string& xmlPath,
			  const string& dllPath,
			  const string& modelName ) :
	FMU( xmlPath, dllPath, modelName ),
	rollbackState_( getTime(), nStates(), 0 ),
	rollbackStateSaved_( false )
{
#ifdef FMI_DEBUG
	cout << "[RollbackFMU::ctor] MODEL_IDENTIFIER = " << modelName.c_str() << endl; fflush( stdout );
#endif
}


RollbackFMU::RollbackFMU( const RollbackFMU& aRollbackFMU ) :
	FMU( aRollbackFMU ),
	rollbackState_( aRollbackFMU.rollbackState_ ),
	rollbackStateSaved_( false )
{
#ifdef FMI_DEBUG
	cout << "[RollbackFMU::ctor]" << endl; fflush( stdout );
#endif
}


RollbackFMU::~RollbackFMU() {}


fmiReal RollbackFMU::integrate( fmiReal tstop, unsigned int nsteps )
{
#ifdef FMI_DEBUG
	cout << "[RollbackFMU::integrate]" << endl; fflush( stdout );
#endif
	fmiTime now = getTime();

	if ( tstop < now ) { // Make a rollback.
		if ( fmiOK != rollback( tstop ) ) return now;
	} else if ( false == rollbackStateSaved_ ) { // Retrieve current state and store it as rollback state.
		rollbackState_.time_ = now;
		if ( 0 != nStates() ) getContinuousStates( rollbackState_.state_ );
	}

	// Integrate.
	assert( nsteps > 0 );
	double deltaT = ( tstop - getTime() ) / nsteps;
	return FMU::integrate( tstop, deltaT );
}


fmiReal RollbackFMU::integrate( fmiReal tstop, double deltaT )
{
#ifdef FMI_DEBUG
	cout << "[RollbackFMU::integrate]" << endl; fflush( stdout );
#endif
	fmiTime now = getTime();

	if ( tstop < now ) { // Make a rollback.
		if ( fmiOK != rollback( tstop ) ) return now;
	} else if ( false == rollbackStateSaved_ ) { // Retrieve current state and store it as rollback state.
		rollbackState_.time_ = now;
		if ( 0 != nStates() ) getContinuousStates( rollbackState_.state_ );
	}

	// Integrate.
	return FMU::integrate( tstop, deltaT );
}


/** Saves the current state of the FMU as internal rollback
    state. This rollback state will not be overwritten until
    "releaseRollbackState()" is called; **/
void RollbackFMU::saveCurrentStateForRollback()
{
	if ( false == rollbackStateSaved_ ) {
		rollbackState_.time_ = getTime();
		if ( 0 != nStates() ) getContinuousStates( rollbackState_.state_ );

#ifdef FMI_DEBUG
		cout << "[RollbackFMU::saveCurrentStateForRollback] saved state at time = " << rollbackState_.time_ << endl; fflush( stdout );
#endif
		rollbackStateSaved_ = true;
	}
}


/** Realease an internal rollback state, that was previously
    saved via "saveCurrentStateForRollback()". **/
void RollbackFMU::releaseRollbackState()
{
	rollbackStateSaved_ = false;
}


fmiStatus RollbackFMU::rollback( fmiTime time )
{
#ifdef FMI_DEBUG
	cout << "[RollbackFMU::rollback]" << endl; fflush( stdout );
#endif
	if ( time < rollbackState_.time_ ) {
#ifdef FMI_DEBUG
		cout << "[RollbackFMU::rollback] FAILED. requested time = " << time
		     << " - rollback state time = " << rollbackState_.time_ << endl; fflush( stdout );
#endif
		return fmiFatal;
	}

	setTime( rollbackState_.time_ );
	raiseEvent();
	handleEvents( rollbackState_.time_, false );

	if ( 0 != nStates() ) {
		setContinuousStates( rollbackState_.state_ );
		raiseEvent();
	}

	handleEvents( rollbackState_.time_, true );

	return fmiOK;
}
