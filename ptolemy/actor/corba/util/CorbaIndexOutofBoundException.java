/*
 * File: ../../../ptolemy/actor/corba/util/CorbaIndexOutofBoundException.java
 * From: CorbaActor.idl
 * Date: Tue Jul 27 13:54:19 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.util;
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
