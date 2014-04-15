/* An action that opens a dialog to rename an object.

 Copyright (c) 2006-2013 The Regents of the University of California.
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
package ptolemy.vergil.kernel;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import ptolemy.actor.gui.RenameDialog;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.FigureAction;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// RenameDialogAction

/**
 An action that creates a dialog to rename an object.

 @author Edward A. Lee and Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
@SuppressWarnings("serial")
public class RenameDialogAction extends FigureAction {

    /**
     * Construct a rename dialog action with the specified parent, which will
     * appear in the menu that uses this action.
     *
     * @param parent
     *            The parent TableauFrame
     */
    public RenameDialogAction(TableauFrame parent) {
        super("");
        if (parent == null) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "ActorDialogAction constructor received NULL argument for TableauFrame");
            iae.fillInStackTrace();
            throw iae;
        }
        //this.parent = parent;

        putValue(Action.NAME, DISPLAY_NAME);
        // Findbugs was reporting that the Field was only ever set to null.
        putValue(GUIUtilities.LARGE_ICON, null /*LARGE_ICON*/);
        putValue("tooltip", TOOLTIP);

        // Findbugs was reporting that the Field was only ever set to null.
        //putValue(GUIUtilities.ACCELERATOR_KEY, ACCELERATOR_KEY);
        putValue(GUIUtilities.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));


        DISPLAY_NAME = "Configure";
        TOOLTIP = "Change Settings for Actor";

    }

    /** Construct a rename dialog action with the specified name,
     *  which will appear in the menu that uses this action.
     *  @param name The name.
     */
    public RenameDialogAction(String name) {
        super(name);

        putValue(GUIUtilities.ACCELERATOR_KEY,
                KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Open a dialog to rename the target.
     *  @param event The action event.
     */
    public void actionPerformed(ActionEvent event) {
        try {
            // Determine which entity was selected for the look inside action.
            super.actionPerformed(event);
            NamedObj target = getTarget();
            if (target == null) {
                return;
            }
            // Create a dialog for configuring the object.
            // First, identify the top parent frame.
            Frame parent = getFrame();
            new RenameDialog(parent, target);
        } catch (Throwable throwable) {
            // Giotto code generator on giotto/demo/Hierarchy/Hierarchy.xml
            // was throwing an exception here that was not being displayed
            // in the UI.
            MessageHandler.error(
                    "Failed to open a dialog to rename the target.", throwable);
        }
    }

    //private TableauFrame parent;
    private static String DISPLAY_NAME = null;
    private static String TOOLTIP = null;
    //private static ImageIcon LARGE_ICON = null;
    //private static KeyStroke ACCELERATOR_KEY = null;
}
