/* A text editor to edit a string attribute.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@AcceptedRating Red (ptolemy@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;

import ptolemy.actor.gui.TextEditor;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.StringUtilities;

/**
A text editor to edit a specified string attribute.

@author Edward A. Lee
@version $Id$
*/

public class TextEditorForStringAttributes extends TextEditor {

    /** Create a annotation text editor for the specified attribute.
     *  @param factory The factory that created this editor.
     *  @param attributeToEdit The string attribute to edit.
     *  @param title The window title to use.
     */
    public TextEditorForStringAttributes(
            TextEditorFactory factory,
            StringAttribute attributeToEdit,
            int rows,
            int columns,
            String title) {

        super(title);
        this._factory = factory;
        _attributeToEdit = attributeToEdit;
        text.append(_attributeToEdit.getExpression());
        text.setColumns(columns);
        text.setRows(rows);
        // The above will mark the text object modified. Reverse this.
        setModified(false);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Adjust the file menu so that only relevant items appear.
     *  This has to be called after pack().
     */
    public void adjustFileMenu() {
        // Rename Save command.
        _fileMenuItems[3].setText("Apply");
        _fileMenuItems[3].setMnemonic(KeyEvent.VK_A);
        // Remove various menu item.
        _fileMenu.remove(7);
        // _fileMenu.remove(6);
        _fileMenu.remove(5);
        _fileMenu.remove(4);
        _fileMenu.remove(2);
        _fileMenu.remove(1);
        _fileMenu.remove(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected methods                    ////
    
    /** Override to query whether to apply the changes, if any.
     *  @return False if the user cancels on a apply query.
     */
    protected boolean _close() {
        // NOTE: The superclass doesn't do the right thing here,
        // since it requires an associated Tableau.

        // NOTE: We use dispose() here rather than just hiding the
        // window.  This ensures that derived classes can react to
        // windowClosed events rather than overriding the
        // windowClosing behavior given here.
        boolean returnValue = true;
        if (isModified()) {
            if (_queryForApply()) {
                dispose();
            } else {
                return false;
            }
        } else {
            // Window is not modified, so just dispose.
            dispose();
        }

        // Ensure that a new editor is opened next time.
        this._factory.clear();

        return returnValue;
    }

    /** Override the base class to apply the change to the attribute.
     *  @return True if the save succeeded.
     */
    protected boolean _save() {
        // Issue a change request to ensure the change is
        // applied at a safe time and that the model is marked
        // modified.
        NamedObj context =
            MoMLChangeRequest.getDeferredToParent(_attributeToEdit);
        if (context == null) {
            context = (NamedObj) _attributeToEdit.getContainer();
        }
        String request =
            "<property name=\""
                + _attributeToEdit.getName(context)
                + "\" value=\""
                + StringUtilities.escapeForXML(_factory.getText())
                + "\"/>";
        context.requestChange(new MoMLChangeRequest(this, context, request));
        setModified(false);
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    // Open a dialog to prompt the user to apply the data.
    // Return false if the user clicks "cancel", and otherwise return true.
    private boolean _queryForApply() {
        Object[] options = { "Apply", "Discard changes", "Cancel" };
        String query =
            "Apply changes to " + _attributeToEdit.getFullName() + "?";

        // Show the MODAL dialog
        int selected =
            JOptionPane.showOptionDialog(
                this,
                query,
                "Apply Changes?",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (selected == 0) {
            return _save();
        } else if (selected == 1) {
            return true;
        }
        return false;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    
    private final TextEditorFactory _factory;
    private StringAttribute _attributeToEdit;
}