/*
A base class for objects that may be visited.

Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (ctsay@eecs.berkeley.edu)
@AcceptedRating Red (ctsay@eecs.berkeley.edu)
*/

package ptolemy.lang;

import java.util.Iterator;
import java.util.HashSet;

//////////////////////////////////////////////////////////////////////////
//// TrackedPropertyMap
/** A base class for objects that may be visited. Visitors may mark such objects
 *  with their corresponding Class objects.
 *
 *  @author Jeff Tsay
 */
public class TrackedPropertyMap extends PropertyMap {
    /** Create a new TrackedPropertyMap. */
    public TrackedPropertyMap() {
        super();
    }
    
    
    /** Add the visitor with the argument class object to the set of visitors
     *  that have visited this object.
     */
    public boolean addVisitor(Class c) {
        return _visitedBySet.add(c);
    }

    /** Clear all traces of visitation from any visitor. */
    public void clearVisitors() {
        _visitedBySet.clear();
    }

    /** Remove the visitor with the argument class object from the set of 
     *  visitors that have visited this object.
     */
    public boolean removeVisitor(Class c) {
        return _visitedBySet.remove(c);
    }

    /** Return true iff this object was visited by a visitor with the
     *  argument class object.
     */
    public boolean wasVisitedBy(Class c) {    
        return _visitedBySet.contains(c);
    }

    /** Return an iterator over the class objects of the visitors that have
     *  visited this object.
     */    
    public Iterator visitorIterator() {
        return _visitedBySet.iterator();
    }

    /** A set of classs objects of the visitors that have visited this 
     *  object. The initial capacity is set to 1 to conserve memory.
     */
    protected HashSet _visitedBySet = new HashSet(1);
}