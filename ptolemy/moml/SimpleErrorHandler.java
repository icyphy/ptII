/* Simply handle MoML Parsing Errors by throwing an Exception.

 Copyright (c) 2012-2014 The Regents of the University of California.
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

import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// SimpleErrorHandler

/**
 Simple error handler for the MoMLParser class. This error handler reports
 errors by throwing an exception.

 @see MoMLParser
 @author Christopher Brooks, based on StreamErrorHandler by Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class SimpleErrorHandler implements ErrorHandler {
    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create an error handler that throws an exception.
     */
    public SimpleErrorHandler() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Enable or disable skipping of errors.  This method does nothing.
     *  @param enable True to enable skipping, false to disable.
     */
    @Override
    public void enableErrorSkipping(boolean enable) {
    }

    /** Handle an error by throwing an exception.
     *  @param element The XML element that triggered the error.
     *  @param context The container object for the element.
     *  @param exception The exception that was thrown.
     *  @return Never returns.
     */
    @Override
    public int handleError(String element, NamedObj context, Throwable exception) {
        throw new InternalErrorException(context, exception, "Element "
                + element + " caused an exception to be thrown.");
    }
}
