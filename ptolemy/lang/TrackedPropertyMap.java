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
    public TrackedPropertyMap() {
        super();
    }

    public boolean addVisitor(Class c) {
        return _visitedBySet.add(c);
    }

    public void clearVisitors() {
        _visitedBySet.clear();
    }

    public boolean removeVisitor(Class c) {
        return _visitedBySet.remove(c);
    }

    public boolean wasVisitedBy(Class c) {
        return _visitedBySet.contains(c);
    }

    public Iterator visitorIterator() {
        return _visitedBySet.iterator();
    }

    protected HashSet _visitedBySet = new HashSet();
}