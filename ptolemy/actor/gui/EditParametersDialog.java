/* A top-level dialog window for editing parameters of a NamedObj.

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
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import java.awt.Frame;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import ptolemy.actor.gui.style.StyleConfigurer;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// EditParametersDialog
/**
This class is a modal dialog box for editing the parameters of a
target object, which is an instance of NamedObj. All attributes that
implement the Settable interface and have visibility FULL, or
NOT_EDITABLE are included in the dialog. An instance of this class
contains an instance of Configurer, which examines the target for
attributes of type EditorPaneFactory.  Those attributes, if they are
present, define the panels that are used to edit the parameters of the
target.  If they are not present, then a default panel is created.

<p> If the panels returned by EditorPaneFactory implement the
CloseListener interface, then they are notified when this dialog
is closed, and are informed of which button (if any) was used to
close the dialog.

<p> The dialog is modal, so that (in lieu of a proper undo mechanism)
the Cancel button can properly undo any modifications that are made.
This means that the statement that creates the dialog will not return
until the user dismisses the dialog.  The method buttonPressed() can
then be called to find out whether the user clicked the Commit button
or the Cancel button (or any other button specified in the
constructor).  Then you can access the component to determine what
values were set by the user.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class EditParametersDialog extends ComponentDialog
    implements ChangeListener {

    /** Construct a dialog with the specified owner and target.
     *  A "Commit" and a "Cancel" button are added to the dialog.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param target The object whose parameters are being edited.
     */
    public EditParametersDialog(Frame owner, NamedObj target) {
        super(owner,
                "Edit parameters for " + target.getName(),
                new Configurer(target),
                _moreButtons);
        // Once we get to here, the dialog has already been dismissed.
        _owner = owner;
        _target = target;
        if (buttonPressed().equals("Add")) {
            _openAddDialog(null, "", "", "ptolemy.data.expr.Parameter");
            _target.removeChangeListener(this);
        } else if (buttonPressed().equals("Remove")) {
            // Create a new dialog to remove a parameter, then open a new
            // EditParametersDialog.
            // First, create a string array with the names of all the
            // parameters.
            List attributeList = _target.attributeList(Settable.class);

            // Count visible attributes
            Iterator parameters = attributeList.iterator();
            int count = 0;
            while (parameters.hasNext()) {
                Settable parameter = (Settable)parameters.next();
                if (Configurer.isVisible(parameter)) {
                    count++;
                }
            }

            String[] attributeNames = new String[count];
            parameters = attributeList.iterator();
            int index = 0;
            while (parameters.hasNext()) {
                Settable parameter = (Settable)parameters.next();
                if (Configurer.isVisible(parameter)) {
                    attributeNames[index++] = ((Attribute)parameter).getName();
                }
            }
            Query query = new Query();
            query.addChoice("delete", "Parameter to delete",
                    attributeNames, null, false);

            ComponentDialog dialog = new ComponentDialog(
                    _owner,
                    "Delete a parameter for " + _target.getFullName(),
                    query,
                    null);
            // If the OK button was pressed, then queue a mutation
            // to delete the parameter.
            String deleteName = query.getStringValue("delete");

            if (dialog.buttonPressed().equals("OK")
                    && !deleteName.equals("")) {
                String moml = "<deleteProperty name=\""
                    + deleteName
                    + "\"/>";
                _target.addChangeListener(this);
                MoMLChangeRequest request = new MoMLChangeRequest(this, _target,
                        moml);
                request.setUndoable(true);
                _target.requestChange(request);
            }
        } else if (buttonPressed().equals("Edit Styles")) {
            // Create a dialog for setting parameter styles.
            try {
                StyleConfigurer panel = new StyleConfigurer(target);
                ComponentDialog dialog = new ComponentDialog(
                        _owner,
                        "Edit parameter styles for " + target.getName(),
                        panel);
                if (!(dialog.buttonPressed().equals("OK"))) {
                    // Restore original parameter values.
                    panel.restore();
                }
                // Open a new dialog.
                new EditParametersDialog(_owner, _target);

            } catch (IllegalActionException ex) {
                MessageHandler.error("Edit Parameter Styles failed", ex);
            }
        } else if (buttonPressed().equals("Help")) {
            try {
                URL doc = getClass().getClassLoader().getResource(
                        "doc/expressions.htm");
                // Try to use the configuration, if we can.
                boolean success = false;
                if (_owner instanceof TableauFrame) {
                    Configuration configuration
                        = ((TableauFrame)_owner).getConfiguration();
                    if (configuration != null) {
                        configuration.openModel(
                                null, doc, doc.toExternalForm());
                        success = true;
                    }
                }
                if (!success) {
                    // Just open an HTML page.
                    HTMLViewer viewer = new HTMLViewer();
                    viewer.setPage(doc);
                    viewer.pack();
                    viewer.show();
                }
            } catch (Exception ex) {
                try {
                    MessageHandler.warning("Cannot open help page.", ex);
                } catch (CancelException exception) {
                    // Ignore the cancel.
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the fact that a change has been successfully executed.
     *  This method opens a new parameter editor to replace the one that
     *  was closed.
     *  @param change The change that was executed.
     */
    public void changeExecuted(ChangeRequest change) {
        // Ignore if this is not the originator.
        if (change == null || change.getSource() != this) return;

        // Open a new dialog.
        // NOTE: this is ugly. It is necessary because the dialog
        // has been dismissed.
        // NOTE: Do this in the event thread, since this might be invoked
        // in whatever thread is processing mutations.
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new EditParametersDialog(_owner, _target);
                }
            });
        _target.removeChangeListener(this);
    }

    /** Notify the listener that a change has resulted in an exception.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, final Exception exception) {
        // Ignore if this is not the originator.
        if (change == null || change.getSource() != this) return;

        _target.removeChangeListener(this);

        if (change.isErrorReported()) {
            // Error has already been reported.
            return;
        }
        change.setErrorReported(true);

        // NOTE: Do this in the event thread, since this might be invoked
        // in whatever thread is processing mutations.
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // When a parameter is removed, and something depends on
                    // it, this gets called when _query is null.
                    // FIXME: Is this the right thing to do?
                    if (_query == null) return;
                    String newName = _query.getStringValue("name");
                    ComponentDialog dialog =
                        _openAddDialog(exception.getMessage()
                                + "\n\nPlease enter a new default value:",
                                newName,
                                _query.getStringValue("default"),
                                _query.getStringValue("class"));
                    _target.removeChangeListener(EditParametersDialog.this);
                    if (!dialog.buttonPressed().equals("OK")) {
                        // Remove the parameter, since it seems to be erroneous
                        // and the user hit cancel or close.
                        String moml =
                            "<deleteProperty name=\"" + newName + "\"/>";
                        MoMLChangeRequest request =
                            new MoMLChangeRequest(this, _target, moml);
                        request.setUndoable(true);
                        _target.requestChange(request);
                    }
                }
            });
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If the contents of this dialog implements the CloseListener
     *  interface, then notify it that the window has closed.
     */
    protected void _handleClosing() {
        super._handleClosing();
        if (!buttonPressed().equals("Commit")
                && !buttonPressed().equals("Add")
                && !buttonPressed().equals("Edit Styles")
                && !buttonPressed().equals("Help")
                && !buttonPressed().equals("Remove")) {
            // Restore original parameter values.
            ((Configurer)contents).restore();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Open a dialog to add a new parameter.
     *  @param message A message to place at the top, or null if none.
     *  @param name The default name.
     *  @param defValue The default value.
     *  @param className The default class name.
     *  @return The dialog that is created.
     */
    private ComponentDialog _openAddDialog(
            String message, String name, String defValue, String className) {
        // Create a new dialog to add a parameter, then open a new
        // EditParametersDialog.
        _query = new Query();
        if (message != null) _query.setMessage(message);
        _query.addLine("name", "Name", name);
        _query.addLine("default", "Default value", defValue);
        _query.addLine("class", "Class", className);
        ComponentDialog dialog = new ComponentDialog(
                _owner,
                "Add a new parameter to " + _target.getFullName(),
                _query,
                null);
        // If the OK button was pressed, then queue a mutation
        // to create the parameter.
        // A blank property name is interpreted as a cancel.
        String newName = _query.getStringValue("name");

        // Need to escape quotes in default value.
        String newDefValue = StringUtilities.escapeForXML(
                _query.getStringValue("default"));

        if (dialog.buttonPressed().equals("OK") && !newName.equals("")) {
            String moml = "<property name=\""
                + newName
                + "\" value=\""
                + newDefValue.toString()
                + "\" class=\""
                + _query.getStringValue("class")
                + "\"/>";
            _target.addChangeListener(this);
            MoMLChangeRequest request = new MoMLChangeRequest(this, _target,
                    moml);
            request.setUndoable(true);
            _target.requestChange(request);
        }
        return dialog;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Button labels.
    private static String[] _moreButtons
    = {"Commit", "Add", "Remove", "Edit Styles", "Help", "Cancel"};

    // The owner window.
    private Frame _owner;

    // The query window for adding parameters.
    private Query _query;

    // The target object whose parameters are being edited.
    private NamedObj _target;
}
