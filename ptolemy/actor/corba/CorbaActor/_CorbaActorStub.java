/*
 * File: ../../..//ptolemy/actor/corba/CorbaActor/_CorbaActorStub.java
 * From: CorbaActor.idl
 * Date: Mon Jul 26 23:21:30 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.CorbaActor;
public class _CorbaActorStub
	extends org.omg.CORBA.portable.ObjectImpl
    	implements ptolemy.actor.corba.CorbaActor.CorbaActor {

    public _CorbaActorStub(org.omg.CORBA.portable.Delegate d) {
          super();
          _set_delegate(d);
    }

    private static final String _type_ids[] = {
        "IDL:CorbaActor/CorbaActor:1.0"
    };

    public String[] _ids() { return (String[]) _type_ids.clone(); }

    //	IDL operations
    //	    Implementation of ::CorbaActor::CorbaActor::fire
    public void fire()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("fire");
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
   }
    //	    Implementation of ::CorbaActor::CorbaActor::getParameter
    public String getParameter(String paramName)
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException, ptolemy.actor.corba.CorbaActor.CorbaUnknownParamException {
           org.omg.CORBA.Request r = _request("getParameter");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           org.omg.CORBA.Any _paramName = r.add_in_arg();
           _paramName.insert_string(paramName);
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaUnknownParamExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaUnknownParamExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaUnknownParamExceptionHelper.extract(__userEx.except);
               }
           }
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::CorbaActor::CorbaActor::initialize
    public void initialize()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("initialize");
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
   }
    //	    Implementation of ::CorbaActor::CorbaActor::hasData
    public boolean hasData(String portName, short portIndex)
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("hasData");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
           org.omg.CORBA.Any _portName = r.add_in_arg();
           _portName.insert_string(portName);
           org.omg.CORBA.Any _portIndex = r.add_in_arg();
           _portIndex.insert_short(portIndex);
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
           boolean __result;
           __result = r.return_value().extract_boolean();
           return __result;
   }
    //	    Implementation of ::CorbaActor::CorbaActor::postfire
    public boolean postfire()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("postfire");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
           boolean __result;
           __result = r.return_value().extract_boolean();
           return __result;
   }
    //	    Implementation of ::CorbaActor::CorbaActor::prefire
    public boolean prefire()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("prefire");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
           boolean __result;
           __result = r.return_value().extract_boolean();
           return __result;
   }
    //	    Implementation of ::CorbaActor::CorbaActor::setParameter
    public void setParameter(String paramName, String paramValue)
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException, ptolemy.actor.corba.CorbaActor.CorbaUnknownParamException, ptolemy.actor.corba.CorbaActor.CorbaIllegalValueException {
           org.omg.CORBA.Request r = _request("setParameter");
           org.omg.CORBA.Any _paramName = r.add_in_arg();
           _paramName.insert_string(paramName);
           org.omg.CORBA.Any _paramValue = r.add_in_arg();
           _paramValue.insert_string(paramValue);
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaUnknownParamExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalValueExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaUnknownParamExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaUnknownParamExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalValueExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalValueExceptionHelper.extract(__userEx.except);
               }
           }
   }
    //	    Implementation of ::CorbaActor::CorbaActor::stopFire
    public void stopFire()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("stopFire");
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
   }
    //	    Implementation of ::CorbaActor::CorbaActor::terminate
    public void terminate()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("terminate");
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
   }
    //	    Implementation of ::CorbaActor::CorbaActor::transferInput
    public void transferInput(String portName, short portIndex, String tokenValue)
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException, ptolemy.actor.corba.CorbaActor.CorbaUnknownPortException, ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundException, ptolemy.actor.corba.CorbaActor.CorbaIllegalValueException {
           org.omg.CORBA.Request r = _request("transferInput");
           org.omg.CORBA.Any _portName = r.add_in_arg();
           _portName.insert_string(portName);
           org.omg.CORBA.Any _portIndex = r.add_in_arg();
           _portIndex.insert_short(portIndex);
           org.omg.CORBA.Any _tokenValue = r.add_in_arg();
           _tokenValue.insert_string(tokenValue);
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaUnknownPortExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalValueExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaUnknownPortExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaUnknownPortExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalValueExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalValueExceptionHelper.extract(__userEx.except);
               }
           }
   }
    //	    Implementation of ::CorbaActor::CorbaActor::transferOutput
    public String transferOutput(String portName, short portIndex)
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException, ptolemy.actor.corba.CorbaActor.CorbaUnknownPortException, ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundException {
           org.omg.CORBA.Request r = _request("transferOutput");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           org.omg.CORBA.Any _portName = r.add_in_arg();
           _portName.insert_string(portName);
           org.omg.CORBA.Any _portIndex = r.add_in_arg();
           _portIndex.insert_short(portIndex);
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaUnknownPortExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaUnknownPortExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaUnknownPortExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundExceptionHelper.extract(__userEx.except);
               }
           }
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::CorbaActor::CorbaActor::wrapup
    public void wrapup()
        throws ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("wrapup");
           r.exceptions().add(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
   }

};
