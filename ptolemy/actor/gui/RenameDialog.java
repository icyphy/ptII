/* A top-level dialog window for renaming objects.

Copyright (c) 1998-2005 The Regents of the University of California.
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

import ptolemy.gui.ComponentDialog;
import ptolemy.kernel.util.NamedObj;


//////////////////////////////////////////////////////////////////////////
//// RenameDialog

/**
   This class is a modal dialog box for renaming an object.
   The dialog is modal, so the statement that creates the dialog will
   not return until the user dismisses the dialog.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (eal)
*/
public class RenameDialog extends ComponentDialog {
    /** Construct a dialog with the specified owner and target.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog, or null if none.
     *  @param target The object being renamed.
     */
    public RenameDialog(Frame owner, NamedObj target) {
        super(owner, "Rename " + target.getName(),
                new RenameConfigurer(target), _buttons);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** If the window is closed with anything but Cancel, apply the changes.
     */
    protected void _handleClosing() {
        super._handleClosing();

        if (!buttonPressed().equals("Cancel")) {
            ((RenameConfigurer) contents).apply();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Button labels.
    private static String[] _buttons = {
        "Commit",
        "Cancel"
    };
}
