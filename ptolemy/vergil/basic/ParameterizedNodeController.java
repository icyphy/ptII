/* The node controller for objects that offer a configure command.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

package ptolemy.vergil.basic;

import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.ActionInteractor;
import diva.canvas.interactor.SelectionModel;
import diva.graph.GraphController;
import diva.graph.NodeInteractor;
import diva.gui.toolbox.MenuCreator;

import ptolemy.vergil.toolbox.ConfigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;

//////////////////////////////////////////////////////////////////////////
//// ParameterizedNodeController
/**
This class provides interaction with nodes that represent Ptolemy II
components with parameters.  It provides a context menu item labeled
"Configure" for editing those parameters, and binds double click
to invoke the dialog that edits those parameters.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class ParameterizedNodeController extends NamedObjController {

    /** Create an attribute controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public ParameterizedNodeController(GraphController controller) {
        super(controller);

        // Add a menu creator.
        _menuCreator = new MenuCreator(null);
        // FIXME: Why doesn't getNodeInteractor() return a NodeInteractor?
        NodeInteractor interactor = (NodeInteractor)getNodeInteractor();
        interactor.addInteractor(_menuCreator);

        // The contents of the menu is determined by the associated
        // menu factory, which is a protected member of this class.
        // Derived classes can add menu items to it.
        _menuFactory = new PtolemyMenuFactory(controller);

        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(_configureAction));
        _menuCreator.setMenuFactory(_menuFactory);

        // Add a double click interactor.
        ActionInteractor doubleClickInteractor
            = new ActionInteractor(_configureAction);
        doubleClickInteractor.setConsuming(false);
        doubleClickInteractor.setMouseFilter(new MouseFilter(1, 0, 0, 2));

        interactor.addInteractor(doubleClickInteractor);
        // NOTE: This dance is so that the
        // doubleClickInteractor gets the events before the drag interactor.
        interactor.setDragInteractor(interactor.getDragInteractor());

        // Set the selection model to allow this to be independently selected.
        SelectionModel sm = controller.getSelectionModel();
        interactor.setSelectionModel(sm);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected members                     ////

    /** The factory belonging to the menu creator. */
    protected PtolemyMenuFactory _menuFactory;

    /** The menu creator. */
    protected MenuCreator _menuCreator;

    /** The configure action, which handles edit parameters requests. */
    protected static ConfigureAction _configureAction
            = new ConfigureAction("Configure");
}
