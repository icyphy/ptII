#include "Matlab2EcslDP.h"
#include "utils.h"
#include "StateflowConverter.h"
#include "DataflowConverter.h"

Matlab2EcslDP::Matlab2EcslDP( const std::string& matlabFileName, const std::string& ecslDPFileName)
: _dnMatlab( matlab::diagram), _dnEcslDP( ECSL_DP::diagram)
{
	std::cerr << "Matlab2EcslDP(): " << matlabFileName << " " << ecslDPFileName << std::endl;
	// open and create the source and target data networks.
	_dnMatlab.OpenExisting( matlabFileName, "Matlab.XSD", Udm::CHANGES_LOST_DEFAULT);
    std::cerr << "Matlab2EcslDP(): About to call _dnEcslDP.CreateNew()" << std::endl;
	_dnEcslDP.CreateNew( ecslDPFileName, UseXSD()( ecslDPFileName) ? "ECSL_DP.XSD" : "ECSL_DP", ECSL_DP::RootFolder::meta, Udm::CHANGES_PERSIST_ALWAYS);
}

Matlab2EcslDP::~Matlab2EcslDP()
{
	// close data networks.
	_dnMatlab.CloseNoUpdate();
	_dnEcslDP.CloseWithUpdate();
}

void Matlab2EcslDP::operator()()
{
	std::cerr << "Matlab2EcslDP::operator()" << std::endl;
	ECSL_DP::System root= convertDataflow();
	convertStateflow( root);
}

ECSL_DP::System Matlab2EcslDP::convertDataflow()
{
	std::cerr << "Matlab2EcslDP::convertDataflow()" << std::endl;
	matlab::System ms= getMatlabSimulinkSystem();
	ECSL_DP::System es= createDataflowSystem( ms.Name());
	DataflowConverter convDF;
	convDF( ms, es);
	return es;
}

void Matlab2EcslDP::convertStateflow( const ECSL_DP::System& ecsldpRootSystem)
{
	std::cerr << "Matlab2EcslDP::convertStateflow()" << std::endl;
	matlab::machine mm= getMatlabStateFlowMachine();
	if ( mm)
	{
		ECSL_DP::State est= createStateflowState( mm.name());
		StateflowConverter convSF( ecsldpRootSystem);
		convSF( mm, est);
	}
}

matlab::System Matlab2EcslDP::getMatlabSimulinkSystem() const
{
	matlab::Simulink root = matlab::Simulink::Cast( _dnMatlab.GetRootObject());
	matlab::SimContainer sc = root.simContainer();
	matlab::System system = sc.system();
	return system;
}

matlab::machine Matlab2EcslDP::getMatlabStateFlowMachine() const
{
	matlab::machine machine;
	matlab::Simulink root = matlab::Simulink::Cast( _dnMatlab.GetRootObject());
	matlab::Stateflow rootSF = root.stateflow();
	if(rootSF) {
		machine = rootSF.machine();
	}
	return machine;
}

ECSL_DP::System Matlab2EcslDP::createDataflowSystem( const std::string& name)
{
	ECSL_DP::RootFolder root= ECSL_DP::RootFolder::Cast( _dnEcslDP.GetRootObject());
	ECSL_DP::Dataflow dataflow= ECSL_DP::Dataflow::Create( root);
	dataflow.name()= "Dataflow";
	ECSL_DP::System system = ECSL_DP::System::Create( dataflow);
	system.Name() = name;
	system.name() = mangle( name);
	return system;
}

ECSL_DP::State Matlab2EcslDP::createStateflowState( const std::string& name)
{
	ECSL_DP::RootFolder root= ECSL_DP::RootFolder::Cast( _dnEcslDP.GetRootObject());
	ECSL_DP::Stateflow stateflow = ECSL_DP::Stateflow::Create( root);
	stateflow.name() = "Stateflow";
	ECSL_DP::State machineState = ECSL_DP::State::Create( stateflow);
	machineState.name() = mangle( name);
	return machineState;
}
