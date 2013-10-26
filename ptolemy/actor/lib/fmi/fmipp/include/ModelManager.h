/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 * --------------------------------------------------------------*/

#ifndef _FMIPP_MODELMANAGER_H
#define _FMIPP_MODELMANAGER_H


#include <string>
#include <map>

#include "fmi_me.h"


class ModelManager {

public:

        ~ModelManager();

        /** Get singleton instance of model manager. **/
        static ModelManager& getModelManager();

        /** Get model (from standard unzipped FMU). **/
        static FMU_functions* getModel( const std::string& fmuPath,
                                        const std::string& modelName );

        /** Get model (from non-standard 'modelName.xml' and 'modelName.dll'). **/
        static FMU_functions* getModel( const std::string& xmlPath,
                                        const std::string& dllPath,
                                        const std::string& modelName );

private:

        /** Private constructor (singleton). **/
        ModelManager() {}

        /** Helper function for loading FMU shared library **/
        static int loadDll( std::string dllPath, FMU_functions* fmuFun );

        /** Helper function for loading FMU shared library **/
        static void* getAdr( int* s, FMU_functions *fmuFun, const char* functionName );

        /** Pointer to singleton instance. **/
        static ModelManager* modelManager_;

        /** Define container for description collection. **/
        typedef std::map<std::string, FMU_functions*> Descriptions;

        /** Collection of descriptions. **/
        Descriptions modelDescriptions_;
};


#endif // _FMIPP_MODELMANAGER_H
