/* CalendarQueue, an O(1) implementation of Priority Queue.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Yellow (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.util;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.InternalErrorException;

import java.util.LinkedList;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// CalendarQueue
/**
This class implements a fast priority queue. Entries are sorted
according to their sort key. A dequeue operation will remove the entry
that has the smallest sort key.
<p>
For reusability, the sort keys can be any instance of Object.
A client needs to implement the CQComparator interface to define how the
sort keys are compared. This implementation is provided to
the CalendarQueue constructor.
<p>
Entries are enqueued using the put() method, and dequeued using the take()
method. The take() method returns the entry associated with the
smallest key. The toArray() methods can be used to examine the contents
of the queue.
<p>
CalendarQueue operates like a 'bag' or multiset
collection. This simply means that an
entry can be added into the queue even if it already exists in the queue.
If a 'set' behavior is desired, one can subclass CalendarQueue and
override the put() method.
<p>
Associated with the take() method, we have getNextKey() and getPreviousKey().
The former returns the current smallest sort key in the queue,
while the latter returns
the sort key associated with the entry that was last dequeued using the
take() method. For example, suppose the smallest-key entry is associated
with value 'CC' and key 'S'. Then the sequence getNextKey(), take(), and
getPreviousKey() will return 'S', 'CC', and 'S'.
<p>
The queue works as follows.  Entries are conceptually stored in
an infinite set of virtual bins (or buckets). The instance of CQComparator
is consulted to determine which virtual bin should be used for an entry
(by calling its getBinIndex() method).  Each virtual bin has a width
(determined by calling the getBinWidth() method of the CQComparator).
Within each virtual bin, entries are sorted by key.
<p>
Having an infinite number of bins, however, is not practical.
Thus, the virtual bins are mapped into physical bins (or buckets)
by a modulo operation.  If there are <i>n</i> physical bins,
then virtual bin <i>i</i> maps into physical bin <i>i</i> mod <i>n</i>.
<p>
This is analogous to a calendar showing 12 months.  Here, <i>n</i> = 12.
An event that happens in January of any year is placed in the first
month (bin) of this calendar.  Its virtual bin number might be
<i>year</i>*12 + <i>month</i>.  Its physical bin number is just <i>month</i>.
<p>
The performance of a calendar queue is very sensitive to the number
of bins, the width of the bins, and the relationship of these quantities
to the keys that are observed.  Thus, this implementation may frequently
change the number of bins.  When it does change the number of bins,
it changes them by a specifiable <i>bin count factor</i>.
This defaults to 2, but can be specified as a constructor argument.
Suppose the bin count factor is <i>f</i> and the current number of
buckets is <i>n</i> (this defaults to 2, but can be specified by
a constructor argument).
The number of bins will be multiplied by <i>f</i> if the 
queue size exceeds <i>nf</i>.
The number of bins will be divided by <i>f</i> if the 
queue size falls below <i>n/f</i>.  Thus, the queue attempts to
keep the number of bins close to the size of the queue.
Each time it resizes the queue, it uses data currently in the
queue to calculate a reasonable bin width (actually, it defers
to the associated CQComparator for this calculation).
<p>
Changing the number of bins is a relatively expensive operation,
so it may be worthwhile to increase <i>f</i> to reduce the frequency
of change operations. Working counter to this, however, is that the
queue is most efficient when there is on average one event per bin.
Thus, the queue becomes less efficient if change operations are less
frequent.  Change operations can be entirely disabled by calling
setAdaptive().
<p>
This implementation is not synchronized, so if multiple threads
depend on it, the caller must be.
<p>
This implementation is based on:
<ul>
<li>Randy Brown, <i>CalendarQueues:A Fast Priority Queue Implementation for
the Simulation Event Set Problem</i>, Communications of the ACM, October 1988,
Volume 31, Number 10.
<li>A. Banerjea and E. W. Knightly, <i>Ptolemy 0 implementation of
CalendarQueue class.</i>
</ul>
@author Lukito Muliadi and Edward A. Lee
@version $Id$
@see CQComparator
*/

public class CalendarQueue {

    /** Construct an empty queue with a given comparator, which
     *  is used to sort the entries.
     *  @param comparator The comparator used to sort entries.
     */
    public CalendarQueue(CQComparator comparator) {
        _cqComparator = comparator;
    }

    /** Construct an empty queue with the specified comparator,
     *  which is used to sort the entries, the specified
     *  minimum number of buckets, and the specified bin count factor.
     *  The bin count factor multiplies or divides the number of bins
     *  when the number of bins is changed.  It defaults to 2 if the
     *  other constructor is used.
     *  The minimum number of buckets is also the initial number
     *  of buckets.  It too defaults to 2 if the other constructor is used.
     *  @param comparator The comparator used to sort entries.
     *  @param minNumBuckets The minimum number of buckets.
     *  @param resizeFactor The threshold factor.
     */
    public CalendarQueue(CQComparator comparator,
            int minNumBuckets,
            int resizeFactor) {
        this(comparator);
        _minNumBuckets = minNumBuckets;
        _queueResizeFactor = resizeFactor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append a listener to the current set of debug listeners.
     *  If the listener is already in the set, do not add it again.
     *  @param listener The listener to which to send debug messages.
     */
    public void addDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            _debugListeners = new LinkedList();
        } else {
            if (_debugListeners.contains(listener)) {
                return;
            }
        }
        _debugListeners.add(listener);
        _debugging = true;
    }

    /** Empty the queue.
     */
    public void clear() {
        _zeroRef = null;
        _qSize = 0;
        _takenKey = null;
    }

    /** Return the key associated with the object that is at the head of the
     *  queue (i.e. the one that would be obtained on the next take()).
     *  If the queue is empty, then throw an exception.
     *  <p>
     *  NOTE: This method is slower
     *  than the similar method getPreviousKey(). Therefore, it is
     *  recommended to use getPreviousKey() whenever possible.
     *
     *  @return Object The smallest sort key in the queue.
     *  @exception IllegalActionException If the queue is empty.
     */
    public final Object getNextKey()
            throws IllegalActionException {
        // First check whether the queue is empty.
        if (_qSize == 0) {
            throw new IllegalActionException(
                "Cannot getNextKey() on an empty queue.");
        }
        Object[][] contents = toArray(1);
        return contents[0][0];
    }

    /** Return the key of the last object dequeued using take() method.
     *  If the queue was empty when the last take() method was invoked,
     *  then throw an exception (note that the last take() method would have
     *  also thrown an exception). If take() has never been called, then throw
     *  an exception as well.
     *  A typical application would call take() followed
     *  by getPreviousKey() to get the value and its corresponding
     *  key, respectively.
     *
     *  @return The sort key associated with the last entry dequeued by the
     *   take() method.
     *  @exception IllegalActionException If take() has not been
     *   called, or the queue was empty when it was last called.
     */
    public final Object getPreviousKey()
            throws IllegalActionException {
        // First check if _takenKey == null which means either the last take()
        // threw an exception or take() has never been called.
        if (_takenKey == null) {
            throw new IllegalActionException("No previous key available.");
        }
        return _takenKey;
    }

    /** Return true if the specified entry is in the queue.
     *  This is checked using the equals() method of the key and value.
     *  @return True if the specified entry is in the queue.
     *  @param key The sort-key of the entry.
     *  @param value The value of the entry.
     */
    public boolean includes(Object key, Object value) {
        // If the queue is empty, then return false
        if (_qSize == 0) return false;

        // Create a CQEntry object to wrap value and key.
        CQEntry cqEntry = new CQEntry(value, key);

        // Calculate the index into the queue array.
        long i = _getBinIndex(key);
        i = i % _nBuckets;
        if (i < 0) i += _nBuckets;

        // Query the bucket to see whether the entry is included.
        return _bucket[(int)i].includes(cqEntry);
    }

    /** Return true if the queue is empty, and false otherwise.
     *  @return True if empty, false otherwise.
     */
    public final boolean isEmpty() {
        return (_qSize == 0);
    }

    /** Add an entry to the queue. An entry is specified by its key and
     *  its value.  This method always returns true.  A derived class,
     *  however, may return false if the entry is already on the queue
     *  and is not added again.  In this class, the entry is always added,
     *  even if an identical entry already exists on the queue.
     *
     *  @param key The key of the entry to be added to the queue.
     *  @param value The value of the entry to be added to the queue.
     *  @return True.
     *  @exception IllegalArgumentException If the key is null.
     */
    public boolean put(Object key, Object value) {
        // create a CQEntry object to wrap value and key
        CQEntry cqEntry = new CQEntry(value, key);
        return put(cqEntry);
    }

    /** Add an entry to the queue. An entry is given as an instance of
     *  CQEntry. This method always returns true.  A derived class,
     *  however, may return false if the entry is already on the queue
     *  and is not added again.  In this class, the entry is always added,
     *  even if an identical entry already exists on the queue.
     *
     *  @param key The key of the entry to be added to the queue.
     *  @param value The value of the entry to be added to the queue.
     *  @return True.
     *  @exception IllegalArgumentException If the key is null.
     */
    public boolean put(CQEntry cqEntry) {
        if (cqEntry.key == null) {
            throw new IllegalArgumentException(
                "CalendarQueue.put() can't accept null key"
            );
        }
        if (_debugging) _debug("+ queueing event: " + cqEntry.value
                + ", tag: " + cqEntry.key);

        // If this is the first put since the queue creation,
        // then do initialization.
        if (_zeroRef == null) {
            _zeroRef = cqEntry.key;
            _qSize = 0;
            _localInit(_minNumBuckets, _cqComparator.getBinWidth(null),
                    cqEntry.key);
            // Indicate that we do not have enough samples redo width.
            _sampleValid = false;
            _previousTakenKey = null;
        }

        // Get the virtual bin number.
        long i = _getBinIndex(cqEntry.key);
        // Translate to a physical bin number.
        i = i % _nBuckets;
        // Make sure the bin number is non-negative.
        if (i < 0) i += _nBuckets;

        // If _minKey equal to null (which happens before any entries are put
        // in the queue), or the queue size is zero,
        // or if the new entry has lower key than the current
        // smallest key) then update the minimum key of the queue.
        if (_minKey == null || _qSize == 0 ||
                _cqComparator.compare(cqEntry.key, _minKey) < 0) {
            _minKey = cqEntry.key;
            _minVirtualBucket = _getBinIndex(_minKey);
            _minBucket = (int)(_minVirtualBucket % _nBuckets);
            if (_minBucket < 0) _minBucket += _nBuckets;
        }

        // Insert the entry into bucket i, which has a sorted list.
        _bucket[(int)i].insert(cqEntry);
        // Indicate increased size.
        ++_qSize;

        // Change the calendar size if needed.
        if (_qSize > _topThreshold) _qSizeOverThreshold++;
        if (_qSizeOverThreshold > _RESIZE_LAG) {
            _resize(_nBuckets*_queueResizeFactor);
        }
        return true;
    }

    /** Remove a specific entry (specified by a key and
     *  value). This method returns true if the entry
     *  is found and successfully removed, and false
     *  if it is not found.
     *  Equality is tested using the equals() method of the key and value.
     *  If there are multiple entries in the queue that match, then
     *  only the first one is removed. The first one always
     *  correspond to the one enqueued first among those multiple entries.
     *  Therefore, the queue follows FIFO behavior.
     *  @param key The sort key for the entry to remove.
     *  @param value The value for the entry to remove.
     *  @return True if a match is found and removed, false otherwise.
     */
    public boolean remove(Object key, Object value) {
        // If the queue is empty then return false.
        if (_qSize == 0) {
            return false;
        }

        CQEntry cqEntry = new CQEntry(value, key);

        // Calculate the bin number.
        long i = _getBinIndex(key);
        i = i % _nBuckets;
        if (i < 0) i += _nBuckets;

        // Remove the object.
        boolean result = _bucket[(int)i].remove(cqEntry);

        if (result) {
            _qSize--;
        }
        return result;
    }

    /** Unregister a debug listener.  If the specified listener has not
     *  been previously registered, then do nothing.
     *  @param listener The listener to remove from the list of listeners
     *   to which debug messages are sent.
     */
    public void removeDebugListener(DebugListener listener) {
        if (_debugListeners == null) {
            return;
        }
        _debugListeners.remove(listener);
        if (_debugListeners.size() == 0) {
            _debugListeners = null;
            _debugging = false;
        }
        return;
    }

    /** Enable or disable changing the number of bins (or buckets)
     *  in the queue.  These change operations are fairly expensive,
     *  so in some circumstances you may wish to simply set the
     *  number of bins (using the constructor) and leave it fixed.
     *  If however the queue size becomes much larger or much smaller
     *  than the number of bins, the queue will become much less
     *  efficient.
     *  @param boole If false, disable changing the number of bins.
     */
    public void setAdaptive(boolean boole) {
        _resizeEnabled = boole;
    }

    /** Return the queue size, which is the number of entries currently
     *  in the queue.
     *  @return The queue size.
     */
    public final int size() {
        return _qSize;
    }

    /** Remove the entry with the smallest key and return its value.
     *  If there are multiple entries with the same smallest key,
     *  then return the first one that was put in the queue (FIFO
     *  behavior).  If the key returned is not the same as the key
     *  previously returned by this method, then add it to the
     *  collection of keys to use in any future recalculations of
     *  the bin width.  Note that since values are
     *  permitted to be null, this method could return null.
     *  @return The value associated with the smallest key.
     *  @exception IllegalActionException If the queue is empty.
     */
    public final Object take() throws IllegalActionException {
        // First check whether the queue is empty.
        if (_qSize == 0) {
            _takenKey = null;
            throw new IllegalActionException(
                "Cannot take from an empty queue.");
        }

        // Search the buckets starting from the index given by _minBucket.
        // This is analogous to starting from the current page(month)
        // of the calendar.
        int i = _minBucket, j = 0;
        int indexOfMinimum = i;
        Object minKeySoFar = null;
        Object result = null;
        while (true) {
            // At each bucket, we first determine if the bucket is empty.
            // If not, then we check whether the first entry in the
            // bucket is in the current year. This is done simply by
            // comparing the virtual bucket number to _minVirtualBucket.
            // If an entry is in the current year, then return it.
            // If no entry is in the current year, then we cycle
            // through all buckets and find the entry with the minimum
            // key.
            if (!_bucket[i].isEmpty()) {
                // The bucket is not empty.
                Object minimumKeyInBucket = _bucket[i].head.contents.key;
                if (_getBinIndex(minimumKeyInBucket) == _minVirtualBucket + j) {
                    // The entry is in the current year. Return it.
                    result = _takeFromBucket(i);
                    break;
                } else {
                    // The entry is not in the current year. 
                    // Compare key to minimum found so far.
                    if (minKeySoFar == null) {
                        minKeySoFar = minimumKeyInBucket;
                        indexOfMinimum = i;
                    } else if (_cqComparator.compare(minimumKeyInBucket,
                            minKeySoFar) < 0) {
                        minKeySoFar = minimumKeyInBucket;
                        indexOfMinimum = i;
                    }
                }
            }
            // Prepare to check the next bucket
            ++i; ++j;
            if (i == _nBuckets) i = 0;

            // If one round of search has already elapsed,
            // then return the minimum that we have found.
            if (i == _minBucket) {
                if (minKeySoFar == null) {
                    throw new InternalErrorException(
                        "Queue is empty, but size() is not zero!");
                }
                result = _takeFromBucket(indexOfMinimum);
                break;
            }
        }
        _collect(_takenKey);
        if (_debugging) _debug("- taking event: " + result);
        return result;
    }

    /** Return the keys and values currently in the queue as a pair
     *  of arrays.  The return value is an array of arrays, where the
     *  0 element is an array of keys and the 1 element is an array
     *  of values.
     *  @return The keys and values currently in the queue.
     */
    public final Object[][] toArray() {
        return toArray(Integer.MAX_VALUE);
    }

    /** Return the keys and values currently in the queue as a pair
     *  of arrays, but no more of them than the number given as an
     *  argument.  The return value is an array of arrays, where the
     *  0 element is an array of keys and the 1 element is an array
     *  of values.  Each array has length equal to the argument
     *  or to the size of the queue, whichever is smaller.
     *  To get all the entries in the queue, call this method
     *  with argument Integer.MAX_VALUE.
     *  @param limit The maximum number of keys and values desired.
     *  @return The keys and values currently in the queue.
     */
    public final Object[][] toArray(int limit) {
        if (limit > _qSize) limit = _qSize;
        Object[][] result = new Object[2][limit];
        if (_qSize == 0) return result;
        int index = 0;

        // Iterate through the buckets collecting entries in order.
        // In each bucket, accept all entries in the current year
        // (with the right virtual bucket number).

        int currentBucket = _minBucket;
        long virtualBucket = _minVirtualBucket;
        long minimumNextVirtualBucket = Long.MAX_VALUE;
        int indexOfMinimum = currentBucket;
        int nextStartBucket = _minBucket;
        Object minKeySoFar = null;

        // Keep track of where we are in each bucket.
        CQCell[] bucketHead = new CQCell[_bucket.length];
        for (int i = 0; i < _bucket.length; i++) {
            bucketHead[i] = _bucket[i].head;
        }
        while (true) {

            // At each bucket, we first determine whether there are more
            // entries to look at in the bucket.
            // If so, then we check whether the next entry in the
            // bucket is in the current year. This is done simply by
            // comparing the virtual bucket number to a running count of
            // the year.
            // If an entry is in the current year, then add it to the result.
            // If no entry is in the current year, then we cycle
            // through all buckets and find the entry with the minimum
            // key.
            if (bucketHead[currentBucket] != null) {
                // There are more entries in the bucket.
                Object nextKeyInBucket = bucketHead[currentBucket].contents.key;

                while (_getBinIndex(nextKeyInBucket) == virtualBucket) {
                    // The entry is in the current year. Return it.
                    result[0][index] = nextKeyInBucket;
                    result[1][index] = bucketHead[currentBucket].contents.value;
                    index++;
                    if (index == limit) return result;
                    bucketHead[currentBucket] = bucketHead[currentBucket].next;
                    if (bucketHead[currentBucket] == null) break;
                    nextKeyInBucket = bucketHead[currentBucket].contents.key;
                }
                long nextVirtualBucket = _getBinIndex(nextKeyInBucket);

                if (nextVirtualBucket < minimumNextVirtualBucket) {
                    minimumNextVirtualBucket = nextVirtualBucket;
                    nextStartBucket = currentBucket;
                }
            }
            // Prepare to check the next bucket
            ++currentBucket; ++virtualBucket;
            if (currentBucket == _nBuckets) currentBucket = 0;

            // If one round of search has elapsed,
            // then increment the virtual bucket.
            if (currentBucket == nextStartBucket) {
                if (minimumNextVirtualBucket == Long.MAX_VALUE) {
                    throw new InternalErrorException(
                        "Queue is empty, but size() is not zero! It is: "
                        + _qSize);
                }
                virtualBucket = minimumNextVirtualBucket;
                minimumNextVirtualBucket = Long.MAX_VALUE;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Collect a key for later use to recalculate bin widths.
    // The key is collected only if it is strictly greater than the
    // previously collected key.
    private void _collect(Object key) {
        if ((_previousTakenKey == null) ||
                (_cqComparator.compare(key, _previousTakenKey) > 0)) {
            _sampleKeys[_sampleKeyIndex++] = key;
            if(_sampleKeyIndex == _SAMPLE_SIZE) {
                _sampleKeyIndex = 0;
                _sampleValid = true;
            }
            _previousTakenKey = key;
        }
    }

    /** Send a debug message to all debug listeners that have registered.
     *  By convention, messages should not include a newline at the end.
     *  The newline will be added by the listener, if appropriate.
     *  @param message The message.
     */
    private final void _debug(String message) {
        if (_debugListeners == null || !_debugging) {
            return;
        } else {
            Iterator listeners = _debugListeners.iterator();
            while (listeners.hasNext()) {
                ((DebugListener)listeners.next()).message(message);
            }
        }
    }

    // Note: This is basically a macro..
    private long _getBinIndex(Object key) {
        return _cqComparator.getBinIndex(key, _zeroRef, _width);
    }

    // Initialize the bucket array to the specified number of buckets
    // with the specified width.
    //
    // nbuckets: number of total buckets
    // bwidth: bucket width
    // startKey: starting date of the new calendar
    private void _localInit(
            int nbuckets,
            Object bwidth,
            Object startKey) {

        _width = bwidth;
        _nBuckets = nbuckets;
        _bucket = new CQLinkedList[_nBuckets];

        for (int i = 0; i < _nBuckets; ++i) {
            // Initialize each bucket with an empty CQLinkedList
            // that uses _cqComparator for sorting.
            _bucket[i] = new CQLinkedList();
        }

        // Set the initial position in queue.
        _minKey = startKey;
        _minVirtualBucket = _getBinIndex(startKey);
        _minBucket = (int)(_minVirtualBucket % _nBuckets);
        if (_minBucket < 0) _minBucket += _nBuckets;

        // Set the queue size change thresholds.
        _bottomThreshold = _nBuckets/_queueResizeFactor;
        _topThreshold = _nBuckets*_queueResizeFactor;
        _qSizeOverThreshold = _qSizeUnderThreshold = 0;
    }

    // Copy the queue into a new queue with the specified number of buckets.
    // Unlike Randy Brown's realization, a new queue is reallocated
    // each time the number of buckets changes.
    // The resize methods follows these steps:
    // 1. A new bin width is computed as a function of the entry statistics.
    // 2. Create the new queue and initialize it.
    // 3. Transfer all elements from the old queue into the new queue.
    private void _resize(int newsize) {
        if (!_resizeEnabled) return;
        if (_debugging) _debug(">>>>>> changing number of buckets to: "
                + newsize);

        // Find new bucket width, if appropriate.
        Object new_width = _width;
        if (_sampleValid) {
            // Have to copy samples into a new array to ensure that they
            // increasing...
            Object[] sampleCopy = new Object[_SAMPLE_SIZE];
            for (int i = 0; i < _SAMPLE_SIZE; i++) {
                sampleCopy[i] = _sampleKeys[_sampleKeyIndex++];
                if(_sampleKeyIndex == _SAMPLE_SIZE) {
                    _sampleKeyIndex = 0;
                }
            }
            new_width = _cqComparator.getBinWidth(sampleCopy);
            if (_debugging) _debug(">>> changing bin width to: " + new_width);
        }

        // Save location and size of the old calendar.
        CQLinkedList[] old_bucket = _bucket;
        int old_nbuckets = _nBuckets;

        // Initialize new calendar.
        _localInit(newsize, new_width, _minKey);
        _qSize = 0;

        // Go through each of the old buckets and add its elements
        // to the new queue. Disable debugging to not report these puts.
        boolean saveDebugging = _debugging;
        _debugging = false;
        _resizeEnabled = false;
        for (int i = 0; i < old_nbuckets; i++) {
            while (!old_bucket[i].isEmpty()) {
                CQEntry entry = (CQEntry)old_bucket[i].take();
                put(entry);
            }
        }
        _debugging = saveDebugging;
        _resizeEnabled = true;
    }

    // Take the first entry from the specified bucket and return
    // its value.
    private Object _takeFromBucket(int index) {
        CQEntry minEntry = (CQEntry)_bucket[index].take();

        // Update the position on the calendar.
        _minBucket = index;
        _minKey = minEntry.key;
        _minVirtualBucket = _getBinIndex(_minKey);
        --_qSize;

        // Reduce the calendar size if needed.
        if (_qSize < _bottomThreshold) _qSizeUnderThreshold++;
        if (_qSizeUnderThreshold > _RESIZE_LAG) {
            // If it is already minimum or close, do nothing.
            if (_nBuckets/_queueResizeFactor > _minNumBuckets) {
                _resize (_nBuckets/_queueResizeFactor);
            }
        }
        // Return the item found.
        _takenKey = minEntry.key;
        return minEntry.value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private inner class               ////

    // A value and its key.
    private class CQEntry {

        public Object value;
        public Object key;

        public CQEntry(Object value, Object key) {
            this.value = value;
            this.key = key;
        }

        // Override Object.equal() method to compare the contents
        // using their equals() methods.
        public final boolean equals(Object obj) {
            if (!(obj instanceof CQEntry)) {
                return false;
            } else {
                CQEntry snd = (CQEntry) obj;

                if (value == null) {
                    if (snd.value != null) {
                        return false;
                    }
                } else if (!value.equals(snd.value)) {
                    return false;
                }
                // Values are equal.

                if (this.key == null || snd.key == null) {
                    throw new InternalErrorException(
                        "Null keys found in queue!");
                } else if (!this.key.equals(snd.key)){
                    return false;
                }
                // Keys are equal.
                return true;
            }
        }
    }

    // Specialized sorted list of CQEntry objects.  This class is used
    // instead of java's SortedMap in order to provide the following
    // features:
    //   - multiset behavior.  An entry can appear more than once.
    //   - monitoring of buckets in order to trigger reallocation.
    //   - monitoring of time stamps in order to help set bin width.
    // The class is very specialized, with many features of generic
    // collections missing.
    private class CQLinkedList {

        // Construct an empty list.
        public CQLinkedList() {
            head = null;
            tail = null;
        }

        public final CQEntry first() {
            return head.contents;
        }

        public final boolean includes(CQEntry obj) {
            return (head.find(obj) != null);
        }

        public final boolean isEmpty() {
            return (head == null);
        }

        // Insert the specified entry into the list, in sorted position.
        // If there are already objects of the same key, then this one
        // is positioned after those objects.
        public final void insert(CQEntry obj) {

            // Special case: linked list is empty.
            if (head == null) {
                head = new CQCell(obj, null);
                tail = head;
                return;
            }

            // LinkedList is not empty.
            // I assert that by construction, when head != null,
            // then tail != null as well.

            // Special case: Check if obj is greater than or equal to tail.
            if (_cqComparator.compare(obj.key, tail.contents.key) >= 0) {
                // obj becomes new tail.
                CQCell newTail = new CQCell(obj, null);
                tail.next = newTail;
                tail = newTail;
                return;
            }

            // Check if head is strictly greater than obj
            if ( _cqComparator.compare(head.contents.key,obj.key) > 0) {
                // obj becomes the new head
                head = new CQCell(obj, head);
                return;
            }

            // No more special cases.
            // Iterate from head of queue.
            CQCell prevCell = head;
            CQCell currCell = prevCell.next;
            // Note that this loop will always terminate via the return
            // statement. This is because tail is assured of being strictly
            // greater than obj.
            do {
                // check if currCell is strictly greater than obj
                if ( _cqComparator.compare(currCell.contents.key,obj.key) > 0) {
                    // insert obj between prevCell and currCell
                    CQCell newcell = new CQCell(obj, currCell);
                    prevCell.next = newcell;
                    return;
                }
                prevCell = currCell;
                currCell = prevCell.next;
            } while (currCell != null);
        }

        // Remove the specified element from the queue, where equal() is used
        // to determine a match.  Only the first matching element that is found
        // is removed. Return true if a matching element is found and removed,
        // and false otherwise.
        public final boolean remove(CQEntry cqEntry) {
            // two special cases:
            // Case 1: list is empty: always return false.
            if (head == null) return false;
            // Case 2: The element I want is at head of the list.
            if (head.contents.equals(cqEntry)) {
                if (head != tail) {
                    // Linked list has at least two cells.
                    head = head.next;
                } else {
                    // Linked list contains only one cell
                    head = null;
                    tail = null;
                }
                return true;
            }

            // Non-special case that requires looping:
            CQCell prevCell = head;
            CQCell currCell = prevCell.next;
            do {
                if (currCell.contents.equals(cqEntry)) {
                    // Found a match.
                    if (tail == currCell) {
                        // Removing the tail. Need to update.
                        tail = prevCell;
                    }
                    prevCell.next = currCell.next;
                    return true;
                }
                prevCell = currCell;
                currCell = currCell.next;
            } while (currCell != null);

            // No matching entry was found.
            return false;
        }

        // Remove and eturn the first element in the list.
        public final CQEntry take() {
            // remove the head
            CQCell oldHead = head;
            head = head.next;
            if (head == null) {
                tail = null;
            }
            return oldHead.contents;
        }

        // The head of the list.
        public CQCell head;

        // The tail of the list.
        public CQCell tail;
    }

    // Simplified and specialized linked list cell.  This is based on
    // Doug Lea's implementation in collections, but with most of the
    // functionality stripped out.
    private class CQCell {

        /** Construct a cell with the specified contents, pointing to the
         *  specified next cell.
         *  @param contents The contents.
         *  @param next The next cell, or null if this is the end of the list.
         */
        public CQCell(CQEntry contents, CQCell next) {
            this.contents = contents;
            this.next = next;
        }

        /** Search the list for the specified element (using equals() to
         *  identify a match).  Note that this does a linear search
         *  starting at the begining of the list.
         *  @param element Element to look for.
         *  @return The cell containing the element, or null if there is none.
         **/
         public final CQCell find(Object element) {
             for (CQCell p = this; p != null; p = p.next) {
                 if (p.contents.equals(element)) return p;
             }
             return null;
         }

        /** The contents of the cell. */
        public CQEntry contents;

        /** The next cell in the list, or null if there is none. */
        public CQCell next;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial The list of DebugListeners registered with this object. */
    private LinkedList _debugListeners = null;

    /** @serial A flag indicating whether there are debug listeners. */
    private boolean _debugging;

    // The number of buckets in the queue.
    private int _nBuckets;

    // The current queue size.
    private int _qSize = 0;

    // The number of times that the queue has exceeded the current
    // threshold.
    private int _qSizeOverThreshold = 0;

    // The number of times that the queue has dropped the current
    // threshold.
    private int _qSizeUnderThreshold = 0;

    // The number of times a threshold must be exceeded to trigger resizing.
    private final static int _RESIZE_LAG = 8;

    // The width of each bucket, as specified by the associated CQComparator.
    private Object _width;

    // The largest queue size before number of buckets gets increased.
    private int _topThreshold;

    // The smallest queue size before number of buckets gets decreased.
    private int _bottomThreshold;

    // The key of the first entry seen.
    private Object _zeroRef = null;

    // The array of bins or buckets.
    private CQLinkedList[] _bucket;

    // The number of buckets to start with and the lower bound
    // on the number of buckets.
    private int _minNumBuckets = 2;

    // The factor by which to multiply (or divide)
    // the number of bins to when resizing.
    private int _queueResizeFactor = 2;

    // Flag for enable/disable changing the number of bins.
    private boolean _resizeEnabled = true;

    // Comparator to determine how to order entries.
    private CQComparator _cqComparator;

    // The key of the most recent entry removed by take().
    private Object _takenKey = null;

    // The minimum key. All elements in the queue have greater keys (or equal).
    private Object _minKey = null;

    // The quantized value of _minKey (the virtual bin number).
    private long _minVirtualBucket;

    // The positive modulo of _minKey (the physical bin number).
    private int _minBucket;

    // Number of key samples to calculate bin width.
    private final static int _SAMPLE_SIZE = 8;

    // Sample keys to use to calculate bin width.
    private Object[] _sampleKeys = new Object[_SAMPLE_SIZE];

    // Index into the sample keys array.
    private int _sampleKeyIndex = 0;

    // Indicator of whether there are enough sample keys to calculate width.
    private boolean _sampleValid = false;

    // The most recently collected key.
    private Object _previousTakenKey = null;
}
