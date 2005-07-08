/* A menu item factory that opens a dialog to rename an object.

 Copyright (c) 1999-2005 The Regents of the University of California.
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

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;

import ptolemy.actor.gui.RenameDialog;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.toolbox.MenuItemFactory;
import diva.gui.toolbox.JContextMenu;

//////////////////////////////////////////////////////////////////////////
//// RenameDialogFactory

/**
 A factory that creates a dialog to rename an object.

 @author Edward A. Lee and Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class RenameDialogFactory implements MenuItemFactory {
    /** Add an item to the given context menu that will open a dialog
     *  to add or remove ports from an object.
     *  @param menu The context menu.
     *  @param object The object whose ports are being manipulated.
     */
    public JMenuItem create(final JContextMenu menu, NamedObj object) {
        String name = "Customize Name";

        // Removed this method since it was never used. EAL
        // final NamedObj target = _getItemTargetFromMenuTarget(object);
        final NamedObj target = object;

        // ensure that we actually have a target.
        if (target == null) {
            return null;
        }

        Action action = new AbstractAction(name) {
            public void actionPerformed(ActionEvent e) {
                // Create a dialog for configuring the object.
                // First, identify the top parent frame.
                // Normally, this is a Frame, but just in case, we check.
                // If it isn't a Frame, then the edit parameters dialog
                // will not have the appropriate parent, and will disappear
                // when put in the background.
                Component parent = menu.getInvoker();

                while (parent.getParent() != null) {
                    parent = parent.getParent();
                }

                if (parent instanceof Frame) {
                    new RenameDialog((Frame) parent, target);
                } else {
                    new RenameDialog(null, target);
                }
            }
        };

        return menu.add(action, name);
    }
}
