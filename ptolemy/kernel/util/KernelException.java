/* Base class for exceptions that report the names of Nameable objects.

 Copyright (c) 1997-2001 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu) stackTraceToString()
*/

package ptolemy.kernel.util;

import java.io.StringWriter;
import java.io.PrintWriter;

//////////////////////////////////////////////////////////////////////////
//// KernelException
/**
Base class for exceptions that report the names of Nameable objects.
This exception should not be thrown directly, since it provides very
little information about the cause of the exception.
This exception is not abstract so that we can easily test it.
If any or all of the arguments to the constructor are null, then the
detail message is adjusted accordingly.
Derived classes can provide multiple constructors that take 0, 1 or 2
Nameable references plus a detail String.

@author John S. Davis, II, Edward A. Lee
@version $Id$
*/
public class KernelException extends Exception {

    /** Constructs an Exception with a no specific detail message */
    public KernelException() {
        // Note: this nullary exception is required.  If it is
        // not present, then the subclasses of this class will not
        // compile.
        this(null, null, null);
    }

    /** Constructs an Exception with a detail message that includes the
     *  names of the first two arguments plus the third argument string.
     *  If one or more of the parameters are null, then the detail
     *  message is adjusted accordingly.
     *  @param object1 The first object.
     *  @param object2 The second object.
     *  @param detail The message.
     */
    public KernelException(Nameable object1, Nameable object2,
            String detail) {
        String object1string = _getFullName(object1);
        String object2string = _getFullName(object2);
        String prefix;
        if (!object1string.equals("")) {
            if (!object2string.equals("")) {
                prefix = new String(object1string + " and " + object2string);
            } else {
                prefix = object1string;
            }
        } else {
            prefix = object2string;
        }
        _setMessage(prefix);
        if (detail != null) {
            if (!detail.equals("")) {
                if (!prefix.equals("")) {
                    _setMessage(new String(prefix + ":\n" + detail));
                } else {
                    _setMessage(detail);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the detail message.
     *  @return The error message.
     */
    public String getMessage() {
        return _message;
    }

    /** Return the stack trace as a String.
     *  This method is useful if we are catching and rethrowing
     *  an throwable.  This method should be used instead of
     *  Throwable.printStackTrace(), which prints the stack trace
     *  to stderr, which is likely to be hidden if we are running
     *  a Ptolemy application from anything but a shell console.
     *  @param throwable An throwable.
     *  @return The stack trace of the throwable.
     */
    public static String stackTraceToString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
            } catch (InvalidStateException ex) {
                name = object.getName();
            }
        }
        return name;
    }

    /** Sets the error message to the specified string.
     *  @param msg The message.
     */
    protected void _setMessage(String msg) {
        if (msg == null) {
            _message = "";
        } else {
            _message = msg;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial The detail message. */
    private String _message ;
}
