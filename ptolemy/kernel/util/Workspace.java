/* An object for synchronization and version tracking of groups of objects.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (johnr@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.io.Serializable;
import java.util.Hashtable;
import collections.LinkedList;
import collections.CollectionEnumeration;

//////////////////////////////////////////////////////////////////////////
//// Workspace
/**
An instance of Workspace is used for synchronization and version tracking
of interdependent groups of objects.  These objects are said to be in the
workspace. This is not the same as the <i>container</i> association
in Ptolemy II.  A workspace is never returned by a getContainer() method.
<p>
The workspace provides a rudimentary directory service that can
be used to keep track of the objects within it.  It is not required to use
it in order to use the workspace for synchronization. Items are added
to the directory by calling add().
The names of the items in the directory are not required to be unique.
<p>
When reading the state of objects in the workspace, a thread must
ensure that no other thread is simultaneously modifying the objects in the
workspace.  To read-synchronize on its workspace, it uses the following
code in a method:
<pre>
    try {
	workspace().getReadAccess();
	// ... code that reads
    } finally {
	workspace().doneReading();
    }
</pre>
We assume that the workspace() method returns the workspace, as for example
in the NamedObj class. The getReadAccess() method suspends the thread if
another thread is currently modifying the workspace, and otherwise
returns immediately. Note that multiple readers can simultaneously have
read access. The finally clause is executed even if
an exception occurs.  This is essential because without the call
to doneReading(), the workspace will never again allow any thread
to modify it.  It believes there is still a thread reading it.
Any number of threads can simultaneously read the workspace.
<p>
To make changes in the workspace, a thread must write-synchronize
using the following code:
<pre>
    try {
	workspace().getWriteAccess();
	// ... code that writes
    } finally {
	workspace().doneWriting();
    }
</pre>
Only one thread can be writing to the workspace at a time, and
while the write permission is held, no thread can read the workspace.
Again, the call to doneWriting() is essential, or the workspace
will remain permanently locked to either reading or writing.
<p>
Note that it is not necessary to obtain a write lock just to add
an item to the workspace directory.  The methods for accessing
the directory are all synchronized, so there is no risk of any
thread reading an inconsistent state.

@author Edward A. Lee
@version $Id$
*/
public class Workspace implements Nameable, Serializable {

    /** Create a workspace with an empty string as its name.
     */
    public Workspace() {
        super();
        setName("");
    }

    /** Create a workspace with the specified name.  This name will form the
     *  prefix of the full name of all contained objects. If the name
     *  argument is null, then an empty string "" is used as the name.
     *  @param name Name of the workspace.
     */
    public Workspace(String name) {
        super();
        setName(name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an item to the directory. The names of the objects
     *  in the directory are not required to be unique.
     *  Only items with no container can be added.  Items with
     *  a container are still viewed as being within the workspace, but
     *  they are not explicitly listed in the directory.  Instead,
     *  their top-level container is expected to be listed (although this
     *  is not enforced).  Increment the version number.
     *  @param item Item to list in the directory.
     *  @exception IllegalActionException If the item has a container, is
     *   already in the directory, or is not in this workspace.
     */
    public synchronized void add(NamedObj item)
            throws IllegalActionException {
        if (item.workspace() != this) {
            throw new IllegalActionException(this, item,
                    "Cannot add an item to the directory of a workspace " +
                    "that it is not in.");
        }
        if (item.getContainer() != null) {
            throw new IllegalActionException(this, item,
                    "Cannot add an object with a container to a workspace " +
                    "directory.");
        }
        if (_directory.firstIndexOf(item) >= 0) {
            throw new IllegalActionException(this, item,
                    "Object is already listed in the workspace directory.");
        }
        _directory.insertLast(item);
        incrVersion();
    }

    /** Return a full description of the workspace and everything in its
     *  directory.  This is accomplished
     *  by calling the description method with an argument for full detail.
     *  @return A description of the workspace.
     */
    public synchronized String description() {
        // NOTE: It is not strictly needed for this method to be
        // synchronized, since _description is.  However, by making it
        // synchronized, the documentation shows this on the public
        // interface, not just the protected one.
        return description(NamedObj.COMPLETE);
    }

    /** Return a description of the workspace. The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class.  This method returns an empty
     *  string (not null) if there is nothing to report.  If the contents
     *  are requested, then the items in the directory are also described.
     *  @param detail The level of detail.
     *  @return A description of the workspace.
     */
    public synchronized String description(int detail) {
        // NOTE: It is not strictly needed for this method to be
        // synchronized, since _description is.  However, by making it
        // synchronized, the documentation shows this on the public
        // interface, not just the protected one.
        return _description(detail, 0, 0);
    }

    /** Enumerate the items in the directory, in the order in which
     *  they were added.
     *  @return An enumeration of NamedObj objects.
     */
    public synchronized CollectionEnumeration directory() {
        return _directory.elements();
    }

    /** Indicate that the calling thread is finished reading.
     *  If this thread is completely done reading (it has no other
     *  active read or write permissions), then wake up any threads that are
     *  suspended on access to this
     *  workspace so that they may contend for permissions.
     *  @exception InvalidStateException If this method is called
     *   before a corresponding call to getReadAccess().
     */
    public synchronized void doneReading() {
        Thread current = Thread.currentThread();
        ReadDepth depth = (ReadDepth)_readers.get(current);
        if (depth == null) {
            throw new InvalidStateException(this,
                    "Workspace: doneReading() called without a prior "
                    + "matching call to getReadAccess()!");
        }
        depth.decr();
        if (depth.isZero()) {
            _readers.remove(current);
            if (_writer != current) {
                notifyAll();
            }
        }
    }

    /** Indicate that the calling thread is finished writing.
     *  This wakes up any threads that are suspended on access to this
     *  workspace so that they may contend for permissions.
     *  It also increments the version number of the workspace.
     */
    public synchronized void doneWriting() {
        _writeReq--;
        _writeDepth--;
        if (_writeDepth == 0) {
            _writer = null;
            incrVersion();
            notifyAll();
        } else if (_writeDepth < 0) {
            throw new InvalidStateException(this,
                    "Workspace: doneWriting called without a prior "
                    + "matching call to getWriteAccess().");
        }
    }

    /** Get the container.  Always return null since a workspace
     *  has no container.
     *  @return null.
     */
    public Nameable getContainer() {
	return null;
    }

    /** Get the full name.
     *  @return The name of the workspace.
     */
    public String getFullName() {
        return _name;
    }

    /** Get the name.
     *  @return The name of the workspace.
     */
    public String getName() {
        return _name;
    }
    /** Obtain permission to read objects in the workspace.
     *  This method suspends the calling thread until such permission
     *  has been obtained.  Permission is granted unless either another
     *  thread has write permission, or there are threads that
     *  have requested write permission and not gotten it yet.
     *  It is essential that doneReading() be called
     *  after this, or write permission may never again be granted in
     *  this workspace.
     */
    public synchronized void getReadAccess() {
        while (true) {
            // If the current thread has write permission, or if there
            // are no pending write requests, then grant read permission.
            Thread current = Thread.currentThread();
            if (current == _writer || _writeReq == 0 ) {
                // The thread may already have read permission.
                ReadDepth depth = (ReadDepth)_readers.get(current);
                if (depth == null) {
                    depth = new ReadDepth();
                    _readers.put(current, depth);
                }
                depth.incr();
                return;
            }
            try {
                wait();
            } catch(InterruptedException ex) {
                throw new InternalErrorException(
                        "Thread interrupted while waiting for read access!"
                        + ex.getMessage());
            }
        }
    }

    /** Get the version number.  The version number is incremented on
     *  each call to doneWriting() and also on calls to incrVersion().
     *  It is meant to track changes in the topologies within the workspace.
     *  @return A non-negative long integer.
     */
    public synchronized long getVersion() {
        return _version;
    }

    /** Obtain permission to write to objects in the workspace.
     *  Permission is granted if there are no other threads that currently
     *  have read or write permission.  In particular, it <i>is</i> granted
     *  if this thread already has write permission, or if it is the only
     *  thread with read permission.
     *  This method suspends the calling thread until such permission
     *  has been obtained.  It is essential that doneWriting() be called
     *  after this, or read or write permission may never again be granted in
     *  this workspace.
     */
    public synchronized void getWriteAccess() {
        _writeReq++;
        while (true) {
            Thread current = Thread.currentThread();
            if (current == _writer) {
                // already have write permission
                _writeDepth++;
                return;
            }
            if ( _writer == null) {
                // there are no writers.  Are there any readers?
                if (_readers.isEmpty()) {
                    // No readers
                    _writer = Thread.currentThread();
                    _writeDepth = 1;
                    return;
                }
                if (_readers.size() == 1 && _readers.get(current) != null) {
                    // Sole reader is this thread.
                    _writer = Thread.currentThread();
                    _writeDepth = 1;
                    return;
                }
            }
            try {
                wait();
            } catch(InterruptedException ex) {
                throw new InternalErrorException(
                        "Thread interrupted while waiting for write access!"
                        + ex.getMessage());
            }
        }
    }

    /** Increment the version number by one.
     */
    public synchronized void incrVersion() {
        _version++;
    }

    /** Remove the specified item from the directory.
     *  Note that that item will still refer to this workspace as
     *  its workspace (its workspace is immutable).  If the object is
     *  not in the directory, do nothing.
     *  Increment the version number.
     */
    public synchronized void remove(NamedObj item) {
        _directory.removeOneOf(item);
        incrVersion();
    }

    /** Remove all items from the directory.
     *  Note that those items will still refer to this workspace as
     *  their workspace (their workspace is immutable).
     *  Increment the version number.
     */
    public synchronized void removeAll() {
        _directory.clear();
        incrVersion();
    }

    /** Set or change the name.  If a null argument is given the
     *  name is set to an empty string.
     *  Increment the version number.
     *  @param name The new name.
     */
    public synchronized void setName(String name) {
        if (name == null) {
            name = "";
        }
        _name = name;
        incrVersion();
    }

    /** Return a concise description of the object.
     *  @return The classname and name.
     */
    public String toString() {
        return getClass().getName() + " {" + getFullName()+ "}";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a description of the workspace.  The level of detail depends
     *  on the argument, which is an or-ing of the static final constants
     *  defined in the NamedObj class.  If the contents are requested,
     *  then the items in the directory are also described.
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  @param detail The level of detail.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the workspace.
     */
    protected synchronized String _description(int detail, int indent,
            int bracket) {
        String result = NamedObj._getIndentPrefix(indent);
        if (bracket == 1 || bracket == 2) result += "{";
        if((detail & NamedObj.CLASSNAME) != 0) {
            result += getClass().getName();
            if((detail & NamedObj.FULLNAME) != 0) {
                result += " ";
            }
        }
        if((detail & NamedObj.FULLNAME) != 0) {
            result += "{" + getFullName() + "}";
        }
        if ((detail & NamedObj.CONTENTS) != 0) {
            if ((detail & (NamedObj.CLASSNAME | NamedObj.FULLNAME)) != 0) {
                result += " ";
            }
            result += "directory {\n";
            CollectionEnumeration enum = directory();
            while (enum.hasMoreElements()) {
                NamedObj obj = (NamedObj)enum.nextElement();
                // If deep is not set, then zero-out the contents flag
                // for the next round.
                if ((detail & NamedObj.DEEP) == 0) {
                    detail &= ~NamedObj.CONTENTS;
                }
                result += obj._description(detail, indent+1, 2) + "\n";
            }
            result += "}";
        }
        if (bracket == 2) result += "}";
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // List of contained objects.
    private LinkedList _directory = new LinkedList();

    // The name
    private String _name;

    // Version number.
    private long _version = 0;

    // The currently writing thread (if any).
    private Thread _writer;

    // The number of pending write requests plus active write permissions.
    private int _writeReq = 0;

    // The number of active write permissions (all to the same thread).
    private int _writeDepth = 0;

    // A table by readers (threads) of how many times they have gotten
    // read permission.
    private Hashtable _readers = new Hashtable();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                            ////

    // Class ReadDepth
    // Keeps track of the number of reader permissions that a thread has.
    // This is used instead of the Integer class because Integer has no
    // incr() or decr() methods.  These methods save creating new instances
    // every time a read permission is granted.
    private class ReadDepth implements Serializable {
        public void decr() {
            _count--;
        }
        public void incr() {
            _count++;
        }
        public boolean isZero() {
            return _count == 0;
        }
        public int _count = 0;
    }
}
