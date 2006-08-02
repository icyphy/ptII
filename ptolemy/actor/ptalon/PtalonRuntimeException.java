package ptolemy.actor.ptalon;

/**
 * An Exception that occurs while trying to populate the PtalonActor
 * in the Ptalon interpreter.
 * @author acataldo
 *
 */
public class PtalonRuntimeException extends Exception {

    /**
     * Create a PtalonRuntimeException
     * @param message An explanation of the offense.
     */
    public PtalonRuntimeException(String message) {
        super(message);
    }
    
    /**
     * Create a PtalonRuntimeException
     * @param message An explanation of the offense.
     * @param cause The cause of the offense.
     */
    public PtalonRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
