/* Base class for graph controllers in Ptolemy.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DialogTableau;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.InstantiableNamedObj;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.toolbox.ConfigureAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import ptolemy.vergil.unit.UnitSolverDialog;
import diva.canvas.Figure;
import diva.canvas.connector.Connector;
import diva.canvas.interactor.SelectionRenderer;
import diva.graph.AbstractGraphController;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.GraphUtilities;
import diva.graph.JGraph;
import diva.graph.NodeController;
import diva.gui.toolbox.MenuCreator;

///////////////////////////////////////////////////////////////////
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
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
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

    /** Request a change that clears all the error highlights. */
    public void clearAllErrorHighlights() {
        ChangeRequest request = _getClearAllErrorHighlightsChangeRequest();
        _frame.getModel().requestChange(request);
    }

    /** Highlight the specified object and all its containers to
     *  indicate that it is the source of an error.
     *  @param culprit The culprit.
     */
    public void highlightError(final Nameable culprit) {
        if (culprit instanceof NamedObj) {
            ChangeRequest request = new ChangeRequest(this, "Error Highlighter") {
                @Override
                protected void _execute() throws Exception {
                    _addErrorHighlightIfNeeded(culprit);
                    NamedObj container = culprit.getContainer();
                    while (container != null) {
                        _addErrorHighlightIfNeeded(container);
                        container = container.getContainer();
                    }
                }
            };
            request.setPersistent(false);
            ((NamedObj) culprit).requestChange(request);
        }
    }

    /** Add commands to the specified menu and toolbar, as appropriate
     *  for this controller.  In this base class, nothing is added.
     *  @param menu The menu to add to, or null if none.
     *  @param toolbar The toolbar to add to, or null if none.
     */
    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
        _addHotKeys(getFrame().getJGraph());
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
    @Override
    public void event(DebugEvent event) {
    }

    /** Get the time delay for animation.  After highlighting,
     *  derived classes are expected to sleep for the specified amount
     *  of time, in milliseconds.
     *  @return The animation delay set by setAnimationDelay().
     *  @see #setAnimationDelay(long)
     */
    public long getAnimationDelay() {
        return _animationDelay;
    }

    /** Return the configuration that has been specified by setConfiguration(),
     *  or null if none.
     *  @return The configuration.
     *  @see #setConfiguration(Configuration)
     */
    public Configuration getConfiguration() {
        return _configuration;
    }

    /** Return the configuration menu factory.
     *
     *  @return The configuration menu factory.
     */
    public MenuActionFactory getConfigureMenuFactory() {
        return _configureMenuFactory;
    }

    /** Get the graph frame, or null if there is none.  This is used by
     *  some of the controllers to mark the modified bit of the frame
     *  and to update any dependents.
     *  @return The graph frame, or null if there is none.
     *  @see #setFrame(BasicGraphFrame)
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
    @Override
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
                List factoryList = ((NamedObj) semanticObject)
                        .attributeList(NodeControllerFactory.class);

                // FIXME: This is creating a new node controller for each instance!!!
                // This causes problems as indicated by the NOTE in ActorInstanceController.
                if (factoryList.size() > 0) {
                    NodeControllerFactory factory = (NodeControllerFactory) factoryList
                            .get(0);
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
    @Override
    public void message(String message) {
    }

    /** Set the time delay for animation.  After highlighting,
     *  derived classes are expected to sleep for the specified amount
     *  of time, in milliseconds.  If this method is not called, or
     *  is called with argument 0, then no delay is introduced.
     *  @param time Time to sleep, in milliseconds.
     *  @see #getAnimationDelay()

     */
    public void setAnimationDelay(long time) {
        _animationDelay = time;
    }

    /** Set the configuration.  This is used by some of the controllers
     *  when opening files or URLs.
     *  The configuration is checked for a "_getDocumentationActionDocPreference",
     *  which, if present, is an integer that is passed to
     *  {@link ptolemy.vergil.basic.GetDocumentationAction#GetDocumentationAction(int)}.
     *  This attribute is used to select the Kepler-specific
     *  KeplerDocumentationAttribute.
     *  @param configuration The configuration.
     *  @see #getConfiguration()
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;

        if (configuration != null && _getDocumentationAction == null) {
            int docPreference = 0;
            String parameterName = "_getDocumentationActionDocPreference";
            try {
                Parameter getDocumentationActionDocPreference = (Parameter) configuration
                        .getAttribute(parameterName, Parameter.class);
                if (getDocumentationActionDocPreference != null) {
                    // If you want KeplerDocumentationAttribute, set
                    // _getDocumentationActionDocPreference to 1.
                    docPreference = Integer
                            .parseInt(getDocumentationActionDocPreference
                                    .getExpression());
                }
            } catch (Exception ex) {
                System.err.println("Warning, failed to parse " + parameterName);
                ex.printStackTrace();
            }
            _getDocumentationAction = new GetDocumentationAction(docPreference);
        }
        if (_getDocumentationAction != null) {
            _getDocumentationAction.setConfiguration(configuration);
        }

        if (_configuration != null && _menuFactory != null) {
            // NOTE: The following requires that the configuration be
            // non-null, or it will report an error.
            _menuFactory.addMenuItemFactory(new MenuActionFactory(
                    _openBaseClassAction));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the figure associated with the given semantic object, and if
     *  that semantic object is Settable, then set up a value listener
     *  so that if its value changes, then the valueChanged() method
     *  is invoked.
     *  The semantic object is normally an attribute that implements
     *  the Locatable interface, and the value indicates the location
     *  of the object.
     *  A null figure clears the association.
     *  @param semanticObject The semantic object (normally a Locatable).
     *  @param figure The figure.
     */
    @Override
    public void setFigure(Object semanticObject, Figure figure) {
        super.setFigure(semanticObject, figure);

        if (semanticObject instanceof Settable) {
            ((Settable) semanticObject).addValueListener(this);
        }
    }

    /** Set the graph frame.  This is used by some of the controllers
     *  to mark the modified bit of the frame and to update any dependents.
     *  @param frame The graph frame, or null if there is none.
     *  @see #getFrame()
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
    @Override
    public void valueChanged(final Settable settable) {
        if (settable instanceof Locatable && !_inValueChanged) {
            // Have to defer this to the event thread, or repaint
            // doesn't work properly.
            Runnable action = new Runnable() {
                @Override
                public void run() {
                    Locatable location = (Locatable) settable;
                    Figure figure = getFigure(location);
                    if (figure != null) {
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

                        double translationX = newLocation[0]
                                - originalUpperLeftX;
                        double translationY = newLocation[1]
                                - originalUpperLeftY;

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
                                        ((Connector) connector).reroute();
                                    }
                                }

                                Iterator outEdges = model.outEdges(userObject);

                                while (outEdges.hasNext()) {
                                    Figure connector = getFigure(outEdges
                                            .next());

                                    if (connector instanceof Connector) {
                                        ((Connector) connector).reroute();
                                    }
                                }

                                if (model.isComposite(userObject)) {
                                    Iterator edges = GraphUtilities
                                            .partiallyContainedEdges(
                                                    userObject, model);

                                    while (edges.hasNext()) {
                                        Figure connector = getFigure(edges
                                                .next());

                                        if (connector instanceof Connector) {
                                            ((Connector) connector).reroute();
                                        }
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

    /** Add hot keys to the actions in the given JGraph.
     *
     *  @param jgraph The JGraph to which hot keys are to be added.
     */
    protected void _addHotKeys(JGraph jgraph) {
    }

    /** Create the controllers for nodes in this graph.
     *  In this base class, nothing is created.
     *  This is called by the constructor, so derived classes that
     *  override this must be careful not to reference local variables
     *  defined in the derived classes, because the derived classes
     *  will not have been fully constructed by the time this is called.
     */
    protected void _createControllers() {
    }

    /** Return true if there are active highlights.
     *  @return True if the list if error highlights is not empty.
     */
    protected boolean _areThereActiveErrorHighlights() {
        return !_errorHighlights.isEmpty();
    }

    /**
     * Return a change request that clears all the highlights.
     * @return a change request that clears all the highlights.
     */
    protected ChangeRequest _getClearAllErrorHighlightsChangeRequest() {
        ChangeRequest request = new ChangeRequest(this,
                "Error Highlight Clearer", true) {
            @Override
            protected void _execute() throws Exception {
                for (Attribute highlight : _errorHighlights) {
                    highlight.setContainer(null);
                }
            }
        };

        // Mark the Error Highlight Clearer request as
        // non-persistant so that we don't mark the model as being
        // modified.  ptolemy/actor/lib/jni/test/Scale/Scale.xml
        // required this change.
        request.setPersistent(false);
        return request;
    }

    /** Initialize interactions for the specified controller.  This
     *  method is called when a new controller is constructed. This
     *  base class does nothing, but derived classes may attach interactors
     *  to the specified controller.
     *  @param controller The controller for which to initialize interaction.
     */
    protected void _initializeInteraction(NamedObjController controller) {
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
    @Override
    protected void initializeInteraction() {
        // Remove the existing menu if it has already been created by an earlier
        // call of this method, because we may invoke this method multiple times
        // but we don't want the same items to show up multiple times.
        // -- tfeng (07/16/2009)
        _menuFactory = null;

        GraphPane pane = getGraphPane();
        // Start Kepler code.
        List configsList = Configuration.configurations();
        Configuration config = _configuration;
        if (config == null) {
            // Is this really necessary?
            for (Iterator it = configsList.iterator(); it.hasNext();) {
                config = (Configuration) it.next();
                if (config != null) {
                    break;
                }
            }
        }

        // If a MenuFactory has been defined in the configuration, use this
        // one; otherwise, use the default Ptolemy one:
        if (config != null && _contextMenuFactoryCreator == null) {
            _contextMenuFactoryCreator = (ContextMenuFactoryCreator) config
                    .getAttribute("canvasContextMenuFactory");
        }

        // NOTE: by passing "this" to the menu factory, we are making it
        // handle right-click menus for the canvas (only) - not the actors or
        // relations; these are controlled by AttributeController and
        // RelationController, respectively - MB - 2/14/06
        if (_contextMenuFactoryCreator != null) {
            try {
                _menuFactory = (PtolemyMenuFactory) _contextMenuFactoryCreator
                        .createContextMenuFactory(this);
                // this is only done here, not for both MenuFactories, because
                // SchematicContextMenuFactory already does this in its
                // constructor:
                // (Save _configureMenuFactory for use in sub classes)
                _configureMenuFactory = new MenuActionFactory(_configureAction);
                _menuFactory.addMenuItemFactory(_configureMenuFactory);
            } catch (Throwable throwable) {
                // do nothing - will default to ptii right-click menus
                // System.out.println("Unable to use the alternative right-click menu "
                // + "handler that was specified in the "
                // + "configuration; defaulting to ptii handler. "
                // + "Exception was: " + throwable);
            }
        }
        // if the above has failed in any way, _menuFactory will still be null,
        // in which case we should default to ptii context menus
        if (_menuFactory == null) {
            _menuFactory = new SchematicContextMenuFactory(this);
        }
        // End Kepler code.

        _menuCreator = new MenuCreator(_menuFactory);
        _menuCreator.setMouseFilter(new PopupMouseFilter());

        // Note that the menuCreator cannot be an interactor, because
        // it accepts all events.
        // NOTE: The above is a very strange comment, since
        // it is an interactor.  EAL 2/5/05.
        pane.getBackgroundEventLayer().addInteractor(_menuCreator);
        pane.getBackgroundEventLayer().setConsuming(false);

        Action[] actions = { _getDocumentationAction,
                new CustomizeDocumentationAction(),
                new RemoveCustomDocumentationAction() };
        _menuFactory.addMenuItemFactory(new MenuActionFactory(actions,
                "Documentation"));

        if (_configuration != null) {
            // NOTE: The following requires that the configuration be
            // non-null, or it will report an error.
            _menuFactory.addMenuItemFactory(new MenuActionFactory(
                    _openBaseClassAction));
            _menuFactory.addMenuItemFactory(new MenuActionFactory(
                    _unitSolverDialogAction));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Currently animated state, if any. */
    protected Figure _animated;

    /** Renderer for animation. */
    protected SelectionRenderer _animationRenderer;

    /** The configure action. */
    protected static ConfigureAction _configureAction = new ConfigureAction(
            "Configure");

    /** The submenu for configure actions. */
    protected static MenuActionFactory _configureMenuFactory;

    /** The interactor for creating context sensitive menus on the
     *  graph itself.
     */
    protected MenuCreator _menuCreator;

    /** The factory belonging to the menu creator. */
    protected PtolemyMenuFactory _menuFactory;

    /** The open base class action. */
    protected OpenBaseClassAction _openBaseClassAction = new OpenBaseClassAction();

    /** The UnitSolverDialog action. */
    protected UnitSolverDialogAction _unitSolverDialogAction = new UnitSolverDialogAction();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add an error highlight color to the specified culprit if it is
     *  not already present.
     *  @param culprit The culprit to highlight.
     *  @exception IllegalActionException If the highlight cannot be added.
     *  @exception NameDuplicationException Should not be thrown.
     */
    private void _addErrorHighlightIfNeeded(Nameable culprit)
            throws IllegalActionException, NameDuplicationException {
        Attribute highlightColor = ((NamedObj) culprit)
                .getAttribute("_highlightColor");
        if (highlightColor == null) {
            highlightColor = new ColorAttribute((NamedObj) culprit,
                    "_highlightColor");
            ((ColorAttribute) highlightColor)
            .setExpression("{1.0, 0.0, 0.0, 1.0}");
            highlightColor.setPersistent(false);
            ((ColorAttribute) highlightColor).setVisibility(Settable.EXPERT);
            _errorHighlights.add(highlightColor);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The time to sleep upon animation. */
    private long _animationDelay = 0L;

    // The configuration.
    private Configuration _configuration;

    /**
     * A configurable object that allows a different MenuFactory to be specified
     * instead of the default ptII one. The MenuFactory constructs the
     * right-click context menus
     */
    private static ContextMenuFactoryCreator _contextMenuFactoryCreator;

    /** List of error highlight attributes we have created. */
    private List<Attribute> _errorHighlights = new LinkedList<Attribute>();

    // The get documentation action.
    private GetDocumentationAction _getDocumentationAction = new GetDocumentationAction();

    // The graph frame, if there is one.
    private BasicGraphFrame _frame;

    // Flag to prevent double rendering upon setting location.
    private boolean _inValueChanged = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    ///////////////////////////////////////////////////////////////////
    //// OpenBaseClassAction

    /** An action that will open the base class of a subclass or the class
     *  of an instance.
     */
    @SuppressWarnings("serial")
    public class OpenBaseClassAction extends FigureAction {
        /** Construct a new action.
         */
        public OpenBaseClassAction() {
            super("Open Base Class");
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Open the base class of a subclass or the class of an instance.
         *  @param e The event.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (_configuration == null) {
                MessageHandler
                .error("Cannot open base class without a configuration.");
                return;
            }

            // Determine which entity was selected for the look inside action.
            super.actionPerformed(e);

            NamedObj target = getTarget();

            if (target == null) {
                return;
            }

            try {
                if (target instanceof InstantiableNamedObj) {
                    InstantiableNamedObj deferTo = (InstantiableNamedObj) ((InstantiableNamedObj) target)
                            .getParent();

                    if (deferTo != null) {
                        _configuration.openModel(deferTo);
                        return;
                    }
                }

                String source = target.getSource();

                if (source != null && !source.trim().equals("")) {
                    // FIXME: Is there a more reasonable base directory
                    // to give for the second argument?
                    URL sourceURL = FileUtilities.nameToURL(source, null,
                            target.getClass().getClassLoader());
                    _configuration.openModel(null, sourceURL, source);
                    return;
                }

                // Target does not defer and does not have a defined "source".
                // Assume its base class is a Java class and open the source
                // code.
                String sourceFileName = StringUtilities
                        .objectToSourceFileName(target);
                URL sourceURL = target.getClass().getClassLoader()
                        .getResource(sourceFileName);
                _configuration.openModel(null, sourceURL,
                        sourceURL.toExternalForm());
            } catch (Exception ex) {
                MessageHandler.error("Open base class failed.", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// UnitSolverDialogAction

    /** An action that will create a UnitSolverDialog.
     */
    @SuppressWarnings("serial")
    public class UnitSolverDialogAction extends AbstractAction {
        /** Construct an action that will create a UnitSolverDialog.
         */
        public UnitSolverDialogAction() {
            super("UnitConstraints Solver");
        }

        /** Construct a UnitSolverDialog.
         *  @param e The action event, ignored by this method.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            // Only makes sense if this is an ActorGraphFrame.
            if (_frame instanceof ActorGraphFrame) {
                DialogTableau dialogTableau = DialogTableau.createDialog(
                        _frame, _configuration,
                        ((ActorGraphFrame) _frame).getEffigy(),
                        UnitSolverDialog.class,
                        (Entity) ((ActorGraphFrame) _frame).getModel());

                if (dialogTableau != null) {
                    dialogTableau.show();
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// SchematicContextMenuFactory

    /** Factory for context menus. */
    public static class SchematicContextMenuFactory extends PtolemyMenuFactory {
        /** Create a new context menu factory associated with the
         *  specified controller.
         *  @param controller The controller.
         */
        public SchematicContextMenuFactory(GraphController controller) {
            super(controller);
            _configureMenuFactory = new MenuActionFactory(_configureAction);
            addMenuItemFactory(_configureMenuFactory);
        }

        @Override
        protected NamedObj _getObjectFromFigure(Figure source) {
            // NOTE: Between Ptolemy 3.0 and 5.0, this would ignore
            // the source argument, even if it was non-null.  Why?
            // EAL 2/5/05.
            if (source != null) {
                Object object = source.getUserObject();
                return (NamedObj) getController().getGraphModel()
                        .getSemanticObject(object);
            } else {
                return (NamedObj) getController().getGraphModel().getRoot();
            }
        }
    }
}
