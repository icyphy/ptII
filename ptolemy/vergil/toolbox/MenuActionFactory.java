/* A menu item factory that creates actions for firing actions

Copyright (c) 2000-2005 The Regents of the University of California.
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
package ptolemy.vergil.toolbox;

import javax.swing.Action;
import javax.swing.JMenuItem;

import ptolemy.kernel.util.NamedObj;
import diva.gui.toolbox.JContextMenu;


//////////////////////////////////////////////////////////////////////////
//// MenuActionFactory

/**
   A factory that adds a given action a given context menu.

   @author Steve Neuendorffer
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (johnr)
*/
public class MenuActionFactory implements MenuItemFactory {
    /** Construct a factory that adds a given action to a given context menu.
     *  @param action The action to be associated with the context menu.
     */
    public MenuActionFactory(Action action) {
        _action = action;
    }

    /**
     * Add an item to the given context menu that will configure the
     * parameters on the given target.
     */
    public JMenuItem create(JContextMenu menu, NamedObj object) {
        return menu.add(_action, (String) _action.getValue(Action.NAME));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The action that will be added to the context menu. */
    private Action _action;
}
