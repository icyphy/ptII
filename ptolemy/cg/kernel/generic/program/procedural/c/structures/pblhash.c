/*
 pblhash.c - hash table implementation

 Copyright (C) 2002 - 2009   Peter Graf

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

    $Log: pblhash.c,v $
    Revision 1.18  2010/05/30 20:06:45  peter
    Removed warnings found by 'Microsoft Visual C++ 2010'.

    Revision 1.17  2009/10/20 21:08:00  peter
    Added the pblHtCurrentKey function.

    Revision 1.16  2009/03/15 21:29:29  peter
    *** empty log message ***

    Revision 1.15  2009/03/11 23:48:44  peter
    More tests and clean up.

    Revision 1.14  2009/03/08 20:56:50  peter
    port to gcc (Ubuntu 4.3.2-1ubuntu12) 4.3.2.
    Exposing the hash set and tree set interfaces.


    Revision 1.9  2009/02/03 16:40:14  peter
    PBL vesion 1.04, optimizations,
    MAC OS X port, port to Microsoft Visual C++ 2008 Express Edition,
    exposing the array list and the linked list interface


    Revision 1.3  2003/10/24 23:33:13  peter
    using a faster hash function
    using LRU on the hash bucket lists

    Revision 1.2  2002/09/12 20:46:30  peter
    added the isam file handling to the library

    Revision 1.1  2002/09/05 13:45:01  peter
    Initial revision
*/

/*
 * make sure "strings <exe> | grep Id | sort -u" shows the source file versions
 */
char* pblhash_c_id = "$Id$";

#include <stdio.h>
#ifndef PT_DOES_NOT_HAVE_MEMORY_H
#include <memory.h>
#endif

#ifndef __APPLE__
#ifndef PT_DOES_NOT_HAVE_MALLOC_H
#include <malloc.h>
#endif
#endif

#include "pbl.h"

/*****************************************************************************/
/* #defines                                                                  */
/*****************************************************************************/
#define PBL_HASHTABLE_SIZE      1019

/*****************************************************************************/
/* typedefs                                                                  */
/*****************************************************************************/

typedef struct pbl_hashitem_s
{
    void                  * key;
    size_t                  keylen;

    void                  * data;

    struct pbl_hashitem_s * next;
    struct pbl_hashitem_s * prev;

    struct pbl_hashitem_s * bucketnext;
    struct pbl_hashitem_s * bucketprev;

} pbl_hashitem_t;

typedef struct pbl_hashbucket_s
{
    pbl_hashitem_t * head;
    pbl_hashitem_t * tail;

} pbl_hashbucket_t;

struct pbl_hashtable_s
{
    char             * magic;
    int                currentdeleted;
    pbl_hashitem_t   * head;
    pbl_hashitem_t   * tail;
    pbl_hashitem_t   * current;
    pbl_hashbucket_t * buckets;

};
typedef struct pbl_hashtable_s pbl_hashtable_t;

/*****************************************************************************/
/* globals                                                                   */
/*****************************************************************************/

/*****************************************************************************/
/* functions                                                                 */
/*****************************************************************************/
#ifndef WIN32
/*
 * The hash function used.
 *
 * Taken from the wikipedia page on hash function.
 * Not used, included just for tests and reference.
 */
unsigned int pblHt_jenkins_one_at_a_time_hash(const unsigned char * key, size_t key_len)
{
    unsigned int hash = 0;
    size_t i;

    for (i = 0; i < key_len; i++) {
        hash += key[i];
        hash += (hash << 10);
        hash ^= (hash >> 6);
    }
    hash += (hash << 3);
    hash ^= (hash >> 11);
    hash += (hash << 15);
    return hash;
}

#include <stdint.h>
#undef get16bits
#if (defined(__GNUC__) && defined(__i386__)) || defined(__WATCOMC__) \
  || defined(_MSC_VER) || defined (__BORLANDC__) || defined (__TURBOC__)
#define get16bits(d) (*((const uint16_t *) (d)))
#endif

#if !defined (get16bits)
#define get16bits(d) ((((uint32_t)(((const uint8_t *)(d))[1])) << 8)\
                       +(uint32_t)(((const uint8_t *)(d))[0]) )
#endif

/*
 * An alternative hash function.
 *
 * Copyright 2004-2008 by Paul Hsieh
 * Not used, included just for tests and reference.
 */
uint32_t pblHt_SuperFastHash (const unsigned char * data, size_t len) {
uint32_t hash = len, tmp;
int rem;

    if (len <= 0 || data == NULL) return 0;

    rem = len & 3;
    len >>= 2;

    /* Main loop */
    for (;len > 0; len--) {
        hash  += get16bits (data);
        tmp    = (get16bits (data+2) << 11) ^ hash;
        hash   = (hash << 16) ^ tmp;
        data  += 2*sizeof (uint16_t);
        hash  += hash >> 11;
    }

    /* Handle end cases */
    switch (rem) {
        case 3: hash += get16bits (data);
                hash ^= hash << 16;
                hash ^= data[sizeof (uint16_t)] << 18;
                hash += hash >> 11;
                break;
        case 2: hash += get16bits (data);
                hash ^= hash << 11;
                hash += hash >> 17;
                break;
        case 1: hash += *data;
                hash ^= hash << 10;
                hash += hash >> 1;
    }

    /* Force "avalanching" of final 127 bits */
    hash ^= hash << 3;
    hash += hash >> 5;
    hash ^= hash << 4;
    hash += hash >> 17;
    hash ^= hash << 25;
    hash += hash >> 6;

    return hash;
}

#endif

/*
 * An alternative hash function.
 *
 * I took the code from the net, it contained the following:
 *
 * Author J. Zobel, April 2001.
 * Permission to use this code is freely granted, provided that this
 * statement is retained.
 */

int pblHt_J_Zobel_Hash( const unsigned char * key, size_t keylen )
{
    int ret = 104729;

    for( ; keylen-- > 0; key++ )
    {
        ret ^= ( (ret << 5) + *key + (ret >> 2) );
    }

    return ( ret & 0x7fffffff );
}

/*
 * Calculates the hash value of a buffer.
 */

int pblHtHashValue( const unsigned char * key, size_t keylen )
{
    return ( pblHt_J_Zobel_Hash( key, keylen ) & 0x7fffffff );
}

/*
 * Calculates the hash value of a '\o' terminated string
 */

int pblHtHashValueOfString( const unsigned char * key )
{
    return ( pblHt_J_Zobel_Hash( key, strlen( (char*)key ) ) & 0x7fffffff );
}

/*
 * Calculate index into hash table.
 */

static int pblHtHashIndex( const unsigned char * key, size_t keylen )
{
    return( pblHtHashValue( key, keylen ) % PBL_HASHTABLE_SIZE );
}

/**
 * Create a new hash table.
 *
 * @return pblHashTable_t * retptr != NULL: A pointer to new hash table.
 * @return pblHashTable_t * retptr == NULL: An error, see pbl_errno:
 * <BR>    PBL_ERROR_OUT_OF_MEMORY: Out of memory.
 */
pblHashTable_t * pblHtCreate( void )
{
    pbl_hashtable_t * ht;

    ht = (pbl_hashtable_t *)pbl_malloc0( "pblHtCreate hashtable", sizeof( pbl_hashtable_t ) );
    if( !ht )
    {
        return( 0 );
    }

    ht->buckets = (pbl_hashbucket_t *)pbl_malloc0( "pblHtCreate buckets",
                               sizeof( pbl_hashbucket_t ) * PBL_HASHTABLE_SIZE);
    if( !ht->buckets )
    {
        PBL_FREE( ht );
        return( 0 );
    }

    /*
     * set the magic marker of the hashtable
     */
    ht->magic = pblhash_c_id;

    return( ( pblHashTable_t * )ht );
}

/**
 * Insert a key / data pair into a hash table.
 *
 * Only the pointer to the data is stored in the hash table,
 * no space is malloced for the data!
 *
 * @return  int ret == 0: Ok.
 * @return  int ret == -1: An error, see pbl_errno:
 * <BR>    PBL_ERROR_EXISTS - An item with the same key already exists.
 * <BR>    PBL_ERROR_OUT_OF_MEMORY - Out of memory.
 */

int pblHtInsert(
pblHashTable_t          * h,      /** Hash table to insert to             */
void                    * key,    /** Key to insert                       */
size_t                    keylen, /** Length of that key                  */
void                    * dataptr /** Dataptr to insert                   */
)
{
    pbl_hashtable_t  * ht = ( pbl_hashtable_t * )h;
    pbl_hashbucket_t * bucket = 0;
    pbl_hashitem_t   * item = 0;

    int                hashval = pblHtHashIndex( key, keylen );

    bucket = ht->buckets + hashval;

    if( keylen < (size_t)1 )
    {
        /*
         * the length of the key can not be smaller than 1
         */
        pbl_errno = PBL_ERROR_EXISTS;
        return( -1 );
    }

    for( item = bucket->head; item; item = item->bucketnext )
    {
        if(( item->keylen == keylen ) && !memcmp( item->key, key, keylen ))
        {
            snprintf( pbl_errstr, PBL_ERRSTR_LEN,
                      "insert of duplicate item in hashtable\n" );
            pbl_errno = PBL_ERROR_EXISTS;
            return( -1 );
        }
    }

    item = (pbl_hashitem_t *)pbl_malloc0( "pblHtInsert hashitem", sizeof( pbl_hashitem_t ) );
    if( !item )
    {
        return( -1 );
    }

    item->key = pbl_memdup( "pblHtInsert item->key", key, keylen );
    if( !item->key )
    {
        PBL_FREE( item );
        return( -1 );
    }
    item->keylen = keylen;
    item->data = dataptr;

    /*
     * link the item
     */
    PBL_LIST_PUSH( bucket->head, bucket->tail, item, bucketnext, bucketprev );
    PBL_LIST_APPEND( ht->head, ht->tail, item, next, prev );

    ht->current = item;
    return( 0 );
}

/**
 * Search for a key in a hash table.
 *
 * @return void * retptr != NULL: The pointer to data of item found.
 * @return void * retptr == NULL: An error, see pbl_errno:
 * <BR>PBL_ERROR_NOT_FOUND - No item found with the given key.
 */

void * pblHtLookup(
pblHashTable_t              * h,      /** Hash table to search in          */
void                        * key,    /** Key to search                    */
size_t                        keylen  /** Length of that key               */
)
{
    pbl_hashtable_t  * ht = ( pbl_hashtable_t * )h;
    pbl_hashbucket_t * bucket = 0;
    pbl_hashitem_t   * item = 0;

    int                hashval = pblHtHashIndex( key, keylen );

    bucket = ht->buckets + hashval;

    for( item = bucket->head; item; item = item->bucketnext )
    {
        if(( item->keylen == keylen ) && !memcmp( item->key, key, keylen ))
        {
            ht->current = item;
            ht->currentdeleted = 0;

            /*
             * if the item is not the first in the chain
             */
            if( item != bucket->head )
            {
                /*
                 * make the item the first in the chain
                 */
                PBL_LIST_UNLINK( bucket->head, bucket->tail, item,
                                 bucketnext, bucketprev );
                PBL_LIST_PUSH( bucket->head, bucket->tail, item,
                               bucketnext, bucketprev );
            }

            return( item->data );
        }
    }

    pbl_errno = PBL_ERROR_NOT_FOUND;

    return( 0 );
}

/**
 * Get data of first key in hash table.
 *
 * This call and \Ref{pblHtNext} can be used in order to loop through all items
 * stored in a hash table.
 *
 * <PRE>
   Example:

   for( data = pblHtFirst( h ); data; data = pblHtNext( h ))
   {
       do something with the data pointer
   }
   </PRE>

 * @return void * retptr != NULL: The pointer to data of first item.
 * @return void * retptr == NULL: An error, see pbl_errno:
 * <BR>PBL_ERROR_NOT_FOUND - The hash table is empty.
 */

void * pblHtFirst(
pblHashTable_t              * h       /** Hash table to look in            */
)
{
    pbl_hashtable_t  * ht = ( pbl_hashtable_t * )h;
    pbl_hashitem_t   * item = 0;

    item = ht->head;
    if( item )
    {
        ht->current = item;
        ht->currentdeleted = 0;
        return( item->data );
    }

    pbl_errno = PBL_ERROR_NOT_FOUND;
    return( 0 );
}

/**
 * Get data of next key in hash table.
 *
 * This call and \Ref{pblHtFirst} can be used in order to loop through all items
 * stored in a hash table.
 *
 * <PRE>
   Example:

   for( data = pblHtFirst( h ); data; data = pblHtNext( h ))
   {
       do something with the data pointer
   }
   </PRE>

 * @return void * retptr != NULL: The pointer to data of next item.
 * @return void * retptr == NULL: An error, see pbl_errno:
 * <BR>PBL_ERROR_NOT_FOUND - There is no next item in the hash table.
 */

void * pblHtNext(
pblHashTable_t              * h       /** Hash table to look in            */
)
{
    pbl_hashtable_t  * ht = ( pbl_hashtable_t * )h;
    pbl_hashitem_t   * item = 0;

    if( ht->current )
    {
        if( ht->currentdeleted )
        {
            item = ht->current;
        }
        else
        {
            item = ht->current->next;
        }
        ht->currentdeleted = 0;
    }
    if( item )
    {
        ht->current = item;
        return( item->data );
    }

    pbl_errno = PBL_ERROR_NOT_FOUND;
    return( 0 );
}

/**
 * Get key of current item in hash table.
 *
 * Parameter keylen is optional, if it is given, it will
 * be set to the length of the key returned.
 *
 * @return void * retptr != NULL: The pointer to the key of the current item.
 * @return void * retptr == NULL: An error, see pbl_errno:
 * <BR>PBL_ERROR_NOT_FOUND - There is no current item in the hash table.
 */

void * pblHtCurrentKey(
pblHashTable_t          * h,      /** Hash table to look in               */
size_t                  * keylen  /** OPT: Length of the key on return    */
)
{
    pbl_hashtable_t  * ht = ( pbl_hashtable_t * )h;

    if( ht->current )
    {
        if( keylen )
        {
            *keylen = ht->current->keylen;
        }
        return( ht->current->key );
    }

    pbl_errno = PBL_ERROR_NOT_FOUND;
    return( 0 );
}

/**
 * Get data of current key in hash table.
 *
 * @return void * retptr != NULL: The pointer to data of current item.
 * @return void * retptr == NULL: An error, see pbl_errno:
 * <BR>PBL_ERROR_NOT_FOUND - There is no current item in the hash table.
 */

void * pblHtCurrent(
pblHashTable_t              * h       /** Hash table to look in            */
)
{
    pbl_hashtable_t  * ht = ( pbl_hashtable_t * )h;

    if( ht->current )
    {
        return( ht->current->data );
    }

    pbl_errno = PBL_ERROR_NOT_FOUND;
    return( 0 );
}

/**
 * Remove an item from the hash table.
 *
 * Parameters key and keylen are optional, if they are not given
 * the current record is deleted
 *
 * If the current record is removed the pointer to the current record
 * is moved to the next record.
 *
 * <PRE>
   Example:

   for( data = pblHtFirst( h ); data; data = pblHtRemove( h, 0, 0 ))
   {
       this loop removes all items from a hash table
   }
   </PRE>
 *
 * If the current record is moved by this function the next call to
 * \Ref{pblHtNext} will return the data of the then current record.
 * Therefore the following code does what is expected:
 * It visits all items of the hash table and removes the expired ones.
 *
 * <PRE>
   for( data = pblHtFirst( h ); data; data = pblHtNext( h ))
   {
       if( needs to be deleted( data ))
       {
           pblHtRemove( h, 0, 0 );
       }
   }
   </PRE>

 * @return int ret == 0: Ok.
 * @return int ret == -1: An error, see pbl_errno:
 * <BR>PBL_ERROR_NOT_FOUND - The current item is not positioned
 *                          or there is no item with the given key.
 */

int pblHtRemove(
pblHashTable_t            * h,     /** Hash table to remove from           */
void                      * key,   /** OPT: Key to remove                  */
size_t                      keylen /** OPT: Length of that key             */
)
{
    pbl_hashtable_t  * ht = ( pbl_hashtable_t * )h;
    pbl_hashbucket_t * bucket = 0;
    pbl_hashitem_t   * item = 0;

    int                hashval = 0;

    if( keylen && key )
    {
        hashval = pblHtHashIndex( key, keylen );
        bucket = ht->buckets + hashval;

        for( item = bucket->head; item; item = item->bucketnext )
        {
            if(( item->keylen == keylen ) && !memcmp( item->key, key, keylen ))
            {
                break;
            }
        }
    }
    else
    {
        item = ht->current;

        if( item )
        {
            hashval = pblHtHashIndex( item->key, item->keylen );
            bucket = ht->buckets + hashval;
        }
    }

    if( item )
    {
        if( item == ht->current )
        {
            ht->currentdeleted = 1;
            ht->current = item->next;
        }

        /*
         * unlink the item
         */
        PBL_LIST_UNLINK( bucket->head, bucket->tail, item,
                         bucketnext, bucketprev );
        PBL_LIST_UNLINK( ht->head, ht->tail, item, next, prev );

        PBL_FREE( item->key );
        PBL_FREE( item );
        return( 0 );
    }

    pbl_errno = PBL_ERROR_NOT_FOUND;
    return( -1 );
}

/**
 * Delete a hash table.
 *
 * The hash table has to be empty!
 *
 * @return int ret == 0: Ok.
 * @return int ret == -1: An error, see pbl_errno:
 * @return     PBL_ERROR_EXISTS - The hash table is not empty.
 */

int pblHtDelete(
pblHashTable_t * h        /** Hash table to delete */
)
{
    pbl_hashtable_t  * ht = ( pbl_hashtable_t * )h;

    if( ht->head )
    {
        pbl_errno = PBL_ERROR_EXISTS;
        return( -1 );
    }

    PBL_FREE( ht->buckets );
    PBL_FREE( ht );

    return( 0 );
}

