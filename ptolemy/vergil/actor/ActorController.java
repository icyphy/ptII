/* The node controller for entities.

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

package ptolemy.vergil.actor;

import java.awt.Event;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import ptolemy.actor.IOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.debugger.BreakpointDialogFactory;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.kernel.PortDialogFactory;
import ptolemy.vergil.toolbox.EditIconAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.PortSite;
import ptolemy.vergil.toolbox.RemoveIconAction;
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
import diva.gui.GUIUtilities;
import diva.util.Filter;

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

        _access = access;

        // "Configure Ports"
        if (access == FULL) {
            // Add to the context menu.
            _portDialogFactory = new PortDialogFactory();
            _menuFactory.addMenuItemFactory(
                    _portDialogFactory);
        }

        if (_configuration != null) {
            // NOTE: The following requires that the configuration be
            // non-null, or it will report an error.
            _menuFactory.addMenuItemFactory(
                    new MenuActionFactory(_lookInsideAction));
            if (access == FULL) {
                _editIconAction.setConfiguration(_configuration);
                _menuFactory.addMenuItemFactory(
                        new MenuActionFactory(_editIconAction));
                _removeIconAction.setConfiguration(_configuration);
                _menuFactory.addMenuItemFactory(
                        new MenuActionFactory(_removeIconAction));
            }
        }

        // NOTE: This requires that the configuration be non null, or it
        // will report an error.
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(new SaveInLibraryAction()));

        _listenToActorAction = new ListenToActorAction(
                (BasicGraphController)getController());
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(_listenToActorAction));
        _listenToActorAction.setConfiguration(_configuration);

        // "Set Breakpoints"
        if (access == FULL) {
            // Add to the context menu.
            _breakpointDialogFactory = new BreakpointDialogFactory(
                    (BasicGraphController)getController());
            _menuFactory.addMenuItemFactory(_breakpointDialogFactory);
        }


        // The filter for the layout algorithm of the ports within this
        // entity. This returns true only if the argument is a Port
        // and the parent implements Locatable.
        Filter portFilter = new Filter() {
                public boolean accept(Object candidate) {
                    GraphModel model = getController().getGraphModel();
                    if (candidate instanceof Locatable &&
                            model.getSemanticObject(candidate) instanceof Entity) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };

        // Anytime we add a port to an entity, we want to layout all the
        // ports within that entity.
        GlobalLayout layout = new EntityLayout();
        controller.addGraphViewListener(
                new IncrementalLayoutListener(
                        new IncrLayoutAdapter(layout) {
                                public void nodeDrawn(Object node) {
                                    layout(node);
                                }
                            }, portFilter));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If access is FULL, then add the jni.ArgumentDailogFactory() to
     *  _menuFactory.  If access is not FULL, then do nothing.
     *  @param menuItemFactory  The MenuItemFactory to be added.
     */
    public void addMenuItemFactory(MenuItemFactory menuItemFactory) {
        // This method is called by jni.ThalesGraphFrame to add a context
        // menu.

        if (_access == FULL) {
            _menuFactory
                .addMenuItemFactory(menuItemFactory);
        }
    }

    /** Set the configuration.  This is used to open documentation files.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        if (_portDialogFactory != null) {
            _portDialogFactory.setConfiguration(configuration);
        }
        if (_listenToActorAction != null) {
            _listenToActorAction.setConfiguration(_configuration);
        }
        if (_configuration != null) {
            // NOTE: The following requires that the configuration be
            // non-null, or it will report an error.
            _menuFactory.addMenuItemFactory(
                    new MenuActionFactory(_lookInsideAction));
            if (_access == FULL) {
                _editIconAction.setConfiguration(_configuration);
                _menuFactory.addMenuItemFactory(
                        new MenuActionFactory(_editIconAction));
                _removeIconAction.setConfiguration(_configuration);
                _menuFactory.addMenuItemFactory(
                        new MenuActionFactory(_removeIconAction));
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    /** The action that handles edit custom icon.
     */
    protected EditIconAction _editIconAction = new EditIconAction();
            
    /** The action that handles look inside.  This is accessed by
     *  by ActorViewerController to create a hot key for the editor.
     */
    protected LookInsideAction _lookInsideAction = new LookInsideAction();

    /** The action that handles removing a custom icon.
     */
    protected RemoveIconAction _removeIconAction = new RemoveIconAction();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private BreakpointDialogFactory _breakpointDialogFactory;

    private ListenToActorAction _listenToActorAction;

    private PortDialogFactory _portDialogFactory;

    private static Font _portLabelFont = new Font("SansSerif", Font.PLAIN, 10);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /////////////////////////////////////////////////////////////////////
    //// EntityLayout

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
            // System.out.println("layout = " + node);
            //        new Exception().printStackTrace();
            Iterator nodes = model.nodes(node);
            Vector westPorts = new Vector ();
            Vector eastPorts = new Vector ();
            Vector southPorts = new Vector ();
            Vector northPorts = new Vector ();
     
            while (nodes.hasNext()) {
                Port port = (Port) nodes.next();
                StringAttribute cardinal =
                    (StringAttribute)port.getAttribute("_cardinal");
                StringAttribute ordinal  =
                    (StringAttribute)port.getAttribute("_ordinal");

                if (cardinal == null) {
                    if (!(port instanceof IOPort)) {
                        southPorts.add(port);
                    } else {
                        IOPort ioport = (IOPort) port;
                        if (ioport.isInput() && ioport.isOutput()) {
                            southPorts.add(port);
                        } else if (ioport.isInput()) {
                            westPorts.add(port);
                        } else if (ioport.isOutput()) {
                            eastPorts.add(port);
                        } else {
                            southPorts.add(port);
                        }
                    }
                }
                else {
                    if (!(port instanceof IOPort)) {
                        southPorts.add(port);
                    } else {
                        String value = cardinal.getExpression();
                        IOPort ioport = (IOPort) port;
                        if ( value.equalsIgnoreCase("SOUTH") ) {
                            southPorts.add(port);
                        } else if ( value.equalsIgnoreCase("WEST") ) {
                            westPorts.add(port);
                        } else if ( value.equalsIgnoreCase("EAST") ) {
                            eastPorts.add(port);
                        } else if ( value.equalsIgnoreCase("NORTH") ) {
                            northPorts.add( port );
                        } else {
                            southPorts.add(port);
                        }
                    }
                }
            }
            CompositeFigure figure =
                (CompositeFigure)getLayoutTarget().getVisualObject(node);

            _reOrderPorts( westPorts );
            _placePortFigures(figure, westPorts,
                    SwingConstants.WEST);
            _reOrderPorts( eastPorts );
            _placePortFigures(figure, eastPorts,
                    SwingConstants.EAST);
            _reOrderPorts( southPorts );
            _placePortFigures(figure, southPorts,
                    SwingConstants.SOUTH);
            _reOrderPorts( northPorts );
            _placePortFigures(figure, northPorts,
                    SwingConstants.NORTH);
        }

        ///////////////////////////////////////////////////////////////
        ////                     private methods                   ////

        // re-order the ports according to _ordinal property
        private void _reOrderPorts( Vector ports ) {
            int size = ports.size();
            Enumeration enumeration = ports.elements();
            Port port;
            StringAttribute ordinal = null;
            int number = 0;
            int index  = 0;

            while ( enumeration.hasMoreElements() ) {
                port = (Port)enumeration.nextElement();
                ordinal = (StringAttribute)port.getAttribute("_ordinal");

                if ( ordinal != null ) {
                    number = Integer.parseInt( ordinal.getExpression() ) ;
                    if ( number >= size ) {
                        ports.remove( index );
                        try {
                            ordinal.setExpression( Integer.toString( size -1 ) );
                        } catch( Exception e ) {
                            MessageHandler.error("Error setting ordinal property", e);
                        }
                        ports.add( port );
                    }
                    else if ( number < 0 ) {
                        ports.remove( index );
                        try {
                            ordinal.setExpression( Integer.toString( 0 ) );
                        } catch ( Exception e ) {
                            MessageHandler.error("Error setting ordinal property", e);
                        }
                        ports.add( 0, port );
                    }
                    else if ( number != index ){
                        ports.remove( index );
                        ports.add( number, port );
                    }

                }
                index++;
            }
        }

        // Place the ports.
        private void _placePortFigures(
                CompositeFigure figure, List portList, int direction) {
            Iterator ports = portList.iterator();
            int number = 0;
            int count = portList.size();
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
                    } else {
                        // Not an input or output.
                        // The 1.0 argument is the padding.
                        label = new LabelFigure(
                                port.getName(),
                                _portLabelFont,
                                1.0,
                                SwingConstants.NORTH);
                        // Shift the label right so it doesn't
                        // collide with ports.
                        label.translateTo(x, y + 5);
                    }
                    figure.add(label);
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////////
    //// ListenToActorAction
    
    /** An action to listen to debug messages in the actor.
     *  This is static so that other classes can use it.
     */
    public static class ListenToActorAction extends FigureAction {
        public ListenToActorAction(BasicGraphController controller) {
            super("Listen to Actor");
            _controller = controller;
        }
        public ListenToActorAction(
                NamedObj target, BasicGraphController controller) {
            super("Listen to Actor");
            _target = target;
            _controller = controller;
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
            NamedObj object = _target;
            if (object == null) {
                object = getTarget();
            }
            try {
                BasicGraphFrame frame = _controller.getFrame();
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

        /** Set the configuration for use by the help screen.
         *  @param configuration The configuration.
         */
        public void setConfiguration(Configuration configuration) {
            _configuration = configuration;
        }

        private Configuration _configuration;
        private BasicGraphController _controller;
        private NamedObj _target;
    }
    
    /////////////////////////////////////////////////////////////////////
    //// LookInsideAction

    // An action to look inside a composite.
    private class LookInsideAction extends FigureAction {

        public LookInsideAction() {
            super("Look Inside");
            // For some inexplicable reason, the I key doesn't work here.
            // Use L, which used to be used for layout.
            putValue(GUIUtilities.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.CTRL_MASK));
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
            
            // NOTE: Used to open source code here if the object
            // was not a CompositeEntity. But this made it impossible
            // to associate a custom tableau with an atomic entity.
            // So now, the Configuration opens the source code as a
            // last resort.

            try {
                _configuration.openModel(object);
            } catch (Exception ex) {
                MessageHandler.error("Look inside failed.", ex);
            }
        }
    }

    /////////////////////////////////////////////////////////////////////
    //// SaveInLibraryAction

    /** An action to save this actor in the library.
     */
    private class SaveInLibraryAction extends FigureAction {

        /**
         *  Create a new action to save a model in a library.
         */
        public SaveInLibraryAction() {
            super("Save Actor In Library");
            putValue("tooltip", "Save Actor as a Component in Library");
        }

        /**
         *  Create a new instance of the current model in the actor library of
         *  the configuration.
         *
         * @param  e  Description of Parameter
         */
        public void actionPerformed(ActionEvent e) {
            // Figure out what entity.
            super.actionPerformed(e);
            NamedObj object = getTarget();
            if (object instanceof Entity) {
                Entity entity = (Entity)object;
                BasicGraphFrame.saveComponentInLibrary(_configuration, entity);
            }
        }
    }

    // Error message used when we can't find the inside definition.
    private static String _CANNOT_FIND_MESSAGE
            = "Cannot find inside definition. "
            + "Perhaps source code is not installed? "
            + "You can obtain source code for Berkeley actors at: "
            + "http://ptolemy.eecs.berkeley.edu/ptolemyII";

    private Access _access;

}
