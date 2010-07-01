/*
 * 
 */
package ptdb.common.exception;


///////////////////////////////////////////////////////////////
//// XMLDBModelParsingException

/**
 * Exception for document or MOML parsing errors. 
 * 
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class XMLDBModelParsingException extends Exception {

///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Construct an instance of XMLDBModelParsingException
     * with the given message.
     * @param errorMessage Exception message.
     */
    public XMLDBModelParsingException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Construct an instance to wrap other exceptions.
     * @param errorMessage The exception message.
     * @param cause The underlying cause for the exception.
     */
    public XMLDBModelParsingException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this._cause = cause;
    }

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
