/*
 * $Id$
 *
 * Copyright (c) 2000 The Regents of the University of California.
 * All rights reserved.  See the file COPYRIGHT for details.
 */
package ptolemy.vergil.toolbox;

import javax.swing.*;
import diva.canvas.*;
import diva.gui.toolbox.*;
import ptolemy.kernel.util.*;

/**
 * A factory for popup menus.  Use this class in conjuction with
 * a MenuCreator to implement context sensitive menus.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision$
 */
public abstract class MenuItemFactory {
    /**
     * Add an item to the given context menu.  The menu item should act on the 
     * given figure.
     */
    public abstract JMenuItem create(JContextMenu menu, NamedObj object);

    /**
     * Get the name of the items that will be created.  This is provided so
     * that factory can be overriden slightly with the name changed.
     */
    protected abstract String _getName();

    /**
     * Given the object that is the target of the MenuFactory, return a new
     * object that is related to the original.  This is provided so that
     * the target of a menu item can be different from the target of the 
     * menu that it is contained in.  In this base class, just return the
     * object that is passed in, since menu items will usually have the
     * same target as the menu.
     */
    protected NamedObj _getItemTargetFromMenuTarget(NamedObj object) {
	return object;
    }
}
