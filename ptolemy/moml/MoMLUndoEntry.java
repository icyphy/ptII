/* Manages undo/redo actions on a MoML model.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.moml;

import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.UndoAction;

//////////////////////////////////////////////////////////////////////////
//// MoMLUndoEntry
/**
This is an undo action on the undo/redo stack.  The undo/redo stack
is stored in an instance of UndoInfoAttribute associated with the top-level
of a model.  If undo/redo is enabled, a MoMLParser will create entries
automatically and put them on the stack whenever it parses MoML.  So the
easiest mechanism to perform undoable actions is to specify those actions
in MoML and issue a MoMLChangeRequest to execute them. An alternative,
however, is to create an instance of this class with no MoML, using
the single argument constructor, and to override execute() to directly
perform the undo.

@see MoMLParser
@see kernel.util.UndoStackAttribute
@see MoMLChangeRequest
@author  Neil Smyth and Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
*/
public class MoMLUndoEntry implements UndoAction {

    /** Create an undo entry comprised of the specified MoML code.
     *  @param context The context in which to execute the undo.
     *  @param undoMoML The MoML specification of the undo action.
     */
    public MoMLUndoEntry(NamedObj context, String undoMoML) {
        _context = context;
        _undoMoML = undoMoML;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Parse the MoML specified in the constructor call in the context
     *  specified in the constructor call using the parser associated
     *  with the context (as determined by ParserAttribute.getParser()).
     *  @see ParserAttribute.getParser(NamedObj)
     */
    public void execute() throws Exception {
        // Use a MoMLChangeRequest so that changes get propogated
        // as appropriate to models that defer to this.
        MoMLChangeRequest request = new MoMLChangeRequest(
                this, _context, _undoMoML);
        // An undo entry is always undoable so that redo works.
        request.setUndoable(true);
        request.execute();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The context in which to execute the undo.
    private NamedObj _context;

    // The MoML specification of the undo.
    private String _undoMoML;
}
