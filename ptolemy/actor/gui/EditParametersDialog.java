/* A top-level dialog window for editing parameters of a NamedObj.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
import ptolemy.kernel.util.NamedObj;

import java.awt.Frame;

//////////////////////////////////////////////////////////////////////////
//// EditParametersDialog
/**
This class is a modal dialog box for editing the parameters of a target
object, which is an instance of NamedObj. It contains an instance of
Configurer, which examines the target for attributes of type
EditorPaneFactory.  Those attributes, if they are present, define
the panels that are used to edit the parameters of the target.
If they are not present, then a default panel is created.
<p>
If the panels returned by EditorPaneFactory implement the
CloseListener interface, then they are notified when this dialog
is closed, and are informed of which button (if any) was used to
close the dialog.
<p>
The dialog is modal, so the statement that creates the dialog will
not return until the user dismisses the dialog.  The method buttonPressed()
can then be called to find out whether the user clicked the OK button
or the Cancel button (or any other button specified in the constructor).
Then you can access the component to determine what values were set
by the user.

@author Edward A. Lee
@version $Id$
*/
public class EditParametersDialog extends ComponentDialog {

    /** Construct a dialog with the specified owner and target.
     *  An "OK" and a "Cancel" button are added to the dialog.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param target The object whose parameters are being edited.
     */
    public EditParametersDialog(Frame owner, NamedObj target) {
        super(owner,
             "Edit parameters for " + target.getName(),
             new Configurer(target),
             null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If the contents of this dialog implements the CloseListener
     *  interface, then notify it that the window has closed.
     */
    protected void _handleClosing() {
        super._handleClosing();
        if (!(buttonPressed().equals("OK"))) {
            // Restore original parameter values.
            ((Configurer)contents).restore();
        }
    }
}
