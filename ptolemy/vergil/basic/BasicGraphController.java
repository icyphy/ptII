/* Base class for graph controllers in Ptolemy.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.Prototype;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.toolbox.ConfigureAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import diva.canvas.Figure;
import diva.canvas.connector.Connector;
import diva.canvas.interactor.SelectionRenderer;
import diva.graph.AbstractGraphController;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.GraphUtilities;
import diva.graph.NodeController;
import diva.gui.toolbox.MenuCreator;


//////////////////////////////////////////////////////////////////////////
//// BasicGraphController
/**
A base class for Ptolemy II graph controllers. This extends the base
class with an association with a configuration. The configuration is
central to a Ptolemy GUI, and is used by derived classes to perform
various functions such as opening models or their documentation.
The class also provides a strategy pattern interface for a controller
to add commands to the menu or toolbar of the frame it is controlling.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public abstract class BasicGraphController extends AbstractGraphController
    implements DebugListener, ValueListener {

    /** Create a new basic controller.
     */
    public BasicGraphController() {
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

    /** Clear any animation highlight that might currently be active.
     */
    public void clearAnimation() {
        // Deselect previous one.
        if (_animated != null && _animationRenderer != null) {
            _animationRenderer.renderDeselected(_animated);
        }
    }

    /** React to an event.  This base class does nothing.
     *  @param event The debug event.
     */
    public void event(DebugEvent event) {
    }

    /** Get the time delay for animation.  After highlighting,
     *  derived classes are expected to sleep for the specified amount
     *  of time, in milliseconds.
     *  @see #setAnimationDelay
     *  @return The animation delay set by setAnimationDelay().
     */
    public long getAnimationDelay() {
        return _animationDelay;
    }

    /** Return the configuration that has been specified by setConfiguration(),
     *  or null if none.
     *  @return The configuration.
     */
    public Configuration getConfiguration() {
        return _configuration;
    }

    /** Get the graph frame, or null if there is none.  This is used by
     *  some of the controllers to mark the modified bit of the frame
     *  and to update any dependents.
     *  @return The graph frame, or null if there is none.
     */
    public BasicGraphFrame getFrame() {
        return _frame;
    }

    /** Return the node controller appropriate for the given object.
     *  In this base class, the method checks to see whether the object
     *  is an instance of Locatable and contains a NodeControllerFactory
     *  (which is an attribute).  If it does, then it invokes that factory
     *  to create a node controller. Otherwise, it returns null.
     *  @param object The object to get a controller for.
     *  @return A custom node controller if there is one, and null otherwise.
     */
    public NodeController getNodeController(Object object) {
        if (object instanceof Locatable) {
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
                if (factoryList.size() > 0) {
                    NodeControllerFactory factory = (NodeControllerFactory)
                        factoryList.get(0);
                    NamedObjController controller = factory.create(this);
                    controller.setConfiguration(getConfiguration());
                    _initializeInteraction(controller);
                    return controller;
                }
            }
        }
        return null;
    }

    /** React to a debug message.  This base class does nothing.
     *  @param message The message.
     */
    public void message(String message) {
    }

    /** Set the time delay for animation.  After highlighting,
     *  derived classes are expected to sleep for the specified amount
     *  of time, in milliseconds.  If this method is not called, or
     *  is called with argument 0, then no delay is introduced.
     *  @param time Time to sleep, in milliseconds.
     */
    public void setAnimationDelay(long time) {
        _animationDelay = time;
    }

    /** Set the configuration.  This is used by some of the controllers
     *  when opening files or URLs.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
        
        if (_configuration != null && _menuFactory != null) {
            // NOTE: The following requires that the configuration be
            // non-null, or it will report an error.
            _menuFactory.addMenuItemFactory(
                    new MenuActionFactory(_openBaseClassAction));
        }
    }

    /** Set the figure associated with the given semantic object, and if
     *  that semantic object is Settable, then set up a value listener
     *  so that if its value changes, then the valueChanged() method
     *  is invoked.
     *  The semantic object is normally an attribute that implements
     *  the Locatable interface, and the value indicates the location
     *  of the object.
     *  A null figure clears the association.
     *  @param semanticObject The semantic object (normally a Locatable).
     */
    public void setFigure(Object semanticObject, Figure figure) {
        super.setFigure(semanticObject, figure);
        if (semanticObject instanceof Settable) {
            ((Settable)semanticObject).addValueListener(this);
        }
    }

    /** Set the graph frame.  This is used by some of the controllers
     *  to mark the modified bit of the frame and to update any dependents.
     *  @param frame The graph frame, or null if there is none.
     */
    public void setFrame(BasicGraphFrame frame) {
        _frame = frame;
    }

    /** React to the fact that the specified Settable has changed.
     *  If the specified Settable implements the Locatable interface,
     *  then this method will move the figure and reroute any connections
     *  to it. This is done immediately if the caller is in the Swing
     *  event thread, but otherwise is deferred to the event thread.
     *  @param settable The object that has changed value.
     */
    public void valueChanged(final Settable settable) {
        if (settable instanceof Locatable && !_inValueChanged) {
            // Have to defer this to the event thread, or repaint
            // doesn't work properly.
            Runnable action = new Runnable() {
                    public void run() {
                        Locatable location = (Locatable)settable;
                        Figure figure = getFigure(location);
                        Point2D origin = figure.getOrigin();
                        double originalUpperLeftX = origin.getX();
                        double originalUpperLeftY = origin.getY();

                        // NOTE: the following call may trigger an evaluation,
                        // which results in another recursive call to this method.
                        // Thus, we ignore the inside call and detect it with a
                        // private variable.
                        double[] newLocation;
                        try {
                            _inValueChanged = true;
                            newLocation = location.getLocation();
                        } finally {
                            _inValueChanged = false;
                        }

                        double translationX = newLocation[0] - originalUpperLeftX;
                        double translationY = newLocation[1] - originalUpperLeftY;

                        if (translationX != 0.0 || translationY != 0.0) {
                            // The translate method supposedly handles the required
                            // repaint.
                            figure.translate(translationX, translationY);

                            // Reroute edges linked to this figure.
                            GraphModel model = getGraphModel();
                            Object userObject = figure.getUserObject();
                            if (userObject != null) {
                                Iterator inEdges = model.inEdges(userObject);
                                while (inEdges.hasNext()) {
                                    Figure connector = getFigure(inEdges.next());
                                    if (connector instanceof Connector) {
                                        ((Connector)connector).reroute();
                                    }
                                }
                                Iterator outEdges = model.outEdges(userObject);
                                while (outEdges.hasNext()) {
                                    Figure connector = getFigure(outEdges.next());
                                    if (connector instanceof Connector) {
                                        ((Connector)connector).reroute();
                                    }
                                }
                                if (model.isComposite(userObject)) {
                                    Iterator edges = GraphUtilities
                                        .partiallyContainedEdges(
                                                userObject, model);
                                    while (edges.hasNext()) {
                                        Figure connector = getFigure(edges.next());
                                        if (connector instanceof Connector) {
                                            ((Connector)connector).reroute();
                                        }
                                    }
                                }
                            }
                        }
                    } /* end of run() method */
                }; /* end of Runnable definition. */
            if (EventQueue.isDispatchThread()) {
                action.run();
            } else {
                SwingUtilities.invokeLater(action);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the controllers for nodes in this graph.
     *  In this base class, nothing is created.
     *  This is called by the constructor, so derived classes that
     *  override this must be careful not to reference local variables
     *  defined in the derived classes, because the derived classes
     *  will not have been fully constructed by the time this is called.
     */
    protected void _createControllers() {
    }

    // NOTE: The following method name does not have a leading underscore
    // because it is a diva method.

    /** Initialize all interaction on the graph pane. This method
     *  is called by the setGraphPane() method of the superclass.
     *  This initialization cannot be done in the constructor because
     *  the controller does not yet have a reference to its pane
     *  at that time.  Regrettably, the canvas is not yet associated
     *  with the GraphPane, so you can't do any initialization that
     *  involves the canvas.
     */
    protected void initializeInteraction() {
        GraphPane pane = getGraphPane();
        _menuFactory = new SchematicContextMenuFactory(this);
        _menuCreator = new MenuCreator(_menuFactory);
        pane.getBackgroundEventLayer().addInteractor(_menuCreator);
        pane.getBackgroundEventLayer().setConsuming(false);
        if (_configuration != null) {
            // NOTE: The following requires that the configuration be
            // non-null, or it will report an error.
            _menuFactory.addMenuItemFactory(
                    new MenuActionFactory(_openBaseClassAction));
        }
    }

    /** Initialize interactions for the specified controller.  This
     *  method is called when a new controller is constructed. This
     *  base class does nothing, but derived classes may attach interactors
     *  to the specified controller.
     *  @param controller The controller for which to initialize interaction.
     */
    protected void _initializeInteraction(NamedObjController controller) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Currently animated state, if any. */
    protected Figure _animated;

    /** Renderer for animation. */
    protected SelectionRenderer _animationRenderer;

    /** The configure action. */
    protected static ConfigureAction _configureAction
            = new ConfigureAction("Configure");

    /** The interactor for creating context sensitive menus on the
     *  graph itself.
     */
    protected MenuCreator _menuCreator;

    /** The factory belonging to the menu creator. */
    protected PtolemyMenuFactory _menuFactory;

    /** The open base class action. */
    protected OpenBaseClassAction _openBaseClassAction
            = new OpenBaseClassAction();

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The time to sleep upon animation. */
    private long _animationDelay = 0l;

    // The configuration.
    private Configuration _configuration;

    // The graph frame, if there is one.
    private BasicGraphFrame _frame;

    // Flag to prevent double rendering upon setting location.
    private boolean _inValueChanged = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ////////////////////////////////////////////////////////////////////////
    //// OpenBaseClassAction
    
    /** An action that will open the base class of a subclass or the class
     *  of an instance.
     */
    public class OpenBaseClassAction extends FigureAction {

        /** Construct a new action.
         *  @param description A description.
         */
        public OpenBaseClassAction() {
            super("Open Base Class");
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Open the base class of a subclass or the class of an instance.
         *  @param e The event.
         */
        public void actionPerformed(ActionEvent e) {
            if (_configuration == null) {
                MessageHandler.error(
                        "Cannot open base class without a configuration.");
                return;
            }

            // Determine which entity was selected for the look inside action.
            super.actionPerformed(e);

            NamedObj target = getTarget();
            if (target == null) return;
            
            try {
                if (target instanceof Prototype) {
                    Prototype deferTo = ((Prototype)target).getDeferTo();
                    if (deferTo != null) {
                        _configuration.openModel(deferTo);
                        return;
                    }
                }
                NamedObj.MoMLInfo info = target.getMoMLInfo();
                if (info.source != null && !info.source.trim().equals("")) {
                    // FIXME: Is there a more reasonable base directory
                    // to give for the second argument?
                    URL sourceURL = StringUtilities.stringToURL(
                            info.source,
                            null,
                            target.getClass().getClassLoader());
                    _configuration.openModel(null, sourceURL, info.source);
                    return;
                }
                // Target does not defer and does not have a defined "source".
                // Assume its base class is a Java class and open the source
                // code.
                String sourceFileName
                        = StringUtilities.objectToSourceFileName(target);
                URL sourceURL
                        = target.getClass().getClassLoader()
                        .getResource(sourceFileName);
                _configuration.openModel(
                        null, sourceURL, sourceURL.toExternalForm());
            } catch (Exception ex) {
                MessageHandler.error("Open base class failed.", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// SchematicContextMenuFactory

    /** Factory for context menus. */
    public static class SchematicContextMenuFactory
            extends PtolemyMenuFactory {

        /** Create a new context menu factory associated with the
         *  specified controller.
         *  @param controller The controller.
         */
        public SchematicContextMenuFactory(GraphController controller) {
            super(controller);
            addMenuItemFactory(new MenuActionFactory(_configureAction));
        }

        protected NamedObj _getObjectFromFigure(Figure source) {
            return (NamedObj)getController().getGraphModel().getRoot();
        }
    }
}
