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
 * which are communicated through GraphViewEvent objects.  GraphViewListeners
 * register themselves with a GraphViewModel object, and receive events
 * from Nodes and Edges contained by that model's root graph
 * or any of its subgraphs.
 *
 * @author Michael Shilman
 * @author Steve Neuendorffer
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public interface GraphViewListener extends java.util.EventListener {
    /**
     * The figure representing a node was moved.
     */
    public void nodeMoved(GraphViewEvent e);

    /**
     * The connector representing an edge was just routed.
     */
    public void edgeRouted(GraphViewEvent e);

    /**
     * A figure representing a node was just drawn.
     */
    public void nodeDrawn(GraphViewEvent e);

    /**
     * A connector representing an edge was just drawn.
     */
    public void edgeDrawn(GraphViewEvent e);
}
