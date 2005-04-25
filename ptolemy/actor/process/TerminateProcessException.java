/* A notification exception used only to terminate a process.

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
package ptolemy.actor.process;

import ptolemy.kernel.util.Nameable;


//////////////////////////////////////////////////////////////////////////
//// TerminateProcessException

/**
   This exception is thrown to terminate a process. This is only a
   notification exception that a ProcessDirector uses to terminate all the
   processes gracefully. It is not an exception that indicates an error in
   the code or model.
   <p>
   In the process domains (PN and CSP for example), a simulation is
   terminated only when a deadlock is detected. During a deadlock, the
   threads corresponding to actors are normally blocked on a method call to
   the receiver. This exception is normally thrown from these methods, so
   that the threads can return from the call and terminate themselves.
   <p>
   This class is a standalone class and not derived from the Ptolemy
   Runtime exceptions as those exceptions indicate an error in the model,
   while this exception is used for passing of information to the threads.

   @author Neil Smyth, Mudit Goel
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (mudit)
   @Pt.AcceptedRating Yellow (mudit)
*/
public class TerminateProcessException extends RuntimeException {
    // NOTE: This class has much duplicated code with KernelException,
    // but because it needs to be a RuntimeException, there seemed to
    // be no way to avoid this.  Should there be an interface defined
    // for the commonality?

    /** Constructs an Exception with only a detail message.
     *  @param detail The message.
     */
    public TerminateProcessException(String detail) {
        this(null, null, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  name of the first argument and the second argument string.
     *  @param object The object.
     *  @param detail The message.
     */
    public TerminateProcessException(Nameable object, String detail) {
        this(object, null, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  names of the first two arguments plus the third argument string.
     *  @param object1 The first object.
     *  @param object2 The second object.
     *  @param detail The message.
     */
    public TerminateProcessException(Nameable object1, Nameable object2,
            String detail) {
        String object1String = _getFullName(object1);
        String object2String = _getFullName(object2);
        String prefix;

        if (!object1String.equals("")) {
            if (!object2String.equals("")) {
                prefix = new String(object1String + " and " + object2String);
            } else {
                prefix = object1String;
            }
        } else {
            prefix = object2String;
        }

        _setMessage(prefix);

        if (detail != null) {
            if (!detail.equals("")) {
                if (!prefix.equals("")) {
                    _setMessage(new String(prefix + ": " + detail));
                } else {
                    _setMessage(detail);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the detail message. */
    public String getMessage() {
        return _message;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the name of a Nameable object.  This method attempts to use
     *  getFullName(), if it is defined, and resorts to getName() if it is
     *  not.  If the argument is a null reference, return an empty string.
     *  @param object An object with a full name.
     *  @return The full name of the argument.
     */
    protected String _getFullName(Nameable object) {
        String name;

        if (object == null) {
            return "";
        } else {
            try {
                name = object.getFullName();
            } catch (TerminateProcessException ex) {
                name = object.getName();
            }
        }

        return name;
    }

    /** Get the name of a Nameable object.
     *  If the argument is a null reference, return an empty string.
     *  @param object An object with a name.
     *  @return The name of the argument.
     */
    protected String _getName(Nameable object) {
        String name;

        if (object == null) {
            return "";
        } else {
            name = object.getName();

            if (name.equals("")) {
                name = new String("<Unnamed Object>");
            }
        }

        return name;
    }

    /** Sets the error message to the specified string.
     *  @param message The message.
     */
    protected void _setMessage(String message) {
        _message = message;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The detail message.
    private String _message;
}
