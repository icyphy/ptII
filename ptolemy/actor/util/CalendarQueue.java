/* CalendarQueue, an implementation of a priority queue.

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
import ptolemy.kernel.util.Debuggable;

import java.util.LinkedList;
import java.util.Iterator;

///////////////////////////////////////////////////////////////////
//// CalendarQueue
/**

This class implements a fast priority queue. Entries are sorted with
the help of a comparator provided to the constructor.  A dequeue
operation will remove the smallest entry according to this sort.
Entries can be any instance of Object that are acceptable to the
specified comparator.

Entries are enqueued using the put() method, and dequeued using the
take() method. The get() method returns the smallest entry in the
queue, but without removing it from the queue.  The toArray() methods
can be used to examine the contents of the queue.

This class operates like a 'bag' or multiset collection.  This simply
means that an entry can be added into the queue even if it already
exists in the queue.  If a 'set' behavior is desired, one can subclass
CalendarQueue and override the put() method.

The queue works as follows.  Entries are conceptually stored in an
infinite set of virtual bins (or buckets). The instance of
CQComparator is consulted to determine which virtual bin should be
used for an entry (by calling its getVirtualBinNumber() method).  Each
virtual bin has a width, which can be altered by calling the
setBinWidth() method of the CQComparator.  Within each virtual bin,
entries are sorted.

Having an infinite number of bins, however, is not practical.  Thus,
the virtual bins are mapped into physical bins (or buckets) by a
modulo operation.  If there are <i>n</i> physical bins, then virtual
bin <i>i</i> maps into physical bin <i>i</i> mod <i>n</i>.

This is analogous to a calendar showing 12 months.  Here, <i>n</i> =
12.  An event that happens in January of any year is placed in the
first month (bin) of this calendar.  Its virtual bin number might be
<i>year</i>*12 + <i>month</i>.  Its physical bin number is just
<i>month</i>.

The performance of a calendar queue is very sensitive to the number of
bins, the width of the bins, and the relationship of these quantities
to the entries that are observed.  Thus, this implementation may
frequently change the number of bins.  When it does change the number
of bins, it changes them by a specifiable <i>bin count factor</i>.
This defaults to 2, but can be specified as a constructor argument.
Suppose the bin count factor is <i>binCountFactor</i> and the current
number of buckets is <i>n</i> (by default, this starts at 2, but can
be specified by a constructor argument, <i>minNumBuckets</i>).  The
number of bins will be multiplied by <i>binCountFactor</i> if the
queue size exceeds <i>n * binCountFactor</i>.  The number of bins will
be divided by <i>binCountFactor</i> if the queue size falls below
<i>n/binCountFactor</i>.  Thus, the queue attempts to keep the number
of bins close to the size of the queue.  Each time it changes the
number of bins, it uses recently dequeued entries to calculate a
reasonable bin width (actually, it defers to the associated
CQComparator for this calculation).

Changing the number of bins is a relatively expensive operation, so it
may be worthwhile to increase <i>binCountFactor</i> to reduce the
frequency of change operations. Working counter to this, however, is
that the queue is most efficient when there is on average one event
per bin.  Thus, the queue becomes less efficient if change operations
are less frequent.  Change operations can be entirely disabled by
calling setAdaptive() with argument <i>false</i>.

This implementation is not synchronized, so if multiple threads
depend on it, the caller must be.

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
@see ptolemy.actor.util.CQComparator
*/
public class CalendarQueue implements Debuggable {
    /** Construct an empty queue with a given comparator, which
     *  is used to sort the entries.  The bin count factor and the
     *  initial and minimum number of bins are set to 2.
     *  @param comparator The comparator used to sort entries.
     */
    public CalendarQueue(CQComparator comparator) {
        _cqComparator = comparator;
    }

    /** Construct an empty queue with the specified comparator,
     *  which is used to sort the entries, the specified
     *  minimum number of buckets, and the specified bin count factor.
     *  The bin count factor multiplies or divides the number of bins
     *  when the number of bins is changed.
     *  The specified minimum number of buckets is also the initial
     *  number of buckets.
                           *  @param comparator The comparator used to sort entries.
                           *  @param minNumBuckets The minimum number of buckets.
                           *  @param binCountFactor The bin count factor.
                           */
                          public CalendarQueue(CQComparator comparator,
                                  int minNumBuckets,
                                  int binCountFactor) {
                              this(comparator);
                              _minNumBuckets = minNumBuckets;
                              _queueBinCountFactor = binCountFactor;
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

                          /** Empty the queue, discarding all current information.
                           *  On the next put(), the queue will be reinitialized, including
                           *  setting the bin width and zero reference of the comparator.
                           *  @see CQComparator#setZeroReference
                           */
                          public void clear() {
                              _initialized = false;
                              _qSize = 0;
                          }

                          /** Return entry that is at the head of the
                           *  queue (i.e. the one that will be obtained by the next take()).
                           *  If the queue is empty, then throw an exception.
                           *
                           *  @return Object The smallest entry in the queue.
                           *  @exception IllegalActionException If the queue is empty.
                           */
                          public final Object get() throws IllegalActionException {
                              // First check whether the queue is empty.
                              if (_qSize == 0) {
                                  throw new IllegalActionException("Queue is empty.");
                              }
                              Object[] contents = toArray(1);
                              return contents[0];
                          }

                          /** Return true if the specified entry is in the queue.
                           *  This is checked using the equals() method.
                           *  @param entry The entry.
                           *  @return True if the specified entry is in the queue.
                           */
                          public boolean includes(Object entry) {
                              if (_qSize == 0) return false;
                              return _bucket[_getBinIndex(entry)].includes(entry);
                          }

                          /** Return true if the queue is empty, and false otherwise.
                           *  @return True if empty, false otherwise.
                           */
                          public final boolean isEmpty() {
                              return (_qSize == 0);
                          }

                          /** Add an entry to the queue.
                           *  The first time this is called after queue creation or after
                           *  calling clear() results in the comparator having its zero reference
                           *  set to the argument and its bin width set to the default.
                           *  Also, the number of buckets is set to the minimum (which has
                           *  been given to the constructor).
                           *
                           *  If the specified entry cannot be compared by the comparator
                           *  associated with this queue, then a ClassCastException will
                           *  be thrown.
                           *
                           *  This method always returns true, but the return value may be
                           *  used in derived classes to indicate that the entry is
                           *  already on the queue
                           *  and is not added again.  In this class, the entry is always added,
                           *  even if an identical entry already exists on the queue.
                           *
                           *  @param entry The entry to be added to the queue.
                           *  @return True.
                           *  @exception ClassCastException If the specified entry cannot
                           *   be compared by the associated comparator.
                           */
                          public boolean put(Object entry) {
                              if (_debugging) _debug("+ putting in queue: " + entry);

                              // If this is the first put since the queue creation,
                              // then do initialization.
                              if (!_initialized) {
                                  _cqComparator.setZeroReference(entry);
                                  _cqComparator.setBinWidth(null);
                                  _qSize = 0;
                                  _localInit(_minNumBuckets, entry);
                                  // Indicate that we do not have enough samples redo width.
                                  _sampleValid = false;
                              }

                              // Get the bin number.
                              int binNumber = _getBinIndex(entry);

                              // If _minimumEntry is equal to null (which happens before any entries
                              // are put in the queue), or the queue size is zero,
                              // or if the new entry is less than the current
                              // smallest entry) then update the minimum entry of the queue.
                              if (_minimumEntry == null || _qSize == 0 ||
                                      _cqComparator.compare(entry, _minimumEntry) < 0) {
                                  _minimumEntry = entry;
                                  _minVirtualBucket = _cqComparator.getVirtualBinNumber(entry);
                                  _minBucket = _getBinIndex(entry);
                              }

                              // Insert the entry into bucket binNumber, which has a sorted list.
                              _bucket[binNumber].insert(entry);
                              // Indicate increased size.
                              ++_qSize;

                              // Change the calendar size if needed.
                              _resize(true);

                              return true;
                          }

                          /** Remove the specified entry and return true if the entry
                           *  is found and successfully removed, and false
                           *  if it is not found.
                           *  Equality is tested using the equals() method of the entry.
                           *  If there are multiple entries in the queue that match, then
                           *  only the first one is removed. The first one always
                           *  corresponds to the one enqueued first among those multiple entries.
                           *  Therefore, the queue has FIFO behavior.
                           *  @param entry The entry to remove.
                           *  @return True If a match is found and the entry is removed.
                           */
                          public boolean remove(Object entry) {
                              // If the queue is empty then return false.
                              if (_qSize == 0) {
                                  return false;
                              }
                              boolean result = _bucket[_getBinIndex(entry)].remove(entry);
                              if (result) {
                                  _qSize--;
                                  _resize(false);
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
                           *  @param flag If false, disable changing the number of bins.
                           */
                          public void setAdaptive(boolean flag) {
                              _resizeEnabled = flag;
                          }

                          /** Return the queue size, which is the number of entries currently
                           *  in the queue.
                           *  @return The queue size.
                           */
                          public final int size() {
                              return _qSize;
                          }

                          /** Remove the smallest entry and return it.
                           *  If there are multiple smallest entries, then return the
                           *  first one that was put in the queue (FIFO behavior).
                           *
                           *  @return The entry that is smallest, per the comparator.
                           *  @exception IllegalActionException If the queue is empty.
                           */
                          public final Object take() throws IllegalActionException {
                              // First check whether the queue is empty.
                              if (_qSize == 0) {
                                  throw new IllegalActionException(
                                          "Cannot take from an empty queue.");
                              }

                              // Search the buckets starting from the index given by _minBucket.
                              // This is analogous to starting from the current page(month)
                              // of the calendar.
                              int i = _minBucket, j = 0;
                              int indexOfMinimum = i;
                              Object minSoFar = null;
                              Object result = null;
                              while (true) {
                                  // At each bucket, we first determine whether the bucket is empty.
                                  // If not, then we check whether the first entry in the
                                  // bucket is in the current year. This is done simply by
                                  // comparing the virtual bucket number to _minVirtualBucket.
                                  // If an entry is in the current year, then return it.
                                  // If no entry is in the current year, then we cycle
                                  // through all buckets and find the minimum entry.
                                  if (!_bucket[i].isEmpty()) {
                                      // The bucket is not empty.
                                      Object minimumInBucket = _bucket[i].head.contents;
                                      if (_cqComparator.getVirtualBinNumber(minimumInBucket)
                                              == _minVirtualBucket + j) {
                                          // The entry is in the current year. Return it.
                                          result = _takeFromBucket(i);
                                          break;
                                      } else {
                                          // The entry is not in the current year.
                                          // Compare to the minimum found so far.
                                          if (minSoFar == null) {
                                              minSoFar = minimumInBucket;
                                              indexOfMinimum = i;
                                          } else if (_cqComparator.compare(minimumInBucket,
                                                  minSoFar) < 0) {
                                              minSoFar = minimumInBucket;
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
                                      if (minSoFar == null) {
                                          throw new InternalErrorException(
                                                  "Queue is empty, but size() is not zero!");
                                      }
                                      result = _takeFromBucket(indexOfMinimum);
                                      break;
                                  }
                              }
                              _collect(result);
                              if (_debugging) _debug("- taking from queue: " + result);
                              return result;
                          }

                          /** Return the entries currently in the queue as an array.
                           *  @return The entries currently in the queue.
                           */
                          public final Object[] toArray() {
                              return toArray(Integer.MAX_VALUE);
                          }

                          /** Return the entries currently in the queue,
                           *  but no more of them than the number given as an
                           *  argument.
                           *  To get all the entries in the queue, call this method
                           *  with argument Integer.MAX_VALUE.
                           *  @param limit The maximum number of entries desired.
                           *  @return The entries currently in the queue.
                           */
                          public final Object[] toArray(int limit) {
                              if (limit > _qSize) limit = _qSize;
                              Object[] result = new Object[limit];
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
                              Object minSoFar = null;

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
                                  // through all buckets and find the minimum entry.
                                  if (bucketHead[currentBucket] != null) {
                                      // There are more entries in the bucket.
                                      Object nextInBucket = bucketHead[currentBucket].contents;
                                      while (_cqComparator.getVirtualBinNumber(nextInBucket)
                                              == virtualBucket) {
                                          // The entry is in the current year. Return it.
                                          result[index] = nextInBucket;
                                          index++;
                                          if (index == limit) return result;
                                          bucketHead[currentBucket] = bucketHead[currentBucket].next;
                                          if (bucketHead[currentBucket] == null) break;
                                          nextInBucket = bucketHead[currentBucket].contents;
                                      }
                                      long nextVirtualBucket =
                                          _cqComparator.getVirtualBinNumber(nextInBucket);

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

                          // Collect an entry for later use to recalculate bin widths.
                          // The entry is collected only if it is strictly greater than the
                          // previously collected entry.
                          private void _collect(Object entry) {
                              if ((_previousTakenEntry == null) ||
                                      (_cqComparator.compare(entry, _previousTakenEntry) > 0)) {
                                  _sampleEntries[_sampleEntryIndex++] = entry;
                                  if(_sampleEntryIndex == _SAMPLE_SIZE) {
                                      _sampleEntryIndex = 0;
                                      _sampleValid = true;
                                  }
                                  _previousTakenEntry = entry;
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

                          // Get the virtual bin index of the entry, and map it into
                          // a physical bin index.
                          private int _getBinIndex(Object entry) {
                              long i = _cqComparator.getVirtualBinNumber(entry);
                              i = i % _nBuckets;
                              if (i < 0) i += _nBuckets;
                              return (int)i;
                          }

                          // Initialize the bucket array to the specified number of buckets
                          // with the specified width.
                          //
                          // @param nbuckets The number of buckets.
                          // @param firstEntry: First entry of the new queue.
                          private void _localInit(int nbuckets, Object firstEntry) {

                              _nBuckets = nbuckets;
                              _bucket = new CQLinkedList[_nBuckets];

                              for (int i = 0; i < _nBuckets; ++i) {
                                  // Initialize each bucket with an empty CQLinkedList
                                  // that uses _cqComparator for sorting.
                                  _bucket[i] = new CQLinkedList();
                              }

                              // Set the initial position in queue.
                              _minimumEntry = firstEntry;
                              _minVirtualBucket = _cqComparator.getVirtualBinNumber(firstEntry);
                              _minBucket = _getBinIndex(firstEntry);

                              // Set the queue size change thresholds.
                              _bottomThreshold = _nBuckets/_queueBinCountFactor;
                              _topThreshold = _nBuckets*_queueBinCountFactor;
                              _qSizeOverThreshold = _qSizeUnderThreshold = 0;
                              _initialized = true;
                          }

                          // Check to see whether the queue needs resizing, and if it does, do it.
                          // Copy the queue into a new queue with the specified number of buckets.
                          // Unlike Randy Brown's realization, a new queue is reallocated
                          // each time the number of buckets changes.
                          // The resize methods follows these steps:
                          // 1. A new bin width is computed as a function of the entry statistics.
                          // 2. Create the new queue and initialize it.
                          // 3. Transfer all elements from the old queue into the new queue.
                          // The argument indicates whether the queue just increased or decreased
                          // in size.
                          private void _resize(boolean increasing) {
                              if (!_resizeEnabled) return;
                              int newsize = _nBuckets;
                              boolean resize = false;
                              if (increasing) {
                                  if (_qSize > _topThreshold) _qSizeOverThreshold++;
                                  if (_qSizeOverThreshold > _RESIZE_LAG) {
                                      resize = true;
                                      _qSizeOverThreshold = 0;
                                      newsize = (_nBuckets*_queueBinCountFactor);
                                      if (_debugging) {
                                          _debug(">>>>>> increasing number of buckets to: "
                                                  + newsize);
                                      }
                                  }
                              } else {
                                  // Queue has just decreased in size.
                                  if (_qSize < _bottomThreshold) _qSizeUnderThreshold++;
                                  if (_qSizeUnderThreshold > _RESIZE_LAG) {
                                      resize = true;
                                      _qSizeUnderThreshold = 0;
                                      // If it is already minimum or close, do nothing.
                                      if (_nBuckets/_queueBinCountFactor > _minNumBuckets) {
                                          newsize = (_nBuckets/_queueBinCountFactor);
                                          if (_debugging) {
                                              _debug(">>>>>> decreasing number of buckets to: "
                                                      + newsize);
                                          }
                                      }
                                  }
                              }
                              if (!resize) return;

                              // Find new bucket width, if appropriate.
                              if (_sampleValid) {
                                  // Have to copy samples into a new array to ensure that they
                                  // increasing...
                                  Object[] sampleCopy = new Object[_SAMPLE_SIZE];
                                  for (int i = 0; i < _SAMPLE_SIZE; i++) {
                                      sampleCopy[i] = _sampleEntries[_sampleEntryIndex++];
                                      if(_sampleEntryIndex == _SAMPLE_SIZE) {
                                          _sampleEntryIndex = 0;
                                      }
                                  }
                                  _cqComparator.setBinWidth(sampleCopy);
                                  if (_debugging) _debug(">>> changing bin width.");
                              }

                              // Save location and size of the old calendar.
                              CQLinkedList[] old_bucket = _bucket;
                              int old_nbuckets = _nBuckets;

                              // Initialize new calendar.
                              _localInit(newsize, _minimumEntry);
                              _qSize = 0;

                              // Go through each of the old buckets and add its elements
                              // to the new queue. Disable debugging to not report these puts.
                              boolean saveDebugging = _debugging;
                              _debugging = false;
                              boolean saveResizeEnabled = _resizeEnabled;
                              _resizeEnabled = false;
                              for (int i = 0; i < old_nbuckets; i++) {
                                  while (!old_bucket[i].isEmpty()) {
                                      put(old_bucket[i].take());
                                  }
                              }
                              _debugging = saveDebugging;
                              _resizeEnabled = saveResizeEnabled;
                          }

                          // Take the first entry from the specified bucket and return
                          // its value.
                          private Object _takeFromBucket(int index) {
                              Object minEntry = _bucket[index].take();

                              // Update the position on the calendar.
                              _minBucket = index;
                              _minimumEntry = minEntry;
                              _minVirtualBucket = _cqComparator.getVirtualBinNumber(minEntry);
                              --_qSize;

                              // Reduce the calendar size if needed.
                              _resize(false);

                              // Return the item found.
                              return minEntry;
                          }

                          ///////////////////////////////////////////////////////////////////
                          ////                         private inner class               ////

                          // Specialized sorted list of objects.  This class is used
                          // instead of java's built-in collections in order to provide the
                          // following features:
                          //   - multiset behavior.  An entry can appear more than once.
                          //   - monitoring of buckets in order to trigger reallocation.
                          //   - monitoring of time stamps in order to help set bin width.
                          // The class is very specialized, with many features of generic
                          // collections missing.
                          //
                          private class CQLinkedList {

                              // Construct an empty list.
                              public CQLinkedList() {
                                  head = null;
                                  tail = null;
                              }

                              public final Object first() {
                                  return head.contents;
                              }

                              public final boolean includes(Object obj) {
                                  return (head.find(obj) != null);
                              }

                              public final boolean isEmpty() {
                                  return (head == null);
                              }

                              // Insert the specified entry into the list, in sorted position.
                              // If there are already objects of the same key, then this one
                              // is positioned after those objects.
                              public final void insert(Object obj) {

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
                                  if (_cqComparator.compare(obj, tail.contents) >= 0) {
                                      // obj becomes new tail.
                                      CQCell newTail = new CQCell(obj, null);
                                      tail.next = newTail;
                                      tail = newTail;
                                      return;
                                  }

                                  // Check if head is strictly greater than obj
                                  if ( _cqComparator.compare(head.contents, obj) > 0) {
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
                                      if ( _cqComparator.compare(currCell.contents, obj) > 0) {
                                          // insert obj between prevCell and currCell
                                          CQCell newcell = new CQCell(obj, currCell);
                                          prevCell.next = newcell;
                                          return;
                                      }
                                      prevCell = currCell;
                                      currCell = prevCell.next;
                                  } while (currCell != null);
                              }

                              // Remove the specified element from the queue, where equals() is used
                              // to determine a match.  Only the first matching element that is found
                              // is removed. Return true if a matching element is found and removed,
                              // and false otherwise.
                              public final boolean remove(Object obj) {
                                  // two special cases:
                                  // Case 1: list is empty: always return false.
                                  if (head == null) return false;
                                  // Case 2: The element I want is at head of the list.
                                  if (head.contents.equals(obj)) {
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
                                      if (currCell.contents.equals(obj)) {
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

                              // Remove and return the first element in the list.
                              public final Object take() {
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

                              // Construct a cell with the specified contents, pointing to the
                              // specified next cell.
                              // @param contents The contents.
                              // @param next The next cell, or null if this is the end of the list.
                              public CQCell(Object contents, CQCell next) {
                                  this.contents = contents;
                                  this.next = next;
                              }

                              // Search the list for the specified element (using equals() to
                              // identify a match).  Note that this does a linear search
                              // starting at the begining of the list.
                              // @param element Element to look for.
                              // @return The cell containing the element, or null if there is none.
                              public final CQCell find(Object element) {
                                  for (CQCell p = this; p != null; p = p.next) {
                                      if (p.contents.equals(element)) return p;
                                  }
                                  return null;
                              }

                              // The contents of the cell.
                              public Object contents;

                              // The next cell in the list, or null if there is none.
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
                          private final static int _RESIZE_LAG = 32;

                          // The largest queue size before number of buckets gets increased.
                          private int _topThreshold;

                          // The smallest queue size before number of buckets gets decreased.
                          private int _bottomThreshold;

                          // An indicator of whether the queue has been initialized.
                          private boolean _initialized = false;

                          // The array of bins or buckets.
                          private CQLinkedList[] _bucket;

                          // The number of buckets to start with and the lower bound
                          // on the number of buckets.
                          private int _minNumBuckets = 2;

                          // The factor by which to multiply (or divide)
                          // the number of bins to when resizing.
                          private int _queueBinCountFactor = 2;

                          // Flag for enable/disable changing the number of bins.
                          private boolean _resizeEnabled = true;

                          // Comparator to determine how to order entries.
                          private CQComparator _cqComparator;

                          // The minimum entry in the queue.
                          // All entries in the queue have greater keys (or equal).
                          private Object _minimumEntry = null;

                          // The quantized value of _minimumEntry (the virtual bin number).
                          private long _minVirtualBucket;

                          // The positive modulo of _minimumEntry (the physical bin number).
                          private int _minBucket;

                          // Number of entries to calculate bin width.
                          private final static int _SAMPLE_SIZE = 8;

                          // Sample entries to use to calculate bin width.
                          private Object[] _sampleEntries = new Object[_SAMPLE_SIZE];

                          // Index into the sample entries array.
                          private int _sampleEntryIndex = 0;

                          // Indicator of whether there are enough sample entries to calculate width.
                          private boolean _sampleValid = false;

                          // The most recently collected entry.
                          private Object _previousTakenEntry = null;
                      }
