/*
 * File: ../../../../../PTOLEMY/DOMAINS/CT/DEMO/CORBA/UTIL/CORBAINDEXOUTOFBOUNDEXCEPTION.JAVA
 * From: CORBAACTOR.IDL
 * Date: Thu Jul 29 14:22:20 1999
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package ptolemy.domains.ct.demo.Corba.util;
public final class CorbaIndexOutofBoundException
	extends org.omg.CORBA.UserException implements org.omg.CORBA.portable.IDLEntity {
    //	instance variables
    public short index;
    //	constructors
    public CorbaIndexOutofBoundException() {
	super();
    }
    public CorbaIndexOutofBoundException(short __index) {
	super();
	index = __index;
    }
}
