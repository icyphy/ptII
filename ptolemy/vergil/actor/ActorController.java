/* The node controller for entities.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

package ptolemy.vergil.actor;

import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.AbstractGlobalLayout;
import diva.graph.layout.GlobalLayout;
import diva.graph.layout.IncrLayoutAdapter;
import diva.graph.layout.IncrementalLayoutListener;
import diva.util.Filter;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.KernelException;
import ptolemy.moml.Location;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.debugger.BreakpointDialogFactory;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.PortDialogFactory;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.PortSite;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.SwingConstants;

//////////////////////////////////////////////////////////////////////////
//// ActorController
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

@author Steve Neuendorffer and Edward A. Lee, Elaine Cheong
@version $Id$
@since Ptolemy II 2.0
*/
public class ActorController extends AttributeController {

    /** Create an entity controller associated with the specified graph
     *  controller with full access.
     *  @param controller The associated graph controller.
     */
    public ActorController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create an entity controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public ActorController(GraphController controller, Access access) {
        super(controller, access);

        // "Configure Ports"
        if (access == FULL) {
            // Add to the context menu.
            _portDialogFactory = new PortDialogFactory();
            _menuFactory.addMenuItemFactory(_portDialogFactory);
        }

	if (_configuration != null) {
	  // NOTE: The following requires that the configuration be
	  // non-null, or it will report an error.

	  // "Look Inside"
	  _menuFactory.addMenuItemFactory(
		new MenuActionFactory(new LookInsideAction()));
	  _addedLookInsideAction = true;
	} else {
	  _addedLookInsideAction = false;
	}

        // "Listen to Actor"
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(new ListenToActorAction()));

        // "Set Breakpoints"
        if (access == FULL) {
            // Add to the context menu.
            _breakpointDialogFactory = new BreakpointDialogFactory(
                    (BasicGraphController)getController());
            _menuFactory.addMenuItemFactory(_breakpointDialogFactory);
        }

        // The filter for the layout algorithm of the ports within this
        // entity. This returns true only if the argument is a Port
        // and the parent is an instance of Location.
        Filter portFilter = new Filter() {
            public boolean accept(Object candidate) {
                GraphModel model = getController().getGraphModel();
                if (candidate instanceof Port &&
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
    ////                         public methods                    ////

    /** Set the configuration.  This is used to open documentation files.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        if (_portDialogFactory != null) {
	    _portDialogFactory.setConfiguration(configuration);
        }

	if (_configuration != null && !_addedLookInsideAction ) {
	    // NOTE: The following requires that the configuration be
	    // non-null, or it will report an error.

	    // "Look Inside"
	    _menuFactory.addMenuItemFactory(
 	      new MenuActionFactory(new LookInsideAction()));
	    _addedLookInsideAction = true;
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private PortDialogFactory _portDialogFactory;

    private static Font _portLabelFont = new Font("SansSerif", Font.PLAIN, 10);

    private BreakpointDialogFactory _breakpointDialogFactory;

    // Set to true if we add the LookInsideAction
    private boolean _addedLookInsideAction = false;

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
            LinkedList inputOutputs = new LinkedList();
            int inCount = 0;
            int outCount = 0;
            int inputOutputCount = 0;

            while (nodes.hasNext()) {
                Port port = (Port) nodes.next();
                if (!(port instanceof IOPort)) {
                    inputOutputCount++;
                    inputOutputs.addLast(port);
                } else {
                    IOPort ioport = (IOPort) port;
                    if (ioport.isInput() && ioport.isOutput()) {
                        inputOutputCount++;
                        inputOutputs.addLast(port);
                    } else if (ioport.isInput()) {
                        inCount++;
                        inputs.addLast(port);
                    } else if (ioport.isOutput()) {
                        outCount++;
                        outputs.addLast(port);
                    } else {
                        inputOutputCount++;
                        inputOutputs.addLast(port);
                    }
                }
            }
            CompositeFigure figure =
                (CompositeFigure)getLayoutTarget().getVisualObject(node);

            _placePortFigures(figure, inputs, inCount,
                    SwingConstants.WEST);
            _placePortFigures(figure, outputs, outCount,
                    SwingConstants.EAST);
            _placePortFigures(figure, inputOutputs, inputOutputCount,
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
            while (ports.hasNext()) {
                IOPort port = (IOPort)ports.next();
                Figure portFigure = getController().getFigure(port);
                // If there is no figure, then ignore this port.  This may
                // happen if the port hasn't been rendered yet.
                if (portFigure == null) continue;
                Rectangle2D portBounds = portFigure.getShape().getBounds2D();
                PortSite site = new PortSite(
                        figure.getBackgroundFigure(),
                        port,
                        number,
                        count);
                number ++;
                // NOTE: previous expression for port location was:
                //    100.0 * number / (count+1)
                // But this leads to squished ports with uneven spacing.

                // Note that we don't use CanvasUtilities.translateTo because
                // we want to only get the bounds of the background of the
                // port figure.
                double x = site.getX() - portBounds.getCenterX();
                double y = site.getY() - portBounds.getCenterY();
                portFigure.translate(x, y);

                // If the port contains an attribute named "_showName",
                // then render the name of the port as well.
                if (port.getAttribute("_showName") != null) {
                    LabelFigure label = null;
                    if (port.isOutput() && port.isInput()) {
                        // The 1.0 argument is the padding.
                        label = new LabelFigure(
                                port.getName(),
                                _portLabelFont,
                                1.0,
                                SwingConstants.NORTH_WEST);
                        // Shift the label right so it doesn't
                        // collide with ports.
                        label.translateTo(x, y - 5);
                        // Rotate the label.
                        AffineTransform rotate = AffineTransform
                                .getRotateInstance(Math.PI/2.0, x, y + 5);
                        label.transform(rotate);
                    } else if (port.isOutput()) {
                        // The 1.0 argument is the padding.
                        label = new LabelFigure(
                                port.getName(),
                                _portLabelFont,
                                1.0,
                                SwingConstants.SOUTH_WEST);
                        // Shift the label right so it doesn't
                        // collide with ports.
                        label.translateTo(x + 5, y);
                    } else if (port.isInput()) {
                        // The 1.0 argument is the padding.
                        label = new LabelFigure(
                                port.getName(),
                                _portLabelFont,
                                1.0,
                                SwingConstants.SOUTH_EAST);
                        // Shift the label right so it doesn't
                        // collide with ports.
                        label.translateTo(x - 5, y);
                    }
                    figure.add(label);
                }
            }
        }
    }

    // An action to listen to debug messages in the actor.
    private class ListenToActorAction extends FigureAction {
        public ListenToActorAction() {
            super("Listen to Actor");
        }
        public void actionPerformed(ActionEvent e) {
            if (_configuration == null) {
                MessageHandler.error(
                        "Cannot listen to actor without a configuration.");
                return;
            }

            // Determine which entity was selected for the listen to
            // actor action.
            super.actionPerformed(e);
            NamedObj object = getTarget();
            try {
                BasicGraphController controller =
                    (BasicGraphController)getController();
                BasicGraphFrame frame = controller.getFrame();
                Tableau tableau = frame.getTableau();

                // effigy is the whole model.
                Effigy effigy = (Effigy)tableau.getContainer();
                
                // We want to open a new window that behaves as a
                // child of the model window.  So, we create a new text
                // effigy inside this one.  Specify model's effigy as
                // a container for this new effigy.
                Effigy textEffigy = new TextEffigy(effigy,
                        effigy.uniqueName("debugListener" + object.getName()));
                
                DebugListenerTableau debugTableau =
                    new DebugListenerTableau(textEffigy,
                            textEffigy.uniqueName("debugListener"
                                    + object.getName()));
                debugTableau.setDebuggable(object);
            }
            catch (KernelException ex) {
                MessageHandler.error(
                        "Failed to create debug listener.", ex);
            }
        }
    }

    // An action to look inside a composite.
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

            // Determine which entity was selected for the look inside action.
            super.actionPerformed(e);
            NamedObj object = getTarget();
            if (!(object instanceof CompositeEntity)) {
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
            try {
                _configuration.openModel(entity);
            } catch (Exception ex) {
                MessageHandler.error("Look inside failed.", ex);
            }
        }
    }
}
