/* An object for synchronization and version tracking of groups of objects.

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

 Made _writer, _lastReader, _lastReaderRecord, and _readerRecords
 transient so that object would be serializable. However, serialization
 is probably not right if there are outstanding read or write permissions.
 -- eal

 */
package ptolemy.kernel.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

///////////////////////////////////////////////////////////////////
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
 The synchronization model of the workspace is a multiple-reader,
 single-writer model. Any number of threads can simultaneously read the
 workspace. Only one thread at a time can have write access to the workspace,
 and while the write access is held, no other thread can get read access.
 <p>
 When reading the state of objects in the workspace, a thread must
 ensure that no other thread is simultaneously modifying the objects in the
 workspace. To read-synchronize on a workspace, use the following code:
 <pre>
 try {
 _workspace.getReadAccess();
 // ... code that reads
 } finally {
 _workspace.doneReading();
 }
 </pre>
 We assume that the _workspace variable references the workspace, as for example
 in the NamedObj class. The getReadAccess() method suspends the current thread
 if another thread is currently modifying the workspace, and otherwise
 returns immediately. Note that multiple readers can simultaneously have
 read access. The finally clause is executed even if
 an exception occurs.  This is essential because without the call
 to doneReading(), the workspace will never again allow any thread
 to modify it.
 <p>
 To make safe changes to the objects in a workspace, a thread must
 write-synchronize using the following code:
 <pre>
 try {
 _workspace.getWriteAccess();
 // ... code that writes
 } finally {
 _workspace.doneWriting();
 }
 </pre>
 Again, the call to doneWriting() is essential, or the workspace
 will remain permanently locked to either reading or writing.
 <p>
 Note that it is not necessary to obtain a write lock just to add
 an item to the workspace directory.  The methods for accessing
 the directory are all synchronized, so there is no risk of any
 thread reading an inconsistent state.
 <p>
 A major subtlety in using this class concerns acquiring a write
 lock while holding a read lock. If a thread holds a read lock
 and calls getWriteAccess(), if any other thread holds a read lock,
 then the call to getWriteAccess() will block the calling thread
 until those other read accesses are released. However, while
 the thread is blocked, it yields its read permissions. This
 prevents a deadlock from occurring, but it means that the
 another thread may acquire write permission while the thread
 is stalled and modify the model.  Specifically, the pattern
 is:
 <pre>
     try {
        _workspace.getReadAccess();
        ... do things ...
        try {
           _workspace.getWriteAccess();
           ... at this point, the structure of a model may have changed! ...
           ... make my own changes knowing that the structure may have changed...
        } finally {
           _workspace.doneWriting();
        }
        ... continue doing things, knowing the model may have changed...
     } finally {
        _workspace.doneReading();
     }
  </pre>
  Unfortunately, a user may acquire a read access and invoke a method
  that, unknown to the user, acquires write access. For this reason, it
  is very important to document methods that acquire write access, and
  to avoid invoking them within blocks that hold read access. Note that
  there is no difficulty acquiring read access from within a block
  holding write access.

 @author Edward A. Lee, Mudit Goel, Lukito Muliadi, Xiaojun Liu
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (liuxj)
 @Pt.AcceptedRating Green (liuxj)
 */
public final class Workspace implements Nameable {
    // Note that Nameable extends ModelErrorHandler, so this class
    // need not declare that it directly implements ModelErrorHandler.

    // NOTE: it would make sense to have Workspace extend Observable
    // in order to notify observers of changes (marten 29-10-2012).

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
    public synchronized void add(NamedObj item) throws IllegalActionException {
        if (item.workspace() != this) {
            throw new IllegalActionException(this, item,
                    "Cannot add an item to the directory of a workspace "
                            + "that it is not in.");
        }

        if (item.getContainer() != null) {
            throw new IllegalActionException(this, item,
                    "Cannot add an object with a container to a workspace "
                            + "directory.");
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
     *  @exception IllegalActionException If thrown while getting the
     *  description of subcomponents.
     */
    @Override
    public synchronized String description() throws IllegalActionException {
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
     *  @exception IllegalActionException If thrown while getting the
     *  description of subcomponents.
     */
    public synchronized String description(int detail)
            throws IllegalActionException {
        // NOTE: It is not strictly needed for this method to be
        // synchronized, since _description is.  However, by making it
        // synchronized, the documentation shows this on the public
        // interface, not just the protected one.
        return _description(detail, 0, 0);
    }

    /** Enumerate the items in the directory, in the order in which
     *  they were added.
     *  @deprecated Use directoryList() instead.
     *  @return An enumeration of NamedObj objects.
     */
    @Deprecated
    public synchronized Enumeration directory() {
        return Collections.enumeration(_directory);
    }

    /** Return an unmodifiable list of the items in the directory,
     *  in the order in which they were added.
     *  @return A list of instances of NamedObj.
     */
    public synchronized List<NamedObj> directoryList() {
        return Collections.unmodifiableList(_directory);
    }

    /** Indicate that the calling thread is finished reading.
     *  If this thread is completely done reading (it has no other
     *  read access to the workspace), then notify all threads that are
     *  waiting to get read/write access to this
     *  workspace so that they may contend for access.
     *  @exception InvalidStateException If this method is called
     *   before a corresponding call to getReadAccess() by the same thread.
     */
    public final synchronized void doneReading() {
        Thread current = Thread.currentThread();
        AccessRecord record = null;

        if (current == _lastReader) {
            record = _lastReaderRecord;
        } else {
            record = _getAccessRecord(current, false);
        }

        if (record == null) {
            throw new InvalidStateException(this,
                    "Workspace: doneReading() called without a prior "
                            + "matching call to getReadAccess()!");
        }

        if (record.readDepth > 0) {
            record.readDepth--;

            if (record.readDepth == 0) {
                // the current thread is no longer a reader
                _numReaders--;

                // notify waiting writers
                // these writers may have read access, so this notification
                // cannot be conditioned on _numReaders == 0
                // possible condition: _writeReq >= _numReaders
                notifyAll();
            }
        } else if (record.failedReadAttempts > 0) {
            record.failedReadAttempts--;
        } else {
            throw new InvalidStateException(this,
                    "Workspace: doneReading() called without a prior "
                            + "matching call to getReadAccess()!");
        }
    }

    /** Indicate that the calling thread is finished writing.
     *  If this thread is completely done writing (it has no other
     *  write access to the workspace), then notify all threads
     *  that are waiting to get read/write access to this workspace
     *  so that they may contend for access.
     *  This method <b>does not</b> increment the version number
     *  of the workspace.  This method is used to temporarily add
     *  an attribute to the workspace without increment the version number,
     *  or otherwise to gain exclusive access to the workspace without
     *  changing the structure of the model.
     *  @exception InvalidStateException If this method is called before
     *   a corresponding call to getWriteAccess() by the same thread.
     *  @see ptolemy.kernel.util.Attribute#Attribute(NamedObj, String, boolean)
     *  @see #doneWriting()
     */
    public final synchronized void doneTemporaryWriting() {
        _doneWriting(false);
    }

    /** Indicate that the calling thread is finished writing.
     *  If this thread is completely done writing (it has no other
     *  write access to the workspace), then notify all threads
     *  that are waiting to get read/write access to this workspace
     *  so that they may contend for access.
     *  It also increments the version number of the workspace.
     *  @exception InvalidStateException If this method is called before
     *   a corresponding call to getWriteAccess() by the same thread.
     */
    public final synchronized void doneWriting() {
        _doneWriting(true);
    }

    /** Get the container.  Always return null since a workspace
     *  has no container.
     *  @return null.
     */
    @Override
    public NamedObj getContainer() {
        return null;
    }

    /** Return a name to present to the user, which is the
     *  same as what is returned by getName().
     *  @return The name.
     *  @see #getName()
     */
    @Override
    public String getDisplayName() {
        return getName();
    }

    /** Get the full name.
     *  @return The name of the workspace.
     */
    @Override
    public String getFullName() {
        return _name;
    }

    /** Get the name.
     *  @return The name of the workspace.
     *  @see #setName(String)
     */
    @Override
    public String getName() {
        return _name;
    }

    /** Get the name. Since this can have no container, the relative
     *  name is always the same as the name.
     *  @param relativeTo This argument is ignored.
     *  @return The name of the workspace.
     *  @see #setName(String)
     */
    @Override
    public String getName(NamedObj relativeTo) {
        return _name;
    }

    /** Obtain permission to read objects in the workspace.
     *  This method suspends the calling thread until read access
     *  has been obtained. Read access is granted unless either another
     *  thread has write access, or there are threads that
     *  have requested write access and not gotten it yet. If this thread
     *  already has read access, then access is granted irrespective of
     *  other write requests.
     *  If the calling thread is interrupted while waiting to get read
     *  access, an InternalErrorException is thrown, and the thread does
     *  not have read permission to the workspace.
     *  It is essential that a call to this method is matched by a call to
     *  doneReading(), regardless of whether this method returns normally or
     *  an exception is thrown. This is to ensure that the workspace is in a
     *  consistent state, otherwise write access may never again be
     *  granted in this workspace.
     *  If while holding read access the thread attempts to get write access
     *  and is blocked, then upon attempting to get write access, the read
     *  lock is released, and it is not reacquired until the write access
     *  is granted. Therefore, upon any call to any method that gets write
     *  access within a block holding read access, the model structure can
     *  change. You should not assume that the model is the same prior to
     *  the call as after.
     *  @exception InternalErrorException If the calling thread is interrupted
     *   while waiting to get read access.
     *  @see #doneReading()
     */
    public final synchronized void getReadAccess() {
        // This method should throw an InterruptedException when the
        // calling thread is interrupted. InterruptedException is a
        // checked exception, so changing this will lead to changes
        // everywhere this method is called, which is a huge amount
        // of work.
        Thread current = Thread.currentThread();
        AccessRecord record = null;

        if (current == _lastReader) {
            record = _lastReaderRecord;
        } else {
            record = _getAccessRecord(current, true);
        }

        if (record.readDepth > 0) {
            // If the current thread has read permission, then grant
            // it read permission
            record.readDepth++;
            return;
        } else if (current == _writer) {
            record.readDepth++;

            // The current thread has write permission, so we grant
            // read permission.
            // This is a new reader, so we increment the number
            // of readers.
            _numReaders++;
            return;
        }

        // Possibly need to wait for read access.
        // First increment this to make the record not empty, so as to
        // prevent the record from being deleted from the _readerRecords
        // table by other threads.
        record.failedReadAttempts++;

        // Go into a loop, and at each iteration check whether the current
        // thread can get read access. If not then do a wait() on the
        // workspace. Otherwise, exit the loop.
        while (_waitingWriteRequests != 0 || _writer != null) {
            try {
                wait();
            } catch (InterruptedException ex) {
                throw new InternalErrorException(current.getName()
                        + " - thread interrupted while waiting to get "
                        + "read access: " + ex.getMessage());
            }
        }

        record.failedReadAttempts--;

        // Now there is no writer, and no thread waiting to get write access.
        record.readDepth++;

        // This is a new reader, so we increment the number
        // of readers.
        _numReaders++;
        return;
    }

    /** Get the version number.  The version number is incremented on
     *  each call to doneWriting() and also on calls to incrVersion().
     *  It is meant to track changes to the objects in the workspace.
     *  @return A non-negative long integer.
     */
    public synchronized final long getVersion() {
        return _version;
    }

    /** Obtain permission to write to objects in the workspace.
     *  Write access is granted if there are no other threads that currently
     *  have read or write access.  In particular, it <i>is</i> granted
     *  if this thread already has write access, or if it is the only
     *  thread with read access. Note that if this method blocks, a side
     *  effect is that other threads will block when trying to acquire read
     *  access so as to ensure that write access will eventually be granted.
     *  This makes it very risky to hold any locks while trying to acquire
     *  read or write access.
     *  <p>
     *  This method suspends the calling thread until such access
     *  has been obtained.
     *  If the calling thread is interrupted while waiting to get write
     *  access, an InternalErrorException is thrown, and the thread does
     *  not have write permission to the workspace.
     *  It is essential that a call to this method is matched by a call to
     *  doneWriting(), regardless of whether this method returns normally or
     *  an exception is thrown. This is to ensure that the workspace is in a
     *  consistent state, otherwise read or write access may never again
     *  be granted in this workspace.
     *  @exception InternalErrorException If the calling thread is interrupted
     *   while waiting to get write access.
     *  @see #doneWriting()
     */
    public final synchronized void getWriteAccess() {
        // This method should throw an InterruptedException when the
        // calling thread is interrupted. InterruptedException is a
        // checked exception, so changing this will lead to changes
        // everywhere this method is called, which is a huge amount
        // of work.
        Thread current = Thread.currentThread();

        if (current == _writer) {
            // Already have write permission.
            _writeDepth++;
            return;
        }

        AccessRecord record = null;

        if (current == _lastReader) {
            record = _lastReaderRecord;
        } else {
            record = _getAccessRecord(current, true);
        }

        // Probably need to wait for write access.
        // First increment this to make the record not empty, so as to
        // prevent the record from being deleted from the _readerRecords
        // table by other threads.
        record.failedWriteAttempts++;

        // Go into an infinite 'while (true)' loop and check if this thread
        // can get a write access. If yes, then return, if not then perform
        // a wait() on the workspace.
        while (true) {
            if (_writer == null) {
                // There are no writers. Are there any readers?
                if (_numReaders == 0 || _numReaders == 1
                        && record.readDepth > 0) {
                    // No readers
                    // or the only reader is the current thread
                    _writer = current;
                    _writeDepth = 1;
                    record.failedWriteAttempts--;
                    return;
                }
            }
            int depth = 0;
            try {
                // If there is already another waiting write request, then
                // we have to release any read permissions that we hold or a
                // deadlock could occur. There is no need to release those
                // read permissions if this is the only pending write
                // request. If another write request shows up during the
                // wait(), then in effect this first arrived write request
                // will have priority because it holds a read permission.
                // If the other thread also holds a read permission, it
                // will have to release it upon issuing the write request.
                // Thus, the subtlety described in the class comment
                // will only occur if the second thread to attempt a
                // write access has the problematic
                // pattern of acquiring write requests inside of read
                // permission blocks. This doesn't eliminate the problems
                // associated with this subtlety, it just makes it
                // somewhat less likely that they will occur.
                if (_waitingWriteRequests > 0) {
                    // Bert Rodier suggests that we should throw an
                    // exception in this circumstance instead of just
                    // releasing read requests.  This would have the
                    // advantage that the problematic cases cited in
                    // class comment may be identified (but only if
                    // the exception is actually thrown, which depends
                    // on an accident of thread scheduling). I would
                    // prefer to use static analysis to identify cases
                    // where write permissions are accessed within
                    // read permission blocks, and analyze those
                    // cases for correct usage. EAL 11/12/08.
                    depth = _releaseAllReadPermissions();
                }
                _waitingWriteRequests++;
                wait();
            } catch (InterruptedException ex) {
                throw new InternalErrorException(current.getName()
                        + " - thread interrupted while waiting to get "
                        + "write access: " + ex.getMessage());
            } finally {
                _waitingWriteRequests--;
                if (depth > 0) {
                    _reacquireReadPermissions(depth);
                }
            }
        }
    }

    /** Handle a model error by throwing the specified exception.
     *  @param context The object in which the error occurred.
     *  @param exception An exception that represents the error.
     *  @return Never returns.
     *  @exception IllegalActionException The exception passed
     *   as an argument is always thrown.
     *  @since Ptolemy II 2.1
     */
    public boolean handleModelError(NamedObj context,
            IllegalActionException exception) throws IllegalActionException {
        throw exception;
    }

    /** Increment the version number by one.
     */
    public final synchronized void incrVersion() {
        _version++;
    }

    /** Reacquire read permission on the workspace for
     *  the current thread. Call this after a call to
     *  releaseReadPermissions().
     *  @param depth The depth of the permissions to reacquire.
     *  @see #releaseReadPermission()
     */
    public void reacquireReadPermission(int depth) {
        _reacquireReadPermissions(depth);
    }

    /** Release read permission on the workspace
     *  held by the current thread, and return the depth of the
     *  nested calls to getReadAccess(). It is essential that
     *  after calling this, you also call
     *  reacquireReadPermission(int), passing it as an argument
     *  the value returned by this method.  Hence, you should
     *  use this as follows:
     *  <pre>
     *    int depth = releaseReadPermission();
     *    try {
     *       ... do whatever here ...
     *    } finally {
     *       reacquireReadPermission(depth);
     *    }
     *  </pre>
     *  Note that this is done automatically by the
     *  wait(Object) method, so if you can use that instead,
     *  please do.
     *  @return The depth of read permissions held by the current
     *   thread.
     *  @see #reacquireReadPermission(int)
     *  @see #wait(Object)
     *  @see #wait(Object, long)
     */
    public synchronized int releaseReadPermission() {
        return _releaseAllReadPermissions();
    }

    /** Remove the specified item from the directory.
     *  Note that that item will still refer to this workspace as
     *  its workspace (its workspace is immutable).  If the object is
     *  not in the directory, do nothing.
     *  Increment the version number.
     *  @param item The NamedObj to be removed.
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
     *  @see #getName()
     */
    @Override
    public synchronized void setName(String name) {
        if (name == null) {
            name = "";
        }

        _name = name;
        incrVersion();
    }

    /** Return a concise description of the object.
     *  @return The class name and name.
     */
    @Override
    public String toString() {
        return getClass().getName() + " {" + getFullName() + "}";
    }

    /** Release all the read accesses held by the current thread and suspend
     *  the thread by calling Object.wait() on the specified object. When the
     *  call returns, re-acquire all the read accesses held earlier by the
     *  thread and return.
     *  If the calling thread is interrupted while waiting to re-acquire read
     *  accesses, an InternalErrorException is thrown, and the thread no longer
     *  has read access to the workspace.
     *  This method helps prevent deadlocks caused when a thread that
     *  waits for another thread to do something prevents it from doing
     *  that something by holding read access on the workspace.
     *  <b>IMPORTANT</b>: The calling thread should <i>not</i> hold a lock
     *  on <i>obj</i> when calling this method, unlike a direct call to
     *  <i>obj</i>.wait().  Holding such a lock can lead to deadlock because
     *  this method can block for an indeterminate amount of time while trying
     *  to reacquire read permissions that it releases. Moreover, holding such
     *  a lock is pointless since this method internally calls
     *  <i>obj</i>.wait() (within its own synchronized(<i>obj</i>) block,
     *  so the calling method cannot assume that the lock on <i>obj</i> was
     *  held during the entire execution of this method.
     *  If the calling thread needs to hold a lock on <i>obj</i> until
     *  <i>obj</i>.wait() is called, then you should manually release
     *  read permissions and release the <i>obj</i> before reacquiring them,
     *  as follows:
     *
     *  <pre>
     *    int depth = 0;
     *    try {
     *       synchronized(obj) {
     *           ...
     *           depth = releaseReadPermission();
     *           obj.wait();
     *        }
     *    } finally {
     *       if (depth &gt; 0) {
     *          reacquireReadPermission(depth);
     *       }
     *    }
     *  </pre>
     *
     *  @param obj The object that the thread wants to wait on.
     *  @exception InterruptedException If the calling thread is interrupted
     *   while waiting on the specified object and all the read accesses held
     *   earlier by the thread are re-acquired.
     *  @exception InternalErrorException If re-acquiring the read accesses
     *   held earlier by the thread fails.
     */
    public void wait(Object obj) throws InterruptedException {
        int depth = 0;
        depth = _releaseAllReadPermissions();

        try {
            synchronized (obj) {
                obj.wait();
            }
        } finally {
            // NOTE: If obj != this, and this method is called
            // inside a synchronized(obj) block, then the following
            // call can lead to deadlock because it does a wait() on
            // this workspace. It has to, since notification that will
            // free the reacquire occurs on the workspace. But it holds
            // a lock on obj during that wait. Meanwhile, another thread
            // with write permission may attempt to acquire a lock on obj.
            // Deadlock. Hence the warning in the method comment.
            _reacquireReadPermissions(depth);
        }
    }

    /** This method is equivalent to the single argument version except that
     *  you can specify a timeout, which is in milliseconds. If value of the
     *  timeout argument is zero, then the method is exactly equivalent
     *  to the single argument version, and no timeout is implemented. If
     *  the value is larger than zero, then the method returns if either
     *  the thread is notified by another thread or the timeout expires.
     *  <b>IMPORTANT</b>: The calling thread should <i>not</i> hold a lock
     *  on <i>obj</i> when calling this method, unlike a direct call to
     *  <i>obj</i>.wait().  Holding such a lock can lead to deadlock because
     *  this method can block for an indeterminate amount of time while trying
     *  to reacquire read permissions that it releases. Moreover, holding such
     *  a lock is pointless since this method internally calls
     *  <i>obj</i>.wait() (within its own synchronized(<i>obj</i>) block,
     *  so the calling method cannot assume that the lock on <i>obj</i> was
     *  held during the entire execution of this method.
     *  @param obj The object that the thread wants to wait on.
     *  @param timeout The maximum amount of time to wait, in milliseconds,
     *   or zero to not specify a timeout.
     *  @exception InterruptedException If the calling thread is interrupted
     *   while waiting on the specified object and all the read accesses held
     *   earlier by the thread are re-acquired.
     *  @exception InternalErrorException If re-acquiring the read accesses
     *   held earlier by the thread fails.
     *  @see #wait(Object)
     */
    public void wait(Object obj, long timeout) throws InterruptedException {
        int depth = 0;
        depth = _releaseAllReadPermissions();

        try {
            synchronized (obj) {
                obj.wait(timeout);
            }
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
     *  @exception IllegalActionException If thrown while getting the
     *  description of subcomponents.
     */
    protected synchronized String _description(int detail, int indent,
            int bracket) throws IllegalActionException {
        StringBuffer result = new StringBuffer(
                NamedObj._getIndentPrefix(indent));

        if (bracket == 1 || bracket == 2) {
            result.append("{");
        }

        if ((detail & NamedObj.CLASSNAME) != 0) {
            result.append(getClass().getName());

            if ((detail & NamedObj.FULLNAME) != 0) {
                result.append(" ");
            }
        }

        if ((detail & NamedObj.FULLNAME) != 0) {
            result.append("{" + getFullName() + "}");
        }

        if ((detail & NamedObj.CONTENTS) != 0) {
            if ((detail & (NamedObj.CLASSNAME | NamedObj.FULLNAME)) != 0) {
                result.append(" ");
            }

            result.append("directory {\n");

            List<NamedObj> directoryList = directoryList();
            for (NamedObj obj : directoryList) {

                // If deep is not set, then zero-out the contents flag
                // for the next round.
                if ((detail & NamedObj.DEEP) == 0) {
                    detail &= ~NamedObj.CONTENTS;
                }

                String description = obj._description(detail, indent + 1, 2)
                        + "\n";
                result.append(description);
            }

            result.append("}");
        }

        if (bracket == 2) {
            result.append("}");
        }

        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Indicate that the calling thread is finished writing.
     *  If this thread is completely done writing (it has no other
     *  write access to the workspace), then notify all threads
     *  that are waiting to get read/write access to this workspace
     *  so that they may contend for access.
     *  It also increments the version number of the workspace
     *  if the incrementWorkspaceVersion parameter is true.
     *  @param incrementWorkspaceVersion True if we should increment
     *  the version.  The incrementWorkspaceVersion parameter should
     *  almost always be true, if it is set to false, then perhaps a
     *  temporary variable is being added.
     *  @exception InvalidStateException If this method is called before
     *   a corresponding call to getWriteAccess() by the same thread.
     */
    private final synchronized void _doneWriting(
            boolean incrementWorkspaceVersion) {
        Thread current = Thread.currentThread();
        AccessRecord record = null;

        if (current == _lastReader) {
            record = _lastReaderRecord;
        } else {
            record = _getAccessRecord(current, false);
        }

        if (incrementWorkspaceVersion) {
            incrVersion();
        }

        if (current != _writer) {
            if (record != null && record.failedWriteAttempts > 0) {
                record.failedWriteAttempts--;
            } else {
                throw new InvalidStateException(this,
                        "Workspace: doneWriting called without a prior "
                                + "matching call to getWriteAccess().");
            }
        } else {
            if (_writeDepth > 0) {
                _writeDepth--;

                if (_writeDepth == 0) {
                    _writer = null;
                    notifyAll();
                }
            } else {
                throw new InvalidStateException(this,
                        "Workspace: doneWriting called without a prior "
                                + "matching call to getWriteAccess().");
            }
        }
    }

    /** Return the AccessRecord object for the specified thread.
     *  If the flag createNew is true and the current thread does not
     *  have an access record, then create a new one and return it.
     *  Set _lastReaderRecord to be the record returned.
     */
    private final AccessRecord _getAccessRecord(Thread current,
            boolean createNew) {
        //System.out.println("-- look up access record for "
        //   + current.getName());
        // If this object has been serialized and deserialized, then
        // _readerRecords could be null.
        if (_readerRecords == null) {
            _readerRecords = new HashMap();
        }

        AccessRecord record = (AccessRecord) _readerRecords.get(current);

        if (record == null) {
            // delete any record that contains no history information
            // AND is not the last reader's record
            Iterator records = _readerRecords.values().iterator();

            while (records.hasNext()) {
                AccessRecord aRecord = (AccessRecord) records.next();

                if (aRecord.failedReadAttempts == 0
                        && aRecord.failedWriteAttempts == 0
                        && aRecord.readDepth == 0
                        && aRecord != _lastReaderRecord) {
                    //System.out.println("-- delete record for thread "
                    //        + aRecord.thread
                    //        + " in " + current.getName());
                    records.remove();
                }
            }

            if (createNew) {
                record = new AccessRecord();
                _readerRecords.put(current, record);
            }
        }

        if (record != null) {
            _lastReader = current;
            _lastReaderRecord = record;
        }

        return record;
    }

    // Obtain permissions to read objects in the workspace. This obtains
    // many permissions on the read access and should be called in
    // conjunction with _releaseAllReadPermissions.
    // This method suspends the calling thread until such permission
    // has been obtained.  Permission is granted unless either another
    // thread has write permission, or there are threads that
    // have requested write permission and not gotten it yet.
    // @param count This is the number of read permissions desired on the
    //  workspace.
    // @exception InternalErrorException If the calling thread is interrupted
    //  while waiting to re-acquire read permissions.
    private synchronized void _reacquireReadPermissions(int count) {
        // If the count argument is equal to zero, which means we would like
        // the current thread to has read depth equal to 0, i.e. not a reader,
        // then it's already trivially done, since this method call is always
        // preceded by _releaseAllReadPermissions.
        if (count == 0) {
            return;
        }

        Thread current = Thread.currentThread();
        AccessRecord record = null;

        if (current == _lastReader) {
            record = _lastReaderRecord;
        } else {
            record = _getAccessRecord(current, false);
        }

        if (record == null || count > record.failedReadAttempts) {
            throw new InvalidStateException(this, "Trying to reacquire "
                    + "read permission not in record.");
        }

        // Go into an infinite 'while (true)' loop, and each time through
        // the loop, check if the condition is satisfied to have the current
        // thread as a writer. If not, then wait on the workspace. Upon
        // re-awakening, iterate in the loop again to check if the condition
        // is now satisfied.
        while (true) {
            // If the current thread has write permission, or if there
            // are no pending write requests, then grant read permission.
            if (current == _writer || _waitingWriteRequests == 0
                    && _writer == null) {
                _numReaders++;
                record.failedReadAttempts -= count;
                record.readDepth = count;
                return;
            }

            try {
                wait();
            } catch (InterruptedException ex) {
                throw new InternalErrorException(
                        "Thread interrupted while waiting for read access!"
                                + ex.getMessage());
            }
        }
    }

    /** Frees the thread of all the readAccesses on the workspace
     *  held by the current thread. The method
     *  _reacquireAllReadAccesses should be called after this method is
     *  called.
     *  @return The number of readAccess that the thread possessed on the
     *  workspace
     */
    private synchronized int _releaseAllReadPermissions() {
        // Find the current thread.
        Thread current = Thread.currentThread();
        AccessRecord record = null;

        if (current == _lastReader) {
            record = _lastReaderRecord;
        } else {
            record = _getAccessRecord(current, false);
        }

        if (record == null || record.readDepth == 0) {
            // current thread is not a reader
            return 0;
        } else {
            _numReaders--;
            notifyAll();

            int result = record.readDepth;
            record.failedReadAttempts += result;
            record.readDepth = 0;
            return result;
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
    private transient Thread _writer;

    /** @serial The number of pending write requests.
     */
    private int _waitingWriteRequests = 0;

    /** @serial The number of active write permissions
     *  (all to the same thread).
     */
    private int _writeDepth = 0;

    /** @serial The last thread that acquires/releases read permission.
     */
    private transient Thread _lastReader = null;

    private transient AccessRecord _lastReaderRecord = null;

    private transient HashMap _readerRecords = new HashMap();

    /** @serial The number of readers.
     *  The use of this field is to increment it every time we have a new
     *  reader and decrement it whenever a reader relinquishes ALL its read
     *  access.
     */
    private long _numReaders = 0;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private static final class AccessRecord {

        // FindBugs suggested making this class a static inner class:
        //
        // "This class is an inner class, but does not use its embedded
        // reference to the object which created it. This reference makes
        // the instances of the class larger, and may keep the reference
        // to the creator object alive longer than necessary. If
        // possible, the class should be made into a static inner class."

        // the number of failed calls to getReadAccess() performed
        // by a thread and not yet matched by a call to doneReading()
        public int failedReadAttempts = 0;

        // the number of failed calls to getWriteAccess() performed
        // by a thread and not yet matched by a call to doneWriting()
        public int failedWriteAttempts = 0;

        // the number of successful calls to getReadAccess() performed
        // by a thread and not yet matched by a call to doneReading()
        public int readDepth = 0;

        //public Thread thread = null;
        //public boolean inUse;
    }
}
