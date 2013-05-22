using namespace std;

#include <cstdio>
#include <cstdlib>
#include <iostream>

#include "IncrementalFMU.h"


int main( int argc, char** argv )
{
	std::string foo[2] = { "k", "x" };
	double bar[2] = { 10, 1 };
	double* state;
	double* result;
	double time=0.0;
	double next=100;
	double oldnext;

	std::string none[0];
	std::string foobar[2] = { "x", "z" };

	const double stepsize=0.001;

	fmiReal p_;
	fmiReal x_;
	fmiReal z_;

	IncrementalFMU fmu("Events", none, 0, foobar, 2);

	fmu.init("Events1", foo, bar, 2, 0, 2*stepsize, stepsize, stepsize/2);

	next=fmu.sync(-42,0);
	state = fmu.getCurrentState();
	result = fmu.getCurrentOutputs();
	cout << 0 << "," << result[0] << "," << result[1] << "," << state[0] << endl;

	time = 0.;

	while ( time < 1. )
	{
		oldnext = next;
		next = fmu.sync( time, ( time+stepsize ) > next ? next : ( time+stepsize ) );
		state = fmu.getCurrentState();
		result = fmu.getCurrentOutputs();
		cout << time << "," << result[0] << "," << result[1] << "," << state[0] << endl;
		time = ( time+stepsize ) > oldnext ? oldnext : ( time+stepsize );
	}

	return 0;

}
