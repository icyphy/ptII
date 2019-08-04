/* A subscriber to exceptions that are handled by CatchExceptionAttribute.

 Copyright (c) 2014 The Regents of the University of California.
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

package ptolemy.actor.lib;

//////////////////////////////////////////////////////////////////////////
//// ExceptionSubscriber

/**
 * An ExceptionSubscriber is an entity that is informed of exceptions and the
 * handling policy for exceptions caught by
 * {@link ptolemy.actor.lib.CatchExceptionAttribute}.
 * The ExceptionSubscriber can then take some action, if desired.
 * For example, actors that communicate with outside clients may want to pass
 * along information about the error that has occurred, instead of returning
 * a generic message or a timeout error response.
 *
 * This follows the Command design pattern, where the invoker is
 * {@link ptolemy.actor.lib.CatchExceptionAttribute}, the client is
 * the Ptolemy developer (defines commands by dragging and dropping
 * attributes into the model), and the receiver is a Ptolemy entity
 * (such as an attribute that writes to a file, or an actor such as
 * {@link org.ptolemy.ptango.lib.HttpActor} which sends a retry
 * message upon exception with restart policy.
 *
 * @author Elizabeth Latronico
 * @version $Id: ExceptionSubscriber.java 69467 2014-06-29 14:35:19Z beth@berkeley.edu$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (beth)
 * @Pt.AcceptedRating Red (beth)
 * @see ptolemy.actor.lib.CatchExceptionAttribute
 */

public interface ExceptionSubscriber {
    /** Invoked by an exception handler (e.g. {@link ptolemy.actor.lib.ExceptionManager}) when an
     *  exception occurs.  Some subscribers may need to set up access to
     *  resources (such as opening a file) prior to being notified of an
     *  exception. These could extend AbstractInitalizableAttribute to do so.
     *
     * @param policy  The exception handling policy (for example, restart) of
     * the exception handler; see {@link ptolemy.actor.lib.CatchExceptionAttribute}
     */

    /** Action to execute upon the occurrence of an exception.
     *  Some ExceptionReactions may need to set up and close access to resources
     *  (such as opening and closing a file).  These could extend
     *  AbstractInitalizableAttribute to do so.
     *
     * @param policy The handling policy of the exception manager (for example,
     *  restart); see {@link ptolemy.actor.lib.CatchExceptionAttribute}
     * @param exception The exception
     * @return True if the subscriber successfully processed the information;
     *  false otherwise (for example, an ExceptionEmailer that fails to send
     *  an email)
     */

    public boolean exceptionOccurred(String policy, Throwable exception);

    /** Invoked by an exception handler (e.g. {@link ptolemy.actor.lib.ExceptionManager}) after
     *  an exception has been handled.
     *
     * @param successful True if the exception was successfully handled; false
     * otherwise
     * @param message A status message from the exception handler
     * @return True if the subscriber successfully processed the information;
     *  false otherwise (for example, an ExceptionEmailer that fails to send
     *  an email)
     */
    public boolean exceptionHandled(boolean successful, String message);
}
