/*
 * File: ../../../ptolemy/actor/corba/util/CorbaIllegalValueException.java
 * From: CorbaActor.idl
 * Date: Tue Jul 27 13:54:19 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.util;
public final class CorbaIllegalValueException
	extends org.omg.CORBA.UserException implements org.omg.CORBA.portable.IDLEntity {
    //	instance variables
    public String message;
    //	constructors
    public CorbaIllegalValueException() {
	super();
    }
    public CorbaIllegalValueException(String __message) {
	super();
	message = __message;
    }
}
