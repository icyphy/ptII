/* An exception that is not reported to the user.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@AcceptedRating Green (janneck@eecs.berkeley.edu)
*/

package ptolemy.util;

//////////////////////////////////////////////////////////////////////////
//// CancelException
/**
An exception that is not reported to the user.  This exception can be used
to cancel an ongoing operation.  It works together with MessageHandler,
where it might be thrown when the user clicks "cancel" in a dialog box.
Throwing an exception allows the execution environment to unwind the stack,
cancelling operations that may only lead to an endless stream of warnings
or errors.  This class is really just a marker, and is intended to not be
reported to the user.

@see MessageHandler
@author Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/

public class CancelException extends Exception {

    /** Construct an exception with a default message.
     */
    public CancelException() {
        super("Operation canceled by the user");
    }

    /** Construct an exception with the specified message.
     */
    public CancelException(String message) {
        super(message);
    }
}
