/*
 * File: ../../../../../PTOLEMY/DOMAINS/CT/DEMO/CORBA/UTIL/CORBAACTORHELPER.JAVA
 * From: CORBAACTOR.IDL
 * Date: Thu Jul 29 14:22:20 1999
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package ptolemy.domains.ct.demo.Corba.util;
public class CorbaActorHelper {
     // It is useless to have instances of this class
     private CorbaActorHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, ptolemy.domains.ct.demo.Corba.util.CorbaActor that) {
        out.write_Object(that);
    }
    public static ptolemy.domains.ct.demo.Corba.util.CorbaActor read(org.omg.CORBA.portable.InputStream in) {
        return ptolemy.domains.ct.demo.Corba.util.CorbaActorHelper.narrow(in.read_Object());
    }
   public static ptolemy.domains.ct.demo.Corba.util.CorbaActor extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, ptolemy.domains.ct.demo.Corba.util.CorbaActor that) {
     org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
     write(out, that);
     a.read_value(out.create_input_stream(), type());
   }
   private static org.omg.CORBA.TypeCode _tc;
   synchronized public static org.omg.CORBA.TypeCode type() {
          if (_tc == null)
             _tc = org.omg.CORBA.ORB.init().create_interface_tc(id(), "CorbaActor");
      return _tc;
   }
   public static String id() {
       return "IDL:util/CorbaActor:1.0";
   }
   public static ptolemy.domains.ct.demo.Corba.util.CorbaActor narrow(org.omg.CORBA.Object that)
	    throws org.omg.CORBA.BAD_PARAM {
        if (that == null)
            return null;
        if (that instanceof ptolemy.domains.ct.demo.Corba.util.CorbaActor)
            return (ptolemy.domains.ct.demo.Corba.util.CorbaActor) that;
	if (!that._is_a(id())) {
	    throw new org.omg.CORBA.BAD_PARAM();
	}
        org.omg.CORBA.portable.Delegate dup = ((org.omg.CORBA.portable.ObjectImpl)that)._get_delegate();
        ptolemy.domains.ct.demo.Corba.util.CorbaActor result = new ptolemy.domains.ct.demo.Corba.util._CorbaActorStub(dup);
        return result;
   }
}
