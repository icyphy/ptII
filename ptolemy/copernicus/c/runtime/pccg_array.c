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
@author Shuvra S. Bhattacharyya 
@version $Id$
*/

#include <stdarg.h>
#include <stdlib.h>
#include "pccg_array.h"

/* Allocate storage for a Java array. */
PCCG_ARRAY_INSTANCE_PTR pccg_array_allocate(
        PCCG_CLASS_PTR element_class, int element_size,
        int dimensions_to_fill, int empty_dimensions, ...) 
{

    int i, next_dimension, first_element_size;
    int total_dimensions,  first_dimension_size, next_dimension_size;
    void *new_array;
    va_list next_argument;
    PCCG_ARRAY_INSTANCE *result;
    extern PCCG_ARRAY_CLASS GENERIC_ARRAY_CLASS;
    
    //FIXME: Initialize generic array class
    
    va_start(next_argument, empty_dimensions);    

    first_dimension_size = va_arg(next_argument, int); 
    first_element_size = (total_dimensions == 1) ? element_size :
            (sizeof(void*));
       
    /* FIXME: need a robust wrapper around malloc that check's for 
     * an out-of-memory error
     */ 
    new_array = malloc(first_dimension_size * first_element_size);

    for (i = 2; i <= dimensions_to_fill; i++) {
        next_dimension_size = va_arg(next_argument, int);
        if (next_dimension == total_dimensions) {
        }
    }
   
    /* FIXME: this is not quite right */ 
    result = (PCCG_ARRAY_INSTANCE *) malloc(sizeof(result));
    result->class = &GENERIC_ARRAY_CLASS;
    result->array_data = new_array;
    return result;
}
      
/* FIXME: provide implementations cooresponding to all of the functions and 
macros that correspond to pccg_array-related constants defined in Class
CNames */

    
