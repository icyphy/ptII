/* CalendarQueue, an O(1) implementation of Priority Queue

 Copyright (c) 1998 The Regents of the University of California.
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

package pt.actor.util;

import collections.*;

//////////////////////////////////////////////////////////////////////////
//// CalendarQueue
/**
This class implements a fast sorted queue. It is similar to priority queue,
with the only difference is in priority queue, event with 'highest
priority' will be dequeued first, while in sorted queue, event with
'lowest key' will be dequeued first).
<p>
The sort-keys are defined by the Object interface.
<p>
Entries are enqueued using the put() method, and dequeued using the take()
method. The take() method returns the entry associated with 
'lowest key'.
<p>
There are 2 modes of operation which affect <i>only</i> how put() method 
behaves, namely BAGMODE and SETMODE. The default mode is BAGMODE.
This mode of operation affect put() method in the following way. If the
queue is in BAGMODE, then the put() method will not check for the
occurence of the entry being enqueued; i.e. it's possible to have multiple
occurences of identical entries. On the other hand, the SETMODE implies
that the put() method will check for the occurence of the enqueued entry,
<i>before</i> actually enqueueing it. This does not, however, imply single
occurence of all entries, but simply, all put() method being invoked while
the queue is in 'Set' mode will effectively do nothing if the enqueued
entry is already in there.
<p>
Associated with the take() method, we have getNextKey() and getPreviousKey().
The first returns the current smallest sort-key, while the latter returns 
the sort-key associated with the entry that was last dequeued using the
take() method. For example, suppose the 'lowest key' entry is associated
with object 'CC', and key 'S', then a sequence of getNextKey(), take(),
getPreviousKey() will returns 'S' 'CC' 'S'.
<p>
This implementation is based on:
<ul>
<li>Randy Brown. "CalendarQueues:A Fast Priority Queue Implementation for 
the Simulation Event Set Problem".
<li>A. Banerjea and E. W. Knightly. "Ptolemy 0 implementation: 
CalendarQueue.cc".
</ul>
@author Lukito Muliadi
@version $Id$
@see CalendarQueueComparator
*/

public class CalendarQueue {

    /** Construct an empty queue.
     *  The mode of operation is 'Bag' by default.
     */
    public CalendarQueue(CQComparator comparator) {
        _cqComparator = comparator;
        // initialization is already done using
        // field initialiation during the
        // declaration of variables
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Add one entry to the queue. An entry is specified by an object and
     *  a Object object. If the Object object is null, then an exception
     *  is thrown.
     *  As explained above, this method will do nothing if the mode of
     *  operation is SETMODE and the enqueued entry is already in the queue.
     *
     * @param obj Object to be put
     * @param key Object corresponding to the Object put
     * @exception IllegalArgumentException
     */
    public void put(Object key, Object obj)
            throws IllegalArgumentException {
        if (key == null)
            throw new IllegalArgumentException(
                    "SortedQueue.put() can't accept null Object object"
                    );

        // if this is the first put since the queue creation,
        // then do initialization.
        // The initialization is deferred until this stage because
        // when the queue is created, we don't know what kind of
        // Object implementation we'll get, so can't
        // initialize the zeroRef, and bin width.
        // The zero refererence is chosen to be the first entry, while
        // initial bin width is obtained by passing null argument to
        // getBinWidth() method in Object.
        if (_zeroRef == null) {
            _zeroRef = key;
            _localInit(2, _cqComparator.getBinWidth(null), key);
        }

        // create a CQEntry object to wrap obj and key
        CQEntry cqEntry = new CQEntry(obj, key);

        // calculate i, the index into the queue array according to
        // these steps:
        // 1. quantize the Object object
        // 2. calculate the modulo with respect to nBuckets
        // 3. add nBuckets if necessary to get a positive modulo

        int i = _cqComparator.getBinIndex(key, _zeroRef, _width);
        i = i % _nBuckets;
        if (i < 0)
            i += _nBuckets;

        // if _minKey equal to null (happens when there are no entries
        // in the queue) or the new entry has lower key than the current
        // lowest key) then update.
        if (_minKey == null || key.compare(_minKey) < 0) {
            _minKey = key;
            _minVirtualBucket = _cqComparator.getBinIndex(_minKey, _zeroRef, _width);
            _minBucket = _minVirtualBucket % _nBuckets;
            if (_minBucket < 0) _minBucket += _nBuckets;
        }

        // Insert entry into bucket i in sorted list
        _bucket[i].insertAndSort(cqEntry);
        // Increase the queue size
        ++_qSize;

        // double the calendar size if needed
        if (_qSize > _topThreshold) {
            _resize(2*_nBuckets);
        }
    }

     /** Remove the smallest entry from the queue and return the object
      *  associated with that entry. If the queue is empty, return null.
      * @return Object associated with the lowest key or null
      */
    public Object take() {
        // first check if the queue is empty, if it is, return null
        if (_qSize == 0) {
            // also need to set _minKey equal to null,
            // because a lower bound of empty set doesn't make sense.
            _minKey = null;
            return null;
        }
        // Search buckets starting from index: _minBucket
        //   analogy: starting from the current page(month) of the calendar
        for (int i = _minBucket, j = 0; ; )
        {
            // Starting from the lastBucket, we go through each bucket in
            // a cyclic fashion (i.e. modulo fashion) for one whole cycle.
            // At each bucket, we first determine if the bucket is empty.
            // If not, then we check if the content of the bucket is in the
            // current year. (This latter check is done simply by
            // comparing the virtual bucket number)
            //
            // A bucket that's found to satisfy both condition is where
            // we're going to take our next item.
            //
            // If turns out, after a whole cycle, we still can't find such
            // bucket, then we go to direct search. Using brute force, we
            // compare the items with lowest key in each bucket and find
            // the one with lowest key among those items. The bucket
            // containing that 'lowest lowest' entry is then
            // recorded as the lastBucket, and we resume by calling take()
            // but now start searching from the new lastBucket.

            if (    !_bucket[i].isEmpty()
                    &&
                    _cqComparator.getBinIndex(_bucket[i].peekKey(), _zeroRef, _width)
                    == _minVirtualBucket + j
                   ) {

                // Item to take has been found, remove that
                // item from the list
                CQEntry linkFound = (CQEntry)_bucket[i].take();

                // Update position on calendar
                _minBucket = i;
                _minKey = linkFound.key();
                _minVirtualBucket = _cqComparator.getBinIndex(_minKey, _zeroRef, _width);
                --_qSize;

                // Halve calendar size if needed.
                if (_qSize < _botThreshold) {
                   _resize (_nBuckets/2);
                }
                // Return the item found.
                return linkFound.object();
            }
            else {
                // Prepare to check next bucket or else go to a direct search.
                ++i; ++j;
                if (i == _nBuckets) i = 0;
                // If one round of search already elapsed,
                // then go to direct search
                if (i == _minBucket) {
                    // Go to direct search
                    break;
                }
            }
        }
        // Directly search for minimum key entry.
        // Find lowest entry by examining first event of each bucket.
        // Note that the first entry of a bucket corresponds to the
        // one with lowest key among those in that bucket.

        // startComparing being false, indicate we have yet to find
        // a non-empty bucket, so can't compare yet.
        boolean startComparing = false;
        int minVirtualBucket = 0;
        int minBucket = -1;
        Object minKey = null;
        for (int i = 0; i < _nBuckets; i++) {
            // First check if bucket[i] is empty
            if (!_bucket[i].isEmpty()) {

                if (!startComparing) {
                    minBucket = i;
                    minKey = _bucket[i].peekKey();
                    minVirtualBucket = _cqComparator.getBinIndex(minKey, _zeroRef, _width);
                    startComparing = true;
                } else {
                    Object maybeMinKey = _bucket[i].peekKey();
                    if (maybeMinKey.compare(minKey) < 0) {
                        minKey = maybeMinKey;
                        minVirtualBucket = _cqComparator.getBinIndex(minKey, _zeroRef, _width);
                        minBucket = i;
                    }
                }

            }
        }

        if (minBucket == -1)
            throw new IllegalStateException("Failed Direct search");
        // Set lastBucket, lastPrio, and bucketTop for this event
        _minBucket = minBucket;
        _minVirtualBucket = minVirtualBucket;
        _minKey = minKey;
        // Resume search at that minimum bucket
        return (take());
    }
    /** Return the key of the last object dequeued using take() method. 
     *  If previous take() method returned null i.e. the queue was empty, 
     *  then this method will return null as well
     *
     *  NOTE: a typical application would call take() followed
     *  by getPreviousKey() to get an object and it's corresponding
     *  priority, respectively.
     * @return Object object associated with the last object taked
     */
    public Object getPreviousKey() {
        return _minKey;
    }

    /** Return the key associated with the object that's at the head of the 
     *  queue (i.e. the one that would be obtained on the next take).
     *  If the queue is empty, return null.
     *
     *  NOTE: due to implementation detail, this method is less efficient
     *  than the similar method getPreviousKey(). Therefore, it's
     *  recommended to use getPreviousKey() whenever possible.
     * @return Object object
     */
    public Object getNextKey() {
        // first check if the queue is empty, if it is, returns null
        if (_qSize == 0) {
            // also need to set _minKey equal to null,
            // because an upper bound of empty set doesn't make sense.
            //
            _minKey = null;
            return null;
        }
        // Search buckets starting from index: _minBucket
        //   analogy: starting from the current page(month) of the calendar
        for (int i = _minBucket, j = 0; ; )
        {
            // Starting from the lastBucket, we go through each bucket in
            // a cyclic fashion (i.e. modulo fashion) for one whole cycle.
            // At each bucket, we first determine if the bucket is empty.
            // If not, then we check if the content of the bucket is in the
            // current year. (This latter check is done simply by
            // comparing the virtual bucket number)
            //
            // A bucket that's found to satisfy both condition is where
            // we're going to take our next item.
            //
            // If turns out, after a whole cycle, we still can't find such
            // bucket, then we go to direct search. Using brute force, we
            // compare the items with lowest key of each bucket and find
            // the one with lowest key among those entries. The bucket
            // containing that 'lowest lowest' key item is then
            // recorded as the lastBucket, and we resume by calling take()
            // but now start searching from the new lastBucket.

            if (    !_bucket[i].isEmpty()
                    &&
                    _cqComparator.getBinIndex(_bucket[i].peekKey(), _zeroRef, _width)
                    == _minVirtualBucket + j
                   ) {

                // Item to take has been found, remove that
                // item from the list
                CQEntry linkFound = (CQEntry)_bucket[i].first();

                // Return the item found.
                return linkFound.key();
            }
            else {
                // Prepare to check next bucket or else go to a direct search.
                ++i; ++j;
                if (i == _nBuckets) i = 0;
                // If one round of search already elapsed,
                // then go to direct search
                if (i == _minBucket) {
                    // Go to direct search
                    break;
                }
            }
        }
        // Directly search for minimum key event.
        // Find lowest key by examining first event of each bucket.
        // Note that the first event of a bucket corresponds to the
        // one with lowest key among those in that bucket.

        // startComparing being false, indicate we have yet to find
        // a non-empty bucket, so can't compare yet.
        boolean startComparing = false;
        int minVirtualBucket = 0;
        int minBucket = -1;
        Object minKey = null;
        for (int i = 0; i < _nBuckets; i++) {
            // First check if bucket[i] is empty
            if (_bucket[i].isEmpty()) {
                // do nothing if it's empty
            } else {
                if (!startComparing) {
                    minBucket = i;
                    minKey = _bucket[i].peekKey();
                    minVirtualBucket = _cqComparator.getBinIndex(minKey, _zeroRef, _width);
                    startComparing = true;
                } else {
                    Object maybeMinKey = _bucket[i].peekKey();
                    if (maybeMinKey.compare(minKey) < 0) {
                        minKey = maybeMinKey;
                        minVirtualBucket = _cqComparator.getBinIndex(minKey, _zeroRef, _width);
                        minBucket = i;
                    }
                }
            }
        }

        if (minBucket == -1)
            throw new IllegalStateException(
                    "Direct search error in SortedQueue.getNextKey"
                    );
        // Return the lowest key of minBucket
        return ((CQEntry)_bucket[minBucket].first()).key();

    }

    /** Remove a specific entry (specified by the object and the
     *  Object object). This method returns true if the object
     *  is found and successfully removed, and returns false
     *  if it's either not found or the queue is empty.
     *
     *  The equality is tested by doing this operation:
     *  obj.equal(obj2) && key.equal(key2)
     *
     * @param obj the object that you want to remove
     * @param key the sort-key corresponding to that object
     * @return true is succeed, false otherwise
     */
    public boolean removesEntry(Object obj, Object key) {
        // if the queue is empty then return false
        if (_qSize == 0) {
            return false;
        }

        // create a CQEntry object to wrap obj and priority
        CQEntry cqEntry = new CQEntry(obj, key);

        // calculate i, the index into the queue array
        int i = _cqComparator.getBinIndex(key, _zeroRef, _width);
        i = i % _nBuckets;
        if (i < 0)
            i += _nBuckets;

        // Remove the object by calling the method in
        // inner class SortedLinkedList
        boolean result = _bucket[i].removes(cqEntry);

        // if the operation succeeded then reduces the number of
        // element in the queue.
        if (result) {
            _qSize--;
        }
        return result;
    }

    /** Query whether a specific entry is in the queue,
     *  return true is succeed and false otherwise.
     *
     * @param obj the object that you want to test
     * @param key the sort-key corresponding to that object
     *
     * @return boolean
     */
    public boolean includesEntry(Object obj, Object key) {
        // if the queue is empty then return false
        if (_qSize == 0) return false;

        // create a CQEntry object to wrap obj and key
        CQEntry cqEntry = new CQEntry(obj, key);

        // calculate i, the index into the queue array
        int i = _cqComparator.getBinIndex(key, _zeroRef, _width);
        i = i % _nBuckets;
        if (i < 0)
            i += _nBuckets;

        // call the includes method in private
        // class SortedLinkedList.
        return _bucket[i].includes(cqEntry);
    }

    /** Return the queue size.
     * @return int representing queue size
     */
    public int size() {
        return _qSize;
    }

     /** Return SETMODE if the queue is in set mode,
      *  return BAGMODE if the queue is in bag mode (default).
      *  In SETMODE, the number of occurences of any entries is at most one,
      *  while in Bag Mode, it may contain multiple occurences of any entries.
     * @return boolean true if Set, false if Bag
     */
    public boolean getMode() {
        return _mode;
    }
    /** set the queue to operate either in Set Mode in Bag Mode.
     *
     * @param newmode the desired mode of operation (SETMODE or BAGMODE)
     */
    public void setMode(boolean newmode) {
        _mode = newmode;
    }

    /////////////////////////////////////////////////////////////////////////
    ////                         public variables                        ////

    /** Indicate that on subsequent invocations to the put() method, an entry
     *  will be added even if it already exist in the queue.
     */
    public static final boolean BAGMODE = false;

    /** Indicate that on subsequent invocations to the put() method, an entry
     *  will not be added if it already exist in the queue.
     */
    public static final boolean SETMODE = true;

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////

    // do local initialization on bucket[] array starting from index qbase,
    // as many as nbuckets. This method is called inside resize() method and
    // during the first invocation of put.
    //
    // nbuckets: number of total buckets
    // bwidth: bucket width
    // startprio: starting date of the new calendar
    private void _localInit(
            int nbuckets,
            SortBinWidth bwidth,
            Object startkey) {

        _width = bwidth;
        _nBuckets = nbuckets;
        _bucket = new SortedLinkedList[_nBuckets];
        // Initialize as empty
        _qSize = 0;

        for (int i = 0; i < _nBuckets; ++i) {
            // intialize each bucket with an empty SortedLinkedList
            // that uses _cqComparator for sorting.
            _bucket[i] = new SortedLinkedList(_cqComparator);
        }


        // Set up initial position in queue
        _minKey = startkey;
        _minVirtualBucket = _cqComparator.getBinIndex(startkey, _zeroRef, _width);
        _minBucket = _minVirtualBucket % _nBuckets;
        if (_minBucket < 0) _minBucket += _nBuckets;

        // Set up queue size change threshold
        // Theoretically we can use botThreshold and topThreshold equal to
        // half and twice nBuckets, respectively. But in practice, botThreshold
        // is calculated as nBuckets/2 - 2. The minus two is here, so that
        // nBuckets will be biased to larger value.
        _botThreshold = _nBuckets/2 - 2;
        _topThreshold = 2*_nBuckets;
    }

    // Copy the queue onto a calendar with newsize number of buckets.
    // Unlike described in the paper, the new calendar is reallocated
    // each time the number of buckets (_nBuckets) changes.
    // The resize methods follow these steps:
    // 1. a new width is computed as a function of the element statistics.
    // 2. Save the old queue to a temp variable.
    // 3. Create the new queue and initialize it
    // 4. Do transfer of all elements in the old queue into the new queue
    private void _resize(int newsize) {
        SortBinWidth new_width;
        int i;
        int old_nbuckets;
        boolean oldmode;
        SortedLinkedList[] old_bucket;

        if (!_resizeEnabled) return;

        // Find new bucket width
        new_width = _computeNewWidth();

        // Save location and size of old calendar
        // for use when copying calendar
        old_bucket = _bucket;
        old_nbuckets = _nBuckets;
        oldmode = _mode;
        _mode = false;

        // Initialize new calendar
        _localInit(newsize, new_width, _minKey);

        // Copy queue elements into new calendar
        for (i = old_nbuckets - 1; i >= 0; --i) {
            // Transfer elements from bucket i to new calendar by putting them
            CollectionEnumeration enum = old_bucket[i].elements();
            while (enum.hasMoreElements()) {
                CQEntry cqEntry = (CQEntry)(enum.nextElement());
                put(cqEntry.object(), cqEntry.key());
            }
        }
        _mode = oldmode;
    }

    // This function calculates the width to use for buckets
    // It does so by these steps:
    // a. Figure out how many samples to take (as a function of qSize)
    // b. Disable resize and take nSamples elements from the queue
    // c. Record the statistics of those elements
    // d. Put the element back in and re-enable resize
    // e. Pass the array containing the data into the priority class
    //    to let it calculate the width object.

    private SortBinWidth _computeNewWidth() {
        int nSamples;
        Object[] sampledData;
        Object[] sampledKey;
        /* Decide how many queue elements to sample */
        if (_qSize < 2) return _cqComparator.getBinWidth(null);
        if (_qSize <= 5) {
            nSamples = _qSize;
        }
        else {
            nSamples = 5 + _qSize/10;
        }
        if (nSamples > 25) {
            nSamples = 25;
        }
        // Record lastprio, lastbucket, buckettop
        Object savedLastPrio = _minKey;
        int savedLastBucket = _minBucket;
        int savedLastVirtualBucket = _minVirtualBucket;

        // Take nsample events from the queue and record their priorities
        // with resizeEnabled equal to false
        // Data sample is done by dequeuing nsample elements, record
        // their values, and then enqueuing them back in again.
        // With resizeEnabled equal to false, the overhead of sampling
        // process is reduced. Besides, more importantly, to do resize
        // we need to call this method (computeNewWidth) anyway; so it
        // also prevents infinite loop.
        //
        _resizeEnabled = false;
        sampledData = new Object[nSamples];
        sampledKey = new Object[nSamples];
        for (int i = 0; i < nSamples; ++i) {
            sampledData[i] = take();
            sampledKey[i] = getPreviousKey();
        }
        // Restore the sampled events to the queue using put
        boolean oldmode = _mode;
        _mode = false;
        for (int i = nSamples-1; i >= 0; --i) {
            put(sampledData[i], sampledKey[i]);
        }
        _mode = oldmode;

        // Done sampling data and putting them back in
        // therefore, we can reenable resize
        _resizeEnabled = true;
        // Restore lastprio, lastbucket, and buckettop
        _minKey = savedLastPrio;
        _minBucket = savedLastBucket;
        _minVirtualBucket = savedLastVirtualBucket;

        /* Calculate average separation of sampled events */
        return _cqComparator.getBinWidth(sampledKey);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // _nBuckets: number of buckets in the current calendar
    private int _nBuckets;
    // _qSize: current queue size
    private int _qSize = 0;
    // _width: Object representing width of each bucket
    private SortBinWidth _width;
    // _topThreshold: largest queue size before number of buckets get doubled
    private int _topThreshold;
    // _botThreshold: smallest queue size before number of buckets get halfed
    private int _botThreshold;
    // _zeroRef: the point zero needed to quantize a Object object
    private Object _zeroRef = null;
    // _bucket: an array of nBuckets buckets
    private SortedLinkedList[] _bucket;

    // _resizeEnabled: enable/disable resize() invocation
    private boolean _resizeEnabled = true;
    // _singleOccurence: only affect put() method. If the entry being put
    // into the queue is equal to one of the entry already in the queue then
    // it's not added into the queue.By 'equal' we mean that both
    // obj1.equal(obj2) and key1.equal(key2) return true
    private boolean _mode = BAGMODE;

    // FIXME: I want a static member but compiler complains like this:
    //SortedQueue.java:674: No enclosing instance of class 
    //pt.actor.util.SortedQueue is in scope; an explicit one must be 
    //provided when creating inner class pt.actor.util.SortedQueue. 
    //CQComparator, as in "outer. new Inner()" or "outer. super()".

    // _cqComparator: static member to initialize the SortedLinkedList
    private CQComparator _cqComparator;

    // _minKey: all elements in the queue is of lower or equal
    //    priority from this _minKey, hence the name.
    private Object _minKey;
    // _minVirtualBucket: at all times equal to the quantized value of
    //    _minKey.
    private int _minVirtualBucket;
    // _minBucket: at all times equal to the positive modulo of
    //    _minKey with _nBuckets.
    private int _minBucket;

    // CQEntry: encapsulate both the objects and it's priority
    // to be inserted into the queue.
    private class CQEntry {
        // Construct a CQEntry with the supplied content (obj)
        // and priority (priority)
        public CQEntry(Object object, Object key) {
            _object = object;
            _key = key;
        }

        // get the object content and the priority from a CQEntry, respectively
        public Object object() {
            return _object;
        }
        public Object key() {
            return _key;
        }

        // override Object.equal() method
        // This is needed, because 2 CQEntry object being equal
        // doesn't mean cqEntry_a == cqEntry_b, but instead
        // that both their members (object and priority) are equals.
        public boolean equals(Object obj) {
            if (!(obj instanceof CQEntry)) {
                return false;
            } else {
                CQEntry snd = (CQEntry) obj;
                boolean sameObject = false;
                boolean sameKey = false;

                if (_object == null && snd._object == null) {
                    sameObject = true;
                } else if (_object == null && snd._object != null) {
                    sameObject = false;
                } else if (_object.equals(snd._object)) {
                    sameObject = true;
                }

                if (_key == null || snd._key == null) {
                    throw new IllegalStateException(
                            "Bug in SortedQueue.CQEntry.equals"
                            );
                } else if (_key.equals(snd._key)){
                    sameKey = true;
                }

                if (sameObject && sameKey) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        // Private variables
        private Object _object;
        private Object _key;

    }

    // SortedLinkedList
    // This is derived from LinkedList. Linked List is chosen because it
    // provides higher flexibility in manipulating individual links
    // (i.e. elements) as opposed to RBTree. But, the drawback is LinkedList
    // is only a Object structure (as opposed to RBTree being Sorted
    // structure). This means we need to explicitly invoke sort() method to
    // tell the LinkedList to sort itself.
    //
    // This derived class implement a function ( insertAndSort) that does
    // both the insertion of entry into the linked list and then call sort
    private class SortedLinkedList extends LinkedList {

        // the argument c is the comparator object that'll be used for
        // doing the ordering comparison
        public SortedLinkedList(Comparator c) {
            super();
            _comp = c;
        }

        // insert and then sort
        public void insertAndSort(Object obj) {
            // use insertLast to gives the queue FIFO behaviour
            // when we have two objects of the same priority
            insertLast(obj);
            sort(_comp);
        }

        // basically a macro, to shorten typing
        public Object peekKey() {
            if (!isEmpty())
                return ((CQEntry)(first())).key();
            else
                return null;
        }

        // removes a specific element from the queeu.
        // returns true if succeed, false otherwise
        public boolean removes(CQEntry cqEntry) {
            CollectionEnumeration e = elements();
            int i = 0;
            while (e.hasMoreElements()) {
                if (cqEntry.equals(e.nextElement())) {
                    removeAt(i);
                    return true;
                }
                i++;
            }
            return false;
        }
        // _comp: a implementation of Comparator interface used for
        // sorting the Queue
        private Comparator _comp;
    }
}
