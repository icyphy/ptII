/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.basic;

import diva.graph.BasicEdgeController;
import diva.graph.BasicNodeController;
import diva.graph.EdgeController;
import diva.graph.NodeController;

/**
 * A controller for bubble-and-arc graph editors.
 *
 * @author         Michael Shilman (michaels@eecs.berkeley.edu)
 * @version        $Revision$
 * @rating      Red
 */
public class BubbleGraphController extends BasicGraphController {
    /**
     * Create a new controller with default node and edge controllers.
     * Set the node renderer to a bubble renderer, and the edge renderer
     * to an arc renderer.
     */
    public BubbleGraphController () {
        NodeController nc = new BasicNodeController(this);
        nc.setNodeRenderer(new BubbleRenderer());
        setNodeController(nc);

        EdgeController ec = new BasicEdgeController(this);
        ec.setEdgeRenderer(new ArcRenderer());
        setEdgeController(ec);
    }


}


