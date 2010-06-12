package ptdb.common.exception;

//////////////////////////////////////////////////////////////////////////
//// DBConnectionException
/**
 * Exception for all the exceptions raised during XML database connection 
 * related operations.
 * 
 * @author Ashwini Bijwe
 * 
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 * 
 *
 */
public class DBConnectionException extends Exception {

    /**
     * Construct an instance of DBConnectionException 
     * with the given message. 
     * @param errorMessage - The exception message.
     */
    public DBConnectionException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Constructor an instance to wrap other exceptions. 
     * @param errorMessage - The exception message.
     * @param cause - The original exception.
     */
    public DBConnectionException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this._cause = cause;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Returns the underlying cause for the exception.
     */
    public Throwable getCause() {
        return this._cause;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Throwable _cause;
}
