/**
 * classes.h
 * Contains conterparts of special classes as C structs.
 */
 
#include "types.h"

#ifndef _CLASSES_H
#define _CLASSES_H

#define THREAD_SHIFT    0x0008
#define THREAD_MASK     0xFF00
#define COUNT_MASK      0x00FF

#define CLASS_MASK      0x00FF
#define CLASS_SHIFT     0

#define GC_MASK         0x2000
#define GC_SHIFT        13

#define IS_ARRAY_MASK   0x4000
#define IS_ARRAY_SHIFT  14

#define IS_ALLOCATED_MASK  0x8000
#define IS_ALLOCATED_SHIFT 15

#define ARRAY_LENGTH_MASK  0x01FF
#define ARRAY_LENGTH_SHIFT 0

#define ELEM_TYPE_MASK  0x1E00
#define ELEM_TYPE_SHIFT 9

#define FREE_BLOCK_SIZE_MASK 0x7FFF
#define FREE_BLOCK_SIZE_SHIFT 0

#define is_array(OBJ_)           (((OBJ_)->flags & IS_ARRAY_MASK) != 0)
#define is_allocated(OBJ_)       (((OBJ_)->flags & IS_ALLOCATED_MASK) != 0)
#define get_na_class_index(OBJ_) (((OBJ_)->flags & CLASS_MASK) >> CLASS_SHIFT)
#define get_element_type(ARR_)   (((ARR_)->flags & ELEM_TYPE_MASK) >> ELEM_TYPE_SHIFT)
#define get_array_length(ARR_)   (((ARR_)->flags & ARRAY_LENGTH_MASK) >> ARRAY_LENGTH_SHIFT)
#define get_monitor_count(OBJ_)  ((OBJ_)->syncInfo & COUNT_MASK)

// Double-check these data structures with the 
// Java declaration of each corresponding class.

typedef struct S_Object
{
  /**
   * Object/block flags.
   * Free block:
   *  -- bits 0-14: Size of free block in words.
   *  -- bit 15   : Zero (not allocated).
   * Objects:
   *  -- bits 0-7 : Class index.
   *  -- bits 8-12: Unused.
   *  -- bit 13   : Garbage collection mark.
   *  -- bit 14   : Zero (not an array).
   *  -- bit 15   : One (allocated).
   * Arrays:
   *  -- bits 0-8 : Array length (0-527).
   *  -- bits 9-12: Element type.
   *  -- bit 13   : Garbage collection mark.
   *  -- bit 14   : One (is an array).
   *  -- bit 15   : One (allocated).
   */
  TWOBYTES flags;

  /**
   * Synchronization state.
   * bits 0-7: Monitor count.
   * bits 8-15: Thread index.
   */
  TWOBYTES syncInfo;

} Object;

typedef struct S_Thread
{
  Object _super;

  REFERENCE nextThread;
  JINT waitingOn;
  JINT stackFrameArray;
  JINT stackArray;
  JINT isReferenceArray;
  JBYTE stackFrameArraySize;
  JBYTE threadId;
  JBYTE state;
} Thread;

typedef struct S_String
{
  Object _super;

  REFERENCE characters;
} String;

#endif _CLASSES_H









