/* A top-level dialog window for configuring the ports of an entity.

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
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.gui.ComponentDialog;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Query;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;

import java.awt.Frame;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// PortConfigurerDialog
/**
This class is a modal dialog box for configuring the ports of an entity.
An instance of this class contains an instance of PortConfigurer.
The dialog is modal, so the statement that creates the dialog will
not return until the user dismisses the dialog.

@see PortConfigurer
@author Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class PortConfigurerDialog extends ComponentDialog
    implements ChangeListener {

    /** Construct a dialog with the specified owner and target.
     *  Several buttons are added to the dialog.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param target The object whose ports are being configured.
     *  @param configuration The configuration to use to open the
     *   help screen (or null if help is not supported).
     */
    public PortConfigurerDialog(Frame owner,
            Entity target,
            Configuration configuration) {
        super(owner,
                "Configure ports for " + target.getName(),
                new PortConfigurer(target),
                _moreButtons);
        // Once we get to here, the dialog has already been dismissed.
        _configuration = configuration;
        _owner = owner;
        _target = target;
        if (buttonPressed().equals("Add")) {
            _openAddDialog(null, "", "", "");
            _target.removeChangeListener(this);
        } else if (buttonPressed().equals("Remove")) {
            // Create a new dialog to remove a port then open a new
            // PortConfigurerDialog.
            // First, create a string array with the names of all the
            // ports.
            List portList = _target.portList();
            String[] portNames = new String[portList.size()];
            Iterator ports = portList.iterator();
            int index = 0;
            while (ports.hasNext()) {
                Port port = (Port)ports.next();
                portNames[index++] = port.getName();
            }
            Query query = new Query();
            query.addChoice("delete", "Port to delete",
                    portNames, null, false);

            ComponentDialog dialog = new ComponentDialog(
                    _owner,
                    "Delete a port for " + _target.getFullName(),
                    query,
                    null);
            // If the OK button was pressed, then queue a mutation
            // to delete the port.
            if (dialog.buttonPressed().equals("OK")) {

                String portName = query.getStringValue("delete");
                if (portName != null) {
                    Port port = _target.getPort(portName);

                    if (port != null) {
                        // The context for the MoML should be the first
                        // container above this port in the hierarchy
                        // that defers its MoML definition, or the
                        // immediate parent if there is none.
                        NamedObj container
                            = MoMLChangeRequest.getDeferredToParent(port);
                        if (container == null) {
                            container = (NamedObj)port.getContainer();
                        }

                        NamedObj composite = (NamedObj)container.getContainer();
                        StringBuffer moml = new StringBuffer();
                        if (composite != null) {
                            moml.append("<deletePort name=\"" + port.getName() +
                                    "\" entity=\"" + container.getName() +
                                    "\" />\n");
                        } else {
                            moml.append("<deletePort name=\"" +
                                    port.getName(container) + "\" />\n");
                        }

                        // NOTE: the context is the composite entity containing
                        // the entity if possible
                        MoMLChangeRequest request = null;
                        if (composite != null) {
                            request = new MoMLChangeRequest(this, composite,
                                    moml.toString());
                        } else {
                            request = new MoMLChangeRequest(this, container,
                                    moml.toString());
                        }
                        request.setUndoable(true);
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
            URL toRead = getClass().getClassLoader().getResource(
                    "ptolemy/actor/gui/doc/portDialog.htm");
            if (toRead != null && configuration != null) {
                try {
                    configuration.openModel(
                            null,  toRead, toRead.toExternalForm());
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
        if (change == null || change.getSource() != this) return;

        // Open a new dialog.
        new PortConfigurerDialog(
                _owner, _target, _configuration);

        _target.removeChangeListener(this);
    }

    /** Notify the listener that a change has resulted in an exception.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Ignore if this is not the originator.
        if (change == null || change.getSource() != this) return;

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
            ((PortConfigurer)contents).apply();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Open a dialog to add a new port.
     *  @param message A message to place at the top, or null if none.
     *  @param name The default name.
     *  @param defValue The default value.
     *  @param className The default class name.
     *  @return The dialog that is created.
     */
    private ComponentDialog _openAddDialog(
            String message, String name, String defValue, String className) {
        // Create a new dialog to add a port, then open a new
        // PortConfigurerDialog.
        _query = new Query();
        if (message != null) _query.setMessage(message);
        _query.addLine("name", "Name", name);
        _query.addLine("class", "Class", className);
        ComponentDialog dialog = new ComponentDialog(
                _owner,
                "Add a new port to " + _target.getFullName(),
                _query,
                null);
        // If the OK button was pressed, then queue a mutation
        // to create the parameter.
        // A blank property name is interpreted as a cancel.
        String newName = _query.getStringValue("name");

        String classMoML = "";
        String classSpec = _query.getStringValue("class");
        if (!classSpec.trim().equals("")) {
            classMoML = " class=\"" + classSpec + "\"";
        }
        if (dialog.buttonPressed().equals("OK") && !newName.equals("")) {
            String moml = "<port name=\""
                + newName
                + "\""
                + classMoML
                + "/>";
            _target.addChangeListener(this);
            MoMLChangeRequest change = new MoMLChangeRequest(this,
                    _target, moml);
            change.setUndoable(true);
            _target.requestChange(change);
        }
        return dialog;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The configuration.
    private Configuration _configuration;

    // Button labels.
    private static String[] _moreButtons
    = {"Commit", "Add", "Remove", "Help", "Cancel"};

    // The owner window.
    private Frame _owner;

    // The query window for adding parameters.
    private Query _query;

    // The target object whose ports are being configured.
    private Entity _target;
}
