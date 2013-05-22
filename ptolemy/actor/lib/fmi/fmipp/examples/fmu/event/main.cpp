using namespace std;

#include <cstdio>
#include <cstdlib>
#include <iostream>

#include "FMU.h"


int main( int argc, char** argv )
{
	string MODELNAME( "Events" );
	FMU fmu( MODELNAME );

	fmiStatus status = fmu.instantiate( "Events1", fmiFalse );
	fmu.logger( status,"instantiation" );
	if ( status != fmiOK ) cout << "instantiation : " << status << endl;

	status = fmu.setValue( "k", 10.0 );
	status = fmu.setValue( "x", 1.0 );

	status = fmu.initialize();
	fmu.logger( status, "initialization" );
	if ( status != fmiOK )  cout << "initialization : " << status << endl;

	fmiReal x_;
	fmiReal z_;

	size_t  nevents = fmu.nEventInds();
	fmiReal* eventsind;
	eventsind = new fmiReal[nevents];

	double t = 0;
	double commStepSize = 0.0001;
	double tstop = 1.0;

	status = fmu.getValue( "x", x_ );
	status = fmu.getValue( "z", z_ );
	status = fmu.getEventIndicators( eventsind );

	printf( "%6.3f,%8.4f,%8.4f,%8.4f\n",t,x_,z_,eventsind[0] );

	while ( t < tstop )
	{
		fmu.integrate( t+commStepSize );

		status = fmu.getValue( "x", x_ );
		status = fmu.getValue( "z", z_ );
		status = fmu.getEventIndicators( eventsind );

		t += commStepSize;
		printf( "%6.3f,%8.4f,%8.4f,%8.4f\n",t,x_,z_,eventsind[0] );
	}

	delete[] eventsind;

	return 0;
}
