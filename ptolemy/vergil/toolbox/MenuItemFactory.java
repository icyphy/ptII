/* An interface for factories that create menu items.

 Copyright (c) 2000-2003 The Regents of the University of California.
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

import diva.gui.toolbox.JContextMenu;

import ptolemy.kernel.util.NamedObj;

import javax.swing.JMenuItem;

//////////////////////////////////////////////////////////////////////////
//// MenuItemFactory
/** 
This is an interface for factories that create menu items.
Objects that implement this interface can be used in conjunction
with a MenuCreator to implement context menu items.
    
@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public interface MenuItemFactory {

    /** Add an item to the given context menu that will operate on
     *  specified target, and return the menu item.  Return null to
     *  decline to add a menu item for the specified target.
     *  @param menu The context menu to add to.
     *  @param target The object that the menu item command will operate on.
     *  @return A menu item, or null to decline to provide a menu item.
     */
    public abstract JMenuItem create(JContextMenu menu, NamedObj target);
}
