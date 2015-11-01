/* An action for editing parameters.

 Copyright (c) 1999-2014 The Regents of the University of California.
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

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.KeyStroke;

import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.actor.gui.Editable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// ConfigureAction

/**
 An action that will configure parameters on the current object.
 If that object contains an attribute that is an instance of EditorFactory,
 then that instance is used to create the dialog (or whatever) to configure
 the object.  Otherwise, an instance of EditParametersDialog is created.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 @see ptolemy.actor.gui.EditParametersDialog
 */
@SuppressWarnings("serial")
public class ConfigureAction extends FigureAction {
    /** Construct a new configure action.
     *  @param description A description.
     */
    public ConfigureAction(String description) {
        super(description);

        // FIXME: For some inexplicable reason, the following works
        // for LookInsideAction to define a hotkey for look inside,
        // but it doesn't work here.

        // Call getMenuShortcutKeyMask() instead of Event.CTRL_MASK
        // and avoid problems with shortcut keys on the Macintosh.  See
        // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3094
        putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                KeyEvent.VK_E, Toolkit.getDefaultToolkit()
                .getMenuShortcutKeyMask()));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Open a dialog to edit the target.
     *  @param e The event.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            // Determine which entity was selected for the look inside action.
            super.actionPerformed(e);

            NamedObj target = getTarget();

            if (target == null) {
                return;
            }

            // Create a dialog for configuring the object.
            // First, identify the top parent frame.
            Frame parent = getFrame();
            _openDialog(parent, target, e);
        } catch (Throwable throwable) {
            // Giotto code generator on giotto/demo/Hierarchy/Hierarchy.xml
            // was throwing an exception here that was not being displayed
            // in the UI.
            MessageHandler.error("Failed to open a dialog to edit the target.",
                    throwable);
        }
    }

    /** Open an edit parameters dialog.  This is a modal dialog, so
     *  this method returns only after the dialog has been dismissed.
     *  @param parent A frame to serve as a parent for the dialog, or
     *  null if there is none.
     *  @param target The object whose parameters are to be edited.
     */
    public void openDialog(Frame parent, NamedObj target) {
        _openDialog(parent, target, null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Open an edit parameters dialog.  This is a modal dialog, so
     *  this method returns only after the dialog has been dismissed.
     *  @param parent A frame to serve as a parent for the dialog, or
     *  null if there is none.
     *  @param target The object whose parameters are to be edited.
     *  @param event The action event that triggered this, or null if
     *   none.
     */
    protected void _openDialog(Frame parent, NamedObj target, ActionEvent event) {
        List<Editable> attributeList = target.attributeList(Editable.class);
        boolean altKeyPressed = false;
        if (event != null) {
            altKeyPressed = (event.getModifiers() & ActionEvent.ALT_MASK) != 0;
        }

        //Use the EditorFactory if the alt key is not pressed and the
        // action command is not "Configure".
        if (attributeList.size() > 0
                && !altKeyPressed
                && (event == null || !event.getActionCommand().equals(
                        "Configure"))) {

            // Use the last editor factory if there is more than one.
            Editable factory = attributeList.get(attributeList.size() - 1);
            factory.createEditor(target, parent);
        } else {
            new EditParametersDialog(parent, target);
        }
    }
}
