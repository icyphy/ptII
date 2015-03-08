/*
 pblList.c - C implementation of two Lists similar
             to the Java ArrayList and Java LinkedList.

 Copyright (C) 2009   Peter Graf

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

    $Log: pblList.c,v $
    Revision 1.40  2010/05/30 20:06:45  peter
    Removed warnings found by 'Microsoft Visual C++ 2010'.

    Revision 1.39  2009/03/11 23:48:44  peter
    More tests and clean up.

    Revision 1.38  2009/03/08 20:56:50  peter
    port to gcc (Ubuntu 4.3.2-1ubuntu12) 4.3.2.
    Exposing the hash set and tree set interfaces.


    Revision 1.21  2009/02/03 16:40:14  peter
    PBL vesion 1.04, optimizations,
    MAC OS X port, port to Microsoft Visual C++ 2008 Express Edition,
    exposing the array list and the linked list interface

*/

/*
 * Make sure "strings <exe> | grep Id | sort -u" shows the source file versions
 */
char* pblList_c_id = "$Id$";

char * PblArrayListMagic = "PblArrayListMagic";
char * PblLinkedListMagic = "PblLinkedListMagic";

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
#ifndef PBLAL_INITIAL_CAPACITY /* value might be set by compiler switch      */
#define PBLAL_INITIAL_CAPACITY 10   /* default initial capacity              */
#endif


/*****************************************************************************/
/* Function declarations                                                     */
/*****************************************************************************/

static int pblLinkedListAdd(
PblLinkedList * list,  /** The list to append to               */
void * element         /** Element to be appended to this list */
);

static void * pblLinkedListRemoveAt(
PblLinkedList * list,   /** The list to use                                */
int index               /** Index at which the element is to be removed    */
);

static void ** pblLinkedListToArray(
PblLinkedList * list      /** The list to use */
);

static int pblLinkedListRemoveRange(
PblLinkedList * list,     /** The list to use                              */
int fromIndex,            /** The index of first element to be removed.    */
int toIndex               /** The index after last element to be removed.  */
);

static PblLinkedNode * pblLinkedListGetNodeAt(
PblLinkedList * list,     /** The list to use                */
int index                 /** Index of the node to return    */
);

static int pblLinkedListAddAt(
PblLinkedList * list,     /** The list to use                              */
int index,                /** Index at which the element is to be inserted */
void * element            /** Element to be appended to this list          */
);

/*****************************************************************************/
/* Functions                                                                 */
/*****************************************************************************/

/**
 * Creates a new array list.
 *
 * This method has a time complexity of O(1).
 *
 * @return PblList * retPtr != NULL: A pointer to the new list.
 * @return PblList * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
PblList * pblListNewArrayList( void )
{
    PblArrayList * pblList = (PblArrayList *)pbl_malloc0( "pblListNewArrayList",
                                       sizeof(PblArrayList) );
    if( !pblList )
    {
        return NULL;
    }

    pblList->genericList.magic = PblArrayListMagic;

    return (PblList *)pblList;
}

/**
 * Creates a new linked list.
 *
 * This method has a time complexity of O(1).
 *
 * @return PblList * retPtr != NULL: A pointer to the new list.
 * @return PblList * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
PblList * pblListNewLinkedList( void )
{
    PblLinkedList * pblList = (PblLinkedList *)pbl_malloc0( "pblListNewLinkedList",
                                        sizeof(PblLinkedList) );
    if( !pblList )
    {
        return NULL;
    }

    pblList->genericList.magic = PblLinkedListMagic;

    return (PblList *)pblList;
}

/*
 * Returns a shallow copy from this linked list of all of the elements
 * whose index is between fromIndex, inclusive and toIndex, exclusive.
 *
 * @return PblList * retPtr != NULL: A pointer to the new list.
 * @return PblList * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY  - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS  - fromIndex is out of range (fromIndex < 0 || fromIndex >= size())
 *                          or toIndex is out of range ( toIndex < 0 || toIndex > size())
 */
static PblList * pblLinkedListCloneRange(
PblLinkedList * list,     /** The list to use                              */
int fromIndex,            /** The index of first element to be cloned.     */
int toIndex               /** The index after last element to be cloned.   */
)
{
    int elementsToClone;
    int distanceToEnd;
    PblLinkedNode * linkedNode;
    PblLinkedList * newList = (PblLinkedList *)pblListNewLinkedList();

    if( !newList )
    {
        return NULL;
    }

    ( (PblLinkedList *)newList )->genericList.compare
            = list->genericList.compare;

    elementsToClone = toIndex - fromIndex;
    if( elementsToClone < 1 )
    {
        return (PblList *)newList;
    }

    distanceToEnd = list->genericList.size - toIndex;
    if( fromIndex <= distanceToEnd )
    {
        // Find the first node to clone from the beginning of the list
        // and clone forward
        //
        linkedNode = pblLinkedListGetNodeAt( list, fromIndex );
        if( !linkedNode )
        {
            pblListFree( (PblList *)newList );
            return NULL;
        }

        while( elementsToClone-- > 0 )
        {
            if( pblLinkedListAdd( newList, linkedNode->element ) < 0 )
            {
                pblListFree( (PblList *)newList );
                return NULL;
            }

            linkedNode = linkedNode->next;
        }
    }
    else
    {
        // Find the last node to clone from the end of the list
        // and clone backward
        //
        linkedNode = pblLinkedListGetNodeAt( list, toIndex - 1 );
        if( !linkedNode )
        {
            pblListFree( (PblList *)newList );
            return NULL;
        }

        while( elementsToClone-- > 0 )
        {
            if( pblLinkedListAddAt( newList, 0, linkedNode->element ) < 0 )
            {
                pblListFree( (PblList *)newList );
                return NULL;
            }

            linkedNode = linkedNode->prev;
        }
    }

    return (PblList *)newList;
}

/*
 * Returns a shallow copy from this array list of all of the elements
 * whose index is between fromIndex, inclusive and toIndex, exclusive.
 *
 * @return PblList * retPtr != NULL: A pointer to the new list.
 * @return PblList * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY  - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS  - fromIndex is out of range (fromIndex < 0 || fromIndex >= size())
 *                          or toIndex is out of range ( toIndex < 0 || toIndex > size())
 */
static PblList * pblArrayListCloneRange(
PblArrayList * list,      /** The list to use                             */
int fromIndex,            /** The index of first element to be cloned.    */
int toIndex               /** The index after last element to be cloned.  */
)
{
    int elementsToClone = toIndex - fromIndex;

    PblArrayList * newList = (PblArrayList *)pblListNewArrayList();
    if( !newList )
    {
        return NULL;
    }

    newList->genericList.compare = list->genericList.compare;

    if( elementsToClone < 1 )
    {
        return (PblList*)newList;
    }

    newList->pointerArray = pbl_memdup( "pblArrayListCloneRange pointerArray",
                                        &( list->pointerArray[ fromIndex ] ),
                                        sizeof(void*) * elementsToClone );

    if( !newList->pointerArray )
    {
        PBL_FREE( newList );
        return NULL;
    }

    newList->capacity = elementsToClone;
    newList->genericList.size = elementsToClone;

    return (PblList*)newList;
}

/**
 * Returns a shallow copy from this list of all of the elements
 * whose index is between fromIndex, inclusive and toIndex, exclusive.
 *
 * For array lists cloning has a time complexity
 * of O(M), with M being the number of elements cloned.
 *
 * For linked lists cloning from the beginning or the end of the list has a time complexity
 * of O(M) with M being the number of elements cloned,
 * while cloning from a random position in the middle of the list has a time
 * complexity of O(N) with N being the number of elements in the list.
 *
 * This method has a memory complexity of O(M),
 * with M being the number of elements cloned.
 *
 * @return PblList * retPtr != NULL: A pointer to the new list.
 * @return PblList * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY  - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS  - fromIndex is out of range (fromIndex < 0 || fromIndex >= size())
 *                          or toIndex is out of range ( toIndex < 0 || toIndex > size()) or range is negative.
 */
PblList * pblListCloneRange(
PblList * list,           /** The list to use                             */
int fromIndex,            /** The index of first element to be cloned.    */
int toIndex               /** The index after last element to be cloned.  */
)
{
    int elementsToClone = toIndex - fromIndex;

    if( fromIndex < 0 || fromIndex > list->size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return NULL;
    }

    if( toIndex < 0 || toIndex > list->size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return NULL;
    }

    if( elementsToClone < 0 )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return NULL;
    }

    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        return pblLinkedListCloneRange( (PblLinkedList *)list, fromIndex, toIndex );
    }

    return pblArrayListCloneRange( (PblArrayList *)list, fromIndex, toIndex );
}

/**
 * Returns a shallow copy of this list instance.
 *
 * The elements themselves are not copied.
 *
 * This method has a memory and time complexity of O(N),
 * with N being the number of elements in the list.
 *
 * @return PblList * retPtr != NULL: A pointer to the new list.
 * @return PblList * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
PblList * pblListClone(
PblList * list          /** The list to clone */
)
{
    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        return pblLinkedListCloneRange( (PblLinkedList *)list, 0, list->size );
    }

    return pblArrayListCloneRange( (PblArrayList *)list, 0, list->size );
}

/*
 * Removes all of the elements from this list.
 *
 * This method has a time complexity of O(N),
 * with N being the number of elements in the list.
 *
 * @return void
 */
static void pblArrayListClear(
PblArrayList * list     /** The list to clear */
)
{
    if( list->genericList.size > 0 && list->pointerArray )
    {
        memset( list->pointerArray, 0, sizeof(void*) * list->genericList.size );
    }
    list->genericList.size = 0;
    list->genericList.changeCounter++;
}

/*
 * Removes all of the elements from this list.
 *
 * This method has a time complexity of O(N),
 * with N being the number of elements in the list.
 *
 * @return void
 */
static void pblLinkedListClear(
PblLinkedList * list     /** The list to clear */
)
{
    while( list->head )
    {
        PblLinkedNode * nodeToFree = list->head;
        PBL_LIST_UNLINK( list->head, list->tail, nodeToFree, next, prev );
        PBL_FREE( nodeToFree );
    }
    list->genericList.size = 0;
    list->genericList.changeCounter++;
}

/**
 * Removes all of the elements from this list.
 *
 * <B>Note:</B> No memory of the elements themselves is freed.
 *
 * This method has a time complexity of O(N),
 * with N being the number of elements in the list.
 *
 * @return void
 */
void pblListClear(
PblList * list     /** The list to clear */
)
{
    if( PBL_LIST_IS_ARRAY_LIST( list ) )
    {
        pblArrayListClear( (PblArrayList *)list );
        return;
    }

    pblLinkedListClear( (PblLinkedList *)list );
}

/*
 * Reverses the order of the elements of the array list.
 *
 * This method has a time complexity of O(N),
 * with N being the number of elements in the list.
 *
 * @return void
 */
static void pblArrayListReverse(
PblArrayList * list /** The list to reverse */
)
{
    unsigned char ** leftPointer = list->pointerArray - 1;
    unsigned char ** rightPointer = list->pointerArray + list->genericList.size;
    unsigned char * tmp;

    if( list->genericList.size < 2 )
    {
        return;
    }
    list->genericList.changeCounter++;

    while( ++leftPointer < --rightPointer )
    {
        tmp = *rightPointer;
        *rightPointer = *leftPointer;
        *leftPointer = tmp;
    }
}

/*
 * Reverses the order of the elements of this linked list.
 *
 * This method has a time complexity of O(N),
 * with N being the number of elements in the list.
 *
 * @return void
 */
static void pblLinkedListReverse(
PblLinkedList * list /** The list to reverse */
)
{
    PblLinkedNode * leftNode = list->head;
    PblLinkedNode * rightNode = list->tail;
    void * tmp;

    if( list->genericList.size < 2 )
    {
        return;
    }
    list->genericList.changeCounter++;

    while( leftNode != rightNode )
    {
        tmp = rightNode->element;
        rightNode->element = leftNode->element;
        leftNode->element = tmp;

        if( leftNode->next == rightNode )
        {
            break;
        }
        leftNode = leftNode->next;
        rightNode = rightNode->prev;
    }
}

/**
 * Reverses the order of the elements of this list.
 *
 * This method has a time complexity of O(N),
 * with N being the number of elements in the list.
 *
 * @return void
 */
void pblListReverse(
PblList * list /** The list to reverse */
)
{
    if( PBL_LIST_IS_ARRAY_LIST( list ))
    {
        pblArrayListReverse( (PblArrayList *)list );
        return;
    }

    pblLinkedListReverse( (PblLinkedList *)list );
}

/*
 * Free the array list's memory from heap.
 *
 * <B>Note:</B> The memory of the elements themselves is not freed.
 *
 * This method has a time complexity of O(1).
 *
 * @return void
 */
static void pblArrayListFree(
PblArrayList * list    /** The list to free */
)
{
    PBL_FREE( list->pointerArray );
    PBL_FREE( list );
}


/**
 * Free the list's memory from heap.
 *
 * <B>Note:</B> The memory of the elements themselves is not freed.
 *
 * For array lists this method has a time complexity of O(1).
 *
 * For linked lists this method has a time complexity of O(N),
 * with N being the number of elements in the list.
 *
 * @return void
 */
void pblListFree(
PblList * list    /** The list to free */
)
{
    if( PBL_LIST_IS_ARRAY_LIST( list ))
    {
        pblArrayListFree( (PblArrayList *)list );
        return;
    }

    pblLinkedListClear( (PblLinkedList *)list );
    PBL_FREE( list );
}

/*
 * Increases the capacity of this array list instance, if necessary.
 *
 * This method ensures that the list can hold
 * at least the number of elements specified by the minimum
 * capacity argument.
 *
 * @return int rc >= 0: OK, the list capacity is returned.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
static int pblArrayListEnsureCapacity(
PblArrayList * list,       /** The list to use              */
int minCapacity            /** The desired minimum capacity */
)
{
    unsigned char ** pointerArray;

    if( minCapacity <= list->capacity )
    {
        /*
         * Nothing to do
         */
        return list->capacity;
    }

    /*
     * Malloc space for minCapacity pointers
     */
    pointerArray = (unsigned char **)pbl_malloc( "pblArrayListEnsureCapacity", sizeof(void*)
            * minCapacity );
    if( !pointerArray )
    {
        return -1;
    }

    if( list->capacity > 0 )
    {
        /*
         * Copy the old values
         */
        memcpy( pointerArray, list->pointerArray, sizeof(void*)
                * list->capacity );
    }

    /*
     * Make sure all new pointers are NULL
     */
    memset( &( pointerArray[ list->capacity ] ), 0, sizeof(void*)
            * ( minCapacity - list->capacity ) );

    /*
     * Remember the new capacity
     */
    list->capacity = minCapacity;

    /*
     * Free any old data
     */
    if( list->pointerArray )
    {
        PBL_FREE( list->pointerArray );
    }

    /*
     * Remember the new pointer array
     */
    list->pointerArray = pointerArray;
    list->genericList.changeCounter++;

    return list->capacity;
}

/**
 * Increases the capacity of this list instance, if necessary.
 *
 * For array lists this method ensures that the list can hold
 * at least the number of elements specified by the minimum
 * capacity argument.
 *
 * For linked lists this method does nothing,
 * it justs returns the value of parameter minCapacity.
 *
 * If the list is an array list and if the capacity is actually
 * increased, this method has a memory and time complexity of O(N),
 * with N being the new capacity of the list.
 *
 * In all other cases this method has a time complexity of O(1).
 *
 * @return int rc >= 0: OK, the list capacity is returned.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblListEnsureCapacity(
PblList * list, /** The list to use              */
int minCapacity /** The desired minimum capacity */
)
{
    if( PBL_LIST_IS_LINKED_LIST( list ))
    {
        return minCapacity;
    }

    return pblArrayListEnsureCapacity( (PblArrayList *)list, minCapacity );
}

/*
 * Sets the size of a linked list.
 *
 * If the size is increased, the new elements are initialized with NULL.
 *
 * This method has a time complexity of O(N),
 * with N being the difference between the old and new size
 * of the list.
 *
 * @return int rc >= 0: OK, the list size is returned.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
static int pblLinkedListSetSize(
PblLinkedList * list,            /** The list to use         */
int size                         /** The desired size to set */
)
{
    int nAdded = 0;

    if( size < 0 || size == list->genericList.size )
    {
        /*
         * Nothing to do
         */
        return list->genericList.size;
    }

    while( size < list->genericList.size )
    {
        pblLinkedListRemoveAt( list, list->genericList.size - 1 );
    }

    while( size > list->genericList.size )
    {
        if( pblLinkedListAdd( list, NULL ) < 0 )
        {
            while( nAdded-- > 0 )
            {
                pblLinkedListRemoveAt( list, list->genericList.size - 1 );
            }
            return -1;
        }
        nAdded++;
    }

    return list->genericList.size;
}

/*
 * Sets the size of an array list.
 *
 * If the size is increased, the new elements are initialized with NULL.
 *
 * This method has a time complexity of O(N),
 * with N being the difference between the old and new size
 * of the list.
 *
 * @return int rc >= 0: OK, the list size is returned.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
static int pblArrayListSetSize(
PblArrayList * list, /** The list to use          */
int size             /** The desired size to set  */
)
{
    if( size < 0 || size == list->genericList.size )
    {
        /*
         * Nothing to do
         */
        return list->genericList.size;
    }

    if( size < list->genericList.size )
    {
        /*
         * Make sure all unused pointers are NULL
         */
        memset( &( list->pointerArray[ size ] ), 0, sizeof(void*)
                * ( list->genericList.size - size ) );
    }
    else if( size > list->capacity )
    {
        /*
         * Make some more space
         */
        int capacity = list->capacity;
        if( capacity < PBLAL_INITIAL_CAPACITY )
        {
            capacity = PBLAL_INITIAL_CAPACITY;
        }

        if( size > capacity * 2 + 1 )
        {
            capacity = size;
        }
        else
        {
            while( size > capacity )
            {
                capacity = capacity * 2 + 1;
            }
        }

        if( pblArrayListEnsureCapacity( list, capacity ) < size )
        {
            return size;
        }
    }

    list->genericList.size = size;
    list->genericList.changeCounter++;
    return list->genericList.size;
}

/**
 * Sets the size of a list.
 *
 * Truncates the list if necessary.
 *
 * If the size is increased, the new elements are initialized with NULL.
 *
 * This method has a time complexity of O(N),
 * with N being the difference between the old and new size
 * of the list.
 *
 * @return int rc >= 0: OK, the list size is returned.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblListSetSize(
PblList * list,     /** The list to use          */
int size            /** The desired size to set  */
)
{
    if( size < 0 || size == list->size )
    {
        /*
         * Nothing to do
         */
        return list->size;
    }

    if( size == 0 )
    {
        pblListClear( list );
        return 0;
    }

    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        return pblLinkedListSetSize( (PblLinkedList *)list, size );
    }

    return pblArrayListSetSize( (PblArrayList *)list, size );
}

/**
 * Returns the capacity of this list instance.
 *
 * For linked lists this call returns the list's size.
 *
 * For array lists it returns the list's capacity.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc: The capacity of this list instance.
 */
int pblListGetCapacity(
PblList * list          /** The list to use */
)
{
    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        return list->size;
    }

    return ((PblArrayList *)list)->capacity;
}

/**
 * Returns the number of elements in this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc: The number of elements in this list.
 */
int pblListSize(
PblList * list   /** The list to use */
)
{
    return list->size;
}

/**
 * Tests if this list has no elements.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc != 0: This list has no elements.
 * @return int rc == 0: This list has elements.
 */
int pblListIsEmpty(
PblList * list      /** The list to test */
)
{
    return 0 == list->size;
}

/**
 * Tests if the object is a list.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc != 0: This object is a list.
 * @return int rc == 0: This object is not a list.
 */
int pblListIsList(
void * object      /** The object to test */
)
{
    return PBL_LIST_IS_LIST(object);
}

/**
 * Tests if the object is an array list.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc != 0: This object is an array list.
 * @return int rc == 0: This object is not an array list.
 */
int pblListIsArrayList(
void * object           /** The object to test */
)
{
    return PBL_LIST_IS_ARRAY_LIST(object);
}

/**
 * Tests if the object is a linked list.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc != 0: This object is a linked list.
 * @return int rc == 0: This object is not a linked list.
 */
int pblListIsLinkedList(
void * object            /** The object to test */
)
{
    return PBL_LIST_IS_LINKED_LIST(object);
}

/*
 * Trims the capacity of this list instance to be the list's current size.
 *
 * If the capacity is actually
 * decreased, this method has a time complexity of O(N),
 * with N being the number of elements in the list.
 *
 * In all other cases this method has a time complexity of O(1).
 *
 * @return int rc >= 0: The capacity of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
static int pblArrayListTrimToSize(
PblArrayList * list         /** The list to use */
)
{
    unsigned char ** pointerArray;
    int nBytes;

    if( list->genericList.size == list->capacity )
    {
        return list->capacity;
    }

    if( list->genericList.size == 0 )
    {
        PBL_FREE( list->pointerArray );
        list->capacity = 0;
        return list->capacity;
    }

    nBytes = sizeof(void*) * list->genericList.size;

    /*
     * Malloc space for size pointers
     */
    pointerArray = (unsigned char **)pbl_malloc( "pblListTrimToSize", nBytes );
    if( !pointerArray )
    {
        return -1;
    }

    /*
     * Copy the values
     */
    memcpy( pointerArray, list->pointerArray, nBytes );

    PBL_FREE( list->pointerArray );
    list->pointerArray = pointerArray;
    list->capacity = list->genericList.size;

    return list->capacity;
}

/**
 * Trims the capacity of this list instance to be the list's current size.
 *
 * For linked list this call returns the list's size.
 *
 * If the list is an array list and if the capacity is actually
 * decreased, this method has a time complexity of O(N),
 * with N being the number of elements in the list.
 *
 * In all other cases this method has a time complexity of O(1).
 *
 * @return int rc >= 0: The capacity of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblListTrimToSize(
PblList * list         /** The list to use */
)
{
    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        return list->size;
    }

    return pblArrayListTrimToSize( (PblArrayList *)list );
}


/*
 * Searches for the first linked node containing the given element.
 *
 * This method has a time complexity of O(N),
 * with N being the number of elements in the list.
 *
 * @return void * retptr != NULL: The linked node containing the element.
 * @return void * retptr == NULL: The specified element is not present.
 */
static PblLinkedNode * pblLinkedListGetNode(
PblLinkedList * list,     /** The list to use                        */
void * element            /** Element to look for                    */
)
{
    PblLinkedNode * linkedNode = list->head;

    while( linkedNode )
    {
        if( !pblCollectionElementCompare( (PblCollection*)list, element, linkedNode->element ) )
        {
            return linkedNode;
        }
        linkedNode = linkedNode->next;
    }

    return NULL;
}

/*
 * Returns the linked list node at the specified position in this linked list.
 *
 * This method has a time complexity of O(N),
 * with N being the minimum of the differences between index and
 * 0 or index and the size of the list. Therefore retrieving the first
 * or last node has a time complexity of O(1),
 * but retrieving a random node from the list has O(N).
 *
 * @return void * retptr != NULL: The node at the specified position in this list.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size().
 */
static PblLinkedNode * pblLinkedListGetNodeAt(
PblLinkedList * list,     /** The list to use                */
int index                 /** Index of the node to return    */
)
{
    PblLinkedNode * linkedNode;

    if( index <= list->genericList.size / 2 )
    {
        linkedNode = list->head;
        while( index-- > 0 )
        {
            if( !linkedNode )
            {
                pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
                return NULL;
            }
            linkedNode = linkedNode->next;
        }
    }
    else
    {
        index = list->genericList.size - ( index + 1 );

        linkedNode = list->tail;
        while( index-- > 0 )
        {
            if( !linkedNode )
            {
                pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
                return NULL;
            }
            linkedNode = linkedNode->prev;
        }
    }

    return linkedNode;
}

/*
 * Inserts the specified element at the specified position in this
 * linked list.
 *
 * This method has a time complexity of O(N),
 * with N being the minimum of the differences between index and
 * 0 or index and the size of the list. Therefore adding the first
 * or last element has a time complexity of O(1),
 * but adding a random element to the list has O(N).
 *
 * @return int rc >= 0: The size of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
static int pblLinkedListAddAt(
PblLinkedList * list,     /** The list to use                              */
int index,                /** Index at which the element is to be inserted */
void * element            /** Element to be appended to this list          */
)
{
    PblLinkedNode * newNode;
    PblLinkedNode * otherNode;

    if( index < 0 || index > list->genericList.size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return -1;
    }

    if( index == list->genericList.size )
    {
        return pblLinkedListAdd( list, element );
    }

    newNode = (PblLinkedNode *)pbl_malloc( "pblLinkedListAddAt", sizeof(PblLinkedNode) );
    if( !newNode )
    {
        return -1;
    }
    newNode->element = element;

    if( index == 0 )
    {
        PBL_LIST_PUSH( list->head, list->tail, newNode, next, prev );
    }
    else
    {
        otherNode = pblLinkedListGetNodeAt( list, index );
        if( !otherNode )
        {
            PBL_FREE( newNode );
            return -1;
        }
        PBL_LIST_INSERT( list->head, otherNode, newNode, next, prev );
    }

    list->genericList.size++;
    list->genericList.changeCounter++;

    return list->genericList.size;
}

/*
 * Inserts the specified element at the specified position in this list.
 *
 * Shifts the element currently at that position (if any) and any subsequent
 * elements to the right (adds one to their indices).
 *
 * For array lists adding to the end of the list has a time complexity
 * of O(1), while adding to the beginning of the list has a time
 * complexity of O(N) with N being the number of elements in the list.
 *
 * @return int rc >= 0: The size of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
static int pblArrayListAddAt(
PblArrayList * list, /** The list to use                              */
int index,           /** Index at which the element is to be inserted */
void * element       /** Element to be appended to this list          */
)
{
    if( index < 0 || index > list->genericList.size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return -1;
    }

    if( list->genericList.size == list->capacity )
    {
        /*
         * Make some more space
         */
        int capacity = list->capacity * 2 + 1;
        if( capacity < PBLAL_INITIAL_CAPACITY )
        {
            capacity = PBLAL_INITIAL_CAPACITY;
        }

        if( pblArrayListEnsureCapacity( list, capacity ) < 0 )
        {
            return -1;
        }
    }

    if( index < list->genericList.size )
    {
        unsigned char * from = (unsigned char*)&( list->pointerArray[ index ] );
        unsigned char * to = from + sizeof(void*);
        int length = sizeof(void*) * ( list->genericList.size - index );

        memmove( to, from, length );
    }

    list->pointerArray[ index ] = (unsigned char *)element;
    list->genericList.size++;
    list->genericList.changeCounter++;

    return list->genericList.size;
}

/**
 * Inserts the specified element at the specified position in this list.
 *
 * Shifts the element currently at that position (if any) and any subsequent
 * elements to the right (adds one to their indices).
 *
 * For array lists adding to the end of the list has a time complexity
 * of O(1), while adding to the beginning of the list has a time
 * complexity of O(N) with N being the number of elements in the list.
 *
 * For linked lists adding to beginning or the end of the list has a time complexity
 * of O(1), while adding to a random position in the middle of the list has a time
 * complexity of O(N) with N being the number of elements in the list.
 *
 * @return int rc >= 0: The size of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
int pblListAddAt(
PblList * list,    /** The list to use                              */
int index,         /** Index at which the element is to be inserted */
void * element     /** Element to be appended to this list          */
)
{
    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        return pblLinkedListAddAt( (PblLinkedList *)list, index, element );
    }

    return pblArrayListAddAt( (PblArrayList *)list, index, element );
}

/*
 * Appends the specified element to the end of this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc >= 0: The size of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
static int pblLinkedListAdd(
PblLinkedList * list,  /** The list to append to               */
void * element         /** Element to be appended to this list */
)
{
    PblLinkedNode * newNode;

    newNode = (PblLinkedNode *)pbl_malloc( "pblLinkedListAdd", sizeof(PblLinkedNode) );
    if( !newNode )
    {
        return -1;
    }

    newNode->element = element;

    PBL_LIST_APPEND( list->head, list->tail, newNode, next, prev );
    list->genericList.size++;
    list->genericList.changeCounter++;

    return list->genericList.size;
}

/**
 * Appends the specified element to the end of this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc >= 0: The size of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblListAdd(
PblList * list,  /** The list to append to               */
void * element   /** Element to be appended to this list */
)
{
    if( PBL_LIST_IS_ARRAY_LIST(list) )
    {
        return pblArrayListAddAt( (PblArrayList *)list, list->size, element );
    }

    return pblLinkedListAdd( (PblLinkedList *)list, element );
}

/**
 * Pushes the specified element to the end (last element) of this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc >= 0: The size of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblListPush(
PblList * list,   /** The list to append to               */
void * element    /** Element to be appended to this list */
)
{
    return pblListAdd( list, element );
}

/**
 * Inserts the given element at the beginning of this list.
 *
 * For array lists this method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * For linked lists this method has a time complexity of O(1).
 *
 * @return int rc >= 0: The size of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblListAddFirst(
PblList * list,           /** The list to add to              */
void * element            /** Element to be added to the list */
)
{
    return pblListAddAt( list, 0, element );
}

/**
 * Appends the given element to the end of this list.
 *
 * (Identical in function to the add method; included only for consistency.)
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc >= 0: The size of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblListAddLast(
PblList * list,           /** The list to add to              */
void * element            /** Element to be added to the list */
)
{
    return pblListAdd( list, element );
}

/**
 * Adds the specified element as the tail (last element) of this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc >= 0: The size of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblListOffer(
PblList * list,           /** The list to add to              */
void * element            /** Element to be added to the list */
)
{
    return pblListAdd( list, element );
}

/*
 * Inserts all of the elements in the specified collection
 * into this linked list at the specified position.
 *
 * @return int rc >= 0: The size of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS           - Index is out of range, (index < 0 || index >= size()).
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The list underlying the iterator was modified concurrently.
 */
static int pblLinkedListAddAllAt(
PblLinkedList * list,  /** The list to use                                           */
int index,             /** Index at which the elements are to be inserted            */
PblIterator * iterator /** The iterator whose elements are to be added to this list. */
)
{
    int nAdded = 0;
    PblLinkedNode * otherNode;
    int hasNext;

    if( index == list->genericList.size )
    {
        // Add at the end of the list
        //
        while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
        {
            void * element = pblIteratorNext( iterator );
            if( element == (void*)-1 || pblLinkedListAdd( list, element ) < 0 )
            {
                // An error, remove the elements added so far
                //
                while( nAdded-- > 0 )
                {
                    pblLinkedListRemoveAt( list, list->genericList.size - 1 );
                }
                return -1;
            }
            nAdded++;
        }
        if( hasNext < 0 )
        {
            // Concurrent modification error on the source list,
            // remove the elements added so far
            //
            while( nAdded-- > 0 )
            {
                pblLinkedListRemoveAt( list, list->genericList.size - 1 );
            }
            return -1;
        }

        return list->genericList.size;
    }

    otherNode = pblLinkedListGetNodeAt( list, index );
    if( !otherNode )
    {
        return -1;
    }

    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        PblLinkedNode * newNode;

        newNode = (PblLinkedNode *)pbl_malloc( "pblLinkedListAddAllAt", sizeof(PblLinkedNode) );
        if( !newNode )
        {
            // Out of memory,
            // remove the elements added so far
            //
            while( nAdded-- > 0 )
            {
                pblLinkedListRemoveAt( list, index );
            }
            return -1;
        }
        newNode->element = pblIteratorNext( iterator );
        if( newNode->element == (void*)-1 )
        {
            // Concurrent modification error on the source list,
            // remove the elements added so far
            //
            while( nAdded-- > 0 )
            {
                pblLinkedListRemoveAt( list, list->genericList.size - 1 );
            }
            PBL_FREE( newNode );
            return -1;
        }

        PBL_LIST_INSERT( list->head, otherNode, newNode, next, prev );
        list->genericList.size++;
        list->genericList.changeCounter++;
        nAdded++;
    }
    if( hasNext < 0 )
    {
        // Concurrent modification error on the source list,
        // remove the elements added so far
        //
        while( nAdded-- > 0 )
        {
            pblLinkedListRemoveAt( list, list->genericList.size - 1 );
        }
        return -1;
    }

    return list->genericList.size;
}

/**
 * Inserts all of the elements in the specified collection
 * into this list at the specified position.
 *
 * Shifts the element currently at that position (if any) and any
 * subsequent elements to the right (increases their indices).
 * The new elements will appear in this list in the order that
 * they are returned by the specified collection's iterator.
 *
 * The behavior of this operation is unspecified if the specified
 * collection is modified while the operation is in progress.
 * (Note that this will occur if the specified collection
 * is this list, and it's nonempty.)
 *
 * For array lists adding to the end of the list has a time complexity
 * of O(M), while adding to the beginning of the list has a time
 * complexity of O(N + M) with N being the number of elements in the list
 * and M being the size of the collection whose elements are added.
 *
 * For linked lists adding to beginning or the end of the list has a time complexity
 * of O(M), while adding to a random position in the middle of the list has a time
 * complexity of O(N + M) with N being the number of elements in the list
 * and M being the size of the collection whose elements are added.
 *
 * @return int rc >= 0: The size of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS           - Index is out of range, (index < 0 || index >= size()).
 * <BR>PBL_ERROR_PARAM_LIST              - Collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - Collection was modified concurrently.
 */
int pblListAddAllAt(
PblList * list,     /** The list to use                                             */
int index,          /** Index at which the element are to be inserted               */
void * collection   /** The collection whose elements are to be added to this list. */
)
{
    PblArrayList * pblList;
    PblIterator    iteratorBuffer;
    PblIterator *  iterator = &iteratorBuffer;
    int iteratorSize;
    int hasNext;

    if( index < 0 || index > list->size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return -1;
    }

    if( pblIteratorInit( (PblCollection *)collection, iterator ) < 0 )
    {
        return -1;
    }

    iteratorSize = pblIteratorSize( iterator );
    if( iteratorSize < 1 )
    {
        return list->size;
    }

    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        int rc = pblLinkedListAddAllAt( (PblLinkedList *)list, index, iterator );
        return rc;
    }

    pblList = (PblArrayList *)list;

    if( pblList->genericList.size + iteratorSize >= pblList->capacity )
    {
        /*
         * Make some more space
         */
        int capacity = pblList->capacity * 2 + 1;
        if( capacity < pblList->genericList.size + iteratorSize )
        {
            capacity = pblList->genericList.size + iteratorSize;
        }
        if( capacity < PBLAL_INITIAL_CAPACITY )
        {
            capacity = PBLAL_INITIAL_CAPACITY;
        }

        if( pblArrayListEnsureCapacity( pblList, capacity ) < 0 )
        {
            return -1;
        }
    }

    if( index < pblList->genericList.size )
    {
        unsigned char * from = (unsigned char*)&( pblList->pointerArray[ index ] );
        unsigned char * to = from + iteratorSize * sizeof(void*);
        int length = sizeof(void*) * ( pblList->genericList.size - index );

        memmove( to, from, length );
    }

    // If the source is an array list, we use memcpy directly
    //
    if( PBL_LIST_IS_ARRAY_LIST(collection) )
    {
        PblArrayList * source = (PblArrayList *)collection;
        unsigned char * from = (unsigned char*)&( source->pointerArray[ 0 ] );
        unsigned char * to = (unsigned char*)&( pblList->pointerArray[ index ] );
        int length = sizeof(void*) * source->genericList.size;

        memcpy( to, from, length );
    }
    else
    {
        while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
        {
            void * next = pblIteratorNext( iterator );
            if( next == (void*)-1 )
            {
                // Concurrent modification on the source collection
                //
                return -1;
            }
            pblList->pointerArray[ index++ ] = (unsigned char *)next;
        }
        if( hasNext < 0 )
        {
            // Concurrent modification on the source collection
            //
            return -1;
        }
    }

    pblList->genericList.size += iteratorSize;
    pblList->genericList.changeCounter++;

    return pblList->genericList.size;
}

/**
 * Appends all of the elements in the specified Collection to the end of this list,
 * in the order that they are returned by the specified Collection's Iterator.
 *
 * This method has a time complexity of O(M),
 * with M being the size of the collection whose elements are added.
 *
 * @return int rc >= 0: The size of this list instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_PARAM_LIST              - Collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - Collection was modified concurrently.
 */
int pblListAddAll(
PblList * list,    /** The list to use                                */
void * collection  /** The collection whose elements are to be added to this list. */
)
{
    return pblListAddAllAt( list, list->size, collection );
}

/**
 * Retrieves and removes the head (first element) of this list.
 *
 * @return void * retptr != (void*)-1: The head of this list, can be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - This list is empty.
 */
void * pblListRemove(
PblList * list          /** The list to use */
)
{
    return pblListRemoveAt( list, 0 );
}

/*
 * Removes the element at the specified position in this linked list.
 *
 * Shifts any subsequent elements to the left (subtracts one from their indices).
 *
 * @return void * retptr != (void*)-1: The element that was removed, can be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
static void * pblLinkedListRemoveAt(
PblLinkedList * list,   /** The list to use                                */
int index               /** Index at which the element is to be removed    */
)
{
    PblLinkedNode * nodeToFree;
    unsigned char * result;

    nodeToFree = pblLinkedListGetNodeAt( list, index );
    if( !nodeToFree )
    {
        return (void*)-1;
    }

    result = nodeToFree->element;

    PBL_LIST_UNLINK( list->head, list->tail, nodeToFree, next, prev );
    list->genericList.size--;
    list->genericList.changeCounter++;

    PBL_FREE( nodeToFree );

    return result;
}

/*
 * Removes the element at the specified position in this array list.
 *
 * Shifts any subsequent elements to the left (subtracts one from their indices).
 *
 * @return void * retptr != (void*)-1: The element that was removed, can be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
void * pblArrayListRemoveAt(
PblArrayList * list,    /** The list to use                                */
int index               /** Index at which the element is to be removed    */
)
{
    unsigned char * result;

    if( index < 0 || index >= list->genericList.size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return (void*)-1;
    }

    result = list->pointerArray[ index ];

    if( index < list->genericList.size - 1 )
    {
        unsigned char * to = (unsigned char*)&( list->pointerArray[ index ] );
        unsigned char * from = to + sizeof(void*);
        int length = sizeof(void*) * ( ( list->genericList.size - 1 ) - index );

        memmove( to, from, length );
    }

    list->pointerArray[ list->genericList.size - 1 ] = NULL;
    list->genericList.size--;
    list->genericList.changeCounter++;

    return result;
}

/**
 * Removes the element at the specified position in this list.
 *
 * Shifts any subsequent elements to the left (subtracts one from their indices).
 *
 * For array lists removing from the end of the list has a time complexity
 * of O(1), while removing from the beginning of the list has a time
 * complexity of O(N) with N being the number of elements in the list.
 *
 * For linked lists removing from the beginning or the end of the list has a time complexity
 * of O(1), while removing from a random position in the middle of the list has a time
 * complexity of O(N) with N being the number of elements in the list.
 *
 * @return void * retptr != (void*)-1: The element that was removed, can be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
void * pblListRemoveAt(
PblList * list,         /** The list to use                                 */
int index               /** The index at which the element is to be removed */
)
{
    if( index < 0 || index >= list->size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return (void*)-1;
    }

    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        return pblLinkedListRemoveAt( (PblLinkedList *)list, index );
    }

    return pblArrayListRemoveAt( (PblArrayList *)list, index );
}

/*
 * Removes from this list all of the elements whose index is between
 * fromIndex, inclusive and toIndex, exclusive.
 *
 * For linked lists removing from the beginning or the end of the list has a time complexity
 * of O(M), while removing from a random position in the middle of the list has a time
 * complexity of O(N + M) with N being the number of elements in the list
 * and M being the number of elements removed.
 *
 * @return int rc >= 0: The size of the list.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS -  fromIndex is out of range (fromIndex < 0 || fromIndex >= size())
 *                         or toIndex is out of range ( toIndex < 0 || toIndex > size())
 */
static int pblLinkedListRemoveRange(
PblLinkedList * list,     /** The list to use                              */
int fromIndex,            /** The index of first element to be removed.    */
int toIndex               /** The index after last element to be removed.  */
)
{
    int elementsToRemove = toIndex - fromIndex;
    int distanceToEnd = list->genericList.size - toIndex;
    PblLinkedNode * linkedNode;

    if( fromIndex <= distanceToEnd )
    {
        // Find the first node to remove from the beginning of the list
        // and remove forward
        //
        linkedNode = pblLinkedListGetNodeAt( list, fromIndex );
        if( !linkedNode )
        {
            return -1;
        }

        list->genericList.size -= elementsToRemove;
        while( elementsToRemove-- > 0 )
        {
            PblLinkedNode * nodeToFree = linkedNode;
            linkedNode = linkedNode->next;

            PBL_LIST_UNLINK( list->head, list->tail, nodeToFree, next, prev );
            PBL_FREE( nodeToFree );
        }
    }
    else
    {
        // Find the last node to remove from the end of the list
        // and remove backward
        //
        linkedNode = pblLinkedListGetNodeAt( list, toIndex - 1 );
        if( !linkedNode )
        {
            return -1;
        }

        list->genericList.size -= elementsToRemove;
        while( elementsToRemove-- > 0 )
        {
            PblLinkedNode * nodeToFree = linkedNode;
            linkedNode = linkedNode->prev;

            PBL_LIST_UNLINK( list->head, list->tail, nodeToFree, next, prev );
            PBL_FREE( nodeToFree );
        }
    }

    list->genericList.changeCounter++;
    return list->genericList.size;
}

/**
 * Removes from this list all of the elements whose index is between
 * fromIndex, inclusive and toIndex, exclusive. Shifts any succeeding
 * elements to the left (reduces their index).
 * This call shortens the list by (toIndex - fromIndex) elements.
 * (If toIndex==fromIndex, this operation has no effect.)
 *
 * <B>Note:</B> No memory of the elements themselves is freed.
 *
 * For array lists removing from the end of the list has a time complexity
 * of O(M), while removing from the beginning of the list has a time
 * complexity of O(N + M) with N being the number of elements in the list
 * and M being the number of elements removed.
 *
 * For linked lists removing from the beginning or the end of the list has a time complexity
 * of O(M), while removing from a random position in the middle of the list has a time
 * complexity of O(N + M) with N being the number of elements in the list
 * and M being the number of elements removed.
 *
 * @return int rc >= 0: The size of the list.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS  -  fromIndex is out of range (fromIndex < 0 || fromIndex >= size())
 *                          or toIndex is out of range ( toIndex < 0 || toIndex > size())
 */
int pblListRemoveRange(
PblList * list,           /** The list to use                              */
int fromIndex,            /** The index of first element to be removed.    */
int toIndex               /** The index after last element to be removed.  */
)
{
    PblArrayList * pblList;
    int elementsToRemove;

    if( fromIndex < 0 || fromIndex >= list->size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return -1;
    }

    if( toIndex < 0 || toIndex > list->size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return -1;
    }

    elementsToRemove = toIndex - fromIndex;
    if( elementsToRemove < 1 )
    {
        return list->size;
    }

    if( elementsToRemove == list->size )
    {
        pblListClear( list );
        return 0;
    }

    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        return pblLinkedListRemoveRange( (PblLinkedList *)list, fromIndex, toIndex );
    }

    pblList = (PblArrayList *)list;

    if( toIndex < pblList->genericList.size )
    {
        unsigned char * to =
                (unsigned char*)&( pblList->pointerArray[ fromIndex ] );
        unsigned char * from = to + elementsToRemove * sizeof(void*);
        int length = sizeof(void*) * ( pblList->genericList.size - toIndex );

        memmove( to, from, length );
    }

    if( elementsToRemove > 0 )
    {
        unsigned char * to =
                (unsigned char*)&( pblList->pointerArray[ pblList->genericList.size
                        - elementsToRemove ] );
        int length = sizeof(void*) * elementsToRemove;

        memset( to, 0, length );
        pblList->genericList.size -= elementsToRemove;
    }

    pblList->genericList.changeCounter++;

    return pblList->genericList.size;
}

/**
 * Removes and returns the last element in this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The last element in this list, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this list is empty.
 */
void * pblListRemoveLast(
PblList * list         /** The list to use */
)
{
    return pblListRemoveAt( list, list->size - 1 );
}

/**
 * Removes and returns the first element in this list.
 *
 * For array lists this method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * For linked lists this method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The first element in this list, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this list is empty.
 */
void * pblListRemoveFirst(
PblList * list         /** The list to use */
)
{
    return pblListRemoveAt( list, 0 );
}

/**
 * Returns the last element in this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The last element in this list, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this list is empty.
 */
void * pblListGetLast(
PblList * list         /** The list to use */
)
{
    return pblListGet( list, list->size - 1 );
}

/**
 * Retrieves, but does not remove, the tail (last element) of this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The last element in this list, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this list is empty.
 */
void * pblListTail(
PblList * list         /** The list to use */
)
{
    return pblListGet( list, list->size - 1 );
}

/**
 * Returns the first element in this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The first element in this list, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this list is empty.
 */
void * pblListGetFirst(
PblList * list          /** The list to use */
)
{
    return pblListGet( list, 0 );
}

/**
 * Returns the element at the specified position in this list.
 *
 * For array lists this method has a time complexity of O(1).
 *
 * For linked lists this method has a time complexity of O(N),
 * with N being the minimum of the differences between index and
 * 0 or index and the size of the list. Therefore retrieving the first
 * or last element has a time complexity of O(1),
 * but retrieving a random element from the list has O(N).
 *
 * @return void * retptr != (void*)-1: The element at the specified position in this list, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
void * pblListGet(
PblList * list,     /** The list to use                */
int index           /** Index of the element to return */
)
{
    /*
     * Check the parameter
     */
    if( index < 0 || index >= list->size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return (void*)-1;
    }

    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        PblLinkedNode * linkedNode = pblLinkedListGetNodeAt( (PblLinkedList *)list, index );
        if( !linkedNode )
        {
            return (void*)-1;
        }
        return linkedNode->element;
    }
    else
    {
        PblArrayList * pblList = (PblArrayList *)list;
        return pblList->pointerArray[ index ];
    }
}

/**
 * Retrieves, but does not remove, the head (first element) of this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The first element of this list, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this list is empty.
 */
void * pblListPeek(
PblList * list      /** The list to use */
)
{
    return pblListGet( list, 0 );
}

/**
 * Retrieves, but does not remove, the tail (last element) of this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The last element of this list, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this list is empty.
 */
void * pblListTop(
PblList * list     /** The list to use */
)
{
    return pblListGet( list, list->size - 1 );
}

/**
 * Retrieves and removes the tail (last element) of this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The last element of this list, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this list is empty.
 */
void * pblListPop(
PblList * list     /** The list to use */
)
{
    return pblListRemoveAt( list, list->size - 1 );
}

/**
 * Retrieves and removes the head (first element) of this list.
 *
 * For array lists this method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * For linked lists this method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The head of this list, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this list is empty.
 */
void * pblListPoll(
PblList * list      /** The list to use                */
)
{
    return pblListRemoveAt( list, 0 );
}

/**
 * Retrieves, but does not remove, the head (first element) of this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The first element of this list, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this list is empty.
 */
void * pblListElement(
PblList * list         /** The list to use  */
)
{
    return pblListGet( list, 0 );
}

/**
 * Retrieves, but does not remove, the head (first element) of this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The first element of this list, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this list is empty.
 */
void * pblListHead(
PblList * list         /** The list to use  */
)
{
    return pblListGet( list, 0 );
}

/**
 * Replaces the head (first element) of this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The first element of this list before this call, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this list is empty.
 */
void * pblListSetFirst(
PblList * list,         /** The list to use                            */
void    * element       /** Element to be stored at the first position */
)
{
    return pblListSet( list, 0, element );
}

/**
 * Replaces the tail (last element) of this list.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The last element of this list before this call, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this list is empty.
 */
void * pblListSetLast(
PblList * list,        /** The list to use                           */
void    * element      /** Element to be stored at the last position */
)
{
    return pblListSet( list, list->size - 1, element );
}

/**
 * Replaces the element at the specified position in this list with the specified element.
 *
 * For array lists this method has a time complexity of O(1).
 *
 * For linked lists this method has a time complexity of O(N),
 * with N being the minimum of the differences between index and
 * 0 or index and the size of the list. Therefore changing the first
 * or last element has a time complexity of O(1),
 * but changing a random element from the list has O(N).
 *
 * @return void * retptr != (void*)-1: The element previously at the specified position, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
void * pblListSet(
PblList * list,    /** The list to use                                */
int index,         /** Index of element to replace                    */
void * element     /** Element to be stored at the specified position */
)
{
    unsigned char * result;

    if( index < 0 || index >= list->size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return (void*)-1;
    }

    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        PblLinkedNode * linkedNode = pblLinkedListGetNodeAt( (PblLinkedList *)list, index );
        if( !linkedNode )
        {
            return (void*)-1;
        }

        result = (unsigned char *)linkedNode->element;
        linkedNode->element = element;
        return result;
    }
    else
    {
        PblArrayList * pblList = (PblArrayList *)list;
        result = pblList->pointerArray[ index ];
        pblList->pointerArray[ index ] = (unsigned char *)element;
        return result;
    }
}

/**
 * Sets an application specific compare function for the elements
 * of the list.
 *
 * An application specific compare function can be set to the list.
 * If no specific compare function is specified by the user,
 * the default compare function is used.
 * The default compare function compares the two pointers directly,
 * i.e. it tests for object identity.
 *
 * The compare function specified should behave like the one that
 * can be specified for the C-library function 'qsort'.
 *
 * The arguments actually passed to the compare function when it is called
 * are addresses of the element pointers added to the list.
 * E.g.: If you add char * pointers to the list, the compare function
 * will be called with char ** pointers as arguments. See the documentation
 * for the C-library function 'qsort' for further information.
 *
 * This method has a time complexity of O(1).
 *
 * @return * retptr: The compare function used before, may be NULL.
 */
void * pblListSetCompareFunction(
PblList * list,              /** The list to set compare function for   */
int ( *compare )             /** The compare function to set            */
    (
        const void* prev,    /** The "left" element for compare         */
        const void* next     /** The "right" element for compare        */
    )
)
{
    void * retptr = list->compare;

    list->compare = compare;

    return retptr;
}

/**
 * Gets the application specific compare function for the elements
 * of the list.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr: The compare function used, may be NULL.
 */
void * pblListGetCompareFunction(
PblList * list         /** The list to get the compare function for   */
)
{
    return list->compare;
}

/*
 * Sorts the elements of the array list.
 *
 * This method uses the C library function 'qsort',
 * therefore it normally has a time complexity of O( N * Log(N) ),
 * with N being he size of the list to sort.
 *
 * @return int rc == 0: Ok.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 */
static int pblArrayListSort(
PblArrayList * list,         /** The list to sort                       */
int ( *compare )             /** Specific compare function to use       */
    (
        const void* prev,    /** "left" element for compare             */
        const void* next     /** "right" element for compare            */
    )
)
{
    if( list->genericList.size < 2 )
    {
        return 0;
    }

    qsort( list->pointerArray, (size_t)list->genericList.size,
           (size_t)sizeof(void*), ( compare ? compare
                   : list->genericList.compare ? list->genericList.compare
                           : pblCollectionDefaultCompare ) );

    list->genericList.changeCounter++;
    return 0;
}

/*
 * Sorts the elements of the linked list.
 *
 * This method uses the C library function 'qsort',
 * therefore it normally has a time complexity of O( N * Log(N) ),
 * with N being he size of the list to sort.
 *
 * This method has a memory complexity of O(N).
 *
 * @return int rc == 0: Ok.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 */
static int pblLinkedListSort(
PblLinkedList * list,        /** The list to sort                       */
int ( *compare )             /** Specific compare function to use       */
    (
        const void* prev,    /** "left" element for compare             */
        const void* next     /** "right" element for compare            */
    )
)
{
    PblLinkedNode * node = list->head;
    void ** array;
    void ** arrayPointer;

    if( list->genericList.size < 2 )
    {
        return 0;
    }

    array = pblLinkedListToArray( list );
    if( !array )
    {
        // Out of memory.
        //
        return -1;
    }

    arrayPointer = array;
    qsort( array, (size_t)list->genericList.size, (size_t)sizeof(void*),
           ( compare ? compare
                   : list->genericList.compare ? list->genericList.compare
                           : pblCollectionDefaultCompare ) );

    while( node )
    {
        node->element = *arrayPointer++;
        node = node->next;
    }
    PBL_FREE(array);

    list->genericList.changeCounter++;
    return 0;
}

/**
 * Sorts the elements of the list.
 *
 * A specific compare function can be used for the sort.
 *
 * If NULL is specific as specific compare function, the compare
 * function set for the list will be used if any, otherwise the
 * default compare function is used.
 *
 * The default compare function compares the two pointers directly.
 *
 * The compare function specified should behave like the one that
 * can be specified for the C-library function 'qsort'.
 *
 * This method uses the C library function 'qsort',
 * therefore it normally has a time complexity of O( N * Log(N) ),
 * with N being he size of the list to sort.
 *
 * For linked lists this method has a memory complexity of O(N).
 * For array lists this method does not need memory.
 *
 * @return int rc == 0: Ok.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 */
int pblListSort(
PblList * list,              /** The list to sort                       */
int ( *compare )             /** Specific compare function to use       */
    (
        const void* prev,    /** "left" element for compare             */
        const void* next    /** "right" element for compare            */
    )
)
{
    if( PBL_LIST_IS_ARRAY_LIST( list ) )
    {
        return pblArrayListSort( (PblArrayList*)list, compare );
    }

    return pblLinkedListSort( (PblLinkedList*)list, compare );
}

/*
 * Searches for the first occurence of the given argument.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * @return int rc >= 0: The index of the specified element.
 * @return int rc <  0: The specified element is not present.
 */
static int pblLinkedListIndexOf(
PblLinkedList * list,     /** The list to use      */
void * element            /** Element to look for  */
)
{
    int index = 0;
    PblLinkedNode * linkedNode = list->head;

    while( linkedNode )
    {
        if( !pblCollectionElementCompare( (PblCollection*)list, element, linkedNode->element ) )
        {
            return index;
        }
        linkedNode = linkedNode->next;
        index++;
    }

    return -1;
}

/*
 * Searches for the first occurence of the given argument.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * @return int rc >= 0: The index of the specified element.
 * @return int rc <  0: The specified element is not present.
 */
static int pblArrayListIndexOf(
PblArrayList * list, /** The list to use      */
void * element       /** Element to look for  */
)
{
    int index;

    for( index = 0; index < list->genericList.size; index++ )
    {
        if( !pblCollectionElementCompare( (PblCollection*)list, element, list->pointerArray[ index ] ) )
        {
            return index;
        }
    }

    return -1;
}

/**
 * Searches for the first occurence of the given argument.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * @return int rc >= 0: The index of the specified element.
 * @return int rc <  0: The specified element is not present.
 */
int pblListIndexOf(
PblList * list,              /** The list to use      */
void * element               /** Element to look for  */
)
{
    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        return pblLinkedListIndexOf( (PblLinkedList*)list, element );
    }

    return pblArrayListIndexOf( (PblArrayList*)list, element );
}

/*
 * Searches for the last occurence of the given argument.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * @return int rc >= 0: The last index of the specified element.
 * @return int rc <  0: The specified element is not present.
 */
static int pblLinkedListLastIndexOf(
PblLinkedList * list,        /** The list to use                        */
void * element               /** Element to look for                    */
)
{
    int index = list->genericList.size - 1;
    PblLinkedNode * linkedNode = list->tail;

    while( linkedNode )
    {
        if( !pblCollectionElementCompare( (PblCollection*)list, element, linkedNode->element ) )
        {
            return index;
        }
        linkedNode = linkedNode->prev;
        index--;
    }

    return -1;
}

/*
 * Searches for the last occurence of the given argument.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * @return int rc >= 0: The last index of the specified element.
 * @return int rc <  0: The specified element is not present.
 */
static int pblArrayListLastIndexOf(
PblArrayList * list,         /** The list to use                        */
void * element               /** Element to look for                    */
)
{
    int index;

    for( index = list->genericList.size - 1; index >= 0; index-- )
    {
        if( !pblCollectionElementCompare( (PblCollection*)list, element, list->pointerArray[ index ] ) )
        {
            return index;
        }
    }

    return -1;
}

/**
 * Searches for the last occurence of the given argument.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * @return int rc >= 0: The last index of the specified element.
 * @return int rc <  0: The specified element is not present.
 */
int pblListLastIndexOf(
PblList * list,           /** The list to use                        */
void * element            /** Element to look for                    */
)
{
    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        return pblLinkedListLastIndexOf( (PblLinkedList*)list, element );
    }

    return pblArrayListLastIndexOf( (PblArrayList*)list, element );
}

/**
 * Returns true if this list contains the specified element.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * @return int rc != 0: The specified element is present.
 * @return int rc == 0: The specified element is not present.
 */
int pblListContains(
PblList * list,           /** The list to use                  */
void * element            /** Element to look for              */
)
{
    return ( pblListIndexOf( list, element ) >= 0 );
}

/**
 * Returns a value > 0 if this list contains all of the elements
 * in the specified collection.
 *
 * This implementation iterates over the specified collection,
 * checking each element returned by the iterator in turn to see if it's contained in this list.
 * If all elements are so contained a value > 0 is returned, otherwise 0.
 *
 * This method has a time complexity of O(N * M),
 * with N being the size of the list and M being the size of the
 * collection.
 *
 * @return int rc >  0: The list contains all of the elements in the specified collection.
 * @return int rc == 0: The list does not contain all of the elements.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_COLLECTION        - The collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The collection was modified concurrently.
 */
int pblListContainsAll(
PblList * list,    /** The list to use                                            */
void * collection  /** The collection to be checked for containment in this list. */
)
{
    PblIterator   iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int iteratorSize;
    int hasNext;

    if( pblIteratorInit( collection, iterator ) < 0 )
    {
        return -1;
    }

    iteratorSize = pblIteratorSize( iterator );
    if( iteratorSize < 1 )
    {
        return 1;
    }

    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        void * element = pblIteratorNext( iterator );
        if( element == (void*)-1 )
        {
            // concurrent modification on the collection
            //
            return -1;
        }
        if( !pblListContains( list, element ) )
        {
            return 0;
        }
    }
    if( hasNext < 0 )
    {
        // concurrent modification on the collection
        //
        return -1;
    }

    return 1;
}

/*
 * Removes or retains from this array list all of its elements
 * that are contained in the specified collection.
 *
 * @return int rc >  0: If this list changed as a result of the call.
 * @return int rc == 0: This list did not change.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_LIST - The list cannot be iterated.
 */
static int pblArrayListRemoveRetainAll(
PblArrayList  * list,       /** The list to use                        */
PblCollection * collection, /** The collection to use                  */
int       doRemove          /** Flag: do a remove or a retain          */
)
{
    int index = 0;
    int newIndex = 0;
    void * element;
    int isContained;

    for( index = 0; index < list->genericList.size; index++ )
    {
        element = list->pointerArray[ index ];
        isContained = pblCollectionContains( collection, element );

        if( ( doRemove && !isContained ) || ( !doRemove && isContained ) )
        {
            if( newIndex != index )
            {
                list->pointerArray[ newIndex++ ] = element;
            }
            else
            {
                newIndex++;
            }
        }
    }

    if( newIndex != index )
    {
        if( pblArrayListSetSize( list, newIndex ) != newIndex )
        {
            return -1;
        }
        return 1;
    }

    return 0;
}

/*
 * Removes or retains from this linked list all of its elements
 * that are contained in the specified collection.
 *
 * @return int rc >  0: If this list changed as a result of the call.
 * @return int rc == 0: This list did not change.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_LIST              - The list cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The list was modified concurrently.
 */
static int pblLinkedListRemoveRetainAll(
PblLinkedList * list,       /** The list to use                        */
PblCollection * collection, /** The collection to use                  */
int       doRemove          /** Flag: do a remove or a retain          */
)
{
    PblIterator   iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int rc = 0;
    int hasNext;
    void * element;
    int isContained;

    /*
     * Get the iterator for this list
     */
    if( pblIteratorInit( (PblCollection *)list, iterator ) < 0 )
    {
        pbl_errno = PBL_ERROR_PARAM_LIST;
        return -1;
    }

    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        element = pblIteratorNext( iterator );
        if( element == (void*)-1 )
        {
            // Concurrent modification
            //
            return -1;
        }

        isContained = pblCollectionContains( collection, element );

        if( ( doRemove && isContained ) || ( !doRemove && !isContained ) )
        {
            if( pblIteratorRemove( iterator ) < 0 )
            {
                return -1;
            }

            /*
             * The list changed
             */
            rc = 1;
        }
    }
    if( hasNext < 0 )
    {
        // Concurrent modification
        //
        return -1;
    }

    return rc;
}

/**
 * Removes from this list all of its elements
 * that are contained in the specified collection.
 *
 * This implementation iterates over this list,
 * checking each element returned by the iterator in turn
 * to see if it's contained in the specified collection.
 *
 * If it's so contained, it's removed from this list
 * with the iterator's remove method in case of a linked list
 * and with an optimized direct removal method in case of
 * an array list.
 *
 * This method has a time complexity of O(N * M),
 * with N being the size of the list and M being the size of the
 * collection.
 *
 * @return int rc >  0: If this list changed as a result of the call.
 * @return int rc == 0: This list did not change.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_LIST              - The list be iterated.
 * <BR>PBL_ERROR_PARAM_COLLECTION        - The collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The list was modified concurrently.
 */
int pblListRemoveAll(
PblList * list,    /** The list to use                                                 */
void * collection  /** The collection whose elements are to be removed from this list. */
)
{
    PblIterator   iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int iteratorSize;

    if( list->size < 1 )
    {
        return 0;
    }

    /*
     * Get the iterator for the collection
     */
    if( pblIteratorInit( (PblCollection *)collection, iterator ) < 0 )
    {
        return -1;
    }

    iteratorSize = pblIteratorSize( iterator );
    if( iteratorSize < 1 )
    {
        return 0;
    }

    if( PBL_LIST_IS_ARRAY_LIST( list ) )
    {
        return pblArrayListRemoveRetainAll( (PblArrayList*)list, (PblCollection *)collection, 1 );
    }

    return pblLinkedListRemoveRetainAll( (PblLinkedList*)list, (PblCollection *)collection, 1 );
}

/**
 * Retains only the elements in this list
 * that are contained in the specified collection.
 *
 * In other words, removes from this list all
 * of its elements that are not contained in the specified collection.
 *
 * This implementation iterates over this list,
 * checking each element returned by the iterator in turn
 * to see if it's contained in the specified collection.
 *
 * If it's not so contained, it's removed from this list
 * with the iterator's remove method in case of a linked list
 * and with an optimized direct removal method in case of
 * an array list.
 *
 * This method has a time complexity of O(N * M),
 * with N being the size of the list and M being the size of the
 * collection.
 *
 * @return int rc >  0: If this list changed as a result of the call.
 * @return int rc == 0: This list did not change.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_LIST              - The list cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The list was modified concurrently.
 * <BR>PBL_ERROR_PARAM_COLLECTION        - The collection cannot be iterated.
 */
int pblListRetainAll(
PblList * list,           /** The list to use                           */
void * collection         /** The elements to be retained in this list. */
)
{
    PblIterator   iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int iteratorSize;

    if( list->size < 1 )
    {
        return 0;
    }

    /*
     * Get the iterator for the collection
     */
    if( pblIteratorInit( (PblCollection *)collection, iterator ) < 0 )
    {
        return -1;
    }

    iteratorSize = pblIteratorSize( iterator );
    if( iteratorSize < 0 )
    {
        return -1;
    }

    /*
     * Get the iterator for this list
     */
    if( pblIteratorInit( list, iterator ) < 0 )
    {
        return -1;
    }

    if( iteratorSize == 0 )
    {
        if( pblIteratorSize( iterator ) == 0 )
        {
            return 0;
        }

        /*
         * Clear the entire list
         */
        pblListClear( list );
        return 1;
    }

    if( PBL_LIST_IS_ARRAY_LIST( list ) )
    {
        return pblArrayListRemoveRetainAll( (PblArrayList*)list, (PblCollection *)collection, 0 );
    }

    return pblLinkedListRemoveRetainAll( (PblLinkedList*)list, (PblCollection *)collection, 0 );
}

/*
 * Removes a single instance of the specified element from this
 * linked list, if it is present.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * @return int rc != 0: The list contained the specified element.
 * @return int rc == 0: The specified element is not present.
 */
static int pblLinkedListRemoveElement(
PblLinkedList * list,      /** The list to use                        */
void * element             /** Element to remove                      */
)
{
    PblLinkedNode * nodeToFree = pblLinkedListGetNode( list, element );
    if( !nodeToFree )
    {
        return 0;
    }

    PBL_LIST_UNLINK( list->head, list->tail, nodeToFree, next, prev );
    list->genericList.size--;
    list->genericList.changeCounter++;

    PBL_FREE( nodeToFree );

    return 1;
}

/**
 * Removes a single instance of the specified element from this list,
 * if it is present.
 *
 * More formally, removes an element e such that
 * (o==null ? e==null : o.equals(e)),
 * if the list contains one or more such elements.
 * Returns true if the list contained the specified
 * element (or equivalently, if the list changed as
 * a result of the call).
 *
 * This method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * @return int rc != 0: The list contained the specified element.
 * @return int rc == 0: The specified element is not present.
 */
int pblListRemoveElement(
PblList * list,            /** The list to use                    */
void * element             /** The element to remove              */
)
{
    int index;

    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        return pblLinkedListRemoveElement( (PblLinkedList*)list, element );
    }

    index = pblListIndexOf( list, element );
    if( index >= 0 )
    {
        pblArrayListRemoveAt( (PblArrayList*)list, index );
        return 1;
    }
    return 0;
}

/*
 * Returns an array containing all of the elements in this linked list in the correct order.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * @return void * retptr != NULL: The array containing the elements of the list.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - The list is empty.
 */
static void ** pblLinkedListToArray(
PblLinkedList * list         /** The list to use */
)
{
    PblLinkedNode * linkedNode = list->head;
    void ** arrayPointer;
    void ** resultArray;

    resultArray = (void **)pbl_malloc( "pblLinkedListToArray", sizeof(void*)
            * list->genericList.size );
    if( !resultArray )
    {
        return NULL;
    }

    arrayPointer = resultArray;
    while( linkedNode )
    {
        *arrayPointer++ = linkedNode->element;
        linkedNode = linkedNode->next;
    }

    return resultArray;
}

/**
 * Returns an array containing all of the elements in this list in the correct order.
 *
 * <B>Note:</B> The pointer array returned is malloced from heap, the caller has to free it
 * after it is no longer needed!
 *
 * The size of the pointer array malloced and returned is defined by the pblListSize()
 * of the list.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * @return void * retptr != NULL: The array containing the elements of the list.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - The list is empty.
 */
void ** pblListToArray(
PblList * list           /** The list to use */
)
{
    if( list->size == 0 )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return NULL;
    }

    if( PBL_LIST_IS_LINKED_LIST( list ) )
    {
        return pblLinkedListToArray( (PblLinkedList*)list );
    }
    else
    {
        PblArrayList * pblList = (PblArrayList *)list;

        return (void **)pbl_memdup( "pblListToArray", pblList->pointerArray, sizeof(void*)
                * pblList->genericList.size );
    }
}

/**
 * Compares the specified collection with this list for equality.
 *
 * Returns true if and only if the specified collection is a collection,
 * both collections have the same size, and all corresponding pairs of
 * elements in the two collections are equal. (Two elements e1 and e2
 * are equal if (e1==null ? e2==null : compare( e1, e2 )==0.)
 *
 * In other words, two collections are defined to be equal as lists
 * if they contain the same elements in the same order.
 *
 * This implementation first checks if the specified collection is this list.
 * If so, it returns true; if not, it checks if the specified collection is a list or a tree set.
 * If not, it returns false; if so, it iterates over both collections,
 * comparing corresponding pairs of elements by using the compare function of this list.
 * If any comparison returns false, this method returns false.
 * If either iterator runs out of elements before the other it returns false (as the lists are of unequal length);
 * otherwise it returns true when the iterations complete.
 *
 * This method has a time complexity of O(Min(N,M)),
 * with N being the size of the list and M being the
 * number of elements in the object compared.
 *
 * @return int rc >  0: The specified collection is equal to this list.
 * @return int rc == 0: The specified collection is not equal to this list.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_LIST              - The list or collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The list or collection was modified concurrently.
 */
int pblListEquals(
PblList * list,   /** The list to compare with.                                  */
void * collection /** The collection to be compared for equality with this list. */
)
{
    PblIterator iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;

    PblIterator thisIteratorBuffer;
    PblIterator * thisIterator = &thisIteratorBuffer;

    int hasNext;
    int thisHasNext = 0;
    void * next;
    void * thisNext;

    if( list == collection )
    {
        return 1;
    }

    if( !PBL_COLLECTION_IS_COLLECTION( collection ) )
    {
        return 0;
    }

    if( pblIteratorInit( list, thisIterator ) < 0 )
    {
        return -1;
    }

    if( pblIteratorInit( collection, iterator ) < 0 )
    {
        return -1;
    }

    if( pblIteratorSize( iterator ) != pblIteratorSize( thisIterator ) )
    {
        return 0;
    }

    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        if( ( thisHasNext = pblIteratorHasNext( thisIterator ) ) < 0 )
        {
            return -1;
        }
        if( thisHasNext == 0 )
        {
            return 0;
        }

        next = pblIteratorNext( iterator );
        if( next == (void*)-1 )
        {
            return -1;
        }

        thisNext = pblIteratorNext( thisIterator );
        if( thisNext == (void*)-1 )
        {
            return -1;
        }

        if( pblCollectionElementCompare( (PblCollection*)list, thisNext, next ) )
        {
            return 0;
        }
    }
    if( hasNext < 0 )
    {
        return -1;
    }

    if( ( thisHasNext = pblIteratorHasNext( thisIterator ) ) > 0 )
    {
        return 0;
    }
    if( thisHasNext < 0 )
    {
        return -1;
    }

    return 1;
}

/**
 * Returns an iterator over the elements in this list in proper sequence.
 *
 * The iterator starts the iteration at the beginning of the list.
 *
 * <B>Note</B>: The memory allocated by this method for the iterator returned needs to be released
 *              by calling pblIteratorFree() once the iterator is no longer needed.
 *
 * The iterators returned by the this method are fail-fast:
 * if the list is structurally modified at any time after the iterator is created,
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
 * <BR>PBL_ERROR_PARAM_LIST    - list cannot be iterated.
 */
PblIterator * pblListIterator(
PblList * list                 /** The list to create the iterator for */
)
{
    if( !PBL_LIST_IS_LIST( list ) )
    {
        pbl_errno = PBL_ERROR_PARAM_LIST;
        return NULL;
    }

    return pblIteratorNew( list );
}

/**
 * Returns a reverse iterator over the elements in this list in proper sequence.
 *
 * The reverse iterator starts the iteration at the end of the list.
 *
 * <B>Note:</B> The memory allocated by this method for the iterator returned needs to be released
 *       by calling pblIteratorFree() once the iterator is no longer needed.
 *
 * The iterators returned by the this method are fail-fast:
 * if the list is structurally modified at any time after the iterator is created,
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
 * <BR>PBL_ERROR_PARAM_LIST    - list cannot be iterated.
 */
PblIterator * pblListReverseIterator(
PblList * list                 /** The list to create the iterator for */
)
{
    if( !PBL_LIST_IS_LIST( list ) )
    {
        pbl_errno = PBL_ERROR_PARAM_LIST;
        return NULL;
    }

    return pblIteratorReverseNew( list );
}



