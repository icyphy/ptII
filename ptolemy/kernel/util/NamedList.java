/* An ordered list of objects with names.

 Copyright (c) 1997- The Regents of the University of California.
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

NOTE: This class should probably leverage better
the underlying LinkedList class. E.g., it should be
capable of returning an enumeration sorted alphabetically by name.
This is the reason for a Red rating.
*/

package pt.kernel;

import collections.CorruptedEnumerationException;

import collections.LinkedList;
import collections.CollectionEnumeration;
import java.util.NoSuchElementException;
import java.io.Serializable; 

//////////////////////////////////////////////////////////////////////////
//// NamedList
/** 
An ordered list of objects with names.
The objects must implement the Nameable interface.
The names are required to be unique and non-null.

This class is intended to be used with small lists.
It is implemented as a linked list rather than hash table to
preserve ordering information, and thus would not provide efficient
name lookup for large lists.

@author Mudit Goel, Edward A. Lee
@version $Id$
@see Nameable
@see LinkedList
@see collections.LinkedList
*/
public class NamedList implements Cloneable, Serializable {

    /** Construct an empty NamedList with no container.
     */	
    public NamedList() {
        _namedlist = new LinkedList();
    }

    /** Construct an empty list with a Nameable container.
     */	
    public NamedList(Nameable container) {
        _namedlist = new LinkedList();
        _container = container;
    }

    /** Copy constructor.  Create a copy of the specified list, but
     *  with no container. This is useful to permit enumerations over
     *  a queue while the queue continues to be modified.
     */	
    public NamedList(NamedList model) {
        _namedlist = new LinkedList();
        _namedlist.appendElements(model.getElements());
        _container = null;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           //// 
 
    /** Add an element to the end of the list.
     *  The element is required to have a name that does not coincide with
     *  that of an element already on the list. 
     *  @param element Element to be added to the list.
     *  @exception IllegalActionException Argument has no name.
     *  @exception NameDuplicationException Name coincides with
     *  an element already on the list.
     */
    public void append(Nameable element) 
            throws IllegalActionException, NameDuplicationException {
        String newName = element.getName();
        if (newName == null) {
            throw new IllegalActionException(_container,
                    _nullNameExceptionString);
        }
        if( get(newName) == null ) {
            _namedlist.insertLast(element);
        } else {
            throw new NameDuplicationException(_container, element);
        }
    }

    /** Build an independent copy of the list.
     *  The elements themselves are not cloned.
     */
    public Object clone() { 
        return new NamedList(this); 
    }

    /** Get the first element.
     *  @exception NoSuchElementException The list is empty.
     *  @return The specified element.
     */
    public Nameable first() throws NoSuchElementException {
	return (Nameable)_namedlist.first();
    }

    /** Get an element by name.
     *  @param name The name of the desired element.
     *  @return The requested element if it is found, null otherwise.
     */
    public Nameable get(String name) {
        CollectionEnumeration enum = new NamedListEnumeration();
        while( enum.hasMoreElements() ) {
            Nameable obj = (Nameable)enum.nextElement();
            if( name.equals(obj.getName()) ) {
                return obj;
            }
        }
        return null;
    }

    /** Enumerate the elements of the list.
     *  @see collections.LinkedList#elements()
     *  @return An enumeration of Nameable objects. 
     */
    public CollectionEnumeration getElements() {
        return new NamedListEnumeration();
    }
   
    /** Return true if the specified object is on the list.
     *  @param element Element to be searched for in the list.
     */	
    public boolean includes(Nameable element) {
        CollectionEnumeration enum = new NamedListEnumeration();
        while( enum.hasMoreElements() ) {
            if (element == enum.nextElement()) return true;
        }
        return false;
    }

    /** Insert a new element after an element with the specified name.
     *  If there is no element with such name, then the new element is
     *  appended to the end of the list.
     *  @param name The name of the element after which to insert.
     *  @param element The element to insert.
     *  @exception IllegalActionException Element to insert has no name.
     *  @exception NameDuplicationException Element to insert has a
     *   name that coincides with one already on the list.
     */
    public void insertAfter(String name, Nameable element) 
            throws IllegalActionException, NameDuplicationException {
        int index = _getIndexOf(name);
        if (index == -1) append(element);  // name doesn't exist in list
        else _insertAt((index+1), element); // name exists in list
    }

    /** Insert a new element before an element with the specified name.
     *  If there is no element with such name, then the new element is
     *  added to the beginning of the list.
     *  @param name The name of the element before which to insert.
     *  @param element The element to insert.
     *  @exception IllegalActionException Element to insert has no name.
     *  @exception NameDuplicationException Element to insert has a
     *   name that coincides with one already on the list.
     */
    public void insertBefore(String name, Nameable element) 
            throws IllegalActionException, NameDuplicationException {
        int index = _getIndexOf(name);
        if (index == -1) prepend(element);// name doesn't exist in list
        else _insertAt(index, element);    // name exists in the list
    }

    /** Get the last element.
     *  @exception NoSuchElementException The list is empty.
     *  @return The specified element.
     */
    public Nameable last() throws NoSuchElementException {
	return (Nameable)_namedlist.last();
    }

    /** Add an element to the beginning of the list.
     *  The element is required to have a name that does not coincide with
     *  that of an element already on the list. 
     *  @param element Element to be added to the list.
     *  @exception IllegalActionException Argument has no name.
     *  @exception NameDuplicationException Name coincides with
     *  an element already on the list.
     */
    public void prepend(Nameable element) 
            throws IllegalActionException, NameDuplicationException {
        String newName = element.getName();
        if (newName == null) {
            throw new IllegalActionException(_container,
                    _nullNameExceptionString);
        } else if(get(newName) == null) {
            _namedlist.insertFirst(element);
        } else {
            throw new NameDuplicationException(_container, element);
        }
    }

    /** Remove the specified element.  If the element is not on the
     *  list, do nothing.
     *  @param element Element to be removed.
     */
    public void remove(Nameable element) {
        _namedlist.removeOneOf(element);
    }

    /** Remove an element specified by name.  If no such element exists
     *  on the list, do nothing.
     *  @param name Name of the element to be removed.
     *  @return Return A reference to the removed object, or null if no
     *  object with the specified name is found.
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
    
    /** Return the number of elements in the list. */
    public int size() {
        return _namedlist.size();
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    /*  Get the index of the element with the specified name.
     *  This is private because the
     *  interface to this class does not include the notion of indexes.
     *  @param name The name of the desired element.
     *  @return The index of the desired element, or -1 if it not on the list.
     */
    private int _getIndexOf(String name) {
        CollectionEnumeration enum = new NamedListEnumeration();
        int count = 0;
        while( enum.hasMoreElements() ) {
            Nameable obj = (Nameable)enum.nextElement();
            if( name.equals(obj.getName()) ) {
                return count;
            }
            count += 1;
        }
        return -1;
    }

    /*  Add an element at the specified position index in the list.
     *  The element is inserted just prior to that element that currently
     *  has the specified position index.  This is private because the
     *  interface to this class does not include the notion of indexes.
     *  @param index Where to insert the new element.
     *  @param element The element to insert.
     *  @exception IllegalActionException Element to insert has no name.
     *  @exception NameDuplicationException Element to insert has a
     *   name that coincides with one already on the list.
     */
    private void _insertAt(int index, Nameable element)
            throws IllegalActionException, NameDuplicationException {
        if( element.getName() == null ) {
            throw new IllegalActionException(_container,
                    _nullNameExceptionString);
        } else {
            if (get(element.getName()) == null) {
                _namedlist.insertAt(index, element);
                return;
            }
        }
        throw new NameDuplicationException(_container, element);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // The container (owner) of this list.
    private Nameable _container;

    // A LinkedList containing the elements.
    private LinkedList _namedlist;

    // Constant strings.
    private static final String _nullNameExceptionString = 
    "Attempt to add an object with a null name to a NamedList.";

    //////////////////////////////////////////////////////////////////////////
    ////                         inner classes                            ////
    
    /** Enumerate this NamedList.
     *  @see NamedList
     */
    private class NamedListEnumeration implements CollectionEnumeration {

        public NamedListEnumeration() {
            _enumeration = _namedlist.elements();
        }

        public boolean hasMoreElements() {
            if( corrupted() ) { 
                throw new CorruptedEnumerationException(); 
            }
            return _enumeration.hasMoreElements();
        }

        public Object nextElement() {
            if( corrupted() ) { 
                throw new CorruptedEnumerationException(); 
            }
            return _enumeration.nextElement();
        }

        public boolean corrupted() {
            return _enumeration.corrupted();
        }

        public int numberOfRemainingElements() {
            if( corrupted() ) { 
                throw new CorruptedEnumerationException(); 
            }
            return _enumeration.numberOfRemainingElements();
        }

        private CollectionEnumeration _enumeration;
    }
}
