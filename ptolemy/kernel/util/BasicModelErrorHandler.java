/* Handle a model error by throwing an exception.

Copyright (c) 2000-2003 The Regents of the University of California.
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
@AcceptedRating Green (hyzheng@eecs.berkeley.edu)

*/

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// BasicModelErrorHandler
/**
   Default model error handler.  A model error is an exception that is
   passed up the Ptolemy II hierarchy for handling until a container with
   a registered error handler is found.  If there is no registered error
   handler, then the error is ignored.  It is like throwing an exception, except
   that instead of unraveling the calling stack, it travels up the Ptolemy II
   hierarchy.  This class handles the error by simply throwing the
   exception that has been passed to it.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 2.1
*/
public class BasicModelErrorHandler implements ModelErrorHandler {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle a model error by throwing the specified exception.
     *  @param context The object in which the error occurred.
     *  @param exception An exception that represents the error.
     *  @return Never returns.
     *  @exception IllegalActionException The exception passed
     *   as an argument is always thrown.
     */
    public boolean handleModelError(
            NamedObj context,
            IllegalActionException exception)
            throws IllegalActionException {
        throw exception;
    }
}
