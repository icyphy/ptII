/* A basic context menu for objects in ptolemy.

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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

// Ptolemy imports.
import ptolemy.actor.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.gui.style.*;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.gui.*;
import ptolemy.moml.*;

// Diva imports.
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*;
import diva.graph.model.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import diva.util.Filter;

// Java imports.
import java.awt.geom.Rectangle2D;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// BasicContextMenu
/**
This is a base class for context menus for most objects in the vergil
editor.  It contains a single action, for editing the parameters of
a named object.  This action creates configuration widgets using
the Configurer class.

This class also contains methods that make it simple to add new actions to
this menu.

@author Steve Neuendorffer
@version $Id$
*/
public class BasicContextMenu extends JPopupMenu {

    /**
     * Create a new context menu with a reference to the given application.
     * The context of the menu, which is used to alter the functions of this 
     * menu is the given named object.  Call the initialize method to populate
     * this menu.
     */
    public BasicContextMenu(Application application, NamedObj target) {
	super(target.getFullName());
	_target = target;
	_application = application;
	initialize();
    }

    /** Add an action to this menu and return the menu item created.  If
     * the tool tip is null, use the "tooltip" property already in the
     * action, otherwise add the property to the action.
     * The new menu item is added to the action as the
     * "menuItem" property.  The menu item's text is set using the
     * action's name and is enabled by default.
     */
    public JMenuItem add(Action action, String tooltip) {
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
    public JMenuItem add(Action action,
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

    public Application getApplication() {
	return _application;
    }

    public NamedObj getTarget() {
	return _target;
    }

    /** Populate this menu with items appropriate to its target.  This method
     * is intended to be overridden by subclasses to change the items in the
     * menu.  It is called automatically by the constructor in thie base class.
     */
    protected void initialize() {
	Action action;
	String name = "Edit Parameters";
	action = new AbstractAction(name) {
	    public void actionPerformed(ActionEvent e) {

		// Create a dialog for configuring the object.
                try {
                    Configurer panel = new Configurer(_target);
                    // FIXME: First argument below should be a parent window
                    // (a JFrame).
                    ComponentDialog dialog = new ComponentDialog(
                            null,
                            "Edit parameters for " + _target.getName(),
                            panel);
                    if (!(dialog.buttonPressed().equals("OK"))) {
                        // Restore original parameter values.
                        panel.restore();
                    }
                } catch (IllegalActionException ex) {
                    _application.showError("Edit Parameters failed", ex);
                }
	    }
	};
	add(action, name);

	name = "Edit Parameter Styles";
	action = new AbstractAction(name) {
	    public void actionPerformed(ActionEvent e) {

		// Create a dialog for configuring the object.
                try {
                    StyleConfigurer panel = new StyleConfigurer(_target);
                    // FIXME: First argument below should be a parent window
                    // (a JFrame).
                    ComponentDialog dialog = new ComponentDialog(
                            null,
                            "Edit parameter styles for " + _target.getName(),
                            panel);
                    if (!(dialog.buttonPressed().equals("OK"))) {
                        // Restore original parameter values.
                        panel.restore();
                    }
                } catch (IllegalActionException ex) {
		    _application.showError("Edit Parameter Style failed", ex);
                }
	    }
	};
	add(action, name);
    }

    final private Application _application;
    final private NamedObj _target;
}
