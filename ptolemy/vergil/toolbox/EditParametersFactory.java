/* A menu item factory that creates actions for editing parameters.

 Copyright (c) 1999-2002 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import diva.gui.toolbox.JContextMenu;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.kernel.util.*;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// EditParametersFactory
/**
A factory that adds an action to the given context menu that will configure
parameters on the given object.  If that object contains an attribute that
is an instance of EditorFactory, then that instance is used to create
the dialog (or whatever) to configure the object.  Otherwise, an instance
of EditParametersDialog is created.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
@see ptolemy.actor.gui.EditParametersDialog
*/
public class EditParametersFactory implements MenuItemFactory {

    /** Construct a factory with the default command name, "Edit Parameters"
     *  in the default workspace, with no container.
     *  The command name will appear in the menus.
     */
    public EditParametersFactory() {
        this("Edit Parameters");
    }

    /** Construct a factory with the specified command name in the
     *  default workspace, with no container. The name
     *  will appear in a menu.
     *  @param name The command name.
     */
    public EditParametersFactory(String name) {
        _name = name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an item to the given context menu that will configure the
     *  parameters on the given target.     *  @param menu The context menu.
     *  @param object The object whose parameters are being configured.
     */
    public JMenuItem create(final JContextMenu menu, NamedObj object) {

        // Removed this method since it was never used. EAL
	// final NamedObj target = _getItemTargetFromMenuTarget(object);
        final NamedObj target = object;

	// ensure that we actually have a target.
	if (target == null) return null;
	Action action = new AbstractAction(_name) {
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
                    openDialog((Frame)parent, target);
                } else {
                    openDialog(null, target);
                }
	    }
	};
	return menu.add(action, _name);
    }

    /** Open an edit parameters dialog.  This is a modal dialog, so
     *  this method returns only after the dialog has been dismissed.
     *  @param parent A frame to serve as a parent for the dialog,
     *   or null if there is none.
     *  @param target The object whose parameters are to be edited.
     */
    public void openDialog(Frame parent, NamedObj target) {
        List attributeList = target.attributeList(EditorFactory.class);
        if (attributeList.size() > 0) {
            EditorFactory factory = (EditorFactory)attributeList.get(0);
            factory.createEditor(target, parent);
        } else {
            new EditParametersDialog(parent, target);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The name of this factory.
    private String _name;
}
