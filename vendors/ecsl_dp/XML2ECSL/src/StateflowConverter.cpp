#include "StateflowConverter.h"
#include <algorithm>	// for unique()
#include <UdmStatic.h>	// for UdmStatic
#include <UmlExt.h>	// for IsDerivedFrom()
#include "Utils.h"

const int EVENT_X_POSITION= 75;
const int EVENT_Y_INIT_POSITION= 100;
const int EVENT_Y_OFFET= 100;

const int DATA_X_POSITION= 175;
const int DATA_Y_INIT_POSITION= 100;
const int DATA_Y_OFFSET= 100;

const int CHART_X_INIT_POSITION= 100;
const int CHART_Y_INIT_POSITION= 100;
const int CHART_X_OFFSET= 200;
const int CHART_Y_OFFSET= 200;

const int STATEREF_X_INIT_POSITION= 10;
const int STATEREF_Y_INIT_POSITION= 10;

const int TRANSSTART_X_INIT_POSITION= 175;
const int TRANSSTART_Y_INIT_POSITION= 50;

const int CONNREF_X_INIT_POSITION= 275;
const int CONNREF_X_OFFSET= 100;
const int CONNREF_Y_INIT_POSITION= 50;

const std::string HISTORY_JUNCTION= "HISTORY_JUNCTION";
const std::string CONNECTIVE_JUNCTION= "CONNECTIVE_JUNCTION";
const std::string SFUNCTION= " SFunction ";
const std::string AND_STATE= "AND_STATE";
const std::string OR_STATE= "OR_STATE";
const std::string FUNC_STATE= "FUNC_STATE";
const std::string GROUP_STATE= "GROUP_STATE";

//////////////////////////////////////////////////////////////////////////////
template <class MT, class ET>
void SetPosition(const MT& mt, ET& et) 
{
	std::string loc = mt.position();
	int x=0, y= 0;
	ParseMatlabPosString( loc, "[", " ", " ", x, y);
	x*= GME_X_SCALE_FACTOR/2;
	x+= GME_X_OFFSET;
	y*= GME_Y_SCALE_FACTOR/2;
	y+= GME_Y_OFFSET;
	et.position() = MakeGMEPosString( x, y);
}

//////////////////////////////////////////////////////////////////////////////
class JunctErrorHandler : public IHistoryJunctionHandler
{
public:
	virtual void operator()( const matlab::junction& mj)
	{
		Udm::Object parent= mj.parent();
		if ( Uml::IsDerivedFrom( parent.type(), matlab::chart::meta)) {
			std::string chartName = matlab::chart::Cast( parent).name();
			std::cerr << "Error - History found in chart: " << chartName << std::endl;
		}
		ASSERT( false);
	}
};

class JunctCreateHandler : public IHistoryJunctionHandler
{
public:
	JunctCreateHandler( ECSL_DP::State& es, std::map< matlab::junction, ECSL_DP::History>& historyMap) : _es( es), _histMap( historyMap)
	{}
	virtual void operator()( const matlab::junction& mj)
	{
		ECSL_DP::History eh = ECSL_DP::History::Create( _es);
		eh.name() = "History";
		_histMap[ mj] =eh;
		SetPosition( mj, eh);
	}
private:
	ECSL_DP::State& _es;
	std::map< matlab::junction, ECSL_DP::History>& _histMap;
};

//////////////////////////////////////////////////////////////////////////////
void StateflowConverter::machinePass1( const matlab::machine& matlabMachine, ECSL_DP::State& ecslDPState)
{
	createEvents( matlabMachine, ecslDPState);
	createData( matlabMachine, ecslDPState);
	createCharts( matlabMachine, ecslDPState);
	// !!VIZA 0-level state: 'ecslDPState'. The root state
	//ecslDPState.Order= num_to_string( 0);
}

void StateflowConverter::machinePass2( ECSL_DP::State& ecslDPState)
{
	ecslDPState.Decomposition() = "AND_STATE";
	std::set< ECSL_DP::State> chartStates = ecslDPState.State_kind_children();
	for( std::set< ECSL_DP::State>::iterator i= chartStates.begin(); i!= chartStates.end(); ++i) {
		ECSL_DP::State currState= *i;
		chartPass2( currState);
	}
}

void StateflowConverter::createCharts( const matlab::machine& mm, ECSL_DP::State& es)
{
	int xPos= CHART_X_INIT_POSITION, yPos= CHART_Y_INIT_POSITION;
	std::set< matlab::chart> mcs = mm.charts();
	for( std::set< matlab::chart>::const_iterator i= mcs.begin(); i!= mcs.end(); ++i) {
		const matlab::chart& currMc= *i;
		matlab::instance mi= currMc.instance();
		// see if the system name matches the system passed in
		ECSL_DP::System targetSystem= matchSystem( mi.name());
		if ( !targetSystem)
			continue;
		// create state
		ECSL_DP::State new_es = ECSL_DP::State::Create( es);
		chartPass1( currMc ,new_es);
		// !!VIZA 1-level states: 'new_es'. Created for each matlab chart.
		// positioning
		new_es.position() = MakeGMEPosString( xPos, yPos);
		xPos= ( xPos >= 800) ? ( xPos+= CHART_X_OFFSET) : ( CHART_X_INIT_POSITION);
		yPos= ( xPos == CHART_X_INIT_POSITION) ? ( yPos+=CHART_Y_OFFSET) : yPos;
	}
}

void StateflowConverter::translateEvent( const matlab::event& me, ECSL_DP::Event& ee)
{
	ee.Name() = me.name();
	ee.name() = mangle( me.name());
	ee.Description() = me.description();
	ee.Scope() = me.scope();
	ee.Trigger() = me.trigger();
}

void StateflowConverter::translateData( const matlab::data& md, ECSL_DP::Data& ed)
{
	ed.Name() = md.name();
	ed.name() = mangle( md.name());
	ed.Description() = md.description();
	ed.Scope() = md.scope();
	ed.DataType() = md.dataType();
	ed.Units() = md.units();

	const matlab::props& mp = md.props();	
	if( mp) {
		ed.InitialValue() = mp.initialValue();
		matlab::range mr = mp.range();
		if( mr) {
			ed.Min() = mr.minimum();
			ed.Max() = mr.maximum();
		}
		matlab::array ma = mp.array();
		if( ma) {
			ed.ArraySize() = ma.size();
			ed.ArrayFirstIndex() = ma.firstIndex();
		}
	}
}

ECSL_DP::System StateflowConverter::matchSystem( const std::string& instName) const
{
	std::vector< std::string> sysElems;
	breakDownSystemPath( instName, sysElems);
	// remove duplicate nameas
	std::vector< std::string>::iterator itEnd= std::unique( sysElems.begin(), sysElems.end());
	sysElems.erase( itEnd, sysElems.end());
	// find the subsytem
	return findSubSystem( _ecsldpRootSystem, sysElems);
}

class NameCompareFunc : public std::binary_function< ECSL_DP::System, ECSL_DP::System, bool>
{
public:
	template< class T>
	bool operator()( const T& t1, const T& t2) const {
		const std::string& t1Name= t1.Name();
		const std::string& t2Name= t2.Name();
		return t1Name < t2Name;
	}
};

ECSL_DP::System StateflowConverter::findSubSystem( const ECSL_DP::System& rootSystem, const std::vector< std::string>& sysElemNames) const
{
//for( std::vector< std::string>::const_iterator ite= sysElemNames.begin(); ite!= sysElemNames.end(); ++ite) {
//	std::cout << *ite << "\t";
//}
//std::cout << std::endl << "find these in: " << std::endl;
	typedef std::set< ECSL_DP::System, NameCompareFunc> SystemSet_t;
	SystemSet_t currSystemElems;
	currSystemElems.insert( rootSystem); // initalize set of current systems with root
	ECSL_DP::System systemToFind= ECSL_DP::System::Cast( UdmStatic::CreateObject( ECSL_DP::System::meta));
	ECSL_DP::System foundSystem;
	for ( std::vector< std::string>::const_iterator i= sysElemNames.begin(); i!= sysElemNames.end(); ++i) {
		const std::string& currSysElem= *i;
		systemToFind.Name()= currSysElem;
		SystemSet_t::const_iterator itFind= currSystemElems.find( systemToFind);
//for( SystemSet_t::const_iterator item= currSystemElems.begin(); item!= currSystemElems.end(); ++item) {
//	std::cout << ( std::string)item->Name() << "\t";
//}
//std::cout << std::endl;
		if ( itFind == currSystemElems.end()) {
			ECSL_DP::System nullSystem;
			std::cerr << "WARNING: System " << currSysElem << " not found." << std::endl;
			return nullSystem;	// not found; return null object
		} else {
			// found. update set of current systems with subsytems of current system for the next iteration.
			foundSystem= *itFind;
			currSystemElems.clear();
			currSystemElems= foundSystem.System_kind_children_sorted( NameCompareFunc());
		}
	}
//	if ( systemToFind)
//		systemToFind.DeleteObject();
	return foundSystem;
}

void StateflowConverter::chartPass1( const matlab::chart& mc, ECSL_DP::State& es)
{
	es.name()= mangle( ExtractChartName( mc.name()));
	createEvents( mc, es);
	createData( mc, es);
	createJunctions( mc, es, JunctErrorHandler());
	createTransitions( mc, es);
	std::set< matlab::state> mss = mc.states();
	createSubStates( mss, es);
	calculateOrder( mss);
	createStateRef( mc, es);
}

void StateflowConverter::chartPass2( ECSL_DP::State& es)
{
	StatePass2( es);
}

void StateflowConverter::translateTransition( const matlab::transition& mt, ECSL_DP::Transition& et)
{
	// get trigger, guard, action here
	et.Trigger()= mt.trigger();
	et.Guard()= ( (( std::string) mt.condition()).empty()) ? "" : "[" + ( std::string) mt.condition() + "]";
	et.Action()= mt.action();
	et.ConditionAction()= mt.conditionAction();
	et.Order()= num_to_string( mt.sourceOClock());
}

void StateflowConverter::createSubStates( const std::set< matlab::state>& mss, ECSL_DP::State& es)
{
	for( std::set< matlab::state>::const_iterator i= mss.begin(); i!= mss.end(); ++i) {
		const matlab::state& currMs= *i;
		ECSL_DP::State esSub = ECSL_DP::State::Create( es);
		StatePass1( currMs, esSub);
		SetPosition( currMs, esSub);
	}
}

void StateflowConverter::createStateRef( const matlab::chart& mc, ECSL_DP::State& es)
{
	matlab::instance mi = mc.instance();
	ECSL_DP::System ecsldpSubSystem= matchSystem( mi.name());
	if ( !ecsldpSubSystem) {
		std::cerr << "System: " << ( std::string)mi.name() << " not found error." << std::endl;
		return;
	}
	// found. create references.
	std::string sysname = ecsldpSubSystem.name();
	std::string statename= es.Name();
	std::set< ECSL_DP::Block, NameCompareFunc> ebs = ecsldpSubSystem.Block_kind_children_sorted( NameCompareFunc());
	ECSL_DP::Block blockToFind= ECSL_DP::Block::Cast( UdmStatic::CreateObject( ECSL_DP::Block::meta));
	blockToFind.Name()= SFUNCTION;
	std::set< ECSL_DP::Block, NameCompareFunc>::iterator itFindBlock= ebs.find( blockToFind);
	while ( itFindBlock != ebs.end()) {
		ECSL_DP::Block foundBlock= *itFindBlock;
		// create reference
		ECSL_DP::ConnectorRef ecr= ECSL_DP::ConnectorRef::Create( foundBlock);
		ecr.ref()= es;
		ecr.name()= mangle( sysname+ statename);
		ecr.position()= MakeGMEPosString( STATEREF_X_INIT_POSITION, STATEREF_Y_INIT_POSITION);
		// find next
		ebs.erase( itFindBlock);
		itFindBlock= ebs.find( blockToFind);
	}
//	if ( blockToFind)
//		blockToFind.DeleteObject();
}

void StateflowConverter::StatePass1( const matlab::state& ms, ECSL_DP::State& es)
{
	translateState( ms, es);
	createEvents( ms, es);
	createData( ms, es);
	createJunctions( ms, es, JunctCreateHandler( es, _historyMap));
	createTransitions( ms, es);
	std::set< matlab::state> substates= ms.subStates();
int sz= substates.size();
	createSubStates( substates, es);
	calculateOrder( substates);
}

void StateflowConverter::StatePass2( ECSL_DP::State& es)
{
	std::set< ECSL_DP::State> subStates = es.State_kind_children();
	setStateDecomposition( subStates, es);
	// process substates
	for( std::set< ECSL_DP::State>::const_iterator i= subStates.begin(); i!= subStates.end(); ++i) {
		ECSL_DP::State currState= *i;	
		StatePass2( currState);
	}
	//
	mapTransitions( es);
}

void StateflowConverter::setStateDecomposition( const std::set< ECSL_DP::State>& subStates, ECSL_DP::State& es)
{
	EcslDPStateMap_t::const_iterator itFindState = _ecsldpStateMap.find( es);
	std::string type;
	if ( _ecsldpStateMap.end() != itFindState) { 
		type= itFindState->second.type();
	}
	// if (MDL_stateX.type == FUNC_STATE || MDL_stateX.type == GROUP_STATE) then ...
	if ( ( FUNC_STATE == type) || ( GROUP_STATE== type)) {
		es.Decomposition()= type;
		return;
	}
	// else ...
//std::cerr << "Processing... " << ( std::string)es.name() << "." << std::endl;
	bool hasAndStates= false;	
	bool hasOrStates= false;
	std::string childType;
	for( std::set< ECSL_DP::State>::iterator i= subStates.begin(); i!= subStates.end(); ++i) {
		const ECSL_DP::State& currSubState= *i;
		EcslDPStateMap_t::const_iterator  itFindSubState= _ecsldpStateMap.find( currSubState);
		if( _ecsldpStateMap.end() != itFindSubState) {
			childType = itFindSubState->second.type();
			if ( AND_STATE == childType) {
				hasAndStates= true;
//std::cerr << "\tAND_STATE found."  << std::endl;
			} else if ( OR_STATE == childType) {
				hasOrStates= true;
//std::cerr << "\tOR_STATE found."  << std::endl;
			} else if  ( ( FUNC_STATE == childType) || ( GROUP_STATE == childType)) {
				// do nothing
//std::cerr << "\tFUNC_STATE or GROUP_STATE found."  << std::endl;
			} else {
				std::cerr << "Unknown state decomposition: " << childType << std::endl;
				ASSERT(false);
			}
		} else {
			std::cerr << "State not found error. " << ( std::string)currSubState.name() << std::endl;
			ASSERT(false);
		}
	}
	// if no children || only junction children, then ...
	if( subStates.empty()) {
		// ...leaf state
		childType = OR_STATE;
		hasOrStates = true;
	}
	// else if MDL_stateX has both AND_STATE and OR_STATE children, then ...
	if( hasAndStates && hasOrStates) {
		std::string chartName = es.name();
		std::cerr << "Can't decide chart decomposition of " << chartName << "." << std::endl;
		ASSERT(false);
	// else if all_children of MDL_stateX are AND_STATE || FUNC_STATE || GROUP_STATE, - OR -
	// if all_children of MDL_stateX are OR_STATE || FUNC_STATE || GROUP_STATE, then...
	} else if( hasAndStates || hasOrStates) {
		es.Decomposition()= childType;	
	// else neither has AND_STATE nor OR_STATE children, then ...
	} else {
		std::string chartName = es.name();
		std::cerr << "Can't decide chart decomposition of " << chartName << "." << std::endl;
		ASSERT(false);
	}
}

void StateflowConverter::mapTransitions( ECSL_DP::State& es)
{
	int connRefXPos= CONNREF_X_INIT_POSITION, connRefYPos= CONNREF_Y_INIT_POSITION;
	std::set< ECSL_DP::Transition> ets = es.Transition_kind_children();
	for( std::set< ECSL_DP::Transition>::const_iterator i= ets.begin(); i!= ets.end(); ++i)
	{
		ECSL_DP::Transition currEt= *i;
		TransitionMap_t::const_iterator itFindTrans = _transMap.find( currEt);
		if( _transMap.end()== itFindTrans) {
			std::cerr << "Transition " << (std::string) currEt.name() << " not found. \
				Transition mapping failed. " << std::endl;
			ASSERT(false);
		}
		// found. 
		matlab::transition mt= itFindTrans->second;
		ECSL_DP::TransConnector srcConn;
		ECSL_DP::TransConnector dstConn;
		getTransitionEndPoints( mt, srcConn, dstConn, es);
		setTransitionEndPoints( currEt, srcConn, dstConn, es, connRefXPos, connRefYPos);
	}
}

void StateflowConverter::getTransitionEndPoints( const matlab::transition& mt, ECSL_DP::TransConnector& srcConn, ECSL_DP::TransConnector& dstConn, ECSL_DP::State& es)
{
	// find dst
	matlab::transitionTerminal mttDst = mt.dst();
	if ( true == Uml::IsDerivedFrom( mttDst.type(), matlab::state::meta)) {
		const matlab::state& msDst= matlab::state::Cast( mttDst);
		// look up matching ecsl state
		MatlabStateMap_t::const_iterator itFindState = _matlabStateMap.find( msDst);
		if ( _matlabStateMap.end() != itFindState) {
			const ECSL_DP::State& esDst = itFindState->second;
			dstConn = esDst;
		} else {
			std::cerr << "Destination state for" <<  msDst.id() << " not found error." << std::endl;
			ASSERT(false);
		}
	} else if ( true == Uml::IsDerivedFrom( mttDst.type(), matlab::junction::meta)) {
		const matlab::junction& mjDst= matlab::junction::Cast( mttDst);
		JunctionMap_t::const_iterator itFindJunct= _juncMap.find( mjDst);
		if ( _juncMap.end() != itFindJunct) {
			// found junction.
			const ECSL_DP::Junction& ejDst= itFindJunct->second;
			dstConn= ejDst;
		} else {
			HistoryMap_t::const_iterator itFindHist= _historyMap.find( mjDst);
			if ( _historyMap.end() != itFindHist) {
				// found history.
				const ECSL_DP::History& ehDst= itFindHist->second;
				dstConn= ehDst;
			} else {
				std::cerr << "Destination junction for" <<  mjDst.id() << " not found error." << std::endl;
				ASSERT( false);
			}
		}
	}
	// find src
	matlab::transitionTerminal mttSrc = mt.src();
	if( !mttSrc) {
		// must be default transition
		ECSL_DP::TransStart ets = ECSL_DP::TransStart::Create( es);
		ets.position()= MakeGMEPosString( TRANSSTART_X_INIT_POSITION, TRANSSTART_Y_INIT_POSITION);
		srcConn= ets;
	} else {
		if ( true == Uml::IsDerivedFrom( mttSrc.type(), matlab::state::meta)) {
			const matlab::state& msSrc= matlab::state::Cast( mttSrc);
			MatlabStateMap_t::const_iterator itFindState= _matlabStateMap.find( msSrc);
			if( _matlabStateMap.end() != itFindState) {
				const ECSL_DP::State esSrc = itFindState->second;
				srcConn = esSrc;
			} else {
				std::cerr << "Destination state for" <<  msSrc.id() << " not found error." << std::endl;
				ASSERT(false);
			}
		} else if ( true == Uml::IsDerivedFrom( mttSrc.type(), matlab::junction::meta)) { 
			const matlab::junction& mjSrc= matlab::junction::Cast( mttSrc);
			JunctionMap_t::const_iterator itFindJunct= _juncMap.find( mjSrc);
			if( _juncMap.end() != itFindJunct) {
				const ECSL_DP::Junction& ejSrc = itFindJunct->second;
				srcConn = ejSrc;
			} else {
				HistoryMap_t::const_iterator itFindHist= _historyMap.find( mjSrc);
				if ( _historyMap.end() != itFindHist) {
					const ECSL_DP::History& ehSrc = itFindHist->second;
					srcConn = ehSrc;
				} else {
					std::cerr << "Source junction for" <<  mjSrc.id() << " not found error." << std::endl;
					ASSERT(false);
				}
			}
		}
	}
}

void StateflowConverter::setTransitionEndPoints( 
	ECSL_DP::Transition& et, 
	ECSL_DP::TransConnector& srcConn, 
	ECSL_DP::TransConnector& dstConn, 
	ECSL_DP::State& es, 
	int& xPos, 
	int& yPos
	)
{
	if ( ( ECSL_DP::State)srcConn.State_parent() == ( ECSL_DP::State)dstConn.State_parent()) {
		setTransitionEndPoints( srcConn, dstConn, et);
		return;
	}
	// cross hierarchy transition; reference needed
	// create reference to the destination always
	setTransitionEndPoints( srcConn, createConnectorRef( ( ECSL_DP::State)srcConn.State_parent(), dstConn, xPos, yPos), et);
	/*
	if ( ( ECSL_DP::State)srcConn.State_parent() == es) {
		// create ref to dst
		setTransitionEndPoints( srcConn, createConnectorRef( ( ECSL_DP::State)srcConn.State_parent(), dstConn, xPos, yPos), et);
		return;
	}
	if ( ( ECSL_DP::State)dstConn.State_parent() == es) {
		// create ref to source 
		setTransitionEndPoints( createConnectorRef( ( ECSL_DP::State)dstConn.State_parent(), srcConn, xPos, yPos), dstConn, et);
		return;
	}
	// create refs to both
	setTransitionEndPoints( createConnectorRef( es, srcConn, xPos, yPos), createConnectorRef( es, dstConn, xPos, yPos), et);
	*/
}

void StateflowConverter::setTransitionEndPoints( const ECSL_DP::TransConnector& etcSrc, const ECSL_DP::TransConnector& etcDst, ECSL_DP::Transition& et)
{
	et.srcTransition_end()= etcSrc;
	et.dstTransition_end()= etcDst;
}

ECSL_DP::ConnectorRef StateflowConverter::createConnectorRef( ECSL_DP::State& parent, ECSL_DP::TransConnector& toRef, int& xPos, int& yPos)
{
	ECSL_DP::ConnectorRef ecr= ECSL_DP::ConnectorRef::Create( parent);
	ecr.ref()= toRef;
	ecr.position()= MakeGMEPosString( xPos, yPos);
	xPos+= CONNREF_X_OFFSET;
	return ecr;
}

void StateflowConverter::translateState( const matlab::state& ms, ECSL_DP::State& es)
{
	_matlabStateMap[ ms]= es;
	_ecsldpStateMap[ es]= ms;
	// copy attributes
	es.name()= ms.name();
	es.DuringAction()= ms.duringAction();
	es.EnterAction()= ms.entryAction();
	es.ExitAction()= ms.exitAction();
	// es.Order()= ??? !!VIZA
	// es.Decomposition()= computed in pass2!
}

///////////////////////////////////////////////////////////////////////////////
// OrderFunctor used in StateflowConverter::calculateOrder()
class OrderFunctor : public binary_function< matlab::state, matlab::state, bool>
{
public:
	bool operator()( const matlab::state& ms1, const matlab::state ms2) const {
		std::string pos1= ms1.position();
		std::string pos2= ms2.position();
		int x1= 0, y1= 0, x2= 0, y2= 0;
		ParseMatlabPosString( pos1, "[", " ", "]", x1, y1);
		ParseMatlabPosString( pos2, "[", " ", "]", x2, y2);
		if ( x1 < x2)
			return true;
		if ( x1 > x2)
			return false;
		// equal x pos -> y pos will decide
		return y1 < y2;
	}
};

///////////////////////////////////////////////////////////////////////////////
// OrderCounter used in StateflowConverter::calculateOrder()
class OrderCounter
{
public:
	OrderCounter( const std::map< matlab::state, ECSL_DP::State>& msm, unsigned int cnt= 0) :
	  _matlabStateMapRef( msm), _cnt( cnt)
	  {}
	//
	void operator()( const matlab::state& ms) {
		MatlabStateMap_t::const_iterator itFindMs= _matlabStateMapRef.find( ms);
		if( _matlabStateMapRef.end() != itFindMs) {
			const ECSL_DP::State& es= itFindMs->second;
			es.Order()= num_to_string( _cnt++);
		} else {
			std::cerr << "State not found error. " << ( std::string)ms.name() << std::endl;
			ASSERT(false);
		}
	}
private:
	//
	typedef std::map< matlab::state, ECSL_DP::State> MatlabStateMap_t;
	const MatlabStateMap_t& _matlabStateMapRef;
	//
	unsigned int _cnt;
};

///////////////////////////////////////////////////////////////////////////////
// OrderPrinter used in StateflowConverter::calculateOrder()
class OrderPrinter
{
public:
	OrderPrinter( const std::map< matlab::state, ECSL_DP::State>& msm) :
	  _matlabStateMapRef( msm)
	  {}
	//
	void operator()( const matlab::state& ms) {
		MatlabStateMap_t::const_iterator itFindMs= _matlabStateMapRef.find( ms);
		if( _matlabStateMapRef.end() != itFindMs) {
			const ECSL_DP::State& es= itFindMs->second;
			std::cout << "\t" << ( std::string) es.name() << ": " << ( std::string) es.Order() << std::endl;
		} else {
			std::cerr << "State not found error. " << ( std::string)ms.name() << std::endl;
			ASSERT(false);
		}
	}
private:
	//
	typedef std::map< matlab::state, ECSL_DP::State> MatlabStateMap_t;
	const MatlabStateMap_t& _matlabStateMapRef;
};

void StateflowConverter::calculateOrder( const std::set< matlab::state>& substates)
{
// logging
//if ( substates.empty())
//	return;
//Udm::Object parent= (*substates.begin()).parent();
//if ( parent.type() == matlab::state::meta) {
//	std::cout << "Calculate order for states in state: " << ( std::string) matlab::state::Cast( parent).name() << std::endl;
//} else if ( parent.type() == matlab::chart::meta) {
//	std::cout << "Calculate order for states in state: " << ( std::string) matlab::chart::Cast( parent).name() << std::endl;
//}

	std::set< matlab::state, OrderFunctor> orderedStates;
	std::copy( substates.begin(), substates.end(), std::inserter( orderedStates, orderedStates.begin()));
	for_each( orderedStates.begin(), orderedStates.end(), OrderCounter( _matlabStateMap));

// logging
//std::cout << "Sub states: " << orderedStates.size() << std::endl;
//for_each( orderedStates.begin(), orderedStates.end(), OrderPrinter( _matlabStateMap));
}

template< class T>
void StateflowConverter::createEvents( const T& eventsParent, ECSL_DP::State& es)
{
	int yPos= EVENT_Y_INIT_POSITION;
	std::set< matlab::event> mes = eventsParent.events();
	for( std::set< matlab::event>::const_iterator i= mes.begin(); i!= mes.end(); ++i) {
		const matlab::event& currMe= *i;
		ECSL_DP::Event ee = ECSL_DP::Event::Create( es);
		translateEvent(currMe, ee);
		// positioning
		ee.position() = MakeGMEPosString( EVENT_X_POSITION, yPos);
		yPos+= EVENT_Y_OFFET;
	}
}

template< class T>
void StateflowConverter::createData( const T& dataParent, ECSL_DP::State& es)
{
	int yPos= DATA_Y_INIT_POSITION;
	std::set< matlab::data> mds = dataParent.data();
	for( std::set< matlab::data>::const_iterator i= mds.begin(); i!= mds.end(); ++i) {
		const matlab::data& currMd= *i;
		ECSL_DP::Data ed = ECSL_DP::Data::Create( es);
		translateData( currMd, ed);
		// positioning
		ed.position() = MakeGMEPosString( DATA_X_POSITION, yPos);
		yPos+= DATA_Y_OFFSET;
	}
}

template< class T>
void StateflowConverter::createTransitions( const T& transitionParent, ECSL_DP::State& es)
{
	std::set< matlab::transition> mts = transitionParent.transitions();
	for( std::set< matlab::transition>::const_iterator i= mts.begin(); i!= mts.end(); ++i) {
		const matlab::transition& currMt= *i;
		ECSL_DP::Transition et = ECSL_DP::Transition::Create( es);
		_transMap[ et] = currMt;
		translateTransition( currMt, et);
	}
}


template <class T>
void StateflowConverter::createJunctions( const T& juncParent, ECSL_DP::State& es, IHistoryJunctionHandler& hjHandler)
{

	std::set< matlab::junction> mjs= juncParent.junctions();
	for( std::set< matlab::junction>::const_iterator i= mjs.begin(); i!= mjs.end(); ++i) {
		const matlab::junction& currMj= *i;
		std::string junct_type = currMj.type();
		if( CONNECTIVE_JUNCTION == junct_type) {
			ECSL_DP::Junction ej= ECSL_DP::Junction::Create( es);
			_juncMap[ currMj] = ej;
			SetPosition( currMj, ej);
		} else if( HISTORY_JUNCTION == junct_type) {
			hjHandler( currMj);
		} else {
			std::cerr << "Error - Unrecognized junction type: " << junct_type << " in : " << ( std::string) juncParent.name() << std::endl;
			ASSERT(false);
		}
	}
}


