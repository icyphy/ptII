/* A ChangeListener is an interface implemented by objects that are
   interested in being kept informed about mutations.

   Copyright (c) 1998-2003 The Regents of the University of California.
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

   @ProposedRating Green (eal@eecs.berkeley.edu)
   @AcceptedRating Green (neuendor@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// ChangeListener
/**
   A ChangeListener is an interface implemented by objects that are
   interested in being kept informed about changes in a model as they
   are executed. These listeners are informed when each change is successfully
   executed, or when an attempt to execute it results in an exception.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 1.0
   @see ChangeRequest
*/
public interface ChangeListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change request has been successfully executed.
     *  This method is called after a change request
     *  has been executed successfully.
     *  @param change The change that has been executed, or null if
     *   the change was not done via a ChangeRequest.
     */
    public void changeExecuted(ChangeRequest change);

    /** React to a change request has resulted in an exception.
     *  This method is called after a change request was executed,
     *  but during the execution an exception was thrown.
     *  @param change The change that was attempted or null if
     *   the change was not done via a ChangeRequest.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception);
}
