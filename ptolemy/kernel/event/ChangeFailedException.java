/* An exception that is thrown when execution of a change request fails.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.kernel.event;

import ptolemy.kernel.util.KernelException;

//////////////////////////////////////////////////////////////////////////
//// ChangeFailedException
/**
An exception that is thrown when execution of a change request fails.
This exception merely constructs a detailed message about the cause
of the exception.

@author Edward A. Lee
@version $Id$
*/
public class ChangeFailedException extends KernelException {

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create an exception with a detailed message constructed from
     *  the message of another exception.
     *  @param request The change request.
     *  @param ex The exception that resulted from executing it.
     */
    public ChangeFailedException(ChangeRequest request, Exception ex) {
        super(request.getOriginator(), null, "Change request failed: "
                + request.getDescription() + "\n"
                + ex.toString());
    }

    /** Create an exception with a detailed message about the change
     *  that failed.
     *  @param request The change request.
     *  @param msg A message explaining the problem with executing the change
     *   request.
     */
    public ChangeFailedException(ChangeRequest request, String msg) {
        super(request.getOriginator(), null, "Change request failed: "
                + request.getDescription() + "\n"
                + msg);
    }
}
