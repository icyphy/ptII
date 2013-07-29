using namespace std;

#include <cstdio>
#include <cstdlib>
#include <iostream>

#include "FMU.h"


int main( int argc, char** argv )
{
	string MODELNAME( "bouncingBallME1" );
	string xmlPath( "bouncingBallME1/modelDescription.xml" );
	string dllPath( "bouncingBallME1/binaries/linux64/bouncingBallME1.so" );
	FMU *fmu = new FMU(xmlPath, dllPath, MODELNAME );

	fmiStatus status = fmu->instantiate( "bouncingBallME1", fmiFalse );
	fmu->logger( status, "instantiation" );
	if ( status != fmiOK ) cout << "instantiation : " << status << endl;

	//status = fmu.setValue("p", 0.1);
	//status = fmu->setValue("x", 0.5);

	status = fmu->initialize();
	fmu->logger( status, "initialization" );
	if ( status != fmiOK )  cout << "initialization : " << status << endl;

	fmiReal h_;
	fmiReal v_;
	double t = 0;
	double commStepSize = 0.1;
	double tstop = 5.0;

	status = fmu->getValue( "h", h_ );
	status = fmu->getValue( "v", v_ );

	printf( "  time      h        v    \n" );
	printf( "%6.3f %8.4f  %8.4f\n",t,h_,v_ );

	while ( t < tstop )
	{
		fmu->integrate(t+commStepSize);

		status = fmu->getValue( "h", h_ );
		status = fmu->getValue( "v", v_ );

		t += commStepSize;
		printf( "%6.3f %8.4f  %8.4f\n",t,h_,v_ );
	}

	cout << "time " << t << endl;

	delete fmu;

	// Try a second FMU
	string MODELNAME2( "dqME1" );
	string xmlPath2( "dqME1/modelDescription.xml" );
	string dllPath2( "dqME1/binaries/linux64/dqME1.so" );
	FMU *fmu2 = new FMU(xmlPath2, dllPath2, MODELNAME2 );

	fmiStatus status2 = fmu2->instantiate( "dqME1", fmiFalse );
	fmu2->logger( status2, "instantiation" );
	if ( status2 != fmiOK ) cout << "instantiation : " << status << endl;

	status2 = fmu2->initialize();
	fmu2->logger( status2, "initialization" );
	if ( status2 != fmiOK )  cout << "initialization : " << status << endl;

	fmiReal x_;
	fmiReal k_;
	double t2 = 0;
	double commStepSize2 = 0.1;
	double tstop2 = 1.0;

	status = fmu2->getValue( "x", x_ );
	status = fmu2->getValue( "k", k_ );

	printf( "  time      x        k    \n" );
	printf( "%6.3f %8.4f  %8.4f\n",t,x_,k_ );

	while ( t2 < tstop2 )
	{
		fmu2->integrate(t2+commStepSize2);

		status = fmu2->getValue( "x", x_ );
		status = fmu2->getValue( "k", k_ );

		t2 += commStepSize2;
		printf( "%6.3f %8.4f  %8.4f\n",t,x_,k_ );
	}

	cout << "time " << t << endl;

	delete fmu2;

	return 0;
}
