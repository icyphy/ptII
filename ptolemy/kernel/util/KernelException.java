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
@AcceptedRating Red (cxh@eecs.berkeley.edu) complete restructuring
*/

package ptolemy.kernel.util;

import java.io.StringWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

//////////////////////////////////////////////////////////////////////////
//// KernelException
/**
Base class for exceptions that report the names of Nameable objects.
This exception should not be thrown directly, since it provides very
little information about what objects were involved when the exception was
thrown.
This exception is not abstract so that we can easily test it.
If any or all of the arguments to the constructor are null, then the
detail message is adjusted accordingly.
Derived classes can provide multiple constructors that take 0, 1 or 2
Nameable references, a Throwable cause and a detail String.

<p>The cause argument to the constructor is a Throwable that
caused the exception.  The cause argument is used when code throws
an exception and we want to rethrow the exception but print
the stacktrace where the first exception occurred.  This is called
exception chaining.

<p>JDK1.4 supports exception chaining.  We are implementing a version of
exception chaining here ourselves here so that we can use JVMs earlier
than JDK1.4.

<p>In this implementation, we have the following differences from
the JDK1.4 exception chaining implementation:
<menu>
<li>In this implementation, the detail message includes the detail
message from the cause argument.
<li>The printStackTrace() methods print the stack of the cause
exception, which should include frames from the primary exception.
In JDK1.4, the stack trace for the primary exception is printed, then
the stack trace for the cause exception.
</menu>

@see KernelRuntimeException
@author John S. Davis, II, Edward A. Lee, Christopher Hylands
@version $Id$
*/
public class KernelException extends Exception {

    /** Constructs an Exception with a no specific detail message */
    public KernelException() {
        // Note: this nullary exception is required.  If it is
        // not present, then the subclasses of this class will not
        // compile.
        this(null, null, null, null);
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
        this(object1, object2, null, detail);
    }

    /** Constructs an Exception with a detail message that includes
     *  the names of the first two arguments plus the third argument
     *  string.  If the cause argument is non-null, then the detail
     *  message of this argument will include the detail message of
     *  the cause argument.  The stack trace of the cause argument is
     *  used when we print the stack trace of this exception.  If one
     *  or more of the parameters are null, then the detail message is
     *  adjusted accordingly.
     *  @param object1 The first object.
     *  @param object2 The second object.
     *  @param cause The cause of this exception.
     *  @param detail The message.
     */
    public KernelException(Nameable object1, Nameable object2,
            Throwable cause, String detail) {
        _cause = cause;
        _setMessage(_generateMessage(object1, object2, _cause, detail));
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

    /** Get the detail message.
     *  @return The error message.
     */
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

    /** Return the stack trace of the given argument as a String.
     *  This method is useful if we are catching and rethrowing
     *  a throwable.  This method should be used instead of
     *  Throwable.printStackTrace(), which prints the stack trace
     *  to stderr, which is likely to be hidden if we are running
     *  a Ptolemy application from anything but a shell console.
     *  @param throwable A throwable.
     *  @return The stack trace of the throwable.
     */
    public static String stackTraceToString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         package friendly methods          ////

    /** Generate a properly formatted detail message.
     *  If one or more of the parameters are null, then the detail message is
     *  adjusted accordingly.
     *
     *  <p>This method is package friendly so that both KernelException
     *  and KernelRuntimeException and any classes derived from those
     *  classes can use it.  KernelRuntimeException must extend
     *  RuntimeException so that the java compiler will allow methods
     *  that throw KernelRuntimeException to not declare that they
     *  throw it, and KernelException cannot extend RuntimeException
     *  for the same reason.
     *
     *  @param object1 The first object.
     *  @param object2 The second object.
     *  @param cause The cause of this exception.
     *  @param detail The message.
     *  @return A properly formatted message
     */
    static String _generateMessage(Nameable object1, Nameable object2,
            Throwable cause, String detail) {
        String object1String = _getFullName(object1);
        String object2String = _getFullName(object2);
        String prefix;
        if (!object1String.equals("")) {
            if (!object2String.equals("")) {
                prefix = object1String + " and " + object2String;
            } else {
                prefix = object1String;
            }
        } else {
            prefix = object2String;
        }
        return _generateMessage(prefix, cause, detail);
    }

    /** Generate a properly formatted detail message.
     *  If one or more of the parameters are null, then the detail message is
     *  adjusted accordingly.
     *
     *  <p>This method is package friendly so that both KernelException
     *  and KernelRuntimeException and any classes derived from those
     *  classes can use it.  KernelRuntimeException must extend
     *  RuntimeException so that the java compiler will allow methods
     *  that throw KernelRuntimeException to not declare that they
     *  throw it, and KernelException cannot extend RuntimeException
     *  for the same reason.
     *
     *  @param prefix The prefix string at the start of the message
     *  that usually contains the names of the objects involved.
     *  @param cause The cause of this exception.
     *  @param detail The message.
     *  @return A properly formatted message
     */
    static String _generateMessage(String prefix,
            Throwable cause, String detail) {
        // We need this method to support the constructors
        // in InvalidStateException that take Enumerations and Lists.

        // Using 'boolean ? if true : if false' is usually frowned
        // upon, but in this case, the alternatives are a very large
        // and complex if/else tree or else the creation of a bunch
        // of temporary strings with a smaller if/else tree.
        return
                // Do we print the prefix?
                ((prefix.equals("")) ?
                        "" : prefix)

                // Do we add a \n?
                + ((!prefix.equals("")
                        && detail != null && !detail.equals("")) ?
                        ":\n" : "")

                // Do we print the detail?
                + ((detail == null || detail.equals("")) ?
                        "" : detail)

                // Do we add a \n?
                + (((!prefix.equals("")
                        || (detail != null && !detail.equals("")))
                        && cause != null) ?
                        "\n" : "")

                // Do we print the cause?
                + ((cause == null) ?
                        "" : ("Caused by:\n " + cause));
    }

    /** Get the name of a Nameable object.
     *  If the argument is a null reference, return an empty string.
     *
     *  <p>This method is package friendly so that both KernelException
     *  and KernelRuntimeException and any classes derived from those
     *  classes can use it.  KernelRuntimeException must extend
     *  RuntimeException so that the java compiler will allow methods
     *  that throw KernelRuntimeException to not declare that they
     *  throw it, and KernelException cannot extend RuntimeException
     *  for the same reason.
     *
     *  @param object An object with a name.
     *  @return The name of the argument.
     */
    static String _getName(Nameable object) {
        if (object == null) {
            return "";
        } else {
            String name;
            name = object.getName();
            if (name.equals("")) {
                name = "<Unnamed Object>";
            }
            return name;
        }
    }

    /** Get the name of a Nameable object.  This method attempts to use
     *  getFullName(), if it is defined, and resorts to getName() if it is
     *  not.  If the argument is a null reference, return an empty string.
     *
     *  <p>This method is package friendly so that both KernelException
     *  and KernelRuntimeException and any classes derived from those
     *  classes can use it.  KernelRuntimeException must extend
     *  RuntimeException so that the java compiler will allow methods
     *  that throw KernelRuntimeException to not declare that they
     *  throw it, and KernelException cannot extend RuntimeException
     *  for the same reason.
     *
     *  @param object An object with a full name.
     *  @return The full name of the argument.
     */
    static String _getFullName(Nameable object) {
        if (object == null) {
            return "";
        } else {
            String name;
            try {
                name = object.getFullName();
            } catch (InvalidStateException ex) {
                name = object.getName();
            }
            return name;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Sets the error message to the specified string.
     *  @param message The message.
     */
    protected void _setMessage(String message) {
        if (message == null) {
            _message = "";
        } else {
            _message = message;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The detail message
    protected String _message ;

    // The cause of this exception.
    protected Throwable _cause;
}
