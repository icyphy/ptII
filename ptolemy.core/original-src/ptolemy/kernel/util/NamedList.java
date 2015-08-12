/* An ordered list of objects with names.

 Copyright (c) 1997-2014 The Regents of the University of California.
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


 NOTE: This class could leverage better
 the underlying LinkedList class. E.g., it could be
 capable of returning an enumeration sorted alphabetically by name.
 This would require extensions to the interface, but not modifications
 of the current interface.
 */
package ptolemy.kernel.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

///////////////////////////////////////////////////////////////////
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

 @author Mudit Goel, Edward A. Lee, Contributor: Jason E. Smith
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)
 @see Nameable
 */
@SuppressWarnings("serial")
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
     *  a list while the list continues to be modified.  If the argument
     *  list is null, then the resulting copy will be an empty named list.
     *  @param original The list to copy.
     */
    public NamedList(NamedList original) {
        super();

        if (original != null) {
            _namedList.addAll(original.elementList());
            if (_hashEnabled) {
                _hashedList = (HashMap<String, Nameable>) original._hashedList
                        .clone();
            }
        }

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
    public void append(Nameable element) throws IllegalActionException,
    NameDuplicationException {
        String newName = element.getName();

        if (newName == null) {
            throw new IllegalActionException(_container,
                    _NULL_NAME_EXCEPTION_STRING);
        }

        // NOTE: Having to do this named lookup each time is expensive.
        if (get(newName) == null) {
            _namedList.add(element);
            if (_hashEnabled) {
                _hashedList.put(newName, element);
            }
        } else {
            throw new NameDuplicationException(_container, element);
        }
    }

    /** Build an independent copy of the list.
     *  The elements themselves are not cloned.
     *  @return A new instance of NamedList.
     */
    @Override
    public Object clone() {
        return new NamedList(this);
    }

    /** Return an unmodifiable list with the contents of this named list.
     *  @return A list of Nameable objects.
     */
    public List elementList() {
        return Collections.unmodifiableList(_namedList);
    }

    /** Enumerate the elements of the list.
     *  @deprecated Use elementList() instead.
     *  @return An enumeration of Nameable objects.
     */
    @Deprecated
    public Enumeration elements() {
        return Collections.enumeration(_namedList);
    }

    /** Get the first element.
     *  @exception NoSuchElementException If the list is empty.
     *  @return The specified element.
     */
    public Nameable first() throws NoSuchElementException {
        return _namedList.getFirst();
    }

    /** Get an element by name.
     *  @param name The name of the desired element.
     *  @return The requested element if it is found, and null otherwise.
     */
    public Nameable get(String name) {
        if (_hashEnabled) {
            // If the name was changed, then _hashedList will be wrong
            Nameable found = _hashedList.get(name);
            if (found != null) {
                if (found.getName().equals(name)) {
                    return found;
                } else {
                    // The name of the Nameable was changed, but the
                    // _hashedList was not updated, so we remove the old
                    // entry.
                    _hashedList.remove(name);
                }
            }
        }

        // Do a linear search
        Iterator iterator = _namedList.iterator();

        while (iterator.hasNext()) {
            Nameable obj = (Nameable) iterator.next();

            if (name.equals(obj.getName())) {
                if (_hashEnabled) {
                    // The name of the NamedObj was likely changed, so
                    // add it to the hashedList.
                    _hashedList.put(name, obj);
                }
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
        if (_hashEnabled) {
            return get(element.getName()) != null;
        }
        return _namedList.contains(element);
    }

    /** Insert a new element after the specified element.
     *  If there is no such element, then append the new element
     *  to the end of the list.
     *  @param name The element after which to insert the new element.
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
            _insertAt(index + 1, element);
        }
        if (_hashEnabled) {
            _hashedList.put(element.getName(), element);
        }
    }

    /** Insert a new element before the specified element.
     *  If there is no such element, then the insert the new element
     *  at the beginning of the list.
     *  @param name The element before which to insert the new element.
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
        if (_hashEnabled) {
            _hashedList.put(element.getName(), element);
        }
    }

    /** Get the last element.
     *  @exception NoSuchElementException If the list is empty.
     *  @return The last element.
     */
    public Nameable last() throws NoSuchElementException {
        return _namedList.getLast();
    }

    /** Move the specified element down by one in the list.
     *  If the specified element is not in the list, then throw
     *  an exception. If the element is
     *  already at the end of the list, the leave it where it is.
     *  @param element Element to move down in the list.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it was not moved (it is already last).
     *  @exception IllegalActionException If the argument is not
     *   on the list.
     */
    public int moveDown(Nameable element) throws IllegalActionException {
        int index = _namedList.indexOf(element);

        if (index < 0) {
            // The element is not on the list.
            throw new IllegalActionException(element, "Not on the list.");
        } else if (index < _namedList.size() - 1) {
            _namedList.remove(element);
            _namedList.add(index + 1, element);
            return index;
        } else {
            return -1;
        }
    }

    /** Move the specified element to the beginning of the list.
     *  If the specified element is not in the list, then throw
     *  an exception.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it was not moved (it is already first).
     *  @param element Element to move to the top of the list.
     *  @exception IllegalActionException If the argument is not
     *   on the list.
     */
    public int moveToFirst(Nameable element) throws IllegalActionException {
        int index = _namedList.indexOf(element);

        if (index < 0) {
            // The element is not on the list.
            throw new IllegalActionException(element, "Not on the list.");
        } else if (index > 0) {
            _namedList.remove(element);
            _namedList.add(0, element);
            return index;
        } else {
            return -1;
        }
    }

    /** Move the specified element to the specified position in the list.
     *  If the specified element is not in the list, then throw
     *  an exception.
     *  @param element Element to move.
     *  @param index The position to which to move it.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it was not moved (it is already at the specified
     *   position).
     *  @exception IllegalActionException If the argument is not
     *   on the list, or if the specified position is out of range.
     */
    public int moveToIndex(Nameable element, int index)
            throws IllegalActionException {
        int priorIndex = _namedList.indexOf(element);

        if (priorIndex < 0) {
            // The element is not on the list.
            throw new IllegalActionException(element, "Not on the list.");
        } else if (index < 0 || index >= _namedList.size()) {
            throw new IllegalActionException(element, "Index out of range.");
        } else if (priorIndex != index) {
            _namedList.remove(element);
            _namedList.add(index, element);
            return priorIndex;
        } else {
            return -1;
        }
    }

    /** Move the specified element to the end of the list.
     *  If the specified element is not in the list, then throw
     *  an exception.
     *  @param element Element to move to the end of the list.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it was not moved (it is already last).
     *  @exception IllegalActionException If the argument is not
     *   on the list.
     */
    public int moveToLast(Nameable element) throws IllegalActionException {
        int index = _namedList.indexOf(element);

        if (index < 0) {
            // The element is not on the list.
            throw new IllegalActionException(element, "Not on the list.");
        } else if (index < _namedList.size() - 1) {
            _namedList.remove(element);
            _namedList.add(element);
            return index;
        } else {
            return -1;
        }
    }

    /** Move the specified element up by one in the list.
     *  If the specified element is not in the list, then
     *  throw an exception.
     *  @param element Element to move up in the list.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it was not moved (it is already first).
     *  @exception IllegalActionException If the argument is not
     *   on the list.
     */
    public int moveUp(Nameable element) throws IllegalActionException {
        int index = _namedList.indexOf(element);

        if (index < 0) {
            // The element is not on the list.
            throw new IllegalActionException(element, "Not on the list.");
        } else if (index > 0) {
            _namedList.remove(element);
            _namedList.add(index - 1, element);
            return index;
        } else {
            return -1;
        }
    }

    /** Add an element to the beginning of the list.
     *  The element is required to have a name that does not coincide with
     *  that of an element already on the list.
     *  @param element Element to be added to the list.
     *  @exception IllegalActionException If the argument is not
     *   on the list.
     *  @exception IllegalActionException If the argument has no name.
     *  @exception NameDuplicationException If the name coincides with
     *   an element already on the list.
     */
    public void prepend(Nameable element) throws IllegalActionException,
    NameDuplicationException {
        _insertAt(0, element);
        if (_hashEnabled) {
            _hashedList.put(element.getName(), element);
        }
    }

    /** Remove the specified element.  If the element is not on the
     *  list, do nothing.
     *  @param element Element to be removed.
     */
    public void remove(Nameable element) {
        _namedList.remove(element);
        if (_hashEnabled) {
            _hashedList.remove(element.getName());
        }
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
            if (_hashEnabled) {
                _hashedList.remove(name);
            }
            return element;
        }

        return null;
    }

    /** Remove all elements from the list. */
    public void removeAll() {
        _namedList.clear();
        if (_hashEnabled) {
            _hashedList.clear();
        }
    }

    /** Return the number of elements in the list.
     *  @return A non-negative integer.
     */
    public int size() {
        return _namedList.size();
    }

    /** Return a string description of the list.
     *  @return A string description of the list.
     */
    @Override
    public String toString() {
        if (_namedList != null) {
            return _namedList.toString();
        }
        return "[]";
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
        Iterator iterator = _namedList.iterator();
        int count = 0;

        while (iterator.hasNext()) {
            Nameable obj = (Nameable) iterator.next();

            if (name.equals(obj.getName())) {
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
        if (element.getName() == null) {
            throw new IllegalActionException(_container,
                    _NULL_NAME_EXCEPTION_STRING);
        } else if (get(element.getName()) == null) {
            _namedList.add(index, element);
            return;
        }

        throw new NameDuplicationException(_container, element);
    }

    /*
     * Activates use of a hashmap to quicken lookup times of items
     * stored in this list.
     */
    private void enableHash() {

        _hashedList = new HashMap<String, Nameable>(_threshold + 1, 3.0f);

        Iterator iterator = _namedList.iterator();

        while (iterator.hasNext()) {
            Nameable obj = (Nameable) iterator.next();
            _hashedList.put(obj.getName(), obj);
        }

        _hashEnabled = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial The container (owner) of this list. */
    private Nameable _container;

    private static final int _threshold = 100;

    /** @serial A LinkedList containing the elements. */
    private LinkedList<Nameable> _namedList = new LinkedList<Nameable>() {
        @Override
        public boolean add(Nameable obj) {
            // Findbugs: "Ambiguous invocation of either an outer or
            // inherited method java.util.LinkedList.size()," so we use super.size()
            if (super.size() > _threshold && !_hashEnabled) {
                enableHash();
            }
            return super.add(obj);
        }
    };

    /** @serial A HashMap linking names to LinkedList entries */
    private HashMap<String, Nameable> _hashedList = null;

    /** @serial A boolean indicating that the hashmap was enabled */
    private boolean _hashEnabled = false;

    // Constant strings.
    private static final String _NULL_NAME_EXCEPTION_STRING = "Attempt to add an object with a null name to a NamedList.";

}
