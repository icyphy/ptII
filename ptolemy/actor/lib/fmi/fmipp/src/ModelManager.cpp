/* --------------------------------------------------------------
 * Copyright (c) 2013, AIT Austrian Institute of Technology GmbH.
 * All rights reserved. See file FMIPP_LICENSE for details.
 *
 * Except functions 'loadDll' and 'getAdr', adapted from FmuSdk:
 *
 * Copyright 2010 QTronic GmbH. All rights reserved.
 *
 * The FmuSdk is licensed by the copyright holder under the BSD License
 * (http://www.opensource.org/licenses/bsd-license.html):
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY QTRONIC GMBH "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL QTRONIC GMBH BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * --------------------------------------------------------------*/

#if defined(_MSC_VER)
#define _CRT_SECURE_NO_WARNINGS
#endif

#define BUFSIZE 4096

#include <stdio.h>
#include <iostream>
#include <fstream>
#include "FMIPPConfig.h"
#include "ModelManager.h"


ModelManager* ModelManager::modelManager_ = 0;


ModelManager::~ModelManager()
{
	Descriptions::iterator begin= modelDescriptions_.begin();
	Descriptions::iterator end = modelDescriptions_.end();
	for ( Descriptions::iterator it = begin; it != end; ++it ) {
#if defined(MINGW)
		FreeLibrary( static_cast<HMODULE>( it->second->dllHandle ) );
#elif defined(_MSC_VER)
		FreeLibrary( static_cast<HMODULE>( it->second->dllHandle ) );
#else
		dlclose( it->second->dllHandle );
#endif

		freeElement( it->second->modelDescription );

		delete it->second;
	}
}


ModelManager& ModelManager::getModelManager()
{
	// Singleton instance
	static ModelManager modelManagerInstance;
	if ( 0 == modelManager_ ) {
		modelManager_ = &modelManagerInstance;
	}
	return *modelManager_;
}


FMU_functions* ModelManager::getModel( const std::string& fmuPath,
				       const std::string& modelName )
{
	// Description already available?
	Descriptions::iterator itFind = modelManager_->modelDescriptions_.find( modelName );
	if ( itFind != modelManager_->modelDescriptions_.end() ) { // Model name found in list of descriptions.
		return itFind->second;
	}

	// fix this for other OSs and 32bit !!!
#if defined(_MSC_VER)
	std::string dllPath = fmuPath + "/binaries/win32/" + modelName + ".dll";
#elif defined(MINGW)
	std::string dllPath = fmuPath +  "/binaries/win32/" + modelName + ".dll";
#elif defined(__APPLE__)
	std::string dllPath = fmuPath +  "/binaries/darwin-x86_64/" + modelName + ".so";
        // darwin-x86_64 is used by OpenModelica.  The standard is to use darwin64.
        std::ifstream file(dllPath.c_str());
        if (!file) {
            dllPath = fmuPath + "/binaries/darwin64/" + modelName + ".dylib";
        }
#else
	std::string dllPath = fmuPath + "/binaries/linux64/" + modelName + ".so";
#endif

	FMU_functions* description = new FMU_functions;

	std::string descriptionPath = fmuPath + "/modelDescription.xml";
	description->modelDescription = parse( descriptionPath.c_str() );

	if (loadDll( dllPath, description ) == 0) {
#ifdef FMI_DEBUG
	std::cout << "ModelManager GetModel, returning null."<< std::endl; fflush (stdout);
#endif
	  return NULL;
	}

	modelManager_->modelDescriptions_[modelName] = description;
	return description;
}


FMU_functions* ModelManager::getModel( const std::string& xmlPath,
				       const std::string& dllPath,
				       const std::string& modelName )
{
	// Description already available?
	Descriptions::iterator itFind = modelManager_->modelDescriptions_.find( modelName );
	if ( itFind != modelManager_->modelDescriptions_.end() ) { // Model name found in list of descriptions.
		return itFind->second;
	}

	//std::string fullDllPath = dllPath + "/" + modelName + ".dll";
	std::string fullDllPath = dllPath;

	FMU_functions* description = new FMU_functions;

	//std::string descriptionPath = xmlPath + "/" + modelName + ".xml";
	std::string descriptionPath = xmlPath;

	description->modelDescription = parse( descriptionPath.c_str() );

	if (loadDll( fullDllPath, description ) == 0) {
#ifdef FMI_DEBUG
	  std::cout << "ModelManager GetModel, returning null."<< std::endl; fflush (stdout);
#endif
	  return NULL;
	}

	modelManager_->modelDescriptions_[modelName] = description;
#ifdef FMI_DEBUG
	std::cout << "ModelManager GetModel, returning description: " << description << std::endl; fflush (stdout);
#endif
	return description;
}



// Load the given dll and set function pointers in fmu
// Return 0 to indicate failure
int ModelManager::loadDll( std::string dllPath, FMU_functions* fmuFun )
{
	int s = 1;
#ifdef FMI_COSIMULATION
	int x = 1;
#endif

#if defined(MINGW)
	HANDLE h = LoadLibrary( dllPath.c_str() );
#elif defined(_MSC_VER)
	HANDLE h = LoadLibrary( dllPath.c_str() );
#else
	HANDLE h = dlopen( dllPath.c_str(), RTLD_LAZY );
#endif

#ifdef FMI_DEBUG
	std::cout << "ModelManager() start:" << dllPath << std::endl; fflush(stdout);
#endif
	if ( !h ) {
		printf( "ERROR: Could not load %s\n", dllPath.c_str() ); fflush(stdout);
		return 0; // failure
	}

	fmuFun->dllHandle = h;

#ifdef FMI_COSIMULATION
	fmuFun->getTypesPlatform        = (fGetTypesPlatform)   getAdr( &s, fmuFun, "fmiGetTypesPlatform" );
	if ( s == 0 ) {
		s = 1; // work around bug for FMUs exported using Dymola 2012 and SimulationX 3.x
		fmuFun->getTypesPlatform    = (fGetTypesPlatform)   getAdr( &s, fmuFun, "fmiGetModelTypesPlatform" );
		if ( s == 1 ) { printf( "  using fmiGetModelTypesPlatform instead\n" ); fflush( stdout ); }
	}
	fmuFun->instantiateSlave        = (fInstantiateSlave)   getAdr( &s, fmuFun, "fmiInstantiateSlave" );
	fmuFun->initializeSlave         = (fInitializeSlave)    getAdr( &s, fmuFun, "fmiInitializeSlave" );
	fmuFun->terminateSlave          = (fTerminateSlave)     getAdr( &s, fmuFun, "fmiTerminateSlave" );
	fmuFun->resetSlave              = (fResetSlave)         getAdr( &s, fmuFun, "fmiResetSlave" );
	fmuFun->freeSlaveInstance       = (fFreeSlaveInstance)  getAdr( &s, fmuFun, "fmiFreeSlaveInstance" );
	fmuFun->setRealInputDerivatives = (fSetRealInputDerivatives) getAdr( &s, fmuFun, "fmiSetRealInputDerivatives" );
	fmuFun->getRealOutputDerivatives = (fGetRealOutputDerivatives) getAdr( &s, fmuFun, "fmiGetRealOutputDerivatives" );
	fmuFun->cancelStep              = (fCancelStep)         getAdr( &s, fmuFun, "fmiCancelStep" );
	fmuFun->doStep                  = (fDoStep)             getAdr( &s, fmuFun, "fmiDoStep" );
	// SimulationX 3.4 and 3.5 do not yet export getStatus and getXStatus: do not count this as failure here
	fmuFun->getStatus               = (fGetStatus)          getAdr( &x, fmuFun, "fmiGetStatus" );
	fmuFun->getRealStatus           = (fGetRealStatus)      getAdr( &x, fmuFun, "fmiGetRealStatus" );
	fmuFun->getIntegerStatus        = (fGetIntegerStatus)   getAdr( &x, fmuFun, "fmiGetIntegerStatus" );
	fmuFun->getBooleanStatus        = (fGetBooleanStatus)   getAdr( &x, fmuFun, "fmiGetBooleanStatus" );
	fmuFun->getStringStatus         = (fGetStringStatus)    getAdr( &x, fmuFun, "fmiGetStringStatus" );

#else // FMI for Model Exchange 1.0
#ifdef FMI_DEBUG
	std::cout << "ModelManager Model Exchange 1.0, s:" << s << std::endl; fflush (stdout);
#endif
	fmuFun->getModelTypesPlatform   = (fGetModelTypesPlatform) getAdr( &s, fmuFun, "fmiGetModelTypesPlatform" );
	fmuFun->instantiateModel        = (fInstantiateModel)   getAdr( &s, fmuFun, "fmiInstantiateModel" );
	fmuFun->freeModelInstance       = (fFreeModelInstance)  getAdr( &s, fmuFun, "fmiFreeModelInstance" );
	fmuFun->setTime                 = (fSetTime)            getAdr( &s, fmuFun, "fmiSetTime" );
	fmuFun->setContinuousStates     = (fSetContinuousStates)getAdr( &s, fmuFun, "fmiSetContinuousStates" );
	fmuFun->completedIntegratorStep = (fCompletedIntegratorStep)getAdr( &s, fmuFun, "fmiCompletedIntegratorStep" );
	fmuFun->initialize              = (fInitialize)         getAdr( &s, fmuFun, "fmiInitialize" );
	fmuFun->getDerivatives          = (fGetDerivatives)     getAdr( &s, fmuFun, "fmiGetDerivatives" );
	fmuFun->getEventIndicators      = (fGetEventIndicators) getAdr( &s, fmuFun, "fmiGetEventIndicators" );
	fmuFun->eventUpdate             = (fEventUpdate)        getAdr( &s, fmuFun, "fmiEventUpdate" );
	fmuFun->getContinuousStates     = (fGetContinuousStates)getAdr( &s, fmuFun, "fmiGetContinuousStates" );
	fmuFun->getNominalContinuousStates = (fGetNominalContinuousStates)getAdr( &s, fmuFun, "fmiGetNominalContinuousStates" );
	fmuFun->getStateValueReferences = (fGetStateValueReferences)getAdr( &s, fmuFun, "fmiGetStateValueReferences" );
	fmuFun->terminate               = (fTerminate)          getAdr( &s, fmuFun, "fmiTerminate" );
#endif
#ifdef FMI_DEBUG
	std::cout << "ModelManager Get Version s:" << s << std::endl; fflush (stdout);
#endif

	fmuFun->getVersion              = (fGetVersion)         getAdr( &s, fmuFun, "fmiGetVersion" );
	fmuFun->setDebugLogging         = (fSetDebugLogging)    getAdr( &s, fmuFun, "fmiSetDebugLogging" );
	fmuFun->setReal                 = (fSetReal)            getAdr( &s, fmuFun, "fmiSetReal" );
	fmuFun->setInteger              = (fSetInteger)         getAdr( &s, fmuFun, "fmiSetInteger" );
	fmuFun->setBoolean              = (fSetBoolean)         getAdr( &s, fmuFun, "fmiSetBoolean" );
	fmuFun->setString               = (fSetString)          getAdr( &s, fmuFun, "fmiSetString" );
	fmuFun->getReal                 = (fGetReal)            getAdr( &s, fmuFun, "fmiGetReal" );
	fmuFun->getInteger              = (fGetInteger)         getAdr( &s, fmuFun, "fmiGetInteger" );
	fmuFun->getBoolean              = (fGetBoolean)         getAdr( &s, fmuFun, "fmiGetBoolean" );
	fmuFun->getString               = (fGetString)          getAdr( &s, fmuFun, "fmiGetString" );
#ifdef FMI_DEBUG
	std::cout << "ModelManager end s:" << s << std::endl; fflush (stdout);
#endif
	return s;
}


void* ModelManager::getAdr( int* s, FMU_functions *fmuFun, const char* functionName )
{
	char name[BUFSIZE];
	void* fp;
	sprintf( name, "%s_%s", getModelIdentifier( fmuFun->modelDescription ), functionName );

#if defined(MINGW)
	fp = reinterpret_cast<void*>( GetProcAddress( static_cast<HMODULE>( fmuFun->dllHandle ), name ) );
#elif defined(_MSC_VER)
	fp = reinterpret_cast<void*>( GetProcAddress( static_cast<HMODULE>( fmuFun->dllHandle ), name ) );
#else
	fp = dlsym( fmuFun->dllHandle, name );
#endif

	if ( !fp ) {
		printf ( "WARNING: Function %s not found.\n", name ); fflush( stdout );
		*s = 0; // mark dll load as 'failed'
	}

	return fp;
}
