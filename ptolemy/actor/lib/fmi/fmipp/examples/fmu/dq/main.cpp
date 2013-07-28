using namespace std;

#include <cstdio>
#include <cstdlib>
#include <iostream>

#include "FMU.h"


int main( int argc, char** argv )
{
	string MODELNAME( "dqME1" );
	FMU fmu( MODELNAME );

	fmiStatus status = fmu.instantiate( "dqME1", fmiFalse );
	fmu.logger( status, "instantiation" );
	if ( status != fmiOK ) cout << "instantiation : " << status << endl;

	status = fmu.setValue("p", 0.1);
	status = fmu.setValue("x", 0.5);

	status = fmu.initialize();
	fmu.logger( status, "initialization" );
	if ( status != fmiOK )  cout << "initialization : " << status << endl;

	fmiReal x_;
	fmiReal k_;
	double t = 0;
	double commStepSize = 0.1;
	double tstop = 1.0;

	status = fmu.getValue( "x", x_ );
	status = fmu.getValue( "k", k_ );

	printf( "  time      x        k    \n" );
	printf( "%6.3f %8.4f  %8.4f\n",t,x_,k_ );

	while ( t < tstop )
	{
		fmu.integrate(t+commStepSize);

		status = fmu.getValue( "x", x_ );
		status = fmu.getValue( "k", k_ );

		t += commStepSize;
		printf( "%6.3f %8.4f  %8.4f\n",t,x_,k_ );
	}

	cout << "time " << t << endl;

	return 0;
}
