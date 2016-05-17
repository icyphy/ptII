/**
 * Federation PRISE
 * ----------------
 * @file Joystick_Fed.cc
 *
 */

/*-----------------------------------------------------------------------------*/
/*			Federate Specific includes                             */
/*-----------------------------------------------------------------------------*/
#include "Joystick_Fed.hh"
#include "Common.hh"



/*-----------------------------------------------------------------------------*/
/*			Name space used ....                                   */
/*-----------------------------------------------------------------------------*/
using std::string;
using std::cout;
using std::cerr;
using std::endl;
using std::auto_ptr;

/*********************************************************/
/*****      Federate Ambassador Method*******************/
/*********************************************************/

/***** CONSTRUCTOR **********/
Joystick_Fed::Joystick_Fed() : NullFederateAmbassador() {

    _Aileron   = 0.0;	
    _Elevator  = 0.0;
    _Rudder    = 0.0;
    _Throttle_Left  	= 0.0;
    _Throttle_Right  	= 0.0;

    _Discov_USER_DATA                  	= false;
    _New_USER_DATA                  	= false;

  // variables time management
    _IsTimeReg = _IsTimeConst = _TimeAdvanceGrant = false;

  // variables synchro
    _SyncRegSuccess = _SyncRegFailed = _InPause = false;

  // Le federe Dynamique du vol est le createur donc false
    _Am_I_God = false;

} 

/***** LOCAL ALGORITHM **********/

void Joystick_Fed::Initialization() {
		/***** parse XML file ****/
		vector<string> input_vector(3);
		input_vector[0] = "JOYSTICK";
		input_vector[1] = "input";

		input_vector[2] = "Aileron";
		_Aileron_port = getFirstInt(DEFAULT_XML_PATH, input_vector);
		input_vector[2] = "Elevator";
		_Elevator_port  = getFirstInt(DEFAULT_XML_PATH, input_vector); 
		input_vector[2] = "Rudder";
		_Rudder_port    = getFirstInt(DEFAULT_XML_PATH, input_vector);
		input_vector[2] = "Throttle_Left";
		_Throttle_Left_port  = getFirstInt(DEFAULT_XML_PATH, input_vector);
		input_vector[2] = "Throttle_Right";
		_Throttle_Right_port  = getFirstInt(DEFAULT_XML_PATH, input_vector);
}

void Joystick_Fed::Compute_Local_State_Algorithm() {


}

void Joystick_Fed::Compute_Local_Output_Algorithm() {
				
    double* Inputs ;

    Inputs = _MyJoystick.getInputs() ;
			
    _Aileron   = Inputs[_Aileron_port];  //0
    _Elevator  = Inputs[_Elevator_port]; //1
    _Rudder    = Inputs[_Rudder_port];   //2
    _Throttle_Left  = Inputs[_Throttle_Left_port]; //3 
	_Throttle_Right  = Inputs[_Throttle_Right_port]; //3
    
    cout << "PUBLISHED :" << endl;
    cout << "Joystick (Elevator) :  " << _Elevator << endl;
    cout << "Joystick (Aileron) :   " << _Aileron << endl;
    cout << "Joystick (Rudder) :    " << _Rudder << endl;
    cout << "Joystick (Throttle Left) :  " << _Throttle_Left << endl;
    cout << "Joystick (Throttle Right) :  " << _Throttle_Right << endl;
}

/***** Discover Object Instance *****/


void Joystick_Fed::discoverObjectInstance(RTI::ObjectHandle theObject,
                                                RTI::ObjectClassHandle theObjectClass,
                                                const char *theObjectName)
                                         throw (RTI::CouldNotDiscover,
                                                RTI::ObjectClassNotKnown,
                                                RTI::FederateInternalError) {
  /* The name of the object Classe */
    if ( (theObjectClass == _USER_DATA_ClassHandle) && (!strcmp(theObjectName,"User_Data")) ) {
        _Discov_USER_DATA = true;
        _ObjInstance_USER_DATA_ObjectHandle = theObject;
    } 
} // Fin de 

/*********    Callback : Reflect Attribute Values without time           ***********/
void Joystick_Fed::reflectAttributeValues(RTI::ObjectHandle theObject,
                                                const RTI::AttributeHandleValuePairSet& theAttributes,
                                                const char *theTag)
                                         throw (RTI::ObjectNotKnown,
                                                RTI::AttributeNotKnown,
                                                RTI::FederateOwnsAttributes,
                                                RTI::FederateInternalError) {

    RTI::ULong valueLength ;
    RTI::AttributeHandle parmHandle ;
    char *attrValue ;

    libhla::MessageBuffer buffer;

    if (theObject == _ObjInstance_USER_DATA_ObjectHandle){
        for (unsigned int j=0 ; j<theAttributes.size(); j++) {

            parmHandle = theAttributes.getHandle(j);
            valueLength = theAttributes.getValueLength(j);
            assert(valueLength>0);
            buffer.resize(valueLength);        
            buffer.reset();        
            theAttributes.getValue(j, static_cast<char*>(buffer(0)), valueLength);        
            buffer.assumeSizeFromReservedBytes();

            if      (parmHandle == _ORDER_ATTRIBUTE) { buffer.read_double(); }
            else    { cout << "Fic Joystick_Fed.cc : ReflectAttributeValues function ==> ERREUR: handle inconnu." << endl; }
        }   
        _New_USER_DATA = true;
    } /* Fin du if (theObject == _ObjInstance_USER_DATA_ObjectHandle) */
    
}  /********* Fin de Callback : Reflect Attribute Values without time      ***********/

/*********     Callback : Reflect Attribute Values with time           ***********/
void Joystick_Fed::reflectAttributeValues(RTI::ObjectHandle theObject,
                                         const RTI::AttributeHandleValuePairSet& theAttributes,
                                         const RTI::FedTime& /*theTime*/,
                                         const char *theTag,
                                         RTI::EventRetractionHandle)
                                  throw (RTI::ObjectNotKnown,
                                         RTI::AttributeNotKnown,
                                         RTI::FederateOwnsAttributes,
                                         RTI::InvalidFederationTime, 
                                         RTI::FederateInternalError) {

    RTI::ULong valueLength ;
    RTI::AttributeHandle parmHandle ;
    char *attrValue ;

    libhla::MessageBuffer buffer;

    if (theObject == _ObjInstance_USER_DATA_ObjectHandle){
        for (unsigned int j=0 ; j<theAttributes.size(); j++) {

            parmHandle = theAttributes.getHandle(j);
            valueLength = theAttributes.getValueLength(j);
            assert(valueLength>0);
            buffer.resize(valueLength);        
            buffer.reset();        
            theAttributes.getValue(j, static_cast<char*>(buffer(0)), valueLength);        
            buffer.assumeSizeFromReservedBytes();

            if      (parmHandle == _ORDER_ATTRIBUTE) { 
		        double newOrder=buffer.read_double();
		        int code= (int) (newOrder/ORDER_LENGTH);
		        newOrder=newOrder-(code+0.5)*ORDER_LENGTH;

                if (newOrder == ORDER_STOP){     
		            if (code == 7) {_MyJoystick.setFailureInputs(0,1);}
		            if (code == 8) {_MyJoystick.setFailureInputs(1,1);}
		            if (code == 9) {_MyJoystick.setFailureInputs(2,1);}
		            if (code == 10) {_MyJoystick.setFailureInputs(3,1);}
		            if (code == 11) {_MyJoystick.setFailureInputs(4,1);}
                } else if (newOrder == ORDER_START){     
		            if (code == 7) {_MyJoystick.setFailureInputs(0,0);}
		            if (code == 8) {_MyJoystick.setFailureInputs(1,0);}
		            if (code == 9) {_MyJoystick.setFailureInputs(2,0);}
		            if (code == 10) {_MyJoystick.setFailureInputs(3,0);}
		            if (code == 11) {_MyJoystick.setFailureInputs(4,0);}
                } else {
		            if (code == 7) {_MyJoystick.setInputs(0,newOrder);}
		            if (code == 8) {_MyJoystick.setInputs(1,newOrder);}
		            if (code == 9) {_MyJoystick.setInputs(2,newOrder);}
		            if (code == 10) {_MyJoystick.setInputs(3,newOrder);}
		            if (code == 11) {_MyJoystick.setInputs(4,newOrder==1.0);}
                }
		    } 
            else    { cout << "Fic Joystick_Fed.cc : ReflectAttributeValues function ==> ERREUR: handle inconnu." << endl; }
        }   
        _New_USER_DATA = true;
    } /* Fin du if (theObject == _ObjInstance_USER_DATA_ObjectHandle) */
    
    
}  /********* Fin de Callback : Reflect Attribute Values with time      ***********/

/******************************************/
/* HLA specific methods : TIME MANAGEMENT */
/******************************************/
// Callback : timeRegulationEnabled
void Joystick_Fed::timeRegulationEnabled(const RTI::FedTime& theTime)
    throw ( RTI::InvalidFederationTime,
            RTI::EnableTimeRegulationWasNotPending,
            RTI::FederateInternalError) {
_IsTimeReg = true ;
} // End of timeRegulationEnabled

// Callback : timeConstrainedEnabled
void Joystick_Fed::timeConstrainedEnabled(const RTI::FedTime& theTime)
    throw ( RTI::InvalidFederationTime,
            RTI::EnableTimeConstrainedWasNotPending,
            RTI::FederateInternalError) {
_IsTimeConst = true ;
} // End of timeConstrainedEnabled

// Callback : timeAdvanceGrant
void Joystick_Fed::timeAdvanceGrant(const RTI::FedTime& theTime)
    throw ( RTI::InvalidFederationTime,
            RTI::TimeAdvanceWasNotInProgress,
            RTI::FederateInternalError) {
_LocalTime = theTime ;
_TimeAdvanceGrant =  true ;
if (TRACE_SIMU){ 
cout << "                                                " << endl;
cout << " >> TAG RECU == LocalTime = " <<  _LocalTime << " <<" << endl;
cout << "                                           " << endl;  
} // Fin du if (TRACE_SIMU){ 
} // End of timeAdvanceGrant

/******************************************/
/* HLA specific methods : SYNCHRONISATION */
/******************************************/

// Callback : synchronizationPointRegistrationSucceeded
void Joystick_Fed::synchronizationPointRegistrationSucceeded(const char *label)
    throw ( RTI::FederateInternalError) {
    _SyncRegSuccess = true ;
} // End of synchronizationPointRegistrationSucceeded

// Callback : synchronizationPointRegistrationFailed
void Joystick_Fed::synchronizationPointRegistrationFailed(const char *label)
    throw ( RTI::FederateInternalError) {
    _SyncRegFailed = true ;
} // End of synchronizationPointRegistrationFailed

// Callback : announceSynchronizationPoint
void Joystick_Fed::announceSynchronizationPoint(const char *label, const char *tag)
    throw ( RTI::FederateInternalError) {
       _InPause = true ;
} // End of announceSynchronizationPoint

// Callback : federationSynchronized
void Joystick_Fed::federationSynchronized(const char *label)
    throw ( RTI::FederateInternalError) {
    _InPause = false ;
} // End of federationSynchronized
