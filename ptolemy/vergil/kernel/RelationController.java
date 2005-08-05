/* The node controller for relations (and vertices)

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.vergil.kernel;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Line2D;

import javax.swing.SwingConstants;

import ptolemy.actor.IORelation;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.Vertex;
import ptolemy.vergil.VergilPreferences;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.ParameterizedNodeController;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.LabelFigure;
import diva.canvas.toolbox.SVGUtilities;
import diva.graph.GraphController;
import diva.graph.NodeRenderer;
import diva.util.java2d.Polygon2D;

//////////////////////////////////////////////////////////////////////////
//// RelationController

/**
 This class provides interaction with nodes that represent Ptolemy II
 relations.  It provides a double click binding to edit the parameters
 of the relation, and a context menu containing a command to edit parameters
 ("Configure"), and a command to get documentation.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class RelationController extends ParameterizedNodeController {
    /** Create a relation controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public RelationController(GraphController controller) {
        super(controller);
        setNodeRenderer(new RelationRenderer());

        // Add to the context menu.
        _menuFactory.addMenuItemFactory(new MenuActionFactory(
                new GetDocumentationAction()));
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    private static Font _relationLabelFont = new Font("SansSerif", Font.PLAIN,
            10);

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The renderer for relation node.  This class creates a Figure that
     *  looks like a black diamond.
     */
    public class RelationRenderer implements NodeRenderer {

        /**
         * Render a visual representation of the given node.
         * @param node The node to render.
         * @return The persistent object that is drawn on the screen.
         */  
        public Figure render(Object node) {
            // Default values.
            double height = 12.0;
            double width = 12.0;
            
            Relation relation = null;
            if (node != null) {
                Vertex vertex = (Vertex) node;
                relation = (Relation) vertex.getContainer();
                
                // NOTE: The preferences mechanism may set this.
                Token relationSize = VergilPreferences.preferenceValue(relation, "_relationSize");
                if (relationSize instanceof DoubleToken) {
                    height = ((DoubleToken)relationSize).doubleValue();
                    width = height;
                }
            }

            Polygon2D.Double polygon = new Polygon2D.Double();
            polygon.moveTo(width / 2, 0);
            polygon.lineTo(0, height / 2);
            polygon.lineTo(-width / 2, 0);
            polygon.lineTo(0, -height / 2);
            polygon.closePath();

            Figure figure = new BasicFigure(polygon, Color.black);

            if (node != null) {
                ActorGraphModel model = (ActorGraphModel) getController()
                        .getGraphModel();
                figure
                        .setToolTipText(relation.getName(model
                                .getPtolemyModel()));

                StringAttribute colorAttr = (StringAttribute) (relation
                        .getAttribute("_color"));

                if (colorAttr != null) {
                    String color = colorAttr.getExpression();
                    ((BasicFigure) figure).setFillPaint(SVGUtilities
                            .getColor(color));
                }
            }

            CompositeFigure result = new CompositeFigure(figure);

            if (relation instanceof IORelation) {
                if (((IORelation) relation).getWidth() > 1) {
                    // Restore width and height to the default to get a reasonable slash.
                    width = 12.0;
                    height = 12.0;
                    Line2D.Double line = new Line2D.Double(-width / 2, height / 2,
                            width / 2, -height / 2);
                    Figure lineFigure = new BasicFigure(line, Color.black);
                    result.add(lineFigure);

                    LabelFigure label = new LabelFigure(""
                            + ((IORelation) relation).getWidth(),
                            _relationLabelFont, 0, SwingConstants.SOUTH_WEST);
                    label.translateTo(width / 2 + 1.0, -height / 2 - 1.0);
                    result.add(label);
                }
            }
            return result;
        }
    }
}
