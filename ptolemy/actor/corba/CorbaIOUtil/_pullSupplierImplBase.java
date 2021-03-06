package ptolemy.actor.corba.CorbaIOUtil;

/**
 * ptolemy/actor/corba/CorbaIOUtil/_pullSupplierImplBase.java .
 * Generated by the IDL-to-Java compiler (portable), version "3.1"
 * from CorbaIO.idl
 * Wednesday, April 16, 2003 5:05:14 PM PDT
 */

/* A CORBA compatible interface for a pull supplier.
 */
@SuppressWarnings("serial")
public abstract class _pullSupplierImplBase
        extends org.omg.CORBA.portable.ObjectImpl
        implements ptolemy.actor.corba.CorbaIOUtil.pullSupplier,
        org.omg.CORBA.portable.InvokeHandler {
    // Constructors
    public _pullSupplierImplBase() {
    }

    private static java.util.Hashtable _methods = new java.util.Hashtable();

    static {
        _methods.put("pull", new java.lang.Integer(0));
    }

    @Override
    public org.omg.CORBA.portable.OutputStream _invoke(String $method,
            org.omg.CORBA.portable.InputStream in,
            org.omg.CORBA.portable.ResponseHandler $rh) {
        org.omg.CORBA.portable.OutputStream out = null;
        java.lang.Integer __method = (java.lang.Integer) _methods.get($method);

        if (__method == null) {
            throw new org.omg.CORBA.BAD_OPERATION(0,
                    org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        }

        switch (__method.intValue()) {
        /* this method is intended to be called remotely by a pull consumer
         * to request data from its supplier.
         */
        case 0: // CorbaIOUtil/pullSupplier/pull
        {
            try {
                org.omg.CORBA.Any $result = null;
                $result = this.pull();
                out = $rh.createReply();
                out.write_any($result);
            } catch (ptolemy.actor.corba.CorbaIOUtil.CorbaIllegalActionException $ex) {
                out = $rh.createExceptionReply();
                ptolemy.actor.corba.CorbaIOUtil.CorbaIllegalActionExceptionHelper
                        .write(out, $ex);
            }

            break;
        }

        default:
            throw new org.omg.CORBA.BAD_OPERATION(0,
                    org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        }

        return out;
    } // _invoke

    // Type-specific CORBA::Object operations
    private static String[] __ids = { "IDL:CorbaIOUtil/pullSupplier:1.0" };

    @Override
    public String[] _ids() {
        return __ids.clone();
    }
} // class _pullSupplierImplBase
