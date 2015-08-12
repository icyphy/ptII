/* A top-level dialog window for editing parameters of a NamedObj.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.Frame;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import ptolemy.actor.gui.style.StyleConfigurer;
import ptolemy.data.expr.StringParameter;
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

///////////////////////////////////////////////////////////////////
//// EditParametersDialog

/**
 This class is a modal dialog box for editing the parameters of a
 target object, which is an instance of NamedObj. All attributes that
 implement the Settable interface and have visibility FULL or
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
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (neuendor)
 */
@SuppressWarnings("serial")
public class EditParametersDialog extends ComponentDialog implements
ChangeListener {
    /** Construct a dialog with the specified owner and target.
     *  A "Commit" and a "Cancel" button are added to the dialog.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param target The object whose parameters are being edited.
     */
    public EditParametersDialog(Frame owner, NamedObj target) {
        this(owner, target, "Edit parameters for " + target.getName());
    }

    /** Construct a dialog with the specified owner and target.
     *  A "Commit" and a "Cancel" button are added to the dialog.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param target The object whose parameters are being edited.
     *  @param label The label for the dialog box.
     */
    public EditParametersDialog(Frame owner, NamedObj target, String label) {
        super(owner, label, new Configurer(target), _moreButtons);

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
            List<Settable> attributeList = _target
                    .attributeList(Settable.class);

            // Count visible attributes
            Iterator<Settable> parameters = attributeList.iterator();
            int count = 0;

            while (parameters.hasNext()) {
                Settable parameter = parameters.next();

                if (Configurer.isVisible(target, parameter)) {
                    count++;
                }
            }

            String[] attributeNames = new String[count];
            parameters = attributeList.iterator();

            int index = 0;

            while (parameters.hasNext()) {
                Settable parameter = parameters.next();

                if (Configurer.isVisible(target, parameter)) {
                    attributeNames[index++] = ((Attribute) parameter).getName();
                }
            }

            Query query = new Query();
            query.addChoice("delete", "Parameter to delete", attributeNames,
                    null, false);

            ComponentDialog dialog = new ComponentDialog(_owner,
                    "Delete a parameter for " + _target.getFullName(), query,
                    null);

            // If the OK button was pressed, then queue a mutation
            // to delete the parameter.
            String deleteName = query.getStringValue("delete");

            if (dialog.buttonPressed().equals("OK") && !deleteName.equals("")) {
                String moml = "<deleteProperty name=\"" + deleteName + "\"/>";
                _target.addChangeListener(this);

                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        _target, moml);
                request.setUndoable(true);
                _target.requestChange(request);
            }
        } else if (buttonPressed().equals("Defaults")) {
            ((Configurer) contents).restoreToDefaults();

            // Open a new dialog (a modal dialog).
            new EditParametersDialog(_owner, _target);
        } else if (buttonPressed().equals("Preferences")) {
            // Create a dialog for setting parameter styles.
            try {
                StyleConfigurer panel = new StyleConfigurer(target);
                ComponentDialog dialog = new ComponentDialog(_owner,
                        "Edit preferences for " + target.getName(), panel);

                if (!dialog.buttonPressed().equals("OK")) {
                    // Restore original parameter values.
                    panel.restore();
                }

                new EditParametersDialog(_owner, _target);

                // NOTE: Instead of te above line, this used
                // to do the following. This isn't quite right because it violates
                // the modal dialog premise, since this method will
                // return and then a new dialog will open.
                // In particular, the preferences manager relies on the
                // modal behavior of the dialog. I'm sure other places do to.
                // EAL 7/05.
                // _reOpen();
            } catch (IllegalActionException ex) {
                MessageHandler.error("Edit Parameter Styles failed", ex);
            }
        } else if (buttonPressed().equals("Help")) {
            String helpURL = _getHelpURL();

            try {
                URL doc = getClass().getClassLoader().getResource(helpURL);

                // Try to use the configuration, if we can.
                boolean success = false;

                if (_owner instanceof TableauFrame) {
                    // According to FindBugs the cast is an error:
                    //  [M D BC] Unchecked/unconfirmed cast [BC_UNCONFIRMED_CAST]
                    // However it is checked that _owner instanceof TableauFrame,
                    // so FindBugs is wrong.

                    Configuration configuration = ((TableauFrame) _owner)
                            .getConfiguration();

                    if (configuration != null) {
                        configuration
                        .openModel(null, doc, doc.toExternalForm());
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
                    MessageHandler.warning("Cannot open help page \"" + helpURL
                            + "\".", ex);
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
    @Override
    public void changeExecuted(ChangeRequest change) {
        // Ignore if this is not the originator.
        if (change == null || change.getSource() != this) {
            return;
        }

        // Open a new dialog.
        // NOTE: this is ugly. It is necessary because the dialog
        // has been dismissed.
        // NOTE: Do this in the event thread, since this might be invoked
        // in whatever thread is processing mutations.
        Runnable changeExecutedRunnable = new ChangeExecutedRunnable();
        SwingUtilities.invokeLater(changeExecutedRunnable);
        _target.removeChangeListener(this);
    }

    /** Notify the listener that a change has resulted in an exception.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    @Override
    public void changeFailed(ChangeRequest change, final Exception exception) {
        // Ignore if this is not the originator.
        if (change == null || change.getSource() != this) {
            return;
        }

        _target.removeChangeListener(this);

        if (change.isErrorReported()) {
            // Error has already been reported.
            return;
        }

        change.setErrorReported(true);

        // NOTE: Do this in the event thread, since this might be invoked
        // in whatever thread is processing mutations.
        Runnable changeFailedRunnable = new ChangeFailedRunnable(exception);
        SwingUtilities.invokeLater(changeFailedRunnable);
    }

    /** Do the layout and then pack.
     */
    public void doLayoutAndPack() {
        // The doLayoutAndPack method is declared in the
        // ptolemy.gui.EditableParametersDialog interface.
        // That interface is necessary to avoid a dependency
        // between ptolemy.gui.Query and this class.
        doLayout();
        pack();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If the contents of this dialog implements the CloseListener
     *  interface, then notify it that the window has closed.
     */
    @Override
    protected void _handleClosing() {
        super._handleClosing();

        if (!buttonPressed().equals("Commit") && !buttonPressed().equals("Add")
                && !buttonPressed().equals("Preferences")
                && !buttonPressed().equals("Help")
                && !buttonPressed().equals("Remove")) {
            // Restore original parameter values.
            ((Configurer) contents).restore();
        }
    }

    /** Open a dialog to add a new parameter.
     *  @param message A message to place at the top, or null if none.
     *  @param name The default name.
     *  @param defValue The default value.
     *  @param className The default class name.
     *  @return The dialog that is created.
     */
    protected ComponentDialog _openAddDialog(String message, String name,
            String defValue, String className) {
        // Create a new dialog to add a parameter, then open a new
        // EditParametersDialog.
        _query = new Query();

        if (message != null) {
            _query.setMessage(message);
        }

        _query.addChoice("class", "Class", new String[] {
                "ptolemy.data.expr.Parameter",
                "ptolemy.data.expr.FileParameter",
                "ptolemy.kernel.util.StringAttribute",
        "ptolemy.actor.gui.ColorAttribute" },
        "ptolemy.data.expr.Parameter", true);

        _query.addLine("name", "Name", name);
        _query.addLine("default", "Default value", defValue);

        ComponentDialog dialog = new ComponentDialog(_owner,
                "Add a new parameter to " + _target.getFullName(), _query, null);

        String parameterClass = _query.getStringValue("class");

        // If the OK button was pressed, then queue a mutation
        // to create the parameter.
        // A blank property name is interpreted as a cancel.
        String newName = _query.getStringValue("name");

        // Need to escape quotes in default value.
        String newDefValue = StringUtilities.escapeForXML(_query
                .getStringValue("default"));

        if (dialog.buttonPressed().equals("OK") && !newName.equals("")) {
            String moml = "<property name=\"" + newName + "\" value=\""
                    + newDefValue + "\" class=\"" + parameterClass + "\"/>";
            _target.addChangeListener(this);

            MoMLChangeRequest request = new MoMLChangeRequest(this, _target,
                    moml);
            request.setUndoable(true);
            _target.requestChange(request);
        }
        return dialog;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A runnable for the change executed event. */
    class ChangeExecutedRunnable implements Runnable {
        @Override
        public void run() {
            new EditParametersDialog(_owner, _target);
        }
    }

    /** A runnable for the change failed event. */
    class ChangeFailedRunnable implements Runnable {
        public ChangeFailedRunnable(Exception exception) {
            _exception = exception;
        }

        @Override
        public void run() {
            // When a parameter is removed, and something depends on
            // it, this gets called when _query is null.
            // FIXME: Is this the right thing to do?
            if (_query == null) {
                return;
            }

            String newName = _query.getStringValue("name");
            ComponentDialog dialog = _openAddDialog(_exception.getMessage()
                    + "\n\nPlease enter a new default value:", newName,
                    _query.getStringValue("default"),
                    _query.getStringValue("class"));
            _target.removeChangeListener(EditParametersDialog.this);

            if (!dialog.buttonPressed().equals("OK")) {
                // Remove the parameter, since it seems to be erroneous
                // and the user hit cancel or close.
                String moml = "<deleteProperty name=\"" + newName + "\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        _target, moml);
                request.setUndoable(true);
                _target.requestChange(request);
            }
        }

        private Exception _exception;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variable                ////

    /** The owner window. */
    protected Frame _owner;

    /** The query window for adding parameters. */
    protected Query _query;

    /** The target object whose parameters are being edited. */
    protected NamedObj _target;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Returns the URL of the help file to be displayed when the user
     *  clicks the Help button. The help file defaults to the expression
     *  language documentation, but can be overridden by attaching a
     *  parameter {@code _helpURL} to the target object.
     *  @return URL of the help file to be displayed, parsable by the
     *          Ptolemy class loader.
     */
    private String _getHelpURL() {
        // Look for a _helpURL parameter attached to the target object
        List<StringParameter> attributeList = _target
                .attributeList(StringParameter.class);
        for (StringParameter attribute : attributeList) {
            if (attribute.getName().equals("_helpURL")) {
                try {
                    return attribute.stringValue();
                } catch (IllegalActionException ex) {
                    try {
                        MessageHandler.warning(
                                "Couldn't access help URL parameter.", ex);
                    } catch (CancelException exception) {
                        // Ignore the cancel.
                    }
                }
            }
        }

        // We couldn't find one, so return the default path to the
        // expression language help
        return "doc/expressions.htm";
    }

    /** Open a new dialog in a change request that defers
     *  to the Swing thread. This ensures no race conditions
     *  when we are re-opening a dialog to display the result
     *  of an edit change.
     */
    //    private void _reOpen() {
    //        ChangeRequest reOpen = new ChangeRequest(this,
    //                "Re-open configure dialog") {
    //            protected void _execute() throws Exception {
    //                SwingUtilities.invokeLater(new Runnable() {
    //                    public void run() {
    //                        new EditParametersDialog(_owner, _target);
    //                    }
    //                });
    //            }
    //        };
    //
    //        _target.requestChange(reOpen);
    //    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Button labels.
    private static String[] _moreButtons = { "Commit", "Add", "Remove",
        "Defaults", "Preferences", "Help", "Cancel" };
}
