// ---------------------------------------------------------------------------
// CERTI - HLA RunTime Infrastructure
// Copyright (C) 2002  ONERA
//
// This file is part of CERTI
//
// CERTI is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// CERTI is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// $Id: my_timer.cc,v 1.1.1.1 2007/03/29 17:55:36 ausbourg Exp $
// ---------------------------------------------------------------------------

/*----------------------------------------------------------------------------*/
/*                                                                            */
/*  Implementing a Real Time Timer for controlling the federation             */
/*                                                                            */
/*----------------------------------------------------------------------------*/

/*----------------------------------------------------------------------------*/
/*			System includes                                                   */
/*----------------------------------------------------------------------------*/
#include <string.h>

/*----------------------------------------------------------------------------*/
/*			Specific includes                                                 */
/*----------------------------------------------------------------------------*/
// #include "constants.hh"
#include "my_timer.hh"

/*----------------------------------------------------------------------------*/
/*			Forward declarations of functions                                 */
/*----------------------------------------------------------------------------*/
extern "C" void proc_alarm (int e) ;
extern "C" void proc_kill (int e) ;

/*----------------------------------------------------------------------------*/
/*			METHOD : Timer ()				                                  */
/*----------------------------------------------------------------------------*/
/* INPUT PARAMETER                                         		              */
/*									                                          */
/* OUTPUT PARAMETERS 						                                  */
/*									                                          */
/*----------------------------------------------------------------------------*/
/* ROLE								  	                                      */
/*      Class Constructor                                                     */
/*                                                                            */
/*----------------------------------------------------------------------------*/
Timer::Timer() {
     /*----- Initialize the struct timer and variables -----*/	
     memset(&ontimer, (char)0, sizeof(ontimer)); 
     memset(&offtimer, (char)0, sizeof(offtimer)); 

     /*----- Programs the action to make in different cases -----*/
     act_alarm.sa_handler = proc_alarm;
     act_alarm.sa_flags = SA_RESTART;
     act_kill.sa_handler = proc_kill;  
     act_kill.sa_flags = SA_RESTART;

     /*----- Set mode in default mode -----*/
     mode = DEFAULT;

     /*-----Indicates the action to do if Ctrl-C is hit by the user -----*/
     sigaction(SIGINT, &act_kill, NULL);

     /*-----Indicates the action to do when alarm is emitted by timer -----*/
     sigaction(SIGALRM, &act_alarm, NULL);

     /*-----Init the stop parameters for the timer -----*/
     offtimer.it_interval.tv_sec = 0 ;
     offtimer.it_interval.tv_usec = 0 ; 
     offtimer.it_value.tv_sec = 0 ;
     offtimer.it_value.tv_usec = 0 ;
}

/*----------------------------------------------------------------------------*/
/*			METHOD : ~Timer ()				      */
/*----------------------------------------------------------------------------*/
/* INPUT PARAMETER                                         		      */
/*									      */
/* OUTPUT PARAMETERS 						              */
/*									      */
/*----------------------------------------------------------------------------*/
/* ROLE								  	      */
/*      Class Destructor
/*                                                                            */
/*----------------------------------------------------------------------------*/
Timer::~Timer(){ }

/*----------------------------------------------------------------------------*/
/*			METHOD : initTimer ()				      */
/*----------------------------------------------------------------------------*/
/* INPUT PARAMETER                                         		      */
/*     By Value                                                               */
/*         int inte_sec   The interval duration between ticks in seconds      */
/*         int inte_usec  The same in micro seconds                           */
/*         int val_sec    Time to next tick in seconds when set               */
/*         int val_usec   The same in micro seconds                           */   
/*									      */
/* OUTPUT PARAMETERS 						              */
/*									      */
/*----------------------------------------------------------------------------*/
/* ROLE								  	      */
/*      Programming the timer. The inte_sec and inte_usec permit to define    */
/*      the time interval between two ticks of the timer. This duration is    */
/*      expressed in seconds and microseconds. The val_sec and val_usec       */
/*      parameters indicate in how many time the timer first expires after    */
/*      it has been set by setitimer()                                        */
/*                                                                            */
/*----------------------------------------------------------------------------*/
void Timer::initTimer(int inte_sec,int inte_usec,int val_sec,int val_usec) {
  /*-----Set the timer struct with the parameters-----*/
  ontimer.it_interval.tv_sec = inte_sec ;
  ontimer.it_interval.tv_usec = inte_usec ; 
  ontimer.it_value.tv_sec = val_sec ;
  ontimer.it_value.tv_usec = val_usec ;
}

/*----------------------------------------------------------------------------*/
/*			METHOD : startTimer ()				      */
/*----------------------------------------------------------------------------*/
/* INPUT PARAMETER                                         		      */
/*									      */
/* OUTPUT PARAMETERS 						              */
/*									      */
/*----------------------------------------------------------------------------*/
/* ROLE								  	      */
/*      Starts the timer
/*      the time interval between two ticks of the timer. This duration is    */
/*      expressed in seconds and microseconds. The val_sec and val_usec       */
/*      parameters indicate in how many time the timer first expires after    */
/*      it has been set by setitimer()                                        */
/*                                                                            */
/*----------------------------------------------------------------------------*/
void Timer::startTimer( ){
  /*-----Initializes or reinitializes values -----*/
  nbTriggers = 0 ; 
  toRearm = 0;
  rtElapsedOk = 0;

  /*-----Starts the real timer -----*/
  setitimer(ITIMER_REAL, &ontimer, 0);
}

/*----------------------------------------------------------------------------*/
/*			METHOD : stopTimer ()				      */
/*----------------------------------------------------------------------------*/
/* INPUT PARAMETER                                         		      */
/*									      */
/* OUTPUT PARAMETERS 						              */
/*									      */
/*----------------------------------------------------------------------------*/
/* ROLE								  	      */
/*      Stops the timer
/*                                                                            */
/*----------------------------------------------------------------------------*/
void Timer::stopTimer( ){

 /*-----Sets the timer to null -----*/
  setitimer(ITIMER_REAL, &offtimer, 0);
}

/*----------------------------------------------------------------------------*/
/*			METHOD : beginAppli ()				      */
/*----------------------------------------------------------------------------*/
/* INPUT PARAMETER                                         		      */
/*									      */
/* OUTPUT PARAMETERS 						              */
/*									      */
/*----------------------------------------------------------------------------*/
/* ROLE								  	      */
/*      This method programs the mode of the timer in mode Appli. This fixes  */
/*      the action to do when SIGALRM is emitted, due to                      */
/*      a trigger of the real  time clock while a federate is in a computing  */
/*      phase. In that case, the cycle is not finished in the elapsed time    */
/*      and the SIGALRM signal is an error to be caught                       */      
/*                                                                            */
/*----------------------------------------------------------------------------*/
void Timer::beginAppli( ){
  /*----- action to perform if SIGALRM during application-----*/
  mode = APPLI;
}

/*----------------------------------------------------------------------------*/
/*			METHOD : beginWaiting ()			      */
/*----------------------------------------------------------------------------*/
/* INPUT PARAMETER                                         		      */
/*									      */
/* OUTPUT PARAMETERS 						              */
/*									      */
/*----------------------------------------------------------------------------*/
/* ROLE								  	      */
/*      This method programs the mode of the timer in mode WAIT . This fixes  */
/*      the action to do when SIGALRM is emitted, due to                      */
/*      a trigger of the real time clock while a federate is waiting for it   */
/*                                                                            */
/*----------------------------------------------------------------------------*/
void Timer::beginWaiting( ){
  /*-----Action to take when SIGALRM is emitted while waiting for it -----*/
  mode = WAIT;
}

/*----------------------------------------------------------------------------*/
/*			METHOD : beginSynchro ()			      */
/*----------------------------------------------------------------------------*/
/* INPUT PARAMETER                                         		      */
/*									      */
/* OUTPUT PARAMETERS 						              */
/*									      */
/*----------------------------------------------------------------------------*/
/* ROLE								  	      */
/*      This method programs the mode of the timer in mode SYNCHRO. This fixes*/
/*      the action to do when SIGALRM is emitted, due to                      */
/*      a trigger of the real time clock while a federate is beginning its    */
/*      synchronization with other federates.                                 */
/*                                                                            */
/*----------------------------------------------------------------------------*/
void Timer::beginSynchro( ) {
  if ( toRearm ) {
    /*-----The timer must be rearmed because it has previously elapsed-----*/
    #if defined(TRACE) || defined(DEBUG)
    cout << time_to_str ( NULL, "Rearming !! ") << endl ;
    #endif
    
    startTimer();
  }

  /*-----reprograms actions to be taken -----*/
  mode = SYNCHRO; 
}

/*----------------------------------------------------------------------------*/
/*			METHOD : beginReceive ()			      */
/*----------------------------------------------------------------------------*/
/* INPUT PARAMETER                                         		      */
/*									      */
/* OUTPUT PARAMETERS 						              */
/*									      */
/*----------------------------------------------------------------------------*/
/* ROLE								  	      */
/*      This method programs the mode of the timer in mode RECEIPT. This fixes*/
/*      the action to do when SIGALRM is emitted, due to                      */
/*      a trigger of the real time clock while a federate is beginning its    */
/*      receiving phase of data from other federates.                         */
/*                                                                            */
/*----------------------------------------------------------------------------*/
void Timer::beginReceive( ) {

  /*-----reprograms actions to be taken -----*/
  mode = RECEIPT;
}


/*----------------------------------------------------------------------------*/
/*			METHOD : beginSend ()		        	      */
/*----------------------------------------------------------------------------*/
/* INPUT PARAMETER                                         		      */
/*									      */
/* OUTPUT PARAMETERS 						              */
/*									      */
/*----------------------------------------------------------------------------*/
/* ROLE								  	      */
/*      This method programs the mode of the timer in mode SEND. This fixes   */
/*      the action to do when SIGALRM is emitted, due to                      */
/*      a trigger of the real time clock while a federate is beginning its    */
/*      sending phase of data from other federates.                           */
/*                                                                            */
/*----------------------------------------------------------------------------*/
void Timer::beginSend( ) {

  /*-----reprograms actions to be taken -----*/
  mode = SEND;
}

/*----------------------------------------------------------------------------*/
/*			FUNCTION : proc_alarm ()			      */
/*----------------------------------------------------------------------------*/
/* INPUT PARAMETER                                         		      */
/*     By Value                                                               */
/*          int e                                                             */
/*									      */
/* OUTPUT PARAMETERS 						              */
/*									      */
/*----------------------------------------------------------------------------*/
/* ROLE								  	      */
/*      This function programs the action to be taken  when the real timer    */
/*      elapsed during the receipt phase. The fact that the                   */
/*      timer has triggered this function is registered by incrementing the   */
/*      nbTriggers variable. The fact that the timer has                      */
/*      elapsed in receipt phase is registered  by setting the variable       */
/*      toRearm                                                               */  
/*                                                                            */
/*----------------------------------------------------------------------------*/
void proc_alarm  (int e) {

  /*-----Stops the Timer-----*/
  T.stopTimer();
  
  switch (T.mode) 
    {
    case RECEIPT :
      /*-----This can occur at the beginning of cycles -----*/
      #if defined(TRACE) || defined(DEBUG)
      cout << time_to_str (NULL, 
			   "***DRIIING*** Elapsed Time during Receive Phase"
			  ) 
	   << endl ; 
      #endif
      nbTriggers++;
      break;

    case WAIT :
      #if defined(TRACE) || defined(DEBUG)
      cout << time_to_str ( NULL, 
			    "***DRIIING*** Elapsed Time during Waiting Phase") 
	   << endl ; 
      #endif
      /*----Registering the timer elpased at the end of the cycle : Ok-----*/
      rtElapsedOk ++ ;
      break;
    
    case SYNCHRO :
      /*-----This can occur at the beginning of cycles -----*/
      #if defined(TRACE) || defined(DEBUG)
      cout << time_to_str ( NULL, 
			    "***DRIIING*** Elapsed Time during Synchro Phase") 
	   << endl ; 
      #endif
      nbTriggers++;
      break;

    case APPLI :
      /*-----Trace of the error-----*/
      #if defined(TRACE) || defined(DEBUG)
      cout << time_to_str (NULL, 
			   "***DRIIING*** : Elapsed Time during Computing phase") 
	   << endl ; 
      #endif
      nbTriggers++;
      break;

    case SEND :
      /*-----Trace of the error-----*/
      #if defined(TRACE) || defined(DEBUG)
      cout << time_to_str (NULL, 
			   "***DRIIING*** : Elapsed Time during Send Phase") 
	   << endl ; 
      #endif
      nbTriggers++;
      break;
    }
  /*----Register the fact that the timer must be restarted-----*/
  toRearm = 1 ;  
}

/*----------------------------------------------------------------------------*/
/*			FUNCTION : proc_kill () 			      */
/*----------------------------------------------------------------------------*/
/* INPUT PARAMETER                                         		      */
/*     By Value                                                               */
/*          int e                                                             */
/*									      */
/* OUTPUT PARAMETERS 						              */
/*									      */
/*----------------------------------------------------------------------------*/
/* ROLE								  	      */
/*      This function programs the action to be taken  when the user hits     */
/*      The Ctrl-C key at the console and kills the federation                */
/*                                                                            */
/*----------------------------------------------------------------------------*/
void proc_kill (int e) {
  /*-----Trace the event-----*/
  #if defined(TRACE) || defined(DEBUG)
  cout << time_to_str ( NULL, "( timer ) ^C recu") << endl ; 
  #endif

  /*-----Goodbye everybody -----*/
  exit (-1);
}

