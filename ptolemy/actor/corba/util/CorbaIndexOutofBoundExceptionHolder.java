/*
 * File: ../../../ptolemy/actor/corba/util/CorbaIndexOutofBoundExceptionHolder.java
 * From: CorbaActor.idl
 * Date: Wed Jul 28 17:18:28 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.util;
public final class CorbaIndexOutofBoundExceptionHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public ptolemy.actor.corba.util.CorbaIndexOutofBoundException value;
    //	constructors 
    public CorbaIndexOutofBoundExceptionHolder() {
	this(null);
    }
    public CorbaIndexOutofBoundExceptionHolder(ptolemy.actor.corba.util.CorbaIndexOutofBoundException __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        ptolemy.actor.corba.util.CorbaIndexOutofBoundExceptionHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = ptolemy.actor.corba.util.CorbaIndexOutofBoundExceptionHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return ptolemy.actor.corba.util.CorbaIndexOutofBoundExceptionHelper.type();
    }
}
