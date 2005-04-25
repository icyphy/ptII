/* A top-level dialog window for configuring the breakpoints of an entity.

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
package ptolemy.vergil.debugger;

import ptolemy.actor.gui.QueryUtilities;
import ptolemy.gui.ComponentDialog;
import ptolemy.kernel.Entity;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphController;

import java.awt.Frame;


//////////////////////////////////////////////////////////////////////////
//// BreakpointConfigurerDialog

/**
   A top-level dialog window for configuring the breakpoints of an
   entity.  An instance of this class contains an instance of
   BreakpointConfigurer.  The dialog is modal, so the statement that
   creates the dialog will not return until the user dismisses the
   dialog.

   @see ptolemy.actor.gui.PortConfigurerDialog
   @see BreakpointConfigurer

   @author Elaine Cheong
   @version $Id$
   @since Ptolemy II 2.1
   @Pt.ProposedRating Red (celaine)
   @Pt.AcceptedRating Red (celaine)
*/
public class BreakpointConfigurerDialog extends ComponentDialog {
    /** Construct a dialog with the specified owner and target.
     *  The dialog is placed relative to the owner.
     *  @param owner The object that, per the user, appears to be
     *   generating the dialog.
     *  @param target The object whose breakpoints are being configured.
     *  @param graphController The GraphController associated with the
     *  target.
     */
    public BreakpointConfigurerDialog(Frame owner, Entity target,
            BasicGraphController graphController) {
        super(owner, "Configure breakpoints for " + target.getName(),
                new BreakpointConfigurer(target, graphController), _moreButtons);

        // Once we get to here, the dialog has already been dismissed.
        if (buttonPressed().equals("Help")) {
            QueryUtilities.openHTMLResource("ptolemy/vergil/debugger/breakpoints.htm",
                    owner);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Apply the changes if the window is closed with anything but
     *  Cancel.
     */
    protected void _handleClosing() {
        super._handleClosing();

        if (!buttonPressed().equals("Cancel")
                && !buttonPressed().equals("Help")) {
            try {
                ((BreakpointConfigurer) contents).apply();
            } catch (Throwable throwable) {
                MessageHandler.error("Failed to handle closing of breakpoint "
                        + "dialog.", throwable);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Button labels.
    private static String[] _moreButtons = {
        "OK",
        "Cancel",
        "Help"
    };
}
