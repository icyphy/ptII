/*
 * File: ../../../ptolemy/actor/corba/util/CorbaUnknownParamException.java
 * From: CorbaActor.idl
 * Date: Tue Jul 27 13:54:19 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.util;
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
