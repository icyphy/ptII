/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph;


/**
 * A simple graph controller, which works well if all nodes have the same
 * interaction, and all edges have the same interaction.  It defers the
 * node related methods to a node controller, the edge related methods to
 * an edge controller.  For more complex visualizations that use multiple
 * kinds of nodes, use CompositeGraphController instead.
 *
 * @author         Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version        $Revision$
 * @rating      Red
 */
public abstract class SimpleGraphController extends AbstractGraphController {

    /** The node controller
     */
    private NodeController _nodeController;

    /** The edge controller
     */
    private EdgeController _edgeController;

    /**
     * Create a new basic controller with default node and edge controllers.
     */
    public SimpleGraphController () {
        super();
    }

    /**
     * Return the edge controller.
     */
    public EdgeController getEdgeController() {
        return _edgeController;
    }

    /**
     * Return the node controller.
     */
    public NodeController getNodeController() {
        return _nodeController;
    }

    /**
     * Given an edge, return the controller associated with that
     * edge.
     */
    public EdgeController getEdgeController(Object edge) {
        return getEdgeController();
    }

    /**
     * Given an node, return the controller associated with that
     * node.
     */
    public NodeController getNodeController(Object node) {
        return getNodeController();
    }

    /**
     * Set the edge controller.
     */
    public void setEdgeController(EdgeController c) {
        _edgeController = c;
    }

    /**
     * Set the node controller.
     */
    public void setNodeController(NodeController c) {
        _nodeController = c;
    }
}




