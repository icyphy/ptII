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
// $Id$
// ---------------------------------------------------------------------------
#ifndef FC_TIMER_HH
#define FC_TIMER_HH

/*----------------------------------------------------------------------------*/
/*			Basic System includes     			      */
/*----------------------------------------------------------------------------*/
#include <stdio>
#include <signal>
#include <sys/time>
#include <errno>
#include <iostream>
#include <unistd>

/*----------------------------------------------------------------------------*/
/*   Struct sigaction defining actions to perform on signal occurrences       */
/*----------------------------------------------------------------------------*/
static  struct sigaction act_alarm ;	             /* Action in operation   */
static  struct sigaction act_kill ;	             /* When killed by Ctrl-C */


/*----------------------------------------------------------------------------*/
/*                 Commands of the Timer                                      */
/*----------------------------------------------------------------------------*/
#define DEFAULT          0x0000                       /* Default mode         */
#define SYNCHRO          0x0001                       /* Synchro phase        */
#define RECEIPT          0x0002                       /* Receipt phase        */
#define APPLI            0x0004                       /* Application phase    */
#define WAIT             0x0008                       /* Waiting phase        */     
#define SEND             0x0010                       /* Sending phase        */     

/*----------------------------------------------------------------------------*/
/*                 Printing the time                                          */
/*----------------------------------------------------------------------------*/
extern char * time_to_str (struct timeval *tp, const  char * mess) ;

/*----------------------------------------------------------------------------*/
/*				CLASS : Timer                                                 */
/*----------------------------------------------------------------------------*/
/* FUNCTION :                                                                 */
/* ---------								                                  */
/*      This class implements a real time timer to control the whole          */
/*      federation.                                                           */
/*									                                          */
/*----------------------------------------------------------------------------*/
class Timer{

    public:
        struct itimerval ontimer;	             /* Parameters on         */
        struct itimerval offtimer;	             /* Parameters off        */
        Timer();                                 /* Constructor           */      
        ~Timer();                                /* Destructor            */
	                                             /* Programming timer     */
        void initTimer(int inte_sec,             /* interval in seconds   */               
                       int inte_usec,            /* in micro seconds      */
                       int val_sec,              /* every val_sec seconds */
                       int val_usec);            /* every micro-seconds   */
        void startTimer ( );                     /* Start the timer       */
        void stopTimer ( ) ;                     /* Stop it               */    
        void beginAppli ( );                         
        void beginWaiting ( );
        void beginSynchro ( );
        void beginReceive ( );
        void beginSend ( );
        int mode ;                               /* Functionning mode     */   
}; //Timer

/*----------------------------------------------------------------------------*/
/*                 Control Data of the Timer                                  */
/*----------------------------------------------------------------------------*/
extern int nbTriggers;                                /* # of triggers        */
extern int toRearm;                                   /* timer to rearm       */
extern int rtElapsedOk;                               /* time elapsed ok      */
extern Timer T;                                       /* The real timer       */

#endif // FC_TIMER_HH
// $Id$
