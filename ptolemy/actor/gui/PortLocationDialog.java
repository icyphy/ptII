/* A top-level dialog window for configuring the port locations of an entity.

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

import java.awt.Frame;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// PortLocationDialog
/**
This class is a modal dialog box for configuring the port locations of
an entity. An instance of this class contains an instance of
PortLocationConfigurer.  The dialog is modal, so the statement that
creates the dialog will not return until the user dismisses the
dialog.

@see PortLocationConfigurer
@author Mason Holding, Contributor: Christopher Hylands
@version $Id$
@since Ptolemy II 2.1
*/
public class PortLocationDialog extends ComponentDialog
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
    public PortLocationDialog(Frame owner,
            Entity target,
            Configuration configuration) {
        super(owner,
                "Place ports for " + target.getName(),
                new PortLocationConfigurer(target),
                _moreButtons);
        // Once we get to here, the dialog has already been dismissed.
        _configuration = configuration;
        _owner = owner;
        _target = target;
        if (buttonPressed().equals("Help")) {
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
                    MessageHandler.error("Help screen failure: "
                            + ex.toString());
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
        if (change == null || change.getSource() != this) {
            return;
        }
        // Open a new dialog.
        PortLocationDialog dialog = new PortLocationDialog(
                _owner, _target, _configuration);

        _target.removeChangeListener(this);
    }

    /** Notify the listener that a change has resulted in an exception.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Ignore if this is not the originator.
        if (change == null || change.getSource() != this) {
            return;
        }
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
            ((PortLocationConfigurer)contents).apply();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The configuration.
    private Configuration _configuration;

    // Button labels.
    private static String[] _moreButtons
    = {"Commit", "Help", "Cancel"};

    // The owner window.
    private Frame _owner;

    // The target object whose ports are being configured.
    private Entity _target;
}
