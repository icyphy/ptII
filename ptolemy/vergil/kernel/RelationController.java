/* The node controller for relations (and vertices)

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

package ptolemy.vergil.kernel;

import diva.canvas.Figure;
import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.SVGUtilities;
import diva.graph.GraphController;
import diva.graph.NodeRenderer;
import diva.util.java2d.Polygon2D;

import ptolemy.kernel.Relation;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.Vertex;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.ParameterizedNodeController;
import ptolemy.vergil.toolbox.MenuActionFactory;

import java.awt.Color;

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
        _menuFactory.addMenuItemFactory(
                new MenuActionFactory(new GetDocumentationAction()));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The renderer for relation node.  This class creates a Figure that
     *  looks like a black diamond.
     */
    public class RelationRenderer implements NodeRenderer {
        public Figure render(Object n) {
            double h = 12.0;
            double w = 12.0;

            Polygon2D.Double polygon = new Polygon2D.Double();
            polygon.moveTo(w/2, 0);
            polygon.lineTo(0, h/2);
            polygon.lineTo(-w/2, 0);
            polygon.lineTo(0, -h/2);
            polygon.closePath();
            Figure figure = new BasicFigure(polygon, Color.black);
            if (n != null) {
                Vertex vertex = (Vertex)n;
                Relation relation = (Relation) vertex.getContainer();
                ActorGraphModel model =
                    (ActorGraphModel)getController().getGraphModel();
                figure.setToolTipText(relation.getName(
                        model.getPtolemyModel()));
                StringAttribute _colorAttr =
                    (StringAttribute) (relation.getAttribute("_color"));
                if (_colorAttr != null) {
                    String _color = _colorAttr.getExpression();
                    ((BasicFigure) figure).setFillPaint(
                        SVGUtilities.getColor(_color));
                }
            }
            return figure;
        }
    }
}
