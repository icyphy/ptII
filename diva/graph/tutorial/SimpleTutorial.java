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


