/* Some object or set of objects has a state that in theory is not permitted.

Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.kernel.util;


//////////////////////////////////////////////////////////////////////////
//// InternalErrorException

/**
   This exception should be thrown if an unexpected error is encountered
   other than one for which InvalidStateException would be appropriate.
   Our design should make it impossible for this exception to ever occur,
   so occurrence is a bug. This exception is a RuntimeException
   so that it does not have to be declared.

   @author Edward A. Lee, Christopher Hylands
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (cxh)
   @Pt.AcceptedRating Green (cxh)
*/
public class InternalErrorException extends KernelRuntimeException {
    /** Construct an exception with a detail message.
     *  @param detail The message.
     */
    public InternalErrorException(String detail) {
        super(detail);
    }

    /** Construct an exception with only a cause.
     *  If the cause argument is non-null, then the detail
     *  message of this argument will include the detail message of
     *  the cause argument.  The stack trace of the cause argument is
     *  used when we print the stack trace of this exception.
     *
     *  <p>This constructor is commonly used when we want to
     *  catch an exception and rethrow it as a RuntimeException
     *  so that the method where the exception is thrown
     *  need not declare that this method throws the initial exception.
     *  @param cause The cause of this exception.
     */
    public InternalErrorException(Throwable cause) {
        super(null, null, cause, null);
    }

    /** Construct an exception with a detail message that includes
     *  the names of the first argument plus the third argument
     *  string.  If the cause argument is non-null, then the detail
     *  message of this argument will include the detail message of
     *  the cause argument.  The stack trace of the cause argument is
     *  used when we print the stack trace of this exception.  If one
     *  or more of the parameters are null, then the detail message is
     *  adjusted accordingly.
     *
     *  @param object The object associated with this exception.
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public InternalErrorException(Nameable object, Throwable cause,
        String detail) {
        super(object, null, cause, detail);
    }
}
