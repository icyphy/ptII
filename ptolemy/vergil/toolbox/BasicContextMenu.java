/* A basic context menu for objects in ptolemy.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.gui.*;
import ptolemy.moml.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*; 
import diva.graph.model.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import java.awt.geom.Rectangle2D;
import diva.util.Filter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// BasicContextMenu
/**
This is a base class for context menus for most objects in the ptolemy
editor.  It contains a single action, for editing the parameters of 
a named object.  This action creates a configurer consistent with the 
object.  If the object does not specify a configurer, then an
instance of ParameterEditor is used.

This class also contains methods that make it simple to add new actions to
this menu.

@author Steve Neuendorffer 
@version $Id$
*/   
public class BasicContextMenu extends JPopupMenu {
    public BasicContextMenu(NamedObj target) {
	super(target.getFullName());
	
	Action action;
	action = new AbstractAction ("Edit Parameters") {
	    public void actionPerformed(ActionEvent e) {
		// Create a dialog and attach the dialog values 
		// to the parameters of the object                    
	    NamedObj object = (NamedObj) getValue("target");
	    JFrame frame = new JFrame("Parameters for " + object.getName());
	    JPanel pane = (JPanel) frame.getContentPane();
	    Query query;
	    try {
		// FIXME What if this implements the WidgetConfigurable
		// interface?
		query = new ParameterEditor(object);
	    } catch (IllegalActionException ex) {
		ex.printStackTrace();
		throw new RuntimeException(ex.getMessage());
	    }
	    
	    pane.add(query);
	    frame.setVisible(true);
	    frame.pack();
	    }
	};
	
	action.putValue("target", target);
	add(action, "Edit Parameters");
    }

    /** Add an action to this menu and return the menu item created.  If
     * the tool tip is null, use the "tooltip" property already in the
     * action, otherwise add the property to the action. 
     * The new menu item is added to the action as the
     * "menuItem" property.  The menu item's text is set using the
     * action's name and is enabled by default.
     */
    public JMenuItem add (Action action, String tooltip) {
        String label = (String)action.getValue(action.NAME);
        return add(action, tooltip, label, true);
    }

    /** Add an action to this menu and return the menu item created.  If
     * the tool tip is null, use the "tooltip" property already in the
     * action, otherwise add the property to the action. 
     * The new menu item is added to the action as the
     * "menuItem" property.  The menu item's text is set to be "label",
     * and is disabled or enabled according to "isEnabled."
     */
    public JMenuItem add (Action action,
            String tooltip, String label, boolean isEnabled) {
        if (tooltip == null) {
            tooltip = (String) action.getValue("tooltip");
        } 
        action.putValue("tooltip", tooltip);
        JMenuItem item = add(action);
        item.setText(label);
        item.setEnabled(isEnabled);
	item.setToolTipText(tooltip);
        action.putValue("menuItem", item);
        return item;
    }
}
