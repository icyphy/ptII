/* The node controller for entities.

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

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.PortSite;
import ptolemy.gui.MessageHandler;
import ptolemy.moml.Location;
import ptolemy.moml.URLAttribute;

import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.AbstractGlobalLayout;
import diva.graph.layout.GlobalLayout;
import diva.graph.layout.IncrementalLayoutListener;
import diva.graph.layout.IncrLayoutAdapter;
import diva.util.Filter;

import java.awt.geom.Rectangle2D;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.net.URL;
import javax.swing.SwingConstants;

//////////////////////////////////////////////////////////////////////////
//// EntityController
/**
This class provides interaction with nodes that represent Ptolemy II
entities.  It provides a double click binding and context menu
entry to edit the parameters of the node ("Configure"), a
command to get documentation, and a command to look inside.
It can have one of two access levels, FULL or PARTIAL.
If the access level is FULL, the the context menu also
contains a command to rename the node and to configure its ports.
In addition, a layout algorithm is applied so that
the figures for ports are automatically placed on the sides of the
figure for the entity.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/
public class EntityController extends AttributeController {

    /** Create an entity controller associated with the specified graph
     *  controller with full access.
     *  @param controller The associated graph controller.
     */
    public EntityController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create an entity controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public EntityController(GraphController controller, Access access) {
	super(controller, access);

        if(access == FULL) {
            // Add to the context menu.
            _menuFactory.addMenuItemFactory(
                    new PortDialogFactory());
        }

        // NOTE: This requires that the configuration be non null, or it
        // will report an error.
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(new LookInsideAction()));

	// The filter for the layout algorithm of the ports within this
	// entity. This returns true only if the argument is a Port
        // and the parent is an instance of Location.
	Filter portFilter = new Filter() {
	    public boolean accept(Object candidate) {
		GraphModel model = getController().getGraphModel();
		if(candidate instanceof Port &&
                        model.getParent(candidate) instanceof Location) {
		    return true;
		} else {
		    return false;
		}
	    }
	};

	// Anytime we add a port to an entity, we want to layout all the
	// ports within that entity.
	GlobalLayout layout = new EntityLayout();
	controller.addGraphViewListener(new IncrementalLayoutListener(
                new IncrLayoutAdapter(layout), portFilter));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This layout algorithm is responsible for laying out the ports
     *  within an entity.
     */
    public class EntityLayout extends AbstractGlobalLayout {

        /** Create a new layout manager. */
	public EntityLayout() {
	    super(new BasicLayoutTarget(getController()));
	}

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Layout the ports of the specified node.
         *  @param node The node, which is assumed to be an entity.
         */
	public void layout(Object node) {
	    GraphModel model = getController().getGraphModel();
	    Iterator nodes = model.nodes(node);
	    LinkedList inputs = new LinkedList();
	    LinkedList outputs = new LinkedList();
	    LinkedList inouts = new LinkedList();
	    int inCount = 0;
	    int outCount = 0;
	    int inOutCount = 0;

	    while(nodes.hasNext()) {
		Port port = (Port) nodes.next();
		if(!(port instanceof IOPort)) {
		    inOutCount++;
		    inouts.addLast(port);
		} else {
		    IOPort ioport = (IOPort) port;
		    if(ioport.isInput() && ioport.isOutput()) {
			inOutCount++;
			inouts.addLast(port);
		    } else if(ioport.isInput()) {
			inCount++;
			inputs.addLast(port);
		    } else if(ioport.isOutput()) {
			outCount++;
			outputs.addLast(port);
		    } else {
                        inOutCount++;
			inouts.addLast(port);
		    } 
		}
	    }
	    CompositeFigure figure =
		(CompositeFigure)getLayoutTarget().getVisualObject(node);

	    _placePortFigures(figure, inputs, inCount,
                    SwingConstants.WEST);
	    _placePortFigures(figure, outputs, outCount,
                    SwingConstants.EAST);
	    _placePortFigures(figure, inouts, inOutCount,
                    SwingConstants.SOUTH);

	}

        ///////////////////////////////////////////////////////////////
        ////                     private methods                   ////

        // Place the ports.
	private void _placePortFigures(
                CompositeFigure figure,
                List portList,
                int count,
                int direction) {
	    Iterator ports = portList.iterator();
	    int number = 0;
	    while(ports.hasNext()) {
		Object port = ports.next();
		Figure portFigure = getController().getFigure(port);
		// If there is no figure, then ignore this port.  This may
		// happen if the port hasn't been rendered yet.
		if(portFigure == null) continue;
                Rectangle2D portBounds = portFigure.getShape().getBounds2D();
		PortSite site = new PortSite(
                        figure.getBackgroundFigure(),
                        (IOPort)port,
                        number,
                        count);
		number ++;
                // NOTE: previous expression for port location was:
                //    100.0 * number / (count+1)
                // But this leads to squished ports with uneven spacing.

                // Note that we don't use CanvasUtilities.translateTo because
                // we want to only get the bounds of the background of the
                // port figure.
                portFigure.translate(
                        site.getX() - portBounds.getCenterX(),
                        site.getY() - portBounds.getCenterY());
	    }
	}
    }

    // An action to look inside a composite.
    // NOTE: This requires that the configuration be non null, or it
    // will report an error with a fairly cryptic message.
    private class LookInsideAction extends FigureAction {
	public LookInsideAction() {
	    super("Look Inside");
	}
	public void actionPerformed(ActionEvent e) {

            if (_configuration == null) {
                MessageHandler.error(
                        "Cannot look inside without a configuration.");
                return;
            }

	    // Figure out what entity.
	    super.actionPerformed(e);
	    NamedObj object = getTarget();
	    if(!(object instanceof CompositeEntity)) {
                // Open the source code, if possible.
                String filename = object.getClass()
                        .getName().replace('.', '/') + ".java";
                try {
                    URL toRead = getClass().getClassLoader()
                           .getResource(filename);
                    if (toRead != null) {
                        _configuration.openModel(null,
                               toRead, toRead.toExternalForm());
                    } else {
                        MessageHandler.error("Cannot find inside definition.");
                    }
                } catch (Exception ex) {
                    MessageHandler.error("Cannot find inside definition.", ex);
                }
                return;
            }
	    CompositeEntity entity = (CompositeEntity)object;

            // If the entity defers its MoML definition to another,
            // then open that other.
	    NamedObj deferredTo = entity.getMoMLInfo().deferTo;
	    if(deferredTo != null) {
		entity = (CompositeEntity)deferredTo;
	    }

            // Search the model directory for an effigy that already
            // refers to this model.
            PtolemyEffigy effigy = _configuration.getEffigy(entity);
            if (effigy != null) {
                // Found one.  Display all open tableaux.
                effigy.showTableaux();
            } else {
                try {

                    // There is no pre-existing effigy.  Create one.
                    effigy = new PtolemyEffigy(_configuration.workspace());
                    effigy.setModel(entity);

                    // Look to see whether the model has a URLAttribute.
                    List attributes = entity.attributeList(URLAttribute.class);
                    if (attributes.size() > 0) {
                        // The entity has a URL, which was probably
                        // inserted by MoMLParser.

                        URL url = ((URLAttribute)attributes.get(0)).getURL();

                        // Set the url and identifier of the effigy.
                        effigy.url.setURL(url);
                        effigy.identifier.setExpression(url.toExternalForm());

                        // Put the effigy into the directory
                        ModelDirectory directory =
                                _configuration.getDirectory();
                        effigy.setName(directory.uniqueName(entity.getName()));
                        effigy.setContainer(directory);

                        // Create a default tableau.
                        _configuration.createPrimaryTableau(effigy);

                    } else {

                        // If we get here, then we are looking inside a model
                        // that is defined within the same file as the parent,
                        // probably.  Create a new PtolemyEffigy
                        // and open a tableau for it.

                        // Put the effigy inside the effigy of the parent,
                        // rather than directly into the directory.
                        CompositeEntity parent =
                            (CompositeEntity)entity.getContainer();
                        boolean isContainerSet = false;
                        if (parent != null) {
                            PtolemyEffigy parentEffigy =
                                    _configuration.getEffigy(parent);
                            if (parentEffigy != null) {
                                // OK, we can put it into this other effigy.
                                effigy.setName(parentEffigy.uniqueName(
                                        entity.getName()));
                                effigy.setContainer(parentEffigy);

                                // Set the identifier of the effigy to be that
                                // of the parent with the model name appended.
                                effigy.identifier.setExpression(
                                        parentEffigy.identifier.getExpression()
                                        + "#" + entity.getName());

                                // Set the url of the effigy to that of
                                // the parent.
                                effigy.url.setURL(parentEffigy.url.getURL());

                                // Indicate success.
                                isContainerSet = true;
                            }
                        }
                        // If the above code did not find an effigy to put
                        // the new effigy within, then put it into the
                        // directory directly.
                        if (!isContainerSet) {
                            CompositeEntity directory = 
                                    _configuration.getDirectory();
                            effigy.setName(
                                    directory.uniqueName(entity.getName()));
                            effigy.setContainer(directory);
                            effigy.identifier.setExpression(
                                    entity.getFullName());
                        }

                        _configuration.createPrimaryTableau(effigy);
                    }
                } catch (Exception ex) {
                    MessageHandler.error("Look inside failed: ", ex);
                }
            }
	}
    }
}
