/*
 * File: ../../../../../PTOLEMY/DOMAINS/CT/DEMO/CORBA/UTIL/CORBAILLEGALACTIONEXCEPTIONHOLDER.JAVA
 * From: CORBAACTOR.IDL
 * Date: Thu Jul 29 14:22:20 1999
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package ptolemy.domains.ct.demo.Corba.util;
public final class CorbaIllegalActionExceptionHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException value;
    //	constructors 
    public CorbaIllegalActionExceptionHolder() {
	this(null);
    }
    public CorbaIllegalActionExceptionHolder(ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper.type();
    }
}
