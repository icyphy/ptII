/* An attribute that holds the undo/redo information about a model.

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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.moml;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NotPersistent;
import ptolemy.kernel.util.SingletonAttribute;

import java.io.Writer;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

//////////////////////////////////////////////////////////////////////////
//// UndoInfoAttribute
/**
An attribute that holds the undo/redo information about a models change
history. This attribute is not persistent. This makes sense because a user
commonly expects a models undo/redo information to disappear once the
application that is being worked on is closed. It is also a singleton,
meaning that it will replace any previous attribute that has the same name
and is an instance of the base class, SingletonAttribute.

<p>Two stacks of information are maintained - one for undo information and
one for redo information. The information in the redo stack is placed there
whenever an undo is carried out, and can be used to reverse the effect
of the undo.

<p>NOTE: the information in the redo stack is emptied when a new undo entry is
pushed onto the undo stack that was not the result of a redo being
requested. This situation arises when a user requests a series of undo
and redo operations, and then performs some normal undoable action. At this
point the information in the redo stack is not relevant to the state of
the model and so must be cleared.

@author     Neil Smyth
@version    $Id$
@since Ptolemy II 2.1
*/
public class UndoInfoAttribute extends SingletonAttribute
    implements NotPersistent {

    /**
     *  Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null,
     *  or a NullPointerException will be thrown. This attribute will
     *  use the workspace of the container for synchronization and
     *  version counts. If the name argument is null, then the name is
     *  set to the empty string. The object is added to the directory
     *  of the workspace if the container is null. Increment the
     *  version of the workspace.
     *
     * @param  container                     The container.
     * @param  name                          The name of this attribute.
     * @exception  IllegalActionException    If the attribute is not of an
     *      acceptable class for the container, or if the name contains a
     *      period.
     * @exception  NameDuplicationException  If the name coincides with an
     *      attribute already in the container.
     */
    public UndoInfoAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // Flag that indicates if the redo information should be cleared
        _clearRedoInfo = true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  Write a MoML description of this object, which in this case is empty.
     *  Nothing is written. MoML is an XML modeling markup language.
     *
     * @param  output           The output stream to write to.
     * @param  depth            The depth in the hierarchy, to determine
     *      indenting.
     * @param  name             The name to use instead of the current name.
     */
    public void exportMoML(Writer output, int depth, String name) {
    }

    /** Merge the top two undo entries into a single entry if they are
     *  both using the same context. This is used primarily to
     *  allow the deletion of ports attached to hidden relations to be
     *  undoable.
     */
    public void mergeTopTwoUndos() {
        if (_undoEntries.size() < 2) {
            // Nothing to do so return
            return;
        }
        MoMLUndoEntry entryLast = popUndoEntry();
        MoMLUndoEntry entryFirst = popUndoEntry();
        NamedObj contextFirst = entryFirst.getUndoContext();
        if (contextFirst != entryLast.getUndoContext()) {
            // cannot merge
            pushUndoEntry(entryFirst);
            pushUndoEntry(entryLast);
            return;
        }
        StringBuffer combinedMoML = new StringBuffer();
        combinedMoML.append("<group>\n");
        combinedMoML.append(entryLast.getUndoMoML());
        combinedMoML.append(entryFirst.getUndoMoML());
        combinedMoML.append("</group>\n");
        pushUndoEntry(new MoMLUndoEntry(contextFirst,
                combinedMoML.toString()));
    }


    /** Get the UndoInfoAttribute associated with the given model. If no such
     *  attribute is currently associated, create and attach a new one.
     *  @param namedObj the namedObj contained by the model to obtain
     *   the UndoInfoAttribute for
     *  @return the current undo info attribute if there is one, or a new one.
     *  @throws Exception if the argument is null
     */
    public static UndoInfoAttribute getUndoInfo(NamedObj namedObj)
            throws Exception {
        if (namedObj == null) {
            // Argmuent cannot be null as we then have no entry point
            // into the model
            throw new Exception("Unable to get undo information on a model" +
                    " without a named object from the model");
        }
        // Get the top level of the model
        Nameable top = namedObj;
        while (top.getContainer() != null) {
            top = top.getContainer();
        }
        // Cast to a NamedObj
        NamedObj topLevel = (NamedObj)top;
        UndoInfoAttribute result;
        List attrList = topLevel.attributeList(UndoInfoAttribute.class);
        if (attrList.size() > 0) {
            result = (UndoInfoAttribute)attrList.get(0);
        } else {
            // Create and attach a new instance
            result = new UndoInfoAttribute(topLevel, "_undoInfo");
        }
        return result;
    }

    /**
     *  Returns the redo entry at the top of the stack without removing it
     *  from the stack. If there is no redo entry on the stack then return
     *  null.
     *
     * @return    the MoML from the first redo entry, or null if there are no
     *      redo entries
     */
    public String peekRedoEntry() {
        if (_redoEntries.isEmpty()) {
            return null;
        }
        MoMLUndoEntry entry = (MoMLUndoEntry)_redoEntries.peek();
        return entry.getUndoMoML();
    }

    /**
     *  Returns the undo entry at the top of the stack without
     *  removing it from the stack. If there is no undo entry on the
     *  stack then return null.
     *
     * @return    the MoML from the first undo entry, or null if there are no
     *      undo entries
     */
    public String peekUndoEntry() {
        if (_undoEntries.isEmpty()) {
            return null;
        }
        MoMLUndoEntry entry = (MoMLUndoEntry)_undoEntries.peek();
        return entry.getUndoMoML();
    }

    /**
     *  Remove the top undo entry and return it. If there are no undo entries
     *  available then null is returned.
     *
     * @return    the most recent undo entry, or null if there are no undo
     *      entries available.
     */
    public MoMLUndoEntry popUndoEntry() {
        MoMLUndoEntry undoable = null;
        try {
            undoable = (MoMLUndoEntry)_undoEntries.pop();
        }
        catch (EmptyStackException ex) {
            // Undo called with nothing to undo, do nothing
            undoable = null;
        }
        return undoable;
    }

    /**
     *  Remove the top redo entry and return it. If there are no redo entries
     *  available then null is returned.
     *
     * @return    the most recent redo entry, or null if there are no redo
     *      entries available.
     */
    public MoMLUndoEntry popRedoEntry() {
        MoMLUndoEntry redoable = null;
        try {
            redoable = (MoMLUndoEntry)_redoEntries.pop();
        }
        catch (EmptyStackException ex) {
            // Undo called with nothing to undo, do nothing
            redoable = null;
        }
        // Information is removed from the redo stack so do not clear this
        // stack on the next pushing of information onto the undo stack
        _clearRedoInfo = false;
        return redoable;
    }


    /**
     *  Add an entry to the undo stack
     *
     * @param  newEntry  The MoML that will carry out the undo
     */
    public void pushUndoEntry(MoMLUndoEntry newEntry) {
        // Just carried out an undo
        _undoEntries.push(newEntry);
        // Check whether this undo info is the result of a redo request, in
        // which case do not clear the redo stack. Otherwise clea the redo
        // stack.
        if (_clearRedoInfo) {
            _redoEntries.clear();
        }
        // Set the flag to true - this implies that two successive undo push
        // requests will clear the redo stack which is correct.
        _clearRedoInfo = true;
    }

    /**
     *  Add an entry to the redo stack
     *
     * @param  newEntry  The MoML that will carry out the redo
     */
    public void pushRedoEntry(MoMLUndoEntry newEntry) {
        // Just carried out an undo, need to store for a redo
        _redoEntries.push(newEntry);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The stack of available redo entries
    private Stack _redoEntries = new Stack();

    // The stack of available undo entries
    private Stack _undoEntries = new Stack();

    // Flag to indicate if the information in the redo stack needs to
    // be cleared
    private boolean _clearRedoInfo;
}
