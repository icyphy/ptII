/* The node controller for entities (and icons)

 Copyright (c) 1998-2001 The Regents of the University of California.
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

package ptolemy.vergil.ptolemy.fsm;

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.vergil.*;
import ptolemy.vergil.toolbox.*;
import ptolemy.vergil.ptolemy.*;
import ptolemy.gui.*;
import ptolemy.moml.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import java.awt.geom.Rectangle2D;
import diva.util.Filter;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.*;
import java.net.URL;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// FSMStateController
/**
This class provides interaction with nodes that represent Ptolemy II entities.
(Or, more specifically, with the icon that is contained in an entity.)
It contains a node controller for the ports that the entity contains, and when
it draws an entity, it defers to that controller to draw the ports.  The
figures for ports are automatically placed on the left and right side of the
figure for the entity.  Standard selection and movement interaction is
provided.  In addition, right clicking on the entity will create a context
menu for the entity.

@author Steve Neuendorffer
@version $Id$
*/
public class FSMStateController extends LocatableNodeController {

    /** Create an entity controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public FSMStateController(GraphController controller) {
	super(controller);
	setNodeRenderer(new StateRenderer());

	SelectionModel sm = controller.getSelectionModel();
        SelectionInteractor interactor =
            (SelectionInteractor) getNodeInteractor();
	interactor.setSelectionModel(sm);

	_menuCreator = new MenuCreator(null);
	interactor.addInteractor(_menuCreator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the menu factory that will create context menus for this
     *  controller.
     */
    public MenuFactory getMenuFactory() {
        return _menuCreator.getMenuFactory();
    }

    /** Set the menu factory that will create menus for this Entity.
     */
    public void setMenuFactory(MenuFactory factory) {
        _menuCreator.setMenuFactory(factory);
    }

    public static class StateRenderer implements NodeRenderer {
	public Figure render(Object n) {
	    Location location = (Location)n;
	    NamedObj object = (NamedObj) location.getContainer();

	    // FIXME: may want to use another type of icon
	    // FIXME: this code is the same as in PtolemyTreeCellRenderer.
	    EditorIcon icon;
            try {
                icon = (EditorIcon)object.getAttribute("_icon");
		if(icon == null) {
		    icon = new XMLIcon(object, "_icon");
		}
	    } catch (KernelException ex) {
		throw new InternalErrorException("could not create icon " +
                        "in " + object + " even " +
                        "though one did not exist");
	    }

	    Figure figure = icon.createFigure();
            figure.setToolTipText(object.getName());
	    return figure;
	}
    }

    private MenuCreator _menuCreator;
}




