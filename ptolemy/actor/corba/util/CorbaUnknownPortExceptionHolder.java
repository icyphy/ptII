/*
 * File: ../../../ptolemy/actor/corba/util/CorbaUnknownPortExceptionHolder.java
 * From: CorbaActor.idl
 * Date: Tue Jul 27 13:54:19 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.util;
public final class CorbaUnknownPortExceptionHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public ptolemy.actor.corba.util.CorbaUnknownPortException value;
    //	constructors 
    public CorbaUnknownPortExceptionHolder() {
	this(null);
    }
    public CorbaUnknownPortExceptionHolder(ptolemy.actor.corba.util.CorbaUnknownPortException __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return ptolemy.actor.corba.util.CorbaUnknownPortExceptionHelper.type();
    }
}
