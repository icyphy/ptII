/*
 Copyright (c) 1998-2005 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package diva.util;

import java.io.PrintStream;

/**
 * This is an abstract base class for objects that need to log
 * error or warning messages. It is useful for things like parsers
 * and printers -- anything that need to perform a complex operation
 * but might encounter errors that need to be displayed to the
 * user.
 *
 * @author John Reekie
 * @version $Id$
 */
public class LoggableOp {
    /** The output stream for error messages. By default, this
     * is System.err.
     */
    private PrintStream _err = System.err;

    /* If true, then print extra information in the error output.
     * This variable is protected so that subclasses can check it
     * easily.
     */
    protected boolean _verbose = false;

    /** The number of warnings generated in the most recent parse.
     */
    private int _warnCount;

    /** The number of errors generated in the most recent parse.
     */
    private int _errCount;

    /** The indenting level
     */
    private int _indent = 0;

    /** The indent tab string
     */
    private String _tab = "    ";

    /** Get the number of errors generated during the most
     * recent operation.
     */
    public int getErrorCount() {
        return _errCount;
    }

    /** Get the stream to which errors are printed.
     */
    public PrintStream getErrorStream() {
        return _err;
    }

    /** Get the number of warnings generated during the most
     * recent parse.
     */
    public int getWarningCount() {
        return _warnCount;
    }

    /* Get the current line number. Subclasses should override
     * if they know about line numbers. The default implementation
     * returns -1, which means that line numbers are not understood.
     */
    public int getLineNumber() {
        return -1;
    }

    /** Print an error message to the error stream. This message
     * includes the line number, and info message.
     */
    public void logError(String msg) {
        logError("ERROR", msg, null);
    }

    /** Print an error message to the error stream. This prints
     * the error id, line number, and info message.
     */
    public void logError(String id, String msg) {
        logError(id, msg, null);
    }

    /** Print an error message to the error stream. This prints
     * the error id, line number, info message. On a second line,
     * it prints the detail information.
     */
    public void logError(String id, String msg, String detail) {
        int linenum = getLineNumber();
        _err.print(id + ":");

        if (linenum >= 0) {
            _err.print(linenum + ":");
        }

        _err.println(" " + msg);

        if (detail != null) {
            _err.println("                " + detail + "\n");
        }

        _errCount++;
    }

    /** If in verbose mode, print an info message to the error stream,
     * otherwise do nothing. This message includes the line number,
     * and info message. The output is indented by the current
     * indentation amount, allowing recursive operations to be printed
     * more clearly.
     */
    public void logInfo(String msg) {
        logInfo(null, msg);
    }

    /** If in verbose mode, print an info message to the error stream,
     * otherwise do nothing. This prints the info id, line number, and
     * info message. The output is indented by the current indentation
     * amount, allowing recursive operations to be printed more
     * clearly.
     */
    public void logInfo(String id, String msg) {
        for (int i = _indent; i > 0; i--) {
            _err.print(_tab);
        }

        if (id != null) {
            _err.print(id + ":");
        }

        int linenum = getLineNumber();

        if (linenum >= 0) {
            _err.print(linenum + ":");
        }

        _err.println(" " + msg);
    }

    /** Increment the indentation counter.
     */
    public void indent() {
        _indent++;
    }

    /** Test if we are in verbose mode
     */
    public final boolean isVerbose() {
        return _verbose;
    }

    /** Reset the error counters.
     */
    public void reset() {
        _indent = 0;
        _errCount = 0;
        _warnCount = 0;
    }

    /** Set the stream to which errors are printed. The default
     * error stream is System.err.
     */
    public void setErrorStream(PrintStream err) {
        _err = err;
    }

    /** Set the verbose mode flag. In verbose mode, calls to
     * the logInfo() method write to the error output.
     */
    public void setVerbose(boolean v) {
        _verbose = v;
    }

    /** Decrement the indentation counter.
     */
    public void unindent() {
        _indent--;
    }

    /** Print an warning message to the error stream. This message
     * includes the line number, and info message.
     */
    public void logWarning(String msg) {
        logWarning("Warning", msg, null);
    }

    /** Print an warning message to the error stream. This prints
     * the warning id, line number, and info message.
     */
    public void logWarning(String id, String msg) {
        logWarning(id, msg, null);
    }

    /** Print an warning message to the error stream. This prints
     * the warning id, line number, info message. On a second line,
     * it prints the detail information.
     */
    public void logWarning(String id, String msg, String detail) {
        int linenum = getLineNumber();
        _err.print(id + ":");

        if (linenum >= 0) {
            _err.print(linenum + ":");
        }

        _err.println(" " + msg);

        if (detail != null) {
            _err.println("                " + detail + "\n");
        }

        _warnCount++;
    }
}
