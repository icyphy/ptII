/*
 * File: ../../..//ptolemy/actor/corba/CorbaActor/CorbaIndexOutofBoundException.java
 * From: CorbaActor.idl
 * Date: Mon Jul 26 23:21:30 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.CorbaActor;
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
