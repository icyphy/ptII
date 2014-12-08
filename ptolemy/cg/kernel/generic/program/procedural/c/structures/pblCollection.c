/*
 pblCollection.c - C implementation of a Collection similar
                   to the Java Collection.

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

    $Log: pblCollection.c,v $
    Revision 1.13  2010/05/19 22:38:45  peter
    Testing the map.

    Revision 1.12  2009/03/11 23:48:44  peter
    More tests and clean up.

    Revision 1.11  2009/03/08 20:56:50  peter
    port to gcc (Ubuntu 4.3.2-1ubuntu12) 4.3.2.
    Exposing the hash set and tree set interfaces.

*/

/*
 * Make sure "strings <exe> | grep Id | sort -u" shows the source file versions
 */
char* pblCollection_c_id = "$Id$";

#include <stdio.h>
#ifndef PT_DOES_NOT_HAVE_MEMORY_H
#include <memory.h>
#endif

#ifndef __APPLE__
#ifndef PT_DOES_NOT_HAVE_MALLOC_H
#include <malloc.h>
#endif
#endif

#include <stdlib.h>

#include "pbl.h"

/*****************************************************************************/
/* Functions                                                                 */
/*****************************************************************************/

/**
 * Compare two elements of a collection.
 *
 * @return int rc  < 0: left is smaller than right
 * @return int rc == 0: left and right are equal
 * @return int rc  > 0: left is greater than right
 */
int pblCollectionElementCompare(
PblCollection * collection,    /** The collection to compare the elements for   */
void *left,
void *right
)
{
    if( left == right )
    {
        return 0;
    }

    if( !left )
    {
        if( !right )
        {
            return 0;
        }
        return -1;
    }
    if( !right )
    {
        if( !left )
        {
            return 0;
        }
        return 1;
    }

    if( collection->compare )
    {
        /*
         * There is a specific compare function for the collection
         */
        return ( *( collection->compare ) )( &left, &right );
    }

    /*
     * Use the pointers of the objects to compare as default
     */
    return pblCollectionDefaultCompare( &left, &right );
}

/*
 * Default compare function used if no application specific compare function
 * is specified by the user via a call to \Ref{pblCollectionSetCompareFunction}.
 *
 * This function compares the values of the pointers directly,
 * i.e. it tests for object identity.
 *
 * This compare function behaves like the one that
 * can be specified for the C-library function 'qsort'.
 *
 * The arguments actually passed to the compare function when it is called
 * are addresses of the element pointers added to the list.
 * E.g.: If char * pointers are added to the list, the compare function
 * will be called with char ** pointers as arguments. See the documentation
 * for the C-library function 'qsort' for further information.
 *
 * @return int rc  < 0: left is smaller than right
 * @return int rc == 0: left and right are equal
 * @return int rc  > 0: left is greater than right
 */
int pblCollectionDefaultCompare(
    const void *left,     /** The left element for compare  */
    const void *right     /** The right element for compare */
)
{
    char * leftPointer = *(char**)left;
    char * rightPointer = *(char**)right;

    /*
     * Use the pointers of the objects to compare
     */
    if( leftPointer < rightPointer )
    {
        return ( -1 );
    }

    if( leftPointer == rightPointer )
    {
        return 0;
    }

    return ( 1 );
}

/**
 * Compares two '\0' terminated strings.
 *
 * Can be used as compare function for collections in case '\0'
 * terminated strings are inserted into the collection.
 *
 * If this function is to be used, it has to be set to the collection
 * via a call to \Ref{pblCollectionSetCompareFunction}
 *
 * This compare function behaves like the one that
 * can be specified for the C-library function 'qsort'.
 *
 * The arguments actually passed to the compare function when it is called
 * are addresses of the element pointers added to the list.
 * E.g.: If you add char * pointers to the list, the compare function
 * will be called with char ** pointers as arguments. See the documentation
 * for the C-library function 'qsort' for further information.
 *
 * @return int rc  < 0: left is smaller than right
 * @return int rc == 0: left and right are equal
 * @return int rc  > 0: left is greater than right
 */
int pblCollectionStringCompareFunction(
const void * left,                /* The left value for comparison  */
const void * right                /* The right value for comparison */
)
{
    char * leftPointer = *(char**)left;
    char * rightPointer = *(char**)right;

    if( !leftPointer )
    {
        if( rightPointer )
        {
            return -1;
        }
        return 0;
    }
    if( !rightPointer )
    {
        return 1;
    }
    return strcmp( leftPointer, rightPointer );
}

/**
 * Sets an application specific compare function for the elements
 * of the collection.
 *
 * An application specific compare function can be set to the collection
 * only if the collection is empty.
 *
 * If no specific compare function is specified by the user,
 * the default compare function is used.
 * The default compare function compares the two pointers directly,
 * i.e. it tests for object identity.
 *
 * The compare function specified should behave like the one that
 * can be specified for the C-library function 'qsort'.
 *
 * The arguments actually passed to the compare function when it is called
 * are addresses of the element pointers added to the collection.
 * E.g.: If you add char * pointers to the set, the compare function
 * will be called with char ** pointers as arguments. See the documentation
 * for the C-library function 'qsort' for further information.
 *
 * This method has a time complexity of O(1).
 *
 * @return * retptr != (void*)-1: The compare function used before, may be NULL.
 * @return * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_COLLECTION - The collection is not empty.
 */
void * pblCollectionSetCompareFunction(
PblCollection * collection,  /** The collection to set compare function for   */
int ( *compare )             /** The compare function to set                  */
    (
        const void* prev,    /** "left" element for compare                   */
        const void* next     /** "right" element for compare                  */
    )
)
{
    void * retptr = collection->compare;

    if( collection->size > 0 )
    {
        pbl_errno = PBL_ERROR_PARAM_COLLECTION;
        return (void*)-1;
    }

    collection->compare = compare;

    return retptr;
}

/**
 * Returns true if this collection contains the specified element.
 *
 * For lists this method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * For tree sets this method has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * For hash sets this method has a complexity of O(1).
 *
 * @return int rc != 0: The specified element is present.
 * @return int rc == 0: The specified element is not present.
 */
int pblCollectionContains(
PblCollection * collection, /** The collection to use            */
void * element              /** Element to look for              */
)
{
    if( PBL_LIST_IS_LIST( collection ) )
    {
        return ( pblListIndexOf( (PblList *)collection, element ) >= 0 );
    }
    return pblSetContains( (PblSet*)collection, element );
}

/**
 * Tests if the object is a collection.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc != 0: This object is a collection.
 * @return int rc == 0: This object is not a collection.
 */
int pblCollectionIsCollection(
void * object      /** The object to test */
)
{
    return PBL_COLLECTION_IS_COLLECTION(object);
}

/**
 * Returns a pblArrayList with a shallow copy of this collection instance.
 *
 * The elements themselves are not copied.
 *
 * This method has a memory and time complexity of O(N),
 * with N being the number of elements in the collection.
 *
 * @return PblList * retPtr != NULL: A pointer to the new list.
 * @return PblList * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_PARAM_COLLECTION        - The collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The collection was modified concurrently.
 */
PblList * pblCollectionConvertToArrayList(
PblCollection * collection /** The collection to convert */
)
{
    PblList * list = pblListNewArrayList();
    if( !list )
    {
        return NULL;
    }
    list->compare = collection->compare;

    if( pblListAddAll( list, collection ) < 0 )
    {
        pblListFree( list );
        return NULL;
    }

    return list;
}

/**
 * Returns a pblHashSet with a shallow copy of this collection instance.
 * An application specific hash value function can be set.
 *
 * The elements themselves are not copied.
 *
 * NULL elements contained in the collection are silently ignored.
 *
 * This method has a memory and time complexity of O(N),
 * with N being the number of elements in the collection.
 *
 * @return PblSet * retPtr != NULL: A pointer to the new set.
 * @return PblSet * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_PARAM_COLLECTION        - The collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The collection was modified concurrently.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS           - Maximum capacity of the hash set exceeded.
 */
PblSet * pblCollectionConvertToHashSet(
PblCollection * collection,  /** The collection to convert                            */
int ( *hashValue )           /** The hash value function for the new set, may be NULL */
    (
        const void* element  /** The element to get the hash value for                */
    ))
{
    PblSet * set = pblSetNewHashSet();
    if( !set )
    {
        return NULL;
    }

    set->compare = collection->compare;

    if( hashValue )
    {
        ( (PblHashSet*)set )->hashValue = hashValue;
    }
    else if( PBL_SET_IS_HASH_SET( collection ) )
    {
        ( (PblHashSet*)set )->hashValue
                = ( (PblHashSet*)collection )->hashValue;
    }

    if( pblSetAddAll( set, collection ) < 0 )
    {
        pblSetFree( set );
        return NULL;
    }

    return set;
}

/**
 * Returns a pblLinkedList with a shallow copy of this collection instance.
 *
 * The elements themselves are not copied.
 *
 * This method has a memory and time complexity of O(N),
 * with N being the number of elements in the collection.
 *
 * @return PblList * retPtr != NULL: A pointer to the new list.
 * @return PblList * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_PARAM_COLLECTION        - The collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The collection was modified concurrently.
 */
PblList * pblCollectionConvertToLinkedList(
PblCollection * collection /** The collection to convert */
)
{
    PblList * list = pblListNewLinkedList();
    if( !list )
    {
        return NULL;
    }
    list->compare = collection->compare;

    if( pblListAddAll( list, collection ) < 0 )
    {
        pblListFree( list );
        return NULL;
    }

    return list;
}

/**
 * Returns a pblTreeSet with a shallow copy of this collection instance.
 *
 * The elements themselves are not copied.
 *
 * NULL elements contained in the collection are silently ignored.
 *
 * This method has a memory complexity of O( N ) and a time complexity of O(N * Log N),
 * with N being the number of elements in the collection.
 *
 * @return PblSet * retPtr != NULL: A pointer to the new set.
 * @return PblSet * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_PARAM_COLLECTION        - The collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The collection was modified concurrently.
 */
PblSet * pblCollectionConvertToTreeSet(
PblCollection * collection /** The collection to convert */
)
{
    PblSet * set = pblSetNewTreeSet();
    if( !set )
    {
        return NULL;
    }
    set->compare = collection->compare;

    if( pblSetAddAll( set, collection ) < 0 )
    {
        pblSetFree( set );
        return NULL;
    }

    return set;
}

/**
 * Aggregates a collection by calling the aggregation function on every element
 * of the collection while running through the collection with an iterator.
 *
 * The application context supplied is passed to the aggregation function
 * for each element.
 *
 * If the aggregation function for an element returns a value > 0,
 * the aggregation is terminated immediately and the return value of
 * the aggregation function is given back to the caller.
 *
 * Otherwise the aggregation is continued to the next element
 * of the collection until the iterator reaches its end.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the collection.
 *
 * @return int rc  > 0: The aggregation was terminated with the return code of the aggregation function.
 * @return int rc == 0: The aggregation succeeded, all elements where aggregated.
 * @return int rc  < 0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_COLLECTION        - The collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The collection was modified concurrently.
 */
int pblCollectionAggregate(
PblCollection * collection,  /** The collection to aggregate.                                 */
void * context,              /** The application context to pass to the aggregation function. */
int ( *aggregation )         /** The aggregation function called on every collection element. */
    (
        void * context,      /** The application context passed.                              */
        int index,           /** The index of the element passed.                             */
        void * element       /** The collection element to aggregate.                         */
    )
)
{
    PblIterator iteratorBuffer;
    PblIterator * iterator = (PblIterator *)&iteratorBuffer;
    int rc = 0;
    int hasNext;
    int index = 0;
    void * element;

    /*
     * Get the iterator for this collection
     */
    if( pblIteratorInit( collection, &iteratorBuffer ) < 0 )
    {
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

        /*
         * Call the aggregation function on the element
         */
        rc = ( *( aggregation ) )( context, index++, element );
        if( rc > 0 )
        {
            return rc;
        }

    }
    if( hasNext < 0 )
    {
        // Concurrent modification
        //
        return -1;
    }

    return 0;
}

