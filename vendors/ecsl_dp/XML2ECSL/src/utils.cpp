#include "utils.h"
#include <iostream>	// for std::cout

std::string mangle( const std::string& expr)
{
	using std::string;
	string theVal= expr;
	string::size_type ip;

	ip = theVal.find("\\n");
	while(ip != string::npos)
	{
		theVal.replace(ip,2,"__");
		ip = theVal.find("\\n",++ip);
	}

	ip = theVal.find("\\t");
	while(ip != string::npos)
	{
		theVal.replace(ip,2,"__");
		ip = theVal.find("\\t",++ip);
	}

	ip = theVal.find("\\r");
	while(ip != string::npos)
	{
		theVal.replace(ip,2,"__");
		ip = theVal.find("\\r",++ip);
	}

	ip = theVal.find("\\f");
	while(ip != string::npos)
	{
		theVal.replace(ip,2,"__");
		ip = theVal.find("\\f",++ip);
	}

	ip = theVal.find_first_of(" !@#$%^&*-+=\\/\'\"{}[];:.,?|()");
	while(ip != string::npos)
	{
		theVal.replace(ip,1,"_");
		ip = theVal.find_first_of(" !@#$%^&*-+=\\/\'\"{}[];:.,?|()",++ip);
	}

	return(theVal);
}


void ParsePoints( const std::string& pointsExpr, std::list< std::string>& parsedPoints)
{
	using std::string;
	string::size_type startPos = pointsExpr.find("[");
	string::size_type commaPos = startPos;
	string::size_type semiPos = startPos;
	string::size_type stopPos;
	string xVal;
	string yVal;
	string pairVal;
	bool done = false;
	do {
		commaPos = pointsExpr.find(",", commaPos + 1);
		semiPos = pointsExpr.find(";", semiPos + 1);
		if(semiPos == string::npos) {
			stopPos = pointsExpr.find("]");
			done = true;
			if(stopPos == string::npos) {
				// no points to parse, so return
				break;
			}
		}
		xVal = pointsExpr.substr(startPos+1,commaPos-(startPos+1));
		startPos = semiPos;
		if(!done) {
			yVal = pointsExpr.substr(commaPos+1,semiPos-(commaPos+1));
		} else {
			yVal = pointsExpr.substr(commaPos+1,stopPos-(commaPos+1));
		}
		parsedPoints.push_back( MakeGMEPosString( xVal, yVal));

	} while(!done);
}

std::string ExtractChartName( const std::string& extendedchartname)
{
	std::string::size_type pos= extendedchartname.find_last_of("/");
	if ( std::string::npos == pos)
		return extendedchartname;
	return extendedchartname.substr(pos + 1, extendedchartname.length() - pos);
}

std::string MakeGMEPosString( int x, int y)
{
	return MakeGMEPosString( num_to_string( x), num_to_string( y));
}

std::string MakeGMEPosString( const std::string& x, const std::string& y)
{
	return "(" + x + "," + y + ")";
}

void ParseGMEPosString( const std::string& expr, int& x, int& y)
{
	using std::string;
	string::size_type lBracketPos = expr.find( "(");
	string::size_type commaPos = expr.find( ",");
	string::size_type termPos = expr.find( ")", commaPos+1);
	string x_str = expr.substr( lBracketPos+1, commaPos - ( lBracketPos+1));
	string y_str = expr.substr( commaPos+1, termPos - ( commaPos + 1));
	x = atoi( x_str.c_str());
	y = atoi( y_str.c_str());
}

void ParseMatlabPosString( 
	const std::string& expr, 
	const std::string& firstLimiter, 
	const std::string& secondLimiter, 
	const std::string& thirdLimiter,
	int& x, 
	int& y
	)
{
	using std::string;
	string::size_type firstPos = expr.find( firstLimiter);
	string::size_type secondPos = expr.find( secondLimiter, firstPos+1 );
	string::size_type thirdPos = expr.find( thirdLimiter, secondPos+1);
//std::cout << firstPos << ", " << secondPos << ", " << thirdPos << ", " << expr.length() << std::endl;
	string x_str = expr.substr( firstPos+1, secondPos - ( firstPos+1));
	string y_str = expr.substr( secondPos+1, thirdPos - ( secondPos + 1));
	x = atoi( x_str.c_str());
	y = atoi( y_str.c_str());

}

void ParseMatlabPosString( const std::string& expr, int& x, int& y)
{
	ParseMatlabPosString( expr, "[", ",", "]", x, y);
}

void breakDownSystemPath( const std::string& sysPath, std::vector< std::string>& sysElems)
{
	using std::string;
	const string SYS_PATH_SEPARATOR= "/";
	string path= sysPath;
	while ( !path.empty()) {
		string::size_type pos= path.find_first_of( SYS_PATH_SEPARATOR);
		if ( pos != string::npos) {
			// break elem from the front
			if ( pos != 0)	// if elem is not empty 
				sysElems.push_back( path.substr( 0, pos));
			path= path.substr( pos+ 1, path.length()- pos); // OK if path ends with "/"
		} else {
			sysElems.push_back( path);
			path.erase();
		}
	}
}
