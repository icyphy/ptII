/*
 * $Id$
 *
@Copyright (c) 1998-2004 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package diva.graph.tutorial;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.graph.MutableGraphModel;
import diva.graph.NodeRenderer;
import diva.graph.basic.BasicGraphController;
import diva.graph.basic.BasicGraphModel;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.basic.BasicNodeRenderer;
import diva.graph.layout.LevelLayout;
import diva.graph.modular.Edge;
import diva.graph.modular.Graph;
import diva.graph.modular.Node;
import diva.graph.toolbox.TypedNodeRenderer;
import diva.gui.AppContext;
import diva.gui.BasicFrame;

/**
 * This tutorial shows how to customize the look of
 * the nodes based on the semantic properties of the
 * nodes.  It creates a graph which has a bunch of
 * nodes with user objects of different types, and
 * renders these nodes according to their types.
 * The rendering looks like this:
 * <ul>
 *   <li> String =>  a blue circle
 *   <li> Set    =>  a red circle
 *   <li> Integer => an orange square
 *   <li> Default => a grey circle
 * </ul>
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Id$
 * @rating Red
 */
public class NodeRendererTutorial {
    /**
     * Instantiate a new tutorial window and
     * display it.
     */
    public static void main(String argv[]) {
        final AppContext context = new BasicFrame("Node Renderer Tutorial");
        context.setSize(800, 600);

        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new NodeRendererTutorial(context);
                    context.setVisible(true);
                }
            });
    }

    private NodeRendererTutorial(AppContext context) {
        final BasicGraphController bgc = new BasicGraphController();

        // Build the renderers
        NodeRenderer defaultRenderer =
            new BasicNodeRenderer(bgc,
                    new Ellipse2D.Double(0.0,0.0,40.0,40.0),
                    new Ellipse2D.Double(0.0,0.0,600.0,600.0),
                    Color.gray, Color.gray, .3);
        NodeRenderer stringRenderer =
            new BasicNodeRenderer(bgc,
                    new Ellipse2D.Double(0.0,0.0,40.0,40.0),
                    new Ellipse2D.Double(0.0,0.0,600.0,600.0),
                    Color.blue, Color.blue, .3);
        NodeRenderer integerRenderer =
            new BasicNodeRenderer(bgc,
                    new Rectangle2D.Double(0.0,0.0,40.0,40.0),
                    new Rectangle2D.Double(0.0,0.0,600.0,600.0),
                    Color.orange, Color.orange, .3);
        NodeRenderer setRenderer =
            new BasicNodeRenderer(bgc,
                    new Ellipse2D.Double(0.0,0.0,40.0,40.0),
                    new Ellipse2D.Double(0.0,0.0,600.0,600.0),
                    Color.red, Color.red, .3);
        TypedNodeRenderer typedRenderer =
            new TypedNodeRenderer(bgc, defaultRenderer);
        typedRenderer.addTypedRenderer(Integer.class, integerRenderer);
        typedRenderer.addTypedRenderer(ArrayList.class, setRenderer);
        typedRenderer.addTypedRenderer(String.class, stringRenderer);

        // Use the renderer in the JGraph
        GraphPane gp = new GraphPane(bgc, new BasicGraphModel());
        bgc.getNodeController().setNodeRenderer(typedRenderer); // <=== HERE!
        JGraph g = new JGraph(gp);

        // Display it all
        context.getContentPane().add("Center", g);

        // Build the model
        final MutableGraphModel model = makeTypedModel();
        bgc.setGraphModel(model);

        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    LevelLayout random =
                        new LevelLayout(new BasicLayoutTarget(bgc));
                    random.layout(model.getRoot());
                }
            });
    }

    /**
     * Construct an interesting graph model with nodes that
     * have user objects of type {String, Set, Integer}
     */
    public MutableGraphModel makeTypedModel() {
        BasicGraphModel model = new BasicGraphModel();
        ArrayList set0 = new ArrayList();
        Object o1 = new Integer(5);
        Object o2 = new Integer(6);
        Object o3 = new Integer(10);
        Object o4 = "foo";
        Object o5 = "bar";
        set0.add(o1);
        set0.add(o2);
        set0.add(o3);
        set0.add(o4);
        set0.add(o5);

        Graph root = (Graph)model.getRoot();
        Node s0 = model.createNode(set0);
        Node n1 = model.createNode(o1);
        Node n2 = model.createNode(o2);
        Node n3 = model.createNode(o3);
        Node n4 = model.createNode(o4);
        Node n5 = model.createNode(o5);
        model.addNode(this, s0, root);
        model.addNode(this, n1, root);
        model.addNode(this, n2, root);
        model.addNode(this, n3, root);
        model.addNode(this, n4, root);
        model.addNode(this, n5, root);

        Edge e1 = model.createEdge("e1");
        Edge e2 = model.createEdge("e2");
        Edge e3 = model.createEdge("e3");
        Edge e4 = model.createEdge("e4");
        Edge e5 = model.createEdge("e5");
        model.connectEdge(this, e1, s0, n1);
        model.connectEdge(this, e2, s0, n2);
        model.connectEdge(this, e3, s0, n3);
        model.connectEdge(this, e4, s0, n4);
        model.connectEdge(this, e5, s0, n5);

        // another cluster
        ArrayList set1 = new ArrayList();
        Object o11 = new Integer(15);
        Object o12 = new Integer(15);
        Node s1 = model.createNode(set1);
        Node n11 = model.createNode(o11);
        Node n12 = model.createNode(o12);
        model.addNode(this, s1, root);
        model.addNode(this, n11, root);
        model.addNode(this, n12, root);

        Edge e6 = model.createEdge("e6");
        Edge e7 = model.createEdge("e7");
        model.connectEdge(this, e6, s1, n11);
        model.connectEdge(this, e7, s1, n12);

        // an empty cluster
        ArrayList set2 = new ArrayList();
        Node s2 = model.createNode(set2);
        model.addNode(this, s2, root);

        return model;
    }
}

