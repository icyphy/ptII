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
     * Create a new DBConnectionException with the given message.
     * @param message The given message.
     */
    public DBConnectionException(String message) {
        
        super(message);
    
    }

    /**
     * Create an instance to wrap other exceptions. 
     * @param message The exception message.
     * @param cause The original exception.
     */
    public DBConnectionException(String message, Throwable cause) {
        super(message, cause);
        this._cause = cause;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     * Return the underlying cause for the exception.
     * 
     * @return The cause of the exception.
     */
    public Throwable getCause() {
        return this._cause;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private Throwable _cause;
}
