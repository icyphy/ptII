/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
  PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
  CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
  ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY
*/
package diva.graph.basic;

import diva.graph.GraphPane;
import diva.graph.JGraph;
import diva.gui.AppContext;
import diva.gui.BasicFrame;

import java.awt.GridLayout;


/**
 * The graph demo demonstrates basic graph editing and layout
 * functionality, illustrates the key points of the graph
 * architecture. A graph is constructed programmatically, and can then
 * be edited interactively by the user. There are two views of the
 * graph: one which has an automatic layout algorithm applied each
 * time a new node is added, and one which uses a random or
 * user-driven layout. <p>
 *
 * The interaction and display in the graph editor, although currently
 * fairly simple, uses the features of the Diva canvas to good
 * effect. The use of two views of the graph highlights the
 * Swing-style model-view-controller architecture of the graph
 * package.
 *
 * @author Michael Shilman
 * @author Steve Neuendorffer
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class BasicGraphDemo {
    /**
     * Construct a new instance of graph demo, which does the work of
     * setting up the graphs and displaying itself.
     */
    public static void main(String[] argv) {
        AppContext context = new BasicFrame("Basic Graph Demo");
        new BasicGraphDemo(context);
    }

    public BasicGraphDemo(AppContext context) {
        final BasicGraphModel model = new BasicGraphModel();
        JGraph jg = new JGraph(new GraphPane(new BasicGraphController(), model));
        JGraph jg2 = new JGraph(new GraphPane(new BasicGraphController(), model));

        context.getContentPane().setLayout(new GridLayout(2, 1));
        context.getContentPane().add(jg);
        context.getContentPane().add(jg2);

        /*
          GraphController controller = jg.getGraphPane().getGraphController();
          final BasicLayoutTarget target = new BasicLayoutTarget(controller);
          JButton but = new JButton("Layout");
          but.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent evt) {
          //                GlobalLayout l = new GridAnnealingLayout();
          GlobalLayout l = new LevelLayout();
          l.layout(target, model.getRoot());
          }
          });
          context.getContentPane().add("South", but);

          ActionListener deletionListener = new DeletionListener();
          jg.registerKeyboardAction(deletionListener, "Delete",
          KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
          JComponent.WHEN_IN_FOCUSED_WINDOW);
          jg.setRequestFocusEnabled(true);
        */
        context.setSize(600, 400);
        context.setVisible(true);
    }
}
