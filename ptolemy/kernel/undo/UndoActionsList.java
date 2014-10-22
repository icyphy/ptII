/* Represents an undo or redo action.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

import java.util.LinkedList;
import java.util.List;

///////////////////////////////////////////////////////////////////
//// UndoActionsList

/**
 This class contains a sequential list of UndoAction instances that
 can be executed in order.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class UndoActionsList implements UndoAction {

    /** Create an undo action with the specified action
     *  to be executed first.
     *  @param firstAction The action to execute first.
     */
    public UndoActionsList(UndoAction firstAction) {
        _actionList = new LinkedList<UndoAction>();
        _actionList.add(firstAction);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append a new entry to the list.
     *  @param action The entry to append.
     */
    public void add(UndoAction action) {
        _actionList.add(action);
    }

    /** Execute the action. */
    @Override
    public void execute() throws Exception {
        for (UndoAction action : _actionList) {
            action.execute();
        }
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("Action List:\n");
        for (UndoAction action : _actionList) {
            result.append(action.toString());
            result.append("\n");
        }
        result.append("\nEnd of action list.\n");
        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The list of actions to execute. */
    private List<UndoAction> _actionList;
}
