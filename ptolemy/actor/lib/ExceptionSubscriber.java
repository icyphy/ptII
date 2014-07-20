/* A subscriber to exceptions that are handled by CatchExceptionAttribute.

 Copyright (c) 2006-2013 The Regents of the University of California.
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
 * An ExceptionSubscriber is informed of exceptions and the planned model action
 * for exceptions that are caught by CatchExceptionAttribute.  
 * The ExceptionSubscriber can then take some action, if desired.  
 * For example, actors that communicate with outside clients may want to pass
 * along information about the arror that has occurred, instead of returning
 * a generic message or a timeout error response. 
 * 
 * @author Elizabeth Latronico
 * @version $Id: ExceptionSubscriber.java 69467 2014-06-29 14:35:19Z beth@berkeley.edu$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (beth)
 * @Pt.AcceptedRating Red (beth)
 * @see CatchExceptionAttribute
 */

public interface ExceptionSubscriber {
    /** Invoked by an exception handler (e.g. CatchExceptionAttribute) when an
     *  exception occurs.
     *   
     * @param policy  The exception handling policy of the exception handler
     */
    public void exceptionOccurred(String policy);
}


