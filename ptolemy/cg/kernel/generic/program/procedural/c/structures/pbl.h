#ifndef _PBL_H_
#define _PBL_H_
/*
 pbl.h - external include file of library

 Copyright (C) 2002 - 2007   Peter Graf

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

    $Log: pbl.h,v $
    Revision 1.61  2010/08/29 15:29:31  peter
    Added the heap functions.


    Revision 1.59  2010/08/20 20:10:25  peter
    Implemented the priority queue functions.

    Revision 1.51  2010/05/20 21:42:53  peter
    Added pblSetReplace.

    Revision 1.50  2010/05/19 22:38:45  peter
    Testing the map.

    Revision 1.49  2010/05/16 20:57:24  peter
    Working on maps

    Revision 1.48  2010/05/15 16:26:10  peter
    Exposing the map interface.

    Revision 1.47  2009/10/20 21:08:00  peter
    Added the pblHtCurrentKey function.

    Revision 1.46  2009/03/08 20:56:50  peter
    port to gcc (Ubuntu 4.3.2-1ubuntu12) 4.3.2.
    Exposing the hash set and tree set interfaces.


    Revision 1.28  2009/02/03 16:40:14  peter
    PBL vesion 1.04, optimizations,
    MAC OS X port, port to Microsoft Visual C++ 2008 Express Edition,
    exposing the array list and the linked list interface


    Revision 1.3  2004/04/04 13:12:53  peter
    Added an ifdef vor Cygwin, proposed by Jari Aalto

    Revision 1.2  2002/09/12 20:47:18  peter
    added the isam file handling to the library

    Revision 1.1  2002/09/05 13:44:12  peter
    Initial revision

*/

#ifdef __cplusplus
extern "C" {
#endif

#ifdef WIN32
    #include <direct.h>
    #include <io.h>
        #include <string.h>
#else
#ifndef __CYGWIN__
    #if !defined(__AVR__) && !defined(__MBED__)
        #include <sys/dir.h>
    #endif
#endif
#endif

/*****************************************************************************/
/* #defines                                                                  */
/*****************************************************************************/


#ifdef _WIN32

/*
 * some functions have strange names on windows
 */
#define strcasecmp   _stricmp
#define strncasecmp  _strnicmp
#define snprintf     _snprintf
#define close        _close
#define strdup       _strdup
#define open         _open
#define lseek        _lseek
#define write        _write
#define read         _read
#define unlink       _unlink
#define _CRT_SECURE_NO_WARNINGS 1

#else

#ifndef O_BINARY
#define O_BINARY     0
#endif

#endif

#define PBL_ERRSTR_LEN                    2048

/** @name B: Files
 *  List of important files of component
 *  <P>
 *  <B>FILES</B>
 *  <UL>
 *  <LI> <a href="../pbl.h">pbl.h</a> - The include file of the library
 *  <LI> <a href="../pbl.c">pbl.c</a> - Source for the base functions
 *  <LI> <a href="../pblList.c">pblList.c</a> - Source file for the
 *                                              ArrayList and LinkedList functions
 *  <LI> <a href="../pblListTest.c">pblListTest.c</a> - Source file for the
 *                                              ArrayList and LinkedList function test frame
 *  <LI> <a href="../pblhash.c">pblhash.c</a> - Source file for the
 *                                              hash functions
 *  <LI> <a href="../pblhttst.c">pblhttst.c</a> - Source file for the
 *                                              hash function test frame
 *  <LI> <a href="../pblkf.c">pblkf.c</a> - Source file for the key
 *                                              file functions
 *  <LI> <a href="../pblkftst.c">pblkftst.c</a> - Source file for the
 *                                              key file handling test frame
 *  <LI> <a href="../pblisam.c">pblisam.c</a> - Source file for the isam
 *                                              file functions
 *  <LI> <a href="../pbliftst.c">pbliftst.c</a> - Source file for the
 *                                              isam file handling test frame
 *  <LI> <a href="../makefile">makefile</a> - A Unix makefile for the
 *                                              component
 *  <LI> <a href="../pblhttstdeb.dsp">pblhttstdeb.dsp</a> - A Microsoft Visual
 *                                              Studio 6.0 project file for
 *                                              hash table debug
 *  <LI> <a href="../pblkftstdeb.dsp">pblkftstdeb.dsp</a> - A Microsoft
 *                                              Visual Studio 6.0 project file
 *                                              for key file debug
 *  <LI> <a href="../pbliftstdeb.dsp">pbliftstdeb.dsp</a> - A Microsoft Visual
 *                                              Studio 6.0 project file for
 *                                              isam file debug
 *  <LI> <a href="../ISAM0001.TST">ISAM0001.TST</a> - A test case for the
 *                                              isam file handling test frame
 *  <LI> <a href="../ISAM0002.TST">ISAM0002.TST</a> - A test case for the
 *                                              isam file handling test frame
 *  <LI> <a href="../ISAM0003.TST">ISAM0003.TST</a> - A test case for the
 *                                              isam file handling test frame
 *  <LI> <a href="../ISAM0004.TST">ISAM0004.TST</a> - A test case for the
 *                                              isam file handling test frame
 *  <LI> <a href="../ISAM0005.TST">ISAM0005.TST</a> - A test case for the
 *                                              isam file handling test frame
 *  <LI> <a href="../ISAM0006.TST">ISAM0006.TST</a> - A test case for the
 *                                              isam file handling test frame
 *  <LI> <a href="../LINKEDLIST0001.TST">LINKEDLIST0001.TST</a> - A test case for the
 *                                              LinkedList file handling test frame
 *  <LI> <a href="../ARRAYLIST0001.TST">ARRAYLIST0001.TST</a> - A test case for the
 *                                              ArrayList file handling test frame
 *  <LI> <a href="../pbl.dxx">pbl.dxx</a> - The source for this document
 *  </UL>
 */

#define PBL_FILE_LIST

/** @name C: Error codes
 *  error codes of the pbl library
 *
 *  @field PBL_ERROR_OUT_OF_MEMORY            Out of memory.
 *  @field PBL_ERROR_EXISTS                   The record already exists.
 *  @field PBL_ERROR_NOT_FOUND                Record not found.
 *  @field PBL_ERROR_BAD_FILE                 File structure damaged.
 *  @field PBL_ERROR_PARAM_MODE               Parameter mode is not valid.
 *  @field PBL_ERROR_PARAM_KEY                Parameter key is not valid.
 *  @field PBL_ERROR_PARAM_KEYLEN             Parameter keylen is not valid.
 *  @field PBL_ERROR_PARAM_DATA               Parameter data is not valid.
 *  @field PBL_ERROR_PARAM_DATALEN            Parameter datalen is not valid.
 *  @field PBL_ERROR_PARAM_INDEX              Parameter index is not valid.
 *  @field PBL_ERROR_PARAM_LIST               Parameter list is not valid.
 *  @field PBL_ERROR_PARAM_COLLECTION         Parameter list is not valid.
 *  @field PBL_ERROR_PARAM_ELEMENT            Parameter element is not valid.
 *  @field PBL_ERROR_PARAM_SET                Parameter set is not valid.
 *  @field PBL_ERROR_CREATE                   File system create error, see errno.
 *  @field PBL_ERROR_OPEN                     File system open error, see errno.
 *  @field PBL_ERROR_SEEK                     File system seek error, see errno.
 *  @field PBL_ERROR_READ                     File system read error, see errno.
 *  @field PBL_ERROR_WRITE                    File system write error, see errno.
 *  @field PBL_ERROR_PROGRAM                  An internal error in the code, debug it!!
 *  @field PBL_ERROR_NOFIT                    An internal condition forcing a block split.
 *  @field PBL_ERROR_NOT_ALLOWED              File not open for update, operation not allowed.
 *  @field PBL_ERROR_POSITION                 The current record is not positioned.
 *  @field PBL_ERROR_OUT_OF_BOUNDS            Index out of range (index < 0 || index >= size()).
 *  @field PBL_ERROR_CONCURRENT_MODIFICATION  A collection was modified concurrently to an iterator.
 */
#define PBL_ERROR_BASE                    1000

#define PBL_ERROR_OUT_OF_MEMORY           ( PBL_ERROR_BASE + 1 )
#define PBL_ERROR_EXISTS                  ( PBL_ERROR_BASE + 2 )
#define PBL_ERROR_NOT_FOUND               ( PBL_ERROR_BASE + 3 )
#define PBL_ERROR_BAD_FILE                ( PBL_ERROR_BASE + 4 )
#define PBL_ERROR_PARAM_MODE              ( PBL_ERROR_BASE + 5 )
#define PBL_ERROR_PARAM_KEY               ( PBL_ERROR_BASE + 6 )
#define PBL_ERROR_PARAM_KEYLEN            ( PBL_ERROR_BASE + 7 )
#define PBL_ERROR_PARAM_DATA              ( PBL_ERROR_BASE + 8 )
#define PBL_ERROR_PARAM_DATALEN           ( PBL_ERROR_BASE + 9 )
#define PBL_ERROR_PARAM_INDEX             ( PBL_ERROR_BASE + 10 )
#define PBL_ERROR_PARAM_LIST              ( PBL_ERROR_BASE + 11 )
#define PBL_ERROR_PARAM_COLLECTION        ( PBL_ERROR_BASE + 12 )
#define PBL_ERROR_PARAM_ELEMENT           ( PBL_ERROR_BASE + 13 )
#define PBL_ERROR_PARAM_SET               ( PBL_ERROR_BASE + 14 )

#define PBL_ERROR_CREATE                  ( PBL_ERROR_BASE + 20 )
#define PBL_ERROR_OPEN                    ( PBL_ERROR_BASE + 21 )
#define PBL_ERROR_SEEK                    ( PBL_ERROR_BASE + 22 )
#define PBL_ERROR_READ                    ( PBL_ERROR_BASE + 23 )
#define PBL_ERROR_WRITE                   ( PBL_ERROR_BASE + 24 )

#define PBL_ERROR_PROGRAM                 ( PBL_ERROR_BASE + 30 )
#define PBL_ERROR_NOFIT                   ( PBL_ERROR_BASE + 31 )

#define PBL_ERROR_NOT_ALLOWED             ( PBL_ERROR_BASE + 40 )
#define PBL_ERROR_POSITION                ( PBL_ERROR_BASE + 41 )
#define PBL_ERROR_OUT_OF_BOUNDS           ( PBL_ERROR_BASE + 42 )
#define PBL_ERROR_CONCURRENT_MODIFICATION ( PBL_ERROR_BASE + 43 )


/** @name D: Definitions for Key File Parameters
  * DEFINES FOR PARAMETER <B> mode </B> OF \Ref{pblKfFind}()
  * @field PBLEQ                   any record that is equal
  * @field PBLFI                   first record that is equal
  * @field PBLLA                   last record that is equal
  * @field PBLGE                   last equal or first that is greater
  * @field PBLGT                   first that is greater
  * @field PBLLE                   first equal or last that is smaller
  * @field PBLLT                   last that is smaller
  */
#define PBLEQ               1
#define PBLFI               2
#define PBLLA               3
#define PBLGE               4
#define PBLGT               5
#define PBLLE               6
#define PBLLT               7

/** @name E: Definitions for ISAM Parameters
  * DEFINES FOR PARAMETER <B> which </B> OF \Ref{pblIsamGet}()
  * @field PBLTHIS                  get key and keylen of current record
  * @field PBLNEXT                  get key and keylen of next record
  * @field PBLPREV                  get key and keylen of previous record
  * @field PBLFIRST                 get key and keylen of first record
  * @field PBLLAST                  get key and keylen of last record
 */
#define PBLTHIS             1
#define PBLNEXT             2
#define PBLPREV             3
#define PBLFIRST            4
#define PBLLAST             5

/**
 * the maximum length of a key of the key file component,
 * @doc maximum length of a key, 255 for now
 */
#define PBLKEYLENGTH      255

/**
 * maximum data length of data being stored on index blocks of key files,
 * @doc maximum length of data stored with an item on the level 0 block, 1024
 * @doc data that is longer is stored on data blocks.
 */
#define PBLDATALENGTH    1024

/*****************************************************************************/
/* macros                                                                    */
/*****************************************************************************/

/*
 * The PBL_MEMTRACE define can be used for debugging the library,
 * if defined the library will log a line for all memory chunks
 * that are allocated for more than 3 minutes into the file ./pblmemtrace.log
 *
 * This can be used to detect heap memory lost by the code.
 * See also function pbl_memtrace_out in pbl.c
 */

/* #define PBL_MEMTRACE   */
#ifdef  PBL_MEMTRACE

extern void pbl_memtrace_init( void );
extern void pbl_memtrace_delete( void * data );
extern void pbl_memtrace_out( int checktime );

#define PBL_FREE( ptr ) if( ptr ){ pbl_memtrace_init(); pbl_memtrace_delete( ptr );\
                                   free( ptr ); ptr = 0; }

#else

/**
 * make free save against NULL pointers,
 * @doc also the parameter ptr is set to NULL
 */
#define PBL_FREE( ptr ) if( ptr ){ free( ptr ); ptr = 0; }

#endif

/**
 * macros for linear list handling,
 */
#define PBL_LIST_( Parameters )

/**
  * push an item to the beginning of a linear list
  */
#define PBL_LIST_PUSH( HEAD, TAIL, ITEM, NEXT, PREV )\
{\
    (ITEM)->PREV = 0;\
    if(( (ITEM)->NEXT = (HEAD) ))\
        { (ITEM)->NEXT->PREV = (ITEM); }\
    else\
        { (TAIL) = (ITEM); }\
    (HEAD) = (ITEM);\
}

/**
  * append an item to the end of a linear list
  */
#define PBL_LIST_APPEND( HEAD, TAIL, ITEM, NEXT, PREV )\
                         PBL_LIST_PUSH( TAIL, HEAD, ITEM, PREV, NEXT )

/**
  * add an item before another item of a linear list
  */
#define PBL_LIST_INSERT( HEAD, OTHER, ITEM, NEXT, PREV )\
{\
    if(((ITEM)->PREV = (OTHER)->PREV))\
        { (ITEM)->PREV->NEXT = (ITEM); }\
    else\
        { HEAD = (ITEM); }\
    (ITEM)->NEXT = (OTHER);\
    (OTHER)->PREV = (ITEM);\
}

/**
  * add an item after another item of a linear list
  */
#define PBL_LIST_INSERT_AFTER( TAIL, OTHER, ITEM, NEXT, PREV )\
                               PBL_LIST_INSERT( TAIL, OTHER, ITEM, PREV, NEXT )

/**
  * remove an item from a linear list
  */
#define PBL_LIST_UNLINK( HEAD, TAIL, ITEM, NEXT, PREV )\
{\
    if( (ITEM)->NEXT )\
        { (ITEM)->NEXT->PREV = (ITEM)->PREV; }\
    else\
        { (TAIL) = (ITEM)->PREV; }\
    if( (ITEM)->PREV )\
        { (ITEM)->PREV->NEXT = (ITEM)->NEXT; }\
    else\
        { (HEAD) = (ITEM)->NEXT; }\
}

/*
 * SOME MACROS FOR KEY FILE READ FUNCTIONS
 */
/**
 * set the current record to the first record of the file
 */
#define pblKfFirst( KF, K, L ) pblKfGetAbs( KF,  0, K, L )

/**
 * set the current record to the last record of the file
 */
#define  pblKfLast( KF, K, L ) pblKfGetAbs( KF, -1, K, L )

/**
 * set the current record to the next record of the file
 */
#define  pblKfNext( KF, K, L ) pblKfGetRel( KF,  1, K, L )

/**
 * set the current record to the previous record of the file
 */
#define  pblKfPrev( KF, K, L ) pblKfGetRel( KF, -1, K, L )

/**
 * return the datalen of the current record
 */
#define  pblKfThis( KF, K, L ) pblKfGetRel( KF,  0, K, L )

/*
 * Macros to allow to distinguish collection objects
 */
#define PBL_LIST_IS_LIST( LIST ) (PBL_LIST_IS_ARRAY_LIST( LIST )\
        || PBL_LIST_IS_LINKED_LIST( LIST ))

#define PBL_LIST_IS_ARRAY_LIST( LIST )\
    (LIST ? (((PblList*)LIST)->magic == PblArrayListMagic) : 0 )

#define PBL_LIST_IS_LINKED_LIST( LIST )\
    (LIST ? (((PblList*)LIST)->magic == PblLinkedListMagic) : 0 )

#define PBL_SET_IS_SET( SET ) (PBL_SET_IS_HASH_SET( SET )\
    || PBL_SET_IS_TREE_SET( SET ))

#define PBL_SET_IS_HASH_SET( SET )\
    (SET ? (((PblSet*)SET)->magic == PblHashSetMagic) : 0 )

#define PBL_SET_IS_TREE_SET( SET )\
    (SET ? (((PblSet*)SET)->magic == PblTreeSetMagic) : 0 )

#define PBL_COLLECTION_IS_COLLECTION( COLL ) \
        (PBL_LIST_IS_LIST( COLL ) || PBL_SET_IS_SET( COLL ))

/*****************************************************************************/
/* typedefs                                                                  */
/*****************************************************************************/

struct pblHashTable_s
{
    char * magic;
};

/**
  * The hash table type the pblHt* functions are dealing with.
  * @doc The details of the structure are hidden from the user.
  */
typedef struct pblHashTable_s pblHashTable_t;

struct pblKeyFile_s
{
    char * magic;
};

/**
  * The key file type the pblKf* functions are dealing with.
  * @doc The details of the structure are hidden from the user.
  */
typedef struct pblKeyFile_s pblKeyFile_t;

struct pblIsamFile_s
{
    char * magic;
};

/**
  * The ISAM file type the pblIsam* functions are dealing with.
  * @doc The details of the structure are hidden from the user.
  */
typedef struct pblIsamFile_s pblIsamFile_t;

/*
 * The generic collection.
 */
struct PblCollection_s
{
    char * magic;         /* The magic string of collections                 */
    int size;             /* The size of the collections                     */

    /* A user defined element compare function                               */
    int (*compare)( const void * prev, const void * next );

    unsigned long changeCounter; /* Number of changes on the collections     */

};

/**
 * The generic collection type.
 */
typedef struct PblCollection_s PblCollection;

/*
 * An node type used in a tree set.
 */
typedef struct PblTreeNode_s
{
    void * element;                     /* The element the node points to  */

    struct PblTreeNode_s * prev;        /* The left node                   */
    struct PblTreeNode_s * next;        /* The right node                  */

    struct PblTreeNode_s * parent;      /* The parent node                 */
    int balance;                        /* AVL balance information of node */


} PblTreeNode;

/**
 * The generic set type.
 */
typedef PblCollection PblSet;

/*
 * The hash set type.
 */
typedef struct PblHashSet_s
{
    PblSet genericSet;     /* The generic set definition of the hash set     */

    /* The array of all pointers hashed                                      */
    unsigned char ** pointerArray;

    int capacity;          /* The capacity of the pointer array              */
    int stepSize;          /* The step size used for collision resolution    */
    double loadFactor;     /* The load factor of the hash set                */

    /* A user defined element hash value function                            */
    int (*hashValue)( const void * element );

} PblHashSet;

/*
 * The tree set type, actually an AVL tree with a parent node pointer
 * allowing iteration from a node to its predecessor and successor
 */
typedef struct PblTreeSet_s
{
    PblSet genericSet;       /* The generic set definition of the tree set   */

    PblTreeNode * rootNode;  /* The root node of the AVL tree                */

} PblTreeSet;

/*
 * An node type used in a linked list.
 */
typedef struct PblLinkedNode_s
{
    void * element;

    struct PblLinkedNode_s * prev;       /* The previous node in the list    */
    struct PblLinkedNode_s * next;       /* The next node in the list        */

} PblLinkedNode;

/**
 * The generic list type.
 */

#ifndef __cplusplus
typedef PblCollection PblList;
#endif

/*
 * The linked list type.
 */
typedef struct PblLinkedList_s
{
    PblCollection genericList;  /* The generic list definition of the linked list  */

    PblLinkedNode * head;       /* The head of list of all nodes             */
    PblLinkedNode * tail;       /* The tail of list of all nodes             */

} PblLinkedList;

/*
 * The array list type.
 */
typedef struct PblArrayList_s
{
    PblCollection genericList;  /* The generic list definition of the array list   */

    /* The array of all pointers known                                       */
    unsigned char ** pointerArray;

    int capacity;         /* The capacity of the pointer array               */

} PblArrayList;

/*
 * The iterator struct.
 */
struct PblIterator_s
{
    char          * magic;         /* The magic string of iterators            */
    unsigned long   changeCounter; /* The number of changes on the collection  */
    PblCollection * collection;    /* The collection the iterator works on     */
    int             index;         /* The current index of the iterator        */

    int lastIndexReturned;         /* Index of element that was returned last  */

    PblLinkedNode * current;       /* The current node in the linked list      */

    PblLinkedNode * prev;          /* The previous node in the linked  list    */
    PblLinkedNode * next;          /* The next node in the linked  list        */
};

/**
 * The iterator type.
 */
typedef struct PblIterator_s PblIterator;

/*
 * The map struct
 */
struct PblMap_s
{
    PblSet * entrySet;        /* The set containing the entries of the map   */
};

/**
 * The map type.
 */
typedef struct PblMap_s PblMap;

/*
 * The map entry struct
 */
struct PblMapEntry_s
{
    int         tag;
    size_t      keyLength;
    size_t      valueLength;
    char        buffer[];
};

/**
 * The map entry type.
 */
typedef struct PblMapEntry_s PblMapEntry;

/*
 * The map key struct
 */
struct PblMapKey_s
{
    int         tag;
    size_t      keyLength;
    void    *   key;
};

/**
 * The map key type.
 */
typedef struct PblMapKey_s PblMapKey;

/**
 * The heap.
 */
typedef struct PblList PblHeap;

/**
 * The priority queue entry struct.
 * The entry consists of the priority and of the payload.
 */
struct PblPriorityQueueEntry_s
{
    int         priority;
    void    *   element;
};

/**
 * The priority queue entry.
 */
typedef struct PblPriorityQueueEntry_s PblPriorityQueueEntry;

/**
 * The priority queue.
 */
typedef struct PblList PblPriorityQueue;

/*****************************************************************************/
/* variable declarations                                                     */
/*****************************************************************************/
/**
  * Integer value used for returning error codes
  */
extern int    pbl_errno;

/**
  * Character buffer used for returning error strings
  */
extern char * pbl_errstr;

/*
 * "Magic" strings to distinguish between objects
 */
extern char * PblHashSetMagic;
extern char * PblTreeSetMagic;
extern char * PblArrayListMagic;
extern char * PblLinkedListMagic;
extern char * PblIteratorMagic;

/*****************************************************************************/
/* function declarations                                                     */
/*****************************************************************************/
extern void * pbl_malloc( char * tag, size_t size );
extern void * pbl_malloc0( char * tag, size_t size );
extern void * pbl_memdup( char * tag, void * data, size_t size );
extern void * pbl_strdup( char * tag, char * data );
extern void * pbl_mem2dup( char * tag, void * mem1, size_t len1,
                           void * mem2, size_t len2 );
extern int    pbl_memcmplen( void * left, size_t llen,
                             void * right, size_t rlen );
extern int    pbl_memcmp( void * left, size_t llen, void * right, size_t rlen );
extern size_t pbl_memlcpy( void * to, size_t tolen, void * from, size_t n );

extern void   pbl_ShortToBuf( unsigned char * buf, int s );
extern int    pbl_BufToShort( unsigned char * buf );
extern void   pbl_LongToBuf( unsigned char * buf, long l );
extern long   pbl_BufToLong( unsigned char * buf );
extern int    pbl_LongToVarBuf( unsigned char * buffer, unsigned long value );
extern int    pbl_VarBufToLong( unsigned char * buffer, unsigned long * value );
extern int    pbl_LongSize( unsigned long value );
extern int    pbl_VarBufSize( unsigned char * buffer );
extern void   pbl_LongToHexString( unsigned char * buf, unsigned long l );

extern int pblHtHashValue( const unsigned char * key, size_t keylen );
extern int pblHtHashValueOfString( const unsigned char * key );

extern pblHashTable_t * pblHtCreate( void );
extern int    pblHtInsert  ( pblHashTable_t * h, void * key, size_t keylen,
                             void * dataptr);
extern void * pblHtLookup  ( pblHashTable_t * h, void * key, size_t keylen );
extern void * pblHtFirst   ( pblHashTable_t * h );
extern void * pblHtNext    ( pblHashTable_t * h );
extern void * pblHtCurrent ( pblHashTable_t * h );
extern void * pblHtCurrentKey ( pblHashTable_t * h, size_t * keylen );
extern int    pblHtRemove  ( pblHashTable_t * h, void * key, size_t keylen );
extern int    pblHtDelete  ( pblHashTable_t * h );

/*
 * Functions on collections
 */
extern int pblCollectionAggregate(
    PblCollection * collection,  /** The collection to aggregate.                                 */
    void * context,              /** The application context to pass to the aggregation function. */
    int ( *aggregation )         /** The aggregation function called on every collection element. */
        (
            void * context,      /** The application context passed.                              */
            int index,           /** The index of the element passed.                             */
            void * element       /** The collection element to aggregate.                         */
        )
    );

extern int pblCollectionContains(
    PblCollection * collection, /** The collection to use            */
    void * element              /** Element to look for              */
    );

extern PblList * pblCollectionConvertToArrayList( PblCollection * collection );

extern PblSet * pblCollectionConvertToHashSet(
PblCollection * collection,  /** The collection to convert                            */
int ( *hashValue )           /** The hash value function for the new set, may be NULL */
    (
        const void* element  /** The element to get the hash value for                */
    ));

extern PblList * pblCollectionConvertToLinkedList( PblCollection * collection );

extern PblSet * pblCollectionConvertToTreeSet( PblCollection * collection );

extern int pblCollectionDefaultCompare(
    const void *left,     /** left element for compare  */
    const void *right     /** right element for compare */
);

extern int pblCollectionElementCompare(
PblCollection * collection,    /** The collection to compare the elements for   */
void *left,
void *right
);

extern int pblCollectionIsCollection(
    void * object      /** The object to test */
    );

extern int pblCollectionStringCompareFunction(
        const void * left,                /* left value for comparison  */
        const void * right                /* right value for comparison */
        );

extern void * pblCollectionSetCompareFunction(
PblCollection * collection,  /** The collection to set compare function for   */
int ( *compare )             /** The compare function to set                  */
    (
        const void* prev,    /** "left" element for compare                   */
        const void* next     /** "right" element for compare                  */
    )
);

/*
 * FUNCTIONS ON ITERATORS
 */
extern int pblIteratorAdd(
        PblIterator * iterator, /** The iterator to add the element to */
        void * element          /** Element to be added to this list   */
        );


extern void pblIteratorFree(
        PblIterator * iterator /** The iterator to free */
        );

extern int pblIteratorHasNext(
        PblIterator * iterator /** The iterator to check the next element for */
        );


extern int pblIteratorHasPrevious(
        PblIterator * iterator /** The iterator to check the previous element for */
        );

extern int pblIteratorInit(
PblCollection * collection,    /** The collection to create the iterator for */
PblIterator   * iterator
);

extern PblIterator * pblIteratorNew(
PblCollection * collection     /** The collection to create the iterator for */
);

extern void * pblIteratorNext(
        PblIterator * iterator /** The iterator to return the next element for */
        );

extern int pblIteratorNextIndex(
        PblIterator * iterator /** The iterator to use */
        );

extern void * pblIteratorPrevious(
        PblIterator * iterator /** The iterator to return the previous element for */
        );

extern int pblIteratorPreviousIndex(
        PblIterator * iterator /** The iterator to use */
        );

extern int pblIteratorRemove(
        PblIterator * iterator /** The iterator to remove the next element from */
        );

extern int pblIteratorReverseInit(
        PblCollection * collection,          /** The collection to create the iterator for */
        PblIterator   * iterator             /** The iterator to initialize                */
        );

extern PblIterator * pblIteratorReverseNew(
PblCollection * collection          /** The collection to create the iterator for */
);

extern void * pblIteratorSet(
        PblIterator * iterator, /** The iterator to replace the element of. */
        void * element          /** Element with which to replace the last element returned by next or previous. */
        );

extern int pblIteratorSize(
        PblIterator * iterator /** The iterator to use */
        );

/*
 * FUNCTIONS ON LISTS
 */
extern int pblListAdd(
        PblList * list, /** The list to use                                */
        void * element  /** Element to be appended to this list            */
        );

extern int pblListAddAll(
        PblList * list,   /** The list to use                                */
        void * collection /** The collection whose elements are to be added to this list. */
        );

extern int pblListAddAllAt(
        PblList * list,   /** The list to use                                */
        int index,        /** Index at which the element is to be inserted   */
        void * collection /** The collection whose elements are to be added to this list. */
        );

extern int pblListAddAt(
        PblList * list, /** The list to use                                */
        int index,      /** Index at which the element is to be inserted   */
        void * element  /** Element to be appended to this list            */
        );

extern int pblListAddFirst(
        PblList * list, /** The list to add to              */
        void * element  /** Element to be added to the list */
        );

extern int pblListAddLast(
        PblList * list, /** The list to add to              */
        void * element  /** Element to be added to the list */
        );

extern void pblListClear(
        PblList * list  /** The list to clear */
        );

extern PblList * pblListClone(
        PblList * list  /** The list to clone */
        );

extern PblList * pblListCloneRange(
        PblList * list,   /** The list to use                             */
        int fromIndex,    /** The index of first element to be cloned.    */
        int toIndex       /** The index after last element to be cloned.  */
        );

extern int pblListContains(
        PblList * list,           /** The list to use                   */
        void * element            /** Element to look for               */
        );

extern int pblListContainsAll(
        PblList * list,   /** The list to use */
        void * collection /** The collection whose elements are to be added to this list. */
        );

extern int pblListDefaultCompare( const void *left, const void *right );

extern void * pblListElement(
        PblList * list   /** The list to use */
        );

extern int pblListEnsureCapacity(
        PblList * list,  /** The list to use              */
        int minCapacity  /** The desired minimum capacity */
        );

extern int pblListEquals(
        PblList * list,   /** The list to compare with.                                  */
        void * collection /** The collection to be compared for equality with this list. */
        );

extern void pblListFree(
        PblList * list  /** The list to free */
        );

extern void * pblListGet(
        PblList * list, /** The list to use                */
        int index       /** Index of the element to return */
        );

extern int pblListGetCapacity(
        PblList * list  /** The list to use  */
        );

extern void * pblListGetCompareFunction(
        PblList * list  /** The list to get compare function for */
        );

extern void * pblListGetFirst(
        PblList * list  /** The list to use */
        );

extern void * pblListGetLast(
        PblList * list  /** The list to use */
        );

extern void * pblListHead(
PblList * list         /** The list to use  */
);

extern int pblListIndexOf(
        PblList * list,              /** The list to use     */
        void * element               /** Element to look for */
        );

extern int pblListIsArrayList(
        void * object   /** The object to test */
        );

extern int pblListIsEmpty(
        PblList * list  /** The list to use */
        );

extern int pblListIsLinkedList(
        void * object   /** The object to test */
        );

extern int pblListIsList(
        void * object   /** The list to test */
        );

extern PblIterator * pblListIterator(
        PblList * list  /** The list to create the iterator for */
        );

extern int pblListLastIndexOf(
        PblList * list,           /** The list to use                   */
        void * element            /** Element to look for               */
        );

extern PblList * pblListNewArrayList( void );

extern PblList * pblListNewLinkedList( void );

extern int pblListOffer(
        PblList * list, /** The list to add to              */
        void * element  /** Element to be added to the list */
        );

extern void * pblListPeek(
        PblList * list  /** The list to use */
        );

extern void * pblListPoll(
        PblList * list  /** The list to use */
        );

extern void * pblListPop(
        PblList * list  /** The list to use */
        );

extern int pblListPush(
        PblList * list,   /** The list to append to               */
        void * element    /** Element to be appended to this list */
        );

extern void * pblListRemove(
        PblList * list  /** The list to use */
        );

extern int pblListRemoveAll(
        PblList * list,   /** The list to use */
        void * collection /** The collection whose elements are to be removed from this list. */
        );

extern void * pblListRemoveAt(
        PblList * list, /** The list to use */
        int index       /** Index at which the element is to be removed */
        );

extern int pblListRemoveElement(
        PblList * list,           /** The list to use   */
        void * element            /** Element to remove */
        );

extern void * pblListRemoveFirst(
        PblList * list  /** The list to use */
        );

extern void * pblListRemoveLast(
        PblList * list  /** The list to use */
        );

extern int pblListRemoveRange(
        PblList * list, /** The list to use */
        int fromIndex,  /** The index of first element to be removed.*/
        int toIndex     /** The after last element to be removed.    */
        );

extern int pblListRetainAll(
        PblList * list,   /** The list to use */
        void * collection /** The collection whose elements are to be removed from this list. */
        );

extern void pblListReverse(
        PblList * list     /** The list to reverse */
        );

extern PblIterator * pblListReverseIterator(
        PblList * list  /** The list to create the iterator for */
        );

extern void * pblListSet(
        PblList * list, /** The list to use             */
        int index,      /** Index of element to replace */
        void * element  /** Element to be stored at the specified position */
        );

extern void * pblListSetCompareFunction(
        PblList * list,    /** The list to set compare function for   */
        int ( *compare )   /** compare function to set                */
            (
               const void* prev, /** "left" element for compare  */
               const void* next /** "right" element for compare */
            )
        );

extern void * pblListSetFirst(
        PblList * list,   /** The list to use                            */
        void    * element /** Element to be stored at the first position */
        );

extern void * pblListSetLast(
        PblList * list,   /** The list to use                            */
        void    * element /** Element to be stored at the last position  */
        );


extern int pblListSetSize(
        PblList * list, /** The list to set size for  */
        int size        /** The size to set           */
        );

extern int pblListSize(
        PblList * list  /** The list to use */
        );

extern int pblListSort(
        PblList * list,              /** The list to sort                       */
        int ( *compare )             /** Specific compare function to use       */
            (
                const void* prev,    /** "left" element for compare             */
                const void* next    /** "right" element for compare            */
            )
        );

extern void * pblListTail(
PblList * list         /** The list to use */
);

extern void ** pblListToArray(
        PblList * list  /** The list to use */
        );

extern void * pblListTop(
        PblList * list   /** The list to use */
        );

extern int pblListTrimToSize(
        PblList * list  /** The list to use*/
        );

extern void * pblArrayListRemoveAt(
PblArrayList * list,    /** The list to use                                */
int index               /** Index at which the element is to be removed    */
);

/*
 * FUNCTIONS ON SETS
 */

extern void ** pblHashElementFirst(
PblHashSet * set
);

extern void ** pblHashElementNext(
PblHashSet * set,
void ** pointer
);

extern void ** pblHashElementLast(
PblHashSet * set
);

extern void ** pblHashElementPrevious(
PblHashSet * set,
void ** pointer
);

extern int pblSetAdd(
        PblSet * set,   /** The set to use                                */
        void * element  /** Element to be appended to this set            */
        );

extern int pblSetAddAll(
    PblSet * set,      /** The set to use                                */
    void * collection  /** The collection whose elements are to be added to this set. */
    );

extern void * pblSetReplaceElement(
        PblSet * set,             /** The set to use                  */
        void * element            /** Element to look for             */
        );

extern void pblSetClear(
        PblSet * set     /** The set to clear */
        );

extern PblSet * pblSetClone(
        PblSet * set            /** The set to use                             */
        );

extern PblSet * pblSetCloneRange(
PblSet * set,              /** The set to use                               */
int fromIndex,             /** The index of first element to be cloned.     */
int toIndex                /** The index after last element to be cloned.   */
);

extern void * pblSetGetElement(
PblSet * set,             /** The set to use                  */
void * element            /** Element to look for             */
);

extern int pblSetContains(
    PblSet * set,             /** The set to use                  */
    void * element            /** Element to look for             */
    );

extern int pblSetContainsAll(
    PblSet * set,      /** The set to use                                            */
    void * collection  /** The collection to be checked for containment in this set. */
    );

extern int pblSetDefaultCompare(
    const void *left,     /** left element for compare  */
    const void *right     /** right element for compare */
);

extern int pblSetDefaultHashValue(
    const void *element     /** Element to calculate hash value for */
);

extern int pblSetStringHashValue(
    const void *string     /** The '\0' terminated string to calculate the hash value for */
    );

extern PblSet * pblSetDifference(
PblSet * setA,     /** The first set to build the difference from  */
PblSet * setB      /** The second set to build the difference from */
);

extern void * pblSetElement(
PblSet * set         /** The set to use  */
);

extern int pblSetEnsureCapacity(
    PblSet * set,   /** The set to use               */
    int minCapacity /** The desired minimum capacity */
    );

extern int pblSetEquals(
    PblSet * set,     /** The set to compare with.                                  */
    void * collection /** The collection to be compared for equality with this set. */
    );

extern void pblSetFree(
        PblSet * set    /** The set to free */
        );

extern void * pblSetGet(
PblSet * set,     /** The set to use                */
int index         /** Index of the element to return */
);

extern int pblSetGetCapacity(
    PblSet * set          /** The set to use */
    );

extern void * pblSetGetCompareFunction(
PblSet * set         /** The set to get the compare function for   */
);

extern void * pblSetGetFirst(
PblSet * set          /** The set to use */
);

extern void * pblSetGetHashValueFunction(
PblSet * set         /** The set to get the hash value function for */
);

extern void * pblSetGetLast(
PblSet * set         /** The set to use */
);

extern void * pblSetHead(
PblSet * set         /** The set to use  */
);

extern int pblSetIndexOf(
PblSet * set,      /** The set to use      */
void * element     /** Element to look for  */
);

extern PblSet * pblSetIntersection(
PblSet * setA,     /** The first set to intersect  */
PblSet * setB      /** The second set to intersect */
);

extern int pblSetIsEmpty(
PblSet * set      /** The set to test */
);

extern int pblSetIsHashSet(
void * object           /** The object to test */
);

extern int pblSetIsSet(
void * object      /** The object to test */
);

extern int pblSetIsSubset(
    PblSet * setA,     /** Superset to check  */
    PblSet * setB      /** Subset to check    */
);

extern int pblSetIsTreeSet(
void * object            /** The object to test */
);

extern PblIterator * pblSetIterator(
PblSet * set                 /** The set to create the iterator for */
);

extern int pblSetLastIndexOf(
    PblSet * set,      /** The set to use      */
    void * element     /** Element to look for  */
    );

extern PblSet * pblSetNewHashSet( void );

extern PblSet * pblSetNewTreeSet( void );

extern void * pblSetPeek(
PblSet * set      /** The set to use */
);

extern void * pblSetPoll(
PblSet * set      /** The set to use                */
);

extern void * pblSetPop(
PblSet * set     /** The set to use */
);

extern void pblSetPrint( FILE * outfile, PblSet * set );

extern void * pblSetRemove(
PblSet * set          /** The set to use */
);

extern int pblSetRemoveAll(
    PblSet * set,      /** The set to use                                                 */
    void * collection  /** The collection whose elements are to be removed from this set. */
    );

extern void * pblSetRemoveAt(
PblSet * set,         /** The set to use                                  */
int index             /** The index at which the element is to be removed */
);

extern int pblSetRemoveElement(
PblSet * set,            /** The set to use                        */
void * element           /** Element to remove                     */
);

extern void * pblSetRemoveFirst(
PblSet * set         /** The set to use */
);

extern void * pblSetRemoveLast(
PblSet * set         /** The set to use */
);

extern int pblSetRetainAll(
    PblSet * set,           /** The set to use                           */
    void * collection       /** The elements to be retained in this set. */
    );

extern PblIterator * pblSetReverseIterator(
PblSet * set                 /** The set to create the iterator for */
);

extern void * pblSetSetCompareFunction(
PblSet * set,                /** The set to set compare function for   */
int ( *compare )             /** compare function to set               */
    (
        const void* prev,    /** "left" element for compare            */
        const void* next     /** "right" element for compare           */
    )
);

extern void * pblSetSetHashValueFunction(
PblSet * set,                /** The set to set hash value function for */
int ( *hashValue )           /** The hash value function to set         */
    (
        const void* element  /** The element to get the hash value for  */
    )
);

extern double pblSetSetLoadFactor(
    PblSet * set,                /** The set to set hash value function for */
    double loadFactor            /** The load factor to set                 */
    );

extern int pblSetSize(
PblSet * set   /** The set to use */
);

extern PblSet * pblSetSymmectricDifference(
PblSet * setA,     /** The first set to use  */
PblSet * setB      /** The second set to use */
);

extern void * pblSetTail(
PblSet * set         /** The set to use */
);

extern void ** pblSetToArray(
PblSet * set           /** The set to use */
);

extern void * pblSetTop(
PblSet * set    /** The set to use */
);

extern int pblSetTrimToSize(
    PblSet * set         /** The set to use */
    );

extern PblSet * pblSetUnion(
PblSet * setA,     /** The first set to unite  */
PblSet * setB      /** The second set to unite */
);

extern PblTreeNode * pblTreeNodeFirst(
PblTreeNode * node                /** The node to use */
);

extern PblTreeNode * pblTreeNodeLast(
PblTreeNode * node                /** The node to use */
);

extern PblTreeNode * pblTreeNodeNext(
PblTreeNode * node                /** The node to use */
);

extern PblTreeNode * pblTreeNodePrevious(
PblTreeNode * node                /** The node to use */
);

extern void pblTreeNodePrint( FILE * outfile, int level, PblTreeNode * node );

/*
 * FUNCTIONS MAPS
 */

extern int pblMapAdd( /*                                          */
    PblMap * map, /**                   The map to add to         */
    void * key, /**                     Key to add a mapping for  */
    size_t keyLength, /**               Length of the key         */
    void * value, /**                   Value of the new mapping  */
    size_t valueLength /**              Length of the value       */
    );

extern int pblMapAddStrStr( /*                                    */
    PblMap * map, /**                   The map to add to         */
    char * key, /**                     Key to add a mapping for  */
    char * value /**                    Value of the new mapping  */
    );

extern void pblMapClear( /*                                            */
    PblMap * map /**                                  The map to clear */
    );

extern int pblMapContainsKey( /*                                                 */
    PblMap * map, /**             The map to check                               */
    void * key, /**               Key whose presence in this map is to be tested */
    size_t keyLength /**          Length of the key                              */
    );

extern int pblMapContainsKeyStr( /*                                              */
    PblMap * map, /**             The map to check                               */
    char * key  /**               Key whose presence in this map is to be tested */
    );

extern int pblMapContainsValue( /*                                               */
    PblMap * map, /**           The map to check                                 */
    void * value, /**           Value whose presence in this map is to be tested */
    size_t valueLength /**      Length of the value                              */
    );

extern int pblMapContainsValueStr( /*                                            */
    PblMap * map, /**           The map to check                                 */
    char * value /**            Value whose presence in this map is to be tested */
    );

extern void * pblMapEntryKey( /*                          */
        PblMapEntry * entry /**                 The entry */
        );

extern size_t pblMapEntryKeyLength( /*                */
    PblMapEntry * entry /**                 The entry */
    );

extern void * pblMapEntryValue( /*                        */
        PblMapEntry * entry /**                 The entry */
        );

extern size_t pblMapEntryValueLength( /*                */
    PblMapEntry * entry /**                   The entry */
    );

extern void pblMapFree( /*                                           */
    PblMap * map /**                                 The map to free */
    );

extern void * pblMapGet( /*                                                       */
        PblMap * map, /**            The map to check                             */
        void * key, /**              Key whose associated value is to be returned */
        size_t keyLength, /**        Length of the key                            */
        size_t * valueLengthPtr /**  Out: Length of the value returned            */
        );

extern void * pblMapGetStr( /*                                                    */
        PblMap * map, /**            The map to check                             */
        char * key, /**              Key whose associated value is to be returned */
        size_t * valueLengthPtr /**  Out: Length of the value returned            */
        );

extern int pblMapIsEmpty( /*                            */
    PblMap * map /**                    The map to test */
    );

extern PblIterator * pblMapIteratorNew( /*                  */
        PblMap * map /** The map to create the iterator for */
        );

extern PblIterator * pblMapIteratorReverseNew( /*           */
        PblMap * map /** The map to create the iterator for */
        );

extern PblMap * pblMapNewHashMap( void );

extern PblMap * pblMapNewTreeMap( void );

extern void * pblMapPut( /*                                                    */
        PblMap * map, /**                                    The map to add to */
        void * key, /**                               Key to add a mapping for */
        size_t keyLength, /**                                Length of the key */
        void * value, /**                             Value of the new mapping */
        size_t valueLength, /**                            Length of the value */
        size_t * valueLengthPtr /**          Out: Length of the value returned */
        );

extern void * pblMapPutStrStr( /*                                              */
        PblMap * map, /**                                    The map to add to */
        char * key, /**                               Key to add a mapping for */
        char * value, /**                             Value of the new mapping */
        size_t * valueLengthPtr /**          Out: Length of the value returned */
        );

extern int pblMapPutAll( /*                                         */
    PblMap * map, /**                The map to copy the entries to */
    PblMap * sourceMap /**         The map to copy the entries from */
    );

extern void * pblMapRemove( /*                                              */
        PblMap * map, /**                            The map to remove from */
        void * key, /**                    Key whose association is removed */
        size_t keyLength, /**                             Length of the key */
        size_t * valueLengthPtr /**       Out: Length of the value returned */
        );

extern void * pblMapRemoveStr( /*                                           */
        PblMap * map, /**                            The map to remove from */
        char * key, /**                    Key whose association is removed */
        size_t * valueLengthPtr /**       Out: Length of the value returned */
        );

extern int pblMapSize( /*           */
    PblMap * map /** The map to use */
    );

/*
 * FUNCTIONS ON HEAPS
 */
extern PblHeap * pblHeapNew( void );

extern void * pblHeapSetCompareFunction( /*                      */
        PblHeap * heap, /** The heap to set compare function for */
        int(*compare) /** The compare function to set            */
        ( /*                                                     */
        const void* prev, /** The "left" element for compare     */
        const void* next /** The "right" element for compare     */
        ) /*                                                     */
        );

extern void pblHeapClear( /*             */
    PblHeap * heap /** The heap to clear */
    );

extern void pblHeapFree( /*             */
    PblHeap * heap /** The heap to free */
    );

extern int pblHeapEnsureCapacity( /*                 */
    PblHeap * heap, /** The heap to use              */
    int minCapacity /** The desired minimum capacity */
    );

extern int pblHeapGetCapacity( /*      */
    PblHeap * heap /** The heap to use */
    );

extern int pblHeapSize( /*             */
    PblHeap * heap /** The heap to use */
    );

extern int pblHeapIsEmpty( /*          */
    PblHeap * heap /** The heap to use */
    );

extern int pblHeapTrimToSize( /*       */
    PblHeap * heap /** The heap to use */
    );

extern int pblHeapEnsureCondition( /*                     */
    PblHeap * heap, /** The heap to use                   */
    int index /** Index of element to ensure condtion for */
    );

extern int pblHeapEnsureConditionFirst( /**/
    PblHeap * heap /** The heap to use    */
    );

extern int pblHeapAddLast( /*                          */
    PblHeap * heap, /** The heap to use                */
    void * element /** Element to be added to the heap */
    );

extern int pblHeapInsert( /*                              */
    PblHeap * heap, /** The heap to use                   */
    void * element /** Element to be inserted to the heap */
    );

extern void * pblHeapRemoveLast( /*        */
        PblHeap * heap /** The heap to use */
        );

extern void * pblHeapRemoveAt( /*                                     */
        PblHeap * heap, /** The heap to use                           */
        int index /** The index at which the element is to be removed */
        );

extern void * pblHeapRemoveFirst( /*       */
        PblHeap * heap /** The heap to use */
        );

extern void * pblHeapGet( /*                         */
        PblHeap * heap, /** The heap to use          */
        int index /** Index of the element to return */
        );

extern void * pblHeapGetFirst( /*          */
        PblHeap * heap /** The heap to use */
        );

extern void pblHeapConstruct( /*       */
    PblHeap * heap /** The heap to use */
    );

extern PblIterator * pblHeapIterator( /*   */
        PblHeap * heap /** The heap to use */
        );

extern int pblHeapJoin( /*                     */
    PblHeap * heap, /** The heap to join to    */
    PblHeap * other /** The other heap to join */
    );

/*
 * FUNCTIONS ON PRIORITY QUEUES
 */
extern PblPriorityQueue * pblPriorityQueueNew( void );

extern void pblPriorityQueueClear( /*               */
    PblPriorityQueue * queue /** The queue to clear */
    );

extern void pblPriorityQueueFree( /*               */
    PblPriorityQueue * queue /** The queue to free */
    );

extern int pblPriorityQueueEnsureCapacity( /*        */
    PblPriorityQueue * queue, /** The queue to use   */
    int minCapacity /** The desired minimum capacity */
    );

extern int pblPriorityQueueGetCapacity( /*        */
    PblPriorityQueue * queue /** The queue to use */
    );

extern int pblPriorityQueueSize( /*               */
    PblPriorityQueue * queue /** The queue to use */
    );

extern int pblPriorityQueueIsEmpty( /*            */
    PblPriorityQueue * queue /** The queue to use */
    );

extern int pblPriorityQueueTrimToSize( /*         */
    PblPriorityQueue * queue /** The queue to use */
    );

extern int pblPriorityQueueAddLast( /*                    */
    PblPriorityQueue * queue, /** The queue to use        */
    int priority, /** Priority of the element to be added */
    void * element /** Element to be added to the queue   */
    );

extern int pblPriorityQueueInsert( /*                        */
    PblPriorityQueue * queue, /** The queue to use           */
    int priority, /** Priority of the element to be inserted */
    void * element /** Element to be inserted to the queue   */
    );

extern void * pblPriorityQueueRemoveAt( /*                                        */
        PblPriorityQueue * queue, /** The queue to use                            */
        int index, /** The index at which the element is to be removed            */
        int * priority /** On return contains the priority of the element removed */
        );

extern void * pblPriorityQueueRemoveLast( /*                                      */
        PblPriorityQueue * queue, /** The queue to use                            */
        int * priority /** On return contains the priority of the element removed */
        );

extern void * pblPriorityQueueRemoveFirst( /*                                 */
    PblPriorityQueue * queue, /** The queue to use                            */
    int * priority /** On return contains the priority of the element removed */
    );

extern void * pblPriorityQueueGet( /*                                     */
        PblPriorityQueue * queue, /** The queue to use                    */
        int index, /** Index of the element to return                     */
        int * priority /** On return contains the priority of the element */
        );

extern void * pblPriorityQueueGetFirst( /*                                  */
    PblPriorityQueue * queue, /** The queue to use                          */
    int * priority /** On return contains the priority of the first element */
    );

extern void pblPriorityQueueConstruct( /*         */
    PblPriorityQueue * queue /** The queue to use */
    );

extern int pblPriorityQueueChangePriorityAt( /*                     */
    PblPriorityQueue * queue, /** The queue to use                  */
    int index, /** The index at which the priority is to be changed */
    int priority /** The new priority of the element                */
    );

extern int pblPriorityQueueChangePriorityFirst( /*         */
    PblPriorityQueue * queue, /** The queue to use         */
    int priority /** The new priority of the first element */
    );

extern PblIterator * pblPriorityQueueIterator( /*     */
        PblPriorityQueue * queue /** The queue to use */
        );

extern int pblPriorityQueueJoin( /*                      */
    PblPriorityQueue * queue, /** The queue to join to   */
    PblPriorityQueue * other /** The other queue to join */
    );

/*
 * FUNCTIONS ON KEY FILES
 */
int                   pblKfInit  ( int nblocks );
extern pblKeyFile_t * pblKfCreate( char * path, void * filesettag );
extern pblKeyFile_t * pblKfOpen  ( char * path, int update, void * filesettag );
extern int            pblKfClose ( pblKeyFile_t * k );
extern int            pblKfFlush ( pblKeyFile_t * k );
extern int            pblKfStartTransaction( pblKeyFile_t * k );
extern int            pblKfCommit( pblKeyFile_t * k, int rollback );
extern int            pblKfSavePosition( pblKeyFile_t * k );
extern int            pblKfRestorePosition( pblKeyFile_t * k );

extern void pblKfSetCompareFunction(
pblKeyFile_t * k,             /** key file to set compare function for  */
int ( *keycompare )           /** compare function to set               */
    (
                void* prev,   /** "left" buffer for compare             */
                size_t llen,  /** length of that buffer                 */
                void* next,  /** "right" buffer for compare            */
                size_t rlen   /** length of that buffer                 */
    )
);

/*
 * WRITE FUNCTIONS ON RECORDS, DELETE AND UPDATE WORK ON CURRENT RECORD
 */
extern int pblKfInsert(
pblKeyFile_t   * k,
void           * key,
size_t           keylen,
void           * data,
size_t           datalen
);

extern int pblKfDelete( pblKeyFile_t * k );

extern int pblKfUpdate(
pblKeyFile_t   * k,
void           * data,
size_t           datalen
);

/*
 * KEY FILE READ FUNCTIONS ON RECORDS
 */
extern long pblKfFind(
pblKeyFile_t   * k,
int              mode,
void           * skey,
size_t           skeylen,
void           * okey,
size_t         * okeylen
);

extern long pblKfRead(
pblKeyFile_t  * k,
void          * data,
long            datalen
);

/*
 * FUNCTIONS ACTUALLY ONLY TO BE USED THROUGH THE MAKROS DEFINED BELOW
 *
 * however, the functions work, but they are not very fast
 *
 * pblKfGetRel - positions relative to the current record to any other
 *               record of the file, interface is like pblKfNext
 *
 * pblKfGetAbs - positions absolute to the absindex 'th record of the file,
 *               -1L means last, interface is like pblKfFirst
 */
extern long pblKfGetRel( pblKeyFile_t * k,
                         long           relindex,
                         void         * okey,
                         size_t       * okeylen);
extern long pblKfGetAbs( pblKeyFile_t * k,
                         long           absindex,
                         void         * okey,
                         size_t       * okeylen);

/*
 * FUNCTIONS ON ISAM FILES
 */
extern int pblIsamClose( pblIsamFile_t * isamfile );
extern int pblIsamFlush( pblIsamFile_t * isamfile );
extern int pblIsamDelete( pblIsamFile_t * isamfile );

extern pblIsamFile_t * pblIsamOpen(
char        * path,
int           update,
void        * filesettag,
int           nkeys,
char       ** keyfilenames,
int         * keydup
);

extern int pblIsamInsert(
pblIsamFile_t * isamfile,
void          * allkeys,
size_t          allkeyslen,
void          * data,
size_t          datalen
);

extern int pblIsamFind(
pblIsamFile_t  * isamfile,
int              mode,
int              index,
void           * skey,
size_t           skeylen,
void           * okey
);

extern int pblIsamGet(
pblIsamFile_t  * isamfile,
int              which,
int              index,
void           * okey
);

extern int pblIsamReadKey(
pblIsamFile_t  * isamfile,
int              index,
void           * okey
);

extern long pblIsamReadDatalen( pblIsamFile_t * isamfile );

extern long pblIsamReadData(
pblIsamFile_t * isamfile,
void          * buffer,
size_t          bufferlen
);

extern long pblIsamUpdateData(
pblIsamFile_t * isamfile,
void          * data,
size_t          datalen
);

extern int pblIsamUpdateKey(
pblIsamFile_t  * isamfile,
int              index,
void           * ukey,
size_t           ukeylen
);

extern int pblIsamStartTransaction( int nfiles, pblIsamFile_t ** isamfiles );
extern int pblIsamCommit( int nfiles, pblIsamFile_t ** isamfiles, int rollback);

#ifdef __APPLE__ /* Mac OS X needs this */

extern void * malloc( size_t size );
extern void free( void *);

#endif

#ifdef __cplusplus
}
#endif

#endif

