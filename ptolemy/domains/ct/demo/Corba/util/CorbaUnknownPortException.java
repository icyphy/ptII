/*
 * File: ../../../../../PTOLEMY/DOMAINS/CT/DEMO/CORBA/UTIL/CORBAUNKNOWNPORTEXCEPTION.JAVA
 * From: CORBAACTOR.IDL
 * Date: Thu Jul 29 14:22:20 1999
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package ptolemy.domains.ct.demo.Corba.util;
public final class CorbaUnknownPortException
	extends org.omg.CORBA.UserException implements org.omg.CORBA.portable.IDLEntity {
    //	instance variables
    public String portName;
    public String message;
    //	constructors
    public CorbaUnknownPortException() {
	super();
    }
    public CorbaUnknownPortException(String __portName, String __message) {
	super();
	portName = __portName;
	message = __message;
    }
}
