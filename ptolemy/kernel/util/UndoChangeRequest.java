/* A request to undo.

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
@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// 
/**
A change request to undo. When executed, this change request will
identify the undo stack associated with the specified context,
and it will execute the top undo action on that stack, if there is
one.
<p>
See MoMLChangeRequest for subtleties regarding the use of MoML change
requests. In particular, the choice of context is important, and should
be that returned by the getDeferredToContext() static method of
MoMLChangeRequest.
<p>
@author Edward A. Lee and Neil Smyth
@version $Id$
*/
public class UndoChangeRequest extends ChangeRequest {

    /** Construct a change request to be executed in the specified context.
     *  The undo stack associated with the specified context will be used.
     *  That stack is the one returned by UndoStackAttribute.getUndoInfo().
     *  @see UndoStackAttribute
     *  @param originator The originator of the change request.
     *  @param context The context in which to execute the MoML.
     */
    public UndoChangeRequest(Object originator, NamedObj context) {
        super(originator, "Request to undo.");
        _context = context;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the change by invoking undo on the undo stack of
     *  the context specified in the constructor.
     *  @exception Exception If an exception is thrown
     *   while evaluating the request.
     */
    protected void _execute() throws Exception {
        // Check to see whether there is a context...
        if (_context == null) {
            throw new InternalErrorException("Context is unexpectedly null.");
        }
        UndoStackAttribute undoStack = UndoStackAttribute.getUndoInfo(_context);
        undoStack.undo();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The context in which to execute the request.
    private NamedObj _context;
}
