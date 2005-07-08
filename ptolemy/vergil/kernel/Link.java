/* An object representing a link between a port and a relation.

 Copyright (c) 2000-2005 The Regents of the University of California.
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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.vergil.kernel;

import ptolemy.kernel.ComponentRelation;

//////////////////////////////////////////////////////////////////////////
//// Link

/**
 Instances of this class represent a link between a port and a
 relation, between two relations,
 or a between two ports.  In the first two cases,
 the relations are represented by an explicit node in the graph.  In the
 third case, there is no explicit node representing the relation and
 the edge runs directly from one port to the other.  Connections are made
 and broken by the graph model depending on
 which of the above contexts the link is being used in.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class Link {
    /** Return the head of this link.   This may be a port, or a vertex
     *  in a relation.
     */
    public Object getHead() {
        return _head;
    }

    /** Return the relation that this link represents.  If the link goes
     *  from a port to a port, then this is the only way to get at the
     *  relation.  If the link goes from a vertex to a port, then the
     *  relation will be the container of the vertex.
     */
    public ComponentRelation getRelation() {
        return _relation;
    }

    /** Return the tail of this link.   This may be a port, or a vertex
     *  in a relation.
     */
    public Object getTail() {
        return _tail;
    }

    /** Set the head of this link.   This may be a port, or a vertex
     *  in a relation.
     */
    public void setHead(Object head) {
        _head = head;
    }

    /** Set the relation for this link.
     */
    public void setRelation(ComponentRelation relation) {
        _relation = relation;
    }

    /** Set the tail of this link.   This may be a port, or a vertex
     *  in a relation.
     */
    public void setTail(Object tail) {
        _tail = tail;
    }

    /** Return a string representation of this link.
     */
    public String toString() {
        return "Link(" + _head + ", " + _tail + ", " + _relation + ")";
    }

    private Object _head;

    private Object _tail;

    private ComponentRelation _relation;
}
