/* The edge controller for transitions in an FSM.

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

package ptolemy.vergil.fsm;

import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.connector.ArcConnector;
import diva.canvas.connector.ArcManipulator;
import diva.canvas.connector.Arrowhead;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorAdapter;
import diva.canvas.connector.ConnectorEvent;
import diva.canvas.connector.ConnectorListener;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.connector.ConnectorTarget;
import diva.canvas.connector.PerimeterTarget;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.interactor.ActionInteractor;
import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.Manipulator;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.BasicEdgeController;
import diva.graph.EdgeRenderer;
import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.gui.toolbox.MenuCreator;
import diva.gui.toolbox.MenuFactory;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.*;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.toolbox.EditParametersFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Paint;
import java.awt.event.ActionEvent;

//////////////////////////////////////////////////////////////////////////
//// TransitionController
/**
This class provides interaction techniques for transitions in an FSM.

@author Steve Neuendorffer, Contributor: Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class TransitionController extends BasicEdgeController {

    /** Create a transition controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public TransitionController(final GraphController controller) {
	super(controller);
	SelectionModel sm = controller.getSelectionModel();
	SelectionInteractor interactor =
            (SelectionInteractor) getEdgeInteractor();
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
	setEdgeRenderer(new LinkRenderer());

	_menuCreator = new MenuCreator(null);
	interactor.addInteractor(_menuCreator);

        // The contents of the menu is determined by the associated
        // menu factory, which is a protected member of this class.
        // Derived classes can add menu items to it.
        _menuFactory = new PtolemyMenuFactory(controller);
        _menuFactory.addMenuItemFactory(new EditParametersFactory("Configure"));
        _menuCreator.setMenuFactory(_menuFactory);

        // Add a double click interactor.
        Action action = new AbstractAction("Configure") {
	    public void actionPerformed(ActionEvent e) {
                LayerEvent event = (LayerEvent)e.getSource();
                Figure figure = event.getFigureSource();
                Object object = figure.getUserObject();
                GraphModel graphModel = controller.getGraphModel();
                NamedObj target =
                    (NamedObj)graphModel.getSemanticObject(object);
                // Create a dialog for configuring the object.
                Component pane = controller.getGraphPane().getCanvas();
                while (pane.getParent() != null) {
                    pane = pane.getParent();
                }
                if (pane instanceof Frame) {
                    // The first argument below is the parent window
                    // (a Frame), which ensures that if this is iconified
                    // or sent to the background, it can be found again.
                    new EditParametersDialog((Frame)pane, target);
                } else {
                    new EditParametersDialog(null, target);
                }
	    }
	};
        ActionInteractor doubleClickInteractor = new ActionInteractor(action);
        doubleClickInteractor.setConsuming(false);
        doubleClickInteractor.setMouseFilter(new MouseFilter(1, 0, 0, 2));

        interactor.addInteractor(doubleClickInteractor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the configuration.  This is may be used by derived controllers
     *  to open files or URLs.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected members                     ////

    /** The configuration. */
    protected Configuration _configuration;

    /** The menu creator. */
    protected MenuCreator _menuCreator;

    /** The factory belonging to the menu creator. */
    protected PtolemyMenuFactory _menuFactory;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static Font _labelFont = new Font("SansSerif", Font.PLAIN, 10);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An inner class that handles interactive changes to connectivity. */
    protected class LinkDropper extends ConnectorAdapter {

        /** Called when a connector end is dropped.  Attach or
         *  detach the edge as appropriate.
         */
        public void connectorDropped(ConnectorEvent evt) {
            Connector c = evt.getConnector();
            Figure f = evt.getTarget();
            Object edge = c.getUserObject();
            Object node = (f == null) ? null : f.getUserObject();
            FSMGraphModel model =
		(FSMGraphModel) getController().getGraphModel();

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
	    // Make the arc rerender itself so that geometry is preserved
	    Arc arc = (Arc) edge;
	    Transition transition = (Transition)arc.getRelation();
	    if (transition != null && c instanceof ArcConnector) {
		double angle = ((ArcConnector)c).getAngle();
		double gamma = ((ArcConnector)c).getGamma();
		// Set the new exitAngle and gamma parameter values based
		// on the current arc.
		String moml = "<group><property name=\"exitAngle\" value=\""
		    + angle + "\"/>" +
		    "<property name=\"gamma\" value=\"" + gamma + "\"/></group>";
		MoMLChangeRequest request = new MoMLChangeRequest(
								  this, transition, moml);
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

    public class LinkRenderer implements EdgeRenderer {

	/** Render a visual representation of the given edge. */
        public Connector render(Object edge, Site tailSite, Site headSite) {
            ArcConnector c = new ArcConnector(tailSite, headSite);
            c.setHeadEnd(new Arrowhead());
            c.setLineWidth((float)2.0);
            c.setUserObject(edge);
            Arc arc = (Arc) edge;
            Transition transition = (Transition)arc.getRelation();
	    if (transition != null) {
		c.setToolTipText(transition.getName());
                String labelStr = transition.getLabel();
                try {
                    double exitAngle = ((DoubleToken)(transition.exitAngle
                            .getToken())).doubleValue();
                    // If the angle is too large, then truncate it to
                    // a reasonable value.
                    double maximum = 99.0*Math.PI;
                    if (exitAngle > maximum) {
                        exitAngle = maximum;
                    } else if (exitAngle < -maximum) {
                        exitAngle = -maximum;
                    }
                    // If the angle is zero, then the arc does not get
                    // drawn.  So we restrict it so that it can't quite
                    // go to zero.
                    double minimum = Math.PI/999.0;
                    if (exitAngle < minimum && exitAngle > -minimum) {
                        if (exitAngle > 0.0) {
                            exitAngle = minimum;
                        } else {
                            exitAngle = - minimum;
                        }
                    }
                    c.setAngle(exitAngle);

		    // Set the gamma angle
                    double gamma = ((DoubleToken)(transition.gamma
                            .getToken())).doubleValue();
                    c.setGamma(gamma);

                } catch (IllegalActionException ex) {
                    // Ignore, accepting the default.
                    // This exception should not occur.
                }
                if (!labelStr.equals("")) {
                    // FIXME: get label position modifier, if any.
                    LabelFigure label = new LabelFigure(
                            labelStr, _labelFont);
                    label.setFillPaint(Color.black);
                    c.setLabelFigure(label);
                }
            }
            return c;
        }
    }

    public class LinkTarget extends PerimeterTarget {
        public boolean acceptHead(Connector c, Figure f) {
            Object object = f.getUserObject();
   	    if (object instanceof Locatable) {
                Locatable location = (Locatable)object;
                if (location.getContainer() instanceof Entity)
                    return true;
                else
                    return false;
            }
	    return false;
        }

        public boolean acceptTail(Connector c, Figure f) {
            return acceptHead(c, f);
        }
    }
}
