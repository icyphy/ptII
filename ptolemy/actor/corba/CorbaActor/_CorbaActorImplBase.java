/*
 * File: ../../..//ptolemy/actor/corba/CorbaActor/_CorbaActorImplBase.java
 * From: CorbaActor.idl
 * Date: Mon Jul 26 23:21:30 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.CorbaActor;
public abstract class _CorbaActorImplBase extends org.omg.CORBA.DynamicImplementation implements ptolemy.actor.corba.CorbaActor.CorbaActor {
    // Constructor
    public _CorbaActorImplBase() {
         super();
    }
    // Type strings for this class and its superclases
    private static final String _type_ids[] = {
        "IDL:CorbaActor/CorbaActor:1.0"
    };

    public String[] _ids() { return (String[]) _type_ids.clone(); }

    private static java.util.Dictionary _methods = new java.util.Hashtable();
    static {
      _methods.put("fire", new java.lang.Integer(0));
      _methods.put("getParameter", new java.lang.Integer(1));
      _methods.put("initialize", new java.lang.Integer(2));
      _methods.put("hasData", new java.lang.Integer(3));
      _methods.put("postfire", new java.lang.Integer(4));
      _methods.put("prefire", new java.lang.Integer(5));
      _methods.put("setParameter", new java.lang.Integer(6));
      _methods.put("stopFire", new java.lang.Integer(7));
      _methods.put("terminate", new java.lang.Integer(8));
      _methods.put("transferInput", new java.lang.Integer(9));
      _methods.put("transferOutput", new java.lang.Integer(10));
      _methods.put("wrapup", new java.lang.Integer(11));
     }
    // DSI Dispatch call
    public void invoke(org.omg.CORBA.ServerRequest r) {
       switch (((java.lang.Integer) _methods.get(r.op_name())).intValue()) {
           case 0: // ptolemy.actor.corba.CorbaActor.CorbaActor.fire
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              try {
                            this.fire();
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __return = _orb().create_any();
              __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
              r.result(__return);
              }
              break;
           case 1: // ptolemy.actor.corba.CorbaActor.CorbaActor.getParameter
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              org.omg.CORBA.Any _paramName = _orb().create_any();
              _paramName.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
              _list.add_value("paramName", _paramName, org.omg.CORBA.ARG_IN.value);
              r.params(_list);
              String paramName;
              paramName = _paramName.extract_string();
              String ___result;
              try {
                            ___result = this.getParameter(paramName);
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaUnknownParamException e1) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaUnknownParamExceptionHelper.insert(_except, e1);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_string(___result);
              r.result(__result);
              }
              break;
           case 2: // ptolemy.actor.corba.CorbaActor.CorbaActor.initialize
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              try {
                            this.initialize();
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __return = _orb().create_any();
              __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
              r.result(__return);
              }
              break;
           case 3: // ptolemy.actor.corba.CorbaActor.CorbaActor.hasData
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              org.omg.CORBA.Any _portName = _orb().create_any();
              _portName.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
              _list.add_value("portName", _portName, org.omg.CORBA.ARG_IN.value);
              org.omg.CORBA.Any _portIndex = _orb().create_any();
              _portIndex.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short));
              _list.add_value("portIndex", _portIndex, org.omg.CORBA.ARG_IN.value);
              r.params(_list);
              String portName;
              portName = _portName.extract_string();
              short portIndex;
              portIndex = _portIndex.extract_short();
              boolean ___result;
              try {
                            ___result = this.hasData(portName, portIndex);
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_boolean(___result);
              r.result(__result);
              }
              break;
           case 4: // ptolemy.actor.corba.CorbaActor.CorbaActor.postfire
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              boolean ___result;
              try {
                            ___result = this.postfire();
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_boolean(___result);
              r.result(__result);
              }
              break;
           case 5: // ptolemy.actor.corba.CorbaActor.CorbaActor.prefire
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              boolean ___result;
              try {
                            ___result = this.prefire();
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_boolean(___result);
              r.result(__result);
              }
              break;
           case 6: // ptolemy.actor.corba.CorbaActor.CorbaActor.setParameter
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              org.omg.CORBA.Any _paramName = _orb().create_any();
              _paramName.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
              _list.add_value("paramName", _paramName, org.omg.CORBA.ARG_IN.value);
              org.omg.CORBA.Any _paramValue = _orb().create_any();
              _paramValue.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
              _list.add_value("paramValue", _paramValue, org.omg.CORBA.ARG_IN.value);
              r.params(_list);
              String paramName;
              paramName = _paramName.extract_string();
              String paramValue;
              paramValue = _paramValue.extract_string();
              try {
                            this.setParameter(paramName, paramValue);
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaUnknownParamException e1) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaUnknownParamExceptionHelper.insert(_except, e1);
                            r.except(_except);
                            return;
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalValueException e2) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalValueExceptionHelper.insert(_except, e2);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __return = _orb().create_any();
              __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
              r.result(__return);
              }
              break;
           case 7: // ptolemy.actor.corba.CorbaActor.CorbaActor.stopFire
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              try {
                            this.stopFire();
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __return = _orb().create_any();
              __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
              r.result(__return);
              }
              break;
           case 8: // ptolemy.actor.corba.CorbaActor.CorbaActor.terminate
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              try {
                            this.terminate();
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __return = _orb().create_any();
              __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
              r.result(__return);
              }
              break;
           case 9: // ptolemy.actor.corba.CorbaActor.CorbaActor.transferInput
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              org.omg.CORBA.Any _portName = _orb().create_any();
              _portName.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
              _list.add_value("portName", _portName, org.omg.CORBA.ARG_IN.value);
              org.omg.CORBA.Any _portIndex = _orb().create_any();
              _portIndex.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short));
              _list.add_value("portIndex", _portIndex, org.omg.CORBA.ARG_IN.value);
              org.omg.CORBA.Any _tokenValue = _orb().create_any();
              _tokenValue.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
              _list.add_value("tokenValue", _tokenValue, org.omg.CORBA.ARG_IN.value);
              r.params(_list);
              String portName;
              portName = _portName.extract_string();
              short portIndex;
              portIndex = _portIndex.extract_short();
              String tokenValue;
              tokenValue = _tokenValue.extract_string();
              try {
                            this.transferInput(portName, portIndex, tokenValue);
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaUnknownPortException e1) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaUnknownPortExceptionHelper.insert(_except, e1);
                            r.except(_except);
                            return;
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundException e2) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundExceptionHelper.insert(_except, e2);
                            r.except(_except);
                            return;
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalValueException e3) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalValueExceptionHelper.insert(_except, e3);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __return = _orb().create_any();
              __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
              r.result(__return);
              }
              break;
           case 10: // ptolemy.actor.corba.CorbaActor.CorbaActor.transferOutput
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              org.omg.CORBA.Any _portName = _orb().create_any();
              _portName.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
              _list.add_value("portName", _portName, org.omg.CORBA.ARG_IN.value);
              org.omg.CORBA.Any _portIndex = _orb().create_any();
              _portIndex.type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short));
              _list.add_value("portIndex", _portIndex, org.omg.CORBA.ARG_IN.value);
              r.params(_list);
              String portName;
              portName = _portName.extract_string();
              short portIndex;
              portIndex = _portIndex.extract_short();
              String ___result;
              try {
                            ___result = this.transferOutput(portName, portIndex);
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaUnknownPortException e1) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaUnknownPortExceptionHelper.insert(_except, e1);
                            r.except(_except);
                            return;
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundException e2) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundExceptionHelper.insert(_except, e2);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __result = _orb().create_any();
              __result.insert_string(___result);
              r.result(__result);
              }
              break;
           case 11: // ptolemy.actor.corba.CorbaActor.CorbaActor.wrapup
              {
              org.omg.CORBA.NVList _list = _orb().create_list(0);
              r.params(_list);
              try {
                            this.wrapup();
              }
              catch (ptolemy.actor.corba.CorbaActor.CorbaIllegalActionException e0) {
                            org.omg.CORBA.Any _except = _orb().create_any();
                            ptolemy.actor.corba.CorbaActor.CorbaIllegalActionExceptionHelper.insert(_except, e0);
                            r.except(_except);
                            return;
              }
              org.omg.CORBA.Any __return = _orb().create_any();
              __return.type(_orb().get_primitive_tc(org.omg.CORBA.TCKind.tk_void));
              r.result(__return);
              }
              break;
            default:
              throw new org.omg.CORBA.BAD_OPERATION(0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
       }
 }
}
