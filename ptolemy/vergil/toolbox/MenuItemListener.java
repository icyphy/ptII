/* An interface for listeners that are invoked when a menu item is created.

@Copyright (c) 2008 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.vergil.toolbox;

import javax.swing.JMenuItem;

import diva.gui.toolbox.JContextMenu;
import ptolemy.kernel.util.NamedObj;

/**
 An interface for listeners that are invoked when a menu item is created.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @see MenuActionFactory
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public interface MenuItemListener {

    /** The method to be invoked when a menu item is created, so that this
     *  listener can modify the menu item if it needs to.
     *
     *  @param menu The menu context.
     *  @param object The object for which the menu item is created.
     *  @param menuItem The created menu item.
     */
    public void menuItemCreated(JContextMenu menu, NamedObj object,
            JMenuItem menuItem);
}
