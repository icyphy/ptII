/*
 * File: ../../../ptolemy/actor/corba/util/_CorbaActorStub.java
 * From: CorbaActor.idl
 * Date: Wed Jul 28 17:18:28 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.util;
public class _CorbaActorStub
	extends org.omg.CORBA.portable.ObjectImpl
    	implements ptolemy.actor.corba.util.CorbaActor {

    public _CorbaActorStub(org.omg.CORBA.portable.Delegate d) {
          super();
          _set_delegate(d);
    }

    private static final String _type_ids[] = {
        "IDL:util/CorbaActor:1.0"
    };

    public String[] _ids() { return (String[]) _type_ids.clone(); }

    //	IDL operations
    //	    Implementation of ::util::CorbaActor::fire
    public void fire()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("fire");
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
   }
    //	    Implementation of ::util::CorbaActor::getParameter
    public String getParameter(String paramName)
        throws ptolemy.actor.corba.util.CorbaIllegalActionException, ptolemy.actor.corba.util.CorbaUnknownParamException {
           org.omg.CORBA.Request r = _request("getParameter");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           org.omg.CORBA.Any _paramName = r.add_in_arg();
           _paramName.insert_string(paramName);
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.util.CorbaUnknownParamExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaUnknownParamExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaUnknownParamExceptionHelper.extract(__userEx.except);
               }
           }
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::util::CorbaActor::initialize
    public void initialize()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("initialize");
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
   }
    //	    Implementation of ::util::CorbaActor::hasData
    public boolean hasData(String portName, short portIndex)
        throws ptolemy.actor.corba.util.CorbaIllegalActionException, ptolemy.actor.corba.util.CorbaIndexOutofBoundException, ptolemy.actor.corba.util.CorbaUnknownPortException {
           org.omg.CORBA.Request r = _request("hasData");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
           org.omg.CORBA.Any _portName = r.add_in_arg();
           _portName.insert_string(portName);
           org.omg.CORBA.Any _portIndex = r.add_in_arg();
           _portIndex.insert_short(portIndex);
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIndexOutofBoundExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIndexOutofBoundExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIndexOutofBoundExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.extract(__userEx.except);
               }
           }
           boolean __result;
           __result = r.return_value().extract_boolean();
           return __result;
   }
    //	    Implementation of ::util::CorbaActor::hasParameter
    public boolean hasParameter(String paramName)
 {
           org.omg.CORBA.Request r = _request("hasParameter");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
           org.omg.CORBA.Any _paramName = r.add_in_arg();
           _paramName.insert_string(paramName);
           r.invoke();
           boolean __result;
           __result = r.return_value().extract_boolean();
           return __result;
   }
    //	    Implementation of ::util::CorbaActor::hasPort
    public boolean hasPort(String portName, boolean isInput, boolean isOutput, boolean isMultiport)
 {
           org.omg.CORBA.Request r = _request("hasPort");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
           org.omg.CORBA.Any _portName = r.add_in_arg();
           _portName.insert_string(portName);
           org.omg.CORBA.Any _isInput = r.add_in_arg();
           _isInput.insert_boolean(isInput);
           org.omg.CORBA.Any _isOutput = r.add_in_arg();
           _isOutput.insert_boolean(isOutput);
           org.omg.CORBA.Any _isMultiport = r.add_in_arg();
           _isMultiport.insert_boolean(isMultiport);
           r.invoke();
           boolean __result;
           __result = r.return_value().extract_boolean();
           return __result;
   }
    //	    Implementation of ::util::CorbaActor::setPortWidth
    public void setPortWidth(String portName, short width)
        throws ptolemy.actor.corba.util.CorbaIllegalActionException, ptolemy.actor.corba.util.CorbaUnknownPortException {
           org.omg.CORBA.Request r = _request("setPortWidth");
           org.omg.CORBA.Any _portName = r.add_in_arg();
           _portName.insert_string(portName);
           org.omg.CORBA.Any _width = r.add_in_arg();
           _width.insert_short(width);
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.extract(__userEx.except);
               }
           }
   }
    //	    Implementation of ::util::CorbaActor::postfire
    public boolean postfire()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("postfire");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
           boolean __result;
           __result = r.return_value().extract_boolean();
           return __result;
   }
    //	    Implementation of ::util::CorbaActor::prefire
    public boolean prefire()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("prefire");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_boolean));
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
           boolean __result;
           __result = r.return_value().extract_boolean();
           return __result;
   }
    //	    Implementation of ::util::CorbaActor::setParameter
    public void setParameter(String paramName, String paramValue)
        throws ptolemy.actor.corba.util.CorbaIllegalActionException, ptolemy.actor.corba.util.CorbaUnknownParamException, ptolemy.actor.corba.util.CorbaIllegalValueException {
           org.omg.CORBA.Request r = _request("setParameter");
           org.omg.CORBA.Any _paramName = r.add_in_arg();
           _paramName.insert_string(paramName);
           org.omg.CORBA.Any _paramValue = r.add_in_arg();
           _paramValue.insert_string(paramValue);
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.util.CorbaUnknownParamExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalValueExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaUnknownParamExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaUnknownParamExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalValueExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalValueExceptionHelper.extract(__userEx.except);
               }
           }
   }
    //	    Implementation of ::util::CorbaActor::stopFire
    public void stopFire()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("stopFire");
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
   }
    //	    Implementation of ::util::CorbaActor::terminate
    public void terminate()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("terminate");
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
   }
    //	    Implementation of ::util::CorbaActor::transferInput
    public void transferInput(String portName, short portIndex, String tokenValue)
        throws ptolemy.actor.corba.util.CorbaIllegalActionException, ptolemy.actor.corba.util.CorbaUnknownPortException, ptolemy.actor.corba.util.CorbaIndexOutofBoundException, ptolemy.actor.corba.util.CorbaIllegalValueException {
           org.omg.CORBA.Request r = _request("transferInput");
           org.omg.CORBA.Any _portName = r.add_in_arg();
           _portName.insert_string(portName);
           org.omg.CORBA.Any _portIndex = r.add_in_arg();
           _portIndex.insert_short(portIndex);
           org.omg.CORBA.Any _tokenValue = r.add_in_arg();
           _tokenValue.insert_string(tokenValue);
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIndexOutofBoundExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalValueExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIndexOutofBoundExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIndexOutofBoundExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalValueExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalValueExceptionHelper.extract(__userEx.except);
               }
           }
   }
    //	    Implementation of ::util::CorbaActor::transferOutput
    public String transferOutput(String portName, short portIndex)
        throws ptolemy.actor.corba.util.CorbaIllegalActionException, ptolemy.actor.corba.util.CorbaUnknownPortException, ptolemy.actor.corba.util.CorbaIndexOutofBoundException {
           org.omg.CORBA.Request r = _request("transferOutput");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           org.omg.CORBA.Any _portName = r.add_in_arg();
           _portName.insert_string(portName);
           org.omg.CORBA.Any _portIndex = r.add_in_arg();
           _portIndex.insert_short(portIndex);
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.type());
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIndexOutofBoundExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIndexOutofBoundExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIndexOutofBoundExceptionHelper.extract(__userEx.except);
               }
           }
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::util::CorbaActor::wrapup
    public void wrapup()
        throws ptolemy.actor.corba.util.CorbaIllegalActionException {
           org.omg.CORBA.Request r = _request("wrapup");
           r.exceptions().add(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.type())) {
                   throw ptolemy.actor.corba.util.CorbaIllegalActionExceptionHelper.extract(__userEx.except);
               }
           }
   }

};
