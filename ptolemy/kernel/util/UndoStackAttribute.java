/* An attribute that holds the undo/redo information about a model.

 Copyright (c) 2003 The Regents of the University of California.
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

package ptolemy.kernel.util;

import java.util.List;
import java.util.Stack;

//////////////////////////////////////////////////////////////////////////
//// UndoStackAttribute
/**
This attribute holds the undo/redo information for a model.
This attribute is not persistent, so undo/redo information disappears
when the model is closed. It is also a singleton, meaning that it will
replace any previous attribute that has the same name
and is an instance of the same base class, SingletonAttribute.
<p>
Two stacks of information are maintained - one for undo information and
one for redo information. Normally, a push onto this stack puts the
undo information in the undo stack. However, if the push occurs during
the execution of an undo, then the information is put on the redo stack.
The entries on the stack implement the UndoAction interface.
<p>
NOTE: the information in the redo stack is emptied when a new undo action is
pushed onto the undo stack that was not the result of a redo being
requested. This situation arises when a user requests a series of undo
and redo operations, and then performs some normal undoable action. At this
point the information in the redo stack is not relevant to the state of
the model and so must be cleared.

@see UndoAction
@author Neil Smyth and Edward A. Lee
@version $Id$
@since Ptolemy II 3.1
*/
public class UndoStackAttribute extends SingletonAttribute {

    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null,
     *  or a NullPointerException will be thrown. This attribute will
     *  use the workspace of the container for synchronization and
     *  version counts. If the name argument is null, then the name is
     *  set to the empty string. The object is added to the directory
     *  of the workspace if the container is null. Increment the
     *  version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a
     *   period.
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public UndoStackAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the UndoStackAttribute associated with the given object.
     *  This is done by searching up the containment hierarchy until
     *  such an attribute is found. If no such attribute is found,
     *  then create and attach a new one to the top level.
     *  @param object The model for which an undo stack is required.
     *  @return The current undo stack attribute if there is one, or a new one.
     *  @exception NullPointerException If the argument is null.
     */
    public static UndoStackAttribute getUndoInfo(NamedObj object) {
        NamedObj topLevel = object.toplevel();
        while (object != null) {
            List attrList = object.attributeList(UndoStackAttribute.class);
            if (attrList.size() > 0) {
                return (UndoStackAttribute)attrList.get(0);
            }
            object = (NamedObj)object.getContainer();
        }
        // If we get here, there is no such attribute.
        // Create and attach a new instance.
        try {
            return new UndoStackAttribute(topLevel, "_undoInfo");
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }
    }

    /** Merge the top two undo entries into a single action. If there
     *  are fewer than two entries on the stack, do nothing.
     */
    public void mergeTopTwo() {
        if (_undoEntries.size() > 1) {
            final UndoAction actionLast = (UndoAction)_undoEntries.pop();
            final UndoAction actionFirst = (UndoAction)_undoEntries.pop();
            UndoAction newAction = new UndoAction() {
                public void execute() throws Exception {
                    actionFirst.execute();
                    actionLast.execute();
                }
            };
            _undoEntries.push(newAction);
        }
    }

    /** Push an action to the undo stack, or if we are executing an undo,
     *  onto the redo stack.
     *  @param action The undo action.
     */
    public void push(UndoAction action) {
        if (_inUndo) {
            _redoEntries.push(action);
        } else {
            _undoEntries.push(action);
            if (!_inRedo) {
                _redoEntries.clear();
            }
        }
    }

    /** Remove the top redo action and execute it.
     *  If there are no redo entries, do nothing.
     */
    public void redo() throws Exception {
        if (_redoEntries.size() > 0) {
            UndoAction action = (UndoAction)_redoEntries.pop();
            try {
                _inRedo = true;
                action.execute();
            } finally {
                _inRedo = false;
            }
        }
    }

    /** Remove the top undo action and execute it.
     *  If there are no undo entries, do nothing.
     */
    public void undo() throws Exception {
        if (_undoEntries.size() > 0) {
            UndoAction action = (UndoAction)_undoEntries.pop();
            try {
                _inUndo = true;
                action.execute();
            } finally {
                _inUndo = false;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Flag to indicate that we are in a redo.
    private boolean _inRedo = false;

    // Flag to indicate that we are in an undo.
    private boolean _inUndo = false;

    // The stack of available redo entries
    private Stack _redoEntries = new Stack();

    // The stack of available undo entries
    private Stack _undoEntries = new Stack();
}
