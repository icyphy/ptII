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
package diva.graph.modular;

/**
 * An edge is an object that is contained by a graph and connects
 * nodes.  An edge has a "head" and a "tail" as if it was directed,
 * but also has a method isDirected() that says whether or not the
 * edge should be treated as directed (e.g. should there be an arrow
 * drawn on the head).  An edge has a semantic object that is its
 * semantic equivalent in the application and may have a visual object
 * which is its syntactic representation in the user interface.
 *
 * @author Michael Shilman
 * @version $Id$
 * @Pt.AcceptedRating Red
 */
public interface MutableEdgeModel extends EdgeModel {
    /**
     * Return whether or not the given node is a valid
     * head of this edge.
     */
    public boolean acceptHead(Object edge, Object head);

    /**
     * Return whether or not the given node is a valid
     * tail of this edge.
     */
    public boolean acceptTail(Object edge, Object tail);

    /**
     * Set the node that this edge points to.  Implementors
     * of this method are also responsible for insuring
     * that it is set properly as an "incoming" edge of
     * the node, and that it is removed as an incoming
     * edge from its previous head node.
     */
    public void setHead(Object edge, Object head);

    /**
     * Set the node that this edge stems from.  Implementors
     * of this method are also responsible for insuring
     * that it is set properly as an "outgoing" edge of
     * the node, and that it is removed as an outgoing
     * edge from its previous tail node.
     */
    public void setTail(Object edge, Object tail);
}
