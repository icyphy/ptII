/* A generic visual notation for all Ptolemy models.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.vergil.ptolemy;

import ptolemy.vergil.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;

import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import ptolemy.actor.gui.Documentation;
import ptolemy.kernel.util.Attribute;


import ptolemy.vergil.graph.*;
import ptolemy.vergil.toolbox.*;

import java.awt.Color;
import java.awt.geom.*;

import diva.canvas.*;
import diva.canvas.connector.*;

import diva.canvas.toolbox.*;
import diva.gui.*;
import diva.graph.*;

import java.awt.Font;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import java.util.*;

/**
 * A visual notation creates views for a ptolemy document in Vergil.
 * This class adds additional visualization for the performance data that
 * comes from compaan.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 */

public class CompaanNotation extends Attribute implements VisualNotation {

    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public CompaanNotation() {
	super();
        _performance = new CompaanPerformance();
    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public CompaanNotation(Workspace workspace) {
	super(workspace);
        setMoMLElementName("notation");
        _performance = new CompaanPerformance();
    }

    /** Construct an attribute with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public CompaanNotation(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
                super(container, name);
        _performance = new CompaanPerformance();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct a view on the given document.  
     */
    public GraphPane createView(Document d) {
	if(d instanceof PtolemyDocument) {
	    return createView((PtolemyDocument) d);
	} 
	throw new InternalErrorException("Ptolemy Notation is only " +
                "compatible with Ptolemy documents.");
    }

    /** Construct a view on the given document.
     */
    public GraphPane createView(PtolemyDocument d) {
	
	// These two things control the view of a ptolemy model.
	EditorGraphController controller = new EditorGraphController();

        // This 
	controller.getEntityController().setNodeRenderer(
                new CompaanEntityRenderer());        

	controller.getLinkController().setEdgeRenderer(
                new CompaanEdgeRenderer());

        // Basically, make sure all Node references should become links.
	CompositeActor toplevel = (CompositeActor)d.getModel();
	PtolemyGraphModel model = 
	    new PtolemyGraphModel(toplevel);
	GraphPane pane = new GraphPane(controller, model);

        Manager manager = toplevel.getManager();
        if(manager == null) {
            try {
                // FIXME manager creation sucks.
                manager =
                    new Manager(toplevel.workspace(), "Manager");
                toplevel.setManager(manager);
                manager.addExecutionListener(
                        new PtolemyModule.VergilExecutionListener(
                                d.getApplication())); 
            }                
            catch (Exception e) {
                d.getApplication().showError("Failed to create Manager", e);
            }
        }

        _listener = new CompaanListener(controller);
        manager.addExecutionListener(_listener);

	return pane;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** */
    private CompaanListener _listener;

    /** The Compaan Performance collector object. */
    private CompaanPerformance _performance = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    /** This inner class implements... */
    private class CompaanListener implements ExecutionListener {

        /** Construct a CompaanListeren wich a particular controller.
         *
         * @param controller the graph controller.
         */
        public CompaanListener(GraphController controller) {
            _controller = controller; 
        }
        

        /** Execute Error is called upon when an error occurs when
         *  executing the model.
         *
         *  @param manager the manager of this model          
         *  @param exeception the exeception that occured when
         *  executing the model.
         */
        public void executionError(Manager manager, Exception exception) {
        }

        /** Execute finished is called upon when the model has
         *  finished execution. This is a moment to obtained global
         *  information about the model, that will influence the way
         *  the model is presented after being rerender.
         * 
         *  @param manager the manager of this model.
         */
        public void executionFinished(Manager manager) {
            if ( manager != null ) {

                // Obtain the global performance metrics of this model.
                _performance.determineGlobalPerformanceMetrics(manager);

                // Anonymous innnerclass to invoke at the appriopriate
                // time the rerendering of the model.
                SwingUtilities.invokeLater(
                        new Runnable() {
                    public void run() {
                        _controller.rerender();
                    }
                });
            }
        }

        /** Manager State Changed is called upon when the state in
         *  which the modek resides has changed. 
         * 
         *  @param manager the manager of this model.
         */
        public void managerStateChanged(Manager manager) { }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        /** The graph controller of this model in vergil. */
        private GraphController _controller;
    }

    /** */
    public class CompaanEntityRenderer implements NodeRenderer {
	public Figure render(Object n) {

	    CompositeFigure figure;
	    EditorIcon icon = (EditorIcon)n;
	    figure = (CompositeFigure)icon.createFigure();
	    Rectangle2D bounds = figure.getBounds();
	    Entity entity = (Entity)icon.getContainer();

            Variable ehrhartVariable = 
                (Variable)entity.getAttribute("ehrhart");
            Color actorColor = _performance.getActorColor( ehrhartVariable );
            figure.setBackgroundFigure(new BasicRectangle(0, 0, 
                    bounds.getWidth(), bounds.getHeight(), actorColor ));
            
            if (ehrhartVariable != null) {
                try {
                    String s = ehrhartVariable.getToken().toString();
                    Figure background = figure.getBackgroundFigure();
                    Rectangle2D backBounds = background.getBounds();
                    LabelFigure label = new LabelFigure("fire: " + s);
                    label.setFont(new Font("SansSerif", Font.PLAIN, 12));
                    label.setPadding(1);
                    // Attach the label at the upper left corner.
                    label.setAnchor(SwingConstants.NORTH_WEST);
                    // Put the label's upper left corner at the lower right
                    // corner of rht efigure.
                    label.translateTo(backBounds.getX(), 
                            backBounds.getY() + backBounds.getHeight());
                    figure.add(label);
                } catch (IllegalActionException e) { }  
            }
	    return figure;
	}
    }
    
    /** */
    public class CompaanEdgeRenderer extends LinkController.LinkRenderer {
	public Connector render(Object n, Site tailSite, Site headSite) {
            AbstractConnector connector = (AbstractConnector) 
                super.render(n, tailSite, headSite);

            // adorn the connector
            // connector.setLabelFigure(new LabelFigure("100.0"));
            // connector.setStrokePaint(Color.blue);
            // connector.setToolTipText("Hello");
            // connector.setLineWidth(10);

            Link link = (Link)n;
            Relation relation = link.getRelation();

            // Set the tooltip for this connector
            Documentation doc = 
                (Documentation)relation.getAttribute("_doc_");
            String message = doc.getValue();
            connector.setToolTipText(message);
            if ( message.endsWith("broadcast") ) {
                connector.setLineWidth(5);
            }
            
            Variable communicationVariable = 
                (Variable)relation.getAttribute("communication");

            // Set the color for this connector
            Color relationColor = _performance.getRelationColor( 
                    communicationVariable );
            connector.setStrokePaint(relationColor);

            if (communicationVariable != null) {
                try {
                    String s = communicationVariable.getToken().toString();
                    LabelFigure label = new LabelFigure(s);
                    label.setFont(new Font("Helvetica", Font.PLAIN, 9));
                    label.setPadding(8);
                    connector.setLabelFigure(label);
                } catch (IllegalActionException e) { }  
            }                       
            return connector;
	}
    }


}
