/* This class provides interaction techniques for relations in an ontology.

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
package ptolemy.vergil.ontologies;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ontologies.Concept;
import ptolemy.data.ontologies.ConceptRelation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.PopupMouseFilter;
import ptolemy.vergil.kernel.Link;
import ptolemy.vergil.toolbox.ConfigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.ArcConnector;
import diva.canvas.connector.ArcManipulator;
import diva.canvas.connector.Arrowhead;
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
import diva.gui.toolbox.MenuCreator;

///////////////////////////////////////////////////////////////////
//// ConceptRelationController

/** This class provides interaction techniques for relations in an ontology.

 @author Charles Shelton, Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class ConceptRelationController extends BasicEdgeController {

    /** Create a transition controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public ConceptRelationController(final GraphController controller) {
        super(controller);
        SelectionModel sm = controller.getSelectionModel();
        SelectionInteractor interactor = (SelectionInteractor) getEdgeInteractor();
        interactor.setSelectionModel(sm);

        // Create and set up the manipulator for connectors.
        // This overrides the manipulator created by the base class.
        ConnectorManipulator manipulator = new ArcManipulator();
        manipulator.setSnapHalo(4.0);
        manipulator.addConnectorListener(new RelationDropper());
        interactor.setPrototypeDecorator(manipulator);

        // The mouse filter needs to accept regular click or control click
        MouseFilter handleFilter = new MouseFilter(1, 0, 0);
        manipulator.setHandleFilter(handleFilter);

        ConnectorTarget ct = new RelationTarget();
        setConnectorTarget(ct);
        setEdgeRenderer(new RelationRenderer());

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
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do not add hot keys for the ontology relation. We don't have any.
     *  @param jgraph The JGraph to which hot keys are to be added.
     */
    public void addHotKeys(JGraph jgraph) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The configure action, which handles edit parameters requests. */
    protected final static ConfigureAction _configureAction = new ConfigureAction(
            "Configure");

    /** The submenu for configure actions. */
    protected MenuActionFactory _configureMenuFactory;

    /** The menu creator. */
    protected MenuCreator _menuCreator;

    /** The factory belonging to the menu creator. */
    protected PtolemyMenuFactory _menuFactory;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** */
    private static Font _labelFont = new Font("SansSerif", Font.PLAIN, 10);

    ///////////////////////////////////////////////////////////////////
    ////                         private inner class               ////

    ///////////////////////////////////////////////////////////////////
    //// RelationDropper

    /** An inner class that handles interactive changes to connections between
     *  concepts in the ontology model.
     */
    private class RelationDropper extends ConnectorAdapter {

        /** Called when a connector end is dropped.  Attach or
         *  detach the edge as appropriate.
         *  @param evt The connector event received when a connect end is
         *   dropped.
         */
        @Override
        public void connectorDropped(ConnectorEvent evt) {
            Connector c = evt.getConnector();
            Figure f = evt.getTarget();
            Object edge = c.getUserObject();
            Object node = f == null ? null : f.getUserObject();
            OntologyGraphModel model = (OntologyGraphModel) getController()
                    .getGraphModel();

            switch (evt.getEnd()) {
            case ConnectorEvent.HEAD_END:
                model.getRelationModel().setHead(edge, node);
                break;

            case ConnectorEvent.TAIL_END:
                model.getRelationModel().setTail(edge, node);
                break;

            case ConnectorEvent.MIDPOINT:
                break;

            default:
                throw new IllegalStateException(
                        "Cannot handle both ends of an edge being dragged.");
            }

            // Make the arc rerender itself so that geometry is preserved
            Link link = (Link) edge;
            ConceptRelation transition = (ConceptRelation) link.getRelation();

            if (transition != null && c instanceof ArcConnector) {
                double angle = ((ArcConnector) c).getAngle();
                double gamma = ((ArcConnector) c).getGamma();

                // Set the new exitAngle and gamma parameter values based
                // on the current link.
                String moml = "<group><property name=\"exitAngle\" value=\""
                        + angle + "\"/>" + "<property name=\"gamma\" value=\""
                        + gamma + "\"/></group>";
                MoMLChangeRequest request = new MoMLChangeRequest(this,
                        transition, moml);
                transition.requestChange(request);
            }

            // Rerender the edge.  This is necessary for several reasons.
            // First, the edge is only associated with a relation after it
            // is fully connected.  Second, edges that aren't
            // connected should be erased (which this will rather
            // conveniently take care of for us).
            getController().rerenderEdge(edge);
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// RelationRenderer

    /** Renderer for a relation in an ontology.
     */
    private static class RelationRenderer implements EdgeRenderer {

        /** Render a visual representation of the given ontology relation.
         *  @param edge The edge object that contains a reference to the
         *   ontology model relation.
         *  @param tailSite The site for the tail end of the relation in the
         *   model graph.
         *  @param headSite The site for the head end of the relation in the
         *   model graph.
         *  @return The connector object to be drawn in the model graph.
         */
        @Override
        public Connector render(Object edge, Site tailSite, Site headSite) {
            ArcConnector c = new ArcConnector(tailSite, headSite);
            // For some reason, setting the angle to 0.0 doesn't work.
            // The arc doesn't get drawn. Set it small.
            c.setAngle(Math.PI / 999.0);
            Arrowhead arrowhead = new Arrowhead();
            c.setHeadEnd(arrowhead);
            // FIXME: For book.
            // c.setLineWidth((float) 3.0);
            c.setLineWidth((float) 2.0);
            c.setUserObject(edge);

            Link link = (Link) edge;
            ConceptRelation transition = (ConceptRelation) link.getRelation();

            // When first dragging out a transition, the relation
            // may still be null.
            if (transition != null) {
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

                    // If the angle is zero, then the arc does not get
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

                    // Set the color.
                    c.setStrokePaint(transition.color.asColor());

                    // Set dashed, if appropriate.
                    if (((BooleanToken) transition.dashed.getToken())
                            .booleanValue()) {
                        float[] dashvalues = new float[2];
                        dashvalues[0] = (float) 2.0;
                        dashvalues[1] = (float) 2.0;
                        // FIXME: For book
                        /*
                        Stroke dashed = new BasicStroke(3.0f,
                                BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                                0, dashvalues, 0);
                         */
                        Stroke dashed = new BasicStroke(2.0f,
                                BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                                0, dashvalues, 0);
                        c.setStroke(dashed);
                    }
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

    ///////////////////////////////////////////////////////////////////
    //// RelationTarget

    /** A class for potential ontology relation targets. The target of a
     *  relation should always be a concept in the ontology model.
     */
    private static class RelationTarget extends PerimeterTarget {

        /** Return true if the given connector's head can be attached to
         *  the given figure in the model graph.
         *  @param c The connector object.
         *  @param f The graphical figure object in the model graph.
         *  @return true if the connector head can be attached, false otherwise.
         */
        @Override
        public boolean acceptHead(Connector c, Figure f) {
            return _acceptConnection(c, f, true);
        }

        /** Return true if the given connector's tail can be attached to
         *  the given figure in the model graph.
         *  @param c The connector object.
         *  @param f The graphical figure object in the model graph.
         *  @return true if the connector tail can be attached, false otherwise.
         */
        @Override
        public boolean acceptTail(Connector c, Figure f) {
            return _acceptConnection(c, f, false);
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private methods                   ////

        /** Return true if the given connection can be attached to the given
         *  figure in the model graph.
         *  @param c The connector to be checked.
         *  @param f The figure being attached to the connector.
         *  @param isHead True if the method should check the head connection,
         *   false if the method should check the tail connection.
         *  @return True if the connection can be made, false otherwise.
         */
        private boolean _acceptConnection(Connector c, Figure f, boolean isHead) {
            Object object = f.getUserObject();

            if (object instanceof Locatable) {
                Locatable location = (Locatable) object;
                NamedObj modelElement = location.getContainer();

                // Do not accept the connection if it is a link back to
                // the same concept.
                if (modelElement instanceof Concept) {
                    boolean isSelfLink = _isConnectorHeadOrTailConnectedToConcept(
                            c, (Concept) modelElement, !isHead);
                    return !isSelfLink;
                } else {
                    return false;
                }
            }

            return false;
        }

        /** Return true if the given connector's ontology relation head or tail is
         *  connected to the given ontology concept.
         *  @param connector The connector object that represents the graphical
         *   connection in the ontology model.
         *  @param concept The ontology concept.
         *  @param isHead true if the method should check the connector's head,
         *   false if the method should check the connector's tail.
         *  @return true if the connector's head or tail is connected to the given
         *   ontology concept, false otherwise.
         */
        private boolean _isConnectorHeadOrTailConnectedToConcept(
                Connector connector, Concept concept, boolean isHead) {
            if (connector != null) {
                Site connectionSite = null;
                if (isHead) {
                    connectionSite = connector.getHeadSite();
                } else {
                    connectionSite = connector.getTailSite();
                }
                if (connectionSite != null) {
                    Figure tailFigure = connectionSite.getFigure();
                    if (tailFigure != null) {
                        Object tailObject = tailFigure.getUserObject();
                        if (tailObject instanceof Locatable) {
                            if (concept.equals(((Locatable) tailObject)
                                    .getContainer())) {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }
    }
}
