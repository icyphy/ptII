/* The edge controller for transitions in an FSM.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.modal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import ptolemy.actor.TypedActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.data.DoubleToken;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.PopupMouseFilter;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.toolbox.ConfigureAction;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.ArcConnector;
import diva.canvas.connector.ArcManipulator;
import diva.canvas.connector.Arrowhead;
import diva.canvas.connector.Blob;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorAdapter;
import diva.canvas.connector.ConnectorEvent;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.connector.ConnectorTarget;
import diva.canvas.connector.PerimeterTarget;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.ActionInteractor;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.BasicEdgeController;
import diva.graph.EdgeRenderer;
import diva.graph.GraphController;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;
import diva.gui.toolbox.MenuCreator;

///////////////////////////////////////////////////////////////////
//// TransitionController

/**
 This class provides interaction techniques for transitions in an FSM.

 @author Steve Neuendorffer, Contributor: Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class TransitionController extends BasicEdgeController {
    /** Create a transition controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public TransitionController(final GraphController controller) {
        super(controller);

        SelectionModel sm = controller.getSelectionModel();
        SelectionInteractor interactor = (SelectionInteractor) getEdgeInteractor();
        interactor.setSelectionModel(sm);

        // Create and set up the manipulator for connectors.
        // This overrides the manipulator created by the base class.
        ConnectorManipulator manipulator = new ArcManipulator();
        manipulator.setSnapHalo(4.0);
        manipulator.addConnectorListener(new LinkDropper());
        interactor.setPrototypeDecorator(manipulator);

        // The mouse filter needs to accept regular click or control click
        MouseFilter handleFilter = new MouseFilter(1, 0, 0);
        manipulator.setHandleFilter(handleFilter);

        ConnectorTarget ct = new LinkTarget();
        setConnectorTarget(ct);
        _createEdgeRenderer();

        _menuCreator = new MenuCreator(null);
        _menuCreator.setMouseFilter(new PopupMouseFilter());
        interactor.addInteractor(_menuCreator);

        // The contents of the menu is determined by the associated
        // menu factory, which is a protected member of this class.
        // Derived classes can add menu items to it.
        _menuFactory = new PtolemyMenuFactory(controller);
        _configureMenuFactory = new MenuActionFactory(_configureAction);
        _menuFactory.addMenuItemFactory(_configureMenuFactory);
        _menuCreator.setMenuFactory(_menuFactory);

        // Add a double click interactor.
        ActionInteractor doubleClickInteractor = new ActionInteractor(
                _configureAction);
        doubleClickInteractor.setConsuming(false);
        doubleClickInteractor.setMouseFilter(new MouseFilter(1, 0, 0, 2));

        interactor.addInteractor(doubleClickInteractor);

        _setUpLookInsideAction();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add hot keys to the actions in the given JGraph.
     *   It would be better that this method was added higher in the hierarchy. Now
     *   most controllers
     *  @param jgraph The JGraph to which hot keys are to be added.
     */
    public void addHotKeys(JGraph jgraph) {
        GUIUtilities.addHotKey(jgraph, _lookInsideAction);
    }

    /** Set the configuration.  This is may be used by derived controllers
     *  to open files or URLs.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;

        _setUpLookInsideAction();
    }

    ///////////////////////////////////////////////////////////////////
    ////                  public inner classes                     ////

    /** Render a link.
     */
    public static class LinkRenderer implements EdgeRenderer {
        /** Render a visual representation of the given edge. */
        @Override
        public Connector render(Object edge, Site tailSite, Site headSite) {
            ArcConnector c = new ArcConnector(tailSite, headSite);
            c.setLineWidth((float) 2.0);
            c.setUserObject(edge);

            Link link = (Link) edge;
            Transition transition = (Transition) link.getRelation();

            // When first dragging out a transition, the relation
            // may still be null.
            if (transition != null) {
                boolean isHistory = false;
                try {
                    isHistory = transition.isHistory();
                } catch (IllegalActionException e2) {
                    // Ignore and render as a non-history transition.
                }
                if (isHistory) {
                    Blob blob = new Blob(0, 0, 0, Blob.ARROW_CIRCLE_H, 6.0,
                            Color.white);
                    c.setHeadEnd(blob);
                } else {
                    Arrowhead arrowhead = new Arrowhead();
                    c.setHeadEnd(arrowhead);
                }

                try {
                    if (transition.isPreemptive() && !transition.isImmediate()) {
                        Blob blob = new Blob(0, 0, 0, Blob.BLOB_CIRCLE, 4.0,
                                Color.red);
                        blob.setFilled(true);
                        c.setTailEnd(blob);
                    } else if (transition.isImmediate()
                            && !transition.isPreemptive()) {
                        Blob blob = new Blob(0, 0, 0, Blob.BLOB_DIAMOND, 5.0,
                                Color.red);
                        blob.setFilled(true);
                        c.setTailEnd(blob);
                    } else if (transition.isImmediate()
                            && transition.isPreemptive()) {
                        Blob blob = new Blob(0, 0, 0, Blob.BLOB_CIRCLE_DIAMOND,
                                5.0, Color.red);
                        blob.setFilled(true);
                        c.setTailEnd(blob);
                    } else if (transition.isErrorTransition()) {
                        Blob blob = new Blob(0, 0, 0, Blob.STAR, 5.0, Color.red);
                        blob.setFilled(true);
                        c.setTailEnd(blob);
                    } else if (transition.isTermination()) {
                        Blob blob = new Blob(0, 0, 0, Blob.TRIANGLE, 5.0,
                                Color.green);
                        blob.setFilled(true);
                        c.setTailEnd(blob);
                    }
                } catch (IllegalActionException ex) {
                    Blob blob = new Blob(0, 0, 0, Blob.ERROR, 5.0, Color.red);
                    blob.setFilled(true);
                    c.setTailEnd(blob);
                }
                if (transition.isNondeterministic()) {
                    c.setStrokePaint(Color.RED);
                }
                try {
                    if (transition.isDefault()) {
                        float[] dashvalues = new float[2];
                        dashvalues[0] = (float) 2.0;
                        dashvalues[1] = (float) 2.0;
                        Stroke dashed = new BasicStroke(1.0f,
                                BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                                0, dashvalues, 0);
                        c.setStroke(dashed);
                    }
                } catch (IllegalActionException e) {
                    // Ignore and don't render dashed line if default parameter fails to evaluate.
                }

                try {
                    TypedActor[] refinements = transition.getRefinement();
                    if (refinements != null && refinements.length > 0) {
                        c.setLineWidth(4.0f);
                    }
                } catch (IllegalActionException e1) {
                    // Ignore. Unable to get refinement.
                }

                c.setToolTipText(transition.getName());

                String labelStr = transition.getLabel();

                try {
                    double exitAngle = ((DoubleToken) transition.exitAngle
                            .getToken()).doubleValue();

                    // If the angle is too large, then truncate it to
                    // a reasonable value.
                    double maximum = 99.0 * Math.PI;

                    if (exitAngle > maximum) {
                        exitAngle = maximum;
                    } else if (exitAngle < -maximum) {
                        exitAngle = -maximum;
                    }

                    // If the angle is zero, then the link does not get
                    // drawn.  So we restrict it so that it can't quite
                    // go to zero.
                    double minimum = Math.PI / 999.0;

                    if (exitAngle < minimum && exitAngle > -minimum) {
                        if (exitAngle > 0.0) {
                            exitAngle = minimum;
                        } else {
                            exitAngle = -minimum;
                        }
                    }

                    c.setAngle(exitAngle);

                    // Set the gamma angle
                    double gamma = ((DoubleToken) transition.gamma.getToken())
                            .doubleValue();
                    c.setGamma(gamma);
                } catch (IllegalActionException ex) {
                    // Ignore, accepting the default.
                    // This exception should not occur.
                }

                if (!labelStr.equals("")) {
                    // FIXME: get label position modifier, if any.
                    LabelFigure label = new LabelFigure(labelStr, _labelFont);
                    label.setFillPaint(Color.black);
                    c.setLabelFigure(label);
                }
            }

            return c;
        }
    }

    /** A Link target.
     */
    public static class LinkTarget extends PerimeterTarget {
        @Override
        public boolean acceptHead(Connector c, Figure f) {
            Object object = f.getUserObject();

            if (object instanceof Locatable) {
                Locatable location = (Locatable) object;

                if (location.getContainer() instanceof Entity) {
                    return true;
                } else {
                    return false;
                }
            }

            return false;
        }

        @Override
        public boolean acceptTail(Connector c, Figure f) {
            return acceptHead(c, f);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create an edge renderer specifically for instances of Transition.
     */
    protected void _createEdgeRenderer() {
        setEdgeRenderer(new LinkRenderer());
    }

    /** Open the instance or the model.  In this base class, the default
     *  look inside action is to open the model.  In derived classes such
     *  as the Ptera editor, the default action is to open the instance.
     *  @param configuration  The configuration with which to open the model or instance.
     *  @param refinement The model or instance to open.
     *  @exception IllegalActionException If constructing an effigy or tableau
     *   fails.
     *  @exception NameDuplicationException If a name conflict occurs (this
     *   should not be thrown).
     *  @see ptolemy.actor.gui.Configuration#openInstance(NamedObj)
     *  @see ptolemy.actor.gui.Configuration#openModel(NamedObj)
     */
    protected void _openInstanceOrModel(Configuration configuration,
            NamedObj refinement) throws IllegalActionException,
            NameDuplicationException {
        configuration.openModel(refinement);
    }

    /** Set up look inside actions, if appropriate.
     */
    protected void _setUpLookInsideAction() {
        if (_configuration != null && _lookInsideActionFactory == null) {
            _lookInsideActionFactory = new MenuActionFactory(_lookInsideAction);
            // NOTE: The following requires that the configuration be
            // non-null, or it will report an error.
            _menuFactory.addMenuItemFactory(_lookInsideActionFactory);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected members                     ////

    /** The configuration. */
    protected Configuration _configuration;

    /** The configure action, which handles edit parameters requests. */
    protected static ConfigureAction _configureAction = new ConfigureAction(
            "Configure");

    /** The submenu for configure actions. */
    protected MenuActionFactory _configureMenuFactory;

    /** The action that handles look inside. */
    protected LookInsideAction _lookInsideAction = new LookInsideAction();

    /** The menu factory for _lookInsideAction. null if the factory has not been
    added to the context menu. */
    protected MenuActionFactory _lookInsideActionFactory;

    /** The menu creator. */
    protected MenuCreator _menuCreator;

    /** The factory belonging to the menu creator. */
    protected PtolemyMenuFactory _menuFactory;

    ///////////////////////////////////////////////////////////////////
    ////               protected inner classes                     ////

    /** An inner class that handles interactive changes to connectivity. */
    protected class LinkDropper extends ConnectorAdapter {
        /** Called when a connector end is dropped.  Attach or
         *  detach the edge as appropriate.
         */
        @Override
        public void connectorDropped(ConnectorEvent evt) {
            Connector c = evt.getConnector();
            Figure f = evt.getTarget();
            Object edge = c.getUserObject();
            Object node = f == null ? null : f.getUserObject();
            FSMGraphModel model = (FSMGraphModel) getController()
                    .getGraphModel();

            switch (evt.getEnd()) {
            case ConnectorEvent.HEAD_END:
                model.getArcModel().setHead(edge, node);
                break;

            case ConnectorEvent.TAIL_END:
                model.getArcModel().setTail(edge, node);
                break;

            case ConnectorEvent.MIDPOINT:
                break;

            default:
                throw new IllegalStateException(
                        "Cannot handle both ends of an edge being dragged.");
            }

            // Make the link rerender itself so that geometry is preserved
            Link link = (Link) edge;
            ComponentRelation transition = link.getRelation();

            if (transition != null && c instanceof ArcConnector) {
                double angle = ((ArcConnector) c).getAngle();
                double gamma = ((ArcConnector) c).getGamma();

                // Set the new exitAngle and gamma parameter values based
                // on the current link. These will be created if they
                // don't already exist.
                String moml = "<group><property name=\"exitAngle\" value=\""
                        + angle + "\" class=\"ptolemy.data.expr.Parameter\"/>"
                        + "<property name=\"gamma\" value=\"" + gamma
                        + "\" class=\"ptolemy.data.expr.Parameter\"/></group>";
                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        transition, moml);
                transition.requestChange(request);
            }

            // rerender the edge.  This is necessary for several reasons.
            // First, the edge is only associated with a relation after it
            // is fully connected.  Second, edges that aren't
            // connected should be erased (which this will rather
            // conveniently take care of for us).
            getController().rerenderEdge(edge);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An action to look inside a transition at its refinement, if it has one.
     *  NOTE: This requires that the configuration be non null, or it
     *  will report an error with a fairly cryptic message.
     */
    @SuppressWarnings("serial")
    private class LookInsideAction extends FigureAction {
        public LookInsideAction() {
            super("Look Inside");

            // If we are in an applet, so Control-L or Command-L will
            // be caught by the browser as "Open Location", so we don't
            // supply Control-L or Command-L as a shortcut under applets.
            if (!StringUtilities.inApplet()) {
                // For some inexplicable reason, the I key doesn't work here.
                // So we use L.
                putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                        KeyEvent.VK_L, Toolkit.getDefaultToolkit()
                        .getMenuShortcutKeyMask()));
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (_configuration == null) {
                MessageHandler
                .error("Cannot look inside without a configuration.");
                return;
            }

            try {
                super.actionPerformed(e);

                NamedObj target = getTarget();

                // If the target is not an instance of
                // State or Transition, do nothing.

                TypedActor[] refinements = null;

                if (target instanceof Transition) {
                    refinements = ((Transition) target).getRefinement();
                } else if (target instanceof State) {
                    refinements = ((State) target).getRefinement();
                }

                if (refinements != null && refinements.length > 0) {
                    for (TypedActor refinement : refinements) {
                        // Open each refinement.
                        // Derived classes may open the instance, this class opens the model.
                        _openInstanceOrModel(_configuration,
                                (NamedObj) refinement);
                    }
                } else {
                    MessageHandler.error("No refinement.");
                }
            } catch (Exception ex) {
                MessageHandler.error("Look inside failed: ", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static Font _labelFont = new Font("SansSerif", Font.PLAIN, 10);
}
