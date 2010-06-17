package ptdb.common.exception;

/**
 * Exception class for thrown when creating a model that already exist in the 
 * database.
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */

///////////////////////////////////////////////////////////////
//// ModelAlreadyExistException


public class ModelAlreadyExistException extends Exception {

    /**
     * Construct an instance of ModelAlreadyExistException
     * with the given message.
     * @param errorMessage A String message that represents the exception.
     */
    public ModelAlreadyExistException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Construct an instance to wrap other exceptions.
     * @param errorMessage A String message that represents the exception.
     * @param cause A Throwable object that represents the cause for the exception.
     */
    public ModelAlreadyExistException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this._cause = cause;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the underlying cause for the exception.
     */
    public Throwable getCause() {
        return this._cause;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Throwable _cause;
}
