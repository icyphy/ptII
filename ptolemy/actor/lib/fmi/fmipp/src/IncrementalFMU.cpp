/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#include <iostream>
#include <cassert>
#include "IncrementalFMU.h"
#include "FMU.h"


using namespace std;


IncrementalFMU::IncrementalFMU( const string& modelName )
{
	fmu_ = new FMU( modelName );
}


IncrementalFMU::IncrementalFMU( const string& fmuPath,
				const string& modelName )
{
	fmu_ = new FMU( fmuPath, modelName );
}


IncrementalFMU::IncrementalFMU( const string& xmlPath,
				const string& dllPath,
				const string& modelName )
{
	fmu_ = new FMU( xmlPath, dllPath, modelName );
}


IncrementalFMU::IncrementalFMU( const string& name,
				const string inputs[], const size_t nInputs,
				const string outputs[], const size_t nOutputs ) {
	fmu_ = new FMU( name);
	defineInputs( inputs, nInputs );
	defineOutputs( outputs, nOutputs );
}



IncrementalFMU::IncrementalFMU( const string& name,
				size_t nInputs, size_t nOutputs) {
	fmu_ = new FMU( name );
	nInputs_ = nInputs;
	nOutputs_ = nOutputs;
}


IncrementalFMU::IncrementalFMU( const IncrementalFMU& aIncrementalFMU ) {
	fmu_ = new FMU( *(aIncrementalFMU.fmu_) );
	nInputs_ = aIncrementalFMU.nInputs_;
	nOutputs_ = aIncrementalFMU.nOutputs_;
}


IncrementalFMU::~IncrementalFMU()
{
	delete fmu_;
}


void IncrementalFMU::defineInputs( const string inputs[], const size_t nInputs ) {
	nInputs_ = nInputs;
	inputRefs_ = new size_t[nInputs];
	for ( size_t i = 0; i < nInputs; ++i ) {
		inputRefs_[i] = fmu_->getValueRef( inputs[i] );
	}
}


void IncrementalFMU::defineOutputs( const string outputs[], const size_t nOutputs ) {
	nOutputs_ = nOutputs;
	outputRefs_ = new size_t[nOutputs];
	for ( size_t i = 0; i < nOutputs; ++i ) {
		outputRefs_[i] = fmu_->getValueRef( outputs[i] );
	}
}


bool IncrementalFMU::checkForEvent( const HistoryEntry& newestPrediction )
{
	return fmu_->getStateEventFlag();
}


void IncrementalFMU::handleEvent()
{
}


void IncrementalFMU::setInitialInputs( const string variableNames[], const fmiReal* values, size_t nvars )
{
	for ( size_t i = 0; i < nvars; ++i ) {
		fmu_->setValue(variableNames[i], values[i]);
	}
}


void IncrementalFMU::initializeIntegration( HistoryEntry& initialPrediction )
{
	fmiReal* initialState = initialPrediction.state_;
	fmu_->setContinuousStates(initialState);
}


void IncrementalFMU::getContinuousStates( fmiReal* state ) const
{
	fmu_->getContinuousStates( state );
}


void IncrementalFMU::getOutputs( fmiReal* outputs ) const
{
	for ( size_t i = 0; i < nOutputs_; ++i ) {
		fmu_->getValue( outputRefs_[i], outputs[i] );
	}
}


int IncrementalFMU::init( const string& instanceName,
			  const string variableNames[],
			  const fmiReal* values,
			  const size_t nvars,
			  const fmiTime startTime,
			  const fmiTime lookAheadHorizon,
			  const fmiTime lookAheadStepSize,
			  const fmiTime integratorStepSize )
{
	assert( lookAheadHorizon > 0. );
	assert( lookAheadStepSize > 0. );
	assert( integratorStepSize > 0. );

	fmiBoolean loggingOn = fmiFalse;
	fmiStatus status = fmu_->instantiate( instanceName, loggingOn );

	if ( status != fmiOK ) return 0;

	// Set inputs (has to happen before initialization of FMU).
	setInitialInputs( variableNames, values, nvars );

	// Intialize FMU.
	if ( fmu_->initialize() != fmiOK ) return 0;

	// Define the initial state: The initial state might include guesses. In such
	// cases we have to raise an event (and iterate over fmiEventUpdate) until the
	// FMU has found a solution ...

	HistoryEntry init( startTime, fmu_->nStates(), nOutputs_ );
	// incME1.fmu has numberOfContinuousStates="0"
	if (fmu_->nStates() > 0) {
	  getContinuousStates( init.state_ );
	}
	getOutputs( init.values_ );

	if (fmu_->nStates() > 0) {
	  initializeIntegration( init ); // Set values (but don't integrate afterwards) ...
	}
	fmu_->raiseEvent(); // ... then raise an event ...
	fmu_->handleEvents( startTime, false ); // ... and finally take proper actions.
	retrieveFMUState( init.state_, init.values_ ); // Then retrieve the result and ...
	predictions_.push_back( init ); // ... store as prediction -> will be used by first call to updateState().

	lookAheadHorizon_ = lookAheadHorizon;
	lookaheadStepSize_ = lookAheadStepSize;
	integratorStepSize_ = integratorStepSize;

	return 1;  /* return 1 on success, 0 on failure */
}


/* In case no look-ahead prediction is given for time t, this function is responsible to provide
 * an estimate for the corresponding state. For convenience, a REVERSE iterator pointing to the
 * next prediction available AFTER time t is handed over to the function.
 */
void IncrementalFMU::interpolateState( fmiTime t,
				       History_const_reverse_iterator& historyEntry,
				       HistoryEntry& result)
{
	const HistoryEntry& right = *(historyEntry-1);
	const HistoryEntry& left = *(historyEntry);

	for ( size_t i = 0; i < fmu_->nStates(); ++i ) {
		result.state_[i] = interpolateValue( t, left.time_, left.state_[i], right.time_, right.state_[i] );
	}

	for ( size_t i = 0; i < nOutputs_; ++i ) {
		result.values_[i] = interpolateValue( t, left.time_, left.values_[i], right.time_, right.values_[i] );
	}

	result.time_ = t;
}


/* Linear value interpolation. */
fmiReal IncrementalFMU::interpolateValue( fmiReal x, fmiReal x0, fmiReal y0, fmiReal x1, fmiReal y1 ) const
{
	return y0 + (x - x0)*(y1 - y0)/(x1 - x0);
}


fmiTime IncrementalFMU::sync( fmiTime t0, fmiTime t1 )
{
	fmiTime t_update = updateState( t0, t1 ); // Update state.

	if ( t_update != t1 ) {
		return t_update; // Return t_update in case of failure.
	}

	// Predict the future state (but make no update yet!), return time for next update.
	fmiTime t2 = predictState( t1 );
	return t2;
}


/* be very careful with this sync function, as the inputs are set for the prediction
   i.e. at the _end_ of the interval [t0, t1], before the lookahead takes place */
fmiTime IncrementalFMU::sync( fmiTime t0, fmiTime t1, fmiReal* inputs )
{
	fmiTime t_update = updateState( t0, t1 ); // Update state.

	if ( t_update != t1 ) {
		return t_update; // Return t_update in case of failure.
	}

	// set the new inputs before makeing a prediction
	setInputs( inputs );

	// Predict the future state (but make no update yet!), return time for next update.
	fmiTime t2 = predictState( t1 );

	return t2;
}


void IncrementalFMU::getState( fmiTime t, HistoryEntry& state )
{
	fmiTime oldestPredictionTime = predictions_.front().time_;
	fmiTime newestPredictionTime = predictions_.back().time_;

	// Check if time stamp t is within the range of the predictions.
	if ( ( t < oldestPredictionTime ) || ( t > newestPredictionTime ) ) {
		state.time_ = INVALID_FMI_TIME;
		return;
	}

	// If necessary, rewind the internal FMU time.
	if ( t < newestPredictionTime ) {
		// fmu_->rewindTime( newestPredictionTime - t );
		fmu_->setTime( t );
	}

	// Search the previous predictions for the state at time t. The search is
	// performed from back to front, because the last entry is hopefully the
	// correct one ...
	History_const_reverse_iterator itFind = predictions_.rbegin();
	History_const_reverse_iterator itEnd = predictions_.rend();
	for ( ; itFind != itEnd; ++itFind ) {
		if ( t == itFind->time_ ) {
			state = *itFind;
			/* should not be necessary, remove again, but have a look ;) !!!
			   if ( t < newestPredictionTime ) {
			   fmu_->setContinuousStates(state.state);
			   fmu_->rewindTime( newestPredictionTime - t );
			   }
			*/
			return;
		}
		if ( t > itFind->time_ ) {
			interpolateState(t, itFind, state);
			return;
		}
	}

	state.time_ = INVALID_FMI_TIME;
}


/* Apply the most recent prediction and make a state update. */
fmiTime IncrementalFMU::updateState( fmiTime t0, fmiTime t1 )
{
	// Get prediction for time t1.
	getState( t1, currentState_ );

	if ( INVALID_FMI_TIME == currentState_.time_ ) {
		return INVALID_FMI_TIME;
	}

	// somewhere i have to do this, ask EW which functions he overloads, so we can solve this better!!!
	// incME1.fmu has numberOfContinuousStates="0"
	if (fmu_->nStates() > 0) {
	  initializeIntegration( currentState_ );
	}
	fmu_->setTime( t1 );

	return t1;
}


/* Predict the future state but make no update yet. */
fmiTime IncrementalFMU::predictState( fmiTime t1 )
{
	lastEventTime_ = numeric_limits<fmiTime>::infinity();

	// Return if initial state is invalid.
	if ( INVALID_FMI_TIME == currentState_.time_ ) {
		return INVALID_FMI_TIME;
	}

	// Clear previous predictions.
	predictions_.clear();

	// Initialize the first state and the FMU.
	HistoryEntry prediction;

	prediction = currentState_;
	prediction.time_ = t1;

	// Retrieve the current state of the FMU, considering altered inputs.
	fmu_->handleEvents( prediction.time_, false );
	retrieveFMUState( prediction.state_, prediction.values_ );

	// Initialize integration.
	if (fmu_->nStates() > 0) {
	  initializeIntegration( prediction );
	}

	// Set the initial prediction.
	predictions_.push_back( prediction );

	// Make predictions ...
	fmiTime horizon = t1 + lookAheadHorizon_;
	while ( prediction.time_ < horizon ) {

		// if used with other version of FMU.h, remove "prediction.time +"
		// Integration step.
		lastEventTime_ = fmu_->integrate( prediction.time_ + lookaheadStepSize_, integratorStepSize_ );

		// Retrieve results from FMU integration.
		retrieveFMUState( prediction.state_, prediction.values_ );

		// Add latest prediction.
		prediction.time_ += lookaheadStepSize_;

		predictions_.push_back( prediction );

		if ( lastEventTime_ >= prediction.time_ ) {
			fmu_->setStateEventFlag( fmiFalse );
		}

		// Check if an event has occured.
		// interpolation for the events or something better than just stopping and integration
		// until the end of the step after which the event has occurred would be nice !!!
		if ( checkForEvent( prediction ) ) {
			handleEvent();

			// "handleEvent()" might alter the last prediction stored
			// in vector "predictions_"  --> use reference instead of
			// loop variable "prediction"!
			HistoryEntry& lastPrediction = predictions_.back();

			// this has to be changed if the event is detected precisely
			// and is not just within the last step !!!
			lastPrediction.time_ = lastEventTime_ + integratorStepSize_;

			fmu_->handleEvents( lastPrediction.time_, false );

			retrieveFMUState( lastPrediction.state_, lastPrediction.values_ );

			return lastPrediction.time_;
		}
	}

	//if ((0 == lookAheadHorizon_) && (prediction.time > horizon)) return horizon;

	return prediction.time_;
}


void IncrementalFMU::retrieveFMUState( fmiReal* result, fmiReal* values ) const
{
        if (fmu_->nStates() > 0) {
	    fmu_->getContinuousStates(result);
        }
	for ( size_t i = 0; i < nOutputs_; ++i ) {
		fmu_->getValue(outputRefs_[i], values[i]);
	}
}


fmiStatus IncrementalFMU::setInputs(fmiReal* inputs) const {

	fmiStatus status = fmiOK;

	for ( size_t i = 0; i < nInputs_; ++i ) {
		if ( fmiOK != fmu_->setValue(inputRefs_[i], inputs[i]) ) status = fmiError;
	}

	return status;
}
