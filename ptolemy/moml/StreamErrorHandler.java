/* Handle MoML Parsing Errors.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.moml;

import java.io.OutputStream;
import java.io.PrintStream;

import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// StreamErrorHandler

/**
 Basic error handler for the MoMLParser class. This error handler reports
 errors to a stream or to standard error, and requests that parsing continue.

 @see MoMLParser
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class StreamErrorHandler implements ErrorHandler {
    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create an error handler that sends messages to the standard error.
     */
    public StreamErrorHandler() {
        _output = System.err;
    }

    /** Create an error handler that sends messages to the specified stream.
     *  @param out The OutputStream
     */
    public StreamErrorHandler(OutputStream out) {
        _output = new PrintStream(out);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Enable or disable skipping of errors.  This method does nothing.
     *  @param enable True to enable skipping, false to disable.
     */
    @Override
    public void enableErrorSkipping(boolean enable) {
    }

    /** Handle an error by printing a description of the error to
     *  the stream specified in the constructor.
     *  @param element The XML element that triggered the error.
     *  @param context The container object for the element.
     *  @param exception The exception that was thrown.
     *  @return CONTINUE to request skipping this element.
     */
    @Override
    public int handleError(String element, NamedObj context, Throwable exception) {
        _output.println("Error encountered in:\n" + element + "\n"
                + KernelException.stackTraceToString(exception));
        return CONTINUE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private PrintStream _output;
}
