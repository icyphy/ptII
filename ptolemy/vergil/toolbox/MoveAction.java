/* An action for moving an object up or down in its list.

Copyright (c) 1999-2004 The Regents of the University of California.
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

package ptolemy.vergil.toolbox;

import java.awt.event.ActionEvent;

import ptolemy.kernel.undo.UndoAction;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// MoveAction
/**
   An action to move an object up or down in its list.
   This can be used, for example, to move icon elements towards
   the foreground or to control the order in which attributes
   or ports appear.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (johnr)
*/
    
public class MoveAction extends FigureAction {

    /** Construct a new action. The type of move is specified by
     *  the public fields DOWN, TO_FIRST, TO_LAST, and UP.
     *  @param description A description.
     *  @param type Indicator of the type of move.
     */
    public MoveAction(String description, MoveType type) {
        super(description);
        _type = type;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicator for move down. */
    public static MoveType DOWN = new MoveType();

    /** Indicator for move to first. */
    public static MoveType TO_FIRST = new MoveType();
    
    /** Indicator for move to last. */
    public static MoveType TO_LAST = new MoveType();

    /** Indicator for move up. */
    public static MoveType UP = new MoveType();

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Perform the move action and register the undo action.
     *  @param event The event.
     */
    public void actionPerformed(final ActionEvent event) {
        // Determine which entity was selected for the look inside action.
        super.actionPerformed(event);
        final NamedObj target = getTarget();
        if (target == null) return;
        ChangeRequest request = new ChangeRequest(target, "Move towards last") {
            protected void _execute() throws IllegalActionException {
                _doAction(target);
            }
        };
        target.requestChange(request);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Perform the move. This is factored out as a separate method so
     *  that it can be called in the redo action.
     *  @param target The object to move.
     */
    private void _doAction(final NamedObj target) {
        int index;
        if (_type == DOWN) {
            index = target.moveDown();
        } else if (_type == TO_FIRST) {
            index = target.moveToFirst();
        } else if (_type == TO_LAST) {
            index = target.moveToLast();
        } else {
            index = target.moveUp();
        }
        final int priorIndex = index;
        
        if (priorIndex < 0) {
            // Do not generate any undo action if no move happened.
            return;
        }

        UndoAction undoAction = new UndoAction() {
            public void execute() {
                target.moveToIndex(priorIndex);
                        
                // Create redo action.
                UndoAction redoAction = new UndoAction() {
                    public void execute() {
                        _doAction(target);
                    }
                };
                UndoStackAttribute undoInfo
                        = UndoStackAttribute.getUndoInfo(target);
                undoInfo.push(redoAction);
            }
        };
        UndoStackAttribute undoInfo
                = UndoStackAttribute.getUndoInfo(target);
        undoInfo.push(undoAction);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The type of move. */
    private MoveType _type;
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** For a type-safe enumeration. */
    private static class MoveType {
    }
}
