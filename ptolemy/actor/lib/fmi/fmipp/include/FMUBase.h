/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#ifndef _FMIPP_FMUBASE_H
#define _FMIPP_FMUBASE_H


#include <string>

#include "FMIPPConfig.h"
#include "fmi_me.h"


/**
 * Abstract base class for wrappers handling FMUs.
 **/


class __FMI_DLL FMUBase
{

public:

        virtual ~FMUBase() {}

        /** Instantiate the FMU. **/
        virtual fmiStatus instantiate( const std::string& instanceName, fmiBoolean loggingOn ) = 0;

        /** Initialize the FMU. **/
        virtual fmiStatus initialize() = 0;

        /** Get current time. **/
        virtual fmiReal getTime() const = 0;
        /** Set current time. This affects only the value of the internal FMU time, not the internal state. **/
        virtual void setTime( fmiReal time ) = 0;
        /** Rewind current time. This affects only the value of the internal FMU time, not the internal state. **/
        virtual void rewindTime( fmiReal deltaRewindTime ) = 0;

        /** Set single value of type fmiReal, using the value reference. **/
        virtual fmiStatus setValue( fmiValueReference valref, fmiReal& val ) = 0;
        /** Set single value of type fmiInteger, using the value reference. **/
        virtual fmiStatus setValue( fmiValueReference valref, fmiInteger& val ) = 0;

        /** Set values of type fmiReal, using an array of value references. **/
        virtual fmiStatus setValue( fmiValueReference* valref, fmiReal* val, std::size_t ival ) = 0;
        /** Set values of type fmiInteger, using an array of value references. **/
        virtual fmiStatus setValue( fmiValueReference* valref, fmiInteger* val, std::size_t ival ) = 0;

        /** Set single value of type fmiReal, using the variable name. **/
        virtual fmiStatus setValue( const std::string& name,  fmiReal val ) = 0;
        /** Set single value of type fmiInteger, using the variable name. **/
        virtual fmiStatus setValue( const std::string& name,  fmiInteger val ) = 0;

        /** Get single value of type fmiReal, using the value reference. **/
        virtual fmiStatus getValue( fmiValueReference valref, fmiReal& val ) const = 0;
        /** Get single value of type fmiInteger, using the value reference. **/
        virtual fmiStatus getValue( fmiValueReference valref, fmiInteger& val ) const = 0;

        /** Get values of type fmiReal, using an array of value references. **/
        virtual fmiStatus getValue( fmiValueReference* valref, fmiReal* val, std::size_t ival ) const = 0;
        /** Get values of type fmiInteger, using an array of value references. **/
        virtual fmiStatus getValue( fmiValueReference* valref, fmiInteger* val, std::size_t ival ) const = 0;

        /** Get single value of type fmiReal, using the variable name. **/
        virtual fmiStatus getValue( const std::string& name,  fmiReal& val ) const = 0;
        /** Get single value of type fmiInteger, using the variable name. **/
        virtual fmiStatus getValue( const std::string& name,  fmiInteger& val ) const = 0;

        /** Get value reference associated to variable name. **/
        virtual fmiValueReference getValueRef( const std::string& name ) const = 0;

        /** Get continuous states. **/
        virtual fmiStatus getContinuousStates( fmiReal* val ) const = 0;
        /** Set continuous states. **/
        virtual fmiStatus setContinuousStates( const fmiReal* val ) = 0;

        /** Get derivatives. **/
        virtual fmiStatus getDerivatives( fmiReal* val ) const = 0;

        /** Get event indicators. **/
        virtual fmiStatus getEventIndicators( fmiReal* eventsind ) const = 0;

        /** Integrate internal state. **/
        virtual fmiReal integrate( fmiReal tend, unsigned int nsteps ) = 0;
        /** Integrate internal state. **/
        virtual fmiReal integrate( fmiReal tend, double deltaT ) = 0;

        /** Raise an event. **/
        virtual void raiseEvent() = 0;
        /** Handle events. **/
        virtual void handleEvents( fmiTime tstop, bool completedIntegratorStep ) = 0;

        /** Get state event flag. **/
        virtual fmiBoolean getStateEventFlag() = 0;

        /** Get number of continuous states. **/
        virtual std::size_t nStates() const = 0;
        /** Get number of event indicators. **/
        virtual std::size_t nEventInds() const = 0;
        /** Get number of value references (equals the numer of variables). **/
        virtual std::size_t nValueRefs() const = 0;

};


#endif // _FMIPP_FMUBASE_H
