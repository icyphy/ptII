/* Base class for exceptions that report the names of Nameable objects.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
@AcceptedRating Green (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

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
     *  @param obj1 The first object.
     *  @param obj2 The second object.
     *  @param detail The message.
     */
    public KernelException(Nameable obj1, Nameable obj2,
            String detail) {
        String obj1string = _getFullName(obj1);
        String obj2string = _getFullName(obj2);
        String prefix;
        if (!obj1string.equals("")) {
            if (!obj2string.equals("")) {
                prefix = new String(obj1string + " and " + obj2string);
            } else {
                prefix = obj1string;
            }
        } else {
            prefix = obj2string;
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the name of a Nameable object.
     *  If the argument is a null reference, return an empty string.
     *  @param obj An object with a name.
     *  @return The name of the argument.
     */
    protected String _getName(Nameable obj) {
        String name;
        if (obj == null) {
            return "";
        } else {
            name = obj.getName();
            if (name.equals("")) {
                name = new String("<Unnamed Object>");
            }
        }
        return name;
    }

    /** Get the name of a Nameable object.  This method attempts to use
     *  getFullName(), if it is defined, and resorts to getName() if it is
     *  not.  If the argument is a null reference, return an empty string.
     *  @param obj An object with a full name.
     *  @return The full name of the argument.
     */
    protected String _getFullName(Nameable obj) {
        String name;
        if (obj == null) {
            return "";
        } else {
            try {
                name = obj.getFullName();
            } catch (InvalidStateException ex) {
                name = obj.getName();
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
