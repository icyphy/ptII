#ifndef DATAFLOWCONVERTER_H
#define DATAFLOWCONVERTER_H

#include <map>
#include "matlab.h"
#include "ECSL_DP.h"

class DataflowConverter
{
public:
	void operator()( const matlab::System& matlabSystem, ECSL_DP::System& ecslDPSystem) {
		init();
		systemPass1( matlabSystem, ecslDPSystem);
		systemPass2( ecslDPSystem);
	}

protected:
	void init();
	//
	void systemPass1( const matlab::System& matlabSystem, ECSL_DP::System& ecslDPSystem);
	void systemPass2( ECSL_DP::System& ecslDPSystem);
	//
	void createAnnotations( const matlab::System& ms, ECSL_DP::System& es);
	void createBlocks( const matlab::System& ms, ECSL_DP::System& es);
	//
	void subSystemPass1(const matlab::Block& mb, ECSL_DP::System& es);
	void blockPass1(const matlab::Block& mb, ECSL_DP::Block& eb);
	//
	void getParameters( const matlab::Block& mb, std::set< matlab::Parameter>& parameters) const;
	void copyParameters( const matlab::Block& mb, ECSL_DP::Block& eb);
	//
	void createPorts( const matlab::Block& mb, ECSL_DP::Block& eb);
	//
	void createInputPort(const matlab::Primitive& mp, ECSL_DP::System& es);
	void createTriggerPort( const matlab::Primitive& mp, ECSL_DP::System& es);
	void createEnablePort( const matlab::Primitive& mp, ECSL_DP::System& es);
	void createOutputPort( const matlab::Primitive& mp, ECSL_DP::System& es);
	//
	int getMaxBlockXPos( const ECSL_DP::System& es) const;
	void setPortPositions( ECSL_DP::System& es, int outputPortXPos);
	//
	void processLines( ECSL_DP::System& es);
	void createDirectLine( const matlab::DirectLine& mdl, ECSL_DP::System& es);
	void createBranchedLine( const matlab::BranchedLine& mbl, ECSL_DP::System& es);
	//
	ECSL_DP::Port getECSLPort( const matlab::Port mp) const;
	void fetchPortsFromDirectBranches( const std::set< matlab::DirectBranch>& dbs, std::set< ECSL_DP::Port>& dstPorts);
	void fetchPortsFromNestedBranches( const std::set< matlab::NestedBranch>& nbs, std::set< ECSL_DP::Port>& dstPorts);
	//
	template< class MATLAB_PORT, class ECSLDP_PORT>
	void createPort( const matlab::Primitive& mp, ECSL_DP::System& es, MATLAB_PORT& mPort, ECSLDP_PORT& ePort);
	//
	template< class MATLAB_PORT, class ECSLDP_PORT>
	void createPort( ECSL_DP::Block& eb, MATLAB_PORT& mPort, ECSLDP_PORT& ePort, int xPos, int yPos);
	//
	template< class T>
	void getDestinationPorts( const T& branchParent, std::set< ECSL_DP::Port>& dstPorts);

private:
	//
	typedef std::map< ECSL_DP::System, matlab::System> SystemMap_t;
	SystemMap_t _systemMap;
	//
	typedef std::map< matlab::Port, ECSL_DP::Port> MatlabPortMap_t;
	MatlabPortMap_t _matlabPortMap;
	//
	typedef std::map< ECSL_DP::Port, matlab::Port> ECSLDPPortMap_t;
	ECSLDPPortMap_t _ecsldpPortMap;
	//
	typedef std::map< ECSL_DP::Block, matlab::Block> BlockMap_t;
	BlockMap_t _blockMap;
	//
	int _ipnum;
	int _opnum;
};

#endif //DATAFLOWCONVERTER_H
