/* Exception thrown on an attempt to perform an action that would result in an
   inconsistent or contradictory data structure if it were allowed to
   complete.

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
//// IllegalActionException

/**
   Thrown on an attempt to perform an action that would result in an
   inconsistent or contradictory data structure if it were allowed to
   complete.
   <p>Examples include:
   <menu>
   <li> An attempt to remove a port from an entity when the
   port does not belong to the entity.
   <li> An attempt to add an item with no name to a named list.
   </menu>

   @author Edward A. Lee, Christopher Hylands
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (cxh)
   @Pt.AcceptedRating Green (cxh)
*/
public class IllegalActionException extends KernelException {
    /** Construct an exception with a detail message.
     *  @param detail The message.
     */
    public IllegalActionException(String detail) {
        this(null, null, null, detail);
    }

    /** Construct an exception with a detail message that is only the
     *  name of the argument.
     *  @param object The object.
     */
    public IllegalActionException(Nameable object) {
        this(null, object, null, null);
    }

    /** Construct an exception with a detail message that includes the
     *  name of the first argument.
     *  @param object The object.
     *  @param detail The message.
     */
    public IllegalActionException(Nameable object, String detail) {
        this(object, null, null, detail);
    }

    /** Construct an exception with a detail message that includes the
     *  name of the first argument.
     *  @param object The object.
     *  @param cause The cause of this exception, or null if the cause
     *  is not known or nonexistent.
     *  @param detail The message.
     */
    public IllegalActionException(Nameable object, Throwable cause,
            String detail) {
        this(object, null, cause, detail);
    }

    /** Construct an exception with a detail message that consists of
     *  only the names of the object1 and object2 arguments.  If one
     *  or more of the parameters are null, then the message of the
     *  exception is adjusted accordingly.
     *  @param object1 The first object.
     *  @param object2 The second object.
     */
    public IllegalActionException(Nameable object1, Nameable object2) {
        this(object1, object2, null, null);
    }

    /** Construct an exception with a detail message that includes the
     *  names of the first two arguments plus the third argument
     *  string.  If one or more of the parameters are null, then the
     *  message of the exception is adjusted accordingly.
     *  @param object1 The first object.
     *  @param object2 The second object.
     *  @param detail The message.
     */
    public IllegalActionException(Nameable object1, Nameable object2,
            String detail) {
        this(object1, object2, null, detail);
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
     *  @param cause The cause of this exception, or null if the cause
     *  is not known or nonexistent.
     *  @param detail The message.
     */
    public IllegalActionException(Nameable object1, Nameable object2,
            Throwable cause, String detail) {
        super(object1, object2, cause, detail);
    }
}
