/*******************************************************************
Header file for SingleClass Mode.

Copyright (c) 2001-2003 The University of Maryland.

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
Header file for SingleClass Mode.
@author Shuvra S. Bhattacharyya, Ankush Varma
@version $Id$
*/

#ifndef _pccg_array_h
#define _pccg_array_h

#include "pccg.h"

/* Simple versions of array instance and array access macros to get 
   early versions (before support for arrays is completed) of the translator 
   to work.
*/

#define PCCG_ARRAY_ACCESS(base, element_type, index) \
        (((element_type)((base)[(index)])))

/* Given a pointer to an array instance structure, return the
 * length of the associated array.
 */
#define PCCG_ARRAY_LENGTH(array) \
        (((PCCG_ARRAY_INSTANCE_PTR)(array))->array_length)


/* Structure that implements instances of array objects. 
 *
 * Caution: the ordering of field declarations in this structure is important. 
 * It must be consistent with the format of class instances in the
 * generated code. Specifically, the sequence of fields that precede
 * specific to array instances must be identical to the sequence of
 * fields that occurs at the beginning of class instance (object) descriptors.
 *
 */
/*
typedef struct
{
    int dummy; To suppress warnings.

} PCCG_ARRAY_CLASS;
*/
/*
typedef struct
{
    PCCG_ARRAY_CLASS *class;
    int array_length;
    void *array_data;
} PCCG_ARRAY_INSTANCE;
*/

typedef float PCCG_ARRAY_INSTANCE;

/* FIXME: Make this a structure typedef. */
typedef char  PCCG_ARRAY_char_elem;
typedef short PCCG_ARRAY_short_elem;
typedef long  PCCG_ARRAY_long_elem;
typedef float PCCG_ARRAY_float_elem;
typedef double PCCG_ARRAY_double_elem;

typedef PCCG_ARRAY_INSTANCE *PCCG_ARRAY_INSTANCE_PTR;

/* extern PCCG_ARRAY_CLASS GENERIC_ARRAY_CLASS; */

/* Allocate storage for an array. FIXME: complete this function.
   and its code. It should use varargs. */
extern PCCG_ARRAY_INSTANCE_PTR pccg_array_allocate(
        PCCG_CLASS_PTR element_class,
        int element_size,
        int dimensions_to_fill,
        int empty_dimensions,
        ...);

       
#endif 
