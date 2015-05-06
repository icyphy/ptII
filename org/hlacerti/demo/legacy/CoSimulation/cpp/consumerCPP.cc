/**
 * @file consumerCPP.cc
 *
 * @brief This federate simulates a...
 *
 * $Id: consumerCPP.cc 71890 2015-04-03 18:28:43Z David.COME@supaero.isae.fr $
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
 * The class consumerCPPAmb realizes... So the occurrence of special federate
 * services can be determined in main().
 */
class consumerCPPAmb : public NullFederateAmbassador {

private:

  double val1;
  double val2;

  bool newVal1;
  bool newVal2;

public:
    
  RTI::ObjectClassHandle myObjectID;
  RTI::AttributeHandle val1Id;
  RTI::AttributeHandle val2Id;

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

  consumerCPPAmb() : NullFederateAmbassador(), 
		     val1(0), newVal1(false),
                     val2(0), newVal2(false) {
    /* variables time management */
    _IsTimeReg = _IsTimeConst = _TimeAdvanceGrant = false ;
    }
  virtual ~consumerCPPAmb() throw (RTI::FederateInternalError) {}

    bool getNewVal1() const {
        return newVal1;
    }

    void setNewVal1(bool nval) {
	newVal1 = nval;
    } 

    double getVal1() {
      return val1;
    }

    bool getNewVal2() const {
        return newVal2;
    }

    void setNewVal2(bool nval) {
	newVal2 = nval;
    } 

    double getVal2() {
      return val2;
    }

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

    /**
     * Reflect Attribute Values. This callback informs the federate of a state
     * update for a set of instance-attributes according to its current
     * subscription interests.
     * @ingroup RequiredFederateServices
     * @param[in] theObject,
     * @param[in] theAttributes,
     * @param[in] theTag
     */
    /*void
    reflectAttributeValues (RTI::ObjectHandle theObject,
                            const RTI::AttributeHandleValuePairSet&
                            theAttributes,
                            const char *theTag)
    throw ( RTI::ObjectNotKnown,
            RTI::AttributeNotKnown,
            RTI::FederateOwnsAttributes,
            RTI::FederateInternalError) {
        RTI::ULong length;

        cout << "DEBUG: REFLECT is called ! " << endl;

        for (unsigned int i=0; i<theAttributes.size(); i++) {
            RTI::AttributeHandle handle = theAttributes.getHandle(i);
            length = theAttributes.getValueLength(i);
	    HLAdata<HLAfloat64BE> value;
	    theAttributes.getValue(i, const_cast<char*>(value.data()), length);	    

	    if (handle == val1Id) {
                /*char *attrValue;
                //attrValue = new char[length];
                //theAttributes.getValue(i, attrValue, length);
	        //val1 = *(reinterpret_cast<double*>(attrValue));
                //newVal1 = true;
                //delete[] attrValue;
                //HLAdata<HLAfloat64BE> F64BE(attrValue,length);

                val1 = (*value);
                newVal1 = true;

	    } else if (handle == val2Id) {
                val2 = (*value);
                newVal2 = true;
	    }
        }
    }*/

   
    /*********     Callback : Reflect Attribute Values with time           ***********/
    void reflectAttributeValues
       (RTI::ObjectHandle theObject,
        const RTI::AttributeHandleValuePairSet& theAttributes,
        const RTI::FedTime& theTime,
        const char *theTag,
        RTI::EventRetractionHandle)
    throw (RTI::ObjectNotKnown,
           RTI::AttributeNotKnown,
           RTI::FederateOwnsAttributes,
           RTI::InvalidFederationTime, 
           RTI::FederateInternalError) 
    {
    RTI::ULong length;

    //cout << "DEBUG: REFLECT with TIME is called ! at Time " << &theTime << endl;

        for (unsigned int i=0; i<theAttributes.size(); i++) {
            RTI::AttributeHandle handle = theAttributes.getHandle(i);
            length = theAttributes.getValueLength(i);
	    HLAdata<HLAfloat64BE> value;
	    theAttributes.getValue(i, const_cast<char*>(value.data()), length);	    

	    if (handle == val1Id) {
                val1 = (*value);
                newVal1 = true;
	    } else if (handle == val2Id) {
                val2 = (*value);
                newVal2 = true;
	    }
        }
 
}  /********* Fin de Callback : Reflect Attribute Values with time      ***********/

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
 * main of consumerCPP.cc
 */
int main() {
    RTI::RTIambassador   rtiAmb;
    consumerCPPAmb       myFedAmb;

    string federationName = "CoSimulation";
    string federateName   = "consumerCPP";
    string fedFile        = "CoSimulation.fed";

    /* create federation execution */
    try {
        rtiAmb.createFederationExecution(federationName.c_str(), 
					 fedFile.c_str());
    } catch ( RTI::FederationExecutionAlreadyExists ) {
        cout << "Federation already created by another federate." 
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
  

    try {
        myFedAmb.myObjectID = rtiAmb.getObjectClassHandle("myObjectClass");
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }
    cout << "myObject ID:" << myFedAmb.myObjectID << endl;


    /* get attribute handle */

    try {
        myFedAmb.val1Id = rtiAmb.getAttributeHandle("val1", myFedAmb.myObjectID);
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }
    cout << "val1Id:" << myFedAmb.val1Id << endl;

    try {
        myFedAmb.val2Id = rtiAmb.getAttributeHandle("val2", myFedAmb.myObjectID);
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }
    cout << "val2Id:" << myFedAmb.val2Id << endl;


    /* add attribute handle to AttributeHandleSet */
    auto_ptr<RTI::AttributeHandleSet>
    attrVAL(RTI::AttributeHandleSetFactory::create(2));
    attrVAL->add(myFedAmb.val1Id);
    attrVAL->add(myFedAmb.val2Id);

    /* subscribe to val1 and val2 */
    try {
        rtiAmb.subscribeObjectClassAttributes(myFedAmb.myObjectID, *attrVAL);
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }


    /* Time Management Setting */
    /* Declaration du temps local et du lookahead */
    myFedAmb.Set_LocalTime(0.0) ;
    myFedAmb.Set_TimeStep(1.0) ;
    myFedAmb.Set_Lookahead(0.0) ;
      
    /* Demande d'etre regulateur */
    rtiAmb.enableTimeRegulation(myFedAmb.Get_LocalTime(),myFedAmb.Get_Lookahead()) ;
      
    /* Demande d'etre contraint */   
    rtiAmb.enableTimeConstrained() ;

    /* Tant que les callbacks de validation de regulateur et contraint ne sont pas la => tick2() ! */
    while ( !myFedAmb.Get_IsTimeReg()) {
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
    cout << ">> Time management setting. "                       << endl ;  
    cout << ">> Is constrained ? " << myFedAmb.Get_IsTimeConst() << endl ;
    cout << ">> Is regulator ? " << myFedAmb.Get_IsTimeReg()     << endl ;

    /*******************************************************/
    /* IMPORTANT : Si la gestion du temps Reg et Contraint */
    /* enableAsynchronousDelivery() permet de recevoir des */
    /* callback telle que discoverObjectInstance           */
    /*******************************************************/    
    rtiAmb.enableAsynchronousDelivery() ;

   bool SYNCHRO_INITIALISATION = true;
    if(SYNCHRO_INITIALISATION){
        cout << " "                           << endl;
        cout << ">> Simulating the consumerCPP." << endl;  

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

    double cpt = 0;
    while (cpt <= 10) {

        /* update attribute */
        /* while (!myFedAmb.getNewVal1() && !myFedAmb.getNewVal2()) {
            try {
                rtiAmb.tick2();
            } catch ( RTI::Exception &e ) {
                cerr << "RTI exception: " << e._name << " ["
                << (e._reason ? e._reason : "undefined") << "]." << endl;
            } catch ( ... ) {
                cerr << "Error: unknown non-RTI exception." << endl;
            }

        }*/

	if (myFedAmb.getNewVal1()) {
	    myFedAmb.setNewVal1(false);
            cout << "Value val1 (update) received: " << myFedAmb.getVal1() << endl;
	}

        if (myFedAmb.getNewVal2()) {
	    myFedAmb.setNewVal2(false);
            cout << "Value val2 (update) received: " << myFedAmb.getVal2() << endl;
	}
	cpt += 1;

                /* demande davance dans le temps */              
        try {
             rtiAmb.nextEventRequest(myFedAmb.Get_LocalTime() 
                                       + myFedAmb.Get_TimeStep());  
	     cout << " >> NER(" << myFedAmb.Get_LocalTime() + myFedAmb.Get_TimeStep() << ") SEND == RequestedTime = " << myFedAmb.Get_LocalTime() + myFedAmb.Get_TimeStep() << " << " << endl; 

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

    try {
        rtiAmb.unsubscribeObjectClass(myFedAmb.val1Id);
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
    } catch ( ... ) {
        cerr << "Error: unknown non-RTI exception." << endl;
    }

   try {
        rtiAmb.unsubscribeObjectClass(myFedAmb.val2Id);
    } catch ( RTI::Exception &e ) {
        cerr << "RTI exception: " << e._name << " ["
        << (e._reason ? e._reason : "undefined") << "]." << endl;
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
