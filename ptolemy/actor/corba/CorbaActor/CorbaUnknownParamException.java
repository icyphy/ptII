/*
 * File: ../../..//ptolemy/actor/corba/CorbaActor/CorbaUnknownParamException.java
 * From: CorbaActor.idl
 * Date: Mon Jul 26 23:21:30 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.CorbaActor;
public final class CorbaUnknownParamException
	extends org.omg.CORBA.UserException implements org.omg.CORBA.portable.IDLEntity {
    //	instance variables
    public String paramName;
    public String message;
    //	constructors
    public CorbaUnknownParamException() {
	super();
    }
    public CorbaUnknownParamException(String __paramName, String __message) {
	super();
	paramName = __paramName;
	message = __message;
    }
}
