/* An attribute that represents a waypoint in a relation.

 Copyright (c) 1998 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.moml;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// VertexAttribute
/** 
An attribute that represents a waypoint in a relation.  The attribute contains
an (x,y) coordinate position, and an optional reference to another vertex 
which is connected to this one.  The coordinates are
expressed relative to the origin of the object's container.
<p>
Only one vertex in the relation is allowed to not have a 
reference to another vertex. Furthermore, the vertecies may not be
connected cyclically.  Neither of these conditions is enforced by this class,
but is assumed to be maintained by the code that creates these vertecies.
Equivalently, the vertecies within a relation form a spanning tree, and
recursively calling getVertex on any vertex will eventually return null, 
indicating the single unique node that is the root of the spanning tree.

@author Steve Neuendorffer
@version $Id$
*/
public class VertexAttribute extends Attribute {

    /** Construct an attribute with the given name and position.
     *  @param container The container.
     *  @param x The x coordinate of the vertex.
     *  @param y The y coordinate of the vertex.
     */	
    public VertexAttribute(NamedObj container, String name, int x, int y) 
	throws IllegalActionException, NameDuplicationException {
        super(container, name);
	if(!(container instanceof Relation)) {
	    throw new IllegalActionException("Vertex can only be contained " + 
					     "in a relation");
	}
	_x = x;
	_y = y;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the vertex that this vertex is directly connected to, or 
     *  null if this vertex has no parent in the spanning tree.
     */
    public VertexAttribute getVertex() {
	return _vertex;
    }

    /** Return the x coordinate of this location.
     */
    public int getX() {
	return _x;
    }
 
    /** Return the y coordinate of this location.
     */
    public int getY() {
	return _y;
    }

    /** Set the container of this vertex.  
     *  @exception IllegalActionException If the container is not a Relation.
     */
    public void setContainer(NamedObj container) 
	throws IllegalActionException, NameDuplicationException {
	if(!(container instanceof Relation)) {
	    throw new IllegalActionException("Vertex can only be contained " + 
					     "in a relation");
	} else {
	    super.setContainer(container);
	}
    }

    /** Set the vertex that this vertex is directly connected to, or 
     *  null if this vertex has no parent in the spanning tree.
     */
    public void setVertex(VertexAttribute vertex) {
	_vertex = vertex;
    }

    /** Set the x coordinate of this location.
     */
    public void setX(int value) {
	_x = value;
    }

    /** Set the y coordinate of this location.
     */
    public void setY(int value) {
	_y = value;
    }

    /** Get the vertex as a string.
     */	
    public String toString() {
        return "Vertex[" + getName() + ", X=" + _x + ", Y=" + _y + "]";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The vertex that this attribute is connected to.
    private VertexAttribute _vertex;

    // The coordinates of the vertex.
    private int _x;
    private int _y;
}
