/* The graph controller for the icon editor.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.vergil.icon;

import javax.swing.JMenu;
import javax.swing.JToolBar;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.kernel.AttributeController;
import diva.canvas.interactor.SelectionDragger;
import diva.graph.EdgeController;
import diva.graph.GraphPane;
import diva.graph.NodeController;

///////////////////////////////////////////////////////////////////
//// EditIconGraphController

/**
 A graph controller for the Ptolemy II icon editor.
 This controller contains a set of default node controllers for attributes,
 which are the only objects that an icon editor can contain. The default
 controller can be overridden by attributes of type NodeControllerFactory.
 The getNodeController() method always returns an attribute controller.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class EditIconGraphController extends BasicGraphController {
    /** Create a new basic controller with default
     *  terminal and edge interactors and default context menus.
     */
    public EditIconGraphController() {
        super();
        _createControllers();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add hot key for look inside.
     *  @param menu The menu to add to, which is ignored.
     *  @param toolbar The toolbar to add to, which is also ignored.
     */
    @Override
    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
        super.addToMenuAndToolbar(menu, toolbar);

        // FIXME: Placeholder for adding support for icon editing here.
    }

    /** Throw an exception. This should not be called.
     *  @param edge The edge object.
     *  @exception InternalErrorException If this is called.
     *  @return An exception.
     */
    @Override
    public EdgeController getEdgeController(Object edge) {
        throw new InternalErrorException("An icon editor has no edges.");
    }

    /** Return the node controller appropriate for the specified object.
     *  If the specified object is an instance of Locatable and
     *  its container contains a NodeControllerFactory
     *  (which is an attribute), then invoke that factory
     *  to create a node controller.  Otherwise,
     *  if the object implements Locatable and is contained by an
     *  instance of Attribute, then return the attribute controller.
     *  Otherwise, throw a runtime exception.
     *  @param object A Vertex, Locatable, or Port.
     *  @return object The node controller.
     *  @exception RuntimeException If the specified object is not
     *   a Locatable contained by an Attribute.
     */
    @Override
    public NodeController getNodeController(Object object) {
        // Defer to the superclass if it can provide a controller.
        NodeController result = super.getNodeController(object);

        if (result != null) {
            ((NamedObjController) result).setSnapResolution(_SNAP_RESOLUTION);
            return result;
        }

        // Superclass cannot provide a controller. Use defaults.
        if (object instanceof Locatable) {
            Object semanticObject = getGraphModel().getSemanticObject(object);

            if (semanticObject instanceof Attribute) {
                return _attributeController;
            } else {
                throw new RuntimeException("Unrecognized object: "
                        + semanticObject);
            }
        } else if (object == null) {
            return _attributeController;
        }

        throw new RuntimeException("Node with unknown semantic object: "
                + object);
    }

    /** Set the configuration.  The configuration is used when
     *  opening documentation files, for example.
     *  @param configuration The configuration.
     */
    @Override
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        _attributeController.setConfiguration(configuration);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the controllers for nodes in this graph.
     *  This is called by the constructor, so derived classes that
     *  override this must be careful not to reference local variables
     *  defined in the derived classes, because the derived classes
     *  will not have been fully constructed by the time this is called.
     */
    @Override
    protected void _createControllers() {
        super._createControllers();
        _attributeController = new AttributeController(this,
                AttributeController.FULL);

        // Set the snap resolution smaller than the default of 5.0.
        _attributeController.setSnapResolution(_SNAP_RESOLUTION);
    }

    /** Initialize all interaction on the graph pane. This method
     *  is called by the setGraphPane() method of the superclass.
     *  This initialization cannot be done in the constructor because
     *  the controller does not yet have a reference to its pane
     *  at that time.
     */
    @Override
    protected void initializeInteraction() {
        GraphPane pane = getGraphPane();

        // Create and set up the selection dragger
        _selectionDragger = new SelectionDragger(pane);
        _selectionDragger.addSelectionModel(getSelectionModel());

        super.initializeInteraction();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The attribute controller. */
    protected NamedObjController _attributeController;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The selection interactor for drag-selecting nodes
    private SelectionDragger _selectionDragger;

    // Default snap resolution.
    private static double _SNAP_RESOLUTION = 1.0;
}
