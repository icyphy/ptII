/* An exception to be thrown in model transformation.

@Copyright (c) 2007-2009 The Regents of the University of California.
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

package ptolemy.actor.gt;

import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Nameable;

/**
 An exception to be thrown in model transformation.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 @see GraphTransformer
 */
@SuppressWarnings("serial")
public class TransformationException extends KernelException {

    /** Construct an exception with a no specific detail message.
     */
    public TransformationException() {
    }

    /** Construct an exception with a detail message that includes the
     *  names of the first two arguments plus the third argument
     *  string.  If one or more of the parameters are null, then the
     *  message of the exception is adjusted accordingly.
     *
     *  @param object1 The first object.
     *  @param object2 The second object.
     *  @param detail The message.
     */
    public TransformationException(Nameable object1, Nameable object2,
            String detail) {
        super(object1, object2, detail);
    }

    /** Construct an exception with a detail message that includes the
     *  names of the first two arguments plus the third argument
     *  string.  If the cause argument is non-null, then the message
     *  of this exception will include the message of the cause
     *  argument.  The stack trace of the cause argument is used when
     *  we print the stack trace of this exception.  If one or more of
     *  the parameters are null, then the message of the exception is
     *  adjusted accordingly.
     *
     *  @param object1 The first object.
     *  @param object2 The second object.
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public TransformationException(Nameable object1, Nameable object2,
            Throwable cause, String detail) {
        super(object1, object2, cause, detail);
    }

    /** Construct an exception with a detail message.
     *
     *  @param detail The message.
     */
    public TransformationException(String detail) {
        this(null, null, detail);
    }

    /** Construct an exception with a detail message.  The stack trace of the
     *  cause argument is used when we print the stack trace of this exception.
     *
     *  @param detail The message.
     *  @param cause The cause of this exception.
     */
    public TransformationException(String detail, Throwable cause) {
        this(null, null, cause, detail);
    }

}
