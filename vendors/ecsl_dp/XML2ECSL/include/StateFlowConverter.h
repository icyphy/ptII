#ifndef STATEFLOWCONVERTER_H
#define STATEFLOWCONVERTER_H

#include "matlab.h"
#include "ECSL_DP.h"

#include <map>
#include <set>
#include <vector>
#include <string>

class IHistoryJunctionHandler 
{
public:
	virtual void operator()( const matlab::junction& mj)= 0;
};


class StateflowConverter
{
public:
	StateflowConverter( const ECSL_DP::System& ecsldpRootSystem) 
		: _ecsldpRootSystem( ecsldpRootSystem)
	{}
	void operator()( const matlab::machine& matlabMachine, ECSL_DP::State& ecslDPState) {
		machinePass1( matlabMachine, ecslDPState);
		machinePass2( ecslDPState);
	}

protected:
	//
	void machinePass1( const matlab::machine& matlabMachine, ECSL_DP::State& ecslDPState);
	void machinePass2( ECSL_DP::State& ecslDPState);
	//
	void createCharts( const matlab::machine& mm, ECSL_DP::State& es);
	//
	void translateEvent( const matlab::event& me, ECSL_DP::Event& ee);
	void translateData( const matlab::data& md, ECSL_DP::Data& ed);
	//
	void chartPass1( const matlab::chart& mc, ECSL_DP::State& es);
	void chartPass2( ECSL_DP::State& es);
	//
	ECSL_DP::System matchSystem( const std::string& instName) const;
	ECSL_DP::System findSubSystem( const ECSL_DP::System& rootSystem, const std::vector< std::string>& sysElemNames) const;
	//
	void translateTransition( const matlab::transition& mt, ECSL_DP::Transition& et);
	//
	void createSubStates( const std::set< matlab::state>& mss, ECSL_DP::State& es);
	void StatePass1( const matlab::state& ms, ECSL_DP::State& es);
	void StatePass2( ECSL_DP::State& es);
	//
	void setStateDecomposition( const std::set< ECSL_DP::State>& subStates, ECSL_DP::State& es);
	void mapTransitions( ECSL_DP::State& es);
	void getTransitionEndPoints( const matlab::transition& mt, ECSL_DP::TransConnector& srcConn, ECSL_DP::TransConnector& dstConn, ECSL_DP::State& es);
	void setTransitionEndPoints( ECSL_DP::Transition& et, ECSL_DP::TransConnector& srcConn, ECSL_DP::TransConnector& dstConn, ECSL_DP::State& es, int& xPos, int& yPos);
	void setTransitionEndPoints( const ECSL_DP::TransConnector& etcSrc, const ECSL_DP::TransConnector& etcDst, ECSL_DP::Transition& et);
	ECSL_DP::ConnectorRef createConnectorRef( ECSL_DP::State& parent, ECSL_DP::TransConnector& toRef, int& xPos, int& yPos);
	//
	void translateState( const matlab::state& ms, ECSL_DP::State& es);
	//
	void createStateRef( const matlab::chart& mc, ECSL_DP::State& es);
	//
	void calculateOrder( const std::set< matlab::state>& substates);
	//
	template< class T>
	void createEvents( const T& eventsParent, ECSL_DP::State& es);
	template< class T>
	void createData( const T& dataParent, ECSL_DP::State& es);
	//
	template< class T>
	void createTransitions( const T& transitionParent, ECSL_DP::State& es);
	//
	template <class T>
	void createJunctions( const T& juncParent, ECSL_DP::State& es, IHistoryJunctionHandler& hjHandler);

private:
	//
	ECSL_DP::System _ecsldpRootSystem;
	//
	typedef std::map< ECSL_DP::Transition, matlab::transition> TransitionMap_t;
	TransitionMap_t _transMap;
	//
	typedef std::map< matlab::junction, ECSL_DP::Junction> JunctionMap_t;
	JunctionMap_t _juncMap;
	// 
	typedef std::map< matlab::state, ECSL_DP::State> MatlabStateMap_t;
	MatlabStateMap_t _matlabStateMap;
	typedef std::map< ECSL_DP::State, matlab::state> EcslDPStateMap_t;
	EcslDPStateMap_t _ecsldpStateMap;
	typedef std::map< matlab::junction, ECSL_DP::History> HistoryMap_t;
	HistoryMap_t _historyMap;
};

#endif //STATEFLOWCONVERTER_H
