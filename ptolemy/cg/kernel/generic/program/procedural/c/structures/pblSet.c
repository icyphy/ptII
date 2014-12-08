/*
 pblSet.c - C implementation of two Sets similar
            to the Java HashSet and Java TreeSet.

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

 $Log: pblSet.c,v $
 Revision 1.30  2010/08/20 20:10:25  peter
 Implemented the priority queue functions.

 Revision 1.28  2010/05/30 20:06:45  peter
 Removed warnings found by 'Microsoft Visual C++ 2010'.

 Revision 1.27  2010/05/20 21:42:53  peter
 Added pblSetReplace.

 Revision 1.26  2010/05/15 16:26:10  peter
 Exposing the map interface.

 Revision 1.25  2009/11/26 14:42:23  peter
 Some cleanup during C# port of Avl tree.

 Revision 1.24  2009/09/03 20:48:59  peter
 Just some cleanup on sets.

 Revision 1.23  2009/03/11 23:48:44  peter
 More tests and clean up.

 Revision 1.22  2009/03/08 20:56:50  peter
 port to gcc (Ubuntu 4.3.2-1ubuntu12) 4.3.2.
 Exposing the hash set and tree set interfaces.

 */

/*
 * Make sure "strings <exe> | grep Id | sort -u" shows the source file versions
 */
char* pblSet_c_id = "$Id$";

char * PblHashSetMagic = "PblHashSetMagic";
char * PblTreeSetMagic = "PblTreeSetMagic";


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
/* #defines                                                                  */
/*****************************************************************************/

/*
 * Linear probing is used for resolving hash collisions, the
 * default step size used for small sets
 */
#define _PBL_STEP_SIZE   3

/*
 * Macros for setting node pointers and maintaining the parent pointer
 */
#define PBL_AVL_TREE_SET_PREV( node, referenceNode )\
{\
    if( node->prev != referenceNode )\
        if(( node->prev = referenceNode )){ node->prev->parent = node; }\
}

#define PBL_AVL_TREE_SET_NEXT( node, referenceNode )\
{\
    if( node->next != referenceNode )\
        if(( node->next = referenceNode )){ node->next->parent = node; }\
}

/*****************************************************************************/
/* Globals                                                                   */
/*****************************************************************************/

/*
 * Step sizes used for the different capacities, the smallest prime numbers
 * that are bigger than the powers of two.
 */
static int pblPrimeStepSize[] =
{
        5, 11, 17, 37,
        67, 131, 257, 521,
        1031, 2053, 4099, 8209,
        16411, 32771
#ifndef __AVR__
        , 65537, 131101,
        262147, 524309, 1048583, 2097169,
        4194319, 8388617, 16777259, 33554467,
        67108879, 134217757
#endif
};

/*
 * Capacities used for the hash set, the powers of two.
 */
static int pblCapacities[] =
{       0x8, 0x10, 0x20, 0x40,
        0x80, 0x100, 0x200, 0x400,
        0x800, 0x1000, 0x2000, 0x4000,
        0x8000, 0x10000
#ifndef __AVR__
, 0x20000, 0x40000,
        0x80000, 0x100000, 0x200000, 0x400000,
        0x800000, 0x1000000, 0x2000000, 0x4000000,
        0x8000000, 0x10000000, 0x20000000, 0x40000000
#endif
};

/*****************************************************************************/
/* Function declarations                                                     */
/*****************************************************************************/
static int pblHashSetAdd(
PblHashSet * set,            /** The set to use                              */
void * element               /** Element to be appended to this set          */
);

static int pblHashSetRemoveElement(
PblHashSet * set,     /** The set to use                        */
void * element        /** Element to remove                     */
);

static int pblTreeSetRemoveElement(
PblTreeSet * set,     /** The set to use                        */
void * element        /** Element to remove                     */
);

static void pblTreeNodeFree(
PblTreeNode * node                /** The node to free */
);

/*****************************************************************************/
/* Functions                                                                 */
/*****************************************************************************/

/**
 * Sets an application specific compare function for the elements
 * of the set.
 *
 * An application specific compare function can be set to the set
 * only if the set is empty.
 *
 * If no specific hash value function is specified by the user,
 * the default hash value function \Ref{pblSetDefaultHashValue} is used.
 * If no specific compare function is specified by the user,
 * the default compare function is used.
 * The default compare function compares the two pointers directly,
 * i.e. it tests for object identity.
 *
 * The compare function specified should behave like the one that
 * can be specified for the C-library function 'qsort'.
 *
 * The arguments actually passed to the compare function when it is called
 * are addresses of the element pointers added to the set.
 * E.g.: If you add char * pointers to the set, the compare function
 * will be called with char ** pointers as arguments. See the documentation
 * for the C-library function 'qsort' for further information.
 *
 * This method has a time complexity of O(1).
 *
 * @return * retptr != (void*)-1: The compare function used before, may be NULL.
 * @return * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_SET - The set is not empty.
 */
void * pblSetSetCompareFunction(
PblSet * set,                /** The set to set compare function for   */
int ( *compare )             /** compare function to set               */
    (
        const void* prev,    /** "left" element for compare            */
        const void* next     /** "right" element for compare           */
    )
)
{
    void * retptr = (void*)set->compare;

    if( set->size > 0 )
    {
        pbl_errno = PBL_ERROR_PARAM_SET;
        return (void*)-1;
    }

    set->compare = compare;

    return retptr;
}

/**
 * Gets the application specific compare function for the elements
 * of the set.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr: The compare function used, may be NULL.
 */
void * pblSetGetCompareFunction(
PblSet * set         /** The set to get the compare function for   */
)
{
    return (void*)set->compare;
}

/**
 * Default hash value function used if no application specific function
 * is specified by the user.
 *
 * This computes the hash value from the value of the pointer given,
 * i.e. it tests for object identity.
 *
 * @return int rc: The hash value of the pointer that was passed.
 */
int pblSetDefaultHashValue(
const void *element     /** Element to calculate hash value for */
)
{
    return pblHtHashValue( (unsigned char *)&element, sizeof(void*) );
}

/**
 * Creates a hash value of byte buffer and its length.
 *
 * @return int rc: The hash value of the buffer that was passed.
 */
int pblSetByteBufferHashValue(
const void *buffer,     /** The byte buffer to calculate the hash value for */
const size_t length     /** The length of the byte buffer to hash           */
)
{
    return pblHtHashValue( (unsigned char *)buffer, length );
}

/**
 * Creates a hash value of a '\0' terminated string.
 *
 * Can be used as hash value function for lists in case '\0'
 * terminated strings are inserted into the set.
 *
 * @return int rc: The hash value of string that was passed.
 */
int pblSetStringHashValue(
const void *string     /** The '\0' terminated string to calculate the hash value for */
)
{
    return pblHtHashValueOfString( (unsigned char *)string );
}

/**
 * Sets an application specific hash value function for the elements
 * of the hash set. For tree sets this function does nothing.
 *
 * An application specific hash value function can be set to the set
 * only if the set is empty.
 *
 * The hash function specified should be based on the hash functions
 * supplied by the library:
 * \Ref{pblSetByteBufferHashValue} and \Ref{pblSetStringHashValue}.
 *
 * If no specific hash value function is specified by the user,
 * the default hash value function \Ref{pblSetDefaultHashValue} is used.
 *
 * This method has a time complexity of O(1).
 *
 * @return * retptr != (void*)-1: The hash value function used before.
 * @return * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_SET - The set is not empty.
 */
void * pblSetSetHashValueFunction(
PblSet * set,                /** The set to set hash value function for */
int ( *hashValue )           /** The hash value function to set         */
    (
        const void* element  /** The element to get the hash value for  */
    )
)
{
    void * retptr;

    if( !PBL_SET_IS_HASH_SET( set ) )
    {
        return NULL;
    }

    if( set->size > 0 )
    {
        pbl_errno = PBL_ERROR_PARAM_SET;
        return (void*)-1;
    }

    retptr = (void*)( (PblHashSet*)set )->hashValue;
    ( (PblHashSet*)set )->hashValue = hashValue;

    return retptr;
}

/**
 * Sets an application specific load factor for a hash set.
 * For tree sets this function does nothing.
 *
 * The load factor has to be between 0.1 and 0.9,
 * the default load factor used ins 0.75.
 *
 * This method has a time complexity of O(1).
 *
 * @return double factor: The load factor used before.
 */
double pblSetSetLoadFactor(
PblSet * set,                /** The set to set hash value function for */
double loadFactor            /** The load factor to set                 */
)
{
    double factor;

    if( !PBL_SET_IS_HASH_SET( set ) )
    {
        return 0.0;
    }

    factor = ( (PblHashSet*)set )->loadFactor;

    if( loadFactor >= 0.1 && loadFactor <= 0.9 )
    {
        ( (PblHashSet*)set )->loadFactor = loadFactor;
    }

    return factor;
}

/**
 * Gets the application specific hash value function for the elements
 * of the set.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * retptr: The hash value function used, may be NULL.
 */
void * pblSetGetHashValueFunction(
PblSet * set         /** The set to get the hash value function for */
)
{
    if( !PBL_SET_IS_HASH_SET( set ) )
    {
        return NULL;
    }

    return (void*)( (PblHashSet*)set )->hashValue;
}

/**
 * Creates a new tree set.
 *
 * This method has a time complexity of O(1).
 *
 * @return pblSet * retPtr != NULL: A pointer to the new set.
 * @return pblSet * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
PblSet * pblSetNewTreeSet( void )
{
    PblTreeSet * pblSet = (PblTreeSet *)pbl_malloc0( "pblSetNewTreeSet", sizeof(PblTreeSet) );
    if( !pblSet )
    {
        return NULL;
    }

    pblSet->genericSet.magic = PblTreeSetMagic;

    return (PblSet *)pblSet;
}

/**
 * Creates a new hash set.
 *
 * This method has a time complexity of O(1).
 *
 * @return PblSet * retPtr != NULL: A pointer to the new set.
 * @return PblSet * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
PblSet * pblSetNewHashSet( void )
{
    PblHashSet * pblSet = (PblHashSet *)pbl_malloc0( "pblSetNewHashSet", sizeof(PblHashSet) );
    if( !pblSet )
    {
        return NULL;
    }

    pblSet->genericSet.magic = PblHashSetMagic;
    pblSet->hashValue = pblSetDefaultHashValue;
    pblSet->loadFactor = 0.75;
    pblSet->stepSize = _PBL_STEP_SIZE;

    return (PblSet *)pblSet;
}

/*
 * Clones a tree node.
 * Uses recursion to clone all child nodes.
 *
 * @return PblTreeNode * retPtr != Null: The new node.
 * @return PblTreeNode * retPtr == Null: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
static PblTreeNode * pblTreeNodeClone( PblTreeNode * node )
{
    PblTreeNode * newNode = (PblTreeNode *)pbl_malloc0( "pblTreeNodeClone",
                                         sizeof(PblTreeNode) );
    if( !newNode )
    {
        return newNode;
    }

    newNode->element = node->element;
    newNode->balance = node->balance;

    if( node->prev )
    {
        PblTreeNode * clone = pblTreeNodeClone( node->prev );
        if( !clone )
        {
            pblTreeNodeFree( newNode );
            return NULL;
        }
        PBL_AVL_TREE_SET_PREV( newNode, clone );
    }

    if( node->next )
    {
        PblTreeNode * clone = pblTreeNodeClone( node->next );
        if( !clone )
        {
            pblTreeNodeFree( newNode );
            return NULL;
        }
        PBL_AVL_TREE_SET_NEXT( newNode, clone );
    }

    return newNode;
}

/*
 * Returns a shallow copy of this hash set instance.
 *
 * The elements themselves are not copied.
 *
 * @return PblSet * retPtr != NULL: A pointer to the new set.
 * @return PblSet * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY  - Out of memory.
 */
static PblSet * pblHashSetClone( PblHashSet * set )
{
    PblHashSet * newSet = (PblHashSet *)pblSetNewHashSet();
    if( !newSet )
    {
        return NULL;
    }

    newSet->hashValue = set->hashValue;
    newSet->loadFactor = set->loadFactor;
    newSet->genericSet.compare = set->genericSet.compare;

    if( set->genericSet.size < 1 )
    {
        return (PblSet*)newSet;
    }

    /*
     * Low level copy of references
     */
    newSet->pointerArray = pbl_memdup( "pblHashSetClone pointer buffer",
                                       &( set->pointerArray[ 0 ] ),
                                       sizeof(void*) * set->capacity );
    if( !newSet->pointerArray )
    {
        PBL_FREE( newSet );
        return NULL;
    }

    newSet->capacity = set->capacity;
    newSet->stepSize = set->stepSize;
    newSet->genericSet.size = set->genericSet.size;

    return (PblSet*)newSet;
}

/*
 * Returns the first node in the tree defined by the node given as parameter.
 *
 * @return PblTreeNode * node: The first node in the sub tree.
 */
PblTreeNode * pblTreeNodeFirst(
PblTreeNode * node                /** The node to use */
)
{
    while( node->prev )
    {
        node = node->prev;
    }
    return node;
}

/*
 * Returns the next node after the node given as parameter.
 *
 * @return PblTreeNode * node: The next node, may be NULL.
 */
PblTreeNode * pblTreeNodeNext(
PblTreeNode * node                /** The node to use */
)
{
    if( node->next )
    {
        return pblTreeNodeFirst( node->next );
    }
    else
    {
        PblTreeNode * child = node;

        while( ( node = node->parent ) )
        {
            if( child == node->prev )
            {
                return node;
            }
            child = node;
        }
    }

    return NULL;
}

/*
 * Returns the last node in the tree defined by the node given as parameter.
 *
 * @return PblTreeNode * node: The last node in the sub tree.
 */
PblTreeNode * pblTreeNodeLast(
PblTreeNode * node                /** The node to use */
)
{
    while( node->next )
    {
        node = node->next;
    }
    return node;
}

/*
 * Returns the previous node before the node given as parameter.
 *
 * @return PblTreeNode * node: The previous node, may be NULL.
 */
PblTreeNode * pblTreeNodePrevious(
PblTreeNode * node                /** The node to use */
)
{
    if( node->prev )
    {
        return pblTreeNodeLast( node->prev );
    }
    else
    {
        PblTreeNode * child = node;

        while( ( node = node->parent ) )
        {
            if( child == node->next )
            {
                return node;
            }
            child = node;
        }
    }

    return NULL;
}

/*
 * Free the node's memory from heap. Recursively going through
 * the sub trees.
 *
 * <B>Note:</B> The memory of the elements themselves is not freed.
 *
 * @return void
 */
static void pblTreeNodeFree(
PblTreeNode * node                /** The node to free */
)
{
    if( node->prev )
    {
        pblTreeNodeFree( node->prev);
    }

    if( node->next )
    {
        pblTreeNodeFree( node->next);
    }

    PBL_FREE( node );
}


/*
 * Free the tree set's memory from heap.
 *
 * <B>Note:</B> The memory of the elements themselves is not freed.
 *
 * This method has a time complexity of O(N).
 *
 * @return void
 */
static void pblTreeSetFree(
PblTreeSet * set                /** The set to free */
)
{
    if( set->rootNode )
    {
        pblTreeNodeFree( set->rootNode );
    }

    PBL_FREE( set );
}

/*
 * Returns a shallow copy of this tree set instance.
 *
 * The elements themselves are not copied.
 *
 * @return PblSet * retPtr != NULL: A pointer to the new set.
 * @return PblSet * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY  - Out of memory.
 */
static PblSet * pblTreeSetClone( PblTreeSet * set )
{
    PblTreeSet * newSet = (PblTreeSet *)pblSetNewTreeSet();
    if( !newSet )
    {
        return NULL;
    }

    newSet->genericSet.compare = set->genericSet.compare;

    if( set->rootNode )
    {
        PblTreeNode * clone = pblTreeNodeClone( set->rootNode );
        if( !clone )
        {
            pblTreeSetFree( newSet );
            return NULL;
        }
        newSet->rootNode = clone;
    }

    newSet->genericSet.size = set->genericSet.size;

    return (PblSet*)newSet;
}

/**
 * Returns a shallow copy of this set instance.
 *
 * The elements themselves are not copied.
 *
 * This method has a memory and time complexity of O(N),
 * with N being the number of elements in the set.
 *
 * @return PblSet * retPtr != NULL: A pointer to the new set.
 * @return PblSet * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY  - Out of memory.
 */
PblSet * pblSetClone(
PblSet * set           /** The set to clone */
)
{
    if( PBL_SET_IS_TREE_SET( set ) )
    {
        return pblTreeSetClone( (PblTreeSet *)set );
    }

    return pblHashSetClone( (PblHashSet *)set );
}

/**
 * Returns a shallow copy from this set of all of the elements
 * whose index is between fromIndex, inclusive and toIndex, exclusive.
 *
 * For hash sets cloning from the beginning or the end of the set has a time complexity
 * of O(M) with M being the number of elements cloned,
 * while cloning from a random position in the middle of the set has a time
 * complexity of O(N) with N being the number of elements in the set.
 *
 * For tree sets cloning from the beginning or the end of the set has a time complexity
 * of O(M * Log N),
 * while cloning from a random position in the middle of the set has a time
 * complexity of O(N * Log N) with N being the number of elements in the set
 * and with M being the number of elements cloned.
 *
 * This method has a memory complexity of O(M),
 * with M being the number of elements cloned.
 *
 * @return PblSet * retPtr != NULL: A pointer to the new set.
 * @return PblSet * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY  - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS  - fromIndex is out of range (fromIndex < 0 || fromIndex >= size())
 *                          or toIndex is out of range ( toIndex < 0 || toIndex > size()) or range is negative.
 */
PblSet * pblSetCloneRange(
PblSet * set,              /** The set to use                               */
int fromIndex,             /** The index of first element to be cloned.     */
int toIndex                /** The index after last element to be cloned.   */
)
{
    int elementsToClone;
    int distanceToEnd;
    int hasMore;
    PblSet * newSet;
    PblIterator   iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;

    if( fromIndex < 0 || fromIndex > set->size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return NULL;
    }

    if( toIndex < 0 || toIndex > set->size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return NULL;
    }

    elementsToClone = toIndex - fromIndex;
    if( elementsToClone < 0 )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return NULL;
    }

    if( elementsToClone > 0 && fromIndex == 0 && toIndex == set->size )
    {
        /*
         * pblSetClone is faster!
         */
        return pblSetClone( set );
    }

    if( PBL_SET_IS_TREE_SET( set ) )
    {
        newSet = pblSetNewTreeSet();
        if( !newSet )
        {
            return NULL;
        }
    }
    else
    {
        newSet = pblSetNewHashSet();
        if( !newSet )
        {
            return NULL;
        }
        ((PblHashSet*)newSet)->hashValue = ((PblHashSet*)set)->hashValue;
        ((PblHashSet*)newSet)->loadFactor = ((PblHashSet*)set)->loadFactor;
    }

    newSet->compare = set->compare;

    if( elementsToClone < 1 )
    {
        return (PblSet *)newSet;
    }

    distanceToEnd = set->size - toIndex;
    if( fromIndex <= distanceToEnd )
    {
        if( pblIteratorInit( set, iterator ) < 0 )
        {
            pblSetFree( newSet );
            return NULL;
        }

        while( ( hasMore = pblIteratorHasNext( iterator ) ) > 0 )
        {
            void * element = pblIteratorNext( iterator );
            if( element == (void*)-1 )
            {
                pblSetFree( newSet );
                return NULL;
            }

            /*
             * Skip the first elements
             */
            if( fromIndex > 0 )
            {
                fromIndex--;
                continue;
            }

            if( pblSetAdd( newSet, element ) < 0 )
            {
                pblSetFree( newSet );
                return NULL;
            }

            /*
             * Break if enough elements are copied
             */
            if( --elementsToClone == 0 )
            {
                break;
            }
        }
    }
    else
    {
        if( pblIteratorReverseInit( set, iterator ) < 0 )
        {
            pblSetFree( newSet );
            return NULL;
        }

        while( ( hasMore = pblIteratorHasPrevious( iterator ) ) > 0 )
        {
            void * element = pblIteratorPrevious( iterator );
            if( element == (void*)-1 )
            {
                pblSetFree( newSet );
                return NULL;
            }

            /*
             * Skip the last elements
             */
            if( distanceToEnd > 0 )
            {
                distanceToEnd--;
                continue;
            }

            if( pblSetAdd( newSet, element ) < 0 )
            {
                pblSetFree( newSet );
                return NULL;
            }

            /*
             * Break if enough elements are copied
             */
            if( --elementsToClone == 0 )
            {
                break;
            }
        }
    }

    if( hasMore < 0 )
    {
        // Concurrent modification error on the source set,
        //
        pblSetFree( newSet );
        return NULL;
    }

    return (PblSet *)newSet;
}

/**
 * Creates a new set containing the union of the elements of both
 * sets passed as parameters.
 *
 * This functions clones the larger of the two parameter sets
 * and then adds all elements from the smaller set to the clone.
 *
 * @return PblSet * retPtr != NULL: A pointer to the new set.
 * @return PblSet * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_PARAM_COLLECTION        - A set cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - A set was modified concurrently.
 */
PblSet * pblSetUnion(
PblSet * setA,     /** The first set to unite  */
PblSet * setB      /** The second set to unite */
)
{
    PblSet * unionSet;
    PblSet * smallerSet;

    if( pblSetSize( setA ) >= pblSetSize( setB ) )
    {
        unionSet = pblSetClone( setA );
        smallerSet = setB;
    }
    else
    {
        unionSet = pblSetClone( setB );
        smallerSet = setA;
    }

    if( !unionSet )
    {
        return NULL;
    }

    if( pblSetAddAll( unionSet, smallerSet ) < 0 )
    {
        pblSetFree( unionSet );
        return NULL;
    }

    return unionSet;
}

/**
 * Creates a new set containing the intersection of the elements of the two
 * sets passed as parameters.
 *
 * This function creates an empty clone of the smaller of the two
 * parameter sets and then iterates the smaller of the two sets
 * and adds all elements to the clone that are also contained in
 * the larger set.
 *
 * @return PblSet * retPtr != NULL: A pointer to the new set.
 * @return PblSet * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_PARAM_COLLECTION        - A set cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - A set was modified concurrently.
 */
PblSet * pblSetIntersection(
PblSet * setA,     /** The first set to intersect  */
PblSet * setB      /** The second set to intersect */
)
{
    PblSet * intersectionSet;
    PblSet * smallerSet;
    PblSet * largerSet;
    PblIterator iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int hasNext;

    if( pblSetSize( setA ) <= pblSetSize( setB ) )
    {
        smallerSet = setA;
        largerSet = setB;
    }
    else
    {
        smallerSet = setB;
        largerSet = setA;
    }

    intersectionSet = pblSetCloneRange( smallerSet, 0, 0 );
    if( !intersectionSet )
    {
        return NULL;
    }

    if( pblIteratorInit( smallerSet, iterator ) < 0 )
    {
        pblSetFree( intersectionSet );
        return NULL;
    }

    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        void * element = pblIteratorNext( iterator );
        if( element == (void*)-1 )
        {
            // concurrent modification on the collection
            //
            pblSetFree( intersectionSet );
            return NULL;
        }

        if( pblSetContains( largerSet, element ) )
        {
            if( pblSetAdd( intersectionSet, element ) < 0 )
            {
                pblSetFree( intersectionSet );
                return NULL;
            }
        }
    }
    if( hasNext < 0 )
    {
        // concurrent modification on the collection
        //
        pblSetFree( intersectionSet );
        return NULL;
    }

    return intersectionSet;
}

/**
 * Creates a new set containing the difference of the elements of the two
 * sets passed as parameters. The difference contains all elements that are
 * contained in the first set but are not contained in the second set.
 *
 * This function creates an empty clone of the first of the two
 * parameter sets and then iterates the first of the two sets
 * and adds all elements to the clone that are not contained in
 * the second set.
 *
 * @return PblSet * retPtr != NULL: A pointer to the new set.
 * @return PblSet * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_PARAM_COLLECTION        - A set cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - A set was modified concurrently.
 */
PblSet * pblSetDifference(
PblSet * setA,     /** The first set to build the difference from  */
PblSet * setB      /** The second set to build the difference from */
)
{
    PblSet * clone;
    PblIterator iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int hasNext;

    clone = pblSetCloneRange( setA, 0, 0 );
    if( !clone )
    {
        return NULL;
    }

    if( pblIteratorInit( setA, iterator ) < 0 )
    {
        pblSetFree( clone );
        return NULL;
    }

    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        void * element = pblIteratorNext( iterator );
        if( element == (void*)-1 )
        {
            // concurrent modification on the collection
            //
            pblSetFree( clone );
            return NULL;
        }

        if( !pblSetContains( setB, element ) )
        {
            if( pblSetAdd( clone, element ) < 0 )
            {
                pblSetFree( clone );
                return NULL;
            }
        }
    }
    if( hasNext < 0 )
    {
        // concurrent modification on the collection
        //
        pblSetFree( clone );
        return NULL;
    }

    return clone;
}

/**
 * Creates a new set containing all elements of the two
 * sets passed as parameters that are contained in either
 * of the sets but not in both of them.
 *
 * This function creates an empty clone of the first of the two
 * parameter sets and then iterates the first of the two sets
 * and adds all elements to the clone that are not contained in
 * the second set. Then it iterates the second of the two sets
 * and adds all elements to the clone that are not contained in
 * the first set.
 *
 * @return PblSet * retPtr != NULL: A pointer to the new set.
 * @return PblSet * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_PARAM_COLLECTION        - A set cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - A set was modified concurrently.
 */
PblSet * pblSetSymmectricDifference(
PblSet * setA,     /** The first set to use  */
PblSet * setB      /** The second set to use */
)
{
    PblSet * clone;
    PblIterator iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int hasNext;

    clone = pblSetCloneRange( setA, 0, 0 );
    if( !clone )
    {
        return NULL;
    }

    if( pblIteratorInit( setA, iterator ) < 0 )
    {
        pblSetFree( clone );
        return NULL;
    }

    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        void * element = pblIteratorNext( iterator );
        if( element == (void*)-1 )
        {
            // concurrent modification on the collection
            //
            pblSetFree( clone );
            return NULL;
        }

        if( !pblSetContains( setB, element ) )
        {
            if( pblSetAdd( clone, element ) < 0 )
            {
                pblSetFree( clone );
                return NULL;
            }
        }
    }
    if( hasNext < 0 )
    {
        // concurrent modification on the collection
        //
        pblSetFree( clone );
        return NULL;
    }

    if( pblIteratorInit( setB, iterator ) < 0 )
    {
        pblSetFree( clone );
        return NULL;
    }

    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        void * element = pblIteratorNext( iterator );
        if( element == (void*)-1 )
        {
            // concurrent modification on the collection
            //
            pblSetFree( clone );
            return NULL;
        }

        if( !pblSetContains( setA, element ) )
        {
            if( pblSetAdd( clone, element ) < 0 )
            {
                pblSetFree( clone );
                return NULL;
            }
        }
    }
    if( hasNext < 0 )
    {
        // concurrent modification on the collection
        //
        pblSetFree( clone );
        return NULL;
    }

    return clone;
}

/**
 * Returns a value > 0 if the set passed as second parameter is a subset
 * of the set passed as first parameter, i.e. the first set contains all of
 * the elements in the second set.
 *
 * This implementation iterates over the set passed as second parameter,
 * checking each element returned by the iterator in turn to see if it's contained
 * in the set passed as first parameter.
 *
 * If all elements are so contained a value > 0 is returned, otherwise 0.
 *
 * For hash sets this method has a time complexity of O(M) and
 * for tree sets this method has a time complexity of O((Log N) * M),
 * with N being the size of the first set and M being the size of the
 * second set.
 *
 * @return int rc >  0: The set contains all of the elements in the specified collection.
 * @return int rc == 0: The set does not contain all of the elements.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_COLLECTION        - The collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The collection was modified concurrently.
 */
int pblSetIsSubset(
    PblSet * setA,     /** Superset to check  */
    PblSet * setB      /** Subset to check    */
)
{
    PblIterator   iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int hasNext;

    if( setA == setB )
    {
        return 1;
    }

    if( setB->size == 0 )
    {
        return 1;
    }

    if( setA->size < setB->size )
    {
        return 0;
    }

    if( pblIteratorInit( setB, iterator ) < 0 )
    {
        return -1;
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
        if( !pblSetContains( setA, element ) )
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
 * Removes all of the elements from this tree set.
 *
 * <B>Note:</B> The memory of the elements themselves is not freed.
 *
 * This method has a time complexity of O(N),
 * with N being the number of elements in the set.
 *
 * @return void
 */
static void pblTreeSetClear(
PblTreeSet * set                /** The set to clear */
)
{
    if( set->rootNode )
    {
        pblTreeNodeFree( set->rootNode );
        set->rootNode = NULL;
    }

    set->genericSet.size = 0;
    set->genericSet.changeCounter++;
}

/*
 * Removes all of the elements from this hash set.
 *
 * This method has a time complexity of O(N),
 * with N being the capacity of the set.
 *
 * @return void
 */
static void pblHashSetClear(
PblHashSet * set                /** The set to clear */
)
{
    if( set->capacity > 0 && set->pointerArray )
    {
        memset( set->pointerArray, 0, sizeof(void*) * set->capacity );
    }
    set->genericSet.size = 0;
    set->genericSet.changeCounter++;
}

/**
 * Removes all of the elements from this set.
 *
 * <B>Note:</B> No memory of the elements themselves is freed.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the set.
 *
 * @return void
 */
void pblSetClear(
PblSet * set               /** The set to clear */
)
{
    if( PBL_SET_IS_TREE_SET( set ) )
    {
        pblTreeSetClear( (PblTreeSet *)set );
        return;
    }

    pblHashSetClear( (PblHashSet *)set );
}

/*
 * Free the hash set's memory from heap.
 *
 * <B>Note:</B> The memory of the elements themselves is not freed.
 *
 * This method has a time complexity of O(1).
 *
 * @return void
 */
static void pblHashSetFree(
PblHashSet * set                /** The set to free */
)
{
    PBL_FREE( set->pointerArray );
    PBL_FREE( set );
}

/**
 * Free the set's memory from heap.
 *
 * <B>Note:</B> The memory of the elements themselves is not freed.
 *
 * For hash sets this method has a time complexity of O(1).
 * For tree sets this method has a time complexity of O(N).
 *
 * @return void
 */
void pblSetFree(
PblSet * set     /** The set to free */
)
{
    if( PBL_SET_IS_TREE_SET( set ) )
    {
        pblTreeSetFree( (PblTreeSet *)set );
        return;
    }

    pblHashSetFree( (PblHashSet *)set );
}

/*
 * Decreases the capacity of this hash set instance, if possible.
 *
 * @return int rc >= 0: OK, the set capacity is returned.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - The needed capacity is bigger than maximum allowed value.
 */
static int pblHashSetTrimToSize(
PblHashSet * set                    /** The set to use               */
)
{
    PblHashSet * newSet;
    int neededCapacity = (int)( ( (double)( (PblSet*)set )->size )
            / set->loadFactor ) + 1;
    int targetCapacity = 0;
    int stepSize = _PBL_STEP_SIZE;
    int i;

    if( ( (PblSet*)set )->size == 0 )
    {
        PBL_FREE( set->pointerArray );
        set->capacity = 0;
        set->stepSize = _PBL_STEP_SIZE;
        return set->capacity;
    }

    /*
     * Find the capacity needed
     */
    for( i = 0; i < 28; i++ )
    {
        if( i > 1 )
        {
            stepSize = pblPrimeStepSize[ i - 2 ];
        }
        if( neededCapacity <= pblCapacities[ i ] )
        {
            targetCapacity = pblCapacities[ i ];
            break;
        }
    }

    if( targetCapacity == 0 )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return -1;
    }

    if( targetCapacity >= set->capacity )
    {
        /*
         * Nothing to do
         */
        return set->capacity;
    }

    /*
     * Create a new hash set
     */
    newSet = (PblHashSet*)pblSetNewHashSet();
    if( !newSet )
    {
        pblSetFree( (PblSet*)newSet );
        return -1;
    }

    newSet->genericSet.compare = set->genericSet.compare;
    newSet->hashValue = set->hashValue;
    newSet->loadFactor = set->loadFactor;

    /*
     * Malloc space for 'targetCapacity' pointers
     */
    newSet->pointerArray = (unsigned char **)pbl_malloc0( "pblHashSetTrimToSize",
                                        sizeof(void*) * targetCapacity );
    if( !newSet->pointerArray )
    {
        pblSetFree( (PblSet*)newSet );
        return -1;
    }
    newSet->capacity = targetCapacity;
    newSet->stepSize = stepSize;

    /*
     * Rehash the table if necessary
     */
    if( set->genericSet.size > 0 )
    {
        unsigned char ** pointer = set->pointerArray;
        for( i = 0; i < set->capacity; i++ )
        {
            void * element = *pointer++;
            if( !element )
            {
                continue;
            }
            if( pblHashSetAdd( newSet, element ) < 0 )
            {
                pblSetFree( (PblSet*)newSet );
                return -1;
            }
        }
    }

    /*
     * Free old pointer buffer
     */
    if( set->pointerArray )
    {
        PBL_FREE( set->pointerArray );
    }

    /*
     * Remember the new capacity and step size
     */
    set->capacity = newSet->capacity;
    set->stepSize = newSet->stepSize;

    /*
     * Remember the new pointer buffer
     */
    set->pointerArray = newSet->pointerArray;

    /*
     * Release the new set, but not its pointer buffer
     */
    newSet->pointerArray = NULL;
    pblSetFree( (PblSet*)newSet );

    set->genericSet.changeCounter++;
    return set->capacity;
}

/**
 * Trims the capacity of this set instance to the set's current size
 * divided by the load factor of the set.
 *
 * For tree sets this call returns the set's size.
 *
 * If the set is a hash set and if the capacity is actually
 * decreased, this method has a time complexity of O(N),
 * with N being the number of elements in the set.
 *
 * In all other cases this method has a time complexity of O(1).
 *
 * @return int rc >= 0: The capacity of this set instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblSetTrimToSize(
PblSet * set         /** The set to use */
)
{
    if( PBL_SET_IS_TREE_SET( set ) )
    {
        return set->size;
    }

    return pblHashSetTrimToSize( (PblHashSet *)set );
}

/*
 * Increases the capacity of this hash set instance, if necessary.
 *
 * This method ensures that the set can hold
 * at least the number of elements specified by the minimum
 * capacity argument.
 *
 * @return int rc >= 0: OK, the set capacity is returned.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - minCapacity is bigger than maximum allowed value.
 */
static int pblHashSetEnsureCapacity(
PblHashSet * set,                    /** The set to use               */
int minCapacity                      /** The desired minimum capacity */
)
{
    PblHashSet * newSet;
    int neededCapacity = (int)( ( (double)minCapacity ) / set->loadFactor )
            + 1;
    int targetCapacity = 0;
    int stepSize = _PBL_STEP_SIZE;
    int i;

    /*
     * Find the capacity needed
     */
    for( i = 0; i < 28; i++ )
    {
        if( i > 1 )
        {
            stepSize = pblPrimeStepSize[ i - 2 ];
        }
        if( neededCapacity <= pblCapacities[ i ] )
        {
            targetCapacity = pblCapacities[ i ];
            break;
        }
    }

    if( targetCapacity == 0 )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return -1;
    }

    if( targetCapacity <= set->capacity )
    {
        /*
         * Nothing to do
         */
        return set->capacity;
    }

    /*
     * Create a new hash set
     */
    newSet = (PblHashSet*)pblSetNewHashSet();
    if( !newSet )
    {
        pblSetFree( (PblSet*)newSet );
        return -1;
    }

    newSet->genericSet.compare = set->genericSet.compare;
    newSet->hashValue = set->hashValue;
    newSet->loadFactor = set->loadFactor;

    /*
     * Malloc space for 'targetCapacity' pointers
     */
    newSet->pointerArray = (unsigned char **)pbl_malloc0( "pblHashSetEnsureCapacity",
                                         sizeof(void*) * targetCapacity );
    if( !newSet->pointerArray )
    {
        pblSetFree( (PblSet*)newSet );
        return -1;
    }
    newSet->capacity = targetCapacity;
    newSet->stepSize = stepSize;

    /*
     * Rehash the table if necessary
     */
    if( set->genericSet.size > 0 )
    {
        unsigned char ** pointer = set->pointerArray;
        for( i = 0; i < set->capacity; i++ )
        {
            void * element = *pointer++;
            if( !element )
            {
                continue;
            }
            if( pblHashSetAdd( newSet, element ) < 0 )
            {
                pblSetFree( (PblSet*)newSet );
                return -1;
            }
        }
    }

    /*
     * Free old pointer buffer
     */
    if( set->pointerArray )
    {
        PBL_FREE( set->pointerArray );
    }

    /*
     * Remember the new capacity and step size
     */
    set->capacity = newSet->capacity;
    set->stepSize = newSet->stepSize;

    /*
     * Remember the new pointer buffer
     */
    set->pointerArray = newSet->pointerArray;

    /*
     * Release the new set, but not its pointer buffer
     */
    newSet->pointerArray = NULL;
    pblSetFree( (PblSet*)newSet );

    set->genericSet.changeCounter++;
    return set->capacity;
}

/**
 * Increases the capacity of this set instance, if necessary.
 *
 * For hash sets this method ensures that the set can hold
 * at least the number of elements specified by the minimum
 * capacity argument.
 *
 * For tree sets this method does nothing,
 * it justs returns the value of parameter minCapacity.
 *
 * If the set is a hash set and if the capacity is actually
 * increased, this method has a memory and time complexity of O(N),
 * with N being the new capacity of the set.
 *
 * In all other cases this method has a time complexity of O(1).
 *
 * @return int rc >= 0: OK, the set capacity is returned.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblSetEnsureCapacity(
PblSet * set,   /** The set to use               */
int minCapacity /** The desired minimum capacity */
)
{
    if( PBL_SET_IS_TREE_SET( set ))
    {
        return minCapacity;
    }

    return pblHashSetEnsureCapacity( (PblHashSet *)set, minCapacity );
}

/*
 * Adds the specified element to this hash set.
 *
 * NULL elements added are silently ignored.
 *
 * The add operation runs in amortized constant time,
 * that is, adding n elements requires O(n) time.
 *
 * @return int rc >  0: The set did not already contain the specified element.
 * @return int rc == 0: The set did already contain the specified element.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Maximum capacity of the hash set exceeded.
 */
static int pblHashSetAdd(
PblHashSet * set,         /** The set to use                              */
void * element            /** Element to be appended to this set          */
)
{
    int mask = set->capacity - 1;
    int minCapacity = set->genericSet.size + 1;
    int neededCapacity = (int)( ( (double)minCapacity ) / set->loadFactor );
    int elementIndex;
    int index;
    unsigned char ** elementPtr;

    if( !element )
    {
        return 0;
    }

    for( ;; )
    {
        if( neededCapacity > set->capacity )
        {
            if( pblHashSetEnsureCapacity( set, minCapacity ) < 0 )
            {
                return -1;
            }
            mask = set->capacity - 1;
        }

        elementIndex = set->hashValue( element ) & mask;
        index = elementIndex;

        for( ;; )
        {
            elementPtr = &( set->pointerArray[ index ] );
            if( !*elementPtr )
            {
                /*
                 * Store the element in the table
                 */
                *elementPtr = (unsigned char *)element;

                /*
                 * The element was stored in the hash table
                 */
                set->genericSet.size++;
                set->genericSet.changeCounter++;

                return 1;
            }

            if( !pblCollectionElementCompare( (PblCollection*)set, *elementPtr,
                                              element ) )
            {
                /*
                 * Element already in hash table
                 */
                return 0;
            }

            index = ( index + set->stepSize ) & mask;
            if( elementIndex == index )
            {
                /*
                 * Linear probing brought us back to the original index,
                 * break the loop
                 */
                break;
            }
        }

        /*
         * More space is needed in the hash table
         */
        minCapacity = set->capacity + 1;
        neededCapacity = (int)( ( (double)minCapacity ) / set->loadFactor );
    }
}

/*
 * Returns the first element pointer in the hash set.
 *
 * @return void ** node == NULL: The hash set is empty.
 * @return void ** node != NULL: Address of first pointer to an element in the hash.
 */
void ** pblHashElementFirst(
PblHashSet * set
)
{
    int i;

    if( set->genericSet.size == 0 )
    {
        return NULL;
    }

    for( i = 0; i < set->capacity; i++ )
    {
        if( set->pointerArray[ i ] )
        {
            return (void**)&set->pointerArray[ i ];
        }
    }

    return NULL;
}

/*
 * Returns the next element pointer in the hash set.
 *
 * @return void ** node == NULL: The last element has been reached.
 * @return void ** node != NULL: Address of next pointer to an element in the hash.
 */
void ** pblHashElementNext(
PblHashSet * set,
void ** pointer
)
{
    void ** endPointer;

    if( set->genericSet.size == 0 )
    {
        return NULL;
    }

    endPointer = (void**)set->pointerArray + set->capacity;

    for( pointer++; pointer < endPointer; pointer++ )
    {
        if( *pointer )
        {
            return pointer;
        }
    }
    return NULL;
}

/*
 * Returns the last element pointer in the hash set.
 *
 * @return void ** node == NULL: The hash set is empty
 * @return void ** node != NULL: Address of last pointer to an element in the hash.
 */
void ** pblHashElementLast(
PblHashSet * set
)
{
    int i;

    if( set->genericSet.size == 0 )
    {
        return NULL;
    }

    for( i = set->capacity - 1; i >= 0; i-- )
    {
        if( set->pointerArray[ i ] )
        {
            return (void**)&set->pointerArray[ i ];
        }
    }

    return NULL;
}


/*
 * Returns the previous element pointer in the hash set.
 *
 * @return void ** node == NULL: The first element has been reached.
 * @return void ** node != NULL: Address of previous pointer to an element in the hash.
 */
void ** pblHashElementPrevious(
PblHashSet * set,
void ** pointer
)
{
    void ** endPointer;

    if( set->genericSet.size == 0 )
    {
        return NULL;
    }

    endPointer = (void**)set->pointerArray - 1;

    for( pointer--; pointer > endPointer; pointer-- )
    {
        if( *pointer )
        {
            return pointer;
        }
    }
    return NULL;
}

/*
 * Create a new tree node.
 *
 * @return PblTreeNode * retPtr != Null: The new node inserted.
 * @return PblTreeNode * retPtr == Null: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
static PblTreeNode * pblTreeNodeCreate(
PblTreeSet * set,
void * element
)
{
    PblTreeNode * newNode = (PblTreeNode *)pbl_malloc0( "pblTreeNodeCreate", sizeof(PblTreeNode) );
    if( !newNode )
    {
        return newNode;
    }

    newNode->element = element;

    set->genericSet.size++;
    set->genericSet.changeCounter++;

    return newNode;
}

/*
 * Inserts a new tree node into a tree set
 *
 * @return PblTreeNode * retPtr != Null: The subtree p after the insert.
 * @return PblTreeNode * retPtr == Null: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
static PblTreeNode * pblTreeNodeInsert(
PblTreeSet * set,                      /** The AVL tree to insert into    */
PblTreeNode * parentNode,              /** The parent node to insert to   */
void * element,                        /** The element to insert          */
int  * heightChanged                   /** Set if the tree height changed */
)
{
    PblTreeNode * p1;
    PblTreeNode * p2;
    int compareResult;

    *heightChanged = 0;

    if( parentNode == NULL )
    {
        /*
         * Element is not in the tree yet, insert it.
         */
        *heightChanged = 1;
        return pblTreeNodeCreate( set, element );
    }

    compareResult = pblCollectionElementCompare( (PblCollection*)set, element,
                                                 parentNode->element );
    if( !compareResult )
    {
        /*
         * Element already in tree
         */
        return parentNode;
    }

    if( compareResult < 0 )
    {
        /*
         * Insert into left sub tree
         */
        p1 = pblTreeNodeInsert( set, parentNode->prev, element, heightChanged );
        if( !p1 )
        {
            return p1;
        }
        PBL_AVL_TREE_SET_PREV( parentNode, p1 );

        if( !*heightChanged )
        {
            return parentNode;
        }

        /*
         * Left sub tree increased in height
         */
        if( parentNode->balance == 1 )
        {
            parentNode->balance = 0;
            *heightChanged = 0;
            return parentNode;
        }

        if( parentNode->balance == 0 )
        {
            parentNode->balance = -1;
            return parentNode;
        }

        /*
         * Balancing needed
         */
        p1 = parentNode->prev;

        if( p1->balance == -1 )
        {
            /*
             * Simple LL rotation
             */
            PBL_AVL_TREE_SET_PREV( parentNode, p1->next );

            PBL_AVL_TREE_SET_NEXT( p1, parentNode );
            parentNode->balance = 0;

            parentNode = p1;
            parentNode->balance = 0;
            *heightChanged = 0;
            return parentNode;
        }

        /*
         * double LR rotation
         */
        p2 = p1->next;

        PBL_AVL_TREE_SET_NEXT( p1, p2->prev );

        PBL_AVL_TREE_SET_PREV( p2, p1 );

        PBL_AVL_TREE_SET_PREV( parentNode, p2->next );

        PBL_AVL_TREE_SET_NEXT( p2, parentNode );

        if( p2->balance == -1 )
        {
            parentNode->balance = 1;
        }
        else
        {
            parentNode->balance = 0;
        }

        if( p2->balance == 1 )
        {
            p1->balance = -1;
        }
        else
        {
            p1->balance = 0;
        }
        parentNode = p2;
        parentNode->balance = 0;
        *heightChanged = 0;
        return parentNode;
    }

    /*
     * Insert into right sub tree
     */
    p1 = pblTreeNodeInsert( set, parentNode->next, element, heightChanged );
    if( !p1 )
    {
        return p1;
    }
    PBL_AVL_TREE_SET_NEXT( parentNode, p1 );

    if( !*heightChanged )
    {
        return parentNode;
    }

    /*
     * Right sub tree increased in height
     */
    if( parentNode->balance == -1 )
    {
        parentNode->balance = 0;
        *heightChanged = 0;
        return parentNode;
    }

    if( parentNode->balance == 0 )
    {
        parentNode->balance = 1;
        return parentNode;
    }

    /*
     * Balancing needed
     */
    p1 = parentNode->next;

    if( p1->balance == 1 )
    {
        /*
         * Simple RR rotation
         */
        PBL_AVL_TREE_SET_NEXT( parentNode, p1->prev );

        PBL_AVL_TREE_SET_PREV( p1, parentNode );
        parentNode->balance = 0;

        parentNode = p1;
        parentNode->balance = 0;
        *heightChanged = 0;
        return parentNode;
    }

    /*
     * double RL rotation
     */
    p2 = p1->prev;

    PBL_AVL_TREE_SET_PREV( p1, p2->next );

    PBL_AVL_TREE_SET_NEXT( p2, p1 );

    PBL_AVL_TREE_SET_NEXT( parentNode, p2->prev );

    PBL_AVL_TREE_SET_PREV( p2, parentNode );

    if( p2->balance == 1 )
    {
        parentNode->balance = -1;
    }
    else
    {
        parentNode->balance = 0;
    }

    if( p2->balance == -1 )
    {
        p1->balance = 1;
    }
    else
    {
        p1->balance = 0;
    }
    parentNode = p2;
    parentNode->balance = 0;
    *heightChanged = 0;
    return parentNode;
}

/*
 * Adds the specified element to this tree set.
 *
 * NULL elements added are silently ignored.
 *
 * The add operation runs O(Log N) time.
 *
 * @return int rc >  0: The set did not already contain the specified element.
 * @return int rc == 0: The set did already contain the specified element.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
static int pblTreeSetAdd(
PblTreeSet * set,         /** The set to use                              */
void * element            /** Element to be appended to this set          */
)
{
    int size = set->genericSet.size;
    int h = 0;
    PblTreeNode * insertResult;

    if( !element )
    {
        return 0;
    }

    insertResult = pblTreeNodeInsert( set, set->rootNode, element, &h );
    if( insertResult == NULL )
    {
        return -1;
    }

    /*
     * Remember the tree after the insert
     */
    insertResult->parent = NULL;
    set->rootNode = insertResult;

    if( size == set->genericSet.size )
    {
        return 0;
    }

    return 1;
}

/**
 * Adds the specified element to this set.
 *
 * For hash sets his method has a time complexity of O(1).
 * For tree sets his method has a time complexity of O(Log N).
 *
 * @return int rc >  0: The set did not already contain the specified element.
 * @return int rc == 0: The set did already contain the specified element.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 * <BR>PBL_ERROR_PARAM_ELEMENT - The element passed is NULL.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Maximum capacity of the hash set exceeded.
 */
int pblSetAdd(
PblSet * set,  /** The set to add to               */
void * element /** Element to be added to this set */
)
{
    if( !element )
    {
        pbl_errno = PBL_ERROR_PARAM_ELEMENT;
        return -1;
    }

    if( PBL_SET_IS_TREE_SET( set ) )
    {
        return pblTreeSetAdd( (PblTreeSet *)set, element );
    }
    return pblHashSetAdd( (PblHashSet *)set, element );
}

/*
 * Adds all of the elements returned by the iterator into this hash set.
 *
 * @return int rc >= 0: The size of this set instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The collection underlying the iterator was modified concurrently.
 */
static int pblHashSetAddAll(
PblHashSet  * set,     /** The set to use                                           */
PblIterator * iterator /** The iterator whose elements are to be added to this set. */
)
{
    int hasNext;
    int hasPrev;

    // Add to the hash set
    //
    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        void * element = pblIteratorNext( iterator );
        if( element == (void*)-1 )
        {
            return -1;
        }
        if( pblHashSetAdd( set, element ) < 0 )
        {
            // An error, remove the elements added so far
            //
            while( ( hasPrev = pblIteratorHasPrevious( iterator ) ) > 0 )
            {
                void * element = pblIteratorPrevious( iterator );
                if( element == (void*)-1 || pblHashSetRemoveElement( set, element ) < 0 )
                {
                    return -1;
                }
            }
            return -1;
        }
    }
    if( hasNext < 0 )
    {
        // Concurrent modification error on the source set,
        //
        return -1;
    }

    return set->genericSet.size;
}

/*
 * Adds all of the elements returned by the iterator into this tree set.
 *
 * @return int rc >= 0: The size of this set instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The collection underlying the iterator was modified concurrently.
 */
static int pblTreeSetAddAll(
PblTreeSet  * set,     /** The set to use                                           */
PblIterator * iterator /** The iterator whose elements are to be added to this set. */
)
{
    int hasNext;
    int hasPrev;

    // Add to the hash set
    //
    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        void * element = pblIteratorNext( iterator );
        if( element == (void*)-1 )
        {
            return -1;
        }
        if( pblTreeSetAdd( set, element ) < 0 )
        {
            // An error, remove the elements added so far
            //
            while( ( hasPrev = pblIteratorHasPrevious( iterator ) ) > 0 )
            {
                void * element = pblIteratorPrevious( iterator );
                if( element == (void*)-1 || pblTreeSetRemoveElement( set, element ) < 0 )
                {
                    return -1;
                }
            }
            return -1;
        }
    }
    if( hasNext < 0 )
    {
        // Concurrent modification error on the source set,
        //
        return -1;
    }

    return set->genericSet.size;
}

/**
 * Adds all of the elements in the specified Collection to this set.
 * NULL elements added are silently ignored.
 *
 * For hash sets this method has a time complexity of O(M),
 * with M being the size of the collection whose elements are added.
 *
 * For tree sets this method has a time complexity of O(M * Log N),
 * with M being the size of the collection whose elements are added,
 * and N being the number of elements in the set.
 *
 * @return int rc >= 0: The size of this set instance.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 * <BR>PBL_ERROR_PARAM_COLLECTION        - Collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - Collection was modified concurrently.
 */
int pblSetAddAll(
PblSet * set,      /** The set to use                                             */
void * collection  /** The collection whose elements are to be added to this set. */
)
{
    PblIterator   iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;

    if( pblIteratorInit( collection, iterator ) < 0 )
    {
        return -1;
    }

    if( pblIteratorSize( iterator ) < 1 )
    {
        return set->size;
    }

    if( PBL_SET_IS_HASH_SET( set ) )
    {
        int rc = pblHashSetAddAll( (PblHashSet *)set, iterator );
        return rc;
    }

    return pblTreeSetAddAll( (PblTreeSet *)set, iterator );
}

/*
 * Balances an AVL tree.
 *
 * Used if left sub tree decreased in size.
 *
 * @return PblTreeNode * retPtr: The subtree p after the balance.
 *
 */
static PblTreeNode * pblTreeNodeBalanceLeft(
PblTreeNode * node,                       /** The node to balance     */
int  * heightChanged                      /** Set if the tree height changed */
)
{
    PblTreeNode * p1;
    PblTreeNode * p2;
    int b1;
    int b2;

    *heightChanged = 1;

    if( node->balance == -1 )
    {
        node->balance = 0;
        return node;
    }

    if( node->balance == 0 )
    {
        node->balance = 1;
        *heightChanged = 0;
        return node;
    }

    /*
     * Balancing needed
     */
    p1 = node->next;
    b1 = p1->balance;

    if( b1 >= 0 )
    {
        /*
         * Simple RR rotation
         */
        PBL_AVL_TREE_SET_NEXT( node, p1->prev );

        PBL_AVL_TREE_SET_PREV( p1, node );

        if( b1 == 0 )
        {
            node->balance = 1;
            p1->balance = -1;
            *heightChanged = 0;
        }
        else
        {
            node->balance = 0;
            p1->balance = 0;
        }
        return p1;
    }

    /*
     * double RL rotation
     */
    p2 = p1->prev;
    b2 = p2->balance;

    PBL_AVL_TREE_SET_PREV( p1, p2->next );

    PBL_AVL_TREE_SET_NEXT( p2, p1 );

    PBL_AVL_TREE_SET_NEXT( node, p2->prev );

    PBL_AVL_TREE_SET_PREV( p2, node );

    if( b2 == 1 )
    {
        node->balance = -1;
    }
    else
    {
        node->balance = 0;
    }

    if( b2 == -1 )
    {
        p1->balance = 1;
    }
    else
    {
        p1->balance = 0;
    }

    p2->balance = 0;
    return p2;
}

/*
 * Balances an AVL tree.
 *
 * Used if right sub tree decreased in size.
 *
 * @return PblTreeNode * retPtr: The subtree p after the balance.
 *
 */
static PblTreeNode * pblTreeNodeBalanceRight(
PblTreeNode * node,                         /** The node to balance            */
int  * heightChanged                        /** Set if the tree height changed */
)
{
    PblTreeNode * p1;
    PblTreeNode * p2;
    int b1;
    int b2;

    *heightChanged = 1;

    if( node->balance == 1 )
    {
        node->balance = 0;
        return node;
    }

    if( node->balance == 0 )
    {
        node->balance = -1;
        *heightChanged = 0;
        return node;
    }

    /*
     * Balancing needed
     */
    p1 = node->prev;
    b1 = p1->balance;

    if( b1 <= 0 )
    {
        /*
         * Simple LL rotation
         */
        PBL_AVL_TREE_SET_PREV( node, p1->next );

        PBL_AVL_TREE_SET_NEXT( p1, node );

        if( b1 == 0 )
        {
            node->balance = -1;
            p1->balance = 1;
            *heightChanged = 0;
        }
        else
        {
            node->balance = 0;
            p1->balance = 0;
        }
        return p1;
    }

    /*
     * double LR rotation
     */
    p2 = p1->next;
    b2 = p2->balance;

    PBL_AVL_TREE_SET_NEXT( p1, p2->prev );

    PBL_AVL_TREE_SET_PREV( p2, p1 );

    PBL_AVL_TREE_SET_PREV( node, p2->next );

    PBL_AVL_TREE_SET_NEXT( p2, node );

    if( b2 == -1 )
    {
        node->balance = 1;
    }
    else
    {
        node->balance = 0;
    }

    if( b2 == 1 )
    {
        p1->balance = -1;
    }
    else
    {
        p1->balance = 0;
    }

    p2->balance = 0;
    return p2;
}

/*
 * Helper function for AVL tree remove.
 *
 * @return PblTreeNode * retPtr: The subtree p after the remove.
 */
static PblTreeNode * pblTreeNodeRemove2(
PblTreeSet  * set,           /** The AVL tree to remove from    */
PblTreeNode * r,
PblTreeNode * q,
int  * heightChanged
)
{
    PblTreeNode * p;

    *heightChanged = 0;

    if( r->next )
    {
        p = pblTreeNodeRemove2( set, r->next, q, heightChanged );
        PBL_AVL_TREE_SET_NEXT( r, p );
        if( *heightChanged )
        {
            r = pblTreeNodeBalanceRight( r, heightChanged );
        }
        return r;
    }

    q->element = r->element;
    p = r->prev;
    *heightChanged = 1;

    PBL_FREE( r );
    set->genericSet.size--;
    set->genericSet.changeCounter++;

    return p;
}

/*
 * Removes a tree node from a tree set
 *
 * @return PblTreeNode * retPtr: The subtree p after the remove.
 */
static PblTreeNode * pblTreeNodeRemove(
PblTreeSet * set,                      /** The AVL tree to remove from    */
PblTreeNode * p,                       /** The node to remove from        */
void * element,                        /** The element to remove          */
int  * heightChanged                   /** Set if the tree height changed */
)
{
    PblTreeNode * q;
    PblTreeNode * p1;
    int compareResult;

    *heightChanged = 0;

    if( !p )
    {
        /*
         * Not found
         */
        return p;
    }

    compareResult = pblCollectionElementCompare( (PblCollection*)set, element,
                                                 p->element );

    if( compareResult < 0 )
    {
        q = pblTreeNodeRemove( set, p->prev, element, heightChanged );
        PBL_AVL_TREE_SET_PREV( p, q );

        if( *heightChanged )
        {
            p = pblTreeNodeBalanceLeft( p, heightChanged );
        }
        return p;
    }

    if( compareResult > 0 )
    {
        q = pblTreeNodeRemove( set, p->next, element, heightChanged );
        PBL_AVL_TREE_SET_NEXT( p, q );

        if( *heightChanged )
        {
            p = pblTreeNodeBalanceRight( p, heightChanged );
        }
        return p;
    }

    /*
     * p is the node that needs to be removed!
     */
    q = p;

    if( !q->next )
    {
        p = q->prev;
        *heightChanged = 1;

        PBL_FREE( q );
        set->genericSet.size--;
        set->genericSet.changeCounter++;
    }
    else if( !q->prev )
    {
        p = q->next;
        *heightChanged = 1;

        PBL_FREE( q );
        set->genericSet.size--;
        set->genericSet.changeCounter++;
    }
    else
    {
        /*
         * Replace q with is biggest predecessor and remove that
         */
        p1 = pblTreeNodeRemove2( set, q->prev, q, heightChanged );
        PBL_AVL_TREE_SET_PREV( q, p1 );

        if( *heightChanged )
        {
            p = pblTreeNodeBalanceLeft( p, heightChanged );
        }
    }

    return p;
}

/*
 * Removes the specified element from this tree set if it is present.
 *
 * @return int rc != 0: The set contained the specified element.
 * @return int rc == 0: The specified element is not present.
 */
static int pblTreeSetRemoveElement(
PblTreeSet * set,     /** The set to use                        */
void * element        /** Element to remove                     */
)
{
    int h = 0;
    int size = set->genericSet.size;
    if( size == 0 )
    {
        return 0;
    }

    set->rootNode = pblTreeNodeRemove( set, set->rootNode, element, &h );
    if( set->rootNode )
    {
        set->rootNode->parent = NULL;
    }

    if( size == set->genericSet.size )
    {
        return 0;
    }

    return 1;
}

/*
 * Removes the specified element from this hash set if it is present.
 *
 * @return int rc != 0: The set contained the specified element.
 * @return int rc == 0: The specified element is not present.
 */
static int pblHashSetRemoveElement(
PblHashSet * set,     /** The set to use                        */
void * element        /** Element to remove                     */
)
{
    int mask = set->capacity - 1;
    int elementIndex;
    int indexToRemove;
    unsigned char ** elementPtr;
    int nextIndex;
    int hashValue;

    if( set->genericSet.size == 0 || set->capacity < 1 )
    {
        return 0;
    }
    elementIndex = set->hashValue( element ) & mask;

    /*
     * Find out if the element is in the set at all,
     * run through the collision chain
     */
    for( indexToRemove = elementIndex;
         indexToRemove >= 0;
         indexToRemove = ( indexToRemove + set->stepSize ) & mask
       )
    {
        elementPtr = &( set->pointerArray[ indexToRemove ] );
        if( !*elementPtr )
        {
            /*
             * The element is not in the hash table
             */
            return 0;
        }

        if( !pblCollectionElementCompare( (PblCollection*)set, *elementPtr, element ) )
        {
            /*
             * Element is in hash table
             */
            break;
        }
    }

    set->genericSet.changeCounter++;
    set->genericSet.size--;

    for( ;; )
    {
        /*
         * The element at index 'indexToRemove' is to be removed,
         * the collision chains need to be kept in order
         */
        set->pointerArray[ indexToRemove ] = NULL;

        /*
         * Go forward through the collision chain
         */
        for( nextIndex = ( indexToRemove + set->stepSize ) & mask;
             nextIndex >= 0;
             nextIndex = ( nextIndex + set->stepSize ) & mask
           )
        {
            elementPtr = &( set->pointerArray[ nextIndex ] );

            // If the end of the collision chain is found, we are done
            //
            if( !*elementPtr )
            {
                /*
                 * The element was removed
                 */
                return 1;
            }

            // Calculate the hash value
            //
            hashValue = set->hashValue( *elementPtr ) & mask;

            // If the element at 'nextIndex' belongs to the same
            // collision chain as the one to remove
            //
            if( hashValue == indexToRemove )
            {
                // The element at 'nextIndex' can be moved to the empty spot
                //
                set->pointerArray[ indexToRemove ] = *elementPtr;

                // Continue at 'nextIndex' to keep the collision chain in order
                //
                indexToRemove = nextIndex;
                break;
            }

            if( hashValue == nextIndex )
            {
                // The element at 'nextIndex' is the head of a different collision chain,
                // it cannot be moved
                //
                continue;
            }
            else
            {
                int index;

                for( index = ( indexToRemove + set->stepSize ) & mask;
                     index != nextIndex;
                     index = ( index + set->stepSize ) & mask
                   )
                {
                    if( hashValue == index )
                    {
                        // The element at 'nextIndex' belongs to a different
                        // collision chain starting at 'index', it cannot be moved
                        //
                        break;
                    }

                }

                if( hashValue == index )
                {
                    // The element at 'nextIndex' belongs to a different
                    // collision chain starting at 'index', it cannot be moved
                    //
                    continue;
                }

                // The element at 'nextIndex' belongs to a collision chain that
                // passes through 'indexToRemove', therefore the element can be moved
                //
                set->pointerArray[ indexToRemove ] = *elementPtr;

                // Continue at 'nextIndex' to keep the collision chain in order
                //
                indexToRemove = nextIndex;
                break;
            }
        }
    }
}

/**
 * Removes the specified element from this set if it is present.
 *
 * For tree sets this method has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * For hash sets this method has a complexity of O(1).
 *
 * @return int rc != 0: The set contained the specified element.
 * @return int rc == 0: The specified element is not present.
 */
int pblSetRemoveElement(
PblSet * set,            /** The set to use                        */
void * element           /** Element to remove                     */
)
{
    if( PBL_SET_IS_TREE_SET( set ))
    {
        return pblTreeSetRemoveElement( (PblTreeSet *)set, element );
    }
    return pblHashSetRemoveElement( (PblHashSet *)set, element );
}

/*
 * Replaces the element of the tree set that matches the given element
 * with the given element.
 *
 * If a matching element is found it is replaced and returned.
 * If no matching element is found NULL is returned.
 *
 * This method has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * @return void * retptr != NULL: The element that was replaced.
 * @return void * retptr == NULL: There is no matching element.
 *
 */
static void * pblTreeSetReplaceElement(
PblTreeSet * set,         /** The set to use                  */
void * element            /** Element to look for             */
)
{
    PblTreeNode * node = set->rootNode;
    int compareResult;

    if( set->genericSet.size == 0 )
    {
        return NULL;
    }

    while( node )
    {
        compareResult = pblCollectionElementCompare( (PblCollection*)set, element,
                                                     node->element );
        if( !compareResult )
        {
            void * returnValue = node->element;
            node->element = element;
            return returnValue;
        }

        if( compareResult < 0 )
        {
            node = node->prev;
        }
        else
        {
            node = node->next;
        }
    }
    return NULL;
}

/*
 * Replaces the element of the hash set that matches the given element
 * with the given element.
 *
 * If a matching element is found it is replaced and returned.
 * If no matching element is found NULL is returned.
 *
 * This operation runs in constant time.
 *
 * @return void * retptr != NULL: The element that was replaced.
 * @return void * retptr == NULL: There is no matching element.
 */
static void * pblHashSetReplaceElement(
PblHashSet * set,         /** The set to use                  */
void * element            /** Element to look for             */
)
{
    int mask = set->capacity - 1;
    int index;
    void * pointer;

    if( set->genericSet.size == 0 || set->capacity < 1 )
    {
        return NULL;
    }
    index = set->hashValue( element ) & mask;

    for( ;; )
    {
        pointer = set->pointerArray[ index ];
        if( !pointer )
        {
            return NULL;
        }

        if( !pblCollectionElementCompare( (PblCollection*)set, pointer, element ) )
        {
            /*
             * Element in hash table
             */
            void * returnValue = pointer;
            set->pointerArray[ index ] = element;
            return returnValue;
        }

        index = ( index + set->stepSize ) & mask;
    }
}

/**
 * Replaces the element of the set that matches the given element
 * with the given element.
 *
 * If a matching element is found it is replaced and returned.
 * If no matching element is found NULL is returned.
 *
 * For tree sets this method has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * For hash sets this method has a complexity of O(1).
 *
 * @return void * retptr != NULL: The element that was replaced.
 * @return void * retptr == NULL: There is no matching element.
 */
void * pblSetReplaceElement(
PblSet * set,             /** The set to use                  */
void * element            /** Element to look for             */
)
{
    if( PBL_SET_IS_HASH_SET( set ))
    {
        return pblHashSetReplaceElement( (PblHashSet *)set, element );
    }

    return pblTreeSetReplaceElement( (PblTreeSet *)set, element );
}

/*
 * Returns the element of this tree set that matches the given element.
 *
 * This method has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * @return void * retptr != NULL: The element that matches.
 * @return void * retptr == NULL: There is no matching element.
 */
static void * pblTreeSetGetElement(
PblTreeSet * set,         /** The set to use                  */
void * element            /** Element to look for             */
)
{
    PblTreeNode * node = set->rootNode;
    int compareResult;

    if( set->genericSet.size == 0 )
    {
        return NULL;
    }

    while( node )
    {
        compareResult = pblCollectionElementCompare( (PblCollection*)set, element,
                                                     node->element );
        if( !compareResult )
        {
            return node->element;
        }

        if( compareResult < 0 )
        {
            node = node->prev;
        }
        else
        {
            node = node->next;
        }
    }
    return NULL;
}

/*
 * Returns the element of this hash set that matches the given element.
 *
 * This operation runs in constant time.
 *
 * @return void * retptr != NULL: The element that matches.
 * @return void * retptr == NULL: There is no matching element.
 */
static void * pblHashSetGetElement(
PblHashSet * set,         /** The set to use                  */
void * element            /** Element to look for             */
)
{
    int mask = set->capacity - 1;
    int index;
    void * pointer;

    if( set->genericSet.size == 0 || set->capacity < 1 )
    {
        return NULL;
    }
    index = set->hashValue( element ) & mask;

    for( ;; )
    {
        pointer = set->pointerArray[ index ];
        if( !pointer )
        {
            return NULL;
        }

        if( !pblCollectionElementCompare( (PblCollection*)set, pointer, element ) )
        {
            /*
             * Element in hash table
             */
            return pointer;
        }

        index = ( index + set->stepSize ) & mask;
    }
}

/**
 * Returns the element of this set that matches the given element.
 *
 * For tree sets this method has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * For hash sets this method has a complexity of O(1).
 *
 * @return void * retptr != NULL: The element that matches.
 * @return void * retptr == NULL: There is no matching element.
 */
void * pblSetGetElement(
PblSet * set,             /** The set to use                  */
void * element            /** Element to look for             */
)
{
    if( PBL_SET_IS_HASH_SET( set ))
    {
        return pblHashSetGetElement( (PblHashSet *)set, element );
    }

    return pblTreeSetGetElement( (PblTreeSet *)set, element );
}

/*
 * Returns true if this tree set contains the specified element.
 *
 * This method has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * @return int rc != 0: The specified element is present.
 * @return int rc == 0: The specified element is not present.
 */
static int pblTreeSetContains(
PblTreeSet * set,         /** The set to use                  */
void * element            /** Element to look for             */
)
{
    PblTreeNode * node = set->rootNode;
    int compareResult;

    if( set->genericSet.size == 0 )
    {
        return 0;
    }

    while( node )
    {
        compareResult = pblCollectionElementCompare( (PblCollection*)set, element,
                                                     node->element );
        if( !compareResult )
        {
            return 1;
        }

        if( compareResult < 0 )
        {
            node = node->prev;
        }
        else
        {
            node = node->next;
        }
    }
    return 0;
}

/*
 * Returns true if this hash set contains the specified element.
 *
 * This operation runs in constant time.
 *
 * @return int rc != 0: The specified element is present.
 * @return int rc == 0: The specified element is not present.
 */
static int pblHashSetContains(
PblHashSet * set,         /** The set to use                  */
void * element            /** Element to look for             */
)
{
    int mask = set->capacity - 1;
    int index;
    void * pointer;

    if( set->genericSet.size == 0 || set->capacity < 1 )
    {
        return 0;
    }
    index = set->hashValue( element ) & mask;

    for( ;; )
    {
        pointer = set->pointerArray[ index ];
        if( !pointer )
        {
            return 0;
        }

        if( !pblCollectionElementCompare( (PblCollection*)set, pointer, element ) )
        {
            /*
             * Element in hash table
             */
            return 1;
        }

        index = ( index + set->stepSize ) & mask;
    }
}

/**
 * Returns true if this set contains the specified element.
 *
 * For tree sets this method has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * For hash sets this method has a complexity of O(1).
 *
 * @return int rc != 0: The specified element is present.
 * @return int rc == 0: The specified element is not present.
 */
int pblSetContains(
PblSet * set,             /** The set to use                  */
void * element            /** Element to look for             */
)
{
    if( PBL_SET_IS_HASH_SET( set ))
    {
        return pblHashSetContains( (PblHashSet *)set, element );
    }

    return pblTreeSetContains( (PblTreeSet *)set, element );
}

/**
 * Returns a value > 0 if this set contains all of the elements
 * in the specified collection.
 *
 * This implementation iterates over the specified collection,
 * checking each element returned by the iterator in turn to see if it's contained in this set.
 * If all elements are so contained a value > 0 is returned, otherwise 0.
 *
 * For hash sets this method has a time complexity of O(M) and
 * for hash sets this method has a time complexity of O(Log N * M),
 * with N being the size of the set and M being the size of the
 * collection.
 *
 * @return int rc >  0: The set contains all of the elements in the specified collection.
 * @return int rc == 0: The set does not contain all of the elements.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_COLLECTION        - The collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The collection was modified concurrently.
 */
int pblSetContainsAll(
PblSet * set,      /** The set to use                                            */
void * collection  /** The collection to be checked for containment in this set. */
)
{
    PblIterator   iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int hasNext;

    if( pblIteratorInit( collection, iterator ) < 0 )
    {
        return -1;
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
        if( !pblSetContains( set, element ) )
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

/**
 * Removes and returns the last element of this set.
 *
 * For hash sets removing an element at the end of the set has a time complexity
 * of O(N), with N being the capacity of the set.
 *
 * For tree sets removing an element at the end of the set has a time complexity
 * of O(Log N), with N being the number of elements in the set.
 *
 * @return void * retptr != NULL: The element that was removed.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - This set is empty.
 */
void * pblSetRemoveLast(
PblSet * set         /** The set to use */
)
{
    return pblSetRemoveAt( set, set->size - 1 );
}

/**
 * Removes and returns the first element in this set.
 *
 * For hash sets removing an element at the start of the set has a time complexity
 * of O(N), with N being the capacity of the set.
 *
 * For tree sets removing an element at the start of the set has a time complexity
 * of O(Log N), with N being the number of elements in the set.
 *
 * @return void * retptr != NULL: The element that was removed.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - This set is empty.
 */
void * pblSetRemoveFirst(
PblSet * set         /** The set to use */
)
{
    return pblSetRemoveAt( set, 0 );
}

/*
 * Retains in this tree set only the elements
 * that are contained in the specified collection.
 *
 * @return int rc >  0: If this set changed as a result of the call.
 * @return int rc == 0: This set did not change.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_SET               - The set cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The set was modified concurrently.
 */
static int pblTreeSetRetainAll(
PblTreeSet    * set,        /** The set to use                        */
PblCollection * collection  /** The collection to use                 */
)
{
    PblIterator   iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int rc = 0;
    int hasNext;
    void * element;

    /*
     * Get the iterator for this set
     */
    if( pblIteratorInit( (PblCollection *)set, iterator ) < 0 )
    {
        pbl_errno = PBL_ERROR_PARAM_SET;
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

        if( !pblCollectionContains( collection, element ) )
        {
            if( pblIteratorRemove( iterator ) < 0 )
            {
                return -1;
            }

            /*
             * The set changed
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

/*
 * Retains in this hash set only the elements
 * that are contained in the specified collection.
 *
 * @return int rc >  0: If this set changed as a result of the call.
 * @return int rc == 0: This set did not change.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_SET               - The set cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The set was modified concurrently.
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 */
static int pblHashSetRetainAll(
PblHashSet    * set,        /** The set to use                        */
PblCollection * collection  /** The collection to use                 */
)
{
    PblIterator iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int rc = 0;
    int hasNext;
    void * element;

    PblList * elementsToBeRemoved = pblListNewArrayList();
    if( !elementsToBeRemoved )
    {
        return -1;
    }

    /*
     * Get the iterator for this set
     */
    if( pblIteratorInit( (PblCollection *)set, iterator ) < 0 )
    {
        pbl_errno = PBL_ERROR_PARAM_SET;
        pblListFree( elementsToBeRemoved );
        return -1;
    }

    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        element = pblIteratorNext( iterator );
        if( element == (void*)-1 )
        {
            // Concurrent modification
            //
            pblListFree( elementsToBeRemoved );
            return -1;
        }

        if( !pblCollectionContains( collection, element ) )
        {
            /*
             * Remember the element to be removed for later
             */
            if( pblListAdd( elementsToBeRemoved, element ) < 0 )
            {
                pblListFree( elementsToBeRemoved );
                return -1;
            }
        }
    }
    if( hasNext < 0 )
    {
        // Concurrent modification
        //
        pblListFree( elementsToBeRemoved );
        return -1;
    }

    /*
     * Get the iterator for the elements that need to be removed
     */
    if( pblIteratorInit( elementsToBeRemoved, iterator ) < 0 )
    {
        pbl_errno = PBL_ERROR_PARAM_SET;
        pblListFree( elementsToBeRemoved );
        return -1;
    }

    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        element = pblIteratorNext( iterator );
        if( element == (void*)-1 )
        {
            // Concurrent modification
            //
            pblListFree( elementsToBeRemoved );
            return -1;
        }

        rc |= pblHashSetRemoveElement( set, element );
    }
    if( hasNext < 0 )
    {
        // Concurrent modification
        //
        pblListFree( elementsToBeRemoved );
        return -1;
    }

    pblListFree( elementsToBeRemoved );
    return rc;
}

/**
 * Retains only the elements in this set
 * that are contained in the specified collection.
 *
 * In other words, removes from this set all
 * of its elements that are not contained in the specified collection.
 *
 * This implementation iterates over this set,
 * checking each element returned by the iterator in turn
 * to see if it's contained in the specified collection.
 *
 * If it's not so contained, it's removed from this set
 * with the iterator's remove method in case of a tree set
 * and with a direct removal method in case of
 * an hash set. This hash set removal has a memory complexity
 * of O(N), with N being the number of elements to be removed.
 *
 * This method has a time complexity of O(N * M),
 * with N being the size of the set and M being the size of the
 * collection.
 *
 * @return int rc >  0: If this set changed as a result of the call.
 * @return int rc == 0: This set did not change.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_SET               - The set cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The set was modified concurrently.
 * <BR>PBL_ERROR_PARAM_COLLECTION        - The collection cannot be iterated.
 * <BR>PBL_ERROR_OUT_OF_MEMORY           - Out of memory.
 */
int pblSetRetainAll(
PblSet * set,           /** The set to use                           */
void * collection       /** The elements to be retained in this set. */
)
{
    PblIterator   iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int iteratorSize;

    if( set->size < 1 )
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
     * Get the iterator for this set
     */
    if( pblIteratorInit( set, iterator ) < 0 )
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
         * Clear the entire set
         */
        pblSetClear( set );
        return 1;
    }

    if( PBL_SET_IS_HASH_SET( set ) )
    {
        return pblHashSetRetainAll( (PblHashSet*)set, (PblCollection *)collection );
    }

    return pblTreeSetRetainAll( (PblTreeSet*)set, (PblCollection *)collection );
}

/**
 * Returns the last element in this set.
 *
 * For hash sets accessing an element at the end of the set has a time complexity
 * of O(N), with N being the capacity of the set.
 *
 * For tree sets accessing an element at the end of the set has a time complexity
 * of O(Log N), with N being the number of elements in the set.
 *
 * @return void * retptr != NULL: The last element of the set.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - This set is empty.
 */
void * pblSetGetLast(
PblSet * set         /** The set to use */
)
{
    return pblSetGet( set, set->size - 1 );
}

/**
 * Retrieves, but does not remove, the tail (last element) of this set.
 *
 * For hash sets accessing an element at the end of the set has a time complexity
 * of O(N), with N being the capacity of the set.
 *
 * For tree sets accessing an element at the end of the set has a time complexity
 * of O(Log N), with N being the number of elements in the set.
 *
 * @return void * retptr != NULL: The last element of the set.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - This set is empty.
 */
void * pblSetTail(
PblSet * set         /** The set to use */
)
{
    return pblSetGet( set, set->size - 1 );
}

/**
 * Returns the first element in this set.
 *
 * For hash sets accessing an element at the start of the set has a time complexity
 * of O(N), with N being the capacity of the set.
 *
 * For tree sets accessing an element at the start of the set has a time complexity
 * of O(Log N), with N being the number of elements in the set.
 *
 * @return void * retptr != NULL: The first element of the set.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - This set is empty.
 */
void * pblSetGetFirst(
PblSet * set          /** The set to use */
)
{
    return pblSetGet( set, 0 );
}

/**
 * Returns the element at the specified position in this set.
 *
 * Retrieving the first or last element of a hash set has a time complexity of O(N), with N being the capacity of the set.
 *
 * Retrieving the first or last element of a tree set has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * For any set retrieving a random element from any set has O(N),
 * with N being the size of the set.
 *
 * @return void * retptr != NULL: The element at the specified position in this set.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS      - Index is out of range (index < 0 || index >= size()).
 * <BR>PBL_ERROR_PARAM_COLLECTION   - The set cannot be iterated.
 */
void * pblSetGet(
PblSet * set,     /** The set to use                */
int index         /** Index of the element to return */
)
{
    PblIterator iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int hasMore = 0;
    void * element = NULL;

    /*
     * Check the parameter
     */
    if( index < 0 || index >= set->size )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return NULL;
    }

    if( index <= set->size / 2 )
    {
        if( pblIteratorInit( set, iterator ) < 0 )
        {
            return NULL;
        }

        while( index-- >= 0 && ( hasMore = pblIteratorHasNext( iterator ) ) > 0 )
        {
            element = pblIteratorNext( iterator );
            if( element == (void*)-1 )
            {
                // Concurrent modification
                //
                return NULL;
            }
        }
        if( hasMore < 0 )
        {
            return NULL;
        }
        return element;
    }
    else
    {
        if( pblIteratorReverseInit( set, iterator ) < 0 )
        {
            return NULL;
        }

        index = set->size - ( index + 1 );
        while( index-- >= 0 && ( hasMore = pblIteratorHasPrevious( iterator ) ) > 0 )
        {
            element = pblIteratorPrevious( iterator );
            if( element == (void*)-1 )
            {
                // Concurrent modification
                //
                return NULL;
            }
        }
        if( hasMore < 0 )
        {
            return NULL;
        }
        return element;
    }
}

/**
 * Returns the capacity of this set instance.
 *
 * For tree sets this call returns the set's size.
 *
 * For hash sets it returns the set's capacity.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc: The capacity of this set instance.
 */
int pblSetGetCapacity(
PblSet * set          /** The set to use */
)
{
    if( PBL_SET_IS_TREE_SET( set ) )
    {
        return set->size;
    }

    return ( (PblHashSet *)set )->capacity;
}

/**
 * Retrieves, but does not remove, the head (first element) of this set.
 *
 * Retrieving the first element of a hash set has a time complexity of O(N),
 * with N being the capacity of the set.
 *
 * Retrieving the first element of a tree set has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * @return void * retptr != NULL: The first element of this set.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this set is empty.
 */
void * pblSetElement(
PblSet * set         /** The set to use  */
)
{
    return pblSetGet( set, 0 );
}

/**
 * Retrieves, but does not remove, the head (first element) of this set.
 *
 * Retrieving the head of a hash set has a time complexity of O(N),
 * with N being the capacity of the set.
 *
 * Retrieving the head of a tree set has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * @return void * retptr != NULL: The first element of this set.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this set is empty.
 */
void * pblSetHead(
PblSet * set         /** The set to use  */
)
{
    return pblSetGet( set, 0 );
}

/**
 * Retrieves, but does not remove, the head (first element) of this set.
 *
 * Retrieving the head of a hash set has a time complexity of O(N),
 * with N being the capacity of the set.
 *
 * Retrieving the head of a tree set has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * @return void * retptr != NULL: The first element of this set.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this set is empty.
 */
void * pblSetPeek(
PblSet * set      /** The set to use */
)
{
    return pblSetGet( set, 0 );
}

/**
 * Retrieves, but does not remove, the tail (last element) of this set.
 *
 * Retrieving the tail of a hash set has a time complexity of O(N),
 * with N being the capacity of the set.
 *
 * Retrieving the tail of a tree set has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * @return void * retptr != NULL: The last element of this set.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this set is empty.
 */
void * pblSetTop(
PblSet * set    /** The set to use */
)
{
    return pblSetGet( set, set->size - 1 );
}
/**
 * Retrieves and removes the tail (last element) of this set.
 *
 * For tree sets this method has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * For hash sets this method has a time complexity of O(N),
 * with N being the capacity of the set.
 *
 * @return void * retptr != NULL: The last element of this set.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this set is empty.
 */
void * pblSetPop(
PblSet * set     /** The set to use */
)
{
    return pblSetRemoveAt( set, set->size - 1 );
}

/**
 * Retrieves and removes the head (first element) of this set.
 *
 * For tree sets this method has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * For hash sets this method has a time complexity of O(N),
 * with N being the capacity of the set.
 *
 * @return void * retptr != NULL: The element that was removed.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this set is empty.
 */
void * pblSetPoll(
PblSet * set      /** The set to use                */
)
{
    return pblSetRemoveAt( set, 0 );
}

/**
 * Retrieves and removes the head (first element) of this set.
 *
 * For tree sets this method has a time complexity of O(Log N),
 * with N being the size of the set.
 *
 * For hash sets this method has a time complexity of O(N),
 * with N being the capacity of the set.
 *
 * @return void * retptr != NULL: The element that was removed.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - this set is empty.
 */
void * pblSetRemove(
PblSet * set          /** The set to use */
)
{
    return pblSetRemoveAt( set, 0 );
}

/**
 * Removes from this set all of its elements
 * that are contained in the specified collection.
 *
 * For tree sets this method has a time complexity of O(M * Log N),
 * with N being the size of the set and M being the size of the
 * collection.
 *
 * For hash sets this method has a complexity of O(M),
 * with M being the size of the collection.
 *
 * @return int rc >  0: If this set changed as a result of the call.
 * @return int rc == 0: This set did not change.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_COLLECTION        - The collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The collection was modified concurrently.
 */
int pblSetRemoveAll(
PblSet * set,      /** The set to use                                                 */
void * collection  /** The collection whose elements are to be removed from this set. */
)
{
    PblIterator   iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int hasNext;
    int rc = 0;

    if( set->size < 1 )
    {
        return 0;
    }

    if( pblIteratorInit( collection, iterator ) < 0 )
    {
        return -1;
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
        if( pblSetRemoveElement( set, element ))
        {
            rc = 1;
        }
        if( set->size < 1 )
        {
            return rc;
        }
    }
    if( hasNext < 0 )
    {
        // concurrent modification on the collection
        //
        return -1;
    }

    return rc;
}

/**
 * Removes the element at the specified position in this set.
 *
 * For hash sets removing from a position of the set has a time
 * complexity of O(N), with N being the capacity of the set.
 *
 * For tree sets removing from the beginning or the end of the set has a time complexity
 * of O(Log N), while removing from a random position in the middle of the set has a time
 * complexity of O(N), with N being the number of elements in the set.
 *
 * @return void * retptr != NULL: The element that was removed.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Index is out of range (index < 0 || index >= size()).
 */
void * pblSetRemoveAt(
PblSet * set,         /** The set to use                                  */
int index             /** The index at which the element is to be removed */
)
{
    void * element = pblSetGet( set, index );
    if( NULL == element )
    {
        return NULL;
    }

    if( !pblSetRemoveElement( set, element ) )
    {
        return NULL;
    }
    return element;
}

/**
 * Tests if the object is a set.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc != 0: This object is a set.
 * @return int rc == 0: This object is not a set.
 */
int pblSetIsSet(
void * object      /** The object to test */
)
{
    return PBL_SET_IS_SET(object);
}

/**
 * Tests if the object is a hash set.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc != 0: This object is a hash set.
 * @return int rc == 0: This object is not a hash set.
 */
int pblSetIsHashSet(
void * object           /** The object to test */
)
{
    return PBL_SET_IS_HASH_SET(object);
}

/**
 * Tests if the object is a tree set.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc != 0: This object is a tree set.
 * @return int rc == 0: This object is not a tree set.
 */
int pblSetIsTreeSet(
void * object            /** The object to test */
)
{
    return PBL_SET_IS_TREE_SET(object);
}

/**
 * Returns the number of elements in this set.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc: The number of elements in this set.
 */
int pblSetSize(
PblSet * set   /** The set to use */
)
{
    return set->size;
}

/**
 * Returns the index of the given argument in the set.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the set.
 *
 * @return int rc >= 0: The index of the specified element.
 * @return int rc <  0: The specified element is not present.
 */
int pblSetIndexOf(
PblSet * set,      /** The set to use      */
void * element     /** Element to look for  */
)
{
    PblIterator iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int index;
    void * setElement;

    /*
     * See whether the element is in the set at all, before looking at the index
     */
    if( pblSetContains( set, element ) == 0 )
    {
        return -1;
    }

    if( pblIteratorInit( set, iterator ) < 0 )
    {
        return -1;
    }

    for( index = 0; pblIteratorHasNext( iterator ) > 0; index++ )
    {
        setElement = pblIteratorNext( iterator );
        if( setElement == (void*)-1 )
        {
            // concurrent modification on the collection
            //
            return -1;
        }
        if( !pblCollectionElementCompare( (PblCollection*)set, element,
                                          setElement ) )
        {
            return index;
        }
    }

    return -1;
}

/**
 * Returns the index of the given argument in the set.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the set.
 *
 * @return int rc >= 0: The index of the specified element.
 * @return int rc <  0: The specified element is not present.
 */
int pblSetLastIndexOf(
PblSet * set,      /** The set to use      */
void * element     /** Element to look for  */
)
{
    return pblSetIndexOf( set, element );
}

/**
 * Tests if this set has no elements.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc != 0: This set has no elements.
 * @return int rc == 0: This set has elements.
 */
int pblSetIsEmpty(
PblSet * set      /** The set to test */
)
{
    return 0 == set->size;
}

/**
 * Compares the specified collection with this set for equality.
 *
 * Returns true if the specified collection is a collection,
 * the two collections have the same size
 * and every member of the specified collection is contained in this set.
 *
 * In other words, two collections are defined to be equal as sets
 * if they contain the same elements.
 *
 * For hash sets this method has a time complexity of O(M)
 * and for tree sets this method has a time complexity of O(M * Log N ),
 * with N being the size of the set and M being the
 * number of elements in the object compared.
 *
 * @return int rc >  0: The specified collection is equal to this set.
 * @return int rc == 0: The specified collection is not equal to this set.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_PARAM_COLLECTION        - The collection cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The collection was modified concurrently.
 */
int pblSetEquals(
PblSet * set,     /** The set to compare with.                                  */
void * collection /** The collection to be compared for equality with this set. */
)
{
    PblIterator iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;

    int hasNext;
    void * element;

    if( set == collection )
    {
        return 1;
    }

    if( !PBL_COLLECTION_IS_COLLECTION( collection ) )
    {
        pbl_errno = PBL_ERROR_PARAM_COLLECTION;
        return -1;
    }

    if( pblIteratorInit( (PblCollection *)collection, iterator ) < 0 )
    {
        return -1;
    }

    if( pblIteratorSize( iterator ) != set->size )
    {
        return 0;
    }

    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        element = pblIteratorNext( iterator );
        if( element == (void*)-1 )
        {
            return -1;
        }

        if( !pblCollectionContains( set, element ) )
        {
            return 0;
        }
    }
    if( hasNext < 0 )
    {
        return -1;
    }

    return 1;
}

/*
 * Print the tree for debuging.
 *
 * Assumes the 'element' can be printed as a string with %s.
 */
void pblTreeNodePrint( FILE * outfile, int level, PblTreeNode * node )
{
    int i;

    if( !node )
    {
        fprintf( outfile, "# " );
        for( i = 0; i < level; i++ )
        {
            fprintf( outfile, " " );
        }
        fprintf( outfile, "- %d, %s\n", level, "Node empty" );
        fflush( outfile );
        return;
    }

    if( node->prev )
    {
        pblTreeNodePrint( outfile, level + 1, node->prev );
    }

    fprintf( outfile, "# " );
    for( i = 0; i < level; i++ )
    {
        fprintf( outfile, " " );
    }
    fprintf( outfile, "- %d, %s\n", level, (char*)node->element );
    fflush( outfile );

    if( node->next )
    {
        pblTreeNodePrint( outfile, level + 1, node->next );
    }

}

/*
 * Print the tree set for debuging.
 *
 * Assumes the 'element' can be printed as a string with %s.
 */
static void pblTreeSetPrint( FILE * outfile,PblTreeSet * set )
{
    pblTreeNodePrint( outfile, 0, set->rootNode );
}

/*
 * Print the hash for debuging.
 *
 * Assumes the 'element' can be printed as a string with %s.
 */
void pblHashSetPrint( FILE * outfile, PblHashSet * set )
{
    int i;

    fprintf( outfile, "# size %d\n", set->genericSet.size );
    fprintf( outfile, "# capacity %d\n", set->capacity );
    fprintf( outfile, "# step size %d\n", set->stepSize );

    for( i = 0; i < set->capacity; i++ )
    {
        unsigned char * ptr = set->pointerArray[i];
        if( !ptr )
        {
            continue;
        }
        fprintf( outfile, "# i %d, hashval %d, %s\n", i, set->hashValue( ptr ) & ( set->capacity - 1 ), ptr );
    }
}

/*
 * Print the set for debuging.
 *
 * Assumes the 'element' can be printed as a string with %s.
 */
void pblSetPrint( FILE * outfile, PblSet * set )
{
    if( PBL_SET_IS_TREE_SET( set ))
    {
        pblTreeSetPrint( outfile, (PblTreeSet *)set );
    }

    if( PBL_SET_IS_HASH_SET( set ))
    {
        pblHashSetPrint( outfile, (PblHashSet *)set );
    }

}

/**
 * Returns an iterator over the elements in this set.
 *
 * The iterator starts the iteration at the beginning of the set.
 *
 * <B>Note</B>: The memory allocated by this method for the iterator returned needs to be released
 *              by calling pblIteratorFree() once the iterator is no longer needed.
 *
 * The iterators returned by the this method are fail-fast:
 * if the set is structurally modified at any time after the iterator is created,
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
 * <BR>PBL_ERROR_PARAM_SET     - The set cannot be iterated.
 */
PblIterator * pblSetIterator(
PblSet * set                 /** The set to create the iterator for */
)
{
    if( !PBL_SET_IS_SET( set ) )
    {
        pbl_errno = PBL_ERROR_PARAM_SET;
        return NULL;
    }

    return pblIteratorNew( set );
}

/**
 * Returns a reverse iterator over the elements in this set.
 *
 * The reverse iterator starts the iteration at the end of the set.
 *
 * <B>Note:</B> The memory allocated by this method for the iterator returned needs to be released
 *       by calling pblIteratorFree() once the iterator is no longer needed.
 *
 * The iterators returned by the this method are fail-fast:
 * if the set is structurally modified at any time after the iterator is created,
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
 * <BR>PBL_ERROR_PARAM_SET     - The set cannot be iterated.
 */
PblIterator * pblSetReverseIterator(
PblSet * set                 /** The set to create the iterator for */
)
{
    if( !PBL_SET_IS_SET( set ) )
    {
        pbl_errno = PBL_ERROR_PARAM_SET;
        return NULL;
    }

    return pblIteratorReverseNew( set );
}

/**
 * Returns an array containing all of the elements in this set.
 *
 * <B>Note:</B> The pointer array returned is malloced from heap, the caller has to free it
 * after it is no longer needed!
 *
 * The size of the pointer array malloced and returned is defined by the pblSetSize()
 * of the set.
 *
 * This method has a time complexity of O(N),
 * with N being the size of the list.
 *
 * @return void * retptr != NULL: The array containing the elements of the set.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY    - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS    - The set is empty.
 * <BR>PBL_ERROR_PARAM_COLLECTION - The set cannot be iterated.
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The set was modified concurrently.
 */
void ** pblSetToArray(
PblSet * set           /** The set to use */
)
{
    void ** resultArray;
    PblIterator iteratorBuffer;
    PblIterator * iterator = &iteratorBuffer;
    int index = 0;
    void * element;
    int hasNext;

    if( set->size == 0 )
    {
        pbl_errno = PBL_ERROR_OUT_OF_BOUNDS;
        return NULL;
    }

    if( pblIteratorInit( set, iterator ) < 0 )
    {
        return NULL;
    }

    resultArray = (void **)pbl_malloc( "pblSetToArray", sizeof(void*) * set->size );
    if( !resultArray )
    {
        return NULL;
    }

    while( ( hasNext = pblIteratorHasNext( iterator ) ) > 0 )
    {
        element = pblIteratorNext( iterator );
        if( element == (void*)-1 )
        {
            // concurrent modification on the collection
            //
            PBL_FREE( resultArray );
            return NULL;
        }
        resultArray[ index++ ] = element;
    }
    if( hasNext < 0 )
    {
        PBL_FREE( resultArray );
        return NULL;
    }

    return resultArray;
}
