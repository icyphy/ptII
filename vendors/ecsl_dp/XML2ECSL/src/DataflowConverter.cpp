#include "DataflowConverter.h"
#include <set>
#include <UmlExt.h>	// for 'IsDerivedFrom()'
#include "Utils.h"

const int PARAM_X_INIT_POS= 100;
const int PARAM_Y_INIT_POS= 100;
const int PARAM_X_OFFSET= 100;
const int PARAM_Y_OFFSET= 100;
const int PARAM_X_MAX_POS= 500;

const int INPUTPORT_X_POS= 26;
const int TRIGGERPORT_X_POS= 27;
const int ENABLEPORT_X_POS= 28;
const int OUTPUTPORT_X_POS= 600;
const int PORT_Y_INITPOS= 29;

const std::string INPUTPORT_PREFIX= "In";
const std::string OUTPUTPORT_PREFIX= "Out";

//////////////////////////////////////////////////////////////////////////////
template <class MT, class ET>
void SetPosition(const MT& mt, ET& et, const std::string& termExpr) 
{
	std::string loc = mt.Position();
	int x=0, y= 0;
	ParseMatlabPosString( loc, "[", ",", termExpr, x, y);
	x*= GME_X_SCALE_FACTOR;
	y*= GME_Y_SCALE_FACTOR;
	et.position() = MakeGMEPosString( x, y);
}

//////////////////////////////////////////////////////////////////////////////
void DataflowConverter::init()
{
	_systemMap.clear();	
	_ipnum= 0;
	_opnum= 0;
}

void DataflowConverter::systemPass1( const matlab::System& matlabSystem, ECSL_DP::System& ecslDPSystem)
{
	_systemMap[ ecslDPSystem]= matlabSystem;
	createAnnotations( matlabSystem, ecslDPSystem);
	createBlocks( matlabSystem, ecslDPSystem);
}

void DataflowConverter::systemPass2( ECSL_DP::System& ecslDPSystem)
{
	int maxBlockXPos= std::max( OUTPUTPORT_X_POS, getMaxBlockXPos( ecslDPSystem))+ GME_X_OFFSET;
	setPortPositions( ecslDPSystem, maxBlockXPos);
	processLines( ecslDPSystem);
	// recursion step
	std::set< ECSL_DP::System> subSystems = ecslDPSystem.System_kind_children();
	for( std::set< ECSL_DP::System>::iterator i = subSystems.begin(); i!=subSystems.end(); ++i) {
		ECSL_DP::System currSubSystem= *i;
		systemPass2( currSubSystem);
	}
}

void DataflowConverter::createAnnotations( const matlab::System& ms, ECSL_DP::System& es)
{
	int note_cnt= 1;
	std::set< matlab::Annotation> annotations = ms.annotations();
	for( std::set< matlab::Annotation>::iterator i= annotations.begin(); i != annotations.end(); ++i)
	{
		const matlab::Annotation& curr_ma= *i;
		ECSL_DP::Annotation ea = ECSL_DP::Annotation::Create( es);
		ea.name()= "Note" + num_to_string( note_cnt++);
		ea.Text()= curr_ma.Text();
		SetPosition( curr_ma, ea, "]");
	}
}

void DataflowConverter::createBlocks( const matlab::System& ms, ECSL_DP::System& es)
{
	std::set< matlab::Block> blocks = ms.blocks();
//	std::set< matlab::Block> blocks = ms.Block_kind_children();
	for( std::set< matlab::Block>::iterator i= blocks.begin(); i!= blocks.end(); ++i)
	{
		const matlab::Block& curr_mb= *i;
		std::string blockType = curr_mb.BlockType();
		if ( "SubSystem" == blockType) {
			subSystemPass1( curr_mb, es);
		} else if ( "Reference" == blockType) {
			ECSL_DP::Reference reference = ECSL_DP::Reference::Create( es);
			reference.Name() = curr_mb.Name();
			reference.name() = mangle( curr_mb.Name());
			blockPass1( curr_mb, reference);
		} else if ( "Inport" == blockType) {
			matlab::Primitive mp = matlab::Primitive::Cast( curr_mb);
			createInputPort( mp, es);
		} else if ( "TriggerPort" == blockType) {
			matlab::Primitive mp = matlab::Primitive::Cast( curr_mb);
			createTriggerPort( mp, es);
		} else if ( "EnablePort" == blockType) {
			matlab::Primitive mp = matlab::Primitive::Cast( curr_mb);
			createEnablePort( mp, es);
		} else if ( "Outport" == blockType) {
			matlab::Primitive mp = matlab::Primitive::Cast( curr_mb);
			createOutputPort( mp, es);
		} else {
			// otherwise primitive
			ECSL_DP::Primitive primitive = ECSL_DP::Primitive::Create( es);
			primitive.Name() = curr_mb.Name();
			primitive.name() = mangle( curr_mb.Name());
			blockPass1( curr_mb, primitive);
		}
	}
}

void DataflowConverter::subSystemPass1(const matlab::Block& mb, ECSL_DP::System& es) 
{
	// create subsystem
	ECSL_DP::System subSystem = ECSL_DP::System::Create( es);
	
	subSystem.Name() = mb.Name();
	subSystem.name() = mangle( mb.Name());
	subSystem.position() = MakeGMEPosString( GME_X_DEFAULT_POS, GME_Y_DEFAULT_POS);
	// copy parameters
	copyParameters( mb, subSystem);
	matlab::System ms= matlab::Subsystem::Cast( mb).system();
	// set subsystem position
	SetPosition( mb, subSystem, ",");
	// recursion
	systemPass1( ms, subSystem);
}

void DataflowConverter::getParameters( const matlab::Block& mb, std::set< matlab::Parameter>& parameters) const
{
	parameters= mb.parameters();
}

void DataflowConverter::copyParameters( const matlab::Block& mb, ECSL_DP::Block& eb)
{
	std::set< matlab::Parameter> parameters;
	getParameters( mb, parameters);
	int currXPos= PARAM_X_INIT_POS, currYPos= PARAM_Y_INIT_POS;
	for( std::set< matlab::Parameter>::iterator i = parameters.begin(); i != parameters.end(); ++i) {
		matlab::Text mt= matlab::Text::Cast( *i);
		ECSL_DP::Parameter ep= ECSL_DP::Parameter::Create( eb);
		ep.name() = mt.Name();
		ep.Value() = mt.Value();
		ep.position() = MakeGMEPosString( currXPos, currYPos);
		currXPos= ( currXPos > PARAM_X_MAX_POS) ? ( PARAM_X_INIT_POS) : ( currXPos+= PARAM_X_OFFSET);
		currYPos= ( currXPos == PARAM_X_INIT_POS) ? ( currYPos+= PARAM_Y_OFFSET) : currYPos;
	}		
}

void DataflowConverter::blockPass1( const matlab::Block& mb, ECSL_DP::Block& eb)
{
	_blockMap[ eb] = mb;
	createPorts( mb, eb);
	copyParameters( mb, eb);
	// set attributes
	eb.BlockType() = mb.BlockType();
	eb.Priority() = atoi( std::string( mb.Priority()).c_str());
	eb.Tag() = mb.Tag();
	eb.Description() = mb.Description();
	// set position
	SetPosition( mb, eb, ",");
	
}

void DataflowConverter::createPorts( const matlab::Block& mb, ECSL_DP::Block& eb)
{
	std::set< matlab::Port> ports = mb.ports();
	int yPos= PORT_Y_INITPOS;
	for( std::set< matlab::Port>::const_iterator i = ports.begin(); i != ports.end(); ++i, yPos+= GME_Y_OFFSET) {
		const matlab::Port& currPort= *i;
		if ( Uml::IsDerivedFrom( currPort.type(), matlab::InputPort::meta)) {
			const matlab::InputPort& mip= matlab::InputPort::Cast( currPort);
			ECSL_DP::InputPort eip;
			createPort( eb, mip, eip, INPUTPORT_X_POS, yPos);
			eip.name()= INPUTPORT_PREFIX+ num_to_string( mip.Number());
			eip.Number()= mip.Number();
		}
		else if ( Uml::IsDerivedFrom( currPort.type(), matlab::TriggerPort::meta)) {
			const matlab::TriggerPort& mtp= matlab::TriggerPort::Cast( currPort);
			ECSL_DP::TriggerPort etp;
			createPort( eb, mtp, etp, TRIGGERPORT_X_POS, yPos);
			etp.name()= mtp.Name();
			etp.TriggerType()= mtp.TriggerType();
		}
		else if ( Uml::IsDerivedFrom( currPort.type(), matlab::EnablePort::meta)) {
			const matlab::EnablePort& mep= matlab::EnablePort::Cast( currPort);
			ECSL_DP::EnablePort eep;
			createPort( eb, mep, eep, ENABLEPORT_X_POS, yPos);
			eep.name()= mep.Name();
			eep.StatesWhenEnabling()= mep.StatesWhenEnabling();
		}
		else if ( Uml::IsDerivedFrom( currPort.type(), matlab::OutputPort::meta)) {
			const matlab::OutputPort& mop= matlab::OutputPort::Cast( currPort);
			ECSL_DP::OutputPort eop;
			createPort( eb, mop, eop, OUTPUTPORT_X_POS, yPos);
			eop.name()= OUTPUTPORT_PREFIX+ num_to_string( mop.Number());
			eop.Number()= mop.Number();
		} else {
			std::cerr << "Port cast error - SubSystemPass1" << std::endl;
		}
	}
}

void DataflowConverter::createInputPort( const matlab::Primitive& mp, ECSL_DP::System& es)
{
	matlab::InputPort mip;
	ECSL_DP::InputPort eip;
	createPort( mp, es, mip, eip);
	if ( mip) {
		eip.Number()= mip.Number();
	} else {
		// TBD SKN -- this case happens when the "InputPort" is present in a root-system
		// we need to determine the actual port-numbers, which is probably present as an attribute
		eip.Number() = ++_ipnum;		// automatically assign port numbers
	}
}

void DataflowConverter::createTriggerPort( const matlab::Primitive& mp, ECSL_DP::System& es)
{
	matlab::TriggerPort mtp;
	ECSL_DP::TriggerPort etp;
	createPort( mp, es, mtp, etp);
	if( mtp && etp) {
		etp.TriggerType() = mtp.TriggerType();
	}
}

void DataflowConverter::createEnablePort( const matlab::Primitive& mp, ECSL_DP::System& es)
{
	matlab::EnablePort mep;
	ECSL_DP::EnablePort eep;
	createPort( mp, es, mep, eep);
	if ( mep && eep) {
		eep.StatesWhenEnabling()= mep.StatesWhenEnabling();
	}
}

void DataflowConverter::createOutputPort( const matlab::Primitive& mp, ECSL_DP::System& es)
{
	matlab::OutputPort mop;
	ECSL_DP::OutputPort eop;
	createPort( mp, es, mop, eop);
	if ( mop) {
		eop.Number()= mop.Number();
	} else {
		eop.Number()= ++ _opnum;
	}
}

int DataflowConverter::getMaxBlockXPos( const ECSL_DP::System& es) const
{
	std::set< ECSL_DP::Block> blocks = es.Block_kind_children();
	int maxXPos= 0, xPos= 0, yPos= 0;
	for( std::set< ECSL_DP::Block>::const_iterator i = blocks.begin(); i != blocks.end(); ++i) {
		const ECSL_DP::Block& currBlock= *i;
		ParseGMEPosString( currBlock.position(),  xPos, yPos);
		maxXPos= std::max( maxXPos, xPos);
	}
	return maxXPos;
}

void DataflowConverter::setPortPositions( ECSL_DP::System& es, int outputPortXPos)
{
	std::set< ECSL_DP::Port> ports = es.Port_kind_children();
	int yPos= PORT_Y_INITPOS, yOutputPortPos= PORT_Y_INITPOS;
	for( std::set< ECSL_DP::Port>::const_iterator i = ports.begin(); i != ports.end(); ++i, yPos+= GME_Y_OFFSET, yOutputPortPos+= GME_Y_OFFSET) {
		const ECSL_DP::Port& currPort= *i;
		if ( Uml::IsDerivedFrom( currPort.type(), ECSL_DP::InputPort::meta)) {
			const ECSL_DP::InputPort& eip= ECSL_DP::InputPort::Cast( currPort);
			eip.position()= MakeGMEPosString( INPUTPORT_X_POS, yPos);
			yPos+= GME_Y_OFFSET;
		}
		else if ( Uml::IsDerivedFrom( currPort.type(), ECSL_DP::TriggerPort::meta)) {
			const ECSL_DP::TriggerPort& etp= ECSL_DP::TriggerPort::Cast( currPort);
			etp.position()= MakeGMEPosString( TRIGGERPORT_X_POS, yPos);
			yPos+= GME_Y_OFFSET;
		}
		else if ( Uml::IsDerivedFrom( currPort.type(), ECSL_DP::EnablePort::meta)) {
			const ECSL_DP::EnablePort& eep= ECSL_DP::EnablePort::Cast( currPort);
			eep.position()= MakeGMEPosString( ENABLEPORT_X_POS, yPos);
			yPos+= GME_Y_OFFSET;
		}
		else if ( Uml::IsDerivedFrom( currPort.type(), ECSL_DP::OutputPort::meta)) {
			const ECSL_DP::OutputPort& eop= ECSL_DP::OutputPort::Cast( currPort);
			eop.position()= MakeGMEPosString( outputPortXPos, yPos);
		} else {
			std::cerr << "Port cast error - setPortPositions" << std::endl;
		}
	}
}

void DataflowConverter::processLines( ECSL_DP::System& es)
{
	SystemMap_t::const_iterator itFind = _systemMap.find( es);
	if( itFind == _systemMap.end()) {
		std::cerr << "ECSL System: " << es.Name() << " not found in System map." << std::endl;
		return;
	}
	const matlab::System& ms = itFind->second;
	std::set< matlab::Line> lines = ms.lines();
	for( std::set< matlab::Line>::const_iterator i= lines.begin(); i!= lines.end(); ++i)
	{
		const matlab::Line& currLine= *i;
		if( Uml::IsDerivedFrom( currLine.type(), matlab::DirectLine::meta))
		{
			// easier case: get the matlab ports, map to ecsl ports, and connect
			const matlab::DirectLine& directLine = matlab::DirectLine::Cast( currLine);
			createDirectLine( directLine, es);
		} else if( Uml::IsDerivedFrom( currLine.type(), matlab::BranchedLine::meta)) {
			const matlab::BranchedLine& branchedLine = matlab::BranchedLine::Cast( currLine);
			createBranchedLine( branchedLine, es);
		}
	}
}

void DataflowConverter::createDirectLine( const matlab::DirectLine& mdl, ECSL_DP::System& es)
{
	matlab::Port matlabSrc = mdl.srcPort();
	matlab::Port matlabDst = mdl.dstPort();
	ECSL_DP::Port ecslSrc;
	ECSL_DP::Port ecslDst;
	MatlabPortMap_t::const_iterator itFindSrc= _matlabPortMap.find( matlabSrc);
	if ( itFindSrc != _matlabPortMap.end()) {
		ecslSrc= itFindSrc->second;
	}
	MatlabPortMap_t::const_iterator itFindDst= _matlabPortMap.find( matlabDst);
	if ( itFindDst != _matlabPortMap.end()) {
		ecslDst= itFindDst->second;
	}
	if ( ecslSrc && ecslDst)
	{
		ECSL_DP::Line connectLine = ECSL_DP::Line::Create( es);
		connectLine.srcLine_end() = ecslSrc;
		connectLine.dstLine_end() = ecslDst;
	}
}

void DataflowConverter::createBranchedLine( const matlab::BranchedLine& mbl, ECSL_DP::System& es)
{
	// matlab::BranchedLine has 1 source, but 0..* destinations
	ECSL_DP::Port srcPort= getECSLPort( mbl.srcPort());
	if ( !srcPort) {
		return;	// no srcPort
	}
	std::set< ECSL_DP::Port> dstPorts;
	getDestinationPorts( mbl, dstPorts);
	// Create a Line between each pair of ports.
	for ( std::set< ECSL_DP::Port>::const_iterator i= dstPorts.begin(); i!= dstPorts.end(); ++i) {
		const ECSL_DP::Port& currDstPort= *i;
		ECSL_DP::Line line= ECSL_DP::Line::Create( es);
		// connect ports
		line.srcLine_end()= srcPort;
		line.dstLine_end()= currDstPort;
	}
}

ECSL_DP::Port DataflowConverter::getECSLPort( const matlab::Port mp) const
{
	ECSL_DP::Port ep;
	if ( mp) {
		MatlabPortMap_t::const_iterator itFindPort= _matlabPortMap.find( mp);
		if ( itFindPort != _matlabPortMap.end()) {
			ep= itFindPort->second;
		} else {
			std::cerr << "Matlab port: " << mp.Name() << " not found error." << std::endl;
		}
	}
	return ep;
}

void DataflowConverter::fetchPortsFromDirectBranches( const std::set< matlab::DirectBranch>& dbs, std::set< ECSL_DP::Port>& dstPorts)
{
	for( std::set< matlab::DirectBranch>::const_iterator i= dbs.begin(); i!= dbs.end(); ++i) {
		const matlab::DirectBranch& currDB= *i;
		ECSL_DP::Port ep= getECSLPort( currDB.dstPort());
		if ( ep) {
			dstPorts.insert( ep);
		}
	}
}

void DataflowConverter::fetchPortsFromNestedBranches( const std::set< matlab::NestedBranch>& nbs, std::set< ECSL_DP::Port>& dstPorts)
{
	for( std::set< matlab::NestedBranch>::const_iterator i= nbs.begin(); i!= nbs.end(); ++i) {
		const matlab::NestedBranch& currNB= *i;
		getDestinationPorts( currNB, dstPorts);
	}
}


template< class MATLAB_PORT, class ECSLDP_PORT>
void DataflowConverter::createPort( const matlab::Primitive& mp, ECSL_DP::System& es, MATLAB_PORT& mPort, ECSLDP_PORT& ePort)
{
	ePort= ECSLDP_PORT::Create( es);
	ePort.name() = mangle( mp.Name());
	// get all matlab port children of this primitive
	// make a map entry for each one
	std::set< matlab::Port> primPorts = mp.ports();
	for( std::set< matlab::Port>::iterator i= primPorts.begin(); i!= primPorts.end(); ++i) {
			_matlabPortMap[ *i] = ePort;
	}
	// now find the port it refers to in the parent block and rename it to match
	matlab::Port parentPort = mp.refPort();
	if ( parentPort) {
		_matlabPortMap[ parentPort] = ePort;
		mPort = MATLAB_PORT::Cast( parentPort);
	}
}

template< class MATLAB_PORT, class ECSLDP_PORT>
void DataflowConverter::createPort( ECSL_DP::Block& eb, MATLAB_PORT& mPort, ECSLDP_PORT& ePort, int xPos, int yPos)
{
	 ePort= ECSLDP_PORT::Create( eb);
	 _ecsldpPortMap[ ePort]= mPort;
	 _matlabPortMap[ mPort]= ePort;
	ePort.position()= MakeGMEPosString( xPos, yPos);
}

template< class T>
void DataflowConverter::getDestinationPorts( const T& branchParent, std::set< ECSL_DP::Port>& dstPorts)
{
	// Direct branches
	std::set< matlab::DirectBranch> dbs= branchParent.DirectBranch_kind_children();
	fetchPortsFromDirectBranches( dbs, dstPorts);
	// Nested branches
	std::set< matlab::NestedBranch> nbs= branchParent.NestedBranch_kind_children();
	fetchPortsFromNestedBranches( nbs, dstPorts);
}
