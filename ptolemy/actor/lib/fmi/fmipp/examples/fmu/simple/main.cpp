using namespace std;

#include <cstdio>
#include <cstdlib>
#include <iostream>

#include "FMU.h"


int main( int argc, char** argv )
{
	string MODELNAME( "Simple" );
	FMU fmu( MODELNAME );

	fmiStatus status = fmu.instantiate( "Simple", fmiFalse );
	fmu.logger( status, "instantiation" );
	if ( status != fmiOK ) cout << "instantiation : " << status << endl;

	status = fmu.setValue("p", 0.1);
	status = fmu.setValue("x", 0.5);

	status = fmu.initialize();
	fmu.logger( status, "initialization" );
	if ( status != fmiOK )  cout << "initialization : " << status << endl;

	fmiReal p_;
	fmiReal x_;
	fmiReal z_;
	double t = 0;
	double commStepSize = 0.1;
	double tstop = 1.0;

	status = fmu.getValue( "x", x_ );
	status = fmu.getValue( "z", z_ );

	printf( "  time      x        z    \n" );
	printf( "%6.3f %8.4f  %8.4f\n",t,x_,z_ );

	while ( t < tstop )
	{
		fmu.integrate(t+commStepSize);

		status = fmu.getValue( "x", x_ );
		status = fmu.getValue( "z", z_ );

		t += commStepSize;
		printf( "%6.3f %8.4f  %8.4f\n",t,x_,z_ );
	}

	cout << "time " << t << endl;

	//// second fmu for, just fun

	FMU fmu2(fmu);

	status = fmu2.instantiate( "Simple2", fmiFalse );
	fmu2.logger( status, "instantiation" );
	if ( status != fmiOK ) cout << "instantiation : " << status << endl;

	status = fmu2.setValue( "p", 2.5 );
	status = fmu2.setValue( "x", .3 );

	status = fmu2.initialize();
	fmu2.logger( status,"initialization" );

	if ( status != fmiOK )  cout << "initialization : " << status << endl;

	t = 0;
	commStepSize = 0.1;
	tstop = 1.0;

	status = fmu2.getValue( "x", x_ );
	status = fmu2.getValue( "z", z_ );

	printf( "  time      x        z    \n" );
	printf( "%6.3f %8.4f  %8.4f\n",t,x_,z_ );

	while ( t < tstop )
	{
		fmu2.integrate( t+commStepSize );

		status = fmu2.getValue( "x", x_ );
		status = fmu2.getValue( "z", z_ );

		t += commStepSize;
		printf( "%6.3f %8.4f  %8.4f\n",t,x_,z_ );
	}

	return 0;
}
