#ifndef JOYSTICK_FED_HH_DEF
#define JOYSTICK_FED_HH_DEF

/*
 * Linux System includes
 */
#include <iostream>
#include <memory>
#include <string>
#include <string.h>
/*
 * <RTI.hh>
 * This header is the necessary include for using HLA RunTime Infrastructure
 * API.
 */
#include <RTI.hh>

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

/* Useful here : see with Eric */
#include <MessageBuffer.hh>
#include <assert.h>

#include "YokePedalSystem.hh"
#include "Common.hh" 

/*
 * Joystick_Fed Class Declaration
 */
class Joystick_Fed : public NullFederateAmbassador {

private:

  /****************************************************/
  /* Methodes pour les Handles des donnees souscrites */
  /****************************************************/

  /* Handle des objets auxquels on souscrit */
 
  /* Handle des attributs souscris et fournis par le federe Flight_Dynamics */

  /* Flags pour savoir si une nouvelle donnee est disponible */

  /****************************************************/
  /* Methodes pour les Handles des donnees publiees */
  /****************************************************/

    RTI::ObjectClassHandle _JOYSTICK_ClassHandle; 

  /* Handle des objets que l'on publie */

    RTI::ObjectHandle _ObjInstance_JOYSTICK_ObjectHandle;

  /* Handle des attributs mesure par les Joystick */
  /* Classe JOYSTICK souscrit */
    RTI::AttributeHandle _AILERON_ATTRIBUTE, 
                         _ELEVATOR_ATTRIBUTE, 
                         _RUDDER_ATTRIBUTE, 
                         _THROTTLE_LEFT_ATTRIBUTE, 
                         _THROTTLE_RIGHT_ATTRIBUTE;
                         
    double _Aileron, _Elevator, _Rudder, _Throttle_Left, _Throttle_Right;
    
	/* Port numbers of the joystick */                     
	int _Aileron_port, _Elevator_port, _Rudder_port, _Throttle_Left_port, _Throttle_Right_port;

  /****************************************************/
  /* Methodes pour les Handles des donnees souscrite */
  /****************************************************/


    RTI::ObjectClassHandle _USER_DATA_ClassHandle;

  /* Handle des objets auxquels on souscrit */
  /* Cette variable est modifiee dans le discover object instance */

    RTI::ObjectHandle _ObjInstance_USER_DATA_ObjectHandle;

  /* Handle des attributs souscris et fournis par ... */
  /* Classe USER_DATA souscrit */
    RTI::AttributeHandle _ORDER_ATTRIBUTE; 


  /* Flags pour savoir si une nouvelle donnee est disponible */
    bool _Discov_USER_DATA;
    bool _New_USER_DATA;


  /************************************************************/
  /* Declaration des objets Joystick (Classe YokePedalSystem) */
  /************************************************************/
    YokePedalSystem _MyJoystick ;

  /*****************************/
  /* TIME MANAGEMENT VARIABLES */
  /*****************************/
  // flag pour savoir si le federe est regulateur et/ou contraint
    bool _IsTimeReg, _IsTimeConst, _TimeAdvanceGrant;
    RTIfedTime _LocalTime ;
    RTIfedTime _TimeStep ;   
    RTIfedTime _Lookahead ;   
    RTIfedTime _RestOfTimeStep ; 

  /*****************************/
  /* SYNCHRONIZATION VARIABLES */
  /*****************************/
  // flag pour savoir si le federe est synchronise ou pas
    bool _SyncRegSuccess, _SyncRegFailed, _InPause; 

  /*****************************/
  /* TIME MANAGEMENT VARIABLES */
  /*****************************/
    bool _Am_I_God ; 

public:

  /*********************************************************/
  /*****       Specific Federate Methods      **************/
  /*********************************************************/
    Joystick_Fed() ;
    virtual ~Joystick_Fed() throw (RTI::FederateInternalError) {}

  // Methode qui contient l'algorithme propre du federe
    void Initialization();
    void Compute_Local_State_Algorithm();
    void Compute_Local_Output_Algorithm();

    double Get_AILERON()   { return _Aileron;} ;
    double Get_ELEVATOR()  { return _Elevator;} ;
    double Get_RUDDER()    { return _Rudder;} ;
    double Get_THROTTLE_LEFT()  { return _Throttle_Left;} ;
    double Get_THROTTLE_RIGHT()  { return _Throttle_Right;} ;
    
    void Set_AILERON(double val)  { _Aileron = val;} ;
    void Set_ELEVATOR(double val) { _Elevator = val;} ;
    void Set_RUDDER(double val)   { _Rudder = val;} ;
    void Set_THROTTLE_LEFT(double val) { _Throttle_Left = val;} ;
    void Set_THROTTLE_RIGHT(double val) { _Throttle_Right = val;} ;
    
  /******************************************/
  /* Accesseurs pour les flags de reception */
  /******************************************/ 
  
    bool Get_Discov_USER_DATA()            	 	{ return _Discov_USER_DATA;} ;

    void Set_Discov_USER_DATA(bool val)            	{ _Discov_USER_DATA = val;} ;
   
    bool Get_New_USER_DATA()                  		{ return _New_USER_DATA;} ;

    void Set_New_USER_DATA(bool val)           	      	{ _New_USER_DATA = val;} ;

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

  /***************************************************/
  /* Accesseurs pour les flags de la synchronisation */
  /***************************************************/
    bool Get_SyncRegSuccess() { return _SyncRegSuccess ;} ;
    bool Get_SyncRegFailed()  { return _SyncRegFailed ;} ; 
    bool Get_InPause()        { return _InPause ;} ;
    
    void Set_SyncRegSuccess(bool val) {_SyncRegSuccess = val;} ;
    void Set_SyncRegFailed(bool val)  {_SyncRegFailed = val;} ;
    void Set_InPause(bool val)        {_InPause = val;} ;

  /**********************************************/
  /* Accesseurs pour le flag de Createur ou pas */
  /**********************************************/
    bool Get_Am_I_God() { return _Am_I_God ;} ;
    void Set_Am_I_God(bool val) {_Am_I_God = val;} ;
    
  /**************************************************/
  /* Methodes pour les Handles des donnees publiees */
  /**************************************************/
    
  /* Objects Class Handle */
    RTI::ObjectClassHandle Get_JOYSTICK_ClassHandle()          			{ return _JOYSTICK_ClassHandle; } ;
   
    void Set_JOYSTICK_ClassHandle(RTI::ObjectClassHandle Handle)          	{ _JOYSTICK_ClassHandle = Handle; } ;  
    
  /* Handle des objets */
    RTI::ObjectHandle Get_ObjInstance_JOYSTICK_ObjectHandle()         		 { return _ObjInstance_JOYSTICK_ObjectHandle; } ;

    void Set_ObjInstance_JOYSTICK_ObjectHandle(RTI::ObjectHandle Handle)          { _ObjInstance_JOYSTICK_ObjectHandle = Handle; } ;

  /* Attributes Handles */
    RTI::AttributeHandle Get_AILERON_ATTRIBUTE()   { return _AILERON_ATTRIBUTE; } ;
    RTI::AttributeHandle Get_ELEVATOR_ATTRIBUTE()  { return _ELEVATOR_ATTRIBUTE; } ;
    RTI::AttributeHandle Get_RUDDER_ATTRIBUTE()    { return _RUDDER_ATTRIBUTE; } ;
    RTI::AttributeHandle Get_THROTTLE_LEFT_ATTRIBUTE()  { return _THROTTLE_LEFT_ATTRIBUTE; } ;
    RTI::AttributeHandle Get_THROTTLE_RIGHT_ATTRIBUTE()  { return _THROTTLE_RIGHT_ATTRIBUTE; } ;
    
    void Set_AILERON_ATTRIBUTE(RTI::AttributeHandle Handle)           	{ _AILERON_ATTRIBUTE = Handle; } ; 
    void Set_ELEVATOR_ATTRIBUTE(RTI::AttributeHandle Handle) 			{ _ELEVATOR_ATTRIBUTE = Handle; } ;
    void Set_RUDDER_ATTRIBUTE(RTI::AttributeHandle Handle)            	{ _RUDDER_ATTRIBUTE = Handle; } ;
    void Set_THROTTLE_LEFT_ATTRIBUTE(RTI::AttributeHandle Handle) 		{ _THROTTLE_LEFT_ATTRIBUTE = Handle; } ;
    void Set_THROTTLE_RIGHT_ATTRIBUTE(RTI::AttributeHandle Handle) 		{ _THROTTLE_RIGHT_ATTRIBUTE = Handle; } ; 



  /************************************************/
  /* Methodes pour les Handles des donnees recues */
  /************************************************/

  /* Objects Class Handle */
    RTI::ObjectClassHandle Get_USER_DATA_ClassHandle()       	   		{ return _USER_DATA_ClassHandle; } ;

    void Set_USER_DATA_ClassHandle(RTI::ObjectClassHandle Handle)             	{ _USER_DATA_ClassHandle = Handle; } ;

  /* Handle des objets  */
    RTI::ObjectHandle Get_ObjInstance_USER_DATA_ObjectHandle()                  { return _ObjInstance_USER_DATA_ObjectHandle; } ;

    void Set_ObjInstance_USER_DATA_ObjectHandle(RTI::ObjectHandle Handle)       { _ObjInstance_USER_DATA_ObjectHandle = Handle; } ;

  /* Attributes Handles */
    RTI::AttributeHandle Get_ORDER_ATTRIBUTE() { return _ORDER_ATTRIBUTE; } ;
    
    void Set_ORDER_ATTRIBUTE(RTI::AttributeHandle Handle) { _ORDER_ATTRIBUTE = Handle; } ;
 /*********************************************************/
  /*****      Federate Ambassador Methods      *************/
  /*********************************************************/  

  // Callback : discover object instance
    void discoverObjectInstance(RTI::ObjectHandle theObject,
                                RTI::ObjectClassHandle theObjectClass,
                                const char *theObjectName)
    throw (RTI::CouldNotDiscover,
           RTI::ObjectClassNotKnown,
           RTI::FederateInternalError);

  // Callback : reflect attribute values without time
    void reflectAttributeValues(RTI::ObjectHandle theObject,
                                const RTI::AttributeHandleValuePairSet& theAttributes,
                                const char *theTag)
    throw (RTI::ObjectNotKnown,
           RTI::AttributeNotKnown,
           RTI::FederateOwnsAttributes,
           RTI::FederateInternalError);

  // Callback : reflect attribute values with time
    void reflectAttributeValues(RTI::ObjectHandle theObject,
                                const RTI::AttributeHandleValuePairSet& theAttributes,
                                const RTI::FedTime& /*theTime*/,
                                const char *theTag,
                                RTI::EventRetractionHandle)
    throw (RTI::ObjectNotKnown,
           RTI::AttributeNotKnown,
           RTI::FederateOwnsAttributes,
           RTI::InvalidFederationTime, 
           RTI::FederateInternalError);

  /******************************************/
  /* HLA specific methods : TIME MANAGEMENT */
  /******************************************/

  // Callback : timeRegulationEnabled
    void timeRegulationEnabled(const RTI::FedTime& theTime)
    throw (RTI::InvalidFederationTime,
           RTI::EnableTimeRegulationWasNotPending,
           RTI::FederateInternalError);

  // Callback : timeConstrainedEnabled
    void timeConstrainedEnabled(const RTI::FedTime& theTime)
    throw (RTI::InvalidFederationTime,
           RTI::EnableTimeConstrainedWasNotPending,
           RTI::FederateInternalError);

  // Callback : timeAdvanceGrant
    void timeAdvanceGrant(const RTI::FedTime& theTime)
    throw (RTI::InvalidFederationTime,
           RTI::TimeAdvanceWasNotInProgress,
           RTI::FederateInternalError);

  /******************************************/
  /******************************************/
  /* HLA specific methods : SYNCHRONISATION */

  // Callback : synchronizationPointRegistrationSucceeded
    void synchronizationPointRegistrationSucceeded(const char *label)
    throw (RTI::FederateInternalError) ;

  // Callback : synchronizationPointRegistrationFailed
    void synchronizationPointRegistrationFailed(const char *label)
    throw (RTI::FederateInternalError) ;

  // Callback : announceSynchronizationPoint
    void announceSynchronizationPoint(const char *label, const char *tag)
    throw (RTI::FederateInternalError) ;

  // Callback : federationSynchronized
    void federationSynchronized(const char *label)
    throw (RTI::FederateInternalError) ;

} ;

#endif // JOYSTICK_FED_HH_DEF
