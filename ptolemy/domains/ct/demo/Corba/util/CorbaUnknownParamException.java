/*
 * File: ../../../../../PTOLEMY/DOMAINS/CT/DEMO/CORBA/UTIL/CORBAUNKNOWNPARAMEXCEPTION.JAVA
 * From: CORBAACTOR.IDL
 * Date: Thu Jul 29 14:22:20 1999
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package ptolemy.domains.ct.demo.Corba.util;
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
