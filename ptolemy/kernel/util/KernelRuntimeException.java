/* A base class for runtime exceptions.

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

@ProposedRating Green (lmuliadi@eecs.berkeley.edu)
@AcceptedRating Green (bart@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.io.StringWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// KernelRuntimeException
/**
Base class for runtime exceptions.  This class extends the basic
Java RuntimeException with a constructor that can take a Nameable as
an argument.

This exception supports all the constructor forms of KernelException,
but is implemented as a RuntimeException so that it does not have to
be declared.

<p>The cause argument to the constructor is a Throwable that 
caused the exception.  The cause argument is used when code throws
an exception and we want to rethrow the exception but print
the stacktrace where the first exception occurred.  This is called
exception chaining.

<p>JDK1.4 supports exception chaining.  We are implement them
ourselves here so that we can use JVMs earlier than JDK1.4.

@see KernelException
@author Edward A. Lee, Christopher Hylands
@version $Id$ */
public class KernelRuntimeException extends RuntimeException {

    // NOTE: This class has much duplicated code with KernelException,
    // but because it needs to extend java.lang.RuntimeException,
    // so that methods that throw this exception not need to declare
    // that they throw this exception. There seemed to
    // be no way to avoid this.  Should there be an interface defined
    // for the commonality?

    /** Constructs an Exception with only a detail message.
     *  @param detail The message.
     */
    public KernelRuntimeException(String detail) {
        this(null, null, null, detail);
    }

    /** Constructs an Exception with a detail message that includes the
     *  name of the first argument and the second argument string.
     *  @param obj The object.
     *  @param detail The message.
     */
    public KernelRuntimeException(Nameable obj, String detail) {
        this(obj, null, null, detail);
    }

    /** Constructs an Exception with a detail message that includes
     *  the names of the first two arguments plus the third argument
     *  string.  If the cause argument is non-null, then the detail
     *  message of this argument will include the detail message of
     *  the cause argument.  The stack trace of the cause argument is
     *  used when we print the stack trace of this exception.  If one
     *  or more of the parameters are null, then the detail message is
     *  adjusted accordingly.
     *
     *  @param obj1 The first object.
     *  @param obj2 The second object.
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public KernelRuntimeException(Nameable obj1, Nameable obj2,
            Throwable cause, String detail) {
        String obj1string = _getFullName(obj1);
        String obj2string = _getFullName(obj2);
        String prefix;
        if (!obj1string.equals("")) {
            if (!obj2string.equals("")) {
                prefix = obj1string + " and " + obj2string;
            } else {
                prefix = obj1string;
            }
        } else {
            prefix = obj2string;
        }
        _setMessage(prefix);
	_cause = cause;
        if (_cause != null) {
	    if (detail == null) {
		detail = "Caused by " + _cause;
	    } else {
		detail = detail + "Caused by " + _cause;

	    }
	}
        if (detail != null) {
            if (!detail.equals("")) {
                if (!prefix.equals("")) {
                    _setMessage(prefix + ": " + detail); 
                } else {
                    _setMessage(detail);
                }
            }
        }
    }

    /** Construct an exception with a detail message that includes the
     *  names of an enumeration of nameable object plus the argument string.
     *  @param objects The enumeration of Nameable objects
     *  @param detail The message.
     */
    public KernelRuntimeException(Enumeration objects, String detail) {
	this(objects, null, detail);
    }

    /** Construct an exception with a detail message that includes the
     *  names of an enumeration of nameable object, the detail message
     *  of the cause plus the argument string.  If one or more of the
     *  parameters are null, then the detail message is adjusted
     *  accordingly.

     *  @param objects The enumeration of Nameable objects
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public KernelRuntimeException(Enumeration objects, 
				  Throwable cause, String detail) {
        String prefix = "";
        String name;
        while(objects.hasMoreElements()) {
            Object obj = objects.nextElement();
            if (obj instanceof Nameable) {
                name = _getFullName((Nameable)obj);
            } else {
                name = "<Object of class " +
                    (obj.getClass()).getName() + ">";
            }
            prefix += name + ", ";
        }
        prefix = prefix.substring(0, prefix.length()-2);
        prefix += ": ";
        _setMessage(prefix);
	_cause = cause;
        if (_cause != null) {
	    if (detail == null) {
		detail = "Caused by " + _cause;
	    } else {
		detail = detail + "Caused by " + _cause;

	    }
	}
        if (detail != null) {
            if (!detail.equals("")) {
                if (!prefix.equals("")) {
                    _setMessage(prefix + detail);
                } else {
                    _setMessage(detail);
                }
            }
        }
    }

    /** Constructs an exception with a detail message that includes the
     *  names of a list of nameable objects plus the argument string.
     *  @param objects The enumeration of Nameable objects
     *  @param detail The message.
     */
    public KernelRuntimeException(List objects, String detail) {
        this(Collections.enumeration(objects), detail);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the cause of this exception.
     *  @return The cause that was passed in as an argument to the
     *  constructor, or null of no cause was specified.
     */
    public Throwable getCause() {
        return _cause;
    }

    /** Get the detail message. */
    public String getMessage() {
        return _message;
    }

    /** Print this exception, its stack trace and if the cause
     *  exception is known, print the cause exception and the
     *  cause stacktrace.
     */
    public void printStackTrace() {
        // Note that chained exceptions are new JDK1.4.
        // We are implement them ourselves here so that we can
        // use JVMs earlier than JDK1.4.  The JDK1.4 Throwable.getCause()
        // documentation states that it is not necessary to overwrite
        // printStream, but this is only the case when we have a JDK1.4
        // JVM.
        super.printStackTrace();
        if (_cause != null) {
            System.err.print("Caused by: ");
            _cause.printStackTrace();
        }
    }

    /** Print this exception, its stack trace and if the cause
     *  exception is known, print the cause exception and the
     *  cause stacktrace.
     *  @param printStream The PrintStream to write to.
     */
    public void printStackTrace(PrintStream printStream) {
        super.printStackTrace(printStream);
        if (_cause != null) {
            printStream.print("Caused by: ");
            _cause.printStackTrace(printStream);
        }
    }

    /** Print this exception, its stack trace and if the cause
     *  exception is known, print the cause exception and the
     *  cause stacktrace.
     *  @param printWriter The PrintWriter to write to.
     */
    public void printStackTrace(PrintWriter printWriter) {
        super.printStackTrace(printWriter);
        if (_cause != null) {
            printWriter.print("Caused by: ");
            _cause.printStackTrace(printWriter);
        }
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
                name = "<Unnamed Object>";
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
            } catch (KernelRuntimeException ex) {
                name = obj.getName();
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

    /** @serial The detail message. */
    private String _message ;

    // The cause of this exception.
    private Throwable _cause;

}
