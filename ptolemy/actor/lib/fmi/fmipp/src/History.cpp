/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#include "History.h"


HistoryEntry::HistoryEntry()
{
	time_ = INVALID_FMI_TIME;
	nstates_ = 0;
	nvalues_ = 0;
	state_ = NULL;
	values_ = NULL;
}


HistoryEntry::HistoryEntry( std::size_t nStates, std::size_t nValues )
{
	time_ = INVALID_FMI_TIME;
	nstates_ = nStates;
	nvalues_ = nValues;
	state_ = nStates ? new fmiReal[nStates] : NULL;
	values_ = nValues ? new fmiReal[nValues] : NULL;
}


HistoryEntry::HistoryEntry( const fmiTime& t, std::size_t nStates, std::size_t nValues )
{
	time_ = t;
	nstates_ = nStates;
	nvalues_ = nValues;
	state_ = nStates ? new fmiReal[nStates] : NULL;
	values_ = nValues ? new fmiReal[nValues] : NULL;
}


HistoryEntry::HistoryEntry( const fmiTime& t, fmiReal* s, std::size_t nStates, fmiReal* v, std::size_t nValues )
{
	time_ = t;
	nstates_ = nStates;
	nvalues_ = nValues;
	state_ = nstates_ ? new fmiReal[nstates_] : NULL;
	for ( std::size_t i = 0; i < nStates; ++i ) {
		state_[i] = s[i];
	}
	values_ = nvalues_ ? new fmiReal[nvalues_] : NULL;
	for ( std::size_t i = 0; i < nValues; ++i ) {
		values_[i] = v[i];
	}
}


HistoryEntry::HistoryEntry( const HistoryEntry& aHistoryEntry )
{
	time_ = aHistoryEntry.time_;
	nstates_ = aHistoryEntry.nstates_;
	nvalues_ = aHistoryEntry.nvalues_;
	state_ = nstates_ ? new fmiReal[nstates_] : NULL;
	values_ = nvalues_ ? new fmiReal[nvalues_] : NULL;
	for ( std::size_t i = 0; i < nstates_; ++i ) {
		state_[i] = aHistoryEntry.state_[i];
	}
	for ( std::size_t i = 0; i < nvalues_; ++i ) {
		values_[i] = aHistoryEntry.values_[i];
	}
}


HistoryEntry& HistoryEntry::operator=( HistoryEntry aHistoryEntry )
{
	time_ = aHistoryEntry.time_;
	if ( nstates_ != aHistoryEntry.nstates_ ) {
		nstates_ = aHistoryEntry.nstates_;
		delete [] state_;
		state_ = nstates_ ? new fmiReal[nstates_] : NULL;
	}
	for ( std::size_t i = 0; i < nstates_; ++i ) {
		state_[i] = aHistoryEntry.state_[i];
	}

	if ( nvalues_ != aHistoryEntry.nvalues_ ) {
		nvalues_ = aHistoryEntry.nvalues_;
		delete [] values_;
		values_ = nvalues_ ? new fmiReal[nvalues_] : NULL;
	}
	for ( std::size_t i = 0; i < nvalues_; ++i ) {
		values_[i] = aHistoryEntry.values_[i];
	}

	return *this;
}
