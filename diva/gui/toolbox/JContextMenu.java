/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui.toolbox;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * An improved version of a popup menu that works well for context menus.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision$
 */
public class JContextMenu extends JPopupMenu {
    /**
     * Create a popup menu without an "invoker".  (Whatever the hell that
     * means... I'm just copying this from the jdk1.2.2 docs)  The menu
     * will be created with the given target.
     */
    public JContextMenu(Object target) {
        super();
        _target = target;
    }

    /**
     * Create a popup menu with the given target and the given title.
     */
    public JContextMenu(Object target, String title) {
        super(title);
        _target = target;
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

    /** Add an action to the given menu and return the menu item created.  If
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

    /**
     * The object that this context menu was created on.
     */
    public Object getTarget() {
        return _target;
    }

    private Object _target;
}


