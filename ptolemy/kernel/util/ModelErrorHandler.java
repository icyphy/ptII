/* Handle a model error.

 Copyright (c) 2000-2002 The Regents of the University of California.
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

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// ModelErrorHandler
/**
Interface for model error handlers.  A model error is an error that is
passed up the Ptolemy II hierarchy for handling until a container with
a registered error handler is found.  If there is no registered error
handler, then the error is ignored.  It is like an exception, except
that instead of unraveling the calling stack, it unravels the Ptolemy II
hierarchy.

@author Edward A. Lee
@version $Id$
*/
public interface ModelErrorHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle a model error.
     *  @param context The object in which the error occurred.
     *  @param exception An exception that represents the error.
     *  @return True if the error has been handled, or false to decline
     *   to handle it.
     *  @exception IllegalActionException If the handler handles the
     *   error by throwing an exception.
     */
    public boolean handleError(
            NamedObj context,
            IllegalActionException exception)
            throws IllegalActionException;
}
