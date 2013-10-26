/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#ifndef _FMIPP_INCREMENTALFMU_H
#define _FMIPP_INCREMENTALFMU_H


#include <string>
#include "FMIPPConfig.h"
#include "fmiModelTypes.h"
#include "History.h"



class FMU;


class __FMI_DLL IncrementalFMU
{

public:

        IncrementalFMU( const std::string& modelName );

        IncrementalFMU( const std::string& fmuPath,
                        const std::string& modelName );

        IncrementalFMU( const std::string& xmlPath,
                        const std::string& dllPath,
                        const std::string& modelName );


        IncrementalFMU( const std::string& name,
                        const std::string inputs[],
                        const std::size_t nInputs,
                        const std::string outputs[],
                        const std::size_t nOutputs);

        IncrementalFMU( const std::string& name,
                        const std::size_t nInputs,
                        const std::size_t nOutputs );

        IncrementalFMU( const IncrementalFMU& aIncrementalFMU );

        ~IncrementalFMU();

        /** Initialize the FMU. **/
        int init( const std::string& instanceName,
                  const std::string variableNames[],
                  const fmiReal* values,
                  const std::size_t nvars,
                  const fmiTime startTime,
                  const fmiTime looakaheadhorizon,
                  const fmiTime lookaheadstepsize,
                  const fmiTime integratorstepsize );

        /** Define inputs of the FMU. **/
        void defineInputs( const std::string inputs[],
                           const std::size_t nInputs );

        /** Define outputs of the FMU. **/
        void defineOutputs( const std::string outputs[],
                            const std::size_t nOutputs );

        /** Get pointer to current state. **/
        fmiReal* getCurrentState() const { return currentState_.state_; }

        /** Get pointer to current outputs. **/
        fmiReal* getCurrentOutputs() const { return currentState_.values_; }

        /** Simulate FMU from time t0 until t1. **/
        fmiTime sync( fmiTime t0, fmiTime t1 );

        /** Simulate FMU from time t0 until t1. **/
        fmiTime sync( fmiTime t0, fmiTime t1, fmiReal* inputs );


protected:

        typedef History::History History;
        typedef History::const_iterator History_const_iterator;
        typedef History::iterator       History_iterator;
        typedef History::const_reverse_iterator History_const_reverse_iterator;
        typedef History::reverse_iterator       History_reverse_iterator;

        /** Vector of state predictions. **/
        History predictions_;

        /** Check the latest prediction if an event has occured. If so, update the latest prediction accordingly. **/
        virtual bool checkForEvent( const HistoryEntry& newestPrediction );

        /** Called in case checkForEvent() returns true. **/
        virtual void handleEvent();

        /** Set initial values for integration (i.e. for each look-ahead). **/
        virtual void initializeIntegration( HistoryEntry& initialPrediction );

        /** Define the initial inputs of the FMU (input states before initialization). **/
        void setInitialInputs(const std::string variableNames[], const fmiReal* values, std::size_t nvars);

        /** Get the continuous state of the FMU. **/
        void getContinuousStates( fmiReal* state ) const;

        /** Set the inputs of the FMU. **/
        fmiStatus setInputs(fmiReal* inputs) const;

        /** Get the inputs of the FMU. **/
        void getOutputs( fmiReal* outputs ) const;

        /** In case no look-ahead prediction is given for time t, this function is responsible to provide
         *  an estimate for the corresponding state. For convenience, a REVERSE iterator pointing to the
         *  next prediction available AFTER time t is handed over to the function.
         **/
        void interpolateState(fmiTime t, History_const_reverse_iterator& historyEntry, HistoryEntry& state);

        /** Helper function: linear value interpolation. **/
        double interpolateValue( fmiReal x, fmiReal x0, fmiReal y0, fmiReal x1, fmiReal y1 ) const;

private:

        /** Interface to the FMU. **/
        FMU* fmu_;

        /** The current state. **/
        HistoryEntry currentState_;

        /** Names of the inputs. **/
        std::size_t* inputRefs_;

        /** Number of inputs. **/
        std::size_t nInputs_;

        /** Names of the outputs. **/
        std::size_t* outputRefs_;

        /** Number of outputs. **/
        std::size_t nOutputs_;

        /** Look-ahead horizon. **/
        fmiTime lookAheadHorizon_;

        /** Look-ahead step size. **/
        fmiTime lookaheadStepSize_;

        /** Intergrator step size. **/
        fmiTime integratorStepSize_;

        /** Time the last event occurred **/
        fmiTime lastEventTime_;

        /** Protect default constructor. **/
        IncrementalFMU() {}

        /** Compute state at time t from previous state predictions. **/
        void getState(fmiTime t, HistoryEntry& state);

        /** Update state at time t1, i.e. change the actual state using previous prediction(s). **/
        fmiTime updateState( fmiTime t0, fmiTime t1 );

        /** Compute state predictions. **/
        fmiTime predictState( fmiTime t1 );

        /** Retrieve values after each integration step from FMU. **/
        void retrieveFMUState( fmiReal* result, fmiReal* values ) const;

};


#endif // _FMIPP_INCREMENTALFMU_H
