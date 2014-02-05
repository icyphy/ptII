/* Exception for graph invalid state exceptions

 Copyright (c) 2008-2009 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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
package ptolemy.graph;

import ptolemy.kernel.util.InvalidStateException;

//////////////////////////////////////////////////////////////////////////
//// GraphInvalidStateException

/**
 Exception for graph invalid state action errors.  This exception is
 thrown when an object has a state that is not permitted.

 This is a RuntimeException and thus need not be declared by a method.

 @author Mingyung Ko
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (myko)
 @Pt.AcceptedRating Red (ssb)
 */
@SuppressWarnings("serial")
public class GraphInvalidStateException extends InvalidStateException {

    // This class extends IllegalActionException so that we have
    // the dependency from graph to kernel.util in as few places
    // as possible.  However, we must extend InvaldiStateException
    // because that is the exception that is caught in the UI,
    // which indicates which actor has a problem.  A better design
    // would be to have base class exceptions that do not
    // refer to NamedObj in a separate package and then have
    // kernel.util exceptions extend those exceptions.

    /**
     *  Construct an exception with a detail message.  This exception
     *  is thrown when an object has a state that is not permitted.
     *  @param message Detailed description of the error.
     */
    public GraphInvalidStateException(String message) {
        super(message);
    }

    /** Construct an exception with a detail message that includes the
     *  name of the first argument, the cause and the third argument string.
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public GraphInvalidStateException(Throwable cause, String detail) {
        super(null, null, cause, detail);
    }
}
