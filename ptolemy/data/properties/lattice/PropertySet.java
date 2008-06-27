/* Set property.

 Copyright (c) 1997-2008 The Regents of the University of California.
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
package ptolemy.data.properties.lattice;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



//////////////////////////////////////////////////////////////////////////
////PropertySet

/**
 Property set base class.

 @author Man-Kit Leung
 @version $Id: PropertySet.java,v 1.8 2008/04/20 07:32:02 mankit Exp $
 @since Ptolemy II 7.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 @see ptolemy.graph.CPO
 */

public class PropertySet extends LatticeProperty implements Set {

    public PropertySet(PropertyLattice lattice, Collection elements) {
        super(lattice);
        _elements = new HashSet(elements);
    }

    HashSet _elements;

    public boolean add(Object e) {
        return _elements.add(e);
    }

    public boolean addAll(Collection c) {
        return _elements.addAll(c);
    }

    public void clear() {
        _elements.clear();        
    }

    public boolean contains(Object o) {
        return _elements.contains(o);
    }

    public boolean containsAll(Collection c) {
        return _elements.containsAll(c);
    }

    /**
     * Return true if this is an acceptable solution.
     * @return true if this is an acceptable solution; otherwise, false;
     */
    public boolean isAcceptableSolution() {
        return ((PropertySetLattice) _lattice).isAcceptableSolution(this);
    }
    
    public boolean isEmpty() {
        return _elements.isEmpty();
    }

    public Iterator iterator() {
        return _elements.iterator();
    }

    public boolean remove(Object o) {
        return _elements.remove(o);
    }

    public boolean removeAll(Collection c) {
        return _elements.removeAll(c);
    }

    public boolean retainAll(Collection c) {
        return _elements.retainAll(c);
    }

    public int size() {
        return _elements.size();
    }

    public Object[] toArray() {
        return _elements.toArray();
    }

    public Object[] toArray(Object[] a) {
        return _elements.toArray(a);
    }
}
