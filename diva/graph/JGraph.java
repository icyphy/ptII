/*
 Copyright (c) 1998-2001 The Regents of the University of California
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

package diva.graph;

import diva.canvas.JCanvas;

/**
 * A graph widget analagous to java.swing.JTree.
 * JGraph functions as a container for an
 * instance of GraphPane, which is a multi-layer graphics
 * object containing (among other things) a layer upon which
 * graph elements are drawn and manipulated.
 *
 * @see GraphModel
 * @see GraphPane
 * @author         Michael Shilman (michaels@eecs.berkeley.edu)
 * @version        $Id$
 */
public class JGraph extends JCanvas {


    /**
     * Construct a new JGraph with the given graph pane.
     */
    public JGraph(GraphPane pane) {
        super(pane);
    }

    /**
     * Return the canvas pane, which is of type
     * GraphPane.
     */
    public GraphPane getGraphPane() {
        return (GraphPane)getCanvasPane();
    }

    /**
     * Set the graph pane of this widget.
     */
    public void setGraphPane(GraphPane p) {
        setCanvasPane(p);
    }
}


