/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#ifndef _FMIPP_ROLLBACKFMU_H
#define _FMIPP_ROLLBACKFMU_H


#include "FMU.h"
#include "History.h"


/**
 *  This class allows to perform rollbacks to times not longer
 *  ago than the previous update (or a saved internal state).
 **/


class __FMI_DLL RollbackFMU : public FMU
{

public:

        RollbackFMU( const std::string& modelName );

        RollbackFMU( const std::string& fmuPath,
                     const std::string& modelName );

        RollbackFMU( const std::string& xmlPath,
                     const std::string& dllPath,
                     const std::string& modelName );

        RollbackFMU( const RollbackFMU& aRollbackFMU );

        ~RollbackFMU();

        /** Integrate internal state. **/
        virtual fmiReal integrate( fmiReal tstop, unsigned int nsteps );
        /** Integrate internal state. **/
        virtual fmiReal integrate( fmiReal tstop, double deltaT=1E-5 );

        /** Saves the current state of the FMU as internal rollback
            state. This rollback state will not be overwritten until
            "releaseRollbackState()" is called; **/
        void saveCurrentStateForRollback();
        /** Realease an internal rollback state, that was previously
            saved via "saveCurrentStateForRollback()". **/
        void releaseRollbackState();

protected:

        /** Make a rollback. **/
        fmiStatus rollback( fmiTime time );

private:

        /**  prevent calling the default constructor **/
        RollbackFMU();

        HistoryEntry rollbackState_;

        bool rollbackStateSaved_;

};


#endif // _FMIPP_ROLLBACKFMU_H
