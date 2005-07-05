#ifndef MATLAB_INTERSECTION2CLOCK_H
#define MATLAB_INTERSECTION2CLOCK_H

#include <string>
#include <vector>

extern double processStateIntersection( const std::string& exprIntersection);
extern double processJunctionIntersection( const std::string& exprIntersection);

// General tokenizer class
class Tokenizer
{
public:
	Tokenizer( const std::vector< std::string>& delims, const std::vector< std::string>& ignores)
		: _delims( delims), _ignores( ignores)
	{}
	Tokenizer( const std::string& delim, const std::string& ignore= "")
	{
		_delims.push_back( delim);
		if ( !ignore.empty())
			_ignores.push_back( ignore);
	}
	std::string tokenize( std::string& line) const;

protected:
	std::string ignore( const std::string& line) const;

private:
	// members
	std::vector< std::string> _delims;
	std::vector< std::string> _ignores;
	// no copy
	Tokenizer( const Tokenizer&) {}
	Tokenizer& operator=( const Tokenizer&) {}
};

#endif //MATLAB_INTERSECTION2CLOCK_H
