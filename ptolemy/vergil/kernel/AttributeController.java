/* The node controller for icons of attributes

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.vergil.kernel;

import ptolemy.vergil.basic.IconController;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MoveAction;

import diva.graph.GraphController;


//////////////////////////////////////////////////////////////////////////
//// AttributeController

/**
   This class provides interaction with nodes that represent Ptolemy II
   attributes.  It provides a double click binding and context menu
   entry to edit the parameters of the node ("Configure") and a
   command to get documentation.
   It can have one of two access levels, FULL or PARTIAL.
   If the access level is FULL, the the context menu also
   contains a command to rename the node.

   @author Steve Neuendorffer and Edward A. Lee
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (johnr)
*/
public class AttributeController extends IconController {
    /** Create an attribute controller associated with the specified graph
     *  controller.  The attribute controller is given full access.
     *  @param controller The associated graph controller.
     */
    public AttributeController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create an attribute controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public AttributeController(GraphController controller, Access access) {
        super(controller);

        if (access == FULL) {
            // Add to the context menu.
            _menuFactory.addMenuItemFactory(new RenameDialogFactory());
            _menuFactory.addMenuItemFactory(new MenuActionFactory(
                                                    new GetDocumentationAction()));
            _menuFactory.addMenuItemFactory(new MenuActionFactory(
                                                    new MoveAction("Move to first (back)", MoveAction.TO_FIRST)));

            /* The following doesn't work, and seems confusing anyway.
               _menuFactory.addMenuItemFactory(
               new MenuActionFactory(
               new MoveAction("Move towards first", MoveAction.UP)));
            */
            _menuFactory.addMenuItemFactory(new MenuActionFactory(
                                                    new MoveAction("Move to last (front)", MoveAction.TO_LAST)));

            /* The following doesn't work, and seems confusing anyway.
               _menuFactory.addMenuItemFactory(
               new MenuActionFactory(
               new MoveAction("Move towards last", MoveAction.DOWN)));
            */
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public members                        ////

    /** Indicator to give full access to the attribute. */
    public static final Access FULL = new Access();

    /** Indicator to give partial access to the attribute. */
    public static final Access PARTIAL = new Access();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A static enumerator for constructor arguments. */
    protected static class Access {
    }
}
