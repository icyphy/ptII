/*******************************************************************
Run-time C code generation functionality for translation of arrays.

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
Run-time C code generation functionality for translation of arrays.

@author Shuvra S. Bhattacharyya, Ankush Varma
@version $Id$
*/

#include <stdarg.h>
#include <stdlib.h>
#include "pccg_array.h"

#ifdef GC
#include "include/gc.h"
#define malloc(x) GC_MALLOC(x)
#endif

PCCG_ARRAY_CLASS GENERIC_ARRAY_CLASS;

/* Forward declaration*/
PCCG_ARRAY_INSTANCE_PTR pccg_array_allocate_list(
        PCCG_CLASS_PTR element_class, int element_size,
        int dimensions, int dimensions_to_fill, va_list next_argument);

/* Allocate storage for a Java array using a variable number of arguments.
 * The size1, size2 etc. indicate sizes of the filled dimensions.
 */   
PCCG_ARRAY_INSTANCE_PTR pccg_array_allocate(
        PCCG_CLASS_PTR element_class, int element_size,
        int dimensions, int dimensions_to_fill,/* int size1,*/ ...) {
    va_list next_argument;
    
    va_start(next_argument, dimensions_to_fill);

    return pccg_array_allocate_list(element_class, element_size, 
            dimensions, dimensions_to_fill, next_argument);
}

/* Allocate space for a Java array using a va_arg instead of variable
 * number of arguments. This enables recursion.
 * next_argument a list of the sizes of the dimensions. Note that
 * dimensions is the total number of dimensions in the array, including
 * both filled and empty dimensions. However, we need to worry only about
 * the dimensions we have to fill.
 */
PCCG_ARRAY_INSTANCE_PTR pccg_array_allocate_list(
        PCCG_CLASS_PTR element_class, int element_size,
        int dimensions, int dimensions_to_fill, va_list next_argument) {
    int first_dimension_size, first_element_size;
    int i;
    void* new_array;
    /* Synonym with correct type. */
    PCCG_ARRAY_INSTANCE_PTR* new_array_clone;
    PCCG_ARRAY_INSTANCE *result;
   
    first_dimension_size = va_arg(next_argument, int);
    
    /* If the elements of this array are normal Objects/items and not
       arrays, then they are allocated directly, else pointers to them are
       allocated.
    */
    if (dimensions == 1) {
        first_element_size = element_size;
    }
    else {
        first_element_size = sizeof(result);
    }

    /* Allocate memory for the outermost array. */
#ifdef sun
    new_array = memalign(8, first_dimension_size* first_element_size);
#else
    new_array = malloc(first_dimension_size * first_element_size);
#endif

    /* If this is an array of arrays, its elements must be pointers to the
       sub-arrays. It is not sufficient to merely allocate memory. The
       pointers must also be set correctly.
    */
    if (dimensions_to_fill > 1) {
        for (i = 0; i < first_dimension_size; i++) {
            new_array_clone = new_array;
            new_array_clone[i]=
                pccg_array_allocate_list(element_class, element_size,
                        dimensions - 1, dimensions_to_fill - 1, next_argument);
        }
    }

    /* FIXME: this is not quite right */ 
#ifdef sun
    result = (PCCG_ARRAY_INSTANCE *) memalign(8, sizeof(result));
#else
    result = (PCCG_ARRAY_INSTANCE *) malloc(sizeof(PCCG_ARRAY_INSTANCE));
#endif
    result->class = &GENERIC_ARRAY_CLASS;
    result->array_data = new_array;
    result->array_length = first_dimension_size;
    result->element_size = first_element_size;

    return result;
}
