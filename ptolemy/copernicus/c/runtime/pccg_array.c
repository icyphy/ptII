/*******************************************************************
Run-time C code generation functionality for translation of arrays.

Copyright (c) 2001 The University of Maryland.

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

PCCG_ARRAY_CLASS GENERIC_ARRAY_CLASS;

/* Allocate space for a Java array using a va_arg instead of variable
 * number of arguments. This enables recursion.
 * next_argument stores dimensions_to_fill, empty_dimensions ... 
 */
PCCG_ARRAY_INSTANCE_PTR pccg_array_allocate_list(
        PCCG_CLASS_PTR element_class, int element_size,
        int dimensions_to_fill, int empty_dimensions,
        va_list next_argument) {
    int first_dimension_size, first_element_size;
    // The parameters for calling this function recursively.
    int new_dimensions_to_fill, new_empty_dimensions;
    int i;
    void* new_array;
    PCCG_ARRAY_INSTANCE *result;
   
    first_dimension_size = va_arg(next_argument, int);
    
    // If the elements of this array are normal Objects/items and not
    // arrays, then they are allocated directly, else pointers to them are
    // allocated.
    if (empty_dimensions == 0) {
        first_element_size = element_size;
    }
    else {
        first_element_size = sizeof(void *);
    }

    // Allocate memory for the outermost array.
    new_array = calloc(first_dimension_size, first_element_size);

    // If this is an array of arrays, its elements must be pointers to the
    // sub-arrays. It is not sufficient to merely allocate memory. The
    // pointers must also be set correctly.
    if (empty_dimensions != 0) {
        new_empty_dimensions = empty_dimensions - 1;
        for (i = 0; i <= first_dimension_size; i++) {
            *((void**)new_array + i) =
                pccg_array_allocate_list(element_class, element_size,
                        new_dimensions_to_fill, new_empty_dimensions,
                        next_argument);
        }
    }

    /* FIXME: this is not quite right */ 
    result = (PCCG_ARRAY_INSTANCE *) malloc(sizeof(result));
    result->class = &GENERIC_ARRAY_CLASS;
    result->array_data = new_array;
    result->array_length = first_dimension_size;
    return result;
}
    
    


/* Allocate storage for a Java array using a variable number of arguments */
PCCG_ARRAY_INSTANCE_PTR pccg_array_allocate(
        PCCG_CLASS_PTR element_class, int element_size,
        int dimensions_to_fill, int empty_dimensions, ...) {
    va_list next_argument;
    
    va_start(next_argument, empty_dimensions);

    return pccg_array_allocate_list(element_class, element_size, 
            dimensions_to_fill, empty_dimensions, next_argument);
}
    

#if 0
    int i, next_dimension, first_element_size, next_element_size;
    int total_dimensions,  first_dimension_size, next_dimension_size;
    void* new_array;
    va_list next_argument;
    PCCG_ARRAY_INSTANCE *result;
    //extern PCCG_ARRAY_CLASS GENERIC_ARRAY_CLASS;
    
    //FIXME: Initialize generic array class
    
    va_start(next_argument, empty_dimensions);    

    first_dimension_size = va_arg(next_argument, int); 
    first_element_size = (total_dimensions == 1) ? element_size :
            (sizeof(void*));
       
    /* FIXME: need a robust wrapper around malloc that check's for 
     * an out-of-memory error
     */ 
    new_array = calloc(first_dimension_size, first_element_size);

    for (i = 2; i <= dimensions_to_fill; i++) {
        next_dimension_size = va_arg(next_argument, int);
        
        // The next element is a pointer except for the last dimension.
        if (next_dimension == total_dimensions) {
            next_element_size = element_size;
            
            // Allocate memory for the next dimension in the array.
            *((void **)new_array + i * next_element_size) 
                    = calloc(next_dimension_size, next_element_size);
         }
        else {
            next_element_size = sizeof(void *);
            *((void **)new_array + i * next_element_size) =
                    pccg_array_allocate_list(element_class, element_size, next_argument);
        }

    }
   
    /* FIXME: this is not quite right */ 
    result = (PCCG_ARRAY_INSTANCE *) malloc(sizeof(result));
    result->class = &GENERIC_ARRAY_CLASS;
    result->array_data = new_array;
    result->array_length = first_dimension_size;
    return result;
#endif
