package ptolemy.caltrop.ddi;

/**
 * A general-purpose exception used in the <tt>ddi</tt> package, used to indicate an error during domain dependent
 * interpretation.
 *
 * @author Christopher Chang <cbc@eecs.berkeley.edu>
 */
public class DDIException extends RuntimeException {

    /**
     * Create a <tt>DDIException</tt>.
     */
    public DDIException() {}

    /**
     * Create a <tt>DDIException</tt> with an error message.
     * @param msg The error message.
     */
    public DDIException(String msg) {
        super(msg);
    }

    /**
     * Create a <tt>DDIException</tt> with an error message and a cause.
     * @param msg The error message.
     * @param cause The cause.
     */
    public DDIException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a <tt>DDIException</tt> with a cause.
     * @param cause The cause.
     */
    public DDIException(Throwable cause) {
        super(cause);
    }
}
