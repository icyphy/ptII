/*
 pblHeap.c - C implementation of a binary heap.

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

 $Log: pblHeap.c,v $
 Revision 1.5  2010/08/31 21:06:20  peter
 Added the heap functions.


 */

/*
 * Make sure "strings <exe> | grep Id | sort -u" shows the source file versions
 */
char* pblHeap_c_id = "$Id$";

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

/**
 * Creates a new heap.
 *
 * The pbl heap implementation is a
 * <a href="http://en.wikipedia.org/wiki/Binary_heap">binary heap</a>
 * using an
 * <a href="http://www.mission-base.com/peter/source/pbl/doc/list.html">array list</a>
 * as underlying data structure.
 *
 * This function has a time complexity of O(1).
 *
 * @return PblHeap * retPtr != NULL: A pointer to the new heap.
 * @return PblHeap * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
PblHeap * pblHeapNew( void )
{
    return (PblHeap *)pblListNewArrayList();
}

/**
 * Sets an application specific compare function for the elements
 * of the heap.
 *
 * An application specific compare function can be set to the heap.
 * If no specific compare function is specified by the user,
 * the default compare function is used.
 * The default compare function compares the two pointers directly,
 * i.e. it tests for object identity.
 *
 * The compare function specified should behave like the one that
 * can be specified for the C-library function 'qsort'.
 *
 * The arguments actually passed to the compare function when it is called
 * are addresses of the element pointers added to the heap.
 * E.g.: If you add char * pointers to the heap, the compare function
 * will be called with char ** pointers as arguments. See the documentation
 * for the C-library function 'qsort' for further information.
 *
 * This method has a time complexity of O(1).
 *
 * @return * retptr: The compare function used before, may be NULL.
 */
void * pblHeapSetCompareFunction( /*                     */
PblHeap * heap, /** The heap to set compare function for */
int(*compare) /** The compare function to set            */
( /*                                                     */
const void* prev, /** The "left" element for compare     */
const void* next /** The "right" element for compare     */
) /*                                                     */
)
{
    return pblListSetCompareFunction( (PblList *)heap, compare );
}

/**
 * Removes all of the elements from the heap.
 *
 * <B>Note:</B> No memory of the elements themselves is freed.
 *
 * This function has a time complexity of O(N),
 * with N being the number of elements in the heap.
 *
 * @return void
 */
void pblHeapClear( /*                */
PblHeap * heap /** The heap to clear */
)
{
    pblListClear( (PblList *)heap );
}

/**
 * Frees the heap's memory from heap.
 *
 * <B>Note:</B> The memory of the elements themselves is not freed.
 *
 * This function has a time complexity of O(N),
 * with N being the number of elements in the heap.
 *
 * @return void
 */
void pblHeapFree( /*                */
PblHeap * heap /** The heap to free */
)
{
    pblListClear( (PblList *)heap );
    pblListFree( (PblList *)heap );
}

/**
 * Increases the capacity of the heap, if necessary.
 *
 * This function ensures that the heap can hold
 * at least the number of elements specified by the minimum
 * capacity argument.
 *
 * If the capacity is actually increased,
 * this function has a memory and time complexity of O(N),
 * with N being the new capacity of the heap.
 *
 * @return int rc >= 0: OK, the heap capacity is returned.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblHeapEnsureCapacity( /*                    */
PblHeap * heap, /** The heap to use              */
int minCapacity /** The desired minimum capacity */
)
{
    return pblListEnsureCapacity( (PblList *)heap, minCapacity );
}

/**
 * Returns the capacity of the heap.
 *
 * This function has a time complexity of O(1).
 *
 * @return int rc: The capacity of the heap.
 */
int pblHeapGetCapacity( /*         */
PblHeap * heap /** The heap to use */
)
{
    return pblListGetCapacity( (PblList *)heap );
}

/**
 * Returns the number of elements in the heap.
 *
 * This function has a time complexity of O(1).
 *
 * @return int rc: The number of elements in the heap.
 */
int pblHeapSize( /*                */
PblHeap * heap /** The heap to use */
)
{
    return pblListSize( (PblList *)heap );
}

/**
 * Tests if the heap has no elements.
 *
 * This function has a time complexity of O(1).
 *
 * @return int rc != 0: The heap has no elements.
 * @return int rc == 0: The heap has elements.
 */
int pblHeapIsEmpty( /*             */
PblHeap * heap /** The heap to use */
)
{
    return pblListIsEmpty( (PblList *)heap );
}

/**
 * Trims the capacity of the heap to the heap's current size.
 *
 * If the capacity is actually decreased,
 * this function has a time complexity of O(N),
 * with N being the number of elements in the heap.
 *
 * @return int rc >= 0: The capacity of the heap.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblHeapTrimToSize( /*          */
PblHeap * heap /** The heap to use */
)
{
    return pblListTrimToSize( (PblList *)heap );
}

/*
 * Ensures the heap condition of the heap downward in the "tree".
 *
 * This function has a time complexity of O(Log N).
 *
 * @return int rc: The new index of the element.
 */
static int pblEnsureHeapConditionDownward( PblHeap * heap, int size, int index )
{
    // For zero based arrays all elements with an index bigger
    // than 'size / 2 - 1' do not have any children
    //
    int lastParentIndex = size / 2 - 1;

    if( index <= lastParentIndex )
    {
        void *entry = pblListGet( (PblList *)heap, index );

        // As long as the entry has at least one child
        //
        while( index <= lastParentIndex )
        {
            // For zero based arrays the left child is at '2 * index + 1'
            //
            int childIndex = 2 * index + 1;

            void *child = pblListGet( (PblList *)heap, childIndex );

            // If the entry also has a right child, the bigger child
            // needs to be considered for the swap
            //
            if( childIndex < size - 1 )
            {
                // The right child is next to the child in the array
                //
                int rightChildIndex = childIndex + 1;
                void *rightChild =
                        pblListGet( (PblList *)heap, rightChildIndex );

                if( pblCollectionElementCompare( (PblCollection*)heap,
                                                 rightChild, child ) > 0 )
                {
                    // Use the right child for the swap
                    //
                    child = rightChild;
                    childIndex = rightChildIndex;
                }
            }

            if( pblCollectionElementCompare( (PblCollection*)heap, entry, child )
                    >= 0 )
            {
                // The heap condition is fulfilled for the entry
                //
                return index;
            }

            // Do the swap with the child
            //
            pblListSet( (PblList *)heap, childIndex, entry );
            pblListSet( (PblList *)heap, index, child );

            index = childIndex;
        }
    }

    // The entry does not have any children, therefore
    // the heap condition is fulfilled for the entry
    //
    return index;
}

/*
 * Ensures the heap condition of the heap upward in the "tree".
 *
 * This function has a time complexity of O(Log N).
 *
 * @return int rc: The new index of the element.
 */
static int pblEnsureHeapConditionUpward( PblHeap * heap, int index )
{
    if( index > 0 )
    {
        void *entry = pblListGet( (PblList *)heap, index );

        // As long as the entry is not the top of the heap
        //
        while( index > 0 )
        {
            // For zero based arrays the parent index is '( index - 1 ) / 2'
            //
            int parentIndex = ( index - 1 ) / 2;
            void *parent = pblListGet( (PblList *)heap, parentIndex );

            if( pblCollectionElementCompare( (PblCollection*)heap, entry,
                                             parent ) <= 0 )
            {
                // The heap condition is fulfilled for the entry
                //
                return index;
            }

            // Do the swap with the parent
            //
            pblListSet( (PblList *)heap, parentIndex, entry );
            pblListSet( (PblList *)heap, index, parent );

            index = parentIndex;
        }
    }

    // The entry is the top of the heap
    //
    return index;
}

/**
 * Ensures the heap condition of the heap for the element with the given index.
 *
 * This function has a time complexity of O(Log N).
 *
 * @return int rc: The new index of the element.
 */
int pblHeapEnsureCondition( /*                        */
PblHeap * heap, /** The heap to use                   */
int index /** Index of element to ensure condtion for */
)
{
    int rc = pblEnsureHeapConditionUpward( heap, index );
    if( rc == index )
    {
        rc = pblEnsureHeapConditionDownward( heap, pblHeapSize( heap ), index );
    }
    return rc;
}

/**
 * Ensures the heap condition for the first element.
 *
 * This function has a time complexity of O(Log N).
 *
 * @return int rc: The new index of the element.
 */
int pblHeapEnsureConditionFirst( /**/
PblHeap * heap /** The heap to use */
)
{
    return pblHeapEnsureCondition( heap, 0 );
}

/**
 * Adds the element to the end of the heap without ensuring the
 * heap condition.
 *
 * This function has a time complexity of O(1).
 *
 * This function together with \Ref{pblHeapConstruct}()
 * can be used to build a heap with N elements
 * in time proportional to N.
 *
 * First create an empty heap with \Ref{pblHeapNew}()
 * and ensure the heap has space for N elements via a
 * call to \Ref{pblHeapEnsureCapacity}(),
 * then add all elements via calls to this function
 * and finally ensure the heap condition with a call
 * to \Ref{pblHeapConstruct}().
 *
 * @return int rc >= 0: The size of the heap.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblHeapAddLast( /*                             */
PblHeap * heap, /** The heap to use                */
void * element /** Element to be added to the heap */
)
{
    return pblListAdd( (PblList *)heap, element );
}

/**
 * Inserts the element into the heap and maintains the heap condition
 * of the heap.
 *
 * This function has a time complexity of O(Log N),
 * with N being the number of elements in the heap.
 *
 * @return int rc >= 0: The size of the heap.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblHeapInsert( /*                                 */
PblHeap * heap, /** The heap to use                   */
void * element /** Element to be inserted to the heap */
)
{
    // Add to the end of the heap
    //
    int rc = pblListAdd( (PblList *)heap, element );
    if( rc > 1 )
    {
        // Ensure the heap condition for the last entry
        //
        pblEnsureHeapConditionUpward( heap, rc - 1 );
    }
    return rc;
}

/**
 * Removes the last element from the heap,
 * maintaining the heap condition of the heap.
 *
 * <B>Note:</B> The last element is not guaranteed to be
 * the smallest element!
 *
 * This function has a time complexity of O(1).
 *
 * @return void* retptr != (void*)-1: The element removed.
 * @return void* retptr == (void*)-1: An error see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - The heap is empty.
 */
void * pblHeapRemoveLast( /*       */
PblHeap * heap /** The heap to use */
)
{
    return pblListRemoveLast( (PblList *)heap );
}

/**
 * Removes the element at the specified position from the
 * heap, maintaining the heap condition of the heap.
 *
 * This function has a time complexity of O(Log N),
 * with N being the number of elements in the heap.
 *
 * @return void* retptr != (void*)-1: The element removed.
 * @return void* retptr == (void*)-1: An error see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
void * pblHeapRemoveAt( /*                                    */
PblHeap * heap, /** The heap to use                           */
int index /** The index at which the element is to be removed */
)
{
    void *retptr;
    void *lastEntry;
    void *entry;
    int size = pblListSize( (PblList *)heap );

    if( index == size - 1 )
    {
        return pblListRemoveLast( (PblList *)heap );
    }

    entry = pblListGet( (PblList *)heap, index );
    if( entry == (void *)-1 )
    {
        return (void*)-1;
    }

    // Remove the last entry in the array list
    //
    lastEntry = pblListRemoveLast( (PblList *)heap );
    if( lastEntry == (void *)-1 )
    {
        return (void*)-1;
    }

    // One entry was removed, therefore the size must be decreased as well
    //
    size -= 1;
    if( size < 1 )
    {
        // The heap is empty now
        //
        return lastEntry;
    }

    retptr = pblListSet( (PblList *)heap, index, lastEntry );
    if( retptr == (void *)-1 )
    {
        return (void*)-1;
    }

    if( size > 1 )
    {
        // Copying the values might have violated the heap condition
        // at position index, it needs to ensured
        //
        pblEnsureHeapConditionDownward( heap, size, index );
    }

    return retptr;
}

/**
 * Removes the biggest element from the heap,
 * maintaining the heap condition of the heap.
 *
 * This function has a time complexity of O(Log N),
 * with N being the number of elements in the heap.
 *
 * @return void* retptr != (void*)-1: The element removed.
 * @return void* retptr == (void*)-1: An error see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - The heap is empty.
 */
void * pblHeapRemoveFirst( /*      */
PblHeap * heap /** The heap to use */
)
{
    return pblHeapRemoveAt( heap, 0 );
}

/**
 * Returns the element at the specified position in the heap.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The element at the specified position, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
void * pblHeapGet( /*                        */
PblHeap * heap, /** The heap to use          */
int index /** Index of the element to return */
)
{
    return pblListGet( (PblList *)heap, index );
}

/**
 * Returns but does not remove the element at the top of the
 * heap.
 *
 * This function has a time complexity of O(1).
 *
 * @return void* retptr != (void*)-1: The element returned.
 * @return void* retptr == (void*)-1: An error see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - The heap is empty.
 */
void * pblHeapGetFirst( /*         */
PblHeap * heap /** The heap to use */
)
{
    return pblListGet( (PblList *)heap, 0 );
}

/**
 * Constructs a heap using 'bottom-up heap construction'.
 *
 * This function has a time complexity of O(N),
 * with N being the number of elements in the heap.
 *
 * This function together with \Ref{pblHeapAddLast}()
 * can be used to build a heap with N elements
 * in time proportional to N.
 *
 * First create an empty heap with \Ref{pblHeapNew}()
 * and ensure the heap has space for N elements via a
 * call to \Ref{pblHeapEnsureCapacity}(),
 * then add all elements via calls to \Ref{pblHeapAddLast}(),
 * and finally ensure the heap condition with a call
 * to this function.
 */
void pblHeapConstruct( /*          */
PblHeap * heap /** The heap to use */
)
{
    int size = pblListSize( (PblList *)heap );
    int index;

    // For zero based arrays all entries with an index bigger
    // than 'size / 2 - 1' do not have any children.
    //
    // The heap condition is always fulfilled for entries without children,
    // therefore 'bottom-up heap construction' only needs to look
    // at elements that do have children
    //
    for( index = size / 2 - 1; index >= 0; index-- )
    {
        pblEnsureHeapConditionDownward( heap, size, index );
    }
}

/**
 * Returns an iterator over the elements in the heap.
 *
 * The iterator starts the iteration at the biggest element.
 *
 * <B>Note</B>: The memory allocated by this method for the iterator returned needs to be released
 *              by calling \Ref{pblIteratorFree}() once the iterator is no longer needed.
 *
 * Modifying the heap via the Iterator's own remove or add methods
 * does not maintain the heap property of the heap.
 * In this case the heap property has to be restored by a call
 * to \Ref{pblHeapConstruct}().
 *
 * The iterators returned by the this method are fail-fast:
 * if the heap is structurally modified at any time after the iterator is created,
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
PblIterator * pblHeapIterator( /*  */
PblHeap * heap /** The heap to use */
)
{
    return pblIteratorNew( (PblList *)heap );
}

/**
 * Joins the two heaps by moving all elements of the 'other'
 * heap. When this function returns, 'other' will be empty.
 *
 * This function has a time complexity of O(N), with N
 * being the number of elements in the heap after the join.
 *
 * @return int rc >= 0: The size of the heap after tjhe join.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblHeapJoin( /*                        */
PblHeap * heap, /** The heap to join to    */
PblHeap * other /** The other heap to join */
)
{
    int otherSize = pblListSize( (PblList *)other );
    int size = pblListSize( (PblList *)heap );

    if( otherSize == 0 )
    {
        // Joining from an empty 'other', nothing to do
        //
        return size;
    }

    // Make sure there is enough space for all the
    // elements having to be moved
    //
    size += otherSize;
    if( pblListEnsureCapacity( (PblList *)heap, size ) < 0 )
    {
        return -1;
    }

    // Low level entry copy from 'other' to 'heap'
    //
    if( pblListAddAll( (PblList *)heap, other ) < 0 )
    {
        return -1;
    }

    // The entries have been copied to 'heap',
    // they can be cleared from 'other', thus implementing a move
    //
    pblListClear( (PblList *)other );

    // If 'size == otherSize', then 'heap' was originally empty,
    // as 'other' fulfilled the heap condition before the join
    // and the low level entry copy did not violate it,
    // the heap condition now holds for the result of the join
    //
    if( size == otherSize )
    {
        return size;
    }

    // The entries of 'other' where appended to the entries
    // of 'heap', potentially breaking the heap condition,
    // therefore the heap condition needs to ensured anew
    //
    pblHeapConstruct( heap );

    return size;
}
