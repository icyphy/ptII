/* An object for synchronization and version tracking of groups of objects.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
import java.util.Collections;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

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
	_workspace.getReadAccess();
	// ... code that reads
    } finally {
	_workspace.doneReading();
    }
</pre>
We assume that the _workspace variable references the workspace, as for example
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
	_workspace.getWriteAccess();
	// ... code that writes
    } finally {
	_workspace.doneWriting();
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
<p>
To improve performance, one can use the PtolemyThread class instead of the
ordinary Thread class. The improvement is gained by having PtolemyThread
object includes an integer field to store the read depth. This is as opposed
to using a hashtable as a map between (ordinary )Thread object and its
read depth field.
<p>
Workspace can be made read-only by calling the setReadOnly(boolean)
method with <i>true</i> as an argument. A read-only workspace can only have
readers but no writers. The getReadAccess() and doneReading() methods invoked
on a read-only workspace return immediately bypassing all checks. Note that
the pairing of the getReadAccess() and doneReading() methods is not checked
under this condition. The getWriteAccess() and doneWriting() methods invoked
on a read-only workspace will throw an exception. This is done to improve
performance, as in many cases there are certain parts of simulation where
we can predict in advance that no write access will be needed.


@author Edward A. Lee, Mudit Goel, Lukito Muliadi
@version $Id$
*/

public final class Workspace implements Nameable, Serializable {

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
        if (_directory.indexOf(item) >= 0) {
            throw new IllegalActionException(this, item,
                    "Object is already listed in the workspace directory.");
        }
        _directory.add(item);
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

    /** Return an unmodifiable list of the items in the directory,
     *  in the order in which they were added.
     *  @return A list of instances of NamedObj.
     */
    public synchronized List directoryList() {
        return Collections.unmodifiableList(_directory);
    }

    /** Enumerate the items in the directory, in the order in which
     *  they were added.
     *  @deprecated Use directoryList() instead.
     *  @return An enumeration of NamedObj objects.
     */
    public synchronized Enumeration directory() {
        return Collections.enumeration(_directory);
    }

    /** Indicate that the calling thread is finished reading.
     *  If this thread is completely done reading (it has no other
     *  active read or write permissions), then wake up any threads that are
     *  suspended on access to this
     *  workspace so that they may contend for permissions.
     *  If the workspace is read-only, the pairing
     *  between the getReadAccess() and doneReading() methods is not checked.
     *  @exception InvalidStateException If this method is called
     *   before a corresponding call to getReadAccess().
     */
    public final synchronized void doneReading() {
        // A read-only workspace can't have any writers.
        // Since, getReadAccess() simply returns, so does doneReading().
        if (_readOnly) {
            return;
        }

        Thread current = Thread.currentThread();

        // Implementation note: I used instance of here, because of some
        // performance details plus instanceof is really a better coding
        // style, in term of code visibility (lmuliadi).

        // FIXME: The code for PtolemyThread and non-PtolemyThread are really
        // similar. Find way to consolidate them.. I sure can't find one now
        // (lmuliadi).

        if (current instanceof PtolemyThread) {
            // The current thread is a PtolemyThread.
            PtolemyThread ptThread = (PtolemyThread)current;
            if (ptThread.readDepth == 0) {
                throw new InvalidStateException(this,
                        "Workspace: doneReading() called without a prior "
                        + "matching call to getReadAccess()!");
            }
            ptThread.readDepth--;
            if (ptThread.readDepth == 0) {
                // Look at the use of _numPtReaders at this variable
                // declaration location. (near the bottom of this file)
                // This thread is no longer a reader.
                _numPtReaders--;
                if (_writer != current) {
                    notifyAll();
                }
            }
        } else {
            // The current thread is not a PtolemyThread.
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
    }

    /** Indicate that the calling thread is finished writing.
     *  This wakes up any threads that are suspended on access to this
     *  workspace so that they may contend for permissions.
     *  It also increments the version number of the workspace.
     *  @exception InvalidStateException If this method is called when
     *  the workspace is read-only.
     */
    public final synchronized void doneWriting() {
        // A read-only workspace can't be written, so calling this method
        // doesn't really make sense.
        if (_readOnly) {
            throw new InvalidStateException(this, "Trying to relinquish " +
                    "write access on a write-protected workspace.");
        }
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
     *  have requested write permission and not gotten it yet. If this thread
     *  already has a read permission, then another permission is granted
     *  irrespective of other write requests.
     *  It is essential that doneReading() be called
     *  after this, or write permission may never again be granted in
     *  this workspace.
     *  If the workspace is read-only, the calling thread will not suspend,
     *  and will just return immediately from the method call. The pairing
     *  between the getReadAccess() and doneReading() methods is not checked
     *  under this condition.
     */
    public final synchronized void getReadAccess() {

        // The workspace is read-only, so there are no writers,
        // so always grant the permission.
        if (_readOnly) {
            return;
        }
        // Go into an infinite 'while (true)' loop, and at each iteration
        // check if this current thread can get a read access, if not then
        // at the end of iteration, do a wait() on the workspace. Otherwise,
        // once the thread get a read access, it returns.
        while (true) {
            // If the current thread has read permission, then grant
            // it read permission
            Thread current = Thread.currentThread();

            if (current instanceof PtolemyThread) {
                // If the current thread is an instance of PtolemyThread,
                // then use the readDepth field of it.
                PtolemyThread ptThread = (PtolemyThread)current;
                if (ptThread.readDepth != 0) {
                    ptThread.readDepth++;
                    return;
                } else {
                    if (current == _writer || _writeReq == 0 ) {
                        ptThread.readDepth++;
                        // This is a new reader, so we increment the number
                        // of Ptolemy readers.
                        _numPtReaders++;
                        return;
                    }
                }
            } else {
                // The current thread is not an instance of PtolemyThread.
                ReadDepth depth = (ReadDepth)_readers.get(current);
                if (depth != null) {
                    depth.incr();
                    return;
                } else {
                    // If the current thread has write permission or if there
                    // are no pending write requests, then grant
                    // read permission.
                    if (current == _writer || _writeReq == 0 ) {
                        // The thread may already have read permission.
                        if (depth == null) {
                            // FIXME: Note that depth will always be null in
                            // here, so maybe we should get rid of this inner
                            // if... Let me know if this is not right..
                            // (lmuliadi)
                            depth = new ReadDepth();
                            _readers.put(current, depth);
                        }
                        depth.incr();
                        return;
                    }
                }
            }
            wait(this);
        }
    }


    /** Get the version number.  The version number is incremented on
     *  each call to doneWriting() and also on calls to incrVersion().
     *  It is meant to track changes in the topologies within the workspace.
     *  @return A non-negative long integer.
     */
    public synchronized final long getVersion() {
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
     *  @exception InvalidStateException If this method is called when the
     *  workspace is read-only.
     */
    public final synchronized void getWriteAccess() {
        // A read-only workspace can't be written, so throw an exception.
        if (_readOnly) {
            throw new InvalidStateException(this, "Trying to get write " +
                    "access on a write-protected workspace.");
        }
        _writeReq++;

        // Go into an infinite 'while (true)' loop and check if this thread
        // can get a write access. If yes, then return, if not then perform
        // a wait() on the workspace.

        while (true) {
            Thread current = Thread.currentThread();
            if (current == _writer) {
                // already have write permission
                _writeDepth++;
                return;
            }
            if ( _writer == null) {
                // there are no writers.  Are there any readers?
                if (_readers.isEmpty() && _numPtReaders == 0) {
                    // No readers

                    _writer = current;

                    _writeDepth = 1;
                    return;
                }

                // Check if sole reader is this current thread.

                if (current instanceof PtolemyThread) {
                    // current thread is a PtolemyThread.
                    PtolemyThread ptThread = (PtolemyThread)current;
                    if (_numPtReaders == 1
                            && _readers.size() == 0
                            && ptThread.readDepth > 0) {
                        // Sole reader is this thread.
                        _writer = current;
                        _writeDepth = 1;
                        return;

                    }
                } else {
                    // current thread is not a PtolemyThread.
                    if (_readers.size() == 1 &&
                            _numPtReaders == 0 &&
                            _readers.get(current) != null) {
                        _writer = current;
                        _writeDepth = 1;
                        return;
                    }
                }
            }
	    try {
		wait();
	    } catch (InterruptedException e) {
		System.err.println(e.toString());
	    }
        }
    }

    /** Increment the version number by one.
     */
    public final synchronized void incrVersion() {
        _version++;
    }

    /** Return true if the workspace is read only, and false otherwise.
     *  @return True if the workspace is read-only, false otherwise.
     */
    public final synchronized boolean isReadOnly() {
        return _readOnly;
    }

    /** Remove the specified item from the directory.
     *  Note that that item will still refer to this workspace as
     *  its workspace (its workspace is immutable).  If the object is
     *  not in the directory, do nothing.
     *  Increment the version number.
     */
    public synchronized void remove(NamedObj item) {
        _directory.remove(item);
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

    /** Specify whether this workspace is read only. When the workspace is
     *  read only, calling getWriteAccess() or doneWriting() will result in
     *  a runtime exception, and calling getReadAccess() and doneReading()
     *  will return immediately. Accesses to topology information are
     *  considerably more efficient if the workspace is read only.
     *
     *  @param flagValue True to make the workspace read only, and false
     *   otherwise.
     *  @exception IllegalActionException If a thread has write
     *   access on the workspace.
     */
    public synchronized void setReadOnly(boolean flagValue)
            throws IllegalActionException {
        if (flagValue == true) {
            // Check if there's no writer.
            if (_writer != null) {
                throw new IllegalActionException(this, "Can't make a " +
                        "workspace read-only while there is a writer on it.");
            }
        }
        _readOnly = flagValue;
    }

    /** Return a concise description of the object.
     *  @return The class name and name.
     */
    public String toString() {
        return getClass().getName() + " {" + getFullName()+ "}";
    }

    /** Release all the read accesses held by the current thread and call
     *  wait() on the specified object. When wait() returns, re-acquire
     *  all the read accesses held earlier by the thread and return.
     *  This method helps prevent deadlocks caused when a thread that
     *  waiting for another thread to do something prevents it from doing
     *  that something by holding read access on the workspace.
     *  @param obj The object that the thread wants to wait on.
     */
    public void wait(Object obj) {
	int depth = 0;
        depth = _releaseAllReadPermissions();
        try {
            synchronized(obj) {
                obj.wait();
            }
        } catch (InterruptedException ex) {
            throw new InternalErrorException(
                    "Thread interrupted while paused! " +
                    ex.getMessage());
        } finally {
            _reacquireReadPermissions(depth);
        }
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
            Enumeration enum = directory();
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
    ////                         private methods                   ////

    /** Obtain permissions to read objects in the workspace. This obtains
     *  many permissions on the read access and should be called in
     *  conjunction with _releaseAllReadPermissions.
     *  This method suspends the calling thread until such permission
     *  has been obtained.  Permission is granted unless either another
     *  thread has write permission, or there are threads that
     *  have requested write permission and not gotten it yet.
     *  @param count This is the number of read permissions desired on the
     *  workspace.
     */
    private synchronized void _reacquireReadPermissions(int count) {
        // FIXME: this might introduce later, when you have the workspace
        // write-protected and wanting to wait(obj). Right now, since
        // only PN uses this method and PN doesn't permit the workspace
        // to be write-protected, this case shouldn't happen at all.
        // See also the _releaseAllReadPermissions() method
        if (_readOnly) {
            return;
        }


        // If the count argument is equal to zero, which means we would like
        // the current thread to has read depth equal to 0, i.e. not a reader,
        // then it's already trivially done, since this method call is always
        // preceded by _releaseAllReadPermissions.
	if (count == 0) return;

        // Go into an infinite 'while (true)' loop, and each time through
        // the loop, check if the condition is satisfied to have the current
        // thread as a writer. If not, then wait on the workspace. Upon
        // re-awakening, iterate in the loop again to check if the condition
        // is now satisfied.
        while (true) {
            Thread current = Thread.currentThread();

            // If the current thread has write permission, or if there
            // are no pending write requests, then grant read permission.
            if (current == _writer || _writeReq == 0 ) {
                if (current instanceof PtolemyThread) {
                    // Current thread is an instance of PtolemyThread.
                    _numPtReaders++;
                    ((PtolemyThread)current).readDepth = count;
                    return;
                } else {
                    // Current thread is not an instance of PtolemyThread.
                    ReadDepth depth = new ReadDepth();
                    _readers.put(current, depth);
                    depth._count = count;
                    return;
                }
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

    /** Frees the thread of all the readAccesses on the workspace. The method
     *  _reacquireAllReadAccesses should be called after this method is
     *  called.
     *  @return The number of readAccess that the thread possessed on the
     *  workspace
     */
    private synchronized int _releaseAllReadPermissions() {

        // FIXME: this might introduce a bug later..
        // Since, currently, only PN uses this code, and PN never let the
        // workspace to be read-only, you don't have to worry about it.
        if (_readOnly) {
            return 0;
        }

        // Find the current thread.
        Thread current = Thread.currentThread();

        // First check whether current thread is an instance of PtolemyThread.

        if (current instanceof PtolemyThread) {
            // the current thread is an instance of PtolemyThread.
            PtolemyThread pthread = (PtolemyThread) current;
            int depth = pthread.readDepth;
            // check if the thread is a reader.
            if (depth == 0) {
                // not a reader, so just return.
                return 0;
            } else {
                // the thread is a reader, so make it not a reader.
                // Reduce the number of PtReaders
                _numPtReaders--;
                // Make sure the state is consistent, by setting the readDepth
                // field in the thread to be zero.
                pthread.readDepth = 0;
                notifyAll();
                return depth;
            }
        } else {
            // the current thread is not an instance of PtolemyThread.
            ReadDepth depth = (ReadDepth)_readers.get(current);
            // check if the thread is a reader.
            if (depth == null) {
                return 0;
            }
            _readers.remove(current);
            notifyAll();
            return depth._count;
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial List of contained objects. */
    private LinkedList _directory = new LinkedList();

    /** @serial The name. */
    private String _name;

    /** @serial Version number. */
    private long _version = 0;

    /** @serial The currently writing thread (if any). */
    private Thread _writer;

    /** @serial The number of pending write requests plus active write
     *  permissions.
     */
    private int _writeReq = 0;

    /** @serial The number of active write permissions
     *  (all to the same thread).
     */
    private int _writeDepth = 0;

    /** @serial A table by readers (threads) of how many times they have
     *  gotten read permission.
     */
    private Hashtable _readers = new Hashtable();

    /** @serial The number of PtolemyThread readers.
     *  The use of this field is to increment it every time we have a new
     *  Ptolemy reader (readDepth field goes from 0 to 1) and decrement it
     *  whenever a Ptolemy reader relinquishes ALL its read access (readDepth
     *  field goes from 1 to 0).
     */
    private long _numPtReaders = 0;

    /** @serial Indicate that the workspace is read-only, and no changes in
     * this Workspace object is permitted.
     */
    private boolean _readOnly = false;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

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
