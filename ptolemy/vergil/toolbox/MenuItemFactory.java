/* A Factory for Popup menus

 Copyright (c) 2000-2001 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import javax.swing.*;
import diva.canvas.*;
import diva.gui.toolbox.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// MenuItemFactory
/**
 * A factory for popup menus.  Use this class in conjuction with
 * a MenuCreator to implement context sensitive menus.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Id$
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
