/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#ifndef _FMIPP_FMU_H
#define _FMIPP_FMU_H


#include <map>

#include "FMUBase.h"


class FMUIntegrator;


/**
 *  The FMI standard requires to define the macro MODEL_IDENTIFIER for each
 *  type of FMU seperately. This is not done here, because this class links
 *  dynamically during run-time.
 */


class __FMI_DLL FMU : public FMUBase
{

public:

        FMU( const std::string& modelName );

        FMU( const std::string& fmuPath,
             const std::string& modelName );

        FMU( const std::string& xmlPath,
             const std::string& dllPath,
             const std::string& modelName );

        FMU( const FMU& aFMU );

        ~FMU();

        /** Instantiate the FMU. **/
        fmiStatus instantiate( const std::string& instanceName,
                               fmiBoolean loggingOn = fmiFalse );

        /** Initialize the FMU. **/
        virtual fmiStatus initialize();

        /** Get current time. **/
        virtual fmiReal getTime() const;
        /** Set current time. This affects only the value of the internal FMU time, not the internal state. **/
        virtual void setTime( fmiReal time );
        /** Rewind current time. This affects only the value of the internal FMU time, not the internal state. **/
        virtual void rewindTime( fmiReal deltaRewindTime );

        /** Set single value of type fmiReal, using the value reference. **/
        virtual fmiStatus setValue( fmiValueReference valref, fmiReal& val );
        /** Set single value of type fmiInteger, using the value reference. **/
        virtual fmiStatus setValue( fmiValueReference valref, fmiInteger& val );

        /** Set values of type fmiReal, using an array of value references. **/
        virtual fmiStatus setValue( fmiValueReference* valref, fmiReal* val, std::size_t ival );
        /** Set values of type fmiInteger, using an array of value references. **/
        virtual fmiStatus setValue( fmiValueReference* valref, fmiInteger* val, std::size_t ival );

        /** Set single value of type fmiReal, using the variable name. **/
        virtual fmiStatus setValue( const std::string& name,  fmiReal val );
        /** Set single value of type fmiInteger, using the variable name. **/
        virtual fmiStatus setValue( const std::string& name,  fmiInteger val );

        /** Get single value of type fmiReal, using the value reference. **/
        virtual fmiStatus getValue( fmiValueReference valref, fmiReal& val ) const;
        /** Get single value of type fmiInteger, using the value reference. **/
        virtual fmiStatus getValue( fmiValueReference valref, fmiInteger& val ) const;

        /** Get values of type fmiReal, using an array of value references. **/
        virtual fmiStatus getValue( fmiValueReference* valref, fmiReal* val, std::size_t ival ) const;
        /** Get values of type fmiInteger, using an array of value references. **/
        virtual fmiStatus getValue( fmiValueReference* valref, fmiInteger* val, std::size_t ival ) const;

        /** Get single value of type fmiReal, using the variable name. **/
        virtual fmiStatus getValue( const std::string& name,  fmiReal& val ) const;
        /** Get single value of type fmiInteger, using the variable name. **/
        virtual fmiStatus getValue( const std::string& name,  fmiInteger& val ) const;

        /** Get value reference associated to variable name. **/
        virtual fmiValueReference getValueRef( const std::string& name ) const;

        /** Get continuous states. **/
        virtual fmiStatus getContinuousStates( fmiReal* val ) const;
        /** Set continuous states. **/
        virtual fmiStatus setContinuousStates( const fmiReal* val );

        /** Get derivatives. **/
        virtual fmiStatus getDerivatives( fmiReal* val ) const;

        /** Get event indicators. **/
        virtual fmiStatus getEventIndicators( fmiReal* eventsind ) const;

        /** Integrate internal state. **/
        virtual fmiReal integrate( fmiReal tend, unsigned int nsteps );
        /** Integrate internal state. **/
        virtual fmiReal integrate( fmiReal tend, double deltaT = 1e-5 );

        /** Raise an event. **/
        virtual void raiseEvent();
        /** Handle events. **/
        virtual void handleEvents( fmiTime tstop, bool completedIntegratorStep );

        /** Get number of continuous states. **/
        virtual std::size_t nStates() const;
        /** Get number of event indicators. **/
        virtual std::size_t nEventInds() const;
        /** Get number of value references (equals the numer of variables). **/
        virtual std::size_t nValueRefs() const;

        /** Get state event flag. **/
        fmiBoolean getStateEventFlag();
        /** Set state event flag. **/
        void setStateEventFlag( fmiBoolean flag );

        /** Send message to FMU logger. **/
        void logger( fmiStatus status, const std::string& msg ) const;
        /** Send message to FMU logger. **/
        void logger( fmiStatus status, const char* msg ) const;

        /** Logger function handed to the internal FMU instance. **/
        static void logger( fmiComponent m, fmiString instanceName,
                            fmiStatus status, fmiString category,
                            fmiString message, ... );

private:


        FMU(); // Prevent calling the default constructor.

        std::string instanceName_;

        fmiComponent instance_; // Internal FMU instance.

        FMU_functions *fmuFun_; // Internal pointer to FMU functions.

        FMUIntegrator* integrator_; // Integrator instance.

        std::size_t nStateVars_; // Number of state variables.
        std::size_t nEventInds_; // Number of event indivators.
        std::size_t nValueRefs_; // Number of value references.

        // Maps variable names and value references.
        std::map<std::string,fmiValueReference> varMap_;

        fmiReal time_;
        fmiReal tnextevent_;
        fmiReal lastEventTime_;

        fmiEventInfo* eventinfo_;
        fmiReal*      eventsind_;
        fmiReal*      preeventsind_;

        fmiBoolean callEventUpdate_;
        fmiBoolean stateEvent_;
        fmiBoolean timeEvent_;
        fmiBoolean raisedEvent_;
        fmiBoolean stateEventFlag_;

        void readModelDescription();

        static const unsigned int maxEventIterations_ = 5;

};

#endif // _FMIPP_FMU_H
