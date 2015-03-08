/*
 pblPriorityQueue.c - C implementation of a binary max-heap based priority queue.

 Copyright (C) 2010   Peter Graf

 This file is part of PBL - The Program Base Library.
 PBL is free software.

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 For more information on the Program Base Library or Peter Graf,
 please see: http://www.mission-base.com/.

 $Log: pblPriorityQueue.c,v $
 Revision 1.12  2010/10/01 20:44:31  peter
 Port to 64 bit windows

 Revision 1.11  2010/08/29 15:29:31  peter
 Added the heap functions.

 Revision 1.10  2010/08/20 20:10:25  peter
 Implemented the priority queue functions.


 */

/*
 * Make sure "strings <exe> | grep Id | sort -u" shows the source file versions
 */
char* pblPriorityQueue_c_id =
        "$Id$";

#include <stdio.h>
#ifndef PT_DOES_NOT_HAVE_MEMORY_H
#ifndef __MBED__
#include <memory.h>
#endif
#endif

#ifndef __APPLE__
#ifndef PT_DOES_NOT_HAVE_MALLOC_H
#ifndef __MBED__
#include <malloc.h>
#endif
#endif
#endif

#include <stdlib.h>

#include "pbl.h"

/*****************************************************************************/
/* #defines                                                                  */
/*****************************************************************************/

/*****************************************************************************/
/* Function declarations                                                     */
/*****************************************************************************/

/*****************************************************************************/
/* Functions                                                                 */
/*****************************************************************************/

/*
 * Compares two priority queue entries.
 *
 * Used as compare function for priority queues.
 *
 * @return int rc  < 0: left is smaller than right
 * @return int rc == 0: left and right are equal
 * @return int rc  > 0: left is greater than right
 */
static int PblPriorityQueueEntryCompareFunction( /*      */
const void * left, /* The left value for the comparison  */
const void * right /* The right value for the comparison */
)
{
    PblPriorityQueueEntry * leftEntry = *(PblPriorityQueueEntry**)left;
    PblPriorityQueueEntry * rightEntry = *(PblPriorityQueueEntry**)right;

    if( !leftEntry )
    {
        if( rightEntry )
        {
            return -1;
        }
        return 0;
    }
    if( !rightEntry )
    {
        return 1;
    }

    if( leftEntry->priority < rightEntry->priority )
    {
        return -1;
    }
    if( leftEntry->priority == rightEntry->priority )
    {
        return 0;
    }
    return 1;
}

/**
 * Creates a new priority queue.
 *
 * The priority queue implementation is a
 * <a href="http://en.wikipedia.org/wiki/Binary_heap">binary max-heap</a> using
 * an array list as underlying data structure.
 * The entries kept in the array list are of type \Ref{PblPriorityQueueEntry}.
 * Each entry holding the 'void *' element payload and the 'int' priority
 * associated with the element.
 *
 * This function has a time complexity of O(1).
 *
 * @return PblPriorityQueue * retPtr != NULL: A pointer to the new priority queue.
 * @return PblPriorityQueue * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
PblPriorityQueue * pblPriorityQueueNew( void )
{
    PblHeap * pblHeap = pblHeapNew();
    if( !pblHeap )
    {
        return NULL;
    }

    pblHeapSetCompareFunction( pblHeap, PblPriorityQueueEntryCompareFunction );

    return (PblPriorityQueue *)pblHeap;
}

/**
 * Removes all of the elements from the priority queue.
 *
 * <B>Note:</B> No memory of the elements themselves is freed.
 *
 * This function has a time complexity of O(N),
 * with N being the number of elements in the priority queue.
 *
 * @return void
 */
void pblPriorityQueueClear( /*                  */
PblPriorityQueue * queue /** The queue to clear */
)
{
    int index;

    // Loop over the heap and free the entries allocated
    // by pblPriorityQueueAddLast()
    //
    for( index = pblHeapSize( (PblHeap *)queue ) - 1; index >= 0; index-- )
    {
        void * ptr = pblHeapGet( (PblHeap *)queue, index );
        if( ptr != (void*)-1 )
        {
            PBL_FREE(ptr);
        }
    }
    pblHeapClear( (PblHeap *)queue );
}

/**
 * Frees the priority queue's memory from heap.
 *
 * <B>Note:</B> The memory of the elements themselves is not freed.
 *
 * This function has a time complexity of O(N),
 * with N being the number of elements in the priority queue.
 *
 * @return void
 */
void pblPriorityQueueFree( /*                  */
PblPriorityQueue * queue /** The queue to free */
)
{
    pblPriorityQueueClear( queue );
    pblHeapFree( (PblHeap *)queue );
}

/**
 * Increases the capacity of the priority queue, if necessary.
 *
 * This function ensures that the priority queue can hold
 * at least the number of elements specified by the minimum
 * capacity argument.
 *
 * If the capacity is actually increased,
 * this function has a memory and time complexity of O(N),
 * with N being the new capacity of the queue.
 *
 * @return int rc >= 0: OK, the queue capacity is returned.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblPriorityQueueEnsureCapacity( /*           */
PblPriorityQueue * queue, /** The queue to use   */
int minCapacity /** The desired minimum capacity */
)
{
    return pblHeapEnsureCapacity( (PblHeap *)queue, minCapacity );
}

/**
 * Returns the capacity of the priority queue.
 *
 * This function has a time complexity of O(1).
 *
 * @return int rc: The capacity of the queue.
 */
int pblPriorityQueueGetCapacity( /*           */
PblPriorityQueue * queue /** The queue to use */
)
{
    return pblHeapGetCapacity( (PblHeap *)queue );
}

/**
 * Returns the number of elements in the priority queue.
 *
 * This function has a time complexity of O(1).
 *
 * @return int rc: The number of elements in the priority queue.
 */
int pblPriorityQueueSize( /**/
PblPriorityQueue * queue /** The queue to use */
)
{
    return pblHeapSize( (PblHeap *)queue );
}

/**
 * Tests if the priority queue has no elements.
 *
 * This function has a time complexity of O(1).
 *
 * @return int rc != 0: The queue has no elements.
 * @return int rc == 0: The queue has elements.
 */
int pblPriorityQueueIsEmpty( /*               */
PblPriorityQueue * queue /** The queue to use */
)
{
    return pblHeapIsEmpty( (PblHeap *)queue );
}

/**
 * Trims the capacity of the queue to the priority queue's current size.
 *
 * If the capacity is actually decreased,
 * this function has a time complexity of O(N),
 * with N being the number of elements in the priority queue.
 *
 * @return int rc >= 0: The capacity of the queue.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblPriorityQueueTrimToSize( /*            */
PblPriorityQueue * queue /** The queue to use */
)
{
    return pblHeapTrimToSize( (PblHeap *)queue );
}

/**
 * Adds the element with the specified priority to the
 * end of the priority queue without ensuring the
 * heap condition.
 *
 * This function has a time complexity of O(1).
 *
 * This function together with \Ref{pblPriorityQueueConstruct}()
 * can be used to build a priority queue with N elements
 * in time proportional to N.
 *
 * First create an empty queue with \Ref{pblPriorityQueueNew}()
 * and ensure the queue has space for N elements via a
 * call to \Ref{pblPriorityQueueEnsureCapacity}(),
 * then add all elements via calls to this function
 * and finally ensure the heap condition with a call
 * to \Ref{pblPriorityQueueConstruct}().
 *
 * @return int rc >= 0: The size of the queue.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblPriorityQueueAddLast( /*                       */
PblPriorityQueue * queue, /** The queue to use        */
int priority, /** Priority of the element to be added */
void * element /** Element to be added to the queue   */
)
{
    int rc;
    PblPriorityQueueEntry *newEntry =
            (PblPriorityQueueEntry *)pbl_malloc( "pblPriorityQueueAddLast",
                                                 sizeof(PblPriorityQueueEntry) );
    if( !newEntry )
    {
        return -1;
    }

    newEntry->element = element;
    newEntry->priority = priority;

    rc = pblHeapAddLast( (PblHeap *)queue, newEntry );
    if( rc < 0 )
    {
        PBL_FREE(newEntry);
        return rc;
    }

    return pblHeapSize( (PblHeap *)queue );
}

/**
 * Inserts the element with the specified priority into the
 * priority queue and maintains the heap condition
 * of the priority queue.
 *
 * This function has a time complexity of O(Log N),
 * with N being the number of elements in the queue.
 *
 * @return int rc >= 0: The size of the queue.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblPriorityQueueInsert( /*                           */
PblPriorityQueue * queue, /** The queue to use           */
int priority, /** Priority of the element to be inserted */
void * element /** Element to be inserted to the queue   */
)
{
    // Add to the end of the queue
    //
    int rc = pblPriorityQueueAddLast( queue, priority, element );
    if( rc > 1 )
    {
        // Ensure the heap condition for the last entry
        //
        pblHeapEnsureCondition( queue, rc - 1 );
    }
    return rc;
}

/**
 * Removes the last element from the priority queue,
 * maintaining the heap condition of the queue.
 *
 * <B>Note:</B> The last element is not guaranteed to be
 * the element with the lowest priority!
 *
 * This function has a time complexity of O(1).
 *
 * @return void* retptr != (void*)-1: The element removed.
 * @return void* retptr == (void*)-1: An error see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - The queue is empty.
 */
void * pblPriorityQueueRemoveLast( /*                                     */
PblPriorityQueue * queue, /** The queue to use                            */
int * priority /** On return contains the priority of the element removed */
)
{
    void * retptr;
    PblPriorityQueueEntry *lastEntry =
            (PblPriorityQueueEntry *)pblHeapRemoveLast( (PblHeap *)queue );

    if( lastEntry == (PblPriorityQueueEntry *)-1 )
    {
        return (void*)-1;
    }

    retptr = lastEntry->element;
    if( priority )
    {
        *priority = lastEntry->priority;
    }

    PBL_FREE(lastEntry);

    // Removing the last entry cannot break the heap condition!
    //
    return retptr;
}

/**
 * Removes the element at the specified position from the
 * priority queue, maintaining the heap condition of the queue.
 *
 * This function has a time complexity of O(Log N),
 * with N being the number of elements in the queue.
 *
 * @return void* retptr != (void*)-1: The element removed.
 * @return void* retptr == (void*)-1: An error see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
void * pblPriorityQueueRemoveAt( /*                                       */
PblPriorityQueue * queue, /** The queue to use                            */
int index, /** The index at which the element is to be removed            */
int * priority /** On return contains the priority of the element removed */
)
{
    void * retptr;
    PblPriorityQueueEntry *entry = pblHeapRemoveAt( (PblHeap *)queue, index );
    if( entry == (PblPriorityQueueEntry *)-1 )
    {
        return (void*)-1;
    }

    retptr = entry->element;
    if( priority )
    {
        *priority = entry->priority;
    }

    PBL_FREE(entry);

    return retptr;
}

/**
 * Removes the element with the highest priority from the
 * priority queue, maintaining the heap condition of the queue.
 *
 * This function has a time complexity of O(Log N),
 * with N being the number of elements in the queue.
 *
 * @return void* retptr != (void*)-1: The element removed.
 * @return void* retptr == (void*)-1: An error see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - The queue is empty.
 */
void * pblPriorityQueueRemoveFirst( /*                                    */
PblPriorityQueue * queue, /** The queue to use                            */
int * priority /** On return contains the priority of the element removed */
)
{
    return pblPriorityQueueRemoveAt( queue, 0, priority );
}

/**
 * Returns the element at the specified position in the priority queue.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The element at the specified position, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
void * pblPriorityQueueGet( /*                                    */
PblPriorityQueue * queue, /** The queue to use                    */
int index, /** Index of the element to return                     */
int * priority /** On return contains the priority of the element */
)
{
    PblPriorityQueueEntry *entry =
            (PblPriorityQueueEntry *)pblHeapGet( (PblHeap *)queue, index );

    if( entry == (PblPriorityQueueEntry *)-1 )
    {
        return (void*)-1;
    }

    if( priority )
    {
        *priority = entry->priority;
    }

    return entry->element;
}

/**
 * Returns but does not remove the element with the highest priority in the
 * priority queue.
 *
 * This function has a time complexity of O(1).
 *
 * @return void* retptr != (void*)-1: The element returned.
 * @return void* retptr == (void*)-1: An error see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - The queue is empty.
 */
void * pblPriorityQueueGetFirst( /*                                     */
PblPriorityQueue * queue, /** The queue to use                          */
int * priority /** On return contains the priority of the first element */
)
{
    return pblPriorityQueueGet( queue, 0, priority );
}

/**
 * Constructs a priority queue using 'bottom-up heap construction'.
 *
 * This function has a time complexity of O(N),
 * with N being the number of elements in the queue.
 *
 * This function together with \Ref{pblPriorityQueueAddLast}()
 * can be used to build a priority queue with N elements
 * in time proportional to N.
 *
 * First create an empty queue with \Ref{pblPriorityQueueNew}()
 * and ensure the queue has space for N elements via a
 * call to \Ref{pblPriorityQueueEnsureCapacity}(),
 * then add all elements via calls to \Ref{pblPriorityQueueAddLast}(),
 * and finally ensure the heap condition with a call
 * to this function.
 */
void pblPriorityQueueConstruct( /*            */
PblPriorityQueue * queue /** The queue to use */
)
{
    pblHeapConstruct( (PblHeap *)queue );
}

/**
 * Changes the priority of the element at the specified position of the
 * priority queue, maintaining the heap condition of the queue.
 *
 * This function has a time complexity of O(Log N),
 * with N being the number of elements in the queue.
 *
 * @return int rc >= 0: The index of the element after the priority change.
 * @return int rc <  0: An error see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
int pblPriorityQueueChangePriorityAt( /*                        */
PblPriorityQueue * queue, /** The queue to use                  */
int index, /** The index at which the priority is to be changed */
int priority /** The new priority of the element                */
)
{
    PblPriorityQueueEntry *entry =
            (PblPriorityQueueEntry *)pblHeapGet( (PblHeap *)queue, index );
    if( entry == (PblPriorityQueueEntry *)-1 )
    {
        return -1;
    }

    if( priority < entry->priority )
    {
        int size = pblHeapSize( (PblHeap *)queue );

        entry->priority = priority;

        // For zero based arrays all entries with an index bigger
        // than 'size / 2 - 1' do not have any children.
        //
        // Decreasing the priority cannot violate the heap condition
        // for entries with no children
        //
        if( index <= size / 2 - 1 )
        {
            // The heap condition only needs to be ensured
            // if the entry has children
            //
            return pblHeapEnsureCondition( queue, index );
        }
    }
    else if( priority > entry->priority )
    {
        entry->priority = priority;

        // Increasing the priority cannot violate the heap condition
        // for the top entry at index 0
        //
        if( index > 0 )
        {
            // The heap condition needs to ensured
            //
            return pblHeapEnsureCondition( queue, index );
        }
    }

    return index;
}

/**
 * Changes the priority of the first element of the priority queue,
 * maintaining the heap condition of the queue.
 *
 * This function has a time complexity of O(Log N),
 * with N being the number of elements in the queue.
 *
 * @return int rc >= 0: The index of the first element after the priority change.
 * @return int rc <  0: An error see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - The queue is empty.
 */
int pblPriorityQueueChangePriorityFirst( /*            */
PblPriorityQueue * queue, /** The queue to use         */
int priority /** The new priority of the first element */
)
{
    return pblPriorityQueueChangePriorityAt( queue, 0, priority );
}

/**
 * Returns an iterator over the elements in the queue.
 *
 * The iterator starts the iteration at the element with the highest priority.
 *
 * <B>Note</B>: The memory allocated by this method for the iterator returned needs to be released
 *              by calling \Ref{pblIteratorFree}() once the iterator is no longer needed.
 *
 * The pointers returned by the \Ref{pblIteratorNext}() or \Ref{pblIteratorPrevious}() functions
 * of the iterator are of type \Ref{PblPriorityQueueEntry} allowing to access priority and
 * element.
 *
 * Modifying the priority queue via the Iterator's own remove or add methods
 * does not maintain the heap property of the priority queue.
 * In this case the heap property has to be restored by a call
 * to \Ref{pblPriorityQueueConstruct}().
 *
 * The iterators returned by the this method are fail-fast:
 * if the queue is structurally modified at any time after the iterator is created,
 * in any way except through the Iterator's own remove or add methods,
 * the iterator will return a PBL_ERROR_CONCURRENT_MODIFICATION error.
 *
 * Thus, in the face of concurrent modification,
 * the iterator fails quickly and cleanly,
 * rather than risking arbitrary, non-deterministic
 * behavior at an undetermined time in the future.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != NULL: The iterator.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
PblIterator * pblPriorityQueueIterator( /*    */
PblPriorityQueue * queue /** The queue to use */
)
{
    return pblHeapIterator( (PblHeap *)queue );
}

/**
 * Joins the two priority queues by moving all elements of the 'other'
 * queue. When this function returns, 'other' will be empty.
 *
 * This function has a time complexity of O(N), with N
 * being the number of elements in the queue after the join.
 *
 * @return int rc >= 0: The size of the queue after tjhe join.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblPriorityQueueJoin( /*                         */
PblPriorityQueue * queue, /** The queue to join to   */
PblPriorityQueue * other /** The other queue to join */
)
{
    return pblHeapJoin( (PblHeap *)queue, (PblHeap *)other );
}
