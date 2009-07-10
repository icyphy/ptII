// Simple simulation program to illustrate implementation of a client.

/*
********************************************************************
Copyright Notice
----------------

Building Controls Virtual Test Bed (BCVTB) Copyright (c) 2008-2009, The
Regents of the University of California, through Lawrence Berkeley
National Laboratory (subject to receipt of any required approvals from
the U.S. Dept. of Energy). All rights reserved.

If you have questions about your rights to use or distribute this
software, please contact Berkeley Lab's Technology Transfer Department
at TTD@lbl.gov

NOTICE.  This software was developed under partial funding from the U.S.
Department of Energy.  As such, the U.S. Government has been granted for
itself and others acting on its behalf a paid-up, nonexclusive,
irrevocable, worldwide license in the Software to reproduce, prepare
derivative works, and perform publicly and display publicly.  Beginning
five (5) years after the date permission to assert copyright is obtained
from the U.S. Department of Energy, and subject to any subsequent five
(5) year renewals, the U.S. Government is granted for itself and others
acting on its behalf a paid-up, nonexclusive, irrevocable, worldwide
license in the Software to reproduce, prepare derivative works,
distribute copies to the public, perform publicly and display publicly,
and to permit others to do so.


Modified BSD License agreement
------------------------------

Building Controls Virtual Test Bed (BCVTB) Copyright (c) 2008-2009, The
Regents of the University of California, through Lawrence Berkeley
National Laboratory (subject to receipt of any required approvals from
the U.S. Dept. of Energy).  All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

   1. Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
   2. Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in
      the documentation and/or other materials provided with the
      distribution.
   3. Neither the name of the University of California, Lawrence
      Berkeley National Laboratory, U.S. Dept. of Energy nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

You are under no obligation whatsoever to provide any bug fixes,
patches, or upgrades to the features, functionality or performance of
the source code ("Enhancements") to anyone; however, if you choose to
make your Enhancements available either publicly, or directly to
Lawrence Berkeley National Laboratory, without imposing a separate
written license agreement for such Enhancements, then you hereby grant
the following license: a non-exclusive, royalty-free perpetual license
to install, use, modify, prepare derivative works, incorporate into
other computer software, distribute, and sublicense such enhancements or
derivative works thereof, in binary and source code form.

********************************************************************
*/

///////////////////////////////////////////////////////
/// \file   cclient.c
///
/// \brief  Simple simulation program to illustrate
///         implementation of a client.
///
/// \author Michael Wetter,
///         Simulation Research Group, 
///         LBNL,
///         MWetter@lbl.gov
///
/// \date   2007-12-01
///
/// \version $Id$
///
/// This file is a simple simulation program written 
/// in C to illustrate how to implement a client.
/// The program simulates two rooms, each represented
/// by a first order ordinary differential equation
/// that describes the time rate of change of the
/// room temperature.
/// Input to the room model is the control signal
/// for a heater. The control signal is obtained from
/// Ptolemy II. Output of the model is the room 
/// temperature, which is sent to Ptolemy II.
/// The differential equation is solved using an 
/// explicit Euler integration.
///
///////////////////////////////////////////////////////

#include <stdio.h>
#include <stdlib.h>
//#include <unistd.h> // for sleep 
#include "utilSocket.h"

//////////////////////////////////////////////////////
/// Main function
int main(int argc, char *argv[]){
  //////////////////////////////////////////////////////
  // Declare variables for the socket communication
  // File name used to get the port number
  const char *const simCfgFilNam = "socket.cfg";
  // client error flag
  const int cliErrFla = -1;
  // Flags to exchange the status of the simulation program 
  // and of the middleware.
  int flaWri = 0;
  int flaRea = 0;
  // Number of variables to be exchanged
  const int nDblWri = 2;
  const int nIntWri = 0;
  const int nBooWri = 0;
  int nDblRea, nIntRea, nBooRea;
  // Number of rooms
  int nRoo =2;
  // Arrays that contain the variables to be exchanged
  double dblValWri[2];
  int intValWri[1]; // zero array's not allowed for MS compiler
  int booValWri[1]; // zero array's not allowed for MS compiler
  double dblValRea[2];
  int intValRea[1]; // zero array's not allowed for MS compiler
  int booValRea[1]; // zero array's not allowed for MS compiler
  int i, sockfd, retVal;
  // set simulation time step
  double delTim;

  //////////////////////////////////////////////////////
  // Declare variables of the room model
  double simTimWri = 0;
  double simTimRea = 0;
  double TIni   = 10;
  double tau    = 2*3600;
  double Q0Hea  = 100;
  double UA     = Q0Hea / 20;
  double TOut   = 5;
  double C[]    = {tau*UA, 2*tau*UA};
  double TRoo[] = {TIni, TIni};

  double y[]    = {0, 0};
  //////////////////////////////////////////////////////
  if (argc <= 1) {
    printf("Usage: %s simulation_timestep_in_seconds\n", argv[0]);
    return(1);
  }
  delTim = atof(argv[1]);
  fprintf(stderr,"Simulation model has time step %8.5g\n", delTim);
  /////////////////////////////////////////////////////////////
  // Establish the client socket
  sockfd = establishclientsocket(simCfgFilNam);
  if (sockfd < 0){
    fprintf(stderr,"Error: Failed to obtain socket file descriptor. sockfd=%d.\n", sockfd);
    exit((sockfd)+100);
  }

  /////////////////////////////////////////////////////////////
  // Simulation loop
  while(1){
    /////////////////////////////////////////////////////////////
    // assign values to be exchanged
    for(i=0; i < nDblWri; i++)
      dblValWri[i]=TRoo[i];
    for(i=0; i < nIntWri; i++)
      intValWri[i]=0;
    for(i=0; i < nBooWri; i++)
      booValWri[i]=1;

    /////////////////////////////////////////////////////////////
    // Exchange values
    retVal = exchangewithsocket(&sockfd, &flaWri, &flaRea,
				&nDblWri, &nIntWri, &nBooWri,
				&nDblRea, &nIntRea, &nBooRea,
				&simTimWri,
				dblValWri, intValWri, booValWri,
				&simTimRea,
				dblValRea, intValRea, booValRea);
    /////////////////////////////////////////////////////////////
    // Check flags
    if (retVal < 0){
      sendclienterror(&sockfd, &cliErrFla);
      printf("Simulator received value %d when reading from socket. Exit simulation.\n", retVal);
      closeipc(&sockfd);
      exit((retVal)+100);
    }

    if (flaRea == 1){
      printf("Simulator received end of simulation signal from server. Exit simulation.\n");
      closeipc(&sockfd);
      exit(0);
    }

    if (flaRea != 0){
      printf("Simulator received flag = %d from server. Exit simulation.\n", flaRea);
      closeipc(&sockfd);
      exit(1);
    }
    /////////////////////////////////////////////////////////////
    // No flags found that require the simulation to terminate. 
    // Assign exchanged variables
    for(i=0; i < nRoo; i++)
      y[i] = dblValRea[i];

    /////////////////////////////////////////////////////////////
    // Having obtained y_k, we compute the new state x_k+1 = f(y_k)
    // This is the actual simulation of the client, such as an
    // EnergyPlus time step
    for(i=0; i < nRoo; i++)
      TRoo[i] = TRoo[i] + delTim/C[i] * ( UA * (TOut-TRoo[i] ) + Q0Hea * y[i] );
    simTimWri += delTim; // advance simulation time
  } // end of simulation loop
}

