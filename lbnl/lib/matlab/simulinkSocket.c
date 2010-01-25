/**
 *
 * \file   simulinkSocket.c
 *
 * \brief  Methods for interfacing Simulink
 *         using BSD sockets.
 *
 * \author Michael Wetter,
 *         Simulation Research Group, 
 *         LBNL,
 *         MWetter@lbl.gov
 *
 * \date   2007-12-01
 *
 * This file provides methods that allows Simulink to
 * establish a socket connection. The file
 * \c compile.m is using this file.
 *
 */

#include "simulinkSocket.h"

int16_T establishBSDSocket(int16_T* flag){
  int retVal;
  if ( *flag == 0 ){
    retVal = establishclientsocket("socket.cfg");
  }
  else
    retVal = *flag;
  return retVal;
}

int16_T exchangeDoublesWithBSDSocket(int16_T* sockfd,
			      int16_T* flaWri, 
		              int16_T* flaRea,
         		      int16_T* nDblWri,
			      double* simTimWri,
			      double dblValWri[], 
			      double* simTimRea,
			      double dblValRea[]){
  int mySockfd = *sockfd;
  int myFlaWri = *flaWri;
  int myFlaRea = 0;
  int myNDblWri = *nDblWri;
  int nDblRea;
  int retVal = exchangedoubleswithsocket(&mySockfd, 
					 &myFlaWri, &myFlaRea,
					 &myNDblWri,
					 &nDblRea,
					 simTimWri,
					 dblValWri,
					 simTimRea,
					 dblValRea);
  /* NDBLMAX is defined in lib/defines.h */
  if ( nDblRea > NDBLMAX ){
    fprintf(stderr, "simulinkSocket: Read too many double values.\n");
    fprintf(stderr, "   Received nDblRea = %d, maximum is %d\n", nDblRea, NDBLMAX);
    fprintf(stderr, "   To fix, change NDBLMAX in lib/defines.h and recompile simulink interface.\n");
    retVal = -1;
  }
  *flaRea = myFlaRea;
  return retVal;
}

int16_T closeBSDSocket(int16_T* sockfd, int16_T* doClose){
  if ( (0 == *doClose) || (*sockfd < 0) ){ /* do not close socket */
    return 0;
  }
  else{
    int mySockfd = *sockfd;
    return closeipc(&mySockfd);
  }
}
