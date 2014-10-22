/*
 Copyright (c) 1998-2014 The Regents of the University of California
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

/**
 * A simple graph controller, which works well if all nodes have the same
 * interaction, and all edges have the same interaction.  It defers the
 * node related methods to a node controller, the edge related methods to
 * an edge controller.  For more complex visualizations that use multiple
 * kinds of nodes, use CompositeGraphController instead.
 *
 * @author         Steve Neuendorffer
 * @version        $Id$
 * @Pt.AcceptedRating      Red
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
    public SimpleGraphController() {
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
    @Override
    public EdgeController getEdgeController(Object edge) {
        return getEdgeController();
    }

    /**
     * Given an node, return the controller associated with that
     * node.
     */
    @Override
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
