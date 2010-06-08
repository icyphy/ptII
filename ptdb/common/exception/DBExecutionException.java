package ptdb.common.exception;

//////////////////////////////////////////////////////////////////////////
//// DBConnectionParameters

/**
 * Exception class for all the exceptions raised during XML 
 * database connection related operations.
 * 
 * @author Ashwini Bijwe
 * 
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */

public class DBExecutionException extends Exception {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Construct an instance of DBConnectionException 
     * with the given message 
     * @param errorMessage - exception message
     */
    public DBExecutionException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Construct and instance to wrap other exceptions 
     * @param errorMessage - exception message
     * @param cause
     */
    public DBExecutionException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this._cause = cause;
    }

    /**
     * Return the underlying cause for the exception
     */
    public Throwable getCause() {
        return this._cause;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Throwable _cause;
}
