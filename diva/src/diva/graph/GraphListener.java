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

/**
 * A listener for changes in a graph's structure or contents,
 * which are communicated through GraphEvent objects.  GraphListeners
 * register themselves with a GraphModel object, and receive events
 * from Nodes and Edges contained by that model's root graph
 * or any of its subgraphs.
 *
 * @author Michael Shilman
 * @author John Reekie
 * @version $Id$
 * @Pt.AcceptedRating Yellow
 */
public interface GraphListener extends java.util.EventListener {
    /**
     * An edge's head has been changed in a registered
     * graph or one of its subgraphs.  The added edge
     * is the "source" of the event.  The previous head
     * is accessible via e.getOldValue().
     */
    public void edgeHeadChanged(GraphEvent e);

    /**
     * An edge's tail has been changed in a registered
     * graph or one of its subgraphs.  The added edge
     * is the "source" of the event.  The previous tail
     * is accessible via e.getOldValue().
     */
    public void edgeTailChanged(GraphEvent e);

    /**
     * A node has been been added to the registered
     * graph or one of its subgraphs.  The added node
     * is the "source" of the event.
     */
    public void nodeAdded(GraphEvent e);

    /**
     * A node has been been deleted from the registered
     * graphs or one of its subgraphs.  The deleted node
     * is the "source" of the event.  The previous parent
     * graph is accessible via e.getOldValue().
     */
    public void nodeRemoved(GraphEvent e);

    /**
     * The structure of the event's "source" graph has
     * been drastically changed in some way, and this
     * event signals the listener to refresh its view
     * of that graph from model.
     */
    public void structureChanged(GraphEvent e);
}
