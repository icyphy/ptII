/*******************************************************************
Run-time C code generation header file for translation of arrays.

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

#ifndef _pccg_array_h
#define _pccg_array_h

#include "pccg.h"
#include "java/lang/Object.h"

/* Simple versions of array instance and array access macros to get 
   early versions (before support for arrays is completed) of the translator 
   to work.
*/

/* Given a pointer to an array instance structure, return the
 * length of the associated array.
 */
#define PCCG_ARRAY_LENGTH(array) \
        (((PCCG_ARRAY_INSTANCE_PTR)(array))->array_length)

/*Without runtime bounds check.*/
#define PCCG_ARRAY_ACCESS(base, element_type, index) \
        (((element_type*)((base)->array_data))[(index)])

/* With runtime bounds check 
#define PCCG_ARRAY_ACCESS(base, element_type, index) \
        (((element_type*)((base)->array_data))\
        [(((index)<(PCCG_ARRAY_LENGTH(base)))? \
        (index) \
        :(printf("Array bounds error\n")))])
*/






/* Structure that implements instances of array objects. 
 *
 * Caution: the ordering of field declarations in this structure is important. 
 * It must be consistent with the format of class instances in the
 * generated code. Specifically, the sequence of fields that precede
 * specific to array instances must be identical to the sequence of
 * fields that occurs at the beginning of class instance (object) descriptors.
 *
 */
typedef struct
{
    
    /* The name of this class. */
    char* name;

    /* The memory needed by instances of this class. */
    long instance_size;

    /* Pointer to superclass structure */
    void* superclass;

    /* Pointer to array class */
    void* array_class;

    /* Interface lookup function. */
    void* (*lookup)(long int);

    /* Function for handling the "instanceof" operator. */
    short (*instanceOf)(void*, long int);

    struct 
    {
        /* Inherited/Overridden methods from java.lang.Object */
        i0530663260_Class (*m02100232897_getClass)(i1063877011_Object);
        int (*m1164761901_hashCode)(i1063877011_Object);
        int (*m0443847483_equals)(i1063877011_Object, i1063877011_Object);
        i1063877011_Object (*m1911935377_clone)(i1063877011_Object);
        i1195259493_String (*m0295957240_toString)(i1063877011_Object);
        void (*m01409940714_notify)(i1063877011_Object);
        void (*m1261533197_notifyAll)(i1063877011_Object);
        void (*m1714276806_wait)(i1063877011_Object, long);
        void (*m01553783965_wait)(i1063877011_Object, long, int);
        void (*m1588196674_wait)(i1063877011_Object);
        void (*m1571050411_finalize)(i1063877011_Object);

        /* Constructors */
        void (*m442386431__init_)(i1063877011_Object);

    } methods;

    /* other class-specific information follows */


} PCCG_ARRAY_CLASS;

typedef struct
{
    PCCG_ARRAY_CLASS *class;
    int array_length;
    int element_size;
#if defined(sun) && defined(__GNUC__)
    void *array_data __attribute__((aligned(8)));
} PCCG_ARRAY_INSTANCE __attribute__((aligned(8)));
#else
    void *array_data;
} PCCG_ARRAY_INSTANCE;
#endif


/* FIXME: make this structure typedef
 */
typedef char  PCCG_ARRAY_char_elem;
typedef short PCCG_ARRAY_short_elem;
typedef long  PCCG_ARRAY_long_elem;
typedef float PCCG_ARRAY_float_elem;
typedef double PCCG_ARRAY_double_elem;
typedef int PCCG_ARRAY_int_elem;

typedef PCCG_ARRAY_INSTANCE *PCCG_ARRAY_INSTANCE_PTR;

extern PCCG_ARRAY_CLASS GENERIC_ARRAY_CLASS;

/* Allocate storage for a Java array using a variable number of arguments.
 * The size1, size2 etc. indicate sizes of the filled dimensions.
 */   
PCCG_ARRAY_INSTANCE_PTR pccg_array_allocate(
        PCCG_CLASS_PTR element_class, int element_size,
        int dimensions, int dimensions_to_fill, /*int size1,*/ ...); 
       
#endif 
