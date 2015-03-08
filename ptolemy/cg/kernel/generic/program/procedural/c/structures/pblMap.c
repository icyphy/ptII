/*
 pblMap.c - C implementation of a Map similar
 to the Java Map.

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

 $Log: pblMap.c,v $
 Revision 1.7  2010/05/30 20:06:45  peter
 Removed warnings found by 'Microsoft Visual C++ 2010'.

 Revision 1.6  2010/05/20 21:42:53  peter
 Added pblSetReplace.

 Revision 1.5  2010/05/19 22:38:45  peter
 Testing the map.

 Revision 1.4  2010/05/16 20:57:24  peter
 Working on maps

 Revision 1.3  2010/05/16 01:06:45  peter
 More work on maps.

 Revision 1.2  2010/05/15 16:33:43  peter
 Some fixes.

 Revision 1.1  2010/05/15 16:26:10  peter
 Exposing the map interface.

 */

/*
 * Make sure "strings <exe> | grep Id | sort -u" shows the source file versions
 */
char* pblMap_c_id = "$Id$";

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
#define PBL_MAP_ENTRY_TAG               1
#define PBL_MAP_KEY_TAG                 2

/*****************************************************************************/
/* Globals                                                                   */
/*****************************************************************************/

/*****************************************************************************/
/* Function declarations                                                     */
/*****************************************************************************/

/*****************************************************************************/
/* Functions                                                                 */
/*****************************************************************************/

/*
 * Hash value function used for map entries.
 *
 * @return int rc: The hash value of the entry that was passed.
 */
static int pblMapEntryHashValue( /*                                          */
const void *element /**                 Element to calculate hash value for  */
)
{
    PblMapEntry * entry = (PblMapEntry*)element;

    if( !entry || 0 == entry->keyLength )
    {
        return 0;
    }

    if( entry->tag == PBL_MAP_ENTRY_TAG )
    {
        return pblHtHashValue( (unsigned char *)entry->buffer, entry->keyLength );
    }
    else
    {
        PblMapKey * key = (PblMapKey*)element;
        return pblHtHashValue( (unsigned char *)key->key, key->keyLength );
    }
}

/*
 * Compares two map entries.
 *
 * Used as compare function for maps.
 *
 * @return int rc  < 0: left is smaller than right
 * @return int rc == 0: left and right are equal
 * @return int rc  > 0: left is greater than right
 */
static int pblMapEntryCompareFunction( /*                                    */
const void * left, /*                     The left value for the comparison  */
const void * right /*                     The right value for the comparison */
)
{
    PblMapEntry * leftEntry = *(PblMapEntry**)left;
    PblMapEntry * rightEntry = *(PblMapEntry**)right;

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
    if( leftEntry->tag == PBL_MAP_ENTRY_TAG )
    {
        if( rightEntry->tag == PBL_MAP_ENTRY_TAG )
        {
            return pbl_memcmp( leftEntry->buffer, leftEntry->keyLength,
                               rightEntry->buffer, rightEntry->keyLength );
        }
        else
        {
            PblMapKey * rightKey = *(PblMapKey**)right;

            return pbl_memcmp( leftEntry->buffer, leftEntry->keyLength,
                               rightKey->key, rightKey->keyLength );
        }
    }
    else
    {
        PblMapKey * leftKey = *(PblMapKey**)left;

        if( rightEntry->tag == PBL_MAP_ENTRY_TAG )
        {
            return pbl_memcmp( leftKey->key, leftKey->keyLength,
                               rightEntry->buffer, rightEntry->keyLength );
        }
        else
        {
            PblMapKey * rightKey = *(PblMapKey**)right;

            return pbl_memcmp( leftKey->key, leftKey->keyLength, rightKey->key,
                               rightKey->keyLength );
        }
    }
}

/**
 * Creates a new tree map.
 *
 * This method has a time complexity of O(1).
 *
 * @return pblMap * retPtr != NULL: A pointer to the new map.
 * @return pblMap * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
PblMap * pblMapNewTreeMap( void )
{
    PblMap * pblMap = (PblMap *)pbl_malloc0( "pblMapNewTreeMap", sizeof(PblMap) );
    if( !pblMap )
    {
        return NULL;
    }

    pblMap->entrySet = pblSetNewTreeSet();
    if( !pblMap->entrySet )
    {
        PBL_FREE( pblMap );
        return NULL;
    }

    pblSetSetCompareFunction( pblMap->entrySet, pblMapEntryCompareFunction );
    pblSetSetHashValueFunction( pblMap->entrySet, pblMapEntryHashValue );

    return pblMap;
}

/**
 * Creates a new hash map.
 *
 * This method has a time complexity of O(1).
 *
 * @return PblMap * retPtr != NULL: A pointer to the new map.
 * @return PblMap * retPtr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
PblMap * pblMapNewHashMap( void )
{
    PblMap * pblMap = (PblMap *)pbl_malloc0( "pblMapNewHashMap", sizeof(PblMap) );
    if( !pblMap )
    {
        return NULL;
    }

    pblMap->entrySet = pblSetNewHashSet();
    if( !pblMap->entrySet )
    {
        PBL_FREE( pblMap );
        return NULL;
    }

    pblSetSetCompareFunction( pblMap->entrySet, pblMapEntryCompareFunction );
    pblSetSetHashValueFunction( pblMap->entrySet, pblMapEntryHashValue );

    return pblMap;
}

/**
 * Removes all of the mappings from this map. The map will be empty after this call returns.
 *
 * <B>Note:</B> The memory of the entries cleared is freed.
 *
 * For hash maps this method has a time complexity of O(N).
 * For tree maps this method has a time complexity of O(N * Log N).
 *
 * @return void
 */
void pblMapClear( /*                                               */
PblMap * map /**                                  The map to clear */
)
{
    void * ptr;
    while( pblSetSize( map->entrySet ) > 0 )
    {
        ptr = pblSetRemove( map->entrySet );
        PBL_FREE(ptr);
    }
}

/**
 * Removes all of the mappings from this map and frees the map's memory from heap.
 *
 * For hash maps this method has a time complexity of O(N).
 * For tree maps this method has a time complexity of O(N * Log N).
 *
 * @return void
 */
void pblMapFree( /*                                              */
PblMap * map /**                                 The map to free */
)
{
    pblMapClear( map );
    pblSetFree( map->entrySet );
    PBL_FREE(map);
}

/**
 * Returns true if this map contains a mapping for the specified string key.
 *
 * More formally, returns true if and only if this map contains a mapping for a key k such that
 * (key==null ? k==null : memcmp( key, k, keyLength ) == 0. (There can be at most one such mapping.)
 *
 * For hash maps his method has a time complexity of O(1).
 * For tree maps this method has a time complexity of O(Log N).
 *
 * @return int rc >  0: The map contains a mapping for the specified key.
 * @return int rc == 0: The map did not contain a mapping for the key.
 */
int pblMapContainsKeyStr( /*                                                 */
PblMap * map, /**             The map to check                               */
char * key /**                Key whose presence in this map is to be tested */
)
{
    return pblMapContainsKey( map, key, key ? 1 + strlen( key ) : 0 );
}

/**
 * Returns true if this map contains a mapping for the specified key.
 *
 * More formally, returns true if and only if this map contains a mapping for a key k such that
 * (key==null ? k==null : memcmp( key, k, keyLength ) == 0. (There can be at most one such mapping.)
 *
 * For hash maps his method has a time complexity of O(1).
 * For tree maps this method has a time complexity of O(Log N).
 *
 * @return int rc >  0: The map contains a mapping for the specified key.
 * @return int rc == 0: The map did not contain a mapping for the key.
 */
int pblMapContainsKey( /*                                                    */
PblMap * map, /**             The map to check                               */
void * key, /**               Key whose presence in this map is to be tested */
size_t keyLength /**          Length of the key                              */
)
{
    PblMapKey mapKey;

    mapKey.tag = PBL_MAP_KEY_TAG;
    mapKey.keyLength = keyLength;
    mapKey.key = key;

    return pblSetContains( map->entrySet, &mapKey );
}

/**
 * Returns true if this map contains a mapping for the specified string value.
 *
 * This method has a time complexity of O(N).
 *
 * @return int rc >  0: The map contains a mapping for the specified value.
 * @return int rc == 0: The map did not contain a mapping for the value.
 * @return int rc <  0:  An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The underlying collection was modified concurrently.
 */
int pblMapContainsValueStr( /*                                               */
PblMap * map, /**           The map to check                                 */
char * value /**            Value whose presence in this map is to be tested */
)
{
    return pblMapContainsValue( map, value, value ? 1 + strlen( value ) : 0 );
}

/**
 * Returns true if this map contains a mapping for the specified value.
 *
 * This method has a time complexity of O(N).
 *
 * @return int rc >  0: The map contains a mapping for the specified value.
 * @return int rc == 0: The map did not contain a mapping for the value.
 * @return int rc <  0:  An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The underlying collection was modified concurrently.
 */
int pblMapContainsValue( /*                                                  */
PblMap * map, /**           The map to check                                 */
void * value, /**           Value whose presence in this map is to be tested */
size_t valueLength /**      Length of the value                              */
)
{
    int hasNext;
    void * element;
    PblMapEntry * entry;

    PblIterator iterator;
    pblIteratorInit( map->entrySet, &iterator );

    while( ( hasNext = pblIteratorHasNext( &iterator ) ) > 0 )
    {
        element = pblIteratorNext( &iterator );
        if( element == (void*)-1 )
        {
            // Concurrent modification
            //
            return -1;
        }

        entry = (PblMapEntry *)element;
        if( !entry )
        {
            continue;
        }

        if( entry->valueLength != valueLength )
        {
            continue;
        }

        if( 0 == valueLength )
        {
            return 1;
        }

        if( 0 == memcmp( value, entry->buffer + entry->keyLength, valueLength ) )
        {
            return 1;
        }
    }

    return 0;
}

/**
 * Returns the value to which the specified string key is mapped,
 * or null if this map contains no mapping for the key.
 *
 * More formally, if this map contains a mapping from a key k to a value v such that
 * (key==null ? k==null : memcmp( key, k, keyLength ) == 0,
 * then this method returns v; otherwise it returns null.
 * (There can be at most one such mapping.)
 *
 * For hash maps this method has a time complexity of O(1).
 * For tree maps this method has a time complexity of O(Log N).
 *
 * @return void * retptr != NULL: The associated value.
 * @return void * retptr == NULL: There is no associated value.
 */
void * pblMapGetStr( /*                                                   */
PblMap * map, /**            The map to check                             */
char * key, /**              Key whose associated value is to be returned */
size_t * valueLengthPtr /**  Out: Length of the value returned            */
)
{
    return pblMapGet( map, key, key ? 1 + strlen( key ) : 0, valueLengthPtr );
}

/**
 * Returns the value to which the specified key is mapped,
 * or null if this map contains no mapping for the key.
 *
 * More formally, if this map contains a mapping from a key k to a value v such that
 * (key==null ? k==null : memcmp( key, k, keyLength ) == 0,
 * then this method returns v; otherwise it returns null.
 * (There can be at most one such mapping.)
 *
 * For hash maps this method has a time complexity of O(1).
 * For tree maps this method has a time complexity of O(Log N).
 *
 * @return void * retptr != NULL: The associated value.
 * @return void * retptr == NULL: There is no associated value.
 */
void * pblMapGet( /*                                                      */
PblMap * map, /**            The map to check                             */
void * key, /**              Key whose associated value is to be returned */
size_t keyLength, /**        Length of the key                            */
size_t * valueLengthPtr /**  Out: Length of the value returned            */
)
{
    PblMapEntry * mapEntry;
    PblMapKey mapKey;

    mapKey.tag = PBL_MAP_KEY_TAG;
    mapKey.keyLength = keyLength;
    mapKey.key = key;

    mapEntry = (PblMapEntry *)pblSetGetElement( map->entrySet, &mapKey );
    if( !mapEntry )
    {
        if( valueLengthPtr )
        {
            *valueLengthPtr = 0;
        }
        return NULL;
    }

    if( valueLengthPtr )
    {
        *valueLengthPtr = mapEntry->valueLength;
    }

    return mapEntry->buffer + mapEntry->keyLength;
}

/**
 * Associates the specified string value with the specified string key in this map.
 *
 * If the map previously contained a mapping for the key, the old value is replaced by the specified value.
 * (A map m is said to contain a mapping for a key k if and only if pblMapContainsKey(k) would return true.)
 *
 * For hash maps this method has a time complexity of O(1).
 * For tree maps this method has a time complexity of O(Log N).
 *
 * @return int rc >  0: The map did not already contain a mapping for the key.
 * @return int rc == 0: The map did already contain a mapping for the key.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Maximum capacity of the hash set exceeded.
 */
int pblMapAddStrStr( /*                                       */
PblMap * map, /**                   The map to add to         */
char * key, /**                     Key to add a mapping for  */
char * value /**                    Value of the new mapping  */
)
{
    return pblMapAdd( map, key, key ? 1 + strlen( key ) : 0, value, value ? 1
            + strlen( value ) : 0 );
}

/*
 * Creates a new map entry
 *
 * @return void * retptr != NULL: The new entry.
 * @return void * retptr == NULL: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
static PblMapEntry * pblMapEntryNew( void * key, /**                     Key to add a mapping for  */
size_t keyLength, /**               Length of the key         */
void * value, /**                   Value of the new mapping  */
size_t valueLength /**              Length of the value       */
)
{
    PblMapEntry * newEntry = (PblMapEntry *)pbl_malloc( "pblMapEntryNew", sizeof(PblMapEntry)
            + keyLength + valueLength );
    if( !newEntry )
    {
        return NULL;
    }

    newEntry->tag = PBL_MAP_ENTRY_TAG;
    newEntry->keyLength = keyLength;
    if( keyLength > 0 )
    {
        memcpy( newEntry->buffer, key, keyLength);
    }
    newEntry->valueLength = valueLength;
    if( valueLength > 0 )
    {
        memcpy( newEntry->buffer + keyLength, value, valueLength );
    }
    return newEntry;
}

/**
 * Associates the specified value with the specified key in this map.
 *
 * If the map previously contained a mapping for the key, the old value is replaced by the specified value.
 * (A map m is said to contain a mapping for a key k if and only if pblMapContainsKey(k) would return true.)
 *
 * For hash maps this method has a time complexity of O(1).
 * For tree maps this method has a time complexity of O(Log N).
 *
 * @return int rc >  0: The map did not already contain a mapping for the key.
 * @return int rc == 0: The map did already contain a mapping for the key.
 * @return int rc <  0: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Maximum capacity of the hash set exceeded.
 */
int pblMapAdd( /*                                             */
PblMap * map, /**                   The map to add to         */
void * key, /**                     Key to add a mapping for  */
size_t keyLength, /**               Length of the key         */
void * value, /**                   Value of the new mapping  */
size_t valueLength /**              Length of the value       */
)
{
    int rc;
    PblMapEntry * mapEntry;
    PblMapEntry * newEntry =
            pblMapEntryNew( key, keyLength, value, valueLength );
    if( !newEntry )
    {
        return -1;
    }

    mapEntry = (PblMapEntry *)pblSetReplaceElement( map->entrySet, newEntry );
    if( mapEntry )
    {
        PBL_FREE( mapEntry );
        return 0;
    }

    rc = pblSetAdd( map->entrySet, newEntry );
    if( rc < 0 )
    {
        PBL_FREE(newEntry);
        return -1;
    }

    return 1;
}

/**
 * Associates the specified string value with the specified string key in this map.
 *
 * If the map previously contained a mapping for the key, the old value is replaced by the specified value.
 * (A map m is said to contain a mapping for a key k if and only if pblMapContainsKey(k) would return true.)
 *
 * Returns the previous value associated with key, NULL if there was no mapping for key
 * or (void*)-1 in case of an error.
 *
 * Note: If a valid pointer to a value is returned, the value returned is
 * malloced on the heap. It is the caller's responsibility to free that memory
 * once it is no longer needed.
 *
 * For hash maps this method has a time complexity of O(1).
 * For tree maps this method has a time complexity of O(Log N).
 *
 * @return void * retptr != (void*)-1: The previously associated value.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Maximum capacity of the hash set exceeded.
 */
void * pblMapPutStrStr( /*                                             */
PblMap * map, /**                                    The map to add to */
char * key, /**                               Key to add a mapping for */
char * value, /**                             Value of the new mapping */
size_t * valueLengthPtr /**          Out: Length of the value returned */
)
{
    return pblMapPut( map, key, key ? 1 + strlen( key ) : 0, value, value ? 1
            + strlen( value ) : 0, valueLengthPtr );
}

/**
 * Associates the specified value with the specified key in this map.
 *
 * If the map previously contained a mapping for the key, the old value is replaced by the specified value.
 * (A map m is said to contain a mapping for a key k if and only if pblMapContainsKey(k) would return true.)
 *
 * Returns the previous value associated with key, NULL if there was no mapping for key
 * or (void*)-1 in case of an error.
 *
 * Note: If a valid pointer to a value is returned, the value returned is
 * malloced on the heap. It is the caller's responsibility to free that memory
 * once it is no longer needed.
 *
 * For hash maps this method has a time complexity of O(1).
 * For tree maps this method has a time complexity of O(Log N).
 *
 * @return void * retptr != (void*)-1: The previously associated value.
 * @return void * retptr == NULL: There was no previously associated value.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 * <BR>PBL_ERROR_OUT_OF_BOUNDS - Maximum capacity of the hash set exceeded.
 */
void * pblMapPut( /*                                                   */
PblMap * map, /**                                    The map to add to */
void * key, /**                               Key to add a mapping for */
size_t keyLength, /**                                Length of the key */
void * value, /**                             Value of the new mapping */
size_t valueLength, /**                            Length of the value */
size_t * valueLengthPtr /**          Out: Length of the value returned */
)
{
    int rc;
    PblMapEntry * mapEntry;
    PblMapEntry * newEntry =
            pblMapEntryNew( key, keyLength, value, valueLength );
    if( !newEntry )
    {
        return (void*)-1;
    }

    mapEntry = (PblMapEntry *)pblSetReplaceElement( map->entrySet, newEntry );
    if( mapEntry )
    {
        void * retptr;

        if( mapEntry->valueLength > 0 )
        {
            retptr = pbl_memdup( "pblMapPut", mapEntry->buffer
                    + mapEntry->keyLength, mapEntry->valueLength );
        }
        else
        {
            retptr = pbl_malloc0( "pblMapPut0", 1 );
        }
        if( !retptr )
        {
            if( valueLengthPtr )
            {
                *valueLengthPtr = 0;
            }
            pblSetReplaceElement( map->entrySet, mapEntry );
            PBL_FREE( newEntry );
            return (void*)-1;
        }

        if( valueLengthPtr )
        {
            *valueLengthPtr = mapEntry->valueLength;
        }
        PBL_FREE( mapEntry );
        return retptr;
    }

    if( valueLengthPtr )
    {
        *valueLengthPtr = 0;
    }

    rc = pblSetAdd( map->entrySet, newEntry );
    if( rc < 0 )
    {
        PBL_FREE(newEntry);
        return (void*)-1;
    }

    return NULL;
}

/**
 * Copies all of the mappings from the specified source map to this map.
 * These mappings will replace any mappings that this map had for any
 * of the keys currently in the specified map.
 *
 * For hash maps this method has a time complexity of O(M).
 * For tree maps this method has a time complexity of O(M * Log N).
 * With M being the number of elements in the source map and
 * N being the number of elements in the target map.
 *
 * @return int rc == 0: Ok.
 * @return int rc <  0:  An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_CONCURRENT_MODIFICATION - The source map was modified concurrently.
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
int pblMapPutAll( /*                                            */
PblMap * map, /**                The map to copy the entries to */
PblMap * sourceMap /**         The map to copy the entries from */
)
{
    int hasNext;
    void * element;
    PblMapEntry * entry;

    PblIterator iterator;
    pblIteratorInit( sourceMap->entrySet, &iterator );

    while( ( hasNext = pblIteratorHasNext( &iterator ) ) > 0 )
    {
        element = pblIteratorNext( &iterator );
        if( element == (void*)-1 )
        {
            // Concurrent modification
            //
            return -1;
        }

        entry = (PblMapEntry *)element;
        if( !entry )
        {
            continue;
        }

        if( pblMapAdd( map, entry->buffer, entry->keyLength, entry->buffer
                + entry->keyLength, entry->valueLength ) < 0 )
        {
            return -1;
        }
    }

    return 0;
}

/**
 * Removes the mapping for this string key from this map if it is present.
 *
 * More formally, if this map contains a mapping from a key k to a value v such that
 * (key==null ? k==null : memcmp( key, k, keyLength ) == 0,
 * that mapping is removed.
 * (The map can contain at most one such mapping.)
 *
 * Returns the previous value associated with key,
 * NULL if there was no mapping for key
 * or (void*)-1 in case of an error.
 *
 * Note: If a valid pointer to a value is returned, the value returned is
 * malloced on the heap. It is the caller's responsibility to free that memory
 * once it is no longer needed.
 *
 * For hash maps this method has a time complexity of O(1).
 * For tree maps this method has a time complexity of O(Log N).
 *
 * @return void * retptr != (void*)-1: The previously associated value.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
void * pblMapRemoveStr( /*                                          */
PblMap * map, /**                            The map to remove from */
char * key, /**                    Key whose association is removed */
size_t * valueLengthPtr /**       Out: Length of the value returned */
)
{
    return pblMapRemove( map, key, key ? 1 + strlen( key ) : 0, valueLengthPtr );
}

/**
 * Removes the mapping for this key from this map if it is present.
 *
 * More formally, if this map contains a mapping from a key k to a value v such that
 * (key==null ? k==null : memcmp( key, k, keyLength ) == 0,
 * that mapping is removed.
 * (The map can contain at most one such mapping.)
 *
 * Returns the previous value associated with key,
 * NULL if there was no mapping for key
 * or (void*)-1 in case of an error.
 *
 * Note: If a valid pointer to a value is returned, the value returned is
 * malloced on the heap. It is the caller's responsibility to free that memory
 * once it is no longer needed.
 *
 * For hash maps this method has a time complexity of O(1).
 * For tree maps this method has a time complexity of O(Log N).
 *
 * @return void * retptr != (void*)-1: The previously associated value.
 * @return void * retptr == (void*)-1: An error, see pbl_errno:
 *
 * <BR>PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */
void * pblMapRemove( /*                                             */
PblMap * map, /**                            The map to remove from */
void * key, /**                    Key whose association is removed */
size_t keyLength, /**                             Length of the key */
size_t * valueLengthPtr /**       Out: Length of the value returned */
)
{
    PblMapEntry * mapEntry;
    void * retptr = NULL;
    PblMapKey mapKey;

    mapKey.tag = PBL_MAP_KEY_TAG;
    mapKey.keyLength = keyLength;
    mapKey.key = key;

    mapEntry = (PblMapEntry *)pblSetGetElement( map->entrySet, &mapKey );
    if( !mapEntry )
    {
        if( valueLengthPtr )
        {
            *valueLengthPtr = 0;
        }
        return NULL;
    }

    if( mapEntry->valueLength > 0 )
    {
        retptr = pbl_memdup( "pblMapRemove", mapEntry->buffer
                + mapEntry->keyLength, mapEntry->valueLength );
    }
    else
    {
        retptr = pbl_malloc0( "pblMapRemove0", 1 );
    }
    if( !retptr )
    {
        if( valueLengthPtr )
        {
            *valueLengthPtr = 0;
        }
        return (void*)-1;
    }

    if( valueLengthPtr )
    {
        *valueLengthPtr = mapEntry->valueLength;
    }

    pblSetRemoveElement( map->entrySet, mapEntry );

    PBL_FREE( mapEntry );

    return retptr;
}

/**
 * Tests if this map has no elements.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc != 0: This map has no elements.
 * @return int rc == 0: This map has elements.
 */
int pblMapIsEmpty( /*                               */
PblMap * map /**                    The map to test */
)
{
    return 0 == map->entrySet->size;
}

/**
 * Returns the number of entries in this map.
 *
 * This method has a time complexity of O(1).
 *
 * @return int rc: The number of entries in this map.
 */
int pblMapSize( /*                              */
PblMap * map /**                 The map to use */
)
{
    return map->entrySet->size;
}

/**
 * Returns an iterator over the map entries in this map in proper sequence.
 *
 * The iterator starts the iteration at the beginning of the map.
 *
 * The elements returned by the pblIteratorNext() and pblIteratorPrevious() calls to
 * this iterator are of type 'PblMapEntry *'. Use the methods pblMapEntryKeyLength(),
 * pblMapEntryKey(), pblMapEntryValueLength() and pblMapEntryValue() to retrieve
 * the attributes of the map entries.
 *
 * <B>Note</B>: The memory allocated by this method for the iterator returned needs to be released
 *              by calling pblIteratorFree() once the iterator is no longer needed.
 *
 * The iterators returned by the this method are fail-fast:
 * if the map is structurally modified at any time after the iterator is created,
 * in any way, the iterator will return a PBL_ERROR_CONCURRENT_MODIFICATION error.
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
 * <BR>PBL_ERROR_PARAM_COLLECTION    - The map cannot be iterated.
 */
PblIterator * pblMapIteratorNew( /*                 */
PblMap * map /** The map to create the iterator for */
)
{
    return pblIteratorNew( map->entrySet );
}

/**
 * Returns a reverse iterator over the elements in this map in proper sequence.
 *
 * The reverse iterator starts the iteration at the end of the map.
 *
 * The elements returned by the pblIteratorNext() and pblIteratorPrevious() calls to
 * this iterator are of type 'PblMapEntry *'. Use the methods pblMapEntryKeyLength(),
 * pblMapEntryKey(), pblMapEntryValueLength() and pblMapEntryValue() to retrieve
 * the attributes of the map entries.
 *
 * <B>Note</B>: The memory allocated by this method for the iterator returned needs to be released
 *              by calling pblIteratorFree() once the iterator is no longer needed.
 *
 * The iterators returned by the this method are fail-fast:
 * if the map is structurally modified at any time after the iterator is created,
 * in any way, the iterator will return a PBL_ERROR_CONCURRENT_MODIFICATION error.
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
 * <BR>PBL_ERROR_PARAM_COLLECTION    - The map cannot be iterated.
 */
PblIterator * pblMapIteratorReverseNew( /*          */
PblMap * map /** The map to create the iterator for */
)
{
    return pblIteratorReverseNew( map->entrySet );
}

/**
 * Returns the key length of the map entry passed.
 *
 * This method has a time complexity of O(1).
 *
 * @return size_t length: The key length of the map entry.
 */
size_t pblMapEntryKeyLength( /*                   */
PblMapEntry * entry /**                 The entry */
)
{
    return entry->keyLength;
}

/**
 * Returns the value length of the map entry passed.
 *
 * This method has a time complexity of O(1).
 *
 * @return size_t length: The value length of the map entry.
 */
size_t pblMapEntryValueLength( /*                   */
PblMapEntry * entry /**                   The entry */
)
{
    return entry->valueLength;
}

/**
 * Returns a pointer to the key of the map entry passed.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * ptr: The key of the map entry.
 */
void * pblMapEntryKey( /*                         */
PblMapEntry * entry /**                 The entry */
)
{
    return entry->buffer;
}

/**
 * Returns a pointer to the value of the map entry passed.
 *
 * This method has a time complexity of O(1).
 *
 * @return void * ptr: The value of the map entry.
 */
void * pblMapEntryValue( /*                       */
PblMapEntry * entry /**                 The entry */
)
{
    return entry->buffer + entry->keyLength;
}

