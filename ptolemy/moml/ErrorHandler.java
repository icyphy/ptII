/* Handle a MoML Parsing Error

 Copyright (c) 2000-2005 The Regents of the University of California.
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

import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// ErrorHandler

/**
 Interface for error handlers for the MoMLParser class.

 @see MoMLParser
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public interface ErrorHandler {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Enable or disable skipping of errors.
     *  If this method is called with a true argument, then an implementation
     *  of this interface may ignore subsequent errors by returning
     *  CONTINUE in handleError() without reporting the error.
     *  If it is called with a false argument, then the implementation
     *  is expected to report all subsequent errors (but it is not
     *  required to do so).
     *  <p>
     *  This method is intended to be used when an operation may trigger
     *  a large number of errors, and the user interface wishes to offer
     *  the user the option of ignoring them.  This method should be
     *  called with a true argument before the operation begins, and
     *  then called with a false argument after the operation ends.
     *  @param enable True to enable skipping, false to disable.
     */
    public void enableErrorSkipping(boolean enable);

    /** Handle an error.
     *  @param element The XML element that triggered the error.
     *  @param context The container object for the element.
     *  @param exception The exception that was thrown.
     *  @return CONTINUE to skip this element, CANCEL to abort processing
     *   of the XML, or RETHROW to request that the exception be rethrown.
     */
    public int handleError(String element, NamedObj context, Throwable exception);

    ///////////////////////////////////////////////////////////////////
    ////                         public members                    ////

    /** Indicator to skip this element and continue parsing XML. */
    public static final int CONTINUE = 0;

    /** Indicator to cancel parsing XML. */
    public static final int CANCEL = 1;

    /** Indicator to request that the exception be rethrown */
    public static final int RETHROW = 3;
}
