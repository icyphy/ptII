/* A top-level dialog window for configuring the arguments of an entity.
    Largely inspired of PortConfigurerDialog

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
@ProposedRating Yellow (vincent.arnould@thalesgroup.com)
@AcceptedRating Red (vincent.arnould@thalesgroup.com)
*/

package jni;

import java.awt.Frame;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.gui.Configuration;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// ArgumentConfigurerDialog
/**
   This class is a modal dialog box for configuring the arguments of an entity.
   An instance of this class contains an instance of ArgumentConfigurer.
   The dialog is modal, so the statement that creates the dialog will
   not return until the user dismisses the dialog.

   @see ArgumentConfigurer
   @author Edward A. Lee, V.Arnould
   @version $Id$
*/
public class ArgumentConfigurerDialog
    extends ComponentDialog
    implements ChangeListener {

    /** Construct a dialog with the specified owner and target.
     *  Several buttons are added to the dialog.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param target The object whose arguments are being configured.
     *  @param configuration The configuration to use to open the
     *   help screen (or null if help is not supported).
     */
    public ArgumentConfigurerDialog(
            Frame owner,
            Entity target,
            Configuration configuration) {
        super(
                owner,
                "Configure arguments for " + target.getName(),
                new ArgumentConfigurer((GenericJNIActor) target),
                _moreButtons);
        // Once we get to here, the dialog has already been dismissed.
        _configuration = configuration;
        _owner = owner;
        _target = (GenericJNIActor) target;

        if (buttonPressed().equals("Add")) {
            try {
                _openAddDialog(null, "", "", "");
            } catch (Exception ex) {
                MessageHandler.error("TRT error : ", ex);
            }
            _target.removeChangeListener(this);
        } else if (buttonPressed().equals("Remove")) {
            // Create a new dialog to remove a argument then open a new
            // ArgumentConfigurerDialog.
            // First, create a string array with the names of all the
            // ports.
            List argumentsList = ((GenericJNIActor) _target).argumentsList();
            String[] argumentNames = new String[argumentsList.size()];
            Iterator arguments = argumentsList.iterator();
            int index = 0;
            while (arguments.hasNext()) {
                Argument argument = (Argument) arguments.next();
                argumentNames[index++] = argument.getName();
            }
            Query query = new Query();
            query.addChoice(
                    "delete",
                    "Argument to delete",
                    argumentNames,
                    null,
                    false);

            ComponentDialog dialog =
                new ComponentDialog(
                        _owner,
                        "Delete a argument for " + _target.getFullName(),
                        query,
                        null);

            // If the OK button was pressed, then queue a mutation
            // to delete the argument.
            if (dialog.buttonPressed().equals("OK")) {

                String argumentName = query.getStringValue("delete");
                if (argumentName != null) {
                    Argument argument =
                        ((GenericJNIActor) _target).getArgument(argumentName);

                    if (argument != null) {
                        try {
                            ((GenericJNIActor) _target).
                                _removeArgument(argument);
                        } catch (Exception e) {
                            MessageHandler.error(
                                    "Unable to remove argument '" +
                                    argument + "'.", e);
                        }
                        // The context for the MoML should be the first
                        // container above this port in the hierarchy
                        // that defers its MoML definition, or the
                        // immediate parent if there is none.
                        NamedObj container = (NamedObj) argument.getContainer();
                        String moml =
                            "<deleteProperty name=\""
                            + argument.getName()
                            + "\"/>\n";

                        ChangeRequest request =
                            new MoMLChangeRequest(this, container, moml);
                        container.addChangeListener(this);
                        container.requestChange(request);
                    }
                }
            }
        } else if (buttonPressed().equals("Help")) {
            // Documentation used by classes should be in a subpackage
            // of the class so that it is easier to ship the class.
            // Having the documentation in a different package hierarchy
            // adds package dependencies, which makes it harder to ship
            // packages.
            URL toRead =
                getClass().getClassLoader().getResource(
                        "ptolemy/actor/gui/doc/argDialog.htm");
            if (toRead != null && configuration != null) {
                try {
                    configuration.openModel(
                            null,
                            toRead,
                            toRead.toExternalForm());
                } catch (Exception ex) {
                    MessageHandler.error("Help screen failure", ex);
                }
            } else {
                MessageHandler.error("No help available.");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify the listener that a change has been successfully executed.
     *  @param change The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {
        // Ignore if this is not the originator.
        if (change == null || change.getSource() != this)
            return;
        // Open a new dialog.
        try {
            new ArgumentConfigurerDialog(_owner, _target, _configuration);

        } catch (Exception e) {
            MessageHandler.error("TRT error !", e);
        }

        _target.removeChangeListener(this);
    }

    /** Notify the listener that a change has resulted in an exception.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Ignore if this is not the originator.
        if (change == null || change.getSource() != this)
            return;

        _target.removeChangeListener(this);

        if (!change.isErrorReported()) {
            MessageHandler.error("Change failed: ", exception);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If the window is closed with anything but Cancel, apply the changes.
     */
    protected void _handleClosing() {
        super._handleClosing();
        if (!buttonPressed().equals("Cancel")) {
            ((ArgumentConfigurer) contents).apply();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /** Open a dialog to add a new argument.
     *  @param message A message to place at the top, or null if none.
     *  @param name The default name.
     *  @param defValue The default value.
     *  @param cType The default c type.
     *  @return The dialog that is created.
     */
    private ComponentDialog _openAddDialog(
            String message,
            String name,
            String defValue,
            String cType)
            throws IllegalActionException, NameDuplicationException {
        // Create a new dialog to add an argument, then open a new
        // ArgumentConfigurerDialog.
        //name = "new ";
        Set optionsDefault = new HashSet();
        _query = new Query();
        if (message != null)
            _query.setMessage(message);
        _query.addLine(name + "Name", name + "Name", name);
        _query.addLine(name + "CType", name + "C or C++ Type", cType);
        _query.addSelectButtons(
                name + "Kind",
                name + "Kind",
                _optionsArray,
                optionsDefault);

        ComponentDialog dialog =
            new ComponentDialog(
                    _owner,
                    "Add a new argument to " + _target.getFullName(),
                    _query,
                    null);
        // If the OK button was pressed, then queue a mutation
        // to create the parameter.
        // A blank property name is interpreted as a cancel.
        String newName = _query.getStringValue(name + "Name");
        String newCType = _query.getStringValue(name + "CType");
        String newKind = _query.getStringValue(name + "Kind");
        if (dialog.buttonPressed().equals("OK")
                && !newName.equals("")
                && !newCType.equals("")&& !newKind.equals("")) {
            //set name
            Argument argument = _target.getArgument(newName);
            if (argument == null) {
                argument = new Argument(_target, newName);
                argument.setName(newName);
            } else {
                MessageHandler.error("This name is already used !");
            }
            argument.setKind(newKind.trim());
            argument.setCType(newCType.trim());
            argument.setExpression();
            String moml =
                "<property name=\""
                + argument.getName()
                + "\" value=\""
                + argument.getExpression()
                + "\""
                + " class=\""
                + "jni.Argument"
                + "\"/>";
            _target.addChangeListener(this);
            _target.requestChange(new MoMLChangeRequest(this, _target, moml));
        }
        return dialog;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /** The configuration.
     */
    private Configuration _configuration;

    /** Button labels.
     */
    private static String[] _moreButtons =
    { "Commit", "Add", "Remove", "Help", "Cancel" };

    /** The owner window.
     */
    private Frame _owner;

    /** The query window for adding arguments.
     */
    private Query _query;

    /** The target object whose arguments are being configured.
     */
    private GenericJNIActor _target;

    /** Possible configurations.
     */
    private String[] _optionsArray = { "input", "ouput", "return" };
}
