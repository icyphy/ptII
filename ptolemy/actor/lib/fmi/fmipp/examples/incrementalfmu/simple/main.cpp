using namespace std;

#include <cstdio>
#include <cstdlib>
#include <iostream>

#include "IncrementalFMU.h"


int main( int argc, char** argv )
{
	std::string foo[2] = { "p", "x" };
	double bar[2] = { 0.1, 0.5 };
	double* result;

	cout << "creating new IncrementalFMU \"Simple\"" << endl;

	string MODELNAME( "Simple" );
	IncrementalFMU fmu( MODELNAME, 2, 0 );

	cout << "calling fmu.init(foo, bar, 2, 0, 1, 0.1, 0.05)" << endl;
	fmu.init( "Simple1", foo, bar, 2, 0, 1, 0.1, 0.05 );

	cout << "calling fmu.sync(-42, 0) \t";
	fmu.sync( -42,0 );
	cout << "result: ";
	result = fmu.getCurrentState();
	for ( int k = 0; k < 1; ++k )
		cout << "state " << k << ": " << result[k] << endl;


	for ( int j = 0; j < 5; ++j )
	{
		cout << "calling fmu.sync(" << (double)j/10 << ", " << (double)(j+1)/10 << ") \t";
		fmu.sync( (double)j/10, (double)(j+1)/10 );
		cout << "result: ";
		result = fmu.getCurrentState();
		for( int k = 0; k < 1; ++k )
			cout << "state " << k << ": " << result[k] << endl;
	}

	return 0;
}
