/*
 * File: ../../..//ptolemy/actor/corba/CorbaActor/CorbaIllegalValueExceptionHolder.java
 * From: CorbaActor.idl
 * Date: Mon Jul 26 23:21:30 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.CorbaActor;
public final class CorbaIllegalValueExceptionHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public ptolemy.actor.corba.CorbaActor.CorbaIllegalValueException value;
    //	constructors 
    public CorbaIllegalValueExceptionHolder() {
	this(null);
    }
    public CorbaIllegalValueExceptionHolder(ptolemy.actor.corba.CorbaActor.CorbaIllegalValueException __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        ptolemy.actor.corba.CorbaActor.CorbaIllegalValueExceptionHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = ptolemy.actor.corba.CorbaActor.CorbaIllegalValueExceptionHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return ptolemy.actor.corba.CorbaActor.CorbaIllegalValueExceptionHelper.type();
    }
}
