/*
@Copyright (c) 2010-2013 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


*/
package ptdb.common.exception;

///////////////////////////////////////////////////////////////////
//// DBModelNotFoundException

/**
 *
 * Exception class that is thrown if the model is not found in the database.
 *
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */

public class DBModelNotFoundException extends Exception {

    /**
     * Construct an instance of DBModelNotFoundException
     * with the given message.
     * @param errorMessage A String message that represents the exception.
     */
    public DBModelNotFoundException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Construct an instance to wrap other exceptions.
     * @param errorMessage A String message that represents the exception.
     * @param cause A Throwable object that represents the cause for the exception.
     */
    public DBModelNotFoundException(String errorMessage, Throwable cause) {
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
