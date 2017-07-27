/**
 * Federation PRISE
 * ----------------
 * @file Joystick_Fed_Main.cc
 *
 */

/*----------------------------------------------------------------------------*/
/*			RTI specific includes                                 */
/*----------------------------------------------------------------------------*/
#include <RTI.hh>
#include <NullFederateAmbassador.hh>
/* Certi Message Buffer special include to correctly convert double to int */
/* Heful to use certi::MessageBuffer */
#include <MessageBuffer.hh>

/*
 * <fedtime.hh>
 * The header provides virtual methods for time Management services
 * implemented.
 */
#include <fedtime.hh>

/*-----------------------------------------------------------------------------*/
/*			System standard includes                               */
/*-----------------------------------------------------------------------------*/
#include <iostream>
#include <fstream>
#include <stdlib.h>
#include <memory>
#include <string>
#include <string.h>

/*-----------------------------------------------------------------------------*/
/*			System scheduling includes                             */
/*-----------------------------------------------------------------------------*/
// #ifdef R_LINUX
#include <time.h>
#include <sys/mman.h>
#include <sched.h>

/*-----------------------------------------------------------------------------*/
/*			                             */
/*-----------------------------------------------------------------------------*/
#include "Joystick_Fed.hh"  
#include "Common.hh"   

/* Pour les mesures de temps */
#include "Timer.hh" 

/*For real Time timer */
#include <time.h>
#include <signal.h>
     

/*-----------------------------------------------------------------*/
/*			Name space used ....                                   */
/*-----------------------------------------------------------------*/
using std::string;
using std::cout;
using std::cin;
using std::cerr;
using std::endl;
using std::auto_ptr;
/*-----------------------------------------------------------------*/
/*			Beginning of the main                                  */
/*-----------------------------------------------------------------*/
int main(int argc, char *argv[]) {

/*******************************************************************/
/************************* DECLARATIONS ****************************/
/*******************************************************************/

/* Les noms Federation, federe et fichier fed */
    string federationName =  getFederationName(DEFAULT_XML_PATH);//"PRISE_V2";
    string federateName   =  getFederateName(DEFAULT_XML_PATH, "JOYSTICK");//"Joystick_Fed";
    string fedFile        =  getFedFile(DEFAULT_XML_PATH); //"PRISE_V2.fed";

/* RTI Ambassadeur et Federe Ambassadeur */
    RTI::RTIambassador myRtiAmb ; // RTI Ambassador
    Joystick_Fed myFedAmb ; // Federate Ambassador

/* Message Buffer to cast local variable to Char* */
    libhla::MessageBuffer CertiMessagebuffer;

/* Declaration des données de simulation */
    unsigned int NbTotalcycles = NB_CYCLES_50_HZ, NbCycles = 1;

/* Test for real time timer */
    struct itimerspec       itime;
    timer_t                 timer_id;
    timer_create(CLOCK_REALTIME, NULL, &timer_id);

/* we will receive our pulse 
 * in 0 seconds (the itime.it_value) and every 20ms 
 * thereafter (the itime.it_interval)
 */
    itime.it_value.tv_sec = 1;
    itime.it_value.tv_nsec = 0; 
    itime.it_interval.tv_sec = 1;
/* 20 million nsecs = 20 millisecond = 0.02 secs */
    itime.it_interval.tv_nsec = 20000000; 

/* Pour les mesures de temps */
    Timer Timer ;
/* Variable tick pour les instants */
    double tick0, tick1 ;
    double *d_cycles,*d_commutation, *d_calcul, *d_envoi, *d_recep ; 
    d_cycles = new double[NbTotalcycles];

    struct timespec timeout0;
    struct timespec timeout1;
    struct timespec* tmp;
    struct timespec* t0 = &timeout0;
    struct timespec* t1 = &timeout1;


/************************************************************************/
/******************    Phase de Creation I  *****************************/
/************************************************************************/
/****************** (I.1) Creation de la fédération *********************/
/****************** (I.2) Adhésion à la fédération **********************/
/************************************************************************/
    cout << "Start" <<endl;
    //cout << federationName <<endl;
    cout << fedFile.c_str() <<endl;
    cout << "Fin start" <<endl;   

    /* (I.1) create federation execution */
    try {
        myRtiAmb.createFederationExecution(federationName.c_str(),
                                           fedFile.c_str());				        
		cout << ">> Federation \"" << federationName.c_str() << "\" created." << endl;				   
    } catch ( RTI::FederationExecutionAlreadyExists ) {
        cout << ">> Federation \"" << federationName.c_str() << "\" already created by another federate." << endl;
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }

    /* (I.2) join federation execution */
    try {
        myRtiAmb.joinFederationExecution(federateName.c_str(),
                                         federationName.c_str(),
                                         &myFedAmb);
        cout << ">> Federation \"" << federationName.c_str() << "\" joined by federate \"" << federateName.c_str() << "\"." << endl;
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }

/***********************************************************************************/
/******************    Phase d'Initialisation II       *****************************/
/***********************************************************************************/
/** (II.0) Récupération des Handles des Classes d'Objet et des Attributs ***********/
/** (II.1) Déclaration des intentions de publication de souscription ***************/
/** (II.2) Enregistrement des objets simulés ***************************************/
/** (II.3) Mise en place de la politique de gestion du temps  **********************/
/** (II.4) Phase de synchronisation initiale entre les fédérés *********************/
/***********************************************************************************/

  /* (II.0) get object class handle for Subscribe */

    try {
        myFedAmb.Set_USER_DATA_ClassHandle(      	myRtiAmb.getObjectClassHandle("USER_DATA"));
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }
  /* (II.0) get object class handle for Publish */
  
    try {
        myFedAmb.Set_JOYSTICK_ClassHandle(myRtiAmb.getObjectClassHandle("JOYSTICK"));
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }
	
  /* (II.0) get attribute handle for Subscribe */
  
    try {
        	myFedAmb.Set_ORDER_ATTRIBUTE(					myRtiAmb.getAttributeHandle("ORDER",myFedAmb.Get_USER_DATA_ClassHandle()));             
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }
  /* (II.0) get attribute handle for Publish */
  
    try {
        myFedAmb.Set_AILERON_ATTRIBUTE(  myRtiAmb.getAttributeHandle("AILERON",myFedAmb.Get_JOYSTICK_ClassHandle()));
        myFedAmb.Set_ELEVATOR_ATTRIBUTE( myRtiAmb.getAttributeHandle("ELEVATOR", myFedAmb.Get_JOYSTICK_ClassHandle()));
        myFedAmb.Set_RUDDER_ATTRIBUTE(   myRtiAmb.getAttributeHandle("RUDDER", myFedAmb.Get_JOYSTICK_ClassHandle()));
        myFedAmb.Set_THROTTLE_LEFT_ATTRIBUTE( myRtiAmb.getAttributeHandle("THROTTLE_LEFT", myFedAmb.Get_JOYSTICK_ClassHandle()));
        myFedAmb.Set_THROTTLE_RIGHT_ATTRIBUTE( myRtiAmb.getAttributeHandle("THROTTLE_RIGHT", myFedAmb.Get_JOYSTICK_ClassHandle()));      
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }
    
  /* (II.0) AttributeHandleSet for Subscribe */

    auto_ptr<RTI::AttributeHandleSet> attr_USER_DATA(	         RTI::AttributeHandleSetFactory::create(1)) ;

    attr_USER_DATA->add(myFedAmb.Get_ORDER_ATTRIBUTE());   

  /* (II.0) AttributeHandleSet for Publish */
  
    auto_ptr<RTI::AttributeHandleSet> attr_JOYSTICK(RTI::AttributeHandleSetFactory::create(5));
    
    attr_JOYSTICK->add(myFedAmb.Get_AILERON_ATTRIBUTE());
    attr_JOYSTICK->add(myFedAmb.Get_ELEVATOR_ATTRIBUTE());
    attr_JOYSTICK->add(myFedAmb.Get_RUDDER_ATTRIBUTE());
    attr_JOYSTICK->add(myFedAmb.Get_THROTTLE_LEFT_ATTRIBUTE());
    attr_JOYSTICK->add(myFedAmb.Get_THROTTLE_RIGHT_ATTRIBUTE());
  
  /* (II.1) Subscribe */
       try {
        myRtiAmb.subscribeObjectClassAttributes(myFedAmb.Get_USER_DATA_ClassHandle(),      *attr_USER_DATA);
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }

  /* (II.1) Publish */

    try {
        myRtiAmb.publishObjectClass(myFedAmb.Get_JOYSTICK_ClassHandle(),*attr_JOYSTICK);
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }
    
  /* (II.2) Register object */
   
    try {
        myFedAmb.Set_ObjInstance_JOYSTICK_ObjectHandle(myRtiAmb.registerObjectInstance(myFedAmb.Get_JOYSTICK_ClassHandle(),"Joystick"));
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }
   
  /* (II.2) Discover object */
  
    cout << "                                    " << endl;		
    cout << ">> Discovering each object instance." << endl;

    myFedAmb.Set_Discov_USER_DATA(true);

    while (!myFedAmb.Get_Discov_USER_DATA()) {
        try {
            myRtiAmb.tick();
        } catch ( RTI::Exception &e ) {
            cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
        } catch ( ... ) {
            cerr << "Error: unknown non-RTI exception." << endl;
        }
        sleep(1);
    }
    myFedAmb.Set_Discov_USER_DATA(false);
    
    cout << ">> Object instances discovered." << endl;

  /* (II.3) Time Management Setting */

    if (TIME_MANAGEMENT){

      /* Declaration du temps local et du lookahead */
      
        myFedAmb.Set_LocalTime(0.0) ;
        //myFedAmb.Set_TimeStep(0.01) ;
        myFedAmb.Set_TimeStep(0.01);

        switch (TIME_MANAGEMENT){

            case 0 :
            case 1 : 
            
                myFedAmb.Set_Lookahead(3.0) ; 
                break;
                
            case 2 :
            
                myFedAmb.Set_Lookahead(0.0000001) ; 
                break;
        }  
      
      /* Demande d'etre regulateur */
      
        myRtiAmb.enableTimeRegulation(myFedAmb.Get_LocalTime(),myFedAmb.Get_Lookahead()) ;

      /* Demande d'etre contraint */   
      
        myRtiAmb.enableTimeConstrained() ;

      /* Tant que les callbacks de validation de regulateur et contraint ne sont pas la ...*/
      /* On tick() !!!!! */
        while ( !myFedAmb.Get_IsTimeReg() || !myFedAmb.Get_IsTimeConst()) {
            try {
                myRtiAmb.tick2();
            } catch ( RTI::Exception &e ) {
                cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
            } catch ( ... ) {
                cerr << "Error: unknown non-RTI exception." << endl;
            }    
        } // Fin du while ( (!myFedAmb.Get_IsTimeReg()) && (!myFedAmb.Get_IsTimeConst()) )
        
        cout << " "                                                  << endl ;
        cout << ">> Time management setting. "                       << endl ;  
        cout << ">> Is constrained ? " << myFedAmb.Get_IsTimeConst() << endl ;
        cout << ">> Is regulator ? " << myFedAmb.Get_IsTimeReg()     << endl ;

      /*******************************************************/
      /* IMPORTANT : Si la gestion du temps Reg et Contraint */
      /* enableAsynchronousDelivery() permet de recevoir des */
      /* callback telle que discoverObjectInstance           */
      /*******************************************************/    
    
        myRtiAmb.enableAsynchronousDelivery() ;

    } /* End if (TIME_MANAGEMENT) */

  /* (II.4) Phase de Syncronisation Initialisation */
  /* Le federe DynaVol Synchronise une premiere    */
  /* fois pour trouver le point d'equilibre        */
  
    // if(SYNCHRO_INITIALISATION){
    
    //     cout << " "                                  << endl;
    //     cout << ">> Phase 1: Trimming the aircraft." << endl;  

    //     if(myFedAmb.Get_Am_I_God() ){
    //         cout << ">> PRESS ENTER TO START TRIMMING " << endl;      
    // 	        std::getchar();
    //         try { 
    //             myRtiAmb.registerFederationSynchronizationPoint("Trimming","");
    //         } catch ( RTI::Exception &e ) {
    //             cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    //         } catch ( ... ) {
    //             cerr << "Error: unknown non-RTI exception." << endl;
    //         }

    //         while (!myFedAmb.Get_SyncRegSuccess() && !myFedAmb.Get_SyncRegFailed()) {
    //             try {
    //               /* Appel au tick bloquant */
    //                 myRtiAmb.tick2();
    //             } catch ( RTI::Exception &e ) {
    //                 cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    //             } catch ( ... ) {
    //                 cerr << "Error: unknown non-RTI exception." << endl;
    //             }
    //             cout << ">> Waiting for success or failure of synchronisation point Trimming. " << endl;
    //         } // Fin du while (...)
    //         if(myFedAmb.Get_SyncRegFailed()){
    //             cout << ">> ERREUR DE SYNCHRONISATION" << endl;
    //         } 
    //     } // End of if(myFedAmb.Get_Am_I_God() )

    //   /* Le federe attend l'annonce du point de synchronisation */
    //     while (!myFedAmb.Get_InPause()) {
    //         cout << ">> Waiting for synchronisation point Trimming announcement." << endl;
    //         try {
    //             myRtiAmb.tick2();
    //         } catch ( RTI::Exception &e ) {
    //             cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    //         } catch ( ... ) {
    //             cerr << "Error: unknown non-RTI exception." << endl;
    //         }
    //     } // Fin du while (...)  

    //   /* Le federe satisfait le point de synchronisation */  
    //     try {
    //         myRtiAmb.synchronizationPointAchieved("Trimming");
    //     } catch ( RTI::Exception &e ) {
    //         cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    //     } catch ( ... ) {
    //         cerr << "Error: unknown non-RTI exception." << endl;
    //     }
    //     cout << ">> Synchronisation point Trimming satisfied." << endl;     

    //   /* Le federe attend que la federation soit synchronise */
    //     while (myFedAmb.Get_InPause()) {
    //         cout << ">> Waiting for initialization phase." << endl ;
    //         try {
    //             myRtiAmb.tick2();
    //         } catch ( RTI::Exception &e ) {
    //             cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    //         } catch ( ... ) {
    //             cerr << "Error: unknown non-RTI exception." << endl;
    //         }
    //     } // Fin du while (...)   

    // } // Fin du if(SYNCHRO_INTIALISATION)
    
    cout << " "                         << endl;
    cout << ">> INITIALIZATION STARTED" << endl;

    myFedAmb.Initialization() ;

  /* On cree un pointeur pour chaque instance d'objet publie */
    auto_ptr<RTI::AttributeHandleValuePairSet> ahvps_JOYSTICK(RTI::AttributeSetFactory::create(5));
    
  /*********************************/
  /* update attribute for JOYSTICK */
  /*********************************/
    
    CertiMessagebuffer.reset() ;
    CertiMessagebuffer.write_double(myFedAmb.Get_AILERON());
    CertiMessagebuffer.updateReservedBytes();
    ahvps_JOYSTICK -> add(myFedAmb.Get_AILERON_ATTRIBUTE(), static_cast<char*>(CertiMessagebuffer(0)), CertiMessagebuffer.size()); 

    CertiMessagebuffer.reset() ;
    CertiMessagebuffer.write_double(myFedAmb.Get_ELEVATOR());
    CertiMessagebuffer.updateReservedBytes();
    ahvps_JOYSTICK -> add(myFedAmb.Get_ELEVATOR_ATTRIBUTE(), static_cast<char*>(CertiMessagebuffer(0)), CertiMessagebuffer.size()); 

    CertiMessagebuffer.reset() ;
    CertiMessagebuffer.write_double(myFedAmb.Get_RUDDER());
    CertiMessagebuffer.updateReservedBytes();
    ahvps_JOYSTICK -> add(myFedAmb.Get_RUDDER_ATTRIBUTE(), static_cast<char*>(CertiMessagebuffer(0)), CertiMessagebuffer.size()); 

    CertiMessagebuffer.reset() ;
    CertiMessagebuffer.write_double(myFedAmb.Get_THROTTLE_LEFT());
    CertiMessagebuffer.updateReservedBytes();
    ahvps_JOYSTICK -> add(myFedAmb.Get_THROTTLE_LEFT_ATTRIBUTE(), static_cast<char*>(CertiMessagebuffer(0)), CertiMessagebuffer.size()); 

    CertiMessagebuffer.reset() ;
    CertiMessagebuffer.write_double(myFedAmb.Get_THROTTLE_RIGHT());
    CertiMessagebuffer.updateReservedBytes();
    ahvps_JOYSTICK -> add(myFedAmb.Get_THROTTLE_RIGHT_ATTRIBUTE(), static_cast<char*>(CertiMessagebuffer(0)), CertiMessagebuffer.size()); 
    
    try {
        myRtiAmb.updateAttributeValues(myFedAmb.Get_ObjInstance_JOYSTICK_ObjectHandle(), *ahvps_JOYSTICK, "Joystick");
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    } 

  /* On vide les pointeurs */
  
    ahvps_JOYSTICK -> empty ();

  /**************************************************/
  /* (II.4) Phase de Synchronisation Simulation     */
  /* Le federe DynaVol Synchronise une deuxieme     */
  /* fois avant de lancer la simulation             */
  /**************************************************/
  
    if(SYNCHRO_INITIALISATION){
    
        cout << " "                                    << endl;
        cout << ">> Phase 2: Simulating the aircraft." << endl;  

        if(myFedAmb.Get_Am_I_God() ){
            cout << ">> PRESS ENTER TO START SIMULATING " << endl;      
            cin.get();
            cin.get();

            try { 
                myRtiAmb.registerFederationSynchronizationPoint("Simulating","");
            } catch ( RTI::Exception &e ) {
                cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
            } catch ( ... ) {
                cerr << "Error: unknown non-RTI exception." << endl;
            }

            while (!myFedAmb.Get_SyncRegSuccess() && !myFedAmb.Get_SyncRegFailed()) {
                try {
                  /* Appel au tick bloquant */
                    myRtiAmb.tick2();
                } catch ( RTI::Exception &e ) {
                    cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
                } catch ( ... ) {
                    cerr << "Error: unknown non-RTI exception." << endl;
                }
                cout << ">> Waiting for success or failure of synchronisation point Simulating. " << endl;
            } // Fin du while (...)
            if(myFedAmb.Get_SyncRegFailed()){
                cout << ">> ERREUR DE SYNCHRONISATION" << endl;
            } 
        } // End of if(myFedAmb.Get_Am_I_God() )

      /* Le federe attend l'annonce du point de synchronisation */
        while (!myFedAmb.Get_InPause()) {
            cout << ">> Waiting for synchronisation point Simulating announcement." << endl;
            try {
                myRtiAmb.tick2();
            } catch ( RTI::Exception &e ) {
                cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
            } catch ( ... ) {
                cerr << "Error: unknown non-RTI exception." << endl;
            }
        } // Fin du while (...)  

      /* Le federe satisfait le point de synchronisation */  
        try {
            myRtiAmb.synchronizationPointAchieved("Simulating");
        } catch ( RTI::Exception &e ) {
            cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
        } catch ( ... ) {
            cerr << "Error: unknown non-RTI exception." << endl;
        }
        cout << ">> Synchronisation point Simulating satisfied." << endl;     

      /* Le federe attend que la federation soit synchronise */
        while (myFedAmb.Get_InPause()) {
            cout << ">> Waiting for simulation phase." << endl ;
            try {
                myRtiAmb.tick2();
            } catch ( RTI::Exception &e ) {
                cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
            } catch ( ... ) {
                cerr << "Error: unknown non-RTI exception." << endl;
            }
        } // Fin du while (...)   

    } // Fin du if(SYNCHRO_INTIALISATION)

/************************************************************************/
/**************   Phase de boucle de simulation  III        *************/
/************************************************************************/
/****************** (III.1) Avance dans le temps (Pas ici) **************/
/****************** (III.2) Réception des mises à jour  *****************/
/****************** (III.3) Calculs locaux   ****************************/
/****************** (III.4) Envoi de mises à jour  **********************/
/************************************************************************/

/* SIMULATION LOOP */
    cout << "****************************************" << endl;
    cout << "******                         *********" << endl;
    cout << "****** SIMULATION LOOP STARTED *********" << endl;
    cout << "******                         *********" << endl;
    cout << "****************************************" << endl;
       
/* On demarre le timer */
    Timer.start();
/*
 * As of the timer_settime(), we will receive our pulse 
 * in 0 seconds (the itime.it_value) and every 20ms 
 * thereafter (the itime.it_interval)
 */
    int rest_time ;

    while ( NbTotalcycles-NbCycles != 0 ) {
    
        tick0 = Timer.getElapsedTimeInMicroSec() ;

        if (TRACE_SIMU){ 
            cout << "                                   " << endl;
            cout << " >> Cycle : " <<  NbCycles << " << " << endl;
            cout << "                                   " << endl;  
        }// Fin du if (TRACE_SIMU){ 

      /********************************************************/
      /* (III.1)  CALCULS LOCAUX DU FEDERE (OUTPUT)           */
      /********************************************************/
    
        myFedAmb.Compute_Local_Output_Algorithm() ;  

      /********************************************************/
      /* (III.2)  ENVOI  DES MISES A JOUR                     */
      /********************************************************/
    
      /*********************************/
      /* update attribute for JOYSTICK */
      /*********************************/
    
        CertiMessagebuffer.reset() ;
        CertiMessagebuffer.write_double(myFedAmb.Get_AILERON());
        CertiMessagebuffer.updateReservedBytes();
        ahvps_JOYSTICK -> add(myFedAmb.Get_AILERON_ATTRIBUTE(), static_cast<char*>(CertiMessagebuffer(0)), CertiMessagebuffer.size()); 

        CertiMessagebuffer.reset() ;
        CertiMessagebuffer.write_double(myFedAmb.Get_ELEVATOR());
        CertiMessagebuffer.updateReservedBytes();
        ahvps_JOYSTICK -> add(myFedAmb.Get_ELEVATOR_ATTRIBUTE(), static_cast<char*>(CertiMessagebuffer(0)), CertiMessagebuffer.size()); 

        CertiMessagebuffer.reset() ;
        CertiMessagebuffer.write_double(myFedAmb.Get_RUDDER());
        CertiMessagebuffer.updateReservedBytes();
        ahvps_JOYSTICK -> add(myFedAmb.Get_RUDDER_ATTRIBUTE(), static_cast<char*>(CertiMessagebuffer(0)), CertiMessagebuffer.size()); 

        CertiMessagebuffer.reset() ;
		CertiMessagebuffer.write_double(myFedAmb.Get_THROTTLE_LEFT());
		CertiMessagebuffer.updateReservedBytes();
		ahvps_JOYSTICK -> add(myFedAmb.Get_THROTTLE_LEFT_ATTRIBUTE(), static_cast<char*>(CertiMessagebuffer(0)), CertiMessagebuffer.size()); 

		CertiMessagebuffer.reset() ;
		CertiMessagebuffer.write_double(myFedAmb.Get_THROTTLE_RIGHT());
		CertiMessagebuffer.updateReservedBytes();
		ahvps_JOYSTICK -> add(myFedAmb.Get_THROTTLE_RIGHT_ATTRIBUTE(), static_cast<char*>(CertiMessagebuffer(0)), CertiMessagebuffer.size());  

        switch (TIME_MANAGEMENT){

            case 0 : // Data Flow

                try {
                    myRtiAmb.updateAttributeValues(myFedAmb.Get_ObjInstance_JOYSTICK_ObjectHandle(), *ahvps_JOYSTICK,"Joystick");
                } catch ( RTI::Exception &e ) {
                    cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
                } catch ( ... ) {
                    cerr << "Error: unknown non-RTI exception." << endl;
                }           
                break;
                
            case 1 :
            case 2 :
            
                try {
                    myRtiAmb.updateAttributeValues(myFedAmb.Get_ObjInstance_JOYSTICK_ObjectHandle(), *ahvps_JOYSTICK, 
                                                   myFedAmb.Get_LocalTime()+myFedAmb.Get_Lookahead(),"Joystick");
                } catch ( RTI::Exception &e ) {
                    cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
                } catch ( ... ) {
                    cerr << "Error: unknown non-RTI exception." << endl;
                }
                break;
        

        } /* Fin du switch (TIME_MANAGEMENT) pour la publication JOYSTICK */

      /* On vide les pointeurs */
        ahvps_JOYSTICK -> empty ();

      /********************************************************/
      /* (III.3) DEMANDE D'AVANCE DE TEMPS                    */
      /********************************************************/

        switch (TIME_MANAGEMENT){

            case 0 : // Data Flow
                cout << "Waiting for udpates" << endl;
            
                while (!myFedAmb.Get_New_USER_DATA()) {
                    try {
                        myRtiAmb.tick2();
                      /* On libere le processeur */
                        sched_yield();
                    } catch ( RTI::Exception &e ) {
                        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
                    } catch ( ... ) {
                        cerr << "Error: unknown non-RTI exception." << endl;
                    }
                }
                myFedAmb.Set_New_USER_DATA(false);
            
                cout << "Got udpates" << endl;
                
                break;
                
            case 1 :
            case 2 :
            
                try {
                    myRtiAmb.timeAdvanceRequest(myFedAmb.Get_LocalTime() + myFedAmb.Get_TimeStep());  
	            } catch ( RTI::Exception &e ) {
                    cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
                } catch ( ... ) {
                    cerr << "Error: unknown non-RTI exception." << endl;
                }
            
            /* Wait for TAG */
  
                while (!myFedAmb.Get_TimeAdvanceGrant()) {
                    try {
                      /* Appel au tick bloquant */
                        myRtiAmb.tick2();
                      /* On libere le processeur */
                        sched_yield();
                    } catch ( RTI::Exception &e ) {
                        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
                    } catch ( ... ) {
                        cerr << "Error: unknown non-RTI exception." << endl;
                    }
                } // Fin du while
        	
                myFedAmb.Set_TimeAdvanceGrant(false);
                break;                

        } /* Fin du switch (TIME_MANAGEMENT) pour la reception */
        
      /*******************************************************/
      /* (III.4)  CALCULS LOCAUX DU FEDERE (STATE)           */
      /*******************************************************/
    
        myFedAmb.Compute_Local_State_Algorithm() ;
 
      /*********************************/
      /* On incremente la date de simu */
      /*********************************/
        NbCycles++;
        
        tick1 = Timer.getElapsedTimeInMicroSec() ;
  
        if (!TIME_MANAGEMENT){     

            t0->tv_sec  =  0;
            t0->tv_nsec = (int) (500000 - (tick1-tick0)) * 1000;

          /* on attend l'expiration du delai de XX millisecondes */
            while ((nanosleep(t0, t1) == (-1))){
                tmp = t0;
                t0 = t1;
                t1 = tmp;
            }
        } 

        //getchar();
        tick1 = Timer.getElapsedTimeInMicroSec() ;
        
        cout << "Laps: "    << tick1-tick0 << endl;

    } // END OF SIMULATION LOOP

  /*On efface le timer */
    timer_delete(timer_id) ;

/************************************************************************/
/**************   Phase de Terminaison IV            ********************/
/************************************************************************/
/**** (IV.1) Suppression des objets enregistrés *************************/
/**** (IV.2) Désactivation de la politique de gestion du temps (pas ici)*/
/************************************************************************/



    /* (IV.1) Declaration Management */ 
   try {
        myRtiAmb.unsubscribeObjectClass(myFedAmb.Get_USER_DATA_ClassHandle());
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }

    try {
        myRtiAmb.unpublishObjectClass(myFedAmb.Get_JOYSTICK_ClassHandle());
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }
    
/************************************************************************/
/**************   Phase de Suppression V            *********************/
/************************************************************************/
/**** (V.1) Le fédéré quitte la fédération ******************************/
/**** (V.2) DDestruction de la fédération *******************************/
/************************************************************************/


	
    /* Federation Management */
    /* (V.1) resign federation execution */
    try {
        myRtiAmb.resignFederationExecution(RTI::DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES);
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }


	
    /* (V.2) destroy federation execution */
    try {
        myRtiAmb.destroyFederationExecution(federationName.c_str());
    } catch (RTI::FederatesCurrentlyJoined) {
        cout << "Federates currently joined." << endl;
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }
    
    return 0;
}
