/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */

package diva.graph.tutorial;
import diva.graph.JGraph;
import diva.graph.GraphPane;
import diva.graph.basic.*;
import diva.gui.AppContext;
import diva.gui.BasicFrame;
import javax.swing.SwingUtilities;

/**
 * This is the most basic tutorial, popping up an empty graph
 * editing window.  Control-click to add nodes,
 * select a node and control-drag to create new edges.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating Red
 */
public class SimpleTutorial {
    /**
     * Pop up an empty graph editing window.
     */
    public static void main(String argv[]) {
        final AppContext context = new BasicFrame("Simple Tutorial");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {       
                new SimpleTutorial(context);
                context.setVisible(true);
            }
        });
    }

    public SimpleTutorial(AppContext context) {
        JGraph jg = new JGraph(new GraphPane(new BasicGraphController(),
                new BasicGraphModel()));
        context.getContentPane().add("Center", jg);
    }
}


