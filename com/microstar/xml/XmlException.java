// XmlException.java: Simple base class for AElfred processors.
// NO WARRANTY! See README, and copyright below.
// $Id$
/* Portions of this file are
 Copyright (c) 2002-2003 The Regents of the University of California.
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
 @ProposedRating Red (cxh)
 @AcceptedRating Red (cxh)
 */
package com.microstar.xml;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 Convenience exception class for reporting XML parsing errors.
 <p>This is an exception class that you can use to encapsulate all
 of the information from &AElig;lfred's <code>error</code> callback.
 This is not necessary for routine use of &AElig;lfred, but it
 is used by the optional <code>HandlerBase</code> class.

 <p>Note that the core &AElig;lfred classes do <em>not</em>
 use this exception.

 <p>JDK1.4 supports exception chaining.  We are implementing a version of
 exception chaining here ourselves so that we can use JVMs earlier
 than JDK1.4.

 <p>In this implementation, we have the following differences from
 the JDK1.4 exception chaining implementation:

 <menu>
 <li>In this implementation, the detail message includes the detail
 message from the cause argument.
 <li>In this implementation, we implement a protected _setCause()
 method, but not the public initCause() method that JDK1.4 has
 </menu>

 @author Copyright (c) 1998 by Microstar Software Ltd.
 @author written by David Megginson &lt;dmeggins@microstar.com&gt;
 @author Exception Chaining added by Christopher Hylands
 @version 1.1
 @since Ptolemy II 0.2
 @see XmlHandler#error
 @see HandlerBase
 */
@SuppressWarnings("serial")
public class XmlException extends Exception {
    /** Construct a new XML parsing exception.
     * @param message The error message from the parser.
     * @param systemId The URI of the entity containing the error.
     */
    public XmlException(String message, String systemId) {
        this(message, systemId, -1, -1, null);
    }

    /** Construct a new XML parsing exception.
     * @param message The error message from the parser.
     * @param systemId The URI of the entity containing the error.
     * @param line The line number where the error appeared.
     * @param column The column number where the error appeared.
     */
    public XmlException(String message, String systemId, int line, int column) {
        this(message, systemId, line, column, null);
    }

    /** Construct a new XML parsing exception.
     * @param message The error message from the parser.
     * @param systemId The URI of the entity containing the error.
     * @param line The line number where the error appeared.
     * @param column The column number where the error appeared.
     * @param cause The cause of this exception, if any
     */
    public XmlException(String message, String systemId, int line, int column,
            Throwable cause) {
        _message = message;
        _systemId = systemId;
        _line = line;
        _column = column;
        _cause = cause;
    }

    /** Get the cause of this exception.
     *  @return The cause that was passed in as an argument to the
     *  constructor, or null of no cause was specified.
     */
    @Override
    public Throwable getCause() {
        return _cause;
    }

    /**
     * Get the error message from the parser.
     * @return A string describing the error.
     */
    @Override
    public String getMessage() {
        // Modified by Steve Neuendorffer because the message didn't tell what
        // the location was.
        return _message
                + " in "
                + _systemId
                + (_line == -1 ? "unknown line " : " at line " + _line)
                + (_column == -1 ? " and unknown column " : " and column "
                        + _column)
                        + (_cause == null ? "" : "\nCaused by:\n " + _cause);
    }

    /** Get the URI of the entity containing the error.
     * @return The URI as a string.
     */
    public String getSystemId() {
        return _systemId;
    }

    /** Get the line number containing the error.
     * @return The line number as an integer.
     */
    public int getLine() {
        return _line;
    }

    /** Get the column number containing the error.
     * @return The column number as an integer.
     */
    public int getColumn() {
        return _column;
    }

    /** Print the following to stderr:
     *  this exception, its stack trace and if the cause
     *  exception is known, print the cause exception and the
     *  cause stacktrace.
     */
    @Override
    public void printStackTrace() {
        // Note that chained exceptions are new JDK1.4.
        // We are implement them ourselves here so that we can
        // use JVMs earlier than JDK1.4.  The JDK1.4 Throwable.getCause()
        // documentation states that it is not necessary to overwrite
        // printStackTrace, but this is only the case when we have a JDK1.4
        // JVM.
        printStackTrace(new PrintWriter(System.err));
    }

    /** Print this exception, its stack trace and if the cause
     *  exception is known, print the cause exception and the cause
     *  stacktrace.
     *  @param printStream The PrintStream to write to.
     */
    @Override
    public void printStackTrace(PrintStream printStream) {
        printStackTrace(new PrintWriter(printStream));
    }

    /** Print this exception, its stack trace and if the cause
     *  exception is known, print the cause exception and the
     *  cause stacktrace.
     *  @param printWriter The PrintWriter to write to.
     */
    @Override
    public void printStackTrace(PrintWriter printWriter) {
        super.printStackTrace(printWriter);

        if (_cause != null) {
            printWriter.print("Caused by: ");
            _cause.printStackTrace(printWriter);
        }

        printWriter.flush();
    }

    /** Return the stack trace of the given argument as a String.
     *  This method is useful if we are catching and rethrowing
     *  a throwable that does not take a throwable cause argument.
     *  For example, the XML parser exception does not take
     *  a cause argument, so we call this method instead.
     *  This method should be used instead of
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
    ////                         private variables                 ////
    private String _message;

    // The cause of this exception.
    private Throwable _cause;

    private String _systemId;

    private int _line = -1;

    private int _column = -1;
}
