/* Base class for graph controllers in Ptolemy.

 Copyright (c) 1999-2001 The Regents of the University of California.
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

import java.util.List;

import javax.swing.JMenu;
import javax.swing.JToolBar;

import diva.canvas.Figure;
import diva.graph.AbstractGraphController;
import diva.graph.GraphController;
import diva.graph.GraphPane;
import diva.graph.NodeController;
import diva.gui.toolbox.MenuCreator;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Location;
import ptolemy.vergil.toolbox.EditParametersFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;

//////////////////////////////////////////////////////////////////////////
//// PtolemyGraphController
/**
A base class for Ptolemy II graph controllers. This extends the base
class with an association with a configuration. The configuration is
central to a Ptolemy GUI, and is used by derived classes to perform
various functions such as opening models or their documentation.
The class also provides a strategy pattern interface for a controller
to add commands to the menu or toolbar of the frame it is controlling.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/
public abstract class PtolemyGraphController extends AbstractGraphController {

    /** Create a new basic controller.
     */
    public PtolemyGraphController() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add commands to the specified menu and toolbar, as appropriate
     *  for this controller.  In this base class, nothing is added.
     *  @param menu The menu to add to, or null if none.
     *  @param toolbar The toolbar to add to, or null if none.
     */
    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
    }

    /** Return the configuration that has been specified by setConfiguration(),
     *  or null if none.
     *  @return The configuration.
     */
    public Configuration getConfiguration() {
	return _configuration;
    }

    /** Return the node controller appropriate for the given object.
     *  In this base class, the method checks to see whether the object
     *  is an instance of Location and contains a NodeControllerFactory
     *  (which is an attribute).  If it does, then it invokes that factory
     *  to create a node controller. Otherwise, it returns null.
     *  @param object The object to get a controller for.
     *  @return A custom node controller if there is one, and null otherwise.
     */
    public NodeController getNodeController(Object object) {
        if(object instanceof Location) {
            Object semanticObject = getGraphModel().getSemanticObject(object);
            // Check to see whether
            // this is a NamedObj that contains a NodeControllerFactory.
            // If so, that should be used. If not, use the defaults
            // below.  This allows any object in Ptolemy II to have
            // its own controller, which means its own context menu
            // and its own interactors.
            if (semanticObject instanceof NamedObj) {
                List factoryList = ((NamedObj)semanticObject)
                        .attributeList(NodeControllerFactory.class);
                if(factoryList.size() > 0) {
                    NodeControllerFactory factory = (NodeControllerFactory)
                           factoryList.get(0);
                    PtolemyNodeController controller = factory.create(this);
                    controller.setConfiguration(getConfiguration());
                    return controller;
                }
            }
        }
        return null;
    }

    /** Set the configuration.  This is used by some of the controllers
     *  to open files or URLs.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Initialize all interaction on the graph pane. This method
     *  is called by the setGraphPane() method of the superclass.
     *  This initialization cannot be done in the constructor because
     *  the controller does not yet have a reference to its pane
     *  at that time.
     */
    protected void initializeInteraction() {
        GraphPane pane = getGraphPane();
	_menuFactory = new SchematicContextMenuFactory(this);
	_menuCreator = new MenuCreator(_menuFactory);
	pane.getBackgroundEventLayer().addInteractor(_menuCreator);
	pane.getBackgroundEventLayer().setConsuming(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected variables                ////

    /** The interactor for creating context sensitive menus on the
     *  graph itself.
     */
    protected MenuCreator _menuCreator;

    /** The factory belonging to the menu creator. */
    protected PtolemyMenuFactory _menuFactory;

    ///////////////////////////////////////////////////////////////////
    ////                        private variables                  ////

    // The configuration.
    private Configuration _configuration;

    ///////////////////////////////////////////////////////////////////
    ////                          inner classes                    ////

    /** Factory for context menus. */
    public static class SchematicContextMenuFactory
	    extends PtolemyMenuFactory {

        /** Create a new context menu factory associated with the
         *  specified controller.
         *  @param controller The controller.
         */
	public SchematicContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new EditParametersFactory());
	}

	protected NamedObj _getObjectFromFigure(Figure source) {
	    return (NamedObj)getController().getGraphModel().getRoot();
	}
    }
}
