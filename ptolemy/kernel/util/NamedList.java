/* An ordered list of objects with names.

 Copyright (c) 1997-1999 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (johnr@eecs.berkeley.edu)

NOTE: This class could leverage better
the underlying LinkedList class. E.g., it could be
capable of returning an enumeration sorted alphabetically by name.
This would require extensions to the interface, but not modifications
of the current interface.
*/

package ptolemy.kernel.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.io.Serializable;

//////////////////////////////////////////////////////////////////////////
//// NamedList
/**
An ordered list of objects with names.
The objects must implement the Nameable interface.
The names are required to be unique and non-null.
<p>
This class is biased towards sequential accesses.
If it is used with name lookups, the list should be small.
It is implemented as a linked list rather than hash table to
preserve ordering information, and thus would not provide efficient
name lookup for large lists.
Also, it permits the name of an object in the list
to change without this list being informed.
<p>
An instance of this class may have a container, but that container
is only used for error reporting.

@author Mudit Goel, Edward A. Lee
@version $Id$
@see Nameable
*/
public final class NamedList implements Cloneable, Serializable {

    /** Construct an empty NamedList with no container.
     */
    public NamedList() {
        super();
    }

    /** Construct an empty list with a Nameable container.
     *  @param container The container (for error reporting).
     */
    public NamedList(Nameable container) {
        super();
        _container = container;
    }

    /** Copy constructor.  Create a copy of the specified list, but
     *  with no container. This is useful to permit enumerations over
     *  a list while the list continues to be modified.
     *  @param original The list to copy.
     */
    public NamedList(NamedList original) {
        super();
        _namedlist.addAll(original.elementList());
        _container = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an element to the end of the list.
     *  The element is required to have a name that does not coincide with
     *  that of an element already on the list.
     *  @param element Element to be added to the list.
     *  @exception IllegalActionException If the argument has no name.
     *  @exception NameDuplicationException If the name coincides with
     *   an element already on the list.
     */
    public void append(Nameable element)
            throws IllegalActionException, NameDuplicationException {
        String newName = element.getName();
        if (newName == null) {
            throw new IllegalActionException(_container,
                    _NULL_NAME_EXCEPTION_STRING);
        }
        // NOTE: Having to do this named lookup each time is expensive.
        if( get(newName) == null ) {
            _namedlist.add(element);
        } else {
            throw new NameDuplicationException(_container, element);
        }
    }

    /** Build an independent copy of the list.
     *  The elements themselves are not cloned.
     *  @return A new instance of NamedList.
     */
    public Object clone() {
        return new NamedList(this);
    }

    /** Return an unmodifiable list with the contents of this named list.
     *  @return A list of Nameable objects.
     */
    public List elementList() {
        return Collections.unmodifiableList(_namedlist);
    }

    /** Enumerate the elements of the list.
     *  @deprecated Use elementList() instead.
     *  @return An enumeration of Nameable objects.
     */
    public Enumeration elements() {
        return Collections.enumeration(_namedlist);
    }

    /** Get the first element.
     *  @exception NoSuchElementException If the list is empty.
     *  @return The specified element.
     */
    public Nameable first() throws NoSuchElementException {
	return (Nameable)_namedlist.getFirst();
    }

    /** Get an element by name.
     *  @param name The name of the desired element.
     *  @return The requested element if it is found, and null otherwise.
     */
    public Nameable get(String name) {
        Iterator iterator = _namedlist.iterator();
        while( iterator.hasNext() ) {
            Nameable obj = (Nameable)iterator.next();
            if( name.equals(obj.getName()) ) {
                return obj;
            }
        }
        return null;
    }

    /** Return true if the specified object is on the list.
     *  @param element Element to be searched for in the list.
     *  @return A boolean indicating whether the element is on the list.
     */
    public boolean includes(Nameable element) {
        return _namedlist.contains(element);
    }

    /** Insert a new element after an element with the specified name.
     *  If there is no element with such name, then the new element is
     *  appended to the end of the list.
     *  @param name The name of the element after which to insert.
     *  @param element The element to insert.
     *  @exception IllegalActionException If the element to insert has no name.
     *  @exception NameDuplicationException If the element to insert has a
     *   name that coincides with one already on the list.
     */
    public void insertAfter(String name, Nameable element)
            throws IllegalActionException, NameDuplicationException {
        int index = _getIndexOf(name);
        if (index == -1) {
            // name doesn't exist in list
            append(element);
        } else {
            // name exists in list
            _insertAt((index+1), element);
        }
    }

    /** Insert a new element before an element with the specified name.
     *  If there is no element with such name, then the new element is
     *  added to the beginning of the list.
     *  @param name The name of the element before which to insert.
     *  @param element The element to insert.
     *  @exception IllegalActionException If the element to insert has no name.
     *  @exception NameDuplicationException If the element to insert has a
     *   name that coincides with one already on the list.
     */
    public void insertBefore(String name, Nameable element)
            throws IllegalActionException, NameDuplicationException {
        int index = _getIndexOf(name);
        if (index == -1) {
            // name doesn't exist in list
            prepend(element);
        } else {
            // name exists in the list
            _insertAt(index, element);
        }
    }

    /** Get the last element.
     *  @exception NoSuchElementException If the list is empty.
     *  @return The last element.
     */
    public Nameable last() throws NoSuchElementException {
	return (Nameable)_namedlist.getLast();
    }

    /** Add an element to the beginning of the list.
     *  The element is required to have a name that does not coincide with
     *  that of an element already on the list.
     *  @param element Element to be added to the list.
     *  @exception IllegalActionException If the argument has no name.
     *  @exception NameDuplicationException If the name coincides with
     *   an element already on the list.
     */
    public void prepend(Nameable element)
            throws IllegalActionException, NameDuplicationException {
        _insertAt(0, element);
    }

    /** Remove the specified element.  If the element is not on the
     *  list, do nothing.
     *  @param element Element to be removed.
     */
    public void remove(Nameable element) {
        _namedlist.remove(element);
    }

    /** Remove an element specified by name.  If no such element exists
     *  on the list, do nothing.
     *  @param name Name of the element to be removed.
     *  @return A reference to the removed object, or null if no
     *   object with the specified name is found.
     */
    public Nameable remove(String name) {
        Nameable element = get(name);
        if (element != null) {
            remove(element);
            return element;
        }
        return null;
    }

    /** Remove all elements from the list. */
    public void removeAll() {
        _namedlist.clear();
    }

    /** Return the number of elements in the list.
     *  @return A non-negative integer.
     */
    public int size() {
        return _namedlist.size();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Get the index of the element with the specified name.
     *  This is private because the
     *  interface to this class does not include the notion of indexes.
     *  @param name The name of the desired element.
     *  @return The index of the desired element, or -1 if it not on the list.
     */
    private int _getIndexOf(String name) {
        Iterator iterator = _namedlist.iterator();
        int count = 0;
        while( iterator.hasNext() ) {
            Nameable obj = (Nameable)iterator.next();
            if( name.equals(obj.getName()) ) {
                return count;
            }
            count++;
        }
        return -1;
    }

    /*  Add an element at the specified position index in the list.
     *  The element is inserted just prior to that element that currently
     *  has the specified position index.  The index can range from 0..size()
     * (i.e., one past the current last index). If the index is equal to
     *  size(), the element is appended as the new last element.
     *  This is private because the
     *  interface to this class does not include the notion of indexes.
     *  @param index Where to insert the new element.
     *  @param element The element to insert.
     *  @exception IllegalActionException If the Element to insert has no name.
     *  @exception NameDuplicationException If the Element to insert has a
     *   name that coincides with one already on the list.
     */
    private void _insertAt(int index, Nameable element)
            throws IllegalActionException, NameDuplicationException {
        if( element.getName() == null ) {
            throw new IllegalActionException(_container,
                    _NULL_NAME_EXCEPTION_STRING);
        } else if (get(element.getName()) == null) {
            _namedlist.add(index, element);
            return;
        }
        throw new NameDuplicationException(_container, element);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial The container (owner) of this list. */
    private Nameable _container;

    /** @serial A LinkedList containing the elements. */
    private LinkedList _namedlist = new LinkedList();

    // Constant strings.
    private static final String _NULL_NAME_EXCEPTION_STRING =
    "Attempt to add an object with a null name to a NamedList.";
}
