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

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

import diva.graph.JGraph;
import diva.graph.toolbox.DeletionListener;
import diva.gui.AppContext;
import diva.gui.BasicFrame;

/**
 * Another graph demo.  This uses a different rendering strategy for
 * the same graph model as the BasicGraphDemo.  Nodes are rendered as
 * round bubble and edges are rendered as curved arcs.
 *
 * @author Michael Shilman
 * @author Steve Neuendorffer
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public class BubbleGraphDemo {
    /**
     * Construct a new instance of graph demo, which does the work of
     * setting up the graphs and displaying itself.
     */
    public static void main(String[] argv) {
        new BubbleGraphDemo(new BasicFrame("Bubble Graph Demo"));
    }

    public BubbleGraphDemo(AppContext context) {
        JGraph jg = new JGraph(new BubblePane());
        context.getContentPane().add("Center", jg);

        ActionListener deletionListener = new DeletionListener();
        jg.registerKeyboardAction(deletionListener, "Delete", KeyStroke
                .getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        jg.setRequestFocusEnabled(true);

        context.setSize(600, 400);
        context.setVisible(true);
    }
}
