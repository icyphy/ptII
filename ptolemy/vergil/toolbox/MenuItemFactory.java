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
}
