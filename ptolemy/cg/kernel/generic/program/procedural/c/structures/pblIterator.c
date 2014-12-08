/*
 pblIterator.c - C implementation of an Iterator similar
                 to the Java Iterator and Java ListIterator.

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

    $Log: pblIterator.c,v $
    Revision 1.9  2010/05/30 20:06:45  peter
    Removed warnings found by 'Microsoft Visual C++ 2010'.

    Revision 1.8  2009/03/15 21:29:29  peter
    *** empty log message ***

    Revision 1.7  2009/03/11 23:48:44  peter
    More tests and clean up.

    Revision 1.6  2009/03/08 20:56:50  peter
    port to gcc (Ubuntu 4.3.2-1ubuntu12) 4.3.2.
    Exposing the hash set and tree set interfaces.

*/

/*
 * Make sure "strings <exe> | grep Id | sort -u" shows the source file versions
 */
char* pblIterator_c_id = "$Id$";

char * PblIteratorMagic = "PblIteratorMagic";

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
/* Typedefs                                                                  */
/*****************************************************************************/

/*
 * The tree iterator type.
 */
typedef struct PblTreeIterator_s
{
    char          * magic;         /* The magic string of iterators            */
    unsigned long   changeCounter; /* The number of changes on the collection  */
    PblCollection * collection;    /* The collection the iterator works on     */
    int             index;         /* The current index of the iterator        */

    int lastIndexReturned;         /* Index of element that was returned last  */

    PblTreeNode * current;         /* The current node in the tree set         */

    PblTreeNode * prev;            /* The previous node in the tree set        */
    PblTreeNode * next;            /* The next node in the tree set            */

} PblTreeIterator;

/*
 * The hash iterator type.
 */
typedef struct PblHashIterator_s
{
    char          * magic;         /* The magic string of iterators            */
    unsigned long   changeCounter; /* The number of changes on the collection  */
    PblCollection * collection;    /* The collection the iterator works on     */
    int             index;         /* The current index of the iterator        */

    int lastIndexReturned;         /* Index of element that was returned last  */

    void ** current;               /* The current element in the hash          */

    void ** prev;                  /* The previous element in the hash         */
    void ** next;                  /* The next element in the hash             */

} PblHashIterator;

/*****************************************************************************/
/* Functions                                                                 */
/*****************************************************************************/

/**
 * Returns an iterator over the elements in this collection in proper sequence.
 *
 * The iterator starts the iteration at the beginning of the collection.
 *
 * <B>Note</B>: The memory allocated by this method for the iterator returned needs to be released
 *              by calling pblIteratorFree() once the iterator is no longer needed.
 *
 * The iterators returned by the this method are fail-fast:
 * if the collection is structurally modified at any time after the iterator is created,
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
 * <BR>PBL_ERROR_OUT_OF_MEMORY       - Out of memory.
 * <BR>PBL_ERROR_PARAM_COLLECTION    - The collection cannot be iterated.
 */
PblIterator * pblIteratorNew(
PblCollection * collection     /** The collection to create the iterator for */
)
{
    PblIterator * iterator;

    if( !PBL_COLLECTION_IS_COLLECTION( collection ) )
    {
        pbl_errno = PBL_ERROR_PARAM_COLLECTION;
        return NULL;
    }

    iterator = (PblIterator *)pbl_malloc( "pblIteratorNew", sizeof(PblIterator) );
    if( !iterator )
    {
        return NULL;
    }

    if( pblIteratorInit( collection, iterator ) < 0 )
    {
        PBL_FREE( iterator );
        return NULL;
    }

    return (PblIterator *)iterator;
}

/*
 * Initializes an iterator over the elements in this collection in proper sequence.
 *
 * The iterator starts the iteration at the beginning of the collection.
 *
 * The iterators returned by the this method are fail-fast:
 * if the collection is structurally modified at any time after the iterator is created,
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
 * @return int rc == 0: Ok, the iterator is initialized.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_COLLECTION    - The collection cannot be iterated.
 */
int pblIteratorInit(
PblCollection * collection,    /** The collection to create the iterator for */
PblIterator   * iterator       /** The iterator to initialize                */
)
{
    if( !PBL_COLLECTION_IS_COLLECTION( collection ) )
    {
        pbl_errno = PBL_ERROR_PARAM_COLLECTION;
        return -1;
    }

    iterator->magic = PblIteratorMagic;
    iterator->collection = collection;
    iterator->index = 0;
    iterator->lastIndexReturned = -1;
    iterator->changeCounter = collection->changeCounter;

    iterator->current = NULL;
    iterator->prev = NULL;

    if( PBL_SET_IS_HASH_SET( collection ) )
    {
        PblHashIterator * hashIterator = (PblHashIterator *)iterator;
        hashIterator->next = pblHashElementFirst( (PblHashSet*)collection );
    }
    else if( PBL_SET_IS_TREE_SET( collection ) )
    {
        PblTreeIterator * treeIterator = (PblTreeIterator *)iterator;
        PblTreeSet * treeSet = (PblTreeSet*)collection;
        treeIterator->next
                = treeSet->rootNode ? pblTreeNodeFirst( treeSet->rootNode )
                        : NULL;
    }
    else if( PBL_LIST_IS_LINKED_LIST( collection ) )
    {
        PblLinkedList * linkedList = (PblLinkedList*)collection;
        iterator->next = linkedList->head;
    }
    else
    {
        iterator->next = NULL;
    }

    return 0;
}

/**
 * Returns a reverse iterator over the elements in this collection in proper sequence.
 *
 * The reverse iterator starts the iteration at the end of the collection.
 *
 * <B>Note:</B> The memory allocated by this method for the iterator returned needs to be released
 *       by calling pblIteratorFree() once the iterator is no longer needed.
 *
 * The iterators returned by the this method are fail-fast:
 * if the collection is structurally modified at any time after the iterator is created,
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
 * <BR>PBL_ERROR_OUT_OF_MEMORY      - Out of memory.
 * <BR>PBL_COLLECTION_IS_COLLECTION - The collection cannot be iterated.
 */
PblIterator * pblIteratorReverseNew(
PblCollection * collection          /** The collection to create the iterator for */
)
{
    PblIterator * iterator;

    if( !PBL_COLLECTION_IS_COLLECTION( collection ) )
    {
        pbl_errno = PBL_ERROR_PARAM_COLLECTION;
        return NULL;
    }

    iterator = (PblIterator *)pbl_malloc( "pblIteratorReverseNew", sizeof(PblIterator) );
    if( !iterator )
    {
        return NULL;
    }

    if( pblIteratorReverseInit( collection, iterator ) < 0 )
    {
        PBL_FREE( iterator );
        return NULL;
    }

    return (PblIterator *)iterator;
}

/*
 * Initializes a reverse iterator over the elements in this collection in proper sequence.
 *
 * The reverse iterator starts the iteration at the end of the collection.
 *
 * <B>Note:</B> The memory allocated by this method for the iterator returned needs to be released
 *       by calling pblIteratorFree() once the iterator is no longer needed.
 *
 * The iterators returned by the this method are fail-fast:
 * if the collection is structurally modified at any time after the iterator is created,
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
 * @return int rc == 0: Ok, the iterator is initialized.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_COLLECTION_IS_COLLECTION - The collection cannot be iterated.
 */
int pblIteratorReverseInit(
PblCollection * collection,          /** The collection to create the iterator for */
PblIterator   * iterator             /** The iterator to initialize                */
)
{
    if( !PBL_COLLECTION_IS_COLLECTION( collection ) )
    {
        pbl_errno = PBL_ERROR_PARAM_LIST;
        return -1;
    }

    iterator->magic = PblIteratorMagic;
    iterator->collection = collection;
    iterator->index = collection->size;
    iterator->lastIndexReturned = -1;
    iterator->changeCounter = collection->changeCounter;

    iterator->current = NULL;
    iterator->next = NULL;

    if( PBL_SET_IS_HASH_SET( collection ) )
    {
        PblHashIterator * hashIterator = (PblHashIterator *)iterator;
        hashIterator->prev = pblHashElementLast( (PblHashSet*)collection );
    }
    else if( PBL_SET_IS_TREE_SET( collection ) )
    {
        PblTreeIterator * treeIterator = (PblTreeIterator *)iterator;
        PblTreeSet * treeSet = (PblTreeSet*)collection;
        treeIterator->prev
                = treeSet->rootNode ? pblTreeNodeLast( treeSet->rootNode )
                        : NULL;
    }
    else if( PBL_LIST_IS_LINKED_LIST( collection ) )
    {
        PblLinkedList * linkedList = (PblLinkedList*)collection;
        iterator->prev = linkedList->tail;
    }
    else
    {
        iterator->prev = NULL;
    }

    return 0;
}

/**
 * Returns true if this iterator has more elements when traversing the collection in the reverse direction.
 *
 * In other words, returns a value > 0 if pblIteratorPrevious would return
 * an element rather than returning (void*)-1.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc >  0: The iterator has more elements.
 * @return int rc == 0: The iteration has no more elements.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The underlying collection was modified concurrently.
 *
 */
int pblIteratorHasPrevious(
PblIterator * iterator /** The iterator to check the previous element for */
)
{
    if( iterator->changeCounter != iterator->collection->changeCounter )
    {
        pbl_errno = PBL_ERROR_CONCURRENT_MODIFICATION;
        return -1;
    }

    if( iterator->index > 0 && iterator->index <= iterator->collection->size )
    {
        return 1;
    }
    return 0;
}

/**
 * Returns true if this iterator has more elements.
 *
 * In other words, returns a value > 0 if pblIteratorNext would return
 * an element rather than returning (void*)-1.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc >  0: The iterator has more elements.
 * @return int rc == 0: The iteration has no more elements.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The underlying collection was modified concurrently.
 */
int pblIteratorHasNext(
PblIterator * iterator /** The iterator to check the next element for */
)
{
    if( iterator->changeCounter != iterator->collection->changeCounter )
    {
        pbl_errno = PBL_ERROR_CONCURRENT_MODIFICATION;
        return -1;
    }

    if( iterator->index >= 0 && iterator->index < iterator->collection->size )
    {
        return 1;
    }
    return 0;
}

/**
 * Returns the next element in the iteration.
 *
 * Calling this method repeatedly until the hasNext()
 * method returns false will return each element
 * in the underlying collection exactly once.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The next element in the iteration.
 *                                     May be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_NOT_FOUND               - The iteration has no more elements.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The underlying collection was modified concurrently.
 */
void * pblIteratorNext(
PblIterator * iterator /** The iterator to return the next element for */
)
{
    void * element;
    int hasNext = pblIteratorHasNext( iterator );
    if( hasNext < 0 )
    {
        return (void*)-1;
    }

    if( !hasNext )
    {
        pbl_errno = PBL_ERROR_NOT_FOUND;
        return (void*)-1;
    }

    if( PBL_SET_IS_HASH_SET( iterator->collection ) )
    {
        PblHashIterator * hashIterator = (PblHashIterator *)iterator;

        if( !hashIterator->next )
        {
            pbl_errno = PBL_ERROR_NOT_FOUND;
            return (void*)-1;
        }

        hashIterator->current = hashIterator->next;
        hashIterator->prev = hashIterator->next;
        hashIterator->next
                = pblHashElementNext( (PblHashSet *)hashIterator->collection,
                                      hashIterator->next );

        element = *( hashIterator->current );
    }
    else if( PBL_SET_IS_TREE_SET( iterator->collection ) )
    {
        PblTreeIterator * treeIterator = (PblTreeIterator *)iterator;

        if( !treeIterator->next )
        {
            pbl_errno = PBL_ERROR_NOT_FOUND;
            return (void*)-1;
        }

        treeIterator->current = treeIterator->next;
        treeIterator->prev = treeIterator->next;
        treeIterator->next = pblTreeNodeNext( treeIterator->next );

        element = treeIterator->current->element;
    }
    else if( PBL_LIST_IS_LINKED_LIST( iterator->collection ) )
    {
        if( !iterator->next )
        {
            pbl_errno = PBL_ERROR_NOT_FOUND;
            return (void*)-1;
        }

        iterator->current = iterator->next;
        iterator->prev = iterator->next;
        iterator->next = iterator->next->next;

        element = iterator->current->element;
    }
    else
    {
        element = pblListGet( (PblList*)iterator->collection, iterator->index );
        if( element == (void*)-1 )
        {
            pbl_errno = PBL_ERROR_NOT_FOUND;
            return (void*)-1;
        }
    }

    iterator->lastIndexReturned = iterator->index;
    iterator->index++;

    return element;
}

/**
 * Returns the previous element in the iteration.
 *
 * This method may be called repeatedly to iterate through the list backwards,
 * or intermixed with calls to next to go back and forth.
 * (Note that alternating calls to next and previous will return the same element repeatedly.)
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The previous element in the iteration.
 *                                     May be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_NOT_FOUND               - The iteration has no more elements.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The underlying collection was modified concurrently.
 */
void * pblIteratorPrevious(
PblIterator * iterator /** The iterator to return the previous element for */
)
{
    void * element;
    int hasPrevious = pblIteratorHasPrevious( iterator );
    if( hasPrevious < 0 )
    {
        return (void*)-1;
    }
    if( !hasPrevious )
    {
        pbl_errno = PBL_ERROR_NOT_FOUND;
        return (void*)-1;
    }

    if( PBL_SET_IS_HASH_SET( iterator->collection ) )
    {
        PblHashIterator * hashIterator = (PblHashIterator *)iterator;

        if( !hashIterator->prev )
        {
            pbl_errno = PBL_ERROR_NOT_FOUND;
            return (void*)-1;
        }

        hashIterator->current = hashIterator->prev;
        hashIterator->next = hashIterator->prev;
        hashIterator->prev
                = pblHashElementPrevious(
                                          (PblHashSet *)hashIterator->collection,
                                          hashIterator->prev );

        element = *( hashIterator->current );
    }
    else if( PBL_SET_IS_TREE_SET( iterator->collection ) )
    {
        PblTreeIterator * treeIterator = (PblTreeIterator *)iterator;

        if( !treeIterator->prev )
        {
            pbl_errno = PBL_ERROR_NOT_FOUND;
            return (void*)-1;
        }

        treeIterator->current = treeIterator->prev;
        treeIterator->next = treeIterator->prev;
        treeIterator->prev = pblTreeNodePrevious( treeIterator->prev );

        element = treeIterator->current->element;
    }
    else if( PBL_LIST_IS_LINKED_LIST( iterator->collection ) )
    {
        if( !iterator->prev )
        {
            pbl_errno = PBL_ERROR_NOT_FOUND;
            return (void*)-1;
        }

        iterator->current = iterator->prev;
        iterator->next = iterator->prev;
        iterator->prev = iterator->prev->prev;

        element = iterator->current->element;
    }
    else
    {
        element = pblListGet( (PblList*)iterator->collection, iterator->index
                - 1 );

        if( element == (void*)-1 )
        {
            pbl_errno = PBL_ERROR_NOT_FOUND;
            return (void*)-1;
        }
    }

    iterator->index--;
    iterator->lastIndexReturned = iterator->index;

    return element;
}

/**
 * Inserts the specified element into the underlying collection.
 *
 * The element is inserted immediately before the next element that would be returned by next,
 * if any, and after the next element that would be returned by previous, if any.
 *
 * If the list contains no elements, the new element becomes the sole element on the list.
 *
 * The new element is inserted before the implicit cursor:
 * a subsequent call to next would be unaffected,
 * and a subsequent call to previous would return the new element.
 * This call increases by one the value that would be returned by a
 * call to nextIndex or previousIndex.
 *
 * For array lists this method has a time complexity of O(N),
 * with N being the number of elements in the underlying list.
 *
 * For linked lists this method has a time complexity of O(1).
 *
 * @return int rc >= 0: The size of the list.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The underlying list was modified concurrently.
 * <BR>PBL_ERROR_PARAM_LIST              - The underlying collection is not a list.
 */
int pblIteratorAdd(
PblIterator * iterator, /** The iterator to add the element to */
void * element          /** Element to be added to this list   */
)
{
    PblList * list = (PblList*)iterator->collection;

    if( !PBL_LIST_IS_LIST( list ) )
    {
        pbl_errno = PBL_ERROR_PARAM_LIST;
        return -1;
    }

    if( iterator->changeCounter != list->changeCounter )
    {
        pbl_errno = PBL_ERROR_CONCURRENT_MODIFICATION;
        return -1;
    }

    if( list->size == 0 )
    {
        if( pblListAdd( (PblList*)list, element ) < 1 )
        {
            return -1;
        }

        iterator->index = 1;
        iterator->next = NULL;

        iterator->lastIndexReturned = -1;
        iterator->current = NULL;

        if( PBL_LIST_IS_LINKED_LIST( list ) )
        {
            PblLinkedList * linkedList = (PblLinkedList*)list;
            iterator->prev = linkedList->tail;
        }
        iterator->changeCounter = list->changeCounter;
        return 1;
    }

    if( PBL_LIST_IS_ARRAY_LIST( list ) )
    {
        int rc = pblListAddAt( (PblList*)list, iterator->index, element );
        if( rc < 0 )
        {
            return -1;
        }
    }
    else
    {
        PblLinkedList * linkedList = (PblLinkedList*)list;
        PblLinkedNode * newNode = (PblLinkedNode *)pbl_malloc( "pblIteratorAdd",
                                              sizeof(PblLinkedNode) );
        if( !newNode )
        {
            return -1;
        }
        newNode->element = element;

        if( !iterator->next )
        {
            PBL_LIST_APPEND( linkedList->head, linkedList->tail, newNode, next, prev );
        }
        else
        {
            PBL_LIST_INSERT( linkedList->head, iterator->next, newNode, next, prev );
        }
        linkedList->genericList.size++;
        linkedList->genericList.changeCounter++;

        iterator->prev = newNode;
    }

    iterator->lastIndexReturned = -1;
    iterator->current = NULL;

    iterator->index++;

    iterator->changeCounter = list->changeCounter;
    return list->size;
}

/**
 * Removes from the underlying list or tree set the last element returned by the iterator.
 *
 * This method can be called only once per call to next or previous.
 * It can be made only if pblIteratorAdd() has not been called after the last call to next or previous.
 *
 * For array lists this method has a time complexity of O(N),
 * with N being the number of elements in the underlying list.
 *
 * For linked lists and hash sets this method has a time complexity of O(1).
 *
 * For tree sets this method has a time complexity of O(Log N),
 * with N being the number of elements in the underlying collection.
 *
 * @return int rc >= 0: The size of the collection.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_NOT_ALLOWED - The the next or previous method has not yet been called,
 *                         or the remove method has already been called after the last call to the next or previous method.
 *
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The underlying list or tree set was modified concurrently.
 * <BR>PBL_ERROR_PARAM_LIST              - The underlying collection is neither a list nor a tree set.
 */
int pblIteratorRemove(
PblIterator * iterator /** The iterator to remove the next element from */
)
{
    if( iterator->changeCounter != iterator->collection->changeCounter )
    {
        pbl_errno = PBL_ERROR_CONCURRENT_MODIFICATION;
        return -1;
    }

    if( PBL_SET_IS_TREE_SET( iterator->collection ) )
    {
        PblTreeIterator * treeIterator = (PblTreeIterator *)iterator;

        if( !treeIterator->current )
        {
            pbl_errno = PBL_ERROR_NOT_ALLOWED;
            return -1;
        }
        else
        {
            if( treeIterator->next == treeIterator->current )
            {
                treeIterator->next = pblTreeNodeNext( treeIterator->next );
            }
            else if( treeIterator->prev == treeIterator->current )
            {
                treeIterator->prev = pblTreeNodePrevious( treeIterator->prev );
            }

            pblSetRemoveElement( (PblSet*)treeIterator->collection,
                                 treeIterator->current->element );
        }
    }
    else if( PBL_LIST_IS_LINKED_LIST( iterator->collection ) )
    {
        if( !iterator->current )
        {
            pbl_errno = PBL_ERROR_NOT_ALLOWED;
            return -1;
        }
        else
        {
            PblLinkedList * linkedList = (PblLinkedList *)iterator->collection;
            PblLinkedNode * nodeToFree = iterator->current;

            if( iterator->next == iterator->current )
            {
                iterator->next = iterator->next->next;
            }
            else if( iterator->prev == iterator->current )
            {
                iterator->prev = iterator->prev->prev;
            }

            PBL_LIST_UNLINK( linkedList->head, linkedList->tail, nodeToFree, next, prev );
            linkedList->genericList.size--;
            linkedList->genericList.changeCounter++;

            PBL_FREE( nodeToFree );
        }
    }
    else if( PBL_LIST_IS_ARRAY_LIST( iterator->collection ) )
    {
        if( iterator->lastIndexReturned < 0 )
        {
            pbl_errno = PBL_ERROR_NOT_ALLOWED;
            return -1;
        }

        if( pblArrayListRemoveAt( (PblArrayList*)iterator->collection,
                                  iterator->lastIndexReturned ) == (void*)-1 )
        {
            return -1;
        }
    }
    else
    {
        pbl_errno = PBL_ERROR_PARAM_LIST;
        return -1;
    }

    if( iterator->lastIndexReturned < iterator->index )
    {
        iterator->index--;
    }

    iterator->current = NULL;
    iterator->lastIndexReturned = -1;

    iterator->changeCounter = iterator->collection->changeCounter;

    return iterator->collection->size;
}

/**
 * Replaces in the underlying list the last element returned by next or previous with the specified element.
 *
 * This call can be made only if neither pblIteratorRemove() nor pblIteratorAdd() have
 * been called after the last call to next or previous.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr != (void*)-1: The element replaced, may be NULL.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_NOT_ALLOWED     - Neither the next nor previous have been called,
 *                             or remove or add have been called after the last
 *                             call to next or previous.
 *
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The underlying list was modified concurrently.
 * <BR>PBL_ERROR_PARAM_LIST              - The underlying collection is not a list.
 */
void * pblIteratorSet(
PblIterator * iterator, /** The iterator to replace the element of. */
void * element          /** Element with which to replace the last element returned by next or previous. */
)
{
    void * retptr = (void*)-1;

    if( !PBL_LIST_IS_LIST( iterator->collection ) )
    {
        pbl_errno = PBL_ERROR_PARAM_LIST;
        return retptr;
    }

    if( iterator->changeCounter != iterator->collection->changeCounter )
    {
        pbl_errno = PBL_ERROR_CONCURRENT_MODIFICATION;
        return retptr;
    }

    if( PBL_LIST_IS_LINKED_LIST( iterator->collection ) )
    {
        if( !iterator->current )
        {
            pbl_errno = PBL_ERROR_NOT_ALLOWED;
            return retptr;
        }
        else
        {
            retptr = iterator->current->element;
            iterator->current->element = element;
        }
    }
    else
    {
        if( iterator->lastIndexReturned < 0 )
        {
            pbl_errno = PBL_ERROR_NOT_ALLOWED;
            return retptr;
        }

        retptr = pblListSet( (PblList*)iterator->collection,
                             iterator->lastIndexReturned, element );
    }

    return retptr;
}

/**
 * Returns the index of the element that would be returned by a subsequent call to next.
 *
 * Returns list size if the list iterator is at the end of the list.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc: The index of the element that would be returned by a subsequent call to next,
 *                 or list size if list iterator is at end of list.
 */
int pblIteratorNextIndex(
PblIterator * iterator /** The iterator to use */
)
{
    return iterator->index;
}

/**
 * Returns the index of the element that would be returned by a subsequent call to previous.
 *
 * Returns -1 if the list iterator is at the beginning of the list.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc: The index of the element that would be returned by a subsequent call to previous,
 *                 or -1 if list iterator is at beginning of list.
 */
int pblIteratorPreviousIndex(
PblIterator * iterator /** The iterator to use */
)
{
    return iterator->index - 1;
}

/**
 * Returns the number of elements in the underlying collection of the iterator.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc: The number of elements in the collection.
 */
int pblIteratorSize(
PblIterator * iterator /** The iterator to use */
)
{
    return iterator->collection->size;
}

/**
 * Frees the memory used by the iterator.
 *
 * This method has a time complexity of O(1).
 *
 * Must be called once the iterator is no longer needed.
 */
void pblIteratorFree(
PblIterator * iterator /** The iterator to free */
)
{
    PBL_FREE(iterator);
}




