/* The edge controller for relations in an ontology.

 Copyright (c) 1998-2010 The Regents of the University of California.
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

import java.awt.Color;
import java.awt.Font;
import java.util.List;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.DoubleToken;
import ptolemy.data.ontologies.ConceptRelation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.vergil.modal.Arc;
import ptolemy.vergil.modal.TransitionController;
import diva.canvas.Site;
import diva.canvas.connector.ArcConnector;
import diva.canvas.connector.Arrowhead;
import diva.canvas.connector.Connector;
import diva.canvas.toolbox.LabelFigure;
import diva.graph.EdgeRenderer;
import diva.graph.GraphController;
import diva.graph.JGraph;

///////////////////////////////////////////////////////////////////
//// RelationController

/**
 This class provides interaction techniques for relations in an ontology.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class RelationController extends TransitionController {
    /** Create a transition controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public RelationController(final GraphController controller) {
        super(controller);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do not hot keys for the transition. We don't have any.
    *  @param jgraph The JGraph to which hot keys are to be added.
    */
    public void addHotKeys(JGraph jgraph) {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create an edge renderer specifically for relations in an ontology.
     */
    protected void _createEdgeRenderer() {
        setEdgeRenderer(new LinkRenderer());
    }

    /** Do not set up look inside actions, which are not used in ontology
     *  editors.
     */
    protected void _setUpLookInsideAction() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private static Font _labelFont = new Font("SansSerif", Font.PLAIN, 10);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Renderer for a relation in an ontology.
     */
    public static class LinkRenderer implements EdgeRenderer {

        /** Render a visual representation of the given edge. */
        public Connector render(Object edge, Site tailSite, Site headSite) {
            ArcConnector c = new ArcConnector(tailSite, headSite);
            // Make the default curvature zero.
            // NOTE: Comment below says it won't be drawn if zero!!! Why?
            // Seems like a bug in diva.
            c.setAngle(Math.PI / 999.0);
            Arrowhead arrowhead = new Arrowhead();
            c.setHeadEnd(arrowhead);
            c.setLineWidth((float) 2.0);
            c.setUserObject(edge);

            Arc arc = (Arc) edge;
            ConceptRelation relation = (ConceptRelation) arc.getRelation();

            // When first dragging out a relation, the relation
            // may still be null.
            if (relation != null) {

                c.setToolTipText(relation.getName());

                try {
                    double exitAngle = ((DoubleToken) (relation.exitAngle
                            .getToken())).doubleValue();

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

                    if ((exitAngle < minimum) && (exitAngle > -minimum)) {
                        if (exitAngle > 0.0) {
                            exitAngle = minimum;
                        } else {
                            exitAngle = -minimum;
                        }
                    }

                    c.setAngle(exitAngle);
                    
                    List<ColorAttribute> colors = relation.attributeList(ColorAttribute.class);
                    if (colors != null && colors.size() > 0) {
                        // Use the first color only if there is more than one.
                        c.setStrokePaint(colors.get(0).asColor());
                    }
                } catch (IllegalActionException ex) {
                    // Ignore, accepting the default.
                    // This exception should not occur.
                }

                String labelStr = relation.getLabel();
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
}
