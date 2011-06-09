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
package diva.graph;

import diva.canvas.Figure;
import diva.canvas.interactor.Interactor;

/**
 * Specifies the interface for objects that manage creation
 * of and interaction with graph nodes. GraphControllers
 * contain one or more instances of NodeController, typically
 * one for each type of node.
 *
 * @author         Steve Neuendorffer
 * @version        $Id$
 * @Pt.AcceptedRating      Red
 */
public interface NodeController {
    /** Given a node, add it to this graph editor and perform a layout
     * on the new node.
     */
    public void addNode(Object node);

    /** Add the node to this graph editor and render it
     * at the given location.
     */
    public void addNode(Object node, double x, double y);

    /**
     * Add the node to this graph editor, inside the given parent node
     * and place it where convenient
     */
    public void addNode(Object node, Object parent);

    /**
     * Add the node to this graph editor, inside the given parent node
     * and render it at the given location relative to its parent.
     */
    public void addNode(Object node, Object parent, double x, double y);

    /**
     * Remove the figure for the given node.
     */
    public void clearNode(Object node);

    /**
     * Render the given node and add the resulting figure to the foreground
     * layer of the graph pane.  If the node was previously rendered, then
     * infer the new location of the figure from the old.
     */
    public Figure drawNode(Object node);

    /**
     * Render the given node and add the resulting figure to the given
     * node's figure, which is assumed to be a CompositeFigure
     * in the controller's graph pane.
     */
    public Figure drawNode(Object node, Object parent);

    /**
     * Return the graph controller containing this controller.
     */
    public GraphController getController();

    /**
     * Return the node interactor associated with this controller.
     */
    public Interactor getNodeInteractor();

    /**
     * Return the node renderer associated with this controller.
     */
    public NodeRenderer getNodeRenderer();

    /**
     * Remove the node.
     */
    public void removeNode(Object node);

    /**
     * Set the node interactor for this controller
     */
    public void setNodeInteractor(Interactor interactor);

    /**
     * Set the node renderer for this controller
     */
    public void setNodeRenderer(NodeRenderer renderer);
}
