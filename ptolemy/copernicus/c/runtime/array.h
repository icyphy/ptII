/*******************************************************************
Run-time C code generation header file for translation of arrays.

Copyright (c) 2001-2002 The University of Maryland.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.
********************************************************************/

/*
Run-time C code generation functionality for translation of arrays.
@author Shuvra S. Bhattacharyya 
@version $Id$
*/

#ifndef _pccg_array_h
#define _pccg_array_h

#include "pccg.h"

/* Simple versions of array instance and array access macros to get 
   early versions (before support for arrays is completed) of the translator 
   to work.
*/
#define PCCG_ARRAY_INSTANCE void*
#define PCCG_ARRAY_ACCESS(arrayBase, arrayIndex) \
(((float*)(arrayBase))[arrayIndex])

/* Given a pointer to an array instance structure, return the
 * length of the associated array.
 */ 
#define PCCG_ARRAY_LENGTH(array) \
        ((PCCG_ARRAY_INSTANCE)(array).array_length)

/* Structure that implements instances of array objects. 
 *
 * Caution: the ordering of field declarations in this structure is important. 
 * It must be consistent with the format of class instances in the
 * generated code. Specifically, the sequence of fields that precede
 * specific to array instances must be identical to the sequence of
 * fields that occurs at the beginning of class instance (object) descriptors.
 *
 */
typedef struct pccg_array_instance {

    /* Pointer to the class descriptor for this type of array. */
    PCCG_CLASS class;

    /* Pointer to the data stored in this array. */
    void *array_data;

    /* The number of elements in the array (i.e., the capacity) */
    int array_length;

} *__PCCG_ARRAY_INSTANCE;
/* FIXME: replace PCCG_ARRAY_INSTANCE with the above data structure once support
   for it is complete 
*/

/* Allocate storage for an array. FIXME: complete this function.
   and its code. It should use varargs. */
/* FIXME: enable importing of the following function once suport for arrays 
   is completed
extern PCCG_ARRAY_INSTANCE pccg_array_allocate(
        PCCG_CLASS element_class,
        int element_size,
        int dimensions_to_fill,
        int empty_dimensions,
        ...);
*/
       
#endif 
