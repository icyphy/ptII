/*
 * File: ../../..//ptolemy/actor/corba/CorbaActor/CorbaActorHelper.java
 * From: CorbaActor.idl
 * Date: Mon Jul 26 23:21:30 1999
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package ptolemy.actor.corba.CorbaActor;
public class CorbaActorHelper {
     // It is useless to have instances of this class
     private CorbaActorHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, ptolemy.actor.corba.CorbaActor.CorbaActor that) {
        out.write_Object(that);
    }
    public static ptolemy.actor.corba.CorbaActor.CorbaActor read(org.omg.CORBA.portable.InputStream in) {
        return ptolemy.actor.corba.CorbaActor.CorbaActorHelper.narrow(in.read_Object());
    }
   public static ptolemy.actor.corba.CorbaActor.CorbaActor extract(org.omg.CORBA.Any a) {
     org.omg.CORBA.portable.InputStream in = a.create_input_stream();
     return read(in);
   }
   public static void insert(org.omg.CORBA.Any a, ptolemy.actor.corba.CorbaActor.CorbaActor that) {
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
       return "IDL:CorbaActor/CorbaActor:1.0";
   }
   public static ptolemy.actor.corba.CorbaActor.CorbaActor narrow(org.omg.CORBA.Object that)
	    throws org.omg.CORBA.BAD_PARAM {
        if (that == null)
            return null;
        if (that instanceof ptolemy.actor.corba.CorbaActor.CorbaActor)
            return (ptolemy.actor.corba.CorbaActor.CorbaActor) that;
	if (!that._is_a(id())) {
	    throw new org.omg.CORBA.BAD_PARAM();
	}
        org.omg.CORBA.portable.Delegate dup = ((org.omg.CORBA.portable.ObjectImpl)that)._get_delegate();
        ptolemy.actor.corba.CorbaActor.CorbaActor result = new ptolemy.actor.corba.CorbaActor._CorbaActorStub(dup);
        return result;
   }
}
