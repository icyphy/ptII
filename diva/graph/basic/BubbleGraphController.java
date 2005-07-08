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

import diva.graph.BasicEdgeController;
import diva.graph.BasicNodeController;
import diva.graph.EdgeController;
import diva.graph.NodeController;

/**
 * A controller for bubble-and-arc graph editors.
 *
 * @author         Michael Shilman
 * @version        $Id$
 * @Pt.AcceptedRating      Red
 */
public class BubbleGraphController extends BasicGraphController {
    /**
     * Create a new controller with default node and edge controllers.
     * Set the node renderer to a bubble renderer, and the edge renderer
     * to an arc renderer.
     */
    public BubbleGraphController() {
        NodeController nc = new BasicNodeController(this);
        nc.setNodeRenderer(new BubbleRenderer());
        setNodeController(nc);

        EdgeController ec = new BasicEdgeController(this);
        ec.setEdgeRenderer(new ArcRenderer());
        setEdgeController(ec);
    }
}
