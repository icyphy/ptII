package ptolemy.caltrop;

/**
 * An exception used to indicate an IO error during interpretation of a CAL actor in Ptolemy.
 * This can occur during
 * the getting/putting of a {@link ptolemy.data.Token caltrop.data.Token} on a channel.
 *
 * @author Jörn W. Janneck <janneck@eecs.berkeley.edu>
 */
public class CalIOException extends RuntimeException {

    /**
     * Create a <tt>CalIOException()</tt>.
     */
    public CalIOException() {}

    /**
     * Create a <tt>CalIOException</tt> with a message.
     * @param msg The message.
     */
    public CalIOException(String msg) {
        super(msg);
    }

    /**
     * Create a <tt>CalIOException</tt> with a message and a cause.
     * @param msg The message.
     * @param cause The cause.
     */
    public CalIOException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Create a <tt>CalIOException</tt> with a cause.
     * @param cause The cause.
     */
    public CalIOException(Throwable cause) {
        super(cause);
    }
}
