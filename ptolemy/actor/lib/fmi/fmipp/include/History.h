/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#ifndef _FMIPP_HISTORY_H
#define _FMIPP_HISTORY_H


#include <vector>
#include <string>
#include <limits>

#include "fmiModelTypes.h"

/**
 * Helpers used internally by IncrementalFMU to manage predictions.
 **/

class HistoryEntry
{

public:

        HistoryEntry();
        HistoryEntry( std::size_t nStates, std::size_t nValues );
        HistoryEntry( const fmiTime& t, std::size_t nStates, std::size_t nValues );
        HistoryEntry( const fmiTime& t, fmiReal* s, std::size_t nStates, fmiReal* v, std::size_t nValues );
        HistoryEntry( const HistoryEntry& aHistoryEntry );

        ~HistoryEntry() { delete [] state_; delete [] values_; }

        HistoryEntry& operator=( HistoryEntry aHistoryEntry );

        fmiTime time_;
        std::size_t nstates_;
        std::size_t nvalues_;
        fmiReal* state_;
        fmiReal* values_;
};


namespace History
{
        typedef std::vector< HistoryEntry > History;
        typedef std::vector< HistoryEntry >::const_iterator const_iterator;
        typedef std::vector< HistoryEntry >::iterator iterator;
        typedef std::vector< HistoryEntry >::const_reverse_iterator const_reverse_iterator;
        typedef std::vector< HistoryEntry >::reverse_iterator reverse_iterator;
};


#endif // _FMIPP_HISTORY_H
