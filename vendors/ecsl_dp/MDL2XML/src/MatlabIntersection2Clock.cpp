#include "MatlabIntersection2Clock.h"
#include <math.h>
#include <iostream>


//source oclock		intersection
//---------------------------------------------------------------
//0					[1 0 -1 0.5 126.1091 42.6953 0 55.2727]
//1					[1 0 -1 0.8333 146.2274 42.6953 0 55.2727]
//2					[2 1 0 0.1667 156.2866 46.9724 0 55.2727]
//3					[2 1 0 0.5 156.2866 55.5267 0 55.2727]
//4					[2 1 0 0.8333 156.2866 64.0809 0 55.2727]
//5					[3 0 1 0.1667 146.2274 68.358 0 55.2727]
//6					[3 0 1 0.5 126.1091 68.358 0 55.2727]
//7					[3 0 1 0.8333 105.9907 68.358 0 55.2727]
//8					[4 -1 0 0.1667 95.9315 64.0809 0 55.2727]
//9					[4 -1 0 0.5 95.9315 55.5267 0 55.2727]
//10				[4 -1 0 0.8333 95.9315 46.9724 0 55.2727]
//11				[1 0 -1 0.1667 105.9907 42.6953 0 55.2727]
//12				[1 0 -1 0.5 126.1091 42.6953 0 55.2727]
const double PI= 3.1415926535897932384626433832795;

// Returns 1, if x>=0, otherwise returns -1.
template <class T>
T sgn( T x)
{
	return (x == fabs( x)) * 2.0 - 1.0;
}
// warps angle phi into range 0..2*PI 
double warpAngle( double phi)
{
	while (( phi<0) || ( phi>=2.0*PI))
		phi-= sgn( phi)* 2.0* PI;
	return phi;
}

// Given 3 Simulink State coordinates (x, -y, offset),
// returns the angle in radians in clockwise direction starting from 0hour 
//	(where the unit circle crosses the positive y-axis)
//		0   <-coord3->  1
//	1	------(-1,0)-----   0
//		|		^(-coord1)
//	^	|		|		|		^
//	|	|		|		|		|
//coord3(-1,0)--+----(1,0)coord2|
//	|	|		|		|	  coord3		
//	v	|		|		|		|
//		|		|		|		v
//	0	------(1,0)------	1
//		1   <-coord3->  0
//	*coord3 is a relative offset coordinate that resides on the appropriate 
//	 side of the limiting square.
double SimulinkStateCoordsToAngle( double coord1, double coord2, double coord3)
{
	// scale coordinate 0..1 -> -1..1
	double sc= coord3* 2.0 - 1.0;
	// get arctangent of the scaled coordinate.
	double angle= atan( sc);
	// offset the angle depending on which side coord3 resides on. 
	if ( coord1 == 1)
		angle+= PI/2.0;
	if ( coord1 == -1)
		angle+= 3.0*PI/2.0;
	if ( coord2 == 1)
		angle+= PI;
	// warp into 0..2*PI
	angle= warpAngle( angle);
	return angle;
}

// Given 2 Simulink Junction coordinates (x, -y),
// returns the angle in radians in clockwise direction starting from 0hour 
//	(where the unit circle crosses the positive y-axis)
//		------(-1,0)----- 
//		|		^(-coord1)
//		|		|		|	
//		|		|		|	
//	  (-1,0)--+-------(1,0) coord2
//		|		|		|
//		|		|		|
//		|		|		|
//		------(1,0)------
double SimulinkJunctionCoordsToAngle( double coord1, double coord2)
{
	// get arctangent of the upside down coords (x,y)
	double angle= atan( coord2/ -coord1);
	// convert angle so that (-1,0) means 0 angle.
	angle-= PI/2.0;
	angle= warpAngle( -angle);
	return angle;
}

// converts radians to clock
double radianAngleToClock( double phi)
{
	return phi/ (2.0 * PI) * 12.0;
}

// parses  a matlab transition intersection attribute 
void parseIntersection( const std::string& exprIntersection, std::vector< double>& coords)
{
	std::vector< std::string> delims;
	delims.push_back( " ");
	std::vector< std::string> ignores;
	ignores.push_back( "[");
	ignores.push_back( "]");
	Tokenizer intersectionTokenizer( delims, ignores);
	std::string line= exprIntersection;
	while ( false == line.empty())
	{
		std::string token= intersectionTokenizer.tokenize( line);
		coords.push_back( atof( token.c_str()));
	}
}

double processStateIntersection( const std::string& exprIntersection)
{
	std::vector< double> coords;
	parseIntersection( exprIntersection, coords);
	return radianAngleToClock( SimulinkStateCoordsToAngle( coords[ 1], coords[ 2], coords[ 3]));
}

double processJunctionIntersection( const std::string& exprIntersection)
{
	std::vector< double> coords;
	parseIntersection( exprIntersection, coords);
	return radianAngleToClock( SimulinkJunctionCoordsToAngle( coords[ 1], coords[ 2]));
}

///////////////////////////////////////////////////////////////////////////////
// class Tokenizer implementation
std::string Tokenizer::tokenize( std::string& line) const
{
	std::string token;
	std::vector< std::string>::const_iterator itDelims= _delims.begin();
	bool foundToken= false;
	while( !foundToken && ( itDelims!=_delims.end()))
	{
		std::string::size_type findPos= line.find( *itDelims);
		if (findPos!= std::string::npos)
		{
			// delimiter found, get token
			token= line.substr( 0, findPos);
			line= line.substr( findPos+1);
			if ( std::find( _ignores.begin(), _ignores.end(), token) != _ignores.end())
			{
				// token is ignore string
				return tokenize( line);
			}
			// otherwhise return with token without ignores
			token= ignore( token);
			foundToken= true;
		}
		++itDelims;	// try next deliminator
	}
	if ( !foundToken)
	{
		token= ignore( line);
		line.erase();	// no token, clear contents of line.
	}
	return token;
}

std::string Tokenizer::ignore( const std::string& line) const
{
	std::string token = line;
	for ( std::vector< std::string>::const_iterator itIgnores= _ignores.begin(); itIgnores!= _ignores.end(); ++itIgnores)
	{
		std::string::size_type findPos;
		if ( ( findPos= token.find( *itIgnores)) != std::string::npos)
			token= token.erase( findPos, (*itIgnores).length());
	}
	return token;
}
