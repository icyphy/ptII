/* A menu item factory that opens a dialog for adding arguments.
   Largely inspired by PortDialogFactory

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
package jni;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.toolbox.MenuItemFactory;

import diva.gui.toolbox.JContextMenu;

import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;


//////////////////////////////////////////////////////////////////////////
//// ArgumentDialogFactory

/**
   A factory that creates a dialog to configure, add, or remove Arguments
   from objects.

   @author Edward A. Lee and Steve Neuendorffer, modified by Vincent Arnould
   @version $Id$
   @since Ptolemy II 2.3
   @Pt.ProposedRating Red (vincent.arnould)
   @Pt.AcceptedRating Red (vincent.arnould)
*/
public class ArgumentDialogFactory implements MenuItemFactory {
    /** Construct an ArgumentDialogFactory. */
    public ArgumentDialogFactory() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an item to the given context menu that will open a dialog
     *  to add or remove arguments from an object.
     *  @param menu The context menu.
     *  @param object The object whose ports are being manipulated.
     *  @return The JMenuItem that was created or null if the object
     *  is not a GenericJNIActor.
     */
    public JMenuItem create(final JContextMenu menu, NamedObj object) {
        String name = "Configure Arguments";

        // Removed this method since it was never used. EAL
        // final NamedObj target = _getItemTargetFromMenuTarget(object);
        final NamedObj target = object;

        // ensure that we actually have a target, and that it's an Entity.
        if (!(target instanceof GenericJNIActor)) {
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
                        //TRT begin changes
                        new ArgumentConfigurerDialog((Frame) parent,
                            (Entity) target, _configuration);
                    } else {
                        System.out.println("No Frame");

                        new ArgumentConfigurerDialog(null, (Entity) target,
                            _configuration);

                        //TRT end
                    }
                }
            };

        return menu.add(action, name);
    }

    /** Set the configuration for use by the help screen.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The configuration.
     */
    private Configuration _configuration;
}
