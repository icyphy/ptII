#ifndef MATLAB2ECSLDP_H
#define MATLAB2ECSLDP_H

#include <string>
#include "UdmBase.h"
#include "matlab.h"
#include "ECSL_DP.h"

// do not inherit from this class without setting the destructor virtual.
class Matlab2EcslDP
{
public:
	// ctor & dtor
	Matlab2EcslDP( const std::string& matlabFileName, const std::string& ecslDPFileName);
	~Matlab2EcslDP();
	// do the conversion
	void operator()();

protected:
	//
	ECSL_DP::System convertDataflow();
	void convertStateflow( const ECSL_DP::System& ecsldpRootSystem);
	//
	matlab::System getMatlabSimulinkSystem() const;
	matlab::machine  getMatlabStateFlowMachine() const;
	ECSL_DP::System createDataflowSystem( const std::string& name);
	ECSL_DP::State createStateflowState( const std::string& name);

private:
	//
	Udm::SmartDataNetwork _dnMatlab;
	Udm::SmartDataNetwork _dnEcslDP;
};

#endif //MATLAB2ECSLDP_H
