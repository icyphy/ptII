/* A request to redo.

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
package ptolemy.kernel.undo;

import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////

/**
 A change request to redo. When executed, this change request will
 identify the undo stack associated with the specified context,
 and it will execute the top redo action on that stack, if there is
 one.
 <p>
 @author Edward A. Lee and Neil Smyth
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (hyzheng)
 */
public class RedoChangeRequest extends ChangeRequest {
    /** Construct a change request to be executed in the specified context.
     *  The redo stack associated with the specified context will be used.
     *  That stack is the one returned by UndoStackAttribute.getUndoInfo().
     *  @see UndoStackAttribute
     *  @param originator The originator of the change request.
     *  @param context The context in which to execute the MoML.
     */
    public RedoChangeRequest(Object originator, NamedObj context) {
        super(originator, "Request to redo.");
        _context = context;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the context specified in the constructor, or null if none
     *  was specified.
     *  @return The context.
     */
    public NamedObj getContext() {
        return _context;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Execute the change by invoking redo on the undo stack of
     *  the context specified in the constructor.
     *  @exception Exception If an exception is thrown
     *   while evaluating the request.
     */
    @Override
    protected void _execute() throws Exception {
        // Check to see whether there is a context...
        if (_context == null) {
            throw new InternalErrorException("Context is unexpectedly null.");
        }

        UndoStackAttribute undoStack = UndoStackAttribute.getUndoInfo(_context);

        // The undo action may involve several subactions.
        // These may queue further change requests.
        // Collect these change requests without executing them
        // until after the whole action is completed.
        boolean previous = _context.isDeferringChangeRequests();

        try {
            previous = _context.isDeferringChangeRequests();
            _context.setDeferringChangeRequests(true);
            undoStack.redo();
        } finally {
            _context.setDeferringChangeRequests(previous);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The context in which to execute the request.
    private NamedObj _context;
}
