package ptolemy.actor.ptalon;

/**
 * An Exception related to the scope of variables
 * in the Ptalon interpreter.
 * @author acataldo
 *
 */

public class PtalonScopeException extends Exception {

    /**
     * Generate a Ptalon ScopeException
     * @param message An explanation of the offense
     */
    public PtalonScopeException(String message) {
        super(message);
    }

    /**
     * Generate a Ptalon ScopeException
     * @param message An explanation of the offense.
     * @param cause The cause of the offense.
     */
    public PtalonScopeException(String message, Throwable cause) {
        super(message, cause);
    }

}
