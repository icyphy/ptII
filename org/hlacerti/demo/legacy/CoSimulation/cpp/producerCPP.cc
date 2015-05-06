/**
 * @file producerFederate.cc
 *
 * @brief This federate simulates a...
 *
 * $Id: producerCPP.cc 71890 2015-04-03 18:28:43Z David.COME@supaero.isae.fr $
 * $Author: David.COME@supaero.isae.fr $
 * $Data$
 */

/* <RTI.hh>
 * This header is the necessary include for using HLA RTI API.
 */
#include <RTI.hh>

/**
 * This header is for data representation between CERTI C++ and
 * Java implementation.
 */
#include <HLAtypesIEEE1516.hh>
using namespace libhla;

/*
 * <fedtime.hh>
 * The header provides virtual methods for time Management services
 * implemented.
 */
#include <fedtime.hh>

/*
 * <NullFederateAmbassador.hh>
 * The header provides virtual methods of the abstract basis class
 * FederateAmbassador included by <RTI.hh>. When using the
 * NullFederateAmbassador only required federate services have to be
 * implemented.
 */
#include <NullFederateAmbassador.hh>

#include <iostream>
#include <memory>
#include <string>

#include <string.h>
#include <unistd.h>

using std::string;
using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::auto_ptr;

/**
 * The class producerFedAmb realizes... So the occurrence of special federate
 * services can be determined in main().
 */
class producerFedAmb : public NullFederateAmbassador {

public:
  /*****************************/
  /* TIME MANAGEMENT VARIABLES */
  /*****************************/
  // flag pour savoir si le federe est regulateur et/ou contraint
  bool _IsTimeReg, _IsTimeConst, _TimeAdvanceGrant;
  bool _SyncRegSuccess, _SyncRegFailed, _InPause;
  RTIfedTime _LocalTime ;
  RTIfedTime _TimeStep ;   
  RTIfedTime _Lookahead ;   
  RTIfedTime _RestOfTimeStep ; 
    
  producerFedAmb() : NullFederateAmbassador() { }
  virtual ~producerFedAmb() throw (RTI::FederateInternalError) {}

      /*************************************************/
    /* Accesseurs pour les flags de gestion du temps */
    /*************************************************/
    bool Get_IsTimeReg()        { return _IsTimeReg ;} ; 
    bool Get_IsTimeConst()      { return _IsTimeConst ;} ;
    bool Get_TimeAdvanceGrant() { return _TimeAdvanceGrant ;} ;
    
    void Set_IsTimeReg(bool val)        {_IsTimeReg = val;} ;
    void Set_IsTimeConst(bool val)      {_IsTimeConst = val;} ;
    void Set_TimeAdvanceGrant(bool val) {_TimeAdvanceGrant = val;} ;
    
    RTIfedTime Get_LocalTime() { return _LocalTime; } ;
    void Set_LocalTime(RTIfedTime val) {_LocalTime = val; } ;

    RTIfedTime Get_TimeStep()  { return _TimeStep; };   
    void Set_TimeStep(RTIfedTime val) {_TimeStep = val; } ;

    RTIfedTime Get_Lookahead()  { return _Lookahead; }; 
    void Set_Lookahead(RTIfedTime val) {_Lookahead = val; } ;

    RTIfedTime Get_RestOfTimeStep()  { return _RestOfTimeStep; }; 
    void Set_RestOfTimeStep(RTIfedTime val) {_RestOfTimeStep = val; } ;

    bool Get_SyncRegSuccess()         { return _SyncRegSuccess;} ; 
    void Set_SyncRegSuccess(bool val) {_SyncRegSuccess = val;} ;

    bool Get_SyncRegFailed()         { return _SyncRegFailed;} ; 
    void Set_SyncRegFailed(bool val) {_SyncRegFailed = val;} ;

    bool Get_InPause()         { return _InPause;} ; 
    void Set_InPause(bool val) {_InPause = val;} ;


/************************************************/
/* HLA specific methods : TIME MANAGEMENT */
/************************************************/

// Callback : timeRegulationEnabled
void timeRegulationEnabled(const RTI::FedTime& theTime)
                                 throw (RTI::InvalidFederationTime,
                                        RTI::EnableTimeRegulationWasNotPending,
                                        RTI::FederateInternalError) {
    _IsTimeReg = true ;
} // End of timeRegulationEnabled

// Callback : timeConstrainedEnabled
void timeConstrainedEnabled(const RTI::FedTime& theTime)
                                  throw (RTI::InvalidFederationTime,
                                         RTI::EnableTimeConstrainedWasNotPending,
                                         RTI::FederateInternalError) {
    _IsTimeConst = true ;
} // End of timeConstrainedEnabled

// Callback : timeAdvanceGrant
void timeAdvanceGrant(const RTI::FedTime& theTime)
                            throw (RTI::InvalidFederationTime,
                                   RTI::TimeAdvanceWasNotInProgress,
                                   RTI::FederateInternalError) {
    _LocalTime = theTime ;
    _TimeAdvanceGrant =  true ;
    if (true){ 
        cout << " >> TAG(" << _LocalTime << ") RCV == LocalTime = " << _LocalTime << " << " << endl; 
    } // Fin du if (TRACE_SIMU){ 
} // End of timeAdvanceGrant


/************************************************/
/* HLA specific methods : SYNCHRONISATION */
/************************************************/

// Callback : synchronizationPointRegistrationSucceeded
void synchronizationPointRegistrationSucceeded(const char *label)
                                                     throw (RTI::FederateInternalError) {
    _SyncRegSuccess = true ;
} // End of synchronizationPointRegistrationSucceeded

// Callback : synchronizationPointRegistrationFailed
void synchronizationPointRegistrationFailed(const char *label)
                                                  throw (RTI::FederateInternalError) {
    _SyncRegFailed = true ;
} // End of synchronizationPointRegistrationFailed

// Callback : announceSynchronizationPoint
void announceSynchronizationPoint(const char *label, const char *tag)
                                        throw (RTI::FederateInternalError) {
    _InPause = true ;
} // End of announceSynchronizationPoint

// Callback : federationSynchronized
void federationSynchronized(const char *label)
                                  throw (RTI::FederateInternalError) {
    _InPause = false ;
} // End of federationSynchronized

};
  
/**
 * main of producerFed.cc
 */
int main() {
    RTI::RTIambassador   rtiAmb;
    producerFedAmb       myFedAmb;

    string federationName = "CoSimulation";
    string federateName   = "producerCPP";
    string fedFile        = "CoSimulation.fed";

    //cout << "Initialization of federate: p3Fed" << endl; 

    /* create federation execution */
    try {
        rtiAmb.createFederationExecution(federationName.c_str(), 
					 fedFile.c_str());
    } catch ( RTI::FederationExecutionAlreadyExists ) {
        cout << "WARNING !! Federation already created by another federate !!" 
	<< endl;
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }

    /* join federation execution */
    try {
        rtiAmb.joinFederationExecution(federateName.c_str(),
                                       federationName.c_str(),
                                       &myFedAmb);
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }

    /* Declaration Management */

    /* get object class handle */
    RTI::ObjectClassHandle myObjectID;

    try {
        myObjectID = rtiAmb.getObjectClassHandle("myObjectClass");
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }

    /* get attribute handle */
    RTI::AttributeHandle valID;
    try {
        valID = rtiAmb.getAttributeHandle("val", myObjectID);
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }

    /* add attribute handle to AttributeHandleSet */
    auto_ptr<RTI::AttributeHandleSet>
    attrVAL(RTI::AttributeHandleSetFactory::create(1));
    attrVAL->add(valID);
    
    /* publish to val */
    try {
        rtiAmb.publishObjectClass(myObjectID, *attrVAL);
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }
    
    /* Register object */
    RTI::ObjectHandle objInstID_val;
    
    try {
        objInstID_val = rtiAmb.registerObjectInstance(myObjectID, "VAL3");
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }

    /* Time Management Setting */
    /* Declaration du temps local et du lookahead */
    myFedAmb.Set_LocalTime(0.0) ;
    myFedAmb.Set_TimeStep(4.0) ;
    myFedAmb.Set_Lookahead(1.0) ;
      
    /* Demande d'etre regulateur */
    rtiAmb.enableTimeRegulation(myFedAmb.Get_LocalTime(),myFedAmb.Get_Lookahead()) ;
      
    /* Demande d'etre contraint */   
    rtiAmb.enableTimeConstrained() ;

    /* Tant que les callbacks de validation de regulateur et contraint ne sont pas la => tick2() ! */
    while ( !myFedAmb.Get_IsTimeReg() ) {
      try {
           rtiAmb.tick2();
      } catch ( RTI::Exception &e ) {
         cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
      } catch ( ... ) {
         cerr << "Error: unknown non-RTI exception." << endl;
      }     
     } 

   while ( !myFedAmb.Get_IsTimeConst()) {
      try {
           rtiAmb.tick2();
      } catch ( RTI::Exception &e ) {
         cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
      } catch ( ... ) {
         cerr << "Error: unknown non-RTI exception." << endl;
      }     
     } 
        
    cout << " "                                                  << endl ;
    cout << ">> Time management setting: constrained=" << myFedAmb.Get_IsTimeConst() << " regulator=" << myFedAmb.Get_IsTimeReg()     << endl ;

    /*******************************************************/
    /* IMPORTANT : Si la gestion du temps Reg et Contraint */
    /* enableAsynchronousDelivery() permet de recevoir des */
    /* callback telle que discoverObjectInstance           */
    /*******************************************************/    
    rtiAmb.enableAsynchronousDelivery() ;

    bool SYNCHRO_INITIALISATION = true;
    if(SYNCHRO_INITIALISATION){
        cout << " "                           << endl;
        cout << ">> Simulating the producerCPP." << endl;  

        /* Producer is the creator : GOD MODE ! */
        if(false){
            cout << ">> PRESS ENTER TO START SIMULATING " << endl;      
            cin.get();

            try { 
                rtiAmb.registerFederationSynchronizationPoint("Simulating","");
            } catch ( RTI::Exception &e ) {
                cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
            } catch ( ... ) {
                cerr << "Error: unknown non-RTI exception." << endl;
            }

            while (!myFedAmb.Get_SyncRegSuccess() && !myFedAmb.Get_SyncRegFailed()) {
                try {
                  /* Appel au tick bloquant */
                    rtiAmb.tick2();
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
                rtiAmb.tick2();
            } catch ( RTI::Exception &e ) {
                cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
            } catch ( ... ) {
                cerr << "Error: unknown non-RTI exception." << endl;
            }
        } // Fin du while (...)  

      /* Le federe satisfait le point de synchronisation */  
        try {
            rtiAmb.synchronizationPointAchieved("Simulating");
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
                rtiAmb.tick2();
            } catch ( RTI::Exception &e ) {
                cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
            } catch ( ... ) {
                cerr << "Error: unknown non-RTI exception." << endl;
            }
        } // Fin du while (...)   

    } // Fin du if(SYNCHRO_INTIALISATION)
    
    /* Object Management */
    
    auto_ptr<RTI::AttributeHandleValuePairSet>
    ahvps(RTI::AttributeSetFactory::create(1));
	    
        cout << " "                           << endl;

    HLAdata<HLAfloat64BE> value;
    double cpt = 0;
    while (cpt < 5) {
       (*value) = 3;
       
        /* update attribute */
        ahvps -> add(valID, const_cast<char*>(value.data()), sizeof(double));
        
        try {
	  rtiAmb.updateAttributeValues(objInstID_val, *ahvps, myFedAmb.Get_LocalTime()+myFedAmb.Get_Lookahead() ,"VAL");
        } catch ( RTI::Exception &e ) {
            cerr << "RTI exception: " << e._name << " ["
            << (e._reason ? e._reason : "undefined") << "]." << endl;
        } catch ( ... ) {
            cerr << "Error: unknown non-RTI exception." << endl;
        }
        
        ahvps -> empty ();

        cout << "Federate producerCPP has updated the value val: " << cpt << endl;
	
 	try {
                rtiAmb.tick();
        } catch ( RTI::Exception &e ) {
            cerr << "RTI exception: " << e._name << " ["
            << (e._reason ? e._reason : "undefined") << "]." << endl;
        } catch ( ... ) {
            cerr << "Error: unknown non-RTI exception." << endl;
        }
	sleep(1);
        cpt += 1;

        /* demande davance dans le temps */              
        try {
             rtiAmb.timeAdvanceRequest(myFedAmb.Get_LocalTime() + myFedAmb.Get_TimeStep());  
	     cout << " >> TAR(" << myFedAmb.Get_LocalTime() + myFedAmb.Get_TimeStep() << ") SEND == RequestedTime = " << myFedAmb.Get_LocalTime() + myFedAmb.Get_TimeStep() << " << " << endl; 

	} catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
        } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
        }
            
        /* Wait for TAG */
        while (!myFedAmb.Get_TimeAdvanceGrant()) {
        try {
	  /* Appel au tick bloquant */
	  rtiAmb.tick2();
	  /* On libere le processeur */
	  sched_yield();
        } catch ( RTI::Exception &e ) {
          cerr << "RTI exception: " << e._name << " [" << (e._reason ? e._reason : "undefined") << "]." << endl;
        } catch ( ... ) {
          cerr << "Error: unknown non-RTI exception." << endl;
        }
      } // Fin du while  	
      myFedAmb.Set_TimeAdvanceGrant(false);
    }

    /* Declaration Management */
    
    /* unpublish */
    try {
        rtiAmb.unpublishObjectClass(valID);
    } catch ( RTI::Exception &e ) {
        cout << " " << endl;
        /*cerr << "RTI exception: " << e._name << " ["
	  << (e._reason ? e._reason : "undefined") << "]." << endl;*/
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }

    /* Federation Management */

    /* resign federation execution */
    try {
        rtiAmb.resignFederationExecution(
            RTI::DELETE_OBJECTS_AND_RELEASE_ATTRIBUTES);
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }

    /* destroy federation execution */
    try {
        rtiAmb.destroyFederationExecution(federationName.c_str());
    } catch (RTI::FederatesCurrentlyJoined) {
        cout << "Federates currently joined." << endl;
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }

    return 0;
}
