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
import javax.swing.SwingUtilities;

import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.graph.MutableGraphModel;
import diva.graph.basic.BasicGraphController;
import diva.graph.basic.BasicGraphModel;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.RandomLayout;
import diva.graph.modular.CompositeNode;
import diva.graph.modular.Edge;
import diva.graph.modular.Node;
import diva.gui.AppContext;
import diva.gui.BasicFrame;

/**
 * This example shows three alternatives to display a prepopulated
 * graph model in a window.  Prepopulated means that there are
 * already nodes in the model before the model was placed in a
 * GraphPane. There was a problem in the initial release which made
 * this break. That problem has been fixed and it will just work now,
 * except for one little gotcha, which is if you set the model while
 * the window is closed, the GraphPane thinks its size is 0x0, so the
 * nodes all get layed out in the upper-left corner of the canvas. The
 * way to fix this is to either set the model once the window is open,
 * or explicitly call a global layout once the window has been opened.
 * More comments below in the individual methods.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class NestedTutorial {
    public static void main(String argv[]) {
        final AppContext context = new BasicFrame("Nested Tutorial");
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new NestedTutorial(context);
                    context.setVisible(true);
                }
            });
    }

    private NestedTutorial(AppContext context) {
        // Construct the prepopulated model
        //
        //        A
        //       / \
        //      B   C
        //
        BasicGraphModel model = new BasicGraphModel();
        Node a = model.createNode("a");
        Node b = model.createNode("b");
        CompositeNode c = model.createComposite("c");
        CompositeNode c2 = model.createComposite("c2");
        Node d = model.createNode("d");
        Node d2 = model.createNode("d2");
        model.addNode(this,a, model.getRoot());
        model.addNode(this,c, model.getRoot());
        model.addNode(this,b, model.getRoot());
        model.addNode(this,c2, model.getRoot());
        model.addNode(this,d, c);
        model.addNode(this,d2, c);
        Edge e = model.createEdge("edge");
        model.connectEdge(this,e,a,b);
        e = model.createEdge("edge");
        model.connectEdge(this,e,a,d);
        e = model.createEdge("edge");
        model.connectEdge(this,e,d2,d);
        e = model.createEdge("edge");
        model.connectEdge(this,e,a,c);
        e = model.createEdge("edge");
        model.connectEdge(this,e,c2,c);

        try {
            layoutPostDisplay(model,context);
        }
        catch(Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * In this version you construct the graph widget with the model,
     * and apply a layout to the graph once the window is showing.  I
     * think the "set model post display" version is preferable, but
     * this might be useful in some cases.
     */
    public void layoutPostDisplay(final MutableGraphModel model,
            AppContext context) {
        final BasicGraphController bgc = new BasicGraphController();
        context.getContentPane().add(
                new JGraph(new GraphPane(bgc, model)));
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    RandomLayout random =
                        new RandomLayout(new BasicLayoutTarget(bgc));
                    random.layout(model.getRoot());
                }
            });
    }
}


