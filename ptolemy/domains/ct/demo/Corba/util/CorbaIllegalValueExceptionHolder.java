/*
 * File: ../../../../../PTOLEMY/DOMAINS/CT/DEMO/CORBA/UTIL/CORBAILLEGALVALUEEXCEPTIONHOLDER.JAVA
 * From: CORBAACTOR.IDL
 * Date: Thu Jul 29 14:22:20 1999
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package ptolemy.domains.ct.demo.Corba.util;
public final class CorbaIllegalValueExceptionHolder
     implements org.omg.CORBA.portable.Streamable{
    //	instance variable 
    public ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueException value;
    //	constructors 
    public CorbaIllegalValueExceptionHolder() {
	this(null);
    }
    public CorbaIllegalValueExceptionHolder(ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueException __arg) {
	value = __arg;
    }

    public void _write(org.omg.CORBA.portable.OutputStream out) {
        ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueExceptionHelper.write(out, value);
    }

    public void _read(org.omg.CORBA.portable.InputStream in) {
        value = ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueExceptionHelper.read(in);
    }

    public org.omg.CORBA.TypeCode _type() {
        return ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueExceptionHelper.type();
    }
}
