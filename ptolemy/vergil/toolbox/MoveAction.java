/* An action for moving an object up or down in its list.

Copyright (c) 1999-2005 The Regents of the University of California.
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

import ptolemy.kernel.undo.UndoAction;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


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
    public static final MoveType DOWN = new MoveType();

    /** Indicator for move to first. */
    public static final MoveType TO_FIRST = new MoveType();

    /** Indicator for move to last. */
    public static final MoveType TO_LAST = new MoveType();

    /** Indicator for move up. */
    public static final MoveType UP = new MoveType();

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Perform the move action and register the undo action.
     *  @param event The event.
     */
    public void actionPerformed(final ActionEvent event) {
        // Determine which entity was selected for the look inside action.
        super.actionPerformed(event);

        final NamedObj target = getTarget();

        if (target == null) {
            return;
        }

        if (target.getDerivedLevel() < Integer.MAX_VALUE) {
            MessageHandler.error("Cannot change the position of "
                    + target.getFullName()
                    + " because the position is set by the class.");
            return;
        }

        ChangeRequest request = new ChangeRequest(target, "Move towards last") {
                protected void _execute() throws IllegalActionException {
                    // Static method takes a list, so we construct a
                    // list with one element.
                    LinkedList targets = new LinkedList();
                    targets.add(target);
                    move(targets, _type, target);
                }
            };

        target.requestChange(request);
    }

    /** Move the objects in the specified list up or down in the list
     *  of similar objects in their container, as specified by the move type.
     *  If the type is TO_FIRST or UP, then
     *  the objects in the specified list are processed in reverse order,
     *  under the assumption that they will already be sorted into the order
     *  in which they appear in the list of similar objects in their container.
     *  This is factored out as a separate static method so
     *  that it can be called in the redo action and so that it can be
     *  used elsewhere.  The context is what is used to register an
     *  undo action. It should be a common container, or if there is
     *  only one target, then the target itself.
     *  @param targets The list of objects to move.
     *  @param type One of DOWN, TO_FIRST, TO_LAST, and UP.
     *  @param context The context.
     */
    public static void move(final List targets, final MoveType type,
            final NamedObj context) {
        final int[] priorIndexes = new int[targets.size()];
        boolean movedOne = false;

        try {
            if ((type == TO_FIRST) || (type == UP)) {
                // Traverse the list in reverse order.
                ListIterator targetIterator = targets.listIterator(targets.size());

                for (int i = targets.size() - 1; i >= 0; i--) {
                    NamedObj target = (NamedObj) targetIterator.previous();

                    if (type == DOWN) {
                        priorIndexes[i] = target.moveDown();
                    } else if (type == TO_FIRST) {
                        priorIndexes[i] = target.moveToFirst();
                    } else if (type == TO_LAST) {
                        priorIndexes[i] = target.moveToLast();
                    } else {
                        priorIndexes[i] = target.moveUp();
                    }

                    if (priorIndexes[i] >= 0) {
                        movedOne = true;
                    }
                }
            } else {
                // Traverse the list in forward order.
                Iterator targetIterator = targets.iterator();

                for (int i = 0; i < targets.size(); i++) {
                    NamedObj target;
                    target = (NamedObj) targetIterator.next();

                    if (type == DOWN) {
                        priorIndexes[i] = target.moveDown();
                    } else if (type == TO_FIRST) {
                        priorIndexes[i] = target.moveToFirst();
                    } else if (type == TO_LAST) {
                        priorIndexes[i] = target.moveToLast();
                    } else {
                        priorIndexes[i] = target.moveUp();
                    }

                    if (priorIndexes[i] >= 0) {
                        movedOne = true;
                    }
                }
            }
        } catch (IllegalActionException e) {
            // This should only be thrown if the target
            // has no container, which in theory is not
            // possible.
            throw new InternalErrorException(e);
        }

        if (!movedOne) {
            // Do not generate any undo action if no move happened.
            return;
        }

        UndoAction undoAction = new UndoAction() {
                public void execute() {
                    try {
                        // Undo has to reverse the order of the do.
                        if ((type == TO_FIRST) || (type == UP)) {
                            // Traverse the list in forward order.
                            Iterator targetIterator = targets.iterator();

                            for (int i = 0; i < targets.size(); i++) {
                                NamedObj target = (NamedObj) targetIterator
                                    .next();
                                target.moveToIndex(priorIndexes[i]);
                            }
                        } else {
                            // Traverse the list in reverse order.
                            ListIterator targetIterator = targets.listIterator(targets
                                    .size());

                            for (int i = targets.size() - 1; i >= 0; i--) {
                                NamedObj target = (NamedObj) targetIterator
                                    .previous();
                                target.moveToIndex(priorIndexes[i]);
                            }
                        }
                    } catch (IllegalActionException e) {
                        // This should only be thrown if the target
                        // has no container, which in theory is not
                        // possible.
                        throw new InternalErrorException(e);
                    }

                    // Create redo action.
                    UndoAction redoAction = new UndoAction() {
                            public void execute() {
                                move(targets, type, context);
                            }
                        };

                    UndoStackAttribute undoInfo = UndoStackAttribute
                        .getUndoInfo(context);
                    undoInfo.push(redoAction);
                }
            };

        UndoStackAttribute undoInfo = UndoStackAttribute.getUndoInfo(context);
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
