/* The node controller for icons of attributes

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

package ptolemy.vergil.ptolemy.kernel;

// FIXME: Replace with per-class imports.
import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.vergil.*;
import ptolemy.vergil.ptolemy.*;
import ptolemy.vergil.ptolemy.LocatableNodeController;
import ptolemy.vergil.toolbox.*;
import ptolemy.gui.*;
import ptolemy.moml.*;

import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import diva.graph.*;
import diva.graph.basic.*;
import diva.graph.layout.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.util.Filter;

import java.awt.geom.Rectangle2D;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.*;
import java.net.URL;
import javax.swing.JMenuItem;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// AttributeController
/**
This class provides interaction with nodes that represent Ptolemy II
attributes.
(Or, more specifically, with the icon that is contained in an attribute.)

Standard selection and movement interaction is
provided.  In addition, right clicking on the entity will create a context
menu for the entity.

@author Steve Neuendorffer
@version $Id$
*/
public class AttributeController extends LocatableNodeController {

    /** Create an attribute controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public AttributeController(GraphController controller) {
	super(controller);
	setNodeRenderer(new AttributeRenderer());

	SelectionModel sm = controller.getSelectionModel();
        NodeInteractor interactor =
            (NodeInteractor) getNodeInteractor();
	interactor.setSelectionModel(sm);

	// Initialize the menu creator.
	_menuCreator = new MenuCreator(null);
	interactor.addInteractor(_menuCreator);
    }

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

    public static class AttributeRenderer implements NodeRenderer {
	public Figure render(Object n) {
	    Location location = (Location)n;
	    NamedObj object = (NamedObj) location.getContainer();

	    // FIXME: may want to use another type of icon
	    // FIXME: this code is the same as in PtolemyTreeCellRenderer and
            // EntityController.
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
            figure.setToolTipText(object.getClass().getName());
	    return figure;
	}
    }

    private MenuCreator _menuCreator;
}
