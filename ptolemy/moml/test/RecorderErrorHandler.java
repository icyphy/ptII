/* Handle MoML Parsing Errors

 Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.moml.test;

import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.ErrorHandler;


//////////////////////////////////////////////////////////////////////////
//// RecorderErrorHandler
/**
Record MoML Errors and retrieve them later

@see ptolemy.kernel.util.RecorderListener
@author Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class RecorderErrorHandler implements ErrorHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create an error handler
     */
    public RecorderErrorHandler() {

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Enable or disable skipping of errors.  This method does nothing.
     *  @param enable True to enable skipping, false to disable.
     */
    public void enableErrorSkipping(boolean enable) {
    }

    /** Get the messages recorded so far.
     */
    public String getMessages() {
        return _buffer.toString();
    }

    /** Handle an error by printing a description of the error to
     *  the stream specified in the constructor.
     *  @param element The XML element that triggered the error.
     *  @param context The container object for the element.
     *  @param exception The exception that was thrown.
     *  @return CONTINUE to request skipping this element.
     */
    public int handleError(
            String element,
            NamedObj context,
            Throwable exception) {
        _buffer.append("RecorderErrorHandler: Error encountered in:\n"
                + element
                + "\n"
                + KernelException.stackTraceToString(exception));
        return CONTINUE;
    }

    /** Clear the buffer.
     */
    public void reset() {
        _buffer = new StringBuffer();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private StringBuffer _buffer = new StringBuffer();
}
