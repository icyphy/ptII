/*
 * File: ../../..//ptolemy/actor/corba/CorbaActor/CorbaIndexOutofBoundExceptionHolder.java
 * From: CorbaActor.idl
 * Date: Mon Jul 26 23:21:30 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.CorbaActor;
public final class CorbaIndexOutofBoundExceptionHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundException value;
    //	constructors 
    public CorbaIndexOutofBoundExceptionHolder() {
	this(null);
    }
    public CorbaIndexOutofBoundExceptionHolder(ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundException __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundExceptionHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundExceptionHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return ptolemy.actor.corba.CorbaActor.CorbaIndexOutofBoundExceptionHelper.type();
    }
}
