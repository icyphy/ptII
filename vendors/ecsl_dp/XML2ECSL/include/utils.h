#ifndef UTILS_H
#define UTILS_H

#include <string>
#include <sstream>
#include <list>
#include <vector>

const int GME_X_SCALE_FACTOR= 3;
const int GME_Y_SCALE_FACTOR= 3;
const int GME_X_OFFSET= 225;
const int GME_Y_OFFSET= 100;
const int GME_X_DEFAULT_POS= 400;
const int GME_Y_DEFAULT_POS= 400;


//
class UseXSD
{
public:
	// Returns true if the file 'fname' uses dtd file to open/ create.
	bool operator()( const std::string& fname)
	{
		std::string ext( fname);
		std::string::size_type findExt= ext.rfind( '.');
		if ( findExt != std::string::npos)
			if( *(ext.begin()+ findExt+ 1) != '\\')
				ext.erase( ext.begin(), ext.begin()+ ++findExt);	// delete '.' too.
			else
				ext.clear();
		return ext == "xml";
	}
};

//
extern std::string mangle( const std::string& expr);
//
extern void ParsePoints( const std::string& pointsExpr, std::list< std::string>& parsedPoints);
//
extern std::string ExtractChartName( const std::string& extendedchartname);
//
extern std::string MakeGMEPosString( const std::string& x, const std::string& y);
extern std::string MakeGMEPosString( int x, int y);
//
extern void ParseGMEPosString( const std::string& expr, int& x, int& y);
//
extern void ParseMatlabPosString( 
	const std::string& expr, 
	const std::string& firstLimiter, 
	const std::string& secondLimiter, 
	const std::string& thirdLimiter,
	int& x, 
	int& y
	);
// same as the previous with limiters: "[", ",", "]" respectively.
extern void ParseMatlabPosString( const std::string& expr, int& x, int& y);
//
extern void breakDownSystemPath( const std::string& sysPath, std::vector< std::string>& sysElems);
//
template< class T>
std::string num_to_string( T i)
{
	std::ostringstream oss;
	oss << i;
	return oss.str();
}

#endif //UTILS_H
